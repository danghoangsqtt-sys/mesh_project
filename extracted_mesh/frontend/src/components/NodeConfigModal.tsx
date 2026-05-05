import React, { useState, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import type { SoldierNode } from '../stores/useMeshStore';

interface NodeConfigModalProps {
  node: SoldierNode;
  onClose: () => void;
}

const NodeConfigModal: React.FC<NodeConfigModalProps> = ({ node, onClose }) => {
  const { t } = useTranslation();
  const [txRate, setTxRate] = useState<number>(10);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleSave = async () => {
    setIsSubmitting(true);
    setMessage('');
    try {
      const payloadHex = txRate.toString(16).padStart(2, '0');
      
      const response = await fetch('/api/commands', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          target_node_id: node.node_id,
          command_type: 'CONFIG',
          payload_hex: payloadHex
        })
      });

      if (!response.ok) throw new Error('Failed to send command');
      setMessage('Command sent successfully. Waiting for ACK...');
      setTimeout(onClose, 2000);
    } catch (err) {
      console.error(err);
      setMessage('Error sending command.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleOTAUpload = async () => {
    if (!fileInputRef.current?.files?.length) return;
    const file = fileInputRef.current.files[0];
    if (!file.name.endsWith('.bin')) {
      setMessage('Error: Only .bin files allowed for OTA.');
      return;
    }

    setIsSubmitting(true);
    setMessage('Uploading firmware...');
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      const response = await fetch(`/api/ota/upload/${node.node_id}`, {
        method: 'POST',
        body: formData
      });

      if (!response.ok) throw new Error('Upload failed');
      setMessage('OTA Update started. Check node logs.');
    } catch (err) {
      console.error(err);
      setMessage('Error starting OTA update.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{
      position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.5)', zIndex: 100,
      display: 'flex', alignItems: 'center', justifyContent: 'center'
    }}>
      <div className="glass-panel" style={{ padding: 'var(--spacing-lg)', width: '350px' }}>
        <h3 style={{ marginTop: 0, color: 'var(--color-primary)' }}>{t('config_node')} {node.node_id}</h3>
        
        <div style={{ marginBottom: 'var(--spacing-md)' }}>
          <label style={{ display: 'block', fontSize: '0.875rem', marginBottom: 'var(--spacing-xs)' }}>{t('tx_rate')}</label>
          <div style={{ display: 'flex', gap: '8px' }}>
            <input 
              type="number" 
              value={txRate} 
              onChange={(e) => setTxRate(Number(e.target.value))}
              style={{ 
                flex: 1, padding: 'var(--spacing-sm)', 
                backgroundColor: 'var(--color-bg-base)', color: 'white', 
                border: '1px solid var(--color-border)', borderRadius: 'var(--radius-sm)'
              }}
            />
            <button onClick={handleSave} disabled={isSubmitting} style={{ 
              padding: 'var(--spacing-sm) var(--spacing-md)', 
              backgroundColor: 'var(--color-primary)', color: 'white', border: 'none', borderRadius: 'var(--radius-sm)', cursor: 'pointer' 
            }}>{t('set')}</button>
          </div>
        </div>

        <div style={{ marginBottom: 'var(--spacing-md)', borderTop: '1px solid var(--color-border)', paddingTop: 'var(--spacing-md)' }}>
          <label style={{ display: 'block', fontSize: '0.875rem', marginBottom: 'var(--spacing-xs)' }}>{t('ota_update')}</label>
          <input 
            type="file" 
            accept=".bin"
            ref={fileInputRef}
            style={{ 
              width: '100%', marginBottom: '8px', fontSize: '0.875rem', color: 'var(--color-text-muted)'
            }}
          />
          <button onClick={handleOTAUpload} disabled={isSubmitting} style={{ 
            width: '100%', padding: 'var(--spacing-sm)', 
            backgroundColor: 'var(--color-secondary)', color: 'white', border: 'none', borderRadius: 'var(--radius-sm)', cursor: 'pointer' 
          }}>{t('start_ota')}</button>
        </div>

        {message && <div style={{ fontSize: '0.875rem', marginBottom: 'var(--spacing-md)', color: message.includes('Error') ? 'var(--color-status-critical)' : 'var(--color-status-ok)' }}>{message}</div>}

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 'var(--spacing-sm)' }}>
          <button onClick={onClose} disabled={isSubmitting} style={{ 
            padding: 'var(--spacing-sm) var(--spacing-md)', 
            backgroundColor: 'transparent', color: 'white', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-sm)', cursor: 'pointer' 
          }}>{t('close')}</button>
        </div>
      </div>
    </div>
  );
};

export default NodeConfigModal;
