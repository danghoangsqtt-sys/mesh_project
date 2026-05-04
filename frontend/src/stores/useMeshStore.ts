import { create } from 'zustand';

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

interface MeshStore {
  nodes: Record<number, SoldierNode>;
  events: MeshEvent[];
  connectionStatus: 'connecting' | 'connected' | 'disconnected';
  updateNode: (node: SoldierNode) => void;
  addEvent: (event: MeshEvent) => void;
  setConnectionStatus: (status: 'connecting' | 'connected' | 'disconnected') => void;
}

export const useMeshStore = create<MeshStore>((set) => ({
  nodes: {},
  events: [],
  connectionStatus: 'disconnected',
  updateNode: (node) => 
    set((state) => ({
      nodes: { ...state.nodes, [node.node_id]: node }
    })),
  addEvent: (event) =>
    set((state) => ({
      events: [event, ...state.events].slice(0, 100) // Keep last 100 events
    })),
  setConnectionStatus: (status) => set({ connectionStatus: status }),
}));
