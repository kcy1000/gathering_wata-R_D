package com.geotwo.LAB_TEST.Voucher.Retrofit;

public class SubwayFloorInfo {

    private String Gid;
    private String FleNm;
    private String Floor;
    private String Cx;
    private String Cy;
    private String Rotate;
    private String Scale;
    private String Scalex;
    private String Scaley;

    private String Sx;
    private String Sy;

    public SubwayFloorInfo(String gid, String file_nm, String floor, String cx, String cy, String rotate, String scale, String scalex, String scaley , String sx, String sy) {
        this.Gid = gid;
        this.FleNm = file_nm;
        this.Floor = floor;
        this.Cx = cx;
        this.Cy = cy;

        this.Rotate = rotate;
        this.Scale = scale;
        this.Scalex = scalex;
        this.Scaley = scaley;

        this.Sx = sx;
        this.Sy = sy;

    }

    public void setGid(String gid) {
        this.Gid = gid;
    }
    public String getGid() {
        return Gid;
    }

    public void setFleNm(String file_nm) {
        this.FleNm = file_nm;
    }
    public String getFleNm() {
        return FleNm;
    }

    public void setFloor(String floor) {
        this.Floor = floor;
    }
    public String getFloor() {
        return Floor;
    }

    public void setCx(String cx) {
        this.Cx = cx;
    }
    public String getCx() {
        return Cx;
    }

    public void setCy(String cy) {
        this.Cy = cy;
    }
    public String getCy() {
        return Cy;
    }

    public void setRotate(String rotate) {
        this.Rotate = rotate;
    }
    public String getRotate() {
        return Rotate;
    }
    public void setScale(String scale) {
        this.Scale = scale;
    }
    public String getScale() {
        return Scale;
    }

    public void setScalex(String scalex) {
        this.Scalex = scalex;
    }
    public String getScalex() {
        return Scalex;
    }

    public void setScaley(String scaley) {
        this.Scaley = scaley;
    }
    public String getScaley() {
        return Scaley;
    }

    public void setSx(String sx) {
        this.Sx = sx;
    }
    public String getSx() {
        return Sx;
    }

    public void setSy(String sy) {
        this.Sy = sy;
    }
    public String getSy() {
        return Sy;
    }

}
