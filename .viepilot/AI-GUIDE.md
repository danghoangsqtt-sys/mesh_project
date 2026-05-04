# AI-GUIDE — Mesh Pi5 Server

## Quick Context
- **Project:** Mesh Pi5 Server — Web-based Command & Control for LoRa Mesh Network
- **Platform:** Raspberry Pi 5 (4GB RAM), Raspberry Pi OS Bookworm
- **Backend:** Python 3.11+ / FastAPI / SQLite / pyserial
- **Frontend:** React + Vite + TypeScript / MapLibre GL JS / PMTiles
- **Deployment:** Nginx reverse proxy, systemd service, WiFi AP (NetworkManager)
- **Lead:** dangh
- **License:** MIT / 2026

## File Relationships

```
.viepilot/
├── AI-GUIDE.md          ← YOU ARE HERE — start reading
├── PROJECT-META.md      ← Project metadata, developer info
├── ARCHITECTURE.md      ← System design, data flow, protocol
├── architecture/        ← Mermaid sidecar files
├── PROJECT-CONTEXT.md   ← Domain knowledge, product vision
├── SYSTEM-RULES.md      ← Coding rules, conventions
├── STACKS.md            ← Stack cache index
├── ROADMAP.md           ← Phase/task breakdown (start here for /vp-auto)
├── TRACKER.md           ← Current progress state
├── HANDOFF.json         ← Machine-readable resume state
├── phases/              ← Per-phase state tracking
└── schemas/             ← Database schema
```

## Context Loading Strategy

### For implementation (/vp-auto):
1. Read `AI-GUIDE.md` (this file) — quick orientation
2. Read `TRACKER.md` — where are we now?
3. Read `ROADMAP.md` — what's the current phase/task?
4. Read `phases/{current}/PHASE-STATE.md` — task status
5. Read `ARCHITECTURE.md` — only sections relevant to current task
6. Read `STACKS.md` → global cache for stack rules

### For auditing (/vp-audit):
1. Read `TRACKER.md` + `HANDOFF.json` — state consistency
2. Read `ROADMAP.md` — phase status
3. Scan `phases/*/PHASE-STATE.md` — cross-check

### Minimal context (small tasks):
- `AI-GUIDE.md` + `TRACKER.md` + relevant task file only
