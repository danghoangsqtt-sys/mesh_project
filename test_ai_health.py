import sys
import time

# Add backend directory to path so we can import modules
sys.path.append('e:/data/2.MyProject/2026/mesh_pi5_server/backend')

from app.services.ai_health_analyzer import ai_health_analyzer

def test_heat_stress():
    print("--- KIỂM TRA: DẤU HIỆU SỐC NHIỆT (HEAT STRESS) ---")
    print("Node 3 gửi dữ liệu: Nhịp tim = 135, Nhiệt độ = 36.5°C")
    
    # Giả lập gửi 3 gói tin liên tiếp báo nhiệt độ cao và nhịp tim cao
    for i in range(3):
        events = ai_health_analyzer.analyze_node(node_id=3, heart_rate=135, spo2=98, temp=36.5)
        for e in events:
            print(f"🚨 PHÁT HIỆN SỰ KIỆN: [{e.severity}] {e.event_type} - {e.message}")
        time.sleep(0.1)
    print("-> Đã kiểm tra xong Sốc nhiệt.\n")

def test_hypoxia():
    print("--- KIỂM TRA: DẤU HIỆU THIẾU OXY (HYPOXIA) ---")
    print("Node 1 gửi dữ liệu: SpO2 giảm dần xuống 92% trong 10 nhịp")
    
    # Clear history for clean test
    ai_health_analyzer.nodes_data[1].history.clear()
    
    # Gửi 9 gói tin SpO2 = 92% (chưa đủ 10 gói để trigger cảnh báo)
    for i in range(9):
        events = ai_health_analyzer.analyze_node(node_id=1, heart_rate=80, spo2=92, temp=30.0)
        if events:
            for e in events:
                print(f"🚨 LỖI: Cảnh báo phát ra quá sớm ở nhịp {i+1}")
                
    print("Đã gửi 9 gói SpO2 = 92%, chuẩn bị gửi gói thứ 10...")
    
    # Gói thứ 10 sẽ trigger cảnh báo
    events = ai_health_analyzer.analyze_node(node_id=1, heart_rate=80, spo2=92, temp=30.0)
    for e in events:
        print(f"🚨 PHÁT HIỆN SỰ KIỆN: [{e.severity}] {e.event_type} - {e.message}")
        
    print("-> Đã kiểm tra xong Thiếu oxy.\n")

def test_recovery():
    print("--- KIỂM TRA: PHỤC HỒI (RECOVERY) ---")
    print("Node 3 nghỉ ngơi: Nhịp tim = 80, Nhiệt độ = 33.0°C")
    
    events = ai_health_analyzer.analyze_node(node_id=3, heart_rate=80, spo2=98, temp=33.0)
    print(f"Sự kiện sinh ra: {len(events)} (AI tự động gỡ cờ cảnh báo trong bộ nhớ)")
    if "HEAT_STRESS" not in ai_health_analyzer.active_warnings[3]:
        print("✅ Đã gỡ cờ HEAT_STRESS thành công.")
        
    print("-> Đã kiểm tra xong Phục hồi.\n")

if __name__ == "__main__":
    test_heat_stress()
    test_hypoxia()
    test_recovery()
