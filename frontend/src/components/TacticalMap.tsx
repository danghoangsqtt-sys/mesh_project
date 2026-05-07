import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import { Protocol } from 'pmtiles';

let protocolAdded = false;

const ZONES = {
  safe: {
    id: 'safe-zone',
    coordinates: [[
      [109.1920, 12.2370], [109.1950, 12.2370],
      [109.1950, 12.2400], [109.1920, 12.2400],
      [109.1920, 12.2370]
    ]],
    center: [109.1935, 12.2385] as [number, number],
    fillColor: '#10b981',
    lineColor: '#10b981',
    fillOpacity: 0.18,
    lineWidth: 2,
    dashArray: [3, 2],
    label: 'Vùng An Toàn',
    icon: '✓',
  },
  shared: {
    id: 'shared-zone',
    coordinates: [[
      [109.1950, 12.2370], [109.1980, 12.2370],
      [109.1980, 12.2400], [109.1950, 12.2400],
      [109.1950, 12.2370]
    ]],
    center: [109.1965, 12.2385] as [number, number],
    fillColor: '#f59e0b',
    lineColor: '#f59e0b',
    fillOpacity: 0.15,
    lineWidth: 2,
    dashArray: [6, 3],
    label: 'Vùng Chung',
    icon: '◎',
  },
  danger: {
    id: 'danger-zone',
    coordinates: [[
      [109.1980, 12.2370], [109.2010, 12.2370],
      [109.2010, 12.2400], [109.1980, 12.2400],
      [109.1980, 12.2370]
    ]],
    center: [109.1995, 12.2385] as [number, number],
    fillColor: '#ef4444',
    lineColor: '#ef4444',
    fillOpacity: 0.22,
    lineWidth: 2,
    dashArray: [2, 2],
    label: 'Vùng Nguy Hiểm',
    icon: '⚠',
  },
} as const;

interface TacticalMapProps {
  mapUrl?: string;
  center?: [number, number];
  zoom?: number;
}

const TacticalMap: React.FC<TacticalMapProps> = ({
  mapUrl = '/maps/offline.pmtiles',
  center = [109.1965, 12.2388],
  zoom = 15
}) => {
  const { t } = useTranslation();
  const mapContainer = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<maplibregl.Map | null>(null);
  const markersRef = useRef<maplibregl.Marker[]>([]);
  const [mapLoaded, setMapLoaded] = useState(false);

  const pathStartRef = useRef<[number, number] | null>(null);
  const [pathStatus, setPathStatus] = useState<'idle' | 'waiting_end' | 'drawn'>('idle');

  useEffect(() => {
    if (!protocolAdded) {
      const protocol = new Protocol();
      maplibregl.addProtocol('pmtiles', protocol.tile);
      protocolAdded = true;
    }

    if (!mapContainer.current) return;

    const zoneSources: Record<string, maplibregl.SourceSpecification> = {};
    for (const zone of Object.values(ZONES)) {
      zoneSources[zone.id] = {
        type: 'geojson',
        data: {
          type: 'Feature',
          properties: { label: zone.label },
          geometry: { type: 'Polygon', coordinates: zone.coordinates as unknown as maplibregl.GeoJSON.Position[][] },
        },
      };
    }

    const zoneLayers: maplibregl.LayerSpecification[] = [];
    for (const zone of Object.values(ZONES)) {
      zoneLayers.push({
        id: `${zone.id}-fill`,
        type: 'fill',
        source: zone.id,
        paint: { 'fill-color': zone.fillColor, 'fill-opacity': zone.fillOpacity },
      });
      zoneLayers.push({
        id: `${zone.id}-line`,
        type: 'line',
        source: zone.id,
        paint: {
          'line-color': zone.lineColor,
          'line-width': zone.lineWidth,
          'line-dasharray': zone.dashArray,
        },
      });
    }

    const map = new maplibregl.Map({
      container: mapContainer.current,
      style: {
        version: 8,
        sources: {
          'offline-map': { type: 'vector', url: `pmtiles://${mapUrl}` },
          ...zoneSources,
          'ai-path': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] },
          },
          'vision-targets': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] },
          },
        },
        layers: [
          { id: 'background', type: 'background', paint: { 'background-color': '#0f172a' } },
          ...zoneLayers,
          {
            id: 'ai-path-line',
            type: 'line',
            source: 'ai-path',
            paint: { 'line-color': '#0ea5e9', 'line-width': 4 },
          },
          {
            id: 'vision-targets',
            type: 'circle',
            source: 'vision-targets',
            paint: {
              'circle-color': '#fbbf24',
              'circle-radius': 6,
              'circle-stroke-width': 2,
              'circle-stroke-color': '#b45309',
            },
          },
        ],
      },
      center: center,
      zoom: zoom,
      attributionControl: false,
    });

    let targetInterval: ReturnType<typeof setInterval>;

    map.on('load', () => {
      setMapLoaded(true);

      // Add HTML markers for zone labels
      for (const zone of Object.values(ZONES)) {
        const el = document.createElement('div');
        el.style.cssText = `
          background: rgba(0,0,0,0.75);
          color: ${zone.fillColor};
          padding: 5px 10px;
          border-radius: 5px;
          border: 1.5px solid ${zone.fillColor};
          font-size: 12px;
          font-weight: 700;
          pointer-events: none;
          white-space: nowrap;
          letter-spacing: 0.03em;
          text-shadow: 0 1px 3px rgba(0,0,0,0.8);
          display: flex;
          align-items: center;
          gap: 5px;
        `;
        el.innerHTML = `<span style="font-size:14px">${zone.icon}</span><span>${zone.label}</span>`;

        const marker = new maplibregl.Marker({ element: el, anchor: 'center' })
          .setLngLat(zone.center)
          .addTo(map);
        markersRef.current.push(marker);
      }

      // Target polling
      targetInterval = setInterval(async () => {
        try {
          const res = await fetch('/api/vision/targets');
          if (res.ok) {
            const data = await res.json();
            const features = data.targets.map((t: { id: string; longitude: number; latitude: number; class: string; confidence: number }) => ({
              type: 'Feature',
              geometry: { type: 'Point', coordinates: [t.longitude, t.latitude] },
              properties: { id: t.id, class: t.class, confidence: t.confidence },
            }));
            const source = map.getSource('vision-targets') as maplibregl.GeoJSONSource;
            if (source) source.setData({ type: 'FeatureCollection', features });
          }
        } catch { /* ignore */ }
      }, 5000);

      map.on('click', async (e) => {
        const coords: [number, number] = [e.lngLat.lng, e.lngLat.lat];
        const source = map.getSource('ai-path') as maplibregl.GeoJSONSource;

        if (!pathStartRef.current) {
          pathStartRef.current = coords;
          setPathStatus('waiting_end');
          source.setData({ type: 'FeatureCollection', features: [] });
        } else {
          const start = pathStartRef.current;
          pathStartRef.current = null;
          setPathStatus('idle');
          try {
            const res = await fetch('/api/ai/pathfinding', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({
                start_lat: start[1], start_lng: start[0],
                end_lat: coords[1], end_lng: coords[0],
              }),
            });
            if (res.ok) {
              const geojson = await res.json();
              source.setData(geojson);
              setPathStatus('drawn');
            }
          } catch (err) {
            console.error('Pathfinding error', err);
          }
        }
      });
    });

    mapInstance.current = map;

    return () => {
      if (targetInterval) clearInterval(targetInterval);
      for (const marker of markersRef.current) marker.remove();
      markersRef.current = [];
      map.remove();
      mapInstance.current = null;
    };
  }, [mapUrl, center, zoom]);

  return (
    <div className="tactical-map-wrapper">
      <div ref={mapContainer} className="tactical-map">
        {!mapLoaded && (
          <div className="tactical-map-loading">Loading Tactical Map...</div>
        )}
      </div>

      {mapLoaded && (
        <>
          <div className="tactical-map-path-status">
            {t('pathfinding')}: {pathStatus === 'drawn' ? t('path_drawn') : pathStatus === 'waiting_end' ? t('select_end') : t('select_start')}
          </div>

          <div className="tactical-map-legend">
            <div className="tactical-map-legend__title">Chú giải vùng</div>
            {Object.values(ZONES).map((zone) => (
              <div key={zone.id} className="tactical-map-legend__item">
                <span
                  className="tactical-map-legend__swatch"
                  style={{ backgroundColor: zone.fillColor, border: `1.5px solid ${zone.fillColor}` }}
                />
                <span className="tactical-map-legend__icon" style={{ color: zone.fillColor }}>{zone.icon}</span>
                <span>{zone.label}</span>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default TacticalMap;
