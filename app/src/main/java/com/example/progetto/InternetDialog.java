package com.example.progetto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

public class InternetDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppTheme));
        builder.setTitle("Internet connection")
                .setMessage("The app couldn't download data from the server because there is no active internet connection");
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
