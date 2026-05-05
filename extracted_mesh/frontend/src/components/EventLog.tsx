import React from 'react';
import { useTranslation } from 'react-i18next';
import { useMeshStore } from '../stores/useMeshStore';
import { format } from 'date-fns';

const EventLog: React.FC = () => {
  const { t } = useTranslation();
  const events = useMeshStore((state) => state.events);

  const serialEndRef = React.useRef<HTMLDivElement>(null);

  React.useEffect(() => {
    if (serialEndRef.current) {
      serialEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [events]);

  return (
    <div style={{ 
      width: '100%', 
      height: '100%',
      display: 'flex', 
      flexDirection: 'column',
      overflow: 'hidden'
    }}>
      <div 
        style={{ 
          padding: '8px 16px', 
          backgroundColor: '#2d3328',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          borderBottom: '1px solid #1a1f16'
        }}
      >
        <div style={{ display: 'flex', gap: '16px' }}>
          <span style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#d1d5db', textTransform: 'uppercase' }}>
            📋 {t('event_log')}
          </span>
        </div>
      </div>
      
      <div style={{ flex: 1, overflowY: 'auto', padding: '8px', backgroundColor: '#1a1f16', fontFamily: 'monospace' }}>
        {events.length === 0 ? (
          <div style={{ color: '#6b7280', textAlign: 'center', marginTop: '1rem', fontSize: '0.85rem' }}>
            {t('no_recent_events')}
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', fontSize: '0.8rem' }}>
            {events.map((event) => (
              <div key={event.id} style={{ display: 'flex', gap: '8px' }}>
                <span style={{ color: '#6b7280' }}>{format(new Date(event.created_at), 'HH:mm:ss')}</span>
                <span style={{ color: event.severity === 'CRITICAL' ? '#ef4444' : event.severity === 'WARNING' ? '#f59e0b' : '#10b981' }}>
                  {event.node_id ? `Node ${event.node_id}:` : 'System:'}
                </span>
                <span style={{ color: event.severity === 'CRITICAL' ? '#ef4444' : '#d1d5db' }}>{event.message}</span>
              </div>
            ))}
            <div ref={serialEndRef} />
          </div>
        )}
      </div>

    </div>
  );
};

export default EventLog;
