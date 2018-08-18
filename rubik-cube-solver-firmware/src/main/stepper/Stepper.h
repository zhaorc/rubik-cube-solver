/*
 * Stepper.h
 *
 *  Created on: 2018年8月6日
 *      Author: richard.zhao
 */
/**
 *   1  on  on  on
 *   2  off on  on
 *   4  on  off on
 *   8  off off on
 *  16  on  on  off
 *  32  off on  off
 *  64  on  off off
 * 128  off off off
 */
#ifndef SRC_MAIN_STEPPER_STEPPER_H_
#define SRC_MAIN_STEPPER_STEPPER_H_

class Stepper {
public:
    /// Constructor
    /// \param[in] stepPin Arduino pin to controll step
    /// \param[in] dirPin Arduino pin to controll direction
    /// \param[in] steps one round's steps
    /// \param[in] speed speed in RPM
    Stepper(int stepPin, int dirPin, int steps, int speed);
    unsigned long getPosition();
    void run(long steps);
    virtual ~Stepper();

private:
    int _stepPin;
    int _dirPin;
    unsigned long _speed = 60; // in rpm
    unsigned long _interval = 0;
    unsigned long _position = 0;
};

#endif /* SRC_MAIN_STEPPER_STEPPER_H_ */
