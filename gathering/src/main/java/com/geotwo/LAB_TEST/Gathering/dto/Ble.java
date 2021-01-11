package com.geotwo.LAB_TEST.Gathering.dto;

import java.io.Serializable;

/**
 * Created by hyuck on 2017. 1. 9..
 */

public class Ble implements Serializable {
    public String mac = "";
    public String uuid = "";
    public String major = "";
    public String minor = "";
    public boolean isIgnored = false;
    public Object tag = null;

    public void set(Ble temporary) {
        this.mac = temporary.mac;
        this.uuid = temporary.uuid;
        this.major = temporary.major;
        this.minor = temporary.minor;
    }

    @Override
    public boolean equals(Object o) {
        boolean flag = false;
        if(o != null && o instanceof Ble){
            Ble temporary = (Ble) o;
            if(mac.equals(temporary.mac) && uuid.equals(temporary.uuid) && major.equals(temporary.major) && minor.equals(temporary.minor)){
                flag = true;
            } else {
                flag = false;
            }
        } else {
            flag = false;
        }

        return flag;
    }

    @Override
    public int hashCode() {
        return String.format("%s%s%s%s", mac, uuid, major, minor).hashCode();
    }
}