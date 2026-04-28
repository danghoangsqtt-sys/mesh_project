-- ============================================================
-- Mesh Command Android App — Database Schema (Room/SQLite)
-- ============================================================

-- Soldier nodes: stores latest state for each node
CREATE TABLE IF NOT EXISTS soldiers (
    node_id       INTEGER PRIMARY KEY,               -- uint16 from packet
    team_name     TEXT    NOT NULL DEFAULT 'unassigned',
    latitude      REAL    NOT NULL DEFAULT 0.0,       -- float32
    longitude     REAL    NOT NULL DEFAULT 0.0,       -- float32
    heading       REAL    NOT NULL DEFAULT 0.0,       -- float32 (degrees)
    heart_rate    INTEGER NOT NULL DEFAULT 0,          -- uint8
    spo2          INTEGER NOT NULL DEFAULT 0,          -- uint8
    temperature   REAL    NOT NULL DEFAULT 0.0,       -- float32 (°C)
    humidity      REAL    NOT NULL DEFAULT 0.0,       -- float32 (%)
    pressure      REAL    NOT NULL DEFAULT 0.0,       -- float32 (hPa)
    battery_volts REAL    NOT NULL DEFAULT 0.0,       -- float32 (V)
    status_flags  INTEGER NOT NULL DEFAULT 0,          -- uint16 bitmap
    alert_level   INTEGER NOT NULL DEFAULT 0,          -- 0=OK, 1=WARN, 2=CRIT
    is_online     INTEGER NOT NULL DEFAULT 0,          -- boolean (0/1)
    last_seen_ms  INTEGER NOT NULL DEFAULT 0,          -- millis since epoch
    created_at    INTEGER NOT NULL DEFAULT (strftime('%s','now')),
    updated_at    INTEGER NOT NULL DEFAULT (strftime('%s','now'))
);

CREATE INDEX idx_soldiers_team ON soldiers(team_name);
CREATE INDEX idx_soldiers_online ON soldiers(is_online);
CREATE INDEX idx_soldiers_alert ON soldiers(alert_level);

-- Position history: stores GPS trail for path replay
CREATE TABLE IF NOT EXISTS position_history (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    node_id       INTEGER NOT NULL,
    latitude      REAL    NOT NULL,
    longitude     REAL    NOT NULL,
    heading       REAL    NOT NULL DEFAULT 0.0,
    timestamp_ms  INTEGER NOT NULL,                    -- millis since epoch
    FOREIGN KEY (node_id) REFERENCES soldiers(node_id) ON DELETE CASCADE
);

CREATE INDEX idx_pos_history_node ON position_history(node_id);
CREATE INDEX idx_pos_history_time ON position_history(timestamp_ms);

-- Event log: system events, alerts, commands
CREATE TABLE IF NOT EXISTS events (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp_ms  INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000),
    event_type    TEXT    NOT NULL,                     -- 'ALERT', 'COMMAND', 'CONNECTION', 'SYSTEM'
    severity      INTEGER NOT NULL DEFAULT 0,           -- 0=INFO, 1=WARN, 2=ERROR
    node_id       INTEGER,                              -- NULL for system events
    message       TEXT    NOT NULL,
    acknowledged  INTEGER NOT NULL DEFAULT 0            -- boolean (0/1)
);

CREATE INDEX idx_events_type ON events(event_type);
CREATE INDEX idx_events_time ON events(timestamp_ms);
CREATE INDEX idx_events_node ON events(node_id);

-- Gateway info: stores gateway position and status
CREATE TABLE IF NOT EXISTS gateway (
    id            INTEGER PRIMARY KEY DEFAULT 1,       -- Singleton row (Phase 1)
    latitude      REAL    NOT NULL DEFAULT 0.0,
    longitude     REAL    NOT NULL DEFAULT 0.0,
    connection    TEXT    NOT NULL DEFAULT 'disconnected',  -- 'usb', 'wifi', 'disconnected'
    device_name   TEXT,
    last_seen_ms  INTEGER NOT NULL DEFAULT 0,
    updated_at    INTEGER NOT NULL DEFAULT (strftime('%s','now'))
);

-- Teams: team configuration (Phase 2)
CREATE TABLE IF NOT EXISTS teams (
    team_name     TEXT    PRIMARY KEY,
    display_name  TEXT    NOT NULL,
    color_hex     TEXT    NOT NULL DEFAULT '#6B8E23',   -- OliveDrab default
    sort_order    INTEGER NOT NULL DEFAULT 0
);

-- Default teams
INSERT OR IGNORE INTO teams (team_name, display_name, color_hex, sort_order) VALUES
    ('alpha',   'ALPHA',   '#4CAF50', 1),
    ('bravo',   'BRAVO',   '#2196F3', 2),
    ('charlie', 'CHARLIE', '#FF9800', 3),
    ('delta',   'DELTA',   '#9C27B0', 4);

-- Geofences: saved zones (Phase 3)
CREATE TABLE IF NOT EXISTS geofences (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    fence_type    TEXT    NOT NULL DEFAULT 'safe',      -- 'safe', 'danger', 'restricted'
    polygon_json  TEXT    NOT NULL,                     -- GeoJSON polygon coordinates
    is_active     INTEGER NOT NULL DEFAULT 1,
    created_at    INTEGER NOT NULL DEFAULT (strftime('%s','now'))
);

-- Waypoints: saved points of interest (Phase 3)
CREATE TABLE IF NOT EXISTS waypoints (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    latitude      REAL    NOT NULL,
    longitude     REAL    NOT NULL,
    icon_type     TEXT    NOT NULL DEFAULT 'default',
    notes         TEXT,
    created_at    INTEGER NOT NULL DEFAULT (strftime('%s','now'))
);
