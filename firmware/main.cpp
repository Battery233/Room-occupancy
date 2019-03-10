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
//distance sensors
#define tof_address_1 (0x29)
#define tof_address_2 (0x30)
#define tof_sensor_1_SHUT   p16
#define tof_sensor_2_SHUT   p17
#define I2C_SCL   p7
#define I2C_SDA   p30
static DevI2C devI2c(I2C_SDA, I2C_SCL);
static DigitalOut shutdown1_pin(tof_sensor_1_SHUT);
static DigitalOut shutdown2_pin(tof_sensor_2_SHUT);
static VL53L0X tof_sensor_1(&devI2c, &shutdown1_pin, NC);
static VL53L0X tof_sensor_2(&devI2c, &shutdown2_pin, NC);

// UUID setup
uint16_t disServiceUUID_1  = 0xAB00;
uint16_t distanceCharUUID_1      = 0xAB01;
uint16_t disServiceUUID_2      = 0xAB10;
uint16_t distanceCharUUID_2      = 0xAB11;
uint16_t pirServiceUUID_1      = 0xAB20;
uint16_t pirCharUUID_1      = 0xAB21;
uint16_t pirServiceUUID_2      = 0xAB30;
uint16_t pirCharUUID_2      = 0xAB31;

// device info
const static char     DEVICE_NAME[]        = "OMG"; // change this
static const uint16_t uuid16_list[]        = {0xFFFF}; //Custom UUID, FFFF is reserved for development

// payload
static uint8_t readValue = 0;

// gatt chars
ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(readValue)> distanceChar_1(distanceCharUUID_1, &readValue,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ |
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY );

ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(readValue)> distanceChar_2(distanceCharUUID_2, &readValue,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ |
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY );


ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(readValue)> pirChar_1(pirCharUUID_1, &readValue,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ |
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY );

ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(readValue)> pirChar_2(pirCharUUID_2, &readValue,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ |
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY );

/* Set up custom service */
GattCharacteristic *characteristics_1[] = {&distanceChar_1};
GattService distanceService_1(disServiceUUID_1, characteristics_1, sizeof(characteristics_1) / sizeof(GattCharacteristic *));
GattCharacteristic *characteristics_2[] = {&distanceChar_2};
GattService distanceService_2(disServiceUUID_2, characteristics_2, sizeof(characteristics_2) / sizeof(GattCharacteristic *));
GattCharacteristic *characteristics_3[] = {&pirChar_1};
GattService pirService_1(pirServiceUUID_1, characteristics_3, sizeof(characteristics_3) / sizeof(GattCharacteristic *));
GattCharacteristic *characteristics_4[] = {&pirChar_2};
GattService pirService_2(pirServiceUUID_2, characteristics_4, sizeof(characteristics_4) / sizeof(GattCharacteristic *));

/*
 *  Restart advertising when phone app disconnects
*/
void disconnectionCallback(const Gap::DisconnectionCallbackParams_t *)
{
    BLE::Instance(BLE::DEFAULT_INSTANCE).gap().startAdvertising();
}

/*
 * Initialization callback
 */
void bleInitComplete(BLE::InitializationCompleteCallbackContext *params)
{
    BLE &ble          = params->ble;
    ble_error_t error = params->error;

    if (error != BLE_ERROR_NONE) {
        return;
    }

    ble.gap().onDisconnection(disconnectionCallback);

    /* Setup advertising */
    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::BREDR_NOT_SUPPORTED | GapAdvertisingData::LE_GENERAL_DISCOVERABLE); // BLE only, no classic BT
    ble.gap().setAdvertisingType(GapAdvertisingParams::ADV_CONNECTABLE_UNDIRECTED); // advertising type
    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::COMPLETE_LOCAL_NAME, (uint8_t *)DEVICE_NAME, sizeof(DEVICE_NAME)); // add name
    ble.gap().accumulateAdvertisingPayload(GapAdvertisingData::COMPLETE_LIST_16BIT_SERVICE_IDS, (uint8_t *)uuid16_list, sizeof(uuid16_list)); // UUID's broadcast in advertising packet
    ble.gap().setAdvertisingInterval(100); // 100ms.

    /* Add our custom service */
    ble.addService(distanceService_1);
    ble.addService(distanceService_2);
    ble.addService(pirService_1);
    ble.addService(pirService_2);

    /* Start advertising */
    ble.gap().startAdvertising();
}

int main()
{
    BLE& ble = BLE::Instance(BLE::DEFAULT_INSTANCE);
    ble.init(bleInitComplete);

    /* SpinWait for initialization to complete. This is necessary because the
     * BLE object is used in the main loop below. */
    while (ble.hasInitialized()  == false) { /* spin loop */ }

    //init tof sensors
    tof_sensor_1.init_sensor(tof_address_1);
    tof_sensor_2.init_sensor(tof_address_2);

    int pir_on = 0;
    uint32_t distance1;
    uint32_t distance2;
    printf("\n\r********* Device ready!*********\n\r");

    while(1) {
        uint8_t distance_boolean;
        led=!led;

        tof_sensor_1.get_distance(&distance1);
        tof_sensor_2.get_distance(&distance2);
        printf("Distance: %d, %d\r\n", distance1, distance2);

        //        get string and send via bluetooth
        distance_boolean = distance1>DISTANCE_MIN && distance1<DISTANCE_MAX? 1 : 0;
        memcpy(&readValue, &distance_boolean, sizeof(distance_boolean));
        ble.updateCharacteristicValue(distanceChar_1.getValueHandle(), &readValue, sizeof(readValue));

        distance_boolean = distance2>DISTANCE_MIN && distance2<DISTANCE_MAX? 1 : 0;
        memcpy(&readValue, &distance_boolean, sizeof(distance_boolean));
        ble.updateCharacteristicValue(distanceChar_2.getValueHandle(), &readValue, sizeof(readValue));

        //set pir value
        if(pir_1==0x00) {
            printf("PIR 1 off!\t");
            pir_on = 0;
        } else {
            printf("PIR 1 on!\t");
            pir_on = 1;
        }
        memcpy(&readValue, &pir_on, sizeof(pir_on));
        ble.updateCharacteristicValue(pirChar_1.getValueHandle(), &readValue, sizeof(readValue));

        //set pir value
        if(pir_2==0x00) {
            printf("PIR 2 off!\r\n");
            pir_on = 0;
        } else {
            printf("PIR 2 on!\r\n");
            pir_on = 1;
        }
        memcpy(&readValue, &pir_on, sizeof(pir_on));
        ble.updateCharacteristicValue(pirChar_2.getValueHandle(), &readValue, sizeof(readValue));

        //wait
        wait(0.005);
    }
}