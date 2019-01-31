#include <nrf51.h>

// Define pins for buttons and LEDs
#define BTN_1 17
#define BTN_2 18
#define BTN_3 19
#define BTN_4 20
#define LED_1 21
#define LED_2 22
#define LED_3 23
#define LED_4 24

// Set direction of GPIO 17 & 18 to input
void init_buttons(void) {
	NRF_GPIO->PIN_CNF[BTN_1] = 0x0C;
	NRF_GPIO->PIN_CNF[BTN_2] = 0x0C;
	NRF_GPIO->PIN_CNF[BTN_3] = 0x0C;
	NRF_GPIO->PIN_CNF[BTN_4] = 0x0C;
	NRF_GPIO->DIRSET &= ~(0x1 << (BTN_1)) | ~(0x1 << (BTN_2)) | ~(0x1 << (BTN_3)) | ~(0x1 << (BTN_4));
}

// Set direction of GPIO 21 & 22 to output
void init_leds(void) {
	NRF_GPIO->DIRSET |= (0x1 <<(LED_1)) | (0x1 <<(LED_2)) | (0x1 <<(LED_3)) | (0x1 <<(LED_4));
}

// Set the output pin 21 to low (LEDs are active low)
void led1_on(void) {
	NRF_GPIO->OUTCLR &= (0x1 <<(LED_1));
}

// Set the output pin 22 to low
void led2_on(void) {
	NRF_GPIO->OUTCLR &= (0x1 <<(LED_2));
}

// Set the output pin 22 to low
void led3_on(void) {
	NRF_GPIO->OUTCLR &= (0x1 <<(LED_3));
}

// Set the output pin 22 to low
void led4_on(void) {
	NRF_GPIO->OUTCLR &= (0x1 <<(LED_4));
}

// Set output pins 21 & 22 to hign
void led_off(void) {
	NRF_GPIO->OUTSET |= (0x1 <<(LED_1));
	NRF_GPIO->OUTSET |= (0x1 <<(LED_2));
	NRF_GPIO->OUTSET |= (0x1 <<(LED_3));
	NRF_GPIO->OUTSET |= (0x1 <<(LED_4));
}

/*----------------------------------------------------------------------------
 MAIN function
 *----------------------------------------------------------------------------*/

int main() {
	// Initialise LEDs and buttons
	init_leds();
	init_buttons();
	
	while(1){
		// If button 1 is pressed turn on LED 1
		if(!(NRF_GPIO->IN >> BTN_1 & 0x1))
			led1_on();
		// If button 2 is pressed turn on LED 2
		if(!(NRF_GPIO->IN >> BTN_2 & 0x1))
			led2_on();
		// If button 3 is pressed turn on LED 2
		if(!(NRF_GPIO->IN >> BTN_3 & 0x1))
			led3_on();
		// If button 4 is pressed turn on LED 2
		if(!(NRF_GPIO->IN >> BTN_4 & 0x1))
			led4_on();
		// Turn off the LEDs
		led_off();
	}
}
