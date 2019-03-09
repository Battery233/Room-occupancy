package io.github.battery233.roomOccupancy.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

import io.github.battery233.roomOccupancy.profile.callback.BlinkyPir1DataCallback;
import io.github.battery233.roomOccupancy.profile.callback.BlinkyPir2DataCallback;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;
import io.github.battery233.roomOccupancy.profile.callback.BlinkyButtonDataCallback;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

public class BlinkyManager extends BleManager<BlinkyManagerCallbacks> {
    /**
     * Nordic Blinky Service UUID.
     */
    public final static UUID UUID_SERVICE = UUID.fromString("0000ab00-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_SERVICE_2 = UUID.fromString("0000ab10-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_SERVICE_3 = UUID.fromString("0000ab20-0000-1000-8000-00805f9b34fb");

    private final static UUID UUID_TOF_CHAR = UUID.fromString("0000ab01-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_PIR1_CHAR = UUID.fromString("0000ab11-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_PIR2_CHAR = UUID.fromString("0000ab21-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic mTofCharacteristic, mPir1Characteristic, mPir2Characteristic;
    private LogSession mLogSession;
    private boolean mSupported;

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

    /**
     * The Button callback will be notified when a notification from Button characteristic
     * has been received, or its data was read.
     * <p>
     * If the data received are valid (single byte equal to 0x00 or 0x01), the
     * {@link BlinkyButtonDataCallback#onButtonStateChanged} will be called.
     * Otherwise, the {@link BlinkyButtonDataCallback#onInvalidDataReceived(BluetoothDevice, Data)}
     * will be called with the data received.
     */
    private final BlinkyButtonDataCallback mTofCallBack = new BlinkyButtonDataCallback() {
        @Override
        public void onButtonStateChanged(@NonNull final BluetoothDevice device,
                                         final boolean pressed) {
            log(LogContract.Log.Level.APPLICATION, "Button " + (pressed ? "pressed" : "released"));
            mCallbacks.onButtonStateChanged(device, pressed);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device,
                                          @NonNull final Data data) {
            log(Log.WARN, "Invalid data received: " + data);
        }
    };

    private final BlinkyPir1DataCallback mPir1CallBack = new BlinkyPir1DataCallback() {

        @Override
        public void onPir1StateChanged(@NonNull BluetoothDevice device, boolean pressed) {
            mCallbacks.onPir1StateChanged(device, pressed);
        }
    };

    private final BlinkyPir2DataCallback mPir2CallBack = new BlinkyPir2DataCallback() {

        @Override
        public void onPir2StateChanged(@NonNull BluetoothDevice device, boolean pressed) {
            mCallbacks.onPir2StateChanged(device, pressed);
        }
    };
    /**
     * BluetoothGatt callbacks object.
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
            setNotificationCallback(mTofCharacteristic).with(mTofCallBack);
            readCharacteristic(mTofCharacteristic).with(mTofCallBack).enqueue();
            enableNotifications(mTofCharacteristic).enqueue();
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
            final BluetoothGattService service2 = gatt.getService(UUID_SERVICE_2);
            final BluetoothGattService service3 = gatt.getService(UUID_SERVICE_3);
            if (service != null && service2 != null && service3 != null) {
                mTofCharacteristic = service.getCharacteristic(UUID_TOF_CHAR);
                mPir1Characteristic = service2.getCharacteristic(UUID_PIR1_CHAR);
                mPir2Characteristic = service3.getCharacteristic(UUID_PIR2_CHAR);
            }

//			boolean writeRequest = false;
//			if (mLedCharacteristic != null) {
//				final int rxProperties = mLedCharacteristic.getProperties();
//				writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
//			}

            mSupported = mTofCharacteristic != null;// && mPir1Characteristic != null && mPir2Characteristic != null; //&& writeRequest;
            return mSupported;
        }

        @Override
        protected void onDeviceDisconnected() {
            mTofCharacteristic = null;
            mPir1Characteristic = null;
            mPir2Characteristic = null;
        }
    };
}
