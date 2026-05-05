import { create } from 'zustand';

const defaultQuickMessages = [
  "msg_rally",
  "msg_medic",
  "msg_enemy",
  "msg_clear"
];

const getInitialQuickMessages = () => {
  try {
    const stored = localStorage.getItem('mesh_quick_messages');
    if (stored) return JSON.parse(stored);
  } catch (e) {
    console.error('Failed to load quick messages', e);
  }
  return defaultQuickMessages;
};

export interface SoldierNode {
  node_id: number;
  name?: string;
  latitude: number;
  longitude: number;
  heading: number;
  heart_rate: number;
  spo2: number;
  temperature: number;
  humidity: number;
  pressure: number;
  battery_voltage: number;
  status_flags: number;
  flags: {
    gps_fix: boolean;
    imu_valid: boolean;
    hr_valid: boolean;
    spo2_valid: boolean;
    temp_valid: boolean;
    low_battery: boolean;
    critical_battery: boolean;
    sensor_error: boolean;
    alert: boolean;
    man_down: boolean;
    heat_stress: boolean;
  };
  last_seen: string;
}

export interface MeshEvent {
  id: number;
  node_id: number | null;
  event_type: string;
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  message: string;
  created_at: string;
}

export interface RawLog {
  timestamp: string;
  direction: 'RX' | 'TX';
  hex: string;
}

interface MeshStore {
  nodes: Record<number, SoldierNode>;
  events: MeshEvent[];
  connectionStatus: 'connecting' | 'connected' | 'disconnected';
  mapDownloadProgress: number;
  mapDownloadStatus: string;
  mapVersion: number;
  updateNode: (node: SoldierNode) => void;
  addEvent: (event: MeshEvent) => void;
  setConnectionStatus: (status: 'connecting' | 'connected' | 'disconnected') => void;
  setMapDownloadState: (progress: number, status: string) => void;
  incrementMapVersion: () => void;
  rawLogs: RawLog[];
  addRawLog: (log: RawLog) => void;
  clearNodes: () => void;
  isDemoMode: boolean;
  setDemoMode: (val: boolean) => void;
  quickMessages: string[];
  updateQuickMessage: (index: number, msg: string) => void;
}

export const useMeshStore = create<MeshStore>((set) => ({
  nodes: {},
  events: [],
  connectionStatus: 'disconnected',
  mapDownloadProgress: 0,
  mapDownloadStatus: '',
  mapVersion: 0,
  updateNode: (node) => 
    set((state) => ({
      nodes: { ...state.nodes, [node.node_id]: node }
    })),
  addEvent: (event) =>
    set((state) => ({
      events: [event, ...state.events].slice(0, 100) // Keep last 100 events
    })),
  setConnectionStatus: (status) => set({ connectionStatus: status }),
  setMapDownloadState: (progress, status) => set({ mapDownloadProgress: progress, mapDownloadStatus: status }),
  incrementMapVersion: () => set((state) => ({ mapVersion: state.mapVersion + 1 })),
  rawLogs: [],
  addRawLog: (log) =>
    set((state) => ({
      rawLogs: [...state.rawLogs, log].slice(-200) // Keep last 200 logs, newer at end
    })),
  clearNodes: () => set({ nodes: {} }),
  isDemoMode: false,
  setDemoMode: (val) => set({ isDemoMode: val }),
  quickMessages: getInitialQuickMessages(),
  updateQuickMessage: (index, msg) => set((state) => {
    const newMsgs = [...state.quickMessages];
    newMsgs[index] = msg;
    try {
      localStorage.setItem('mesh_quick_messages', JSON.stringify(newMsgs));
    } catch (e) {
      console.error('Failed to save quick messages', e);
    }
    return { quickMessages: newMsgs };
  }),
}));
