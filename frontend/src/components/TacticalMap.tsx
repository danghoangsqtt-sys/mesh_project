import React, { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import { Protocol } from 'pmtiles';
import { layers, LIGHT } from '@protomaps/basemaps';
import MapboxDraw from '@mapbox/mapbox-gl-draw';
import '@mapbox/mapbox-gl-draw/dist/mapbox-gl-draw.css';
import { useMeshStore } from '../stores/useMeshStore';

// Haversine distance (meters) between two [lng, lat] points
function haversineDistance(c1: [number, number], c2: [number, number]): number {
  const R = 6371000;
  const toRad = (d: number) => d * Math.PI / 180;
  const dLat = toRad(c2[1] - c1[1]);
  const dLon = toRad(c2[0] - c1[0]);
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(c1[1])) * Math.cos(toRad(c2[1])) * Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

// Bearing (degrees) from c1 to c2
function bearing(c1: [number, number], c2: [number, number]): number {
  const toRad = (d: number) => d * Math.PI / 180;
  const toDeg = (r: number) => r * 180 / Math.PI;
  const dLon = toRad(c2[0] - c1[0]);
  const y = Math.sin(dLon) * Math.cos(toRad(c2[1]));
  const x = Math.cos(toRad(c1[1])) * Math.sin(toRad(c2[1])) - Math.sin(toRad(c1[1])) * Math.cos(toRad(c2[1])) * Math.cos(dLon);
  return (toDeg(Math.atan2(y, x)) + 360) % 360;
}

function formatDist(m: number): string {
  return m >= 1000 ? `${(m / 1000).toFixed(2)} km` : `${Math.round(m)} m`;
}

// ═══════════════════════════════════════════════════════════════════════
// SCHEMA COMPATIBILITY FIX — PMTiles v3 (pmap:kind) vs Basemaps v5 (kind)
// The offline.pmtiles uses older Protomaps schema where classification
// fields are prefixed with "pmap:" (e.g. pmap:kind, pmap:kind_detail).
// However @protomaps/basemaps v5.x generates filters using unprefixed
// "kind" fields. This transformer patches all expressions to match.
// ═══════════════════════════════════════════════════════════════════════
const FIELD_REMAP: Record<string, string> = {
  'kind': 'pmap:kind',
  'kind_detail': 'pmap:kind_detail',
  'min_zoom': 'pmap:min_zoom',
  'brk_a3': 'pmap:brk_a3',
  'min_admin_level': 'pmap:min_admin_level',
  'level': 'pmap:level',
  'link': 'pmap:link',
  'is_link': 'pmap:link',
};

/** Recursively remap field names in MapLibre GL filter/paint expressions */
const remapExpression = (expr: any): any => {
  if (!Array.isArray(expr)) return expr;
  
  const op = expr[0];
  
  // Property access: ["get", "kind"] → ["get", "pmap:kind"]
  if (op === 'get' && typeof expr[1] === 'string' && FIELD_REMAP[expr[1]]) {
    return ['get', FIELD_REMAP[expr[1]], ...expr.slice(2).map(remapExpression)];
  }
  
  // "has" / "!has" operator: ["has", "kind"] → ["has", "pmap:kind"]
  if ((op === 'has' || op === '!has') && typeof expr[1] === 'string' && FIELD_REMAP[expr[1]]) {
    return [op, FIELD_REMAP[expr[1]]];
  }
  
  // Comparison with shorthand: ["==", "kind", "highway"] → ["==", "pmap:kind", "highway"]
  if (['==', '!=', '>', '<', '>=', '<=', 'in', '!in'].includes(op)) {
    if (typeof expr[1] === 'string' && FIELD_REMAP[expr[1]]) {
      return [op, FIELD_REMAP[expr[1]], ...expr.slice(2).map(remapExpression)];
    }
  }
  
  // "match" expression: ["match", ["get","kind"], ...] → remap the inner get
  // "case", "interpolate", "step", "coalesce", "all", "any", "none" — recurse
  return expr.map(remapExpression);
};

/** Remap all field refs in a layer's filter and paint/layout expressions */
const remapLayer = (layer: any): any => {
  const patched = { ...layer };
  
  // Remap filter
  if (patched.filter) {
    patched.filter = remapExpression(patched.filter);
  }
  
  // Remap paint expressions
  if (patched.paint) {
    const newPaint: any = {};
    for (const [key, val] of Object.entries(patched.paint)) {
      newPaint[key] = remapExpression(val);
    }
    patched.paint = newPaint;
  }
  
  // Remap layout expressions
  if (patched.layout) {
    const newLayout: any = {};
    for (const [key, val] of Object.entries(patched.layout)) {
      newLayout[key] = remapExpression(val);
    }
    patched.layout = newLayout;
  }
  
  return patched;
};

// ═══════════════════════════════════════════════════════════════════════
// TACTICAL MAP STYLE ENGINE — High-contrast multi-color vector layers
// Transforms the near-white Protomaps LIGHT palette into a vivid, 
// field-ready tactical display with clear roads, vegetation & terrain.
// Inspired by OSM Liberty + AliFlux/VectorTileRenderer color palettes.
// ═══════════════════════════════════════════════════════════════════════
const getCustomLayers = () => {
  // Generate base layers, then remap field names for PMTiles compatibility
  const rawLayers = layers('offline-map', LIGHT, { lang: 'vi' });
  const baseLayers = rawLayers.map(remapLayer);
  
  // Tactical road width multiplier: boost widths for field visibility
  // Returns undefined if original is undefined (casing-only layers without line-width)
  const boostWidth = (original: any, factor: number, minWidth: number = 0.5): any => {
    if (original === undefined || original === null) return undefined; // Don't create width where none existed
    if (typeof original === 'number') return Math.max(original * factor, minWidth);
    if (Array.isArray(original) && original[0] === 'interpolate') {
      // Deep clone to avoid mutating shared protomaps references
      const boosted = JSON.parse(JSON.stringify(original));
      for (let i = 4; i < boosted.length; i += 2) { // numeric values are at even indices after header
        if (typeof boosted[i] === 'number') {
          boosted[i] = Math.max(boosted[i] * factor, minWidth);
        }
      }
      return boosted;
    }
    return original;
  };
  
  // Safely set boosted width — only assigns if the boosted value is defined
  const setBoostWidth = (paint: any, original: any, factor: number, minWidth: number = 0.5) => {
    const boosted = boostWidth(original, factor, minWidth);
    if (boosted !== undefined) paint['line-width'] = boosted;
  };

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

    // ══════════════════════════════════════════
    // BACKGROUND — dark tactical base
    // ══════════════════════════════════════════
    if (id === 'background') {
      paintCopy['background-color'] = '#f0ece4'; // warm parchment background
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ══════════════════════════════════════════
    // WATER (fill + line) — vivid blue
    // ══════════════════════════════════════════
    if (sl === 'water' || id.includes('water')) {
      if (type === 'fill') {
        paintCopy['fill-color'] = '#4da6e8';
        paintCopy['fill-opacity'] = 0.85;
      } else if (type === 'line') {
        paintCopy['line-color'] = '#2980b9';
        if (id.includes('stream')) {
          paintCopy['line-color'] = '#5dade2';
          paintCopy['line-width'] = ['interpolate', ['linear'], ['zoom'], 9, 0.5, 14, 1.5, 18, 3];
        } else if (id.includes('river')) {
          paintCopy['line-color'] = '#2e86c1';
          paintCopy['line-width'] = ['interpolate', ['linear'], ['zoom'], 9, 1, 14, 2.5, 18, 5];
        }
      } else if (type === 'symbol') {
        paintCopy['text-color'] = '#1a5276';
        paintCopy['text-halo-color'] = '#aed6f1';
        paintCopy['text-halo-width'] = 1.5;
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ══════════════════════════════════════════
    // LANDCOVER — vegetation must be visible at ALL zoom levels
    // The default protomaps LIGHT fades landcover to 0 at zoom 7+
    // ══════════════════════════════════════════
    if (id === 'landcover') {
      paintCopy['fill-color'] = [
        'match', ['get', 'pmap:kind'],
        'grassland', '#b8e6b0',      // light green grassland
        'barren', '#e8dcc8',          // sandy barren
        'urban_area', '#ddd8d0',      // urban gray
        'farmland', '#c5e6b8',        // farm green
        'glacier', '#e8f0f8',         // icy white-blue
        'scrub', '#c8d8a0',           // scrub yellow-green
        '#a8d8b0'                     // default forest green
      ];
      // KEY FIX: keep landcover visible at ALL zoom levels (was fading to 0 at z7+)
      paintCopy['fill-opacity'] = ['interpolate', ['linear'], ['zoom'], 0, 0.8, 7, 0.5, 12, 0.4, 18, 0.35];
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ══════════════════════════════════════════
    // LANDUSE — parks, forests, military zones etc.
    // ══════════════════════════════════════════
    if (sl === 'landuse') {
      if (type === 'fill') {
        if (id === 'landuse_park') {
           paintCopy['fill-color'] = [
            'case',
            // Military zones — MUST stand out for tactical awareness
            ['in', ['get', 'pmap:kind'], ['literal', ['military', 'naval_base', 'airfield']]],
            '#8a9a68',  // Olive drab — standard military color
            // Parks & forests
            ['in', ['get', 'pmap:kind'], ['literal', ['national_park', 'park', 'cemetery', 'protected_area', 'nature_reserve', 'forest', 'golf_course']]],
            '#7bc87e',  // Rich park green
            ['==', ['get', 'pmap:kind'], 'wood'],
            '#6aad6d',  // Dense forest darker green
            ['in', ['get', 'pmap:kind'], ['literal', ['scrub', 'grassland', 'grass']]],
            '#9ad49c',  // Light grass green
            ['==', ['get', 'pmap:kind'], 'glacier'],
            '#d8e8f0',
            ['==', ['get', 'pmap:kind'], 'sand'],
            '#e8dcc0',
            '#d0c8b8'
          ];
          paintCopy['fill-opacity'] = ['interpolate', ['linear'], ['zoom'], 6, 0.4, 10, 0.6, 14, 0.7];
        } else if (id === 'landuse_urban_green') {
          paintCopy['fill-color'] = '#88cc8a';
          paintCopy['fill-opacity'] = 0.6;
        } else if (id === 'landuse_hospital') {
          paintCopy['fill-color'] = '#f0d0d0';
          paintCopy['fill-opacity'] = 0.6;
        } else if (id === 'landuse_industrial') {
          paintCopy['fill-color'] = '#d0c8c0';
          paintCopy['fill-opacity'] = 0.5;
        } else if (id === 'landuse_school') {
          paintCopy['fill-color'] = '#e8dcc0';
          paintCopy['fill-opacity'] = 0.5;
        } else if (id === 'landuse_beach') {
          paintCopy['fill-color'] = '#f0e8c0';
          paintCopy['fill-opacity'] = 0.7;
        } else if (id === 'landuse_zoo') {
          paintCopy['fill-color'] = '#a8d0b8';
          paintCopy['fill-opacity'] = 0.5;
        } else if (id === 'landuse_aerodrome') {
          paintCopy['fill-color'] = '#d0d0d8';
          paintCopy['fill-opacity'] = 0.5;
        } else if (id === 'landuse_runway') {
          paintCopy['fill-color'] = '#c0c0c8';
        } else if (id === 'landuse_pedestrian') {
          paintCopy['fill-color'] = '#e0d8c8';
        } else if (id === 'landuse_pier') {
          paintCopy['fill-color'] = '#d0c8c0';
        } else {
          // Generic landuse
          paintCopy['fill-color'] = '#90c890';
          paintCopy['fill-opacity'] = 0.3;
        }
      } else if (type === 'line') {
        paintCopy['line-color'] = '#5a9e5c';
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ══════════════════════════════════════════
    // EARTH (base land) — warm beige tone
    // ══════════════════════════════════════════
    if (sl === 'earth' || id === 'earth') {
      if (type === 'fill') {
        paintCopy['fill-color'] = '#f0ece4';  // warm parchment base
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ══════════════════════════════════════════
    // ROADS — TACTICAL HIGH-CONTRAST COLORS + BOOSTED WIDTHS
    // Key fix: override BOTH color AND width for field visibility
    // ══════════════════════════════════════════
    if (sl === 'roads') {
      if (type === 'line') {
        const originalWidth = paintCopy['line-width'];
        
        // ── Highways (bright amber + red casing) ──
        if (id.includes('highway')) {
          paintCopy['line-color'] = id.includes('casing') ? '#c0392b' : '#f39c12';
          setBoostWidth(paintCopy, originalWidth, id.includes('casing') ? 1.3 : 1.5, 1);
        }
        // ── Major roads (orange + dark casing) ──
        else if (id.includes('major')) {
          paintCopy['line-color'] = id.includes('casing') ? '#a04000' : '#e67e22';
          setBoostWidth(paintCopy, originalWidth, id.includes('casing') ? 1.2 : 1.4, 0.8);
        }
        // ── Minor roads (warm gray) ──
        else if (id.includes('minor') && !id.includes('service')) {
          paintCopy['line-color'] = id.includes('casing') ? '#7f8c8d' : '#bdc3c7';
          setBoostWidth(paintCopy, originalWidth, 1.3, 0.5);
        }
        // ── Minor service roads (lighter gray) ──
        else if (id.includes('service')) {
          paintCopy['line-color'] = id.includes('casing') ? '#95a5a6' : '#d5dbdb';
          setBoostWidth(paintCopy, originalWidth, 1.2, 0.4);
        }
        // ── Link roads ──
        else if (id.includes('link')) {
          paintCopy['line-color'] = id.includes('casing') ? '#a04000' : '#e67e22';
          setBoostWidth(paintCopy, originalWidth, 1.2, 0.6);
        }
        // ── Rail = steel blue dashed ──
        else if (id.includes('rail')) {
          paintCopy['line-color'] = '#5b6abf';
          setBoostWidth(paintCopy, originalWidth, 1.5, 0.5);
          paintCopy['line-opacity'] = 0.7;
        }
        // ── Runway/Taxiway ──
        else if (id.includes('runway') || id.includes('taxiway')) {
          paintCopy['line-color'] = '#8e8e9a';
        }
        // ── Pier ──
        else if (id.includes('pier')) {
          paintCopy['line-color'] = '#b8b8c0';
        }
        // ── Bridge roads (tinted blue-gray) ──
        else if (id.includes('bridge')) {
          paintCopy['line-color'] = id.includes('casing') ? '#566573' : '#aab7b8';
          setBoostWidth(paintCopy, originalWidth, 1.2, 0.5);
        }
        // ── Tunnel roads (dashed, muted) ──
        else if (id.includes('tunnel')) {
          paintCopy['line-color'] = '#a0a0a8';
          setBoostWidth(paintCopy, originalWidth, 1.1, 0.4);
        }
        // ── Other roads (catch-all) ──
        else {
          paintCopy['line-color'] = '#a0a0a8';
          setBoostWidth(paintCopy, originalWidth, 1.2, 0.4);
        }
      } else if (type === 'symbol') {
        // Road labels — high contrast
        paintCopy['text-color'] = '#2c3e50';
        paintCopy['text-halo-color'] = '#ffffff';
        paintCopy['text-halo-width'] = 2;
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ══════════════════════════════════════════
    // BOUNDARIES — red dashed lines
    // ══════════════════════════════════════════
    if (sl === 'boundaries' || id.includes('boundaries') || id.includes('boundary')) {
      if (type === 'line') {
        paintCopy['line-color'] = '#e74c3c';
        paintCopy['line-width'] = id.includes('country') ? 2 : 1;
        paintCopy['line-dasharray'] = [4, 2];
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ══════════════════════════════════════════
    // TRANSIT — purple
    // ══════════════════════════════════════════
    if (sl === 'transit' || id.includes('transit')) {
      if (type === 'line') {
        paintCopy['line-color'] = '#8e44ad';
      }
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ══════════════════════════════════════════
    // BUILDINGS (hide 2D flat, we add 3D below)
    // ══════════════════════════════════════════
    if (id === 'buildings' || (sl === 'buildings' && type === 'fill')) {
      return { ...layer, layout: { ...layoutCopy, visibility: 'none' } };
    }

    // ══════════════════════════════════════════
    // POIs — improve visibility
    // ══════════════════════════════════════════
    if (id === 'pois' && type === 'symbol') {
      paintCopy['text-color'] = '#2c3e50';
      paintCopy['text-halo-color'] = '#ffffff';
      paintCopy['text-halo-width'] = 1.5;
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ══════════════════════════════════════════
    // PLACES — city/town/village labels
    // ══════════════════════════════════════════
    if (sl === 'places' && type === 'symbol') {
      paintCopy['text-color'] = '#1a2530';
      paintCopy['text-halo-color'] = '#ffffff';
      paintCopy['text-halo-width'] = 2;
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }

    // ══════════════════════════════════════════
    // LABELS — ensure font fallback + high contrast
    // ══════════════════════════════════════════
    if (type === 'symbol' && layoutCopy['text-field']) {
      if (!layoutCopy['text-font'] || (Array.isArray(layoutCopy['text-font']) && layoutCopy['text-font'].some((f: string) => f.includes('Open Sans') || f.includes('Arial Unicode')))) {
        layoutCopy['text-font'] = ['Noto Sans Regular'];
      }
      // Ensure all labels have good contrast
      if (!paintCopy['text-halo-width']) {
        paintCopy['text-halo-color'] = '#ffffff';
        paintCopy['text-halo-width'] = 1.5;
      }
    }

    // Return with any arrow-icon fix applied
    if (changed) {
      return { ...layer, paint: paintCopy, layout: layoutCopy };
    }
    return layer;
  });

  // ══════════════════════════════════════════════════════════════════
  // CRITICAL: Add layers for source-layers MISSING from @protomaps/basemaps
  // The PMTiles contains 'natural' (zoom 2-15) and 'physical_line' (zoom 9-15)
  // but basemaps v5 generates ZERO layers for them — leaving mountains blank!
  // ══════════════════════════════════════════════════════════════════

  // ── NATURAL FILL — forests, scrub, wetland, bare rock, sand, glacier ──
  // This is the MOST IMPORTANT missing layer — it colors mountains & hills!
  // Insert BEFORE buildings so it renders underneath everything
  const naturalInsertIndex = modifiedLayers.findIndex(l => l.id === 'buildings' || l['source-layer'] === 'buildings');
  const naturalLayers = [
    {
      id: 'natural-fill',
      type: 'fill',
      source: 'offline-map',
      'source-layer': 'natural',
      paint: {
        'fill-color': [
          'match', ['get', 'pmap:kind'],
          'wood',       '#5a9e5c',   // Dense forest — dark green
          'forest',     '#5a9e5c',   // Forest — dark green  
          'scrub',      '#8aad6d',   // Scrub/bush — olive green
          'grassland',  '#a0c890',   // Grassland — light green
          'grass',      '#a8d090',   // Grass — bright green
          'wetland',    '#7aad8d',   // Wetland — blue-green
          'marsh',      '#7aad8d',   // Marsh — blue-green
          'bare_rock',  '#c0b8a0',   // Bare rock/mountain — tan/brown
          'rock',       '#c0b8a0',   // Rock — tan/brown
          'scree',      '#b8b0a0',   // Scree — gray-brown
          'sand',       '#e0d8b8',   // Sand — light tan
          'glacier',    '#d8e8f0',   // Glacier — icy blue
          'heath',      '#a0b880',   // Heath — yellow-green
          'fell',       '#b0a890',   // Mountain fell — gray-brown
          'cliff',      '#a8a098',   // Cliff — dark gray
          'peak',       '#8a8078',   // Peak — darker
          '#6aad6d'                  // Default — medium green (forest)
        ],
        'fill-opacity': [
          'interpolate', ['linear'], ['zoom'],
          2, 0.4,    // Visible even at overview
          6, 0.5,
          10, 0.6,   // Strong at city zoom
          14, 0.65,  // Bold at detail zoom
          18, 0.6
        ]
      }
    },
    {
      id: 'natural-outline',
      type: 'line',
      source: 'offline-map',
      'source-layer': 'natural',
      minzoom: 10,
      paint: {
        'line-color': [
          'match', ['get', 'pmap:kind'],
          'wood',     '#4a8e4c',
          'forest',   '#4a8e4c',
          'scrub',    '#7a9d5d',
          'bare_rock','#a0988a',
          '#5a9d5c'
        ],
        'line-width': 0.5,
        'line-opacity': 0.3
      }
    }
  ];

  // ── PHYSICAL_LINE — rivers, streams, canals at higher zooms ──
  const physicalLineLayers = [
    {
      id: 'physical-line-river',
      type: 'line',
      source: 'offline-map',
      'source-layer': 'physical_line',
      filter: ['in', ['get', 'pmap:kind'], ['literal', ['river', 'canal']]],
      paint: {
        'line-color': '#3a90c0',
        'line-width': ['interpolate', ['linear'], ['zoom'], 9, 0.5, 14, 2, 18, 4],
        'line-opacity': 0.7
      }
    },
    {
      id: 'physical-line-stream',
      type: 'line',
      source: 'offline-map',
      'source-layer': 'physical_line',
      filter: ['in', ['get', 'pmap:kind'], ['literal', ['stream', 'ditch', 'drain']]],
      minzoom: 12,
      paint: {
        'line-color': '#5aade2',
        'line-width': ['interpolate', ['linear'], ['zoom'], 12, 0.3, 14, 0.8, 18, 2],
        'line-opacity': 0.6
      }
    }
  ];

  // Insert natural layers right before buildings
  if (naturalInsertIndex > 0) {
    modifiedLayers.splice(naturalInsertIndex, 0, ...naturalLayers, ...physicalLineLayers);
  } else {
    // Fallback: push before the end
    modifiedLayers.push(...naturalLayers, ...physicalLineLayers);
  }

  // Add 3D Extruded Buildings — prominent for tactical detail
  modifiedLayers.push({
    id: 'buildings-3d',
    type: 'fill-extrusion',
    source: 'offline-map',
    'source-layer': 'buildings',
    minzoom: 13,
    paint: {
      'fill-extrusion-color': [
        'interpolate', ['linear'], ['get', 'height'],
        0, '#a09890',   // Low buildings: warm brown-gray
        10, '#908880',  // Medium: darker
        20, '#807870',  // Tall: even darker
        40, '#706860'   // Very tall: darkest
      ],
      'fill-extrusion-height': [
        'interpolate', ['linear'], ['zoom'],
        13, ['*', ['coalesce', ['get', 'height'], 12], 0.5],
        15, ['coalesce', ['get', 'height'], 12],
        18, ['coalesce', ['get', 'height'], 12]
      ],
      'fill-extrusion-base': ['coalesce', ['get', 'min_height'], 0],
      'fill-extrusion-opacity': [
        'interpolate', ['linear'], ['zoom'],
        13, 0.4,
        15, 0.8,
        18, 0.85
      ]
    }
  });

  // ══════════════════════════════════════════════════════════════════
  // MILITARY ZONE OVERLAY — renders ON TOP of everything for maximum visibility
  // Double-border + tinted fill for unmistakable tactical identification
  // ══════════════════════════════════════════════════════════════════
  
  // Outer glow border — wider, softer for spatial awareness
  modifiedLayers.push({
    id: 'military-zone-border-glow',
    type: 'line',
    source: 'offline-map',
    'source-layer': 'landuse',
    filter: ['in', ['get', 'pmap:kind'], ['literal', ['military', 'naval_base', 'airfield']]],
    paint: {
      'line-color': '#c0392b',   // Red glow
      'line-width': ['interpolate', ['linear'], ['zoom'], 10, 3, 14, 6, 18, 10],
      'line-opacity': 0.25,
      'line-blur': 3
    }
  });
  
  // Fill — olive tint 
  modifiedLayers.push({
    id: 'military-zone-fill',
    type: 'fill',
    source: 'offline-map',
    'source-layer': 'landuse',
    filter: ['in', ['get', 'pmap:kind'], ['literal', ['military', 'naval_base', 'airfield']]],
    paint: {
      'fill-color': '#556b2f',   // Dark olive green
      'fill-opacity': ['interpolate', ['linear'], ['zoom'], 8, 0.12, 12, 0.18, 16, 0.22]
    }
  });
  
  // Inner dashed border — sharp, high-contrast
  modifiedLayers.push({
    id: 'military-zone-border',
    type: 'line',
    source: 'offline-map',
    'source-layer': 'landuse',
    filter: ['in', ['get', 'pmap:kind'], ['literal', ['military', 'naval_base', 'airfield']]],
    paint: {
      'line-color': '#c0392b',   // Bold red
      'line-width': ['interpolate', ['linear'], ['zoom'], 10, 1.5, 14, 3, 18, 5],
      'line-dasharray': [5, 3],
      'line-opacity': 0.85
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
  const nodeTrails = useMeshStore(state => state.nodeTrails);
  const showNodeTrails = useMeshStore(state => state.showNodeTrails);
  const setShowNodeTrails = useMeshStore(state => state.setShowNodeTrails);
  
  const [pathStart, setPathStart] = useState<[number, number] | null>(null);
  const [pathEnd, setPathEnd] = useState<[number, number] | null>(null);
  const [isTacticalToolsOpen, setIsTacticalToolsOpen] = useState(true);
  
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

  // Measure distance state — polyline multi-point (like Google Maps)
  const [measureMode, setMeasureMode] = useState(false);
  const [measurePoints, setMeasurePoints] = useState<[number, number][]>([]);
  const [showNodeDistances, setShowNodeDistances] = useState(false);
  const measureModeRef = useRef(false);

  // Sync state to refs
  useEffect(() => { pathStartRef.current = pathStart; }, [pathStart]);
  useEffect(() => { pathEndRef.current = pathEnd; }, [pathEnd]);
  useEffect(() => { selectedMarkerIconRef.current = selectedMarkerIcon; }, [selectedMarkerIcon]);
  useEffect(() => { graphicTypeRef.current = graphicType; }, [graphicType]);
  useEffect(() => { measureModeRef.current = measureMode; }, [measureMode]);

  // Compute polyline measure data
  const measureSegments = React.useMemo(() => {
    if (measurePoints.length < 2) return { segments: [], totalDist: 0, totalBear: 0 };
    let totalDist = 0;
    const segments: { from: [number,number]; to: [number,number]; dist: number; bear: number; cumDist: number }[] = [];
    for (let i = 0; i < measurePoints.length - 1; i++) {
      const d = haversineDistance(measurePoints[i], measurePoints[i + 1]);
      const b = bearing(measurePoints[i], measurePoints[i + 1]);
      totalDist += d;
      segments.push({ from: measurePoints[i], to: measurePoints[i + 1], dist: d, bear: b, cumDist: totalDist });
    }
    const totalBear = bearing(measurePoints[0], measurePoints[measurePoints.length - 1]);
    return { segments, totalDist, totalBear };
  }, [measurePoints]);

  // Update measure polyline on map when points change
  useEffect(() => {
    const map = mapInstance.current;
    if (!map || !isMapLoaded) return;
    const src = map.getSource('measure-line') as maplibregl.GeoJSONSource;
    if (!src) return;
    if (measurePoints.length < 2) {
      // Show point markers even if only 1 point
      const features: any[] = measurePoints.map((p, i) => ({
        type: 'Feature', geometry: { type: 'Point', coordinates: p },
        properties: { label: `${i + 1}`, markerType: 'vertex' }
      }));
      src.setData({ type: 'FeatureCollection', features });
      return;
    }
    const features: any[] = [];
    // Full polyline
    features.push({ type: 'Feature', geometry: { type: 'LineString', coordinates: measurePoints }, properties: {} });
    // Segment midpoint labels
    measureSegments.segments.forEach((seg, _i) => {
      const mid: [number, number] = [(seg.from[0] + seg.to[0]) / 2, (seg.from[1] + seg.to[1]) / 2];
      features.push({ type: 'Feature', geometry: { type: 'Point', coordinates: mid },
        properties: { label: formatDist(seg.dist), markerType: 'segment' } });
    });
    // Vertex markers (numbered)
    measurePoints.forEach((p, i) => {
      features.push({ type: 'Feature', geometry: { type: 'Point', coordinates: p },
        properties: { label: `${i + 1}`, markerType: 'vertex' } });
    });
    src.setData({ type: 'FeatureCollection', features });
  }, [measurePoints, measureSegments, isMapLoaded]);

  // Auto node-to-node distance lines
  useEffect(() => {
    const map = mapInstance.current;
    if (!map || !isMapLoaded) return;
    const src = map.getSource('node-distances') as maplibregl.GeoJSONSource;
    if (!src) return;
    if (!showNodeDistances) { src.setData({ type: 'FeatureCollection', features: [] }); return; }
    const nodeList = Object.values(nodes).filter(n => n.latitude && n.longitude);
    const features: any[] = [];
    for (let i = 0; i < nodeList.length; i++) {
      for (let j = i + 1; j < nodeList.length; j++) {
        const a: [number, number] = [nodeList[i].longitude, nodeList[i].latitude];
        const b: [number, number] = [nodeList[j].longitude, nodeList[j].latitude];
        const d = haversineDistance(a, b);
        const mid: [number, number] = [(a[0] + b[0]) / 2, (a[1] + b[1]) / 2];
        features.push({ type: 'Feature', geometry: { type: 'LineString', coordinates: [a, b] }, properties: {} });
        features.push({ type: 'Feature', geometry: { type: 'Point', coordinates: mid }, properties: { label: formatDist(d) } });
      }
    }
    src.setData({ type: 'FeatureCollection', features });
  }, [nodes, showNodeDistances, isMapLoaded]);

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

  // Sync node trails to map
  useEffect(() => {
    const map = mapInstance.current;
    if (!map || !isMapLoaded) return;
    
    const source = map.getSource('node-trails') as maplibregl.GeoJSONSource;
    if (source) {
      if (!showNodeTrails) {
        source.setData({ type: 'FeatureCollection', features: [] });
        return;
      }
      
      const features: any[] = [];
      const colors = ['#3b82f6', '#ef4444', '#22c55e', '#eab308', '#a855f7', '#06b6d4'];
      
      Object.entries(nodeTrails).forEach(([_, trail], index) => {
        if (trail.length > 0) {
          const color = colors[index % colors.length];
          // Line
          if (trail.length > 1) {
            features.push({
              type: 'Feature',
              geometry: { type: 'LineString', coordinates: trail },
              properties: { color }
            });
          }
          // Start Marker
          features.push({
            type: 'Feature',
            geometry: { type: 'Point', coordinates: trail[0] },
            properties: { color, type: 'start' }
          });
        }
      });
      source.setData({ type: 'FeatureCollection', features });
    }
  }, [nodeTrails, showNodeTrails, isMapLoaded]);

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
        // Offline font glyphs served from Pi via nginx + tileserver fonts
        glyphs: '/fonts/{fontstack}/{range}.pbf',
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
          'node-trails': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] }
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
          },
          'measure-line': {
            type: 'geojson',
            data: { type: 'FeatureCollection', features: [] }
          },
          'node-distances': {
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
            id: 'node-trails-line', type: 'line', source: 'node-trails',
            filter: ['==', '$type', 'LineString'],
            layout: { 'line-join': 'round', 'line-cap': 'round' },
            paint: { 
              'line-color': ['get', 'color'], 
              'line-width': 4,
              'line-opacity': 0.6,
              'line-dasharray': [1, 2] // Tactical dashed line
            }
          },
          {
            id: 'node-trails-start', type: 'symbol', source: 'node-trails',
            filter: ['all', ['==', '$type', 'Point'], ['==', 'type', 'start']],
            layout: {
              'text-field': '🏁 START',
              'text-size': 10,
              'text-offset': [0, 1],
              'text-anchor': 'top',
              'text-font': ['Noto Sans Bold']
            },
            paint: {
              'text-color': ['get', 'color'],
              'text-halo-color': '#0f172a',
              'text-halo-width': 2
            }
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
          // ── Measure line ──
          {
            id: 'measure-line-layer',
            type: 'line',
            source: 'measure-line',
            filter: ['==', '$type', 'LineString'],
            paint: { 'line-color': '#f59e0b', 'line-width': 3, 'line-dasharray': [4, 3] }
          },
          {
            id: 'measure-label-layer',
            type: 'symbol',
            source: 'measure-line',
            filter: ['==', '$type', 'Point'],
            layout: { 'text-field': ['get', 'label'], 'text-size': 13, 'text-font': ['Noto Sans Bold'], 'text-allow-overlap': true },
            paint: { 'text-color': '#fbbf24', 'text-halo-color': '#0f172a', 'text-halo-width': 2 }
          },
          // ── Node distance lines (auto) ──
          {
            id: 'node-dist-line-layer',
            type: 'line',
            source: 'node-distances',
            filter: ['==', '$type', 'LineString'],
            paint: { 'line-color': '#06b6d4', 'line-width': 1.5, 'line-dasharray': [6, 4], 'line-opacity': 0.6 }
          },
          {
            id: 'node-dist-label-layer',
            type: 'symbol',
            source: 'node-distances',
            filter: ['==', '$type', 'Point'],
            layout: { 'text-field': ['get', 'label'], 'text-size': 10, 'text-font': ['Noto Sans Regular'], 'text-allow-overlap': true },
            paint: { 'text-color': '#67e8f9', 'text-halo-color': '#0f172a', 'text-halo-width': 1.5 }
          }
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

      map.on('click', async (e: any) => {
        // Measure mode: add points to polyline (unlimited)
        if (measureModeRef.current) {
          const pt: [number, number] = [e.lngLat.lng, e.lngLat.lat];
          setMeasurePoints(prev => [...prev, pt]);
          return;
        }
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
         <div style={{
            position: 'absolute',
            bottom: '24px',
            left: 0,
            zIndex: 10,
            display: 'flex',
            flexDirection: 'column',
            gap: '8px',
            pointerEvents: 'none',
            width: '280px',
            transition: 'transform 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            transform: isTacticalToolsOpen ? 'translateX(24px)' : 'translateX(-280px)'
         }}>
            <button
               onClick={() => setIsTacticalToolsOpen(!isTacticalToolsOpen)}
               title={isTacticalToolsOpen ? 'Thu gọn' : 'Mở công cụ'}
               style={{
                  position: 'absolute',
                  top: '50%',
                  right: '-32px',
                  transform: 'translateY(-50%)',
                  background: '#3f6212',
                  color: '#bef264',
                  border: '1px solid #4d7c0f',
                  borderLeft: 'none',
                  borderRadius: '0 6px 6px 0',
                  width: '32px',
                  height: '60px',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '2px 0 5px rgba(0,0,0,0.5)',
                  zIndex: 11,
                  fontSize: '1rem',
                  pointerEvents: 'auto'
               }}
            >
               {isTacticalToolsOpen ? '◀' : '▶'}
            </button>

            {/* Tactical tools content */}
            <div style={{ pointerEvents: 'auto' }}>
            {activeMode && activeMode.startsWith('draw_') && (
               <div style={{ background: 'rgba(234, 179, 8, 0.9)', color: '#000', padding: '6px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold', textAlign: 'center', marginBottom: '4px', border: '1px solid #ca8a04' }}>
                  {t('click_to_draw')}
               </div>
            )}
            
            <div style={{ background: 'rgba(15, 23, 42, 0.85)', border: '1px solid #3f6212', borderRadius: '4px', overflow: 'hidden', backdropFilter: 'blur(4px)' }}>
                <div style={{ background: '#3f6212', color: '#fff', fontSize: '0.6rem', padding: '2px 4px', fontWeight: 'bold', letterSpacing: '0.5px' }}>
                    {t('tactical_tools')}
                </div>
                <div style={{ padding: '4px', display: 'flex', flexDirection: 'column', gap: '2px' }}>
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
                           border: '1px solid #4d7c0f', padding: '4px 6px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.65rem', fontWeight: 'bold', textAlign: 'left', transition: 'all 0.2s' 
                       }}
                    >
                       📌 {t('draw_point')}
                    </button>

                    {/* Measure distance button */}
                    <button 
                       onClick={() => {
                         const newMode = !measureMode;
                         setMeasureMode(newMode);
                         setMeasurePoints([]);
                         if (newMode) setActiveMode('measure');
                         else setActiveMode('simple_select');
                       }}
                       style={{ 
                           background: measureMode ? '#b45309' : 'transparent', 
                           color: measureMode ? '#fff' : '#a3a8b4', 
                           border: '1px solid #b45309', padding: '4px 6px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.65rem', fontWeight: 'bold', textAlign: 'left', transition: 'all 0.2s' 
                       }}
                    >
                       📏 ĐO CỰ LY
                    </button>

                    {/* Auto node distances toggle */}
                    <button 
                       onClick={() => setShowNodeDistances(!showNodeDistances)}
                       style={{ 
                           background: showNodeDistances ? '#0e7490' : 'transparent', 
                           color: showNodeDistances ? '#fff' : '#a3a8b4', 
                           border: '1px solid #0e7490', padding: '4px 6px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.65rem', fontWeight: 'bold', textAlign: 'left', transition: 'all 0.2s' 
                       }}
                    >
                       🔗 {showNodeDistances ? 'ẨN' : 'HIỆN'} CỰ LY NODES
                    </button>

                    {/* Node Trails toggle */}
                    <button 
                       onClick={() => setShowNodeTrails(!showNodeTrails)}
                       style={{ 
                           background: showNodeTrails ? '#6b21a8' : 'transparent', 
                           color: showNodeTrails ? '#fff' : '#a3a8b4', 
                           border: '1px solid #6b21a8', padding: '4px 6px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.65rem', fontWeight: 'bold', textAlign: 'left', transition: 'all 0.2s' 
                       }}
                    >
                       👁️ {showNodeTrails ? t('hide_trails') : t('show_trails')}
                    </button>

                    <button 
                       onClick={() => {
                         const addEvent = useMeshStore.getState().addEvent;
                         addEvent({
                           id: Date.now(),
                           node_id: 3,
                           event_type: 'ai_predict_heat_stress',
                           severity: 'CRITICAL',
                           message: 'AI Scan Hoàn tất: Dấu hiệu kiệt sức do nhiệt tại Node 3.',
                           created_at: new Date().toISOString()
                         });
                       }}
                       style={{ 
                           background: 'rgba(239, 68, 68, 0.2)', 
                           color: '#fca5a5', 
                           border: '1px solid #ef4444', padding: '4px 6px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.65rem', fontWeight: 'bold', textAlign: 'left', transition: 'all 0.2s' 
                       }}
                    >
                       🤖 AI SCAN
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
                           background: 'transparent', color: '#ef4444', border: '1px solid #991b1b', padding: '4px 6px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.65rem', fontWeight: 'bold', textAlign: 'left' 
                       }}
                    >
                       ❌ {t('delete_selected')}
                    </button>
                </div>
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
              onClick={async (e) => {
                  const btn = e.currentTarget;
                  const originalText = btn.innerText;
                  const type = selectedGraphic.properties?.type || selectedGraphic.properties?.user_type || graphicType;
                  const coordText = `${selectedGraphic.geometry.coordinates[1].toFixed(5)}, ${selectedGraphic.geometry.coordinates[0].toFixed(5)}`;
                  const textToHex = (text: string) => text.split('').map(c => c.charCodeAt(0).toString(16).padStart(2, '0')).join('');
                  const payload = { target_node_id: 65535, command_type: 'BROADCAST', payload_hex: textToHex(`[${type}] ${coordText}`) };
                  
                  btn.innerText = "Đang gửi...";
                  try {
                      const response = await fetch('/api/commands/', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
                      if (!response.ok) throw new Error("Server error");
                      
                      btn.innerText = "Đã gửi ✓";
                      btn.style.background = "#10b981";
                      
                      // Log to events
                      const addEvent = useMeshStore.getState().addEvent;
                      addEvent({
                         id: Date.now(),
                         node_id: 0,
                         event_type: 'broadcast',
                         severity: 'INFO',
                         message: `Đã phát sóng tọa độ: [${type}] ${coordText}`,
                         created_at: new Date().toISOString()
                      });
                  } catch (err) {
                      btn.innerText = "Lỗi!";
                      btn.style.background = "#ef4444";
                  }
                  setTimeout(() => {
                      btn.innerText = originalText;
                      btn.style.background = "#3b82f6";
                  }, 2000);
              }}
              style={{ background: '#3b82f6', color: '#fff', border: 'none', padding: '6px 12px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem', flex: 1, transition: 'all 0.3s' }}
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

      {/* Measure result popup — polyline multi-point */}
      {measureMode && measurePoints.length >= 1 && (
        <div style={{ position: 'absolute', top: 10, right: 50, zIndex: 10, backgroundColor: 'rgba(15,23,42,0.95)', padding: '12px', borderRadius: '6px', color: '#f8fafc', border: '1px solid #b45309', minWidth: '220px', maxWidth: 'min(300px, 75vw)', boxShadow: '0 4px 12px rgba(0,0,0,0.5)', backdropFilter: 'blur(4px)' }}>
          <h4 style={{ margin: '0 0 8px 0', fontSize: '0.75rem', color: '#fbbf24', textTransform: 'uppercase', letterSpacing: '1px' }}>
             📏 ĐO CỰ LY ({measurePoints.length} điểm)
          </h4>
          {measurePoints.length < 2 ? (
            <div style={{ fontSize: '0.8rem', color: '#94a3b8' }}>Click trên bản đồ để đo...</div>
          ) : (
            <>
              <div style={{ fontSize: '1.2rem', fontWeight: 'bold', color: '#fbbf24', marginBottom: '4px' }}>
                Tổng: {formatDist(measureSegments.totalDist)}
              </div>
              <div style={{ fontSize: '0.7rem', color: '#94a3b8', marginBottom: '6px' }}>
                Đường chim bay: {formatDist(haversineDistance(measurePoints[0], measurePoints[measurePoints.length - 1]))} | Phương vị: <span style={{ color: '#67e8f9' }}>{measureSegments.totalBear.toFixed(0)}°</span>
              </div>
              {measureSegments.segments.length <= 8 && (
                <div style={{ maxHeight: '120px', overflowY: 'auto', marginBottom: '6px', borderTop: '1px solid #334155', paddingTop: '4px' }}>
                  {measureSegments.segments.map((seg, i) => (
                    <div key={i} style={{ fontSize: '0.65rem', color: '#64748b', display: 'flex', justifyContent: 'space-between', padding: '1px 0' }}>
                      <span>Đoạn {i + 1}→{i + 2}</span>
                      <span style={{ color: '#a3e635' }}>{formatDist(seg.dist)}</span>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
          <div style={{ display: 'flex', gap: '4px', marginTop: '6px' }}>
            <button 
              onClick={() => setMeasurePoints(prev => prev.slice(0, -1))}
              disabled={measurePoints.length === 0}
              style={{ flex: 1, background: '#4b5563', color: '#fff', border: 'none', padding: '5px 8px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.7rem', opacity: measurePoints.length === 0 ? 0.4 : 1 }}
            >
              ↩ HOÀN TÁC
            </button>
            <button 
              onClick={() => setMeasurePoints([])}
              style={{ flex: 1, background: '#991b1b', color: '#fff', border: 'none', padding: '5px 8px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.7rem' }}
            >
              🗑 XÓA HẾT
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default TacticalMap;
