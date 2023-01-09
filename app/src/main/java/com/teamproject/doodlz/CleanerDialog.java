package com.teamproject.doodlz;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class CleanerDialog
{
    public void showDialog(final MainActivity activity)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage("Are you sure you want to delete your amazing picture?").setTitle("Screen cleaning");
        builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.clearDrawing();
            }
        });
        builder.setNegativeButton("Back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        builder.show();
    }
}
