package com.example.runalyzerapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;

import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    final int REQUEST_CODE_CREATE_COMPILATION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); //using the entire width and height of the display by drawing behind the system bars
        setContentView(R.layout.activity_main);
        //some layout settings for handling system bars etc:
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Check if Opencv is loaded correctly, copied from OpenCV documentation
        if (OpenCVLoader.initLocal()) {
            Log.d("Benni", "OpenCV loaded successfully");
        } else {
            Log.d("Benni", "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        final Button button_recordVideo = (Button) findViewById(R.id.button_recordVideo);
        final Button button_createCompilation = (Button) findViewById(R.id.button_createCompilation);

        button_recordVideo.setOnClickListener(v -> {
            if (v == button_recordVideo) {
                Intent intent = new Intent(MainActivity.this, Activity_RecordVideo.class);
                MainActivity.this.startActivity(intent);
            }
        });

        button_createCompilation.setOnClickListener(v -> {
            if (v == button_createCompilation) {
                Intent intent = new Intent(MainActivity.this, Activity_CreateCompilation.class);
                MainActivity.this.startActivityForResult(intent, REQUEST_CODE_CREATE_COMPILATION);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check if permission is granted, will run regularly, also checks for different Android versions
        if(!Utils.isPermissionGranted(this))
        {
            new AlertDialog.Builder(this)
                        .setTitle("Permission Required")
                        .setMessage("Please grant permission to access external storage")
                        .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                takePermission();
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
        }
        else
        {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 101);
            }
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 101);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0)
        {
            if (requestCode==101)
            {
                boolean readExt = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (!readExt)
                {
                    takePermission();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_COMPILATION) {
            if(resultCode == Activity.RESULT_OK){
                String filePath = data.getStringExtra("filePath");
                Log.d("Benni", "File path: " + filePath);

                String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
                File directory = new File(directoryPath);
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        MediaScannerConnection.scanFile(this,
                                new String[] { file.getAbsolutePath() }, null, null);
                    }
                }

                // Create an Intent to view the video file and start the activity
                Uri uri = Uri.parse(filePath);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setDataAndType(uri, "video/mp4");
                startActivity(intent);
            }
        }
    }
}