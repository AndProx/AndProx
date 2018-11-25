# Debugging AndProx on physical hardware

Most Android devices only have one USB port, which means it's not possible to debug the application
over USB and run the Proxmark at the same time.

However, you can enable TCP/IP debugging over WiFi, which will free up the USB port for the
proxmark3.

## Enabling TCP debugging with adb

1. Connect your device to your PC via USB.
2. Run `adb tcpip 5555` to switch your device to TCP/IP debugging mode
3. Disconnect the USB cable
4. Run `adb connect 192.0.2.2` (substituting with your device's IPv4 address)

You can then switch back to usb debugging with `adb usb` or by restarting the device.

## Enabling TCP debugging on rooted devices

Rooted devices can do this on-device.  Enter these commands in a shell:

```
su
setprop service.adb.tcp.port 5555
stop adbd
start adbd
```

Some Android distributions also have a custom developer option to enable this.
