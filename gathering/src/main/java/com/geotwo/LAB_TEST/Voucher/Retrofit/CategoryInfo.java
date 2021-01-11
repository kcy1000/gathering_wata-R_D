package com.geotwo.LAB_TEST.Voucher.Retrofit;

public class CategoryInfo {

    private String categoryCode;
    private String categoryNm;


    public CategoryInfo(String category_code, String category_nm) {
        this.categoryCode = category_code;
        this.categoryNm = category_nm;
    }

    public void setCategoryCode(String category_code) {
        this.categoryCode = category_code;
    }
    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryNm(String category_nm) {
        this.categoryNm = category_nm;
    }
    public String getCategoryNm() {
        return categoryNm;
    }

}
