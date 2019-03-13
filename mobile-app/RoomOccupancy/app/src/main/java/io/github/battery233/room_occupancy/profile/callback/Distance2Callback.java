package io.github.battery233.room_occupancy.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface Distance2Callback {

    /**
     * Called when a button was pressed or released on device.
     *
     * @param device  the target device.
     * @param pressed true if the button was pressed, false if released.
     */
    void onDistance2StateChanged(@NonNull final BluetoothDevice device, final String pressed);
}
