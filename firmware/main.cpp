#include "mbed.h"
#include "vl53l0x_api.h"
#include "vl53l0x_platform.h"
#include "vl53l0x_i2c_platform.h"
#include "tof.h"

#include "ble/BLE.h"

#define USE_I2C_2V8

// IMPORTAINT used with library: https://codeload.github.com/ARMmbed/VL53L0X-mbedOS/zip/master
// Change the first line after includes in file vl53l0x_i2c_platform.cpp to:
// I2C i2c(p30, p7);

Serial pc(USBTX, USBRX);
DigitalOut led(LED1);

#define PIR_PIN_1 p14
DigitalIn pir_1(p14, PullNone);
#define PIR_PIN_2 p15
DigitalIn pir_2(p15, PullNone);

uint16_t disServiceUUID  = 0xAB00;
uint16_t distanceCharUUID      = 0xAB01;
uint16_t pirServiceUUID_1      = 0xAB10;
uint16_t pirCharUUID_1      = 0xAB11;
uint16_t pirServiceUUID_2      = 0xAB20;
uint16_t pirCharUUID_2      = 0xAB21;

const static char     DEVICE_NAME[]        = "OMG"; // change this
static const uint16_t uuid16_list[]        = {0xFFFF}; //Custom UUID, FFFF is reserved for development

// payload
static uint8_t readValue = 0;

ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(readValue)> distanceChar(distanceCharUUID, &readValue,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ |
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY );

ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(readValue)> pirChar_1(pirCharUUID_1, &readValue,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ |
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY );

ReadOnlyArrayGattCharacteristic<uint8_t, sizeof(readValue)> pirChar_2(pirCharUUID_2, &readValue,
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_READ |
        GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY );

/* Set up custom service */
GattCharacteristic *characteristics[] = {&distanceChar};
GattService distanceService(disServiceUUID, characteristics, sizeof(characteristics) / sizeof(GattCharacteristic *));
GattCharacteristic *characteristics_2[] = {&pirChar_1};
GattService pirService_1(pirServiceUUID_1, characteristics_2, sizeof(characteristics_2) / sizeof(GattCharacteristic *));
GattCharacteristic *characteristics_3[] = {&pirChar_2};
GattService pirService_2(pirServiceUUID_2, characteristics_3, sizeof(characteristics_3) / sizeof(GattCharacteristic *));
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
    ble.addService(distanceService);
    ble.addService(pirService_1);
    ble.addService(pirService_2);

    /* Start advertising */
    ble.gap().startAdvertising();
}

int main()
{

    /* initialize stuff */
    printf("\n\r********* Start device*********\n\r");

    BLE& ble = BLE::Instance(BLE::DEFAULT_INSTANCE);
    ble.init(bleInitComplete);

    /* SpinWait for initialization to complete. This is necessary because the
     * BLE object is used in the main loop below. */
    while (ble.hasInitialized()  == false) { /* spin loop */ }

    int measure=0;
    VL53L0X_Dev_t MyDevice;
    VL53L0X_Dev_t *pMyDevice = &MyDevice;
    VL53L0X_RangingMeasurementData_t    RangingMeasurementData;
    VL53L0X_RangingMeasurementData_t   *pRangingMeasurementData    = &RangingMeasurementData;
//    VL53L0X_Version_t                   Version;

    // Initialize Comms
    pMyDevice->I2cDevAddr      = 0x52;
    pMyDevice->comms_type      =  1;
    pMyDevice->comms_speed_khz =  400;

//    VL53L0X_ERROR_CONTROL_INTERFACE;
    VL53L0X_RdWord(&MyDevice, VL53L0X_REG_OSC_CALIBRATE_VAL,0);
    VL53L0X_DataInit(&MyDevice); // Data initialization
//    VL53L0X_Error Status = VL53L0X_ERROR_NONE;
    uint32_t refSpadCount;
    uint8_t isApertureSpads;
    uint8_t VhvSettings;
    uint8_t PhaseCal;

    VL53L0X_StaticInit(pMyDevice); // Device Initialization
    VL53L0X_PerformRefSpadManagement(pMyDevice, &refSpadCount, &isApertureSpads); // Device Initialization
    VL53L0X_PerformRefCalibration(pMyDevice, &VhvSettings, &PhaseCal); // Device Initialization
    VL53L0X_SetDeviceMode(pMyDevice, VL53L0X_DEVICEMODE_CONTINUOUS_RANGING); // Setup in single ranging mode
    VL53L0X_SetLimitCheckValue(pMyDevice, VL53L0X_CHECKENABLE_SIGNAL_RATE_FINAL_RANGE, (FixPoint1616_t)(0.25*65536)); //High Accuracy mode, see API PDF
    VL53L0X_SetLimitCheckValue(pMyDevice, VL53L0X_CHECKENABLE_SIGMA_FINAL_RANGE, (FixPoint1616_t)(18*65536)); //High Accuracy mode, see API PDF
    VL53L0X_SetMeasurementTimingBudgetMicroSeconds(pMyDevice, 200000); //High Accuracy mode, see API PDF
    VL53L0X_StartMeasurement(pMyDevice);

    int pir_on = 0;

    while(1) {

        //ToF sensor
        WaitMeasurementDataReady(pMyDevice);
        VL53L0X_GetRangingMeasurementData(pMyDevice, pRangingMeasurementData);
        measure=pRangingMeasurementData->RangeMilliMeter;
        printf("In loop measurement %d\r\n", measure);
        led=!led;
        // Clear the interrupt
        VL53L0X_ClearInterruptMask(pMyDevice, VL53L0X_REG_SYSTEM_INTERRUPT_GPIO_NEW_SAMPLE_READY);
        VL53L0X_PollingDelay(pMyDevice);

        uint8_t distance_boolean = measure>10 && measure<500? 1 : 0;

        //get string and send via bluetooth
        memcpy(&readValue, &distance_boolean, sizeof(distance_boolean));
        ble.updateCharacteristicValue(distanceChar.getValueHandle(), &readValue, sizeof(readValue));

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
        wait(0.01);
    }
    //VL53L0X_StopMeasurement(pMyDevice);
//    WaitStopCompleted(pMyDevice);
//    VL53L0X_ClearInterruptMask(pMyDevice,VL53L0X_REG_SYSTEM_INTERRUPT_GPIO_NEW_SAMPLE_READY);
}