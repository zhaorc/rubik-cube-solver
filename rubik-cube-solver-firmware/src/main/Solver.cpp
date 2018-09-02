#include <Arduino.h>
#include<SoftwareSerial.h>
#include "cube/Cube.h"

#define STATE_READ_MOVES 1
#define STATE_SOLVE        2
#define STATE_SOLVE_FINISH       3

SoftwareSerial bluetooth(6, 7);
Stepper push_stepper(8, 9, 6400, 30);
Stepper rotate_stepper(10, 11, 6400, 60);
Cube *cube;
char buf[61];
char* buf_ptr;
int state = 0;

void readMoves();
void solveCube();

void setup() {
    delay(3000);
    Serial.begin(115200);
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
        state = STATE_READ_MOVES;
        break;
    }
}

void readMoves() {
    char buf[121];
    int data = 0;
    int read_length = 0;
    while (true) {
        if (!bluetooth.available()) {
            continue;
        }
        data = bluetooth.read();
        if (data == '\n' || data == '\r' || data == '\0') {
            buf[read_length] = '\0';
            buf_ptr = buf;
            state = STATE_SOLVE;
            //Serial.println(read_length);
            break;
        }
        buf[read_length++] = data;
    }
}

void solveCube() {
    char c1 = *buf_ptr++;
    char c2 = *buf_ptr++;
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
            //XXX
            Serial.println("x");
            cube->x();
            //XXX
            bluetooth.println("done");
            break;
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
            break;
        }
        break;
    case 'U':
        switch (c2) {
        case '2':
            //Serial.println("U2");
            cube->U2();
            break;
        case '\'':
            //Serial.println("U'");
            cube->U_();
            break;
        default:
            //Serial.println("U");
            cube->U();
            buf_ptr--;
        }
        break;
    case 'D':
        switch (c2) {
        case '2':
            //Serial.println("D2");
            cube->D2();
            break;
        case '\'':
            //Serial.println("D'");
            cube->D_();
            break;
        default:
            Serial.println("D");
            cube->D();
            buf_ptr--;
        }
        break;
    case 'L':
        switch (c2) {
        case '2':
            //Serial.println("L2");
            cube->L2();
            break;
        case '\'':
            //Serial.println("L'");
            cube->L_();
            break;
        default:
            //Serial.println("L");
            cube->L();
            buf_ptr--;
        }
        break;
    case 'R':
        switch (c2) {
        case '2':
            //Serial.println("R2");
            cube->R2();
            break;
        case '\'':
            //Serial.println("R'");
            cube->R_();
            break;
        default:
            //Serial.println("R");
            cube->R();
            buf_ptr--;
        }
        break;
    case 'F':
        switch (c2) {
        case '2':
            //Serial.println("F2");
            cube->FF2();
            break;
        case '\'':
            //Serial.println("F'");
            cube->FF_();
            break;
        default:
            //Serial.println("F");
            cube->FF();
            buf_ptr--;
        }
        break;
    case 'B':
        switch (c2) {
        case '2':
            //Serial.println("B2");
            cube->B2();
            break;
        case '\'':
            //Serial.println("B'");
            cube->B_();
            break;
        default:
            //Serial.println("B");
            cube->B();
            buf_ptr--;
        }
        break;
    }
}
