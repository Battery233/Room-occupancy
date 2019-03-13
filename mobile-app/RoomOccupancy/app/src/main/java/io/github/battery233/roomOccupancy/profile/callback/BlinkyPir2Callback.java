package io.github.battery233.roomOccupancy.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface BlinkyPir2Callback {
    void onPir2StateChanged(@NonNull final BluetoothDevice device, final String pressed);
}
