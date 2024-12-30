package com.example.runalyzerapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Activity_CreateCompilation extends AppCompatActivity {
    private int[] millisCreationTime;
    private List<Uri> inputVideoUris;
    private List<Uri> videoPickerUris;
    private WorkerUsingThread runalyzerThread;
    private TextView statusText;
    private VideoView videoView1;
    private VideoView videoView2;

    private EditText editTextVideo1;
    private EditText editTextVideo2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_compilation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        videoView1 = findViewById(R.id.videoView1);
        editTextVideo1 = findViewById(R.id.editTextVideo1);
        videoView2 = findViewById(R.id.videoView2);
        editTextVideo2 = findViewById(R.id.editTextVideo2);

        Button button1 = findViewById(R.id.buttonEditText1);
        Button button2 = findViewById(R.id.buttonEditText2);
        Button button3 = findViewById(R.id.buttonTextView);

        button1.setEnabled(false);
        button2.setEnabled(false);
        button3.setEnabled(false);

        Spinner spinner1 = findViewById(R.id.number_spinner1);
        Spinner spinner2 = findViewById(R.id.number_spinner2);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.numbers_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // receive milliscreationtime as int from editText
                try {
                    //String timeInput = editTextVideo1.getText().toString();
                    String timeInput = "09:52:26.381"; //yellow

                    // Split the input string into hours, minutes, seconds, and milliseconds
                    String[] timeParts = timeInput.split("[:.]");
                    int hours = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);
                    int seconds = Integer.parseInt(timeParts[2]);
                    int milliseconds = Integer.parseInt(timeParts[3]);

                    // Convert the time to milliseconds
                    int totalMilliseconds = (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000) + milliseconds;

                    if (millisCreationTime != null && inputVideoUris != null) {
                        //int video_pos1 = Integer.parseInt((String) spinner1.getSelectedItem()) - 1;
                        int video_pos1 = 0; //yellow
                        inputVideoUris.set(video_pos1, videoPickerUris.get(0));
                        millisCreationTime[video_pos1] = totalMilliseconds;
                        Log.d("Benni", "millisCreationTime[" + video_pos1 + "] = " + millisCreationTime[video_pos1] + " video_pos1 = " + video_pos1);
                        boolean allSet;
                        allSet = Arrays.stream(millisCreationTime).noneMatch(time -> time == -1) &&
                                inputVideoUris.stream().noneMatch(uri -> uri == null);
                        button3.setEnabled(allSet);
                    }
                } catch (NumberFormatException e) {
                    // Display an error message to the user
                    Toast.makeText(Activity_CreateCompilation.this, "Invalid input. Please enter a valid time in the format HH:MM:SS.mmm", Toast.LENGTH_LONG).show();
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //String timeInput = editTextVideo2.getText().toString();
                    String timeInput = "09:52:28.176"; //yellow

                    // Split the input string into hours, minutes, seconds, and milliseconds
                    String[] timeParts = timeInput.split("[:.]");
                    int hours = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);
                    int seconds = Integer.parseInt(timeParts[2]);
                    int milliseconds = Integer.parseInt(timeParts[3]);

                    // Convert the time to milliseconds
                    int totalMilliseconds = (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000) + milliseconds;

                    if (millisCreationTime != null && inputVideoUris != null) {
                        //int video_pos2 = Integer.parseInt((String) spinner2.getSelectedItem()) - 1;
                        int video_pos2 = 1; //yellow
                        inputVideoUris.set(video_pos2, videoPickerUris.get(1));
                        millisCreationTime[video_pos2] = totalMilliseconds;
                        Log.d("Benni", "millisCreationTime[" + video_pos2 + "] = " + millisCreationTime[video_pos2] + " video_pos2 = " + video_pos2);
                        boolean allSet;
                        allSet = Arrays.stream(millisCreationTime).noneMatch(time -> time == -1) &&
                                inputVideoUris.stream().noneMatch(uri -> uri == null);
                        button3.setEnabled(allSet);
                    }

                } catch (NumberFormatException e) {
                    // Display an error message to the user
                    Toast.makeText(Activity_CreateCompilation.this, "Invalid input. Please enter a valid time in the format HH:MM:SS.mmm", Toast.LENGTH_LONG).show();
                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Benni", "Starting runalyzerThread");
                statusText = (TextView) findViewById(R.id.textView_status);
                statusText.setText("Starting Runalyzer...");
                runalyzerThread = new WorkerUsingThread(Activity_CreateCompilation.this);
                runalyzerThread.start();
            }
        });


        // Registers a photo picker activity launcher in multi-select mode.
        ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
                registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(5), uris -> {
                    // Callback is invoked after the user selects media items or closes the photo picker.
                    if (!uris.isEmpty()) {
                        Log.d("PhotoPicker", "Number of items selected: " + uris.size());
                        videoPickerUris = uris;
                        inputVideoUris = new ArrayList<>(Collections.nCopies(videoPickerUris.size(), null));

                        //TODO: get millisCreationTime in another way
                        millisCreationTime = new int[uris.size()];
                        millisCreationTime[0] = -1;
                        millisCreationTime[1] = -1;

                        button1.setEnabled(true);
                        button2.setEnabled(true);


                        videoView1.setVideoURI(uris.get(0));
                        videoView1.start();

                        videoView2.setVideoURI(uris.get(1));
                        videoView2.start();

                        //millisCreationTime[0] = 0;      //vid01
                        //millisCreationTime[1] = 4087;   //vid02

                    } else {
                        Log.d("PhotoPicker", "No media selected");
                    }
                });

        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly.INSTANCE)
                .build());
    }
    //on resumne
    @Override
    protected void onResume() {
        super.onResume();
        if(videoPickerUris != null && videoView1 != null){
            videoView1.start();
        }
        if (videoPickerUris != null && videoView2 != null){
            videoView2.start();
        }
    }

    class WorkerUsingThread implements Runnable {
        private volatile boolean running = false;
        private Thread thread;
        private Activity activity;

        WorkerUsingThread(Activity activity) {
            this.activity = activity;
        }

        private void print(final String s){
            runOnUiThread(new Runnable() {
                @Override
                public void run(){
                    statusText.setText(s);
                }
            });
        }

        @Override
        public void run() {
            String retval;
            Runalyzer runalyzer = new Runalyzer(inputVideoUris, millisCreationTime);
            if(running){
                Log.d("Benni", "millisCreationTime[0] = " + millisCreationTime[0]);
                Log.d("Benni", "millisCreationTime[1] = " + millisCreationTime[1]);
                print("Detecting runner-information...");
                retval = runalyzer.detectRunnerInformation(Activity_CreateCompilation.this);
                if(!Objects.equals(retval, "success")){
                    print("Error: " + retval);
                    runalyzerThread.stop();
                }

                print("Detecting maximum runner-width and -height...");
                retval = runalyzer.detectRunnerDimensions();
                if(!Objects.equals(retval, "success")){
                    print("Error: " + retval);
                    runalyzerThread.stop();
                }

                print("Cropping all single frames...");
                retval = runalyzer.cropSingleFrames();
                if(!Objects.equals(retval, "success")){
                    print("Error: " + retval);
                    runalyzerThread.stop();
                }

                print("Creating final video...");
                retval = runalyzer.createFinalVideo();
                if(!Objects.equals(retval, "success")){
                    print("Error: " + retval);
                    runalyzerThread.stop();
                }

                print("Finished!");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                runalyzerThread.stop();
            }
        }

        void start() {
            running = true;
            thread = new Thread(this);
            thread.start();
        }

        void stop(){
            if(!running){
                //TODO
            }else{
                running = false;
                while(true){
                    try {
                        Log.d("Benni", "Before Join");
                        thread.join();
                        Log.d("Benni", "Thread stopped");
                        break;
                    } catch (Exception e) {
                        Log.d("Benni", "Error stopping thread");
                    }
                }
                String filePath = FilePathHolder.getInstance().getFilePath();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("filePath", filePath);
                activity.setResult(Activity.RESULT_OK, resultIntent);
                activity.finish();
            }
        }
    }


}