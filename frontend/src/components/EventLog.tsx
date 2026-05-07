import React from 'react';
import { useTranslation } from 'react-i18next';
import { useMeshStore } from '../stores/useMeshStore';
import { format } from 'date-fns';

interface EventLogProps {
  flexMode?: boolean;
}

const EventLog: React.FC<EventLogProps> = ({ flexMode = false }) => {
  const { t } = useTranslation();
  const events = useMeshStore((state) => state.events);

  const getEventColor = (severity: string) => {
    switch (severity) {
      case 'CRITICAL': return 'var(--color-status-critical)';
      case 'WARNING': return 'var(--color-status-warning)';
      default: return 'var(--color-status-ok)';
    }
  };

  return (
    <div className="glass-panel" style={{ 
      width: '100%', 
      height: flexMode ? '100%' : '250px',
      flex: flexMode ? 1 : 'none',
      display: 'flex', 
      flexDirection: 'column',
      overflow: 'hidden',
      border: flexMode ? 'none' : undefined,
      borderRadius: flexMode ? 0 : undefined
    }}>
      <div style={{ padding: 'var(--spacing-sm) var(--spacing-md)', borderBottom: '1px solid var(--color-border)', backgroundColor: 'rgba(0,0,0,0.2)' }}>
        <h3 style={{ fontSize: '1rem', margin: 0 }}>{t('system_events')}</h3>
      </div>
      
      <div style={{ flex: 1, overflowY: 'auto', padding: 'var(--spacing-sm)' }}>
        {events.length === 0 ? (
          <div style={{ color: 'var(--color-text-muted)', textAlign: 'center', marginTop: '2rem' }}>
            {t('no_recent_events')}
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            {events.map((event) => (
              <div key={event.id} style={{ display: 'flex', flexDirection: 'column', padding: '8px 4px', borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
                  <span style={{ color: 'var(--color-text-muted)', fontSize: '0.7rem', fontFamily: 'monospace' }}>
                    {format(new Date(event.created_at), 'HH:mm:ss')}
                  </span>
                  <span style={{ 
                    padding: '1px 4px', 
                    borderRadius: '3px', 
                    fontSize: '0.65rem', 
                    backgroundColor: getEventColor(event.severity),
                    color: event.severity === 'WARNING' ? '#000' : '#fff',
                    fontWeight: 'bold'
                  }}>
                    {event.severity}
                  </span>
                  <span style={{ fontSize: '0.75rem', color: 'var(--color-secondary)', fontWeight: 'bold' }}>
                    {event.node_id ? `Node ${event.node_id}` : 'System'}
                  </span>
                </div>
                <div style={{ paddingLeft: '2px' }}>
                  <div style={{ fontWeight: 'bold', fontSize: '0.8rem', color: '#e2e8f0', lineHeight: 1.2 }}>
                    {t(event.event_type) || event.event_type}
                  </div>
                  {event.message && (
                    <div style={{ fontSize: '0.7rem', color: '#9ca3af', marginTop: '3px', lineHeight: 1.2 }}>
                      {t('instruction')}: {t(event.message) || event.message}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default EventLog;
