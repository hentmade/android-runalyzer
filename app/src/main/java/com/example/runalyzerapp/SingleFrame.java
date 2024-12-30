package com.example.runalyzerapp;

import static org.opencv.core.CvType.CV_32F;
import android.util.Log;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class SingleFrame {
    private Mat frame;
    private Mat previousFrame;
    private Mat croppedFrame = null;
    private boolean hasRunner;
    private double timecode;
    private RunnerInformation runnerInformation;

    public SingleFrame(Mat frame, Mat previousFrame, double timecode){
        this.frame = frame;
        this.previousFrame = previousFrame;
        this.timecode = timecode;
        hasRunner = false;
        runnerInformation = new RunnerInformation();
    }

    public Mat getFrame(){
        return frame;
    }

    public Mat getPreviousFrame(){
        return previousFrame;
    }

    public Mat getCroppedFrame(){
        return croppedFrame;
    }

    public boolean hasRunner(){
        return hasRunner;
    }

    public void setHasRunner(boolean hasRunner){
        this.hasRunner = hasRunner;
        if(!hasRunner){
            runnerInformation = new RunnerInformation();
            runnerInformation.setEmptyRunnerInformation();
        }
    }

    public RunnerInformation getRunnerInformation(){
        return runnerInformation;
    }

    public void setRunnerInformation(RunnerInformation runnerInformation){
        this.runnerInformation = runnerInformation;
    }

    public double getTimecode(){
        return timecode;
    }

    public String cropFrame(int width, int height, int yPosition){
        if(width == 0 || height == 0 || yPosition == 0){
            Log.d("Benni", "SingleFrame: cropFrame(): Width, Height or Y-Position is 0");
            return ("Width, Height or Y-Position is 0, frame can't be cropped.");
        }
        if(hasRunner){
            if(runnerInformation.getRunnerPosition().getX() == 0 && runnerInformation.getRunnerPosition().getY() == 0){
                Log.d("Benni", "SingleFrame: cropFrame(): No runner position available");
                return ("No runner position available, frame can't be cropped.");
            }
            if(frame.width() == 0|| frame.height() == 0){
                Log.d("Benni", "SingleFrame: cropFrame(): Frame size is 0");
                return ("No frame size available, frame can't be cropped.");
            }

            int xStartPos = runnerInformation.getRunnerPosition().getX() - (width/2);
            int yStartPos = yPosition - (height/2);
            int xShift = 0;
            int yShift = 0;
            boolean imgShiftingNeeded = false;

            if(xStartPos < 0){
                imgShiftingNeeded = true;
                xShift = -xStartPos;
                xStartPos = 0;
            }else if(xStartPos > (frame.width()-width)){
                imgShiftingNeeded = true;
                xShift = (frame.width()-width) - xStartPos;
                xStartPos = frame.width()-width;
            }

            if(yStartPos < 0){
                imgShiftingNeeded = true;
                yShift = -yStartPos;
                yStartPos = 0;
            }else if(yStartPos > (frame.height()-height)){
                imgShiftingNeeded = true;
                yShift = (frame.height()-height) - yStartPos;
                yStartPos = frame.height()-height;
            }

            Rect rectCrop = new Rect(xStartPos, yStartPos, width, height);
            croppedFrame = new Mat(frame,rectCrop);

            if(imgShiftingNeeded){
                Mat transformationMat = new Mat(2,3, CV_32F);
                transformationMat.put(0,0, 1); transformationMat.put(0,1, 0); transformationMat.put(0,2, xShift);
                transformationMat.put(1,0, 0); transformationMat.put(1,1, 1); transformationMat.put(1,2, yShift);
                Imgproc.warpAffine(croppedFrame, croppedFrame, transformationMat, new Size(width, height));
                transformationMat.release();
            }
        }
        return "success";
    }
}
