package hu.rics.videostreamandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainReceiverActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String DEFAULT_HOST = "192.168.0.101";

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
        Toast toast = Toast.makeText(this, "Connect test", Toast.LENGTH_SHORT);
        toast.show();
    }
}
