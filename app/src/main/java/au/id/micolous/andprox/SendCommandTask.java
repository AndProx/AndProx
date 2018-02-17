package au.id.micolous.andprox;

import android.os.AsyncTask;

import au.id.micolous.andprox.natives.Natives;

/**
 * Dispatches commands to the PM3 client thread.
 */

public class SendCommandTask extends AsyncTask<String, Void, Void> {
    public interface DoneCallback {
        void onCommandFinished();
    }

    protected DoneCallback c;

    public SendCommandTask() {
        this(null);
    }

    public SendCommandTask(DoneCallback c) {
        super();
        this.c = c;
    }

    @Override
    protected Void doInBackground(String... cmds) {
        for (String cmd : cmds) {
            Natives.sendCmd(cmd);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        c.onCommandFinished();
    }
}
