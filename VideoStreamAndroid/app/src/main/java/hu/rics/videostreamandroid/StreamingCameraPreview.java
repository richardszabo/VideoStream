package hu.rics.videostreamandroid;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import hu.rics.camera1util.CameraPreview;
import hu.rics.camera1util.LibraryInfo;
import hu.rics.camera1util.MediaRecorderWrapper;

/**
 * Created by rics on 2017.02.17..
 */
public class StreamingCameraPreview extends CameraPreview implements SurfaceHolder.Callback, Camera.PreviewCallback {

    Communicator communicator;
    Context context;
    int rotation;

    public StreamingCameraPreview(Context context) {
        super(context);
        this.context = context;
    }

    public void startSend() {
        communicator = new Communicator();
        communicator.execute();
    }

    public void stopSend() {
        try {
            if( communicator != null ) {
                communicator.close();
            }
            communicator = null;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(MainSenderActivity.TAG,"cannot close communicator");
        }
    }

    @Override
    public void startPreview() {
        super.startPreview();
        Log.d(MainSenderActivity.TAG,"StreamingCameraPreview.startSending");
        startSend();
    }

    @Override
    public void stopPreview() {
        super.stopPreview();
        Log.d(MainSenderActivity.TAG,"StreamingCameraPreview.stopSending");
        stopSend();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(MainSenderActivity.TAG,"StreamingCameraPreview.onPreviewFrame:" + data.length + ":");
        if( communicator != null ) {
            Iterator<Communicator.StreamingConnection> c = communicator.getConnections().iterator();
            while (c.hasNext() ) {
                Communicator.StreamingConnection connection = c.next();
                if( connection != null ) {
                    BufferedOutputStream bos = connection.getBufferedOutputStream();
                    DataOutputStream dos = connection.getDataOutputStream();
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try {
                        if( !connection.inited ) {
                            if( dos != null ) {
                                rotation = CameraPreview.getCameraDisplayOrientation(context, MediaRecorderWrapper.CAMERA_ID,camera);
                                assert rotation % 90 == 0; // assuming that getCameraDisplayOrientation return a multiplier of 90
                                Log.i(LibraryInfo.TAG,"width: " + size.width + " height: " + size.height + ":" +
                                        CameraPreview.getCameraDisplayOrientation(context, MediaRecorderWrapper.CAMERA_ID,camera));
                                if( rotation / 90 % 2 != 0 ) {
                                    int tmp = size.width;
                                    size.width = size.height;
                                    size.height = tmp;
                                }
                                dos.writeInt (size.width);
                                dos.writeInt (size.height);
                                dos.flush();
                                connection.inited = true;
                            }
                        } else {
                            if (bos != null) {
                                byte []rotated;
                                rotated = data;
                                for( int i = 0; i < rotation/90; ++i ) {
                                    rotated = rotateYUV420Degree90(rotated,size.width,size.height);
                                }
                                bos.write(rotated);
                                bos.flush();
                            }
                        }
                    } catch (IOException ioe) {
                        Log.d(MainSenderActivity.TAG, "Cannot send data:" + ioe.toString());
                        c.remove();
                    }
                }
            }
        }
    }

    // taken from here: http://stackoverflow.com/a/15775173/21047
    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
    {
        byte [] yuv = new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i = 0;
        for(int x = 0;x < imageWidth;x++) {
            for(int y = imageHeight-1;y >= 0;y--) {
                yuv[i] = data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth*imageHeight*3/2-1;
        for(int x = imageWidth-1;x > 0;x=x-2) {
            for(int y = 0;y < imageHeight/2;y++) {
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                i--;
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                i--;
            }
        }
        return yuv;
    }
}
