import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';

import { useMeshStore, type SoldierNode } from '../stores/useMeshStore';
import EventLog from './EventLog';

interface TacticalPanelProps {
  activeTab: 'nodes' | 'chat' | 'logs';
}

const TacticalPanel: React.FC<TacticalPanelProps> = ({ activeTab }) => {
  const { t } = useTranslation();
  const nodes = useMeshStore((state) => state.nodes);
  const updateNode = useMeshStore((state) => state.updateNode);
  const nodeList = Object.values(nodes);
  
  const [message, setMessage] = useState('');
  const [targetType, setTargetType] = useState<'broadcast'|'specific'>('broadcast');
  const [targetNodeId, setTargetNodeId] = useState<string>('');
  const [isSending, setIsSending] = useState(false);
  const [chatHistory, setChatHistory] = useState<Array<{id: number, text: string, time: string, target: string, status: 'sent'|'error'|'ai_response'}>>([]);
  const [isEditingQuickMsgs, setIsEditingQuickMsgs] = useState(false);
  const [isAiMode, setIsAiMode] = useState(false);

  const isDemoMode = useMeshStore(state => state.isDemoMode);
  const quickMessages = useMeshStore(state => state.quickMessages);
  const updateQuickMessage = useMeshStore(state => state.updateQuickMessage);

  React.useEffect(() => {
    if (!isDemoMode) return;

    const demoNodes = [
      { id: 101, lat: 12.2388, lng: 109.1967, hr: 75, temp: 36.5, bat: 4.1 },
      { id: 102, lat: 12.2395, lng: 109.1955, hr: 125, temp: 37.2, bat: 3.8 },
      { id: 103, lat: 12.2370, lng: 109.1980, hr: 80, temp: 36.8, bat: 3.2 },
      { id: 104, lat: 12.2410, lng: 109.1940, hr: 65, temp: 36.4, bat: 4.0 },
      { id: 105, lat: 12.2360, lng: 109.1990, hr: 90, temp: 36.6, bat: 3.9 },
    ];

    const pushDemoData = () => {
      demoNodes.forEach(n => {
        n.lat += (Math.random() - 0.5) * 0.0002;
        n.lng += (Math.random() - 0.5) * 0.0002;
        n.hr += Math.floor((Math.random() - 0.5) * 6);
        if (n.hr < 60) n.hr = 60;
        if (n.hr > 180) n.hr = 180;

        const isCritBat = n.bat < 3.3;
        const isAlert = n.hr > 120;

        updateNode({
          node_id: n.id,
          name: `Alpha-${n.id}`,
          latitude: n.lat,
          longitude: n.lng,
          heading: Math.floor(Math.random() * 360),
          heart_rate: n.hr,
          spo2: Math.floor(95 + Math.random() * 5),
          temperature: n.temp,
          humidity: 60,
          pressure: 1013,
          battery_voltage: n.bat,
          status_flags: 0,
          flags: {
            gps_fix: true,
            imu_valid: true,
            hr_valid: true,
            spo2_valid: true,
            temp_valid: true,
            low_battery: n.bat < 3.5,
            critical_battery: isCritBat,
            sensor_error: false,
            alert: isAlert,
            man_down: false,
            heat_stress: false,
          },
          last_seen: new Date().toISOString()
        });
      });
    };

    pushDemoData();
    const interval = setInterval(pushDemoData, 3000);
    return () => clearInterval(interval);
  }, [isDemoMode, updateNode]);

  const getStatusColor = (node: SoldierNode) => {
    if (node.flags.man_down || node.flags.critical_battery) return '#ef4444'; // CRITICAL red
    if (node.flags.alert || node.flags.heat_stress || node.flags.low_battery) return '#f59e0b'; // WARNING orange
    return '#10b981'; // OK green
  };
  
  const getStatusText = (node: SoldierNode) => {
    if (node.flags.man_down || node.flags.critical_battery) return t('status_critical');
    if (node.flags.alert || node.flags.heat_stress || node.flags.low_battery) return t('status_warning');
    return t('status_ok');
  };

  const sendMessageRequest = async (msgText: string, tType: 'broadcast'|'specific', tNodeId: string) => {
    if (!msgText.trim()) return;
    
    // Validate specific target
    if (tType === 'specific' && !tNodeId) {
      alert(t('error_select_target') || 'Please select a target node');
      return;
    }

    setIsSending(true);
    
    try {
      const payloadHex = Array.from(new TextEncoder().encode(msgText))
        .map(b => b.toString(16).padStart(2, '0'))
        .join('');

      const response = await fetch('/api/commands/', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          target_node_id: tType === 'broadcast' ? null : Number(tNodeId),
          command_type: tType === 'broadcast' ? 'BROADCAST' : 'DIRECT_MESSAGE',
          payload_hex: payloadHex
        })
      });

      if (response.ok) {
        if (msgText === message.trim()) setMessage('');
        setChatHistory(prev => [...prev, {
          id: Date.now(),
          text: msgText,
          time: new Date().toLocaleTimeString(),
          target: tType === 'broadcast' ? 'BROADCAST' : `Node ${tNodeId}`,
          status: 'sent'
        }]);
      } else {
        console.error('Failed to send message:', await response.text());
        setChatHistory(prev => [...prev, {
          id: Date.now(),
          text: msgText,
          time: new Date().toLocaleTimeString(),
          target: tType === 'broadcast' ? 'BROADCAST' : `Node ${tNodeId}`,
          status: 'error'
        }]);
      }
    } catch (error) {
      console.error('Network error sending message:', error);
      setChatHistory(prev => [...prev, {
        id: Date.now(),
        text: msgText,
        time: new Date().toLocaleTimeString(),
        target: tType === 'broadcast' ? 'BROADCAST' : `Node ${tNodeId}`,
        status: 'error'
      }]);
    } finally {
      setIsSending(false);
    }
  };

  const handleSendMessage = async () => {
    if (!message.trim()) return;

    if (isAiMode) {
      setIsSending(true);
      const newMsg = { id: Date.now(), text: message, time: new Date().toLocaleTimeString(), target: 'AI Assistant', status: 'sent' as const };
      setChatHistory(prev => [...prev, newMsg]);

      try {
        // MUST use relative URL /api/llm/chat to avoid CORS when served by Nginx
        const res = await fetch('/api/llm/chat', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ message })
        });
        const data = await res.json();
        
        const aiResponseText = `🧠 [AI ACTION]: ${data.action}\nPayload: ${data.payload}\nTarget: ${data.target}`;
        setChatHistory(prev => [...prev, { 
          id: Date.now() + 1, 
          text: aiResponseText, 
          time: new Date().toLocaleTimeString(), 
          target: 'Commander', 
          status: 'ai_response' as const
        }]);

        // Auto-execute if valid action
        if (data.action === "SEND_SOS" || data.action === "BROADCAST_MSG") {
            const actualType = data.target === 'ALL' ? 'broadcast' : 'specific';
            await sendMessageRequest(data.payload, actualType, data.target === 'ALL' ? '' : data.target);
        }

      } catch (err) {
        setChatHistory(prev => prev.map(m => m.id === newMsg.id ? { ...m, status: 'error' } : m));
      } finally {
        setIsSending(false);
        setMessage('');
      }
      return;
    }

    sendMessageRequest(message.trim(), targetType, targetNodeId);
  };

  const handleEmergency = () => {
    const sosMessage = "EMERGENCY / SOS";
    sendMessageRequest(sosMessage, 'broadcast', '');
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', backgroundColor: '#1a1f16' }}>
      
      {/* Network Overview */}
      <div style={{ padding: '8px 16px', display: 'flex', justifyContent: 'space-between', backgroundColor: '#2d3328', borderBottom: '1px solid #1a1f16' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <div style={{ width: 10, height: 10, borderRadius: '50%', backgroundColor: '#10b981' }} />
          <span style={{ fontSize: '0.85rem', color: '#10b981', fontWeight: 'bold' }}>{t('active')}: {nodeList.length}</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <span style={{ fontSize: '0.85rem', color: '#9ca3af' }}>📡 {t('last_rx')}:</span>
          <span style={{ fontSize: '0.85rem', color: '#d1d5db', fontFamily: 'monospace' }}>{new Date().toLocaleTimeString()}</span>
        </div>
      </div>

      {/* Soldier Table Tab */}
      {activeTab === 'nodes' && (
        <div style={{ flex: 1, overflowY: 'auto', padding: '0', minHeight: 0 }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem', textAlign: 'left' }}>
            <thead style={{ position: 'sticky', top: 0, backgroundColor: '#2d3328', zIndex: 1, boxShadow: '0 2px 4px rgba(0,0,0,0.2)' }}>
              <tr style={{ color: '#facc15' }}>
                <th style={{ padding: '8px 12px', fontWeight: 'bold' }}>ID</th>
                <th style={{ padding: '8px 12px', fontWeight: 'bold' }}>STS</th>
                <th style={{ padding: '8px 12px', fontWeight: 'bold' }}>{t('heart_rate')}</th>
                <th style={{ padding: '8px 12px', fontWeight: 'bold' }}>{t('oxygen')}</th>
                <th style={{ padding: '8px 12px', fontWeight: 'bold' }}>{t('temp')}</th>
                <th style={{ padding: '8px 12px', fontWeight: 'bold' }}>{t('battery')}</th>
              </tr>
            </thead>
            <tbody>
              {nodeList.map(node => (
                <tr key={node.node_id} style={{ borderBottom: '1px solid #2d3328', color: '#e2e8f0', cursor: 'pointer' }} className="table-row-hover">
                  <td style={{ padding: '10px 12px', fontWeight: 'bold' }}>{node.node_id}</td>
                  <td style={{ padding: '10px 12px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <div className={getStatusText(node) === 'CRITICAL' ? 'critical-pulse' : ''} style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: getStatusColor(node) }} />
                      <span style={{ color: getStatusColor(node), fontWeight: 'bold', fontSize: '0.75rem' }}>{getStatusText(node)}</span>
                    </div>
                  </td>
                  <td style={{ padding: '10px 12px' }}>{node.heart_rate}</td>
                  <td style={{ padding: '10px 12px', color: '#10b981' }}>{node.spo2}%</td>
                  <td style={{ padding: '10px 12px', color: '#10b981' }}>{node.temperature.toFixed(1)}°</td>
                  <td style={{ padding: '10px 12px' }}>{node.battery_voltage.toFixed(1)}V</td>
                </tr>
              ))}
              {nodeList.length === 0 && (
                <tr>
                  <td colSpan={6} style={{ padding: '16px', textAlign: 'center', color: '#6b7280' }}>{t('no_recent_events')}</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Command & Control Tab */}
      {activeTab === 'chat' && (
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: '16px', backgroundColor: '#1a1f16', overflowY: 'auto', minHeight: 0 }}>
          
          {/* Chat History */}
          <div style={{ flex: 1, minHeight: 0, overflowY: 'auto', marginBottom: '16px', border: '1px solid #2d3328', borderRadius: '4px', backgroundColor: '#11140f', padding: '12px' }}>
            {chatHistory.length === 0 ? (
              <div style={{ color: '#4b5563', textAlign: 'center', marginTop: '20px', fontSize: '0.85rem' }}>
                {t('no_messages') || 'No messages sent in this session.'}
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {chatHistory.map(msg => (
                  <div key={msg.id} style={{ alignSelf: 'flex-end', maxWidth: '85%', backgroundColor: '#2d3328', padding: '8px 12px', borderRadius: '8px', borderBottomRightRadius: 0 }}>
                    <div style={{ fontSize: '0.7rem', color: '#9ca3af', marginBottom: '4px', display: 'flex', justifyContent: 'space-between', gap: '16px' }}>
                      <span>TO: {msg.target}</span>
                      <span>{msg.time}</span>
                    </div>
                    <div style={{ color: '#e2e8f0', fontSize: '0.9rem', wordBreak: 'break-word', whiteSpace: 'pre-wrap' }}>
                      {msg.text}
                    </div>
                    {msg.status === 'error' && (
                      <div style={{ color: '#ef4444', fontSize: '0.7rem', marginTop: '4px', fontWeight: 'bold' }}>FAILED TO SEND</div>
                    )}
                    {msg.status === 'ai_response' && (
                      <div style={{ color: '#a855f7', fontSize: '0.7rem', marginTop: '4px', fontWeight: 'bold' }}>AI EXECUTED</div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Quick Messages Board */}
          <div style={{ marginBottom: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <span style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#9ca3af', display: 'block' }}>⚡ {t('quick_messages')}</span>
              <button 
                onClick={() => setIsEditingQuickMsgs(!isEditingQuickMsgs)}
                style={{ background: 'none', border: 'none', color: isEditingQuickMsgs ? '#10b981' : '#9ca3af', cursor: 'pointer', fontSize: '0.9rem' }}
                title={isEditingQuickMsgs ? "Save Quick Messages" : "Edit Quick Messages"}
              >
                {isEditingQuickMsgs ? '✅' : '✏️'}
              </button>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
              {quickMessages.map((msg, idx) => {
                const getDisplayMsg = (rawMsg: string) => {
                  if (rawMsg === "MOVE TO RALLY POINT" || rawMsg === "msg_rally") return t('msg_rally');
                  if (rawMsg === "MEDIC REQUIRED" || rawMsg === "msg_medic") return t('msg_medic');
                  if (rawMsg === "ENEMY SPOTTED" || rawMsg === "msg_enemy") return t('msg_enemy');
                  if (rawMsg === "ALL CLEAR" || rawMsg === "msg_clear") return t('msg_clear');
                  return rawMsg;
                };
                
                const displayMsg = getDisplayMsg(msg);
                const colors = ['#10b981', '#3b82f6', '#f59e0b', '#d1d5db'];
                
                if (isEditingQuickMsgs) {
                  return (
                    <input 
                      key={`qm-edit-${idx}`}
                      type="text" 
                      value={displayMsg} 
                      onChange={(e) => updateQuickMessage(idx, e.target.value)}
                      style={{ padding: '8px', background: '#1a1f16', color: colors[idx], border: `1px solid ${colors[idx]}55`, borderRadius: '4px', fontSize: '0.8rem', fontWeight: 'bold' }}
                    />
                  );
                }
                return (
                  <button 
                    key={`qm-btn-${idx}`}
                    onClick={() => setMessage(displayMsg)}
                    style={{ padding: '8px', background: '#2d3328', color: colors[idx], border: '1px solid #4b5563', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem', fontWeight: 'bold', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}
                    title={displayMsg}
                  >
                    {displayMsg}
                  </button>
                );
              })}
            </div>
            
            <button 
              onClick={handleEmergency}
              disabled={isSending}
              style={{ width: '100%', marginTop: '8px', padding: '10px', background: '#ef4444', color: '#fff', border: 'none', borderRadius: '4px', cursor: isSending ? 'not-allowed' : 'pointer', fontSize: '0.9rem', fontWeight: 'bold', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', boxShadow: '0 2px 4px rgba(239, 68, 68, 0.4)' }}
            >
              🚨 {t('emergency')}
            </button>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#d1d5db', marginBottom: '8px', textTransform: 'uppercase' }}>💬 {t('compose_msg') || 'COMPOSE MESSAGE'}</span>
            
            <div style={{ display: 'flex', gap: '8px', marginBottom: '8px' }}>
              <button 
                onClick={() => setTargetType('broadcast')}
                style={{ flex: 1, background: targetType === 'broadcast' ? '#3b82f6' : '#2d3328', border: 'none', color: targetType === 'broadcast' ? '#fff' : '#9ca3af', padding: '8px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85rem', fontWeight: 'bold', transition: 'background 0.2s' }}
              >
                {t('target_broadcast')}
              </button>
              <button 
                onClick={() => setTargetType('specific')}
                style={{ flex: 1, background: targetType === 'specific' ? '#3b82f6' : '#2d3328', border: 'none', color: targetType === 'specific' ? '#fff' : '#9ca3af', padding: '8px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.85rem', fontWeight: 'bold', transition: 'background 0.2s' }}
              >
                {t('target_specific')}
              </button>
            </div>

            {targetType === 'specific' && !isAiMode && (
              <select 
                value={targetNodeId} 
                onChange={(e) => setTargetNodeId(e.target.value)}
                style={{ width: '100%', marginBottom: '8px', padding: '8px', background: '#2d3328', border: '1px solid #4b5563', borderRadius: '4px', color: '#fff', fontSize: '0.9rem' }}
              >
                <option value="" disabled>-- {t('select_target') || 'Select Unit'} --</option>
                {nodeList.map(n => (
                  <option key={n.node_id} value={n.node_id}>Node {n.node_id}</option>
                ))}
              </select>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '8px' }}>
              <button 
                onClick={() => setIsAiMode(!isAiMode)}
                style={{ background: isAiMode ? '#a855f7' : 'transparent', color: isAiMode ? '#fff' : '#a855f7', border: '1px solid #a855f7', padding: '4px 8px', borderRadius: '4px', cursor: 'pointer', fontSize: '0.8rem', fontWeight: 'bold' }}
              >
                {isAiMode ? '🤖 AI Copilot ON' : '🤖 Enable AI Copilot'}
              </button>
            </div>

            <textarea 
              placeholder={isAiMode ? "Ra lệnh bằng lời nói (VD: Gửi thông báo khẩn rút lui cho toàn đội)" : t('type_msg')} 
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              disabled={isSending}
              style={{ minHeight: '80px', background: isAiMode ? '#2e1065' : '#2d3328', border: isAiMode ? '1px solid #a855f7' : '1px solid #4b5563', borderRadius: '4px', padding: '12px', color: '#fff', fontSize: '1rem', marginBottom: '8px', resize: 'vertical', fontFamily: 'monospace' }}
            />
          </div>
          
          <button 
            onClick={handleSendMessage}
            disabled={!message.trim() || isSending}
            style={{ 
              width: '100%', 
              padding: '12px', 
              background: message.trim() && !isSending ? '#65a30d' : '#4b5563', 
              color: '#fff', 
              border: 'none', 
              borderRadius: '4px', 
              cursor: message.trim() && !isSending ? 'pointer' : 'not-allowed', 
              fontWeight: 'bold', 
              fontSize: '1rem',
              transition: 'background 0.2s',
              boxShadow: message.trim() && !isSending ? '0 4px 6px rgba(0,0,0,0.3)' : 'none'
            }}
          >
            {isSending ? (t('sending') || 'SENDING...') : t('send_msg')}
          </button>
        </div>
      )}

      {/* Logs Tab */}
      {activeTab === 'logs' && (
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minHeight: 0 }}>
          <EventLog />
        </div>
      )}

    </div>
  );
};

export default TacticalPanel;
