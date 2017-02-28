package hu.rics.videostreamandroid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.rics.camera1util.MediaRecorderWrapper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "VideoStreamAndroid";
    MediaRecorderWrapper mediaRecorderWrapper;
    Button startButton;
    TextView ipTextView;
    String defaultName = "VideoStreamAndroid";
    String ext = ".mp4";
    File sdcardLocation;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 123;
    boolean hasRights = false;
    StreamingCameraPreview streamingCameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if( requestPermission() ) {
            hasRights = true;
            streamingCameraPreview = new StreamingCameraPreview(this);
            mediaRecorderWrapper = new MediaRecorderWrapper(this,R.id.camera_preview,streamingCameraPreview);
        }
        ipTextView = (TextView) findViewById(R.id.ipTextView);
        try {
            ipTextView.setText("IP address:" + getIPAddress(true));
        } catch (SocketException e) {
            Log.e(MainActivity.TAG,"Cannot get IP address:" + e.toString());
            e.printStackTrace();
        }
        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if( hasRights && mediaRecorderWrapper != null ) {
            mediaRecorderWrapper.startPreview();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if( hasRights && mediaRecorderWrapper != null ) {
            mediaRecorderWrapper.stopPreview();
        }
    }

    @Override
    public void onClick(View v) {
        if( !(hasRights && mediaRecorderWrapper != null) ) {
            return;
        }
        if( mediaRecorderWrapper.isRecording() ) {
            mediaRecorderWrapper.stopRecording();
            startButton.setText("Start");
            finish();
        } else {
            sdcardLocation = Environment.getExternalStorageDirectory();
            File imageLocation = new File(sdcardLocation, defaultName);
            mediaRecorderWrapper.startRecording(imageLocation.getAbsolutePath() + ext);
            startButton.setText("Stop");
        }
    }

    boolean requestPermission() {
        // permission check (https://inthecheesefactory.com/blog/things-you-need-to-know-about-android-m-permission-developer-edition/en)
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.CAMERA)) {
            Log.i(TAG,"permissionsNeeded.add(\"Camera\");");
            permissionsNeeded.add("Camera");
        }
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.i(TAG,"permissionsNeeded.add(\"Storage\");");
            permissionsNeeded.add("External storage");
        }

        if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO)) {
            Log.i(TAG,"permissionsNeeded.add(\"Audio\");");
            permissionsNeeded.add("Audio");
        }
        if (!addPermission(permissionsList, Manifest.permission.INTERNET)) {
            Log.i(TAG,"permissionsNeeded.add(\"Internet\");");
            permissionsNeeded.add("Internet");
        }
        Log.i(TAG,"onCreate:" + permissionsList.size() + ":" + permissionsNeeded.size());
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return false;
            }
            ActivityCompat.requestPermissions(this,permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,permission)) {
                return false;
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.INTERNET, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++) {
                    Log.i(TAG,"i:" + i + ":" + permissions[i] + ":" + grantResults[i] );
                    perms.put(permissions[i], grantResults[i]);
                }
                // Check for ACCESS_FINE_LOCATION
                if( perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    hasRights = true;
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Some Permission is Denied", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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
