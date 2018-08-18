///  Created on: 2018年8月6日
///  Author: richard.zhao

///   1   on   on   on
///   2   on   on  off
///   4   on  off   on
///   8   on  off  off
///  16  off   on   on
///  32  off   on  off
///  64  off  off   on
/// 128  off  off  off x
#ifndef SRC_MAIN_STEPPER_STEPPER_H_
#define SRC_MAIN_STEPPER_STEPPER_H_

class Stepper {
public:
    /// Constructor
    /// \param[in] stepPin 控制转动的Arduino pin
    /// \param[in] dirPin 控制方向的Arduino pin
    /// \param[in] steps 电机一周的步数
    /// \param[in] speed 转速RPM
    Stepper(int stepPin, int dirPin, int steps, int speed);
    /// 获取电机的一周转数
    int getSteps();
    int getPosition();
    /// 转动指定的步数
    /// \param[in] steps 转动的步数
    void run(long steps);
    /// 转到某个位置
    /// \param[in] position 目标位置
    void runTo(long position);
    /// 转动某个角度
    void runDegree(float degree);

    virtual ~Stepper();

private:
    int _stepPin;
    int _dirPin;
    unsigned long _speed = 60; // in rpm
    /// 电机一周的步数
    int _steps = 0;
    /// 脉冲周期
    unsigned long _interval = 0;
    long _position = 0;
};

#endif /* SRC_MAIN_STEPPER_STEPPER_H_ */
