# Next Steps: Terminal Installation (Beginner's Guide)

Based on your terminal output, you have successfully navigated into the `mesh_pi5_server` directory. Here is exactly what you need to type next in that black screen.

## 1. The Commands to Run (Copy and Paste)

**First Command:**
You need to grant "execute" permissions to the installation files. Copy the line below, paste it into your Terminal, and press Enter:
```bash
sudo chmod +x deploy/install.sh deploy/setup-wifi-ap.sh
```
*(If it returns a blank new line like `pi@k2pi:...`, it means success).*

**Second Command:**
Now we start the actual installation. Copy this line, paste it, and press Enter:
```bash
sudo ./deploy/install.sh
```

## 2. The Waiting Process

Right after you press Enter on the second command, a lot of text will scroll across the screen. Don't panic!
- The Pi 5 is automatically downloading and installing Python, Node.js, configuring the WiFi, and starting the Website.
- **Wait time:** About 10 to 15 minutes.
- DO NOT close the Terminal window during this time.

## 3. What Happens Next?

Once the installation reaches 100%, the system will automatically transform your Raspberry Pi into a WiFi Router (Access Point).
Because of this, **its connection to your home internet will drop**, and your Terminal (SSH) window might freeze or show a "Connection lost" error. **This is completely normal and a sign of success!**

## 4. Usage Guide After Installation

When your Terminal disconnects, follow these steps to use the system:

1. **Take your Phone or Laptop** and open the WiFi settings.
2. Look for a new WiFi network named: **`MESH_TACTICAL_WIFI`**
3. Connect to that network.
   - **Password:** `meshpassword`
4. Plug your LoRa T-Beam device into the USB port of the Pi 5.
5. Open a web browser (Google Chrome or Safari) on the Phone/Laptop you just connected to the WiFi.
6. Type the following address into the search bar: 
   👉 **`http://10.42.0.1`**
7. Done! You will see the Tactical Command Dashboard appear. The system is fully ready. For all future uses, just plug the Pi into power and follow steps 1 to 6 (no need to reinstall).
