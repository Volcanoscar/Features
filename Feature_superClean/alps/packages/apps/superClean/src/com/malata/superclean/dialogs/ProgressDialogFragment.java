package com.malata.superclean.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

/**
 * Created by xuxiantao on 2015/9/14.
 */
public class ProgressDialogFragment extends DialogFragment {

    int mIndeterminateDrawable;
    String mMessage;

    public static ProgressDialogFragment newInstance(int indeterminateDrawable, String message) {
        ProgressDialogFragment progressDialogFragment = new ProgressDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("indeterminateDrawable", indeterminateDrawable);
        bundle.putString("message", message);
        progressDialogFragment.setArguments(bundle);

        return progressDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIndeterminateDrawable = getArguments().getInt("indeterminateDrawable");
        mMessage = getArguments().getString("message");

        ProgressDialog mProgressDialog = new ProgressDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT);
        if(mIndeterminateDrawable > 0) {
            mProgressDialog.setIndeterminateDrawable(getActivity().getResources().getDrawable(mIndeterminateDrawable));
        }

        if(mMessage != null) {
            mProgressDialog.setMessage(mMessage);
        }

        return mProgressDialog;
    }

    public void setMessage(String mMessage) {
        if(mMessage != null) {
            setMessage(mMessage);
        }
    }
}
