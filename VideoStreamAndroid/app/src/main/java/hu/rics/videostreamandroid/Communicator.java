package hu.rics.videostreamandroid;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by rics on 2017.01.31..
 */

public class Communicator extends AsyncTask<Void, Void, Boolean> {
    private static final int PORT = 55556;
    ServerSocket serverSocket;
    Socket socket;
    BufferedOutputStream bos;
    DataOutputStream dos;
    private boolean isConnected = false;

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            Log.e(MainActivity.TAG,"Cannot initiate server:" + e.toString());
            e.printStackTrace();
        }
        try {
            socket = serverSocket.accept();
            bos = new BufferedOutputStream(socket.getOutputStream());
            dos = new DataOutputStream(bos);
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
        socket.close();
        serverSocket.close();
    }

    BufferedOutputStream getBufferedOutputStream() {
        return bos;
    }

    DataOutputStream getDataOutputStream() {
        return dos;
    }

}