# Tham chiếu Kỹ năng Tự động hóa (Skills Reference)

Dự án này sử dụng nền tảng để quản lý tiến độ, quy trình vòng đời (lifecycle) và tự động sinh code. Dưới đây là danh sách các kỹ năng (`skills`) và luồng công việc (`workflows`) chính đã áp dụng để đẩy nhanh quá trình hoàn thiện Milestone 2.

## /vp-auto (Autonomous Execution Loop)
- **Chức năng:** Tự động hóa quá trình code dựa trên các mô tả chi tiết từ thư mục `.viepilot/phases/`.
- **Áp dụng:** Được dùng làm công cụ chính yếu xuyên suốt Phase 5, Phase 6, Phase 7, và Phase 8. Nhờ có `vp-auto`, các khối công việc như thuật toán Ray-Casting cho Geofence, hay giao thức kết nối USB Serial hai chiều đều được sinh ra và tự động gắn kết (inject) vào cấu trúc Hilt / Jetpack Compose một cách trơn tru.

## /vp-docs (Documentation Generator)
- **Chức năng:** Quét cấu trúc dự án hiện tại, đọc file trạng thái `TRACKER.md` và `ROADMAP.md` để sinh ra toàn bộ tài liệu hướng dẫn chuẩn chỉnh cho cả Dev và End-User.
- **Áp dụng:** Sinh ra cấu trúc thư mục `docs/` mà bạn đang xem, giúp cập nhật tính năng mới nhất (như Geofencing) thành User Manual nhanh chóng, đồng bộ `CHANGELOG.md` sau khi chốt Milestone.

## /vp-status (Progress Dashboard)
- **Chức năng:** Hiển thị trực quan tiến độ của dự án, rà soát lại quá trình thực thi Phase.
- **Áp dụng:** Đảm bảo toàn bộ các nhánh tính năng (Tasks) được mark `Done` đúng thứ tự trước khi merge sang Phase tiếp theo.

## /vp-evolve (Feature Evolution)
- **Chức năng:** Chuyển đổi và nâng cấp từ các request ngắn gọn (của User) thành các bản Doc-first Design (thiết kế tĩnh).
- **Áp dụng:** Lên ý tưởng và khung sườn cho kiến trúc Mesh Geofence ở đầu Phase 8.
