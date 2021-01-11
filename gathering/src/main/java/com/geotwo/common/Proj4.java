package com.geotwo.common;

import com.geotwo.o2mapmobile.geometry.Vector;

import org.proj4.PJ;

/**
 * Created by bang on 2016-08-31.
 */
public class Proj4 {
    private final double x;
    private final double y;
    private PJ sourcePJ = null;
    private PJ targetPJ = null;

    public Proj4(double x, double y, String sourcePJ, String targetPJ){
        this.x = x;
        this.y = y;
        this.sourcePJ = new PJ(sourcePJ);
        this.targetPJ = new PJ(targetPJ);
    }

    public Vector transformToVector(){
        double[] coordinates = { x,y };
        sourcePJ.transform(targetPJ, 2, coordinates, 0, 1);
        return new Vector(coordinates[0],coordinates[1]);
    }
}
