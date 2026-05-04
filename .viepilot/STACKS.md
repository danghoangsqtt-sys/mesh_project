# STACKS — Mesh Pi5 Server

## Detected Stacks

| Stack | Role | Cache Path |
|-------|------|------------|
| python-fastapi | Backend framework | `~/.viepilot/stacks/python-fastapi/` |
| react-vite | Frontend framework | `~/.viepilot/stacks/react-vite/` |
| maplibre-gl-js | Map rendering | `~/.viepilot/stacks/maplibre-gl-js/` |
| sqlite | Database | `~/.viepilot/stacks/sqlite/` |
| nginx | Reverse proxy | `~/.viepilot/stacks/nginx/` |
| raspberry-pi | Hardware platform | `~/.viepilot/stacks/raspberry-pi/` |

## Stack Rules Summary

### Python FastAPI
- Use async endpoints for I/O-bound operations
- Use background threads for blocking serial I/O (pyserial)
- Bridge threads to asyncio via `asyncio.Queue` + `call_soon_threadsafe`
- Use Pydantic models for request/response validation
- Use lifespan events (not deprecated `on_event`)

### React + Vite
- Use functional components with hooks
- Use TypeScript strict mode
- Build production with `vite build` → serve static via Nginx
- Use `useEffect` cleanup for WebSocket disconnection
- Lazy load heavy components (MapLibre)

### MapLibre GL JS
- Use PMTiles format (not MBTiles) for browser-native offline
- Register custom protocol via `addProtocol()` before map init
- Cache sprites + fonts locally in `public/` directory
- Use vector tiles for tactical precision at high zoom

### SQLite
- Use WAL mode for concurrent reads
- Use SQLAlchemy async driver (`aiosqlite`) or sync with thread pool
- Keep migrations in `alembic/`

### Nginx
- Serve `frontend/dist/` as static root
- Proxy `/api/` and `/ws` to FastAPI (Uvicorn on port 8000)
- Enable gzip for JS/CSS assets
- WebSocket proxy: set `Upgrade` and `Connection` headers
