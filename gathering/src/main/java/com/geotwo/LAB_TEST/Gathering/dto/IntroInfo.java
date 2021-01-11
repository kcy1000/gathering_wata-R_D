package com.geotwo.LAB_TEST.Gathering.dto;

import java.io.Serializable;

public class IntroInfo implements Serializable {

    public IntroInfo(String fileNum, String address, String floors){
        FileNum = fileNum;
        Address = address;
        Floors = floors;

    }

    public String FileNum = "";
    public String Address = "";
    public String Floors = "";


    public String getFileNum() {
        return FileNum;
    }
    public void setFileNum(String fileNum) {
        FileNum = fileNum;
    }

    public String getAddress() {
        return Address;
    }
    public void setAddress(String address) {
        Address = address;
    }

    public String getFloors() {
        return Floors;
    }
    public void setFloors(String floors) {
        Floors = floors;
    }



}