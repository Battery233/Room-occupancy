/* mbed Microcontroller Library
 * Copyright (c) 2006-2013 ARM Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
#ifndef __BLE_PIR_SERVICE_H__
#define __BLE_PIR_SERVICE_H__
 
class PirService {
public:
    const static uint16_t BUTTON_SERVICE_UUID              = 0xA000;
    const static uint16_t BUTTON_STATE_CHARACTERISTIC_UUID = 0xA001;
 
    PirService(BLE &_ble, bool buttonPressedInitial) :
        ble(_ble), PirState(BUTTON_STATE_CHARACTERISTIC_UUID, &buttonPressedInitial, GattCharacteristic::BLE_GATT_CHAR_PROPERTIES_NOTIFY)
    {
        GattCharacteristic *charTable[] = {&PirState};
        GattService PirService(PirService::BUTTON_SERVICE_UUID, charTable, sizeof(charTable) / sizeof(GattCharacteristic *));
        ble.gattServer().addService(PirService);
    }
 
    void updatePirState(bool newState) {
        ble.gattServer().write(PirState.getValueHandle(), (uint8_t *)&newState, sizeof(bool));
    }
 
private:
    BLE                              &ble;
    ReadOnlyGattCharacteristic<bool>  PirState;
};
 
#endif /* #ifndef __BLE_PIR_SERVICE_H__ */