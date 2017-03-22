package hu.rics.videostreamandroid.receiver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import hu.rics.videostreamandroid.R;


public class MainReceiverActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String DEFAULT_HOST = "192.168.0.101";

    ReceiverCommunicator receiverCommunicator;

    EditText ipEditText;
    Button connectButton;
    ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_receiver);

        ipEditText = (EditText) findViewById(R.id.ipEditText);
        ipEditText.setText(DEFAULT_HOST);

        imageView = (ImageView) findViewById(R.id.imageView);

        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if( receiverCommunicator != null ) {
            if( receiverCommunicator.isConnected() ) {
                receiverCommunicator.close();
            }
            receiverCommunicator = null;
            connectButton.setText("Connect");
        } else {
            receiverCommunicator = new ReceiverCommunicator(this, imageView);
            receiverCommunicator.execute(ipEditText.getText().toString());
            connectButton.setText("Disconnect");
        }

    }
}
