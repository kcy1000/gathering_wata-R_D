package com.geotwo.LAB_TEST.Cell;

public enum CellType {
    Unknown(0),
    Cdma(1),
    Gsm(2),
    Wcdma(3),
    Lte(4),
    Nr(5),
    Tdscdma(6);

    private int value;

    private CellType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CellType fromValue(int value) {
        switch (value) {
            case 1:
                return Cdma;
            case 2:
                return Gsm;
            case 3:
                return Wcdma;
            case 4:
                return Lte;
            case 5:
                return Nr;
            case 6:
                return Tdscdma;
            case 0:
            default:
                return Unknown;
        }
    }
}
