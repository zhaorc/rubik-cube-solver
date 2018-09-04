#include <Arduino.h>
#include<SoftwareSerial.h>
#include "cube/Cube.h"

#define STATE_READ_MOVES 1
#define STATE_SOLVE        2
#define STATE_SOLVE_FINISH       3

SoftwareSerial bluetooth(10, 11);
Stepper push_stepper(4, 5, 6400, 30);
Stepper rotate_stepper(6, 7, 6400, 60);
Cube *cube;
char buf[121];
int data_size = 0;
int ptr = 0;
//char* buf_ptr;
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
    //char buf[121];
    int data = 0;
    int read_length = 0;
    // first 4 bytes are length
    // followed by data
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
    while (bluetooth.available() < data_size) {
        //do nothing
    }
    for (int i = 0; i < data_size; i++) {
        data = bluetooth.read();
        buf[read_length++] = data;
    }
//    //清除缓冲区
//    while (bluetooth.available()) {
//        data = bluetooth.read();
//        delay(10);
//    }
    buf[read_length] = '\0';
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
            break;
        case '\'':
            cube->x_();
            break;
        default:
            cube->x();
            ptr--;
        }
        break;
    case 'y':
        switch (c2) {
        case '2':
            cube->y2();
            break;
        case '\'':
            cube->y_();
            break;
        default:
            cube->y();
            ptr--;
        }
        break;
    case 'U':
        switch (c2) {
        case '2':
            cube->U2();
            break;
        case '\'':
            cube->U_();
            break;
        default:
            cube->U();
            ptr--;
        }
        break;
    case 'D':
        switch (c2) {
        case '2':
            cube->D2();
            break;
        case '\'':
            cube->D_();
            break;
        default:
            cube->D();
            ptr--;
        }
        break;
    case 'L':
        switch (c2) {
        case '2':
            cube->L2();
            break;
        case '\'':
            cube->L_();
            break;
        default:
            cube->L();
            ptr--;
        }
        break;
    case 'R':
        switch (c2) {
        case '2':
            cube->R2();
            break;
        case '\'':
            cube->R_();
            break;
        default:
            cube->R();
            ptr--;
        }
        break;
    case 'F':
        switch (c2) {
        case '2':
            cube->FF2();
            break;
        case '\'':
            cube->FF_();
            break;
        default:
            cube->FF();
            ptr--;
        }
        break;
    case 'B':
        switch (c2) {
        case '2':
            cube->B2();
            break;
        case '\'':
            cube->B_();
            break;
        default:
            cube->B();
            ptr--;
        }
        break;
    }
}
