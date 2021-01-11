package com.geotwo.LAB_TEST.Gathering;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.wata.LAB_TEST.Gathering.R;

import pdr_collecting.core.pdrvariable;

public class PathDrawingSettingDialog extends Dialog {
    private Context context;
    private View.OnClickListener setpEvent;

    public PathDrawingSettingDialog(Context context,  View.OnClickListener viewstepEvt) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        this.context = context;
        setpEvent = viewstepEvt;
    }

    private ImageView circleImg, sensorSettingImg, frequency_24Img, frequency_5Img;
    private TextView stepLength;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WataLog.i("onCreate!!");
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.7f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.path_drawing_setting_dialog);

        findViewById(R.id.step_length_btn).setOnClickListener(btnEvent);//보폭

        stepLength = (TextView)findViewById(R.id.step_length);

        RelativeLayout frequency_2_4_layout = (RelativeLayout) findViewById(R.id.frequency_2_4_layout);
        frequency_2_4_layout.setOnClickListener(btnEvent);
        frequency_24Img = (ImageView) findViewById(R.id.frequency_2_4img);

        RelativeLayout frequency_5_layout = (RelativeLayout) findViewById(R.id.frequency_5_layout);
        frequency_5_layout.setOnClickListener(btnEvent);
        frequency_5Img = (ImageView) findViewById(R.id.frequency_5_img);

        RelativeLayout rotation_mode_layout = (RelativeLayout) findViewById(R.id.rotation_mode_layout);
        rotation_mode_layout.setOnClickListener(btnEvent);
        circleImg = (ImageView) findViewById(R.id.circle_img);

        RelativeLayout setting_sensor_setting = (RelativeLayout) findViewById(R.id.setting_sensor_setting);
        setting_sensor_setting.setOnClickListener(btnEvent);
        sensorSettingImg = (ImageView) findViewById(R.id.sensor_setting_img);

        RelativeLayout make_map_btn = (RelativeLayout) findViewById(R.id.make_map_btn);
        make_map_btn.setOnClickListener(btnEvent);

        TextView reset_btn = (TextView) findViewById(R.id.reset_btn);
        reset_btn.setOnClickListener(btnEvent);


        TextView step_length = (TextView) findViewById(R.id.step_length);
        step_length.setText(isStepLength(context) + "cm");


        mFrequency = isFrequency(context);

        if(mFrequency){
            setClickImg(frequency_24Img, true);
            setClickImg(frequency_5Img, false);
        } else {
            setClickImg(frequency_24Img, false);
            setClickImg(frequency_5Img, true);
        }

        mRotationMode = isMapRatation(context);
        WataLog.d("mRotationMode=" + mRotationMode);
        setClickImg(circleImg, mRotationMode); // 자동회전
        setClickImg(sensorSettingImg, !mRotationMode);

        
//        mGyroMode = isMapRatation(context);
//        WataLog.d("mGyroMode=" + mGyroMode);
//        setClickImg(sensorSettingImg, mGyroMode);

    }

    private boolean mFrequency = false;
    private boolean mRotationMode = true;
    private boolean mGyroMode = false;

    private View.OnClickListener btnEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.step_length_btn) {
                WataLog.i("check;1");
                viewStepLenSetting();
            } else if (id == R.id.frequency_2_4_layout || id == R.id.frequency_5_layout) { // 주파수 2.4
                boolean isChecked = isFrequency(context);
                WataLog.d("isChecked=" + isChecked);
                if (isChecked && id == R.id.frequency_5_layout) {
                    isChecked = false;
                    setClickImg(frequency_24Img, false);
                    setClickImg(frequency_5Img, true);
                } else if (!isChecked && id == R.id.frequency_2_4_layout) {
                    isChecked = true;
                    setClickImg(frequency_24Img, true);
                    setClickImg(frequency_5Img, false);
                }
                setFrequency(context, isChecked);
            } else if (id == R.id.rotation_mode_layout) { // 지도자동회전
                WataLog.i("지도회전");
                boolean isChecked = isMapRatation(context);
                WataLog.d("isChecked=" + isChecked);
                setSensor(isChecked);
//                setGsensor(context, !isChecked);

            } else if (id == R.id.setting_sensor_setting) { // 자이로센서
                WataLog.i("자이로센서 ");
                //UI 수정 필요함

            } else if(id == R.id.make_map_btn) {
                // 설정
                cancel();
            } else if(id == R.id.reset_btn) {
                //리셋
                setClickImg(frequency_24Img, false);
                setClickImg(frequency_5Img, true);
                setClickImg(circleImg, true);
                setClickImg(sensorSettingImg, false);
            }
        }


//        void viewDialog(int id, String title) {
//            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
//            dialog.setTitle(title);
//
//            if (id == R.id.step_length_btn) {
//                String[] arr = {"30", "35", "40", "45", "50", "55", "60", "65", "70"};
//                WataLog.i("check");
//                dialog.setItems(arr, new OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        WataLog.d("which" + which);
//                    }
//                });
//            }
//            dialog.show();
//        }
    };


    private void viewStepLenSetting() {
        final String[] arr = {"40", "45", "50", "55", "60", "65", "70", "75", "80"};
        new AlertDialog.Builder(context)
//                .setTitle(context.getString(R.string.step_setting))
                .setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strLen = arr[which];
                        int stepLen = 0;
                        WataLog.d("strLen=" + strLen);

                        if (strLen != null) {
                            stepLen = Integer.valueOf(strLen);
                            stepLength.setText(strLen + "cm");
                            setStepLength(context, stepLen);

                            double tempLen = stepLen / 100.0;
                            pdrvariable.setStep_length(tempLen);

                            WataLog.d("stepLen=" + stepLen + ", tempLen=" + tempLen);
                            WataLog.d("pdrvariable.getStep_length==" + pdrvariable.getStep_length())
                            ;
                        }
                    }
                })
                .show();
    }

    private void setSensor(boolean isChecked) {
        if(isChecked) {
            isChecked = false;
        } else {
            isChecked = true;
        }
        setClickImg(circleImg, isChecked); // 자동회전
        setMapRatation(context, isChecked);
    }
    private void setClickImg(ImageView view, boolean check){
        if(check) {
            view.setBackgroundResource(R.mipmap.tick_circle_active);
        } else {
            view.setBackgroundResource(R.mipmap.tick_circle_none);
        }

    }


    public static boolean isFrequency(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constance.KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constance.SETTING_FREQUENCY, false);
    }

    public static void setFrequency(Context context, boolean showing) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constance.KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constance.SETTING_FREQUENCY, showing);
        editor.commit();
    }


    public static boolean isMapRatation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constance.KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(Constance.SETTING_MAP_ROTATION, true);
    }

    public static void setMapRatation(Context context, boolean showing) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constance.KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constance.SETTING_MAP_ROTATION, showing);
        editor.commit();
    }

//       public static boolean isGsensor(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(Constance.KEY, Context.MODE_PRIVATE);
//        return sharedPreferences.getBoolean(Constance.SETTING_G_SENSOR_ROTATION, false);
//    }

//    public static void setGsensor(Context context, boolean showing) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(Constance.KEY, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(Constance.SETTING_G_SENSOR_ROTATION, showing);
//        editor.commit();
//    }

    public static int isStepLength(Context context) {
        SharedPreferences pref = context.getSharedPreferences("stepPref", 0);
        return pref.getInt("stepLength", 70);
    }

    public static void setStepLength(Context context, int length) {
        SharedPreferences pref = context.getSharedPreferences("stepPref", 0);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putInt("stepLength", length);
        prefEditor.commit();
    }


}
