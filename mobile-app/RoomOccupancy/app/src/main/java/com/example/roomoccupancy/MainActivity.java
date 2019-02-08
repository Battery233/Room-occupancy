package com.example.roomoccupancy;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothConnActivity";

    private static final String MAC_ADDRESS_1 = "C6:3A:65:0A:DC:E3 ";

    AlertDialog mSelectionDialog;
    DevicesAdapter mDevicesAdapter;
    BluetoothAdapter mBluetoothAdapter;
    Handler mHandler;
    boolean mScanning;
    BluetoothGatt mGatt;
    BluetoothManager btManager;
    private static final int SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mHandler = new Handler();
        // create instances of mHandler and mDevicesAdapter (add code)

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set dialog title (add code here)

        builder.setAdapter(mDevicesAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishScanning();

                // Connect to GATT server (add code)
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finishScanning();
            }
        });

        // create an instance of mSelectionDialog (add code)

        // Enable Bluetooth (add code)

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Test!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = btManager.getAdapter();
    }

    public void onConnectClick(View view) {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        } else {
            String btnText = ((Button) view).getText().toString();

            if (btnText.equals(getString(R.string.connect))) {
                openSelectionDialog();
            } else if (btnText.equals(getString(R.string.disconnect))) {
                // add code here

                updateConnectButton(BluetoothProfile.STATE_DISCONNECTED);
            }
        }
    }

    void openSelectionDialog() {
        beginScanning();
        // show the selection dialog (add code)
    }

    private void beginScanning() {
        if (!mScanning) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {//TODO: problems starts from here
                @Override
                public void run() {
                    finishScanning();
                }
            }, SCAN_PERIOD);

            // add code here (mDevicesAdapter and mBluetoothAdapter)
        }
    }

    private void finishScanning() {
        if (mScanning) {
            // add code here
        }
    }

    private void updateConnectButton(int state) {
        // Create the Button (add code)
        switch (state) {
            // Change the text of the button
            case BluetoothProfile.STATE_DISCONNECTED:
                // add code here
                break;
            case BluetoothProfile.STATE_CONNECTING:
                // add code here
                break;
            case BluetoothProfile.STATE_CONNECTED:
                // add code here
                break;
        }
    }

    private BluetoothAdapter.LeScanCallback mScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice bluetoothDevice, int i, final byte[] bytes) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Find the board (type "unknown") by its appearance (add code).
                }
            });
        }
    };

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, final int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                if (mGatt.discoverServices()) {
                    Log.i(TAG, "Started service discovery.");
                } else {
                    Log.w(TAG, "Service discovery failed.");
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateConnectButton(newState);
                }
            });
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
