package com.github.gretaand.surfacewaterqualityapp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.github.gretaand.surfacewaterqualityapp.R;

/**
 * Creates dialog fragment that is displayed if the user denies permissions for locations and clicks
 * "do not ask again" or has a device policy to deny permissions. This dialog give rationale for
 * permissions and asks user to either click "settings" to change permission for location or to
 * continue to the app without all features.
 *
 * @author greta
 */
public class PermissionsDeniedDialogFragment extends DialogFragment {

    // Use this instance of the interface to deliver action events
    private PermissionsDeniedDialogListener mListener;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface PermissionsDeniedDialogListener {
        void onPermissionsDeniedDialogPositiveClick();
        void onPermissionsDeniedDialogNegativeClick();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PermissionsDeniedDialogFragment.PermissionsDeniedDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement PermissionsDeniedDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.enter_app_without_location)
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onPermissionsDeniedDialogPositiveClick();
                    }
                })
                .setNegativeButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onPermissionsDeniedDialogNegativeClick();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
