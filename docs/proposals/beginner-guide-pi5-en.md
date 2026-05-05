# Raspberry Pi 5 & Mesh Server Setup (Beginner's Guide)

This guide is written in a step-by-step format to help you set up the system even if you have never used a Raspberry Pi (referred to as Pi 5) before.

---

## PART 1: Preparing the SD Card (On your Windows/Mac Computer)

Unlike a regular computer, the Raspberry Pi doesn't have an internal hard drive. It stores its operating system and data on a **MicroSD Card**. The first thing you need to do is "install Windows" (the operating system) onto this SD card.

**Steps:**
1. Insert the MicroSD card into your computer (use an SD card reader if necessary).
2. Download and install the **Raspberry Pi Imager** software from the official website: [https://www.raspberrypi.com/software/](https://www.raspberrypi.com/software/)
3. Open the software. You will see 3 main buttons:
   - **CHOOSE DEVICE:** Click and select **Raspberry Pi 5**.
   - **CHOOSE OS:** Click and select **Raspberry Pi OS (64-bit)** (This is the default option).
   - **CHOOSE STORAGE:** Click and select the MicroSD card you just inserted (Be very careful not to select your computer's hard drive!).
4. Click **NEXT**. The software will ask if you want to apply OS customisation settings. Choose **EDIT SETTINGS**.
5. In the Settings window:
   - **General Tab:** Set the Username to `pi` and Password to `123456` (or any password you can easily remember). Check the "Configure wireless LAN" box and enter your home WiFi Name (SSID) and Password here (so the Pi can connect to the internet during setup).
   - **Services Tab:** Check the **Enable SSH** box (select "Use password authentication"). This allows you to control the Pi from your computer without needing to plug in a monitor.
6. Click **SAVE**, then click **YES** to start writing. Wait about 5-10 minutes until the software says it's complete.
7. Remove the SD card from your computer.

---

## PART 2: Powering On and Installing the Mesh Server

**Step 1: Booting up the Raspberry Pi 5**
1. Flip the Pi 5 over. You will see a tiny SD card slot on the bottom edge. Insert the SD card you just prepared until it clicks or sits flush.
2. Plug the USB-C power cable into the Pi 5 and plug it into a wall outlet (it's highly recommended to use the official 27W power supply to avoid power drops).
3. The red/green lights on the Pi 5 will turn on. Wait about 2-3 minutes for it to boot up for the first time and automatically connect to your home WiFi.

**Step 2: Controlling the Pi 5 from your computer**
Since we didn't plug a monitor into the Pi 5, we will control it over the network.
1. On your Windows computer, click the `Start` button, type `cmd`, and open the **Command Prompt** (Terminal on Mac).
2. Type the following command and press Enter: `ssh pi@raspberrypi.local`
3. Your computer will ask for a password. Type the password you created in Part 1 (e.g., `123456`) and press Enter (Note: when you type the password, nothing will appear on the screen. Just type it and press Enter).
4. If a green text prompt like `pi@raspberrypi:~ $` appears, congratulations! You have successfully logged into the Pi 5.

**Step 3: Downloading the source code and Auto-installing**
You are now inside the Pi 5. Let's download the Mesh Server software and install it:
1. Type the download command (Copy this line, paste it, and press Enter):
   ```bash
   git clone https://github.com/your-repo/mesh_pi5_server.git
   ```
   *(Note: Replace with your actual repository link, or copy the `mesh_pi5_server` folder to the Pi manually)*
2. Enter the folder you just downloaded:
   ```bash
   cd mesh_pi5_server
   ```
3. Run the auto-install command (Copy and paste):
   ```bash
   sudo chmod +x deploy/install.sh
   sudo ./deploy/install.sh
   ```
4. Now you can go grab a coffee. The Pi 5 will automatically download all necessary software and set everything up. This process takes about 10-15 minutes.

---

## PART 3: Field Deployment (Going to the woods)

After the installation is complete, the Pi 5 has been transformed into a standalone **WiFi Router**. You can take it to the forest or mountains; it no longer needs your home internet connection.

1. Unplug the Pi 5's power cable and take it to your command post location.
2. Power the Pi 5 back on (using a high-quality power bank with a USB-C cable).
3. Take the **LoRa Gateway chip (T-Beam)** and plug it into one of the USB ports on the Pi 5.
4. Take out your Phone or Tablet and open your WiFi settings.
5. You will see a new WiFi network named: **Mesh_Tactical_AP**.
6. Connect to it. The default password is: **meshadmin123**.
7. Open a web browser (Safari/Chrome) on your Phone/Tablet and type into the address bar:
   👉 **http://10.42.0.1**
8. Awesome! The "Mesh Tactical Command" dashboard will appear. Any soldier devices moving in the field will now show up on this map.

**Good luck! If you get stuck on any step, please contact a technician for support.**
