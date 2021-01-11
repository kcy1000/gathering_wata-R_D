package com.geotwo.ImageMap;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.geotwo.o2mapmobile.HandlingContext;
import com.geotwo.o2mapmobile.O2Map;

public class WMapSurfaceView extends GLSurfaceView {
    private O2Map mapRender;

    public WMapSurfaceView(Context context) {
        super(context);
        this.setEGLContextClientVersion(2);
        WMapInstance instance = new WMapInstance(this);
        instance.setUseLighting(false);
        this.mapRender = instance;
        this.setRenderer(this.mapRender);
        this.setPreserveEGLContextOnPause(true);
        this.setRenderMode(0);
    }

    public O2Map getO2mapInstance() {
        return this.mapRender;
    }

    public boolean onTouchEvent(MotionEvent var1) {
        HandlingContext var2 = new HandlingContext();
        var2.setEvent(var1);
        var2.setContext(this.getContext());
        var2.setView(this.mapRender.getView());
        var2.setWidth(this.getWidth());
        var2.setHeight(this.getHeight());
        var2.setModel(this.mapRender.getModel());

        //try {
            this.mapRender.renderLock();
            this.mapRender.getInputHandler().onTouchEvent(var2);
            this.mapRender.renderUnlock();
        //} catch (InterruptedException var4) {
        //    Log.e("geotwo", "Exception occurs during handling touch", var4);
        //}

        this.requestRender();
        return true;
    }
}
