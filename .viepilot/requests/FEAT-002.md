# FEAT: Offline Map Downloader & Modern App Icon

## Meta
- **ID**: FEAT-002
- **Type**: Feature
- **Status**: triaged
- **Priority**: high
- **Created**: 2026-04-28
- **Reporter**: User
- **Assignee**: AI

## Summary
Cập nhật thiết kế Icon ứng dụng hiện đại và xây dựng cơ chế tải/lưu trữ bản đồ Offline hoàn chỉnh (Offline Map Downloader) cho MapLibre. Điều này giúp hệ thống hoạt động độc lập không cần Internet/Bluetooth/Wifi khi tác chiến thực địa.

## Details
1. **App Icon**: Thiết kế và tích hợp một App Icon mới theo phong cách Military/Tactical hiện đại, đồng bộ với thiết kế giao diện Dark Green của ứng dụng thay thế cho icon Android mặc định.
2. **Offline Map Downloader**: 
   - Xây dựng giao diện và cơ chế cho phép người dùng tải bản đồ (Map Tiles) của một khu vực nhất định khi có kết nối Internet (Ví dụ: tại căn cứ).
   - Lưu trữ các tile bản đồ vào bộ nhớ cục bộ (Local Storage) của thiết bị Android dưới dạng thư mục cache hoặc tệp `.mbtiles`.
3. **MapLibre Offline Fallback (Sửa lỗi hiển thị)**:
   - Sửa lỗi hiển thị bản đồ khi không có mạng đã phát hiện trong quá trình Debug trước đó.
   - Ưu tiên nạp bản đồ từ Local Storage, đảm bảo hiển thị bản đồ hoàn chỉnh mượt mà để sử dụng chung với GPS của mạch LilyGo mà không bị crash hay lỗi màn hình Beige.

## Acceptance Criteria
- [ ] Ứng dụng có Icon mới hiển thị chính xác và đẹp mắt trên màn hình chính của điện thoại/máy ảo.
- [ ] Tính năng "Download Map" hoạt động và lưu trữ dữ liệu bản đồ offline thành công.
- [ ] Ứng dụng hiển thị bản đồ bình thường (từ cache/mbtiles) khi ngắt toàn bộ mạng Wifi, Data và Bluetooth (Bật chế độ máy bay).
- [ ] Tính năng định vị và vẽ Geofence hoạt động tốt với bản đồ Offline.

## Related
- Phase: TBD (Dự kiến Phase 9)
- Files: `MapScreen.kt`, `AndroidManifest.xml`, Thêm module Download
- Dependencies: N/A
