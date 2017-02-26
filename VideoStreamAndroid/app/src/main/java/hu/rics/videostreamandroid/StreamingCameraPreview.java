package hu.rics.videostreamandroid;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import hu.rics.camera1util.CameraPreview;
import hu.rics.camera1util.LibraryInfo;

/**
 * Created by rics on 2017.02.17..
 */
public class StreamingCameraPreview extends CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback {

    Communicator communicator;
    boolean sizeSent;

    public StreamingCameraPreview(Context context) {
        super(context);
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
                DataOutputStream dos = communicator.getDataOutputStream();
                if( !sizeSent ) {
                    if( dos != null ) {
                        Camera.Size size = camera.getParameters().getPreviewSize();
                        Log.i(LibraryInfo.TAG,"width: " + size.width + " height: " + size.height);
                        dos.writeInt (size.width);
                        dos.writeInt (size.height);
                        dos.flush();
                        sizeSent = true;
                    }
                } else {
                    if (bos != null) {
                        bos.write(data);
                        bos.flush();
                    }
                }
            } catch (IOException ioe) {
                Log.e(MainActivity.TAG, "Cannot send data:" + ioe.toString());
            }
        }
    }
}
