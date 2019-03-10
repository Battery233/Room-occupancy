package io.github.battery233.roomOccupancy.profile.callback;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("ConstantConditions")
public abstract class BlinkyPir2DataCallback implements ProfileDataCallback, BlinkyPir2Callback {
    private static final int STATE_RELEASED = 0x00;
    private static final int STATE_PRESSED = 0x01;

    @Override
    public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
        if (data.size() != 1) {
            onInvalidDataReceived(device, data);
            return;
        }

        final int state = data.getIntValue(Data.FORMAT_UINT8, 0);
        if (state == STATE_PRESSED) {
            onPir2StateChanged(device, true);
        } else if (state == STATE_RELEASED) {
            onPir2StateChanged(device, false);
        } else {
            onInvalidDataReceived(device, data);
        }
    }
}
