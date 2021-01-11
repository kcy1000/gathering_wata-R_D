package pdr_collecting.core;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;

import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.common.SensorGetter;

public class sensoract extends Activity implements Runnable, SensorEventListener {
    private static SensorGetter sensorGetter = null;

    Application application;
    static SensorManager sensormaneger;
    Sensor accsensor;
    Sensor magsensor;
    Sensor gyrosensor;
    Sensor pressensor;
    Sensor magnetic;
    Sensor gravitySensor;
    Sensor unCalibratedMagneticSensor;
    static SensorEventListener acclistener;
    static SensorEventListener maglistener;
    static SensorEventListener gyrolistener;
    static SensorEventListener presslistener;
    static SensorEventListener magnetlistener;
    static SensorEventListener onGravitySensorListener = null;
    static SensorEventListener onUnCalibratedMagneticSensorListener = null;

    public static float[] accvalue = new float[3];
    public static double magvalue = 0;
    public static double[] gyrovalue = new double[3];
    public static float[] mag_field = new float[3];
    public static float[] mag = new float[3];
    public static float[] gravityFields = new float[3];
    public static float[] unCalibratedMagneticFields = new float[3];
    public static float[] rotationVectors = new float[3];
    public static double pressure;

    static int sensorthreadsleeptime = 50;
    static int MapGyroSize = (int) (1000 / sensorthreadsleeptime);
    boolean bStoped = true;
    boolean bRunning = false;
    Handler pdrHandler = null;

    int step = 0;

    public sensoract(Application app, Handler _pdrHandler) {
        application = app;
        pdrHandler = _pdrHandler;
        step = 0;

        mag_field[0] = 0;
        mag_field[1] = 0;
        mag_field[2] = 0;

        sensormaneger = (SensorManager) application.getSystemService(SENSOR_SERVICE);
        accsensor = sensormaneger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        gyrosensor = sensormaneger.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        pressensor = sensormaneger.getDefaultSensor(Sensor.TYPE_PRESSURE);
        magnetic = sensormaneger.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravitySensor = sensormaneger.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magsensor = sensormaneger.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        unCalibratedMagneticSensor = sensormaneger.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);

        acclistener = new acclistenerclass();
        maglistener = new maglistenerclass();
        gyrolistener = new gyrolistenerclass();
        presslistener = new presslistenerclass();
        magnetlistener = new magnetlistenerclass();
        onGravitySensorListener = new OnGravitySensorListener();
        onUnCalibratedMagneticSensorListener = new OnUnCalibratedMagneticSensorListener();

        sensormaneger.registerListener(gyrolistener, gyrosensor, SensorManager.SENSOR_DELAY_UI);
        sensormaneger.registerListener(presslistener, pressensor, SensorManager.SENSOR_DELAY_UI);
        sensormaneger.registerListener(magnetlistener, magnetic, SensorManager.SENSOR_DELAY_UI);
        sensormaneger.registerListener(maglistener, magsensor, SensorManager.SENSOR_DELAY_UI);
        // 가속도 센서 주기적으로 죽음.
        sensormaneger.registerListener(acclistener, accsensor, SensorManager.SENSOR_DELAY_UI);
        sensormaneger.registerListener(onGravitySensorListener, gravitySensor, SensorManager.SENSOR_DELAY_UI);
        sensormaneger.registerListener(onUnCalibratedMagneticSensorListener, unCalibratedMagneticSensor, SensorManager.SENSOR_DELAY_UI);

        sensorGetter = new SensorGetter(application);
        sensorGetter.get(Sensor.TYPE_ROTATION_VECTOR, SensorManager.SENSOR_DELAY_UI, this);
    }

//	@Override
//	protected void onResume()
//	{
//		super.onResume();
//		Log.i("identification"," aaaa");
//		sensormaneger.registerListener(acclistener, accsensor, SensorManager.SENSOR_DELAY_UI);
//		sensormaneger.registerListener(maglistener, magsensor, SensorManager.SENSOR_DELAY_UI);
//		sensormaneger.registerListener(gyrolistener, gyrosensor, SensorManager.SENSOR_DELAY_UI);
//	}

    static void stop() {
        sensormaneger.unregisterListener(acclistener);
        sensormaneger.unregisterListener(maglistener);
        sensormaneger.unregisterListener(gyrolistener);
        sensormaneger.unregisterListener(onGravitySensorListener);
        sensormaneger.unregisterListener(onUnCalibratedMagneticSensorListener);
        sensorGetter.close();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                rotationVectors = sensorEvent.values.clone();
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class acclistenerclass implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            accvalue[0] = event.values[0];
            accvalue[1] = event.values[1];
            accvalue[2] = event.values[2];
        }
    }

    public class maglistenerclass implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            mag = event.values.clone();
            magvalue = event.values[0] * pdrvariable.degree2radian;
        }
    }

    public class gyrolistenerclass implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            gyrovalue[0] = event.values[0];
            gyrovalue[1] = event.values[1];
            gyrovalue[2] = event.values[2];
        }
    }

    public class presslistenerclass implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            pressure = event.values[0];
        }
    }

    public class magnetlistenerclass implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            mag_field[0] = event.values[0];
            mag_field[1] = event.values[1];
            mag_field[2] = event.values[2];
        }
    }

    public class OnGravitySensorListener implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            gravityFields[0] = event.values[0];
            gravityFields[1] = event.values[1];
            gravityFields[2] = event.values[2];
        }
    }

    public class OnUnCalibratedMagneticSensorListener implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            unCalibratedMagneticFields[0] = event.values[0];
            unCalibratedMagneticFields[1] = event.values[1];
            unCalibratedMagneticFields[2] = event.values[2];
        }
    }

    @Override
    public void run() {
        bRunning = true;
        bStoped = false;
        step = 0;
        while (bRunning) {
            mHandler.sendEmptyMessage(100);
            try {
                Thread.sleep(sensorthreadsleeptime);
            } catch (Exception e) {
            }
        }
        bStoped = true;
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 100) {
				SharedPreferences pref = application.getSharedPreferences(Constance.KEY, 0);
				boolean isGyroSensor = pref.getBoolean(Constance.SETTING_G_SENSOR_ROTATION, false);
//				WataLog.d("isGyroSensor=" + isGyroSensor);

				if(isGyroSensor) {
					pdralgorithm.getsensordata(accvalue[0], accvalue[1], accvalue[2], gyrovalue[1], gyrovalue[2]);
					if (pdrvariable.step_count == 0) step = 0;
					if (step < pdrvariable.step_count) {
						step = pdrvariable.step_count;
						if (pdrHandler != null)
							pdrHandler.sendEmptyMessage(100);
					}
				}


//				String log = new String();
//				log += "이동거리 : "+steplenthestimation.present_acc_variance_value + "/"+steplenthestimation.present_gyro_integral+"/"+steplenthestimation.present_step_frequency+"/"+gyrovalue[2];
//				log += ", 진행방향 : " + pdrvariable.pedestrian_heading * 180/3.14 + "/" + pdrvariable.pedestrian_displayed_heading * 180/3.14 ;
//				log += ", 걸음걸이 : "+ pdrvariable.present_step_length+"/"+pdrvariable.nominal_step_length;
//				log += ", 현재위치 : "+ pdrvariable.I_pedestrian_x_coordinate + "/ "+pdrvariable.I_pedestrian_y_coordinate;
//
//				//Log.d("jeongyeol", "pdr "+pdrvariable.step_count);
//				WataLog.d(log);



//				PdrActivity.text1.setText("걸음수 : "+pdrvariable.step_count+" 자이로이벤트 : "+pdralgorithm.Gyro_event);
//				PdrActivity.text2.setTextSize(15);
//				PdrActivity.text2.setText("이동거리 : "+steplenthestimation.present_acc_variance_value + "\n "+steplenthestimation.present_gyro_integral+" \n"+steplenthestimation.present_step_frequency+"\n"+gyrovalue[2]);
//				PdrActivity.text3.setTextSize(25);
//				PdrActivity.text3.setText("진행방향 : "+pdrvariable.pedestrian_heading * 180/3.14 + "\n "+pdrvariable.pedestrian_displayed_heading* 180/3.14 );
//				PdrActivity.text4.setText("걸음걸이 : "+ pdrvariable.present_step_length+"\n"+pdrvariable.nominal_step_length);
//				PdrActivity.text5.setText("현재위치 : "+ pdrvariable.I_pedestrian_x_coordinate + " "+pdrvariable.I_pedestrian_y_coordinate);
            }
        }
    };

    // GEO2 추가 : Thread 중지를 위한 함수.
    public void requestStop() {
        bRunning = false;
        while (!bStoped) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return;
    }
}