package au.id.micolous.andprox;

import android.os.AsyncTask;

import au.id.micolous.andprox.natives.Natives;

/**
 * Created by michael on 3/12/17.
 */

public class SendCommandTask extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... cmds) {
        for (String cmd : cmds) {
            Natives.sendCmd(cmd);
        }
        return null;
    }
}
