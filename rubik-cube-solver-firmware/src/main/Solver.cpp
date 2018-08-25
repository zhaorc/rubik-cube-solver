#include <Arduino.h>
#include "cube/Cube.h"

#define STATE_READ_FORMULA 1
#define STATE_SOLVE        2
#define STATE_SOLVE_FINISH       3

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
    cube = new Cube(&push_stepper, &rotate_stepper);
//    push_stepper.runDegree(5);
    delay(1000);
    cube->hold();
    cube->release();
    state = STATE_READ_FORMULA;
}

void loop() {
    switch (state) {
    case STATE_READ_FORMULA:
        readMoves();
        break;
    case STATE_SOLVE:
        solveCube();
        break;
    case STATE_SOLVE_FINISH:
        cube->release();
        state = STATE_READ_FORMULA;
        break;
    }
}

void readFormula() {
    char buf[121];
    int data = 0;
    int read_length = 0;
    while (true) {
        if (!Serial.available()) {
            continue;
        }
        data = Serial.read();
        if (data == '\n' || data == '\r' || data == '\0') {
            buf[read_length] = '\0';
            buf_ptr = buf;
            state = STATE_SOLVE;
            Serial.println(read_length);
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
    case 'U':
        switch (c2) {
        case '2':
            Serial.println("U2");
            cube->U2();
            break;
        case '\'':
            Serial.println("U'");
            cube->U_();
            break;
        default:
            Serial.println("U");
            cube->U();
            buf_ptr--;
        }
        break;
    case 'D':
        switch (c2) {
        case '2':
            Serial.println("D2");
            cube->D2();
            break;
        case '\'':
            Serial.println("D'");
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
            Serial.println("L2");
            cube->L2();
            break;
        case '\'':
            Serial.println("L'");
            cube->L_();
            break;
        default:
            Serial.println("L");
            cube->L();
            buf_ptr--;
        }
        break;
    case 'R':
        switch (c2) {
        case '2':
            Serial.println("R2");
            cube->R2();
            break;
        case '\'':
            Serial.println("R'");
            cube->R_();
            break;
        default:
            Serial.println("R");
            cube->R();
            buf_ptr--;
        }
        break;
    case 'F':
        switch (c2) {
        case '2':
            Serial.println("F2");
            cube->FF2();
            break;
        case '\'':
            Serial.println("F'");
            cube->FF_();
            break;
        default:
            Serial.println("F");
            cube->FF();
            buf_ptr--;
        }
        break;
    case 'B':
        switch (c2) {
        case '2':
            Serial.println("B2");
            cube->B2();
            break;
        case '\'':
            Serial.println("B'");
            cube->B_();
            break;
        default:
            Serial.println("B");
            cube->B();
            buf_ptr--;
        }
        break;
    }
}
