#ifndef EKF_H
#define EKF_H

#include <math.h>

class SimpleEKF {
private:
    float x, y, heading, velocity;
    float P[4][4];
    float Q[4][4];
    float R[2][2];

public:
    SimpleEKF() {
        x = 0.0f;
        y = 0.0f;
        heading = 0.0f;
        velocity = 0.0f;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                P[i][j] = (i == j) ? 1.0f : 0.0f;
                Q[i][j] = (i == j) ? 0.01f : 0.0f;
            }
        }

        R[0][0] = 0.5f;
        R[0][1] = 0.0f;
        R[1][0] = 0.0f;
        R[1][1] = 0.5f;
    }

    // Predict step: dx/dy are displacements in meters (East/North),
    // headingDeg is the current heading from AHRS in degrees
    void predict(float dx, float dy, float headingDeg) {
        x += dx;
        y += dy;
        heading = headingDeg;
        velocity = sqrtf(dx * dx + dy * dy);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                P[i][j] += Q[i][j];
            }
        }
    }

    void updateGPS(float gpsX, float gpsY) {
        float y_meas[2] = {gpsX - x, gpsY - y};

        float S[2][2];
        S[0][0] = P[0][0] + R[0][0];
        S[0][1] = P[0][1] + R[0][1];
        S[1][0] = P[1][0] + R[1][0];
        S[1][1] = P[1][1] + R[1][1];

        float detS = S[0][0] * S[1][1] - S[0][1] * S[1][0];
        if (fabs(detS) < 1e-6f) return;

        float invS[2][2];
        invS[0][0] = S[1][1] / detS;
        invS[0][1] = -S[0][1] / detS;
        invS[1][0] = -S[1][0] / detS;
        invS[1][1] = S[0][0] / detS;

        float K[4][2];
        for (int i = 0; i < 4; i++) {
            K[i][0] = P[i][0] * invS[0][0] + P[i][1] * invS[1][0];
            K[i][1] = P[i][0] * invS[0][1] + P[i][1] * invS[1][1];
        }

        x += K[0][0] * y_meas[0] + K[0][1] * y_meas[1];
        y += K[1][0] * y_meas[0] + K[1][1] * y_meas[1];

        float I_KH[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                I_KH[i][j] = (i == j) ? 1.0f : 0.0f;
                if (j < 2) I_KH[i][j] -= K[i][j] * ((i < 2) ? 1.0f : 0.0f);
            }
        }

        float P_new[4][4] = {0};
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    P_new[i][j] += I_KH[i][k] * P[k][j];
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                P[i][j] = P_new[i][j];
            }
        }
    }

    void getState(float &outX, float &outY, float &outHeading, float &outVelocity) {
        outX = x;
        outY = y;
        outHeading = heading;
        outVelocity = velocity;
    }

    void setState(float newX, float newY) {
        x = newX;
        y = newY;
    }
};

#endif
