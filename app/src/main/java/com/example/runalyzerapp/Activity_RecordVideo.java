package com.example.runalyzerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.Camera;
import androidx.camera.video.PendingRecording;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.core.content.ContextCompat;
import com.example.runalyzerapp.databinding.ActivityRecordVideoBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.core.Preview;
import androidx.camera.core.CameraSelector;
import android.util.Log;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.content.PermissionChecker;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.camera.camera2.interop.Camera2CameraControl;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.CaptureRequestOptions;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Range;

public class Activity_RecordVideo extends AppCompatActivity {
    private ActivityRecordVideoBinding viewBinding;

    private VideoCapture<Recorder> videoCapture = null;
    private Recording recording = null;

    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityRecordVideoBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions();
        }

        viewBinding.videoCaptureButton.setOnClickListener(v -> captureVideo());

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    // Implements VideoCapture use case, including start and stop capturing.
    private void captureVideo() {
        if (videoCapture == null) return;

        viewBinding.videoCaptureButton.setEnabled(false);

        if (recording != null) {
            // Stop the current recording session.
            recording.stop();
            recording = null;
            return;
        }

        // Create and start a new recording session
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name + ".mp4");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");
        }

        MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions
                .Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();

        PendingRecording pendingRecording = videoCapture.getOutput()
                .prepareRecording(this, mediaStoreOutputOptions);

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                PermissionChecker.PERMISSION_GRANTED) {
            pendingRecording = pendingRecording.withAudioEnabled();
        }


        recording = pendingRecording.start(ContextCompat.getMainExecutor(this), recordEvent -> {
            if (recordEvent instanceof VideoRecordEvent.Start) {
                viewBinding.videoCaptureButton.setText(getString(R.string.stop_capture));
                viewBinding.videoCaptureButton.setEnabled(true);
            } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                if (!((VideoRecordEvent.Finalize) recordEvent).hasError()) {
                    Uri videoUri = ((VideoRecordEvent.Finalize) recordEvent).getOutputResults().getOutputUri();
                    String msg = "Video capture succeeded: " + videoUri;
                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                } else {
                    recording.close();
                    recording = null;
                    Log.e(TAG, "Video capture ends with error: " +
                            ((VideoRecordEvent.Finalize) recordEvent).getError());
                }
                viewBinding.videoCaptureButton.setText(getString(R.string.start_capture));
                viewBinding.videoCaptureButton.setEnabled(true);
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @OptIn(markerClass = ExperimentalCamera2Interop.class)
            @Override
            public void run() {
                try {
                    // Used to bind the lifecycle of cameras to the lifecycle owner
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    // Preview
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(viewBinding.viewFinder.getSurfaceProvider());

                    Recorder recorder = new Recorder.Builder()
                            .setQualitySelector(QualitySelector.from(Quality.FHD))
                            .build();
                    videoCapture = VideoCapture.withOutput(recorder);

                    // Select back camera as a default
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                    // Unbind use cases before rebinding
                    cameraProvider.unbindAll();

                    // Bind use cases to camera
                    Camera camera = cameraProvider.bindToLifecycle(
                            Activity_RecordVideo.this, cameraSelector, preview, videoCapture);

                    Camera2CameraInfo camera2Info = Camera2CameraInfo.from(camera.getCameraInfo());

                    // Get supported frame rate ranges
                    Range<Integer>[] supportedFpsRanges = camera2Info.getCameraCharacteristic(
                            CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

                    //Log Range
                    for (Range<Integer> range : supportedFpsRanges) {
                        Log.d("Benni", "Supported FPS range: " + range);
                    }

                    //support 30 fps
                    Range<Integer> fpsRange = null;

                    for (Range<Integer> range : supportedFpsRanges) {
                        if (range.contains(30)) {
                            fpsRange = range;
                            break;
                        }
                    }

                    // Get Camera2CameraControl
                    Camera2CameraControl camera2Control = Camera2CameraControl.from(camera.getCameraControl());

                    // Set the frame rate range
                    CaptureRequestOptions requestOptions = new CaptureRequestOptions.Builder()
                            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange)
                            .build();

                    camera2Control.setCaptureRequestOptions(requestOptions);

                } catch (Exception exc) {
                    Log.e(TAG, "Use case binding failed", exc);
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
        if (ContextCompat.checkSelfPermission(
                        getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
    }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    private final ActivityResultLauncher<String[]> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    permissions -> {
        boolean permissionGranted = true;
        for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
        if (Arrays.asList(REQUIRED_PERMISSIONS).contains(entry.getKey()) && !entry.getValue()) {
            permissionGranted = false;
            break;
        }
    }
        if (!permissionGranted) {
            Toast.makeText(getBaseContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show();
        } else {
            startCamera();
        }
    });

    private static final String TAG = "CameraXApp";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String[] REQUIRED_PERMISSIONS =
    Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ?
    new String[]{
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    } :
    new String[]{
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    };
}