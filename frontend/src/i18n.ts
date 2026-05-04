import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

const resources = {
  en: {
    translation: {
      "dashboard_title": "Mesh Tactical Command",
      "status_ok": "OK",
      "status_warning": "Warning",
      "status_critical": "Critical",
      "heart_rate": "Heart Rate",
      "oxygen": "SpO2",
      "temp": "Temp",
      "battery": "Battery",
      "last_seen": "Last Seen",
      "event_log": "Event Log",
      "system_events": "System Events",
      "no_recent_events": "No recent events",
      "time": "Time",
      "node": "Node",
      "event": "Event",
      "severity": "Severity",
      "action": "Action",
      "instruction": "Instruction",
      "language": "Ngôn ngữ",
      "pathfinding": "Pathfinding",
      "select_start": "Select start point",
      "select_end": "Select end point",
      "path_drawn": "Path drawn",
      "config_node": "Configure Node",
      "tx_rate": "Tx Rate (sec)",
      "ota_update": "OTA Firmware Update",
      "start_ota": "Start OTA Update",
      "close": "Close",
      "set": "Set",
      "alert_hr_low": "Heart rate dangerously low",
      "alert_spo2_low": "Oxygen level dangerously low",
      "alert_conn_lost": "Connection lost",
      "alert_power_loss": "Power loss detected",
      "instr_check_medic": "Deploy medic to last known location immediately.",
      "instr_check_conn": "Check gateway and USB connection.",
      "instr_replace_bat": "Replace battery on device."
    }
  },
  vi: {
    translation: {
      "dashboard_title": "Trung tâm Chỉ huy Mesh",
      "status_ok": "Bình thường",
      "status_warning": "Cảnh báo",
      "status_critical": "Nguy hiểm",
      "heart_rate": "Nhịp tim",
      "oxygen": "SpO2",
      "temp": "Nhiệt độ",
      "battery": "Pin",
      "last_seen": "Lần cuối",
      "event_log": "Nhật ký Sự kiện",
      "system_events": "Sự kiện Hệ thống",
      "no_recent_events": "Không có sự kiện gần đây",
      "time": "Thời gian",
      "node": "Thiết bị",
      "event": "Sự kiện",
      "severity": "Mức độ",
      "action": "Thao tác",
      "instruction": "Hướng dẫn xử lý",
      "language": "Language",
      "pathfinding": "Tìm đường",
      "select_start": "Chọn điểm xuất phát",
      "select_end": "Chọn điểm đích",
      "path_drawn": "Đã vẽ tuyến đường",
      "config_node": "Cấu hình Node",
      "tx_rate": "Chu kỳ gửi (giây)",
      "ota_update": "Cập nhật Firmware (OTA)",
      "start_ota": "Bắt đầu cập nhật OTA",
      "close": "Đóng",
      "set": "Lưu",
      "alert_hr_low": "Nhịp tim thấp nguy hiểm",
      "alert_spo2_low": "Nồng độ oxy thấp nguy hiểm",
      "alert_conn_lost": "Mất kết nối",
      "alert_power_loss": "Mất nguồn thiết bị",
      "instr_check_medic": "Điều động quân y đến tọa độ cuối cùng ngay lập tức.",
      "instr_check_conn": "Kiểm tra gateway và cáp USB.",
      "instr_replace_bat": "Thay pin cho thiết bị."
    }
  }
};

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: "vi", // Default language
    fallbackLng: "en",
    interpolation: {
      escapeValue: false
    }
  });

export default i18n;
