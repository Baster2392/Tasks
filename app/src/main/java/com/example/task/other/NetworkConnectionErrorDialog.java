package com.example.task.other;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import java.io.IOException;

public class NetworkConnectionErrorDialog {
    public static void show (Context context, IOException e) {
        Activity activity = (Activity) context;
        AlertDialog.Builder dialog = new AlertDialog.Builder(context).setTitle("Network error")
                .setMessage(e.getMessage())
                .setCancelable(false)
                .setPositiveButton("OK", (dialogInterface, i) -> activity.finish());

        ((Activity) context).runOnUiThread(dialog::show);
    }
}
