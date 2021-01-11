package com.geotwo.LAB_TEST.Gathering;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geotwo.LAB_TEST.Gathering.ui.RecordListAdapter;
import com.geotwo.LAB_TEST.Gathering.ui.SwipeAnimationButton;
import com.geotwo.LAB_TEST.Gathering.ui.SwipeAnimationListener;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.wata.LAB_TEST.Gathering.R;

public class PathDrawingNewActivity extends Activity implements OnClickListener  {
    static {
        System.loadLibrary("proj");
    }

    private boolean ROAD_MODE = false;
    private String SAVE_FILE_NAME = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WataLog.i("onCreate");

        setContentView(R.layout.path_drawing_new_layout);

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getExtras().getString("file_name");
            SAVE_FILE_NAME = name;
            WataLog.d("name=" + name);
            ROAD_MODE = intent.getBooleanExtra("load_file", false);
            WataLog.d("ROAD_MODE=" + ROAD_MODE);
        }

        initNewLayout();

    }

    @Override
    public void onResume() {
        WataLog.i("onResume");

    }


    private RelativeLayout recordTypeLayout, recordLineNumLayout, directionTextLayout,  btCompass, poiModeImg;
    private TextView recordTypeText, recordLineNumText, recordStepCount, recordStepLength, directionText;
    private ImageView optionBtn;
    private LinearLayout undoBtn, settingBtn;
    private View poiModeImgBtn, bt_compass_btn;
    static public Handler _UIHandler = null;

    private void initNewLayout() {

        SwipeAnimationButton bsb = (SwipeAnimationButton) findViewById(R.id.swipe_btn);
        bsb.setOnSwipeAnimationListener(new SwipeAnimationListener() {
            @Override
            public void onSwiped(String position, String isRecord) {
                if (Constance.CENTER_BTN.equals(position)) {
                    WataLog.i("가운데");
                } else if (Constance.RIGHT_BTN.equals(position)) {
                    WataLog.i("오른쪽");
                } else if (Constance.LEFT_BTN.equals(position)) {
                    WataLog.i("왼쪽");
                }
            }

        });

        // 기록타입
        recordTypeLayout = (RelativeLayout) findViewById(R.id.record_type_layout);
        recordTypeText = (TextView) findViewById(R.id.record_type_text);

        // 기록넘버
        recordLineNumLayout = (RelativeLayout) findViewById(R.id.record_line_num_layout);
        recordLineNumText = (TextView) findViewById(R.id.record_line_num_text);

        recordStepCount = (TextView) findViewById(R.id.record_step_count);
        recordStepLength = (TextView) findViewById(R.id.record_step_length);

        optionBtn = (ImageView) findViewById(R.id.option_btn);
        optionBtn.setOnClickListener(this);
        // 기록방향
        directionTextLayout = (RelativeLayout) findViewById(R.id.direction_text_layout);
        directionText = (TextView) findViewById(R.id.direction_text);

        poiModeImg = (RelativeLayout) findViewById(R.id.poi_mode_img);
        btCompass = (RelativeLayout) findViewById(R.id.compass_mode_img);

//        poiModeImgBtn = (View) findViewById(R.id.poi_mode_btn);
//        poiModeImgBtn.setOnClickListener(this);
//        bt_compass_btn = (View) findViewById(R.id.bt_compass_btn);
//        bt_compass_btn.setOnClickListener(this);

        undoBtn = (LinearLayout) findViewById(R.id.ic_undo_btn);
        undoBtn.setOnClickListener(this);
        settingBtn = (LinearLayout) findViewById(R.id.ic_setting_btn);
        settingBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.poi_mode_btn :
//                WataLog.i("poi btn");
//                break;
//            case R.id.bt_compass_btn :
//                WataLog.i("compass btn");
//                break;
            case R.id.ic_undo_btn :
                WataLog.i("취소");
                break;
            case R.id.ic_setting_btn :
                WataLog.i("설정");
                break;
            case R.id.option_btn:
                WataLog.i("옵션");
                break;
        }
    }


}
