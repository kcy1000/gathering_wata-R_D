package com.geotwo.LAB_TEST.Gathering.dto;

import java.io.Serializable;

public class StepInfo implements Serializable {

    public StepInfo(double acc1, double acc2, double acc3, double gyro2, double gyro3){
        Acc1 = acc1;
        Acc2 = acc2;
        Acc3 = acc3;
        Gyro2 = gyro2;
        Gyro3 = gyro3;

    }

    public Double Acc1 = 0.0;
    public Double Acc2 = 0.0;
    public Double Acc3 = 0.0;
    public Double Gyro2 = 0.0;
    public Double Gyro3 = 0.0;


    public double getAcc1() {
        return Acc1;
    }
    public void setAcc1(double acc1) {
        Acc1 = acc1;
    }

    public double getAcc2() {
        return Acc2;
    }
    public void setAcc2(double acc2) {
        Acc2 = acc2;
    }

    public double getAcc3() {
        return Acc3;
    }
    public void setAcc3(double acc3) {
        Acc3 = acc3;
    }

    public double getGyro2() {
        return Gyro2;
    }
    public void setGyro2(double gyro2) {
        Gyro2 = gyro2;
    }

    public double getGyro3() {
        return Gyro3;
    }
    public void setGyro3(double gyro3) {
        Gyro3 = gyro3;
    }


}