# Deugging with VirtualBox (or other x86 virtual machine)

> **Note:** There are some issues with current Proxmark3 firmware through an emulator, so this may
> not work.

The standard Android emulator doesn't support USB.  However, VirtualBox (and other x86 VMs) do!

For this, you'll need:

- An x86 virtual machine (eg: VirtualBox)
- An Android system image (eg: [Android-x86][and-x86])

[Android-x86][and-x86] enables ADB over TCP debugging by default, so additional steps may be
required if you use other images.

VirtualBox settings:

- Your user should be in `vboxusers`, otherwise USB will not work. You will need to log out and log
  in again for this to take effect.

- Create a new host-only adapter in VirtualBox preferences.

In the Virtual Machine:

- **General/Basic:** Use Linux 2.6 / 3.x / 4.x profile
- **System/Motherboard:** Base memory: 2048 MiB
- **System/Motherboard:** Boot device: Optical only
- **System/Motherboard:** Pointing Device: PS/2 Mouse
- **Storage/IDE:** Only `android-x86_64-6.0_r1.iso` should be inserted, enable "Live CD/DVD".
- **Audio:** Disable audio
- **Network/Adapter 1:** During setup, use Bridge or NAT. Once set up, use Host-only Adapter.
- **USB:** USB 1.1 controller should be enabled
- **USB:** Add device filter for vendor `9ac4` device `4b8f` (for current Proxmark3 firmware).
- **USB:** Add device filter for vendor `2d2d` device `504d` (for old Proxmark3 firmware).

Then when you start up the Android-x86 environment, enable debug features (Settings > About > click
Build number 7 times).

Once you have the environment configured but *before* connecting with `adb`, snapshot the running
system in VirtualBox so you can boot quickly.

Then in your host, connect to `adb` with `adb connect 192.168.56.101` (substituting with the actual
IP address of the guest).

If prompted by Google Play Services to check for harmful / malicious software, and you do not have
an active Internet connection in this VM (eg: using host-only adapter), press `Disagree`.  Otherwise
the system may hang for a while attempting to connect to Google.

[and-x86]: http://www.android-x86.org/download
