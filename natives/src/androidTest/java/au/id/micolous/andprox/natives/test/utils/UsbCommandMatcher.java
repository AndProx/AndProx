package au.id.micolous.andprox.natives.test.utils;

import org.mockito.ArgumentMatcher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.annotation.Nullable;

/**
 * Matches UsbCommand messages that we got on the wire.
 *
 * A UsbCommand (defined in usb_cmd.h) is a struct consisting of:
 * {
 *     uint64_t command;
 *     uint64_t arg0;
 *     uint64_t arg1;
 *     uint64_t arg2;
 *     byte_t[512] data;
 * }
 *
 * All the values are little endian.
 */

public class UsbCommandMatcher implements ArgumentMatcher<byte[]> {
    public static final int USB_COMMAND_LENGTH = (8 * 4) + 512;

    @Nullable
    private Long mMatchingCommand = null;

    @Nullable
    private Long mMatchingArg0 = null;

    @Nullable
    private Long mMatchingArg1 = null;

    @Nullable
    private Long mMatchingArg2 = null;

    public UsbCommandMatcher(long command) {
        mMatchingCommand = command;
    }

    @Override
    public boolean matches(byte[] argument) {
        if (argument.length != USB_COMMAND_LENGTH) {
            return false;
        }

        // deserialise the integer
        ByteBuffer bb = ByteBuffer.wrap(argument);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        long command = bb.getLong();
        long arg0 = bb.getLong();
        long arg1 = bb.getLong();
        long arg2 = bb.getLong();

        return (mMatchingCommand == null || mMatchingCommand.equals(command)) &&
                (mMatchingArg0 == null || mMatchingArg0.equals(arg0)) &&
                (mMatchingArg1 == null || mMatchingArg1.equals(arg1)) &&
                (mMatchingArg2 == null || mMatchingArg2.equals(arg2));
    }
}
