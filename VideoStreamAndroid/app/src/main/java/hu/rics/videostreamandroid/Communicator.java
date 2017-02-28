package hu.rics.videostreamandroid;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rics on 2017.01.31..
 */

public class Communicator extends AsyncTask<Void, Void, Void> {
    private static final int PORT = 55556;
    ServerSocket serverSocket;
    List<StreamingConnection> connections = new ArrayList<>();

    @Override
    protected Void doInBackground(Void... params) {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            Log.e(MainActivity.TAG,"Cannot initiate server:" + e.toString());
            e.printStackTrace();
        }
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                connections.add(new StreamingConnection(socket));
            } catch (IOException ioe) {
                Log.e(MainActivity.TAG, "Cannot connect new client:" + ioe.toString());
                break;
            }
        }
        return null;
    }

    void close() throws IOException {
        for (StreamingConnection connection:connections ) {
            connection.close();
        }
        serverSocket.close();
    }

    class StreamingConnection {
        Socket socket;
        BufferedOutputStream bos;
        DataOutputStream dos;
        boolean inited;

        StreamingConnection(Socket socket) {
            this.socket = socket;
            try {
                bos = new BufferedOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                Log.e(MainActivity.TAG,"Cannot connect:" + e.toString());
                e.printStackTrace();
            }
            dos = new DataOutputStream(bos);
        }

        BufferedOutputStream getBufferedOutputStream() { return bos; }

        DataOutputStream getDataOutputStream() {
            return dos;
        }

        void close() throws IOException {
            bos.close();
            socket.close();
        }
    }

    List<StreamingConnection> getConnections() {
        return connections;
    }
}