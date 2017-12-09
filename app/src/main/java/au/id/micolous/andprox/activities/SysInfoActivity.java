package au.id.micolous.andprox.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.R;

public class SysInfoActivity extends AppCompatActivity {

    private String debugOutput;
    private static final String CLIP_TITLE = "AndProx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sys_info);
        setTitle(R.string.sys_info_title);

        // Generate debugging information
        debugOutput = AndProxApplication.getDeviceInfo();
        ((TextView)findViewById(R.id.tvDebugOutput)).setText(debugOutput);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sys_info, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.miCopySysInfo:
                ClipboardManager manager = (ClipboardManager)getSystemService(Activity.CLIPBOARD_SERVICE);
                if (manager == null) {
                    Toast.makeText(this, "Couldn't access clipboard", Toast.LENGTH_LONG).show();
                    return true;
                }

                manager.setPrimaryClip(ClipData.newPlainText(CLIP_TITLE, debugOutput));
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_LONG).show();
                return true;

            case R.id.miShareSysInfo:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, debugOutput);
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
