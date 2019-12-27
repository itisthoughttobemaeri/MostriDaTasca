package com.example.progetto;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

public class PositionDialog extends DialogFragment {
    // Constant used to identify the number of the permission asked
    private static final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme));
        builder.setMessage("Since this game is position-based, you need to give location permission to play it.")
                .setTitle("Location permission")
                .setPositiveButton("Re-give permission", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
                    }
                })
                .setNegativeButton("Exit the app", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        System.exit(0);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
