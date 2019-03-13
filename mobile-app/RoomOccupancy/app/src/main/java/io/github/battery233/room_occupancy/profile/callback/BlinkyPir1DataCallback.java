package io.github.battery233.room_occupancy.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("ConstantConditions")
public abstract class BlinkyPir1DataCallback implements ProfileDataCallback, BlinkyPir1Callback {

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        onPir1StateChanged(device, data.toString());
    }
}
