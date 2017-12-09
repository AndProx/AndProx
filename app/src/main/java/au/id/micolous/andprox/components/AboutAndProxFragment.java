package au.id.micolous.andprox.components;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import au.id.micolous.andprox.AndProxApplication;
import au.id.micolous.andprox.R;
import au.id.micolous.andprox.Utils;
import au.id.micolous.andprox.natives.Natives;


/**
 *
 */
public class AboutAndProxFragment extends Fragment {

    public AboutAndProxFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AboutAndProxFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AboutAndProxFragment newInstance() {
        return new AboutAndProxFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_about_and_prox, container, false);

        ((TextView)v.findViewById(R.id.tvVersionString)).setText(
                Utils.localizeString(R.string.app_version, AndProxApplication.getVersionString()));

        ((TextView)v.findViewById(R.id.tvPm3ClientVersion)).setText(
                Utils.localizeString(R.string.pm3_client_version, Natives.getProxmarkClientVersion()));

        ((TextView)v.findViewById(R.id.tvPm3BuildTS)).setText(
                Utils.localizeString(R.string.pm3_build_ts, Natives.getProxmarkClientBuildTimestamp()));


        return v;
    }
}
