#include <Arduino.h>
#include "stepper/Stepper.h"

Stepper moto_a(8, 9, 400, 60);
int m = 4;
int direction = -1;
void setup() {
    Serial.begin(115200);
}

void loop() {
    direction = -direction;
    moto_a.run(direction * 40 * m);
    delay(3000);
}
