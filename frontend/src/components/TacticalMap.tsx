import React, { useEffect, useRef, useState } from 'react';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import { Protocol } from 'pmtiles';
import { useMeshStore } from '../stores/useMeshStore';

let protocolAdded = false;

interface TacticalMapProps {
  mapUrl?: string; 
  center?: [number, number];
  zoom?: number;
}

const TacticalMap: React.FC<TacticalMapProps> = ({ 
  mapUrl = '/maps/offline.pmtiles', 
  center = [109.1967, 12.2388],
  zoom = 15 
}) => {
  const mapContainer = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<maplibregl.Map | null>(null);
  const [mapLoaded, setMapLoaded] = useState(false);
  
  const [pathStart, setPathStart] = useState<[number, number] | null>(null);
  const [pathEnd, setPathEnd] = useState<[number, number] | null>(null);

  useEffect(() => {
    if (!protocolAdded) {
      const protocol = new Protocol();
      maplibregl.addProtocol('pmtiles', protocol.tile);
      protocolAdded = true;
    }

    if (!mapContainer.current) return;

    const map = new maplibregl.Map({
      container: mapContainer.current,
      style: {
        version: 8,
        sources: {
          'offline-map': { type: 'vector', url: `pmtiles://${mapUrl}` },
          'geofence': {
            type: 'geojson',
            data: {
              type: 'Feature',
              properties: {},
              geometry: {
                type: 'Polygon',
                coordinates: [[
                  [109.1950, 12.2370], [109.1980, 12.2370],
                  [109.1980, 12.2400], [109.1950, 12.2400],
                  [109.1950, 12.2370]
                ]]
              }
            }
          },
          'ai-path': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] }
          },
          'vision-targets': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] }
          }
        },
        layers: [
          { id: 'background', type: 'background', paint: { 'background-color': '#0f172a' } },
          {
            id: 'geofence-fill', type: 'fill', source: 'geofence',
            paint: { 'fill-color': '#ef4444', 'fill-opacity': 0.1 }
          },
          {
            id: 'geofence-line', type: 'line', source: 'geofence',
            paint: { 'line-color': '#ef4444', 'line-width': 2, 'line-dasharray': [2, 2] }
          },
          {
            id: 'ai-path-line', type: 'line', source: 'ai-path',
            paint: { 'line-color': '#0ea5e9', 'line-width': 4 }
          },
          {
            id: 'vision-targets', type: 'circle', source: 'vision-targets',
            paint: { 'circle-color': '#fbbf24', 'circle-radius': 6, 'circle-stroke-width': 2, 'circle-stroke-color': '#b45309' }
          }
        ]
      },
      center: center,
      zoom: zoom,
      attributionControl: false
    });

    let targetInterval: ReturnType<typeof setInterval>;

    map.on('load', () => {
      setMapLoaded(true);
      
      // Target polling
      targetInterval = setInterval(async () => {
        try {
          const res = await fetch('/api/vision/targets');
          if (res.ok) {
            const data = await res.json();
            const features = data.targets.map((t: any) => ({
              type: 'Feature',
              geometry: { type: 'Point', coordinates: [t.longitude, t.latitude] },
              properties: { id: t.id, class: t.class, confidence: t.confidence }
            }));
            const source = map.getSource('vision-targets') as maplibregl.GeoJSONSource;
            if (source) source.setData({ type: 'FeatureCollection', features });
          }
        } catch (e) { /* ignore */ }
      }, 5000);

      
      map.on('click', async (e) => {
        const coords: [number, number] = [e.lngLat.lng, e.lngLat.lat];
        
        // Simple state machine for path selection
        if (!pathStart || (pathStart && pathEnd)) {
          setPathStart(coords);
          setPathEnd(null);
          // Clear path
          (map.getSource('ai-path') as maplibregl.GeoJSONSource).setData({ type: 'FeatureCollection', features: [] });
        } else {
          setPathEnd(coords);
          try {
            const res = await fetch('/api/ai/pathfinding', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({
                start_lat: pathStart[1], start_lng: pathStart[0],
                end_lat: coords[1], end_lng: coords[0]
              })
            });
            if (res.ok) {
              const geojson = await res.json();
              (map.getSource('ai-path') as maplibregl.GeoJSONSource).setData(geojson);
            }
          } catch (err) {
            console.error("Pathfinding error", err);
          }
        }
      });
    });

    mapInstance.current = map;

    return () => {
      if (targetInterval) clearInterval(targetInterval);
      map.remove();
      mapInstance.current = null;
    };
  }, [mapUrl, center, zoom, pathStart, pathEnd]);

  return (
    <div 
      ref={mapContainer} 
      style={{ width: '100%', height: '100%', borderRadius: 'var(--radius-md)', overflow: 'hidden' }} 
      className="tactical-map"
    >
      {!mapLoaded && (
        <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', color: 'var(--color-text-muted)' }}>
          Initializing Tactical Map...
        </div>
      )}
      {mapLoaded && (
        <div style={{ position: 'absolute', top: 10, right: 10, zIndex: 10, backgroundColor: 'rgba(0,0,0,0.7)', padding: '8px', borderRadius: '4px', fontSize: '0.8rem' }}>
          Pathfinding: {pathStart ? (pathEnd ? 'Path drawn' : 'Select end point') : 'Select start point'}
        </div>
      )}
    </div>
  );
};

export default TacticalMap;
