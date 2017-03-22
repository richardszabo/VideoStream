package hu.rics.videostreamandroid.receiver;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import hu.rics.videostreamandroid.MainActivity;


public class ReceiverCommunicator extends AsyncTask<String, Bitmap, Void> {
    private String host;
    private static final int PORT = 55556;
    private Socket socket;
    private BufferedInputStream bis;
    private DataInputStream dis;
    private boolean isConnected;
    private int width;
    private int height;
    private int numPixels;
    private int bufferSize;
    private byte[] buffer;
    private int[] rgbBuffer;
    private ReceiverActivity receiverActivity;
    private ImageView imageView;

    public ReceiverCommunicator(ReceiverActivity receiverActivity, ImageView imageView) {
        this.receiverActivity = receiverActivity;
        this.imageView = imageView;
    }

    @Override
    protected Void doInBackground(String... params) {
        host = params[0];
        if( connect(host) && getImageSize() ) {
            receiveImages();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate (Bitmap... values) {
        imageView.setImageBitmap(values[0]);
    }

    private boolean connect(String host) {
        try {
            socket = new Socket(host,PORT);
            bis = new BufferedInputStream(socket.getInputStream());
            dis = new DataInputStream(bis);
            isConnected = true;
            Log.i(MainActivity.TAG,"Connected");
            return true;
        } catch (Exception e) {
            Log.e(MainActivity.TAG,"Failed to connect: " + e);
            return false;
        }
    }

    private boolean getImageSize() {
        if( dis != null ) {
            try {
                width = dis.readInt();
                height = dis.readInt();
                Log.i(MainActivity.TAG,"width: " + width + " height: " + height);
                numPixels = width * height;
                bufferSize = numPixels * 3 / 2;
                buffer = new byte[bufferSize];
                rgbBuffer = new int[numPixels];
                return true;
            } catch (IOException ex) {
                Log.e(MainActivity.TAG,"Failed to get image size: " + ex);
            }
        }
        return false;
    }

    private void receiveImages() {
        while(isConnected) {
            synchronized (this) {
                try {
                    int offset = 0;
                    while (offset < bufferSize) {
                        offset += bis.read(buffer, offset, bufferSize - offset);
                    }
                    long start = System.currentTimeMillis();
                    decodeYUV420SP(rgbBuffer,buffer,width,height);
                    //decodeYUV420SPRenderScript(context,rgbBuffer,buffer,width,height);
                    Log.i(MainActivity.TAG,"decode time:" + (System.currentTimeMillis()-start));
                    Bitmap bitmap = Bitmap.createBitmap(rgbBuffer,width,height,Bitmap.Config.RGB_565);
                    publishProgress(bitmap);
                } catch (Exception e) {
                    Log.d(MainActivity.TAG,"Connection terminated.");
                    ((Activity)receiverActivity).runOnUiThread(new Runnable() {
                        public void run() {
                            imageView.setImageBitmap(null);
                            receiverActivity.onClick(null);
                        }
                    });
                    break;
                }
            }
        }
    }

    // convertYUVtoARGB is taken from here:
    // https://en.wikipedia.org/wiki/YUV#Y.E2.80.B2UV420sp_.28NV21.29_to_RGB_conversion_.28Android.29
    private static int convertYUVtoARGB(int y, int u, int v) {
        // converting to unsigned int
        y = 0xFF & y;
        u = 0xFF & u;
        v = 0xFF & v;
        int r = (int)(y + (1.370705 * (v-128)));
        int g = (int)(y - (0.698001 * (v-128)) - (0.337633 * (u-128)));
        int b = (int)(y + (1.732446 * (u-128)));
        r = r>255? 255 : r<0 ? 0 : r;
        g = g>255? 255 : g<0 ? 0 : g;
        b = b>255? 255 : b<0 ? 0 : b;
        return 0xff000000 | (r<<16) | (g<<8) | b;
    }

    // based on
    // https://en.wikipedia.org/wiki/YUV#Y.E2.80.B2UV420p_.28and_Y.E2.80.B2V12_or_YV12.29_to_RGB888_conversion
    // YUV420SP aka NV21 pixel layout per channel is the following (for a 16 pixel image):
    // 0123456789ABCDEF01234567
    // YYYYYYYYYYYYYYYYVUVUVUVU
    private static void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
        for (int i = 0; i < height; ++i ) {
            for( int j = 0; j < width; ++j) {
                int y = yuv420sp[i * width + j];
                int v = yuv420sp[2*((i / 2) * (width/2) + (j/2)) + width*height];
                int u = yuv420sp[2*((i / 2) * (width/2) + (j/2))+1 + width*height];
                rgb[i * width + j] = convertYUVtoARGB(y, u, v);
            }
        }
    }

    /*private static void decodeYUV420SPRenderScript(Context context, int[] rgb, byte[] yuv420sp, int width, int height) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuv420sp.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        in.copyFrom(yuv420sp);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        out.copyTo(rgb);
    }*/

    void close() {
        try {
            if (bis != null) bis.close();
            if (socket != null) socket.close();
            isConnected = false;
            Log.i(MainActivity.TAG,"Disconnected");
        } catch (Exception e1) {
            Log.e(MainActivity.TAG,"Exception closing window: " + e1);
        }
    }

    boolean isConnected() {
        return isConnected;
    }
}