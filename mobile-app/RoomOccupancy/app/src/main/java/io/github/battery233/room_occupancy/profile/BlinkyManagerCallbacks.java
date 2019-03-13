package io.github.battery233.room_occupancy.profile;

import io.github.battery233.room_occupancy.profile.callback.BlinkyPir1Callback;
import io.github.battery233.room_occupancy.profile.callback.BlinkyPir2Callback;
import io.github.battery233.room_occupancy.profile.callback.Distance1Callback;
import io.github.battery233.room_occupancy.profile.callback.Distance2Callback;
import no.nordicsemi.android.ble.BleManagerCallbacks;

public interface BlinkyManagerCallbacks extends BleManagerCallbacks,
        Distance1Callback, Distance2Callback, BlinkyPir1Callback, BlinkyPir2Callback {
    // No more methods
}
