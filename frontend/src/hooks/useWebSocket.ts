import { useEffect, useRef } from 'react';
import { useMeshStore, SoldierNode, MeshEvent } from '../stores/useMeshStore';

export const useWebSocket = (url: string = 'ws://localhost:8000/ws') => {
  const ws = useRef<WebSocket | null>(null);
  const { updateNode, addEvent, setConnectionStatus } = useMeshStore();
  
  useEffect(() => {
    // In production, connect to the host's /ws endpoint if url is not provided
    const wsUrl = url.includes('localhost') && window.location.hostname !== 'localhost' 
      ? `ws://${window.location.host}/ws` 
      : url;

    const connect = () => {
      setConnectionStatus('connecting');
      ws.current = new WebSocket(wsUrl);

      ws.current.onopen = () => {
        setConnectionStatus('connected');
      };

      ws.current.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          
          if (data.type === 'node_update') {
            updateNode(data.data as SoldierNode);
          } else if (data.type === 'EVENT' || data.type === 'event' || data.type === 'SOS' || data.type === 'GEOFENCE_BREACH') {
            // Transform to event interface
            addEvent({
              id: Date.now(), // Temporary ID until backend gives one
              node_id: data.data.node_id || null,
              event_type: data.data.event_type || data.type,
              severity: data.data.severity || 'WARNING',
              message: data.data.message || `${data.type} from Node ${data.data.node_id}`,
              created_at: new Date().toISOString()
            });
          }
        } catch (err) {
          console.error('WebSocket parsing error', err);
        }
      };

      ws.current.onclose = () => {
        setConnectionStatus('disconnected');
        // Reconnect after 3 seconds
        setTimeout(connect, 3000);
      };

      ws.current.onerror = (error) => {
        console.error('WebSocket error:', error);
        ws.current?.close();
      };
    };

    connect();

    return () => {
      if (ws.current) {
        ws.current.close();
      }
    };
  }, [url, updateNode, addEvent, setConnectionStatus]);
};
