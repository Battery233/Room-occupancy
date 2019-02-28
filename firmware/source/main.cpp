#include <events/mbed_events.h>
#include <mbed.h>
#include "ble/BLE.h"
#include "ble/Gap.h"
#include "PirService.h"
#include "DistanceService.h"
#include "VL53L0X.h"

#define I2C_SCL   p7
#define I2C_SDA   p30
#define PIR_PIN p16
#define DUMMY_PIN p1
 
DigitalOut  led1(LED1, 1);
DigitalOut  led2(LED2, 1);
InterruptIn pir1(PIR_PIN, PullNone); //PIR sensor on -> 0, sensor off -> 1
 
const static char     DEVICE_NAME[] = "---OMG---";
static const uint16_t uuid16_list[] = {0xFFFF};
static EventQueue eventQueue(/* event count */ 20 * EVENTS_EVENT_SIZE);
Serial pc(USBTX, USBRX);
PirService *pirServicePtr;
DistanceService *distanceServicePtr;

// callback for distance
void distanceTrueCallback(void)
{
    eventQueue.call(Callback<void(bool)>(distanceServicePtr, &DistanceService::updateDistanceState), true);
}
 
void distanceFalseCallback(void)
{
    eventQueue.call(Callback<void(bool)>(distanceServicePtr, &DistanceService::updateDistanceState), false);
}

void queueCallback(void)
{
    led1 = !led1; /* Do blinky on LED1 to indicate system aliveness. */
    // Distance sensor here
    static DevI2C devI2c(I2C_SDA, I2C_SCL);
    static DigitalOut dummyOut(DUMMY_PIN);
    static VL53L0X distance_raw(&devI2c, &dummyOut, NC);
    distance_raw.init_sensor(0x29);
    uint32_t distance = 0;
    distance_raw.get_distance(&distance);
//    if(VL53L0X_ERROR_NONE==distance_raw.get_distance(&distance)){
    if(distance>10&&distance<100){
            led2 = 0;
            distanceTrueCallback();   
        }
        else {
            led2 = 1;
            distanceFalseCallback();
        }
    }
//    }

// system callback 
void disconnectionCallback(const Gap::DisconnectionCallbackParams_t *params)
{
    BLE::Instance().gap().startAdvertising(); // restart advertising
}
 
void onBleInitError(BLE &ble, ble_error_t error)
{
    /* Initialization error handling should go here */
}

// callback functions for pir
void buttonPressedCallback(void)
{
    eventQueue.call(Callback<void(bool)>(pirServicePtr, &PirService::updatePirState), true);
}
 
void buttonReleasedCallback(void)
{
    eventQueue.call(Callback<void(bool)>(pirServicePtr, &PirService::updatePirState), false);
}
 
void bleInitComplete(BLE::InitializationCompleteCallbackContext *params)
{
    BLE&        ble   = params->ble;
    ble_error_t error = params->error;
 
    if (error != BLE_ERROR_NONE) {
        /* In case of error, forward the error handling to onBleInitError */
        onBleInitError(ble, error);
        return;
    }
 
    /* Ensure that it is the default instance of BLE */
    if(ble.getInstanceID() != BLE::DEFAULT_INSTANCE) {
        return;
    }
 
    ble.gap().onDisconnection(disconnectionCallback);
 
    pir1.fall(buttonPressedCallback);
    pir1.rise(buttonReleasedCallback);
 
    /* Setup primary service. */
    pirServicePtr = new PirService(ble, false /* initial value for button pressed */);
    distanceServicePtr = new DistanceService(ble,false);
 
    /* setup advertising */
    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::BREDR_NOT_SUPPORTED | GapAdvertisingData::LE_GENERAL_DISCOVERABLE);
    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::COMPLETE_LIST_16BIT_SERVICE_IDS, (uint8_t *)uuid16_list, sizeof(uuid16_list));
    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::COMPLETE_LOCAL_NAME, (uint8_t *)DEVICE_NAME, sizeof(DEVICE_NAME));
    ble.gap().setAdvertisingType(GapAdvertisingParams::ADV_CONNECTABLE_UNDIRECTED);
    ble.gap().setAdvertisingInterval(1000); /* 1000ms. */
    ble.gap().startAdvertising();
}
 
void scheduleBleEventsProcessing(BLE::OnEventsToProcessCallbackContext* context) {
    BLE &ble = BLE::Instance();
    eventQueue.call(Callback<void()>(&ble, &BLE::processEvents));
}
 
int main()
{
    pc.printf("Hello World!\n");

    eventQueue.call_every(100, queueCallback);
 
    BLE &ble = BLE::Instance();
    ble.onEventsToProcess(scheduleBleEventsProcessing);
    ble.init(bleInitComplete);
 
    eventQueue.dispatch_forever();
 
    return 0;
}
 