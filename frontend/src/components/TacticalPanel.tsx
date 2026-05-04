import React, { useState } from 'react';
import { useMeshStore, SoldierNode } from '../stores/useMeshStore';
import { Activity, Thermometer, Droplets, Battery, AlertTriangle } from 'lucide-react';

const TacticalPanel: React.FC = () => {
  const nodes = useMeshStore((state) => state.nodes);
  const nodeList = Object.values(nodes);
  const [selectedNode, setSelectedNode] = useState<number | null>(null);

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
                {node.flags.alert && <AlertTriangle size={16} color="var(--color-status-warning)" />}
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '4px', marginTop: '8px', fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Activity size={14} /> {node.heart_rate} bpm
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Thermometer size={14} /> {node.temperature.toFixed(1)}°C
                </div>
                {selectedNode === node.node_id && (
                  <>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Droplets size={14} /> {node.humidity.toFixed(0)}%
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                      <Battery size={14} /> {node.battery_voltage.toFixed(2)}V
                    </div>
                    <div style={{ gridColumn: '1 / -1', marginTop: '4px', paddingTop: '4px', borderTop: '1px solid rgba(255,255,255,0.1)' }}>
                      Spo2: {node.spo2}% | GPS Fix: {node.flags.gps_fix ? 'Yes' : 'No'}
                    </div>
                  </>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default TacticalPanel;
