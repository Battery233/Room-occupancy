package io.github.battery233.roomOccupancy.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface BlinkyPir1Callback {
    void onPir1StateChanged(@NonNull final BluetoothDevice device, final boolean pressed);
}
