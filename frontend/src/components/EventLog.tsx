import React from 'react';
import { useTranslation } from 'react-i18next';
import { useMeshStore } from '../stores/useMeshStore';
import { format } from 'date-fns';

const EventLog: React.FC = () => {
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
      height: '250px',
      display: 'flex', 
      flexDirection: 'column',
      overflow: 'hidden'
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
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
            <tbody>
              {events.map((event) => (
                <tr key={event.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                  <td style={{ padding: '8px 4px', width: '100px', color: 'var(--color-text-muted)', whiteSpace: 'nowrap' }}>
                    {format(new Date(event.created_at), 'HH:mm:ss')}
                  </td>
                  <td style={{ padding: '8px 4px', width: '80px' }}>
                    <span style={{ 
                      padding: '2px 6px', 
                      borderRadius: '4px', 
                      fontSize: '0.75rem', 
                      backgroundColor: getEventColor(event.severity),
                      color: event.severity === 'WARNING' ? '#000' : '#fff',
                      fontWeight: 'bold'
                    }}>
                      {event.severity}
                    </span>
                  </td>
                  <td style={{ padding: '8px 4px', width: '80px', color: 'var(--color-secondary)' }}>
                    {event.node_id ? `Node ${event.node_id}` : 'System'}
                  </td>
                  <td style={{ padding: '8px 4px' }}>
                    <div style={{ fontWeight: 'bold' }}>{t(event.event_type) || event.event_type}</div>
                    {event.message && (
                      <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', marginTop: '4px' }}>
                        {t('instruction')}: {t(event.message) || event.message}
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default EventLog;
