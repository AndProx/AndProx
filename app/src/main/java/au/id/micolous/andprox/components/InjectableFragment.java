package au.id.micolous.andprox.components;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.behavior.format.IFormatDevice;

public abstract class InjectableFragment extends Fragment {

    @Inject
    protected IFormatDevice formatDevice;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((AndProxApplication)context.getApplicationContext()).inject(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((AndProxApplication)activity.getApplication()).inject(this);
    }
}
