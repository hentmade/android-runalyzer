package com.example.runalyzerapp;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FinalVideo {
    private int width;
    private int height;
    private List<Mat> finalFrames;

    public FinalVideo (int width, int height){
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String setFinalFrames(List<VideoSequence> videoSequences){
        finalFrames = new ArrayList<>();
        if(videoSequences.isEmpty()){
            Log.d("Benni", "FinalVideo: setFinalFrames(): No video sequences");
            return ("No video sequences to set final frames.");
        }
        double relativeRunnerPosition;
        double switchingTimecode = 0;
        for(VideoSequence vs : videoSequences){
            if(vs.getSelectedSingleFrames().isEmpty()){
                Log.d("Benni", "FinalVideo: setFinalFrames(): No single frames in video sequence");
                return ("No single frames in video sequence, final frames can't be set.");
            }
            for(SingleFrame fr : vs.getSelectedSingleFrames()){
                if(fr.hasRunner() && fr.getTimecode() >= switchingTimecode){
                    if(fr.getRunnerInformation().getRunnerWidth() == 0){
                        Log.d("Benni", "FinalVideo: setFinalFrames(): Frame width is 0");
                        return ("Frame width is 0, final frame can't be selected.");
                    }
                    relativeRunnerPosition = (double) fr.getRunnerInformation().getRunnerPosition().getX() / fr.getFrame().width();
                    if(vs != videoSequences.get(videoSequences.size() - 1) && relativeRunnerPosition >= 0.875){
                        switchingTimecode = fr.getTimecode();
                        break;
                    }
                    else{
                        if(fr.getCroppedFrame() != null){
                            finalFrames.add(fr.getCroppedFrame());
                        }
                    }
                }
                //TODO: added if we just want to create a video without cropping or selection
                //finalFrames.add(fr.getFrame());
                //Log.d("Benni", "Frame added to finalFrames");
            }
        }
        return "success";
    }

    public String create(){
        if(finalFrames.isEmpty()){
            Log.d("Benni","FinalVideo: create(): frames empty, video can't be created");
            return("No frames to create final video.");
        }
        FileChannelWrapper out = null;
        try {
            File moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            String filePath = moviesDir.getAbsolutePath() + "/finalVideo_" + System.currentTimeMillis() + ".mp4";
            FilePathHolder.getInstance().setFilePath(filePath);
            //output file with data and time stamp
            out = NIOUtils.writableFileChannel(filePath);
            AndroidSequenceEncoder encoder = new AndroidSequenceEncoder(out, Rational.R(15, 1));
            for (Mat frame : finalFrames) {
                // Convert the Mat to a Bitmap
                Bitmap image = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(frame, image);
                // Encode the image
                encoder.encodeImage(image);
            }
            // Finalize the encoding, i.e. clear the buffers, write the header, etc.
            encoder.finish();
            Log.d("Benni", "Video written");
        } catch (IOException e) {
            Log.e("Benni", "FinalVideo: create(): " + Log.getStackTraceString(e));
            return ("Create video from frames failed. Details see log.");
        } finally {
            Log.d("Benni", "Closing File");
            NIOUtils.closeQuietly(out);
        }
        return "success";
    }
}
