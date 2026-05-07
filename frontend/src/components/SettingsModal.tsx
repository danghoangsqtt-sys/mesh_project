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
    clearNodes();
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
          <label style={{ display: 'block', fontSize: '0.85rem', color: '#9ca3af', marginBottom: '8px', fontWeight: 'bold' }}>{t('settings_language')}</label>
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
          <label style={{ display: 'block', fontSize: '0.85rem', color: '#9ca3af', marginBottom: '8px', fontWeight: 'bold' }}>{t('settings_system_caps')}</label>
          
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', background: '#1a1f16', borderRadius: '4px', marginBottom: '8px', border: '1px solid #4b5563' }}>
            <div>
              <div style={{ fontWeight: 'bold' }}>{t('settings_sim_mode')}</div>
              <div style={{ fontSize: '0.75rem', color: '#9ca3af' }}>{t('settings_sim_mode_desc')}</div>
            </div>
            <button 
              onClick={handleDemoToggle}
              style={{ padding: '6px 16px', background: isDemoMode ? '#ef4444' : '#10b981', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
            >
              {isDemoMode ? t('on') : t('off')}
            </button>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', background: '#1a1f16', borderRadius: '4px', border: '1px solid #4b5563', marginBottom: '8px' }}>
            <div>
              <div style={{ fontWeight: 'bold' }}>{t('settings_map_manager')}</div>
              <div style={{ fontSize: '0.75rem', color: '#9ca3af' }}>{t('settings_map_manager_desc')}</div>
            </div>
            <button 
              onClick={() => { onClose(); onOpenMapManager(); }}
              style={{ padding: '6px 12px', background: '#4b5563', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
            >
              {t('open')}
            </button>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px', background: 'rgba(239, 68, 68, 0.1)', borderRadius: '4px', border: '1px solid #ef4444' }}>
            <div>
              <div style={{ fontWeight: 'bold', color: '#fca5a5' }}>{t('settings_ai_diag')}</div>
              <div style={{ fontSize: '0.75rem', color: '#9ca3af' }}>{t('settings_ai_diag_desc')}</div>
            </div>
            <button 
              onClick={() => {
                const addEvent = useMeshStore.getState().addEvent;
                addEvent({
                  id: Date.now(),
                  node_id: 3,
                  event_type: 'ai_predict_heat_stress',
                  severity: 'CRITICAL',
                  message: 'Dấu hiệu kiệt sức do nhiệt: Nhịp tim cao trong môi trường nóng. Đề nghị nghỉ ngơi ngay.',
                  created_at: new Date().toISOString()
                });
                onClose();
              }}
              style={{ padding: '6px 12px', background: '#ef4444', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
            >
              {t('execute')}
            </button>
          </div>
        </div>

      </div>
    </div>
  );
};

export default SettingsModal;
