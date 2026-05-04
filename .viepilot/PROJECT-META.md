# PROJECT-META — Mesh Pi5 Server

## Project Information

| Field | Value |
|-------|-------|
| **Project Name** | Mesh Pi5 Server |
| **Repository** | mesh_pi5_server |
| **Version** | 0.1.0 |
| **Language (Backend)** | Python 3.11+ |
| **Language (Frontend)** | TypeScript (React) |
| **Framework (Backend)** | FastAPI + Uvicorn |
| **Framework (Frontend)** | React 18+ / Vite 5+ |
| **Database** | SQLite 3 |
| **Platform** | Raspberry Pi 5 (4GB RAM) |
| **OS** | Raspberry Pi OS Bookworm (Debian 12) |

## Organization

| Field | Value |
|-------|-------|
| **Organization** | — |
| **Website** | — |
| **Repository** | — (local project) |

## Developer

| Field | Value |
|-------|-------|
| **Lead Developer** | dangh |
| **Email** | — |

## License

| Field | Value |
|-------|-------|
| **License** | MIT |
| **Inception Year** | 2026 |

## Project Structure

```
mesh_pi5_server/
├── backend/                    # Python FastAPI backend
│   ├── app/
│   │   ├── main.py             # FastAPI app entry
│   │   ├── config.py           # Settings & env config
│   │   ├── serial_bridge.py    # pyserial thread + queue
│   │   ├── packet_parser.py    # Binary packet protocol (40 bytes)
│   │   ├── models/             # SQLAlchemy models
│   │   ├── routers/            # API route handlers
│   │   ├── services/           # Business logic
│   │   └── ws/                 # WebSocket handlers
│   ├── requirements.txt
│   ├── alembic/                # DB migrations
│   └── tests/
├── frontend/                   # React + Vite frontend
│   ├── src/
│   │   ├── components/         # React components
│   │   ├── hooks/              # Custom hooks (useWebSocket, etc.)
│   │   ├── pages/              # Page views
│   │   ├── services/           # API clients
│   │   ├── stores/             # State management
│   │   └── styles/             # CSS / theme
│   ├── public/
│   │   └── maps/               # PMTiles offline map files
│   ├── package.json
│   └── vite.config.ts
├── deploy/                     # Deployment scripts
│   ├── setup-wifi-ap.sh        # NetworkManager AP config
│   ├── install.sh              # One-liner Pi 5 setup
│   ├── mesh-server.service     # systemd unit file
│   └── nginx.conf              # Nginx reverse proxy config
├── .viepilot/                  # ViePilot project management
├── README.md
├── CHANGELOG.md
└── LICENSE
```

## File Header Template

```python
"""
Mesh Pi5 Server — Web-based Command & Control for LoRa Mesh Network
Copyright (c) 2026 — All rights reserved

File: {filename}
Description: {description}
"""
```
