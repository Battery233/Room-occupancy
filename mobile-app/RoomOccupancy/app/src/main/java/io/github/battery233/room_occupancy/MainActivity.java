package io.github.battery233.room_occupancy;

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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.battery233.room_occupancy.adapter.DiscoveredBluetoothDevice;
import io.github.battery233.room_occupancy.viewmodels.BlinkyViewModel;

@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE = "EXTRA_DEVICE";
    public static final int MAX_OFFLINE_DATA_LENGTH = 10;
    @BindView(R.id.tof_state_1)
    TextView tofState_1;
    @BindView(R.id.tof_state_2)
    TextView tofState_2;
    @BindView(R.id.offline_data_card)
    TextView offlineDataCard;
    @BindView(R.id.pir_state_2)
    TextView pirState2;
    @BindView(R.id.getHighestRssi)
    TextView getHighestRssi;
    @BindView(R.id.button_card_4)
    View buttonCard4;
    private BlinkyViewModel mViewModel;
    private int status;
    private String payloadString;
    private int walkedOutCount = 0;
    private int walkedInCount = 0;
    private boolean offlineDataNumberNotRecorded = true;
    private int offlineInData = 0;
    private boolean offlineInCollected = false;
    private int offlineOutData = 0;
    private boolean offlineOutCollected = false;
    private int boardOnlineTime = 0;

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

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference data = db.collection("Raw Data");

        // Set up views
        final LinearLayout progressContainer = findViewById(R.id.progress_container);
        final TextView connectionState = findViewById(R.id.connection_state);
        final View content = findViewById(R.id.device_container);
        final View notSupported = findViewById(R.id.not_supported);
        buttonCard4.setVisibility(View.GONE);

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
                                        Toast.makeText(MainActivity.this, "Someone walked " + payloadString, Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(MainActivity.this, "Send movement record fail!", Toast.LENGTH_SHORT).show());
                        Log.d("Channel 1: ", "someone walks " + payloadString);
                    }
                    tofState_1.setText(walkedInCount + " in total");
                    tofState_2.setText(walkedOutCount + " in total");
                    Log.d("Channel 1 data: ", String.valueOf(status));
                });

        // get the data of people total in or out
        mViewModel.getDistance2State().observe(this,
                triggered -> {
                    if (offlineDataNumberNotRecorded) {
                        char[] c = {triggered.charAt(5), triggered.charAt(6)};
                        offlineInData = Integer.parseInt(new String(c), 16);
                        c[0] = triggered.charAt(8);
                        c[1] = triggered.charAt(9);
                        offlineOutData = Integer.parseInt(new String(c), 16);
                        if (offlineInData != 0 || offlineOutData != 0) {
                            boardOnlineTime = getValueFromHexString(triggered, 1);
                            Toast.makeText(MainActivity.this, "Offline data found! in: " + offlineInData + " out: " + offlineOutData + " Board timer = " + boardOnlineTime, Toast.LENGTH_SHORT).show();
                            offlineDataNumberNotRecorded = false;
                        }
                    }
                    offlineDataCard.setText("In: " + offlineInData + " Out: " + offlineOutData);
                    Log.d("Channel 2 data: ", "--" + triggered + "--");
                });

        mViewModel.getOfflineInTimeStamps().observe(this,
                triggered -> {
                    Log.d("Channel 3 data: ", "--" + triggered + "--");
                    if (!offlineDataNumberNotRecorded && offlineInData != 0 && !offlineInCollected) {
                        if (offlineInData > MAX_OFFLINE_DATA_LENGTH)
                            offlineInData = MAX_OFFLINE_DATA_LENGTH;
                        //get in data from offline record
                        int i = 0;
                        int timeStamp;
                        Calendar time = Calendar.getInstance();
                        HashMap<String, String> distanceSensorData = new HashMap<>();
                        while (i < offlineInData) {
                            timeStamp = getValueFromHexString(triggered, i);
                            Log.d("Channel 3 data: ", "Offline in time No." + i + " at " + timeStamp);
                            timeStamp = boardOnlineTime - timeStamp;
                            time = Calendar.getInstance();
                            time.add(Calendar.SECOND, -timeStamp);
                            distanceSensorData.put(new SimpleDateFormat("HHmmss", Locale.UK).format(time.getTime()), "IN");
                            Log.d("Channel 3 data: ", "Time when offline walking in happened " + new SimpleDateFormat("yyyyMMddHHmmss", Locale.UK).format(time.getTime()));
                            i++;
                        }
                        data.document(new SimpleDateFormat("yyyyMMdd", Locale.UK).format(time.getTime()))
                                .set(distanceSensorData, SetOptions.merge());
                        Log.d("Channel 3 data: ", "Offline walking in logged, number = " + (i + 1));
                        offlineInCollected = true;
                    }
                }
        );

        mViewModel.getOfflineOutTimeStamps().observe(this,
                triggered ->
                {
                    Log.d("Channel 4 data: ", "--" + triggered + "--");
                    if (!offlineDataNumberNotRecorded && offlineOutData != 0 && !offlineOutCollected) {
                        if (offlineOutData > MAX_OFFLINE_DATA_LENGTH)
                            offlineOutData = MAX_OFFLINE_DATA_LENGTH;
                        //get in data from offline record
                        int i = 0;
                        int timeStamp;
                        Calendar time = Calendar.getInstance();
                        HashMap<String, String> distanceSensorData = new HashMap<>();
                        while (i < offlineOutData) {
                            timeStamp = getValueFromHexString(triggered, i);
                            Log.d("Channel 4 data: ", "Offline out time No." + i + " at " + timeStamp);
                            timeStamp = boardOnlineTime - timeStamp;
                            time = Calendar.getInstance();
                            time.add(Calendar.SECOND, -timeStamp);
                            distanceSensorData.put(new SimpleDateFormat("HHmmss", Locale.UK).format(time.getTime()), "OUT");
                            Log.d("Channel 4 data: ", "Time when offline walking out happened " + new SimpleDateFormat("yyyyMMddHHmmss", Locale.UK).format(time.getTime()));
                            i++;
                        }
                        data.document(new SimpleDateFormat("yyyyMMdd", Locale.UK).format(time.getTime()))
                                .set(distanceSensorData, SetOptions.merge());
                        Log.d("Channel 4 data: ", "Offline walking out logged, number = " + (i + 1));
                        offlineOutCollected = true;
                    }
                }
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
            offlineDataCard.setText(R.string.button_unknown);
            pirState2.setText(R.string.button_unknown);
        }
    }

    //index--> the 16 bit number in the string you want. start from 0
    private int getValueFromHexString(String s, int offset) {
        offset = 5 + offset * 6;
        char[] c = new char[4];
        c[0] = s.charAt(offset + 3);
        c[1] = s.charAt(offset + 4);
        c[2] = s.charAt(offset);
        c[3] = s.charAt(offset + 1);
        Log.e("here!" + s, new String(c));
        return Integer.parseInt(new String(c), 16);
    }
}
