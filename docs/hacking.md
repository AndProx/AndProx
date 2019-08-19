# Hacking notes

This document contains notes about how to hack on AndProx, design philosophy, and code changes made
to PM3.

In this file, you'll find:

* [Getting the code](#getting-the-code) from the git repository.

* [Testing / Developing AndProx](#developing--testing-andprox), including [solutions for common
  build issues][#common-build-issues].

* [Code Layout](#code-layout), including AndProx-specific PM3 client functionality, and [key
  differences between AndProx and upstream PM3](#key-differences).

* [Communication flow](#communication-flow), including how a typical command is handled.

# Getting the code

This project uses [git submodules][1].  You'll need to grab them with a command like:

```
$ git clone --recurse-submodules https://github.com/AndProx/AndProx.git
```

**Do not download ZIP files from GitHub.** `git` is used to tag parts of the build process, and its
metadata is _required._

> **Note:** GitHub's "Download ZIP" and using `git clone` without `--recurse-submodules` do not
> download submodules.  If you see missing compile dependencies (eg: `:GraphView`,
> `:usb-serial-for-android`) from Gradle, you probably haven't pulled the submodules!

# Testing / developing AndProx

AndProx can be imported into Android Studio using the Gradle files provided in this project.  Do not
check in Android Studio project files into the source repository.

You will need to install the following modules from [Android SDK Manager][4]:

* Android SDK Platform 21
* Android SDK Platform 28
* Android Build Tools 28.0.3
* CMake
* LLDB
* NDK

You will also need the `git` command-line tool, but you already have this as you checked out the
code from `git`, right?

## Building AndProx and PM3 client

You should be able to build the `:app` module in Android Studio, or use `./gradlew` to build the
`:app` project, and get an APK with nearly everything in it.

The default configuration will build for both 32 and 64-bit ARM and x86 systems, which should cover
most Android devices.

Android hardware with a MIPS processor is not supported.

## Building firmware

[See `firmware/README.md`](../firmware/README.md).

## Common build issues

See [`build-issues.md`](./build-issues.md).

# Code layout

* `app`: Contains the front-end Android application.  `au.id.micolous.andprox` namespace.

* `firmware`: Gradle build scripts for PM3 firmware, that wrap the PM3 build scripts.

* `natives`: JNI wrappers for the Proxmark3 client code and build scripts for the PM3 client.

* `natives27`: Additional tests for the Native code on Android API 28.

* `third_party`: Contains third-party code which we link to:

  * `GraphView`: Charting and graphing library for Android.

  * `proxmark3`: Upstream proxmark3 client with minimal modifications.

  * `usb-serial-for-android`: Userspace Android library for interfacing with USB serial devices.

## PM3 client extensions

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

## Key differences from regular PM3

In general, AndProx aims to _minimise_ changes to Proxmark3, and work towards upstreaming any
required changes that can't be kept in a separate file.  This is done to minimise the effort
required to update to newer versions of PM3, or to switch to other distributions.

Most PM3 commands should "just work" -- commands are passed in to the interpreter in the same way
that the regular C client would.  There is a `uart_receiver` thread like the mainline client, which
can dispatch events into the command buffer as normal.  The `uart` functions are all wrapped, but
expose the exact same API.

There are some platform-specific differences:

* AndProx has no stdin, so `gets` won't work.  This breaks a lot of the interactive scripting in
  Lua.

* ncurses and readline are also unavailable.

* AndProx uses Android's zlib rather than PM3's. PM3's `inflate` function strips out support for
  some zlib functionality that is never used (like fixed block coding), and has some extra
  functionality in `deflate`. But only `inflate` is ever used in the client.

# Communication flow

## Setting up the library

1. `Natives.initProxmark` sets up the PM3 initial state.

2. AndProx connects to the PM3's USB serial device using `usb-serial-for-android`, and wraps the
   connection in `NativeSerialWrapper`.

3. AndProx calls `Natives.startReaderThread(NativeSerialWrapper)`.

4. `startReaderThread` calls out to `OpenProxmarkAndroid`, a helper function used for driving PM3's
   `OpenProxmark` function:

   1. `OpenProxmarkAndroid` calls `uart_android_open`, and sets up a `serial_port_android` struct
      with references to the Java object for use in JNI, and the JVM.

   2. `OpenProxmarkAndroid` calls `OpenProxmark` with a `serial_port_android` struct (rather than
      `char*`) for a port path.

   3. `OpenProxmark` calls our dummy `uart_open` function, which just returns the
      `serial_port_android` exactly as it got it. We need to do this because the signature of the
      `uart_open` doesn't allow arbitrary data to be sent as part of a setup process.

   4. `OpenProxmark` starts up the worker thread and global state in the PM3 client.

At this point, PM3 will be pumping the serial device for events.

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

10. `NativeSerialWrapper` passes off to a `SerialInterface` (implemented in `app`:
    `TcpSerialAdapter` or ``UsbSerialAdapter`).

11. `UsbSerialAdapter` passes off to `usb-serial-for-android`, which uses Android's USB Host API to
    send the command to the PM3 device.

    `TcpSerialAdapter` passes off to `Socket`, which uses TCP Sockets to send the command to the PM3
    device.

12. The `uart_receiver` thread polls `uart_receive` (in `uart_android.c`).

13. AndProx `uart_android.c` polls `NativeSerialWrapper` over JNI, and converts Java types back into
    standard C types.

14. `uart_receiver` stores the command in a buffer.

15. The command can then handle the event with `WaitForResponse`, and pull the data back into the UI
    thread.

16. `NativeSerialWrapper` also listens for when there hasn't been a command from the device in a
    while, and will inject a `CMD_PING`, to make sure the device is still alive.

[1]: https://git-scm.com/book/en/v2/Git-Tools-Submodules
[4]: https://developer.android.com/studio/intro/update#sdk-manager
[5]: https://github.com/Proxmark/proxmark3/wiki/Getting-Started
[6]: https://github.com/Proxmark/proxmark3/wiki/flashing
