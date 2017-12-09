# AndProx

Prototype / work-in-progress native Android client for [Proxmark 3][1], which doesn't require root,
permission changes or other messing about with kernel modules.

This is intended to become a replacement for [Proxdroid][3] and other "root required" Android forks.

This uses the [Android USB Host API][2] and [mik3y's USB Serial for Android driver][4] in order to
access the Proxmark's USB ACM interface from user-space Java code.

## Compatibility

AndProx requires an Android device with [USB Host support][2].  This can either be provided via a
USB OTG cable, USB Type C to USB Type A adapter, or ports directly on the device.

This project targets the [mainline firmware][1], so some functionality may not be available or
broken if you use another branch.  It is suggested that you build your firmware from whatever commit
`third_party/proxmark3` points at.  Other firmwares may contain incompatible RPC messages.

This software is primarily tested on a Google Pixel Phone and Hardkernel ODroid U3.  It requires at
least Android 5.0 (API 21).

> **Note:** Some hardware, like the Nexus 4, _does not support USB Host mode_.

> **Note:** Some hardware, like Sony Android TV devices, _doesn't properly expose CDC ACM devices to
> userspace Android software_. You should absolutely not have the `cdc_acm` Linux kernel module
> loaded on your device.

## Functionality / Known Issues

- Cross compiles to `armeabi`, `armeabi-v7a`, `arm64-v8a`, `x86` and `x86_64`.  Only really tested
  on ARM.

- Many LF commands appear to work.

- LF graphs are not available yet. (Issue #1, Issue #2)

- Scripting doesn't work properly, but most of the ground work is there. (Issue #3)

- Most HF card functions don't work properly yet (likely to be a firmware issue). (Issue #4)

- `hf mf hardnested` commands use a lot of memory. In the event of running out of memory, it will
  close the application without warning.

- Anything output from `printf` won't be displayed.  `PrintAndLog` will be displayed, but will not
  be written to disk. (Issue #5)

- Probably doesn't exit or sleep cleanly, causing high battery use. Swipe the app away from the App
  Switcher when done, and unplug the PM3.

## Getting started

1. Install the APK.

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

## Licensing

This project is based on [the Proxmark 3 project][1] (licensed under the GPLv2+ license).  Portions
of proxmark3 are licensed under differently.  This also uses the [GraphView][5] (GPLv2+ with linking
exception) and [usb-serial-for-android][4] (LGPLv2.1+).

Copyright 2016-2017 Michael Farrell.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU
General Public License as published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not,
see <http://www.gnu.org/licenses/>.

Under section 7 of the GNU General Public License v3, the following "further restrictions" apply to
this program:

* (b) You must preserve reasonable legal notices and author attributions in the program.

* (c) You must not misrepresent the origin of this program, and need to mark modified versions in
  reasonable ways as different from the original version (such as changing the name and logos).

* (d) You may not use the names of licensors or authors for publicity purposes, without explicit
  written permission.

[0]: https://github.com/AndProx/AndProx
[1]: https://github.com/Proxmark/proxmark3
[2]: https://developer.android.com/guide/topics/connectivity/usb/host.html
[3]: https://github.com/Proxmark/proxmark3/wiki/android
[4]: https://github.com/mik3y/usb-serial-for-android
[5]: https://github.com/appsthatmatter/GraphView
