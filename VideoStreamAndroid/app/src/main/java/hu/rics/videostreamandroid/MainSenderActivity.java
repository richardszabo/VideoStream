package hu.rics.videostreamandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.rics.camera1util.MediaRecorderWrapper;

public class MainSenderActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "VideoStreamAndroid";
    MediaRecorderWrapper mediaRecorderWrapper;
    Button startButton;
    TextView ipTextView;
    StreamingCameraPreview streamingCameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sender);

        streamingCameraPreview = new StreamingCameraPreview(this);
        mediaRecorderWrapper = new MediaRecorderWrapper(this,R.id.camera_preview,streamingCameraPreview);
        ipTextView = (TextView) findViewById(R.id.ipTextView);
        try {
            ipTextView.setText("IP address:" + getIPAddress(true));
        } catch (SocketException e) {
            Log.e(MainSenderActivity.TAG,"Cannot get IP address:" + e.toString());
            e.printStackTrace();
        }
        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if( mediaRecorderWrapper != null ) {
            mediaRecorderWrapper.startPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if( mediaRecorderWrapper != null ) {
            mediaRecorderWrapper.stopPreview();
        }
    }

    @Override
    public void onClick(View v) {
        /*if( !(hasRights && mediaRecorderWrapper != null) ) {
            return;
        }
        if( mediaRecorderWrapper.isPreview() ) {
            mediaRecorderWrapper.stopPreview();
            startButton.setText("Start");
            finish();
        } else {
            mediaRecorderWrapper.startPreview();
            startButton.setText("Stop");
        }*/
    }

    /**
     * Get IP address from first non-localhost interface
     * taken from here: http://stackoverflow.com/a/13007325/21047
     * @param useIPv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) throws SocketException {
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : interfaces) {
            List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress()) {
                    String sAddr = addr.getHostAddress();
                    //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                    boolean isIPv4 = sAddr.indexOf(':')<0;

                    if (useIPv4) {
                        if (isIPv4)
                            return sAddr;
                    } else {
                        if (!isIPv4) {
                            int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                            return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                        }
                    }
                }
            }
        }
        return null;
    }
}