package hu.rics.videostreamandroid;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import hu.rics.videostreamandroid.MainActivity;

import static android.os.Build.HOST;

/**
 * Created by rics on 2017.01.31..
 */

public class Communicator extends AsyncTask<Void, Void, Boolean> {
    private static final String HOST = "192.168.0.106";
    private static final int PORT = 55555;
    Socket sock;
    BufferedOutputStream bos;
    private boolean isConnected = false;

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Log.e(MainActivity.TAG,"Trying to connect to " + HOST + ":" + PORT);
            sock = new Socket(HOST, PORT);
            Log.e(MainActivity.TAG,"Connected:" + sock);
            bos = new BufferedOutputStream(sock.getOutputStream());
            return true;
        } catch(IOException ioe) {
            Log.e(MainActivity.TAG,"Cannot connect:" + ioe.toString());
        }
        return false;
    }

    @Override
    public void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if( result ) {
            isConnected = true;
        }
        Log.i(MainActivity.TAG,"Connection state:" + isConnected);
    }

    boolean isConnected() {
        return isConnected;
    }

    void close() throws IOException {
        bos.close();
        sock.close();
    }

    BufferedOutputStream getBufferedOutputStream() {
        return bos;
    }
}