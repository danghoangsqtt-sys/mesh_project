import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import { Protocol } from 'pmtiles';
import { layers, LIGHT } from '@protomaps/basemaps';
import MapboxDraw from '@mapbox/mapbox-gl-draw';
import '@mapbox/mapbox-gl-draw/dist/mapbox-gl-draw.css';
import { useMeshStore } from '../stores/useMeshStore';

// Tactical multi-color layer transformer
// Replaces the near-white Protomaps LIGHT palette with high-contrast tactical colors
const getCustomLayers = () => {
  const baseLayers = layers('offline-map', LIGHT, { lang: 'vi' });
  
  const modifiedLayers = baseLayers.map((layer: any) => {
    const id = layer.id || '';
    const type = layer.type || '';
    const sl = layer['source-layer'] || '';
    const paintCopy = { ...layer.paint };
    const layoutCopy = { ...layer.layout };
    let changed = false;

    // ── Fix "arrow" icon-image (causes console error) ──
    if (layoutCopy['icon-image']) {
      const iconStr = JSON.stringify(layoutCopy['icon-image']);
      if (iconStr.includes('arrow')) {
        delete layoutCopy['icon-image'];
        changed = true;
      }
    }

    // ── WATER (fill + line) ──
    if (sl === 'water' || id.includes('water')) {
      if (type === 'fill') {
        paintCopy['fill-color'] = '#0ea5e9';
        paintCopy['fill-opacity'] = 0.6;
      } else if (type === 'line') {
        paintCopy['line-color'] = '#0284c7';
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ── NATURAL / PARKS / LANDCOVER (green zones) ──
    if (sl === 'natural' || sl === 'landcover' || sl === 'landuse' ||
        id.includes('natural') || id.includes('park') || id.includes('landuse') || id.includes('landcover')) {
      if (type === 'fill') {
        paintCopy['fill-color'] = '#22c55e';
        paintCopy['fill-opacity'] = 0.25;
      } else if (type === 'line') {
        paintCopy['line-color'] = '#16a34a';
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ── EARTH (base land) ──
    if (sl === 'earth' || id.includes('earth')) {
      if (type === 'fill') {
        paintCopy['fill-color'] = '#e2e8f0';
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ── ROADS — color by class for tactical clarity ──
    if (sl === 'roads' || id.includes('roads') || id.includes('road')) {
      if (type === 'line') {
        // Highway = bright yellow-orange
        if (id.includes('highway')) {
          paintCopy['line-color'] = id.includes('casing') ? '#b45309' : '#f59e0b';
        }
        // Major roads = warm orange  
        else if (id.includes('major')) {
          paintCopy['line-color'] = id.includes('casing') ? '#9a3412' : '#ea580c';
        }
        // Minor roads / links = slate gray
        else if (id.includes('minor') || id.includes('link') || id.includes('other') || id.includes('service')) {
          paintCopy['line-color'] = id.includes('casing') ? '#334155' : '#64748b';
        }
        // Rail = steel blue dashed
        else if (id.includes('rail')) {
          paintCopy['line-color'] = '#6366f1';
        }
        // Bridges = slightly lighter
        else if (id.includes('bridge')) {
          paintCopy['line-color'] = id.includes('casing') ? '#475569' : '#94a3b8';
        }
        // Tunnels = dashed gray
        else if (id.includes('tunnel')) {
          paintCopy['line-color'] = '#94a3b8';
        }
        // Default road catch-all
        else {
          paintCopy['line-color'] = '#64748b';
        }
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ── BOUNDARIES ──
    if (sl === 'boundaries' || id.includes('boundaries') || id.includes('boundary')) {
      if (type === 'line') {
        paintCopy['line-color'] = '#ef4444';
        paintCopy['line-width'] = id.includes('country') ? 1.5 : 0.8;
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ── TRANSIT ──
    if (sl === 'transit' || id.includes('transit')) {
      if (type === 'line') {
        paintCopy['line-color'] = '#8b5cf6';
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ── BUILDINGS (hide 2D flat, we add 3D below) ──
    if (id === 'buildings' || (sl === 'buildings' && type === 'fill')) {
      return { ...layer, layout: { ...layoutCopy, visibility: 'none' } };
    }

    // ── LABELS — ensure font fallback ──
    if (type === 'symbol' && layoutCopy['text-field']) {
      if (!layoutCopy['text-font'] || (Array.isArray(layoutCopy['text-font']) && layoutCopy['text-font'].some((f: string) => f.includes('Open Sans') || f.includes('Arial Unicode')))) {
        layoutCopy['text-font'] = ['Noto Sans Regular'];
      }
    }

    // Return with any arrow-icon fix applied
    if (changed) {
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }
    return layer;
  });

  // Add 3D Extruded Buildings
  modifiedLayers.push({
    id: 'buildings-3d',
    type: 'fill-extrusion',
    source: 'offline-map',
    'source-layer': 'buildings',
    paint: {
      'fill-extrusion-color': '#9ca3af',
      'fill-extrusion-height': ['coalesce', ['get', 'height'], 8],
      'fill-extrusion-base': ['coalesce', ['get', 'min_height'], 0],
      'fill-extrusion-opacity': 0.8
    }
  });

  return modifiedLayers;
};

let protocolAdded = false;

interface TacticalMapProps {
  mapUrl?: string; 
  center?: [number, number];
  zoom?: number;
}

const TacticalMap: React.FC<TacticalMapProps> = ({ 
  mapUrl = '/maps/offline.pmtiles', 
  center = [109.1967, 12.2388],
  zoom = 14 
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
  
  const [selectedGraphic, setSelectedGraphic] = useState<any>(null);
  const [graphicType, setGraphicType] = useState<string>('POI');
  const [activeMode, setActiveMode] = useState<string | null>(null);
  const [showSatellite, setShowSatellite] = useState(true);
  const [selectedMarkerIcon, setSelectedMarkerIcon] = useState<string>('pin-soldier');
  const drawRef = useRef<any>(null);
  const selectedMarkerIconRef = useRef<string>('pin-soldier');
  const graphicTypeRef = useRef<string>('POI');
  const updateMarkerOverlayRef = useRef<() => void>(() => {});

  // Sync state to refs
  useEffect(() => { pathStartRef.current = pathStart; }, [pathStart]);
  useEffect(() => { pathEndRef.current = pathEnd; }, [pathEnd]);
  useEffect(() => { selectedMarkerIconRef.current = selectedMarkerIcon; }, [selectedMarkerIcon]);
  useEffect(() => { graphicTypeRef.current = graphicType; }, [graphicType]);

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
        // Use local font fallback to avoid 404 on missing PBF glyphs
        glyphs: 'https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf',
        sources: {
          'satellite': {
            type: 'raster',
            url: 'pmtiles:///maps/satellite.pmtiles',
            tileSize: 256
          },
          'terrain-source': {
            type: 'raster-dem',
            url: 'pmtiles:///maps/terrain.pmtiles',
            tileSize: 256,
            encoding: 'terrarium'
          },
          'offline-map': { 
            type: 'vector', 
            url: `pmtiles://${mapUrl}`,
            attribution: '<a href="https://protomaps.com">Protomaps</a> &copy; <a href="https://openstreetmap.org">OpenStreetMap</a>'
          },
          'mesh-nodes': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] }
          },
          'ai-path': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] }
          },
          'tactical-markers': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] }
          }
        },
        terrain: {
          source: 'terrain-source',
          exaggeration: 1.5 // Emphasize mountains for tactical planning
        },
        layers: [
          // Satellite imagery as base layer
          {
            id: 'satellite-layer',
            type: 'raster',
            source: 'satellite',
            paint: { 'raster-opacity': 1 } // Managed via showSatellite toggle
          },
          ...getCustomLayers(), // Custom Multi-color Vector overlay & 3D buildings
          {
            id: 'ai-path-line', type: 'line', source: 'ai-path',
            paint: { 'line-color': '#0ea5e9', 'line-width': 4 }
          },
          {
            id: 'mesh-nodes-circle', type: 'circle', source: 'mesh-nodes',
            paint: { 
              'circle-radius': 10, 
              'circle-stroke-width': 3, 
              'circle-stroke-color': '#ffffff',
              'circle-color': [
                'match',
                ['get', 'status'],
                'critical', '#ef4444',
                'warning', '#f59e0b',
                '#10b981' // default safe
              ],
              'circle-opacity': 1
            }
          },
          {
            id: 'mesh-nodes-label', type: 'symbol', source: 'mesh-nodes',
            layout: {
              'text-field': ['concat', 'NODE ', ['to-string', ['get', 'id']]],
              'text-size': 13,
              'text-offset': [0, 1.5],
              'text-anchor': 'top',
              'text-font': ['Noto Sans Bold']
            },
            paint: {
              'text-color': '#ffffff',
              'text-halo-color': '#0f172a',
              'text-halo-width': 2
            }
          },
          // ── Tactical pin marker icons (Points) ──
          {
            id: 'tactical-markers-icons',
            type: 'symbol',
            source: 'tactical-markers',
            filter: ['!=', ['get', 'isZone'], true],
            layout: {
              'icon-image': ['get', 'icon'],
              'icon-size': 0.8,
              'icon-anchor': 'bottom',
              'icon-allow-overlap': true,
              'text-field': ['get', 'label'],
              'text-size': 11,
              'text-offset': [0, 0.5],
              'text-anchor': 'top',
              'text-font': ['Noto Sans Bold'],
              'text-optional': true
            },
            paint: {
              'text-color': '#ffffff',
              'text-halo-color': '#0f172a',
              'text-halo-width': 1.5
            }
          },
          // Zone labels removed — zone feature disabled
        ]
      },
      center: center,
      zoom: zoom,
      attributionControl: false
    });

    map.on('load', () => {
      setIsMapLoaded(true);

      // ── PIN MARKER ICON DEFINITIONS ──
      // Each icon is a teardrop/pin shape with a symbol inside
      const PIN_W = 48;
      const PIN_H = 64;
      const pinMarkers: Record<string, { color: string; emoji: string; label: string }> = {
        'pin-soldier':   { color: '#22c55e', emoji: '🎖', label: 'Binh sĩ' },
        'pin-flag':      { color: '#ef4444', emoji: '🚩', label: 'Cờ hiệu' },
        'pin-crosshair': { color: '#f59e0b', emoji: '🎯', label: 'Mục tiêu' },
        'pin-fire':      { color: '#f97316', emoji: '🔥', label: 'Cháy' },
        'pin-medical':   { color: '#ec4899', emoji: '➕', label: 'Y tế' },
        'pin-home':      { color: '#8b5cf6', emoji: '🏠', label: 'Căn cứ' },
        'pin-vehicle':   { color: '#06b6d4', emoji: '🚗', label: 'Xe cộ' },
        'pin-helicopter': { color: '#3b82f6', emoji: '🚁', label: 'Trực thăng' },
        'pin-warning':   { color: '#eab308', emoji: '⚠', label: 'Cảnh báo' },
        'pin-star':      { color: '#a855f7', emoji: '⭐', label: 'Quan trọng' },
        'pin-eye':       { color: '#14b8a6', emoji: '👁', label: 'Quan sát' },
        'pin-camp':      { color: '#84cc16', emoji: '⛺', label: 'Trại' },
      };

      // Render each pin marker to canvas and register with map
      Object.entries(pinMarkers).forEach(([name, cfg]) => {
        const canvas = document.createElement('canvas');
        canvas.width = PIN_W;
        canvas.height = PIN_H;
        const ctx = canvas.getContext('2d')!;

        // Draw teardrop/pin shape
        ctx.beginPath();
        ctx.moveTo(PIN_W / 2, PIN_H - 4);      // bottom point
        ctx.bezierCurveTo(PIN_W / 2 - 4, PIN_H - 16, 2, PIN_H / 2, 2, PIN_H / 2 - 8);  // left curve
        ctx.arc(PIN_W / 2, PIN_H / 2 - 8, PIN_W / 2 - 2, Math.PI, 0);                   // top circle
        ctx.bezierCurveTo(PIN_W - 2, PIN_H / 2, PIN_W / 2 + 4, PIN_H - 16, PIN_W / 2, PIN_H - 4); // right curve
        ctx.closePath();
        ctx.fillStyle = cfg.color;
        ctx.fill();
        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = 2.5;
        ctx.stroke();

        // Drop shadow effect
        ctx.shadowColor = 'rgba(0,0,0,0.3)';
        ctx.shadowBlur = 4;
        ctx.shadowOffsetY = 2;

        // White circle inside the pin head
        ctx.beginPath();
        ctx.arc(PIN_W / 2, PIN_H / 2 - 8, 13, 0, Math.PI * 2);
        ctx.fillStyle = '#ffffff';
        ctx.shadowColor = 'transparent';
        ctx.fill();

        // Emoji/symbol in the center
        ctx.font = '18px sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillStyle = cfg.color;
        ctx.fillText(cfg.emoji, PIN_W / 2, PIN_H / 2 - 8);

        map.addImage(name, { width: PIN_W, height: PIN_H, data: ctx.getImageData(0, 0, PIN_W, PIN_H).data });
      });

      // Store pin marker definitions on map for later reference
      (map as any)._pinMarkers = pinMarkers;

      const draw = new MapboxDraw({
        displayControlsDefault: false,
        styles: [
          // Polygon styles removed — zone feature disabled
          // Polygon stroke styles removed — zone feature disabled
          // ── LINE ──
          {
            'id': 'gl-draw-line-active',
            'type': 'line',
            'filter': ['all', ['==', '$type', 'LineString'], ['==', 'active', 'true']],
            'paint': { 'line-color': '#f59e0b', 'line-width': 4, 'line-dasharray': [2, 1] }
          },
          {
            'id': 'gl-draw-line-inactive',
            'type': 'line',
            'filter': ['all', ['==', '$type', 'LineString'], ['==', 'active', 'false']],
            'paint': { 'line-color': '#3b82f6', 'line-width': 3 }
          },
          // ── POINT — Use pin icon via symbol layer ──
          {
            'id': 'gl-draw-point-active',
            'type': 'circle',
            'filter': ['all', ['==', '$type', 'Point'], ['==', 'meta', 'feature'], ['==', 'active', 'true']],
            'paint': {
              'circle-radius': 12,
              'circle-color': '#f59e0b',
              'circle-stroke-width': 3,
              'circle-stroke-color': '#ffffff'
            }
          },
          {
            'id': 'gl-draw-point-inactive',
            'type': 'circle',
            'filter': ['all', ['==', '$type', 'Point'], ['==', 'meta', 'feature'], ['==', 'active', 'false']],
            'paint': {
              'circle-radius': 10,
              'circle-color': '#ef4444',
              'circle-stroke-width': 3,
              'circle-stroke-color': '#ffffff'
            }
          },
          // ── VERTEX handles ──
          {
            'id': 'gl-draw-vertex-active',
            'type': 'circle',
            'filter': ['all', ['==', '$type', 'Point'], ['==', 'meta', 'vertex']],
            'paint': { 'circle-radius': 6, 'circle-color': '#fff', 'circle-stroke-width': 2, 'circle-stroke-color': '#f59e0b' }
          },
          {
            'id': 'gl-draw-midpoint',
            'type': 'circle',
            'filter': ['all', ['==', '$type', 'Point'], ['==', 'meta', 'midpoint']],
            'paint': { 'circle-radius': 4, 'circle-color': '#f59e0b' }
          }
        ]
      });
      map.addControl(draw as any, 'bottom-left');
      drawRef.current = draw;
      
      map.on('draw.modechange', (e: any) => {
          setActiveMode(e.mode);
      });

      // Load graphics from backend
      fetch('/api/tactical').then(res => res.json()).then(data => {
         data.forEach((d: any) => {
             const feat = JSON.parse(d.geojson_data);
             feat.id = d.id; // ensure ID matches DB
             draw.add(feat);
         });
      }).catch(err => console.error('Failed to load graphics', err));

      const syncGraphic = async (feature: any) => {
          // feature passed from draw.create lacks updated properties applied just before this call.
          // Get the latest feature directly from MapboxDraw store.
          const latestFeature = drawRef.current?.get(feature.id) || feature;
          const payload = {
              graphic_type: latestFeature.geometry.type,
              name: 'Graphic',
              color: '#ef4444',
              geojson_data: JSON.stringify(latestFeature)
          };
          
          if (typeof feature.id === 'number') {
              // Not doing PUT in MVP, just add new
          } else {
              try {
                  const res = await fetch('/api/tactical', {
                      method: 'POST',
                      headers: { 'Content-Type': 'application/json' },
                      body: JSON.stringify(payload)
                  });
                  const saved = await res.json();
                  drawRef.current?.delete(latestFeature.id);
                  latestFeature.id = saved.id;
                  drawRef.current?.add(latestFeature);
              } catch (err) {}
          }
      };

      // ── Helper: sync draw point features → tactical-markers overlay ──
      const updateMarkerOverlay = () => {
        const allFeatures = draw.getAll();
        const overlayFeatures: any[] = [];
        
        allFeatures.features.forEach((f: any) => {
          // MapboxDraw stores user properties with 'user_' prefix internally,
          // but getAll() returns them WITHOUT prefix. Read both just in case.
          const featureType = f.properties?.type || f.properties?.user_type || '';

          if (f.geometry.type === 'Point') {
            overlayFeatures.push({
              type: 'Feature',
              geometry: f.geometry,
              properties: {
                icon: f.properties?.['marker-icon'] || f.properties?.['user_marker-icon'] || 'pin-soldier',
                label: featureType || ''
              }
            });
          }
          // Polygon/zone overlay removed — zone feature disabled
        });
        
        const src = map.getSource('tactical-markers') as maplibregl.GeoJSONSource;
        if (src) {
          src.setData({ type: 'FeatureCollection', features: overlayFeatures });
        }
      };
      updateMarkerOverlayRef.current = updateMarkerOverlay;

      map.on('draw.create', (e: any) => {
         e.features.forEach((feat: any) => {
           if (feat.geometry.type === 'Point') {
             drawRef.current?.setFeatureProperty(feat.id, 'marker-icon', selectedMarkerIconRef.current);
           }
           syncGraphic(feat);
         });
         // Force MapboxDraw to re-evaluate style expressions after property changes
         drawRef.current?.changeMode('simple_select');
         updateMarkerOverlay();
      });
      
      map.on('draw.delete', async (e: any) => {
         for (const feature of e.features) {
             if (typeof feature.id === 'number') {
                 await fetch(`/api/tactical/${feature.id}`, { method: 'DELETE' });
             }
         }
         updateMarkerOverlay();
      });

      map.on('draw.selectionchange', (e: any) => {
         if (e.features.length > 0) {
             const feat = e.features[0];
             setSelectedGraphic(feat);
             setGraphicType(feat.properties?.type || (feat.geometry.type === 'Point' ? 'LZ' : ''));
         } else {
             setSelectedGraphic(null);
         }
         updateMarkerOverlay();
      });

      // Initial sync after loading from backend
      setTimeout(updateMarkerOverlay, 2000);

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
      style={{ width: '100%', height: '100%', borderRadius: 'var(--radius-md)', overflow: 'hidden', cursor: activeMode?.startsWith('draw_') ? 'crosshair' : 'inherit' }} 
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

      {/* Satellite Toggle Button */}
      {isMapLoaded && (
        <button
          onClick={() => {
            const map = mapInstance.current;
            if (!map) return;
            const newState = !showSatellite;
            setShowSatellite(newState);
            const layer = map.getLayer('satellite-layer');
            if (layer) {
              map.setLayoutProperty('satellite-layer', 'visibility', newState ? 'visible' : 'none');
            }
          }}
          title={t('toggle_satellite')}
          style={{
            position: 'absolute',
            top: 50,
            right: 10,
            zIndex: 10,
            backgroundColor: showSatellite ? '#16a34a' : 'rgba(0,0,0,0.7)',
            color: '#fff',
            border: showSatellite ? '2px solid #22c55e' : '1px solid #555',
            padding: '8px 12px',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '0.75rem',
            fontWeight: 'bold',
            transition: 'all 0.2s',
            boxShadow: '0 2px 4px rgba(0,0,0,0.5)'
          }}
        >
          {showSatellite ? 'SAT ON' : 'SAT OFF'}
        </button>
      )}

      {/* 3D View Toggle Button */}
      {isMapLoaded && (
        <button
          onClick={() => {
            const map = mapInstance.current;
            if (!map) return;
            const currentPitch = map.getPitch();
            if (currentPitch < 45) {
              // Switch to 3D perspective
              map.easeTo({ pitch: 60, bearing: 45, duration: 1500 });
            } else {
              // Switch to 2D flat perspective
              map.easeTo({ pitch: 0, bearing: 0, duration: 1500 });
            }
          }}
          title={t('toggle_3d')}
          style={{
            position: 'absolute',
            top: 90,
            right: 10,
            zIndex: 10,
            backgroundColor: 'rgba(15,23,42,0.8)',
            color: '#bef264',
            border: '1px solid #4d7c0f',
            padding: '8px 12px',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '0.75rem',
            fontWeight: 'bold',
            transition: 'all 0.2s',
            boxShadow: '0 2px 4px rgba(0,0,0,0.5)'
          }}
        >
          3D VIEW
        </button>
      )}
      
      {/* Custom Drawing Toolbar - Bottom Left - Military Style */}
      {isMapLoaded && (
         <div style={{ position: 'absolute', bottom: 20, left: 10, zIndex: 10, display: 'flex', flexDirection: 'column', gap: '6px', width: 'min(220px, calc(100vw - 60px))' }}>
            {activeMode && activeMode.startsWith('draw_') && (
               <div style={{ background: 'rgba(234, 179, 8, 0.9)', color: '#000', padding: '6px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold', textAlign: 'center', marginBottom: '4px', border: '1px solid #ca8a04' }}>
                  {t('click_to_draw')}
               </div>
            )}
            
            <div style={{ background: 'rgba(15, 23, 42, 0.85)', border: '1px solid #3f6212', borderRadius: '6px', overflow: 'hidden', backdropFilter: 'blur(4px)' }}>
                <div style={{ background: '#3f6212', color: '#fff', fontSize: '0.7rem', padding: '4px 8px', fontWeight: 'bold', letterSpacing: '1px' }}>
                    {t('tactical_tools')}
                </div>
                <div style={{ padding: '8px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                    {/* Icon Picker Grid - shown when point mode is active */}
                    {activeMode === 'draw_point' && (
                      <div style={{ marginBottom: '4px' }}>
                        <div style={{ fontSize: '0.65rem', color: '#94a3b8', marginBottom: '4px', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Chọn biểu tượng:</div>
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: '4px' }}>
                          {[
                            { id: 'pin-soldier', emoji: '🎖' },
                            { id: 'pin-flag', emoji: '🚩' },
                            { id: 'pin-crosshair', emoji: '🎯' },
                            { id: 'pin-fire', emoji: '🔥' },
                            { id: 'pin-medical', emoji: '➕' },
                            { id: 'pin-home', emoji: '🏠' },
                            { id: 'pin-vehicle', emoji: '🚗' },
                            { id: 'pin-helicopter', emoji: '🚁' },
                            { id: 'pin-warning', emoji: '⚠' },
                            { id: 'pin-star', emoji: '⭐' },
                            { id: 'pin-eye', emoji: '👁' },
                            { id: 'pin-camp', emoji: '⛺' },
                          ].map(pin => (
                            <button
                              key={pin.id}
                              onClick={() => { setSelectedMarkerIcon(pin.id); }}
                              style={{
                                width: '36px',
                                height: '36px',
                                border: selectedMarkerIcon === pin.id ? '2px solid #22c55e' : '1px solid #475569',
                                borderRadius: '6px',
                                background: selectedMarkerIcon === pin.id ? 'rgba(34,197,94,0.2)' : 'rgba(30,41,59,0.8)',
                                cursor: 'pointer',
                                fontSize: '18px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                transition: 'all 0.15s',
                                padding: 0
                              }}
                              title={pin.id.replace('pin-', '')}
                            >
                              {pin.emoji}
                            </button>
                          ))}
                        </div>
                      </div>
                    )}
                    <button 
                       onClick={() => { drawRef.current?.changeMode('draw_point'); setActiveMode('draw_point'); }}
                       style={{ 
                           background: activeMode === 'draw_point' ? '#4d7c0f' : 'transparent', 
                           color: activeMode === 'draw_point' ? '#fff' : '#a3a8b4', 
                           border: '1px solid #4d7c0f', padding: '8px 12px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem', fontWeight: 'bold', textAlign: 'left', transition: 'all 0.2s' 
                       }}
                    >
                       📌 {t('draw_point')}
                    </button>
                    <button 
                       onClick={async () => {
                         const draw = drawRef.current;
                         if (!draw) return;
                         if (selectedGraphic?.id) {
                           if (typeof selectedGraphic.id === 'number') {
                             try { await fetch(`/api/tactical/${selectedGraphic.id}`, { method: 'DELETE' }); } catch (e) {}
                           }
                           draw.delete(String(selectedGraphic.id));
                           setSelectedGraphic(null);
                         } else {
                           draw.trash();
                         }
                         setActiveMode('simple_select');
                         updateMarkerOverlayRef.current();
                       }}
                       style={{ 
                           background: 'transparent', color: '#ef4444', border: '1px solid #991b1b', padding: '6px 10px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.75rem', fontWeight: 'bold', textAlign: 'left' 
                       }}
                    >
                       ❌ {t('delete_selected')}
                    </button>
                </div>
            </div>
         </div>
      )}

      {isMapLoaded && selectedGraphic && selectedGraphic.geometry.type === 'Point' && (
        <div style={{ position: 'absolute', top: 10, left: 10, zIndex: 10, backgroundColor: 'rgba(15,23,42,0.9)', padding: '12px', borderRadius: '6px', color: '#f8fafc', border: '1px solid #4d7c0f', minWidth: '220px', maxWidth: 'min(280px, 80vw)', boxShadow: '0 4px 12px rgba(0,0,0,0.5)', backdropFilter: 'blur(4px)' }}>
          <h4 style={{ margin: '0 0 6px 0', fontSize: '0.75rem', color: '#bef264', textTransform: 'uppercase', letterSpacing: '1px' }}>
             {t('selected_point')}
          </h4>
          <div style={{ marginBottom: '6px' }}>
             <select 
                value={selectedGraphic.properties?.type || selectedGraphic.properties?.user_type || graphicType}
                onChange={(e) => {
                   const newType = e.target.value;
                   setGraphicType(newType);
                   const draw = drawRef.current;
                   if (draw && selectedGraphic?.id) {
                       draw.setFeatureProperty(selectedGraphic.id, 'type', newType);
                       updateMarkerOverlayRef.current();
                   }
                }}
                style={{ width: '100%', padding: '4px', background: '#1e293b', color: '#f8fafc', border: '1px solid #475569', borderRadius: '4px', fontSize: '0.75rem' }}
             >
                <option value="LZ">{t('lz')}</option>
                <option value="EX">{t('ex')}</option>
                <option value="RV">{t('rv')}</option>
                <option value="BASE">{t('base')}</option>
                <option value="POI">{t('poi')}</option>
             </select>
          </div>
          <p style={{ margin: '0 0 6px 0', fontSize: '0.75rem', color: '#9ca3af', wordBreak: 'break-all' }}>
            {`${selectedGraphic.geometry.coordinates[1].toFixed(5)}, ${selectedGraphic.geometry.coordinates[0].toFixed(5)}`}
          </p>
          <div style={{ display: 'flex', gap: '8px' }}>
            <button 
              onClick={() => {
                  const coordText = `${selectedGraphic.geometry.coordinates[1].toFixed(5)}, ${selectedGraphic.geometry.coordinates[0].toFixed(5)}`;
                  const textToHex = (text: string) => text.split('').map(c => c.charCodeAt(0).toString(16).padStart(2, '0')).join('');
                  const payload = { target_node_id: 65535, command_type: 'BROADCAST', payload_hex: textToHex(`[${graphicType}] ${coordText}`) };
                  fetch('/api/commands', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
              }}
              style={{ background: '#3b82f6', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem', flex: 1 }}
            >
              {t('broadcast_coord')}
            </button>
            <button 
               onClick={() => {
                  const val = `${selectedGraphic.geometry.coordinates[1].toFixed(5)}, ${selectedGraphic.geometry.coordinates[0].toFixed(5)}`;
                  navigator.clipboard.writeText(val);
               }}
               style={{ background: '#4b5563', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem' }}
            >
               {t('copy_coord')}
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default TacticalMap;
