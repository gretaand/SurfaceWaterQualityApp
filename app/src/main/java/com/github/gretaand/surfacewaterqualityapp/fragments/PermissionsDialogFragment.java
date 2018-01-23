package com.github.gretaand.surfacewaterqualityapp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.github.gretaand.surfacewaterqualityapp.R;

/**
 * Creates dialog fragment that is displayed if the user initially denies permissions for locations.
 * This dialog give rationale for permissions and asks again to either give location permission
 * or continue to the app without all features.
 *
 * @author greta
 */
public class PermissionsDialogFragment extends DialogFragment {

    // Use this instance of the interface to deliver action events
    private PermissionsDialogListener mListener;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface PermissionsDialogListener {
        void onPermissionsDialogPositiveClick();
        void onPermissionsDialogNegativeClick();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PermissionsDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement PermissionsDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.permission_rationale)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onPermissionsDialogPositiveClick();
                    }
                })
                .setNegativeButton(R.string.continue_without_location,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onPermissionsDialogNegativeClick();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
