package com.example.progetto;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.view.ContextThemeWrapper;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.mapbox.android.core.permissions.PermissionsManager;


public class PositionDialog extends DialogFragment {
    // Constant used to identify the number of the permission asked

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme));
        builder.setMessage("Since this game is position-based, you must give location permission to play it.")
                .setTitle("Location permission")
                .setPositiveButton("Re-give permission", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.getPermissionsManager().requestLocationPermissions(getActivity());
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
