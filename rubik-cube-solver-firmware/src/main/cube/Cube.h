/*
 * Created on: 2018年8月18日
 * Author: richard.zhao
 */
#ifndef SRC_MAIN_CUBE_CUBE_H_
#define SRC_MAIN_CUBE_CUBE_H_

#include "../stepper/Stepper.h"

class Cube {
public:
    /// Constructor
    /// \param[in] pushStepperStepPin push stepper的控制转动Arduino pin
    /// \param[in] pushStepperDirPin  push stepper的控制方向Arduino pin
    /// \param[in] rotateStepperStepPin rotate stepper的控制转动Arduino pin
    /// \param[in] rotateStepperDirPin rotate stepper的控制方向Arduino pin
    /// \param[in] stepperSteps 电机转一周的步数
    /// \param[in] stepperSpeed 电机的转速,RPM
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

    /// 右面顺时针转90度,R
    void R();
    /// 右面转180度,R2
    void R2();
    /// 右面逆时针转90度,R'
    void R_();

    /// 前面顺时针转90度
    void FF(); //F is used by arduino
    /// 前面转180度
    void FF2();
    /// 前面逆时针转90度
    void FF_();

    void B();
    void B2();
    void B_();

    virtual ~Cube();

private:
    /// stepper to push the cube
    Stepper *_pushStepper;
    /// stepper to rotate the cube
    Stepper *_rotateStepper;
    /// start position of push, in steps
    int _pushPositionStart = 0;
    /// end position of push, in steps
    int _pushPositionEnd = 0;
    /// hold position, in steps
    int _pushPositionHold = 0;
    /// 转90度的步数
    int _rotatePosition = 0;

    /// 将角度位置转换为步数
    void degreeToPosition();
    /// hold
    void hold();
    /// release
    void release();
    /// 转动
    /// \param[in] degree 转动的度数,顺时针为正/逆时针为负
    void rotate(int degree);

    ////
    void sleep();

    typedef enum {
        START = 0, // 起始位置
        END = 0,   // 结束位置
        HOLD = 0,   // 抓握位置
    } _pushPositionInDegree;
    ///
    const int SLEEPTIME = 10;
};

#endif /* SRC_MAIN_CUBE_CUBE_H_ */
