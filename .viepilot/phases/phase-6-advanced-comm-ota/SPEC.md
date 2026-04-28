# SPEC — Phase 6: Advanced Communication & OTA

## Overview
This phase adds two-way command acknowledgment, remote configuration of nodes via LoRa, and Over-The-Air (OTA) firmware updates distributed through the Mesh network.

## Features
- **6.1 Two-way Command Protocol:** Parsing ACKs from nodes to show Delivered/Failed status in the UI.
- **6.2 Remote Node Config:** Settings UI to change Node name, Tx Rate, and toggle sensors remotely.
- **6.3 OTA Firmware Update:** UI to pick a `.bin` file, slice it into chunks, and stream it sequentially over USB OTG to the Gateway for broadcast.

## Dependencies
- USB Serial flow reliability
- File picker APIs
