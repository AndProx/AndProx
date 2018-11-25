# Setting up the Android Emulator

The Android Emulator doesn't support USB.  Instead, you'll need to:

- Debug using a TCP connection to your Proxmark3 (this document).
- [Use something like VirtualBox which supports USB](./virtualbox.md).

This example is written with `socat` in mind, but should work with anything which can run a TCP
server which redirects all communications to the PM3.

## Exposing the PM3 over TCP

These examples open a TCP server on `localhost` port `1234`.

### Using `socat`

On the host machine, run:

```
socat TCP-LISTEN:1234,bind=127.0.0.1,reuseaddr /dev/ttyACM0,raw,echo=0
```

## Connecting using AndProx

You can connect to `127.0.0.1:1234` on your host using the following host/port in the Android
Emulator running AndProx:

```
10.0.2.2:1234
```

`10.0.2.2` is a [special IP used in the Android emulator][emu-netaddr] which maps to `127.0.0.1` on
your host machine.


[emu-netaddr]: https://developer.android.com/studio/run/emulator-networking#networkaddresses