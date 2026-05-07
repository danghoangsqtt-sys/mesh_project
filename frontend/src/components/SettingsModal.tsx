import React from 'react';
import { useTranslation } from 'react-i18next';
import { useMeshStore } from '../stores/useMeshStore';

interface SettingsModalProps {
  onClose: () => void;
  onOpenMapManager: () => void;
}

const SettingsModal: React.FC<SettingsModalProps> = ({ onClose, onOpenMapManager }) => {
  const { t, i18n } = useTranslation();
  const isDemoMode = useMeshStore(state => state.isDemoMode);
  const setDemoMode = useMeshStore(state => state.setDemoMode);
  const clearNodes = useMeshStore(state => state.clearNodes);

  const handleLanguageChange = (lang: string) => {
    i18n.changeLanguage(lang);
  };

  const handleDemoToggle = () => {
    if (isDemoMode) {
      clearNodes();
    }
    setDemoMode(!isDemoMode);
  };

  return (
    <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100 }}>
      <div style={{ backgroundColor: '#2d3328', padding: '24px', borderRadius: '8px', width: '400px', maxWidth: '90%', color: '#e2e8f0', boxShadow: '0 10px 25px rgba(0,0,0,0.5)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <h2 style={{ margin: 0, color: '#facc15', display: 'flex', alignItems: 'center', gap: '8px' }}>
            ⚙️ {t('settings') || 'Settings'}
          </h2>
          <button onClick={onClose} style={{ background: 'none', border: 'none', color: '#9ca3af', cursor: 'pointer', fontSize: '1.2rem' }}>✕</button>
        </div>

        {/* Language Settings */}
        <div style={{ marginBottom: '24px' }}>
          <label style={{ display: 'block', fontSize: '0.85rem', color: '#9ca3af', marginBottom: '8px', fontWeight: 'bold' }}>LANGUAGE / NGÔN NGỮ</label>
          <div style={{ display: 'flex', gap: '8px' }}>
            <button 
              onClick={() => handleLanguageChange('en')}
              style={{ flex: 1, padding: '10px', background: i18n.language === 'en' ? '#3b82f6' : '#1a1f16', color: '#fff', border: '1px solid #4b5563', borderRadius: '4px', cursor: 'pointer' }}
            >
              🇬🇧 English
            </button>
            <button 
              onClick={() => handleLanguageChange('vi')}
              style={{ flex: 1, padding: '10px', background: i18n.language === 'vi' ? '#3b82f6' : '#1a1f16', color: '#fff', border: '1px solid #4b5563', borderRadius: '4px', cursor: 'pointer' }}
            >
              🇻🇳 Tiếng Việt
            </button>
          </div>
        </div>

        {/* System Settings */}
        <div style={{ marginBottom: '24px' }}>
          <label style={{ display: 'block', fontSize: '0.85rem', color: '#9ca3af', marginBottom: '8px', fontWeight: 'bold' }}>SYSTEM CAPABILITIES</label>
          
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', background: '#1a1f16', borderRadius: '4px', marginBottom: '8px', border: '1px solid #4b5563' }}>
            <div>
              <div style={{ fontWeight: 'bold' }}>Demo Mode</div>
              <div style={{ fontSize: '0.75rem', color: '#9ca3af' }}>Simulate fake tactical nodes for testing</div>
            </div>
            <button 
              onClick={handleDemoToggle}
              style={{ padding: '6px 16px', background: isDemoMode ? '#ef4444' : '#10b981', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
            >
              {isDemoMode ? 'ON' : 'OFF'}
            </button>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', background: '#1a1f16', borderRadius: '4px', border: '1px solid #4b5563' }}>
            <div>
              <div style={{ fontWeight: 'bold' }}>Offline Map Manager</div>
              <div style={{ fontSize: '0.75rem', color: '#9ca3af' }}>Download vector maps for offline usage</div>
            </div>
            <button 
              onClick={() => { onClose(); onOpenMapManager(); }}
              style={{ padding: '6px 12px', background: '#4b5563', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
            >
              OPEN
            </button>
          </div>
        </div>

      </div>
    </div>
  );
};

export default SettingsModal;
