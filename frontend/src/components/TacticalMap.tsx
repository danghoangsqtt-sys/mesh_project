import React, { useEffect, useRef, useState } from 'react';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import { Protocol } from 'pmtiles';

// Initialize PMTiles protocol only once
let protocolAdded = false;

interface TacticalMapProps {
  mapUrl?: string; // e.g., '/maps/offline.pmtiles'
  center?: [number, number];
  zoom?: number;
}

const TacticalMap: React.FC<TacticalMapProps> = ({ 
  mapUrl = '/maps/offline.pmtiles', 
  center = [109.1967, 12.2388], // Default to Nha Trang, Vietnam as an example
  zoom = 12 
}) => {
  const mapContainer = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<maplibregl.Map | null>(null);
  const [mapLoaded, setMapLoaded] = useState(false);

  useEffect(() => {
    if (!protocolAdded) {
      const protocol = new Protocol();
      maplibregl.addProtocol('pmtiles', protocol.tile);
      protocolAdded = true;
    }

    if (!mapContainer.current) return;

    // Create a very basic dark style for the map if the real pmtiles is missing.
    // In a real scenario, this style object would define sources pulling from pmtiles://
    const map = new maplibregl.Map({
      container: mapContainer.current,
      style: {
        version: 8,
        sources: {
          'offline-map': {
            type: 'vector',
            url: `pmtiles://${mapUrl}`
          }
        },
        layers: [
          {
            id: 'background',
            type: 'background',
            paint: {
              'background-color': '#0f172a' // Dark theme base
            }
          }
          // Note: Real vector layer styling goes here based on the OSM schema inside the PMTiles.
        ]
      },
      center: center,
      zoom: zoom,
      attributionControl: false
    });

    map.on('load', () => {
      setMapLoaded(true);
      
      // Add custom image for markers
      // In a full implementation, we load an icon from /public
    });

    mapInstance.current = map;

    return () => {
      map.remove();
      mapInstance.current = null;
    };
  }, [mapUrl, center, zoom]);

  return (
    <div 
      ref={mapContainer} 
      style={{ 
        width: '100%', 
        height: '100%', 
        borderRadius: 'var(--radius-md)', 
        overflow: 'hidden' 
      }} 
      className="tactical-map"
    >
      {!mapLoaded && (
        <div style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          color: 'var(--color-text-muted)'
        }}>
          Initializing Tactical Map...
        </div>
      )}
    </div>
  );
};

export default TacticalMap;
