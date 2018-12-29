# firmware

This contains a Gradle wrapper to call out to PM3's build system to produce firmware and bootloader.

**AndProx cannot flash this firmware** -- it requires that you build or download binaries for the
flasher separately, and run them on your PC.

To build firmware, you'll need to install [Proxmark3's dependencies][pm3-start], which includes an
ARM toolchain. The ARM toolchain in the Android NDK won't let you build firmware.

## firmware image (fullimage.elf)

To build:

```
./gradlew --no-daemon firmware:zipFirmware
```

This will produce:

- `firmware/build/firmware/fullimage.elf`: firmware image matching the PM3 client, which you can
  flash to your device.

- `firmware/build/zip/fullimage.elf.zip`: ZIP archive containing firmware. This is used when cutting
  a release of PM3.

## bootloader (bootrom.elf)

> **Note:** You only need to ensure the _firmware_ matches the version used by AndProx.
>
> _There is normally no need to reflash the bootloader for AndProx._
>
> _Do not reflash the bootloader, except using PM3's official version._ Improperly reflashing the
> bootloader can brick your PM3, and requires a JTAG interface device to fix it.

To build:

```
./gradlew --no-daemon firmware:zipBootloader
```

This will produce:

- `firmware/build/bootloader/bootrom.elf`: Bootloader image matching the PM3 client.

- `firmware/build/zip/bootrom.elf.zip`: ZIP archive containing the bootloader.

## Flashing the image

See [the instructions on the Proxmark3 wiki for more details about flashing][pm3-flash].

[pm3-start]: https://github.com/Proxmark/proxmark3/wiki/Getting-Started
[pm3-flash]: https://github.com/Proxmark/proxmark3/wiki/flashing
