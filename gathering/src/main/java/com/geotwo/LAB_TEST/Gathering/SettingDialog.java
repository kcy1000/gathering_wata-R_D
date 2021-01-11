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
import android.widget.ToggleButton;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.wata.LAB_TEST.Gathering.R;

public class SettingDialog extends Dialog {
    private Context context;
    private View.OnClickListener senEvent, setpEvent;

    public SettingDialog(Context context, View.OnClickListener sensetiveEvt, View.OnClickListener viewstepEvt) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        this.context = context;
        senEvent = sensetiveEvt;
        setpEvent = viewstepEvt;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WataLog.i("check!!");
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.7f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.setting_dialog);

        findViewById(R.id.setting_btn2).setOnClickListener(senEvent); //민감도
        //findViewById(R.id.setting_btn3).setOnClickListener(btnEvent);//수집주기
        findViewById(R.id.setting_btn4).setOnClickListener(setpEvent);//보폭
        findViewById(R.id.setting_btn5).setOnClickListener(btnEvent); //닫기
        findViewById(R.id.setting_ignore_bles).setOnClickListener(btnEvent);
        findViewById(R.id.setting_add_bles).setOnClickListener(btnEvent);

        ToggleButton showLinkNumberToggleButton = ((ToggleButton) findViewById(R.id.setting_show_link_number));
        showLinkNumberToggleButton.setChecked(isLinkNumberShowing(context));
        showLinkNumberToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setLinkNumberShowing(context, isChecked);
            }
        });
    }

    private View.OnClickListener btnEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.setting_btn2) {
                viewDialog(R.id.setting_btn2, "민감도 설정");
            } else if (id == R.id.setting_btn3) {
                viewDialog(R.id.setting_btn3, "수집주기 설정");
            } else if (id == R.id.setting_btn4) {
                viewDialog(R.id.setting_btn4, "보폭 설정");
            } else if (id == R.id.setting_btn5) {
                cancel();
            } else if (id == R.id.setting_ignore_bles) {
                context.startActivity(new Intent(context, BleIgnoreSettingActivity.class));
            } else if (id == R.id.setting_add_bles) {
                context.startActivity(new Intent(context, BleAddSettingActivity.class));
            }
        }

        void viewDialog(int id, String title) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(title);

            if (id == R.id.setting_btn2) {

            } else if (id == R.id.setting_btn3) {
                String[] arr = {"최단주기", "2", "3", "4", "5"};
                dialog.setItems(arr, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
            } else if (id == R.id.setting_btn4) {
                String[] arr = {"30", "35", "40", "45", "50", "55", "60", "65", "70"};
                WataLog.i("check");
                dialog.setItems(arr, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
						WataLog.d("which"+ which);


                    }
                });
            }

            dialog.show();
        }
    };

    private static final String KEY = "SettingDialog";
    public static final String SETTING_LINK_NUMBER_SHOWING = "SETTING_LINK_NUMBER_SHOWING";

    public static boolean isLinkNumberShowing(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(SETTING_LINK_NUMBER_SHOWING, true);
    }

    public static void setLinkNumberShowing(Context context, boolean showing) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SETTING_LINK_NUMBER_SHOWING, showing);
        editor.commit();
    }
}
