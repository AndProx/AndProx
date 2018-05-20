/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Under section 7 of the GNU General Public License v3, the following additional
 * terms apply to this program:
 *
 *  (b) You must preserve reasonable legal notices and author attributions in
 *      the program.
 *  (c) You must not misrepresent the origin of this program, and need to mark
 *      modified versions in reasonable ways as different from the original
 *      version (such as changing the name and logos).
 *  (d) You may not use the names of licensors or authors for publicity
 *      purposes, without explicit written permission.
 */

package au.id.micolous.andprox.components;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import au.id.micolous.andprox.R;

/**
 * A pop-up fragment that allows for input of text.
 */

public class TextInputDialogFragment extends DialogFragment {
    public interface TextInputDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, String input);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    private static final String SUMMARY_RES = "summary_res";
    private int mSummaryRes;
    private TextInputDialogListener mListener;

    public static TextInputDialogFragment newInstance(@StringRes int summary, TextInputDialogListener listener) {
        TextInputDialogFragment f = new TextInputDialogFragment();

        Bundle args = new Bundle();
        args.putInt(SUMMARY_RES, summary);
        f.setArguments(args);

        f.mListener = listener;
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSummaryRes = getArguments().getInt(SUMMARY_RES);
    }

    //@NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialog_textinput, null);
        TextView summary = (TextView)v.findViewById(R.id.summary);
        summary.setText(mSummaryRes);

        builder.setView(v)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // Fire the save event back
                    EditText text = (EditText)TextInputDialogFragment.this.getDialog().findViewById(R.id.text);
                    mListener.onDialogPositiveClick(TextInputDialogFragment.this, text.getText().toString());
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    TextInputDialogFragment.this.getDialog().cancel();
                    mListener.onDialogNegativeClick(TextInputDialogFragment.this);
                });
        return builder.create();
    }

    /*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (TextInputDialogListener) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_textinput, container, false);

        TextView lblSummary = (TextView)getView().findViewById(R.id.summary);
        lblSummary.setText(summaryRes);

        return v;
    }
*/

}
