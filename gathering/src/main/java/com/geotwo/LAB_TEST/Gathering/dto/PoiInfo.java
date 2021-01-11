package com.geotwo.LAB_TEST.Gathering.dto;

import java.io.Serializable;

public class PoiInfo implements Serializable {

    public PoiInfo(int number, String poiPositionX , String poiPositionY , String poiText, String category_code, String img_name){
        Number = number;
        PoiPositionX = poiPositionX;
        PoiPositionY = poiPositionY;
        PoiText = poiText;
        categoryCode = category_code;
        imgName = img_name;
    }

    public int Number = 0;
    public String PoiPositionY = "";
    public String PoiPositionX = "";
    public String PoiText = "";
    public String categoryCode;
    public String imgName;



    public int getNumber() {
        return Number;
    }
    public void seNumber(int number) {
        Number = number;
    }

    public String getPoiText() {
        return PoiText;
    }
    public void sePoiText(String poiText) {
        PoiText = poiText;
    }

    public String getPoiPositionX() {
        return PoiPositionX;
    }
    public void setPoiPositionX(String poiPositionX) {
        PoiPositionX = poiPositionX;
    }
    public String getPoiPositionY() {
        return PoiPositionY;
    }
    public void setPoiPositionY(String poiPositionY) {
        PoiPositionY = poiPositionY;
    }

    public void setCategoryCode(String category_code) {
        this.categoryCode = category_code;
    }
    public String getCategoryCode() {
        return categoryCode;
    }

    public void setPotoName(String img_name) {
        this.imgName = img_name;
    }
    public String getPotoName() {
        return imgName;
    }
}