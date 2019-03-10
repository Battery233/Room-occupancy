package io.github.battery233.roomOccupancy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.battery233.roomOccupancy.adapter.DiscoveredBluetoothDevice;
import io.github.battery233.roomOccupancy.viewmodels.BlinkyViewModel;

@SuppressWarnings("ConstantConditions")
public class BlinkyActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    @BindView(R.id.tof_state_1)
    TextView tofState_1;
    @BindView(R.id.tof_state_2)
    TextView tofState_2;
    @BindView(R.id.pir_state_1)
    TextView pirState1;
    @BindView(R.id.pir_state_2)
    TextView pirState2;
    @BindView(R.id.getHighestRssi)
    TextView getHighestRssi;
    private BlinkyViewModel mViewModel;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blinky);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        final DiscoveredBluetoothDevice device = intent.getParcelableExtra(EXTRA_DEVICE);
        final String deviceName = device.getName();
        final String deviceAddress = device.getAddress();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(deviceName);
        getSupportActionBar().setSubtitle(deviceAddress);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Configure the view model
        mViewModel = ViewModelProviders.of(this).get(BlinkyViewModel.class);
        mViewModel.connect(device);

        // Set up views
        final LinearLayout progressContainer = findViewById(R.id.progress_container);
        final TextView connectionState = findViewById(R.id.connection_state);
        final View content = findViewById(R.id.device_container);
        final View notSupported = findViewById(R.id.not_supported);

        mViewModel.isDeviceReady().observe(this, deviceReady -> {
            progressContainer.setVisibility(View.GONE);
            content.setVisibility(View.VISIBLE);
        });

        mViewModel.getConnectionState().observe(this, text -> {
            if (text != null) {
                progressContainer.setVisibility(View.VISIBLE);
                notSupported.setVisibility(View.GONE);
                connectionState.setText(text);
            }
        });
        mViewModel.isConnected().observe(this, this::onConnectionStateChanged);
        mViewModel.isSupported().observe(this, supported -> {
            if (!supported) {
                progressContainer.setVisibility(View.GONE);
                notSupported.setVisibility(View.VISIBLE);
            }
        });

        getHighestRssi.setText("Bluetooth maximum signal strengeth (dBm): " + device.getHighestRssi());

        mViewModel.getDistance1State().observe(this,
                pressed -> tofState_1.setText(pressed ?
                        R.string.TOF_triggered : R.string.TOF_ready));

        mViewModel.getDistance2State().observe(this,
                pressed -> tofState_2.setText(pressed ?
                        R.string.TOF_triggered : R.string.TOF_ready));

        mViewModel.getPir1State().observe(this,
                pressed -> {
                    pirState1.setText(pressed ? R.string.pir_triggered : R.string.pir_ready);

                });

        mViewModel.getPir2State().observe(this,
                pressed -> {
                    pirState2.setText(pressed ? R.string.pir_triggered : R.string.pir_ready);

                });
    }

    @OnClick(R.id.action_clear_cache)
    public void onTryAgainClicked() {
        mViewModel.reconnect();
    }

    private void onConnectionStateChanged(final boolean connected) {
        if (!connected) {
            tofState_1.setText(R.string.button_unknown);
            tofState_2.setText(R.string.button_unknown);
            pirState1.setText(R.string.button_unknown);
            pirState2.setText(R.string.button_unknown);
        }
    }
}
