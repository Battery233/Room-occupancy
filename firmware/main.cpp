#include "mbed.h"
#include "ble/BLE.h"
#include "VL53L0X.h"

Serial pc(USBTX, USBRX);
DigitalOut led(LED1);

// two pir sensors
#define PIR_PIN_1 p14
DigitalIn pir_1(p14, PullNone);
#define PIR_PIN_2 p15
DigitalIn pir_2(p15, PullNone);

// define distance range
#define DISTANCE_MIN 10
#define DISTANCE_MAX 500

#define OFFLINE_TIME_STAMP_SIZE 32 // 2 for each timestamp

//distance sensors
#define tof_address_1 (0x29)
#define tof_address_2 (0x30)
#define tof_sensor_1_SHUT p16
#define tof_sensor_2_SHUT p17
#define I2C_SCL p7
#define I2C_SDA p30
static DevI2C devI2c(I2C_SDA, I2C_SCL);
static DigitalOut shutdown1_pin(tof_sensor_1_SHUT);
static DigitalOut shutdown2_pin(tof_sensor_2_SHUT);
static VL53L0X tof_sensor_1(&devI2c, &shutdown1_pin, NC);
static VL53L0X tof_sensor_2(&devI2c, &shutdown2_pin, NC);

// UUID setup
uint16_t serviceUUID = 0xAB00;
uint16_t distanceCharUUID_1 = 0xAB01;
uint16_t distanceCharUUID_2 = 0xAB02;
uint16_t pirCharUUID_1 = 0xAB03;
uint16_t pirCharUUID_2 = 0xAB04;

// device info
const static char DEVICE_NAME[] = "OMG"; // change this
static const uint16_t uuid16_list[] = { 0xFFFF }; //Custom UUID, FFFF is reserved for development

// payload
static uint8_t readValue = 0;
static uint8_t offlineDoubleValue[4] = {0};
static uint8_t offlineTimeValueIn[OFFLINE_TIME_STAMP_SIZE] = {0};
static uint8_t offlineTimeValueOut[OFFLINE_TIME_STAMP_SIZE] = {0};

// gatt chars
ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(readValue)> distanceChar_1(distanceCharUUID_1, &readValue,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ | GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY);

ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(offlineDoubleValue)> distanceChar_2(distanceCharUUID_2, offlineDoubleValue,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ | GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY);

ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(offlineTimeValueIn)> pirChar_1(pirCharUUID_1, offlineTimeValueIn,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ | GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY);

ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(offlineTimeValueOut)> pirChar_2(pirCharUUID_2, offlineTimeValueOut,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ | GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY);

/* Set up custom service */
GattCharacteristic* characteristics[] = { &distanceChar_1, &distanceChar_2, &pirChar_1, &pirChar_2 };
GattService service(serviceUUID, characteristics, sizeof(characteristics) / sizeof(GattCharacteristic*));

static uint8_t bluetooth_connected;

void connectionCallback(const Gap::ConnectionCallbackParams_t*)
{
    bluetooth_connected = 1;
    printf("--connectionCallback--offline data sent and refreshed!--%d\r\n", bluetooth_connected);
}

/*
 *  Restart advertising when phone app disconnects
*/
void disconnectionCallback(const Gap::DisconnectionCallbackParams_t*)
{
    bluetooth_connected = 0;
    offlineDoubleValue[0] = 0;
    offlineDoubleValue[1] = 0;
    printf("--DisconnectionCallback--%d\r\n", bluetooth_connected);
    BLE::Instance(BLE::DEFAULT_INSTANCE).gap().startAdvertising();
}

/*
 * Initialization callback
 */
void bleInitComplete(BLE::InitializationCompleteCallbackContext* params)
{
    BLE& ble = params->ble;
    ble_error_t error = params->error;

    if (error != BLE_ERROR_NONE) {
        return;
    }

    ble.gap().onDisconnection(disconnectionCallback);
    ble.gap().onConnection(connectionCallback);

    /* Setup advertising */
    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::BREDR_NOT_SUPPORTED | GapAdvertisingData::LE_GENERAL_DISCOVERABLE); // BLE only, no classic BT
    ble.gap().setAdvertisingType(GapAdvertisingParams::ADV_CONNECTABLE_UNDIRECTED); // advertising type
    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::COMPLETE_LOCAL_NAME, (uint8_t*)DEVICE_NAME, sizeof(DEVICE_NAME)); // add name
    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::COMPLETE_LIST_16BIT_SERVICE_IDS, (uint8_t*)uuid16_list, sizeof(uuid16_list)); // UUID's broadcast in advertising packet
    ble.gap().setAdvertisingInterval(100); // 100ms.

    /* Add our custom service */
    ble.addService(service);

    /* Start advertising */
    ble.gap().startAdvertising();
}

int main()
{
    bluetooth_connected = 0;
    BLE& ble = BLE::Instance(BLE::DEFAULT_INSTANCE);
    ble.init(bleInitComplete);

    /* SpinWait for initialization to complete. This is necessary because the
     * BLE object is used in the main loop below. */
    while (ble.hasInitialized() == false) { /* spin loop */
    }

    //init tof sensors
    tof_sensor_1.init_sensor(tof_address_1);
    tof_sensor_2.init_sensor(tof_address_2);

    uint32_t distance1;
    uint32_t distance2;
    uint8_t distance_boolean_1;
    uint8_t distance_boolean_2;
    bool both_triggered = false;
    bool only_left_triggered = false;
    bool only_right_triggered = false;
    bool logged = false;
    uint8_t distance_in_or_out;
    uint32_t loop_counter = 0;
    uint16_t seconds = 0;
    printf("\n\r********* Device ready!*********\n\r");

    while (1) {
        led = !led;

        tof_sensor_1.get_distance(&distance1);
        tof_sensor_2.get_distance(&distance2);
        printf("Distance: %d, %d\r\n", distance1, distance2);

        distance_in_or_out = 0;
        //        get string and send via bluetooth
        distance_boolean_1 = distance1 > DISTANCE_MIN && distance1 < DISTANCE_MAX ? 1 : 0;
        distance_boolean_2 = distance2 > DISTANCE_MIN && distance2 < DISTANCE_MAX ? 1 : 0;

        //is people go in or out
        //0 nothing, 1 in, 2 out
        if(distance_boolean_1 && !distance_boolean_2) {
            if(both_triggered || only_right_triggered) {
                if(!logged) {
                    logged = true;
                    distance_in_or_out = 1;
                }
            }
            only_left_triggered = true;
            only_right_triggered = false;
            both_triggered = false;
        } else if (!distance_boolean_1 && distance_boolean_2) {
            if(both_triggered || only_left_triggered) {
                if(!logged) {
                    logged = true;
                    distance_in_or_out = 2;
                }
            }
            only_left_triggered = false;
            only_right_triggered = true;
            both_triggered = false;
        } else if (distance_boolean_1 && distance_boolean_2) {
            if(only_left_triggered) {
                distance_in_or_out = 2;
                logged = true;
            } else if(only_right_triggered) {
                distance_in_or_out = 1;
                logged = true;
            }
            only_left_triggered = false;
            only_right_triggered = false;
            both_triggered = true;
        } else {
            only_left_triggered = false;
            only_right_triggered = false;
            both_triggered = false;
            logged = false;
        }
        printf("in and out status: %d \r\n", distance_in_or_out);
        memcpy(&readValue, &distance_in_or_out, sizeof(distance_in_or_out));
        ble.updateCharacteristicValue(distanceChar_1.getValueHandle(), &readValue, sizeof(readValue));

// offline counter and timestamps recorded here. max length = OFFLINE_TIME_STAMP_SIZE/2
        if(!bluetooth_connected) {
            if(distance_in_or_out==0x01) {
                memcpy(&offlineTimeValueIn[offlineDoubleValue[0]*2%(OFFLINE_TIME_STAMP_SIZE)], &seconds, sizeof(seconds));
                printf("Offline In time recorded!\r\n");
                offlineDoubleValue[0]++;
            } else if(distance_in_or_out==0x02) {
                memcpy(&offlineTimeValueOut[offlineDoubleValue[1]*2%(OFFLINE_TIME_STAMP_SIZE)], &seconds, sizeof(seconds));
                printf("Offline OUT time recorded!\r\n");
                offlineDoubleValue[1]++;
            }
        }
        memcpy(&offlineDoubleValue[2], &seconds, sizeof(seconds));
        if(!bluetooth_connected) {
            printf("Bluetooth---NOT---connected!\r\n");
            printf("Offline in and out: %d,%d \r\n",offlineDoubleValue[0], offlineDoubleValue[1]);
        } else {
            printf("Bluetooth Connected!\r\n");
        }
        ble.updateCharacteristicValue(distanceChar_2.getValueHandle(), offlineDoubleValue, sizeof(offlineDoubleValue));
        ble.updateCharacteristicValue(pirChar_1.getValueHandle(), offlineTimeValueIn, sizeof(offlineTimeValueIn));
        ble.updateCharacteristicValue(pirChar_2.getValueHandle(), offlineTimeValueOut, sizeof(offlineTimeValueOut));
        loop_counter++;
        seconds = (loop_counter*2)/5;
        printf("While loop %d ends! System temp timer %d seconds!\r\n\n", loop_counter, seconds);
        //wait
        wait(0.005);
    }
}