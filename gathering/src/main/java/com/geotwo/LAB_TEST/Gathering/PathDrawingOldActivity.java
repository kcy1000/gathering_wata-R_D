package com.geotwo.LAB_TEST.Gathering;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.geotwo.ImageMap.WCSFReader;
import com.geotwo.ImageMap.WMapSurfaceView;
import com.geotwo.LAB_TEST.Gathering.clicklistener.GatheringMapTouchListener;
import com.geotwo.LAB_TEST.Gathering.dialog.GatheringStartDialog;
import com.geotwo.LAB_TEST.Gathering.dto.MyWayInfo;
import com.geotwo.LAB_TEST.Gathering.dto.PoiInfo;
import com.geotwo.LAB_TEST.Gathering.dto.StepInfo;
import com.geotwo.LAB_TEST.Gathering.savedata.SaveGatheredData;
import com.geotwo.LAB_TEST.Gathering.ui.RecordListAdapter;
import com.geotwo.LAB_TEST.Gathering.ui.PoiListAdapter;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.data.StaticManager;
import com.geotwo.anim.interpolator.BounceInterpolator;
import com.geotwo.ble.BLEMap;
import com.geotwo.ble.e_BLEScandata;
import com.geotwo.common.AndroidUtils;
import com.geotwo.common.AvgFilter;
import com.geotwo.common.Iron2Calibrator;
import com.geotwo.common.JsonUtils;
import com.geotwo.common.KalmanFilter;
import com.geotwo.common.Proj4;
import com.geotwo.common.SensorUtils;
import com.geotwo.o2mapmobile.Camera;
import com.geotwo.o2mapmobile.InputHandler;
import com.geotwo.o2mapmobile.O2Map;
import com.geotwo.o2mapmobile.element.Element;
import com.geotwo.o2mapmobile.element.ElementHelper;
import com.geotwo.o2mapmobile.geometry.Angle;
import com.geotwo.o2mapmobile.geometry.Line;
import com.geotwo.o2mapmobile.geometry.Plane;
import com.geotwo.o2mapmobile.geometry.Quaternion;
import com.geotwo.o2mapmobile.geometry.Vector;
import com.geotwo.o2mapmobile.inputhandler.MultiInputHandler;
import com.geotwo.o2mapmobile.inputhandler.OrbitInputHandler;
import com.geotwo.o2mapmobile.model.CompositeModel;
import com.geotwo.o2mapmobile.model.SandboxModel;
import com.geotwo.o2mapmobile.path.Node;
import com.geotwo.o2mapmobile.path.PathManager;
import com.geotwo.o2mapmobile.shape.Geometry;
import com.geotwo.o2mapmobile.shape.Point;
import com.geotwo.o2mapmobile.shape.Points;
import com.geotwo.o2mapmobile.shape.Polyline;
import com.geotwo.o2mapmobile.util.Color;
import com.geotwo.o2mapmobile.view.ViewUtil;
import com.wata.LAB_TEST.Gathering.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.proj4.CRSRegistry;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA;
import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA_PAYLOAD;
import GEO2.LBSP.PositionProvider.PositionProvider;
import GEO2.LBSP.SClient.PDI_SCANINFO;
import GEO2.LBSP.SClient.RspPosition;
import LBSP.Common.Interfaces.PDI_RSP_POS;
import geo2.lbsp.ible.BLEManager;
import geo2.lbsp.ible.ServiceReadyCallback;
import geo2.lbsp.ible.StartCompletedListener;
import pdr.inter.pdr_interface;
import pdr_collecting.core.initialmm;
import pdr_collecting.core.newPdralgorithm;
import pdr_collecting.core.pdrvariable;
import pdr_collecting.core.sensoract;

//import com.geotwo.o2mapmobile.shape.CSFReader;

//import geo2.lbsp.ble.BLEManager;

//import geo2.lbsp.ble.BLEManager;

public class PathDrawingOldActivity extends Activity implements OnClickListener, SensorEventListener, ViewPager.PageTransformer {
    static {
        System.loadLibrary("proj");
    }

//    public static int s_mode = 0; // 0 : 이동 1 : POI편집 2 : 사진편집

    private Button pathInverse, calBtn;
    private RelativeLayout _mapLayout = null;

//    double _prevAngle = 0;

    ///////////////////////////////////////////////////
    //O2MapSurfaceView _omapView;		// GLSufaceView
    WMapSurfaceView _omapView;        // GLSufaceView
    sensoract _senact = null;
    ///////////////////////////////////////////////////

    //    ButtonClickListener _clickListener = null;
    GatheringMapTouchListener _mapTouchListener = null;

    Polyline _curLine = null;
    static public Vector _startPoint = null;
    static public Vector _endPoint = null;
    PathManager _pathManager = null;

    //    Toast _gatheringEndToast = null;
    Thread _sensorthread = null;
//    AlertDialog _gatheringEndDialog = null;

    //    boolean _isRouteDrawing = false;
//    boolean _isGatherEnded = false;
    boolean _isHandlerEnded = true;

    static public Handler _UIHandler = null;
    static public boolean bIsGetFP = false;

    DataCore _dataCore = null;
    PathDWifiScanner _wifiScanner = null;
    private com.geotwo.TBP.TBPWiFiScanner wifiScanner;            // 측위 버튼

    List<ScanResult> _list = null;

//    String _buildName = "";
//    String _floor = "";
//    String _fileName = "";

    int _checkedItem = 0;
    int _prevWalkedDistance = 0;
    double _prevDegree = 0;

    float _prevX = 0;
    float _prevY = 0;

    ArrayList<PDI_REPT_COL_DATA> _SCAN_RESULT_TOTAL = null;
    PDI_REPT_COL_DATA _SCAN_RESULT_THIS_EPOCH = null;

    //    static public int pathFlag = 0;
    private boolean gatheringFlag = false, bCal = false;
    private int calIdx = -1;

    private boolean useLight = false;
    //private CSFReader reader = null;

    private WCSFReader wReader = null;
    private String pathPath;
    //	private Polyline pathPolyline; //경로 수집 방법에서 시작버튼 누르면 수집 시작하기 위해 멤버변수화
    private double endX = 0.0;
    private double endY = 0.0;
    private final float IMAGE_WIDTH_SIZE = 240.0f;
    private final float IMAGE_HEIGHT_SIZE = 410.0f;
    private SandboxModel prevPathLayer = null;                // Path Layer
    private SandboxModel testNumlayer = null;                // Path Number Layer
    private SandboxModel myWayNum = null;                // Path Number Layer
    private SandboxModel myWayLine = null, myRecordFLine = null, myRecordRLine = null, mCheckPoint = null;                // line 미리보기

    ArrayList<File> fileList = null;

    int[] mNumTest;

    static public double basePointX = 0, basePointY = 0; // TM 1m Resolution , 지도는 => 10cm Resolution
    static public int mCurrentPath = -1;
    private Typeface tff = null;
    private int hitValue = 30, sucRate = 0, errorCnt = 0; // hitValue 적중율 계산하기 위한 .. (30m) 의 의미 , sucRate : dist 가 30m 이내면 ++
    private double min = 0, max = 0, avg = 0, var = 0;
    private long startTime = 0;
    private AvgFilter mAf;
    private int gatherCnt = 0; // 0,0 을 제외하고 실제로 수집된 포인트 갯수

    public static boolean isInverse = false, filter30 = false, forNis = true;

    private GatheringStartDialog mStartDialog;
    private int sCnt = 0;
    private String scanType = "1"; // 1 = 현행화 , 2 = 검수
    public static boolean bUseStatistics = true;
    public static boolean bLogTypeOnlySuc = true;
    // BLE
    BLEManager _bleManager = null;

    public static int iFrequency = 2;

    // 영상정보수집로그
    private boolean saveLogForImage = false;

    //    private TextView gatheringOrientationDegree = null;
//    private ImageView gatheringOrientation = null;
    private int stepCount = 0;

    //맵회전하기
    private boolean isMapRatation = false;
    private boolean isGyroSensor = false;

    //hunyeon add code, 측정 후 재 측정 수정1, 경로번호를 저장한다.
    int _which = 0;

    private O2Map o2map;
    private Button record_list_btn, reverse_record_btn, poi_list_btn, my_way_path_start, my_way_path_end, record_cancel, step_btn;
    private RelativeLayout record_lisetview, my_way_record_info_layout, poi_point_layout, poi_listview_rlayout, poi_edit_box_layout, progress_layout;
    private ListView mPathDrawingListview, mPoiListview;
    private Button list_ok_btn, poi_list_ok_btn, save_point_check_btn, precision_btn, path_drawing_setting;
    private Polyline mLastPathPoint; //매회 최근경로
    private EditText poi_edit, poi_edit_text;
    private ProgressBar progress;
    private TextView gatheringInfo = null; //
    private LinearLayout angle_layout, angle_control_layout;

    private int mWidthPixel = 0;
    private int mHeightPixels = 0;
    private int mRecordIndexNum = 0;
    public static int pathFlag = 0;

    private boolean ROAD_MODE = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WataLog.i("onCreate");
        // load settings
        isMapRatation = PathDrawingSettingDialog.isMapRatation(this);

        setBasePoint(); //Tag : base X, base Y Setting
        tff = Typeface.createFromAsset(this.getAssets(), "NanumBarunGothic.ttf"); //Font

        _dataCore = DataCore.getInstance();
        _dataCore.setFloorString(StaticManager.floorName);
        _dataCore.setBuildName(StaticManager.title);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        mWidthPixel = dm.widthPixels;
        mHeightPixels = dm.heightPixels;
        WataLog.d("mWidthPixel=" + mWidthPixel);
        WataLog.d("mHeightPixels=" + mHeightPixels);

//        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        try {
            initMap(); //Tag : Map이나 button 설정
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        initView(true);

        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getExtras().getString("file_name");
            SAVE_FILE_NAME = name;
            WataLog.d("name=" + name);
            ROAD_MODE = intent.getBooleanExtra("load_file", false);
            WataLog.d("ROAD_MODE=" + ROAD_MODE);
        }

        loadDataWCSF(); // Load json Data
        WataLog.d("base x=" + basePointX + "/y=" + basePointY);
        basePointX = 0.0;
        basePointY = 0.0;

        o2map = ((WMapSurfaceView) _omapView).getO2mapInstance();
        InputHandler han = o2map.getInputHandler();
//        o2map.getView().getCamera().setDistance(200.0);
        if (han instanceof MultiInputHandler) {
            MultiInputHandler mhan = (MultiInputHandler) han;
            if (mhan.getCurrentInputHandler() instanceof OrbitInputHandler) {

                ((OrbitInputHandler) mhan.getCurrentInputHandler()).setVerticalRotation(true);
            }
        }

        setMapInit();

        o2map.getView().getCamera().setDistance(50.0);
//        ROAD_MODE = true;
        if (ROAD_MODE) {
            String lineJson = JsonUtils.INSTANCE.getData(Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pathGathering/PathDrawing/" + SAVE_FILE_NAME  + "/LINE/" + "LINE.json");
            if (lineJson != null) {
                lineJson = lineJson.replace("\\", "");
                WataLog.d("lineJson = " + lineJson);
                setLineReader(lineJson);
            }

            String poiJson = JsonUtils.INSTANCE.getData(Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pathGathering/PathDrawing/" + SAVE_FILE_NAME  + "/POI/" + "POI.log");
            WataLog.d("poiJson=" + poiJson);
            if (poiJson != null) {
                poiJson = poiJson.replace("\\", "");
                WataLog.d("poiJson = " + poiJson);
                setPoiReader(poiJson);
            }
        } else {
//            setMapSetting();
        }

    }

    private void setMapView() {
        _omapView.getO2mapInstance().renderLock();
        refreshNorthIcon();
        _omapView.getO2mapInstance().renderUnlock();

        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("MMdd-HH:mm", Locale.getDefault()).format(currentTime);
        String pdName = SAVE_FILE_NAME.substring(0, 5) + "..." + date_text;
        StaticManager.setPathDrawingName(pdName);
        WataLog.d("pdName=" + pdName);

        String dataPath = Environment.getExternalStorageDirectory().getPath()
                + "/gathering/saveData/pathGathering/" + StaticManager.folderName + "/" + StaticManager.pathDrawingName + "/";

        WataLog.d("dataPath=====" + dataPath);                // /gathering/saveData/pathGathering/Gangnam/AB01/
        fileList = getGatheringPathData(dataPath);
        mNumTest = new int[fileList.size()];

        boolean bAdd = false;

        if (prevPathLayer == null) {
            prevPathLayer = new SandboxModel("prevPathLayer");
            bAdd = true;
        }
        WataLog.d("wReader=" + wReader);
        Element el = null;
        if (wReader != null) {
            _omapView.getO2mapInstance().renderLock();

            WataLog.d("basePointX=" + basePointX);
            WataLog.d("basePointY=" + basePointY);
            setIndoorMap(StaticManager.folderName, basePointX, basePointY);

            ((CompositeModel) o2map.getModel()).addModel(prevPathLayer);        // Add Layer
            _omapView.getO2mapInstance().renderUnlock();
        }
        _bleManager = new BLEManager(this);

        if (!_bleManager.isBluetoothEnabled()) {
            geo2.lbsp.ible.Utils.startBluetooth(this, new StartCompletedListener() {
                public void onStartCompleted() {
                    Toast.makeText(PathDrawingOldActivity.this, "'Bluetooth가 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        _bleManager.connect(new ServiceReadyCallback() {
            public void onServiceReady() {
                if (_bleManager != null)
                    _bleManager.startService();
            }
        });

        ViewUtil.FitTopView(_omapView.getO2mapInstance().getView(), _omapView.getO2mapInstance().getModel().getExtent());
        //_omapView.requestRender();

        displayPathFinish();

        // 기록내역
        myWayListAdapter = new RecordListAdapter(this);
        myWayListAdapter.setOnItemClickListener(new RecordListAdapter.OnItemClickListner() {
            @Override
            public void onRecordStartPoint(MyWayInfo items, int position) {
                //시작지점이동
//                isInverse = true; // 역방향, 정방향 구별해야함. - kcy1000
                WataLog.i("onRecordStartPoint!");
                WataLog.d("position=" + position);
                WataLog.d("mRecordPosition=" + mRecordPosition);
//                CAMERA_MOVING = true;
//                isMapRatation = false;
//                PathDrawingSettingDialog.setMapRatation(getApplicationContext(), false);

                record_lisetview.setVisibility(View.GONE);
                onToast(Gravity.CENTER, getString(R.string.point_movement_message));

//                mLastPointX = items.getStartPointX();
//                mLastPointY = items.getStartPointY();
                setMapLookPosition(mLastPointX, mLastPointY, 0);
                setStartPointLine();

//                onRecordPointMoving();
            }

            @Override
            public void onRecordEndPoint(MyWayInfo items, int position) {
                //종료지점이동
                WataLog.i("onRecordEndPoint!");
//                CAMERA_MOVING = true;
//                isMapRatation = false;
//                PathDrawingSettingDialog.setMapRatation(getApplicationContext(), false);

                record_lisetview.setVisibility(View.GONE);
                onToast(Gravity.CENTER, getString(R.string.point_movement_message));

//                mLastPointX = items.EndPointX;
//                mLastPointY = items.EndPointY;
                setMapLookPosition(mLastPointX, mLastPointY, 0);
                setStartPointLine();

//                setStartEndPointName();
//                onRecordPointMoving();
            }

            @Override
            public void onPoiSelectLine(MyWayInfo items, int position) {

            }

            @Override
            public void onReverseRecord(MyWayInfo items, int position) {
                try {
                    isInverse = true;
                    CAMERA_MOVING = false;
                    WataLog.d("items.Number=" + items.Number);
                    mRecordPosition = items.Number;
                    mRecordIndexNum = position;
                    onToast(Gravity.CENTER, getString(R.string.revers_record_start_message));
//                    setRecordPointSetting(items.Number, items.EndPointX, items.EndPointY, items.StartPointX, items.StartPointY);
                    setReverLine();
                    setStartEndPointName();
                    mCameraAngle = items.Angle + 180;

//                    Vector cancelV = new Vector(items.StartPointX, items.StartPointY, 0, 0);
//                    _omapView.getO2mapInstance().getView().getCamera().setLookAtPosition(cancelV);

                    onRecordStart();
                } catch (Exception e) {
                    WataLog.e("Exception=" + e.toString());
                    onToast(Gravity.CENTER, getString(R.string.error_message_1));
                }
            }
        });

        // poi 내역
        mPoiListAdapter = new PoiListAdapter(this);
        mPoiListAdapter.setOnItemClickListener(new PoiListAdapter.OnItemClickListner() {
            @Override
            public void onPoiPoint(PoiInfo items, int position) {
                WataLog.d("position=" + position);
                // poi 지점이동
//                mLastPointX = items.PositionX;
//                mLastPointY = items.PositionY;
//                setStartPointSetting(true);

                WataLog.d("items.PositionX=" + items.getPoiPositionX());
                WataLog.d("items.PositionY=" + items.getPoiPositionY());

//                mLastPointX = items.getPoiPositionX();
//                mLastPointY = items.getPoiPositionY();
                onToast(Gravity.CENTER, getString(R.string.point_movement_message));
                setStartPointLine();
                poi_listview_rlayout.setVisibility(View.GONE);


            }

            @Override
            public void onPoiDelete(PoiInfo items, int position) {
                WataLog.d("삭제position=" + position);

                int num = items.getNumber();
                // poi삭제
                mPoiInfoData.remove(position);
                mPoiListAdapter.notifyDataSetChanged();

                if (items.getPoiText().equals(getString(R.string.point))) {
                    //지점
                    mCheckPoint.removeElement("mCheckPoint" + String.valueOf(num));
                } else {
                    //POI
                    PoiMessageModel.removeElement("PoiMessageModel" + String.valueOf(num));
                }

                _omapView.getO2mapInstance().requestRedraw();
//                ((CompositeModel) o2map.getModel()).removeModel(PoiMessageModel);
            }

            @Override
            public void onPoiNameEdit(PoiInfo items, int position) {
                WataLog.d("position=" + position);
                POI_POSITION = position;
                mPoiINfoItem = items;
                // poi 이름변경
                poi_edit_box_layout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onRecordPointMoving() {
        Toast.makeText(PathDrawingOldActivity.this, "수집 시작지점으로 이동해주세요.", Toast.LENGTH_SHORT).show();
        setUiControl(2);

        CAMERA_MOVING = true;
        mRotation = mCameraAngle;

        setStartPointSetting(true);
        record_lisetview.setVisibility(View.GONE);

        Vector v = new Vector(mLastPointX, mLastPointY, 0, 0);
        _omapView.getO2mapInstance().getView().getCamera().setLookAtPosition(v);
    }


    private boolean CAMERA_MOVING = false;

//    private void setDrawingEndPoint() {
//        //시작점 저장하기
//        mRecordStartPointX = mLastPointX;
//        mRecordStartPointY = mLastPointY;
//
//        WataLog.d("mRecordStartPointX=" + mRecordStartPointX);
//        WataLog.d("mRecordStartPointY=" + mRecordStartPointY);
//        if (twoPointerLayer != null) {
//            twoPointerLayer.removeAllElement();
//        }
//
//        lineSelected = (Polyline) getMyWayLine(mLastPointX, mLastPointY);
//        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getStartPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getStartPoint().y);
//        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getEndPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getEndPoint().y);
//        WataLog.d("testNumlayer=" + testNumlayer);
//        if (startPointModel != null) {
//            startPointModel.removeAllElement();
//        }
//        if (endPointModel != null) {
//            endPointModel.removeAllElement();
//        }
//
//        Element s_el = ElementHelper.fromTextBillboard(new Vector(lineSelected.getPart(0).getStartPoint().x, lineSelected.getPart(0).getStartPoint().y, 5.0, 0), 2f, 2f, true, "S", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
//        if (s_el != null) {
//            startPointModel = new SandboxModel("startPointModel");
//            startPointModel.addElement("startPointModel", s_el);
//        }
//        ((CompositeModel) o2map.getModel()).addModel(startPointModel);
//
//        Element e_el = ElementHelper.fromTextBillboard(new Vector(lineSelected.getPart(0).getEndPoint().x, lineSelected.getPart(0).getEndPoint().y, 5.0, 0), 2f, 2f, true, "E", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
//        if (e_el != null) {
//            endPointModel = new SandboxModel("endPointModel");
//            endPointModel.addElement("endPointModel", e_el);
//        }
//        ((CompositeModel) o2map.getModel()).addModel(endPointModel);
//
//        //  라인그리기
//        if (lineSelected != null) {
//            if (myWayLine == null) {
//                myWayLine = new SandboxModel("myWayLine");
//            } else {
//                myWayLine.removeAllElement();
//            }
//
//            Element eal = ElementHelper.fromPolyline(lineSelected, 0.1, 2, new Color(0, 255, 0, 255), false, false);
//            myWayLine.addElement("myWayLine" + mPointCount, eal);
//            ((CompositeModel) o2map.getModel()).addModel(myWayLine);
//        }
//
//        _omapView.getO2mapInstance().requestRedraw();
//
////        o2map.renderUnlock();
////        if (lineSelected != null) {
////            startMyWayGathering(lineSelected);
////        }
//    }


    private int POI_POSITION = 0;
    private PoiInfo mPoiINfoItem = null;
    private SandboxModel indoorMapLayer = null;

    public void setIndoorMap(String area, double x, double y) {
        WataLog.d("area=" + area + " x= " + x + "y" + y);
        O2Map o2map = (_omapView.getO2mapInstance());
        boolean bAdd = false;

//        if (indoorMapLayer == null) {
        indoorMapLayer = new SandboxModel("indoorMap");
        bAdd = true;
//        }
        float w = 4f;
        float h = 4f;

        if (StaticManager.folderName.equalsIgnoreCase("office")) {
            w = 1f;
            h = 1f;
        }

        String mapPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/area/" + "PathDrawing";
        Element el = null;
        WataLog.d("area = " + area);
        switch (area) {
            case "PathDrawing": //Tag : 시작 지점 표시
            {
//                float WIDTH_SIZE = 7688.0f;
//                float HEIGHT_SIZE = 13112.0f;
                float WIDTH_SIZE = 240.0f;
                float HEIGHT_SIZE = 410.0f;
//                float WIDTH_SIZE = 960.0f;
//                float HEIGHT_SIZE = 1640.0f;
                el = ElementHelper.fromImage(WIDTH_SIZE, HEIGHT_SIZE, mapPath + "/gangnam_map_all_sample_3.png");
                el.getTransform().clearTransform();
                el.getTransform().setTranslation((WIDTH_SIZE / 2), -(HEIGHT_SIZE / 2), 2);
                el.getTransform().calcMatrix();
                el.getExtentHelper().updateExtent();
                WataLog.i("check1!");
                indoorMapLayer.addElement(area, el);
                if (bAdd) {
                    //indoorMapLayer.setOrder(100);
                    ((CompositeModel) o2map.getModel()).addModel(indoorMapLayer);
                }
                break;
            }

            case "Gangnam": //Tag : 시작 지점 표시
            {
                float WIDTH_SIZE = 240.0f;
                float HEIGHT_SIZE = 410.0f;

                el = ElementHelper.fromImage(WIDTH_SIZE, HEIGHT_SIZE, mapPath + "/gangnam_map_all_sample_1.png");
                el.getTransform().clearTransform();
                el.getTransform().setTranslation((WIDTH_SIZE / 2), -(HEIGHT_SIZE / 2), 2);
                el.getTransform().calcMatrix();
                el.getExtentHelper().updateExtent();
                WataLog.i("check1!");
                indoorMapLayer.addElement(area, el);
                if (bAdd) {
                    //indoorMapLayer.setOrder(100);
                    ((CompositeModel) o2map.getModel()).addModel(indoorMapLayer);
                }
                break;
            }

            case "AUS_WestfieldChatswood": //Tag : 시작 지점 표시
            {
                float WIDTH_SIZE = 289.0f;
                float HEIGHT_SIZE = 243.0f;

                el = ElementHelper.fromImage(WIDTH_SIZE, HEIGHT_SIZE, mapPath + "/westfield_chatswood.png");

                el.getTransform().clearTransform();
                el.getTransform().setTranslation((WIDTH_SIZE / 2), -(HEIGHT_SIZE / 2), 2);
                el.getTransform().calcMatrix();
                el.getExtentHelper().updateExtent();
                WataLog.i("check1!");
                indoorMapLayer.addElement(area, el);
                if (bAdd) {
                    //indoorMapLayer.setOrder(100);
                    ((CompositeModel) o2map.getModel()).addModel(indoorMapLayer);
                }
                break;
            }
        }
    }

    private void setBasePoint() {
        if (StaticManager.basePointX == null) {
            basePointX = 0;
        } else {
            basePointX = Double.valueOf(StaticManager.basePointX);
        }

        if (StaticManager.basePointY == null) {
            basePointY = 0;
        } else {
            basePointY = Double.valueOf(StaticManager.basePointY);
        }
    }

    private SandboxModel currentPosLayer = null;
    private SandboxModel testPosLayer = null;

//    protected void updatePointer(Vector v) {
//        WataLog.i("updatePointer");
//        O2Map o2map = (this._omapView.getO2mapInstance());
//
//        o2map.renderLock();
//
//        boolean bAdd = false;
//
//        //currentPosLayer = (SandboxModel) ((CompositeModel)o2map.getModel()).getModelByName("currentPos");
//
//        if (currentPosLayer == null) {
//            currentPosLayer = new SandboxModel("currentPos");
//            bAdd = true;
//
//        }
//
//        double pointerSize = 3;
//        double pointerSize2 = 3;
//        if (StaticManager.folderName.equalsIgnoreCase("office")) {
//            pointerSize = 0.6;
//            pointerSize2 = 0.6;
//        }
//
//        // Update Position : by Ethan
//        Vector lookAt = v;//o2map.getView().getCamera().getLookAtPosition();
//        o2map.getView().getCamera().setLookAtPosition(lookAt);
//        Element el = ElementHelper.quadPyramidFromPoint(lookAt.x, lookAt.y, lookAt.z + 0.1, pointerSize, pointerSize, pointerSize2, Color.RED, Color.RED, Color.RED, false);
//        WataLog.d("red: x" + lookAt.x + " y:" + lookAt.y + " z:" + lookAt.z);
//        if (currentPosLayer.size() > 0) {
//            currentPosLayer.removeElement("tester");
//        }
//
////        currentPosLayer.addElement("tester", el);
//
//        if (bAdd) {
//            ((CompositeModel) o2map.getModel()).addModel(currentPosLayer);
//        }
//        o2map.renderUnlock();
//
//    }
//
//    protected void updateCurrentPath(Polyline line, int i) {
//        WataLog.i("updateCurrentPath");
//        O2Map o2map = (this._omapView.getO2mapInstance());
//
//        boolean bAdd = false;
//        if (twoPointerLayer == null) {
//            twoPointerLayer = new SandboxModel("currentPath");
//            bAdd = true;
//        }
//
//        //if(pointerLayer.size()>0) {
//        //	pointerLayer.removeElement(new String("pointer"));
//        //}
//        if (line != null) {
//            WataLog.i("fromPolyline check!!!");
//            Element el = ElementHelper.fromPolyline(line, 0.1, 2, new Color(255, 0, 0, 255), false, false);
//            WataLog.d("pointerPath_" + i);
//
//            WataLog.i("check1!");
//            twoPointerLayer.addElement("pointerPath_" + i, el);
//        }
//
//        if (bAdd)
//            ((CompositeModel) o2map.getModel()).addModel(twoPointerLayer);
//    }

    private PathManager _pathManager_for_nis;
    private ArrayList<String> _testpointName;
    private ArrayList<Node> _pathManager_list;
    private String _testTouchPoint = null;

    protected void loadDataWCSF() {
        WataLog.i("loadDataWCSF");

        StaticManager.setFolderName("PathDrawing");
        StaticManager.setFloorName("AB01");
        // Path Data : 신규로 만들어야함
        pathPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/area/"
                + StaticManager.folderName + "/" + StaticManager.floorName + "_Path.json";

        // gathering/area/Gangnam/AB01_Path.json

        WataLog.d("pathPath = " + pathPath);
        String strJson = JsonUtils.INSTANCE.getData(pathPath);
        WataLog.d("strJson = " + strJson);

//        setLineReader(StaticManager.getResultLinePath()+ "LINE.log");
//        String lineJson = JsonUtils.INSTANCE.getData(Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pathGathering/PathDrawing/test_1123/LINE/" + "LINE.json");
//        if(lineJson != null) {
//            lineJson = lineJson.replace("\\", "");
//            WataLog.d("lineJson = " + lineJson);
//            setLineReader(lineJson);
//        }

        // Draw Path : Ethan
        wReader = WCSFReader.getParsing(strJson);
        if (wReader != null) {
            SandboxModel testPathLayer = new SandboxModel("testPath");

            _pathManager = new PathManager();
            _pathManager.setEpsilon(1);            //10cm

            for (int i = 0; i < wReader.getCount(); i++) {

                Polyline line = null;
                try {
                    line = (Polyline) wReader.getLine(i);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Color color = null;
                color = new Color(12, 59, 146, 255);
                Element el = ElementHelper.fromPolyline(line, 0.1, 1, color, false, useLight);
                String key = String.format("path%d", i);
                testPathLayer.addElement(key, el);

                Points pts = line.getPart(0);
                Point start = pts.data[0];
                Point end = pts.data[pts.getNumPoints() - 1];

                boolean testmode = false; //시작점 종료점 표시를 위한 testmode
                if (testmode) {
                    WataLog.i("check1!");
                    Element els = ElementHelper.fromTextBillboard(new Vector(start.x - 0.5, start.y - 0.5, 0.3, 0), 1f, 1f, true, "S", 10.0f, Typeface.DEFAULT, new Color(44, 233, 41, 255));

                    Element ele = ElementHelper.fromTextBillboard(new Vector(end.x, end.y, 0.7, 0), 1f, 1f, true, "E", 10.0f, Typeface.DEFAULT, new Color(255, 0, 0, 255));

                    testPathLayer.addElement("startpoint" + i, els);
                    testPathLayer.addElement("endpoint" + i, ele);
                }

                String strVal = String.format(("%d"), i);

                if (isMapRatation) {
                    if (StaticManager.folderName.equalsIgnoreCase("office"))
                        el = ElementHelper.fromText(0.6f * strVal.length(), 0.9f, strVal, 100, tff, new Color(80, 80, 80, 255));
                    else
                        el = ElementHelper.fromText(2 * strVal.length(), 2.2f, strVal, 100, tff, new Color(80, 80, 80, 255));

                    el.getTransform().clearTransform();
                    el.getTransform().setTranslation(((start.x + end.x) / 2), ((start.y + end.y) / 2), 2);
                    el.getTransform().calcMatrix();
                    el.getExtentHelper().updateExtent();
                    testPathLayer.addElement("lineNum" + strVal, el);
                }

                Vector[] v = new Vector[pts.getNumPoints()];

                for (int iPoint = 0; iPoint < pts.getNumPoints(); iPoint++) {
                    v[iPoint] = new Vector(pts.data[iPoint].x, pts.data[iPoint].y, 0, 1);
                }

                _pathManager.addLink(v);

            }
            //break;
        }
    }

    private double start_x;
    private double start_y;
    private double finish_x;
    private double finish_y;

    private double mFileStartPointX;
    private double mFileStartPointY;

    private void setLineReader(String jsonData) {
        WataLog.d("jsonData : " + jsonData);

        if (jsonData != null) {            // File is existed
            WataLog.d("JSON Data : " + jsonData);
            try {
                JSONObject jObject = new JSONObject(jsonData);
                WataLog.d("jObject= " + jObject);

                JSONArray arrayPaths = jObject.getJSONArray("LINE_LIST");
                WataLog.d("arrayPaths= " + arrayPaths);

                for (int i = 0; i < arrayPaths.length(); i++) {
                    JSONObject jsonPath = arrayPaths.getJSONObject(i);

                    start_x = jsonPath.getDouble("start_pointx");
                    start_y = jsonPath.getDouble("start_pointy");
                    finish_x = jsonPath.getDouble("end_pointx");
                    finish_y = jsonPath.getDouble("end_pointy");
                    WataLog.d("start_x=" + start_x + "// start_y=" + start_y + "/// finish_x=" + finish_x + "/// finish_y=" + finish_y);
                    if( i == 0) {
                        mFileStartPointX = start_x;
                        mFileStartPointY = start_y;
                    }
                    getRecordLine(start_x, start_y, finish_x, finish_y);
                }

                setMapLookPosition(mFileStartPointX, mFileStartPointY, 0);

            } catch (Exception e) {
                WataLog.d("Error in Reading: " + e.getLocalizedMessage());
            }
        }
    }

    private void setPoiReader(String jsonData) {
        WataLog.d("jsonData : " + jsonData);

        if (jsonData != null) {            // File is existed
            WataLog.d("JSON Data : " + jsonData);
            try {
                JSONObject jObject = new JSONObject(jsonData);
                WataLog.d("jObject= " + jObject);

                JSONArray arrayPaths = jObject.getJSONArray("POI_LIST");
                WataLog.d("arrayPaths= " + arrayPaths);

                for (int i = 0; i < arrayPaths.length(); i++) {
                    JSONObject jsonPath = arrayPaths.getJSONObject(i);

                    double poi_x = jsonPath.getDouble("pointx");
                    double poi_y = jsonPath.getDouble("pointy");
                    String poi_name = jsonPath.getString("poi_name");
                    WataLog.d("poi_x=" + poi_x + "// poi_y=" + poi_y + "/// poi_name=" + poi_name);

                    getRecordPOI(poi_x, poi_y, poi_name);
                }


            } catch (Exception e) {
                WataLog.d("Error in Reading: " + e.getLocalizedMessage());
            }
        }
    }

    public static boolean bScanning = false, bLogging = false;
    private List<ScanResult> wifiList;
    private Button startFP, startLog, showRes;
    //	private Button setFreq;
    private Thread scanThread = null;

    public boolean setBscannning(boolean bScanning) {
        return this.bScanning = bScanning;
    }

    @SuppressLint("NewApi")
    private void initMap() throws IOException, ClassNotFoundException {
        if (_UIHandler == null)
            _UIHandler = new GetheringUIHandler();

        if (_mapTouchListener == null)
            _mapTouchListener = new GatheringMapTouchListener(_UIHandler);

        _omapView = new WMapSurfaceView(this);
        _omapView.setOnTouchListener(_mapTouchListener);

        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.path_drawing_layout, null);

        gatheringInfo = (TextView) v.findViewById(R.id.gatheringInfo);
//        gatheringOrientationDegree = (TextView) v.findViewById(R.id.gatheringOrientationDegree);
//        gatheringOrientation = (ImageView) v.findViewById(R.id.gatheringOrientation);

        // Map View : Ethan
        _mapLayout = (RelativeLayout) v.findViewById(R.id.gather_map_view);
        _mapLayout.addView(_omapView);


        mNorth = new ImageButton(this);
        RelativeLayout.LayoutParams paramsnorth = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        paramsnorth.setMargins(0, 10, 10, 0);
        paramsnorth.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        paramsnorth.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        mNorth.setLayoutParams(paramsnorth);
        mNorth.setBackground(getResources().getDrawable(R.drawable.north_nor_click));
        mNorth.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                ViewUtil.FitTopView(_omapView.getO2mapInstance().getView(), _omapView.getO2mapInstance().getModel().getExtent());
                _omapView.getO2mapInstance().renderLock();
                refreshNorthIcon();
                _omapView.getO2mapInstance().renderUnlock();
                _omapView.requestRender();
            }
        });
        _mapLayout.addView(mNorth);

//        pathInverse = new Button(this);
//        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        params1.setMargins(10, 0, 0, 10);
//        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//        params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        pathInverse.setLayoutParams(params1);
//
//        pathInverse.setText("정방향");
////		pathInverse.setId(PATHINVERSE_BTN_ID);
//        pathInverse.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                _clickListener.reversePath();
//            }
//        });
//        pathInverse.setVisibility(View.INVISIBLE);
//        _mapLayout.addView(pathInverse);
        if (!forNis) {
            calBtn = new Button(this);
            RelativeLayout.LayoutParams params5 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params5.setMargins(0, 10, 10, 0);
            params5.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params5.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            calBtn.setLayoutParams(params5);

            calBtn.setText("cal적용");
            //		pathInverse.setId(PATHINVERSE_BTN_ID);
            calBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String[] arr = {"개활 로그함수", "비개활 로그함수", "개활 1차함수", "비개활 1차함수", "미적용", "회전"};
                    new AlertDialog.Builder(PathDrawingOldActivity.this)
                            .setTitle("cal type")
                            .setItems(arr, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String strLen = arr[which];
                                    WataLog.d("cal type = " + strLen + "/" + which);
                                    //						if(strLen!=null)
                                    {
                                        if (which < 5) {
                                            pointCount = -1;
                                            min = 0;
                                            max = 0;
                                            avg = 0;
                                            var = 0;
                                            gatherCnt = 0;
                                            sucRate = 0;
                                            mAf = null;
                                            mKf = null;
                                            durTime = -1;
                                            simulateWithCalibaration(which);
                                        } else
                                            rotateLog(197385.7690, 550467.8590, 36);
                                    }
                                }
                            }).show();
                }
            });
            _mapLayout.addView(calBtn);
        }
        String getbScanning = getIntent().getStringExtra("bScanning");
        if (getbScanning != null) {
            bScanning = false;
            startFP.performClick();
        }

        setContentView(v);
        WataLog.d("GID=" + StaticManager.gid);
    }

    @SuppressLint("NewApi")
    private void runLog() {
        if (bScanning) {
            bScanning = false;
            startFP.setText("측위");
            startFP.setBackground(getResources().getDrawable(R.drawable.btn_click));
        }

        if (!bLogging) {
            if (calBtn != null)
                calBtn.setVisibility(View.GONE);
            touchedNode = null;
            bLogging = true;
//			((TextView)v).setText("기록중");
            scanThread = new Thread(new Runnable() {
                public void run() {
                    while (bLogging) {

                        if (wifiScanner == null) {
                            wifiScanner = new com.geotwo.TBP.TBPWiFiScanner(PathDrawingOldActivity.this);
                            wifiScanner.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                            IntentFilter NETWORK_STATE_LISTENER = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                            NETWORK_STATE_LISTENER.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                            NETWORK_STATE_LISTENER.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
                            NETWORK_STATE_LISTENER.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
                            NETWORK_STATE_LISTENER.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

                            registerReceiver(wifiScanner, NETWORK_STATE_LISTENER);
                            wifiScanner.ipAddress = _dataCore.getTpsURL();
                            wifiScanner.port = _dataCore.getTpsPort();
                        }

                        if (touchedNode != null) {
                            wifiScanner.startScan();            // 기록 : by Ethan
                            while (!wifiScanner.isScanComplete) {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (wifiList != null)
                                wifiList.clear();
                            wifiList = wifiScanner.wifiManager.getScanResults();
                            wifiScanner.sendData(GEO2.LBSP.SClient.SvrPositionProvider.COORDTYPE.emMiddleTM);
                            System.gc();
                            wifiScanner.isScanComplete = false;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            scanThread.start();
        } else {
            if (calBtn != null)
                calBtn.setVisibility(View.VISIBLE);
//			if(wifiScanner != null)
//				unregisterReceiver ( wifiScanner );
            bLogging = false;
            if (mKf != null) {
                mKf.init(null, null, 1);
                mKf = null;
            }
            pointCount = -1;
            touchedNode = null;
            Toast.makeText(PathDrawingOldActivity.this, " 수집종료.", Toast.LENGTH_SHORT).show();
            _omapView.getO2mapInstance().renderLock();
            if (testPosLayer != null && testPosLayer.size() > 0)
                testPosLayer.removeAllElement();
            _omapView.getO2mapInstance().renderUnlock();
            _omapView.requestRender();
//			((TextView)v).setText("기록");
        }
    }

    private void viewStepLenSetting() {
        final String[] arr = {"40", "45", "50", "55", "60", "65", "70", "75", "80"};
        new AlertDialog.Builder(PathDrawingOldActivity.this)
                .setTitle("보폭설정")
                .setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strLen = arr[which];
                        int stepLen = 0;
                        WataLog.d("strLen=" + strLen);

                        if (strLen != null) {
                            stepLen = Integer.valueOf(strLen);
                            SharedPreferences pref = getSharedPreferences("stepPref", 0);
                            SharedPreferences.Editor prefEditor = pref.edit();
                            prefEditor.putInt("stepLength", stepLen);
                            prefEditor.commit();
                            double tempLen = pref.getInt("stepLength", stepLen) / 100.0;
                            pdrvariable.setStep_length(tempLen);

                            WataLog.d("stepLen=" + stepLen + ", tempLen=" + tempLen);
                            WataLog.d("pdrvariable.getStep_length==" + pdrvariable.getStep_length())
                            ;
                        }
                    }
                })
                .show();
    }

    @Override
    public void onResume() {
        WataLog.i("onResume");

        super.onResume();

        if (_wifiScanner != null) { // refresh
            _wifiScanner.setBleIgnoreList(BleIgnoreSettingActivity.getBleIgnoreList(this));
            _wifiScanner.setBleAddList(BleAddSettingActivity.getBleAddList(this));
        }

        WataLog.d("_wifiScanner=" + _wifiScanner);
        SpinnableImageView spinnableImageView = new SpinnableImageView(this);
        spinnableImageView.setItemListener(new SpinnableImageView.onDegress() {
            @Override
            public void getDegress(double degrees) {
                WataLog.d("degrees=" + degrees);
            }
        });

        setSensor();

//        try {
//            Thread.sleep(1000);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                setMapSetting();
//                o2map.getView().getCamera().setDistance(200.0);
//                setStartPointLine(); // 라인그리기
//                setUiControl(0);
//            }
//        });
//        Toast.makeText(this, "시작지점을 확인해주세요.", Toast.LENGTH_SHORT).show();

//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        if (!ROAD_MODE) {
            setMapSetting();
            setStartPointLine(); // 라인그리기
            setUiControl(0);
            setMapLookPosition(mLastPointX, mLastPointY + 10, 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    private void setMapInit() {
        WataLog.i(" Test 경로수집");
        myWayInfoData = new ArrayList<MyWayInfo>();
        mPoiInfoData = new ArrayList<PoiInfo>();

        setMapView();
        // kcy1000
        mLastPointX = 0.0;
        mLastPointY = 0.0;
        mPointCount = 0;
        mRecordPosition = 0;
        mOmapRatation = 0;
//        gathering_select.setText(strWays[0]);
//                                    setSensor(); // test용 나침반
        myWayLine = null;
        DataCore.iGatherMode = DataCore.GATHER_MODE_START_SELECT;

    }

    public void onDestroy() {
        super.onDestroy();

        if (wifiScanner != null && (bScanning || bLogging))
            unregisterReceiver(wifiScanner);
//		wifiScanner = null;
        if (scanThread != null) {
            bScanning = false;
            bLogging = false;
            if (scanThread.isAlive())
                scanThread.interrupt();
        }
        if (_wifiScanner != null) {
            _wifiScanner.stopStatListener();
            _wifiScanner.stopScan();
            // wifiScanner.setWifiOff();
            _wifiScanner = null;
        }

        StaticManager.setEmptyAll();

        basePointX = 0;
        basePointY = 0;
        gatheringRelease();

        if (beaconManager != null) {
            try {
                beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
            } catch (RemoteException e) {
                WataLog.d("Error while stopping ranging=" + e);
            }
            beaconManager.disconnect();
            beaconManager = null;
        }

        if (nearBLE != null) {
            nearBLE.clear();
            nearBLE = null;
        }

        if (beacons_L != null) {
            beacons_L.clear();
        }

        targetBLE = null;
        m2ndTarget = null;
        lastBLE = null;
        m2ndBLE = null;

    }

    private void wifiSet(boolean b) {
        WataLog.i("wifiSet = " + b);

        if (b) {
            if (_wifiScanner == null) {
                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                try {
                    int REQUEST_SCAN_ALWAYS_AVAILABLE = 100;
                    wifiManager.setWifiEnabled(false);
                    if (Build.VERSION.SDK_INT >= 18 && !wifiManager.isScanAlwaysAvailable()) {
                        ((Activity) PathDrawingOldActivity.this).startActivityForResult(
                                new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), REQUEST_SCAN_ALWAYS_AVAILABLE);

                    }

                } catch (Exception e) {
                    WataLog.e("exception=" + e.toString());
                }
                _list = new ArrayList<ScanResult>();
                //				_wifiScanner = new WifiScanner(GatheringActivity.this, wifiManager, _UIHandler, _list);
                _wifiScanner = new PathDWifiScanner(this, wifiManager, _UIHandler, _list, _bleManager);
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
    }


    private void sensorSet(boolean b)            // sensorthread start : by Ethan
    {
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
//					_sensorthread.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                _sensorthread = null;
            }

            if (_senact != null) {
                _senact = null;
            }
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
        isInverse = false;
        lineSelected = null;
        lineHeader = Double.NaN;
    }

    public void onBackPressed() {
//        super.onBackPressed();
        DataCore.isOnGathering = false;
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

    private ImageButton mNorth;

    private double mCurrAngle = 0;
    private double mPrevAngle = 0;
    private double mAddAngle = 0;
    private LinearLayout map_inner_frm_linear1;
//    private int mFloorsLever = 0;

    private void initView(boolean isFirst) {
        // kcy1000 - 레이아웃 셋팅
        map_inner_frm_linear1 = (LinearLayout) findViewById(R.id.map_inner_frm_linear1);
        map_inner_frm_linear1.setVisibility(View.GONE);
        // 기록 listview
        record_lisetview = (RelativeLayout) findViewById(R.id.record_lisetview);
        mPathDrawingListview = (ListView) findViewById(R.id.my_way_listview);
        list_ok_btn = (Button) findViewById(R.id.list_ok_btn);
        list_ok_btn.setOnClickListener(this);
        // poi listview
//        poi_listview_rlayout = (RelativeLayout) findViewById(R.id.poi_listview_rlayout);
        mPoiListview = (ListView) findViewById(R.id.poi_listview);
//        poi_list_ok_btn = (Button) findViewById(R.id.poi_list_ok_btn);
        poi_list_ok_btn.setOnClickListener(this);

        my_way_record_info_layout = (RelativeLayout) findViewById(R.id.my_way_record_info_layout);
        my_way_record_info_layout.setVisibility(View.GONE);
        reverse_record_btn = (Button) findViewById(R.id.reverse_record_btn); //역방향 기록하기
        record_list_btn = (Button) findViewById(R.id.record_list_btn); // 기록 내역
        poi_list_btn = (Button) findViewById(R.id.poi_list_btn); // poi list보기

        reverse_record_btn.setOnClickListener(this);
        record_list_btn.setOnClickListener(this);
        poi_list_btn.setOnClickListener(this);

        my_way_path_start = (Button) findViewById(R.id.my_way_path_start);
        my_way_path_end = (Button) findViewById(R.id.my_way_path_end);
        record_cancel = (Button) findViewById(R.id.record_cancel);

        my_way_path_start.setOnClickListener(this);
        my_way_path_end.setOnClickListener(this);
        record_cancel.setOnClickListener(this);

//        Button file_save_btn = (Button) findViewById(R.id.file_save_btn);
//        file_save_btn.setOnClickListener(this);

        //poi layout
        poi_point_layout = (RelativeLayout) findViewById(R.id.poi_point_layout);
        poi_point_layout.setVisibility(View.GONE);
        poi_edit = (EditText) findViewById(R.id.poi_edit);

        LinearLayout poi_cancel = (LinearLayout) findViewById(R.id.poi_cancel);
        poi_cancel.setOnClickListener(this);
        Button poi_cancel_btn = (Button) findViewById(R.id.poi_cancel_btn);
        poi_cancel_btn.setOnClickListener(this);
        Button poi_add_btn = (Button) findViewById(R.id.poi_add_btn);
        poi_add_btn.setOnClickListener(this);

        // poi수정
        poi_edit_box_layout = (RelativeLayout) findViewById(R.id.poi_edit_box_layout);
        poi_edit_box_layout.setVisibility(View.GONE);
        poi_edit_text = (EditText) findViewById(R.id.poi_edit_text);
        Button poi_save_btn = (Button) findViewById(R.id.poi_save_btn);
        poi_save_btn.setOnClickListener(this);

        //스텝
        step_btn = (Button) findViewById(R.id.step_btn);
        step_btn.setOnClickListener(this);

        // 지점확인
        save_point_check_btn = (Button) findViewById(R.id.save_point_check_btn);
        save_point_check_btn.setOnClickListener(this);

//        Button point_check_btn = (Button) findViewById(R.id.point_check_btn);
//        point_check_btn.setOnClickListener(this);

        String[] stringMin = new String[100];
        for (int i = 0; i < stringMin.length; i++) {
            stringMin[i] = Integer.toString(i - 9);
        }
//        //층수확인
//        NumberPicker floors_lever_picker = (NumberPicker) findViewById(R.id.floors_lever_picker);
//        floors_lever_picker.setMinValue(0);
//        floors_lever_picker.setMaxValue(99);
//        floors_lever_picker.setDisplayedValues(stringMin);
//        floors_lever_picker.setWrapSelectorWheel(false);
//
//        floors_lever_picker.setValue(10);
//        floors_lever_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//            @Override
//            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                WataLog.d("newVal=" + newVal);
//                mFloorsLever = newVal;
//            }
//        });

        //설정
        path_drawing_setting = (Button) findViewById(R.id.path_drawing_setting);
        path_drawing_setting.setOnClickListener(this);

//        progress_layout = (RelativeLayout) findViewById(R.id.progress_layout);
//        progress = (ProgressBar) findViewById(R.id.progress);

        // 미세 각도 조절
        angle_layout = (LinearLayout) findViewById(R.id.angle_layout);
        precision_btn = (Button) findViewById(R.id.precision_btn);
        precision_btn.setOnClickListener(this);

        angle_control_layout = (LinearLayout) findViewById(R.id.angle_control_layout);
        Button angle_m_btn = (Button) findViewById(R.id.angle_m_btn);
        Button angle_p_btn = (Button) findViewById(R.id.angle_p_btn);
        angle_m_btn.setOnClickListener(this);
        angle_p_btn.setOnClickListener(this);

        if (_SCAN_RESULT_THIS_EPOCH == null)
            _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();

        if (_SCAN_RESULT_TOTAL == null)
            _SCAN_RESULT_TOTAL = _dataCore.getSCAN_RESULT_TOTAL();

        _prevWalkedDistance = 0;

        WataLog.d("isFirst=" + isFirst);
        if (isFirst == false) {
            wifiSet(true);
        }

//		if (_gatheringEndToast == null)
//			_gatheringEndToast = Toast.makeText(GatheringActivity.this, "수집이 완료되었습니다.", Toast.LENGTH_SHORT);
    }


    //	private Polyline[] BackupLine;
    private int PreIndex = -1;
    static public double lineHeader = Double.NaN;
    private PathManager _pathManager_for_col;

    private void addRoadLine(Points pts) {
        if (_pathManager_for_col == null)
            _pathManager_for_col = new PathManager();

        if (pts != null) {
            Vector[] v = new Vector[pts.getNumPoints()];

            for (int iPoint = 0; iPoint < pts.getNumPoints(); iPoint++) {
                v[iPoint] = new Vector(pts.data[iPoint].x, pts.data[iPoint].y, pts.data[iPoint].z, 1);
            }
            _pathManager_for_col.addLink(v);
        }
    }

    private void loadGatheringListActivity() {
//		_settingLayout.setVisibility(View.INVISIBLE);
        Intent intent = new Intent(PathDrawingOldActivity.this, GatherListActivity.class);
        startActivity(intent);
    }

    private double distRemain = -1;
    private SandboxModel currentDistLayer = null;


    private void startStepCountAnimation() {
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        BounceInterpolator bounceInterpolator = new BounceInterpolator(0.2, 40);
        bounceAnimation.setInterpolator(bounceInterpolator);
        gatheringInfo.startAnimation(bounceAnimation);
    }

    private ArrayList<Vector> multi = null;
    private ArrayList<Points> fullpath;
    private boolean bEndPoint = false;

    private void updateTouch(double x, double y) {
        WataLog.d("updateTouch = " + x + " // " + y);
        WataLog.d("DataCore.iGatherMode=" + DataCore.iGatherMode);

        if (DataCore.iGatherMode == DataCore.GATHER_MODE_NONE || DataCore.iGatherMode == DataCore.GATHER_MODE_GATHERING)
            return;

        if (DataCore.iGatherMode == DataCore.GATHER_MODE_START_SELECT) {
            Line line = ViewUtil.computeRayFromScreenPoint(_omapView.getO2mapInstance().getView(), (int) x, (int) y);
            Plane pl = new Plane(0.0, 0.0, 1.0, 0);

            Vector v = pl.intersect(line);
            _startPoint = new Vector(v.x, v.y, v.z);
            WataLog.d("_startPoint =" + _startPoint);

            WataLog.d("iGatherMode =" + DataCore.iGatherMode);
            WataLog.i("시작점 지정하기");
// 확인필요 kcy1000
//            Toast.makeText(this, "시작점을 터치해주세요.", Toast.LENGTH_SHORT).show();
            onMyWayStartEndPoint(DataCore.iGatherMode, v);
        }
        _omapView.getO2mapInstance().requestRedraw();
    }

    // display Finish Path : by Ethan
    void displayPathFinish() {
        WataLog.d("wReader= " + wReader);
        if (wReader != null) {
            int readerSize = wReader.getCount();

            for (int a = 0; a < readerSize; a++) {
                String value = String.format(("%d"), wReader.getPathId(a));
                if (value != null) {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pathGathering/"
                            + StaticManager.folderName + "/" + StaticManager.floorName + "/" + value + "/";

                    File areaDir = new File(path);
                    int findCount = 0;
                    if (areaDir.exists()) {
                        File[] list = areaDir.listFiles();
                        if (list == null) return;

                        for (File file : list) {
                            if (file.isDirectory()) continue;

						/*
						if(file.getName().contains("android") {
						 // 파일명 또는 폴더명에 android가 들어가는지
						}
                        */
                            if (file.getName().endsWith(".temp")) {
                                findCount++;
                            }
                        }
                        WataLog.d("findCount=" + findCount);
                        // display
                        if (findCount == 2) {
                            viewEachFinishPath(a);
                        }

                    }
                }
            }
        }
    }

    SandboxModel eachPathFinishLayer = null;

    void viewEachFinishPath(int pos) {
        WataLog.i("viewEachFinishPath");
        endX = 0.0;
        endY = 0.0;

        O2Map o2map = (_omapView.getO2mapInstance());

        boolean bAdd = false;
        if (eachPathFinishLayer == null) {
            eachPathFinishLayer = new SandboxModel("eachPathFinishLayer");
            bAdd = true;
        }

        String key = "finishPath" + pos;
        if (eachPathFinishLayer.getElement(key) != null) {
            return;
        }

        if (bAdd) {
            ((CompositeModel) o2map.getModel()).addModel(eachPathFinishLayer);
        }
        _omapView.requestRender();
    }

    // -----------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------
    // inner class

    // -----------------------------------------------------------------------------------------------------------
    // ButtonClickListener class start
    public static Polyline lineSelected = null;
    private Button endPoint = null;
    private PathDrawingSettingDialog mPsettingDialog;

    protected void onMyWayStartEndPoint(int which, Vector start) {
        WataLog.i("onMyWayStartEndPoint");
        WataLog.d("which=" + which);

        O2Map o2map = (this._omapView.getO2mapInstance());
        o2map.renderLock();

        boolean bAdd = false;
        if (twoPointerLayer != null) {
            twoPointerLayer.removeAllElement();
            twoPointerLayer = null;
        }
        if (twoPointerLayer == null) {
            twoPointerLayer = new SandboxModel("pointer");
            bAdd = true;
        }

        if (start != null) {
            float w = 4f;
            float h = 4f;

            if (StaticManager.folderName.equalsIgnoreCase("office")) {
                w = 1f;
                h = 1f;
            }
            WataLog.i("시작 & 종료 위치 표시 좌표" + start.x + "///" + start.y);
            mLastPointX = start.x;
            mLastPointY = start.y;

            WataLog.d("mLastPointX=" + mLastPointX);
            WataLog.d("mLastPointY=" + mLastPointY);
            WataLog.d("which=" + which);

//            if (which == DataCore.GATHER_MODE_START_SELECT) {
            Element el = ElementHelper.fromTextBillboard(new Vector(start.x, start.y, 15.0, 0), w, h, true, "S", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
            if (el != null) {
                twoPointerLayer.addElement("pointerStart", el);
            }
//            } else {
//                Element el = ElementHelper.fromTextBillboard(new Vector(start.x, start.y, 5.0, 0), w, h, true, "E", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
//                if (el != null) {
//                    twoPointerLayer.addElement("pointerStart", el);
//                }
//            }
        }
        if (bAdd) {
            ((CompositeModel) o2map.getModel()).addModel(twoPointerLayer);
        }
        o2map.renderUnlock();

//        Toast.makeText(this, "시작점을 터치해주세요.", Toast.LENGTH_SHORT).show();
    }

    private int mOmapRatation = 0;
    private String SAVE_FILE_NAME = "";
    private boolean mRecordDirection = true;
    private ArrayList<StepInfo> STEP_LIST = new ArrayList<StepInfo>();
    private int mSaveDegress = 0;

    @Override
    public void onClick(View v) {
//        testNumlayer = null;
        switch (v.getId()) {
            case R.id.record_cancel: // 기록 취소
                wifiSet(false);
                my_way_path_start.setEnabled(true);
                save_point_check_btn.setEnabled(false);
                mStartRecord = false;

                deleteLine(startPointModel, "startPointModel");
                deleteLine(endPointModel, "endPointModel");

                onPathSetting();

                mPointCount--;

                WataLog.d("mRecordStartPointX=" + mRecordStartPointX + "//// mRecordStartPointY=" + mRecordStartPointY);
                WataLog.d("mLastPointX=" + mLastPointX + "//// mLastPointY=" + mLastPointY);

                mLastPointX = mRecordStartPointX;
                mLastPointY = mRecordStartPointY;

                Vector cancelV = new Vector(mLastPointX, mLastPointY, 0, 0);
                _omapView.getO2mapInstance().getView().getCamera().setLookAtPosition(cancelV);

//                onRecordPointMoving();

                setStartPointLine(); // 라인그리기
                CAMERA_MOVING = true;
                setUiControl(2);
                break;
            case R.id.step_btn: // 한걸음
                WataLog.i("step_btn");
                newPdralgorithm.getsensordata(getApplicationContext());
                _UIHandler.sendEmptyMessage(DataCore.PDR_STAT_CHANGED);
                _omapView.getO2mapInstance().requestRedraw();

                break;
//            case R.id.file_save_btn: // 파일이름 설정, 지도셋팅
////                save_file_layout.setVisibility(View.GONE);
//                WataLog.d("SAVE_FILE_NAME=" + SAVE_FILE_NAME);
//
//                setMapSetting();
//                setStartPointLine(); // 라인그리기
//                setUiControl(0);
//                setMapLookPosition(mLastPointX, mLastPointY, 0);
//
//                //                o2map.getView().getCamera().setDistance(200.0);
//
////                compass_img.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "시작지점을 확인해주세요.", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.file_name_cancel:
//                break;
//            case R.id.save_point_check_btn: // 지점체크
//                WataLog.i("point체크");
//                setPointCheck();
//                break;
//            case R.id.point_check_btn:
//                WataLog.i("point체크");
//                setPointCheck();
//                poi_point_layout.setVisibility(View.GONE);
//                break;
//            case R.id.point_check_list: //point check list
//                WataLog.i("point체크리스트");
//                break;
            case R.id.poi_list_btn: //
                WataLog.i("POI list");

                if (mPoiListAdapter.getCount() > 0) {
                    poi_listview_rlayout.setVisibility(View.VISIBLE);
                } else {
                    Toast toast = Toast.makeText(PathDrawingOldActivity.this, "저장된 기록이 없습니다.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                break;
//            case R.id.poi_list_ok_btn: // poi확인
//                poi_listview_rlayout.setVisibility(View.GONE);
//                onKeyboardHide(poi_edit);
//                break;
            case R.id.poi_cancel_btn:  // 등록취소
                poi_point_layout.setVisibility(View.GONE);
//                String poiLName = poi_edit.getText().toString();
//                WataLog.i("poiLName=" + poiLName);
//                if (!"".equals(poiLName) && poiLName != null) {
//                    setPoiPointMark(mNowPointX, mNowPointY, poiLName, false);
//                    poi_point_layout.setVisibility(View.GONE);
//                    onKeyboardHide(poi_edit);
//                } else {
//                    Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
//                }
                break;
            case R.id.poi_add_btn:  // poi 등록
                String poiRName = poi_edit.getText().toString();
                if (!"".equals(poiRName) && poiRName != null) {
                    setPoiPointMark(mNowPointX, mNowPointY, poiRName, true);
                    poi_point_layout.setVisibility(View.GONE);
                    onKeyboardHide(poi_edit);

                    setPOILog(mNowPointX, mNowPointY, poiRName);

                } else {
                    Toast.makeText(this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.poi_cancel: // 문구삭제
                poi_edit.setText("");
                break;
            case R.id.poi_save_btn: // poi수정
//                String poiName = poi_edit_text.getText().toString();
//                WataLog.d("POI_POSITION=" + POI_POSITION);
//                mPoiInfoData.set(POI_POSITION, new PoiInfo(mPoiINfoItem.Number, mPoiINfoItem.PoiPositionX, mPoiINfoItem.PoiPositionY, poiName));
//                mPoiListAdapter.notifyDataSetChanged();
//                POI_POSITION++;
//
//                PoiMessageModel.removeElement("PoiMessageModel" + String.valueOf(mPoiINfoItem.Number));
//
//                Element poiMessage = ElementHelper.fromTextBillboard(new Vector(mPoiINfoItem.PoiPositionX, mPoiINfoItem.PoiPositionY, 5.0, 0), 2f, 2f, true, poiName, 60.0f, Typeface.DEFAULT_BOLD, Color.BLACK);
//                if (PoiMessageModel == null) {
//                    PoiMessageModel = new SandboxModel("PoiMessageModel");
//                }
//                if (poiMessage != null) {
//                    PoiMessageModel.addElement("PoiMessageModel" + mPoiINfoItem.Number, poiMessage);
//                }
//                ((CompositeModel) o2map.getModel()).addModel(PoiMessageModel);
//                _omapView.getO2mapInstance().requestRedraw();
//
//                poi_edit_box_layout.setVisibility(View.GONE);
                break;


            case R.id.my_way_path_start: // 기록시작 - kcy1000 - 3
                WataLog.i(" 경로 기록 시작 ");
                WataLog.d("isInverse=" + isInverse);
                WataLog.d("mRotation=" + mRotation);
                WataLog.d("mDefaultRotation=" + mDefaultRotation);
                WataLog.d("isGyroSensor=" + isGyroSensor);
                onRecordStart();

                break;
            case R.id.my_way_path_end: // 기록종료 - kcy1000 - 4
                WataLog.i(" 경로 기록 종료 ");
                if (myWayListAdapter.getCount() > 0) {
                    reverse_record_btn.setEnabled(true);
                }

                if (mSaveDegress == 0) { // 최초 한번만 받기위해
                    mSaveDegress = mDegressSum;
                }

                WataLog.d("mSaveDegress=" + mSaveDegress);
                onRecordStop();

                break;
//            case R.id.my_way_reset: // 재설정
//                setUiControl(1);
//                break;
            case R.id.list_ok_btn: // list ok
                record_lisetview.setVisibility(View.GONE);
                break;
            case R.id.reverse_record_btn: // 역방향 기록하기
                try {
                    isInverse = true;
                    CAMERA_MOVING = false;

                    WataLog.i("check! 역방향 기록하기");
                    int size = myWayInfoData.size();
                    MyWayInfo item = myWayInfoData.get(size - 1);
                    mRecordIndexNum = size - 1;
                    onToast(Gravity.CENTER, getString(R.string.revers_record_start_message));

                    // 정향방향 라인지우기
                    myRecordFLine.removeElement("myRecordFLine" + mRecordPosition);
                    myWayLine.removeAllElement();

                    deleteLine(startPointModel, "startPointModel");
                    deleteLine(endPointModel, "endPointModel");
//                    setReversePointSetting(item.EndPointX, item.EndPointY, item.StartPointX, item.StartPointY);
                    setStartEndPointName();
                    mCameraAngle = item.Angle + 180;
                    WataLog.d("mCameraAngle=" + mCameraAngle);

                    onRecordStart();
                } catch (Exception e) {
                    WataLog.e("Exception=" + e.toString());
                    onToast(Gravity.CENTER, getString(R.string.error_message_1));
                }


                break;
            case R.id.record_list_btn: // 기록내역
                WataLog.d("myWayListAdapter =" + myWayListAdapter.getCount());

                if (myWayListAdapter.getCount() > 0) {
                    record_lisetview.setVisibility(View.VISIBLE);
                } else {
                    Toast toast = Toast.makeText(PathDrawingOldActivity.this, "저장된 기록이 없습니다.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                break;
            case R.id.precision_btn: // 각도조절
                precision_btn.setVisibility(View.GONE);
                angle_control_layout.setVisibility(View.VISIBLE);
                CAMERA_MOVING = false;

                break;
            case R.id.angle_m_btn: // "-"
                mCameraAngle -= 5;
                setMapRotation(mCameraAngle);
                //시작점 저장하기
                mRecordStartPointX = mLastPointX;
                mRecordStartPointY = mLastPointY;
                lineSelected = (Polyline) getDrawingPoint(mLastPointX, mLastPointY);
//                onRotation(mCameraAngle);

                mRotation = Math.abs(mCameraAngle);
                RotateAnimation ra = new RotateAnimation((int) mDefaultRotation, (int) mCameraAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setDuration(100);
                ra.setFillAfter(true);
                mDefaultRotation = mCameraAngle;

                setStartPointLine();
//                mRotation = mCameraAngle;
                WataLog.d("mCameraAngle=" + mCameraAngle);
                break;
            case R.id.angle_p_btn: // "+"
                mCameraAngle += 5;
                setMapRotation(mCameraAngle);
                //시작점 저장하기
                mRecordStartPointX = mLastPointX;
                mRecordStartPointY = mLastPointY;
                lineSelected = (Polyline) getDrawingPoint(mLastPointX, mLastPointY);
//                onRotation(mCameraAngle);

                mRotation = Math.abs(mCameraAngle);
                RotateAnimation rota = new RotateAnimation((int) mDefaultRotation, (int) mCameraAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rota.setDuration(100);
                rota.setFillAfter(true);
                mDefaultRotation = mCameraAngle;

                setStartPointLine();
//                mRotation = mCameraAngle;
                WataLog.d("mCameraAngle=" + mCameraAngle);

                break;
            case R.id.path_drawing_setting: // 설정
//                viewStepLenSetting();

                mPsettingDialog = new PathDrawingSettingDialog(PathDrawingOldActivity.this, new OnClickListener() {
                    public void onClick(View v) {
                        WataLog.i("check 0");
                        com.geotwo.LAB_TEST.Gathering.dialog.SettingDialog sensitive = new com.geotwo.LAB_TEST.Gathering.dialog.SettingDialog();
                        AlertDialog senseDialog = sensitive.makeSensitiveSettingDilog(PathDrawingOldActivity.this);
                        senseDialog.show();
                    }
//                }, new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        WataLog.i("check 1");
//                        viewStepLenSetting();
//                    }
                });

                mPsettingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        WataLog.i("onDismiss");

                        SharedPreferences pref = getSharedPreferences(Constance.KEY, 0);
                        isMapRatation = pref.getBoolean(Constance.SETTING_MAP_ROTATION, true);
                        isGyroSensor = pref.getBoolean(Constance.SETTING_G_SENSOR_ROTATION, true);

//                        if(isGyroSensor) {
//                            step_btn.setVisibility(View.GONE);
//                        } else {
//                            step_btn.setVisibility(View.VISIBLE);
//                        }

                        if (isMapRatation) {
                            angle_layout.setVisibility(View.VISIBLE);
                        } else {
                            angle_layout.setVisibility(View.GONE);
                        }


                        WataLog.d("isMapRatation= " + isMapRatation);
//                        boolean currentLinkNumberShowing = SettingDialog.isLinkNumberShowing(PathDrawingActivity.this);
//                        if (currentLinkNumberShowing != isMapRatation) {
//                            isMapRatation = PathDrawingSettingDialog.isMapRatation(PathDrawingActivity.this);
//                            finish();
//                            startActivity(new Intent(PathDrawingActivity.this, PathDrawingActivity.class));
//                        }
                    }
                });
                mPsettingDialog.show();
                break;

        }
    }

    // 최초시작 라인생성 - kcy1000 - 1
    private void setMapSetting() {
        WataLog.i("setMapSetting");
//        SAVE_FILE_NAME = file_save_edit.getText().toString();
//        save_file_layout.setVisibility(View.GONE);
//        onKeyboardHide(file_save_edit);
        startSettingModel = new SandboxModel("startSettingModel");
        int heightPoint = (mHeightPixels / 5) * 3;
        int widthPixel = mWidthPixel / 2;
        WataLog.d("heightPoint=" + heightPoint); // 542
        WataLog.d("widthPixel=" + widthPixel); // 720
//        widthPixel = 100000;
//        heightPoint = 140000;

        Line line = ViewUtil.computeRayFromScreenPoint(_omapView.getO2mapInstance().getView(), widthPixel, heightPoint);
        Plane pl = new Plane(0.0, 0.0, 1.0, 0);
        Vector startVector = pl.intersect(line);
        _startPoint = new Vector(startVector.x, startVector.y, startVector.z);

//        mLastPointX = _startPoint.x;
//        mLastPointY = _startPoint.y;
//        mLastPointX = 64.92536194639865;
//        mLastPointY = -413.2596341935191;
        WataLog.d("mLastPointX=" + mLastPointX + "////mLastPointY=" + mLastPointY);
        updateTouch(mLastPointX, mLastPointY);
        WataLog.d("mLastPointX=" + mLastPointX + "////mLastPointY=" + mLastPointY);
        mRecordStartPointX = mLastPointX;
        mRecordStartPointY = mLastPointY;

        CAMERA_MOVING = true;
        WataLog.d("mLastPointX=" + mLastPointX + "////mLastPointY=" + mLastPointY);
        WataLog.d("_startPoint=" + _startPoint + "////_startPoint=" + _startPoint);

        Element el = ElementHelper.fromTextBillboard(new Vector(_startPoint.x, _startPoint.y, 5.0, 0), 1f, 1f, true, "S", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
        if (el != null) {
            startSettingModel.addElement("startSettingModel", el);
        }

        ((CompositeModel) _omapView.getO2mapInstance().getModel()).addModel(startSettingModel);

//        WataLog.d("mLastPointX=" + mLastPointX);
//        WataLog.d("mLastPointY=" + mLastPointY);
        lineSelected = (Polyline) getStartPointLine(_startPoint.x, _startPoint.y);
    }

    private void onRecordStart() {
        progress_layout.setVisibility(View.VISIBLE);

        setUiControl(1);

        if (startSettingModel != null) {
            startSettingModel.removeAllElement();
        }

        mRecordDirection = true;

        o2map.getView().getCamera().setDistance(60.0); //지도확대
        if (isInverse) {  // 역방향

            Quaternion q = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, Angle.fromDegrees(mCameraAngle)); // 지도회전
            _omapView.getO2mapInstance().getView().getCamera().setRotation(q);
        } else {  // 정방향

            WataLog.d("CAMERA_MOVING=" + CAMERA_MOVING);
            if (CAMERA_MOVING) {
                mOmapRatation = mCameraAngle;
            } else {
//                mOmapRatation = (int) mRotation - 90 + mOmapRatation;
//                mOmapRatation = (int) mRotation + mOmapRatation;
                mOmapRatation = mCameraAngle; // kcy1000 - 계속 확인필요함. 자이로이용하여 미세각도조절시 조절한 각도와 실제이동각도 이동후 각도 확인 필요
            }

            WataLog.d("mOmapRatation=" + mOmapRatation);
            WataLog.d("mCameraAngle=" + mCameraAngle);
            WataLog.d("mRotation=" + mRotation);

//                Toast.makeText(this, "진행방향 -- " + mOmapRatation, Toast.LENGTH_SHORT).show();
            Quaternion q = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, Angle.fromDegrees(mOmapRatation)); // 지도회전
            _omapView.getO2mapInstance().getView().getCamera().setRotation(q);

            mPointCount++;
        }
        WataLog.d("mPointCount=" + mPointCount);
        mLastPathPoint = lineSelected;
        if (lineSelected != null) {
            startMyWayGathering(lineSelected);
        }

        CAMERA_MOVING = false;
    }

    private void onRecordStop() {
        wifiSet(false);
        progress_layout.setVisibility(View.GONE);
        setUiControl(2);

        deleteLine(startPointModel, "startPointModel");
        deleteLine(endPointModel, "endPointModel");

        WataLog.d("mRecordStartPointX=" + mRecordStartPointX + "//// mRecordStartPointY=" + mRecordStartPointY);
        WataLog.d("mLastPointX=" + mLastPointX + "//// mLastPointY=" + mLastPointY);

        // 기록할 경로번호
        if (!isInverse) {
            setPointCount(mRecordStartPointX, mRecordStartPointY, mLastPointX, mLastPointY);
        }
        setRecordLine(); // 기록라인생성
        setRecordGathering(true);
//        setStartPointLine(); // 라인그리기
        CAMERA_MOVING = true;
    }


    // POI 포인터 체크
    private void setPointCheck() {
        try {
            if (mCheckPoint == null) {
                mCheckPoint = new SandboxModel("mCheckPoint");
            }

            WataLog.d("mNowPointX=" + mNowPointX);
            WataLog.d("mNowPointY=" + mNowPointY);
            WataLog.d("mPoiCount=" + mPoiCount);
            mPoiCount++;
//            mPoiInfoData.add(new PoiInfo(mPoiCount, mNowPointX, mNowPointY, getString(R.string.point)));
            mPoiListAdapter.setItems(mPoiInfoData);
            mPoiListview.setAdapter(mPoiListAdapter);

            Vector pointVector = new Vector(mNowPointX, mNowPointY, 0, 0);
            Element el = ElementHelper.quadPyramidFromPoint(pointVector.x, pointVector.y, 0.1, 1.0, 1, 1.1f, Color.BLUE, Color.BLUE, Color.BLUE, false);
            WataLog.d("el=" + el);
            mCheckPoint.addElement("mCheckPoint" + mPoiCount, el);
            ((CompositeModel) _omapView.getO2mapInstance().getModel()).addModel(mCheckPoint);
            _omapView.getO2mapInstance().requestRedraw();

        } catch (Exception e) {
            WataLog.e("Exception=" + e.toString());
        }
    }


//    private void setEndPointMark() {
//        WataLog.i("setEndPointMark");
//        Element el = ElementHelper.quadPyramidFromPoint(mLastPointX, mLastPointY, 0.1, 1.5, 1.5, 1.1f, Color.RED, Color.RED, Color.RED, false);
//        if (el != null) {
//            recordEndPointModel = new SandboxModel("recordEndPointModel");
//            recordEndPointModel.addElement("recordEndPointModel" + mPointCount, el);
//        }
//        ((CompositeModel) o2map.getModel()).addModel(recordEndPointModel);
//    }

    //    private ArrayList<PoiInfo> mPoiArry = new ArrayList<>();
    private int mPoiCount = 0;

    // poi등록
    private void setPoiPointMark(double x, double y, String poiText, boolean poiRight) {
        WataLog.i("setPoiPointMark");
        try {
            WataLog.i("x==" + x);
            WataLog.i("y==" + y);
            mPoiCount++;
//            int count = mPoiArry.size();
//            mPoiArry.add(new PoiInfo(mRecordPosition, x, y, poiText));

//            Polyline polyPoiPoint = (Polyline) getPoiPoint(x, y, poiRight);
//            Element poiMessage = ElementHelper.fromTextBillboard(new Vector(polyPoiPoint.getPart(0).getEndPoint().x, polyPoiPoint.getPart(0).getEndPoint().y, 5.0, 0), 2f, 2f, true, poiText, 60.0f, Typeface.DEFAULT_BOLD, Color.BLACK);
            Element poiMessage = ElementHelper.fromTextBillboard(new Vector(x, y, 5.0, 0), 2f, 2f, true, poiText, 60.0f, Typeface.DEFAULT_BOLD, Color.BLACK);
            if (PoiMessageModel == null) {
                PoiMessageModel = new SandboxModel("PoiMessageModel");
            }
            if (poiMessage != null) {
                WataLog.i("mPoiCount==" + mPoiCount);
                PoiMessageModel.addElement("PoiMessageModel" + mPoiCount, poiMessage);
            }
            ((CompositeModel) o2map.getModel()).addModel(PoiMessageModel);

//            Element poiPoint = ElementHelper.quadPyramidFromPoint( x , y, 0.1, 1.5, 1.5, 1.1f, Color.BLUE, Color.BLUE, Color.BLUE, false);
//            if (poiPoint != null) {
//                PoiPointModel = new SandboxModel("PoiPointModel");
//                PoiPointModel.addElement("PoiPointModel"  + count, poiPoint);
//            }
//            ((CompositeModel) o2map.getModel()).addModel(PoiPointModel);
            _omapView.getO2mapInstance().requestRedraw();

            WataLog.d("poiText=" + poiText);

//            mPoiInfoData.add(new PoiInfo(mPoiCount, x, y, poiText));
            mPoiListAdapter.setItems(mPoiInfoData);
            mPoiListview.setAdapter(mPoiListAdapter);

            // poi내용저장
        } catch (Exception e) {
            WataLog.e("Exception=" + e.toString());
        }
    }

    private double mDefaultRotation = 90;
    //    private ImageView directionImg;
    private double mRotation = 90; // 초기 각도값

    private void onRotation(double i) {
//        Quaternion quaternion = _omapView.getO2mapInstance().getView().getCamera().getRotation();
//        int rotation = (int) quaternion.getZ(); // 지도각도
//        WataLog.d("rotation=" + rotation);
//        WataLog.d("i=" + i);
//        i = i + mOmapRatation;
//        WataLog.d("mRotation=" + mRotation);
//        WataLog.d("mOmapRatation=" + mOmapRatation);
        mRotation = Math.abs(i - 90);
//        mRotation = Math.abs(i - 90) + mOmapRatation;

//        WataLog.d("mRotation=" + mRotation);

        RotateAnimation ra = new RotateAnimation((int) mDefaultRotation, (int) i, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(100);
        ra.setFillAfter(true);
//        directionImg.startAnimation(ra);

        mDefaultRotation = i;

        // 지도 회전하기(각도)
//        Quaternion q = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, Angle.fromDegrees(45));
//        _omapView.getO2mapInstance().getView().getCamera().setRotation(q);

    }

    // 이동할 경로를 미리보여준다.
    private Geometry getStartPointLine(Double startPointX, Double startPointY) {
        Polyline line = new Polyline();
        line.makeParts(1);

        Points pts = new Points();
        pts.makePoints(2);

        Double lastPointX = startPointX, lastPointY = startPointY;
        WataLog.d("mRotation=" + mRotation);
        double tempRatation = mRotation + mOmapRatation;
        WataLog.d("tempRatation=" + tempRatation);

        tempRatation = 2 * 3.14 * (tempRatation / 360);
        WataLog.d("tempRatation=" + tempRatation);
        WataLog.d("Math.cos(tempRatation) * 100=" + Math.cos(tempRatation) * 100);
        WataLog.d("Math.sin(tempRatation) * 100=" + Math.sin(tempRatation) * 100);

        lastPointX += Math.cos(tempRatation) * 200;
        lastPointY += Math.sin(tempRatation) * 200;

        WataLog.d("startPoint= " + startPointX + "//" + startPointY);
        WataLog.d("lastPoint= " + lastPointX + "//" + lastPointY);
        pts.setPoint(0, startPointX, startPointY, 0.0f);
        pts.setPoint(1, lastPointX, lastPointY, 0.0f);

        line.setParts(0, pts);

        return line;
    }


    // 이동할 경로를 미리보여준다.
    private Geometry getMyWayLine(Double startPointX, Double startPointY) {
        Polyline line = new Polyline();
        line.makeParts(1);

        Points pts = new Points();
        pts.makePoints(2);

        Double lastPointX = startPointX, lastPointY = startPointY;
        WataLog.d("mRotation=" + mRotation);
        double tempRatation = mRotation + mOmapRatation;
        WataLog.d("tempRatation=" + tempRatation);

        tempRatation = 2 * 3.14 * (tempRatation / 360);
        WataLog.d("tempRatation=" + tempRatation);
        WataLog.d("Math.cos(tempRatation) * 100=" + Math.cos(tempRatation) * 100);
        WataLog.d("Math.sin(tempRatation) * 100=" + Math.sin(tempRatation) * 100);

        lastPointX += Math.cos(tempRatation) * 200;
        lastPointY += Math.sin(tempRatation) * 200;

        updateTouch(lastPointX, lastPointY);

        WataLog.d("startPoint= " + startPointX + "//" + startPointY);
        WataLog.d("lastPoint= " + lastPointX + "//" + lastPointY);
        pts.setPoint(0, startPointX, startPointY, 0.0f);
        pts.setPoint(1, lastPointX, lastPointY, 0.0f);

        line.setParts(0, pts);

        return line;
    }

    // poi 표시지점위치
    private Geometry getPoiPoint(Double startPointX, Double startPointY, boolean PoiRigth) {
        Polyline line = new Polyline();
        line.makeParts(1);

        Points pts = new Points();
        pts.makePoints(2);

        Double lastPointX = startPointX, lastPointY = startPointY;
        WataLog.d("PoiRigth=" + PoiRigth);

        double poiRatation = 0;
        if (!PoiRigth) {
            poiRatation = 180; // 왼쪽.
        }
        WataLog.d("mOmapRatation=" + mOmapRatation);
        WataLog.d("mRotation=" + mRotation);
        if (mOmapRatation == 0) {
//            if (mRotation == 90) {
//                if(!PoiRigth) {
//                    WataLog.d("mRotation=" + mRotation);
//                    poiRatation += 180; // 왼쪽.
//                } else {
//                    poiRatation -= 180;
//                    WataLog.d("mRotation=" + mRotation);
//                }
//            } else {
//                poiRatation -= 180;
//            }
        }

//        double tempRatation = mRotation + mOmapRatation + poiRatation;
        double tempRatation = mOmapRatation + poiRatation;
        WataLog.d("tempRatation=====" + tempRatation);

        tempRatation = 2 * 3.14 * (tempRatation / 360);

        lastPointX += Math.cos(tempRatation) * 2;
        lastPointY += Math.sin(tempRatation) * 2;

//        updateTouch(lastPointX, lastPointY);
        WataLog.d("startPoint= " + startPointX + "//" + startPointY);
        pts.setPoint(0, startPointX, startPointY, 0.0f);
        pts.setPoint(1, lastPointX, lastPointY, 0.0f);

        line.setParts(0, pts);

        return line;
    }

    private Geometry getDrawingPoint(Double startPointX, Double startPointY) {
        Polyline line = new Polyline();
        line.makeParts(1);

        Points pts = new Points();
        pts.makePoints(2);

        Double lastPointX = startPointX, lastPointY = startPointY;
        double poiRatation = 0;
        double tempRatation = mCameraAngle + 90;

        tempRatation = 2 * 3.14 * (tempRatation / 360);
        lastPointX += Math.cos(tempRatation) * 200;
        lastPointY += Math.sin(tempRatation) * 200;

        pts.setPoint(0, startPointX, startPointY, 0.0f);
        pts.setPoint(1, lastPointX, lastPointY, 0.0f);

        line.setParts(0, pts);

        return line;
    }


    private double mRecordStartPointX = 0.0, mRecordStartPointY = 0.0;

    // 라인 미리보기
    private void setStartPointSetting(boolean addText) {
        WataLog.i("setStartPointSetting");
        //시작점 저장하기
        mRecordStartPointX = mLastPointX;
        mRecordStartPointY = mLastPointY;

        WataLog.d("mRecordStartPointX=" + mRecordStartPointX);
        WataLog.d("mRecordStartPointY=" + mRecordStartPointY);
        if (twoPointerLayer != null) {
            twoPointerLayer.removeAllElement();
        }

        lineSelected = (Polyline) getMyWayLine(mLastPointX, mLastPointY);
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getStartPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getStartPoint().y);
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getEndPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getEndPoint().y);
        WataLog.d("testNumlayer=" + testNumlayer);
        if (startPointModel != null) {
            startPointModel.removeAllElement();
        }
        if (endPointModel != null) {
            endPointModel.removeAllElement();
        }

        if (addText) {
            Element s_el = ElementHelper.fromTextBillboard(new Vector(lineSelected.getPart(0).getStartPoint().x, lineSelected.getPart(0).getStartPoint().y, 5.0, 0), 2f, 2f, true, "S", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
            if (s_el != null) {
                startPointModel = new SandboxModel("startPointModel");
                startPointModel.addElement("startPointModel", s_el);
            }
            ((CompositeModel) o2map.getModel()).addModel(startPointModel);

            Element e_el = ElementHelper.fromTextBillboard(new Vector(lineSelected.getPart(0).getEndPoint().x, lineSelected.getPart(0).getEndPoint().y, 5.0, 0), 2f, 2f, true, "E", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
            if (e_el != null) {
                endPointModel = new SandboxModel("endPointModel");
                endPointModel.addElement("endPointModel", e_el);
            }
            ((CompositeModel) o2map.getModel()).addModel(endPointModel);
        }

        //  라인그리기
        if (lineSelected != null) {
//            WataLog.d("myWayLine=" + myWayLine);
            if (myWayLine == null) {
                myWayLine = new SandboxModel("myWayLine");
            }
            myWayLine.removeAllElement();
            Element eal = ElementHelper.fromPolyline(lineSelected, 0.1, 2, new Color(0, 255, 0, 255), false, false);
            myWayLine.addElement("myWayLine" + mPointCount, eal);
            ((CompositeModel) o2map.getModel()).addModel(myWayLine);
        }

        _omapView.getO2mapInstance().requestRedraw();

//        o2map.renderUnlock();
//        if (lineSelected != null) {
//            startMyWayGathering(lineSelected);
//        }
    }

    private int mPointCount = 0;
    private String mTempPosiont = "";

    private void setPointCount(double startPointX, double startPointY, double endPointX, double endPointY) {
        WataLog.i("setPointCount");
//        boolean mIsAdded = false;
        double pointX = (((endPointX - startPointX) / 2) + startPointX);
        double pointY = (((endPointY - startPointY) / 2) + startPointY);

        WataLog.i("pointX=" + pointX);
        WataLog.i("pointY=" + pointY);

        if (myWayNum == null) {
        } else {
            myWayNum = null;
        }
        myWayNum = new SandboxModel("myWayNum");
//        mIsAdded = true;

        // 라인순번그리기
        WataLog.d("mPointCount=" + mPointCount);
        String countName = String.valueOf(mPointCount);
        Element el = ElementHelper.fromTextBillboard(new Vector(pointX, pointY, 1, 2), 2f, 2f, true, countName, 20.0f, Typeface.DEFAULT_BOLD, Color.BLACK);
        WataLog.i("check1!");
        myWayNum.addElement("pathPointerStart", el);
//        if (mIsAdded) {
        myWayNum.setOrder(100);
        ((CompositeModel) o2map.getModel()).addModel(myWayNum);
//        }
    }

    // 초기위치
    private Double mLastPointX = 0.0;
    private Double mLastPointY = 0.0;

    // kcy1000 - 경로 기록시작
    private void startMyWayGathering(Polyline line) {
        WataLog.d("startMyWayGathering = " + line);
        stepCount = 0;
        gatheringInfo.setText("");
//        gatheringOrientationDegree.setText("");
//        gatheringOrientation.setRotation(0);

        if (bLogging || bScanning) {
            Toast.makeText(this, "측위를 종료해 주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        this._omapView.getO2mapInstance().renderLock();
        PreIndex++;
        WataLog.d("PreIndex=" + PreIndex);
//        updateCurrentPath(line, PreIndex);

        this._omapView.getO2mapInstance().renderUnlock();

        _startPoint = new Vector(line.getPart(0).getStartPoint().x, line.getPart(0).getStartPoint().y, line.getPart(0).getStartPoint().z);
        _endPoint = new Vector(line.getPart(0).getEndPoint().x, line.getPart(0).getEndPoint().y, line.getPart(0).getEndPoint().z);

        WataLog.d("_startPoint =" + _startPoint);
        endX = _endPoint.x;
        endY = _endPoint.y;
        distRemain = -1;

        WataLog.d("endX=" + endX);
        WataLog.d("endY=" + endY);

        lineHeader = getHeader(_startPoint, _endPoint);

        pdrvariable.initValues();
        initialmm.MM_initialize3(line);
        wifiSet(true);
        sensorSet(true);

        //GatheringDialog.makeGatherNameInsertDailog(GatheringActivity.this, StaticManager.title, StaticManager.floorName, _UIHandler, 9999).show();
        final String curTime = AndroidUtils.getCurrentTime();
        final String autoName = StaticManager.title + "-" + StaticManager.floorName + "-" + curTime;

        WataLog.d("curTime=" + curTime);
        WataLog.d("autoName=" + autoName);

        DataCore dataCore = DataCore.getInstance();
        String insertText = autoName;

        WataLog.d("insertText=" + insertText);

        dataCore.setCurGatheringName(insertText);
        dataCore.setCurGatherStartTime(curTime);

//        _UIHandler.sendEmptyMessage(9999);
    }

    // kcy1000 - 경로기록하기
    private void myWayUpdateStep(String setDirection) {
        WataLog.d("myWayUpdateStep= " + setDirection);
        WataLog.d("endX=" + endX + "/// " + "endY=" + endY);

        if (endX == 0.0 && endY == 0.0) {
            gatheringFlag = true;
            return;
        }

        double x, y, z;

        x = (pdrvariable.getPedestrian_x_coordinate());
        y = (pdrvariable.getPedestrian_y_coordinate());
        z = (pdrvariable.getPedestrian_z_coordinate());
//		System.out.printf("update step %f %f %f\n",x,y,z);

        // 마지막경로를 저장한다.
        mLastPointX = x;
        mLastPointY = y;

        if (x != 0 && y != 0) {
            /**
             * 실제 이동시 이곳으로 분기
             */
            stepCount++;
            gatheringInfo.setText(String.format("Step : %d", stepCount));
//            gatheringInfo.playSoundEffect(SoundEffectConstants.CLICK);
//            gatheringInfo.post(new Runnable() {
//                @Override
//                public void run() {
//                    startStepCountAnimation();
//                }
//            });
            float[] rotationVectorOrientations = SensorUtils.getOrientation(sensoract.rotationVectors);

//            WataLog.d("rot = " + rotationVectorOrientations[0]);
//            WataLog.d("rot = " + rotationVectorOrientations[1]);
//            WataLog.d("rot = " + rotationVectorOrientations[2]);

//            gatheringOrientationDegree.setText(String.format("%.1f", rotationVectorOrientations[0]));
//            gatheringOrientation.setRotation(rotationVectorOrientations[0]);

            Vector v = new Vector(x, y, z, 0);
            _omapView.getO2mapInstance().renderLock();
            if (_startPoint != null) {
                double dist = Math.sqrt(Math.pow(x - _startPoint.x, 2) + Math.pow(y - _startPoint.y, 2));
                if (!Double.isNaN(dist)) {
                    boolean bAdded = false;
                    if (currentDistLayer == null) {
                        currentDistLayer = new SandboxModel("currDist");
                        bAdded = true;
                    }

                    if (currentDistLayer != null) {
                        currentDistLayer.removeAllElement();
                    }

                    DecimalFormat deciformat_WF = new DecimalFormat("#.##");
                    final String depth = deciformat_WF.format(dist);
                    WataLog.i("이동거리 및 현재위치");
                    Element el = ElementHelper.fromText(0.6f * depth.length(), 2f, depth + "m", 300, tff, new Color(0, 0, 0, 255));
//                    Element el = ElementHelper.fromText(0.6f * depth.length(), 2f, depth + "m", 300, tff, new Color(255, 0, 0, 255));
                    el.getTransform().clearTransform();
                    el.getTransform().setTranslation(v.x, v.y - 2, 2);
                    el.getTransform().calcMatrix();
                    el.getExtentHelper().updateExtent();
                    currentDistLayer.addElement("currDist", el);
                    if (bAdded)
                        ((CompositeModel) _omapView.getO2mapInstance().getModel()).addModel(currentDistLayer);
                }
            }

            _omapView.getO2mapInstance().getView().getCamera().setLookAtPosition(v);
            _omapView.getO2mapInstance().renderUnlock();
//            updatePointer(v);

//            setMyWayUpdatePointer(v);

            _omapView.requestRender();

            /**
             * 조영수박사님 로그 추가 건
             */
            try {
                ArrayList<PDI_REPT_COL_DATA_PAYLOAD> payloads = _dataCore.getSCAN_RESULT_THIS_EPOCH().B21_Payload;
                PDI_REPT_COL_DATA_PAYLOAD lastPayload = payloads.get(payloads.size() - 1);
                lastPayload.Step_Count++;
                lastPayload.Step_Length = pdrvariable.getStep_length();
                WataLog.d("lastPayload.Step_Count=" + lastPayload.Step_Count);
                WataLog.d("lastPayload.Step_Length=" + lastPayload.Step_Length);
            } catch (Exception e) {
                WataLog.e("Exception=" + e.toString());
            }
            /**
             * 조영수박사님 로그 추가 건 끝
             */
        } else {
//            Toast.makeText(PathDrawingActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
//            WataLog.e("check!!!!!!!!!!!!!!");
//            endX = 0.0;
//            endY = 0.0;
        }

        //Tag : 지점수집이 아니면...
        double dist = Math.sqrt(Math.pow(_endPoint.x - x, 2) + Math.pow(_endPoint.y - y, 2));
        WataLog.d("m remains = " + dist);

        if (Double.isNaN(dist)) {
            Toast.makeText(PathDrawingOldActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            WataLog.e("check!!!!!!!!!!!!!!");
            endX = 0.0;
            endY = 0.0;
        } else {
            if (distRemain < 0 || distRemain >= dist)
                distRemain = dist;
            else
                distRemain = -1;
            if (distRemain >= 0 && distRemain <= 0.7) {
                Toast.makeText(PathDrawingOldActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                WataLog.e("check!!!!!!!!!!!!!!");
                endX = 0.0;
                endY = 0.0;
            }
        }
    }

    // 지도에서 위치이동
    private void setMapLookPosition(double x, double y, double z) {
        Vector v = new Vector(x, y, z, 0);
        _omapView.getO2mapInstance().getView().getCamera().setLookAtPosition(v);
//        _omapView.getO2mapInstance().renderUnlock();
//        _omapView.requestRender();
    }


    // 역방향 기록하기 셋팅
    private void setReversePointSetting(double startPointX, double startPointY, double endPointX, double ebdPointY) {
        WataLog.i("역방향 셋팅");
        // 이동할 좌표 가져와야함.
        lineSelected = (Polyline) getMyWayReverseLine(startPointX, startPointY, endPointX, ebdPointY);
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getStartPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getStartPoint().y);
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getEndPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getEndPoint().y);
        WataLog.d("testNumlayer=" + testNumlayer);
        setReverLine();
    }

    //  정방향 라인그리기
    private void setForwardLine(Polyline line) {
        if (myRecordFLine == null) {
            myRecordFLine = new SandboxModel("myRecordFLine");
        }
        Element eal = ElementHelper.fromPolyline(line, 0.1, 5, new Color(0, 0, 255, 255), false, false);

        WataLog.d("myRecordFLine =" + myRecordFLine);
        mRecordPosition = mPointCount;
        myRecordFLine.addElement("myRecordFLine" + mPointCount, eal);
        ((CompositeModel) o2map.getModel()).addModel(myRecordFLine);

    }

    //  역방향 라인그리기
    private void setReverLine() {
        String removeName = "myRecordFLine" + String.valueOf(mPointCount);
        if (myRecordRLine == null) {
            myRecordRLine = new SandboxModel("myRecordRLine");
        }

        Element eal = ElementHelper.fromPolyline(lineSelected, 0.1, 2, Color.RED, false, false);
        myRecordRLine.addElement("myRecordRLine" + mRecordPosition, eal);
        ((CompositeModel) o2map.getModel()).addModel(myRecordRLine);
        _omapView.getO2mapInstance().requestRedraw();
    }


    private RecordListAdapter myWayListAdapter;
    private ArrayList<MyWayInfo> myWayInfoData;
    private int mRecordPosition = 0;  // list기록순번

    private PoiListAdapter mPoiListAdapter;
    private ArrayList<PoiInfo> mPoiInfoData;

    // 기록 라인그리기
    private void setRecordLine() {
        WataLog.i("setRecordLine");
        Polyline line = new Polyline();
        line.makeParts(1);

        Points pts = new Points();
        pts.makePoints(2);

        WataLog.d("mRecordStartPointX=" + mRecordStartPointX + "//// mRecordStartPointY=" + mRecordStartPointY);
        WataLog.d("mLastPointX=" + mLastPointX + "//// mLastPointY=" + mLastPointY);

        pts.setPoint(0, mRecordStartPointX, mRecordStartPointY, 0.0f);
        pts.setPoint(1, mLastPointX, mLastPointY, 0.0f);
        line.setParts(0, pts);

        Element eal;
        WataLog.d("isInverse=" + isInverse);
        if (!isInverse) { // 정방향
            setForwardLine(line);
        } else { // 역방향
//            setReverLine();
        }

        WataLog.d("mPointCount=" + mPointCount);
        _omapView.getO2mapInstance().requestRedraw();
        delIcon(); // icon제거

        WataLog.d("mCameraAngle==" + mCameraAngle);
        if (lineSelected != null) {
            if (!isInverse) { //정방향
                // 기록할 경로번호
//                setPointCount(lineSelected.getPart(0).getStartPoint().x, lineSelected.getPart(0).getStartPoint().y);

                WataLog.i("check!~!!!!!!!!!!!정방향!!!!!!!!!!!");
                WataLog.d("mRecordPosition=" + mRecordPosition);
                // 기록경로 list로 저장하기.
                double dist = Math.sqrt(Math.pow(mLastPointX - mRecordStartPointX, 2) + Math.pow(mRecordStartPointY - mLastPointX, 2));
                final String depth = deciformat_WF.format(dist);

//                myWayInfoData.add(new MyWayInfo(mRecordPosition, mRecordStartPointX, mRecordStartPointY, mLastPointX, mLastPointY, depth, "완료", "역방향 기록하기", mCameraAngle));
//                myWayListAdapter.setItems(myWayInfoData);
//                mPathDrawingListview.setAdapter(myWayListAdapter);

            } else {
                WataLog.i("check!~!!!!!!!!!!역방향!!!!!!!!!!!!");
                WataLog.d("mPointCount=" + mPointCount);
                WataLog.d("mRecordPosition=" + mRecordPosition);

//                double dist = Math.sqrt(Math.pow(mRecordStartPointX - mLastPointX, 2) + Math.pow(mRecordStartPointY - mLastPointY, 2));
//                final String depth = deciformat_WF.format(dist);
//                myWayInfoData.set(mRecordIndexNum, new MyWayInfo(mRecordPosition, mLastPointX, mLastPointY, mRecordStartPointX, mRecordStartPointY, depth, "완료", "역방향 완료", mCameraAngle));
//                myWayListAdapter.notifyDataSetChanged();
            }
        }
    }

    // kcy1000 - 기록하기
    // Tag : 취소 버튼은 save = false
    private void setRecordGathering(boolean save) {
        WataLog.i("endGathering=" + save);
        wifiSet(false);
        sensorSet(false);
        lineSelected = null;
        bEndPoint = false;
        WataLog.d("fullpath=" + fullpath);
        if (fullpath != null) {
            fullpath.clear();
        }
        fullpath = null;

        WataLog.d("multi=" + multi);
        if (multi != null) {
            multi.clear();
        }
        multi = null;
        lineHeader = Double.NaN;
        if (mKf != null) {
            mKf.init(null, null, 1);
            mKf = null;
        }
        Date currentTime = Calendar.getInstance().getTime();
        String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        StaticManager.setPathNum(date_text + (mPointCount));

        DataCore.iGatherMode = DataCore.GATHER_MODE_NONE;
        _dataCore.insertGatheredData(_dataCore.getCurGatheringName(), _dataCore.getSCAN_RESULT_TOTAL());
        _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();
        WataLog.d("/inverse=" + isInverse);
        WataLog.d("save=" + save);

        if (save) {
            // kcy1000 - 경로수집 저장
            File temp = new File(StaticManager.getResultPath());
            if (temp.exists()) {
                String[] tempArr = temp.list();
                for (int a = 0; a < tempArr.length; a++) {
                    String tempName = tempArr[a];
                    WataLog.d("tempName=" + tempName);

                    if (!isInverse) {
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

            WataLog.d("SAVE_FILE_NAME=" + SAVE_FILE_NAME);
            WataLog.d("_dataCore.getCurGatheringName()=" + _dataCore.getCurGatheringName());

            //log 기록저장하기
            if (!isInverse) {
                SaveGatheredData.saveGatheredData(StaticManager.getResultPathDrawingPath(), SAVE_FILE_NAME + "_" + _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
                SaveGatheredData.saveSendableData(StaticManager.getResultPathDrawingPath(), SAVE_FILE_NAME + "_" + _dataCore.getCurGatheringName() + "_F.temp", _SCAN_RESULT_THIS_EPOCH);

                setLineInfo(mRecordStartPointX, mRecordStartPointY, mLastPointX, mLastPointY);
            } else {
                SaveGatheredData.saveGatheredData(StaticManager.getResultPathDrawingPath(), SAVE_FILE_NAME + "_" + _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
                SaveGatheredData.saveSendableData(StaticManager.getResultPathDrawingPath(), SAVE_FILE_NAME + "_" + _dataCore.getCurGatheringName() + "_R.temp", _SCAN_RESULT_THIS_EPOCH);
            }
        }

        WataLog.d("saveLogForImage=" + saveLogForImage);
        WataLog.d("isInverse=" + isInverse);
        if (saveLogForImage) {
            if (!isInverse) {
                SaveGatheredData.saveLogForImage(StaticManager.getImageLogPath(), SAVE_FILE_NAME + "MYWAY_IMAGE_LOG_" + _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
            } else {
                SaveGatheredData.saveLogForImage(StaticManager.getImageLogPath(), SAVE_FILE_NAME + "MYWAY_IMAGE_LOG_" + _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
            }
            Toast.makeText(this, "영상정보수집로그 저장에 성공하였습니다.", Toast.LENGTH_SHORT).show();
        }

        _SCAN_RESULT_THIS_EPOCH = new PDI_REPT_COL_DATA(Build.SERIAL);
        _SCAN_RESULT_TOTAL = new ArrayList<PDI_REPT_COL_DATA>();

        //Tag : TOTAL 초기화
        _dataCore.setSCAN_RESULT_TOTAL(_SCAN_RESULT_TOTAL);
        _dataCore.setSCAN_RESULT_THIS_EPOCH(_SCAN_RESULT_THIS_EPOCH);

        isInverse = false;
        DataCore.isOnGathering = false;

        _startPoint = null;
        _endPoint = null;

        WataLog.i("check1");
//            delIcon(); //아이콘 제거
        WataLog.d("_which=" + _which);
//            _clickListener.viewEachPath(_which); //path 재설정(?)

        onPathSetting();
//            map_path_start.setVisibility(View.VISIBLE); //path 설정 버튼 가시화
    }

    void onPathSetting() { //기록할때 사용한 이미지 삭제.
        WataLog.i("onPathSetting");
        endX = 0.0;
        endY = 0.0;

        O2Map o2map = (_omapView.getO2mapInstance());
//        WataLog.d("isInverse=" + isInverse);

//        updateCurrentPath(null, 0);
        _UIHandler.sendEmptyMessage(DataCore.AT_START_NEW_RECORD);

//        boolean bAdd = false;
//        if (eachPathLayer == null) {
//            eachPathLayer = new SandboxModel("eachPathLayer");
//            bAdd = true;
//        }
        if (prevPathLayer != null) {
            if (prevPathLayer.size() > 0) {
                for (int i = 0; i < mNumTest.length; i++) {
                    if (mNumTest[i] > 0) {
                        prevPathLayer.removeElement(new String("prevPath" + mNumTest[i]));
                        testNumlayer.removeElement(new String("pointer" + mNumTest[i]));
                    }
                }
            }
        }

//        if (eachPathLayer.size() > 0) {
//            eachPathLayer.removeElement(new String("currentPath"));
//        }
//
//        lineSelected = (Polyline) wReader.getLine(pos);
//
//        if (lineSelected != null) {
//
//            displayPathFinish();
//
//            Points pts = lineSelected.getPart(0);
//            Point start = pts.data[0];
//            Point end = pts.data[pts.getNumPoints() - 1];
//
//            _startPoint = new Vector(start.x, start.y, 0);
//            WataLog.d("_startPoint =" + _startPoint);
////                _startPoint = new Vector(105.606839579521505, -184.879042430578039, 0);
//            startX = start.x;
//            startY = start.y;
//            WataLog.d("_startPoint= " + _startPoint);
//
////                105.606839579521505, -184.879042430578039
//            _endPoint = new Vector(end.x, end.y, 0);
////                _endPoint = new Vector(105.606839579521505, -184.879042430578039, 0);
//            WataLog.d("_endPoint= " + _endPoint);
//            endX = end.x;
//            endY = end.y;
//
//            WataLog.i("chekc");
//            Element el = ElementHelper.fromPolyline(lineSelected, 0.1, 5, new Color(0, 0, 255, 255), false, false);
//            eachPathLayer.addElement("currentPath", el);
//
//            // Start, End Icon : by Ethan
//            setIcon(1, startX, startY); //1:start
//            setIcon(2, endX, endY); //2:end
//            o2map.getView().getCamera().setLookAtPosition(new Vector(startX, startY, 0));
//        }
//
//        if (bAdd) {
//            ((CompositeModel) o2map.getModel()).addModel(eachPathLayer);
//        }
        _omapView.requestRender();
    }

    private void onStartRecord(double startPointX, double startPointY, double endPointX, double ebdPointY) {
        WataLog.i("onStartRecord");

        Toast.makeText(PathDrawingOldActivity.this, "아직은 안되요 ㅠ.ㅠ ", Toast.LENGTH_SHORT).show();
    }

    //==================== 바로가기 셋팅
    private void setRecordPointSetting(int pointCount, double startPointX, double startPointY, double endPointX, double ebdPointY) {
        WataLog.i("방향 셋팅");
        lineSelected = (Polyline) getMyWayReverseLine(startPointX, startPointY, endPointX, ebdPointY);
        // 정향방향 라인지우기
        myRecordFLine.removeElement("myRecordFLine" + pointCount);
        WataLog.i("check!!!");
        if (myWayLine == null) {
            myWayLine = new SandboxModel("myWayLine");
            WataLog.i("check!!!");
        }
        WataLog.i("check!!!");
        myWayLine.removeAllElement();
//        deleteLine(myRecordFLine, "myRecordFLine" + pointCount);
        deleteLine(startPointModel, "startPointModel");
        deleteLine(endPointModel, "endPointModel");

        _omapView.getO2mapInstance().requestRedraw();
        // 카운터 숫자 표시안함

        record_lisetview.setVisibility(View.GONE); // 창닫기
        // 위치표시

    }

    // 역방향 기록라인 생성
    private Geometry getMyWayReverseLine(double startPointX, double startPointY, double endPointX, double ebdPointY) {
        Polyline line = new Polyline();
        line.makeParts(1);

        Points pts = new Points();
        pts.makePoints(2);

        pts.setPoint(0, startPointX, startPointY, 0.0f);
        pts.setPoint(1, endPointX, ebdPointY, 0.0f);
        line.setParts(0, pts);
        return line;
    }

    private void setUiControl(int type) {

        switch (type) {
            case 0: { // 최초 셋팅
                map_inner_frm_linear1.setVisibility(View.VISIBLE);
                my_way_record_info_layout.setVisibility(View.VISIBLE);

                break;
            }
            case 1: { // 기록중
                my_way_path_start.setEnabled(false);
                my_way_path_end.setEnabled(true);
                reverse_record_btn.setEnabled(false);
                record_cancel.setEnabled(true);
                save_point_check_btn.setEnabled(true);
                path_drawing_setting.setEnabled(false);

                angle_layout.setVisibility(View.GONE);
                precision_btn.setVisibility(View.VISIBLE);
                angle_control_layout.setVisibility(View.GONE);

                if (isGyroSensor) {
                    step_btn.setVisibility(View.GONE);
                } else {
                    step_btn.setVisibility(View.VISIBLE);
                }
                break;
            }
            case 2: { // 기록종료
                my_way_path_start.setEnabled(true);
                my_way_path_end.setEnabled(false);
                reverse_record_btn.setEnabled(true);
                record_cancel.setEnabled(false);
                save_point_check_btn.setEnabled(false);
                path_drawing_setting.setEnabled(true);
                step_btn.setVisibility(View.GONE);

                if (isMapRatation) {
                    angle_layout.setVisibility(View.VISIBLE);
                } else {
                    angle_layout.setVisibility(View.GONE);
                }

                break;
            }
            case 3: {
                break;
            }
        }
    }


    //kcy1000 - 지도회전 -1
    private void setCameraMoving() {
        Camera mapRatation = _omapView.getO2mapInstance().getView().getCamera();
        WataLog.d("mapRatation=" + mapRatation);
        WataLog.d("mapRatation.getRotation().W=" + mapRatation.getRotation().w);
        WataLog.d("mapRatation.getRotation().Z=" + mapRatation.getRotation().z);

        Angle angle = mapRatation.getRotation().getAngle();
        WataLog.d("angle=" + angle.getDegrees());

        WataLog.d("getRotationX=" + mapRatation.getRotation().getRotationX());
        WataLog.d("getRotationY=" + mapRatation.getRotation().getRotationY());
        WataLog.d("getRotationZ=" + mapRatation.getRotation().getRotationZ());

//                            mCameraAngle = (int)(2 * 3.14 * (angle.degrees / 360));
        mCameraAngle = (int) angle.degrees;
        WataLog.d("mCameraAngle=" + mCameraAngle);

        double rotationZ = mapRatation.getRotation().getRotationZ().degrees;
        WataLog.d("rotationZ=" + rotationZ);
        if (rotationZ > 0) {
            if (mCameraAngle > 180) {
                mCameraAngle = 360 - mCameraAngle;
            }
        } else {
            if (mCameraAngle < 180) {
                mCameraAngle = 360 - mCameraAngle;
            }
        }

        WataLog.d("mCameraAngle=" + mCameraAngle);
    }


    //kcy1000 - 지도회전 -2
    private void setDrawingPointSetting() {
        WataLog.i("지점이동 라인 미리보기");

//        WataLog.d("mLastPointX=" + mLastPointX);
//        WataLog.d("mLastPointY=" + mLastPointY);

        //시작점 저장하기
        mRecordStartPointX = mLastPointX;
        mRecordStartPointY = mLastPointY;
//        if (twoPointerLayer != null) {
//            twoPointerLayer.removeAllElement();
//        }

        lineSelected = (Polyline) getDrawingPoint(mLastPointX, mLastPointY);
        onRotation(mCameraAngle - 90);

    }

    //kcy1000 - 지도회전 -3
    private void setStartPointLine() {
        //  라인그리기
        WataLog.d("lineSelected=" + lineSelected);
        WataLog.d("mLastPointX=" + mLastPointX + "////mLastPointY=" + mLastPointY);
        WataLog.d("_startPoint=" + _startPoint + "////_startPoint=" + _startPoint);
        if (lineSelected != null) {
            if (myWayLine == null) {
                myWayLine = new SandboxModel("myWayLine");
            }
            myWayLine.removeAllElement();

            Element eal = ElementHelper.fromPolyline(lineSelected, 0.5, 2, Color.RED, false, false);
            myWayLine.addElement("myWayLine", eal);
            ((CompositeModel) o2map.getModel()).addModel(myWayLine);
            _omapView.getO2mapInstance().requestRedraw();
        }
    }

    //kcy1000 - 지도회전 -4
    private void setStartEndPointName() {
        WataLog.d("mRecordStartPointX=" + mRecordStartPointX);
        WataLog.d("mRecordStartPointY=" + mRecordStartPointY);
        if (twoPointerLayer != null) {
            twoPointerLayer.removeAllElement();
        }

        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getStartPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getStartPoint().y);
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getEndPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getEndPoint().y);
        WataLog.d("testNumlayer=" + testNumlayer);
        if (startPointModel != null) {
            startPointModel.removeAllElement();
        }
        if (endPointModel != null) {
            endPointModel.removeAllElement();
        }

        Element s_el = ElementHelper.fromTextBillboard(new Vector(lineSelected.getPart(0).getStartPoint().x, lineSelected.getPart(0).getStartPoint().y, 5.0, 0), 2f, 2f, true, "S", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
        if (s_el != null) {
            startPointModel = new SandboxModel("startPointModel");
            startPointModel.addElement("startPointModel", s_el);
        }
        ((CompositeModel) o2map.getModel()).addModel(startPointModel);

        Element e_el = ElementHelper.fromTextBillboard(new Vector(lineSelected.getPart(0).getEndPoint().x, lineSelected.getPart(0).getEndPoint().y, 5.0, 0), 2f, 2f, true, "E", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
        if (e_el != null) {
            endPointModel = new SandboxModel("endPointModel");
            endPointModel.addElement("endPointModel", e_el);
        }
        ((CompositeModel) o2map.getModel()).addModel(endPointModel);
    }

    public double distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    private double caldistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    // ====================================================================================================================================


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private void setSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);

    }

    private float[] mGravity = null;
    private float[] mGeomagnetic = null;
    private float mCurrentDegree = 0f;
    private ArrayList<Integer> compassArry = new ArrayList<>();
    private int mDegressSum = 0;
    private int mTempDegress = 0;
    private long mTempTime = 0;

    public static float[] rotationVectors = new float[3];

    //나침반
    @Override
    public void onSensorChanged(SensorEvent event) {
        float azimut, pitch, roll;

//        WataLog.d("event.sensor.getType()=" + event.sensor.getType());
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        }

//        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//            mGeomagnetic = event.values;
//            WataLog.d("mGeomagnetic=" + mGeomagnetic[0]);
//        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
//            WataLog.d("mGeomagnetic=" + mGeomagnetic[0]);
        }

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0];
                pitch = orientation[1];
                roll = orientation[2];

                float azimuthinDegress = (int) (Math.toDegrees(SensorManager.getOrientation(R, orientation)[0] + 360) % 360);
//                WataLog.d("azimuthinDegre ss=" + azimuthinDegress);

                RotateAnimation ra = new RotateAnimation(mCurrentDegree + 90, -(azimuthinDegress + 90),
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);

                ra.setDuration(250);
                ra.setFillAfter(true);
//                compass_img.startAnimation(ra);
                mCurrentDegree = -azimuthinDegress;
                int degress = (int) azimuthinDegress;
                long now = System.currentTimeMillis();

//                WataLog.d("mTempTime = " + mTempTime);
//                WataLog.d("time -mTempTime = " + (now - mTempTime));

                if (mTempDegress + 2 != degress && degress != mTempDegress - 2 && ((now - mTempTime) > 600 || mTempTime == 0)) {
                    mDegressSum = degress;
                    mTempTime = now;

//                    WataLog.d("CAMERA_MOVING = " + CAMERA_MOVING);
//                    WataLog.d("isMapRatation = " + isMapRatation);
                    if (CAMERA_MOVING && myWayListAdapter.getCount() > 0 && isMapRatation) {
                        if (isMapRatation) {
                            WataLog.d("지도각도 =" + mDegressSum + "-" + mSaveDegress + "=" + (mDegressSum - mSaveDegress));
                            int mathD = mDegressSum - mSaveDegress;
                            if (mathD > 0) {
                                mCameraAngle = 360 - Math.abs(mathD);
                            } else {
                                mCameraAngle = Math.abs(mathD);
                            }

//                            WataLog.d("now=" + now);
//                            WataLog.d("mCameraAngle=" + mCameraAngle);
                            setMapRotation(mCameraAngle);
//                        setDrawingPointSetting();
                            //시작점 저장하기
                            mRecordStartPointX = mLastPointX; // 끝점을 시작점으로 변경
                            mRecordStartPointY = mLastPointY;
                            lineSelected = (Polyline) getDrawingPoint(mLastPointX, mLastPointY);
                            onRotation(mCameraAngle);

                            setStartPointLine();

//                        setStartEndPointName();

                        }
                    }
                } else {
                    mTempDegress = (int) azimuthinDegress;
//                    mTempTime = now;
                    mDegressSum = degress;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        WataLog.d("accuracy=" + accuracy);
//        onToast(Gravity.CENTER, "센스정확도변경=" + accuracy);
    }

    // 지도회전
    private void setMapRotation(int degress) {
        Quaternion q = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, Angle.fromDegrees(degress)); // 지도회전
        _omapView.getO2mapInstance().getView().getCamera().setRotation(q);
        _omapView.getO2mapInstance().requestRedraw();
    }

    private void delIcon() {
        if (twoPointerLayer != null) {
            O2Map o2map = (_omapView.getO2mapInstance());
            twoPointerLayer.removeAllElement();
            ((CompositeModel) o2map.getModel()).removeModel(twoPointerLayer);
            twoPointerLayer = null;
        }
    }

    private void deleteLine(SandboxModel model, String name) {
        if (model != null) {
            O2Map o2map = (_omapView.getO2mapInstance());
//            model.removeAllElement();
            model.removeElement(name);
            ((CompositeModel) o2map.getModel()).removeModel(model);
            model = null;
        }
    }

    // ButtonClickListener class end
    // -----------------------------------------------------------------------------------------------------------


    @SuppressWarnings("static-access")
    private void refreshNorthIcon() {
        if (_omapView == null)
            return;
        O2Map o2map = (_omapView.getO2mapInstance());

        if (mNorth != null) {
            Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.north_arrow);

            int width = 0;
            int height = 0;
            if (bitmapOrg != null) {
                width = bitmapOrg.getWidth();
                height = bitmapOrg.getHeight();

                // create a matrix for the manipulation
                if (width > 0 && height > 0) {
                    Matrix matrix = new Matrix();
                    //	         renderLock(true);
                    float mDegree = 0.0f;
                    mDegree = (float) ViewUtil.getZAxisDegree(o2map.getView().getCamera().getRotation());
                    //Log.d("jeongyeol", "w "+width+"/h "+height+"/deg "+mDegree);
                    if (!Float.isNaN(mDegree)) {
                        matrix.postRotate(mDegree);
                        //	         renderLock(false);
                        // recreate the new Bitmap
                        Bitmap resizedBitmap = null;
                        try {
                            resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0,
                                    width, height, matrix, true);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }

                        // make a Drawable from Bitmap to allow to set the BitMap
                        // to the ImageView, ImageButton or what ever
                        if (resizedBitmap != null) {
                            @SuppressWarnings("deprecation")
                            BitmapDrawable bmd = new BitmapDrawable(resizedBitmap);

                            // set the Drawable on the ImageView
                            mNorth.setImageDrawable(bmd);

                            // center the Image
                            mNorth.setScaleType(ImageView.ScaleType.CENTER);
                        }
                    }
                }
            }
        }
    }


    private KalmanFilter mKf = null;
    private Element currPos = null;
    private Element TestPos = null;

    private PositionProvider posProvider = null;
    private pdr_interface m_pdrinter = null;
    private boolean bServerFP = false, bkistPDRInited = false;
    private double mNowPointX = 0.0, mNowPointY = 0.0;
    private AlertDialog mPoiDialog;
    private int mCameraAngle = 0;
    private boolean mStartRecord = false;

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
                            if (LayerForApp != null) {
                                _omapView.getO2mapInstance().renderLock();
                                ((CompositeModel) _omapView.getO2mapInstance().getModel()).removeModel(LayerForApp);
                                _omapView.getO2mapInstance().renderUnlock();
                                LayerForApp = null;
                                viaPointIdx = 0;
                            }
                            break;

                        case DataCore.AT_RECORD_SENSING:
//                            WataLog.i("AT_START_NEW_RECORD");
                            boolean bAdd = false;
                            if (LayerForApp == null) {
                                LayerForApp = new SandboxModel("LayerForApp");
                                bAdd = true;
                            }

                            double temp[] = (double[]) msg.obj;

                            //						TransformGeo toGRS80 = new TransformGeo(6378137.0, 298.257223563, 1.0, 38.0, 127.0, 600000.0, 200000.0);
                            //						Vector tempGRS80 = toGRS80.transformTM2GP(temp[1], temp[0]);
                            //						Log.d("jeongyeol", "GRS80 lng="+tempGRS80.x+"("+temp[0]+")"+"/lat="+tempGRS80.y+"("+temp[1]+")");
                            //						Toast.makeText(GatheringActivity.this, "GRS80 lng="+tempGRS80.x+"/lat="+tempGRS80.y, 200).show();

                            double pointerSize = 0.5;

                            _omapView.getO2mapInstance().renderLock();
                            Element el = null;

                            WataLog.d("temp[0]=" + temp[0]);
                            WataLog.d("temp[1]=" + temp[1]);

                            mNowPointX = temp[0];
                            mNowPointY = temp[1];

                            //kcy1000 - 기록되는 지점 표시
                            if (StaticManager.folderName.equalsIgnoreCase("office")) {
                                el = ElementHelper.quadPyramidFromPoint(temp[0] + 5, temp[1] + 5, 0.1, 0.1f, 0.1f, 0.2f, Color.GREEN, Color.GREEN, Color.GREEN, false);
                            } else {
                                pointerSize = 1.5;
                                el = ElementHelper.quadPyramidFromPoint(temp[0], temp[1], 0.1, pointerSize, pointerSize, 1.1f, Color.RED, Color.RED, Color.RED, false);
                            }

//                            WataLog.i("경로 표시");
                            // 경로기록표시
                            LayerForApp.addElement("NodeForApp" + viaPointIdx, el);
                            viaPointIdx++;
                            if (bAdd) {
                                ((CompositeModel) _omapView.getO2mapInstance().getModel()).addModel(LayerForApp);
                            }
                            _omapView.getO2mapInstance().renderUnlock();
                            break;

                        case DataCore.PDR_STAT_CHANGED: // from sensor
                            // 경로기록
                            WataLog.i("PDR_STAT_CHANGED");
                            int scanCnt = _dataCore.getSCAN_RESULT_THIS_EPOCH().B21_Payload.size();
                            WataLog.d("scanCnt=" + scanCnt);
                            if (scanCnt > 1) {
                                myWayUpdateStep("");
                            }

                            break;

                        case DataCore.HANDLER_ENDED:
                            _isHandlerEnded = true;
                            break;

                        case DataCore.REFREASH_WIFI_LIST: //wifi			// Scan Step Count : by Ethan
//                            WataLog.i("REFREASH_WIFI_LIST");
                            _SCAN_RESULT_TOTAL = _dataCore.getSCAN_RESULT_TOTAL();
                            _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();

                            WataLog.d("_SCAN_RESULT_THIS_EPOCH.B21_Payload.size()=" + _SCAN_RESULT_THIS_EPOCH.B21_Payload.size());
                            if (_SCAN_RESULT_THIS_EPOCH.B21_Payload.size() == 2) {
                                progress_layout.setVisibility(View.GONE);
                                if (!mStartRecord) {
                                    save_point_check_btn.setEnabled(true);
                                    Toast.makeText(PathDrawingOldActivity.this, "수집을 시작해주세요.", Toast.LENGTH_SHORT).show();
                                    mStartRecord = true;
                                }
                            } else if (_SCAN_RESULT_THIS_EPOCH.B21_Payload.size() > 2) {
                                if (progress_layout.getVisibility() == View.VISIBLE) {
                                    progress_layout.setVisibility(View.GONE);
                                }
                            }
                            String total = _SCAN_RESULT_THIS_EPOCH.B21_Payload.size() + "회 스캔 완료";
//						setStatusString(total);
                            break;

                        case DataCore.DATA_SEND_ENDED:
                            WataLog.i("DATA_SEND_ENDED");
//                            _gatheringEndDialog.dismiss();
                            Toast.makeText(PathDrawingOldActivity.this, "전송이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            finish();
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
//                            WataLog.i("ON_TOUCH_MOVE_MAP");
                            _omapView.getO2mapInstance().renderLock();
                            refreshNorthIcon();
                            _omapView.getO2mapInstance().renderUnlock();

//                            WataLog.d("CAMERA_MOVING=" + CAMERA_MOVING);
//                            WataLog.d("mLastPointX=" + mLastPointX);
//                            WataLog.d("mLastPointY=" + mLastPointY);
                            WataLog.d("CAMERA_MOVING = " + CAMERA_MOVING);
                            WataLog.d("isMapRatation = " + isMapRatation);

                            if (CAMERA_MOVING && !isMapRatation || myWayListAdapter.getCount() == 0) {

//                                } else {
                                setCameraMoving();
                                setDrawingPointSetting();
                                setStartPointLine();
//                                setStartEndPointName();
//                                }
                            }

//                            WataLog.d("mCameraAngle=" + mCameraAngle);
//                            WataLog.d("mRotation=" + mRotation);
//                            WataLog.d("mapRatation=" + mapRatation.getRotation().getAngle());

                            break;

                        case DataCore.ON_TOUCH_MAP: // gathering listener
                            WataLog.i("ON_TOUCH_MAP");

                            float x = _mapTouchListener.getPrevX();
                            float y = _mapTouchListener.getPrevY();

                            WataLog.d("x=====" + x + "///// y=====" + y);
                            WataLog.i("!!!!!!!!!!!");
                            WataLog.d("mLastPointX= " + mLastPointX);
                            WataLog.d("mLastPointY= " + mLastPointY);
                            if (mLastPointX == 0.0 && mLastPointY == 0.0) {
//                                int heightPoint = (mHeightPixels / 5) * 3;
//                                updateTouch(mWidthPixel/2, heightPoint);

                            } else {
                                // POI지정
                                Line line = ViewUtil.computeRayFromScreenPoint(_omapView.getO2mapInstance().getView(), (int) _mapTouchListener.getPrevX(), (int) _mapTouchListener.getPrevY());
                                Plane pl = new Plane(0.0, 0.0, 1.0, 0);
                                Vector v = pl.intersect(line);

                                mNowPointX = v.x;
                                mNowPointY = v.y;

                                WataLog.d("mNowPointX= " + mNowPointX);
                                WataLog.d("mNowPointY= " + mNowPointY);
                                poi_edit.setText("");
                                poi_point_layout.setVisibility(View.VISIBLE);

//                                    mPoiDialog = new AlertDialog.Builder(PathDrawingActivity.this)
//                                            .setTitle("POI 설정")
//                                            .setNeutralButton("설정", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//
//                                                }
//                                            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
//                                                @Override
//                                                public void onCancel(DialogInterface dialog) {
//                                                    mPoiDialog.dismiss();
//                                                }
//                                            }).show();
                            }

                            break;

                        case DataCore.GATHER_MODE_CHANGED:
                            WataLog.i("GATHER_MODE_CHANGED");
                            break;

                        case 9999: //도착지점 선택시
//                            map_inner_frm_linear1.setVisibility(View.INVISIBLE);
//                            map_inner_frm_linear2.setVisibility(View.VISIBLE);
                            break;

                        case 2919:
                            RspPosition rspPos = (RspPosition) msg.obj;
                            if (rspPos != null)// && rspPos.errorCode >= 0)
                            {
                                Log.d("ejcha", "2919 -- error = " + rspPos.errorCode + "/" + rspPos.x + "/" + rspPos.y + "/" + bScanning + "/" + bLogging);
//							if(rspPos.x == 0 && rspPos.y == 0)
//							{
//								bIsGetFP = false;
//								if(pathFlag == 4 && bLogging)
//								{
//									_wifiScanner._wifiManager.startScan();
//								}
//								break;
//
//							}
                                if (bScanning || bLogging) {
                                    if (rspPos.x != 0.0 && rspPos.y != 0.0) {
                                        Proj4 proj4 = new Proj4(rspPos.y, rspPos.x, CRSRegistry.EPSG_4326, CRSRegistry.EPSG_5186);
                                        Vector currPosTM = proj4.transformToVector();

//									TransformGeo Tg = new TransformGeo(6378137.0, 298.257223563, 1.0, 38.0, 127.0, 600000.0, 200000.0);
//							        Vector currPosTM = Tg.transformGP2TM(rspPos.x, rspPos.y);
                                        rspPos.x = currPosTM.x;
                                        rspPos.y = currPosTM.y;
                                        WataLog.d("2919 -- 좌표변환후= " + rspPos.x + "/" + rspPos.y + "/" + bScanning + "/" + bLogging);
                                    } else {
                                        if (bLogTypeOnlySuc && bLogging) {
                                            bIsGetFP = false;
                                            WataLog.d("3");
                                            _wifiScanner._wifiManager.startScan();            // Logging : by Ethan
                                            break;
                                        }
                                    }

                                    if (targetBLE != null && lastBLE != null) {
                                        rspPos.x = targetBLE.x;
                                        rspPos.y = targetBLE.y;
                                        rspPos.strFloor = "BB01";
                                        Log.d("jeongyeol", "use beacon position");
                                    } else if (m2ndTarget != null) {
                                        double rssiWeight = 10.0;
                                        if (m2ndBLE != null && beacons_L != null) {
                                            boolean found = false;
                                            for (e_BLEScandata e : beacons_L) {
                                                if (m2ndBLE != null && e.MACaddr != null && e.MACaddr.equals(m2ndBLE)) {
                                                    if (SystemClock.currentThreadTimeMillis() - e.LastScanTime < 10 * 1000) {
                                                        found = true;
                                                        if (e.RSSI > -75)
                                                            rssiWeight = 1.0;
                                                        else if (e.RSSI > -78)
                                                            rssiWeight = 2.0;
                                                        else if (e.RSSI > -80)
                                                            rssiWeight = 4.0;
                                                        else
                                                            found = false;
                                                        break;
                                                    } else
                                                        break;
                                                }
                                            }
                                            if (!found) {
                                                m2ndBLE = null;
                                                m2ndRssi = 0;
                                                m2ndTarget = null;
//											bFilterProc = false;
//											return pos_result;
                                            } else if (m2ndTarget != null) {
                                                rspPos.x = rspPos.x + ((m2ndTarget.x - rspPos.x) / rssiWeight);
                                                rspPos.y = rspPos.y + ((m2ndTarget.y - rspPos.y) / rssiWeight);
                                                //					pos_result.F("BB01");
                                                Log.d("jeongyeol", "use beacon signal");
                                            }
                                        } else {
                                            m2ndBLE = null;
                                            m2ndRssi = 0;
                                            m2ndTarget = null;
                                        }
                                    }
//								else
//								{
//									if(SystemClock.uptimeMillis()%4 == 0)
//									{
//										rspPos.x = 0;
//								        rspPos.y = 0;
//									}
//								}
                                    Vector filteredPos = null;
                                    Vector viewVector = null;
//								if(mKf == null)
//								{
//									double[][] tempMatrix = {{rspPos.x}, {8}, {rspPos.y}, {8}};
//									Jama.Matrix input = new Jama.Matrix(tempMatrix);
//									Log.d("jeongyeol", "kf init x = "+input.get(0, 0)+"/ y = "+input.get(2, 0));
//									mKf = new KalmanFilter();
//									mKf.init(input, null, 1);
//									filteredPos = new Vector(rspPos.x, rspPos.y, 0);
////									filteredPos.strFloor = rspPos.strFloor;
//
//									Log.d("jeongyeol", "2919 -- kalman = "+filteredPos.x+"/"+filteredPos.y);
//									if(android.os.Build.BRAND != null && android.os.Build.BRAND.equalsIgnoreCase("samsung") && android.os.Build.MODEL != null && android.os.Build.MODEL.equalsIgnoreCase("SM-P600"))
//										viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[2]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0] - 90)));
//									else
//										viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[1]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0])));
//	//								bIsAccessToSensor = false;
//									viewVector = new Vector(viewVector.getX(),viewVector.getY(),0,0).normalize3();
//									viewVector = viewVector.multiply3(steplenthestimation.present_acc_variance_value/10);
//
//									viewVector = new Vector(filteredPos.x, filteredPos.y, 0).add3(viewVector);
//								}
//								else
//								{
//									double[][] tempMatrix = {{rspPos.x}, {rspPos.y}};
//									Jama.Matrix input = new Jama.Matrix(tempMatrix);
//									Log.d("jeongyeol", "kf update x = "+input.get(0, 0)+"/ y = "+input.get(1, 0));
//									Jama.Matrix output = mKf.doUpdate(input);
//									if(output != null)
//									{
//										filteredPos = new Vector(output.get(0, 0), output.get(2, 0), 0);
////										filteredPos.strFloor = rspPos.strFloor;
//
//										Log.d("jeongyeol", "2919 -- kalman = "+filteredPos.x+"/"+filteredPos.y);
//										if(android.os.Build.BRAND != null && android.os.Build.BRAND.equalsIgnoreCase("samsung") && android.os.Build.MODEL != null && android.os.Build.MODEL.equalsIgnoreCase("SM-P600"))
//											viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[2]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0] - 90)));
//										else
//											viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[1]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0])));
//		//								bIsAccessToSensor = false;
//										viewVector = new Vector(viewVector.getX(),viewVector.getY(),0,0).normalize3();
//										viewVector = viewVector.multiply3(steplenthestimation.present_acc_variance_value/10);
//
//										viewVector = new Vector(filteredPos.x, filteredPos.y, 0).add3(viewVector);
//
//										Log.d("jeongyeol", "2919 -- orientation = "+viewVector.x+"/"+viewVector.y);
//									}
//								}
                                    if (bScanning) {
                                        //								if(rspPos.GID != null && StaticManager.gid != null && !rspPos.GID.equalsIgnoreCase(StaticManager.gid))
                                        //								{
                                        //									bIsGetFP = false;
                                        //									return;
                                        //								}
                                        //								rspPos.x = rspPos.x/10;
                                        //								rspPos.y = rspPos.y/10;
//									Log.d("jeongyeol", "2919 -- no error = "+rspPos.x+"/"+rspPos.y+"/"+rspPos.GID);
                                        //								if(rspPos.x == 0)
                                        //									rspPos.x = 127;
                                        //								if(rspPos.y == 0)
                                        //									rspPos.y = 38;
                                        // PDR POINT ??? Ethan
                                        if (currPos == null || currentPosLayer == null) {
                                            currPos = ElementHelper.quadPyramidFromPoint(0, 0, 0, 2.5, 2.5, 2.5 * 2, Color.RED, Color.RED, Color.RED, false);

                                            O2Map o2map = (_omapView.getO2mapInstance());

                                            boolean bAdded = false;

                                            //currentPosLayer = (SandboxModel) ((CompositeModel)o2map.getModel()).getModelByName("currentPos");

                                            if (currentPosLayer == null) {
                                                currentPosLayer = new SandboxModel("currentPos");
                                                bAdded = true;

                                            }

                                            //					        		Vector lookAt=o2map.getView().getCamera().getLookAtPosition();

                                            if (currentPosLayer.size() > 0) {
                                                currentPosLayer.removeElement("curr");
                                            }
                                            currPos.getTransform().clearTransform();
                                            currPos.getTransform().setTranslation(rspPos.x, rspPos.y, 1);
                                            currPos.getTransform().calcMatrix();
                                            currPos.getExtentHelper().updateExtent();
                                            currentPosLayer.addElement("curr", currPos);
                                            if (bAdded)
                                                ((CompositeModel) o2map.getModel()).addModel(currentPosLayer);
                                        } else if (currPos != null && currentPosLayer != null) {
                                            currPos.getTransform().clearTransform();
                                            currPos.getTransform().setTranslation(rspPos.x, rspPos.y, 1);
                                            currPos.getTransform().calcMatrix();
                                            currPos.getExtentHelper().updateExtent();
                                        }
//						        	Toast.makeText(GatheringActivity.this, rspPos.x+"/"+rspPos.y+"/"+rspPos.strFloor+"/"+rspPos.address, 200).show();
                                        _omapView.requestRender();
                                    } else if (bLogging && touchedNode != null) {
                                        //							if(rspPos.GID != null && StaticManager.gid != null && !rspPos.GID.equalsIgnoreCase(StaticManager.gid))
                                        //							{
                                        //								bIsGetFP = false;
                                        //								return;
                                        //							}
                                        //							rspPos.x = rspPos.x/10;
                                        //							rspPos.y = rspPos.y/10;
//									Toast.makeText(GatheringActivity.this, rspPos.x+"/"+rspPos.y, 200).show();

                                        if (TestPos == null || testPosLayer == null) {
                                            TestPos = ElementHelper.quadPyramidFromPoint(0, 0, 0, 2.5, 2.5, 2.5 * 2, Color.GRAY, Color.GRAY, Color.GRAY, false);

                                            O2Map o2map = (_omapView.getO2mapInstance());

                                            boolean bAdded = false;

                                            //currentPosLayer = (SandboxModel) ((CompositeModel)o2map.getModel()).getModelByName("currentPos");

                                            if (testPosLayer == null) {
                                                testPosLayer = new SandboxModel("TestPos");
                                                bAdded = true;

                                            }

                                            //					        		Vector lookAt=o2map.getView().getCamera().getLookAtPosition();

                                            if (testPosLayer.size() > 0) {
                                                testPosLayer.removeElement("test");
                                            }
                                            TestPos.getTransform().clearTransform();
                                            TestPos.getTransform().setTranslation(rspPos.x, rspPos.y, 1);
                                            TestPos.getTransform().calcMatrix();
                                            TestPos.getExtentHelper().updateExtent();

                                            WataLog.i("check1!");
                                            testPosLayer.addElement("test", TestPos);
                                            if (bAdded)
                                                ((CompositeModel) o2map.getModel()).addModel(testPosLayer);
                                        } else if (TestPos != null && testPosLayer != null) {
                                            if (testPosLayer.size() > 0) {
                                                testPosLayer.removeElement("test");
                                            }
                                            TestPos.getTransform().clearTransform();
                                            TestPos.getTransform().setTranslation(rspPos.x, rspPos.y, 1);
                                            TestPos.getTransform().calcMatrix();
                                            TestPos.getExtentHelper().updateExtent();

                                            WataLog.i("check1!");
                                            testPosLayer.addElement("test", TestPos);
                                        }
//						        	Toast.makeText(GatheringActivity.this, rspPos.x+"/"+rspPos.y, 200).show();
                                        _omapView.requestRender();


                                        File logDir = new File(Environment.getExternalStorageDirectory().getPath() + "/gathering/log/");
                                        String sFreq;
                                        if (iFrequency <= 0)
                                            sFreq = "5GHz";
                                        else
                                            sFreq = "2.4GHz";
                                        if (!logDir.exists())
                                            logDir.mkdir();
                                        String logFile = Environment.getExternalStorageDirectory().getPath() + "/gathering/log/" + sFreq + "_" + StaticManager.address + "_" + StaticManager.floorName + "_" + _testTouchPoint + ".txt";

                                        if (nearBLE != null)
                                            logFile = Environment.getExternalStorageDirectory().getPath() + "/gathering/log/" + StaticManager.address + "_" + StaticManager.floorName + "_ble.txt";
                                        {
                                            Log.d("jeongyeol", "logging = " + pointCount);
                                            recordPoint(logFile, rspPos, filteredPos, null, viewVector, wifiList);
                                        }

                                    }
                                    bIsGetFP = false;
                                }
                            } else {
                                if (bLogTypeOnlySuc && bLogging) {
                                    bIsGetFP = false;
                                    Log.d("-------", "5");
                                    _wifiScanner._wifiManager.startScan();            // Logging : by Ethan
                                    break;
                                }
                                bIsGetFP = false;
                            }

                            break;
                    }

//                    if (msg.what == R.id.map_complete) //완료 버튼 눌렀을때
//                    {
//                        map_back.setVisibility(View.VISIBLE);
//                        map_bottom_linear1.setVisibility(View.VISIBLE);
//                        map_bottom_linear2.setVisibility(View.INVISIBLE);
////						break;
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    _isHandlerEnded = true;
                }
            }
        }

    }// end of Handler

    private SandboxModel twoPointerLayer = null, startPointModel = null, endPointModel = null, recordEndPointModel = null, PoiPointModel = null, PoiMessageModel = null, startSettingModel = null;

    protected void updateStart(Vector start) {

        O2Map o2map = (this._omapView.getO2mapInstance());

        o2map.renderLock();

        boolean bAdd = false;
        if (twoPointerLayer != null) {
            twoPointerLayer.removeAllElement();
            twoPointerLayer = null;
        }
        if (twoPointerLayer == null) {
            twoPointerLayer = new SandboxModel("pointer");
            bAdd = true;
        }

//		if(_startPointerLayer.size()>0) {
//			_startPointerLayer.removeElement(new String("pointer"));
//		}

        if (start != null) {
//			double pointerSize=25;
//			double pointerSize2=50;
//			Vector lookAt=o2map.getView().getCamera().getLookAtPosition();
//			Element el=ElementHelper.quadPyramidFromPoint(start.x,start.y,start.z,pointerSize,pointerSize,pointerSize2,Color.RED,Color.RED,Color.RED,false);
			/*Element el=ElementHelper.fromImage(IMAGE_SIZE,IMAGE_SIZE,Environment.getExternalStorageDirectory().getPath() + "/gathering/ico_map_start_point.png");
			el.getTransform().clearTransform();
//			el.getTransform().setTranslation(basePointX+start.x, basePointY+start.y, 10);
			el.getTransform().setTranslation(start.x, start.y, 10);
			el.getTransform().calcMatrix();
			el.getExtentHelper().updateExtent();*/
            float w = 4f;
            float h = 4f;

            if (StaticManager.folderName.equalsIgnoreCase("office")) {
                w = 1f;
                h = 1f;
            }
            WataLog.i("chekc");
            Element el = ElementHelper.fromTextBillboard(new Vector(start.x, start.y, 0.2, 0), w, h, true, "S", 20.0f, Typeface.DEFAULT_BOLD, new Color(44, 233, 41, 255));
            if (el != null)
                twoPointerLayer.addElement("pointerStart", el);
        }

        if (bAdd)
            ((CompositeModel) o2map.getModel()).addModel(twoPointerLayer);

        o2map.renderUnlock();
    }

    protected void updateEnd(Vector end) {

        O2Map o2map = (this._omapView.getO2mapInstance());

        o2map.renderLock();

        boolean bAdd = false;
        if (twoPointerLayer == null) {
            twoPointerLayer = new SandboxModel("pointer");
            bAdd = true;
        }

//		if(pointerLayer.size()>0) {
//			pointerLayer.removeElement(new String("pointer"));
//		}

        if (end != null) {
            //			double pointerSize=25;
            //			double pointerSize2=50;
            //			Vector lookAt=o2map.getView().getCamera().getLookAtPosition();
            //			Element el=ElementHelper.quadPyramidFromPoint(end.x,end.y,end.z,pointerSize,pointerSize,pointerSize2,Color.BLUE,Color.BLUE,Color.BLUE,false);
			/*Element el=ElementHelper.fromImage(IMAGE_SIZE,IMAGE_SIZE,Environment.getExternalStorageDirectory().getPath() + "/gathering/ico_map_destination.png");
			el.getTransform().clearTransform();
//			el.getTransform().setTranslation(basePointX+end.x, basePointY+end.y, 10);
			el.getTransform().setTranslation(end.x, end.y, 10);
			el.getTransform().calcMatrix();
			el.getExtentHelper().updateExtent();*/

            float w = 4f;
            float h = 4f;
            if (StaticManager.folderName.equalsIgnoreCase("office")) {
                w = 1f;
                h = 1f;
            }
            WataLog.i("chekc");
            Element el = ElementHelper.fromTextBillboard(new Vector(end.x, end.y, 0.2, 0), w, h, true, "G", 20.0f, Typeface.DEFAULT_BOLD, new Color(44, 233, 41, 255));

            twoPointerLayer.addElement("pointerEnd", el);
        }

        if (bAdd)
            ((CompositeModel) o2map.getModel()).addModel(twoPointerLayer);

        o2map.renderUnlock();
    }

    protected void updateVia(Vector via) {

        O2Map o2map = (this._omapView.getO2mapInstance());

        o2map.renderLock();

        boolean bAdd = false;
        if (twoPointerLayer == null) {
            twoPointerLayer = new SandboxModel("pointer");
            bAdd = true;
        }

//		if(pointerLayer.size()>0) {
//			pointerLayer.removeElement(new String("pointer"));
//		}

        if (via != null) {
            //			double pointerSize=25;
            //			double pointerSize2=50;
            //			com.geotwo.o2mapmobile.geometry.Vector lookAt=o2map.getView().getCamera().getLookAtPosition();
            //			Element el=ElementHelper.quadPyramidFromPoint(end.x,end.y,end.z,pointerSize,pointerSize,pointerSize2,Color.BLUE,Color.BLUE,Color.BLUE,false);
			/*Element el=ElementHelper.fromImage(IMAGE_SIZE,IMAGE_SIZE,Environment.getExternalStorageDirectory().getPath() + "/gathering/ico_map_destination.png");
			el.getTransform().clearTransform();
//			el.getTransform().setTranslation(basePointX+end.x, basePointY+end.y, 10);
			el.getTransform().setTranslation(end.x, end.y, 10);
			el.getTransform().calcMatrix();
			el.getExtentHelper().updateExtent();*/

            float w = 4f;
            float h = 4f;
            if (StaticManager.folderName.equalsIgnoreCase("office")) {
                w = 1f;
                h = 1f;
            }
            WataLog.i("chekc");
            Element el = ElementHelper.fromTextBillboard(new Vector(via.x, via.y, 0.2, 0), w, h, true, "V" + (multi.size() - 1), 200.0f, Typeface.DEFAULT, new Color(255, 0, 0, 255));


            twoPointerLayer.addElement("pointerEnd_" + multi.size(), el);
        }

        if (bAdd)
            ((CompositeModel) o2map.getModel()).addModel(twoPointerLayer);

        o2map.renderUnlock();
    }

    protected void updateRealPath(Vector start, Vector end) {

        O2Map o2map = (this._omapView.getO2mapInstance());

        o2map.renderLock();

        boolean bAdd = false;
        SandboxModel pointerLayer = null;
        pointerLayer = (SandboxModel) ((CompositeModel) o2map.getModel()).getModelByName("pointerReal");
        if (pointerLayer == null) {
            pointerLayer = new SandboxModel("pointerReal");
            bAdd = true;

        }

        if (pointerLayer.size() > 0) {
            pointerLayer.removeElement(new String("pointer1"));
            pointerLayer.removeElement(new String("pointer2"));
        }

        double pointerSize = 2.5;
        double pointerSize2 = 5;
        Element el = null;
        WataLog.i("check1!");
        el = ElementHelper.quadPyramidFromPoint(start.x, start.y, start.z, pointerSize, pointerSize, pointerSize2, Color.GREEN, Color.GREEN, Color.GREEN, false);
        pointerLayer.addElement("pointer1", el);
        WataLog.i("check1!");
        el = ElementHelper.quadPyramidFromPoint(end.x, end.y, end.z, pointerSize, pointerSize, pointerSize2, Color.GREEN, Color.GREEN, Color.GREEN, false);
        pointerLayer.addElement("pointer2", el);

        if (bAdd)
            ((CompositeModel) o2map.getModel()).addModel(pointerLayer);

        o2map.renderUnlock();

    }

    static public ArrayList<File> getGatheringPathData(String dataPath) {
        ArrayList<File> items = new ArrayList<File>();
        File pathFolder = new File(dataPath);
//    	Log.i("tag", "dataPath = "+dataPath);
        if (pathFolder.exists()) {
            String[] list = pathFolder.list();
//    		Log.i("tag", "list.length = "+list.length);
            if (list != null) {
                for (int a = 0; a < list.length; a++) {
                    File item = new File(dataPath + "/" + list[a]);
                    items.add(item);
//    				Log.i("tag", "dataPath+/+list[a] = "+dataPath+"/"+list[a]);
                }
            }
        }

        return items;
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

    private int pointCount = -1; //로그 줄번호 0번부터 시작한다
    private Node touchedNode;
    private Vector touchedPoint;
    DecimalFormat deciformat_WF = new DecimalFormat("#.##");

    private long durTime = -1;

    private void showResultDialog() {
        Log.d("whdrms", "gatherCnt=" + gatherCnt);
        if (gatherCnt <= 0)
            return;
        Log.d("ejcha", "var = " + var + " gatherCnt  = " + gatherCnt + " var/gatherCnt = " + (var / gatherCnt) + " durTime = " + (durTime / 1000));
        new AlertDialog.Builder(this)
                .setTitle("검증결과 (" + sCnt + ")")
//        .setMessage("소요시간 : "+(durTime/1000)+"초\n최소오차 : "+deciformat_WF.format(min)+"m\n최대오차 : "+deciformat_WF.format(max)+"m\n평균오차 : "+deciformat_WF.format(avg)+"m\n적중율 : "+(100*sucRate/30)+"%\n에러수 : "+errorCnt)
                .setMessage("소요시간 : " + (durTime / 1000) + "초\n최소오차 : " + deciformat_WF.format(min) + "m\n최대오차 : " + deciformat_WF.format(max) + "m\n평균오차 : " + deciformat_WF.format(avg)
                        + "m\n분산 : " + deciformat_WF.format(var / gatherCnt) + "\n표준편차 : " + deciformat_WF.format(Math.sqrt(var / gatherCnt))
                        + "\n신뢰구간 80% 기대값 : " + deciformat_WF.format(Math.sqrt(var / gatherCnt) * 1.285) + "\n적중율 : " + (100 * sucRate / gatherCnt) + "%\n총횟수(실패횟수) : " + sCnt + " (" + (sCnt - gatherCnt) + ")")
                .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
//				finish();
                        dialog.dismiss();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
//				finish();
                dialog.dismiss();
            }
        }).show();
    }

    //Tag : record
    private void recordPoint(String fileName, RspPosition pos_result, Vector filteredPos, Vector allPathMatch, Vector magPos, List<ScanResult> wifiList) {
        File file = new File(fileName);
        String s = null;
        String divider = "; ";

        if (pos_result != null && (pos_result.x == 0 || pos_result.y == 0)) {
            errorCnt++;
//        	return;
        }
//        if(pathFlag == 4 && !bServerFP)
//        {
//        	if(pos_result != null)
//        	{
//	        	pos_result.x = pos_result.x/10 + basePointX;
//	        	pos_result.y = pos_result.y/10 + basePointY;
//        	}
//
//        	if(filteredPos != null)
//        	{
//        		filteredPos = new Vector(filteredPos.x/10 + basePointX, filteredPos.y/10 + basePointY, 0);
//        	}
//        }
        Log.d("ejcha", "pointcnt = " + pointCount + " sCnt = " + sCnt);

        if (!bCal && pointCount == (sCnt + 1)) {
            touchedPoint = null;
            touchedNode = null;
            bLogging = false;
            startLog.setText("기록");
            pointCount = -1;
            durTime = SystemClock.uptimeMillis() - startTime;


            Toast.makeText(PathDrawingOldActivity.this, " 수집완료." + durTime / 1000 + "초/" + min + "m/" + max + "m/" + avg + "m/" + (100 * sucRate / 30) + "%", Toast.LENGTH_SHORT).show();
//		s = "gathering complete!!!!!!!!!!!!\n소요시간 : "+(durTime/1000)+"초\n최소오차 : "+min+"m\n최대오차 : "+deciformat_WF.format(max)+"m\n평균오차 : "+avg+"m\n적중율 : "+(100*sucRate/30)+"%\n------------------------------------------------------------";
//		s = "gathering complete!!!!!!!!!!!!\n소요시간 : "+(durTime/1000)+"초\n최소오차 : "+min+"m\n최대오차 : "+deciformat_WF.format(max)+"m\n평균오차 : "+deciformat_WF.format(avg)
//				+"m\n분산 : "+deciformat_WF.format(var/gatherCnt)+"\n표준편차 : "+deciformat_WF.format(Math.sqrt(var/gatherCnt))
//				+"\n신뢰구간 80% 기대값 : "+deciformat_WF.format(Math.sqrt(var/gatherCnt)*1.285)+"\n적중율 : "+(100*sucRate/gatherCnt)+"%\n총횟수(실패횟수) : "+sCnt+" ("+(sCnt-gatherCnt)+")"+"%\n------------------------------------------------------------";Log.d("whdrms","durTime="+durTime);s+= (durTime/1000)+divider;
            s = "---------------------------------------------------------------------\n";
            s += "구분자; 소요시간; 최소오차; 최대오차; 평균오차; 분산; 표준편차; 신뢰구간80% 기대값; 적중률; 총횟수(실패횟수)" + "\n";
            s += 9999 + divider;
            s += (durTime / 1000) + divider; //소요시간
            s += min + divider + deciformat_WF.format(max) + divider; //최소오차, 최대오차
            s += deciformat_WF.format(avg) + divider; //평균오차
            s += deciformat_WF.format(var / gatherCnt) + divider; //분산
            s += deciformat_WF.format(Math.sqrt(var / gatherCnt)) + divider; //표준편차
            s += deciformat_WF.format(Math.sqrt(var / gatherCnt) * 1.285) + divider; //신뢰구간 80% 기대값
            if (sucRate != 0) {
                s += 100 * sucRate / gatherCnt + divider;
            } else {
                s += 0 + divider;
            } //적중률
            s += sCnt + "(" + (sCnt - gatherCnt) + ")"; //총횟수(실패횟수)
            s += "\n---------------------------------------------------------------------";
            if (bUseStatistics)
                showResultDialog();

            Toast.makeText(getBaseContext(), "기록이 완료되었습니다.", Toast.LENGTH_LONG).show();
            showRes.setVisibility(View.VISIBLE);

            //currentPosLayer.removeAllElement();
            _omapView.getO2mapInstance().renderLock();
            testPosLayer.removeAllElement();
            _omapView.getO2mapInstance().renderUnlock();
            _omapView.requestRender();
        } else if (pointCount < 0)// && touchedNode != null)
        {
            s = "0; ";
            s = s + getTime() + divider + touchedNode.getUnderlyingPoint().x +
                    divider + touchedNode.getUnderlyingPoint().y +
                    divider + StaticManager.floorName + divider + " // touched point"
            ;
            touchedPoint = touchedNode.getUnderlyingPoint();
            pointCount = 0;
        } else if (pos_result != null) {
            s = getTime() + divider;
            double dist = Math.sqrt(Math.pow(pos_result.x - touchedPoint.x, 2) + Math.pow(pos_result.y - touchedPoint.y, 2));
            double vars = Math.pow(dist, 2);
            if (pos_result != null && (pos_result.x == 0 || pos_result.y == 0)) {
                Log.d("whdrms", "XY=0 -> 신뢰도 0.0 SET");
                dist = 0.0;
            }
            if (pos_result.strFloor != null)
                s = s + pos_result.x + divider + pos_result.y + divider + dist + divider + pos_result.strFloor.substring(0, 4) + divider;
            else
                s = s + pos_result.x + divider + pos_result.y + divider + dist + divider + "noFL" + divider;

            if (pos_result.x != 0 && pos_result.y != 0) {
                gatherCnt++;
                if (min == 0 || min > dist)
                    min = dist;

                if (max == 0 || max < dist)
                    max = dist;

                Log.d("ejcha", "dist : " + dist + " hitvalue : " + hitValue);
                if (dist <= hitValue)
                    sucRate++;

                if (mAf == null) {
                    double[][] tempMatrix = {{dist}, {0}};

                    Jama.Matrix input = new Jama.Matrix(tempMatrix);
                    //Log.d("jeongyeol", "AVG input x = "+input.get(0, 0)+"/ y = "+input.get(1, 0));

                    mAf = new AvgFilter();
                    mAf.init(input);
                } else {
                    double[][] tempMatrix = {{dist}, {0}};

                    Jama.Matrix input = new Jama.Matrix(tempMatrix);
                    //Log.d("jeongyeol", "Avg input x = "+input.get(0, 0)+"/ y = "+input.get(1, 0));
                    Jama.Matrix output = mAf.doUpdate(input);
                    if (output != null) {
                        //Log.d("jeongyeol", "Avg output x = "+output.get(0, 0)+"/ y = "+output.get(1, 0)+"/"+output.getArray().length+"/"+output.getArray()[0].length);

                        avg = output.get(0, 0);
                    }
                }
                var = var + vars;
                Log.d("ejcha", "dist = " + dist + " avg = " + avg + " var = " + var);
            }

            if (filteredPos != null) {
                s = s + filteredPos.x + divider + filteredPos.y + divider + Math.sqrt(Math.pow(filteredPos.x - touchedPoint.x, 2) + Math.pow(filteredPos.y - touchedPoint.y, 2)) + divider;
            }

            if (allPathMatch != null) {
                s = s + allPathMatch.x + divider + allPathMatch.y + divider + Math.sqrt(Math.pow(allPathMatch.x - touchedPoint.x, 2) + Math.pow(allPathMatch.y - touchedPoint.y, 2)) + divider;
            }

            if (magPos != null) {
                s = s + magPos.x + divider + magPos.y + divider + Math.sqrt(Math.pow(magPos.x - touchedPoint.x, 2) + Math.pow(magPos.y - touchedPoint.y, 2)) + divider;
                if (currPos == null || currentPosLayer == null) {
                    currPos = ElementHelper.quadPyramidFromPoint(0, 0, 0, 2.5, 2.5, 2.5 * 2, Color.BLUE, Color.BLUE, Color.BLUE, false);

                    O2Map o2map = (_omapView.getO2mapInstance());

                    boolean bAdded = false;

                    //currentPosLayer = (SandboxModel) ((CompositeModel)o2map.getModel()).getModelByName("currentPos");

                    if (currentPosLayer == null) {
                        currentPosLayer = new SandboxModel("currentPos");
                        bAdded = true;

                    }

//    					        		Vector lookAt=o2map.getView().getCamera().getLookAtPosition();
                    o2map.renderLock();
                    if (currentPosLayer.size() > 0) {
                        currentPosLayer.removeElement("curr");
                    }
                    currPos.getTransform().clearTransform();
                    currPos.getTransform().setTranslation(magPos.x, magPos.y, 1);
                    currPos.getTransform().calcMatrix();
                    currPos.getExtentHelper().updateExtent();
                    currentPosLayer.addElement("curr", currPos);
                    if (bAdded)
                        ((CompositeModel) o2map.getModel()).addModel(currentPosLayer);
                    o2map.renderUnlock();
                } else if (currPos != null && currentPosLayer != null) {
                    O2Map o2map = (_omapView.getO2mapInstance());
                    o2map.renderLock();
                    currPos.getTransform().clearTransform();
                    currPos.getTransform().setTranslation(magPos.x, magPos.y, 1);
                    currPos.getTransform().calcMatrix();
                    currPos.getExtentHelper().updateExtent();
                    o2map.renderUnlock();
                }
                _omapView.requestRender();
            }

            s = (pointCount) + divider + s;
            String mInfo = "";
            if (wifiList != null) {
                for (int i = 0; i < wifiList.size(); i++) {
                    String MACaddr
                            = wifiList.get(i).BSSID.substring(0, 2)
                            + wifiList.get(i).BSSID.substring(3, 5)
                            + wifiList.get(i).BSSID.substring(6, 8)
                            + wifiList.get(i).BSSID.substring(9, 11)
                            + wifiList.get(i).BSSID.substring(12, 14)
                            + wifiList.get(i).BSSID.substring(15, 17);
                    String SSID = wifiList.get(i).SSID;
                    int RSSI = wifiList.get(i).level;
                    int Freq = wifiList.get(i).frequency;
                    //Log.d("jeongyeol", "wifi ssid = "+SSID+"/ rssi = "+RSSI+"/ freq = "+Freq);
                    mInfo = SSID + "," + MACaddr + "," + RSSI + "," + Freq + divider;
                    if (iFrequency <= 0) {
                        s = s + mInfo;
                    } else {
                        if (Freq < 3000)
                            s = s + mInfo;
                    }
                }
                //pointCount++;
                Toast.makeText(PathDrawingOldActivity.this, (pointCount) + " 포인트 수집.", Toast.LENGTH_SHORT).show();
            }
        }
        if (fileName.length() == 0) {
            try {
                FileWriter file_writer = new FileWriter(file);

                BufferedWriter out = new BufferedWriter(file_writer);
                @SuppressWarnings("resource")
                PrintWriter print_writer = new PrintWriter(out, true);

                print_writer.println(s);
//                  if(print_writer.checkError())
//                  {
//                	  System.out.println("print_writer error!!");
//                  }
                try {
                    if (out != null)
                        out.close();
                    if (file_writer != null)
                        file_writer.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                file.createNewFile();
                file.setLastModified(System.currentTimeMillis());
            } catch (IOException e) {
                System.err.println(e); // 에러가 있다면 메시지 출력
                System.exit(1);
            }
        } else   //이어쓰기
        {
            BufferedWriter buff_writer = null;
            try {
                FileWriter file_writer = new FileWriter(file, true);
                buff_writer = new BufferedWriter(file_writer);
                PrintWriter print_writer = new PrintWriter(buff_writer, true);

                print_writer.println(s);
                if (buff_writer != null)
                    buff_writer.close();
                if (file_writer != null)
                    file_writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            if(print_writer.checkError())
//            {
//                   System.out.println("print_writer error!!");
//            }
            //System.out.println("이어쓰기 성공?!ㅋ");
        }

        if (pointCount >= 0)
            pointCount++;
    }

    public String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd_HHmmss", Locale.KOREA);
        Date date = new Date();
        String regDate = format.format(date);
        return regDate;
    }

    private void simulateWithCalibaration(int mode) {
        String logName = Environment.getExternalStorageDirectory().getPath() + "/gathering/log/" + "geo2.txt";
        StringBuffer strBuf = null;
        String[] lineArr = null;
        File simulFP = new File(logName);

        int mFileLine = 0;

        if (simulFP.exists()) {
            bCal = true;
            if (posProvider == null) {
                String dbFileName = Environment.getExternalStorageDirectory().getPath() + "/gathering/area/"
                        + StaticManager.folderName + "/FP.db";

                File geo2DB = new File(dbFileName);
                if (!geo2DB.exists()) {
                    Toast.makeText(PathDrawingOldActivity.this, "db 없음", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        posProvider = new PositionProvider(dbFileName, "", "");
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }

            if (strBuf == null) {
                strBuf = new StringBuffer();
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(simulFP));
                    String str = "";
                    while ((str = br.readLine()) != null) {
                        strBuf.append(str);
                        strBuf.append("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                lineArr = strBuf.toString().split("\n");
            }

            if (lineArr != null) {
                while (mFileLine < lineArr.length) {
                    String[] temp;

                    temp = lineArr[mFileLine].split(";");

                    if (temp.length >= 3) {
                        if (!temp[0].trim().equalsIgnoreCase("0")) {
//							double logX = Double.valueOf(temp[4].trim());//x
//							double logY = Double.valueOf(temp[5].trim());//y
//							String logFloor = temp[7].trim();//f

                            touchedPoint = new Vector(Double.valueOf(temp[2].trim()), Double.valueOf(temp[3].trim()), 0);

                            if (temp.length - 17 > 0) {
                                int apCnt = temp.length - 17;
                                //Log.d("jeongyeol", "ap count = "+apCnt);
                                ArrayList<GEO2.LBSP.Client.PDI_SCANINFO> lstScanInfo = new ArrayList<GEO2.LBSP.Client.PDI_SCANINFO>();

                                for (int i = 0; i < apCnt; i++) {
                                    GEO2.LBSP.Client.PDI_SCANINFO scandata = new GEO2.LBSP.Client.PDI_SCANINFO();

                                    String[] item = temp[17 + i].split(",");
                                    if (item.length == 4) {
                                        scandata.FD1_INFRA_TYPE = PDI_SCANINFO.DEF_INFRA_TYPE_WiFi;
                                        scandata.FD2_INFRA_ID = item[1].trim();

                                        int pre_Rssi = 0;
                                        pre_Rssi = Integer.valueOf(item[2].trim());
                                        Iron2Calibrator cal = new Iron2Calibrator();

                                        if (pre_Rssi != 0) {
//											Log.d("jeongyeol", "ssid="+item[0].trim()+"/prev rssi = "+pre_Rssi);
                                            pre_Rssi = (int) cal.calibrate(pre_Rssi, mode);
//											Log.d("jeongyeol", "ssid="+item[0].trim()+"/curr rssi = "+pre_Rssi);
                                        }
                                        scandata.FD3_RSSI = (byte) Byte.valueOf(String.valueOf(pre_Rssi));
                                        scandata.FD4_FREQ = (short) Short.valueOf(item[3].trim());
                                        scandata.FD5_ENCRYPTION = 0;
                                        scandata.FD6_SSID_LEN = (short) item[0].trim().length();
                                        scandata.FD7_SSID = item[0].trim();
                                        lstScanInfo.add(scandata);
                                        Log.d("jeongyeol", "ap item " + i + " = " + scandata.FD2_INFRA_ID + "/" + scandata.FD3_RSSI + "/" + scandata.FD4_FREQ + "/" + scandata.FD7_SSID);
                                    }
                                }
                                Log.d("jeongyeol", "-----------------------------" + mFileLine);
                                if (lstScanInfo != null && lstScanInfo.size() > 0 && posProvider != null) {
                                    PDI_RSP_POS pdiRspPos = null;
                                    try {
                                        pdiRspPos = posProvider.getLocationA(lstScanInfo);
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }

                                    if (pdiRspPos != null) {
                                        pdiRspPos.x = pdiRspPos.x / 10 + basePointX;
                                        pdiRspPos.y = pdiRspPos.y / 10 + basePointY;
                                        Log.d("jeongyeol", "err = " + pdiRspPos.errCode);
//										Log.d("jeongyeol", "x matching ----------- "+pdiRspPos.x+"/"+logX+"/"+((int)pdiRspPos.x == logX));
//										Log.d("jeongyeol", "y matching ----------- "+pdiRspPos.y+"/"+logY+"/"+((int)pdiRspPos.y == logY));
//										Log.d("jeongyeol", "f matching ----------- "+pdiRspPos.strFloor+"/"+logFloor+"/"+pdiRspPos.strFloor.equalsIgnoreCase(logFloor));

//							        	if(false)
                                        {
//											TransformGeo Tg = new TransformGeo(6378137.0, 298.257223563, 1.0, 38.0, 127.0, 600000.0, 200000.0);
//									        Vector currPosTM = Tg.transformGP2TM(pdiRspPos.y, pdiRspPos.x);
//									        pdiRspPos.x = currPosTM.x;
//									        pdiRspPos.y = currPosTM.y;

                                            Vector filteredPos = null;
                                            Vector viewVector = null;
                                            Vector allPathMatch = null;
//											if(m_pdrinter != null)
//											{
//												if(!bkistPDRInited)
//												{
//													m_pdrinter.refer_set_pedestrian_x_coordinate(pdiRspPos.x);
//													m_pdrinter.refer_set_pedestrian_y_coordinate(pdiRspPos.y);
//													bkistPDRInited = true;
//												}
//												else
//													m_pdrinter.refer_kf_meas_updata((int)pdiRspPos.x, (int)pdiRspPos.y);
//												filteredPos = new Vector(m_pdrinter.refer_get_pedestrian_x_coordinate(), m_pdrinter.refer_get_pedestrian_y_coordinate(), 0);
//												Log.d("jeongyeol", "kist x="+filteredPos.x+"/y="+filteredPos.y);
//											}
                                            if (mKf == null) {
                                                double[][] tempMatrix = {{pdiRspPos.x}, {8}, {pdiRspPos.y}, {8}};
                                                Jama.Matrix input = new Jama.Matrix(tempMatrix);
                                                Log.d("jeongyeol", "kf init x = " + input.get(0, 0) + "/ y = " + input.get(2, 0));
                                                mKf = new KalmanFilter();
                                                mKf.init(input, null, 1);
                                                filteredPos = new Vector(pdiRspPos.x, pdiRspPos.y, 0);
//												filteredPos.strFloor = rspPos.strFloor;

                                                Log.d("jeongyeol", "2919 -- kalman = " + filteredPos.x + "/" + filteredPos.y);
//												if(android.os.Build.BRAND != null && android.os.Build.BRAND.equalsIgnoreCase("samsung") && android.os.Build.MODEL != null && android.os.Build.MODEL.equalsIgnoreCase("SM-P600"))
//													viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[2]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0] - 90)));
//												else
//													viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[1]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0])));
//				//								bIsAccessToSensor = false;
//												viewVector = new Vector(viewVector.getX(),viewVector.getY(),0,0).normalize3();
//												viewVector = viewVector.multiply3(steplenthestimation.present_acc_variance_value/10);
//
//												viewVector = new Vector(filteredPos.x, filteredPos.y, 0).add3(viewVector);
//
//												if(_pathManager != null)
//												{
//													allPathMatch = _pathManager.findNearestPointOnPath(viewVector);
//												}
//
//												if(_pathManager_for_col != null)
//												{
//													Vector matched = _pathManager_for_col.findNearestPointOnPath(viewVector);
//													if(matched != null)
//													{
//														viewVector = matched;
//														Log.d("jeongyeol", "2919 -- map Mapching = "+matched.x+"/"+matched.y);
//													}
//												}
                                            } else {
                                                double[][] tempMatrix = {{pdiRspPos.x}, {pdiRspPos.y}};
                                                Jama.Matrix input = new Jama.Matrix(tempMatrix);
                                                Log.d("jeongyeol", "kf update x = " + input.get(0, 0) + "/ y = " + input.get(1, 0));
                                                Jama.Matrix output = mKf.doUpdate(input);
                                                if (output != null) {
                                                    filteredPos = new Vector(output.get(0, 0), output.get(2, 0), 0);
//													filteredPos.strFloor = rspPos.strFloor;
                                                    Log.d("jeongyeol", "2919 -- kalman = " + filteredPos.x + "/" + filteredPos.y);
//													if(android.os.Build.BRA./ND != null && android.os.Build.BRAND.equalsIgnoreCase("samsung") && android.os.Build.MODEL != null && android.os.Build.MODEL.equalsIgnoreCase("SM-P600"))
//														viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[2]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0] - 90)));
//													else
//														viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[1]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0])));
//					//								bIsAccessToSensor = false;
//													viewVector = new Vector(viewVector.getX(),viewVector.getY(),0,0).normalize3();
//													viewVector = viewVector.multiply3(steplenthestimation.present_acc_variance_value/10);
//
//													viewVector = new Vector(filteredPos.x, filteredPos.y, 0).add3(viewVector);
//
//										        	Log.d("jeongyeol", "2919 -- orientation = "+viewVector.x+"/"+viewVector.y);
//
//										        	if(_pathManager != null)
//													{
//														allPathMatch = _pathManager.findNearestPointOnPath(viewVector);
//													}
//
//													if(_pathManager_for_col != null)
//													{
//														Vector matched = _pathManager_for_col.findNearestPointOnPath(viewVector);
//														if(matched != null)
//														{
//															viewVector = matched;
//															Log.d("jeongyeol", "2919 -- map Mapching = "+matched.x+"/"+matched.y);
//														}
//													}
                                                }
                                            }
//											if(bLogging && touchedPoint != null)
                                            {
                                                //							if(rspPos.GID != null && StaticManager.gid != null && !rspPos.GID.equalsIgnoreCase(StaticManager.gid))
                                                //							{
                                                //								bIsGetFP = false;
                                                //								return;
                                                //							}
                                                //							rspPos.x = rspPos.x/10;
                                                //							rspPos.y = rspPos.y/10;

                                                File logDir = new File(Environment.getExternalStorageDirectory().getPath() + "/gathering/log/");
                                                if (!logDir.exists())
                                                    logDir.mkdir();
                                                String logFile = Environment.getExternalStorageDirectory().getPath() + "/gathering/log/" + StaticManager.address + "_" + StaticManager.floorName + "_calibration_with_" + mode + "_under_60.txt";
                                                if (pdiRspPos != null) {
                                                    RspPosition rspPos = new RspPosition();
                                                    rspPos.x = pdiRspPos.x;
                                                    rspPos.y = pdiRspPos.y;
                                                    rspPos.strFloor = pdiRspPos.strFloor;
                                                    recordPoint(logFile, rspPos, filteredPos, allPathMatch, viewVector, null);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    mFileLine++;
                }
                String res = "최소오차 : " + min + "m\n최대오차 : " + deciformat_WF.format(max) + "m\n평균오차 : " + avg + "m\n------------------------------------------------------------";
                String logFile = Environment.getExternalStorageDirectory().getPath() + "/gathering/log/" + StaticManager.address + "_" + StaticManager.floorName + "_calibration_with_" + mode + "_under_60.txt";

                BufferedWriter buff_writer = null;
                try {
                    FileWriter file_writer = new FileWriter(new File(logFile), true);
                    buff_writer = new BufferedWriter(file_writer);
                    PrintWriter print_writer = new PrintWriter(buff_writer, true);

                    print_writer.println(res);
                    if (buff_writer != null)
                        buff_writer.close();
                    if (file_writer != null)
                        file_writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("jeongyeol", "최소오차 : " + min + "m\n최대오차 : " + deciformat_WF.format(max) + "m\n평균오차 : " + avg + "m\n------------------------------------------------------------");

            }
            bCal = false;
        }
    }

    private void rotateLog(double basex, double basey, double angle) {
        ArrayList<File> logs = getFile(Environment.getExternalStorageDirectory().getPath() + "/gathering/log/", "txt");
        for (int k = 0; k < logs.size(); k++) {
            File simulFP = logs.get(k);

            int mFileLine = 0;

            if (simulFP.exists()) {
                StringBuffer strBuf = null;
                String[] lineArr = null;
                if (strBuf == null) {
                    strBuf = new StringBuffer();
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new FileReader(simulFP));
                        String str = "";
                        while ((str = br.readLine()) != null) {
                            strBuf.append(str);
                            strBuf.append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                    lineArr = strBuf.toString().split("\n");
                }

                String logFile = Environment.getExternalStorageDirectory().getPath() + "/gathering/log/rotate/" + simulFP.getName().substring(0, simulFP.getName().indexOf(".")) + "_" + angle + "_shift.txt";
                File logDir = new File(Environment.getExternalStorageDirectory().getPath() + "/gathering/log/rotate/");
                if (!logDir.exists())
                    logDir.mkdir();

                String divider = "; ";

                if (lineArr != null) {
                    while (mFileLine < lineArr.length) {
                        String[] temp;

                        temp = lineArr[mFileLine].split(";");

                        if (temp.length >= 3) {
                            //						if(!temp[0].trim().equalsIgnoreCase("0"))
                            {
                                String s = "";

                                double logX = 0;
                                for (int i = 0; i < temp.length; i++) {
                                    if (i == 2) {
                                        logX = 0;
                                        logX = Double.valueOf(temp[2].trim());//x
                                        logX = logX - basex;
                                        //									s = s+String.valueOf(logX)+divider;
                                    } else if (i == 3) {
                                        double logY = Double.valueOf(temp[3].trim());//y
                                        logY = logY - basey;
                                        //									s = s+String.valueOf(logY)+divider;

                                        Vector org = new Vector(logX, logY, 0);
                                        Log.d("jeongyeol", "X=" + org.x + "/Y=" + org.y);
                                        Quaternion q = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, Angle.fromDegrees(angle));
                                        org = q.transform(org);

                                        org = org.add3(basex, basey, 0);

                                        Log.d("jeongyeol", "rX=" + org.x + "/rY=" + org.y);
                                        s = s + String.valueOf(org.x) + divider + String.valueOf(org.y) + divider;
                                    } else
                                        s = s + temp[i] + divider;
                                }
                                BufferedWriter buff_writer = null;
                                try {
                                    FileWriter file_writer = new FileWriter(new File(logFile), true);
                                    buff_writer = new BufferedWriter(file_writer);
                                    PrintWriter print_writer = new PrintWriter(buff_writer, true);

                                    print_writer.println(s);
                                    if (buff_writer != null)
                                        buff_writer.close();
                                    if (file_writer != null)
                                        file_writer.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        mFileLine++;
                    }
                }
            }
        }
    }

    static public ArrayList<File> getFile(String dataPath, String ext) {
        ArrayList<File> items = new ArrayList<File>();
        File pathFolder = new File(dataPath);
        //Log.i("jeongyeol", "dataPath = "+dataPath);
        if (pathFolder.exists()) {
            String[] list = pathFolder.list();
            //Log.i("jeongyeol", "list.length = "+list.length);
            if (list != null) {
                for (int a = 0; a < list.length; a++) {
                    //Log.d("jeongyeol", "ext ="+list[a].substring(list[a].indexOf(".")+1, list[a].length())+"/");
                    if (list[a].length() > 5 && (list[a].substring(list[a].indexOf(".") + 1, list[a].length())).equalsIgnoreCase(ext)) {
                        //Log.d("jeongyeol", "filename ="+list[a]);
                        File item = new File(dataPath + "/" + list[a]);
                        items.add(item);
                    }
                }
            }
        }
        return items;
    }

    private BLEMap findBLE(String macAddr) {
        BLEMap res = null;
        for (BLEMap ble : bleList) {
            if (ble.macAddr != null && ble.macAddr.equalsIgnoreCase(macAddr)) {
                res = ble;
                break;
            }
        }
        return res;
    }

    //Tag : 비콘
    private void startEstimote() {
        mBle = new TextView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        mBle.setLayoutParams(params);

        mBle.setText("BLE");
        mBle.setTextColor(0);

        if (beaconManager == null)
            beaconManager = new BeaconManager(this);

        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(1), 500);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            public void onServiceReady() {
                try {
                    if (beaconManager != null)
                        beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
//			            Toast.makeText(ListBeaconsActivity.this, "Cannot start ranging, something terrible happened",
//			                Toast.LENGTH_LONG).show();
//			            Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });
        if (nearBLE == null)
            nearBLE = new ArrayList();
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {

            public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
                // Note that results are not delivered on UI thread.
                runOnUiThread(new Runnable() {

                    public void run() {
                        // Note that beacons reported here are already sorted by estimated
                        // distance between device and beacon.
                        try {
                            boolean isAlreadyListed = false;

                            for (Beacon target : beacons) {
                                for (e_BLEScandata e : beacons_L) {
                                    if (e.MACaddr.compareToIgnoreCase(target.getMacAddress()) == 0) {
                                        e.isUpdated = true;
                                        e.RSSI = target.getRssi();
                                        e.LastScanTime = System.currentTimeMillis();// / 1000;
                                        isAlreadyListed = true;
                                        break;
                                    }
                                }
                                if (isAlreadyListed == false) {
                                    e_BLEScandata newBeacon = new e_BLEScandata();

                                    newBeacon.isUpdated = true;
                                    newBeacon.SSID = target.getName();
                                    newBeacon.MACaddr = target.getMacAddress();
                                    newBeacon.RSSI = target.getRssi();
                                    newBeacon.LastScanTime = System.currentTimeMillis();// / 1000;

                                    beacons_L.add(newBeacon);
                                }
                            }


//	            	   Collections.sort ( beacons_L, e_BLEScandata.compareRSSI );

                            String BLEList = "";
//		            	   Log.d("BLE!! ", "# of scanned beacons= " + beacons_L.size() );
//	            	   Log.d("BLE!! ", "ble list -------------------------------------------------------------");
                            for (e_BLEScandata e : beacons_L) {
//		            		   String str = "SSID= " + e.SSID;
//		            		   str += "  - MAC Address= " + e.MACaddr;
//		            		   str += "  - RSSI= " + e.RSSI;
//
//		            		   str += "  - UUID= " + e.UUID + lineEnd;
//		            		   str += "  - Distance= " + PrintD.df(e.Distance) + "m" + lineEnd;
//		            		   str += "  - MeasuredPower= " + e.MeasuredPower + lineEnd;
//		            		   str += "  - Major= " + e.Major + " / Minor= " + e.Minor + lineEnd;
//		            		   str += "  - Proximity= " + e.Proximity + lineEnd;
//
//		            		   LogDisp.append ( str );
//		            		   flushToLogStr(e);
//	            		   Log.d("BLE!! ", "ble scan " + e.MACaddr+"/"+e.RSSI);

                                if (e.MACaddr != null)// && (e.MACaddr.endsWith("E1") || e.MACaddr.endsWith("4E")))
                                {
                                    BLEMap temp = findBLE(e.MACaddr);

                                    if (temp != null) {
                                        if (e.RSSI > -65) {
                                            if (e.mCnt > 2) {
                                                if (!nearBLE.contains(e)) {
                                                    nearBLE.add(e);
                                                }
                                            } else
                                                e.mCnt++;
                                        } else if (e.RSSI < -70) {
                                            if (e.mCnt == 0) {
                                                if (nearBLE.contains(e)) {
                                                    if (lastBLE != null && lastBLE.equalsIgnoreCase(e.MACaddr)) {
                                                        lastBLE = null;
                                                        targetBLE = null;
                                                    }
                                                    nearBLE.remove(e);
                                                }
                                            } else
                                                e.mCnt--;
                                        }
                                    }
                                }
//		            			Toast.makeText(IndoorNaviActivity.this, "BLE "+e.MACaddr+" is checked in", 200).show();
                            }

                            String nearestBLE = "";
                            for (e_BLEScandata e : nearBLE) {
                                if (SystemClock.currentThreadTimeMillis() - e.LastScanTime > 10 * 1000) {
                                    if (lastBLE != null & lastBLE.equalsIgnoreCase(e.MACaddr)) {
                                        lastBLE = null;
                                        targetBLE = null;
                                    }
                                    if (m2ndBLE != null & m2ndBLE.equalsIgnoreCase(e.MACaddr)) {
                                        m2ndBLE = null;
                                        m2ndTarget = null;
                                    }
                                    nearBLE.remove(e);
                                    Log.d("jeongyeol", "no ble");
                                } else
                                    BLEList = BLEList + "BLE " + e.MACaddr + " is checked in\n";
//	            		   BLEList = BLEList+"BLE "+e.MACaddr+"/"+e.RSSI+"\n";
                                Log.d("jeongyeol", BLEList);
                            }
                            if (nearBLE != null && nearBLE.size() > 0) {
                                Collections.sort(nearBLE, e_BLEScandata.compareRSSI);
//		            	   if(nearBLE.size() > 1)
//		            	   {
//		            		   for(e_BLEScandata e : nearBLE)
//		            		   {
//		            			   if(e.MACaddr != null && (e.MACaddr.endsWith("E1") || e.MACaddr.endsWith("4E")))
//		            			   {
//		            				   nearestBLE = e.MACaddr;
//		            				   break;
//		            			   }
//		            		   }
//		            	   }
//		            	   else
                                nearestBLE = nearBLE.get(0).MACaddr;
                                m2ndBLE = null;
                                m2ndRssi = 0;
                                m2ndTarget = null;
                            } else if (beacons_L != null && beacons_L.size() > 0) {
                                if (beacons_L.get(0).RSSI > -80) {
                                    Collections.sort(beacons_L, e_BLEScandata.compareRSSI);
//		            		   for(e_BLEScandata e : beacons_L)
//		            		   {
//		            			   if(e.MACaddr != null && (e.MACaddr.endsWith("E1") || e.MACaddr.endsWith("4E")))
//		            			   {
//		            				   m2ndBLE = e.MACaddr;
//		            				   m2ndRssi = e.RSSI;
//		            				   break;
//		            			   }
//		            		   }
                                    m2ndBLE = beacons_L.get(0).MACaddr;
                                    m2ndRssi = beacons_L.get(0).RSSI;

                                    BLEMap temp = findBLE(m2ndBLE);
                                    if (temp != null) {
//		            			   int fIdx = getRawFloorIndex(temp.floor);

//		            			   if(fIdx >= 0)
                                        m2ndTarget = new Vector(temp.location[0], temp.location[1], 0);
//		            			   else
//			            		   {
//				            		   m2ndBLE = null;
//				            		   m2ndRssi = 0;
//				            		   m2ndTarget = null;
//				            	   }
                                    } else {
                                        m2ndBLE = null;
                                        m2ndRssi = 0;
                                        m2ndTarget = null;
                                    }
                                } else {
                                    m2ndBLE = null;
                                    m2ndRssi = 0;
                                    m2ndTarget = null;
                                }
                            } else {
                                m2ndBLE = null;
                                m2ndRssi = 0;
                                m2ndTarget = null;
                            }
//		            	   mBle.setText(BLEList);

                            if (nearestBLE != null && !nearestBLE.equalsIgnoreCase("") && (lastBLE == null || !lastBLE.equalsIgnoreCase(nearestBLE))) {
                                BLEMap temp = findBLE(nearestBLE);
                                if (temp != null) {
                                    if (!bFilterProc) {
                                        bFilterProc = true;
//		    						   if(mKf != null)
//		    						   {
//			    						   mKf.init(null, null, 1);
//			    						   mKf = null;
//			    						   double[][] tempMatrix = null;
//		    							   if(nearestBLE.endsWith("E1"))
//		    							   {
//		    								   double[][] temp = {{gate9_10.x}, {8}, {gate9_10.y}, {8}};
//		    								   tempMatrix = temp;
//		    								   targetBLE = gate9_10;
//		    							   }
//		    							   else if(nearestBLE.endsWith("4E"))
//		    							   {
//		    								   double[][] temp = {{gate11_12.x}, {8}, {gate11_12.y}, {8}};
//		    								   tempMatrix = temp;
//		    								   targetBLE = gate11_12;
//		    							   }
//		    							   else
//		    							   {
//		    								   targetBLE = null;
//		    			            		   lastBLE = null;
//		    			            		   bFilterProc = false;
//		    			            		   return;
//		    							   }
//		    							   Jama.Matrix input = new Jama.Matrix(tempMatrix);
//		        						   mKf = new KalmanFilter();
//		        						   mKf.init(input, null, 1);
//		        						   lastBLE = nearestBLE;
//		    						   }
//		    						   else
                                        {
//	    							   int fIdx = getRawFloorIndex(temp.floor);

//			            			   if(fIdx >= 0)
                                            {
                                                lastBLE = nearestBLE;
                                                targetBLE = new Vector(temp.location[0], temp.location[1], 0);
                                            }
//	    							   else
//	    							   {
//	    								   targetBLE = null;
//	    			            		   lastBLE = null;
////	    			            		   bFilterProc = false;
////	    			            		   mBle.setText(BLEList);
////	    			            		   return;
//	    							   }
                                            if (mBle != null) {
                                                BLEList = BLEList + "(" + lastBLE + "/" + m2ndBLE + "(" + m2ndRssi + ")" + ")";
                                                mBle.setText(BLEList);
                                            }
                                        }
                                        bFilterProc = false;
                                    }
                                }
                            } else if (mBle != null) {
                                BLEList = BLEList + "(" + lastBLE + "/" + m2ndBLE + "(" + m2ndRssi + ")" + ")";
                                mBle.setText(BLEList);
//		            		   targetBLE = null;
//		            		   lastBLE = null;
                            }
                        } catch (Exception e) {

                        }
                    }
                });
            }
        });
    }

    private TextView mBle = null;
    private ArrayList<e_BLEScandata> nearBLE;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<e_BLEScandata> beacons_L = new ArrayList<e_BLEScandata>();
    private BeaconManager beaconManager;
    private boolean bFilterProc = false;
    private final int REQUEST_ENABLE_BT = 0000;
    final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    ArrayList<BLEMap> bleList;
    //	final Vector_a gate9_10 = new Vector_a(198365.56875949266, 542066.7169102515, 7.62504768371582);
//	final Vector_a gate11_12 = new Vector_a(198406.01195768124, 542066.3126229655, 7.62504768371582);
    private Vector targetBLE = null, m2ndTarget = null;
    private String lastBLE = null, m2ndBLE = null;
    private int m2ndRssi = 0;

    private void parseMapXml(String XML_PATH) {
        FileInputStream xmlFis = null;
        File xmlFile = new File(XML_PATH);
        if (!xmlFile.exists())
            return;
        try {
            XmlPullParserFactory parserFac = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFac.newPullParser();
            xmlFis = new FileInputStream(xmlFile);
            parser.setInput(xmlFis, "UTF-8");
            int parserEvent = parser.getEventType();
            String tag = "";

            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG:
                        tag = parser.getName();
                        break;

                    case XmlPullParser.TEXT:
                        if (tag.equalsIgnoreCase("beacon")) {
                            if (Build.BRAND.equalsIgnoreCase("samsung")) {
                                String beacons = parser.getText();
                                if (beacons != null) {
                                    bleList = new ArrayList();
                                    String[] beacon = beacons.split(";");
                                    for (int i = 0; i < beacon.length; i++) {
                                        String[] div = beacon[i].trim().split("\\|");
                                        if (div.length == 4) {
                                            BLEMap ble = new BLEMap();
                                            ble.macAddr = div[0].trim();
                                            if (!ble.macAddr.contains(":"))
                                                ble.macAddr = ble.macAddr.substring(0, 2) + ":" + ble.macAddr.substring(2, 4) + ":" + ble.macAddr.substring(4, 6) + ":" + ble.macAddr.substring(6, 8) + ":" + ble.macAddr.substring(8, 10) + ":" + ble.macAddr.substring(10, 12);
                                            ble.location[0] = Double.valueOf(div[1].trim());
                                            ble.location[1] = Double.valueOf(div[2].trim());
                                            ble.floor = div[3].trim();
//										Log.d("jeongyeol", "mac="+ble.macAddr+"\nx="+ble.location[0]+"\ny="+ble.location[1]+"\nfloor="+ble.floor);
                                            bleList.add(ble);
                                        }
                                    }
                                }
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        tag = "";
                        break;
                }

                parserEvent = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (xmlFis != null)
                    xmlFis.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void animate(ImageView view, double fromDegrees, double toDegrees) {
        WataLog.d("fromDegrees=" + fromDegrees);
        WataLog.d("toDegrees=" + toDegrees);

        final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(0);
        rotate.setFillAfter(true);
        view.startAnimation(rotate);
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

    private Toast mToast = null;

    //    Gravity.CENTER
    private void onToast(int which, String message) {
        if (mToast == null) {
            mToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.setGravity(which, 0, 0);
        mToast.show();
    }


    private JSONObject mPoiListObject = new JSONObject();
    private JSONArray mPoiArray = new JSONArray();

    private void setPOILog(double x, double y, String poiName) {
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

            SaveGatheredData.saveData(StaticManager.getResultPOIPath(), "POI.json", poiJsonRecord);

        } catch (JSONException e) {
            WataLog.d("Failed to create JSONObject =" + e);
        }

//        SaveGatheredData.saveData(StaticManager.getResultPOIPath(), "POI.log", poiJson);
//        SaveGatheredData.saveData(StaticManager.getResultPOIPath(), "POI.temp", poiJson);
    }


    private JSONObject mLineListObject = new JSONObject();
    private JSONArray mLineArray = new JSONArray();

    private void setLineInfo(double startX, double startY, double endX, double endy) {
        String lineJson = "";
        String lineJsonRecord = "";
        try {
            final JSONObject lineObject = new JSONObject();
            lineObject.put("start_pointx", startX);
            lineObject.put("start_pointy", startY);
            lineObject.put("end_pointx", endX);
            lineObject.put("end_pointy", endy);

            lineJson = lineObject.toString();
            WataLog.d("lineJson=" + lineJson);

            mLineArray.put(lineJson);
            WataLog.d("mLineArray=" + mLineArray);
            for (int i = 0; i < mLineArray.length(); i++) {
                if (i > 0) {
                    lineJsonRecord += ",";
                }
                lineJsonRecord += mLineArray.get(i);
                WataLog.d("lineJsonRecord=" + lineJsonRecord);
            }

            lineJsonRecord = "{\"LINE_LIST\": [" + lineJsonRecord + "]}";
            WataLog.d("lineJsonRecord=" + lineJsonRecord);

            SaveGatheredData.saveData(StaticManager.getResultLinePath(), "LINE.json", lineJsonRecord);

        } catch (JSONException e) {
            WataLog.d("Failed to create JSONObject =" + e);
        }
    }

    private int mRecordLineCount = 0;

    // 기록된 라인 불려오기
    private void getRecordLine(double recordStartPointX, double recordStartPointY, double lastPointX, double lLastPointY) {
        WataLog.i("getRecordLine");
        Polyline line = new Polyline();
        line.makeParts(1);

        Points pts = new Points();
        pts.makePoints(2);

        pts.setPoint(0, recordStartPointX, recordStartPointY, 0.0f);
        pts.setPoint(1, lastPointX, lLastPointY, 0.0f);
        line.setParts(0, pts);

//        setForwardLine(line);

        if (myRecordFLine == null) {
            myRecordFLine = new SandboxModel("myRecordFLine");
        }

        Element eal = ElementHelper.fromPolyline(line, 0.1, 5, new Color(0, 0, 255, 255), false, false);
        myRecordFLine.addElement("myRecordFLine" + mRecordLineCount, eal);
        mRecordLineCount++;

        ((CompositeModel) o2map.getModel()).addModel(myRecordFLine);


    }

    private static final float MIN_SCALE = 0.85f;
    private static final float MIN_ALPHA = 0.5f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0f);

        } else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vertMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horzMargin - vertMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + vertMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            view.setAlpha(MIN_ALPHA +
                    (scaleFactor - MIN_SCALE) /
                            (1 - MIN_SCALE) * (1 - MIN_ALPHA));

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0f);
        }
    }

    private void getRecordPOI(double pointx, double pointy, String poiText) {
        WataLog.i("getRecordPOI");
        try {
            Element poiMessage = ElementHelper.fromTextBillboard(new Vector(pointx, pointy, 5.0, 0), 2f, 2f, true, poiText, 60.0f, Typeface.DEFAULT_BOLD, Color.BLACK);
            if (PoiMessageModel == null) {
                PoiMessageModel = new SandboxModel("PoiMessageModel");
            }
            if (poiMessage != null) {
                WataLog.i("mPoiCount==" + mPoiCount);
                PoiMessageModel.addElement("PoiMessageModel" + mPoiCount, poiMessage);
            }
            ((CompositeModel) o2map.getModel()).addModel(PoiMessageModel);
            _omapView.getO2mapInstance().requestRedraw();

            mPoiCount++;
            // poi내용저장
        } catch (Exception e) {
            WataLog.e("Exception=" + e.toString());
        }
    }


    //화면 캡쳐하기
//    public File ScreenShot(View view){
////        view.setDrawingCacheEnabled(true);  //화면에 뿌릴때 캐시를 사용하게 한다
////        Bitmap screenBitmap = view.getDrawingCache();   //캐시를 비트맵으로 변환
//
//        View v1 = getWindow().getDecorView().getRootView();
//        v1.setDrawingCacheEnabled(true);
//        Bitmap screenBitmap = Bitmap.createBitmap(v1.getDrawingCache());
//        v1.setDrawingCacheEnabled(false);
//
//
//        String filename = "screenshot.png";
//        File file = new File(Environment.getExternalStorageDirectory()+"/Pictures", filename);  //Pictures폴더 screenshot.png 파일
//        FileOutputStream os = null;
//        try{
//            os = new FileOutputStream(file);
//            screenBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);   //비트맵을 PNG파일로 변환
//            os.close();
//        }catch (IOException e){
//            e.printStackTrace();
//            return null;
//        }
//
//        view.setDrawingCacheEnabled(false);
//        return file;
//    }


    // =================================================================================================================
//    public O2MapInstance getO2Map() {
//        return (O2MapInstance) ((WMapSurfaceView) _omapView).getO2mapInstance();
//    }
//    public static double mDegrees = 0.0;
//
//    public class SpinnableImageView extends android.support.v7.widget.AppCompatImageView {
//        private double mCurrAngle = 0;
//        private double mPrevAngle = 0;
//        private double mAddAngle = 0;
//
//        public SpinnableImageView(Context context) {
//            super(context);
//        }
//
//        public SpinnableImageView(Context context, @Nullable AttributeSet attrs) {
//            super(context, attrs);
//        }
//
//        @Override
//        public boolean performClick() {
//            return super.performClick();
//        }
//
//        @Override
//        public boolean onTouchEvent(MotionEvent motionEvent) {
//            final float centerOfWidth = getWidth() / 2;
//            final float centerOfHeight = getHeight() / 2;
//            final float x = motionEvent.getX();
//            final float y = motionEvent.getY();
//
//            WataLog.d("motionEvent.getAction()=" + motionEvent.getAction());
//            switch (motionEvent.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    mCurrAngle = Math.toDegrees(Math.atan2(x - centerOfWidth, centerOfHeight - y));
//                    break;
//
//                case MotionEvent.ACTION_MOVE:
//                    mPrevAngle = mCurrAngle;
//                    mCurrAngle = Math.toDegrees(Math.atan2(x - centerOfWidth, centerOfHeight - y));
//                    animate(this, mAddAngle, mAddAngle + mCurrAngle - mPrevAngle);
//                    mAddAngle += mCurrAngle - mPrevAngle;
//                    break;
//
//                case MotionEvent.ACTION_UP:
//                    performClick();
//                    break;
//
//            }
//            return true;
//        }
//
//        private void animate(View view, double fromDegrees, double toDegrees) {
//            WataLog.d("fromDegrees=" + fromDegrees);
//            WataLog.d("toDegrees=" + toDegrees);
//            mDegrees = toDegrees;
//            final int degree = (int) toDegrees;
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    now_angle.setText(String.valueOf(degree));
//                }
//            });
//
//            final RotateAnimation rotate = new RotateAnimation((float) fromDegrees, (float) toDegrees,
//                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
//                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);
//            rotate.setDuration(0);
//            rotate.setFillAfter(true);
//            view.startAnimation(rotate);
//        }
//    }
}
