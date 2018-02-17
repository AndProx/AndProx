package au.id.micolous.andprox.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import au.id.micolous.andprox.R;
import au.id.micolous.andprox.natives.Resources;

/**
 * Task to copy PM3's static files to storage.
 */
public class CopyTask extends AsyncTask<Void, Void, Boolean> {
    private ProgressDialog mProgressDialog;

    private WeakReference<Context> mContext;

    public CopyTask(Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Context c = mContext.get();

        mProgressDialog = ProgressDialog.show(c, c.getString(R.string.copying_assets), c.getString(R.string.wait_long), true, false);
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return Resources.extractPM3Resources(mContext.get());
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
            mProgressDialog = null;
        }

        if (!result) {
            Toast.makeText(mContext.get(), R.string.error_copying, Toast.LENGTH_LONG).show();
        }
    }
}