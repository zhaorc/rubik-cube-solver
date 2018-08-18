/*
 * Cube.cpp
 *
 *  Created on: 2018年8月18日
 *      Author: richard.zhao
 */

#include<Arduino.h>
#include "Cube.h"

Cube::Cube(int pushStepperStepPin, int pushStepperDirPin, int rotateStepperStepPin, int rotateStepperDirPin, int stepperSteps, int stepperSpeed) {
    this->_pushStepper = &Stepper(pushStepperStepPin, pushStepperDirPin, stepperSteps, stepperSpeed);
    this->_rotateStepper = &Stepper(rotateStepperStepPin, rotateStepperDirPin, stepperSteps, stepperSpeed);
}

void Cube::x() {
    this->_pushStepper->runTo(this->_pushPositionEnd);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionStart);
}

void Cube::x2() {
    this->_pushStepper->runTo(this->_pushPositionEnd);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionHold);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionEnd);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionStart);
}

void Cube::x3() {
    this->_pushStepper->runTo(this->_pushPositionEnd);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionHold);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionEnd);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionHold);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionEnd);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionStart);
}

void Cube::y() {
    this->rotate(90);
}
void Cube::y2() {
    this->rotate(180);
}
void Cube::y_() {
    this->rotate(-90);
}

void Cube::U() {
    this->x2();
    this->hold();
    this->rotate(-90);
    this->x2();
}
void Cube::U2() {
    this->x2();
    this->hold();
    this->rotate(180);
    this->x2();
}
void Cube::U_() {
    this->x2();
    this->hold();
    this->rotate(90);
    this->x2();
}

void Cube::D() {
    this->hold();
    this->rotate(-90);
    this->release();
}
void Cube::D2() {
    this->hold();
    this->rotate(180);
    this->release();
}
void Cube::D_() {
    this->hold();
    this->rotate(90);
    this->release();
}

void Cube::L() {
    this->rotate(90);
    this->x();
    this->hold();
    this->rotate(90);
    this->x3();
    this->rotate(-90);
}

void Cube::L2() {
    this->rotate(90);
    this->x();
    this->hold();
    this->rotate(180);
    this->x3();
    this->rotate(-90);
}

void Cube::L_() {
    this->rotate(90);
    this->x();
    this->hold();
    this->rotate(-90);
    this->x3();
    this->rotate(-90);
}

void Cube::R() {
    this->rotate(-90);
    this->x();
    this->hold();
    this->rotate(-90);
    this->x3();
    this->rotate(90);
}

void Cube::R2() {
    this->rotate(-90);
    this->x();
    this->hold();
    this->rotate(180);
    this->x3();
    this->rotate(90);
}

void Cube::R_() {
    this->rotate(-90);
    this->x();
    this->hold();
    this->rotate(90);
    this->x3();
    this->rotate(90);
}

void Cube::FF() {
    this->x3();
    this->hold();
    this->rotate(-90);
    this->x();
}
void Cube::FF2() {
    this->x3();
    this->hold();
    this->rotate(180);
    this->x();
}

void Cube::FF_() {
    this->x3();
    this->hold();
    this->rotate(90);
    this->x();
}

void Cube::B() {
    this->x();
    this->hold();
    this->rotate(90);
    this->x3();
}
void Cube::B2() {
    this->x();
    this->hold();
    this->rotate(180);
    this->x3();
}
void Cube::B_() {
    this->x();
    this->hold();
    this->rotate(-90);
    this->x3();
}

void Cube::degreeToPosition() {
    int steps = this->_pushStepper->getSteps();
    this->_pushPositionEnd = (steps * ((float) this->PUSH_END / 360.0));
    this->_pushPositionHold = (steps * ((float) this->PUSH_HOLD / 360.0));
    //this->_rotatePosition = (steps / 4);
}

void Cube::hold() {
    this->_pushStepper->runTo(this->_pushPositionHold);
}

void Cube::release() {
    this->_pushStepper->runTo(this->_pushPositionStart);
}

void Cube::rotate(int degree) {
    long steps = this->_rotateStepper->getSteps();
    int fix = degree > 0 ? this->ROTATE_FIX : -this->ROTATE_FIX;
    this->_rotateStepper->run(steps * ((float) (degree + fix) / 360.0));
    this->sleep();
    this->_rotateStepper->run(steps * ((float) (-fix) / 360.0));
}

void Cube::sleep() {
    delay(this->SLEEPTIME);
}

Cube::~Cube() {
}

