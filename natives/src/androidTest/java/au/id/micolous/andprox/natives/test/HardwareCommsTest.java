package au.id.micolous.andprox.natives.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

import au.id.micolous.andprox.natives.NativeSerialWrapper;
import au.id.micolous.andprox.natives.Natives;
import au.id.micolous.andprox.natives.test.utils.LogSink;
import au.id.micolous.andprox.natives.test.utils.UsbCommandMatcher;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



/**
 * This test case mocks out NativeSerialWrapper in order to make a virtual PM3 device, which can
 * respond to CMD_VERSION.  It will then spin up the PM3 client in JNI with this mocked
 * NativeSerialWrapper, and verifies that the client correctly communicated with the device, and
 * that it was able to log a custom version string.
 */
public class HardwareCommsTest {
    private static final long CMD_VERSION = 0x107;
    private static final long CMD_ACK = 0xff;

    @Mock
    NativeSerialWrapper mNativeSerialWrapper;

    private LogSink mLogSink = new LogSink();

    private boolean versionPending = false;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mNativeSerialWrapper.send(argThat(new UsbCommandMatcher(CMD_VERSION))))
                .thenAnswer(new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {
                        versionPending = true;
                        return true;
                    }
                });

        when(mNativeSerialWrapper.receive(any(byte[].class)))
                .thenAnswer(new Answer<Integer>() {
                    @Override
                    public Integer answer(InvocationOnMock invocation) throws Throwable {
                        if (versionPending) {
                            versionPending = false;

                            byte[] buffer = invocation.getArgument(0);

                            // Copy a reply into the buffer.
                            Arrays.fill(buffer, (byte)0);
                            ByteBuffer bb = ByteBuffer.wrap(buffer);
                            bb.order(ByteOrder.LITTLE_ENDIAN);
                            bb.putLong(CMD_ACK);
                            bb.putLong(0x270B0A40); // AT91SAM7S512 Rev A
                            bb.putLong(0x100); // 512 bytes used
                            bb.putLong(0); // unused value
                            // Whatever we write next is printed to the log.
                            bb.put("hello HardwareCommsTest".getBytes(Charset.forName("UTF-8")));
                            return UsbCommandMatcher.USB_COMMAND_LENGTH;
                        }

                        return null;
                    }
                });

        Natives.initProxmark();
        Natives.registerPrintAndLogHandler(mLogSink);
    }

    @Test
    public void testInitSequence() {
        mLogSink.reset();
        assertTrue("The device must be offline at the start", Natives.isOffline());
        Natives.startReaderThread(mNativeSerialWrapper);
        Natives.sendCmdVersion();

        verify(mNativeSerialWrapper).send(argThat(new UsbCommandMatcher(CMD_VERSION)));

        assertNull("We shouldn't find an error in the log", mLogSink.findInLogLines("got no response"));
        assertNotNull("We should find our custom message in the log", mLogSink.findInLogLines("hello HardwareCommsTest"));

    }

    @After
    public void tearDown() throws Exception {
        Natives.stopReaderThread();
    }

}
