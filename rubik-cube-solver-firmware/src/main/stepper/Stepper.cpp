/*
 * Stepper.cpp
 *
 *  Created on: 2018年8月6日
 *      Author: richard.zhao
 */
#include <Arduino.h>
#include "Stepper.h"

Stepper::Stepper(int stepPin, int dirPin, int steps, int speed) {
    this->_stepPin = stepPin;
    this->_dirPin = dirPin;
    this->_interval = 30000000L / (long) speed;
    this->_speed = speed;
    pinMode(this->_stepPin, OUTPUT);
    pinMode(this->_dirPin, OUTPUT);


}

//void Stepper::setSpeed(unsigned long speed) {
//    this->_speed = speed;
//    this->_interval = 1000000L / this->_speed / 2;
//}

unsigned long Stepper::getPosition() {
    return this->_position;
}

void Stepper::run(long steps) {
//    Serial.print("speed=");
//    Serial.println(this->_speed);
//
//    Serial.print("steps=");
//    Serial.println(steps);

    if (!steps) {
        return;
    }
    if (!this->_speed || !this->_interval) {
        return;
    }
    digitalWrite(this->_dirPin, steps > 0 ? HIGH : LOW);
    unsigned long position = abs(steps);
    for (; position > 0; position--) {
        digitalWrite(this->_stepPin, HIGH);
        delayMicroseconds(this->_interval);
        digitalWrite(this->_stepPin, LOW);
        delayMicroseconds(this->_interval);
    }
    this->_position += steps;
}

Stepper::~Stepper() {
}

