package com.example.runalyzerapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.Manifest;

import androidx.core.content.ContextCompat;

public class Utils {
    public static boolean isPermissionGranted(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) //Android 11 and above
        {
            //Check if the app has permission to access (read/write) external storage
            return Environment.isExternalStorageManager();
        }
        else //Android 10 and below
        {
            int readExtStorage = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeExtStorage = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return readExtStorage == PackageManager.PERMISSION_GRANTED &&
                    writeExtStorage == PackageManager.PERMISSION_GRANTED;
        }
    }

}
