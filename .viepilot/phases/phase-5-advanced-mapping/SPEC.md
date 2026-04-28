# SPEC — Phase 5: Advanced Mapping & Visualization

## Overview
This phase focuses on upgrading MapLibre offline capabilities to a professional tactical standard.

## Features
- **5.1 Heatmap Overlay:** Data-driven heatmap layer to visualize node density or signal strength (RSSI/SNR).
- **5.2 Clustering:** Grouping markers automatically when zooming out to declutter the map.
- **5.3 3D Terrain:** Enabling MapLibre 3D Terrain extrusion (requires DEM tiles) and optimizing camera pitch.
- **5.4 Compass Widget:** On-map compass that responds to device rotation sensors.
- **5.5 GPS Accuracy Circles:** Drawing semi-transparent circles representing the error radius around soldiers.

## Dependencies
- MapLibre Native Android SDK
- Device Sensors (for Compass)
