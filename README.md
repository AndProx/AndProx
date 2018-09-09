# ![logo](https://github.com/AndProx/AndProx/raw/master/app/src/main/res/mipmap-hdpi/ic_launcher.png) AndProx

[![Build Status](https://travis-ci.org/AndProx/AndProx.svg?branch=master)](https://travis-ci.org/AndProx/AndProx)

Prototype / work-in-progress native Android client for [Proxmark 3][1], which doesn't require root,
permission changes or other messing about with kernel modules.

This is intended to become a replacement for [Proxdroid][3] and other "root required" Android forks.

This uses the [Android USB Host API][2] and [mik3y's USB Serial for Android driver][4] in order to
access the Proxmark's USB ACM interface from user-space Java code.

**[Opt-in to the beta on Google Play][7]** | **[Direct APK download also available][6]**

![lf tune](https://github.com/AndProx/AndProx/raw/master/assets/v2_phone/lf-tune.png)

![lf t55xx detect](https://github.com/AndProx/AndProx/raw/master/assets/v2_phone/t55xx-detect.png)

## Compatibility

See `COMPATIBILITY.md`.

## Functionality / Known Issues

- Cross compiles to `armeabi`, `armeabi-v7a`, `arm64-v8a`, `x86` and `x86_64`.  Only really tested
  on ARM.

- Many LF commands appear to work. Some devices need a Y cable and external power source for HF
  commands.  See `COMPATIBILITY.md` for details.

- LF graphs are not available yet. (Issue #1, Issue #2)

- Scripting doesn't work properly, but most of the ground work is there. (Issue #3)

- `hf mf hardnested` commands use a lot of memory. In the event of running out of memory, it will
  close the application without warning.

- Anything output from `printf` won't be displayed.  `PrintAndLog` will be displayed, but will not
  be written to disk. (Issue #5)

- Flasher has not been implemented.

- Probably doesn't exit or sleep cleanly, causing high battery use. Swipe the app away from the App
  Switcher when done, and unplug the PM3.

## Getting started

1. [Install the APK][7], or [side-load the APK from GitHub][6].

2. Plug your Proxmark3 into your Android device.

3. Run AndProx, and accept the permissions bump.

4. Press "Connect"

5. Try `hw version`, `hw status` and `hw tune` to test out the device!

AndProx will write files to _Internal Storage_, in the `proxmark3` directory.

## Troubleshooting

Use the _System Info_ to show what USB devices AndProx can see.  You should see:

* USB Host: yes

  (If it says "no", your device doesn't support USB Host.)

* Found >1 USB Devices; (9ac4:4b8f; name = null)

  (If it shows 2d2d : 504d -- you have an old firmware and should upgrade.)

  (If it shows some other ID -- you may have a third-party firmware and should switch to mainline.)

  (Some devices have WiFi/Bluetooth radios connected over USB, you can ignore those.)

  (If it shows nothing, then your device isn't exposing USB devices properly, or may have the
  `cdc_acm` kernel module loaded when it shouldn't.)

* Found 1 suitable drivers; CdcAcmSerialPort

  (If it shows no suitable drivers, but the correct IDs are showing, then this is probably a
  firmware issue.)

## Building the code

See `HACKING.md`.

**Do not download the ZIP file from GitHub -- it will not work!**

## Licensing

Copyright 2016-2018 Michael Farrell.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU
General Public License as published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not,
see <http://www.gnu.org/licenses/>.

Under section 7 of the GNU General Public License v3, the following "additional terms" apply to
this program:

* (b) You must preserve reasonable legal notices and author attributions in the program.

* (c) You must not misrepresent the origin of this program, and need to mark modified versions in
  reasonable ways as different from the original version (such as changing the name and logos).

* (d) You may not use the names of licensors or authors for publicity purposes, without explicit
  written permission.

AndProx includes several third-party components under other licenses.  More detail of these
components is shown in `third_party/README.md`.

[0]: https://github.com/AndProx/AndProx
[1]: https://github.com/Proxmark/proxmark3
[2]: https://developer.android.com/guide/topics/connectivity/usb/host.html
[3]: https://github.com/Proxmark/proxmark3/wiki/android
[4]: https://github.com/mik3y/usb-serial-for-android
[6]: https://github.com/AndProx/AndProx/releases
[7]: https://play.google.com/apps/testing/au.id.micolous.andprox
