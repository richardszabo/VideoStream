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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.rics.camera1util.MediaRecorderWrapper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "VideoStreamAndroid";
    MediaRecorderWrapper mediaRecorderWrapper;
    Button startButton;
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


}
