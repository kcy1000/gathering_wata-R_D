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
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.geotwo.LAB_TEST.Gathering.ui.SwipeAnimationButton;
import com.geotwo.LAB_TEST.Gathering.ui.SwipeAnimationListener;
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
import com.google.android.gms.maps.model.LatLng;
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

public class PathDrawingActivity extends AppCompatActivity implements OnClickListener, SensorEventListener, ViewPager.PageTransformer {
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

    public static int iFrequency = 0;  // 2 = 2.4GHz, 0 = 2.4 and 5 GHz

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
    private RelativeLayout record_lisetview, my_way_record_info_layout, poi_point_layout, poi_listview_rlayout, poi_edit_box_layout;
    private ListView mPathDrawingListview, mPoiListview;
    private Button save_point_check_btn, path_drawing_setting;
    private Polyline mLastPathPoint; //매회 최근경로
    private EditText poi_edit, poi_edit_text;
    private ProgressBar progress;
    private LinearLayout angle_layout, angle_control_layout;

    private int mWidthPixel = 0;
    private int mHeightPixels = 0;
    private int mRecordIndexNum = 0;
    public static int pathFlag = 0;

    private boolean ROAD_MODE = false;

    private String mapId = "-1";
    private LatLng mMyLatlng;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WataLog.i("onCreate");
        // load settings
        isMapRatation = PathDrawingSettingDialog.isMapRatation(this);

        WataLog.d("isMapRatation=" + isMapRatation);
        setBasePoint(); //Tag : base X, base Y Setting
        tff = Typeface.createFromAsset(this.getAssets(), "NanumBarunGothic.ttf"); //Font

        _dataCore = DataCore.getInstance();
        _dataCore.setFloorString(StaticManager.floorName);
        _dataCore.setBuildName(StaticManager.title);

        WataLog.d("StaticManager.floorName=" + StaticManager.floorName);
        WataLog.d("StaticManager.title=" + StaticManager.title);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        mWidthPixel = dm.widthPixels;
        mHeightPixels = dm.heightPixels;
        WataLog.d("mWidthPixel=" + mWidthPixel);
        WataLog.d("mHeightPixels=" + mHeightPixels);

//        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        Intent intent = getIntent();
        WataLog.i("intent=" + intent);
        if (intent != null) {
            String name = intent.getExtras().getString("file_name");
            SAVE_FILE_NAME = name;
            if (SAVE_FILE_NAME != null) {
                if (SAVE_FILE_NAME.length() > 5) {
                    SAVE_FILE_NAME.substring(5, SAVE_FILE_NAME.length() - 1);
                }
            }
            WataLog.d("SAVE_FILE_NAME=" + SAVE_FILE_NAME);
            ROAD_MODE = intent.getBooleanExtra("load_file", false);
            WataLog.d("ROAD_MODE=" + ROAD_MODE);

            double longitude = intent.getDoubleExtra("longitude", 0.0);
            double latitude = intent.getDoubleExtra("latitude", 0.0);

            WataLog.i("mMyLatlng=" + longitude + "////" + latitude);

            mMyLatlng = new LatLng(latitude, longitude);

            String imageName = intent.getExtras().getString("image_name");
            WataLog.d("imageName=" + imageName);
            mapId = intent.getExtras().getString("map_id");
            WataLog.d("mapId=" + mapId);
            if (!"-1".equals(mapId) && mapId != null) {
                myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/index.jsp?idx=" + mapId;
            } else if (imageName != null && !"".equals(imageName)) {
                myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/nomap.jsp?xpos=" + longitude + "&ypos=" + latitude + "&filepath=" + imageName;
            } else {
                myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/nomap.jsp?xpos=" + longitude + "&ypos=" + latitude;
            }

            WataLog.d("myUrl=" + myUrl);
        }

//        ROAD_MODE = true;
        if (ROAD_MODE) {
            try {
                initMap(); //Tag : Map이나 button 설정
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            initView(true);

            String lineJson = JsonUtils.INSTANCE.getData(Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pathGathering/PathDrawing/" + SAVE_FILE_NAME + "/LINE/" + "LINE.json");
            WataLog.d("SAVE_FILE_NAME=" + SAVE_FILE_NAME);
            if (lineJson != null) {
                lineJson = lineJson.replace("\\", "");
                WataLog.d("lineJson = " + lineJson);
//                setLineReader(lineJson);
                if (lineJson != null) {            // File is existed
                    WataLog.d("JSON Data : " + lineJson);
                    try {
                        JSONObject jObject = new JSONObject(lineJson);
                        WataLog.d("jObject= " + jObject);

                        JSONArray arrayPaths = jObject.getJSONArray("LINE_LIST");
                        WataLog.d("arrayPaths= " + arrayPaths);
                        WataLog.d("arrayPaths= " + arrayPaths);

//                        for (int i = 0; i < arrayPaths.length(); i++) {
//                            JSONObject jsonPath = arrayPaths.getJSONObject(i);
//                            latitude = jsonPath.getDouble("latitude");
//                            longitude = jsonPath.getDouble("longitude");
//                            WataLog.d("latitude=" + latitude);
//                            WataLog.d("longitude=" + longitude);
//                        }

                        JSONObject jsonPath = arrayPaths.getJSONObject(0);
                        latitude = jsonPath.getString("latitude");
                        longitude = jsonPath.getString("longitude");
                        WataLog.d("latitude=" + latitude);
                        WataLog.d("longitude=" + longitude);

                        if ("".equals(latitude) && !"".equals(longitude)) {
                            mapId = longitude;
                        }
                        // kcy1000 - 임시로  맵구별하기 위한 조치 / 추후 수정예정임
                        WataLog.d("mapId=" + mapId);

                        if ("-1".equals(mapId) || mapId == null) {
                            myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/nomap.jsp?xpos=" + longitude + "&ypos=" + latitude;
                        } else {
                            myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/index.jsp?idx=" + longitude;
                        }
                        WataLog.d("myUrl=" + myUrl);
                        initNewLayout();

                    } catch (Exception e) {
                        WataLog.d("Error in Reading: " + e.getLocalizedMessage());
                    }
                }
            }

            setMapInit();

            DataCore.iGatherMode = DataCore.GATHER_MODE_NONE;
        } else {
//            setMapSetting();
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

            initNewLayout();

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

            SwipeAnimationButton.MODE_CHECK = Constance.STOP_RECORD;
        }


    }

    private void setMapView() {
        _omapView.getO2mapInstance().renderLock();
        refreshNorthIcon();
        _omapView.getO2mapInstance().renderUnlock();
//        long now = System.currentTimeMillis();
//        Date date = new Date(now);
//        WataLog.d("now=" + now);
//        SimpleDateFormat mFormat = new SimpleDateFormat("MMdd-HH:MM");
//        Date currentTime = Calendar.getInstance().getTime();
//        String date_text = new SimpleDateFormat("MMdd-HH:MM", Locale.getDefault()).format(now);
//        String date_text = mFormat.format(date);
        WataLog.d("date_text=" + getTime());
        int size = SAVE_FILE_NAME.length();
        String pdName = SAVE_FILE_NAME.substring(0, size / 2) + "..." + getTime();
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
                    Toast.makeText(PathDrawingActivity.this, "'Bluetooth가 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
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
            public void onPoiSelectLine(MyWayInfo items, int position) {
                WataLog.i("해당위치 이동");
                setWebView("drawLineCoords", items.getStartPointX() + "|" + items.getStartPointY() + "|" + items.getEndPointX() + "|" + items.getEndPointY() + "|" + "highlight");
                WataLog.d("역방향확인 =" + items.getRecordR());

                if ("확인".equals(items.getRecordR())) {
                    onRecordView(true, true, String.valueOf(items.getNumber()));
                } else {
                    onRecordView(true, false, String.valueOf(items.getNumber()));
                }

                pathOption.setVisibility(View.GONE);
            }

            @Override
            public void onRecordStartPoint(MyWayInfo items, int position) {
                WataLog.d("기록시작위치 =" + items.getStartPointX() + "," + items.getStartPointY());
                onRecordView(false, false, "0");
//                webTestPoin(items.getStartPointX() + "|" +items.getStartPointY());
                //시작지점이동
                onMovePoint(items.getStartPointX(), items.getStartPointY());
                setFinishPoint(items.getStartPointX(), items.getStartPointY(), items.getStartRelativePointX(), items.getStartRelativePointY());

            }

            @Override
            public void onRecordEndPoint(MyWayInfo items, int position) {
                WataLog.i("종료지점이동");
                WataLog.d("position=" + position);
                onRecordView(false, false, "0");
                //종료지점이동
                WataLog.d("종료지점이동 =" + items.getEndPointX() + "," + items.getEndPointY());
//                webTestPoin(items.getEndPointX() + "|" +items.getEndPointY());

                onMovePoint(items.getEndPointX(), items.getEndPointY());
                setFinishPoint(items.getEndPointX(), items.getEndPointY(), items.getEndRelativePointX(), items.getEndRelativePointY());
            }

            @Override
            public void onReverseRecord(MyWayInfo items, int position) {
                WataLog.i("역방향 기록하기");
                //종료지점이동

                WataLog.d("기록종료위치 =" + items.getEndRelativePointX() + "," + items.getEndRelativePointX());
                WataLog.d("기록종료위치 =" + items.getEndPointX() + "," + items.getEndPointX());

                mReversSpX = items.getStartPointX();
                mReversSpY = items.getStartPointY();
                mReversEpX = items.getEndPointX();
                mReversEpY = items.getEndPointY();

                isRecording = false;
                WataLog.d("items.getNumber()=" + items.getNumber());
                setWebView("moveMapReverse", items.getEndPointX() + "|" + items.getEndPointY() + "|" + items.getNumber());
                mLineNum = items.getNumber();

//                setFinishPoint(items.getEndPointX(), items.getEndPointY(), items.getEndRelativePointX(), items.getEndRelativePointY());

                WataLog.i("check! 역방향 기록하기");
                isInverse = true;
                onToast(Gravity.CENTER, getString(R.string.revers_record_start_message_1));
                mCameraAngle = items.getAngle() + 180;
                lineSelected = (Polyline) getMyWayReverseLine(items.getEndRelativePointX(), items.getEndRelativePointY(), items.getStartRelativePointX(), items.getStartRelativePointY());

//                setReverseRecord(items);
                pathOption.setVisibility(View.GONE);
            }
        });

        // poi 내역
        mPoiListAdapter = new PoiListAdapter(this);
        mPoiListAdapter.setOnItemClickListener(new PoiListAdapter.OnItemClickListner() {
            @Override
            public void onPoiPoint(PoiInfo items, int position) {
                WataLog.d("position=" + position);
                //POI 지점이동
                onMovePoint(items.getPoiPositionX(), items.getPoiPositionY());

                float relativeX = Float.parseFloat(items.getPoiPositionX()) - mAbsoluteSPointX;
                float relativeY = Float.parseFloat(items.getPoiPositionY()) - mAbsoluteSPointY;
                setFinishPoint(items.getPoiPositionX(), items.getPoiPositionY(), relativeX, relativeY);
            }

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
            }
        });
    }

    private void setDeletePOIPopup(final PoiInfo items, final int position) {
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(PathDrawingActivity.this);
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

    private void onMovePoint(String poinX, String pointY) {
        WataLog.d("이동위치 - " + poinX + "," + pointY);
        isRecording = true;
        setWebView("moveMapXY", poinX + "|" + pointY);

        lineSelected = (Polyline) getPreviewLine(Float.valueOf(poinX), Float.valueOf(pointY));

        String lineX = String.valueOf(lineSelected.getPart(0).getEndPoint().x);
        String lineY = String.valueOf(lineSelected.getPart(0).getEndPoint().y);

        WataLog.d("이동위치 - " + lineX + "," + lineY);
//        webTestPoin(lineX + "|" + lineY );
        mWebView.loadUrl("javascript:drawStartLine('" + lineX + "|" + lineY + "')");

        pathOption.setVisibility(View.GONE);
    }

    private void onRecordPointMoving() {
        Toast.makeText(PathDrawingActivity.this, "수집 시작지점으로 이동해주세요.", Toast.LENGTH_SHORT).show();
        setUiControl(2);

//        CAMERA_MOVING = true;
        mRotation = mCameraAngle;

        setStartPointSetting(true);
        record_lisetview.setVisibility(View.GONE);

        Vector v = new Vector(mLastPointX, mLastPointY, 0, 0);
        _omapView.getO2mapInstance().getView().getCamera().setLookAtPosition(v);
    }

    private void setFinishPoint(String pointX, String pointY, double relativePointX, double relativePointY) {

        WataLog.d("종료지점이동 =" + pointX + "," + pointY);

        // 시작위치로 변경
        mStartEndPointX_S = pointX;
        mStartEndPointY_S = pointY;

        mRelativeSPointX = (float) relativePointX;
        mRelativeSPointY = (float) relativePointY;
    }


//    private boolean CAMERA_MOVING = false;

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
                el = ElementHelper.fromImage(WIDTH_SIZE, HEIGHT_SIZE, mapPath + "/gangnam_map_all_sample_2.png");
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
        WataLog.d("StaticManager.basePointX=" + StaticManager.basePointX);
        if (StaticManager.basePointX == null) {
            basePointX = 0;
        } else {
            basePointX = Double.valueOf(StaticManager.basePointX);
        }

        WataLog.d("StaticManager.basePointY=" + StaticManager.basePointY);
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

        WataLog.d(" StaticManager.folderName=" + StaticManager.folderName);
        String folderN = StaticManager.folderName.substring(0, 10);
        WataLog.i("folderN=" + folderN);

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
                    new AlertDialog.Builder(PathDrawingActivity.this)
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
                            wifiScanner = new com.geotwo.TBP.TBPWiFiScanner(PathDrawingActivity.this);
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
            Toast.makeText(PathDrawingActivity.this, " 수집종료.", Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(PathDrawingActivity.this)
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
        if (ROAD_MODE) {
//            setWebView("drawLineCoords", items.getStartPointX() + "|" + items.getStartPointY() + "|" + items.getEndPointX() + "|" + items.getEndPointY() + "|" + "highlight");

//            setMapSetting();
//            setStartPointLine(); // 라인그리기
//            setUiControl(0);
//            setMapLookPosition(mLastPointX, mLastPointY + 10, 0);
        }

//        mPoiListAdapter.setItems(mPoiInfoData);
//        mPoiListview.setAdapter(mPoiListAdapter);
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
        mLastPointX = 0;
        mLastPointY = 0;
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
                        ((Activity) PathDrawingActivity.this).startActivityForResult(
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

//    public void onBackPressed() {
//        super.onBackPressed();
//        if (pathOption.getVisibility() == View.VISIBLE) {
//            pathOption.setVisibility(View.GONE);
//        } else {
//            DataCore.isOnGathering = false;
//            AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
//            alBuilder.setMessage(" 기록을 종료하시겠습니까?");
//            alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    finish();
//                }
//            });
//            alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    return;
//                }
//            });
//            alBuilder.setTitle("기록 종료");
//            alBuilder.show();
//        }
//    }

    private ImageButton mNorth;
    private TextView poiNum;

    private double mCurrAngle = 0;
    private double mPrevAngle = 0;
    private double mAddAngle = 0;

    private LinearLayout map_inner_frm_linear1;
//    private int mFloorsLever = 0;

    private void initView(boolean isFirst) {
        // kcy1000 - 레이아웃 셋팅
        map_inner_frm_linear1 = (LinearLayout) findViewById(R.id.map_inner_frm_linear1);
//        map_inner_frm_linear1.setVisibility(View.GONE);

        my_way_record_info_layout = (RelativeLayout) findViewById(R.id.my_way_record_info_layout);
//        my_way_record_info_layout.setVisibility(View.GONE);
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

        //스텝
        step_btn = (Button) findViewById(R.id.step_btn);
        step_btn.setOnClickListener(this);
        step_btn.setVisibility(View.GONE);

        // 지점확인
        save_point_check_btn = (Button) findViewById(R.id.save_point_check_btn);
        save_point_check_btn.setOnClickListener(this);

        // 기록상태 확인 이미지
        recordViewLyaout = (RelativeLayout) findViewById(R.id.record_view_layout);
        recordView = (ImageView) findViewById(R.id.record_view);
        recordViewText = (TextView) findViewById(R.id.record_view_text);

//        Button point_check_btn = (Button) findViewById(R.id.point_check_btn);
//        point_check_btn.setOnClickListener(this);

//        String[] stringMin = new String[100];
//        for (int i = 0; i < stringMin.length; i++) {
//            stringMin[i] = Integer.toString(i - 9);
//        }
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


        if (_SCAN_RESULT_THIS_EPOCH == null) {
            _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();
            WataLog.d("_dataCore.getSCAN_RESULT_THIS_EPOCH()=" + _dataCore.getSCAN_RESULT_THIS_EPOCH());
        }


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
        Intent intent = new Intent(PathDrawingActivity.this, GatherListActivity.class);
        startActivity(intent);
    }

    private double distRemain = -1;
    private SandboxModel currentDistLayer = null;


    private void startStepCountAnimation() {
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        BounceInterpolator bounceInterpolator = new BounceInterpolator(0.2, 40);
        bounceAnimation.setInterpolator(bounceInterpolator);
    }

    private ArrayList<Vector> multi = null;
    private ArrayList<Points> fullpath;
    private boolean bEndPoint = false;

    private void updateTouch(double x, double y) {
//        WataLog.d("updateTouch = " + x + " // " + y);
//        WataLog.d("DataCore.iGatherMode=" + DataCore.iGatherMode);

        if (DataCore.iGatherMode == DataCore.GATHER_MODE_NONE || DataCore.iGatherMode == DataCore.GATHER_MODE_GATHERING)
            return;

        if (DataCore.iGatherMode == DataCore.GATHER_MODE_START_SELECT) {
            Line line = ViewUtil.computeRayFromScreenPoint(_omapView.getO2mapInstance().getView(), (int) x, (int) y);
            WataLog.d("line=" + line);
            Plane pl = new Plane(0.0, 0.0, 1.0, 0);

            Vector v = pl.intersect(line);
            if (v != null) {
                _startPoint = new Vector(v.x, v.y, v.z);
                WataLog.d("_startPoint =" + _startPoint);
                WataLog.d("iGatherMode =" + DataCore.iGatherMode);
                WataLog.i("시작점 지정하기");
            }


//            Toast.makeText(this, "시작점을 터치해주세요.", Toast.LENGTH_SHORT).show();
//            onMyWayStartEndPoint(DataCore.iGatherMode, v);
        }
//        _omapView.getO2mapInstance().requestRedraw();
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
            WataLog.i("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            mLastPointX = (float) start.x;
            mLastPointY = (float) start.y;

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

    private boolean ON_POI_MODE = false;
    private boolean ON_COMPASS_MODE = false;
    private boolean ON_PRECISION_MODE = false;

    @Override
    public void onClick(View v) {
//        testNumlayer = null;
        switch (v.getId()) {
            case R.id.poi_mode_img_layout:
                WataLog.i("poi모드");
                if (ON_POI_MODE) {
                    ON_POI_MODE = false;
                    poiModeImg.setImageResource(R.mipmap.bt_addpoi);
                    setWebView("togglePoiMode");
                    onToast(Gravity.CENTER, getString(R.string.off_poi_mode));

                } else {
                    ON_POI_MODE = true;
                    poiModeImg.setImageResource(R.mipmap.bt_addpoi_active);
                    setWebView("togglePoiMode");
                    onToast(Gravity.CENTER, getString(R.string.on_poi_mode));
                }
                break;
            case R.id.bt_compass_layout:
                WataLog.i("나침반모드");
                if (ON_COMPASS_MODE) {
                    ON_COMPASS_MODE = false;
                    compassModeImg.setImageResource(R.mipmap.bt_compass_default);
                    onToast(Gravity.CENTER, getString(R.string.off_compass_mode));
                } else {
                    ON_COMPASS_MODE = true;
                    compassModeImg.setImageResource(R.mipmap.bt_compass_acctive);
                    onToast(Gravity.CENTER, getString(R.string.on_compass_mode));
                }
                break;

            case R.id.ic_undo_btn:
                WataLog.i("취소");

                setWebView("cancelRecord");
                onRecordCancel();

                break;
            case R.id.ic_setting_btn:
                WataLog.i("설정");
                setSetting();
                break;
            case R.id.option_btn:
                WataLog.i("옵션");
                WataLog.d("RECORD_CHECK=" + RECORD_CHECK);
                if (!RECORD_CHECK) {
                    pathOption.setVisibility(View.VISIBLE);
                } else {
                    onToast(Gravity.CENTER, getString(R.string.recording_list_error_message));
                }

                break;

            case R.id.record_cancel: // 기록 취소
                onRecordCancel();
                break;

            case R.id.arrow_left: // 기록내역보기 취소
                pathOption.setVisibility(View.GONE);
                break;
            case R.id.record_list_layout: // 옵션 기록 내역보기
                recordLine.setVisibility(View.VISIBLE);
                poiLine.setVisibility(View.GONE);

                mPathDrawingListview.setVisibility(View.VISIBLE);
                mPoiListview.setVisibility(View.GONE);
                break;
            case R.id.poi_list_layout: // 옵션 poi 내역보기
                recordLine.setVisibility(View.GONE);
                poiLine.setVisibility(View.VISIBLE);

                mPathDrawingListview.setVisibility(View.GONE);
                mPoiListview.setVisibility(View.VISIBLE);

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
                    setPOILog(POI_pointX, POI_pointY, poiRName);

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
                break;
            case R.id.precision_btn: // 미세각도조절
                setPrecision(ON_PRECISION_MODE);
                break;
//            case R.id.angle_m_btn: // "-"
//                mCameraAngle -= 2;
//                setWebView("setRotation", String.valueOf(mCameraAngle));
//                break;
//            case R.id.angle_p_btn: // "+"
//                mCameraAngle += 2;
//                setWebView("setRotation", String.valueOf(mCameraAngle));
//                break;
/////// ================================================================================================================================
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
//                //                o2map.getView().getCamera().setDistance(200.0);
////                compass_img.setVisibility(View.VISIBLE);
//                Toast.makeText(this, "시작지점을 확인해주세요.", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.file_name_cancel:
//                break;
            case R.id.poi_list_btn: //
                WataLog.i("POI list");

                if (mPoiListAdapter.getCount() > 0) {
                    poi_listview_rlayout.setVisibility(View.VISIBLE);
                } else {
                    Toast toast = Toast.makeText(PathDrawingActivity.this, "저장된 기록이 없습니다.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                break;
//            case R.id.poi_list_ok_btn: // poi확인
//                poi_listview_rlayout.setVisibility(View.GONE);
//                onKeyboardHide(poi_edit);
//                break;

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
//                    CAMERA_MOVING = false;

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
                    setReversePointSetting(item.EndPointX, item.EndPointY, item.StartPointX, item.StartPointY);
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
                    Toast toast = Toast.makeText(PathDrawingActivity.this, "저장된 기록이 없습니다.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                break;

            case R.id.path_drawing_setting: // 설정
//                viewStepLenSetting();

                setSetting();
                break;

        }
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


    // 최초시작 라인생성 - kcy1000 - 1
    private void setMapSetting() {
        WataLog.i("setMapSetting");
        startSettingModel = new SandboxModel("startSettingModel");
        int heightPoint = (mHeightPixels / 5) * 3;
        int widthPixel = mWidthPixel / 2;
        WataLog.d("heightPoint=" + heightPoint); // 542
        WataLog.d("widthPixel=" + widthPixel); // 720

        Line line = ViewUtil.computeRayFromScreenPoint(_omapView.getO2mapInstance().getView(), widthPixel, heightPoint);
        Plane pl = new Plane(0.0, 0.0, 1.0, 0);
        Vector startVector = pl.intersect(line);
        _startPoint = new Vector(startVector.x, startVector.y, startVector.z);

        WataLog.d("mLastPointX=" + mLastPointX + "////mLastPointY=" + mLastPointY);
        updateTouch(mLastPointX, mLastPointY);
        WataLog.d("mLastPointX=" + mLastPointX + "////mLastPointY=" + mLastPointY);
        mRecordStartPointX = mLastPointX;
        mRecordStartPointY = mLastPointY;

//        CAMERA_MOVING = true;
        WataLog.d("mLastPointX=" + mLastPointX + "////mLastPointY=" + mLastPointY);
        WataLog.d("_startPoint=" + _startPoint + "////_startPoint=" + _startPoint);

        Element el = ElementHelper.fromTextBillboard(new Vector(_startPoint.x, _startPoint.y, 5.0, 0), 1f, 1f, true, "S", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
        if (el != null) {
            startSettingModel.addElement("startSettingModel", el);
        }

        ((CompositeModel) _omapView.getO2mapInstance().getModel()).addModel(startSettingModel);
        lineSelected = (Polyline) getStartPointLine(_startPoint.x, _startPoint.y);
    }

    private void onRecordStart() {
        WataLog.i("onRecordStart");
//        progress_layout.setVisibility(View.VISIBLE);
//        setUiControl(1);

        if (startSettingModel != null) {
            startSettingModel.removeAllElement();
        }

        mRecordDirection = true;
        o2map.getView().getCamera().setDistance(60.0); //지도확대
        if (isInverse) {  // 역방향

//            Quaternion q = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, Angle.fromDegrees(mCameraAngle)); // 지도회전
//            _omapView.getO2mapInstance().getView().getCamera().setRotation(q);
//          webview지도회전
            setWebView("setRotation", String.valueOf(mCameraAngle));

        } else {  // 정방향

//            WataLog.d("CAMERA_MOVING=" + CAMERA_MOVING);
//            if (CAMERA_MOVING) {
//                mOmapRatation = mCameraAngle;
//            } else {
//                mOmapRatation = (int) mRotation - 90 + mOmapRatation;
//                mOmapRatation = (int) mRotation + mOmapRatation;
            mOmapRatation = mCameraAngle; // kcy1000 - 계속 확인필요함. 자이로이용하여 미세각도조절시 조절한 각도와 실제이동각도 이동후 각도 확인 필요
//            }

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

//        CAMERA_MOVING = false;
    }

    private void onRecordStop() {
        wifiSet(false);
        progress.setVisibility(View.GONE);
//        setUiControl(2);

//        deleteLine(startPointModel, "startPointModel");
//        deleteLine(endPointModel, "endPointModel");

        WataLog.d("mRecordStartPointX=" + mRecordStartPointX + "//// mRecordStartPointY=" + mRecordStartPointY);
        WataLog.d("mLastPointX=" + mLastPointX + "//// mLastPointY=" + mLastPointY);

        // 기록할 경로번호
        if (!isInverse) {
//            setPointCount(mRecordStartPointX, mRecordStartPointY, mLastPointX, mLastPointY);
        }
        setRecordLine(); // 기록라인생성
        setRecordGathering(true);
//        setStartPointLine(); // 라인그리기
//        CAMERA_MOVING = true;
    }

    // kcy1000 - webview 역방향 기록하기
    private void setReverseRecord(MyWayInfo items) {
        WataLog.i("check! 역방향 기록하기");
        isInverse = true;
//        int size = myWayInfoData.size();
//        MyWayInfo item = myWayInfoData.get(size - 1);
//        mRecordIndexNum = size - 1;
        onToast(Gravity.CENTER, getString(R.string.revers_record_start_message_1));
        // 정향방향 라인지우기

//        setReversePointSetting(start_x, start_y, end_x, end_y);
        mCameraAngle = items.getAngle() + 180;
        lineSelected = (Polyline) getMyWayReverseLine(items.getEndRelativePointX(), items.getEndRelativePointY(), items.getStartRelativePointX(), items.getStartRelativePointY());
//        setStartEndPointName();

        WataLog.d("mCameraAngle=" + mCameraAngle);

//        onRecordStart();
    }


    private void setSetting() {
        mPsettingDialog = new PathDrawingSettingDialog(PathDrawingActivity.this, new OnClickListener() {
            public void onClick(View v) {
                WataLog.i("check 0");
                com.geotwo.LAB_TEST.Gathering.dialog.SettingDialog sensitive = new com.geotwo.LAB_TEST.Gathering.dialog.SettingDialog();
                AlertDialog senseDialog = sensitive.makeSensitiveSettingDilog(PathDrawingActivity.this);
                senseDialog.show();
            }
        });

        mPsettingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                WataLog.i("onDismiss");

                SharedPreferences pref = getSharedPreferences(Constance.KEY, 0);
                isMapRatation = pref.getBoolean(Constance.SETTING_MAP_ROTATION, true);
                isGyroSensor = pref.getBoolean(Constance.SETTING_G_SENSOR_ROTATION, true);

//                if (isMapRatation) {
//                    angle_layout.setVisibility(View.VISIBLE);
//                } else {
//                    angle_layout.setVisibility(View.GONE);
//                }
                WataLog.d("isMapRatation= " + isMapRatation);
            }
        });
        mPsettingDialog.show();
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

    // 기록 취소
    private void onRecordCancel() {
        wifiSet(false);
        mStartRecord = false;
        mPointCount--;

        WataLog.d("mRecordStartPointX=" + mRecordStartPointX + "//// mRecordStartPointY=" + mRecordStartPointY);
        WataLog.d("mLastPointX=" + mLastPointX + "//// mLastPointY=" + mLastPointY);
        WataLog.i("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        isRecording = true;

        // 종료위치 시작위치로 변경
        mStartEndPointX_S = getAbsoluteStartPoint((float) mRecordStartPointX, true);
        mStartEndPointY_S = getAbsoluteStartPoint((float) mRecordStartPointY, false);

//        webTestPoin(mStartEndPointX_S +"|" + mStartEndPointY_S);

        mRelativeSPointX = (float) mRecordStartPointX;
        mRelativeSPointY = (float) mRecordStartPointY;

        mLastPointX = (float) mRecordStartPointX;
        mLastPointY = (float) mRecordStartPointY;

        endX = mRecordStartPointX;
        endY = mRecordStartPointY;

        lineSelected = (Polyline) getPreviewLine(mLastPointX, mLastPointY);


//        if (Locale.getDefault().getLanguage().equals("ko")) {
//            swipeBtnLayout.setBackgroundResource(R.mipmap.bg_send);
//        } else {
//            swipeBtnLayout.setBackgroundResource(R.mipmap.bg_send_en);
//        }

        onRecordStart();
        isRecording = false;

//        lineHeader = getHeader(_startPoint, _endPoint);
//        pdrvariable.initValues();
//        initialmm.MM_initialize3(lineSelected);

//        pdrvariable.setPedestrian_x_coordinate(endX);
//        pdrvariable.setPedestrian_y_coordinate(endY);


    }


    private double mDefaultRotation = 90;
    //    private ImageView directionImg;

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

        lineSelected = (Polyline) getMyWayLine((double) mLastPointX, (double) mLastPointY);
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
    public static float mLastPointX = 0;
    public static float mLastPointY = 0;

    // kcy1000 - 경로 기록시작
    private void startMyWayGathering(Polyline line) {
        WataLog.d("startMyWayGathering = " + line);
        stepCount = 0;
        recordStepCount.setText("0");
        recordStepLength.setText("0m");
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
        WataLog.d("_endPoint =" + _endPoint);
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

        WataLog.i("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        // 마지막경로를 저장한다.
        mLastPointX = (float) x;
        mLastPointY = (float) y;

        WataLog.d("mLastPointX=" + mLastPointX + "//mLastPointY=" + mLastPointY);

        if (x != 0 && y != 0) {
            /**
             * 실제 이동시 이곳으로 분기
             */
            stepCount++;
            recordStepCount.setText(String.valueOf(stepCount));

            float[] rotationVectorOrientations = SensorUtils.getOrientation(sensoract.rotationVectors);

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
                    recordStepLength.setText(depth + "m");

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
        WataLog.d("m Double.isNaN(dist) = " + Double.isNaN(dist));
        if (Double.isNaN(dist)) {
//            Toast.makeText(PathDrawingActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            setReverseStop();
            WataLog.e("check!!!!!!!!!!!!!!");
            endX = 0.0;
            endY = 0.0;
        } else {
            WataLog.d("m distRemain = " + distRemain);
            WataLog.d("m dist = " + dist);

            if (distRemain < 0 || distRemain >= dist) {
                distRemain = dist;
            } else {
                distRemain = -1;
            }
            if (distRemain >= 0 && distRemain <= 0.7) {
//                Toast.makeText(PathDrawingActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                WataLog.e("check!!!!!!!!!!!!!!");
                setReverseStop();
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
    private void setReversePointSetting(String startPointX, String startPointY, String endPointX, String ebdPointY) {
        WataLog.i("역방향 셋팅");
        // 이동할 좌표 가져와야함.
        lineSelected = (Polyline) getMyWayReverseLine(Double.valueOf(startPointX), Double.valueOf(startPointY), Double.valueOf(endPointX), Double.valueOf(ebdPointY));
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getStartPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getStartPoint().y);
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getEndPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getEndPoint().y);
        WataLog.d("testNumlayer=" + testNumlayer);
//        setReverLine();
    }

    //  정방향 라인그리기
    private void setForwardLine(Polyline line) {
        if (myRecordFLine == null) {
            myRecordFLine = new SandboxModel("myRecordFLine");
        }
        Element eal = ElementHelper.fromPolyline(line, 0.1, 5, new Color(0, 0, 255, 255), false, false);

        WataLog.d("myRecordFLine =" + myRecordFLine);
//        mRecordPosition = mPointCount;
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
//            setForwardLine(line);
        } else { // 역방향
//            setReverLine();
        }

        WataLog.d("mPointCount=" + mPointCount);
//        _omapView.getO2mapInstance().requestRedraw();
//        delIcon(); // icon제거

        WataLog.d("mCameraAngle==" + mCameraAngle);
        if (lineSelected != null) {
            if (!isInverse) { //정방향
                // 기록할 경로번호
//                setPointCount(lineSelected.getPart(0).getStartPoint().x, lineSelected.getPart(0).getStartPoint().y);

                WataLog.i("check!~!!!!!!!!!!!정방향!!!!!!!!!!!");

                double dist = Math.sqrt(Math.pow(mLastPointX - mRecordStartPointX, 2) + Math.pow(mRecordStartPointY - mLastPointX, 2));
                final String depth = deciformat_WF.format(dist);


                WataLog.d("기록시작위치 =" + mLineStartPointX + "," + mLineStartPointY);
                WataLog.d("기록종료위치 =" + mStartEndPointX_S + "," + mStartEndPointY_S);

                WataLog.d("기록시작위치 상대좌표 =" + mRecordStartPointX + "," + mRecordStartPointY);
                WataLog.d("기록종료위치 상대좌표 =" + mLastPointX + "," + mLastPointY);

//                webTestPoin(mStartPointX_S + "|" +mStartPointY_S);
//                webTestPoin(mLineStartPointX + "|" +mLineStartPointY);

                //절대좌표
//                mStartPointX_S 종료지점
//                mLineStartPointY  기록 시작점
                // 상대좌표

                mRecordPosition++;
                WataLog.d("mRecordPosition=" + mRecordPosition);
                // 기록경로 list로 저장하기.
                if (myWayInfoData != null && myWayInfoData.size() > 1) {
                    WataLog.i("!!!!!!!!!!!!!!111111!!!!!!!!!!!!!!!!!!!");
                    myWayInfoData.add(new MyWayInfo(mRecordPosition, TEMP_START_POINTX, TEMP_START_POINTY, mStartEndPointX_S, mStartEndPointY_S, depth, "완료", "역방향 기록하기",
                            mCameraAngle, mRecordStartPointX, mRecordStartPointY, mLastPointX, mLastPointY, mMyLatlng.latitude, mMyLatlng.longitude));
                } else {
                    WataLog.i("!!!!!!!!!!!!!!!!222222!!!!!!!!!!!!!!!!!");
                    myWayInfoData.add(new MyWayInfo(mRecordPosition, mLineStartPointX, mLineStartPointY, mStartEndPointX_S, mStartEndPointY_S, depth, "완료", "역방향 기록하기",
                            mCameraAngle, mRecordStartPointX, mRecordStartPointY, mLastPointX, mLastPointY, mMyLatlng.latitude, mMyLatlng.longitude));
                }
                myWayListAdapter.setItems(myWayInfoData);
                mPathDrawingListview.setAdapter(myWayListAdapter);

            } else {
                WataLog.i("check!~!!!!!!!!!!역방향!!!!!!!!!!!!");
                WataLog.d("mPointCount=" + mPointCount);
                WataLog.d("mRecordPosition=" + mRecordPosition);

                WataLog.d("기록시작위치 =" + mLastPointX + "," + mLastPointY);
                WataLog.d("기록종료위치 =" + mRecordStartPointX + "," + mRecordStartPointY);
                int position = mLineNum - 1;
                myWayInfoData.get(position).setRecordR("완료");
                myWayListAdapter.notifyDataSetChanged();

//                double dist = Math.sqrt(Math.pow(mRecordStartPointX - mLastPointX, 2) + Math.pow(mRecordStartPointY - mLastPointY, 2));
//                final String depth = deciformat_WF.format(dist);
//                myWayInfoData.set(mLineNum, new MyWayInfo(mRecordPosition, mLineStartPointX, mLineStartPointY, mStartEndPointX_S, mStartEndPointY_S, depth, "완료", "역방향 완료",
//                        mCameraAngle, mLastPointX, mLastPointY, mRecordStartPointX, mRecordStartPointY, mMyLatlng.latitude, mMyLatlng.longitude));
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

//        Date currentTime = Calendar.getInstance().getTime();
//        String date_text = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentTime);
        StaticManager.setPathNum(getTime() + (mPointCount));

        DataCore.iGatherMode = DataCore.GATHER_MODE_NONE;
        _dataCore.insertGatheredData(_dataCore.getCurGatheringName(), _dataCore.getSCAN_RESULT_TOTAL());
        _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();
        WataLog.d("_dataCore.getSCAN_RESULT_THIS_EPOCH()=" + _dataCore.getSCAN_RESULT_THIS_EPOCH());

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

            // kcy1000 - 파일작성시 디바이스 이름 및 각종 정보 입력함.
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH);
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH.H1_PSNID());
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH.B1_GID());
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH.B8_ColStartP_F());
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH.B11_ColDevModel());
//            WataLog.d("_SCAN_RESULT_THIS_EPOCH=" + _SCAN_RESULT_THIS_EPOCH.B12_ColOpt());
            String addName = SAVE_FILE_NAME.substring(0, 10);
            WataLog.d("SAVE_FILE_NAME=" + SAVE_FILE_NAME);
            _SCAN_RESULT_THIS_EPOCH.B9_ColStartP_CVADDR(SAVE_FILE_NAME);
//            _SCAN_RESULT_THIS_EPOCH.B9_ColStartP_CVADDR("가나다라마바사");
            //log 기록저장하기 - 파일전송
            if (!isInverse) {
                SaveGatheredData.saveGatheredData(StaticManager.getResultPathDrawingPath(), SAVE_FILE_NAME + "_" + _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
                SaveGatheredData.saveSendableData(StaticManager.getResultPathDrawingPath(), SAVE_FILE_NAME + "_" + _dataCore.getCurGatheringName() + "_F.temp", _SCAN_RESULT_THIS_EPOCH);

//                setLineInfo(mRecordStartPointX, mRecordStartPointY, mLastPointX, mLastPointY);

                WataLog.d("mapId=" + mapId);
                if ("-1".equals(mapId)) {
                    setLineInfo(mLineStartPointX, mLineStartPointY, mStartEndPointX_S, mStartEndPointY_S, String.valueOf(mMyLatlng.latitude), String.valueOf(mMyLatlng.longitude));
                } else {
                    setLineInfo(mLineStartPointX, mLineStartPointY, mStartEndPointX_S, mStartEndPointY_S, "", mapId);
                }

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
        WataLog.d("Build.SERIAL=" + Build.SERIAL);
        _SCAN_RESULT_TOTAL = new ArrayList<PDI_REPT_COL_DATA>();

        //Tag : TOTAL 초기화
        _dataCore.setSCAN_RESULT_TOTAL(_SCAN_RESULT_TOTAL);
        _dataCore.setSCAN_RESULT_THIS_EPOCH(_SCAN_RESULT_THIS_EPOCH);

//        isInverse = false;
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

        Toast.makeText(PathDrawingActivity.this, "아직은 안되요 ㅠ.ㅠ ", Toast.LENGTH_SHORT).show();
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
    private Geometry getMyWayReverseLine(double startPointX, double startPointY, double endPointX, double endPointY) {
        Polyline line = new Polyline();
        line.makeParts(1);

        Points pts = new Points();
        pts.makePoints(2);

        pts.setPoint(0, startPointX, startPointY, 0.0f);
        pts.setPoint(1, endPointX, endPointY, 0.0f);
        line.setParts(0, pts);
        return line;
    }

    private void setUiControl(int type) {

        switch (type) {
            case 0: { // 최초 셋팅
//                map_inner_frm_linear1.setVisibility(View.VISIBLE);
//                my_way_record_info_layout.setVisibility(View.VISIBLE);

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
//                angle_control_layout.setVisibility(View.GONE);
//
//                if (isGyroSensor) {
//                    step_btn.setVisibility(View.GONE);
//                } else {
//                    step_btn.setVisibility(View.VISIBLE);
//                }
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

//                if (isMapRatation) {
//                    angle_layout.setVisibility(View.VISIBLE);
//                } else {
//                    angle_layout.setVisibility(View.GONE);
//                }

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
//        WataLog.d("mapRatation=" + mapRatation);
//        WataLog.d("mapRatation.getRotation().W=" + mapRatation.getRotation().w);
//        WataLog.d("mapRatation.getRotation().Z=" + mapRatation.getRotation().z);

        Angle angle = mapRatation.getRotation().getAngle();
//        WataLog.d("angle=" + angle.getDegrees());
//        WataLog.d("getRotationX=" + mapRatation.getRotation().getRotationX());
//        WataLog.d("getRotationY=" + mapRatation.getRotation().getRotationY());
//        WataLog.d("getRotationZ=" + mapRatation.getRotation().getRotationZ());

//                            mCameraAngle = (int)(2 * 3.14 * (angle.degrees / 360));
        mCameraAngle = (int) angle.degrees;
//        WataLog.d("mCameraAngle=" + mCameraAngle);

        double rotationZ = mapRatation.getRotation().getRotationZ().degrees;
//        WataLog.d("rotationZ=" + rotationZ);
        if (rotationZ > 0) {
            if (mCameraAngle > 180) {
                mCameraAngle = 360 - mCameraAngle;
            }
        } else {
            if (mCameraAngle < 180) {
                mCameraAngle = 360 - mCameraAngle;
            }
        }

//        WataLog.d("mCameraAngle=" + mCameraAngle);
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

        lineSelected = (Polyline) getDrawingPoint((double) mLastPointX, (double) mLastPointY);
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
//                    WataLog.d("isRecording = " + isRecording);
//                    WataLog.d("isMapRatation = " + isMapRatation);
//                    if (CAMERA_MOVING && isMapRatation) {
                    if (myWayListAdapter.getCount() > 0 && isMapRatation && isRecording && !ON_PRECISION_MODE) {
//                        WataLog.d("지도각도 =" + mDegressSum + "-" + mSaveDegress + "=" + (mDegressSum - mSaveDegress));
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
                        lineSelected = (Polyline) getDrawingPoint((double) mRecordStartPointX, (double) mRecordStartPointY);

//                        mRotation = Math.abs(mCameraAngle - 90);
                        //kcy-webview
                        // Webview 지도회전하기
                        setWebView("setRotation", String.valueOf(mCameraAngle));
//                        mRelativeSPointX = getRelativeStartPointFloat(mLastPointX, true);
//                        mRelativeSPointY = getRelativeStartPointFloat(mLastPointY, false);
//                        mRotation = mCameraAngle;
//                        setAngle();

//                        setStartPointLine();
//                        setStartEndPointName();

                    }
//                    }
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
//        WataLog.d("accuracy=" + accuracy);
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
    private float mNowPointX = 0, mNowPointY = 0;
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

//                            WataLog.d("temp[0]=" + temp[0]);
//                            WataLog.d("temp[1]=" + temp[1]);

                            // kcy-webview 지점 정보
                            mNowPointX = (float) temp[0];
                            mNowPointY = (float) temp[1];

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
                                if (!mStartRecord) {
                                    save_point_check_btn.setEnabled(true);
                                    mStartRecord = true;
                                }
                                progress.setVisibility(View.GONE);
                                onToast(Gravity.CENTER, getString(R.string.start_recording));
                            } else if (_SCAN_RESULT_THIS_EPOCH.B21_Payload.size() > 2) {
                                if (progress.getVisibility() == View.VISIBLE) {
                                    progress.setVisibility(View.GONE);
                                }
                            }
//                            String total = _SCAN_RESULT_THIS_EPOCH.B21_Payload.size() + "회 스캔 완료";
//						setStatusString(total);
                            break;

                        case DataCore.DATA_SEND_ENDED:
                            WataLog.i("DATA_SEND_ENDED");
//                            _gatheringEndDialog.dismiss();
                            Toast.makeText(PathDrawingActivity.this, "전송이 완료되었습니다.", Toast.LENGTH_SHORT).show();

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
//                            _omapView.getO2mapInstance().renderLock();
//                            refreshNorthIcon();
//                            _omapView.getO2mapInstance().renderUnlock();
//
////                            WataLog.d("CAMERA_MOVING=" + CAMERA_MOVING);
////                            WataLog.d("mLastPointX=" + mLastPointX);
////                            WataLog.d("mLastPointY=" + mLastPointY);
////                            WataLog.d("CAMERA_MOVING = " + CAMERA_MOVING);
//                            WataLog.d("isMapRatation = " + isMapRatation);
//
//                            if (!isMapRatation || myWayListAdapter.getCount() == 0) {
//
////                                } else {
//                                setCameraMoving();
//                                setDrawingPointSetting();
//                                setStartPointLine();
////                                setStartEndPointName();
////                                }
//                            }

//                            WataLog.d("mCameraAngle=" + mCameraAngle);
//                            WataLog.d("mRotation=" + mRotation);
//                            WataLog.d("mapRatation=" + mapRatation.getRotation().getAngle());

                            break;

                        case DataCore.ON_TOUCH_MAP: // gathering listener
                            WataLog.i("ON_TOUCH_MAP");

//                            float x = _mapTouchListener.getPrevX();
//                            float y = _mapTouchListener.getPrevY();
//
//                            WataLog.d("x=====" + x + "///// y=====" + y);
//                            WataLog.i("!!!!!!!!!!!");
//                            WataLog.d("mLastPointX= " + mLastPointX);
//                            WataLog.d("mLastPointY= " + mLastPointY);
//                            // kcy-webview - 첫시작점 전달
//                            if (mLastPointX == 0.0 && mLastPointY == 0.0 || myWayListAdapter.getCount() == 0) {
////                                int heightPoint = (mHeightPixels / 5) * 3;
////                                updateTouch(x, y);
//
//                                Line line = ViewUtil.computeRayFromScreenPoint(_omapView.getO2mapInstance().getView(), (int) x, (int) y);
//                                Plane pl = new Plane(0.0, 0.0, 1.0, 0);
//
//                                Vector v = pl.intersect(line);
//                                _startPoint = new Vector(v.x, v.y, v.z);
//                                mWebBridge.StartPoint = _startPoint;
//
//                                WataLog.d("mWebBridge.StartPoint=" + mWebBridge.StartPoint);
//                                WataLog.d("_startPointt=" + _startPoint);
////                                Line line = ViewUtil.computeRayFromScreenPoint(_omapView.getO2mapInstance().getView(), mLastPointX, mLastPointY);
////                                Plane pl = new Plane(0.0, 0.0, 1.0, 0);
////                                Vector startVector = pl.intersect(line);
////                                _startPoint = new Vector(startVector.x, startVector.y, startVector.z);
//                                WataLog.i("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                                mLastPointX = (float) _startPoint.x;
//                                mLastPointY = (float) _startPoint.y;
//                                mRecordStartPointX = mLastPointX;
//                                mRecordStartPointY = mLastPointY;
//
////                                CAMERA_MOVING = true;
//                                if (startSettingModel == null) {
//                                    startSettingModel = new SandboxModel("startSettingModel");
//                                } else {
//                                    startSettingModel.removeAllElement();
//                                }
//
//                                Element start_el = ElementHelper.fromTextBillboard(new Vector(_startPoint.x, _startPoint.y, 5.0, 0), 1f, 1f, true, "S", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
//                                if (start_el != null) {
//                                    startSettingModel.addElement("startSettingModel", start_el);
//                                }
//                                ((CompositeModel) _omapView.getO2mapInstance().getModel()).addModel(startSettingModel);
//
//                                mRotation = 90;
//                                lineSelected = (Polyline) getMyWayLine((double) mLastPointX, (double) mLastPointY);
//                                //  라인그리기
//                                if (lineSelected != null) {
//                                    if (myWayLine == null) {
//                                        myWayLine = new SandboxModel("myWayLine");
//                                    }
//                                    myWayLine.removeAllElement();
//                                    Element eal = ElementHelper.fromPolyline(lineSelected, 0.1, 2, new Color(0, 255, 0, 255), false, false);
//                                    myWayLine.addElement("myWayLine" + mPointCount, eal);
//                                    ((CompositeModel) o2map.getModel()).addModel(myWayLine);
//                                }
//
//                                _omapView.getO2mapInstance().requestRedraw();
//
////                                lineSelected = (Polyline) getStartPointLine(_startPoint.x, _startPoint.y);
//                            } else {
//
//                            }

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


            Toast.makeText(PathDrawingActivity.this, " 수집완료." + durTime / 1000 + "초/" + min + "m/" + max + "m/" + avg + "m/" + (100 * sucRate / 30) + "%", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PathDrawingActivity.this, (pointCount) + " 포인트 수집.", Toast.LENGTH_SHORT).show();
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
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        WataLog.d("now=" + now);
        SimpleDateFormat format = new SimpleDateFormat("yy.MM.dd_HHmm", Locale.KOREA);
        String regDate = format.format(date);
        WataLog.d("regDate=" + regDate);
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
                    Toast.makeText(PathDrawingActivity.this, "db 없음", Toast.LENGTH_SHORT).show();
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
            mToast.cancel();
            mToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);

        }
        mToast.setGravity(which, 0, 0);
        mToast.show();
    }


    private JSONObject mPoiListObject = new JSONObject();
    private JSONArray mPoiArray = new JSONArray();

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

            SaveGatheredData.saveData(StaticManager.getResultPOIPath(), "POI.json", poiJsonRecord);

        } catch (JSONException e) {
            WataLog.d("Failed to create JSONObject =" + e);
        }

//        SaveGatheredData.saveData(StaticManager.getResultPOIPath(), "POI.log", poiJson);
//        SaveGatheredData.saveData(StaticManager.getResultPOIPath(), "POI.temp", poiJson);
    }


    private JSONObject mLineListObject = new JSONObject();
    private JSONArray mLineArray = new JSONArray();

    private void setLineInfo(String startX, String startY, String endX, String endy, String latitude, String longitude) {
        String lineJson = "";
        String lineJsonRecord = "";
        try {
            final JSONObject lineObject = new JSONObject();
            lineObject.put("start_pointx", startX);
            lineObject.put("start_pointy", startY);
            lineObject.put("end_pointx", endX);
            lineObject.put("end_pointy", endy);
            lineObject.put("latitude", latitude);
            lineObject.put("longitude", longitude);

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


/// ========================= 신규 UI 적용 ==========================


    private RelativeLayout recordTypeLayout, recordLineNumLayout, directionTextLayout, swipeBtnLayout, recordViewLyaout, optionBtn;
    private TextView recordTypeText, recordLineNumText, recordStepCount, recordStepLength, directionText, recordViewText;
    private ImageView poiModeImg, compassModeImg, recordView, precision_btn;
    private LinearLayout undoBtn, settingBtn, green_signal, red_signal;
    private View pathOption, recordLine, poiLine, poiModeImgBtn, bt_compass_btn;
    private View greenSignalIcon, redSignalIcon;
    private long mStepTempTime = 0;
    private int mFastStepCount = 0;
    private boolean isRecording = false;

    private boolean RECORD_CHECK = false;

    private int mLineNum = 0;
    private int mMotionCount = 0;

    // 역방향 기록하기에서 webview전달하는 시작점 끝점 정보저장
    private String mReversSpX, mReversSpY, mReversEpX, mReversEpY;

    private void initNewLayout() {
        View new_path_drawing = findViewById(R.id.new_path_drawing);
        webViewSetting();

        SwipeAnimationButton bsb = (SwipeAnimationButton) findViewById(R.id.swipe_btn);
        bsb.setOnSwipeAnimationListener(new SwipeAnimationListener() {
            @Override
            public void onSwiped(String position, String isRecord) {
                if (Constance.CENTER_BTN.equals(position)) {

                    WataLog.i("isRecord=" + isRecord);
                    WataLog.i("SwipeAnimationButton.MODE_CHECK=" + SwipeAnimationButton.MODE_CHECK);
                    if (isRecord.equals(Constance.START_RECORD)) {
                        // 기록시작
                        if (mLastPointX == 0.0 && mLastPointY == 0.0) {
                            noneStartPoint();
                        } else {
                            WataLog.i(" 경로 기록 시작 ");
                            WataLog.d("progress=" + progress.getVisibility());

                            angle_layout.setVisibility(View.GONE);
                            mCurrentPath = mRecordPosition + 1;
                            isRecording = false;
                            mStopStep = false;
                            RECORD_CHECK = true;
                            setPrecision(ON_PRECISION_MODE);

                            if (Locale.getDefault().getLanguage().equals("ko")) {
                                swipeBtnLayout.setBackgroundResource(R.mipmap.bg_finish);
                            } else {
                                swipeBtnLayout.setBackgroundResource(R.mipmap.bg_finish_en);
                            }
                            onRecordStart();
                            startRedSignal(true);
                            directionTextLayout.setVisibility(View.VISIBLE);
                            WataLog.d("isInverse=" + isInverse);

                            progress.setVisibility(View.VISIBLE);

                            if (!isInverse) {
                                directionText.setText(getString(R.string.direction));

                                int recordSize = myWayListAdapter.getCount();
                                onRecordView(true, false, String.valueOf(recordSize + 1));

                                mRecordStartPointX = mLastPointX;
                                mRecordStartPointY = mLastPointY;

                                mStartEndPointX_S = getAbsoluteStartPoint((float) mLastPointX, true);
                                mStartEndPointY_S = getAbsoluteStartPoint((float) mLastPointY, false);
                                mLineStartPointX = mStartEndPointX_S;
                                mLineStartPointY = mStartEndPointY_S;

                                WataLog.d("mStartPointX_S=" + mStartEndPointX_S + "// mStartPointY_S=" + mStartEndPointY_S);
                                WataLog.d("mRecordStartPointX=" + mRecordStartPointX + "// mRecordStartPointY=" + mRecordStartPointY);
                                WataLog.d("mRelativeSPointX=" + mRelativeSPointX + "// mRelativeSPointY=" + mRelativeSPointY);

                                if (myWayListAdapter.getCount() > 0) {
                                    WataLog.i("!!!!!!@1111111@");
                                    setWebView("initStartPt");
                                    setPreviewLine();
                                } else {
                                    WataLog.i("!!!!!!@22222222@");
                                    setWebView("startSignalGethering");
                                    setWebView("initStartPt");
                                }
                            } else {
                                directionText.setText(getString(R.string.reverse));

                                onRecordView(true, true, String.valueOf(mLineNum));

                                mRecordStartPointX = mLastPointX;
                                mRecordStartPointY = mLastPointY;

                                mStartEndPointX_S = getAbsoluteStartPoint((float) mLastPointX, true);
                                mStartEndPointY_S = getAbsoluteStartPoint((float) mLastPointY, false);

                                WataLog.d("mRecordStartPointX=" + mRecordStartPointX + "// mRecordStartPointY=" + mRecordStartPointY);
                                WataLog.d("mStartPointX_S=" + mStartEndPointX_S + "// mStartPointY_S=" + mStartEndPointY_S);

                                WataLog.i("!!!!!!@3333333@");
                                setWebView("initStartPt");
//                                    setPreviewLine();
                            }
                        }

                    } else if (isRecord.equals(Constance.STEP_RECORD)) {
                        WataLog.d("progress.getVisibility()=" + progress.getVisibility());
//                        if( progress_layout.getVisibility() == View.GONE) {
//                            WataLog.i("check");
//                        } else

                        if (mLastPointX == 0.0 && mLastPointY == 0.0) {
                            noneStartPoint();
                        } else if (progress.getVisibility() == View.VISIBLE) {
                            onToast(Gravity.CENTER, getString(R.string.on_search_wifi_wait_message));
                        } else if (!mStopStep) {
                            WataLog.i(" 스텝 ");
                            newPdralgorithm.getsensordata(getApplicationContext());
                            _UIHandler.sendEmptyMessage(DataCore.PDR_STAT_CHANGED);
//                            _omapView.getO2mapInstance().requestRedraw();

                            WataLog.d("isInverse=" + isInverse);
                            if (!isInverse) {
                                // signal// 신호 (빨간색)   pos// 내위치    epos // 검은색으로 표시되는 편집점
                                WataLog.d("mLastPointX=" + mLastPointX + "// mLastPointY=" + mLastPointY);
                                WataLog.d("mRecordStartPointX=" + mRecordStartPointX + "// mRecordStartPointY=" + mRecordStartPointY);

                                String poinX = getAbsoluteStartPoint(mLastPointX, true);
                                String poinY = getAbsoluteStartPoint(mLastPointY, false);
//                            mLastPointX 스텝지점
                                onStepWebview(poinX, poinY, "pos");

                                WataLog.d("(getAbsoluteStartPoint(mLastPointX, true)=" + (getAbsoluteStartPoint(mLastPointX, true)));
                                WataLog.d("(getAbsoluteStartPoint(mLastPointY, true)=" + (getAbsoluteStartPoint(mLastPointY, false)));

//                            WataLog.d("mNowPointX=" + mNowPointX);
//                            WataLog.d("mNowPointY=" + mNowPointY);
//                            mNowPointX  지점위치
                                onStepWebview((getAbsoluteStartPoint(mNowPointX, true)), (getAbsoluteStartPoint(mNowPointY, false)), "signal");

                            } else {
                                String poinX = getAbsoluteStartPoint(mLastPointX, true);
                                String poinY = getAbsoluteStartPoint(mLastPointY, false);

                                onReverseStepWebview(poinX, poinY, "pos", mReversSpX, mReversSpY, mReversEpX, mReversEpY);
                                onReverseStepWebview((getAbsoluteStartPoint(mNowPointX, true)), (getAbsoluteStartPoint(mNowPointY, false)), "signal", mReversSpX, mReversSpY, mReversEpX, mReversEpY);
                            }
//
                            long now = System.currentTimeMillis();
                            WataLog.d("now=" + now);
                            WataLog.d("mStepTempTime=" + mStepTempTime);

                            if (mStepTempTime + 1000 > now) {
                                mFastStepCount++;
                            } else {
                                mFastStepCount = 0;
                            }

                            if (mFastStepCount > 5) {
                                if (startAnimation != null) {
                                    startAnimation.cancel();
                                }
//                            startAnimation.reset();
                                onToast(Gravity.CENTER, getString(R.string.fast_step_slow_step_message));
                                startRedSignal(false);
                                mFastStepCount = 0;
                            } else {
                                if (startAnimation != null) {
                                    startAnimation.cancel();
                                }
                                startRedSignal(true);
                            }

                            WataLog.d("mFastStepCount=" + mFastStepCount);
                            mStepTempTime = now;
                        } else {
                            setReverseStop();
                        }

                    } else {

                    }

                } else if (Constance.RIGHT_BTN.equals(position)) {
                    WataLog.d("isRecording=" + isRecording);

                    RECORD_CHECK = false;
                    if (mLastPointX == 0.0 && mLastPointY == 0.0) {
                        noneStartPoint();
                    } else {
                        WataLog.d("isRecording=" + isRecording);
                        if (isRecording) {
                            WataLog.i("전송");
                            // 전송
//                            if (Locale.getDefault().getLanguage().equals("ko")) {
//                                swipeBtnLayout.setBackgroundResource(R.mipmap.bg_send);
//                            } else {
//                                swipeBtnLayout.setBackgroundResource(R.mipmap.bg_send_en);
//                            }
                            AlertDialog.Builder alBuilder = new AlertDialog.Builder(PathDrawingActivity.this);
                            alBuilder.setMessage(getString(R.string.send_file_message_1));
                            alBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    mStopStep = false;
//                                    onRecordView(false, true, "0");
//                                    directionTextLayout.setVisibility(View.GONE);
//
//                                    isRecording = true;
//                                    Intent intent = new Intent(PathDrawingActivity.this, GatherListActivity.class);
//                                    startActivity(intent);
                                }
                            });
                            alBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            });
                            alBuilder.show();
                        } else {
                            // 기록종료
                            WataLog.i("기록종료");
                            AlertDialog.Builder alBuilder = new AlertDialog.Builder(PathDrawingActivity.this);
                            alBuilder.setMessage(getString(R.string.finish_record_message));
                            alBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mStopStep = false;
                                    onRecordView(false, true, "0");
                                    directionTextLayout.setVisibility(View.GONE);

                                    setStopRecord();
                                    isRecording = true;
                                }
                            });
                            alBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SwipeAnimationButton.MODE_CHECK = Constance.STEP_RECORD;
                                    return;
                                }
                            });
                            alBuilder.show();
                        }
                    }
                } else if (Constance.LEFT_BTN.equals(position)) {
                    if (mLastPointX == 0.0 && mLastPointY == 0.0) {
                        noneStartPoint();
                    } else {
                        WataLog.i("지점체크");
                        AlertDialog.Builder alBuilder = new AlertDialog.Builder(PathDrawingActivity.this);
                        alBuilder.setMessage(getString(R.string.input_point_message));
                        alBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WataLog.i("지점");
                                String poinX = getAbsoluteStartPoint(mLastPointX, true);
                                String poinY = getAbsoluteStartPoint(mLastPointY, false);
                                WataLog.d("poinX=" + poinX);
                                WataLog.d("poinY=" + poinY);

                                onStepWebview(poinX, poinY, "epos", String.valueOf(POI_COUNT));

//                                setPointCheck(poinX, poinY);
                            }
                        });
                        alBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SwipeAnimationButton.MODE_CHECK = Constance.STEP_RECORD;
                                return;
                            }
                        });
                        alBuilder.show();
                    }
                }
            }
        });

        swipeBtnLayout = (RelativeLayout) new_path_drawing.findViewById(R.id.swipe_btn_layout);
        if (Locale.getDefault().getLanguage().equals("ko")) {
            swipeBtnLayout.setBackgroundResource(R.mipmap.bg_send);
        } else {
            swipeBtnLayout.setBackgroundResource(R.mipmap.bg_send_en);
        }

        RelativeLayout title_layout = (RelativeLayout) new_path_drawing.findViewById(R.id.title_layout);
        title_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                WataLog.i("터치 가로채기");
            }
        });

        // 기록타입
        recordTypeLayout = (RelativeLayout) new_path_drawing.findViewById(R.id.record_type_layout);
        recordTypeText = (TextView) new_path_drawing.findViewById(R.id.record_type_text);

        // 기록넘버
        recordLineNumLayout = (RelativeLayout) new_path_drawing.findViewById(R.id.record_line_num_layout);
        recordLineNumText = (TextView) new_path_drawing.findViewById(R.id.record_line_num_text);

        recordStepCount = (TextView) new_path_drawing.findViewById(R.id.record_step_count);
        recordStepLength = (TextView) new_path_drawing.findViewById(R.id.record_step_length);

        optionBtn = (RelativeLayout) new_path_drawing.findViewById(R.id.option_btn);
        optionBtn.setOnClickListener(this);
        // 기록방향
        directionTextLayout = (RelativeLayout) new_path_drawing.findViewById(R.id.direction_text_layout);
        directionText = (TextView) new_path_drawing.findViewById(R.id.direction_text);


        // poi mode
        poiModeImgBtn = (RelativeLayout) new_path_drawing.findViewById(R.id.poi_mode_img_layout);
        poiModeImgBtn.setOnClickListener(this);
        // compass mode
        bt_compass_btn = (RelativeLayout) new_path_drawing.findViewById(R.id.bt_compass_layout);
        bt_compass_btn.setOnClickListener(this);

        poiModeImg = (ImageView) findViewById(R.id.poi_mode_img);
        compassModeImg = (ImageView) findViewById(R.id.compass_mode_img);


        undoBtn = (LinearLayout) new_path_drawing.findViewById(R.id.ic_undo_btn);
        undoBtn.setOnClickListener(this);
        settingBtn = (LinearLayout) new_path_drawing.findViewById(R.id.ic_setting_btn);
        settingBtn.setOnClickListener(this);

        progress = (ProgressBar) new_path_drawing.findViewById(R.id.progress);

        greenSignalIcon = (View) new_path_drawing.findViewById(R.id.green_signal_icon);
        redSignalIcon = (View) new_path_drawing.findViewById(R.id.red_signal_icon);

        green_signal = (LinearLayout) new_path_drawing.findViewById(R.id.green_signal);
        red_signal = (LinearLayout) new_path_drawing.findViewById(R.id.red_signal);

        // 미세 각도 조절
        angle_layout = (LinearLayout) new_path_drawing.findViewById(R.id.angle_layout);
        precision_btn = (ImageView) new_path_drawing.findViewById(R.id.precision_btn);
        precision_btn.setOnClickListener(this);

        angle_control_layout = (LinearLayout) new_path_drawing.findViewById(R.id.angle_control_layout);
        ImageView angle_m_btn = (ImageView) new_path_drawing.findViewById(R.id.angle_m_btn);
        ImageView angle_p_btn = (ImageView) new_path_drawing.findViewById(R.id.angle_p_btn);

        RelativeLayout angle_p_btn_layout = (RelativeLayout) new_path_drawing.findViewById(R.id.angle_p_btn_layout);
        RelativeLayout angle_m_btn_layout = (RelativeLayout) new_path_drawing.findViewById(R.id.angle_m_btn_layout);

        angle_p_btn_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        angle_m_btn_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        angle_p_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                mCameraAngle += 1;
                WataLog.d("mCameraAngle=" + mCameraAngle);
//                setWebView("setRotation", String.valueOf(mCameraAngle));
            }
        });
        angle_p_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WataLog.d("event.getAction()=" + event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mMotionCount = 0;
                        mCameraAngle += 1;
                        WataLog.d("mCameraAngle=" + mCameraAngle);
                        setWebView("setRotation", String.valueOf(mCameraAngle));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mMotionCount > 10) {
                            mCameraAngle += 1;
                            WataLog.d("mCameraAngle=" + mCameraAngle);
                            setWebView("setRotation", String.valueOf(mCameraAngle));
                        } else {
                            mMotionCount++;
                        }
                        break;
                }
                return false;
            }
        });


        angle_m_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                mCameraAngle -= 1;
                WataLog.d("mCameraAngle=" + mCameraAngle);
//                setWebView("setRotation", String.valueOf(mCameraAngle));
            }
        });
        angle_m_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WataLog.d("event.getAction()=" + event.getAction());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mMotionCount = 0;
                        mCameraAngle -= 1;
                        WataLog.d("mCameraAngle=" + mCameraAngle);
                        setWebView("setRotation", String.valueOf(mCameraAngle));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mMotionCount > 10) {
                            mCameraAngle -= 1;
                            WataLog.d("mCameraAngle=" + mCameraAngle);
                            setWebView("setRotation", String.valueOf(mCameraAngle));
                        } else {
                            mMotionCount++;
                        }

                        break;
//                    case MotionEvent.ACTION_UP:

                }
                return false;
            }
        });

//        angle_m_btn.setOnClickListener(this);
//        angle_p_btn.setOnClickListener(this);

        // 옵션 layout
        pathOption = findViewById(R.id.path_drawing_option);

        ImageView arrow_left = (ImageView) pathOption.findViewById(R.id.arrow_left);
        arrow_left.setOnClickListener(this);

        RelativeLayout record_list_layout = (RelativeLayout) pathOption.findViewById(R.id.record_list_layout);
        record_list_layout.setOnClickListener(this);

        RelativeLayout poi_list_layout = (RelativeLayout) pathOption.findViewById(R.id.poi_list_layout);
        poi_list_layout.setOnClickListener(this);

        recordLine = (View) pathOption.findViewById(R.id.record_line);
        poiLine = (View) pathOption.findViewById(R.id.poi_line);


        // 기록 listview
        mPathDrawingListview = (ListView) pathOption.findViewById(R.id.record_listview);
        // poi listview
        mPoiListview = (ListView) pathOption.findViewById(R.id.poi_listview);

        setPrecision(ON_PRECISION_MODE);
    }

    private void onRecordView(boolean onRecord, boolean isReverse, String num) {
        if (onRecord) {
            recordViewLyaout.setVisibility(View.VISIBLE);
            if (!isReverse) {
                recordView.setImageResource(R.mipmap.ic_forward);
            } else {
                recordView.setImageResource(R.mipmap.ic_backward);
            }
            recordViewText.setText(num);
        } else {
            recordViewLyaout.setVisibility(View.GONE);
        }

    }

    private WebView mWebView;
    //    http://13.209.43.191:8080/watta_map/index.jsp?idx='리턴 id값'
    private String myUrl = "";
    private WebBridge mWebBridge;
    private int count = 0;

    private void webViewSetting() {
        // 웹뷰 셋팅팅
        mWebView = (WebView) findViewById(R.id.map_webview);
        mWebBridge = new WebBridge(PathDrawingActivity.this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAppCacheEnabled(false);

        mWebView.addJavascriptInterface(mWebBridge, "WATA");

        mWebView.setWebViewClient(new WebViewClientClass());
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
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
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


//        Button click_btn = (Button) findViewById(R.id.click_btn);
//        click_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mWebBridge.pointX = mPointArrlist.get(count);
//                mWebView.loadUrl("javascript:goStep()");
//                count++;
//                if(count>=19){
//                    count = 0;
//                }
//            }
//        });
    }


    private float mStartPointX = 0;
    private float mStartPointY = 0;

    private String mStartEndPointX_S = "";  // 끝점이자 시작점
    private String mStartEndPointY_S = "";

    private String mLineStartPointX = "";  //시작 할때마다 저장함 / 절대좌표
    private String mLineStartPointY = "";

    class WebBridge {
        public double startPointX = 0.0;
        public double startPointY = 0.0;

        public double endPointX = 0.0;
        public double endPointY = 0.0;
        public Vector StartPoint = null;
        public Vector EndPoint = null;

        private PathDrawingActivity pathDrawing;

        public WebBridge(PathDrawingActivity pathDrawingActivity) {
            pathDrawing = pathDrawingActivity;
        }

//        @JavascriptInterface
//        public void setStartPt(double x, double y){
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                }
//            });
//        }

        @JavascriptInterface
        public void setStartPt(String x, String y, final String x1, final String y1, final String x2, final String y2) {
            WataLog.d("x=" + x + "///y =" + y);
            WataLog.d("x1=" + x1 + "///y1 =" + y1);
            WataLog.d("x2=" + x2 + "///y2 =" + y2);

            mStartPointX = Float.parseFloat(x);
            mStartPointY = Float.parseFloat(y);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pathDrawing.setDrawStartLine(Float.parseFloat(x1), Float.parseFloat(y1), Float.parseFloat(x2), Float.parseFloat(y2));
                }
            });
        }

        @JavascriptInterface
        public void rotateEnd(final String angle) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Double rotation_d = Double.valueOf(angle);
                    int rotation_i = Integer.parseInt(String.valueOf(Math.round(rotation_d)));
                    pathDrawing.setRotation(rotation_i);
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
                    pathDrawing.setPoiText(pointX, pointY);
                }
            });
        }

        @JavascriptInterface
        public void overPoint() {
            WataLog.d("overPoint");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pathDrawing.setReverseStop();
                }
            });
        }

        @JavascriptInterface
        public Vector getStartPoint() {
            WataLog.d("StartPoint=" + StartPoint.x + "//" + StartPoint.y);
            return StartPoint;
        }

        @JavascriptInterface
        public void readyToDraw() {
            WataLog.d("readyToDraw");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pathDrawing.setLoadMapLine();
                }
            });
        }

        @JavascriptInterface
        public void setEPositionCoord(final String positionX, final String positionY, final String index) {
            WataLog.d("setEPositionCoord");
            WataLog.d("positionX=" + positionX);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pathDrawing.setEndPosition(positionX, positionY, index);
                }
            });
        }

        @JavascriptInterface
        public void setEPosXY(final String pointX, final String pointY) {
            WataLog.d("setEPosXY");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pathDrawing.setPointCheck(pointX, pointY);
                }
            });
        }
    }

    // 절대좌표 시작점
    private float mAbsoluteSPointX = 0;
    private float mAbsoluteSPointY = 0;

    // 상대좌표
    private float mRelativeSPointX = 0;
    private float mRelativeSPointY = 0;

    // 최초 시작점 확인
    private void setDrawStartLine(float x1, float y1, float x2, float y2) {
        // 상대좌표 받아서 절대좌표로 변경
        mAbsoluteSPointX = x1;
        mAbsoluteSPointY = y1;

        mRelativeSPointX = mStartPointX - x1;
        mRelativeSPointY = mStartPointY - y1;

        // 상대좌표 시작점저장
        mRecordStartPointX = mRelativeSPointX;
        mRecordStartPointY = mRelativeSPointY;

        WataLog.d("mRelativeSPointX=" + mRelativeSPointX + "//mRelativeSPointY=" + mRelativeSPointY);
        mLastPointX = mRelativeSPointX;
        mLastPointY = mRelativeSPointY;
        setAngle();

//        mWebView.loadUrl("javascript:test('Hello from Android')");
    }

    // 상대좌표를 절대좌표를 변경
    private String getAbsoluteStartPoint(float point, boolean pointX) {
        float Absolute = 0;
        if (pointX) {
            Absolute = point + mAbsoluteSPointX;
        } else {
            Absolute = point + mAbsoluteSPointY;
        }
        String relative = String.valueOf(String.format("%.5f", Absolute));
        WataLog.d("relative=" + relative);
        return relative;
    }

    // 상대좌표를 절대좌표를 변경
    private float getAbsolutetartPointFloat(float point, boolean pointX) {
        float relativePoint = 0;
        if (pointX) {
            relativePoint = point + mAbsoluteSPointX;
        } else {
            relativePoint = point + mAbsoluteSPointY;
        }
        String relative = String.valueOf(String.format("%.4f", relativePoint));
        relativePoint = Float.valueOf(relative);
        WataLog.d("relativePoint=" + relativePoint);
        return relativePoint;
    }

    // 상대좌표를 절대좌표를 변경
    private String getAbsoluteStartPoint(double pointX, double pointY) {
        float relativePointX = (float) pointX + mAbsoluteSPointX;
        float relativePointY = (float) pointY + mAbsoluteSPointY;
        String relativeX = String.valueOf(String.format("%.4f", relativePointX));
        String relativeY = String.valueOf(String.format("%.4f", relativePointY));

        WataLog.d(relativeX + "|" + relativeY);

        return relativeX + "|" + relativeY;
    }

    private double mRotation = 90; // 초기 각도값

    private void setAngle() {
        lineSelected = (Polyline) getPreviewLine(mRelativeSPointX, mRelativeSPointY);
        double tempX = lineSelected.getPart(0).getEndPoint().x + mAbsoluteSPointX;
        double tempY = lineSelected.getPart(0).getEndPoint().y + mAbsoluteSPointY;
        String endLineX = String.valueOf(String.format("%.4f", tempX));
        String endLiney = String.valueOf(String.format("%.4f", tempY));
//        WataLog.d("mStartPointX=" + mStartPointX);
//        WataLog.d("mStartPointY=" + mStartPointY);
//        WataLog.d("endLineX=" + endLineX);
//        WataLog.d("endLiney=" + endLiney);
        mWebView.loadUrl("javascript:drawStartLine('" + endLineX + "|" + endLiney + "')");
    }

    private void setPreviewLine() {
        if (lineSelected != null) {
            Polyline previewLine = (Polyline) getPreviewLine((float) lineSelected.getPart(0).getEndPoint().x, (float) lineSelected.getPart(0).getEndPoint().y);

            double tempX = previewLine.getPart(0).getEndPoint().x + mAbsoluteSPointX;
            double tempY = previewLine.getPart(0).getEndPoint().y + mAbsoluteSPointY;
            String endLineX = String.valueOf(String.format("%.4f", tempX));
            String endLiney = String.valueOf(String.format("%.4f", tempY));

//        WataLog.d("mStartPointX=" + mStartPointX);
//        WataLog.d("mStartPointY=" + mStartPointY);
//        WataLog.d("endLineX=" + endLineX);
//        WataLog.d("endLiney=" + endLiney);
            mWebView.loadUrl("javascript:drawStartLine('" + endLineX + "|" + endLiney + "')");
        }
    }

    private void setLoadMapLine() {
        WataLog.i("setLoadMapLine");
        WataLog.i("불러오기 모드");
        String lineJson = JsonUtils.INSTANCE.getData(Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pathGathering/PathDrawing/" + SAVE_FILE_NAME + "/LINE/" + "LINE.json");
        if (lineJson != null) {
            lineJson = lineJson.replace("\\", "");
            WataLog.d("lineJson = " + lineJson);
            setLineReader(lineJson);

            // 해당 좌표로 이동
            String poiJson = JsonUtils.INSTANCE.getData(Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pathGathering/PathDrawing/" + SAVE_FILE_NAME + "/POI/" + "POI.log");
            WataLog.d("poiJson=" + poiJson);
            if (poiJson != null) {
                poiJson = poiJson.replace("\\", "");
                WataLog.d("poiJson = " + poiJson);
                setPoiReader(poiJson);
            }
            setWebView("moveMapXY" + CENTER_POSITION_X + "|" + CENTER_POSITION_Y);

        } else {
            // 신규 지도
        }
    }

    private void setStopRecord() {
        WataLog.i("종료");
        WataLog.d("mLastPointX=" + mLastPointX);
        WataLog.d("mLastPointY=" + mLastPointY);

        // 종료위치 시작위치로 변경
        mStartEndPointX_S = getAbsoluteStartPoint(mLastPointX, true);
        mStartEndPointY_S = getAbsoluteStartPoint(mLastPointY, false);

        mRelativeSPointX = (float) mLastPointX;
        mRelativeSPointY = (float) mLastPointY;

        if (isMapRatation) {
            //자동회전
            setPrecision(true);
        } else {
            setPrecision(false);
        }

        angle_layout.setVisibility(View.VISIBLE);
//                            mStartPointX = getAbsolutetartPointFloat(mLastPointX, true);
//                            mStartPointY = getAbsolutetartPointFloat(mLastPointY, false);
        WataLog.d("mStartPointX_S=" + mStartEndPointX_S + "," + mStartEndPointY_S);

        onRecordStop();

        if (myWayListAdapter.getCount() > 0) {
            WataLog.i("2번째 기록부터");
            reverse_record_btn.setEnabled(true);
        }
        if (mSaveDegress == 0) { // 최초 한번만 받기위해
            mSaveDegress = mDegressSum;
        }

        WataLog.d("mSaveDegress=" + mSaveDegress);
        if (Locale.getDefault().getLanguage().equals("ko")) {
            swipeBtnLayout.setBackgroundResource(R.mipmap.bg_send);
        } else {
            swipeBtnLayout.setBackgroundResource(R.mipmap.bg_send_en);
        }

//                            setWebView("endDrawPoint"); // 좌표추가. 라인번호
        setWebView("endDrawPoint", mStartEndPointX_S + "|" + mStartEndPointY_S + "|" + mRecordPosition);
//                            onStepWebview(mStartPointX_S, mStartPointY_S, "epos");

        if (startAnimation != null) {
            startAnimation.cancel();
        }

        WataLog.d("isInverse=" + isInverse);
        if (!isInverse) {
            // 정방향
        } else {
            isInverse = false;
            setWebView("endReverse", mStartEndPointX_S + "|" + mStartEndPointY_S);
        }
    }


    private String start_x;
    private String start_y;
    private String finish_x;
    private String finish_y;

    private String latitude;
    private String longitude;

    private String CENTER_POSITION_X = "";
    private String CENTER_POSITION_Y = "";

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

                    start_x = jsonPath.getString("start_pointx");
                    start_y = jsonPath.getString("start_pointy");
                    finish_x = jsonPath.getString("end_pointx");
                    finish_y = jsonPath.getString("end_pointy");

                    if (i == 0) {
                        CENTER_POSITION_X = start_x;
                        CENTER_POSITION_Y = start_y;
                    }

                    WataLog.d("start_x=" + start_x + "// start_y=" + start_y + "/// finish_x=" + finish_x + "/// finish_y=" + finish_y);
                    setWebView("drawLineCoords", start_x + "|" + start_y + "|" + finish_x + "|" + finish_y + "|" + "line");

//                    setWebView("endDrawPoint", start_x + "|" + start_y + "|" + i);
//                    if(i == 0) {
//
//                    }
//                    onStepWebview(finish_x, finish_y, "epos");
                }

//                setMapLookPosition(mFileStartPointX, mFileStartPointY, 0);

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

    private void onStepWebview(String x, String y, String div) {
        WataLog.i("onStepWebview");
        mWebView.loadUrl("javascript:drawPoint('" + x + "|" + y + "|" + div + "|" + count + "')");
    }

    private void onStepWebview(String x, String y, String div, String count) {
        WataLog.i("지점 전용");
        mWebView.loadUrl("javascript:drawPoint('" + x + "|" + y + "|" + div + "|" + count + "')");
    }

    private void setWebView(String webId) {
        WataLog.i("webId=" + webId);
        mWebView.loadUrl("javascript:" + webId + "()");
    }

    private void setWebView(String webId, String value) {
        if (!"setRotation".equals(webId)) {
            WataLog.i("webId=" + webId + "//" + value);
        }
        mWebView.loadUrl("javascript:" + webId + "('" + value + "')");
    }

    private void webTestPoin(String webId) {
        WataLog.i("webId=" + webId);
        mWebView.loadUrl("javascript:testPoint('" + webId + "')");
    }

    // 역방향
    private void onReverseStepWebview(String x, String y, String div, String sPonitX, String
            sPonitY, String ePointX, String ePointY) {
        WataLog.i("onReverseStepWebview");
        mWebView.loadUrl("javascript:drawPoint('" + x + "|" + y + "|" + div + "|" + sPonitX + "|" + sPonitY + "|" + ePointX + "|" + ePointY + "')");
    }

    private String POI_pointX = "";
    private String POI_pointY = "";

    private int POI_COUNT = 1;

    private int mPoiCount = 0;

    // 지점체크
    private void setPointCheck(String poinX, String poinY) {
        mPoiInfoData.add(new PoiInfo(POI_COUNT, poinX, poinY, getString(R.string.point),"", ""));
        mPoiListAdapter.setItems(mPoiInfoData);
        mPoiListview.setAdapter(mPoiListAdapter);
        POI_COUNT++;
    }

    // poi등록
    private void setPoiPointMark(String x, String y, String poiText, boolean poiRight) {
        WataLog.i("setPoiPointMark");
        try {
            int pointCount = mPoiInfoData.size();
            mPoiInfoData.add(new PoiInfo(POI_COUNT, x, y, poiText,"", ""));
            mPoiListAdapter.setItems(mPoiInfoData);
            mPoiListview.setAdapter(mPoiListAdapter);
            // poi내용저장
        } catch (Exception e) {
            WataLog.e("Exception=" + e.toString());
        }
    }

    private void setPoiText(String pointX, String pointY) {
        POI_pointX = pointX;
        POI_pointY = pointY;
        poi_edit.setText("");
        WataLog.d("POI_COUNT=" + POI_COUNT);
        poiNum.setText("No.# " + String.valueOf(POI_COUNT));

        WataLog.d("POI_point", POI_pointX + "," + POI_pointY);
        poi_point_layout.setVisibility(View.VISIBLE);
    }

    // 이동할 경로를 미리보여준다.
    private Geometry getPreviewLine(float startPointX, float startPointY) {
        Polyline line = new Polyline();
        line.makeParts(1);

        Points pts = new Points();
        pts.makePoints(2);

        float lastPointX = startPointX, lastPointY = startPointY;
        double tempRatation = mRotation;

        tempRatation = 2 * 3.14 * (tempRatation / 360);

        lastPointX += Math.cos(tempRatation) * 500;
        lastPointY += Math.sin(tempRatation) * 500;

        updateTouch(lastPointX, lastPointY);

//        WataLog.d("startPoint= " + startPointX + "//" + startPointY);
//        WataLog.d("lastPoint= " + lastPointX + "//" + lastPointY);
        pts.setPoint(0, startPointX, startPointY, 0.0f);
        pts.setPoint(1, lastPointX, lastPointY, 0.0f);

        line.setParts(0, pts);

        return line;
    }

    // 라인 이어서 기록하기.
    private void drawContinuLine() {
        mWebView.loadUrl("javascript:drawContinuLine('" + (getAbsoluteStartPoint(mLastPointX, true)) + "|" + (getAbsoluteStartPoint(mLastPointY, false)) + "')");
    }

    private boolean mStopStep = false;

    // 역방향종료 메시지
    private void setReverseStop() {
        WataLog.i("setReverseStop");
        onToast(Gravity.CENTER, getString(R.string.stop_move_message));
        mStopStep = true;
    }


    private int testCount = 0;

    private void setRotation(int angle) {
//        WataLog.d("angle=" + angle);
        if (angle < 0) {
            angle = 360 + angle;
        } else if (angle > 360) {
            angle = angle % 360;
        }

        mRotation = angle + 90;
//        mRotation = (360 - angle) - 90 ;
//        WataLog.d("mRotation=" + mRotation);
        setAngle();
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            WataLog.d("url=" + url);
            view.loadUrl(url);
            return true;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (pathOption != null) {
                    WataLog.d("pathOption" + pathOption.getVisibility());
                    if (pathOption.getVisibility() == View.VISIBLE) {
                        pathOption.setVisibility(View.GONE);
                    } else {
                        AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
                        alBuilder.setMessage(getString(R.string.finish_record_message));
                        alBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
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
                    break;
                }
        }
        return true;
//        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
//            mWebView.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
    }

    private void noneStartPoint() {
        onToast(Gravity.CENTER, getString(R.string.start_point_check_message));
        SwipeAnimationButton.MODE_CHECK = Constance.STOP_RECORD;
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
            green_signal.setVisibility(View.VISIBLE);
            red_signal.setVisibility(View.GONE);
            greenSignalIcon.startAnimation(startAnimation);

        } else {
            green_signal.setVisibility(View.GONE);
            red_signal.setVisibility(View.VISIBLE);
            redSignalIcon.startAnimation(startAnimation);
        }
    }

    private String TEMP_START_POINTX = "";
    private String TEMP_START_POINTY = "";

    private void setEndPosition(String positionX, String positionY, String index) {
        WataLog.d("positionX=" + positionX);
        WataLog.d("positionY=" + positionY);
        WataLog.d("index=" + index);

//        String point[] = position.split("|");
//        int index = Integer.parseInt(point[2]) -1;
//        WataLog.d("index=" + index);

//        webTestPoin(positionX + "|" +positionY);

        TEMP_START_POINTX = positionX;
        TEMP_START_POINTY = positionY;

        myWayInfoData.get(Integer.valueOf(index) - 1).setEndPointX(positionX);
        myWayInfoData.get(Integer.valueOf(index) - 1).setEndPointY(positionY);
    }

    private void setPrecision(boolean mode) {
        if (mode) {
            ON_PRECISION_MODE = false;
            angle_control_layout.setVisibility(View.GONE);
            precision_btn.setImageResource(R.mipmap.bt_angle);
            setWebView("microAngleEnd");
        } else {
            ON_PRECISION_MODE = true;
            angle_control_layout.setVisibility(View.VISIBLE);
            precision_btn.setImageResource(R.mipmap.bt_angle_active);
            setWebView("microAngleStart");

        }
    }

}