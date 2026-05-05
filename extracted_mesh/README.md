# Mesh Pi5 Server

![Version](https://img.shields.io/badge/version-0.1.0-blue)
![Platform](https://img.shields.io/badge/platform-Raspberry%20Pi%205-red)
![License](https://img.shields.io/badge/license-MIT-green)

> Web-based Command & Control for LoRa Mesh Network on Raspberry Pi 5.

## Overview

Hệ thống quản lý và giám sát mạng lưới Mesh LoRa chạy trên Raspberry Pi 5. Pi 5 kết nối với Gateway (ESP32 T-Beam) qua USB Serial, phát WiFi AP, và serve dashboard chiến thuật cho nhiều thiết bị đầu cuối cùng lúc qua trình duyệt web. **100% Offline.**

## Documentation

Toàn bộ hướng dẫn cài đặt và kịch bản test nằm trong thư mục `docs/`:

- 📖 [Đọc Tài Liệu (Documentation Index)](docs/README.md)
- ⚙️ [Hướng dẫn Cài đặt Hệ thống lên Raspberry Pi 5](docs/dev/deployment.md)
- 🧪 [Kịch bản Kiểm thử Thực địa với T-Beam (Testing Guide)](docs/user/testing-guide.md)

## Architecture

```
Soldier Nodes ──(LoRa 433MHz)──► Gateway ──(USB)──► Raspberry Pi 5
                                                      │
                                              ┌───────┴────────┐
                                              │  FastAPI + SQLite │
                                              │  Nginx + React   │
                                              │  WiFi AP          │
                                              └───────┬────────┘
                                                      │ WiFi
                                              ┌───────┴────────┐
                                              │  Tablets/Phones  │
                                              │  Laptops         │
                                              │  (Any Browser)   │
                                              └────────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Python 3.11+ / FastAPI / Uvicorn |
| **Frontend** | React 18+ / Vite / TypeScript |
| **Map** | MapLibre GL JS / PMTiles (offline) |
| **Database** | SQLite 3 (WAL mode) |
| **Serial** | pyserial (USB OTG → ESP32 Gateway) |
| **Proxy** | Nginx (static + reverse proxy) |
| **WiFi AP** | NetworkManager (nmcli) |
| **Platform** | Raspberry Pi 5 (4GB RAM) |

## Quick Start

```bash
# On Raspberry Pi 5
git clone <repo-url> mesh_pi5_server
cd mesh_pi5_server
chmod +x deploy/install.sh
./deploy/install.sh
```

After installation, connect to WiFi `MESH_COMMAND_WIFI` and open `http://10.42.0.1` in any browser.

## Development

### Backend
```bash
cd backend
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Build & Deploy
```bash
cd frontend
npm run build
# Copy dist/ to Pi 5
rsync -avz dist/ pi@<pi-ip>:/opt/mesh_pi5_server/frontend/dist/
```

## Project Status

| Phase | Status | Description |
|-------|--------|-------------|
| Phase 1 | ✅ Done | Pi 5 Foundation & Serial Bridge |
| Phase 2 | ✅ Done | React Web Dashboard |
| Phase 3 | ✅ Done | Two-way Command & Node Mgmt |
| Phase 4 | ✅ Done | WiFi AP Auto-Setup & Deploy |
| Phase 5 | ✅ Done | AI Pathfinding |
| Phase 6 | ✅ Done | AI Vision Integration |
| Phase 7 | ✅ Done | Vitals Alerts & i18n |

## License

MIT © 2026
