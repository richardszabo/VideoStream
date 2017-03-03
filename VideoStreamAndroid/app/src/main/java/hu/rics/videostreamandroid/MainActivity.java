package hu.rics.videostreamandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import hu.rics.videostreamandroid.receiver.MainReceiverActivity;
import hu.rics.videostreamandroid.sender.MainSenderActivity;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "VideoStreamAndroid";
    PermissionHandler permissionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionHandler = new PermissionHandler(this);
        permissionHandler.requestPermission();

        View.OnClickListener senderListener = new View.OnClickListener() {

            public void onClick(View v) {
                if( permissionHandler.hasRights() ) {
                    Intent intent = new Intent(MainActivity.this, MainSenderActivity.class);
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
    }
}
