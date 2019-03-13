package io.github.battery233.roomOccupancy.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.battery233.roomOccupancy.profile.callback.BlinkyPir1DataCallback;
import io.github.battery233.roomOccupancy.profile.callback.BlinkyPir2DataCallback;
import io.github.battery233.roomOccupancy.profile.callback.Distance1DataCallback;
import io.github.battery233.roomOccupancy.profile.callback.Distance2DataCallback;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public class BlinkyManager extends BleManager<BlinkyManagerCallbacks> {
    /**
     * Nordic Service UUID.
     */
    public final static UUID UUID_SERVICE = UUID.fromString("0000ab00-0000-1000-8000-00805f9b34fb");

    private final static UUID UUID_TOF1_CHAR = UUID.fromString("0000ab01-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_TOF2_CHAR = UUID.fromString("0000ab02-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_PIR1_CHAR = UUID.fromString("0000ab03-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_PIR2_CHAR = UUID.fromString("0000ab04-0000-1000-8000-00805f9b34fb");
    /**
     * The Button callback will be notified when a notification from Button characteristic
     * has been received, or its data was read.
     * <p>
     * If the data received are valid (single byte equal to 0x00 or 0x01), the
     * {@link Distance1DataCallback#onDistance1StateChanged} will be called.
     * Otherwise, the {@link Distance1DataCallback#onInvalidDataReceived(BluetoothDevice, Data)}
     * will be called with the data received.
     */
    private final Distance1DataCallback mTof1CallBack = new Distance1DataCallback() {
        @Override
        public void onDistance1StateChanged(@NonNull final BluetoothDevice device,
                                            final String pressed) {
            mCallbacks.onDistance1StateChanged(device, pressed);
        }
    };
    private final Distance2DataCallback mTof2CallBack = new Distance2DataCallback() {
        @Override
        public void onDistance2StateChanged(@NonNull final BluetoothDevice device,
                                            final String pressed) {
            mCallbacks.onDistance2StateChanged(device, pressed);
        }
    };
    private final BlinkyPir1DataCallback mPir1CallBack = new BlinkyPir1DataCallback() {

        @Override
        public void onPir1StateChanged(@NonNull BluetoothDevice device, String pressed) {
            mCallbacks.onPir1StateChanged(device, pressed);
        }
    };
    private final BlinkyPir2DataCallback mPir2CallBack = new BlinkyPir2DataCallback() {

        @Override
        public void onPir2StateChanged(@NonNull BluetoothDevice device, String pressed) {
            mCallbacks.onPir2StateChanged(device, pressed);
        }
    };
    private BluetoothGattCharacteristic mTof1Characteristic, mTof2Characteristic, mPir1Characteristic, mPir2Characteristic;
    private LogSession mLogSession;
    private boolean mSupported;
    /**
     * BluetoothGatt callbacks object.
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
            setNotificationCallback(mTof1Characteristic).with(mTof1CallBack);
            readCharacteristic(mTof1Characteristic).with(mTof1CallBack).enqueue();
            enableNotifications(mTof1Characteristic).enqueue();
            setNotificationCallback(mTof2Characteristic).with(mTof2CallBack);
            readCharacteristic(mTof2Characteristic).with(mTof2CallBack).enqueue();
            enableNotifications(mTof2Characteristic).enqueue();
            setNotificationCallback(mPir1Characteristic).with(mPir1CallBack);
            enableNotifications(mPir1Characteristic).enqueue();
            readCharacteristic(mPir1Characteristic).with(mPir1CallBack).enqueue();
            setNotificationCallback(mPir2Characteristic).with(mPir2CallBack);
            readCharacteristic(mPir2Characteristic).with(mPir2CallBack).enqueue();
            enableNotifications(mPir2Characteristic).enqueue();
        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(UUID_SERVICE);
            if (service != null) {
                mTof1Characteristic = service.getCharacteristic(UUID_TOF1_CHAR);
                mTof2Characteristic = service.getCharacteristic(UUID_TOF2_CHAR);
                mPir1Characteristic = service.getCharacteristic(UUID_PIR1_CHAR);
                mPir2Characteristic = service.getCharacteristic(UUID_PIR2_CHAR);
            }

            mSupported = mTof1Characteristic != null;// && mPir1Characteristic != null && mPir2Characteristic != null; //&& writeRequest;
            return mSupported;
        }

        @Override
        protected void onDeviceDisconnected() {
            mTof1Characteristic = null;
            mTof2Characteristic = null;
            mPir1Characteristic = null;
            mPir2Characteristic = null;
        }
    };

    public BlinkyManager(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    /**
     * Sets the log session to be used for low level logging.
     *
     * @param session the session, or null, if nRF Logger is not installed.
     */
    public void setLogger(@Nullable final LogSession session) {
        this.mLogSession = session;
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
        // The priority is a Log.X constant, while the Logger accepts it's log levels.
        Logger.log(mLogSession, LogContract.Log.Level.fromPriority(priority), message);
    }

    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        return !mSupported;
    }
}
