/*
 * Cube.h
 *
 *  Created on: 2018年8月18日
 *      Author: richard.zhao
 */

#ifndef SRC_MAIN_CUBE_CUBE_H_
#define SRC_MAIN_CUBE_CUBE_H_

#include "../stepper/Stepper.h"

class Cube {
public:
    /// Constructor
    /// \param[in] pushStepperStepPin step pin of push stepper
    /// \param[in] pushStepperDirPin  direction pin of push stepper
    /// \param[in] rotateStepperStepPin step pin of rotate stepper
    /// \param[in] rotateStepperDirPin direction pin of rotate stepper
    /// \param[in] stepperSteps stepper's steps per round
    /// \param[in] stepperSpeed stepper's speed in RPM
    Cube(int pushStepperStepPin, int pushStepperDirPin, int rotateStepperStepPin, int rotateStepperDirPin, int stepperSteps, int stepperSpeed);

    /// 整体做1次R方向的90度转动,x
    void x();
    /// 整体做2次R方向的90度转动,xx
    void x2();
    /// 整体做3次R方向的90度转动,xxx
    void x3();
    /// 整体做1次U方向的90度转动,y
    void y();
    /// 整体做1次U方向的180度转动,y2
    void y2();
    /// 整体做1次U'方向的90度转动,y'
    void y_();


    /// 顶层顺时针转90度,U
    void U();
    /// 顶层转180度,U2
    void U2();
    /// 顶层逆时针转90度,U'
    void U_();

    /// 底层顺时针转90度,D
    void D();
    /// 底层转180度,D2
    void D2();
    /// 底层逆时针转90度,D'
    void D_();

    /// 左面顺时针转90度,L
    void L();
    /// 左面转180度,L2
    void L2();
    /// 左面逆时针转90度,L'
    void L_();

    virtual ~Cube();

private:
    /// stepper to push the cube
    Stepper *pushStepper;
    /// stepper to rotate the cube
    Stepper *rotateStepper;
    /// position of push start, in steps
    int pushPositionStart = 0;
    /// position of push end, in steps
    int pushPositionEnd = 0;
    /// position of hold, in steps
    int holdPosition = 0;

    void hold();
    /// 转动
    /// \param[in] degree 转动的度数,顺时针为正/逆时针为负
    void rotate(int degree);

    typedef enum {
        START = 0, // 起始位置
        END = 0,   // 结束位置
        HOLD = 0,   // 抓握位置
    } _pushPositionInDegree;

};

#endif /* SRC_MAIN_CUBE_CUBE_H_ */
