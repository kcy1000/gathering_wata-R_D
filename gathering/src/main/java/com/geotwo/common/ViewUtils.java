package com.geotwo.common;

import android.view.View;

/**
 * Created by hyuck on 2017. 1. 9..
 */

public class ViewUtils {
    public static void setVisibility(View view, int visibility){
        if(view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }
}
