package hu.rics.videostreamandroid;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.BufferedOutputStream;
import java.io.IOException;

import hu.rics.camera1util.CameraPreview;

import static android.R.attr.data;
import static hu.rics.camera1util.LibraryInfo.TAG;

/**
 * Created by rics on 2017.02.17..
 */
public class StreamingCameraPreview extends CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback {

    Communicator communicator;
    private int[] pixels;
    private static final int WIDTH = 320;
    private static final int HEIGHT = 240;

    public StreamingCameraPreview(Context context) {
        super(context);
        pixels = new int[WIDTH * HEIGHT];
    }

    public void startSend() {
        communicator = new Communicator();
        communicator.execute();
    }

    public void stopSend() {
        try {
            communicator.close();
            communicator = null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(MainActivity.TAG,"cannot close communicator");
        }
    }

    @Override
    public void startRecording() {
        Log.d(MainActivity.TAG,"StreamingCameraPreview.startRecording");
        startSend();
    }

    @Override
    public void stopRecording() {
        Log.d(MainActivity.TAG,"StreamingCameraPreview.stopRecording");
        stopSend();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(MainActivity.TAG,"StreamingCameraPreview.onPreviewFrame:" + data.length + ":");
        if( communicator != null ) {
            try {
                BufferedOutputStream bos = communicator.getBufferedOutputStream();
                if (bos != null) {
                    bos.write(data);
                    bos.flush();
                }
            } catch (IOException ioe) {
                Log.e(MainActivity.TAG, "Cannot send data:" + ioe.toString());
            }
        }
    }
}
