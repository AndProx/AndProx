# Setting up the Android Emulator

The Android Emulator doesn't support USB.  Instead, you'll need to:

- Debug using a TCP connection to your Proxmark3 (this document).
- [Use something like VirtualBox which supports USB](./virtualbox.md).

This example is written with `socat` in mind, but should work with anything which can run a TCP
server which redirects all communications to the PM3.

TCP mode has _no transport security or authentication_, and will allow anything that can make an IP
connection to control your PM3.  The timeouts are also extremely short -- and are probably
unsuitable for anything except local-loopback or LAN communication.

## Exposing the PM3 over TCP

These examples open a TCP server on `localhost` port `1234`.

### Using `socat`

On the host machine, run:

```
socat TCP-LISTEN:1234,bind=127.0.0.1,reuseaddr /dev/ttyACM0,raw,echo=0
```

## Connecting using AndProx

AndProx attempts to detect the Android Emulator, and fills in the IP automatically [which
corresponds to `127.0.0.1` on the host][emu-netaddr] (`10.0.2.2`).

If this has failed for some reason, you can fill this in manually.


[emu-netaddr]: https://developer.android.com/studio/run/emulator-networking#networkaddresses