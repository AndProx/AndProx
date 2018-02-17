# Compatibility

AndProx requires an Android device with [USB Host support][2] (`android.hardware.usb.host`), and
Android 5.0 (API 21). [Android 4.4 _may_ work if you compile it with `api-19.patch`.][3]

This project targets the [mainline firmware][1], so some functionality may not be available or
broken if you use another branch.  It is suggested that you build your firmware from whatever commit
`third_party/proxmark3` points at.  Other firmwares may contain incompatible RPC messages.

You can connect the Proxmark3 to your device via one of the following:

- **USB C:** Using a USB C to USB A adapter.

- **USB Micro-B:** Using a USB On-The-Go (OTG) cable, otherwise known as USB Micro-B to USB A.

- **USB A:** Connecting the Proxmark3 to a USB port on the device.

In addition to these compatibility tables, you may find [usb-serial-for-android's Compatible Android
 Devices][0] a useful source of information.

## Known compatible devices

Manufacturer | Device            | Y-cable           | Notes
-------------|-------------------|-------------------|--------
Google       | Pixel 1 / XL      | Needed for HF     | Device includes USB-C to A dongle
LG           | Nexus 5X          |
Sony         | Bravia TV (2013+) | Not required      | Has "Product Name" bug.


## Known incompatible devices

Manufacturer | Device            | Notes
-------------|-------------------|------------
Google / LG  | Nexus 4           | [Does not supply 5v][10]. Custom or old kernel may allow use with OTG-Y cable.

[**Any device with the `cdc_acm` kernel module will be incompatible.**][4]  Try unloading or unbinding
the module before running AndProx.

[**App Runtime for ChromeOS (ARC)** is incompatible, because ARC does not support USB Host.][5]

## Issues

### Y-cable

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

[Proxmark3 PR#565][6] resolves this issue.

[0]: https://github.com/mik3y/usb-serial-for-android/wiki/Compatible-Android-Devices
[1]: https://github.com/Proxmark/proxmark3
[2]: https://developer.android.com/guide/topics/connectivity/usb/host.html
[3]: https://github.com/AndProx/AndProx/issues/7
[4]: https://github.com/AndProx/AndProx/issues/8
[5]: https://developer.android.com/topic/arc/manifest.html#incompat-entries
[6]: https://github.com/Proxmark/proxmark3/pull/565
[10]: https://android.googlesource.com/device/lge/mako/+/fe9f2793424c61588c093df951733347d0d24df4%5E%21/#
