package com.geotwo.LAB_TEST.Voucher.Retrofit;

public class SubwayInfo {

    private String areaNm;
    private String lineNm;
    private String korSubNm;
    private String CenterX;
    private String CenterY;
    private String areaNo;
    private String stationNo;
//      "area_no":0,"area_nm":"수도권","line_nm":"1","station_no":"1","kor_sub_nm":"가능역","xpos":14142510.184761,"ypos":4543944.64277253},


    public SubwayInfo(String area_no, String area_nm, String line_nm, String station_no, String kor_sub_nm, String xpos, String ypos) {
        this.areaNo = area_no;
        this.areaNm = area_nm;
        this.lineNm = line_nm;
        this.stationNo = station_no;
        this.korSubNm = kor_sub_nm;
        this.CenterX = xpos;
        this.CenterY = ypos;
    }

    public void setAreaNm(String area_nm) {
        this.areaNm = area_nm;
    }
    public String getAreaNm() {
        return areaNm;
    }

    public void setlineNm(String line_nm) {
        this.lineNm = line_nm;
    }
    public String getlineNm() {
        return lineNm;
    }

    public void setkorSubNm(String korSubNm) {
        this.korSubNm = korSubNm;
    }
    public String getkorSubNm() {
        return korSubNm;
    }

    public void setCenterX(String centerX) {
        this.CenterX = centerX;
    }
    public String getCenterX() {
        return CenterX;
    }

    public void setCenterY(String centerY) {
        this.CenterY = centerY;
    }
    public String getCenterY() {
        return CenterY;
    }


    public void setAreaNo(String area_no) {
        this.areaNo = area_no;
    }
    public String getAreaNo() {
        return areaNo;
    }
    public void setStationNo(String station_no) {
        this.stationNo = station_no;
    }
    public String getStationNo() {
        return stationNo;
    }
}
