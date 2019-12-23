package au.id.micolous.andprox.behavior.firmware;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import au.id.micolous.andprox.R;
import au.id.micolous.andprox.Utils;
import au.id.micolous.andprox.natives.Natives;

public class FirmwareManagerImpl implements IFirmwareManager {

    private Context context;

    public FirmwareManagerImpl(Context context) {
        this.context = context;
    }

    @Override
    public void unsupportedFirmwareError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(Utils.localizeString(this.context, R.string.reflash_required_message, Natives.getProxmarkClientVersion()))
                .setTitle(R.string.reflash_required_title)
                .setPositiveButton(R.string.instructions, (dialog, which) -> {
                    context.startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Proxmark/proxmark3/wiki/flashing")));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false);
        builder.show();
    }
}
