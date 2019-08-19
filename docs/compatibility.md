# Compatibility

* [Proxmark SKU compatibility](#proxmark-sku-compatibility)
* [Firmware compatibility](#firmware-compatibility)
* [Known incompatible (Android) devices](#known-incompatible-devices)
* [Known compatible (Android) devices](#known-compatible-devices)

## Proxmark SKU compatibility

Proxmark3 is open hardware, and many vendors have customised the hardware in different ways.

If your device [works with the mainline firmware and client on your PC][mainline], then you should
be able to use it with AndProx, provided it meets [the other firmware compatibility
requirements](#firmware-compatibility).

If the customised version of PM3 that doesn't work with [the mainline firmware and/or
client][mainline], contact the device's vendor for help.

## Firmware compatibility

This project targets the [mainline firmware][mainline], so some functionality may not be available
or broken if you use another branch.

Like Proxmark3, _the client and the firmware on the device are symbiotic_ -- and there is **no**
guarantee of multi-version compatibility.

**To get correct firmware,** you need to [flash it with your computer][flashing]:

* If you're running a "versioned release" of AndProx (or got it from the Google Play Store), then
  you need to download [whatever firmware matches that version of AndProx][and-rel].

* If you're running AndProx from `git`, then you must build your firmware from whatever commit
  `third_party/proxmark3` points at.  [See the HACKING doc for more details][hacking].

**You cannot flash firmware with AndProx.**

When building from source, you **must** use source code checked out directly from git, and have
the `git` command line tool installed in your build environment. This is because of AndProx's use
of `git submodule` and [an issue with PM3's build system][pm3git].

> **Note:** You only need to ensure the _firmware_ (os) matches the version used by AndProx.
>
> _There is normally no need to reflash the bootloader for AndProx._
>
> _Do not reflash the bootloader, except using PM3's official version._ Improperly reflashing the
> bootloader _can brick your PM3,_ and requires a JTAG interface device to fix it.

### Incompatible firmware

The biggest cause of issues for new users of Proxmark3 (in general) is bad firmware on their device:

* Wrong or old firmware versions can fail in strange ways _that look like bugs._

* Many Proxmark3 device vendors ship old versions of PM3's firmware.

* Some Proxmark3 device vendors ship modified versions of PM3's firmware without corresponding
  source code (required by the GPL) or otherwise do not contribute their modifications back to the
  greater community.

_In light of this, AndProx intentionally refuses to run_ on old, or known-bad firmware.

The following firmware is _blocked by default_:

* _Old firmware is blocked:_

  Each version of AndProx indicates which version of PM3 it contains, and [the AndProx releases
  page][and-rel] lists compatible firmware versions for each release.

* _Firmware without version data is blocked:_ this generally indicates that the
  [firmware was built improperly][pm3git].

  This is common with devices sold via online marketplaces.

  Example `hw version` output with missing firmware versions:

  ```
  bootrom: /-suspect 2015-04-02 15:12:04
  os: /-suspect 2015-04-02 15:12:11
  ```

  Only the `os` is checked.

* _Firmware that mentions any online marketplace (eg: URLs, email addresses) is blocked:_ I have no
  idea what modifications have been done to the firmware. The vendors rarely release source code
  (required by the GPL) or contribute their changes back to the community.

* _iceman's firmware is blocked:_ iceman's fork tends to get a lot of bleeding-edge functionality
  before mainline PM3. As a result, patches have been merged in a different order, leading to
  incompatible RPC calls.

  I'm investigating an "Iceman AndProx" build, but first priority is to get mainline stable. Getting
  it working on iceman's fork will require many patches to be applied to it.

_Developers_ are welcome to disable these checks for themselves, _at their own risk_ by modifying
AndProx's source code. The method to change is `ProxmarkVersion.isSupportedVersion`.

**Bug reports with incompatible or non-supported firmware will not be accepted.**

Note: AndProx 2.0.3 and earlier had an issue where devices with _iceman's bootloader_ (but
_mainline OS/firmware_) would be blocked.  This is particularly an issue with the Proxmark3 RDV4
hardware.  _Later versions of AndProx have this issue fixed -- so upgrade if this impacts you._


[mainline]: https://github.com/Proxmark/proxmark3
[and-rel]: https://github.com/AndProx/AndProx/releases
[hacking]: ./hacking.md
[flashing]: https://github.com/Proxmark/proxmark3/wiki/flashing
[pm3git]: https://github.com/Proxmark/proxmark3/issues/12

## Known incompatible devices

The following **will not work under any circumstances, even if it appears in another list:**

* [**Any device with a version of Android older than 5.0.**][andold] You need to update your
  device's software. Contact your phone manufacturer for more information, or replace your device.

* **Any device with the MIPS CPU architecture.** These are fairly rare and no longer supported by
  Android. You will need to get a device with an x86 or ARM CPU instead.

### Non-working for USB connections

* [**Any device with the `cdc_acm` kernel module.**][cdcacm]  You will need to unload or unbind the
  module before running AndProx.

* **App Runtime for ChromeOS (ARC)**. [ARC does not support USB Host.][arcusb]

* [The Android Emulator in the Android SDK.](./debugging/android-emulator.md)

### Additional known incompatible devices

Manufacturer | Device            | Connector   | Notes
-------------|-------------------|-------------|--------
Google / LG  | Nexus 4           | USB Micro-B | [Does not supply 5v][nex4]

## Known compatible devices

Note: Even if the device appears in the list below, you **always need Android 5.0 or later to run
AndProx.** If it is not available from the manufacturer, you'll need to install a third-party
Android distribution to get it.

Manufacturer | Device            | Connector | Y-cable           | Notes
-------------|-------------------|-----------|-------------------|--------
Google       | Pixel 1 / 1 XL    | USB C     | Needed for HF     | Device includes USB-C to A dongle
Google       | Pixel 3a          | USB C     | Not required      | Device includes USB-C to A dongle
Google / LG  | Nexus 5X          | USB C     |
Samsung      | Galaxy S8         | USB C     |
Sony         | Bravia TV (2013+) | USB A     | Not required      | [Product name bug][prod-name]


[andold]: https://github.com/AndProx/AndProx/issues/7
[cdcacm]: https://github.com/AndProx/AndProx/issues/8
[arcusb]: https://developer.android.com/topic/arc/manifest.html#incompat-entries

[nex4]: https://android.googlesource.com/device/lge/mako/+/fe9f2793424c61588c093df951733347d0d24df4%5E%21/

[prod-name]: ./connecting.md#product-name

