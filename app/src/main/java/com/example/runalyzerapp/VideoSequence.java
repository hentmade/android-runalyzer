package com.example.runalyzerapp;

import static org.opencv.videoio.Videoio.CAP_PROP_FPS;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideoSequence {
    private String videoFilePath;
    private Uri videoUri;
    private int relativeCreationTime;
    private int videoDurationInMillis;
    private List<SingleFrame> selectedSingleFrames = new ArrayList<>();
    private RunnerDetection runnerDetector = new BackgroundSubtraction();

    public VideoSequence(Context context, Uri videoUri, int relativeCreationTime) {
        this.videoUri = videoUri;
        this.videoFilePath = getRealPathFromURI(context, videoUri);
        this.relativeCreationTime = relativeCreationTime;
        this.videoDurationInMillis = getVideoDurationFromURI(context, videoUri);
    }

    public List<SingleFrame> getSelectedSingleFrames(){
        return selectedSingleFrames;
    }

    public int getVideoDurationInMillis(){
        return videoDurationInMillis;
    }

    public String separateToFrames(Context context, int totalMillisAllVideos){
        if(Objects.equals(videoFilePath, "")){
            Log.d("Benni","VideoSequence: separateToFrames(): Empty videoFilePath");
            return("Empty video file path. Probably no Uri detected.");
        }
        VideoCapture videoCapture = null;
        try {
            videoCapture = new VideoCapture(videoFilePath);
        } catch (Exception e) {
            Log.e("Benni", Log.getStackTraceString(e));
            return Log.getStackTraceString(e);
        }

        if (!videoCapture.isOpened()) {
            Log.d("Benni", "VideoSequence: separateToFrames(): Failed to open video file: " + videoFilePath);
            return ("Failed to open video file: " + videoFilePath);
        }

        Mat frame = new Mat();
        double timecode = relativeCreationTime;
        double fps = videoCapture.get(CAP_PROP_FPS);
        if(fps == 0){
            Log.d("Benni","VideoSequence: separateToFrames(): fps can't be read from input video");
            return("fps can't be read from input video");
        }
        double millisBetweenFrames = (1000.0/fps);

        //calculate most efficient Mat size:
        long totalFrameCountAllVideos = (int)(totalMillisAllVideos/1000.0 * fps) + 1;
        long sizeOfOneMatInByte = 1920*1080*3; //assume CV_8UC3-format (8 bit per channel, 3 channels(RGB))
        long totalMatSize = sizeOfOneMatInByte * totalFrameCountAllVideos;
        double reductionFactor = 1800000000.0 / totalMatSize; //use 1.8 GB memory for Mats

        Mat previousFrame = null;

        boolean onceHasRunner = false;
        int framesWithoutRunner = 0;
        while (videoCapture.read(frame)) {
            // Save the current frame as a Mat object in the list
            Mat currentFrame = new Mat();

            frame.copyTo(currentFrame); //TODO: maybe remove
            int newMatWidth = (int)(Math.sqrt(reductionFactor) * currentFrame.width());
            int newMatHeight = (int)(newMatWidth * ((double)currentFrame.height()/(double)currentFrame.width()));
            Imgproc.resize(currentFrame, currentFrame, new Size(newMatWidth,newMatHeight));

            SingleFrame singleFrame = new SingleFrame(currentFrame, previousFrame, timecode);
            singleFrame.setRunnerInformation(runnerDetector.detectRunnerInformation(context, singleFrame));

            if(singleFrame.hasRunner()){
                onceHasRunner = true;
                selectedSingleFrames.add(singleFrame);
            } else if(onceHasRunner){
                framesWithoutRunner++;
                if(framesWithoutRunner == 5){ //stop capturing frames when 5 frames have no runner if once a runner was detected (onceHasRunner)
                    break;
                }
            }

            timecode += millisBetweenFrames;
            previousFrame = currentFrame;
        }

        if(selectedSingleFrames.isEmpty()){
            Log.d("Benni","VideoSequence: separateToFrames(): frameList still empty, videoCapture failed");
            return("No single frame could be captured from input Video.");
        }

        frame.release();
        videoCapture.release();

        return ("success");
    }

    public int getMaxRunnerWidth(){
        int maxRunnerWidth = 0;
        if(selectedSingleFrames.isEmpty()){
            Log.d("Benni", "VideoSequence: getMaxRunnerWidth(): No single frames");
            return -1;
        }
        for(SingleFrame frame : selectedSingleFrames){
            if(frame.getRunnerInformation().getRunnerWidth() > maxRunnerWidth){
                maxRunnerWidth = frame.getRunnerInformation().getRunnerWidth();
            }
        }
        return maxRunnerWidth;
    }

    public int getMaxRunnerHeight(){
        int maxRunnerHeight = 0;
        if(selectedSingleFrames.isEmpty()){
            Log.d("Benni", "VideoSequence: getMaxRunnerHeight(): No single frames");
            return -1;
        }
        for(SingleFrame frame : selectedSingleFrames){
            if(frame.getRunnerInformation().getRunnerHeight() > maxRunnerHeight){
                maxRunnerHeight = frame.getRunnerInformation().getRunnerHeight();
            }
        }
        return maxRunnerHeight;
    }

    public int getLowestYPosition(){
        if(selectedSingleFrames.isEmpty()){
            Log.d("Benni", "VideoSequence: getMiddleYPosition(): No single frames.");
            return -1;
        }

        int actYPos;
        int lowestYPosition = selectedSingleFrames.get(0).getFrame().height();
        if(lowestYPosition == 0){
            Log.d("Benni", "VideoSequence: getMiddleYPosition(): Frame-Height of 0 detected, error.");
            return -1;
        }

        for(SingleFrame frame : selectedSingleFrames){
            actYPos = frame.getRunnerInformation().getRunnerPosition().getY();
            if(actYPos == 0){
                Log.d("Benni", "VideoSequence: getMiddleYPosition(): SingleFrame with Runner-Y-Position = 0 detected.");
                return -1;
            }

            if(actYPos < lowestYPosition){
                lowestYPosition = actYPos;
            }
        }

        return lowestYPosition;
    }

    public int getHighestYPosition(){
        if(selectedSingleFrames.isEmpty()){
            Log.d("Benni", "VideoSequence: getMiddleYPosition(): No single frames.");
            return -1;
        }

        int actYPos;
        int highestYPosition = 0;

        for(SingleFrame frame : selectedSingleFrames){
            actYPos = frame.getRunnerInformation().getRunnerPosition().getY();
            if(actYPos == 0){
                Log.d("Benni", "VideoSequence: getMiddleYPosition(): SingleFrame with Runner-Y-Position = 0 detected.");
                return -1;
            }
            if(actYPos > highestYPosition){
                highestYPosition = actYPos;
            }
        }

        return highestYPosition;
    }

    public String cropFrames(int width, int height, int yPosition){
        String retval;
        if(width == 0 || height == 0){
            Log.d("Benni", "VideoSequence: cropFrames(): Width or Height is 0");
            return ("Width or Height is 0, frames can't be cropped.");
        }
        if(selectedSingleFrames.isEmpty()){
            Log.d("Benni", "VideoSequence: cropFrames(): No single frames");
            return ("No single frames, frames can't be cropped.");
        }
        for(SingleFrame frame : selectedSingleFrames){
            if(frame.hasRunner()){
                retval = frame.cropFrame(width, height, yPosition);
                if(!Objects.equals(retval, "success")){
                    return retval;
                }
            }
        }
        return "success";
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        if(contentUri == null){
            Log.d("Benni","VideoSequence: getRealPathFromURI(): contentUri == null");
            return ("");
        }
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String result = cursor.getString(column_index);
            cursor.close();
            return result;
        }
    }

    public int getVideoDurationFromURI(Context context, Uri contentUri) {
        if(contentUri == null){
            Log.d("Benni","VideoSequence: getVideoDurationFromURI(): contentUri == null");
            return -1;
        }
        String[] proj = { MediaStore.Video.Media.DURATION };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return -1;
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int videoDurationMillis = Integer.parseInt(cursor.getString(column_index));
            cursor.close();
            return videoDurationMillis;
        }
    }
}
