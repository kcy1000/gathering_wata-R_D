package com.geotwo.LAB_TEST.Voucher.Retrofit;

public class OfficeInfo {

    private String Gid;
    private String areaNo;
    private String areaNm;
    private String companyNm;
    private String companyNo;
    private String floorInfo;
    private String CenterX;
    private String CenterY;
    private String Sx;
    private String Sy;

//    [{"area_no":1,"area_nm":"부산","company_nm":"BCC","company_no":"0","floor_info":"1","cx":14374183,"cy":4187390,"sx":"14372683","sy":"14372683"},

    public OfficeInfo(String gid, String area_no, String area_nm, String company_nm, String company_no, String floor_info, String cx, String cy, String sx, String sy) {
        this.Gid = gid;
        this.areaNo = area_no;
        this.areaNm = area_nm;
        this.companyNm = company_nm;
        this.companyNo = company_no;
        this.floorInfo = floor_info;
        this.CenterX = cx;
        this.CenterY = cy;
        this.Sx = sx;
        this.Sy = sy;
    }

    public void setGid(String gid) {
        this.Gid = gid;
    }

    public String getGid() {
        return Gid;
    }

    public void setAreaNo(String area_no) {
        this.areaNo = area_no;
    }

    public String getAreaNo() {
        return areaNo;
    }

    public void setAreaNm(String area_nm) {
        this.areaNm = area_nm;
    }

    public String getAreaNm() {
        return areaNm;
    }

    public void setCompanyNm(String company_nm) {
        this.companyNm = company_nm;
    }

    public String getCompanyNm() {
        return companyNm;
    }

    public void setCompanyNo(String company_no) {
        this.companyNo = company_no;
    }

    public String getCompanyNo() {
        return companyNo;
    }

    public void setFloorInfo(String floor_info) {
        this.floorInfo = floor_info;
    }

    public String getFloorInfo() {
        return floorInfo;
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