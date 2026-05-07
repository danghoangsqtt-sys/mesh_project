import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { useMeshStore } from '../stores/useMeshStore';

const AIHealthWidget: React.FC = () => {
  const { t } = useTranslation();
  const events = useMeshStore(state => state.events);
  
  // States
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [position, setPosition] = useState({ x: 20, y: window.innerHeight - 350 }); // Default bottom-left
  const [isDragging, setIsDragging] = useState(false);
  
  const dragRef = useRef<{ startX: number; startY: number; initialX: number; initialY: number } | null>(null);

  // Filter events
  const aiEvents = events
    .filter(e => e.event_type && e.event_type.startsWith('ai_predict'))
    .slice(0, 3);

  const hasCritical = aiEvents.some(e => e.severity === 'CRITICAL');
  const [pulse, setPulse] = useState(false);

  useEffect(() => {
    if (aiEvents.length > 0) {
      const interval = setInterval(() => setPulse(p => !p), hasCritical ? 500 : 1000);
      return () => clearInterval(interval);
    }
  }, [aiEvents.length, hasCritical]);

  // Keep widget on screen when resizing
  useEffect(() => {
    const handleResize = () => {
      setPosition(prev => ({
        x: Math.min(prev.x, window.innerWidth - 320),
        y: Math.min(prev.y, window.innerHeight - 100)
      }));
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  if (aiEvents.length === 0) return null;

  // Drag handlers
  const handlePointerDown = (e: React.PointerEvent) => {
    // Prevent dragging if clicking the collapse button
    if ((e.target as HTMLElement).tagName.toLowerCase() === 'button') return;
    
    (e.target as Element).setPointerCapture(e.pointerId);
    setIsDragging(true);
    dragRef.current = {
      startX: e.clientX,
      startY: e.clientY,
      initialX: position.x,
      initialY: position.y
    };
  };

  const handlePointerMove = (e: React.PointerEvent) => {
    if (!isDragging || !dragRef.current) return;
    const dx = e.clientX - dragRef.current.startX;
    const dy = e.clientY - dragRef.current.startY;
    
    // Bounds checking
    const newX = Math.max(0, Math.min(window.innerWidth - 300, dragRef.current.initialX + dx));
    const newY = Math.max(0, Math.min(window.innerHeight - 40, dragRef.current.initialY + dy));
    
    setPosition({ x: newX, y: newY });
  };

  const handlePointerUp = (e: React.PointerEvent) => {
    (e.target as Element).releasePointerCapture(e.pointerId);
    setIsDragging(false);
    dragRef.current = null;
  };

  return (
    <div style={{
      position: 'absolute',
      top: position.y,
      left: position.x,
      zIndex: 50,
      background: 'rgba(15, 23, 42, 0.85)',
      backdropFilter: 'blur(8px)',
      border: `1px solid ${hasCritical ? '#ef4444' : '#f59e0b'}`,
      borderRadius: '8px',
      boxShadow: `0 0 15px ${hasCritical ? 'rgba(239, 68, 68, 0.3)' : 'rgba(245, 158, 11, 0.3)'}`,
      width: '300px',
      overflow: 'hidden',
      color: '#e2e8f0',
      fontFamily: 'monospace',
      transition: isDragging ? 'none' : 'box-shadow 0.3s'
    }}>
      {/* Header / Drag Handle */}
      <div 
        onPointerDown={handlePointerDown}
        onPointerMove={handlePointerMove}
        onPointerUp={handlePointerUp}
        style={{
          background: hasCritical ? 'rgba(239, 68, 68, 0.2)' : 'rgba(245, 158, 11, 0.2)',
          padding: '8px 12px',
          display: 'flex',
          alignItems: 'center',
          borderBottom: isCollapsed ? 'none' : `1px solid ${hasCritical ? '#ef4444' : '#f59e0b'}`,
          cursor: isDragging ? 'grabbing' : 'grab',
          userSelect: 'none',
          touchAction: 'none' // Prevent scrolling on mobile while dragging
        }}
      >
        <span style={{ 
          fontSize: '1.2rem', 
          marginRight: '8px',
          opacity: pulse ? 1 : 0.5,
          transition: 'opacity 0.3s ease'
        }}>
          🧠
        </span>
        <span style={{ 
          fontWeight: 'bold', 
          textTransform: 'uppercase',
          letterSpacing: '1px',
          color: hasCritical ? '#fca5a5' : '#fcd34d',
          fontSize: '0.85rem'
        }}>
          AI Tactical Assistant
        </span>
        
        {/* Collapse Button */}
        <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '0.65rem', color: '#94a3b8', background: '#1e293b', padding: '2px 4px', borderRadius: '4px' }}>LIVE</span>
          <button 
            onClick={() => setIsCollapsed(!isCollapsed)}
            style={{ 
              background: 'transparent', border: 'none', color: '#cbd5e1', cursor: 'pointer',
              padding: '0 4px', fontSize: '1rem', display: 'flex', alignItems: 'center'
            }}
            title={isCollapsed ? "Mở rộng" : "Thu gọn"}
          >
            {isCollapsed ? '▼' : '▲'}
          </button>
        </div>
      </div>

      {/* Body */}
      {!isCollapsed && (
        <div style={{ padding: '8px' }}>
          {aiEvents.map(event => (
            <div key={event.id} style={{ 
              marginBottom: '8px', 
              padding: '6px', 
              background: 'rgba(0,0,0,0.3)',
              borderRadius: '4px',
              borderLeft: `3px solid ${event.severity === 'CRITICAL' ? '#ef4444' : '#f59e0b'}`
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                <span style={{ fontWeight: 'bold', color: '#fff', fontSize: '0.8rem' }}>
                  Node {event.node_id}
                </span>
                <span style={{ fontSize: '0.7rem', color: '#94a3b8' }}>
                  {new Date(event.created_at).toLocaleTimeString()}
                </span>
              </div>
              <div style={{ color: event.severity === 'CRITICAL' ? '#fca5a5' : '#fcd34d', fontSize: '0.75rem', marginBottom: '2px', fontWeight: 'bold' }}>
                {t(event.event_type)}
              </div>
              {event.message && (
                <div style={{ fontSize: '0.7rem', color: '#cbd5e1', fontStyle: 'italic', lineHeight: '1.3' }}>
                  ↳ {t(event.message)}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AIHealthWidget;
