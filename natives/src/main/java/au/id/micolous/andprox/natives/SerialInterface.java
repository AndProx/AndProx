package au.id.micolous.andprox.natives;

import android.support.annotation.NonNull;

import java.io.IOException;

/**
 * Interface for defining serial port connections.
 */
public interface SerialInterface {
    interface Consumer {
        void accept(SerialInterface iface);
    }

    /**
     * Sends a message to the PM3
     * @param pbtTx The message buffer to send.
     * @return The number of bytes actually written
     * @throws IOException To be thrown on errors. Causes a shutdown.
     */
    int send(@NonNull byte[] pbtTx) throws IOException;

    /**
     * Receives a message from the PM3.
     * @param pbtRx The message buffer to copy in to.
     * @return The number of bytes recieved from the PM3, or -1 on error.
     * @throws IOException To be thrown on errors. Causes a shutdown.
     */
    int receive(@NonNull byte[] pbtRx) throws IOException;

    /**
     * Closes the connection to the PM3.
     */
    void close();
}
