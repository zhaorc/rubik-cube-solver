#include <Arduino.h>
#include "cube/Cube.h"

Stepper push_stepper(8, 9, 6400, 30);
Stepper rotate_stepper(10, 11, 6400, 30);
Cube *cube;
int direction = -1;
void setup() {
    Serial.begin(115200);
    push_stepper.runDegree(5);
    cube = new Cube(&push_stepper, &rotate_stepper);
}

void loop() {
    delay(3000);
    cube->x3();
//    cube->hold();
//    delay(3000);
//    cube->release();
}
