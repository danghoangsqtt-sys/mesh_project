import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import { Protocol } from 'pmtiles';
import { layers, DARK } from '@protomaps/basemaps';
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
  const { t } = useTranslation();
  const mapContainer = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<maplibregl.Map | null>(null);
  const [isMapLoaded, setIsMapLoaded] = useState(false);
  const mapVersion = useMeshStore(state => state.mapVersion);
  const nodes = useMeshStore(state => state.nodes);
  
  const [pathStart, setPathStart] = useState<[number, number] | null>(null);
  const [pathEnd, setPathEnd] = useState<[number, number] | null>(null);
  
  const pathStartRef = useRef<[number, number] | null>(null);
  const pathEndRef = useRef<[number, number] | null>(null);

  // Sync state to refs
  useEffect(() => { pathStartRef.current = pathStart; }, [pathStart]);
  useEffect(() => { pathEndRef.current = pathEnd; }, [pathEnd]);

  // Sync nodes to map
  useEffect(() => {
    const map = mapInstance.current;
    if (!map || !isMapLoaded) return;
    
    const source = map.getSource('mesh-nodes') as maplibregl.GeoJSONSource;
    if (source) {
      const features: any[] = Object.values(nodes).map(node => ({
        type: 'Feature',
        geometry: { type: 'Point', coordinates: [node.longitude, node.latitude] },
        properties: { 
          id: node.node_id, 
          status: node.flags.critical_battery || node.flags.man_down ? 'critical' :
                  node.flags.alert || node.flags.low_battery ? 'warning' : 'safe'
        }
      }));
      source.setData({ type: 'FeatureCollection', features });
    }
  }, [nodes, isMapLoaded]);

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
        glyphs: 'https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf',
        sources: {
          'offline-map': { 
            type: 'vector', 
            url: `pmtiles://${mapUrl}`,
            attribution: '<a href="https://protomaps.com">Protomaps</a> © <a href="https://openstreetmap.org">OpenStreetMap</a>'
          },
          'mesh-nodes': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] }
          },
          'ai-path': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] }
          }
        },
        layers: [
          ...layers('offline-map', DARK),
          {
            id: 'ai-path-line', type: 'line', source: 'ai-path',
            paint: { 'line-color': '#0ea5e9', 'line-width': 4 }
          },
          {
            id: 'mesh-nodes-circle', type: 'circle', source: 'mesh-nodes',
            paint: { 
              'circle-radius': 7, 
              'circle-stroke-width': 2, 
              'circle-stroke-color': '#ffffff',
              'circle-color': [
                'match',
                ['get', 'status'],
                'critical', '#ef4444',
                'warning', '#f59e0b',
                '#10b981' // default safe
              ]
            }
          }
        ]
      },
      center: center,
      zoom: zoom,
      attributionControl: false
    });

    map.on('load', () => {
      setIsMapLoaded(true);
      map.on('click', async (e) => {
        const coords: [number, number] = [e.lngLat.lng, e.lngLat.lat];
        
        // Use refs for current state
        const currentStart = pathStartRef.current;
        const currentEnd = pathEndRef.current;
        
        if (!currentStart || (currentStart && currentEnd)) {
          setPathStart(coords);
          setPathEnd(null);
          // Clear path
          (map.getSource('ai-path') as maplibregl.GeoJSONSource).setData({ type: 'FeatureCollection', features: [] });
        } else {
          setPathEnd(coords);
          // Only fetch pathfinding if we are setting pathEnd
          try {
            const res = await fetch('/api/ai/pathfinding', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({
                start_lat: currentStart[1], start_lng: currentStart[0],
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
      map.remove();
      mapInstance.current = null;
      setIsMapLoaded(false);
    };
  }, [mapUrl, mapVersion]); // Re-init map only when mapUrl or mapVersion changes (new download)

  return (
    <div 
      ref={mapContainer} 
      style={{ width: '100%', height: '100%', borderRadius: 'var(--radius-md)', overflow: 'hidden' }} 
      className="tactical-map"
    >
      {!isMapLoaded && (
        <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', color: 'var(--color-text-muted)' }}>
          Loading Tactical Map...
        </div>
      )}
      {isMapLoaded && (
        <div style={{ position: 'absolute', top: 10, right: 10, zIndex: 10, backgroundColor: 'rgba(0,0,0,0.7)', padding: '8px', borderRadius: '4px', fontSize: '0.8rem' }}>
          {t('pathfinding')}: {pathStart ? (pathEnd ? t('path_drawn') : t('select_end')) : t('select_start')}
        </div>
      )}
    </div>
  );
};

export default TacticalMap;
