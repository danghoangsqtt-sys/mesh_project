# SYSTEM-RULES — Mesh Pi5 Server

## Architecture Rules

1. **Separation of concerns:** Backend (FastAPI) handles serial, data, API. Frontend (React) handles UI only. No business logic in frontend.
2. **Serial isolation:** All serial I/O runs in a dedicated thread. Never call pyserial from async context directly.
3. **Repository pattern:** All database access goes through repository classes. No raw SQL in routers.
4. **WebSocket broadcast:** Use connection manager pattern. Support multiple concurrent clients.
5. **Static frontend:** Frontend is pre-built (`vite build`). Nginx serves static files. No Node.js on Pi 5.

## Coding Rules

### Python (Backend)
- Python 3.11+ required
- Type hints on all functions (params + return)
- Google-style docstrings on public functions
- Use `async def` for all FastAPI endpoints
- Use `Pydantic` models for request/response schemas
- Use `SQLAlchemy` ORM (not raw SQL)
- Max function length: 50 lines
- Max file length: 300 lines (split if larger)

### TypeScript (Frontend)
- Strict mode enabled in `tsconfig.json`
- Functional components only (no class components)
- React hooks for state management
- Props interfaces defined for all components
- No `any` type — use proper typing
- CSS modules or vanilla CSS (no Tailwind unless explicitly requested)

## Comment Standards

### Good Comments
```python
# Bridge serial thread data to async WebSocket clients
# Uses asyncio.Queue for thread-safe communication
async def broadcast_to_clients(data: SoldierPacket):
```

### Bad Comments
```python
# Set x to 5
x = 5

# Loop through items
for item in items:
```

## Versioning

- Follow **Semantic Versioning** (SemVer): `MAJOR.MINOR.PATCH`
- `0.x.y` — Development phase (breaking changes allowed in minor)
- `1.0.0` — First stable release (after Phase 4 deployment ready)

## Git Conventions

- **Conventional Commits:** `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`
- **Branch naming:** `feature/phase-N-description`, `fix/issue-description`
- **Commit scope:** `feat(serial):`, `feat(api):`, `feat(map):`, `fix(ws):`

## Changelog Standards

- Follow **Keep a Changelog** format
- Categories: Added, Changed, Deprecated, Removed, Fixed, Security
- Most recent version at top

## Quality Gates

### Before merge:
- [ ] Python: `ruff check` passes (no lint errors)
- [ ] Python: `mypy` type check passes
- [ ] TypeScript: `tsc --noEmit` passes
- [ ] Frontend: `npm run build` succeeds
- [ ] Tests: `pytest` passes (when test suite exists)

### Before phase complete:
- [ ] All acceptance criteria met
- [ ] CHANGELOG updated
- [ ] TRACKER.md updated
- [ ] No TODO/FIXME in committed code (unless tracked)
