import React, { useState, useEffect, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import maplibregl from 'maplibre-gl';
import 'maplibre-gl/dist/maplibre-gl.css';
import { useMeshStore } from '../stores/useMeshStore';

interface MapManagerModalProps {
  onClose: () => void;
}

const MapManagerModal: React.FC<MapManagerModalProps> = ({ onClose }) => {
  const { t } = useTranslation();
  const { mapDownloadProgress, mapDownloadStatus } = useMeshStore();
  
  const mapContainer = useRef<HTMLDivElement>(null);
  const targetOverlayRef = useRef<HTMLDivElement>(null);
  const mapInstance = useRef<maplibregl.Map | null>(null);

  const [minLon, setMinLon] = useState<string>('109.1');
  const [minLat, setMinLat] = useState<string>('12.1');
  const [maxLon, setMaxLon] = useState<string>('109.3');
  const [maxLat, setMaxLat] = useState<string>('12.3');
  const [sourceUrl, setSourceUrl] = useState<string>('');
  
  const [isRequesting, setIsRequesting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const isDownloading = mapDownloadProgress > 0 && mapDownloadProgress < 100;

  useEffect(() => {
    if (!mapContainer.current) return;

    mapInstance.current = new maplibregl.Map({
      container: mapContainer.current,
      style: {
        version: 8,
        sources: {
          osm: {
            type: 'raster',
            tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
            tileSize: 256,
            attribution: '&copy; OpenStreetMap'
          }
        },
        layers: [
          {
            id: 'osm',
            type: 'raster',
            source: 'osm',
            minzoom: 0,
            maxzoom: 19
          }
        ]
      },
      center: [109.1967, 12.2388], // Nha Trang as default
      zoom: 11
    });

    mapInstance.current.addControl(new maplibregl.NavigationControl(), 'top-right');

    const updateBounds = () => {
      const map = mapInstance.current;
      const overlay = targetOverlayRef.current;
      if (!map || !overlay) return;
      
      const mapRect = map.getContainer().getBoundingClientRect();
      const overRect = overlay.getBoundingClientRect();

      // Convert pixel coords relative to map container to LngLat
      const nw = map.unproject([overRect.left - mapRect.left, overRect.top - mapRect.top]);
      const se = map.unproject([overRect.right - mapRect.left, overRect.bottom - mapRect.top]);

      setMinLon(Math.min(nw.lng, se.lng).toFixed(4));
      setMaxLon(Math.max(nw.lng, se.lng).toFixed(4));
      setMinLat(Math.min(nw.lat, se.lat).toFixed(4));
      setMaxLat(Math.max(nw.lat, se.lat).toFixed(4));
    };

    mapInstance.current.on('move', updateBounds);
    mapInstance.current.on('load', updateBounds);
    // Initial call to set bounds if it loads fast
    setTimeout(updateBounds, 500);

    return () => {
      mapInstance.current?.remove();
    };
  }, []);

  const showProgress = isRequesting || isDownloading || mapDownloadStatus !== '' || errorMsg !== '';

  useEffect(() => {
    if (mapDownloadProgress < 0) {
      setErrorMsg(mapDownloadStatus || 'Lỗi tải bản đồ');
      setIsRequesting(false);
    } else if (mapDownloadProgress === 100) {
      setIsRequesting(false);
    }
  }, [mapDownloadProgress, mapDownloadStatus]);

  const handleDownload = async () => {
    if (isDownloading || isRequesting) return;
    setErrorMsg('');
    setIsRequesting(true);
    
    try {
      const res = await fetch('/api/maps/download', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          min_lon: parseFloat(minLon),
          min_lat: parseFloat(minLat),
          max_lon: parseFloat(maxLon),
          max_lat: parseFloat(maxLat),
          source_url: sourceUrl || undefined
        }),
      });
      
      const data = await res.json();
      if (!res.ok) {
        setErrorMsg(data.detail || 'Download failed to start');
      }
    } catch (err) {
      setErrorMsg('Network error starting download');
    } finally {
      setIsRequesting(false);
    }
  };

  return (
    <div className="modal-overlay" 
      onClick={onClose}
      style={{
        position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
        backgroundColor: 'rgba(255, 255, 255, 0.6)', backdropFilter: 'blur(4px)', zIndex: 1000,
        display: 'flex', alignItems: 'center', justifyContent: 'center'
      }}
    >
      <div className="modal-content glass-panel" 
        onClick={(e) => e.stopPropagation()}
        style={{
          width: '800px', maxWidth: '95%', padding: '24px',
          backgroundColor: 'var(--color-bg-panel)', border: '1px solid var(--color-border)', 
          borderRadius: '8px', boxShadow: '0 10px 40px rgba(0, 0, 0, 0.1)',
          maxHeight: 'calc(100% - 32px)', overflowY: 'auto', display: 'flex', flexDirection: 'column'
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h2 style={{ color: 'var(--color-primary)', fontSize: '1.25rem', margin: 0, textTransform: 'uppercase', letterSpacing: '1px' }}>
            🗺️ {t('manage_map')}
          </h2>
          <button onClick={onClose} style={{ 
            background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', fontSize: '1.5rem' 
          }}>
            &times;
          </button>
        </div>

        <p style={{ color: 'var(--color-text-muted)', fontSize: '0.875rem', marginBottom: '20px' }}>
          {t('map_manager_desc')}
        </p>

        {errorMsg && (
          <div style={{ backgroundColor: 'rgba(239, 68, 68, 0.1)', color: 'var(--color-status-critical)', padding: '10px', borderRadius: '4px', marginBottom: '16px', fontSize: '0.875rem', border: '1px solid var(--color-status-critical)' }}>
            {errorMsg}
          </div>
        )}

        {/* Interactive Map Area */}
        <div style={{ position: 'relative', width: '100%', height: '220px', minHeight: '220px', marginBottom: '20px', borderRadius: 'var(--radius-sm)', overflow: 'hidden', border: '1px solid var(--color-border)', flexShrink: 0 }}>
          <div ref={mapContainer} style={{ width: '100%', height: '100%' }} />
          {/* Target Overlay (Center Frame) */}
          <div ref={targetOverlayRef} style={{
            position: 'absolute', top: '15%', left: '15%', right: '15%', bottom: '15%',
            border: '2px dashed var(--color-primary)', pointerEvents: 'none',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            backgroundColor: 'rgba(16, 185, 129, 0.15)',
            boxShadow: '0 0 0 9999px rgba(0, 0, 0, 0.4)' // Dim the outside area
          }}>
            <div style={{ width: '30px', height: '2px', backgroundColor: 'var(--color-primary)', position: 'absolute' }} />
            <div style={{ width: '2px', height: '30px', backgroundColor: 'var(--color-primary)', position: 'absolute' }} />
            <div style={{ position: 'absolute', top: '-24px', left: 0, color: 'var(--color-primary)', fontSize: '0.75rem', fontWeight: 'bold', textShadow: '0 0 4px #000' }}>
              VÙNG BẢN ĐỒ TẢI VỀ
            </div>
          </div>
          <div style={{
            position: 'absolute', top: '10px', left: '10px',
            backgroundColor: 'rgba(255, 255, 255, 0.9)', padding: '4px 8px', borderRadius: '4px',
            color: 'var(--color-primary)', fontSize: '0.75rem', border: '1px solid var(--color-primary)'
          }}>
            ONLINE OSM PREVIEW
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: '12px', marginBottom: '20px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.7rem', color: 'var(--color-text-muted)', marginBottom: '4px', textTransform: 'uppercase' }}>Min Longitude</label>
            <input 
              type="number" step="0.0001" value={minLon} onChange={e => setMinLon(e.target.value)}
              disabled={isDownloading}
              style={{ width: '100%', padding: '6px', backgroundColor: 'var(--color-bg-base)', border: '1px solid var(--color-border)', color: 'var(--color-primary)', borderRadius: '4px', fontFamily: 'var(--font-family-mono)' }}
            />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: '0.7rem', color: 'var(--color-text-muted)', marginBottom: '4px', textTransform: 'uppercase' }}>Min Latitude</label>
            <input 
              type="number" step="0.0001" value={minLat} onChange={e => setMinLat(e.target.value)}
              disabled={isDownloading}
              style={{ width: '100%', padding: '6px', backgroundColor: 'var(--color-bg-base)', border: '1px solid var(--color-border)', color: 'var(--color-primary)', borderRadius: '4px', fontFamily: 'var(--font-family-mono)' }}
            />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: '0.7rem', color: 'var(--color-text-muted)', marginBottom: '4px', textTransform: 'uppercase' }}>Max Longitude</label>
            <input 
              type="number" step="0.0001" value={maxLon} onChange={e => setMaxLon(e.target.value)}
              disabled={isDownloading}
              style={{ width: '100%', padding: '6px', backgroundColor: 'var(--color-bg-base)', border: '1px solid var(--color-border)', color: 'var(--color-primary)', borderRadius: '4px', fontFamily: 'var(--font-family-mono)' }}
            />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: '0.7rem', color: 'var(--color-text-muted)', marginBottom: '4px', textTransform: 'uppercase' }}>Max Latitude</label>
            <input 
              type="number" step="0.0001" value={maxLat} onChange={e => setMaxLat(e.target.value)}
              disabled={isDownloading}
              style={{ width: '100%', padding: '6px', backgroundColor: 'var(--color-bg-base)', border: '1px solid var(--color-border)', color: 'var(--color-primary)', borderRadius: '4px', fontFamily: 'var(--font-family-mono)' }}
            />
          </div>
        </div>

        <div style={{ marginBottom: '20px' }}>
          <label style={{ display: 'block', fontSize: '0.75rem', color: 'var(--color-text-muted)', marginBottom: '4px' }}>
            {t('map_source_url')}
          </label>
          <input 
            type="text" value={sourceUrl} onChange={e => setSourceUrl(e.target.value)}
            disabled={isDownloading}
            placeholder="https://..."
            style={{ width: '100%', padding: '8px', backgroundColor: 'var(--color-bg-base)', border: '1px solid var(--color-border)', color: 'var(--color-text-main)', borderRadius: '4px', boxSizing: 'border-box' }}
          />
        </div>

        {/* Progress Bar Area */}
        {showProgress && (
          <div style={{ marginBottom: '24px', backgroundColor: 'var(--color-bg-surface)', padding: '12px', borderRadius: '4px', border: '1px solid var(--color-border)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', color: (errorMsg || mapDownloadProgress < 0) ? 'var(--color-status-critical)' : 'var(--color-primary)', marginBottom: '8px', fontWeight: 'bold' }}>
              <span>{errorMsg ? errorMsg : (isRequesting ? 'Đang kết nối / Kiểm tra mạng...' : (mapDownloadStatus || t('downloading')))}</span>
              <span>{errorMsg ? 'LỖI' : (isRequesting ? '0%' : (mapDownloadProgress < 0 ? 'LỖI' : mapDownloadProgress + '%'))}</span>
            </div>
            <div style={{ width: '100%', height: '8px', backgroundColor: 'var(--color-bg-base)', borderRadius: '4px', overflow: 'hidden' }}>
              <div style={{ 
                height: '100%', 
                width: errorMsg ? '100%' : (isRequesting ? '5%' : (mapDownloadProgress < 0 ? '100%' : `${mapDownloadProgress}%`)), 
                backgroundColor: (errorMsg || mapDownloadProgress < 0) ? 'var(--color-status-critical)' : (mapDownloadProgress === 100 ? 'var(--color-status-ok)' : 'var(--color-primary)'),
                transition: 'width 0.3s ease',
                boxShadow: (errorMsg || mapDownloadProgress < 0) ? '0 0 8px var(--color-status-critical-glow)' : '0 0 8px var(--color-primary-glow)',
                animation: (isRequesting && !errorMsg) ? 'pulse-primary 1.5s infinite' : 'none'
              }} />
            </div>
          </div>
        )}

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
          <button 
            onClick={onClose} 
            disabled={isDownloading}
            style={{ 
              padding: '8px 24px', backgroundColor: 'transparent', border: '1px solid var(--color-border)', 
              color: 'var(--color-text-muted)', borderRadius: '4px', cursor: isDownloading ? 'not-allowed' : 'pointer',
              opacity: isDownloading ? 0.5 : 1, textTransform: 'uppercase', fontSize: '0.875rem'
            }}
          >
            {t('cancel')}
          </button>
          
          {mapDownloadProgress === 100 ? (
            <button 
              onClick={onClose}
              style={{ 
                padding: '8px 24px', backgroundColor: 'var(--color-status-ok)', border: 'none', 
                color: '#fff', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold', textTransform: 'uppercase', fontSize: '0.875rem'
              }}
            >
              DONE
            </button>
          ) : (
            <button 
              onClick={handleDownload} 
              disabled={isDownloading || isRequesting}
              style={{ 
                padding: '8px 24px', backgroundColor: 'var(--color-primary)', border: '1px solid var(--color-primary)', 
                color: '#000', borderRadius: '4px', cursor: (isDownloading || isRequesting) ? 'not-allowed' : 'pointer',
                fontWeight: 'bold', opacity: (isDownloading || isRequesting) ? 0.7 : 1, textTransform: 'uppercase', fontSize: '0.875rem',
                boxShadow: '0 0 15px var(--color-primary-glow)'
              }}
            >
              {isRequesting ? t('downloading') : t('download_map')}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default MapManagerModal;
