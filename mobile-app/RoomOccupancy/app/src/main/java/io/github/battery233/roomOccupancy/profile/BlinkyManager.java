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

    /**
     * BUTTON characteristic UUID.
     */
    private final static UUID LBS_UUID_BUTTON_CHAR = UUID.fromString("0000ab01-0000-1000-8000-00805f9b34fb");
    /**
     * LED characteristic UUID.
     */
    private final static UUID LBS_UUID_LED_CHAR = UUID.fromString("0000ab11-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic mButtonCharacteristic, mLedCharacteristic;
    private LogSession mLogSession;
    private boolean mSupported;
    private boolean mLedOn;

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
    private final BlinkyButtonDataCallback mButtonCallback = new BlinkyButtonDataCallback() {
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



    /**
     * BluetoothGatt callbacks object.
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
            setNotificationCallback(mButtonCharacteristic).with(mButtonCallback);
            readCharacteristic(mButtonCharacteristic).with(mButtonCallback).enqueue();
            enableNotifications(mButtonCharacteristic).enqueue();
        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(UUID_SERVICE);
            final BluetoothGattService service2 = gatt.getService(UUID_SERVICE_2);
            if (service != null && service2 != null) {
                mButtonCharacteristic = service.getCharacteristic(LBS_UUID_BUTTON_CHAR);
                mLedCharacteristic = service2.getCharacteristic(LBS_UUID_LED_CHAR);
            }

//			boolean writeRequest = false;
//			if (mLedCharacteristic != null) {
//				final int rxProperties = mLedCharacteristic.getProperties();
//				writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
//			}

            mSupported = mButtonCharacteristic != null && mLedCharacteristic != null; //&& writeRequest;
            return mSupported;
        }

        @Override
        protected void onDeviceDisconnected() {
            mButtonCharacteristic = null;
            mLedCharacteristic = null;
        }
    };
}
