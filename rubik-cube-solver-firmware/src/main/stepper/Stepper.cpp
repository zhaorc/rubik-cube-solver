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
    this->_steps = steps;
    this->_speed = speed;
    this->_interval = 60000000 / (long) speed / (long) steps / 2; //30000000L / (long) speed / (long) steps;
    pinMode(this->_stepPin, OUTPUT);
    pinMode(this->_dirPin, OUTPUT);
}


int Stepper::getSteps() {
    return this->_steps;
}

int Stepper::getPosition() {
    return this->_position;
}

void Stepper::run(long steps) {
//    Serial.print("speed=");
//    Serial.println(this->_speed);
//    Serial.print("interval=");
//    Serial.println(this->_interval);
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

void Stepper::runTo(long position) {
    this->run(position - this->_position);
}

void Stepper::runDegree(float degree) {
    long steps = this->_steps * degree / 360.0;
    this->run(steps);
}

Stepper::~Stepper() {
}

