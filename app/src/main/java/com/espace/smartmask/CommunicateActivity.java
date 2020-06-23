package com.espace.smartmask;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.espace.smartmask.views.RoundProgressDisplayView;

public class CommunicateActivity extends AppCompatActivity {

    private TextView connectionText;
    private Button connectButton;

    private RoundProgressDisplayView mHeartRateView, mSkinTempView,
            mLowBloodPressureView, mHighBloodPressureView, mBloodOxygenView;

    private CommunicateViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup our activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);
        // Enable the back button in the action bar if possible
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup our ViewModel
        viewModel = ViewModelProviders.of(this).get(CommunicateViewModel.class);

        // This method return false if there is an error, so if it does, we should close.
        if (!viewModel.setupViewModel(getIntent().getStringExtra("device_name"), getIntent().getStringExtra("device_mac"))) {
            finish();
            return;
        }

        // Setup our Views
        connectionText = findViewById(R.id.communicate_connection_text);
        connectButton = findViewById(R.id.communicate_connect);

        mHeartRateView = findViewById(R.id.progressbar_heartrate_frag_env);
        mSkinTempView = findViewById(R.id.progressbar_skintemp_frag_env);
        mLowBloodPressureView = findViewById(R.id.progressbar_low_frag_env);
        mHighBloodPressureView = findViewById(R.id.progressbar_high_frag_env);
        mBloodOxygenView = findViewById(R.id.progressbar_oxygen_frag_env);

        // Start observing the data sent to us by the ViewModel
        viewModel.getConnectionStatus().observe(this, this::onConnectionStatus);
        viewModel.getDeviceName().observe(this, name -> setTitle(getString(R.string.device_name_format, name)));
        viewModel.getMessages().observe(this, message -> {
            if (TextUtils.isEmpty(message)) {
                message = getString(R.string.no_messages);
            }
            String[] multipleParams = message.split(",");

            String heartRate = multipleParams[0].replace("HR:", "");
            String oxygen = multipleParams[1].replace("SpO2:", "");
            String skinTemp = multipleParams[2].replace("Temp:", "").replace("C", "");

            String hlBlood = multipleParams[3].replace("H/L:", "").replace("mmHg\r\n", "");
            String[] hl = hlBlood.split("/");
            String high = hl[0];
            String low = hl[1];

            mHeartRateView.setValues(heartRate, (Integer.parseInt(heartRate) / 150.0f));
            mSkinTempView.setValues(skinTemp, (Float.parseFloat(skinTemp) / 40.0f));
            mLowBloodPressureView.setValues(low, (Integer.parseInt(low) / 90.0f));
            mHighBloodPressureView.setValues(high, (Integer.parseInt(high) / 140.0f));
            mBloodOxygenView.setValues(oxygen, (Integer.parseInt(oxygen) / 100.0f));
        });
    }

    // Called when the ViewModel updates us of our connectivity status
    private void onConnectionStatus(CommunicateViewModel.ConnectionStatus connectionStatus) {
        switch (connectionStatus) {
            case CONNECTED:
                connectionText.setText(R.string.status_connected);
                connectButton.setEnabled(true);
                connectButton.setText(R.string.disconnect);
                connectButton.setOnClickListener(v -> viewModel.disconnect());
                break;

            case CONNECTING:
                connectionText.setText(R.string.status_connecting);
                connectButton.setEnabled(false);
                connectButton.setText(R.string.connect);
                break;

            case DISCONNECTED:
                connectionText.setText(R.string.status_disconnected);
                connectButton.setEnabled(true);
                connectButton.setText(R.string.connect);
                connectButton.setOnClickListener(v -> viewModel.connect());
                break;
        }
    }

    // Called when a button in the action bar is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()) {
            case android.R.id.home:
                // If the back button was pressed, handle it the normal way
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Called when the user presses the back button
    @Override
    public void onBackPressed() {
        // Close the activity
        finish();
    }
}
