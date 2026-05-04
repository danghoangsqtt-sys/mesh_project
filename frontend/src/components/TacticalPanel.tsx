import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMeshStore, SoldierNode } from '../stores/useMeshStore';
import { Activity, Thermometer, Droplets, Battery, AlertTriangle, Settings } from 'lucide-react';
import NodeConfigModal from './NodeConfigModal';

const TacticalPanel: React.FC = () => {
  const { t } = useTranslation();
  const nodes = useMeshStore((state) => state.nodes);
  const nodeList = Object.values(nodes);
  const [selectedNode, setSelectedNode] = useState<number | null>(null);
  const [configNode, setConfigNode] = useState<SoldierNode | null>(null);

  const getStatusColor = (node: SoldierNode) => {
    if (node.flags.man_down || node.flags.critical_battery) return 'var(--color-status-critical)';
    if (node.flags.alert || node.flags.heat_stress || node.flags.low_battery) return 'var(--color-status-warning)';
    return 'var(--color-status-ok)';
  };

  return (
    <div className="glass-panel" style={{ 
      width: '320px', 
      display: 'flex', 
      flexDirection: 'column',
      height: '100%',
      overflow: 'hidden'
    }}>
      <div style={{ padding: 'var(--spacing-md)', borderBottom: '1px solid var(--color-border)' }}>
        <h2 style={{ fontSize: '1.25rem' }}>Active Units ({nodeList.length})</h2>
      </div>

      <div style={{ flex: 1, overflowY: 'auto', padding: 'var(--spacing-sm)' }}>
        {nodeList.length === 0 ? (
          <div style={{ color: 'var(--color-text-muted)', textAlign: 'center', marginTop: 'var(--spacing-xl)' }}>
            No units detected
          </div>
        ) : (
          nodeList.map((node) => (
            <div 
              key={node.node_id}
              onClick={() => setSelectedNode(node.node_id === selectedNode ? null : node.node_id)}
              style={{
                padding: 'var(--spacing-sm)',
                marginBottom: 'var(--spacing-sm)',
                backgroundColor: 'rgba(255,255,255,0.05)',
                borderRadius: 'var(--radius-sm)',
                cursor: 'pointer',
                borderLeft: `4px solid ${getStatusColor(node)}`,
                border: selectedNode === node.node_id ? '1px solid var(--color-primary)' : undefined,
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <strong style={{ fontSize: '1.1rem' }}>{node.name || `Unit ${node.node_id}`}</strong>
                <div style={{ display: 'flex', gap: '8px' }}>
                  {node.flags.alert && <AlertTriangle size={16} color="var(--color-status-warning)" />}
                  {selectedNode === node.node_id && (
                    <Settings 
                      size={16} 
                      color="var(--color-text-muted)" 
                      style={{ cursor: 'pointer' }}
                      onClick={(e) => { e.stopPropagation(); setConfigNode(node); }}
                    />
                  )}
                </div>
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '4px', marginTop: '8px', fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Activity size={14} color={node.heart_rate < 60 || node.heart_rate > 100 ? 'var(--color-status-critical)' : 'inherit'} /> 
                  <span title={t('heart_rate')}>{node.heart_rate} bpm</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Droplets size={14} color={node.spo2 < 90 ? 'var(--color-status-critical)' : 'inherit'} /> 
                  <span title={t('oxygen')}>{node.spo2}%</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Thermometer size={14} /> <span title={t('temp')}>{node.temperature.toFixed(1)}°C</span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Battery size={14} color={node.battery_voltage < 3.3 ? 'var(--color-status-critical)' : 'inherit'} /> 
                  <span title={t('battery')}>{node.battery_voltage.toFixed(2)}V</span>
                </div>
              </div>
              
              {selectedNode === node.node_id && (
                <>
                  <div style={{ marginTop: '8px', fontSize: '0.8rem', color: 'var(--color-text-muted)', textAlign: 'right' }}>
                    {t('last_seen')}: {new Date(node.last_seen).toLocaleTimeString()}
                  </div>
                </>
              )}
            </div>
          ))
        )}
      </div>
      
      {configNode && (
        <NodeConfigModal node={configNode} onClose={() => setConfigNode(null)} />
      )}
    </div>
  );
};

export default TacticalPanel;
