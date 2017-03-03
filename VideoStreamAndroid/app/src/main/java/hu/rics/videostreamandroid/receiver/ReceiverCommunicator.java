package hu.rics.videostreamandroid.receiver;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import hu.rics.videostreamandroid.MainActivity;
import hu.rics.videostreamandroid.sender.MainSenderActivity;

import static hu.rics.videostreamandroid.R.id.connectButton;

public class ReceiverCommunicator extends AsyncTask<String, Void, Void> {
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

    @Override
    protected Void doInBackground(String... params) {
        host = params[0];
        if( connect(host) && getImageSize() ) {
            /*updateGUI();
            receiveImages();*/
        }
        return null;
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
                return true;
            } catch (IOException ex) {
                Log.e(MainActivity.TAG,"Failed to get image size: " + ex);
            }
        }
        return false;
    }

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