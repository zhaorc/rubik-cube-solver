/*
 * Created on: 2018年8月18日
 * Author: richard.zhao
 */
#ifndef SRC_MAIN_CUBE_CUBE_H_
#define SRC_MAIN_CUBE_CUBE_H_

#include "../stepper/Stepper.h"

class Cube {
public:

    Cube(Stepper *pushStepper, Stepper *rotateStepper);

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
    void FF(); // F used by arduino
    /// 前面转180度
    void FF2();
    /// 前面逆时针转90度
    void FF_();

    void B();
    void B2();
    void B_();

    /// hold
    void hold();
    /// release
    void release();

    virtual ~Cube();

private:
    /// stepper to push the cube
    Stepper* _pushStepper;
    /// stepper to rotate the cube
    Stepper* _rotateStepper;
    /// start position of push, in steps
    int _pushPositionStart = 0;
    /// end position of push, in steps
    int _pushPositionEnd = 0;
    /// hold position, in steps
    int _pushPositionHold = 0;

    /// 将角度位置转换为步数
    void degreeToPosition();
    /// 转动
    /// \param[in] degree 转动的度数,顺时针为正/逆时针为负
    void rotate(int degree);

    ////
    void sleep();

    const float PUSH_START = 0; // 起始位置
    const float PUSH_HOLD = 35;   // 抓握位置
//    const float PUSH_END = 77.4293;   // 结束位置
    const float PUSH_END = 90;   // 结束位置
    const float ROTATE_FIX = 0;  // 转动角度的修正值.由于底座挡板与魔方之间有间隙,需引入修正值修正转动的角度
    ///
    const int SLEEPTIME = 10;
};

#endif /* SRC_MAIN_CUBE_CUBE_H_ */
