import React from 'react';
import { useTranslation } from 'react-i18next';
import TacticalMap from './components/TacticalMap';
import TacticalPanel from './components/TacticalPanel';
import { useWebSocket } from './hooks/useWebSocket';
import MapManagerModal from './components/MapManagerModal';
import SettingsModal from './components/SettingsModal';
import { useMeshStore } from './stores/useMeshStore';

function App() {
  useWebSocket();
  const { t } = useTranslation();
  const [isMapManagerOpen, setIsMapManagerOpen] = React.useState(false);
  const [activeTab, setActiveTab] = React.useState<'nodes'|'chat'|'logs'>('nodes');
  const [isSettingsOpen, setIsSettingsOpen] = React.useState(false);



  const [gatewayStatus, setGatewayStatus] = React.useState({ connected: false, port: 'MOCK', baudrate: 115200 });
  const [selectedPort, setSelectedPort] = React.useState('AUTO');
  const [selectedBaud, setSelectedBaud] = React.useState('115200');
  const [isConnecting, setIsConnecting] = React.useState(false);
  // Auto-collapse sidebar on narrow screens (e.g. Z Fold 3 folded = 344px)
  const [sidebarOpen, setSidebarOpen] = React.useState(window.innerWidth > 500);

  const setNodes = useMeshStore(state => state.setNodes);

  React.useEffect(() => {
    const fetchStatusAndNodes = async () => {
      // Fetch gateway status
      try {
        const res = await fetch('/api/status');
        const data = await res.json();
        setGatewayStatus(data.serial);
        if (!isConnecting) {
          setSelectedPort(data.serial.port);
          setSelectedBaud(data.serial.baudrate.toString());
        }
      } catch (err) {
        setGatewayStatus(prev => ({ ...prev, connected: false }));
      }

      // Fetch nodes (AJAX Auto-refresh fallback)
      try {
        const nodesRes = await fetch('/api/nodes');
        if (nodesRes.ok) {
          const nodesData = await nodesRes.json();
          setNodes(nodesData);
        }
      } catch (err) {
        console.error("Failed to fetch nodes", err);
      }
    };

    fetchStatusAndNodes();
    const interval = setInterval(fetchStatusAndNodes, 2000);
    return () => clearInterval(interval);
  }, [isConnecting, setNodes]);

  const handleConnectGateway = async () => {
    setIsConnecting(true);
    try {
      await fetch('/api/status/connect', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ port: selectedPort, baudrate: parseInt(selectedBaud) })
      });
      // Give it a moment to reconnect
      setTimeout(() => setIsConnecting(false), 2000);
    } catch (err) {
      console.error(err);
      setIsConnecting(false);
    }
  };


  // Auto-fullscreen on mobile upon first interaction
  React.useEffect(() => {
    const handleFirstInteraction = () => {
      try {
        const docEl = document.documentElement as any;
        const requestFS = docEl.requestFullscreen || docEl.webkitRequestFullscreen || docEl.mozRequestFullScreen || docEl.msRequestFullscreen;
        
        // Only request fullscreen if not already in fullscreen and if we are likely on a mobile device
        const isFullscreen = document.fullscreenElement || (document as any).webkitFullscreenElement || (document as any).mozFullScreenElement || (document as any).msFullscreenElement;
        
        if (requestFS && !isFullscreen && /Mobi|Android/i.test(navigator.userAgent)) {
          const promise = requestFS.call(docEl);
          if (promise && typeof promise.catch === 'function') {
            promise.catch((err: any) => console.warn(`Fullscreen API error: ${err.message}`));
          }
        }
      } catch (err) {
        console.warn("Fullscreen attempt failed:", err);
      }
      
      // Remove listeners after first interaction
      document.removeEventListener('touchstart', handleFirstInteraction);
      document.removeEventListener('click', handleFirstInteraction);
    };

    document.addEventListener('touchstart', handleFirstInteraction, { passive: true });
    document.addEventListener('click', handleFirstInteraction, { passive: true });

    return () => {
      document.removeEventListener('touchstart', handleFirstInteraction);
      document.removeEventListener('click', handleFirstInteraction);
    };
  }, []);

  return (
    <div className="app-container" style={{ position: 'fixed', top: 0, bottom: 0, left: 0, right: 0, display: 'flex', background: '#1a1f16', overflow: 'hidden' }}>
      
      {/* Left Side: Map */}
      <div style={{ flex: 1, position: 'relative', minWidth: 0 }}>
        <TacticalMap />

        {/* Sidebar toggle button — always visible, floats on map edge */}
        <button
          onClick={() => setSidebarOpen(!sidebarOpen)}
          title={sidebarOpen ? 'Thu gọn bảng điều khiển' : 'Mở bảng điều khiển'}
          style={{
            position: 'absolute',
            top: '50%',
            right: 0,
            transform: 'translateY(-50%)',
            zIndex: 30,
            background: '#3b4335',
            border: '1px solid #4d7c0f',
            borderRight: 'none',
            color: '#bef264',
            width: '22px',
            height: '60px',
            borderRadius: '6px 0 0 6px',
            cursor: 'pointer',
            fontSize: '0.9rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '-2px 0 8px rgba(0,0,0,0.4)',
          }}
        >
          {sidebarOpen ? '▶' : '◀'}
        </button>
      </div>

      {/* Right Side: ATAK-style Sidebar — collapsible, overlay on mobile */}
      <div style={{
        width: sidebarOpen ? 'min(420px, 85vw)' : '0',
        minWidth: sidebarOpen ? undefined : '0',
        overflow: sidebarOpen ? 'auto' : 'hidden',
        transition: 'width 0.25s ease',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: '#3b4335',
        borderLeft: sidebarOpen ? '1px solid #2d3328' : 'none',
        color: '#e2e8f0',
        zIndex: 25,
        boxShadow: '-5px 0 15px rgba(0,0,0,0.5)',
        flexShrink: 0,
        // On narrow screens, overlay the map
        ...(window.innerWidth <= 500 && sidebarOpen ? {
          position: 'absolute' as const,
          top: 0,
          right: 0,
          bottom: 0,
          width: '85vw',
        } : {}),
      }}>
        
        {/* Sidebar Header */}
        <div style={{ padding: '8px 12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', backgroundColor: '#2d3328', borderBottom: '1px solid #1a1f16' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '1rem' }}>⚔</span>
            <span style={{ fontWeight: 'bold', letterSpacing: '1px', fontSize: '0.9rem', textTransform: 'uppercase' }}>{t('dashboard_title')}</span>
          </div>
          <button onClick={() => setIsSettingsOpen(true)} style={{ background: 'none', border: 'none', color: '#a3b19b', cursor: 'pointer', fontSize: '1.2rem' }}>
            ⚙️
          </button>
        </div>

        {/* Gateway Connection Box */}
        <div style={{ padding: '12px 16px', borderBottom: '1px solid #2d3328', backgroundColor: 'rgba(0,0,0,0.1)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#fbbf24', display: 'flex', alignItems: 'center', gap: '6px', textTransform: 'uppercase' }}>
              ⚡ {t('gateway_conn')}
            </span>
          </div>
          
          <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
            <input 
              list="port-options"
              value={selectedPort} onChange={(e) => setSelectedPort(e.target.value)}
              style={{ flex: 1, background: '#2d3328', border: '1px solid #4b5563', borderRadius: '4px', padding: '6px 8px', color: '#fff', fontSize: '0.85rem' }}
              placeholder="AUTO, /dev/ttyUSB0"
            />
            <datalist id="port-options">
              <option value="AUTO" />
              <option value="/dev/ttyUSB0" />
              <option value="/dev/ttyACM0" />
              <option value="socket://192.168.4.1:8080" />
            </datalist>
            <span style={{ fontSize: '0.8rem', color: '#9ca3af', alignSelf: 'center' }}>Baud:</span>
            <select 
              value={selectedBaud} onChange={(e) => setSelectedBaud(e.target.value)}
              style={{ background: '#2d3328', border: '1px solid #4b5563', borderRadius: '4px', padding: '6px 8px', color: '#fff', fontSize: '0.85rem', width: '80px' }}
            >
              <option value="9600">9600</option>
              <option value="115200">115200</option>
            </select>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <button 
              onClick={handleConnectGateway}
              disabled={isConnecting}
              style={{
                background: gatewayStatus.connected ? '#ef4444' : '#10b981',
                color: '#fff', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: isConnecting ? 'wait' : 'pointer', fontWeight: 'bold', flex: 1, textTransform: 'uppercase'
              }}
            >
              {isConnecting ? '...' : gatewayStatus.connected ? 'Disconnect' : 'Connect'}
            </button>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flex: 1.5 }}>
               <div className={gatewayStatus.connected ? 'critical-pulse' : ''} style={{ width: 12, height: 12, borderRadius: '50%', backgroundColor: gatewayStatus.connected ? '#10b981' : '#6b7280' }} />
               <span style={{ fontSize: '0.85rem', color: gatewayStatus.connected ? '#10b981' : '#6b7280', fontWeight: 'bold', letterSpacing: '1px' }}>
                 {gatewayStatus.connected ? 'ONLINE' : 'OFFLINE'}
               </span>
               {gatewayStatus.connected && <span style={{ fontSize: '0.7rem', color: '#9ca3af', marginLeft: 'auto', textTransform: 'uppercase' }}>{selectedPort}</span>}
            </div>
          </div>
        </div>

        {/* Sidebar Tabs */}
        <div style={{ display: 'flex', backgroundColor: '#1a1f16', borderBottom: '1px solid #2d3328' }}>
          <button 
            onClick={() => setActiveTab('nodes')}
            style={{ flex: 1, padding: '12px 0', background: activeTab === 'nodes' ? '#3b4335' : 'transparent', color: activeTab === 'nodes' ? '#10b981' : '#9ca3af', border: 'none', borderBottom: activeTab === 'nodes' ? '2px solid #10b981' : '2px solid transparent', cursor: 'pointer', fontWeight: 'bold', fontSize: '0.85rem', textTransform: 'uppercase' }}
          >
            👥 {t('units')}
          </button>
          <button 
            onClick={() => setActiveTab('chat')}
            style={{ flex: 1, padding: '12px 0', background: activeTab === 'chat' ? '#3b4335' : 'transparent', color: activeTab === 'chat' ? '#3b82f6' : '#9ca3af', border: 'none', borderBottom: activeTab === 'chat' ? '2px solid #3b82f6' : '2px solid transparent', cursor: 'pointer', fontWeight: 'bold', fontSize: '0.85rem', textTransform: 'uppercase' }}
          >
            💬 {t('command')}
          </button>
          <button 
            onClick={() => setActiveTab('logs')}
            style={{ flex: 1, padding: '12px 0', background: activeTab === 'logs' ? '#3b4335' : 'transparent', color: activeTab === 'logs' ? '#facc15' : '#9ca3af', border: 'none', borderBottom: activeTab === 'logs' ? '2px solid #facc15' : '2px solid transparent', cursor: 'pointer', fontWeight: 'bold', fontSize: '0.85rem', textTransform: 'uppercase' }}
          >
            📋 {t('logs')}
          </button>
        </div>

        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden', minHeight: 0 }}>
          <TacticalPanel activeTab={activeTab} />
        </div>

      </div>

      {isMapManagerOpen && (
        <MapManagerModal onClose={() => setIsMapManagerOpen(false)} />
      )}
      
      {isSettingsOpen && (
        <SettingsModal 
          onClose={() => setIsSettingsOpen(false)} 
          onOpenMapManager={() => setIsMapManagerOpen(true)} 
        />
      )}
    </div>
  );
}

export default App;
