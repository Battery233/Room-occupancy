#include <events/mbed_events.h>
#include <mbed.h>
#include "ble/BLE.h"
#include "ble/Gap.h"
#include "ble/services/HeartRateService.h"

const static char DEVICE_NAME[] = "The device";

static EventQueue eventQueue(/* event count */ 16 * EVENTS_EVENT_SIZE);

static uint8_t hrmCounter = 100; // init HRM to 100bps
static HeartRateService *hrServicePtr;

void onBleInitError(BLE &ble, ble_error_t error) {
    (void) ble;
    (void) error;
    /* Initialization error handling should go here */
}

// Initiialization routine goes here
void bleInitComplete(BLE::InitializationCompleteCallbackContext *params) {

    BLE &ble = params->ble;
    ble_error_t error = params->error;

    // healthy error checking
    if (error != BLE_ERROR_NONE) {
        onBleInitError(ble, error);
        return;
    }

    if (ble.getInstanceID() != BLE::DEFAULT_INSTANCE) {
        return;
    }

    /* Setup primary service. */
    hrServicePtr = new HeartRateService(ble, hrmCounter,
                                        HeartRateService::LOCATION_FINGER);

    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::GENERIC_HEART_RATE_SENSOR);
    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::COMPLETE_LOCAL_NAME, (uint8_t*) DEVICE_NAME, sizeof(DEVICE_NAME));
    ble.gap().setAdvertisingType(GapAdvertisingParams::ADV_CONNECTABLE_UNDIRECTED);
    ble.gap().setAdvertisingInterval(1000); /* 1000ms */
    ble.gap().startAdvertising();
}

// events processing handler
void scheduleBleEventsProcessing(BLE::OnEventsToProcessCallbackContext *context) {
    eventQueue.call(callback(&(context->ble), &BLE::processEvents));
}

int main() {
    // create BLE instance
    BLE &ble = BLE::Instance();

    // link to events processing handler
    ble.onEventsToProcess(scheduleBleEventsProcessing);
    // initialise the BLE device
    ble.init(bleInitComplete);
    eventQueue.dispatch_forever();

    return 0;
}
