#include "mbed.h"
#include "stats_report.h"

DigitalOut led1(LED1);
DigitalOut led2(LED2);
DigitalOut led3(LED3);
DigitalOut led4(LED4);


#define SLEEP_TIME                  1000 // (msec)
#define SHORT_SLEEP					250
#define PRINT_AFTER_N_LOOPS         20

int main()
{

    int count = 0;
    int iLoopCount = 0;
    int iLoopMax = 4;
    while (true) {
    	if (count == 5) 
    		count = 0;
		switch (count) {
			case 0:  
				for(iLoopCount = 0; iLoopCount < iLoopMax; iLoopCount++){
					led1=!led1;
	  			 	led2=!led2;
	  			 	led3=!led3;
	  			 	led4=!led4;
	  			 	wait_ms(SHORT_SLEEP);
				}
				break;
			case 1:
				 led2=!led2;
	  			 led3=!led3;
	  			 led4=!led4;
				 wait_ms(SLEEP_TIME);
				 break;
			case 2: 
				 led1=!led1;
				 led2=!led2;
				 wait_ms(SLEEP_TIME);
				 break;
			case 3:
				 led2=!led2;
				 led3=!led3;
				 wait_ms(SLEEP_TIME);
				 break;
			case 4:
				 led3=!led3;
				 led4=!led4;
				 wait_ms(SLEEP_TIME);
				 led1=!led1;
	  			 led2=!led2;
	  			 led3=!led3;
				 break;
		}	
        count++;
    }
}

