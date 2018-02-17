# Hacking notes

This document contains notes about how to hack on AndProx, design philosophy, and code changes made
to PM3.

In this file, you'll find:

* [Getting the code](#getting-the-code) from the git repository.

* [Testing / Developing AndProx](#developing--testing-andprox), including how to debug on
  [virtual](#with-an-emulator) and [physical hardware](#with-physical-hardware).

* [Code Layout](#code-layout), including AndProx-specific PM3 client functionality, and [key
  differences between AndProx and upstream PM3](#key-differences).

* [Communication flow](#communication-flow), including how a typical command is handled.

# Getting the code

This project uses [git submodules][1].  You'll need to grab them with a command like:

```
$ git clone --recurse-submodules https://github.com/AndProx/AndProx.git
```

Shallow clones won't work.  If you see missing compile dependencies (eg: `:GraphView`,
`:usb-serial-for-android`) from Gradle, you probably haven't pulled the submodules.

# Testing / developing AndProx

AndProx can be imported into Android Studio using the Gradle files provided in this project.  Do not
check in Android Studio project files into the source repository.

## With an emulator

> **Note:** There are some issues with current Proxmark3 firmware through an emulator, so this may
> not work.

The standard Android Emulator does **not** support USB pass-through, so you'll only be able to test
offline mode with it.

You'll need to use another system emulator (eg: VirtualBox) and your own Android system images (eg:
[Android-x86][2]).  Android-x86 enables ADB over TCP debugging by default, so additional steps may
be required if you use other images.

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

## With physical hardware

First you should test that your cables work by connecting a USB Mouse to your device.  It should
light up and display a cursor on-screen when you move it.

> *If you have a device which uses **USB-C** (eg: Nexus 5X, 6P, Pixel C, Pixel Phone):*
>
> USB-C to Micro-B USB adapters do not work with Micro-B [OTG][3] cables. You need a USB-C to USB-A
> adapter.

Most Android devices only have one USB port, which means it's not possible to debug the application
over USB and run the Proxmark at the same time.

However, you can enable TCP/IP debugging over WiFi, which will free up the USB port for the
proxmark.  To use this:

1. Connect your device to your PC via USB.
2. Run `adb tcpip 5555` to switch your device to TCP/IP debugging mode
3. Disconnect the USB cable
4. Run `adb connect 192.0.2.2` (substituting with your phone's IPv4 address)

You can then switch back to usb debugging with `adb usb` or by restarting the device.

Or if your device is rooted, you can do this with:

```
su
setprop service.adb.tcp.port 5555
stop adbd
start adbd
```

Once TCP/IP debugging is enabled, you can use your Proxmark by plugging it into your Android device
with a [USB-OTG cable][3] or USB-C to USB-A dongle.

# Code layout

* `app`: Contains the front-end Android application.  `au.id.micolous.andprox` namespace.

* `natives`: JNI wrappers for the Proxmark3 client code and build scripts for the PM3 client.

* `third_party`: Contains third-party code which we link to:

  * `GraphView`: Charting and graphing library for Android.

  * `proxmark3`: Upstream proxmark3 client with minimal modifications.

  * `usb-serial-for-android`: Userspace Android library for interfacing with USB serial devices.

AndProx uses a minimally modified version of the Proxmark3 mainline codebase.  Any modifications
made are pushed upstream, so that they no longer have to be carried in a separate branch of PM3.

There are some replacement modules in `natives/src/main/cpp/` which implement Android-specific
functionality:

* A JNI interface for PM3 client, so that it can run as a library.

* Stubs for the client's main code to get the command interpreter running, as well as tell modules
  where to find and write out their files.

* A new tuning API, to allow meaningful graphs to be rendered in Android.

* JNI replacement of `PrintAndLog`, to bring logs into Android space with JNI.

* JNI implementation of `uart.h` (`uart_android.c`), which lets PM3 use `NativeSerialWrapper` and
  `usb-serial-for-android` (an Android userspace USB-serial driver, written in Java).

* Use CMake rather than Makefiles, for better integration with Android build tools, and
  AndProx-specific requirements.

Occasionally, PM3 pulls in extra libraries.  When this happens, the changes need to be also
implemented in AndProx's CMake file.

## Key differences

In general, AndProx aims to _minimise_ changes to Proxmark3, and work towards upstreaming any
required changes that can't be kept in a separate file.  This is done to minimise the effort
required to update to newer versions of PM3, or to switch to other distributions.

Most PM3 commands should "just work" -- commands are passed in to the interpreter in the same way
that the regular C client would.  There is a `uart_receiver` thread like the mainline client, which
can dispatch events into the command buffer as normal.  The `uart` functions are all wrapped, but
expose the exact same API.

There are some platform-specific differences:

* AndProx has no stdin/out, so `printf` and `gets` won't work.  This breaks a lot of the interactive
  scripting in Lua.

* ncurses and readline are also unavailable.

* AndProx uses Android's zlib rather than PM3's. PM3's `inflate` function strips out support for
  some zlib functionality that is never used (like fixed block coding), and has some extra
  functionality in `deflate`. But only `inflate` is ever used in the client.

# Communication flow

## Setting up the library

1. `Natives.initProxmark` sets up the PM3 initial state.

2. AndProx connects to the PM3's USB serial device using `usb-serial-for-android`, and wraps the
   connection in `NativeSerialWrapper`.

3. AndProx calls `Natives.setSerialPort(NativeSerialWrapper)`.

4. `setSerialPort` calls out to `uart_android_open`, and sets up a `serial_port` struct with
   references to the Java object for use in JNI.

## Commands

A typical command follows this process:

1.  The command is entered by the user into `CliActivity`.

2.  `CliActivity` spawns an `SendCommandTask`.

3.  `SendCommandTask` calls `Natives.sendCmd` (JNI bindings).

4.  `Natives.sendCmd` calls `CommandReceived` (PM3's main command entry point).

5.  PM3 dispatches the command through its regular command parser.

6.  The specific command in PM3 calls `SendCommand`.

7.  `SendCommand` stuffs the requested command into `txcmd` buffer.

8.  `txcmd` buffer is picked up by the `uart_receiver` thread, and send with `uart_send`
    (in `uart_android.c`).

9.  AndProx `uart_android.c` converts the buffer into Java types and passes it up to
    `NativeSerialWrapper` in Java.

10. `NativeSerialWrapper` passes off to `UsbSerialPort` (usb-serial-for-android).

11. `usb-serial-for-android` uses Android's USB Host API to send the command to the PM3 device.

12. The `uart_receiver` thread polls `uart_receive` (in `uart_android.c`).

13. AndProx `uart_android.c` polls `NativeSerialWrapper` over JNI, and converts Java types back into
    standard C types.

14. `uart_receiver` stores the command in a buffer, where it can later be collected in
    `WaitForResponse`.



[1]: https://git-scm.com/book/en/v2/Git-Tools-Submodules
[2]: http://www.android-x86.org/download
[3]: https://en.wikipedia.org/wiki/USB_On-The-Go
