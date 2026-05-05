"""
Mesh Pi5 Server — Configuration

File: config.py
Description: Application settings loaded from environment variables with sensible defaults.
"""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application configuration with environment variable support."""

    # Application
    APP_NAME: str = "Mesh Pi5 Server"
    APP_VERSION: str = "0.1.0"
    DEBUG: bool = False

    # Serial connection
    SERIAL_PORT: str = "AUTO"
    SERIAL_BAUDRATE: int = 115200
    SERIAL_TIMEOUT: float = 1.0
    SERIAL_RECONNECT_DELAY: float = 3.0

    # Database
    DATABASE_URL: str = "sqlite+aiosqlite:///./mesh_data.db"

    # WebSocket
    WS_HEARTBEAT_INTERVAL: float = 30.0

    # Server
    HOST: str = "0.0.0.0"
    PORT: int = 8000

    model_config = {
        "env_file": ".env",
        "env_file_encoding": "utf-8",
        "case_sensitive": True,
    }


settings = Settings()
