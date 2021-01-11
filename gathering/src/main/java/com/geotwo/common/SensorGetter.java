package com.geotwo.common;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.LinkedList;

/**
 * Created by hyuck on 2017. 2. 8..
 */

public class SensorGetter {
    private SensorManager sensorManager = null;
    private LinkedList<SensorContainer> sensors = null;

    public SensorGetter(Context context){
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensors = new LinkedList();
    }

    public Sensor get(int sensorType, int sensorDelay, SensorEventListener sensorEventListener){
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        sensorManager.registerListener(sensorEventListener, sensor, sensorDelay);
        SensorContainer sensorContainer = new SensorContainer(sensorType, sensorDelay, sensor, sensorEventListener);
        sensors.add(sensorContainer);

        return sensor;
    }

    public void close(){
        if(sensors != null && sensors.size() > 0){
            for(SensorContainer sensorContainer : sensors){
                SensorEventListener sensorEventListener = sensorContainer.getSensorEventListener();
                sensorManager.unregisterListener(sensorEventListener);
            }
        }
    }

    public class SensorContainer {
        private int sensorType;
        private int sensorDelay;
        private Sensor sensor;
        private SensorEventListener sensorEventListener;
        public SensorContainer(int sensorType, int sensorDelay, Sensor sensor, SensorEventListener sensorEventListener){
            this.sensorType = sensorType;
            this.sensorDelay = sensorDelay;
            this.sensor = sensor;
            this.sensorEventListener = sensorEventListener;
        }
        public int getSensorType() {
            return sensorType;
        }
        public int getSensorDelay() {
            return sensorDelay;
        }
        public Sensor getSensor() {
            return sensor;
        }
        public SensorEventListener getSensorEventListener() {
            return sensorEventListener;
        }
    }
}
