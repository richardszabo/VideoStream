package hu.rics.videostreamandroid;

import android.Manifest;
import android.content.Intent;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hu.rics.camera1util.MediaRecorderWrapper;
import hu.rics.videostreamandroid.receiver.MainReceiverActivity;
import hu.rics.videostreamandroid.sender.MainSenderActivity;
import hu.rics.permissionhandler.PermissionHandler;

import static android.R.attr.width;
import static android.hardware.Camera.open;
import static hu.rics.videostreamandroid.R.id.sizeSpinner;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "VideoStreamAndroid";
    PermissionHandler permissionHandler;
    String permissions[] = {
        Manifest.permission.CAMERA,
        Manifest.permission.INTERNET
    };
    Spinner sizeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionHandler = new PermissionHandler(this);
        permissionHandler.requestPermission(permissions);
        sizeSpinner = (Spinner) findViewById(R.id.sizeSpinner);

        View.OnClickListener senderListener = new View.OnClickListener() {

            public void onClick(View v) {
                if( permissionHandler.hasRights() ) {
                    Log.i(TAG,"spinner selection:" + sizeSpinner.getSelectedItem());
                    Intent intent = new Intent(MainActivity.this, MainSenderActivity.class);
                    PreviewCameraSize size = (PreviewCameraSize)sizeSpinner.getSelectedItem();
                    intent.putExtra("previewsize",new int[]{ size.width,size.height});
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "No right to start", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };

        final Button senderButton = (Button) findViewById(R.id.senderButton);
        senderButton.setOnClickListener(senderListener);

        View.OnClickListener receiverListener = new View.OnClickListener() {

            public void onClick(View v) {
                if( permissionHandler.hasRights() ) {
                    Intent intent = new Intent(MainActivity.this, MainReceiverActivity.class);
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(MainActivity.this, "No right to start", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };

        final Button receiverButton = (Button) findViewById(R.id.receiverButton);
        receiverButton.setOnClickListener(receiverListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionHandler.onRequestPermissionsResult( requestCode, permissions, grantResults);

        if( permissionHandler.hasRights() ) {
            Camera camera = Camera.open(MediaRecorderWrapper.CAMERA_ID);
            List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
            camera.release();
            camera = null;
            List<PreviewCameraSize> psizes = new ArrayList<>();
            for (Camera.Size size : sizes) {
                psizes.add(new PreviewCameraSize(size.width, size.height));
            }
            ArrayAdapter<PreviewCameraSize> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, psizes);
            sizeSpinner.setAdapter(adapter);
        }
    }

    class PreviewCameraSize {
        int width;
        int height;
        PreviewCameraSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
        public String toString() {
            return width + "x" + height;
        }
    }
}
