package io.github.battery233.roomOccupancy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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
    private FirebaseFirestore db;
    private BlinkyViewModel mViewModel;
    private int status;
    private String payloadString;
    private int walkedOutCount = 0;
    private int walkedInCount = 0;
    private boolean offlineData = true;

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

        db = FirebaseFirestore.getInstance();
        CollectionReference data = db.collection("Raw Data");

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

        // get real time data of people in or out
        mViewModel.getDistance1State().observe(this,
                triggered -> {
                    status = triggered.charAt(6) - 48;
                    if (status == 1 || status == 2) {
                        payloadString = status == 1 ? "IN" : "OUT";
                        if (status == 1) walkedInCount++;
                        else walkedOutCount++;
                        HashMap<String, String> distanceSensorData = new HashMap<>();
                        distanceSensorData.put(new SimpleDateFormat("HHmmss", Locale.UK).format(new Date()), payloadString);
                        data.document(new SimpleDateFormat("yyyyMMdd", Locale.UK).format(new Date()))
                                .set(distanceSensorData, SetOptions.merge())
                                .addOnSuccessListener(documentReference ->
                                        Toast.makeText(BlinkyActivity.this, "Someone walked " + payloadString, Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(BlinkyActivity.this, "Send movement record fail!", Toast.LENGTH_SHORT).show());
                        Log.d("Channel 1: ", "someone walks " + payloadString);
                    }
                    tofState_1.setText(walkedInCount + " in total");
                    tofState_2.setText(walkedOutCount + " in total");
                    Log.d("Channel 1 data: ", String.valueOf(status));
                });

        // get the data of people total in or out
        mViewModel.getDistance2State().observe(this,
                triggered -> {
                    if (offlineData) {
                        int in = 10 * (triggered.charAt(5) - 48) + triggered.charAt(6) - 48;
                        int out = 10 * (triggered.charAt(8) - 48) + triggered.charAt(9) - 48;
                        if (in != 0 || out != 0) {
                            Toast.makeText(BlinkyActivity.this, "Offline data found! in: " + in + " out: " + out+ " Board timer = "+getTimeFromHex(triggered,6), Toast.LENGTH_LONG).show();
                            offlineData = false;
                        }
                    }
                    Log.d("Channel 2 data: ", "--" + triggered + "--");
                });

        mViewModel.getPir1State().observe(this,
                triggered ->
                        pirState1.setText(triggered ? R.string.pir_triggered : R.string.pir_ready)
        );

        mViewModel.getPir2State().observe(this,
                triggered ->
                        pirState2.setText(triggered ? R.string.pir_triggered : R.string.pir_ready)
        );
    }

    @OnClick(R.id.action_clear_cache)
    public void onTryAgainClicked() {
        mViewModel.reconnect();
    }

    @SuppressLint("SetTextI18n")
    private void onConnectionStateChanged(final boolean connected) {
        if (!connected) {
            tofState_1.setText(R.string.button_unknown);
            tofState_2.setText(R.string.button_unknown);
            pirState1.setText(R.string.button_unknown);
            pirState2.setText(R.string.button_unknown);
        }
    }

    private int getTimeFromHex(String s, int index){
        //todo bug here
        char[] c = new char[4];
        c[0] = s.charAt(index+3);
        c[1] = s.charAt(index+4);
        c[2] = s.charAt(index+1);
        c[3] = s.charAt(index);
        Log.e("here!" + s,new String(c));
        return Integer.parseInt(new String(c),16);
    }
}
