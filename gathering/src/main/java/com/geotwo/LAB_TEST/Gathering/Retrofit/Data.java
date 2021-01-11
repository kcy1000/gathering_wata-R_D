package com.geotwo.LAB_TEST.Gathering.Retrofit;

public class Data {

    private final int id;
    private final String name;

    Data(int area_id, String pos_name) {
        this.id = area_id;
        this.name = pos_name;
    }

    public int getId() {
        return id;
    }

    public String geName() {
        return name;
    }

}
