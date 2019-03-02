#include "mbed.h"
// using VL53L0X distance sensor
#include "VL53L0X.h"
#define VL53L0_I2C_SCL   p16
#define SHUTDOWN_PIN   p15
#define VL53L0_I2C_SDA   p17 

// USB output
Serial pc(USBTX, USBRX);

//Sensor
static DevI2C devI2c(VL53L0_I2C_SDA, VL53L0_I2C_SCL); 

// LEDs
DigitalOut led1(LED1,1);
DigitalOut led2(LED2,1);
 
int main()
{   
    // setup the sensor
    static DigitalOut shutdown_pin(SHUTDOWN_PIN);
    static VL53L0X range(&devI2c, &shutdown_pin, NC);
    range.init_sensor(0x29);    //default address
    
    uint32_t distance;
    int status;
    
    while(1){
        status = range.get_distance(&distance);
        if (status == VL53L0X_ERROR_NONE) {
            led1 = 0;   // led = 0 means led on
        } else {
            led2 = 0;
        }        
        pc.printf("distance: %d\t", distance);
        wait(0.5);                 
    }
}
 
