package hu.rics.videostreamandroid.receiver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import hu.rics.videostreamandroid.R;
import hu.rics.videostreamandroid.sender.Communicator;


public class MainReceiverActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String DEFAULT_HOST = "192.168.0.101";

    ReceiverCommunicator receiverCommunicator;

    EditText ipEditText;
    Button connectButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_receiver);

        ipEditText = (EditText) findViewById(R.id.ipEditText);
        ipEditText.setText(DEFAULT_HOST);

        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if( receiverCommunicator != null && receiverCommunicator.isConnected() ) {
            receiverCommunicator.close();
            connectButton.setText("Connect");
        } else {
            receiverCommunicator = new ReceiverCommunicator();
            receiverCommunicator.execute(ipEditText.getText().toString());
            connectButton.setText("Disconnect");
        }

    }
}
