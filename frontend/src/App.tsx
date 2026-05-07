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


  return (
    <div className="app-container" style={{ position: 'fixed', top: 0, bottom: 0, left: 0, right: 0, display: 'flex', background: '#1a1f16', overflow: 'hidden' }}>
      
      {/* Left Side: Map */}
      <div style={{ flex: 1, position: 'relative' }}>
        <TacticalMap />
      </div>

      {/* Right Side: ATAK-style Sidebar */}
      <div style={{ width: '420px', display: 'flex', flexDirection: 'column', backgroundColor: '#3b4335', borderLeft: '1px solid #2d3328', color: '#e2e8f0', zIndex: 20, boxShadow: '-5px 0 15px rgba(0,0,0,0.5)' }}>
        
        {/* Sidebar Header */}
        <div style={{ padding: '12px 16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', backgroundColor: '#2d3328', borderBottom: '1px solid #1a1f16' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontSize: '1.2rem' }}>⚔</span>
            <span style={{ fontWeight: 'bold', letterSpacing: '1px', fontSize: '1.1rem', textTransform: 'uppercase' }}>{t('dashboard_title')}</span>
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
