package com.geotwo.LAB_TEST.Voucher.Retrofit;

public class SubwayLineInfo {

    private String Cid;
    private String Area_no;
    private String Lineno;
    private String Station_nm;
    private String Floorinfo;
    private String StX;
    private String StY;
    private String EdX;
    private String EdY;
    private int Line_serno;
    private String Ins_date;
    private String CompletionF;
    private String CompletionR;


    public SubwayLineInfo(String cid, String area_no, String lineno , String station_nm, String floorinfo,
                          String st_x, String st_y,  String ed_x, String ed_y, int line_serno, String ins_date , String completionF , String completionR ) {
        this.Cid = cid;
        this.Area_no = area_no;
        this.Lineno = lineno;
        this.Station_nm = station_nm;
        this.Floorinfo = floorinfo;

        this.StX = st_x;
        this.StY = st_y;
        this.EdX = ed_x;
        this.EdY = ed_y;
        this.Line_serno = line_serno;
        this.Ins_date = ins_date;

        this.CompletionF = completionF;
        this.CompletionR = completionR;

    }

    public void setCid(String cid) {
        this.Cid = cid;
    }
    public String getCid() {
        return Cid;
    }

    public void setArea_no(String area_no) {
        this.Area_no = area_no;
    }
    public String getArea_no() {
        return Area_no;
    }

    public void setLineno(String lineno) {
        this.Lineno = lineno;
    }
    public String getLineno() {
        return Lineno;
    }

    public void settationNm(String station_nm) {
        this.Station_nm = station_nm;
    }
    public String getStationNm() {
        return Station_nm;
    }

    public void setFloorinfo(String cid) {
        this.Cid = cid;
    }
    public String getFloorinfo() {
        return Cid;
    }


    public void setStX(String stX) {
        this.StX = stX;
    }
    public String getStX() {
        return StX;
    }

    public void setStY(String stY) {
        this.StY = stY;
    }
    public String getStY() {
        return StY;
    }

    public void setEdX(String edX) {
        this.EdX = edX;
    }
    public String getEdX() {
        return EdX;
    }

    public void setEdY(String edY) {
        this.EdY = edY;
    }
    public String getEdY() {
        return EdY;
    }

    public void setLineSerno(int line_serno) {
        this.Line_serno = line_serno;
    }
    public int getLineSerno() {
        return Line_serno;
    }

    public void setInsDate(String ins_date) {
        this.Ins_date = ins_date;
    }
    public String getInsDate() {
        return Ins_date;
    }

    public void setCompletionF(String completionF) {
        this.CompletionF = completionF;
    }
    public String getCompletionF() {
        return CompletionF;
    }

    public void setCompletionR(String completionR) {
        this.CompletionR = completionR;
    }
    public String getCompletionR() {
        return CompletionR;
    }


}
