/*
 * Cube.cpp
 *
 *  Created on: 2018年8月18日
 *      Author: richard.zhao
 */

#include "Cube.h"

Cube::Cube(int pushStepperStepPin, int pushStepperDirPin, int rotateStepperStepPin, int rotateStepperDirPin, int stepperSteps, int stepperSpeed) {
    this->pushStepper = &Stepper(pushStepperStepPin, pushStepperDirPin, stepperSteps, stepperSpeed);
    this->rotateStepper = &Stepper(rotateStepperStepPin, rotateStepperDirPin, stepperSteps, stepperSpeed);
}

void Cube::x() {
    this->pushStepper->run(100);
}

void Cube::x2() {

}

void Cube::x3() {

}

void Cube::y() {

}
void Cube::y2() {

}
void Cube::y_() {

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
}
void Cube::D2() {
    this->hold();
    this->rotate(180);
}
void Cube::D_() {
    this->hold();
    this->rotate(90);
}

void Cube::L() {
    this->y();
    this->x();
    this->hold();
    this->rotate(90);
    this->x3();
    this->rotate(-90);
}

void Cube::L2() {
    this->y();
    this->x();
    this->hold();
    this->y2();
    this->x3();
    this->y_();
}

void Cube::L_() {
    this->y();
    this->x();
    this->hold();
    this->y_();
    this->x3();
    this->y_();
}

void Cube::hold() {

}
void Cube::rotate(int degree) {

}

Cube::~Cube() {
}

