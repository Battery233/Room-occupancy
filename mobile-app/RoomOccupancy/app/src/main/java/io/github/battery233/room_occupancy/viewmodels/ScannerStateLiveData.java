package io.github.battery233.room_occupancy.viewmodels;

import androidx.lifecycle.LiveData;

/**
 * This class keeps the current state of the scanner.
 */
@SuppressWarnings("unused")
public class ScannerStateLiveData extends LiveData<ScannerStateLiveData> {
    private boolean mScanningStarted;
    private boolean mHasRecords;
    private boolean mBluetoothEnabled;
    private boolean mLocationEnabled;

    /* package */ ScannerStateLiveData(final boolean bluetoothEnabled,
                                       final boolean locationEnabled) {
        mScanningStarted = false;
        mBluetoothEnabled = bluetoothEnabled;
        mLocationEnabled = locationEnabled;
        postValue(this);
    }

    /* package */ void refresh() {
        postValue(this);
    }

    /* package */ void scanningStarted() {
        mScanningStarted = true;
        postValue(this);
    }

    /* package */ void scanningStopped() {
        mScanningStarted = false;
        postValue(this);
    }

    /* package */ void bluetoothEnabled() {
        mBluetoothEnabled = true;
        postValue(this);
    }

    /* package */
    synchronized void bluetoothDisabled() {
        mBluetoothEnabled = false;
        mHasRecords = false;
        postValue(this);
    }

    /* package */ void setLocationEnabled(final boolean enabled) {
        mLocationEnabled = enabled;
        postValue(this);
    }

    /* package */ void recordFound() {
        mHasRecords = true;
        postValue(this);
    }

    /**
     * Returns whether scanning is in progress.
     */
    boolean isScanning() {
        return mScanningStarted;
    }

    /**
     * Returns whether any records matching filter criteria has been found.
     */
    public boolean hasRecords() {
        return mHasRecords;
    }

    /**
     * Returns whether Bluetooth adapter is enabled.
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothEnabled;
    }

    /**
     * Returns whether Location is enabled.
     */
    public boolean isLocationEnabled() {
        return mLocationEnabled;
    }

    /**
     * Notifies the observer that scanner has no records to show.
     */
    public void clearRecords() {
        mHasRecords = false;
        postValue(this);
    }
}
