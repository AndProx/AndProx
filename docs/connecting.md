# Connecting PM3 to Android devices

Note: these instructions **do not work** for Android Runtime for ChromeOS (ARC).

Your first stop should be the [compatibility list][compat].

## Types of connectors

### USB-A

These are "regular USB ports" that are on most PCs. They are also present on some Android tablets,
TVs, and set top boxes.

Nothing really special is needed here -- you should be able to plug devices in without any special
adapters.

For mains-powered devices, you're unlikely to have [power issues](#power-issues).

If a device has this port, try this first.

### USB Mini-B

This port is fairly uncommon on Android devices.  This has the same requirements as USB Micro-B (see
below).

### USB Micro-B

This port is commonly found on Android devices produced before 2016, and is also present on some
Android set top boxes.

You'll need a [USB On-The-Go (OTG)][usb-otg] adapter to plug in USB devices.

Devices typically only supply 100 mA through the port, so you will likely have [power
issues](#power-issues).  Some devices do not implement USB-OTG correctly.

[usb-otg]: https://en.wikipedia.org/wiki/USB_On-The-Go

### USB C

This port is commonly found on Android devices produced after 2016.

You'll need a USB C to A adapter to plug in USB devices.  You cannot use a USB C to Micro B adapter.

Power delivery out of the port is variable, and depends on your device, and the USB C adapter that
you are using.  All USB C cables and adapters contain microcontrollers at the USB C ends of the
cable, to negotiate power levels.  As a result, you are [likely to have power issues](#power-issues)
with phones and tablets.

## Initial test

Before connecting the PM3, you should test that other USB devices work correctly in Android.

First you should test that your cables work by connecting a USB Mouse to your device.  It should
light up and display a cursor on-screen when you move it.

## Common issues

### Power issues

Some devices don't supply enough power for PM3 (150 mA). To get extra power, you need a USB OTG
Y-cable or USB Mini-B Y-cable, and an external power source (like a USB battery pack).  There are
two degrees of issues with this:

* **Devices which don't supply power at all:** these always need a Y-cable to work. Some devices,
  like _Nexus 4_, have `android.hardware.usb.host` disabled, and will require a third-party Android
  distribution to enable the functionality.

* **Needed for HF:** these supply about 100 mA, and this is not quite enough for HF cards. Commands
  may not work as expected, or at all.  Sometimes, the PM3 will just hang.

### Product Name bug

Android devices from many manufacturers have a "product name" bug, where they do not present USB
devices to applications if they have an invalid USB Product Name.

Many versions of the Proxmark3 firmware have a glitch where they only respond to USB String
Descriptor requests for a product name after about a second.  This is long enough to time out.

[Proxmark3 PR#565][6] resolves this issue -- which requires reflashing the device firmware.
