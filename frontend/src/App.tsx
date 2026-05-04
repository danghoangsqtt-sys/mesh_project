import React from 'react';
import { useTranslation } from 'react-i18next';
import TacticalMap from './components/TacticalMap';
import TacticalPanel from './components/TacticalPanel';
import EventLog from './components/EventLog';
import { useWebSocket } from './hooks/useWebSocket';
import { useMeshStore } from './stores/useMeshStore';

function App() {
  useWebSocket();
  const { t, i18n } = useTranslation();
  const connectionStatus = useMeshStore((state) => state.connectionStatus);

  const toggleLanguage = () => {
    i18n.changeLanguage(i18n.language === 'en' ? 'vi' : 'en');
  };

  return (
    <div className="app-container" style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <header className="glass-panel" style={{ padding: 'var(--spacing-md)', margin: 'var(--spacing-sm)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ color: 'var(--color-primary)' }}>{t('dashboard_title')}</h1>
          <p style={{ color: 'var(--color-text-muted)' }}>Raspberry Pi 5 Server Dashboard</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--spacing-sm)' }}>
          <button onClick={toggleLanguage} style={{
            background: 'none', border: '1px solid var(--color-border)', color: 'var(--color-text-main)',
            padding: '4px 12px', borderRadius: 'var(--radius-sm)', cursor: 'pointer', fontSize: '0.875rem'
          }}>
            {i18n.language === 'en' ? '🇻🇳 Tiếng Việt' : '🇬🇧 English'}
          </button>
          <div style={{
            width: 12, height: 12, borderRadius: '50%',
            backgroundColor: connectionStatus === 'connected' ? 'var(--color-status-ok)' : 
                             connectionStatus === 'connecting' ? 'var(--color-status-warning)' : 
                             'var(--color-status-critical)'
          }} />
          <span style={{ color: 'var(--color-text-main)', textTransform: 'uppercase', fontSize: '0.875rem' }}>
            {connectionStatus}
          </span>
        </div>
      </header>
      
      <main style={{ flex: 1, padding: 'var(--spacing-sm)', position: 'relative', display: 'flex', gap: 'var(--spacing-sm)', overflow: 'hidden' }}>
        {/* Left Side: Tactical Panel */}
        <div style={{ zIndex: 10 }}>
          <TacticalPanel />
        </div>

        {/* Right Side: Map + Event Log Overlay */}
        <div style={{ flex: 1, position: 'relative', display: 'flex', flexDirection: 'column' }}>
          <TacticalMap />
          
          <div style={{ position: 'absolute', bottom: 'var(--spacing-sm)', left: 'var(--spacing-sm)', right: 'var(--spacing-sm)', zIndex: 10 }}>
            <EventLog />
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;
