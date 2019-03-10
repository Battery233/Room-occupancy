package io.github.battery233.roomOccupancy.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

public interface Distance1Callback {

    /**
     * Called when a button was pressed or released on device.
     *
     * @param device  the target device.
     * @param pressed true if the button was pressed, false if released.
     */
    void onDistance1StateChanged(@NonNull final BluetoothDevice device, final boolean pressed);
}
