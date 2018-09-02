/*
 * Cube.cpp
 *
 *  Created on: 2018年8月18日
 *      Author: richard.zhao
 */

#include<Arduino.h>
#include "Cube.h"
#include "../stepper/Stepper.h"

Cube::Cube(Stepper *pushStepper, Stepper *rotateStepper) {
    this->_pushStepper = pushStepper;
    this->_rotateStepper = rotateStepper;
    this->degreeToPosition();
}

void Cube::x() {
    this->_pushStepper->runTo(this->_pushPositionEnd);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionStart);
    this->sleep();
}

void Cube::x2() {
    this->_pushStepper->runTo(this->_pushPositionEnd);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionHold);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionEnd);
    this->sleep();
    this->_pushStepper->runTo(this->_pushPositionStart);
    this->sleep();
}

void Cube::x_() {
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
    this->sleep();
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
    this->rotate(90);
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
    this->rotate(-90);
    this->x2();
}

void Cube::D() {
    this->hold();
    this->rotate(90);
    this->release();
}
void Cube::D2() {
    this->hold();
    this->rotate(180);
    this->release();
}
void Cube::D_() {
    this->hold();
    this->rotate(-90);
    this->release();
}

void Cube::L() {
    this->rotate(-90);
    this->x();
    this->hold();
    this->rotate(90);
    this->x_();
    this->rotate(90);
}

void Cube::L2() {
    this->rotate(-90);
    this->x();
    this->hold();
    this->rotate(180);
    this->x_();
    this->rotate(90);
}

void Cube::L_() {
    this->rotate(-90);
    this->x();
    this->hold();
    this->rotate(-90);
    this->x_();
    this->rotate(90);
}

void Cube::R() {
    this->rotate(90);
    this->x();
    this->hold();
    this->rotate(90);
    this->x_();
    this->rotate(-90);
}

void Cube::R2() {
    this->rotate(90);
    this->x();
    this->hold();
    this->rotate(180);
    this->x_();
    this->rotate(-90);
}

void Cube::R_() {
    this->rotate(90);
    this->x();
    this->hold();
    this->rotate(-90);
    this->x_();
    this->rotate(-90);
}

void Cube::FF() {
    this->x_();
    this->hold();
    this->rotate(90);
    this->x();
}
void Cube::FF2() {
    this->x_();
    this->hold();
    this->rotate(180);
    this->x();
}

void Cube::FF_() {
    this->x_();
    this->hold();
    this->rotate(-90);
    this->x();
}

void Cube::B() {
    this->x();
    this->hold();
    this->rotate(90);
    this->x_();
}
void Cube::B2() {
    this->x();
    this->hold();
    this->rotate(180);
    this->x_();
}
void Cube::B_() {
    this->x();
    this->hold();
    this->rotate(-90);
    this->x_();
}

void Cube::degreeToPosition() {
    int steps = this->_pushStepper->getSteps();
    this->_pushPositionStart = this->_pushStepper->getPosition(); // + (steps * (5.0 / 360.0));
    this->_pushPositionHold = (steps * ((float) this->PUSH_HOLD / 360.0));
    this->_pushPositionEnd = (steps * ((float) this->PUSH_END / 360.0));
}

void Cube::hold() {
    this->_pushStepper->runTo(this->_pushPositionHold);
    this->sleep();
    this->_isHold = true;
}

void Cube::release() {
    this->_pushStepper->runTo(this->_pushPositionStart);
    this->sleep();
    this->_isHold = false;
}

void Cube::rotate(int degree) {
    long steps = this->_rotateStepper->getSteps();
    if (this->_isHold) {
        int fix = degree > 0 ? this->ROTATE_FIX_CLOCKWISE : -this->ROTATE_FIX_COUNTERCLOCKWISE;
        this->_rotateStepper->run(steps * ((float) (degree + fix) / 360.0));
        this->sleep();
        this->_rotateStepper->run(steps * ((float) (-fix) / 360.0));
        this->sleep();
    } else {
        this->_rotateStepper->run(steps * ((float) (degree) / 360.0));
        this->sleep();
    }
    this->sleep();
    this->_isHold = false;
}

void Cube::sleep() {
    delay(this->SLEEPTIME);
}

Cube::~Cube() {
}

