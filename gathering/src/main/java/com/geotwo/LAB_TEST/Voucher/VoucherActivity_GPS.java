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
import android.location.Location;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
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

import com.geotwo.LAB_TEST.Gathering.BleAddSettingActivity;
import com.geotwo.LAB_TEST.Gathering.BleIgnoreSettingActivity;
import com.geotwo.LAB_TEST.Gathering.DataCore;
import com.geotwo.LAB_TEST.Gathering.Retrofit.RetrofitExService;
import com.geotwo.LAB_TEST.Gathering.dialog.SettingDialog;
import com.geotwo.LAB_TEST.Gathering.dto.PoiInfo;
import com.geotwo.LAB_TEST.Gathering.savedata.LoadGatheredData;
import com.geotwo.LAB_TEST.Gathering.savedata.SaveGatheredData;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.Voucher.Retrofit.CategoryInfo;
import com.geotwo.LAB_TEST.data.StaticManager;
import com.geotwo.TBP.TBPWiFiScanner;
import com.geotwo.common.JsonUtils;
import com.geotwo.o2mapmobile.geometry.Angle;
import com.geotwo.o2mapmobile.geometry.Vector;
import com.geotwo.o2mapmobile.model.SandboxModel;
import com.geotwo.o2mapmobile.shape.Geometry;
import com.geotwo.o2mapmobile.shape.Point;
import com.geotwo.o2mapmobile.shape.Points;
import com.geotwo.o2mapmobile.shape.Polyline;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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

//import com.geotwo.o2mapmobile.shape.CSFReader;

//import geo2.lbsp.ble.BLEManager;

//import geo2.lbsp.ble.BLEManager;

public class VoucherActivity_GPS extends AppCompatActivity implements OnClickListener {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WataLog.i("onCreate");
        setContentView(R.layout.gps_layout);

        StaticManager.setGid("TEST000000000000000000000");
//            StaticManager.setGid("1168010100108210001S01300");
        StaticManager.setTitle("수도권" + "_" + "GPS_TEST");

        // 경로설정
        StaticManager.setFolderName("TEST");
        StaticManager.setFloorName("field");
        StaticManager.setSubwayName("G");
        StaticManager.setAddress("G");
        StaticManager.setFloorLevel("1");

        mRelativeX = Long.valueOf("0");
        mRelativeY = Long.valueOf("0");

        WataLog.d("mRelativeX=" + mRelativeX);
        WataLog.d("mRelativeY=" + mRelativeY);

        StaticManager.setBasePointX("0");
        StaticManager.setBasePointX("0");

        StaticManager.setPosition(0);

        myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/gps.htm";

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setInitLayout();
        setDataCore();
    }

    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    private LatLng currentPosition;
    private Location location;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
//                WataLog.d("currentPosition=" + currentPosition);

                String lat = String.valueOf(location.getLatitude());
                String lon = String.valueOf(location.getLongitude());

                setWebView("setGPSPosition", lon + "|" + lat);

                SCAN_POINT_X = location.getLatitude();
                SCAN_POINT_Y = location.getLongitude();

            }
        }
    };

    private void startLocationUpdates() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            WataLog.d("startLocationUpdates : 퍼미션 안가지고 있음");
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private LatLng mMyLatlng;

    @Override
    protected void onResume() {
        super.onResume();
        mStepLength = pdrvariable.getStep_length();
        WataLog.d("mStepLength=" + mStepLength);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        startLocationUpdates();

        SharedPreferences sf = getSharedPreferences("GPS", MODE_PRIVATE);
        String linkId = sf.getString("link_id", "0");
        NOW_POSITION = Integer.valueOf(linkId);
        WataLog.d("NOW_POSITION=" + NOW_POSITION);
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

        StaticManager.setEmptyAll();

        gatheringRelease();
    }

    private TextView recordLineNumText, recordStepCount, recordStepLength, directionText, startBtText, progressText, poiNum, logView;
    private RelativeLayout optionBtn, directionTextLayout, start_btn, step_btn, line_list_layout, undoBtn, poiModeImgBtn, poi_point_layout,
            poi_edit_box_layout, poiFileUploadBtn, fileUploadBtn;
    private View greenSignalIcon, redSignalIcon, voucherRecoding, recordLine, poiLine;
    private LinearLayout greenSignal, redSignal;
    private ImageView poiModeImg;
    private EditText poi_edit, poi_edit_text;

    private ImageView mPotoImageView1, mPotoImageView2;
    private Button cameraBtn1, cameraBtn2;

    private void setInitLayout() {
        webViewSetting();

        // log Text
        logView = (TextView) findViewById(R.id.logView);

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

//        mRecordAdapter = new RecordAdapter(this);
//        mRecordAdapter.setOnItemClickListener(new RecordAdapter.OnItemClickListner() {
//            @Override
//            public void onRecordStartPoint(SubwayLineInfo items, int position) {
//                // 해당 지점으로 이동
//                voucherRecoding.setVisibility(View.GONE);
//                REVERSE_RECORDING = false;
//
//                WataLog.d("mSubwayInfo.get(position).getStX()=" + mSubwayInfo.get(position).getStX());
//                WataLog.d("mSubwayInfo.get(position).getStY()=" + mSubwayInfo.get(position).getStY());
//                WataLog.d("mSubwayInfo.get(position).getEdX()=" + mSubwayInfo.get(position).getEdX());
//                WataLog.d("mSubwayInfo.get(position).getEdY()=" + mSubwayInfo.get(position).getEdY());
//
////                setWebView("drawPosition", mSubwayInfo.get(position).getStX() + "|" + mSubwayInfo.get(position).getStY());
////                setWebView("drawPosition",  mSubwayInfo.get(position).getEdX() + "|" + mSubwayInfo.get(position).getEdY());
//
//                START_POINT_X = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getStX()) - mRelativeX) * 10);
//                START_POINT_Y = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getStY()) - mRelativeY) * 10);
//                END_POINT_X = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getEdX()) - mRelativeX) * 10);
//                END_POINT_Y = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getEdY()) - mRelativeY) * 10);
//
//                WataLog.d("START_POINT_X=" + new DecimalFormat("#########.#####").format(START_POINT_X));
//                WataLog.d("START_POINT_Y=" + new DecimalFormat("#########.#####").format(START_POINT_Y));
//                WataLog.d("START_POINT_X=" + new DecimalFormat("#########.#####").format(END_POINT_X));
//                WataLog.d("START_POINT_Y=" + new DecimalFormat("#########.#####").format(END_POINT_Y));
//
//                NOW_POSITION = position;
//                viewEachPath();
//                // 기록할 라인번호 설정
//                StaticManager.setPathNum("" + (position + 1));
//
//                WataLog.d("StaticManager.getPathNum=" + StaticManager.pathNum);
//                //webview 호출
//                setWebView("moveMapline", mSubwayInfo.get(position).getStX() + "|" + mSubwayInfo.get(position).getStY()
//                        + "|" + mSubwayInfo.get(position).getEdX() + "|" + mSubwayInfo.get(position).getEdY() + "|" + mSubwayInfo.get(position).getLineSerno());
//
//                directionText.setText("정방향");
//            }
//
//            @Override
//            public void onRecordEndPoint(SubwayLineInfo items, int position) {
//                // 해당 지점으로 이동
//                voucherRecoding.setVisibility(View.GONE);
//                REVERSE_RECORDING = true;
//
//                START_POINT_X = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getStX()) - mRelativeX) * 10);
//                START_POINT_Y = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getStY()) - mRelativeY) * 10);
//                END_POINT_X = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getEdX()) - mRelativeX) * 10);
//                END_POINT_Y = (Math.floor(Double.valueOf(mSubwayInfo.get(position).getEdY()) - mRelativeY) * 10);
//
//                WataLog.d("START_POINT_X=" + new DecimalFormat("#########.#####").format(START_POINT_X));
//                WataLog.d("START_POINT_Y=" + new DecimalFormat("#########.#####").format(START_POINT_Y));
//                WataLog.d("START_POINT_X=" + new DecimalFormat("#########.#####").format(END_POINT_X));
//                WataLog.d("START_POINT_Y=" + new DecimalFormat("#########.#####").format(END_POINT_Y));
//
////                setWebView("drawPosition", String.valueOf(START_POINT_X) + "|" + String.valueOf(START_POINT_Y));
////                setWebView("drawPosition", String.valueOf(END_POINT_X) + "|" + String.valueOf(END_POINT_Y));
//
//                NOW_POSITION = position;
//                viewEachPath();
//                // 기록할 라인번호 설정
//                StaticManager.setPathNum("" + (position + 1));
//                //webview 호출
//                setWebView("moveMapReverse", mSubwayInfo.get(position).getEdX() + "|" + mSubwayInfo.get(position).getEdY()
//                        + "|" + mSubwayInfo.get(position).getStX() + "|" + mSubwayInfo.get(position).getStY()
//                        + "|" + mSubwayInfo.get(position).getLineSerno());
//
//                directionText.setText("역방향");
//            }
//        });

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
                    Toast.makeText(VoucherActivity_GPS.this, "'Bluetooth가 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
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

                        WataLog.d("category_nm=" + category_nm);
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
    private VoucherWebBridge mVoucherWebBridge;

    @SuppressLint("SetJavaScriptEnabled")
    private void webViewSetting() {
        // 웹뷰 셋팅팅
        mWebView = (WebView) findViewById(R.id.voucher_webview);
        mVoucherWebBridge = new VoucherActivity_GPS.VoucherWebBridge(VoucherActivity_GPS.this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAppCacheEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }

        mWebView.addJavascriptInterface(mVoucherWebBridge, "WATA");
        mWebView.setWebViewClient(new VoucherActivity_GPS.WebViewClientClass());
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

        private VoucherActivity_GPS mVoucherActivity;

        public VoucherWebBridge(VoucherActivity_GPS voucherActivity) {
            mVoucherActivity = voucherActivity;
        }

        // web지도가 정상적으로 보여지면 호출됨
        @JavascriptInterface
        public void readyToDraw() {
            WataLog.d("readyToDraw");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mVoucherActivity.setLoadMapLine();
                }
            });
        }

        // web지도가 정상적으로 보여지면 호출됨
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
        if ("setRotation".equals(webId) || "drawLineCoords".equals(webId)) {
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
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(VoucherActivity_GPS.this);
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
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_upload_btn:
                AlertDialog.Builder alBuilder = new AlertDialog.Builder(VoucherActivity_GPS.this);
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

                AlertDialog.Builder poiBuilder = new AlertDialog.Builder(VoucherActivity_GPS.this);
                poiBuilder.setMessage("poi 파일을 전송하시겠습니까?");
                poiBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressText.setText("파일 업로드 중입니다.");
                        setProgress(true);
                        Date currentTime = Calendar.getInstance().getTime();
                        POI_FILE = new SimpleDateFormat("MMdd-HH:mm", Locale.getDefault()).format(currentTime) + "_" + StaticManager.subwayName;
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

                    setPoiPointMark(POI_pointX, POI_pointY, poiRName, true);
                    poi_point_layout.setVisibility(View.GONE);
                    onKeyboardHide(poi_edit);

                    WataLog.d("poiRName=" + poiRName);
                    setWebView("setPOIContent", poiRName);

                    WataLog.d("POI_point", POI_pointX + "," + POI_pointY);
                    setPOITestLog(POI_pointX, POI_pointY, poiRName);
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
                WataLog.d("IS_RECORDING=" + IS_RECORDING);
                if (IS_RECORDING) { // 종료
                    onRecordOff();
                    setWebView("toggleTrackLine");
                } else { // 시작
//                    lineHeader = getHeader(START_POINT_VECTOR, END_POINT_VECTOR);
                    onRecordOn();
                }
                break;
            case R.id.step_btn:
                WataLog.i("지점수집");

                POINT_CHECK = true;
                onRecordOn();

                GPSTimer gpsTimer = new GPSTimer();
                mGpsPoint = new Timer();
                mGpsPoint.scheduleAtFixedRate(gpsTimer , 7000, 1000);
                WataLog.d("!!!!!!!- !!!!!!!!!");
                progressText.setText("수집중입니다. 잠시만 기디려주세요.");
                setProgress(true);

                break;
        }
    }

    private Timer mGpsPoint;
    private boolean POINT_CHECK = false;
    class GPSTimer extends TimerTask {

        public void run() {
            WataLog.d("ew Date()=" + new Date());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onRecordOff();
                    mGpsPoint.cancel();
                    setProgress(false);
                    POINT_CHECK = false;
                }
            });


        }
    }

    private void onRecordOff() {
        startBtText.setText(getString(R.string.start));
        IS_RECORDING = false;

        if (!REVERSE_RECORDING) {
            setWebView("endDrawPoint");
        } else {
            setWebView("endReverse");
        }
        OVER_LINE = false;
        setRecordGathering();

        SCAN_POINT_X = 0.0;
        SCAN_POINT_Y = 0.0;

//                    mStepLength = 0;
        recordStepCount.setText("0");
        recordStepLength.setText("0 m");
    }

    private void onRecordOn() {
        START_POINT_X = currentPosition.longitude;
        START_POINT_Y = currentPosition.latitude;

        NOW_POSITION++;
        WataLog.d("NOW_POSITION=" + NOW_POSITION);

        SharedPreferences sf = getSharedPreferences("GPS", MODE_PRIVATE);
        SharedPreferences.Editor editor = sf.edit();
        editor.putString("link_id", String.valueOf(NOW_POSITION));
        editor.commit();

        progressText.setText("수집 준비중입니다. 잠시만 기디려주세요.");
        setProgress(true);
        startBtText.setText(getString(R.string.stop));
        IS_RECORDING = true;

        OVER_LINE = false;

//        startRedSignal(true);

        setWebView("initStartPt");

        // 정북기준 각도값 입력
        lineHeader = 45.0;

        //GatheringDialog.makeGatherNameInsertDailog(GatheringActivity.this, StaticManager.title, StaticManager.floorName, _UIHandler, 9999).show();
        Date currentTime = Calendar.getInstance().getTime();
        String curTime = new SimpleDateFormat("MMdd-HH:mm", Locale.getDefault()).format(currentTime);
        WataLog.d("curTime=" + curTime);

        // 파일이름생성
        final String autoName = "GPS수집" + "-" + curTime;

        WataLog.d("autoName=" + autoName);

        VoucherDataCore dataCore = VoucherDataCore.getInstance();

//                  _startPoint = new Vector(line.getPart(0).getStartPoint().x, line.getPart(0).getStartPoint().y, line.getPart(0).getStartPoint().z);
//                  _endPoint = new Vector(line.getPart(0).getEndPoint().x, line.getPart(0).getEndPoint().y, line.getPart(0).getEndPoint().z);
//                  distRemain = -1;
//        lineHeader = getHeader(START_POINT_VECTOR, END_POINT_VECTOR)
        lineHeader = Double.NaN;

        pdrvariable.initValues();
//                    initialmm.MM_initialize3(line);
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
        sensorSet(false);
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

            SaveGatheredData.saveVoucherData(StaticManager.getResultVoucherPath(), _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
            SaveGatheredData.saveVoucherSendableData(StaticManager.getResultVoucherPath(), _dataCore.getCurGatheringName() + "_F.temp", _SCAN_RESULT_THIS_EPOCH);

            SaveGatheredData.saveVoucherData(StaticManager.getResultVoucherPath(), _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
            SaveGatheredData.saveVoucherSendableData(StaticManager.getResultVoucherPath(), _dataCore.getCurGatheringName() + "_R.temp", _SCAN_RESULT_THIS_EPOCH);
        }


        _SCAN_RESULT_THIS_EPOCH = new PDI_REPT_COL_DATA_VOUCHER(Build.SERIAL);
        WataLog.d("Build.SERIAL=" + Build.SERIAL);
        _SCAN_RESULT_TOTAL = new ArrayList<PDI_REPT_COL_DATA_VOUCHER>();

        //Tag : TOTAL 초기화
        _dataCore.setSCAN_RESULT_TOTAL(_SCAN_RESULT_TOTAL);
        _dataCore.setSCAN_RESULT_THIS_EPOCH(_SCAN_RESULT_THIS_EPOCH);

//        isInverse = false;
        DataCore.isOnGathering = false;
    }

    private VoucherSettingDialog mPsettingDialog;

    private void setSetting() {
        mPsettingDialog = new VoucherSettingDialog(VoucherActivity_GPS.this, new OnClickListener() {
            public void onClick(View v) {
                WataLog.i("check 0");
                SettingDialog sensitive = new SettingDialog();
                AlertDialog senseDialog = sensitive.makeSensitiveSettingDilog(VoucherActivity_GPS.this);
                senseDialog.show();
            }
        });

        mPsettingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                WataLog.i("onDismiss");

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
            _UIHandler = new GetheringUIHandler();

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
                            ((Activity) VoucherActivity_GPS.this).startActivityForResult(
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
    private void sensorSet(boolean b) {
        try {
            if (b) {
                if (_senact == null) {
                    _senact = new sensoract(getApplication(), _UIHandler);
                    WataLog.e("_senact=" + _senact);
                    if (_sensorthread == null) {
                        _sensorthread = new Thread(_senact);
                        _sensorthread.start();
                    }
                }

            } else {
                if (_sensorthread != null) {
                    try {
                        _senact.requestStop();
                        _sensorthread.interrupt();
//                        _sensorthread.stop();
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
        } catch (Exception e) {
            WataLog.e("Exception=" + e.toString());
            onToast(Gravity.CENTER, "Wi-Fi Scanner가 정상 동작하지 않았습니다. 다시 실행해주세요. ");
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

                            String lat = String.valueOf(currentPosition.latitude);
                            String lon = String.valueOf(currentPosition.longitude);

                            setWebView("setSignalPosition", lon + "|" + lat);

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
                              if(!POINT_CHECK) {
                                  onToast(Gravity.CENTER, getString(R.string.start_recording));
                              } else {
                                  onToast(Gravity.CENTER, "수집이 완료 되었습니다.");
                              }
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
            SaveGatheredData.saveData(StaticManager.getResultVoucherPath(), mPoiFileName, poiJsonRecord);

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
                    WataLog.d("list[a]=" + list[a]);
                    String name = list[a].toString();
                    String[] fileName = name.split("\\.");
                    WataLog.d("fileName=" + fileName);
                    if (!fileName[1].equals("log")) {
                        WataLog.d("list[a]=" + list[a]);
                        mFileList.add(list[a]);
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
                WataLog.d("test", "onFailure");
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
        String path = StaticManager.getResultVoucherPath() + fileName;
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

                    URL url = new URL(Constance.DEVE_JUDI_SERVER +"/api/collect/poi/" + StaticManager.gid);
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
        String dataPath = StaticManager.getResultVoucherPath(); // 파일저장된 경로설정
        File pathFolder = new File(dataPath);

        if (pathFolder.exists()) {
            String[] list = pathFolder.list();
            if (list != null) {
                for (int a = 0; a <= list.length - 1; a++) {
                    String name = list[a].toString();
                    String[] fileName = name.split("\\.");
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
            String jsonData = JsonUtils.INSTANCE.getData(StaticManager.getResultVoucherPath() + mPoiList.get(i));
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

    public static void setWifi5G(String SSID, short Freq) {

        WataLog.d("SSID=" + SSID);
        WataLog.d("Freq=" + Freq);

    }

}

