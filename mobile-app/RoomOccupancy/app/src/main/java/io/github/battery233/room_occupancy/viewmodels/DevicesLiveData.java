package io.github.battery233.room_occupancy.viewmodels;

import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import io.github.battery233.room_occupancy.adapter.DiscoveredBluetoothDevice;
import io.github.battery233.room_occupancy.profile.BlinkyManager;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * This class keeps the current list of discovered Bluetooth LE devices matching filter.
 * Each time @{link {@link #applyFilter()} is called, the observers are notified with a new
 * list instance.
 */
@SuppressWarnings("unused")
public class DevicesLiveData extends LiveData<List<DiscoveredBluetoothDevice>> {
    private static final ParcelUuid FILTER_UUID = new ParcelUuid(BlinkyManager.UUID_SERVICE);

    private static final int FILTER_RSSI = -60; // [dBm]

    private final List<DiscoveredBluetoothDevice> mDevices = new ArrayList<>();
    private List<DiscoveredBluetoothDevice> mFilteredDevices = null;
    private boolean mFilterUuidRequired;
    private boolean mFilterNearbyOnly;

    /* package */ DevicesLiveData(final boolean filterUuidRequired, final boolean filterNearbyOnly) {
        mFilterUuidRequired = filterUuidRequired;
        mFilterNearbyOnly = filterNearbyOnly;
    }

    /* package */
    synchronized void bluetoothDisabled() {
        mDevices.clear();
        mFilteredDevices = null;
        postValue(null);
    }

    /* package */  boolean filterByUuid(final boolean uuidRequired) {
        mFilterUuidRequired = uuidRequired;
        return applyFilter();
    }

    /* package */  boolean filterByDistance(final boolean nearbyOnly) {
        mFilterNearbyOnly = nearbyOnly;
        return applyFilter();
    }

    /* package */
    synchronized boolean deviceDiscovered(final ScanResult result) {
        DiscoveredBluetoothDevice device;

        // Check if it's a new device.
        final int index = indexOf(result);
        if (index == -1) {
            device = new DiscoveredBluetoothDevice(result);
            mDevices.add(device);
        } else {
            device = mDevices.get(index);
        }

        // Update RSSI and name.
        device.update(result);

        // Return true if the device was on the filtered list or is to be added.
        return (mFilteredDevices != null && mFilteredDevices.contains(device))
                || (matchesUuidFilter(result) && matchesNearbyFilter(device.getHighestRssi()));
    }

    /**
     * Clears the list of devices.
     */
    public synchronized void clear() {
        mDevices.clear();
        mFilteredDevices = null;
        postValue(null);
    }

    /**
     * Refreshes the filtered device list based on the filter flags.
     */
    /* package */
    synchronized boolean applyFilter() {
        final List<DiscoveredBluetoothDevice> devices = new ArrayList<>();
        for (final DiscoveredBluetoothDevice device : mDevices) {
            final ScanResult result = device.getScanResult();
            if (matchesUuidFilter(result) && matchesNearbyFilter(device.getHighestRssi())) {
                devices.add(device);
            }
        }
        mFilteredDevices = devices;
        postValue(mFilteredDevices);
        return !mFilteredDevices.isEmpty();
    }

    /**
     * Finds the index of existing devices on the device list.
     *
     * @param result scan result.
     * @return Index of -1 if not found.
     */
    private int indexOf(final ScanResult result) {
        int i = 0;
        for (final DiscoveredBluetoothDevice device : mDevices) {
            if (device.matches(result))
                return i;
            i++;
        }
        return -1;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean matchesUuidFilter(final ScanResult result) {
        if (!mFilterUuidRequired)
            return true;

        final ScanRecord record = result.getScanRecord();
        if (record == null)
            return false;

        final List<ParcelUuid> uuids = record.getServiceUuids();
        return uuids != null;
//        return uuids.contains(FILTER_UUID) && uuids.contains(FILTER_UUID_2) && uuids.contains(FILTER_UUID_3);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean matchesNearbyFilter(final int rssi) {
        if (!mFilterNearbyOnly)
            return true;

        return rssi >= FILTER_RSSI;
    }
}
