package com.geotwo.LAB_TEST.Voucher.Retrofit;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

public class SubwayData {

    private String Id;
    private String Name;

    public SubwayData(String area_id, String area_nm) {
        this.Id = area_id;
        this.Name = area_nm;

    }

    public void setId(String area_id) {
        this.Id = area_id;
    }
    public void seName(String area_nm) {
        this.Name = area_nm;
    }

    public String getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }


}
