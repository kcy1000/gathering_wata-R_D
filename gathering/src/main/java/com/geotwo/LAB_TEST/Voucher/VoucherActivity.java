package com.geotwo.LAB_TEST.Voucher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Cell.CellManager;
import com.geotwo.LAB_TEST.Gathering.BleAddSettingActivity;
import com.geotwo.LAB_TEST.Gathering.BleIgnoreSettingActivity;
import com.geotwo.LAB_TEST.Gathering.DataCore;
import com.geotwo.LAB_TEST.Gathering.GatherListActivity;
import com.geotwo.LAB_TEST.Gathering.GatheringActivity;
import com.geotwo.LAB_TEST.Gathering.PathDWifiScanner;
import com.geotwo.LAB_TEST.Gathering.PathDrawingActivity;
import com.geotwo.LAB_TEST.Gathering.PathDrawingOldActivity;
import com.geotwo.LAB_TEST.Gathering.PathDrawingSettingDialog;
import com.geotwo.LAB_TEST.Gathering.Retrofit.RetrofitExService;
import com.geotwo.LAB_TEST.Gathering.dialog.SettingDialog;
import com.geotwo.LAB_TEST.Gathering.dto.PoiInfo;
import com.geotwo.LAB_TEST.Gathering.savedata.LoadGatheredData;
import com.geotwo.LAB_TEST.Gathering.savedata.SaveGatheredData;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.Voucher.Retrofit.CategoryInfo;
import com.geotwo.LAB_TEST.Voucher.Retrofit.SubwayLineInfo;
import com.geotwo.LAB_TEST.data.StaticManager;
import com.geotwo.LAB_TEST.Cell.wtCellInfo;
import com.geotwo.TBP.TBPWiFiScanner;
import com.geotwo.common.JsonUtils;
import com.geotwo.o2mapmobile.geometry.Angle;
import com.geotwo.o2mapmobile.geometry.Vector;
import com.geotwo.o2mapmobile.model.SandboxModel;
import com.geotwo.o2mapmobile.shape.Geometry;
import com.geotwo.o2mapmobile.shape.Point;
import com.geotwo.o2mapmobile.shape.Points;
import com.geotwo.o2mapmobile.shape.Polyline;
import com.wata.LAB_TEST.Gathering.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Time;
import java.text.BreakIterator;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA_VOUCHER;
import geo2.lbsp.ible.BLEManager;
import geo2.lbsp.ible.ServiceReadyCallback;
import geo2.lbsp.ible.StartCompletedListener;
import okhttp3.ResponseBody;
import pdr_collecting.core.pdrvariable;
import pdr_collecting.core.sensoract;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

//import com.geotwo.o2mapmobile.shape.CSFReader;

//import geo2.lbsp.ble.BLEManager;

//import geo2.lbsp.ble.BLEManager;

public class VoucherActivity extends AppCompatActivity implements View.OnClickListener {

    static {
        System.loadLibrary("proj");
    }

    // 기록중인 상태를 확인
    private boolean IS_RECORDING = false;
    // 스텝 카운터
    private int IS_STEP_COUNT = 1;
    // 기록 시작점
    public static double START_POINT_X = 0;
    public static double START_POINT_Y = 0;
    // 기록 끝점
    public static double END_POINT_X = 0;
    public static double END_POINT_Y = 0;

    // 수집지점
    public static double SCAN_POINT_X = 0.0;
    public static double SCAN_POINT_Y = 0.0;

    // 시작점 끝점을 vector로 관리
    public static Vector START_POINT_VECTOR = null;
    public static Vector END_POINT_VECTOR = null;

    // 역방향 기록
    public static boolean REVERSE_RECORDING = false;

    //기록되는 라인정보
    public static Polyline POLY_LINE = null;

    //기록중인 라인 포지션
    public static int NOW_POSITION = -1;

    private Polyline lineSelected;
    private ProgressBar progress;
    private double mStepLength = 0;
    private ListView mRecordListview, mPoiListview;

    private boolean ON_POI_MODE = false;
    private long mRelativeX = 0;
    private long mRelativeY = 0;

    private String Sid = "", mNumLine = "", mSubName = "", mFloorsLever = "", mStationNo = "";
    private Spinner mPoiSpinner;
    private String mDataType;

    private CellManager cellManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WataLog.i("onCreate");
        setContentView(R.layout.voucher_layout);

        Intent intent = getIntent();
        WataLog.i("intent=" + intent);

        if (intent != null) {
            mDataType = intent.getExtras().getString("data_type");
            if ("subway".equals(mDataType)) {
                setSybway(intent);
            } else {
                setOffice(intent);
            }
            // test 4층
//            myUrl = "http://54.180.223.80:8080/watta_map/collect2.jsp";
        }
        setInitLayout();
        setDataCore();
    }

    private void setSybway(Intent intent) {
        String gid = intent.getExtras().getString("gid");
        Sid = intent.getExtras().getString("s_id");
        mNumLine = intent.getExtras().getString("num_line");  // 호선정보
        mSubName = intent.getExtras().getString("sub_name");  // 역사이름
        mFloorsLever = intent.getExtras().getString("floors_lever"); // 층수정보
        mStationNo = intent.getExtras().getString("station_no"); // 역사번호

        // 라인정보 가져오기
        getSubwayInfo(Sid, mNumLine, mSubName, mFloorsLever);

        //gid 설정
        int gidLen = 25 - gid.length();
        String W = "W";
        for (int g = 0; g < gidLen; g++) {
            gid = gid.concat(W);
        }

        StaticManager.setGid(gid);
//            StaticManager.setGid("1168010100108210001S01300");
        StaticManager.setTitle(mSubName + "_" + mFloorsLever + "층");

        // 경로설정
        StaticManager.setFolderName(getAreaName(Sid));
        StaticManager.setFloorName(mNumLine);
        StaticManager.setSubwayName(mSubName);
        StaticManager.setAddress(mSubName);
        StaticManager.setFloorLevel(mFloorsLever);

        String centerX = intent.getExtras().getString("center_x");
        String centerY = intent.getExtras().getString("center_y");
        String sX = intent.getExtras().getString("sx");
        String sY = intent.getExtras().getString("sy");

//        onToast(Gravity.CENTER, sX + "//" + sY);
        if (!"null".equals(sX) && !"".equals(sX)) {
            mRelativeX = Long.valueOf(sX);
            mRelativeY = Long.valueOf(sY);
        } else {
            mRelativeX = 0;
            mRelativeY = 0;
        }

        String fileurl = intent.getExtras().getString("fileurl");
        String cx = intent.getExtras().getString("cx");
        String cy = intent.getExtras().getString("cy");
        String rotate = intent.getExtras().getString("rotate");
        String scalex = intent.getExtras().getString("scalex");
        String scaley = intent.getExtras().getString("scaley");

        String lineName = intent.getExtras().getString("line_name");

        StaticManager.setBasePointX(centerX);
        StaticManager.setBasePointX(centerY);
        StaticManager.setPosition(0);

        //개발
//        myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/collect2.jsp?xpos=" + centerX + "&ypos=" + centerY + "&fileurl=" + fileurl
//                + "&cx=" + cx + "&cy=" + cy + "&rotate=" + rotate + "&scalex=" + scalex + "&scaley=" + scaley + "&linenm=" + lineName;

        if (Constance.BEFORE) {
            myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/collect.jsp?xpos=" + centerX + "&ypos=" + centerY + "&fileurl=" + fileurl
                    + "&cx=" + cx + "&cy=" + cy + "&rotate=" + rotate + "&scalex=" + scalex + "&scaley=" + scaley + "&linenm=" + lineName;
        } else {
            myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/collectED.jsp?areano=" + Sid + "&lineno=" + mNumLine + "&stationno=" + mStationNo + "&floor=" + mFloorsLever;
        }


        WataLog.d("myUrl==" + myUrl);
    }

    private void setOffice(Intent intent) {

        String gid = intent.getExtras().getString("gid");
        String O_Id = intent.getExtras().getString("oid");
        String area_no = intent.getExtras().getString("area_no");  // 지역코드
        String area_nm = intent.getExtras().getString("area_nm");  // 지역명
        String company_nm = intent.getExtras().getString("company_nm"); // 기업명
        String copany_no = intent.getExtras().getString("copany_no"); // 기업명
        String floor_info = intent.getExtras().getString("floor_info"); // 충정보
        String center_x = intent.getExtras().getString("center_x"); // 중심점
        String center_y = intent.getExtras().getString("center_y"); // 중심점
        String sx = intent.getExtras().getString("sx"); // 원점
        String sy = intent.getExtras().getString("sy"); // 원점

        WataLog.d("gid=" + gid);
        WataLog.d("O_Id=" + O_Id);
        WataLog.d("area_no=" + area_no);
        WataLog.d("area_nm=" + area_nm);
        WataLog.d("company_nm=" + company_nm);
        WataLog.d("copany_no=" + copany_no);
        WataLog.d("floor_info=" + floor_info);
        WataLog.d("center_x=" + center_x);
        WataLog.d("center_y=" + center_y);
        WataLog.d("sx=" + sx);
        WataLog.d("sy=" + sy);

        // 라인정보 가져오기
        getSubwayInfo(area_no, "0", company_nm, floor_info);

            //            myUrl 셋팅 - 지하철 중신으로 맵 호출
//            myUrl = "http://13.209.43.191:8080/watta_map/collect.jsp";

//            if(mRelativeX == 14149288) {
//                myUrl = "http://13.209.43.191:8080/watta_map/collect2.jsp";
//            } else {
//            myUrl = "http://13.209.43.191:8080/watta_map/collect.jsp?xpos=" + centerX + "&ypos=" + centerY + "&fileurl=" + fileurl
//                    + "&cx=" + cx + "&cy=" + cy + "&rotate=" + rotate + "&scalex=" + scalex + "&scaley=" + scaley + "&linenm=" + lineName;
//            } else {옴
            /*********
            //myUrl = "http://13.209.43.191:8080/watta_map/collect.jsp?xpos=" + centerX + "&ypos=" + centerY + "&fileurl=" + fileurl
            //        + "&cx=" + cx + "&cy=" + cy + "&rotate=" + rotate + "&scalex=" + scalex + "&scaley=" + scaley + "&linenm=" + lineName;
             ****/
//            }

        //gid 설정
        int gidLen = 25 - gid.length();
        String W = "W";
        for (int g = 0; g < gidLen; g++) {
            gid = gid.concat(W);
        }

        setInitLayout();
        setDataCore();


        StaticManager.setGid(gid);
//            StaticManager.setGid("1168010100108210001S01300");
        StaticManager.setTitle(company_nm + "_" + floor_info + "층");

        // 경로설정
        StaticManager.setFolderName(getAreaName(O_Id));
        StaticManager.setFloorName(company_nm + "_" + floor_info + "층");
        StaticManager.setSubwayName(company_nm);
        StaticManager.setAddress(company_nm);
        StaticManager.setFloorLevel(floor_info);

        mRelativeX = Long.valueOf(sx);
        mRelativeY = Long.valueOf(sy);

        StaticManager.setBasePointX(center_x);
        StaticManager.setBasePointX(center_y);

        StaticManager.setPosition(0);

//        myUrl = "http://13.209.43.191:8080/watta_map/collect.jsp?xpos=" + center_x + "&ypos=" + center_y + "&linenm=" + company_nm;
        // 개발 서버 Test
        myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/collect2.jsp?xpos=" + center_x + "&ypos=" + center_y + "&linenm=" + company_nm;
        WataLog.d("myUrl==" + myUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStepLength = pdrvariable.getStep_length();
        WataLog.d("mStepLength=" + mStepLength);

        SharedPreferences pref = getSharedPreferences("log_message", 0);
        setLOG = pref.getString("log_message", "false");
        WataLog.d("setLOG=" + setLOG);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onBackPressed() {
//        super.onBackPressed();
        WataLog.d("voucherRecoding.getVisibility()= " + voucherRecoding.getVisibility());
        if (voucherRecoding.getVisibility() == View.VISIBLE) {
            voucherRecoding.setVisibility(View.GONE);
        } else {
//            DataCore.isOnGathering = false;
            AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
            alBuilder.setMessage(" 기록을 종료하시겠습니까?");
            alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            alBuilder.setTitle("기록 종료");
            alBuilder.show();
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (wifiScanner != null)
            unregisterReceiver(wifiScanner);
        if (_wifiScanner != null) {
            _wifiScanner.stopStatListener();
            _wifiScanner.stopScan();
            // wifiScanner.setWifiOff();
            _wifiScanner = null;
        }

        sensorSet(false);

        StaticManager.setEmptyAll();

        gatheringRelease();
    }

    private TextView recordLineNumText, recordStepCount, recordStepLength, directionText, startBtText, progressText, poiNum;
    private RelativeLayout optionBtn, directionTextLayout, start_btn, step_btn, line_list_layout, undoBtn, poiModeImgBtn, poi_point_layout,
            poi_edit_box_layout, poiFileUploadBtn, fileUploadBtn;
    private View greenSignalIcon, redSignalIcon, voucherRecoding, recordLine, poiLine;
    private LinearLayout greenSignal, redSignal;
    private ImageView poiModeImg;
    private EditText poi_edit, poi_edit_text;

    private ImageView mPotoImageView1, mPotoImageView2;
    private Button cameraBtn1, cameraBtn2;

    private static TextView wifi_log_text;

    private void setInitLayout() {
        webViewSetting();

        wifi_log_text = (TextView) findViewById(R.id.wifi_log_text);

        // 라인번호
        recordLineNumText = (TextView) findViewById(R.id.record_line_num_text);
        //스텝카운터
        recordStepCount = (TextView) findViewById(R.id.record_step_count);
        //스텝총길이
        recordStepLength = (TextView) findViewById(R.id.record_step_length);
        //옵션메뉴
        optionBtn = (RelativeLayout) findViewById(R.id.option_btn);
        optionBtn.setOnClickListener(this);
        //기록방향
        directionTextLayout = (RelativeLayout) findViewById(R.id.direction_text_layout);
        //기록방향 Text
        directionText = (TextView) findViewById(R.id.direction_text);
        //속도 시그날
        greenSignalIcon = (View) findViewById(R.id.green_signal_icon);
        redSignalIcon = (View) findViewById(R.id.red_signal_icon);
        greenSignal = (LinearLayout) findViewById(R.id.green_signal);
        redSignal = (LinearLayout) findViewById(R.id.red_signal);

        //기록경로 취소
//        undoBtn = (RelativeLayout) findViewById(R.id.ic_undo_btn);
//        undoBtn.setOnClickListener(this);
        // 기록시작, 중지
        start_btn = (RelativeLayout) findViewById(R.id.start_btn);
        startBtText = (TextView) findViewById(R.id.start_bt_text);
        start_btn.setOnClickListener(this);
        // 스텝
        step_btn = (RelativeLayout) findViewById(R.id.step_btn);
        step_btn.setOnClickListener(this);
        //경로설정
        line_list_layout = (RelativeLayout) findViewById(R.id.line_list_layout);
        line_list_layout.setOnClickListener(this);
        // progres bas
        progress = (ProgressBar) findViewById(R.id.progress);
        progressText = (TextView) findViewById(R.id.progress_text);

        poiModeImgBtn = (RelativeLayout) findViewById(R.id.poi_mode_img_layout);
        poiModeImgBtn.setOnClickListener(this);
        poiModeImg = (ImageView) findViewById(R.id.poi_mode_img);

        //poi layout
        poi_point_layout = (RelativeLayout) findViewById(R.id.poi_point_layout);
        poi_point_layout.setVisibility(View.GONE);
        poi_edit = (EditText) findViewById(R.id.poi_edit);

        RelativeLayout poi_cancel = (RelativeLayout) findViewById(R.id.poi_cancel);
        poi_cancel.setOnClickListener(this);

        RelativeLayout poi_cancel_btn = (RelativeLayout) findViewById(R.id.poi_cancel_btn);
        poi_cancel_btn.setOnClickListener(this);
        RelativeLayout poi_add_btn = (RelativeLayout) findViewById(R.id.poi_add_btn);
        poi_add_btn.setOnClickListener(this);

        poiNum = (TextView) findViewById(R.id.poi_num);

        // poi수정
        poi_edit_box_layout = (RelativeLayout) findViewById(R.id.poi_edit_box_layout);
        poi_edit_box_layout.setVisibility(View.GONE);
        poi_edit_text = (EditText) findViewById(R.id.poi_edit_text);
        Button poi_save_btn = (Button) findViewById(R.id.poi_save_btn);
        poi_save_btn.setOnClickListener(this);


        fileUploadBtn = (RelativeLayout) findViewById(R.id.file_upload_btn);
        fileUploadBtn.setOnClickListener(this);

        poiFileUploadBtn = (RelativeLayout) findViewById(R.id.poi_file_upload_btn);
        poiFileUploadBtn.setOnClickListener(this);

        //poi 카테코리
        mPoiSpinner = (Spinner) findViewById(R.id.poi_spinner);
        mPoiSpinner.setPrompt("카테고리를 선택하세요.");


        // 레이아웃과 변수 연결
        mPotoImageView1 = findViewById(R.id.poto_img_1);
        cameraBtn1 = findViewById(R.id.camera_button_1);

        mPotoImageView2 = findViewById(R.id.poto_img_2);
        cameraBtn2 = findViewById(R.id.camera_button_2);

        cameraBtn1.setOnClickListener(this);
        cameraBtn2.setOnClickListener(this);


        // 경로선택 layout
        voucherRecoding = findViewById(R.id.voucher_recoding);

        RelativeLayout title_layout = (RelativeLayout) voucherRecoding.findViewById(R.id.title_layout);
        title_layout.setOnClickListener(this);

        ImageView arrow_left = (ImageView) voucherRecoding.findViewById(R.id.arrow_left);
        arrow_left.setOnClickListener(this);

        RelativeLayout record_list_layout = (RelativeLayout) voucherRecoding.findViewById(R.id.record_list_layout);
        record_list_layout.setOnClickListener(this);

        RelativeLayout poi_list_layout = (RelativeLayout) voucherRecoding.findViewById(R.id.poi_list_layout);
        poi_list_layout.setOnClickListener(this);

        recordLine = (View) voucherRecoding.findViewById(R.id.record_line);
        poiLine = (View) voucherRecoding.findViewById(R.id.poi_line);

        // 기록 listview
        mRecordListview = (ListView) voucherRecoding.findViewById(R.id.record_listview);
        // poi listview
        mPoiListview = (ListView) voucherRecoding.findViewById(R.id.poi_listview);

        mRecordAdapter = new RecordAdapter(this);
        mRecordAdapter.setOnItemClickListener(new RecordAdapter.OnItemClickListner() {
            @Override
            public void onRecordStartPoint(SubwayLineInfo items, int position) {
                // 해당 지점으로 이동
                voucherRecoding.setVisibility(View.GONE);
                REVERSE_RECORDING = false;

                WataLog.d("mSubwayInfo.get(position).getStX()=" + mSubwayInfo.get(position).getStX());
                WataLog.d("mSubwayInfo.get(position).getStY()=" + mSubwayInfo.get(position).getStY());
                WataLog.d("mSubwayInfo.get(position).getEdX()=" + mSubwayInfo.get(position).getEdX());
                WataLog.d("mSubwayInfo.get(position).getEdY()=" + mSubwayInfo.get(position).getEdY());

//                setWebView("drawPosition", mSubwayInfo.get(position).getStX() + "|" + mSubwayInfo.get(position).getStY());
//                setWebView("drawPosition",  mSubwayInfo.get(position).getEdX() + "|" + mSubwayInfo.get(position).getEdY());

                START_POINT_X = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getStX()) - mRelativeX) * 10);
                START_POINT_Y = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getStY()) - mRelativeY) * 10);
                END_POINT_X = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getEdX()) - mRelativeX) * 10);
                END_POINT_Y = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getEdY()) - mRelativeY) * 10);

                WataLog.d("START_POINT_X=" + START_POINT_X);
                WataLog.d("START_POINT_Y=" + START_POINT_Y);
                SCAN_POINT_X = START_POINT_X;
                SCAN_POINT_Y = START_POINT_Y;

                WataLog.d("START_POINT_X=" + new DecimalFormat("#########.#####").format(START_POINT_X));
                WataLog.d("START_POINT_Y=" + new DecimalFormat("#########.#####").format(START_POINT_Y));
                WataLog.d("START_POINT_X=" + new DecimalFormat("#########.#####").format(END_POINT_X));
                WataLog.d("START_POINT_Y=" + new DecimalFormat("#########.#####").format(END_POINT_Y));

                NOW_POSITION = position;
                viewEachPath();
                // 기록할 라인번호 설정
                StaticManager.setPathNum("" + (position + 1));

                WataLog.d("StaticManager.getPathNum=" + StaticManager.pathNum);

                double startPX = Double.valueOf(mSubwayInfo.get(position).getStX());
                double startPY = Double.valueOf(mSubwayInfo.get(position).getStY());
                double endPX = Double.valueOf(mSubwayInfo.get(position).getEdX());
                double endPY = Double.valueOf(mSubwayInfo.get(position).getEdY());

                String spx = String.valueOf((int) startPX);
                String spy = String.valueOf((int) startPY);
                String epx = String.valueOf((int) endPX);
                String epy = String.valueOf((int) endPY);

                setWebView("moveMapline", spx + "|" + spy + "|" + epx + "|" + epy + "|" + mSubwayInfo.get(position).getLineSerno());

//                setWebView("drawPosition", spx + "|" + spy);
//                setWebView("drawPosition", epx + "|" + epy);

                //webview 호출
//                setWebView("moveMapline", mSubwayInfo.get(position).getStX() + "|" + mSubwayInfo.get(position).getStY()
//                        + "|" + mSubwayInfo.get(position).getEdX() + "|" + mSubwayInfo.get(position).getEdY() + "|" + mSubwayInfo.get(position).getLineSerno());


                directionText.setText("정방향");
            }

            @Override
            public void onRecordEndPoint(SubwayLineInfo items, int position) {
                // 해당 지점으로 이동
                voucherRecoding.setVisibility(View.GONE);
                REVERSE_RECORDING = true;

                END_POINT_X = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getStX()) - mRelativeX) * 10);
                END_POINT_Y = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getStY()) - mRelativeY) * 10);
                START_POINT_X = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getEdX()) - mRelativeX) * 10);
                START_POINT_Y = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getEdY()) - mRelativeY) * 10);

                WataLog.d("START_POINT_X=" + new DecimalFormat("#########.#####").format(START_POINT_X));
                WataLog.d("START_POINT_Y=" + new DecimalFormat("#########.#####").format(START_POINT_Y));
                WataLog.d("START_POINT_X=" + new DecimalFormat("#########.#####").format(END_POINT_X));
                WataLog.d("START_POINT_Y=" + new DecimalFormat("#########.#####").format(END_POINT_Y));

                WataLog.d("START_POINT_X=" + START_POINT_X);
                WataLog.d("START_POINT_Y=" + START_POINT_Y);
                SCAN_POINT_X = START_POINT_X;
                SCAN_POINT_Y = START_POINT_Y;

                NOW_POSITION = position;
                viewEachPath();
                // 기록할 라인번호 설정
                StaticManager.setPathNum("" + (position + 1));

                double startPX = Double.valueOf(mSubwayInfo.get(position).getStX());
                double startPY = Double.valueOf(mSubwayInfo.get(position).getStY());
                double endPX = Double.valueOf(mSubwayInfo.get(position).getEdX());
                double endPY = Double.valueOf(mSubwayInfo.get(position).getEdY());

                String spx = String.valueOf((int) startPX);
                String spy = String.valueOf((int) startPY);
                String epx = String.valueOf((int) endPX);
                String epy = String.valueOf((int) endPY);

                setWebView("moveMapline", epx + "|" + epy + "|" + spx + "|" + spy + "|" + mSubwayInfo.get(position).getLineSerno());

//                setWebView("drawPosition", spx + "|" + spy);
//                setWebView("drawPosition", epx + "|" + epy);


                //webview 호출
//                setWebView("moveMapReverse", mSubwayInfo.get(position).getEdX() + "|" + mSubwayInfo.get(position).getEdY()
//                        + "|" + mSubwayInfo.get(position).getStX() + "|" + mSubwayInfo.get(position).getStY()
//                        + "|" + mSubwayInfo.get(position).getLineSerno());

                directionText.setText("역방향");
            }
        });

        // poi 내역
        mPoiListAdapter = new VoucherPoiListAdapter(this);
        mPoiListAdapter.setOnItemClickListener(new VoucherPoiListAdapter.OnItemClickListner() {

            @Override
            public void onPoiDelete(final PoiInfo items, final int position) {
                WataLog.d("삭제position=" + position);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setDeletePOIPopup(items, position);
                    }
                });
            }

            @Override
            public void onPoiNameEdit(PoiInfo items, int position) {
                //추후반영
                WataLog.d("position=" + position);

                mPoiPosition = position;
                mPoiItem = items;
                // poi 이름변경
                poi_edit_box_layout.setVisibility(View.VISIBLE);

            }
        });


        _bleManager = new BLEManager(this);

        if (!_bleManager.isBluetoothEnabled()) {
            geo2.lbsp.ible.Utils.startBluetooth(this, new StartCompletedListener() {
                public void onStartCompleted() {
                    Toast.makeText(VoucherActivity.this, "'Bluetooth가 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        _bleManager.connect(new ServiceReadyCallback() {
            public void onServiceReady() {
                if (_bleManager != null)
                    _bleManager.startService();
            }
        });

        getPoiCategory();
    }

    private int mPoiPosition;
    private PoiInfo mPoiItem;

    private ArrayList<CategoryInfo> mPoiArrList = new ArrayList<CategoryInfo>();
    private ArrayList<String> mPoicategoryList = new ArrayList<String>();

    private void getPoiCategory() {
        WataLog.i("getPoiCategory");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        retrofitExService.categoryInfos().enqueue(new Callback<ResponseBody>() {

            //        retrofitExService.mapGpsSetting("139.7001198", "35.6895075").enqueue(new Callback<List<SubwayAreaData>>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터

                    JSONArray jsonArray = new JSONArray(result);
                    mPoiArrList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String category_code = jsonObject.getString("category_code");
                        String category_nm = jsonObject.getString("category_nm");

//                        WataLog.d("category_nm=" + category_nm);
                        mPoiArrList.add(new CategoryInfo(category_code, category_nm));
                        mPoicategoryList.add(category_nm);
                    }
                    setAreaList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("onFailure");
            }
        });
    }

    private String mCategoryNm = "0";

    private void setAreaList() {
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text_item, mPoicategoryList);
        mPoiSpinner.setAdapter(areaAdapter);
        mPoiSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WataLog.d("position=" + position);

                mCategoryNm = mPoiArrList.get(position).getCategoryCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }


    private void setProgress(boolean isOn) {
        if (isOn) {
            progress.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
        } else {
            progress.setVisibility(View.GONE);
            progressText.setVisibility(View.GONE);
        }
    }

    private Toast mToast = null;

    //    Gravity.CENTER
    private void onToast(int which, String message) {
        if (mToast == null) {
            mToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        } else {
            mToast.cancel();
            mToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);

        }
        mToast.setGravity(which, 0, 0);
        mToast.show();
    }

    private WebView mWebView;
    private String myUrl = "";
    private VoucherActivity.VoucherWebBridge mVoucherWebBridge;

    @SuppressLint("SetJavaScriptEnabled")
    private void webViewSetting() {
        // 웹뷰 셋팅팅
        mWebView = (WebView) findViewById(R.id.voucher_webview);
        mVoucherWebBridge = new VoucherActivity.VoucherWebBridge(VoucherActivity.this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.addJavascriptInterface(mVoucherWebBridge, "WATA");
        mWebView.setWebViewClient(new VoucherActivity.WebViewClientClass());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                WataLog.d("message=" + message);
                WataLog.d("result=" + result);
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Alert")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirm")
                        .setMessage(message)
                        .setPositiveButton("Yes",
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("No",
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }
        });

        WataLog.d("myUrl=" + myUrl);


//        http://13.209.43.191:3000/subwayGuideLines?&areaNo=1&lineNo=0&stationnm=BCC&floor=P
        mWebView.loadUrl(myUrl);
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            WataLog.d("url=" + url);
            view.loadUrl(url);
            return true;
        }
    }

    class VoucherWebBridge {
//        public double startPointX = 0.0;
//        public double startPointY = 0.0;
//        public double endPointX = 0.0;
//        public double endPointY = 0.0;

        private VoucherActivity mVoucherActivity;

        public VoucherWebBridge(VoucherActivity voucherActivity) {
            mVoucherActivity = voucherActivity;
        }

        // web지도가 정상적으로 보여지면 호출됨
        @JavascriptInterface
        public void readyToDraw() {
            WataLog.d("readyToDraw");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVoucherActivity.setLoadMapLine();
                }
            });
        }

        @JavascriptInterface
        public void setHerePoint(final String pointX, final String pointY, final boolean overLine) {
            WataLog.d("setHerePoint");
            WataLog.d("pointX=" + pointX + "///pointY=" + pointY + "///overLine=" + overLine);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVoucherActivity.setPointSetting(pointX, pointY, overLine);
                }
            });
        }

        @JavascriptInterface
        public void setPOI(final String pointX, final String pointY) {
            WataLog.d("pointX=" + pointX);
            WataLog.d("pointY=" + pointY);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVoucherActivity.setPoiText(pointX, pointY);
                }
            });
        }
    }

    private void setWebView(String webId) {
        WataLog.i("webId=" + webId);
        mWebView.loadUrl("javascript:" + webId + "()");
    }

    private void setWebView(String webId, String value) {
//        if ("setRotation".equals(webId) || "drawLineCoords".equals(webId)) {
        if ("setRotation".equals(webId)) {
        } else {
            WataLog.i("webId=" + webId + "//" + value);
        }
        mWebView.loadUrl("javascript:" + webId + "('" + value + "')");

    }

    private int POI_COUNT = 1;
    private String POI_pointX;
    private String POI_pointY;

    private void setPoiText(String pointX, String pointY) {
        POI_pointX = pointX;
        POI_pointY = pointY;
        poi_edit.setText("");
        WataLog.d("POI_COUNT=" + POI_COUNT);
        poiNum.setText("No.# " + String.valueOf(POI_COUNT));

        poi_point_layout.setVisibility(View.VISIBLE);
    }

    private VoucherPoiListAdapter mPoiListAdapter;
    private ArrayList<PoiInfo> mPoiInfoData = new ArrayList<PoiInfo>();

    // poi등록
    private void setPoiPointMark(String x, String y, String poiText, boolean poiRight) {
        WataLog.i("setPoiPointMark");
        try {
//            int pointCount = mPoiInfoData.size();
            String potoName = "";
            for (int i = 0; i < mSendPotoName.size(); i++) {
                String name = mSendPotoName.get(i);
                if (i == 1) {
                    potoName = "|" + name;
                } else {
                    potoName = name;
                }
            }

            mPoiInfoData.add(new PoiInfo(POI_COUNT, x, y, poiText, mCategoryNm, potoName));
            mPoiListAdapter.setItems(mPoiInfoData);
            mPoiListview.setAdapter(mPoiListAdapter);
            // poi내용저장
        } catch (Exception e) {
            WataLog.e("Exception=" + e.toString());
        }
    }

    private boolean OVER_LINE = false;

    private void setPointSetting(String pointX, String pointY, boolean overLine) {
        WataLog.d("pointX=" + pointX + "///pointY=" + pointY + "///overLine=" + overLine);
//        onToast(Gravity.CENTER, String.valueOf(overLine));
        if (overLine) {
            SCAN_POINT_X = (Math.floor(Double.valueOf(pointX) - mRelativeX) * 10);
            SCAN_POINT_Y = (Math.floor(Double.valueOf(pointY) - mRelativeY) * 10);

            WataLog.d("SCAN_POINT_X=" + SCAN_POINT_X + "///SCAN_POINT_Y=" + SCAN_POINT_Y);

            START_POINT_X = (Math.floor(Double.valueOf(pointX) - mRelativeX) * 10);
            START_POINT_Y = (Math.floor(Double.valueOf(pointY) - mRelativeY) * 10);


            lineHeader = getHeader(START_POINT_VECTOR, END_POINT_VECTOR);
        } else {
            OVER_LINE = true;
        }
    }


    private void setDeletePOIPopup(final PoiInfo items, final int position) {
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(VoucherActivity.this);
        alBuilder.setMessage(getString(R.string.poi_delete_message));
        alBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int num = items.getNumber();
                mPoiInfoData.remove(position);
                mPoiListAdapter.notifyDataSetChanged();

                setWebView("deletePOI", items.getPoiPositionX() + "|" + items.getPoiPositionY());

            }
        });
        alBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alBuilder.show();
    }


    private void setLoadMapLine() {
        for (int i = 0; i < mSubwayInfo.size(); i++) {
            setWebView("drawLineCoords", mSubwayInfo.get(i).getStX() + "|" + mSubwayInfo.get(i).getStY() + "|" + mSubwayInfo.get(i).getEdX() + "|" + mSubwayInfo.get(i).getEdY() + "|" + "line" + "|" + (i + 1));
        }
        getRecordFile();
        getPoiPathData();
    }

    // 수집완료된 내역확인
    private ArrayList<File> getRecordFile() {
        ArrayList<File> items = new ArrayList<File>();
        String dataPath = StaticManager.getResultVoucherPath(); // 파일저장된 경로설정
        WataLog.d("dataPath=" + dataPath);
        File pathFolder = new File(dataPath);

        WataLog.d("pathFolder=" + pathFolder);
        if (pathFolder.exists()) {
            String[] list = pathFolder.list();
            WataLog.d("list=" + list);
            if (list != null) {
                for (int a = 0; a <= list.length - 1; a++) {
                    String name = list[a].toString();
                    name = name.replaceAll(" ", "");
                    WataLog.d("name=" + name);
                    String[] fileName = name.split("\\.");
                    WataLog.d("fileName=" + fileName[0]);
                    // 저장된 경로에서 확장자 json 파일만 가져옴
                    WataLog.d("fileName.length=" + fileName.length);
                    if (fileName.length > 1) {
                        if (fileName[1].equals("log")) {
//                            강남역_ B4층 -2 -4-1104-17:11:72_ F
                            // 기존.
//                            String[] rFile = fileName[0].split("-");
//                            WataLog.d("rFile=" + rFile[0]);
//                            WataLog.d("rFile=" + rFile[1]);
//                            WataLog.d("rFile=" + rFile[2]);

                            String[] rFileName = fileName[0].split("_");
                            WataLog.d("rFileName[0]=" + rFileName[0]);
                            WataLog.d("rFileName[1]=" + rFileName[1]);
                            WataLog.d("rFileName[2]=" + rFileName[2]);

                            // 신규. 파일업로드 후 삭제예정
                            String[] rFile = rFileName[1].split("-");
                            WataLog.d("rFile=" + rFile[0]);
                            WataLog.d("rFile=" + rFile[1]);
                            WataLog.d("rFile=" + rFile[2]);

                            int sPosition = 0;
                            if ("subway".equals(mDataType)) {
                                sPosition = Integer.valueOf(rFile[2]) - 1;
//                                WataLog.d("sPosition=" + sPosition);
                                if ("F".equals(rFileName[2])) {
                                    mSubwayInfo.get(sPosition).setCompletionF(getString(R.string.completion));
                                } else {
                                    mSubwayInfo.get(sPosition).setCompletionR(getString(R.string.completion));
                                }

                            } else {
                                sPosition = Integer.valueOf(rFile[1]) - 1;
//                                WataLog.d("sPosition=" + sPosition);
                                if ("F".equals(rFileName[2])) {
                                    mSubwayInfo.get(sPosition).setCompletionF(getString(R.string.completion));
                                } else {
                                    mSubwayInfo.get(sPosition).setCompletionR(getString(R.string.completion));
                                }
                            }

                            if (mSubwayInfo.get(sPosition).getCompletionF().equals(getString(R.string.completion))) {
                                if (mSubwayInfo.get(sPosition).getCompletionR().equals(getString(R.string.completion))) {
                                    setWebView("drawEndLine", mSubwayInfo.get(sPosition).getStX() + "|" + mSubwayInfo.get(sPosition).getStY()
                                            + "|" + mSubwayInfo.get(sPosition).getEdX() + "|" + mSubwayInfo.get(sPosition).getEdY());
                                } else {
                                    setWebView("drawHalfEndLine", mSubwayInfo.get(sPosition).getStX() + "|" + mSubwayInfo.get(sPosition).getStY()
                                            + "|" + mSubwayInfo.get(sPosition).getEdX() + "|" + mSubwayInfo.get(sPosition).getEdY());
                                }
                            } else {
                                if (mSubwayInfo.get(sPosition).getCompletionF().equals(getString(R.string.completion))) {
                                    setWebView("drawEndLine", mSubwayInfo.get(sPosition).getStX() + "|" + mSubwayInfo.get(sPosition).getStY()
                                            + "|" + mSubwayInfo.get(sPosition).getEdX() + "|" + mSubwayInfo.get(sPosition).getEdY());
                                } else {
                                    setWebView("drawHalfEndLine", mSubwayInfo.get(sPosition).getStX() + "|" + mSubwayInfo.get(sPosition).getStY()
                                            + "|" + mSubwayInfo.get(sPosition).getEdX() + "|" + mSubwayInfo.get(sPosition).getEdY());
                                }
                            }
                        }
                    }
                }
                mRecordAdapter.notifyDataSetChanged();
            }
        } else {
            WataLog.i("파일없음");
        }
        return items;
    }

    static public double lineHeader = Double.NaN;
    private long mStepTempTime = 0;
    private int mFastStepCount = 0;
    private String POI_FILE = "";

    final static int TAKE_PICTURE = 1;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private void checkCameraPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (PackageManager.PERMISSION_GRANTED == permissionCheck) {
//            onCamera();
            dispatchTakePictureIntent();
            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (MY_PERMISSIONS_REQUEST_CAMERA == requestCode) {
//            onCamera();
            dispatchTakePictureIntent();
        }

    }

//    private void onCamera() {
//        // 카메라 앱을 여는 소스
//        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(cameraIntent, TAKE_PICTURE);
//    }


    // 카메라로 촬영한 영상을 가져오는 부분
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case TAKE_PICTURE:
                try {
                    File file = new File(mCurrentPhotoPath);
                    Bitmap bitmap = null;
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));

                    if (bitmap != null) {
                        if (CAMER_NUM == 1) {
                            mPotoImageView1.setImageBitmap(bitmap);
                            mSendPotoName.add(mSavePotoName);
                        } else {
                            mPotoImageView2.setImageBitmap(bitmap);
                            mSendPotoName.add(mSavePotoName);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                break;
        }
    }

    private int CAMER_NUM = 1;
    private String mCurrentPhotoPath;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private ArrayList<String> mSendPotoName = new ArrayList<String>();
    ;
//    private File createImageFile() throws IOException {
//        // Create an image file name
////        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = mSubName + "_" + poi_edit.getText().toString() + "_" + CAMER_NUM;
//        WataLog.d("imageFileName=" + imageFileName);
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        WataLog.d("storageDir=" + storageDir);
//
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//
//        WataLog.d("image=" + image);
//
//        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = image.getAbsolutePath();
//        WataLog.d("mCurrentPhotoPath=" + mCurrentPhotoPath);
//        return image;
//    }


    private String mSavePotoName = "";

    //poi이미지 파일생성
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;

            mSavePotoName = mNumLine + "호선_" + mSubName + "_" + mFloorsLever + "층_" + poi_edit.getText().toString() + "_" + CAMER_NUM;
            WataLog.d("mSavePotoName=" + mSavePotoName);

//            photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + imageFileName + ".jpg");

            File file = new File(StaticManager.getResultVoucherPOIPath());
            if (!file.exists()) {
                file.mkdirs();
            }

            photoFile = new File(StaticManager.getResultVoucherPOIPath() + mSavePotoName + ".jpg");

            mCurrentPhotoPath = photoFile.getAbsolutePath();


            // Continue only if the File was successfully created
            if (photoFile != null) {
                WataLog.d("photoFile=" + photoFile);
                Uri photoURI = FileProvider.getUriForFile(this, "com.wata.LAB_TEST.Gathering.fileprovider", photoFile);
                WataLog.d("photoURI=" + photoURI);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
//            System.gc();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_upload_btn:
                AlertDialog.Builder alBuilder = new AlertDialog.Builder(VoucherActivity.this);
                alBuilder.setMessage(getString(R.string.send_file_message_1));
                alBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setUpload();
                    }
                });
                alBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                alBuilder.show();

                break;

            case R.id.poi_file_upload_btn:
                // poi 전송
                AlertDialog.Builder poiBuilder = new AlertDialog.Builder(VoucherActivity.this);
                poiBuilder.setMessage("poi 파일을 전송하시겠습니까?");
                poiBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressText.setText("파일 업로드 중입니다.");
                        setProgress(true);
                        Date currentTime = Calendar.getInstance().getTime();
                        POI_FILE = new SimpleDateFormat("MMdd-HH:mm:ss", Locale.getDefault()).format(currentTime) + "_" + StaticManager.subwayName;
                        WataLog.d("POI_FILE=" + POI_FILE);

                        for (int i = 0; i < mPoiInfoData.size(); i++) {
                            String poiX = mPoiInfoData.get(i).getPoiPositionX();
                            String poiY = mPoiInfoData.get(i).getPoiPositionY();
                            String poiName = mPoiInfoData.get(i).getPoiText();
                            String potoName = mPoiInfoData.get(i).getPotoName();
                            String categoryCode = mPoiInfoData.get(i).getCategoryCode();
                            WataLog.d("potoName=" + potoName);
                            WataLog.d("categoryCode=" + categoryCode);

                            setPOILog(poiX, poiY, poiName);

                            getSubwayLine(poiX, poiY, poiName, categoryCode, potoName);
                        }

                        String fileName = "POI_" + POI_FILE + ".json";
                        WataLog.d("fileName=" + fileName);
                        HttpMultiPartPoiUpload(fileName);

//                        setProgress(false);
                    }
                });
                poiBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                poiBuilder.show();

                break;

            case R.id.poi_mode_img_layout:
                WataLog.i("poi모드");
                if (ON_POI_MODE) {
                    ON_POI_MODE = false;
                    poiModeImg.setImageResource(R.mipmap.bt_addpoi);
                    setWebView("togglePoiMode");
                    onToast(Gravity.CENTER, getString(R.string.off_poi_mode));

                    fileUploadBtn.setVisibility(View.VISIBLE);
                    poiFileUploadBtn.setVisibility(View.GONE);
                } else {
                    ON_POI_MODE = true;
                    poiModeImg.setImageResource(R.mipmap.bt_addpoi_active);
                    setWebView("togglePoiMode");
                    onToast(Gravity.CENTER, getString(R.string.on_poi_mode));

                    fileUploadBtn.setVisibility(View.GONE);
                    poiFileUploadBtn.setVisibility(View.VISIBLE);

                }
                break;

            case R.id.camera_button_1:
                if (poi_edit.getText().toString().equals("")) {
                    onToast(Gravity.CENTER, "POI 입력을 입려하세요");
                } else {
                    CAMER_NUM = 1;
                    checkCameraPermission();
                }
                break;
            case R.id.camera_button_2:
                if (poi_edit.getText().toString().equals("")) {
                    onToast(Gravity.CENTER, "POI 입력을 입려하세요");
                } else {
                    CAMER_NUM = 2;
                    checkCameraPermission();
                }

                break;
            case R.id.poi_add_btn:  // poi 등록
                WataLog.i("poi 등록");
                String poiRName = poi_edit.getText().toString();
                if (!"".equals(poiRName) && poiRName != null) {

                    mPotoImageView1.setImageBitmap(null);
                    mPotoImageView2.setImageBitmap(null);

                    setPoiPointMark(POI_pointX, POI_pointY, poiRName, true);
                    poi_point_layout.setVisibility(View.GONE);
                    onKeyboardHide(poi_edit);

                    WataLog.d("poiRName=" + poiRName);
                    setWebView("setPOIContent", poiRName);

                    WataLog.d("POI_point", POI_pointX + "," + POI_pointY);
//                    setPOITestLog(POI_pointX, POI_pointY, poiRName);
                    POI_COUNT++;

                } else {
                    Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.poi_cancel_btn:  // 등록취소
                WataLog.i("등록취소");
                WataLog.d("POI_point =" + POI_pointX + "," + POI_pointY);

                setWebView("deletePOI", POI_pointX + "|" + POI_pointY);

                poi_point_layout.setVisibility(View.GONE);

                POI_pointX = "";
                POI_pointY = "";

                mPotoImageView1.setImageBitmap(null);
                mPotoImageView2.setImageBitmap(null);

                break;
            case R.id.poi_save_btn: // poi수정
                String poiName = poi_edit_text.getText().toString();

//                mPoiPosition = position;
//                mPoiItem = items;
                WataLog.d("mPoiPosition=" + mPoiPosition);
                mPoiInfoData.get(mPoiPosition).sePoiText(poiName);
                mPoiListAdapter.notifyDataSetChanged();
                String pointX = mPoiInfoData.get(mPoiPosition).PoiPositionX;
                String pointY = mPoiInfoData.get(mPoiPosition).PoiPositionY;

                setPOILog(pointX, pointY, poiName);

                setWebView("updatePOIContent", pointX + "|" + pointY + "|" + poiName);
//                updatePOIContent(x좌표|y좌표|poi 내용)
//                mWebView.loadUrl("javascript:testPoint('" + pointX + "|" + pointY  + "')");
//                setWebView("moveMapXY" + pointX + "|" + pointY);

                poi_edit_box_layout.setVisibility(View.GONE);
                break;
            case R.id.option_btn:
                WataLog.i("옵션");
                setSetting();
                break;
//            case R.id.ic_undo_btn:
//                WataLog.i("역방향");
//                break;
            case R.id.line_list_layout:
                WataLog.i("경로선택");
                voucherRecoding.setVisibility(View.VISIBLE);
                break;
            case R.id.title_layout:
                voucherRecoding.setVisibility(View.GONE);
                break;
            case R.id.record_list_layout:
                // 수집내역
                recordLine.setVisibility(View.VISIBLE);
                poiLine.setVisibility(View.GONE);

                mRecordListview.setVisibility(View.VISIBLE);
                mPoiListview.setVisibility(View.GONE);
                break;
            case R.id.poi_list_layout:
                // POI내역
                recordLine.setVisibility(View.GONE);
                poiLine.setVisibility(View.VISIBLE);

                mRecordListview.setVisibility(View.GONE);
                mPoiListview.setVisibility(View.VISIBLE);
                break;

            case R.id.start_btn:
                WataLog.i("시작 & 정지");
                if (NOW_POSITION == -1) {
                    onToast(Gravity.CENTER, "수집 경로를 지정해주세요!");
                } else {
                    IS_STEP_COUNT = 1;
                    WataLog.d("IS_RECORDING=" + IS_RECORDING);
                    if (IS_RECORDING) { // 종료
                        onRecordOff();


                    } else { // 시작
                        onRecordOn();
                    }
                }

                break;
            case R.id.step_btn:
                WataLog.i("스텝");
                if (IS_RECORDING) {
                    if (!OVER_LINE) {
                        WataLog.d("mStepLength=" + mStepLength);
                        WataLog.d("IS_STEP_COUNT=" + IS_STEP_COUNT);
                        double travelRange = IS_STEP_COUNT * mStepLength;
                        String travelRange_S = String.format("%.2f", travelRange);
                        recordStepCount.setText(String.valueOf(IS_STEP_COUNT));
                        recordStepLength.setText(travelRange_S + " m");

                        WataLog.d("travelRange_S=" + travelRange_S);

//                setWebView("drawPoint", "signal|" + travelRange_S);
                        setWebView("drawPoint", "pos|" + travelRange_S);

                        IS_STEP_COUNT++;

                        long now = System.currentTimeMillis();
                        long temp = mStepTempTime + 1000;
                        WataLog.d("mStepTempTime + 1000=" + temp + "///// now=" + now);
                        if (mStepTempTime + 1000 > now) {
                            mFastStepCount++;
                        } else {
                            mFastStepCount = 0;
                        }

                        WataLog.d("mFastStepCount=" + mFastStepCount);

                        if (mFastStepCount > 1) {
                            if (startAnimation != null) {
                                startAnimation.cancel();
                            }
                            onToast(Gravity.CENTER, getString(R.string.fast_step_slow_step_message));
                            startRedSignal(false);
                            mFastStepCount = 0;
                        } else {
                            if (startAnimation != null) {
                                startAnimation.cancel();
                            }
                            startRedSignal(true);
                        }

                        mStepTempTime = now;

                    } else {
                        onToast(Gravity.CENTER, getString(R.string.finish_message));
                        onRecordOff();
                    }
                }

                break;
        }
    }


    private void onRecordOff() {
        WataLog.i("onRecordOff");
        startBtText.setText(getString(R.string.start));
        setProgress(false);
        IS_RECORDING = false;

        if (!REVERSE_RECORDING) {
            setWebView("endDrawPoint");
        } else {
            setWebView("endReverse");
        }
        OVER_LINE = false;
        setRecordGathering();

        IS_STEP_COUNT = 0;

        SCAN_POINT_X = 0.0;
        SCAN_POINT_Y = 0.0;

//                    mStepLength = 0;
        recordStepCount.setText("0");
        recordStepLength.setText("0 m");


    }

    private void onRecordOn() {
        progressText.setText("수집 준비중입니다. 잠시만 기디려주세요.");
        setProgress(true);
        startBtText.setText(getString(R.string.stop));
        IS_RECORDING = true;

        OVER_LINE = false;

        startRedSignal(true);

        setWebView("initStartPt");

        // 정북기준 각도값 입력
        lineHeader = 45.0;

        //GatheringDialog.makeGatherNameInsertDailog(GatheringActivity.this, StaticManager.title, StaticManager.floorName, _UIHandler, 9999).show();
        Date currentTime = Calendar.getInstance().getTime();
        String curTime = new SimpleDateFormat("MMdd-HH:mm:ss", Locale.getDefault()).format(currentTime);
        WataLog.d("currentTime=" + currentTime);
        WataLog.d("curTime=" + curTime);

        String lineNum = String.valueOf(NOW_POSITION + 1);
        // 파일이름생성

        String autoName;
        if ("subway".equals(mDataType)) {
            autoName = StaticManager.title + "-" + StaticManager.floorName + "-" + lineNum + "-" + curTime;
        } else {
            autoName = StaticManager.title + "-" + lineNum + "-" + curTime;
        }

        WataLog.d("StaticManager.title=" + StaticManager.title);
        WataLog.d("StaticManager.floorName=" + StaticManager.floorName);
        WataLog.d("lineNum=" + lineNum);
        WataLog.d("autoName=" + autoName);

        VoucherDataCore dataCore = VoucherDataCore.getInstance();

//                  _startPoint = new Vector(line.getPart(0).getStartPoint().x, line.getPart(0).getStartPoint().y, line.getPart(0).getStartPoint().z);
//                  _endPoint = new Vector(line.getPart(0).getEndPoint().x, line.getPart(0).getEndPoint().y, line.getPart(0).getEndPoint().z);
//                  distRemain = -1;
        lineHeader = getHeader(START_POINT_VECTOR, END_POINT_VECTOR);

        pdrvariable.initValues();
//      initialmm.MM_initialize3(line);

        // TODO by Ethan
        // setWiFiRTT
        // setLTE/5G
        setCell(true);

        wifiSet(true);
        sensorSet(true);

        dataCore.setCurGatheringName(autoName);
        dataCore.setCurGatherStartTime(curTime);
    }

    public String getTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        WataLog.d("now=" + now);
        SimpleDateFormat format = new SimpleDateFormat("yy.MM.dd_HHmm", Locale.KOREA);
        String regDate = format.format(date);
        WataLog.d("regDate=" + regDate);
        return regDate;
    }

    private void setRecordGathering() {
        wifiSet(false);
//        sensorSet(false);
        sensorSet(false);
        setCell(false);
        lineSelected = null;
        lineHeader = Double.NaN;

//        Date currentTime = Calendar.getInstance().getTime();
//        String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
//        StaticManager.setPathNum(getTime() + (mPointCount));

        DataCore.iGatherMode = DataCore.GATHER_MODE_NONE;
        _dataCore.insertGatheredData(_dataCore.getCurGatheringName(), _dataCore.getSCAN_RESULT_TOTAL());
        _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();
        WataLog.d("_dataCore.getSCAN_RESULT_THIS_EPOCH()=" + _dataCore.getSCAN_RESULT_THIS_EPOCH());

        // kcy1000 - 경로수집 저장
        File temp = new File(StaticManager.getResultPath());
        if (temp.exists()) {
            String[] tempArr = temp.list();
            for (int a = 0; a < tempArr.length; a++) {
                String tempName = tempArr[a];
                WataLog.d("tempName=" + tempName);

                if (!REVERSE_RECORDING) {
                    if (tempName.contains("_F")) {
                        File delFile = new File(StaticManager.getResultPath() + tempName);
                        delFile.delete();
                    }
                } else {
                    if (tempName.contains("_R")) {
                        File delFile = new File(StaticManager.getResultPath() + tempName);
                        delFile.delete();
                    }
                }
            }
        }

        WataLog.d("_dataCore.getCurGatheringName()=" + _dataCore.getCurGatheringName());

        WataLog.d("StaticManager.folderName=" + StaticManager.folderName);
        WataLog.d("StaticManager.floorName=" + StaticManager.floorName);
        WataLog.d("StaticManager.pathNum=" + StaticManager.pathNum);

        // kcy1000 - 파일작성시 디바이스 이름 및 각종 정보 입력함.
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH);
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH.H1_PSNID());
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH.B1_GID());
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH.B8_ColStartP_F());
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH.B11_ColDevModel());
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH.B12_ColOpt());
        _SCAN_RESULT_THIS_EPOCH.B9_ColStartP_CVADDR(StaticManager.address);
//            _SCAN_RESULT_THIS_EPOCH.B9_ColStartP_CVADDR("가나다라마바사");
        WataLog.d("REVERSE_RECORDING=" + REVERSE_RECORDING);
        //log 파일저장하기 - 파일전송
        if (!REVERSE_RECORDING) {
            WataLog.d("StaticManager.getResultVoucherPath()=" + StaticManager.getResultVoucherPath());
            WataLog.d("_dataCore.getCurGatheringName()=" + _dataCore.getCurGatheringName());
//
//            WataLog.d("B1_GID=" + _SCAN_RESULT_THIS_EPOCH.B1_GID);
//            WataLog.d("B2_GID_refP_Latitude=" + _SCAN_RESULT_THIS_EPOCH.B2_GID_refP_Latitude);
//            WataLog.d("B3_GID_refP_Longitude=" + _SCAN_RESULT_THIS_EPOCH.B3_GID_refP_Longitude);
//            WataLog.d("B4_GID_refP_Bearing=" + _SCAN_RESULT_THIS_EPOCH.B4_GID_refP_Bearing);
//            WataLog.d("B5_ColStartP_X=" + _SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X);
//            WataLog.d("B6_ColStartP_Y=" + _SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y);
//            WataLog.d("B12_ColEndP_X=" + _SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X);
//            WataLog.d("B13_ColEndP_Y=" + _SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y);
//            WataLog.d("B14_ColLinkID=" + _SCAN_RESULT_THIS_EPOCH.B14_ColLinkID);
//            WataLog.d("B15_ColLinkFlag=" + _SCAN_RESULT_THIS_EPOCH.B15_ColLinkFlag);
//            WataLog.d("B16_ColLinkHeading=" + _SCAN_RESULT_THIS_EPOCH.B16_ColLinkHeading);
//            WataLog.d("B17_ColOpt=" + _SCAN_RESULT_THIS_EPOCH.B17_ColOpt);
//            WataLog.d("B18_ColStartP_GRS_X=" + _SCAN_RESULT_THIS_EPOCH.B18_ColStartP_GRS_X);
//            WataLog.d("B19_ColStartP_GRS_Y=" + _SCAN_RESULT_THIS_EPOCH.B19_ColStartP_GRS_Y);
//            WataLog.d("B20_ColPoint_CNT=" + _SCAN_RESULT_THIS_EPOCH.B20_ColPoint_CNT);
//            WataLog.d("B21_Payload=" + _SCAN_RESULT_THIS_EPOCH.B21_Payload);
//            WataLog.d("B22_Payload=" + _SCAN_RESULT_THIS_EPOCH.B22_Payload);


            //            String path1 = getExternalFilesDir("TEST").getPath();
//            WataLog.d("path1=" + path1);

            SaveGatheredData.saveVoucherData(StaticManager.getResultVoucherPath(), _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
  //          SaveGatheredData.saveVoucherData(StaticManager.getResultVoucherPath(), _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
            SaveGatheredData.saveVoucherSendableData(StaticManager.getResultVoucherPath(), _dataCore.getCurGatheringName() + "_F.temp", _SCAN_RESULT_THIS_EPOCH);

//                setLineInfo(mRecordStartPointX, mRecordStartPointY, mLastPointX, mLastPointY);

//            WataLog.d("mapId=" + mapId);
//            if ("-1".equals(mapId)) {
//                setLineInfo(mLineStartPointX, mLineStartPointY, mStartEndPointX_S, mStartEndPointY_S, String.valueOf(mMyLatlng.latitude), String.valueOf(mMyLatlng.longitude));
//            } else {
//                setLineInfo(mLineStartPointX, mLineStartPointY, mStartEndPointX_S, mStartEndPointY_S, "", mapId);
//            }
            mSubwayInfo.get(NOW_POSITION).setCompletionF(getString(R.string.completion));
            mRecordAdapter.notifyDataSetChanged();

        } else {
            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH);
            SaveGatheredData.saveVoucherData(StaticManager.getResultVoucherPath(), _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
            SaveGatheredData.saveVoucherSendableData(StaticManager.getResultVoucherPath(), _dataCore.getCurGatheringName() + "_R.temp", _SCAN_RESULT_THIS_EPOCH);

            mSubwayInfo.get(NOW_POSITION).setCompletionR(getString(R.string.completion));
            mRecordAdapter.notifyDataSetChanged();
        }

        _SCAN_RESULT_THIS_EPOCH = new PDI_REPT_COL_DATA_VOUCHER(Build.SERIAL);
        WataLog.d("Build.SERIAL=" + Build.SERIAL);
        _SCAN_RESULT_TOTAL = new ArrayList<PDI_REPT_COL_DATA_VOUCHER>();

        //Tag : TOTAL 초기화
        _dataCore.setSCAN_RESULT_TOTAL(_SCAN_RESULT_TOTAL);
        _dataCore.setSCAN_RESULT_THIS_EPOCH(_SCAN_RESULT_THIS_EPOCH);

//        isInverse = false;
        DataCore.isOnGathering = false;

//        setLog("5G", m5GLogArry);
//        setLog("2.4G", m2GLogArry);
    }

    private VoucherSettingDialog mPsettingDialog;

    private void setSetting() {
        mPsettingDialog = new VoucherSettingDialog(VoucherActivity.this, new OnClickListener() {
            public void onClick(View v) {
                WataLog.i("check 0");
                SettingDialog sensitive = new SettingDialog();
                AlertDialog senseDialog = sensitive.makeSensitiveSettingDilog(VoucherActivity.this);
                senseDialog.show();
            }
        });

        mPsettingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                WataLog.i("onDismiss");

                SharedPreferences pref = getSharedPreferences("log_message", 0);
                setLOG = pref.getString("log_message", "false");
                WataLog.d("setLOG=" + setLOG);

                wifi_log_text.setText("");

//                pdrvariable.setStep_length(tempLen);
//                SharedPreferences pref = getSharedPreferences(Constance.KEY, 0);
//                isMapRatation = pref.getBoolean(Constance.SETTING_MAP_ROTATION, true);
//                isGyroSensor = pref.getBoolean(Constance.SETTING_G_SENSOR_ROTATION, true);

//                if (isMapRatation) {
//                    angle_layout.setVisibility(View.VISIBLE);
//                } else {
//                    angle_layout.setVisibility(View.GONE);
//                }
            }
        });
        mPsettingDialog.show();
    }


    private Animation startAnimation = null;

    public void startRedSignal(boolean switchGreen) {
        startAnimation = new AlphaAnimation(0.0f, 1.0f);
        startAnimation.setDuration(500);
        startAnimation.setRepeatCount(-1);
        startAnimation.setStartOffset(20);
        startAnimation.setRepeatMode(Animation.REVERSE);
        startAnimation.setRepeatCount(Animation.INFINITE);
        if (switchGreen) {
            greenSignal.setVisibility(View.VISIBLE);
            redSignal.setVisibility(View.GONE);
            greenSignalIcon.startAnimation(startAnimation);

        } else {
            greenSignal.setVisibility(View.GONE);
            redSignal.setVisibility(View.VISIBLE);
            redSignalIcon.startAnimation(startAnimation);
        }
    }


    private double getHeader(Vector from, Vector to) {
        Vector base = new Vector(0, 1, 0, 0);

        Vector dir = to.subtract3(from);
        dir = new Vector(dir.getX(), dir.getY(), 0, 0).normalize3();

        Vector cross = base.cross3(dir).normalize3();
        double d = dir.dot3(base);
        if (Double.isNaN(d))
            return 0;
        else {
            d = Math.acos(d);
            if (Double.isNaN(d))
                return 0;
            else {
                d = Angle.fromRadians(d).getDegrees();
                if (Double.isNaN(d))
                    return 0;
                else if (cross.getZ() < 0) {
                    d = 360 - d;
                }
            }
        }
        if (Double.isNaN(d))
            return 0;
        else
            return 360 - d;
    }

    boolean _isHandlerEnded = true;
    static public Handler _UIHandler = null;
    private VoucherWifiScanner _wifiScanner = null;
    private TBPWiFiScanner wifiScanner;
    private List<ScanResult> _list = null;
    private sensoract _senact = null;
    private Thread _sensorthread = null;

    private VoucherDataCore _dataCore = null; // 수집된내용이 저장될 위치
    private ArrayList<PDI_REPT_COL_DATA_VOUCHER> _SCAN_RESULT_TOTAL = null; // wifi 스캔카운터
    private PDI_REPT_COL_DATA_VOUCHER _SCAN_RESULT_THIS_EPOCH = null; // 수집된 모든내용이 저장된다.

    // BLE

    private BLEManager _bleManager = null;

    private void setDataCore() {

        if (_UIHandler == null)
            _UIHandler = new VoucherActivity.GetheringUIHandler();

        _dataCore = VoucherDataCore.getInstance();

        _SCAN_RESULT_THIS_EPOCH = new PDI_REPT_COL_DATA_VOUCHER(Build.SERIAL);
        WataLog.d("Build.SERIAL=" + Build.SERIAL);
        _SCAN_RESULT_TOTAL = new ArrayList<PDI_REPT_COL_DATA_VOUCHER>();

        //Tag : TOTAL 초기화
        _dataCore.setSCAN_RESULT_TOTAL(_SCAN_RESULT_TOTAL);
        _dataCore.setSCAN_RESULT_THIS_EPOCH(_SCAN_RESULT_THIS_EPOCH);
        DataCore.isOnGathering = false;

        WataLog.d("StaticManager.floorName=" + StaticManager.floorName);

        _dataCore.setFloorString(StaticManager.floorName);
        _dataCore.setBuildName(StaticManager.title);

//        StaticManager.setFolderName("Subway");
//        StaticManager.setFloorName("AB01"); // 층수정보 필요
//        _dataCore.setFloorString("subway");
//        _dataCore.setBuildName("강남역");

        if (_SCAN_RESULT_THIS_EPOCH == null) {
            _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();
            WataLog.d("_dataCore.getSCAN_RESULT_THIS_EPOCH()=" + _dataCore.getSCAN_RESULT_THIS_EPOCH());
        }

        if (_SCAN_RESULT_TOTAL == null)
            _SCAN_RESULT_TOTAL = _dataCore.getSCAN_RESULT_TOTAL();

    }

    private void wifiSet(boolean b) {
        WataLog.i("wifiSet = " + b);
        try {
            if (b) {
                if (_wifiScanner == null) {
                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    try {
                        int REQUEST_SCAN_ALWAYS_AVAILABLE = 100;
                        wifiManager.setWifiEnabled(false);
                        if (Build.VERSION.SDK_INT >= 18 && !wifiManager.isScanAlwaysAvailable()) {
                            ((Activity) VoucherActivity.this).startActivityForResult(
                                    new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), REQUEST_SCAN_ALWAYS_AVAILABLE);
                        }

                    } catch (Exception e) {
                        WataLog.e("exception=" + e.toString());
                    }

                    _list = new ArrayList<ScanResult>();
                    //				_wifiScanner = new WifiScanner(GatheringActivity.this, wifiManager, _UIHandler, _list);
                    _wifiScanner = new VoucherWifiScanner(this, wifiManager, _UIHandler, _list, _bleManager);
                    _wifiScanner.setBleIgnoreList(BleIgnoreSettingActivity.getBleIgnoreList(this));
                    _wifiScanner.setBleAddList(BleAddSettingActivity.getBleAddList(this));

                    _wifiScanner.setCellManager(cellManager);
                    _wifiScanner._wifiManager.startScan();        // Add by Ethan
                }

            } else {
                if (_wifiScanner != null) {
                    _wifiScanner.stopStatListener();
                    _wifiScanner.stopScan();
                    // wifiScanner.setWifiOff();
                    _wifiScanner = null;
                }
            }
        } catch (Exception e) {
            WataLog.e("exception=" + e.toString());
            onToast(Gravity.CENTER, "Wi-Fi Scanner가 정상 동작하지 않았습니다. 다시 실행해주세요. ");
            onRecordOff();
        }
    }

    // sensorthread start : by Ethan
    // sensor listeners size has exceeded the maximum limit 128 발생으로인해 onDestroy() 할때만 sensorSet(false) 실행하게 수정함
    private void sensorSet(boolean b) {
//        try {
        if (b) {
            if (_senact == null) {
                _senact = new sensoract(getApplication(), _UIHandler);
                if (_sensorthread == null) {
                    _sensorthread = new Thread(_senact);
                    _sensorthread.start();
                }
            }
        } else {
            if (_sensorthread != null) {
                try {
                    _senact.requestStop();
//                        _sensorthread.stop();
                    _sensorthread.interrupt();
                } catch (Exception e) {
                    WataLog.e("Exception=" + e.toString());
                    e.printStackTrace();
                }
                _sensorthread = null;
            }

            if (_senact != null) {
                _senact = null;
            }
        }
//        } catch (Exception e) {  // 예외처리 코드

//            WataLog.e("Exception=" + e.toString());
//            setProgress(false);
////            onRecordOff();
//
//            _senact = null;
//            _sensorthread = null;

//            startBtText.setText(getString(R.string.start));
//            IS_RECORDING = false;
//
//            if (!REVERSE_RECORDING) {
//                setWebView("endDrawPoint");
//            } else {
//                setWebView("endReverse");
//            }
//            OVER_LINE = false;
////            setRecordGathering();
//
//            IS_STEP_COUNT = 0;
//            SCAN_POINT_X = 0.0;
//            SCAN_POINT_Y = 0.0;
//
////                    mStepLength = 0;
//            recordStepCount.setText("0");
//            recordStepLength.setText("0 m");

//            onToast(Gravity.CENTER, "Wi-Fi Scanner가 정상 동작하지 않았습니다. 다시 실행해주세요. ");
//
//            sensorSet(true);
//        }

    }


    // Cell
    private void setCell(boolean b)
    {
        try {
            if (b) {
                if (cellManager == null) {
                    // Cell
                    Timber.plant(new Timber.DebugTree());
                    cellManager = new CellManager(this);

                    //cellManager.scanCellInfo();
                }

            } else {
                /*
                if (cellManager != null) {

                    try {
                        _senact.requestStop();
                        _sensorthread.stop();
                    } catch (Exception e) {
                        Timber.e("Exception=" + e.toString());
                        e.printStackTrace();
                    }
                    _sensorthread = null;
                }
                */

                if (cellManager != null) {
                    cellManager = null;
                }
            }
        } catch (Exception e) {
            Timber.e("Exception=" + e.toString());
            onToast(Gravity.CENTER, "cellManager가 정상 동작하지 않았습니다. 다시 실행해주세요. ");
            setProgress(false);
            onRecordOff();

        }

    }


    private void gatheringRelease() {
        if (_sensorthread != null) {
            _sensorthread.interrupt();
            _sensorthread = null;
        }
        if (_senact != null) {
            _senact = null;
        }
        if (_UIHandler != null) {
            _UIHandler.sendEmptyMessage(DataCore.HANDLER_ENDED);
            // UIHandler.removeMessages(DataCore.PDR_STAT_CHANGED);
            _UIHandler = null;
        }
        if (_wifiScanner != null) {
            _wifiScanner.stopStatListener();
            // wifiScanner.setWifiOff();
            _wifiScanner = null;
        }
//        isInverse = false;
//        lineSelected = null;
//        lineHeader = Double.NaN;
    }


    public class GetheringUIHandler extends Handler {
        SandboxModel LayerForApp = null;

        private int viaPointIdx = 0;

        public void handleMessage(Message msg) {
            if (_isHandlerEnded == true) {
                _isHandlerEnded = false;
                try {
//                    WataLog.d("msg.what=" + msg.what);
                    switch (msg.what) {
                        case DataCore.AT_START_NEW_RECORD:
                            WataLog.i("AT_START_NEW_RECORD");
                            break;

                        case DataCore.AT_RECORD_SENSING:
                            WataLog.i("AT_RECORD_SENSING");

//                            String temp[] = (String[]) msg.obj;
                            double temp[] = (double[]) msg.obj;
                            WataLog.d("(float) temp[0]=" + temp[0]);
                            WataLog.d("(float) temp[1]=" + temp[1]);

//                            String stepLength = String.valueOf(recordStepLength.getText());

                            double travelRange = IS_STEP_COUNT * mStepLength;
                            WataLog.d("travelRange=" + travelRange);
                            setWebView("drawPoint", "signal|" + travelRange);

                            break;

                        case DataCore.PDR_STAT_CHANGED: // from sensor
                            // 경로기록

                            break;

                        case DataCore.HANDLER_ENDED:
                            break;

                        case DataCore.REFREASH_WIFI_LIST: //wifi

                            _SCAN_RESULT_TOTAL = _dataCore.getSCAN_RESULT_TOTAL();
                            _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();

                            WataLog.d("_SCAN_RESULT_THIS_EPOCH.B21_Payload.size()=" + _SCAN_RESULT_THIS_EPOCH.B21_Payload.size());
                            if (_SCAN_RESULT_THIS_EPOCH.B21_Payload.size() == 2) {
                                //최소 2개이상일때 동작
                                setProgress(false);
                                onToast(Gravity.CENTER, getString(R.string.start_recording));
                            }
//                            if (_SCAN_RESULT_THIS_EPOCH.B21_Payload.size() > 2) {
//
//                            }

                            break;

                        case DataCore.DATA_SEND_ENDED:
                            WataLog.i("DATA_SEND_ENDED");
                            break;

                        case DataCore.ON_CLICK_OK_ON_START:
                            WataLog.i("ON_CLICK_OK_ON_START");
                            break;

                        case DataCore.ON_CLICK_CANCLE_ON_START:
                            WataLog.i("ON_CLICK_CANCLE_ON_START");
                            break;

                        case DataCore.DATA_SEND_READY:
                            WataLog.i("DATA_SEND_READY");
                            break;

                        case DataCore.NETWORK_READY:
                            WataLog.i("NETWORK_READY");
                            break;

                        case DataCore.ON_TOUCH_MOVE_MAP:  //이동할 방향 설정 - kcy1000 - 2
                            WataLog.i("ON_TOUCH_MOVE_MAP");

                            break;

                        case DataCore.ON_TOUCH_MAP: // gathering listener
                            WataLog.i("ON_TOUCH_MAP");
                            break;

                        case DataCore.GATHER_MODE_CHANGED:
                            WataLog.i("GATHER_MODE_CHANGED");
                            break;

                        case 9999: //도착지점 선택시
                            WataLog.i("!!! 9999 !!!");
//                            map_inner_frm_linear1.setVisibility(View.INVISIBLE);
//                            map_inner_frm_linear2.setVisibility(View.VISIBLE);
                            break;

                        case 2919:
                            WataLog.i("!!! 2919 !!!");
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    _isHandlerEnded = true;
                }
            }
        }
    }

    // ========================================

    // 시작점 끝점값 Webview에서 받아옴
    private void viewEachPath() {
        WataLog.i("viewEachPath");

        _UIHandler.sendEmptyMessage(DataCore.AT_START_NEW_RECORD);

        boolean bAdd = false;

        // 시작점 끝점  설정하기.
        lineSelected = (Polyline) getLine();

        if (lineSelected != null) {

            Points pts = lineSelected.getPart(0);

            Point start = pts.data[0];
            Point end = pts.data[pts.getNumPoints() - 1];

            START_POINT_VECTOR = new Vector(start.x, start.y, 0);
            WataLog.d("START_POINT_VECTOR =" + START_POINT_VECTOR);

            END_POINT_VECTOR = new Vector(end.x, end.y, 0);
            WataLog.d("END_POINT_VECTOR= " + END_POINT_VECTOR);
        }
    }

    public Geometry getLine() {
        Polyline line = new Polyline();
        line.makeParts(1);
        Points pts = new Points();
        pts.makePoints(2);

        double sPointX = START_POINT_X;
        double sPointY = START_POINT_Y;

        double ePointX = END_POINT_X;
        double ePointY = END_POINT_Y;

        WataLog.d("sPointX=" + sPointX);
        WataLog.d("sPointY=" + sPointY);

        WataLog.d("ePointX=" + ePointX);
        WataLog.d("ePointY=" + ePointY);

        pts.setPoint(0, sPointX, sPointY, 0.0f);
        pts.setPoint(1, END_POINT_X, END_POINT_Y, 0.0f);

        line.setParts(0, pts);
        return line;
    }

    private ArrayList<SubwayLineInfo> mSubwayInfo = new ArrayList<SubwayLineInfo>();
    private RecordAdapter mRecordAdapter;

//    http://13.209.43.191:3000/subwayGuideLines2

    private void getSubwayInfo(String areano, String lineno, String stationnm, String floor) {
        WataLog.i("getSubwayInfo");
//                    http://13.209.43.191:3000/subwayGuideLines?&areaNo=1&lineNo=0&stationnm=BCC&floor=P
//        areano=1  lineno=0  stationnm=BCC  floor=P
        WataLog.d("areano=" + areano + "  lineno=" + lineno + "  stationnm=" + stationnm + "  floor=" + floor);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);

        retrofitExService.subwayGuideLines(areano, lineno, stationnm, floor).enqueue(new Callback<ResponseBody>() {
            // test 4층
//        retrofitExService.testGuideLine().enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터

                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String cid = jsonObject.getString("cid");
                        String area_no = jsonObject.getString("area_no");
                        String lineno = jsonObject.getString("lineno");
                        String station_nm = jsonObject.getString("station_nm");
                        String floorinfo = jsonObject.getString("floorinfo");

                        String st_x = jsonObject.getString("st_x");
                        String st_y = jsonObject.getString("st_y");
                        String ed_x = jsonObject.getString("ed_x");
                        String ed_y = jsonObject.getString("ed_y");

                        int line_serno = jsonObject.getInt("line_serno");
                        String ins_date = jsonObject.getString("ins_date");

                        mSubwayInfo.add(new SubwayLineInfo(cid, area_no, lineno, station_nm, floorinfo, st_x, st_y, ed_x, ed_y, line_serno, ins_date, "미완료", "미완료"));
                    }

                    mRecordAdapter.setItems(mSubwayInfo);
                    mRecordListview.setAdapter(mRecordAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("onFailure");
            }
        });
    }


    private JSONObject mPoiListObject = new JSONObject();
    private JSONArray mPoiArray = new JSONArray();
    private String mPoiFileName;

    private void setPOILog(String x, String y, String poiName) {
        String poiJson = "";
        String poiJsonRecord = "";
        try {
            final JSONObject poiObject = new JSONObject();
            poiObject.put("pointx", x);
            poiObject.put("pointy", y);
            poiObject.put("poi_name", poiName);

            poiJson = poiObject.toString();

            mPoiArray.put(poiObject);

            try {
                mPoiListObject.put("POI_LIST", mPoiArray);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            poiJson = mPoiListObject.toString();
            WataLog.d("poiJson=" + poiJson);

            for (int i = 0; i < mPoiArray.length(); i++) {
                if (i > 0) {
                    poiJsonRecord += ",";
                }
                poiJsonRecord += mPoiArray.get(i);
                WataLog.d("poiJsonRecord=" + poiJsonRecord);
            }

            poiJsonRecord = "{\"POI_LIST\": [" + poiJsonRecord + "]}";
            WataLog.d("lineJsonRecord=" + poiJsonRecord);

            mPoiFileName = "POI_" + POI_FILE + ".json";
            WataLog.d("mPoiFileName=" + mPoiFileName);
            SaveGatheredData.saveData(StaticManager.getResultVoucherPOIPath(), mPoiFileName, poiJsonRecord);

        } catch (JSONException e) {
            WataLog.d("Failed to create JSONObject =" + e);
        }
    }

    private String getAreaName(String num) {
        switch (num) {
            case "0":
                return "수도권";
            case "1":
                return "부산";
            case "2":
                return "대구";
            case "3":
                return "광주";
            case "4":
                return "대전";
            case "5":
                return "일본";
        }
        return "";
    }


    private void onKeyboardShow() {
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void onKeyboardHide(EditText edit) {
        WataLog.i("onKeyboardHide");
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(edit.getWindowToken(), 0);

    }

    private int mUploadCount = 0;

    // 파일전송
    private void HttpMultiPart(String fileName) {
        String path = StaticManager.getResultVoucherPath() + fileName;
        WataLog.d("path=" + path);

        final File fileData = new File(path);
        final PDI_REPT_COL_DATA_VOUCHER selectedData = LoadGatheredData.decodeSavedSendableVoucherData(fileData);

        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                String boundary = "^-----^";
                String LINE_FEED = "\r\n";
                String charset = "UTF-8";
                OutputStream outputStream;
                PrintWriter writer;

                JSONObject result = null;
                try {

                    URL url = new URL(Constance.DEVE_JUDI_SERVER + "/api/collect/upload/" + UUID);
                    WataLog.d("url=" + url);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestProperty("Content-Type", "multipart/form-data;charset=utf-8;boundary=" + boundary);
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setConnectTimeout(15000);

                    outputStream = connection.getOutputStream();
                    writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);

                    /** Body에 데이터를 넣어줘야 할경우 없으면 Pass **/
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"gid\"").append(LINE_FEED);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.append(StaticManager.gid).append(LINE_FEED);
                    writer.flush();

                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"name\"").append(LINE_FEED);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.append(StaticManager.title).append(LINE_FEED);
                    writer.flush();

                    /** 파일 데이터를 넣는 부분**/
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileData.getName() + "\"").append(LINE_FEED);
                    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileData.getName())).append(LINE_FEED);
                    writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.flush();

                    FileInputStream inputStream = new FileInputStream(fileData);
//                    byte[] buffer = new byte[(int) file.length()];
                    byte[] buffer = selectedData.packet();

                    int bytesRead = -1;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.flush();
                    inputStream.close();

                    WataLog.d("inputStream=" + inputStream);
                    WataLog.d("outputStream=" + outputStream);

                    writer.append(LINE_FEED);
                    writer.flush();

                    writer.append("--" + boundary + "--").append(LINE_FEED);
                    writer.close();

                    int responseCode = connection.getResponseCode();
                    WataLog.d("responseCode=" + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {

                        mUploadCount++;
                        WataLog.d("mUploadCount=" + mUploadCount);
                        if (mUploadCount == mFileList.size()) {
                            WataLog.d("mFileList.size()=" + mFileList.size());
                            endUpload();
                        }

//                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                        String inputLine;
//                        StringBuffer response = new StringBuffer();
//                        while ((inputLine = in.readLine()) != null) {
//                            response.append(inputLine);
//                        }
//                        in.close();
//                        WataLog.d("response.toString()=" + response.toString());

//                        try {
//                            result = new JSONObject(response.toString());
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                } catch (ConnectException e) {
                    WataLog.e("ConnectException=" + e.toString());
                    e.printStackTrace();
                } catch (Exception e) {
                    WataLog.e("ConnectException=" + e.toString());
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
            }

        }.execute();
    }

    private ArrayList<String> mFileList = new ArrayList<String>();

    private void getGatheringPathData() {
        mUploadCount = 0;
        String dataPath = StaticManager.getResultVoucherPath();
        File pathFolder = new File(dataPath);
        WataLog.d("dataPath = " + dataPath);
        if (pathFolder.exists()) {
            String[] list = pathFolder.list();
            if (list != null) {
                WataLog.d("list = " + list.length);

                for (int a = list.length - 1; a >= 0; a--) {
                    File item = new File(dataPath + "/" + list[a]);
                    WataLog.d("list[" + a + "]=" + list[a]);
                    String name = list[a].toString();
                    String[] fileName = name.split("\\.");
                    WataLog.d("fileName=" + fileName);
                    if (fileName.length > 1) {
                        if (!fileName[1].equals("log")) {
                            WataLog.d("list[a]=" + list[a]);
                            mFileList.add(list[a]);
                        }
                    }
                }

                for (int i = 0; i < mFileList.size(); i++) {
                    HttpMultiPart(mFileList.get(i));
                }
            }
        }
    }

//    public static void fileUpload() {
//        String path = StaticManager.getResultVoucherPath() + "_F.temp";
//        String path2 = StaticManager.getResultVoucherPath() + "_R.temp";
//        String path3 = StaticManager.getResultVoucherPath() + "강남역-2호선-0924-10:09_R.temp";
//        WataLog.d("path=" + path);
//        WataLog.d("path2=" + path2);
//        WataLog.d("path3=" + path3);
//
//        final File file1 = new File(path);
//        final File file2 = new File(path2);
//        final File file3 = new File(path3);
//
//        RequestBody requestBody;
//        MultipartBody.Part body;
//        LinkedHashMap<String, RequestBody> mapRequestBody = new LinkedHashMap<String, RequestBody>();
//        List<MultipartBody.Part> arrBody = new ArrayList<>();
//
//        mapRequestBody.put("gid", RequestBody.create(MediaType.parse("text/plain"), StaticManager.gid));
//        mapRequestBody.put("name", RequestBody.create(MediaType.parse("text/plain"), StaticManager.title));
////
////        requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
////        mapRequestBody.put("file\"; filename=\"" + file2.getName(), requestBody);
////        body = MultipartBody.Part.createFormData("fileName", file1.getName(), requestBody);
////        arrBody.add(body);
//
//
//        ArrayList<MultipartBody.Part> fileArrList = new ArrayList<>();
////        for (Uri uri : postUriList) {
////            String path = FileUtil.getPath(uri, this);
////            File file = new File(path);
//        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file1);
//        MultipartBody.Part uploadFile = MultipartBody.Part.createFormData("fileName", file1.getName(), requestFile);
//        fileArrList.add(uploadFile);
////        }
//
//        requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file2);
//        uploadFile = MultipartBody.Part.createFormData("fileName", file2.getName(), requestFile);
//        fileArrList.add(uploadFile);
//
//        Call<ResponseBody> call = RetrofitImg.getInstance().getService().uploadFile(mapRequestBody, fileArrList);
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                WataLog.d("response=" + response);
//                WataLog.d("response.code" + response.code());
//                if (response.body() != null) {
//
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                WataLog.d("Err", t.getMessage());
//            }
//        });
//    }

    private String UUID = "";

    private void setUpload() {
        WataLog.d("setUpload");
        progressText.setText("파일 업로드 중입니다.");
        setProgress(true);
        WataLog.d("StaticManager.title=" + StaticManager.title);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_JUDI_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        retrofitExService.pre_upload(StaticManager.gid, StaticManager.title).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    WataLog.d(response.message()); //받아온 데이터
                    if (response != null) {
                        WataLog.d("response.code()=" + response.code());
                        if (response.code() == Constance.RESPONSE_SUCCESS) {
                            UUID = response.body().string();
                            WataLog.d("UUID=" + UUID);
                            getGatheringPathData();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("onFailure=" + call.request().toString());
                WataLog.d("onFailure=" + call.request().body());
            }
        });
    }

    private void endUpload() {
        WataLog.d("endUpload");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_JUDI_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        retrofitExService.post_upload(UUID).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    WataLog.d(response.message()); //받아온 데이터
                    if (response != null) {
                        WataLog.d("response.code()=" + response.code());
                        if (response.code() == HttpURLConnection.HTTP_OK || response.code() == HttpURLConnection.HTTP_CREATED) {
                            String result = response.body().string();
                            WataLog.d("result=" + result);

                            setProgress(false);

                            new AlertDialog.Builder(VoucherActivity.this)
                                    .setTitle(getString(R.string.send_file))
                                    .setMessage(getString(R.string.finish_upload))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    }).show();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("test", "onFailure");
            }
        });
    }

    // POI 파일전송
    private void HttpMultiPartPoiUpload(String fileName) {
        String path = StaticManager.getResultVoucherPOIPath() + fileName;
        WataLog.d("path=" + path);
        final File fileData = new File(path);

        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                String boundary = "^-----^";
                String LINE_FEED = "\r\n";
                String charset = "UTF-8";
                OutputStream outputStream;
                PrintWriter writer;

                JSONObject result = null;
                try {

                    URL url = new URL(Constance.DEVE_JUDI_SERVER + "/api/collect/poi/" + StaticManager.gid);
                    WataLog.d("url=" + url);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestProperty("Content-Type", "multipart/form-data;charset=utf-8;boundary=" + boundary);
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.setConnectTimeout(15000);

                    outputStream = connection.getOutputStream();
                    writer = new PrintWriter(new OutputStreamWriter(outputStream, charset), true);

                    /** Body에 데이터를 넣어줘야 할경우 없으면 Pass **/
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"gid\"").append(LINE_FEED);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.append(StaticManager.gid).append(LINE_FEED);
                    writer.flush();

                    /** 파일 데이터를 넣는 부분**/
                    writer.append("--" + boundary).append(LINE_FEED);
                    writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileData.getName() + "\"").append(LINE_FEED);
                    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileData.getName())).append(LINE_FEED);
                    writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
                    writer.append(LINE_FEED);
                    writer.flush();

                    FileInputStream inputStream = new FileInputStream(fileData);
                    byte[] buffer = new byte[(int) fileData.length()];

                    int bytesRead = -1;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.flush();
                    inputStream.close();

                    WataLog.d("inputStream=" + inputStream);
                    WataLog.d("outputStream=" + outputStream);

                    writer.append(LINE_FEED);
                    writer.flush();

                    writer.append("--" + boundary + "--").append(LINE_FEED);
                    writer.close();

                    int responseCode = connection.getResponseCode();
                    WataLog.d("responseCode=" + responseCode);
                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setProgress(false);

                            }
                        });
                    }
                } catch (ConnectException e) {
                    WataLog.e("ConnectException=" + e.toString());
                    e.printStackTrace();
                } catch (Exception e) {
                    WataLog.e("ConnectException=" + e.toString());
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
            }

        }.execute();
    }

    //    13.209.43.191:3000/serverPoiInfo?areano=0&lineno=1&stationno=2&floor=B2&userid=admin&poi_content=test poi&xpos=123123123&ypos=456456
    // POI 내역 찰리님에게 전송
    private void getSubwayLine(String poiX, String poiY, String areaId, String categoryCode, String potoName) {

        WataLog.i("getSubwayLine");
        String imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        WataLog.d("retrofitExService=" + retrofitExService);
//        13.209.43.191:3000/serverPoiInfo?areano=0&lineno=1&stationno=2&floor=B2&userid=admin&poi_content=test poi&xpos=123123123&ypos=456456&poi_category=패션(의류)&poi_filenm=test.png
        retrofitExService.serverPoiInfo(Sid, mNumLine, mStationNo, mFloorsLever, imei, areaId, poiX, poiY, categoryCode, potoName).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("onFailure");
            }
        });
    }


    // =============================================POI 불려오기======================================

    private ArrayList<String> mPoiList = new ArrayList<String>();

    private ArrayList<File> getPoiPathData() {
        ArrayList<File> items = new ArrayList<File>();
        String dataPath = StaticManager.getResultVoucherPOIPath(); // 파일저장된 경로설정
        File pathFolder = new File(dataPath);

        if (pathFolder.exists()) {
            String[] list = pathFolder.list();
            if (list != null) {
                for (int a = 0; a <= list.length - 1; a++) {
                    String name = list[a].toString();
                    WataLog.d("name=" + name);
                    String[] fileName = name.split("\\.");
                    WataLog.d("fileName[0]=" + fileName[0]);
                    // 저장된 경로에서 확장자 json 파일만 가져옴
                    WataLog.d("fileName.length=" + fileName.length);
                    if (fileName.length > 1) {
                        WataLog.d("fileName[1]=" + fileName[1]);

                        if (fileName[1].equals("json")) {
                            mPoiList.add(list[a]);
                        }
                    }
                }
                setPoiReader();
            }
        }
        return items;
    }

    private void setPoiReader() {
        WataLog.i("setPoiReader");
        for (int i = 0; i < mPoiList.size(); i++) {
            String jsonData = JsonUtils.INSTANCE.getData(StaticManager.getResultVoucherPOIPath() + mPoiList.get(i));
            if (jsonData != null) {
                try {
                    JSONObject jObject = new JSONObject(jsonData);
                    JSONArray arrayPaths = jObject.getJSONArray("POI_LIST");
                    for (int p = 0; p < arrayPaths.length(); p++) {
                        JSONObject jsonPath = arrayPaths.getJSONObject(p);

                        String pointx = jsonPath.getString("pointx");
                        String pointy = jsonPath.getString("pointy");
                        String poiName = jsonPath.getString("poi_name");

                        WataLog.d("poiName=" + poiName);

                        // json 파일 파싱 후 Webview에 뿌림
//                        mWebView.loadUrl("javascript:" + "drawPOIWithInfo" + "('" + pointx + "|" + pointy + "|" + poiName + "')");

                        setWebView("drawPOIWithInfo", pointx + "|" + pointy + "|" + poiName);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private void setPOITestLog(String x, String y, String poiName) {
        String poiJson = "";
        String poiJsonRecord = "";
        try {
            final JSONObject poiObject = new JSONObject();
            poiObject.put("pointx", x);
            poiObject.put("pointy", y);
            poiObject.put("poi_name", poiName);

            poiJson = poiObject.toString();

            mPoiArray.put(poiObject);

            try {
                mPoiListObject.put("POI_LIST", mPoiArray);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            poiJson = mPoiListObject.toString();
            WataLog.d("poiJson=" + poiJson);

            for (int i = 0; i < mPoiArray.length(); i++) {
                if (i > 0) {
                    poiJsonRecord += ",";
                }
                poiJsonRecord += mPoiArray.get(i);
                WataLog.d("poiJsonRecord=" + poiJsonRecord);
            }

            Date currentTime = Calendar.getInstance().getTime();

            poiJsonRecord = "{\"POI_LIST\": [" + poiJsonRecord + "]}";
            WataLog.d("lineJsonRecord=" + poiJsonRecord);

            mPoiFileName = "POI_" + POI_FILE + "_" + currentTime.toString() + ".text";
            WataLog.d("mPoiFileName=" + mPoiFileName);

            SaveGatheredData.saveData(StaticManager.getResultVoucherPath(), mPoiFileName, poiJsonRecord);

        } catch (JSONException e) {
            WataLog.d("Failed to create JSONObject =" + e);
        }
    }

    private static String setLOG = "false";
    private static ArrayList<String> m5GLogArry = new ArrayList();
    private static ArrayList<String> m2GLogArry = new ArrayList();

    public static void setWifi5G(ArrayList SSID, ArrayList Freq) {
        int listSize = SSID.size();
        String S_5G = "";
        String S_2G = "";

        int I_5G_count = 0;
        int I_2G_count = 0;
        for (int i = 0; i < listSize; i++) {
//            WataLog.d("SSID=" + SSID);
            short freq = (short) Freq.get(i);
//            WataLog.d("freq=" + freq);
            if (freq > 5000) {
                S_5G += SSID.get(i).toString() + " | ";
                I_5G_count++;
            } else {
                S_2G += SSID.get(i).toString() + " | ";
                I_2G_count++;
            }
        }
        if ("true".equals(setLOG)) {
            wifi_log_text.setText("5G개수 : " + I_5G_count + "\n\n" + "5G -> " + S_5G + "\n\n"
                    + "2.4G개수 : " + I_2G_count + "\n\n" + "2G -> " + S_2G);

        }
        m5GLogArry.add(S_5G);
        m2GLogArry.add(S_2G);
    }

    private static JSONObject mLogListObject = new JSONObject();
    private static JSONArray mLogArray = new JSONArray();
    private static String mLogFileName;

    private static void setLog(String logType, ArrayList<String> data) {
        WataLog.i("setLog");
        String logJson = "";
        String logJsonRecord = "";
        try {
            final JSONObject logObject = new JSONObject();
            for (int d = 0; d < data.size(); d++) {
                logObject.put(String.valueOf(d), data.get(d));
            }

            logJson = logObject.toString();

            mLogArray.put(logObject);

            try {
                mLogListObject.put("LOG_LIST", mLogArray);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            logJson = mLogListObject.toString();
//            WataLog.d("logJson=" + logJson);

            for (int i = 0; i < mLogArray.length(); i++) {
                if (i > 0) {
                    logJsonRecord += ",";
                }
                logJsonRecord += mLogArray.get(i);
//                WataLog.d("logJsonRecord=" + logJsonRecord);
            }

            Date currentTime = Calendar.getInstance().getTime();

            logJsonRecord = "{\"LOG_LIST\": [" + logJsonRecord + "]}";
//            WataLog.d("logJsonRecord=" + logJsonRecord);

            mLogFileName = logType + "_LOG_" + ".text";
//            WataLog.d("mLogFileName=" + mLogFileName);

            SaveGatheredData.saveData(StaticManager.getResultVoucherPath(), mLogFileName, logJsonRecord);

        } catch (JSONException e) {
            WataLog.d("Failed to create JSONObject =" + e);
        }
    }


}

// 유심 수량
// 바우처 7팀 * 2명 * 4개(40일) = 56
// 라이다 2팀 * 4개(40일) = 8개
// 개발팀 부산 AR 휴대폰 3대 * 4개(40일) = 12개
// 개발팀 임시사용 5개 = 5개

//moveMapline
//moveMapReverse
//deletePOI
//drawLineCoords
//drawEndLine
//drawHalfEndLine
//togglePoiMode
//setPOIContent
//deletePOI
//updatePOIContent
//drawPoint
//endDrawPoint
//endReverse
//initStartPt
//drawPOIWithInfo
