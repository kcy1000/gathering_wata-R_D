package com.geotwo.common;

import android.hardware.SensorManager;

import pdr_collecting.core.sensoract;

/**
 * Created by hyuck on 2017. 2. 3..
 */

public class SensorUtils {

    /**
     * using camera rotation angles
     * remapping coordinate x, y to x, z
     */
    public static final int DEVICE_ROTATION_ANGLE_CAMERA = 2;

    /**
     * using device compass rotation
     * remapping coordinate x y to y, minus x
     */
    public static final int DEVICE_ROTATION_COMPASS = 1;

    /**
     * using device default rotation
     */
    public static final int DEVICE_ROTATION_DEFAULT = 0;

    public static final int CIRCLE_DEGREE = 360;

    /**
     * not coordinate remapping
     * @param acc accelerometer sensor x, y, z length 3
     * @param mag magnetic field sensor x, y, z length 3
     * @return calculated orientations in radian
     *  - [0] : azimuth
     *  - [1] : pitch
     *  - [2] : roll
     */
    public static float[] getOrientation(float acc[], float mag[]){
        float orientations[] = null;

        // calc if not empty
        if((acc != null && acc.length > 0)
                && (mag != null && mag.length > 0)){
            float R[] = new float[9];
            float I[] = new float[9];
            boolean calc = SensorManager.getRotationMatrix(R, I, acc, mag);
            if(calc){
                float measuredOrientations[] = new float[3];
                SensorManager.getOrientation(R, measuredOrientations);

                // copy
                orientations = measuredOrientations;
            }
        }

        return orientations;
    }

    /**
     * coordinate mapping
     * @param acc accelerometer sensor x, y, z length 3
     * @param mag magnetic field sensor x, y, z length 3
     * @param deviceRotation coordinate mapping options [DEVICE_ROTATION_DEFAULT, DEVICE_ROTATION_COMPASS, DEVICE_ROTATION_ANGLE_CAMERA]
     * @return calculated orientations in degree
     *  - [0] : azimuth
     *  - [1] : pitch
     *  - [2] : roll
     */
    public static float[] getOrientation(float acc[], float mag[], int deviceRotation){
        float orientations[] = null;

        // calc if not empty
        if((acc != null && acc.length > 0)
                && (mag != null && mag.length > 0)){
            float R[] = new float[9];
            float I[] = new float[9];

            boolean calc = SensorManager.getRotationMatrix(R, I, acc, mag);
            if(calc){
                float outR[] = null;
                switch (deviceRotation){
                    case DEVICE_ROTATION_ANGLE_CAMERA:
                        outR = new float[9];
                        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
                        break;
                    case DEVICE_ROTATION_COMPASS:
                        outR = new float[9];
                        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
                        break;
                    case DEVICE_ROTATION_DEFAULT:
                    default:
                        break;
                }

                float measuredOrientations[] = new float[3];
                SensorManager.getOrientation(outR != null ? outR : R, measuredOrientations);

                // copy
                orientations = new float[]{
                        ((float) Math.toDegrees(measuredOrientations[0]) + CIRCLE_DEGREE) % CIRCLE_DEGREE,
                        (float) Math.toDegrees(measuredOrientations[1]),
                        (float) Math.toDegrees(measuredOrientations[2])
                };
            }
        }

        return orientations;
    }

    /**
     * @param rotationVectors rotation vector sensor
     * @return calculated orientations in degree
     *  - [0] : azimuth
     *  - [1] : pitch
     *  - [2] : roll
     */
    public static float[] getOrientation(float rotationVectors[]){
        float orientations[] = null;

        if(rotationVectors != null && rotationVectors.length > 0){
            float rotationMatrixes[] = new float[9];
            float measuredOrientations[] = new float[3];
            SensorManager.getRotationMatrixFromVector(rotationMatrixes, rotationVectors);
            SensorManager.getOrientation(rotationMatrixes, measuredOrientations);

            // copy
            orientations = new float[]{
                    (float) Math.toDegrees(measuredOrientations[0]),
                    (float) Math.toDegrees(measuredOrientations[1]),
                    (float) Math.toDegrees(measuredOrientations[2])
            };
        }

        return orientations;
    }
}
