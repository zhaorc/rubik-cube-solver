#include <Arduino.h>
#include<SoftwareSerial.h>
#include "cube/Cube.h"

#define STATE_READ_MOVES 1
#define STATE_SOLVE        2
#define STATE_SOLVE_FINISH       3

bool testMode = false;
SoftwareSerial bluetooth(10, 11);
Stepper push_stepper(4, 5, 6400, 30);
Stepper rotate_stepper(6, 7, 6400, 60);
Cube *cube;
char buf[121];
int data_size = 0;
int ptr = 0;
int state = 0;

void readMoves();
void solveCube();

void setup() {
    delay(3000);
    Serial.begin(9600);
    bluetooth.begin(9600);
    cube = new Cube(&push_stepper, &rotate_stepper);
    delay(1000);
    cube->hold();
    cube->release();
    state = STATE_READ_MOVES;
}

void loop() {
    switch (state) {
    case STATE_READ_MOVES:
        readMoves();
        break;
    case STATE_SOLVE:
        solveCube();
        break;
    case STATE_SOLVE_FINISH:
        cube->release();
        bluetooth.print("DONE");
        bluetooth.flush();
        state = STATE_READ_MOVES;
        break;
    }
}

void readMoves() {
    data_size = 0;
    int data = 0;
    int read_length = 0;
    // first 4 bytes are length
    // followed by data
    ////////
    if (testMode) {
        while (Serial.available() < 4) {
            // do nothing
        }
        data = Serial.read();
        data_size += ((data - '0') * 1000);
        data = Serial.read();
        data_size += ((data - '0') * 100);
        data = Serial.read();
        data_size += ((data - '0') * 10);
        data = Serial.read();
        data_size += (data - '0');
        //XXX
        Serial.print("data_size=");
        Serial.println(data_size);
        while (true) {
            if (Serial.available()) {
                data = Serial.read();
                buf[read_length++] = data;
            }
            if (read_length == data_size) {
                break;
            }
            //Serial.print("read_length=");
            //Serial.println(read_length);
        }
//        while (Serial.available() < data_size) {
//            //do nothing
//        }
//        for (int i = 0; i < data_size; i++) {
//            data = Serial.read();
//            buf[read_length++] = data;
//        }
    }
    else {
        while (bluetooth.available() < 4) {
            // do nothing
        }
        data = bluetooth.read();
        data_size += ((data - '0') * 1000);
        data = bluetooth.read();
        data_size += ((data - '0') * 100);
        data = bluetooth.read();
        data_size += ((data - '0') * 10);
        data = bluetooth.read();
        data_size += (data - '0');
        //XXX
        Serial.print("data_size=");
        Serial.println(data_size);
        while (true) {
            if (bluetooth.available()) {
                data = bluetooth.read();
                buf[read_length++] = data;
            }
            if (read_length == data_size) {
                break;
            }
            //Serial.print("read_length=");
            //Serial.println(read_length);
        }
//        while (bluetooth.available() < data_size) {
//            //do nothing
//        }
//        for (int i = 0; i < data_size; i++) {
//            data = bluetooth.read();
//            buf[read_length++] = data;
//        }
    }
    buf[read_length] = '\0';
    //XXX
    Serial.print("buf=");
    Serial.println(buf);
    //buf_ptr = buf;
    state = STATE_SOLVE;
    ptr = 0;
}

void solveCube() {
    if (ptr >= data_size) {
        state = STATE_SOLVE_FINISH;
        return;
    }
    char c1 = buf[ptr++];
    char c2 = buf[ptr++];
    switch (c1) {
    case '\0':
        state = STATE_SOLVE_FINISH;
        break;
    case 'x':
        switch (c2) {
        case '2':
            cube->x2();
            //XXX
            Serial.println("x2");
            break;
        case '\'':
            cube->x_();
            //XXX
            Serial.println("x'");
            break;
        default:
            cube->x();
            //XXX
            Serial.println("x");
            ptr--;
        }
        break;
    case 'y':
        switch (c2) {
        case '2':
            cube->y2();
            //XXX
            Serial.println("y2");
            break;
        case '\'':
            cube->y_();
            //XXX
            Serial.println("y'");
            break;
        default:
            cube->y();
            //XXX
            Serial.println("y");
            ptr--;
        }
        break;
    case 'U':
        switch (c2) {
        case '2':
            cube->U2();
            //XXX
            Serial.println("U2");
            break;
        case '\'':
            cube->U_();
            //XXX
            Serial.println("U'");
            break;
        default:
            cube->U();
            //XXX
            Serial.println("U");
            ptr--;
        }
        break;
    case 'D':
        switch (c2) {
        case '2':
            cube->D2();
            //XXX
            Serial.println("D2");
            break;
        case '\'':
            cube->D_();
            //XXX
            Serial.println("D'");
            break;
        default:
            cube->D();
            //XXX
            Serial.println("D");
            ptr--;
        }
        break;
    case 'L':
        switch (c2) {
        case '2':
            cube->L2();
            //XXX
            Serial.println("L2");
            break;
        case '\'':
            cube->L_();
            //XXX
            Serial.println("L'");
            break;
        default:
            cube->L();
            //XXX
            Serial.println("L");
            ptr--;
        }
        break;
    case 'R':
        switch (c2) {
        case '2':
            cube->R2();
            //XXX
            Serial.println("R2");
            break;
        case '\'':
            cube->R_();
            //XXX
            Serial.println("R'");
            break;
        default:
            cube->R();
            //XXX
            Serial.println("R");
            ptr--;
        }
        break;
    case 'F':
        switch (c2) {
        case '2':
            cube->FF2();
            //XXX
            Serial.println("F2");
            break;
        case '\'':
            cube->FF_();
            //XXX
            Serial.println("F'");
            break;
        default:
            cube->FF();
            //XXX
            Serial.println("F");
            ptr--;
        }
        break;
    case 'B':
        switch (c2) {
        case '2':
            cube->B2();
            //XXX
            Serial.println("B2");
            break;
        case '\'':
            cube->B_();
            //XXX
            Serial.println("B'");
            break;
        default:
            cube->B();
            //XXX
            Serial.println("B");
            ptr--;
        }
        break;
    }
}
