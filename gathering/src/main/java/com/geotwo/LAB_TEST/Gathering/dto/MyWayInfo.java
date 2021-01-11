package com.geotwo.LAB_TEST.Gathering.dto;

import java.io.Serializable;

public class MyWayInfo implements Serializable {

    public MyWayInfo(int number, String startPointX,  String startPointY, String endPointX, String endPointY, String recordLength, String recordF, String recordR, int angle,
                     double startRelativePointX,  double startRelativePointY, double endRelativePointX, double endRelativePointY,  double longitude, double latitude){
        Number = number;
        StartPointX = startPointX;
        StartPointY = startPointY;
        EndPointX = endPointX;
        EndPointY = endPointY;
        RecordLength = recordLength;
        RecordF = recordF;
        RecordR = recordR;
        Angle = angle;

        StartRelativePointX = startRelativePointX;
        StartRelativePointY = startRelativePointY;
        EndRelativePointX = endRelativePointX;
        EndRelativePointY = endRelativePointY;

    }

    public int Number = 0;
    public String StartPointX = "";
    public String StartPointY = "";
    public String EndPointX = "";
    public String EndPointY = "";

    public String RecordLength = "";
    public String RecordF = "";
    public String RecordR = "";
    public int Angle = 0;
    public double StartRelativePointX;
    public double StartRelativePointY;
    public double EndRelativePointX;
    public double EndRelativePointY;



    public int getNumber() {
        return Number;
    }
    public void setNumber(int number) {
        Number = number;
    }

    public String getStartPointX() {
        return StartPointX;
    }
    public void setStartPointX(String startPointX) {
        StartPointX = startPointX;
    }

    public String getStartPointY() {
        return StartPointY;
    }
    public void setStartPointY(String startPointY) {
        StartPointY = startPointY;
    }

    public String getEndPointX() {
        return EndPointX;
    }
    public void setEndPointX(String endPointX) {
        EndPointX = endPointX;
    }

    public String getEndPointY() {
        return EndPointY;
    }
    public void setEndPointY(String endPointY) {
        EndPointY = endPointY;
    }

    public String getRecordLength() {
        return RecordLength;
    }
    public void setRecordLength(String recordLength) {
        RecordLength = recordLength;
    }

    public String getRecordF() {
        return RecordF;
    }
    public void setRecordF(String recordF) {
        RecordF = recordF;
    }

    public String getRecordR() {
        return RecordR;
    }
    public void setRecordR(String recordR) {
        RecordR = recordR;
    }

    public int getAngle() {
        return Angle;
    }
    public void setAngle(int angle) {
        Angle = angle;
    }


    public double getStartRelativePointX() {
        return StartRelativePointX;
    }
    public void setStartRelativePointX(int startRelativePointX) {
        StartRelativePointX = startRelativePointX;
    }

    public double getStartRelativePointY() {
        return StartRelativePointY;
    }
    public void setStartRelativePointY(int startRelativePointY) {
        StartRelativePointY = startRelativePointY;
    }
    public double getEndRelativePointX() {
        return EndRelativePointX;
    }
    public void setEndRelativePointX(int endRelativePointX) {
        EndRelativePointX = endRelativePointX;
    }
    public double getEndRelativePointY() {
        return EndRelativePointY;
    }
    public void setEndRelativePointY(int endRelativePointY) {
        EndRelativePointY = endRelativePointY;
    }


}