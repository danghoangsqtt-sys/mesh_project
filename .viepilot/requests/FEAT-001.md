# FEAT: Native Military Desktop App Redesign (iOS Style)

## Meta
- **ID**: FEAT-001
- **Type**: Feature / Enhancement
- **Status**: triaged
- **Priority**: high
- **Created**: 2026-04-24
- **Reporter**: User
- **Assignee**: AI

## Summary
Convert the current Flask Web Dashboard into a native Python desktop application with a professional, military-grade UI inspired by iOS design principles. The map must be integrated directly into the software and default to Nha Trang, Vietnam.

## Details
1. **Architecture Change**: Move from Flask + Web Frontend to a pure Python desktop application framework (PySide6 / PyQt6).
2. **UI/UX Redesign**:
   - iOS-inspired design: modern blur effects, rounded corners, sleek typography, clean sidebar.
   - Professional military feel: dark theme, high contrast alerts, tactical layout.
3. **Map Integration**: 
   - Default coordinates: Nha Trang, Vietnam (approx 12.2388° N, 109.1967° E).
   - Real-time marker updates embedded natively via `QWebEngineView`.
4. **Local Installation**: The software will run completely locally on a PC as a standalone window executable without needing a web browser.

## Acceptance Criteria
- [ ] Desktop app launches as a standalone window.
- [ ] Map successfully loads inside the application and centers on Nha Trang by default.
- [ ] UI features an iOS-inspired dark tactical theme.
- [ ] Serial communication and all existing business logic (alerts, routing) continue to work seamlessly in the background thread.

## Related
- Phase: 5 (New Phase - GUI Modernization)
- Files: `server/*`
