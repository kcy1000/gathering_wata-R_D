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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.geotwo.LAB_TEST.Gathering.savedata.SaveGatheredData;
import com.geotwo.LAB_TEST.Gathering.ui.RecordListAdapter;
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
//import com.geotwo.o2mapmobile.shape.CSFReader;
import com.geotwo.o2mapmobile.shape.Geometry;
import com.geotwo.o2mapmobile.shape.Point;
import com.geotwo.o2mapmobile.shape.Points;
import com.geotwo.o2mapmobile.shape.Polyline;
import com.geotwo.o2mapmobile.util.Color;
import com.geotwo.o2mapmobile.view.ViewUtil;
import com.wata.LAB_TEST.Gathering.R;

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
import java.util.Arrays;
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
import pdr_collecting.core.pdrvariable;
import pdr_collecting.core.sensoract;
import pdr_collecting.core.steplenthestimation;

//import geo2.lbsp.ble.BLEManager;

//import geo2.lbsp.ble.BLEManager;

public class GatheringActivity extends Activity implements OnClickListener {
    static {
        System.loadLibrary("proj");
    }

    String TAG = "GatheringActivity";

    public static int s_mode = 0; // 0 : 이동 1 : POI편집 2 : 사진편집

    private TextView map_title, map_text_info, map_scan_count;
    private LinearLayout map_inner_frm_linear1, map_inner_frm_linear2;        //수집방법선택, 수집중 레이아웃 구분
    private LinearLayout map_bottom_linear1, map_bottom_linear2, map_bottom_linear3;            //수집방법내 경로수집, 두점/지점 수집 구분
    private Button gathering_select, map_path_select, map_path_start;
    private Button pathInverse, calBtn;
    //	private final int PATHINVERSE_BTN_ID = 99999;
    private Button map_back;
    private RelativeLayout _mapLayout = null;
    private RelativeLayout direction_layout;


    ImageButton _saveButton = null;
    ImageButton _exitButton = null;
    ImageButton _openSettingButton = null;
    ImageButton _confirmButton = null;
    ImageButton _cancleButton = null;
    ImageView _optionView = null;
    ImageView _settingView = null;

    double _prevAngle = 0;

    ///////////////////////////////////////////////////
    //O2MapSurfaceView _omapView;		// GLSufaceView
    WMapSurfaceView _omapView;        // GLSufaceView
    sensoract _senact = null;
    ///////////////////////////////////////////////////

    ButtonClickListener _clickListener = null;
    GatheringMapTouchListener _mapTouchListener = null;

    Polyline _curLine = null;
    static public Vector _startPoint = null;
    static public Vector _endPoint = null;
    PathManager _pathManager = null;

    Toast _gatheringEndToast = null;
    Thread _sensorthread = null;
    AlertDialog _gatheringEndDialog = null;

    boolean _isRouteDrawing = false;
    boolean _isGatherEnded = false;
    boolean _isHandlerEnded = true;

    static public Handler _UIHandler = null;
    static public boolean bIsGetFP = false;

    DataCore _dataCore = null;
    WifiScanner _wifiScanner = null;
    private com.geotwo.TBP.TBPWiFiScanner wifiScanner;            // 측위 버튼

    List<ScanResult> _list = null;

    String _buildName = "";
    String _floor = "";
    String _fileName = "";

    int _checkedItem = 0;
    int _prevWalkedDistance = 0;
    double _prevDegree = 0;

    float _prevX = 0;
    float _prevY = 0;

    ArrayList<PDI_REPT_COL_DATA> _SCAN_RESULT_TOTAL = null;
    PDI_REPT_COL_DATA _SCAN_RESULT_THIS_EPOCH = null;

    static public int pathFlag = 0;
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
    private SandboxModel myWayLine = null, myRecordLine = null;                // line 미리보기

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

    private TextView gatheringInfo = null;
    private TextView gatheringOrientationDegree = null;
    private ImageView gatheringOrientation = null;
    private int stepCount = 0;

    boolean isDrawingPathNumber = false;

    //hunyeon add code, 측정 후 재 측정 수정1, 경로번호를 저장한다.
    int _which = 0;

    private O2Map o2map;
    private EditText angle_text;
    private Button angle_p_btn, angle_m_btn, record_list_btn, reverse_record_btn, record_reset_btn, my_way_path_start, my_way_path_end, my_way_direction;
    private TextView now_angle, record_direction_text;
    private RelativeLayout record_lisetview, my_way_record_info_layout;
    private ListView my_way_listview;
    private Button list_ok_btn;
    private Polyline mLastPathPoint; //매회 최근경로

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WataLog.i("onCreate");
        // load settings
        isDrawingPathNumber = SettingDialog.isLinkNumberShowing(this);

        setBasePoint(); //Tag : base X, base Y Setting
        tff = Typeface.createFromAsset(this.getAssets(), "NanumBarunGothic.ttf"); //Font

        _dataCore = DataCore.getInstance();
        _dataCore.setFloorString(StaticManager.floorName);
        _dataCore.setBuildName(StaticManager.title);

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

        loadDataWCSF(); // Load json Data
        WataLog.d("base x=" + basePointX + "/y=" + basePointY);
        basePointX = 0.0;
        basePointY = 0.0;

        o2map = ((WMapSurfaceView) _omapView).getO2mapInstance();
        InputHandler han = o2map.getInputHandler();
        if (han instanceof MultiInputHandler) {
            MultiInputHandler mhan = (MultiInputHandler) han;
            if (mhan.getCurrentInputHandler() instanceof OrbitInputHandler) {

                ((OrbitInputHandler) mhan.getCurrentInputHandler()).setVerticalRotation(true);
            }
        }
    }

    private void setMapView() {
        _omapView.getO2mapInstance().renderLock();
        refreshNorthIcon();
        _omapView.getO2mapInstance().renderUnlock();

        String dataPath = Environment.getExternalStorageDirectory().getPath()
                + "/gathering/saveData/pathGathering/" + StaticManager.folderName + "/" + StaticManager.floorName + "/";

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
            setIndoorMap(StaticManager.folderName, basePointX, basePointY);

            if (pathFlag != 5) {
                Polyline line = null;
                for (int inx = 0; inx < wReader.getCount(); inx++) {
                    String pathName = String.format("%d", wReader.getPathId(inx));//fileList.get(inx).getName();
                    //if( mNumTest[i] == inx ) {

                    boolean mIsAdded = false;

                    if (testNumlayer == null) {
                        testNumlayer = new SandboxModel("testNumlayer");
                        mIsAdded = true;
                    }

                    line = (Polyline) wReader.getLine(inx);

                    Points pts = line.getPart(0);
                    Point start = pts.data[0];
                    Point end = pts.data[pts.getNumPoints() - 1];

                    WataLog.d("isDrawingPathNumber=" + isDrawingPathNumber);
                    if (isDrawingPathNumber) {
                        WataLog.i("chekc");
                        el = ElementHelper.fromText(2 * pathName.length(), 2.2f, pathName, 100, tff, new Color(255, 0, 0, 255));
                        el.getTransform().clearTransform();
                        el.getTransform().setTranslation(((start.x + end.x) / 2), ((start.y + end.y) / 2), 2);
                        el.getTransform().calcMatrix();
                        el.getExtentHelper().updateExtent();
                        WataLog.i("check1!");
                        testNumlayer.addElement("pointer" + pathName, el);

                        if (mIsAdded) {
                            ((CompositeModel) o2map.getModel()).addModel(testNumlayer);

                        }
                        WataLog.d("Add Pointer Num : pointer" + pathName);
                    }

                    // 기록경로 lin색상 표시
                    if (line != null) {
                        Element eal = ElementHelper.fromPolyline(line, 0.1, 2, new Color(0, 255, 0, 255), false, false);
                        prevPathLayer.addElement("prevPath" + pathName, eal);        // onCrete
                    }
                }
            }
            ((CompositeModel) o2map.getModel()).addModel(prevPathLayer);        // Add Layer
            _omapView.getO2mapInstance().renderUnlock();
        }
        _bleManager = new BLEManager(this);

        if (!_bleManager.isBluetoothEnabled()) {
            geo2.lbsp.ible.Utils.startBluetooth(this, new StartCompletedListener() {
                public void onStartCompleted() {
                    Toast.makeText(GatheringActivity.this, "'Bluetooth가 활성화 되었습니다.", Toast.LENGTH_SHORT).show();
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

//        myWayListAdapter = new MyWayListAdapter(this);
//        myWayListAdapter.setOnItemClickListener(new MyWayListAdapter.OnItemClickListner() {
//            @Override
//            public void onRecordClick(MyWayInfo items, int position) {
//                Toast.makeText(GatheringActivity.this, "수집 시작지점으로 이동해주세요.", Toast.LENGTH_SHORT).show();
//                isInverse = true;
//                WataLog.i("check!");
//                record_direction_text.setText("역방향");
//                mRecordPosition = position;
//
//                mRecordStartPointX = items.EndPointX;
//                mRecordStartPointY = items.EndPointY;
//                mLastPointX = items.StartPointX;
//                mLastPointY = items.StartPointY;
//
//                setReversePointSetting(items.EndPointX, items.EndPointY, items.StartPointX, items.StartPointY);
//            }
//        });
    }

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

        String mapPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/area/" + StaticManager.folderName;
        Element el = null;
        WataLog.d("area = " + area);
        switch (area) {
            case "Gangnam": //Tag : 시작 지점 표시
            {
                float WIDTH_SIZE = 240.0f;
                float HEIGHT_SIZE = 410.0f;

                if (pathFlag != 5) {
                    el = ElementHelper.fromImage(WIDTH_SIZE, HEIGHT_SIZE, mapPath + "/gangnam_map_all.png");
                } else {
                    el = ElementHelper.fromImage(WIDTH_SIZE, HEIGHT_SIZE, mapPath + "/gangnam_map_all_sample_1.png");
                }
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


    /*
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// load settings
		isDrawingPathNumber = SettingDialog.isLinkNumberShowing(this);

		setBasePoint(); //Tag : base X, base Y Setting
		tff=Typeface.createFromAsset(this.getAssets(),"NanumBarunGothic.ttf"); //Font
		// pdralgorithm.moved_distance = 0;

		_dataCore = DataCore.getInstance();

		//		Intent intent = getIntent();
		//		_buildName = intent.getStringExtra("station");
		//		_floor = intent.getStringExtra("floor");
		//		_fileName = intent.getStringExtra("fileName");
		//		_dataCore.setFloorString(_floor);
		//		_dataCore.setBuildName(_buildName);
		_dataCore.setFloorString(StaticManager.floorName);
		_dataCore.setBuildName(StaticManager.title);

		// 맵 지도 루트 설정
		//		initialmm.GetMapFile(Environment.getExternalStorageDirectory().getPath() + "/em3D/" + _fileName);

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

		// TODO : Ethan
		loadDataCSF(); //shf geo2 format


		Log.d("jeongyeol", "base x="+basePointX+"/y="+basePointY);
		O2Map o2map = ((O2MapSurfaceView)_omapView).getO2mapInstance();
		InputHandler han=o2map.getInputHandler();
		if(han instanceof MultiInputHandler) {
			MultiInputHandler mhan=(MultiInputHandler) han;
			if(mhan.getCurrentInputHandler() instanceof OrbitInputHandler) {

				((OrbitInputHandler)mhan.getCurrentInputHandler()).setVerticalRotation(true);
			}
		}

		_omapView.getO2mapInstance().renderLock();
		refreshNorthIcon(); 							//Tag : 나침반 침 북으로
		_omapView.getO2mapInstance().renderUnlock();

		String dataPath = Environment.getExternalStorageDirectory().getPath()
				+"/gathering/saveData/pathGathering/"+StaticManager.folderName+"/"+StaticManager.floorName+"/";

		fileList = getGatheringPathData(dataPath);
		mNumTest = new int[fileList.size()];

		for(int i=0;i<mNumTest.length;i++)
		{
			mNumTest[i] = 0;
			ArrayList<File> List = getGatheringPathData(dataPath+fileList.get(i).getName()+"/");
			//Log.d("jeongyeol", "path = "+dataPath+fileList.get(i).getName()+"/");
			if(List.size() > 0)
			{
				mNumTest[i] = Integer.parseInt(fileList.get(i).getName());
				//Log.d("jeongyeol", "log data name ="+List.get(0).getName());

				{
					boolean bAdd=false;
					if(prevPathLayer == null) {
						prevPathLayer = new SandboxModel("prevPathLayer");
						bAdd = true;
					}

					//if(prevPathLayer.size()>0) {
					//	prevPathLayer.removeElement(new String("prevPath"+i));
					//}

					Log.d(TAG, "/gathering/saveData/pathGathering/ : " + pathPath);
					reader = CSFReader.getReader(pathPath);
					if(reader != null)
					{
						_omapView.getO2mapInstance().renderLock();
						try {
							reader.open();
							//						reader.setScale(10, 10, 10);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						reader.moveFirst();
						Polyline line = null;
						for(int a=0; a<reader.getCount(); a++)
						{
							String strVal = reader.getFieldValue(0);
							int val = -1;
							if(strVal!=null)
							{
								val = Integer.valueOf(strVal);
								if(val==(Integer.parseInt(fileList.get(i).getName())))
								{
									boolean mIsAdded = false;
									if(testNumlayer == null)
									{
										testNumlayer = new SandboxModel("testNumlayer");
										mIsAdded = true;
									}
									line = (Polyline)reader.getGeometry();

									Points pts = line.getPart(0);
									Point start = pts.data[0];
									Point end = pts.data[pts.getNumPoints()-1];

									Element el = null;
									if(isDrawingPathNumber) {
										if (StaticManager.folderName.equalsIgnoreCase("office"))
											el = ElementHelper.fromText(0.6f * fileList.get(i).getName().length(), 0.9f, fileList.get(i).getName(), 100, tff, new Color(255, 0, 0, 255));
										else
											el = ElementHelper.fromText(2 * fileList.get(i).getName().length(), 2.2f, fileList.get(i).getName(), 100, tff, new Color(255, 0, 0, 255));
										el.getTransform().clearTransform();
										el.getTransform().setTranslation(((start.x + end.x) / 2), ((start.y + end.y) / 2), 2);
										el.getTransform().calcMatrix();
										el.getExtentHelper().updateExtent();

										testNumlayer.addElement("pointer" + fileList.get(i).getName(), el);
									}
									if(mIsAdded)
										((CompositeModel)o2map.getModel()).addModel(testNumlayer);
									break;
								}
							}

							reader.moveNext();
						}

						if(line!=null)
						{
							Element el=ElementHelper.fromPolyline(line, 0.1, 2, new Color(0,255,0,255), false, false);
//							Points pts = line.getPart(0);
//							if(pts != null)
//								addRoadLine(pts);
							prevPathLayer.addElement("prevPath"+fileList.get(i).getName(), el);
						}

						if(bAdd)
						{
							((CompositeModel)o2map.getModel()).addModel(prevPathLayer);
						}
						_omapView.getO2mapInstance().renderUnlock();
					}
				}

			}
			//else
			//	Log.d("jeongyeol", "log data for "+i+" is null");

		}

		_bleManager = new BLEManager(this);

		if(!_bleManager.isBluetoothEnabled())
		{
			geo2.lbsp.ible.Utils.startBluetooth(this, new StartCompletedListener()
			{
				public void onStartCompleted()
				{
					Toast.makeText ( GatheringActivity.this, "'Bluetooth가 활성화 되었습니다.", Toast.LENGTH_SHORT ).show();
				}
			});
		}

		_bleManager.connect(new ServiceReadyCallback()
		{
			public void onServiceReady()
			{
				if(_bleManager != null)
					_bleManager.startService();
			}
		});
//
//		_bleManager.connect(new geo2.lbsp.ble.BLEManager.ServiceReadyCallback()
//		{
//			public void onServiceReady()
//			{
//				if(_bleManager != null)
//					_bleManager.startService();
//			}
//		});

		if(StaticManager.folderName.equalsIgnoreCase("sadang"))
		{
			parseMapXml(Environment.getExternalStorageDirectory().getPath()+"/indoorMap/Map/S018/S018.xml");
			beaconManager = new BeaconManager(this);
			if(beaconManager.isBluetoothEnabled())
			{
				beaconManager.disconnect();
				try {
					beaconManager.stopMonitoring(ALL_ESTIMOTE_BEACONS_REGION);
					beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startEstimote();
			}
		}
		else {
			Log.d("jeongyeol", " not sadang");
		}
	}
*/
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

    protected Color getRandomColor(int i) {
        WataLog.d("i ==" + i);
        Color color = null;
        int colorType = i % 8;
        switch (colorType) {
            case 0:
                color = new Color(255, 0, 0, 255);
                break;
            case 1:
                color = new Color(0, 255, 0, 255);
                break;
            case 2:
                color = new Color(0, 0, 255, 255);
                break;
            case 3:
                color = new Color(255, 255, 0, 255);
                break;
            case 4:
                color = new Color(0, 255, 255, 255);
                break;
            case 5:
                color = new Color(255, 0, 255, 255);
                break;
            case 6:
                color = new Color(0, 0, 0, 255);
                break;
            case 7:
                color = new Color(255, 255, 255, 255);
                break;
            default:
                color = new Color(255, 255, 255, 255);
                break;
        }

        return color;
    }

    private SandboxModel currentPosLayer = null;
    private SandboxModel testPosLayer = null;

    protected void updatePointer(Vector v) {
        WataLog.i("updatePointer");
        O2Map o2map = (this._omapView.getO2mapInstance());

        o2map.renderLock();

        boolean bAdd = false;

        //currentPosLayer = (SandboxModel) ((CompositeModel)o2map.getModel()).getModelByName("currentPos");

        if (currentPosLayer == null) {
            currentPosLayer = new SandboxModel("currentPos");
            bAdd = true;

        }

        double pointerSize = 3;
        double pointerSize2 = 3;
        if (StaticManager.folderName.equalsIgnoreCase("office")) {
            pointerSize = 0.6;
            pointerSize2 = 0.6;
        }

        // Update Position : by Ethan
        Vector lookAt = v;//o2map.getView().getCamera().getLookAtPosition();
        o2map.getView().getCamera().setLookAtPosition(lookAt);
        Element el = ElementHelper.quadPyramidFromPoint(lookAt.x, lookAt.y, lookAt.z + 0.1, pointerSize, pointerSize, pointerSize2, Color.RED, Color.RED, Color.RED, false);
        WataLog.d("red: x" + lookAt.x + " y:" + lookAt.y + " z:" + lookAt.z);
        if (currentPosLayer.size() > 0) {
            currentPosLayer.removeElement("tester");
        }

//        currentPosLayer.addElement("tester", el);

        if (bAdd) {
            ((CompositeModel) o2map.getModel()).addModel(currentPosLayer);
        }
        o2map.renderUnlock();

    }

    protected void updateCurrentPath(Polyline line, int i) {
        WataLog.i("updateCurrentPath");
        O2Map o2map = (this._omapView.getO2mapInstance());

        boolean bAdd = false;
        if (twoPointerLayer == null) {
            twoPointerLayer = new SandboxModel("currentPath");
            bAdd = true;
        }

        //if(pointerLayer.size()>0) {
        //	pointerLayer.removeElement(new String("pointer"));
        //}
        if (line != null) {
            WataLog.i("fromPolyline check!!!");
            Element el = ElementHelper.fromPolyline(line, 0.1, 2, new Color(255, 0, 0, 255), false, false);
            WataLog.d("pointerPath_" + i);

            WataLog.i("check1!");
            twoPointerLayer.addElement("pointerPath_" + i, el);
        }

        if (bAdd)
            ((CompositeModel) o2map.getModel()).addModel(twoPointerLayer);
    }

    private PathManager _pathManager_for_nis;
    private ArrayList<String> _testpointName;
    private ArrayList<Node> _pathManager_list;
    private String _testTouchPoint = null;

    protected void loadDataWCSF() {
        WataLog.i("loadDataWCSF");
        pathPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/area/"
                + StaticManager.folderName + "/" + StaticManager.floorName + "_Path.json";                    // Path Data : Ethan

        // gathering/area/Gangnam/AB01_Path.json

        WataLog.d("pathPath = " + pathPath);
        String strJson = JsonUtils.INSTANCE.getData(pathPath);
        WataLog.d("strJson = " + strJson);

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

                if (isDrawingPathNumber) {
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

    /*
	protected void loadDataCSF()	{

		O2Map o2map =  this._omapView.getO2mapInstance();
		o2map.renderLock();
		o2map.getView().setNearFar(1, 100000);

		//Set Default Light
		Light light = new Light();
		//light.setDirectionalLight(new Vector(0.0d, 0.0d, -1.0d));
		light.setDirectionalLight(new Vector(0.0d, 1.0d, 1.0d).normalize3());
		//	    light.setPointLight(new Vector(184834d , 523644d, 100d));
		//light.setAmbient(new Color(1f, 1f, 1f, 1.0f));
		light.setAmbient(new Color(0.3f, 0.3f, 0.3f, 1.0f));
		light.setDiffuse(new Color(1f, 1f, 1f, 1.0f));
		light.setSpecular(new Color(0f, 0f, 0f, 1.0f));
		o2map.addLight(light);

		Light light2 = new Light();
		//light.setDirectionalLight(new Vector(0.0d, 0.0d, -1.0d));
		light2.setDirectionalLight(new Vector(-1.0d, 1.0d, 1.0d).normalize3());
		//	    light.setPointLight(new Vector(184834d , 523644d, 100d));
		//light.setAmbient(new Color(1f, 1f, 1f, 1.0f));
		light2.setAmbient(new Color(0.3f, 0.3f, 0.3f, 1.0f));
		light2.setDiffuse(new Color(0.6f, 0.6f, 0.6f, 1.0f));
		light2.setSpecular(new Color(0f, 0f, 0f, 1.0f));
		o2map.addLight(light2);

		Light light3 = new Light();
		//light.setDirectionalLight(new Vector(0.0d, 0.0d, -1.0d));
		light3.setDirectionalLight(new Vector(0d, 0d, 1.0d).normalize3());
		//	    light.setPointLight(new Vector(184834d , 523644d, 100d));
		//light.setAmbient(new Color(1f, 1f, 1f, 1.0f));
		light3.setAmbient(new Color(0.3f, 0.3f, 0.3f, 1.0f));
		light3.setDiffuse(new Color(0.6f, 0.6f, 0.6f, 1.0f));
		light3.setSpecular(new Color(0f, 0f, 0f, 1.0f));
		o2map.addLight(light3);

		((O2MapInstance)o2map).setUseLighting(useLight);
		((O2MapInstance)o2map).setBackgroundColor(new Color(215,215,215,255));

		//CSFReader reader=CSFReader.getReader(Environment.getExternalStorageDirectory().getPath() + "/Download/testPolygon.csf");
		//CSFReader reader=CSFReader.getReader(Environment.getExternalStorageDirectory().getPath() + "/Download/testLine2.csf");

		String pathBase=Environment.getExternalStorageDirectory().getPath() + "/gathering/area/"
				+StaticManager.folderName+"/"+StaticManager.floorName+"_Base.csf";
		String pathWall=Environment.getExternalStorageDirectory().getPath() + "/gathering/area/"
				+StaticManager.folderName+"/"+StaticManager.floorName+"_Wall.csf";
		pathPath=Environment.getExternalStorageDirectory().getPath() + "/gathering/area/"
				+StaticManager.folderName+"/"+StaticManager.floorName+"_Path.csf";					// Path Data : Ethan
		String pathPoi=Environment.getExternalStorageDirectory().getPath() + "/gathering/area/"
				+StaticManager.folderName+"/"+StaticManager.floorName+"_Poi.csf";
		String pointPath=Environment.getExternalStorageDirectory().getPath() + "/gathering/area/"
				+StaticManager.folderName+"/"+StaticManager.floorName+"_test.csf";

		//	    String pathBase=Environment.getExternalStorageDirectory().getPath() + "/Download/sadang_u2_Base.csf";
		//	    String pathWall=Environment.getExternalStorageDirectory().getPath() + "/Download/sadang_u2_Wall.csf";
		//	    String pathPath=Environment.getExternalStorageDirectory().getPath() + "/Download/sadang_u2_Path.csf";
		//	    String pathPoi=Environment.getExternalStorageDirectory().getPath() + "/Download/sadang_u2_Poi.csf";

		////////////////////////////////////////////////////////////
		reader=CSFReader.getReader(pathBase);
		if(reader != null)
		{
			SandboxModel testBaseLayer = new SandboxModel("test");
			try {
				reader.open();
				//			reader.setScale(10, 10, 10);
			} catch (Exception e1) {
				// TODO Auto-generated catch blocks
				e1.printStackTrace();
			}
			reader.moveFirst();

			for(int i=0;i<reader.getCount();i++) {

				Geometry geo=null;
				try {
					geo = reader.getGeometry();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Color color=null;

				String colorType=reader.getFieldValue(0);

				int iType=Integer.parseInt(colorType);

//
//				switch(iType) {
//				case 1:
//					color = new Color(171,222,251,255);
//					break;
//				case 2:
//					color = new Color(171,222,251,255);
//					break;
//				case 3:
//					color = new Color(171,222,251,255);
//					break;
//				case 4:
//					color = new Color(171,222,251,255);
//					break;
//				case 5:
//					color = new Color(171,222,251,255);
//					break;
//				case 6:
//					color = new Color(171,222,251,255);
//					break;
//				case 7:
//					color = new Color(171,222,251,255);
//					break;
//				case 8:
//					color = new Color(171,222,251,255);
//					break;
//				case 9:
//					color = new Color(171,222,251,255);
//					break;
//				default:
//					color = new Color(171,222,251,255);
//					break;
//				}

				color = new Color(251 ,249 ,199 ,255);
				if(iType != 20) {
					Element el=ElementHelper.planeFromPolygon((Polygon)geo, 0,color,color,Color.BLUE,useLight);
					String key=String.format("base%d",i);
					testBaseLayer.addElement(key, el);
				}

				reader.moveNext();
			}
			((CompositeModel)o2map.getModel()).addModel(testBaseLayer);
			//break;
		}

		////////////////////////////////////////////////////////////////////////////////////////////
		reader=CSFReader.getReader(pathWall);
		if(reader != null)
		{
//			SandboxModel wallLayer = new SandboxModel("wallLayer");
			SandboxModel wallLayerTop = new SandboxModel("wallLayerTop");
			try {
				reader.open();
				//			reader.setScale(10, 10, 10);
			} catch (Exception e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			reader.moveFirst();

			for(int i=0;i<reader.getCount();i++) {

				Geometry geo=null;
				try {
					geo = reader.getGeometry();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//			Color color=getRandomColor(i);
				//			Color color=new Color(32,32,32,255);
				//			Color color=new Color(92,92,92,255);
				//			Element el=ElementHelper.wallFromPolyline((Polyline)geo,10,color,color,Color.WHITE,useLight);
				String key=String.format("id%d",i);
				//			el.getTransform().clearTransform();
				//			el.getTransform().setTranslation(basePointX, basePointY, 10);
				//			el.getTransform().calcMatrix();
				//			el.getExtentHelper().updateExtent();
				//			wallLayer.addElement(key, el);

				//el=ElementHelper.lineFromPolyline((Polyline)geo, 100, 3,new Color(32,32,32,255), useLight);
				Element el=ElementHelper.fromPolyline((Polyline)geo, 0.2, 2,new Color(37,64,97,255), false, useLight);
				key=String.format("line%d",i);
				wallLayerTop.addElement(key, el);

				reader.moveNext();
			}
//			((CompositeModel)o2map.getModel()).addModel(wallLayer);
			((CompositeModel)o2map.getModel()).addModel(wallLayerTop);
			//break;
		}
		///////////////////////////////////////////////////////////////////

		// Draw Path : Ethan
		reader=CSFReader.getReader(pathPath);
		if(reader != null)
		{
			SandboxModel testPathLayer = new SandboxModel("testPath");
			try {
				reader.open();
				//			reader.setScale(10, 10, 10);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reader.moveFirst();
			_pathManager = new PathManager();
			_pathManager.setEpsilon(1);	//10cm

			for(int i=0;i<reader.getCount();i++) {

				Geometry geo=null;
				try {
					geo = reader.getGeometry();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Color color=null;
				color=new Color(12,59,146,255);

				Element el=ElementHelper.fromPolyline((Polyline)geo,0.1,1,color, false, useLight);
				String key=String.format("path%d",i);
				testPathLayer.addElement(key, el);

				String strVal = reader.getFieldValue(0);
				if(strVal!=null)
				{
					Log.d("jeongyeol", "line num ="+strVal);
					Polyline line = (Polyline)reader.getGeometry();

					Points pts = line.getPart(0);
					Point start = pts.data[0];
					Point end = pts.data[pts.getNumPoints()-1];

					boolean testmode = false; //시작점 종료점 표시를 위한 testmode
					if(testmode)
					{
						Element els= ElementHelper.fromTextBillboard(new Vector(start.x-0.5,start.y-0.5,0.3,0),1f,1f, true,"S",10.0f,Typeface.DEFAULT,new Color(44, 233, 41,255));

						Element ele= ElementHelper.fromTextBillboard(new Vector(end.x,end.y,0.7,0),1f,1f, true,"E",10.0f,Typeface.DEFAULT,new Color(255, 0, 0,255));

						testPathLayer.addElement("startpoint"+i, els);
						testPathLayer.addElement("endpoint"+i, ele);
					}

					if(isDrawingPathNumber) {
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
				}
				Polyline line=(Polyline)geo;

				Points pts=line.getPart(0);

				Vector[] v=new Vector[pts.getNumPoints()];

				for(int iPoint=0;iPoint<pts.getNumPoints();iPoint++) {
					v[iPoint]=new Vector(pts.data[iPoint].x,pts.data[iPoint].y,0,1);
				}

				_pathManager.addLink(v);

				reader.moveNext();
			}
			((CompositeModel)o2map.getModel()).addModel(testPathLayer);
			//break;
		}

		reader = null;
		reader=CSFReader.getReader(pointPath);
		if(reader != null)
		{
			filter30 = true;
			SandboxModel testPathLayer = new SandboxModel("testPoint");
			try {
				reader.open();
				//			reader.setScale(10, 10, 10);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			reader.moveFirst();
			_pathManager_for_nis = new PathManager();
			_pathManager_for_nis.setEpsilon(1);	//10cm

			_pathManager_list = new ArrayList<Node>();
			_testpointName = new ArrayList<String>();

			for(int i=0;i<reader.getCount();i++) {

				Geometry geo=null;
				try {
					geo = reader.getGeometry();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Color color=null;
				color=new Color(255,255,255,255);

				Element el;//=ElementHelper.fromPolyline((Polyline)geo,0.1,1,color, false, useLight);
//				String key=String.format("point%d",i);
//				testPathLayer.addElement(key, el);

				String strVal = reader.getFieldValue(0);
				String strtype = reader.getFieldValue(1);
//				if(strVal!=null)
				Point pt = (Point)reader.getGeometry();
//				if(strtype.equalsIgnoreCase(scanType))
				{
					if(strVal!=null)
					{
						el= ElementHelper.fromText(strVal.length(),1.2f,strVal,100, tff,new Color(0,0,0,255));
						el.getTransform().clearTransform();
						el.getTransform().setTranslation(pt.x+0.7, pt.y+0.7, 1);
						el.getTransform().calcMatrix();
						el.getExtentHelper().updateExtent();

						String key=String.format("test%d",i);
						testPathLayer.addElement(key, el);

						//					Points pts = line.getPart(0);
						//					Point start = pts.data[0];
						//					Point end = pts.data[pts.getNumPoints()-1];

						Color cType = color.RED;
						if(strtype.equalsIgnoreCase("1"))
							cType = new Color(255, 0, 0, 255);
						else if(strtype.equalsIgnoreCase("2"))
							cType = new Color(0, 0, 255, 255);

						if(StaticManager.folderName.equalsIgnoreCase("office"))
							el=ElementHelper.quadPyramidFromPoint(pt.x,pt.y,0.1,1,1,1.5f,cType,cType,cType,false);
							//el=ElementHelper.quadPyramidFromPoint(pt.x,pt.y,0.1,0.1f,0.1f,0.2f,Color.RED,Color.RED,Color.RED,false);
						else
							el=ElementHelper.quadPyramidFromPoint(pt.x,pt.y,0.1,1,1,1.5f,cType,cType,cType,false);

						//					el.getTransform().clearTransform();
						//					el.getTransform().setTranslation(((start.x+end.x)/2), ((start.y+end.y)/2), 2);
						//					el.getTransform().calcMatrix();
						//					el.getExtentHelper().updateExtent();

						testPathLayer.addElement("pointNum"+i, el);
						//				Polyline line=(Polyline)geo;

						//				Points pts=line.getPart(0);
						//
						//				Vector v=new Vector();

						//				for(int iPoint=0;iPoint<pt.getNumPoints();iPoint++) {
						//					v[iPoint]=new Vector(pt.data[iPoint].x,pt.data[iPoint].y,0,1);
						//				}
						//

						_pathManager_for_nis.addNode(pt.x, pt.y, 0);
						_pathManager_list.add(new Node(pt.x, pt.y, 0));
						_testpointName.add(strVal);

						Log.d("ejcha","testpointname add ?"+_testpointName.size()+"_"+strVal);
					}
				}
				reader.moveNext();
			}
			((CompositeModel)o2map.getModel()).addModel(testPathLayer);

			//break;
		}

		///poi text///////////////////////////////////////////////////////////////////////////////////
		reader=CSFReader.getReader(pathPoi);
		//poi 라벨
		if(reader != null)
		{
			SandboxModel poiLayer = new SandboxModel("poi");
			try {
				reader.open();
				//			reader.setScale(10, 10, 10);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			reader.moveFirst();
			for(int i=0;i<reader.getCount();i++) {

				Geometry geo=null;
				try {
					geo = reader.getGeometry();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Point p=(Point)geo;
				String v=reader.getFieldValue(0);

				Element el = ElementHelper.fromText(v.length()/2,1.2f,v,100, tff,new Color(18,51,140,255));
				el.getTransform().clearTransform();
				el.getTransform().setTranslation(p.x, p.y, 1);
				el.getTransform().calcMatrix();
				el.getExtentHelper().updateExtent();

				String key=String.format("text%d",i);
				poiLayer.addElement(key, el);

				reader.moveNext();
			}
			((CompositeModel)o2map.getModel()).addModel(poiLayer);
		}

		o2map.getView().getCamera().setDistance(((CompositeModel)o2map.getModel()).getExtentHelper().getExtent().getRadius()*2);
		o2map.getView().getCamera().rotateXAxis(15);

		Vector lookAt=((CompositeModel)o2map.getModel()).getExtentHelper().getExtent().getCenter();
		o2map.getView().getCamera().setLookAtPosition(lookAt);
//
//		SandboxModel pointerLayer = new SandboxModel("pointer");
//
//		double pointerSize=0.1;
//		Element el=ElementHelper.quadPyramidFromPoint(lookAt.x,lookAt.y,lookAt.z,pointerSize,pointerSize,0.2f,Color.RED,Color.RED,Color.RED,false);
//		pointerLayer.addElement("pointer", el);
//
//		((CompositeModel)o2map.getModel()).addModel(pointerLayer);
		o2map.renderUnlock();
	}
*/
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
        View v = inflater.inflate(R.layout.map_gathering_layout, null);

        gatheringInfo = (TextView) v.findViewById(R.id.gatheringInfo);
        gatheringOrientationDegree = (TextView) v.findViewById(R.id.gatheringOrientationDegree);
        gatheringOrientation = (ImageView) v.findViewById(R.id.gatheringOrientation);

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

        pathInverse = new Button(this);
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params1.setMargins(10, 0, 0, 10);
        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        pathInverse.setLayoutParams(params1);

        pathInverse.setText("정방향");
//		pathInverse.setId(PATHINVERSE_BTN_ID);
        pathInverse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                _clickListener.reversePath();
            }
        });
        pathInverse.setVisibility(View.INVISIBLE);
        _mapLayout.addView(pathInverse);
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
                    new AlertDialog.Builder(GatheringActivity.this)
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

        showRes = new Button(this);
        RelativeLayout.LayoutParams params6 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params6.setMargins(270, 10, 0, 0);
        params6.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params6.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        showRes.setLayoutParams(params6);

        showRes.setText("결과보기");
        showRes.setBackground(getResources().getDrawable(R.drawable.btn_click));
        showRes.setVisibility(View.INVISIBLE);
//		pathInverse.setId(PATHINVERSE_BTN_ID);
        showRes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showResultDialog();
            }
        });
        _mapLayout.addView(showRes);

//		Button bopock = new Button(this);
//		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		params2.setMargins(0, 0, 10, 10);
//		params2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		bopock.setLayoutParams(params2);
//		bopock.setText("보폭설정");
//		bopock.setOnClickListener(new OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				viewStepLenSetting();
//			}
//		});
//		_mapLayout.addView(bopock);

        // 측위 Button : Ethan
        startFP = new Button(this);
        RelativeLayout.LayoutParams params3 = new RelativeLayout.LayoutParams(170, LayoutParams.WRAP_CONTENT);
        params3.setMargins(0, 0, 20, 10);
        params3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        startFP.setLayoutParams(params3);
        startFP.setText("측위");
        startFP.setTextSize(12.0f);
        startFP.setBackground(getResources().getDrawable(R.drawable.btn_click));

        startFP.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bScanning) {

                    bScanning = true;
                    ((TextView) v).setText("측위중");
                    ((TextView) v).setBackground(getResources().getDrawable(R.drawable.btn_null_on));
                    scanThread = new Thread(new Runnable() {
                        public void run() {
                            while (bScanning) {
                                if (wifiScanner == null) {
                                    wifiScanner = new com.geotwo.TBP.TBPWiFiScanner(GatheringActivity.this);
                                    wifiScanner.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                    //			wifiManager.setWifiEnabled(false);

//									try {
//										int REQUEST_SCAN_ALWAYS_AVAILABLE = 100;
//										if (Build.VERSION.SDK_INT >= 18 && !wifiScanner.wifiManager.isScanAlwaysAvailable()) {
//											((Activity) GatheringActivity.this).startActivityForResult(
//													new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), REQUEST_SCAN_ALWAYS_AVAILABLE);
//
//											wifiScanner.wifiManager.setWifiEnabled(false);
//										}
//
//									} catch (Exception e) {
//
//									}

                                    IntentFilter NETWORK_STATE_LISTENER = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                                    NETWORK_STATE_LISTENER.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                                    NETWORK_STATE_LISTENER.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
                                    NETWORK_STATE_LISTENER.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
                                    NETWORK_STATE_LISTENER.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

                                    registerReceiver(wifiScanner, NETWORK_STATE_LISTENER);
                                    wifiScanner.ipAddress = _dataCore.getTpsURL();
                                    wifiScanner.port = _dataCore.getTpsPort();
                                }
                                wifiScanner.startScan();            // 측위 버튼 클릭 시 : by Ethan

                                while (!wifiScanner.isScanComplete) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                wifiScanner.sendData(GEO2.LBSP.SClient.SvrPositionProvider.COORDTYPE.emMiddleTM);
                                System.gc();
                                wifiScanner.isScanComplete = false;

                                // WifiScan per 1 sec  : Ethan
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    scanThread.start();
                } else {
//					if(wifiScanner != null)
//						unregisterReceiver ( wifiScanner );
                    bScanning = false;
                    if (mKf != null) {
                        mKf.init(null, null, 1);
                        mKf = null;
                    }
                    ((TextView) v).setText("측위");
                    ((TextView) v).setBackground(getResources().getDrawable(R.drawable.btn_click));
                }
            }
        });
        _mapLayout.addView(startFP);


        String getbScanning = getIntent().getStringExtra("bScanning");
        if (getbScanning != null) {
            bScanning = false;
            startFP.performClick();
        }

//		setFreq = new Button(this);
//		RelativeLayout.LayoutParams params11 = new RelativeLayout.LayoutParams(250, LayoutParams.WRAP_CONTENT);
//		params11.setMargins(14, 10, 0, 0);
//		params11.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//		params11.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		setFreq.setLayoutParams(params11);
//		if(iFrequency <= 0)
//			setFreq.setText("5GHz");
//		else
//			setFreq.setText("2.4GHz");
//		setFreq.setTextSize(12.0f);
//		setFreq.setBackground(getResources().getDrawable(R.drawable.btn_click));
//		setFreq.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				if(iFrequency <= 0 )
//				{
//					setFreq.setText("2.4GHz");
//					iFrequency = 2;
//				}else{
//					setFreq.setText("5GHz");
//					iFrequency = 0;
//				}
//			}
//
//		});
        //_mapLayout.addView(setFreq);

        startLog = new Button(this);
        RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(170, LayoutParams.WRAP_CONTENT);
        params4.setMargins(0, 0, 200, 10);
        params4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        startLog.setLayoutParams(params4);
        startLog.setText("기록");

        startLog.setTextSize(12.0f);
        startLog.setBackground(getResources().getDrawable(R.drawable.btn_click));
        startLog.setOnClickListener(new OnClickListener() {
            //ejcha
            @Override
            public void onClick(View v) {
                if (!bLogging) {
                    if (mStartDialog != null) {
                        mStartDialog.show();
                    } else {
                        mStartDialog = new GatheringStartDialog(GatheringActivity.this, new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                sCnt = mStartDialog.getScanCount();
                                if (sCnt > 0) {
                                    mStartDialog.cancel();
//									hitValue = Integer.valueOf(sCnt);
//								SharedPreferences pref = getSharedPreferences("stepPref", 0);
//								SharedPreferences.Editor prefEditor = pref.edit();
//								prefEditor.putInt("stepLength", stepLen);
//								prefEditor.commit();
//								double tempLen = pref.getInt("stepLength", stepLen)/100;
//								pdrvariable.setStep_length(tempLen);
//								Log.e("tag", "stepLen="+stepLen+", tempLen="+tempLen);
                                    startTime = SystemClock.uptimeMillis();
                                    sucRate = 0;
                                    min = 0;
                                    max = 0;
                                    avg = 0;
                                    var = 0;
                                    gatherCnt = 0;
                                    errorCnt = 0;
                                    mAf = null;
                                    durTime = -1;
                                    runLog();
                                } else {
                                    Toast.makeText(GatheringActivity.this, "스캔횟수를 입력해주세요", Toast.LENGTH_LONG).show();
                                }

                            }
                        });
                        mStartDialog.show();
                    }
                } else {
                    ((TextView) v).setText("기록");
                    runLog();
                }
            }

        });

//		{
//			@Override
//			public void onClick(View v)
//			{
//				if(!bLogging)
//				{
//					((TextView)v).setText("기록중");
//					final String[] arr = {"10","20","30","40","50"};
//					new AlertDialog.Builder(GatheringActivity.this)
//					.setTitle("적중거리설정(m)")
//					.setItems(arr, new DialogInterface.OnClickListener()
//					{
//						@Override
//						public void onClick(DialogInterface dialog, int which)
//						{
//							String strLen = arr[which];
//							if(strLen!=null)
//							{
//								hitValue = Integer.valueOf(strLen);
////								SharedPreferences pref = getSharedPreferences("stepPref", 0);
////								SharedPreferences.Editor prefEditor = pref.edit();
////								prefEditor.putInt("stepLength", stepLen);
////								prefEditor.commit();
////								double tempLen = pref.getInt("stepLength", stepLen)/100;
////								pdrvariable.setStep_length(tempLen);
////								Log.e("tag", "stepLen="+stepLen+", tempLen="+tempLen);
//								startTime = SystemClock.uptimeMillis();
//								sucRate = 0;
//								min = 0;
//								max = 0;
//								avg = 0;
//								errorCnt = 0;
//								mAf = null;
//								durTime = -1;
//								runLog();
//							}
//						}
//					})
//					.show();
//				}
//				else
//				{
//					((TextView)v).setText("기록");
//					runLog();
//				}
//			}
//		});
        _mapLayout.addView(startLog);
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
                            wifiScanner = new com.geotwo.TBP.TBPWiFiScanner(GatheringActivity.this);
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
            Toast.makeText(GatheringActivity.this, " 수집종료.", Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(GatheringActivity.this)
                .setTitle("보폭설정")
                .setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strLen = arr[which];
                        int stepLen = 0;
                        if (strLen != null) {
                            stepLen = Integer.valueOf(strLen);
                            SharedPreferences pref = getSharedPreferences("stepPref", 0);
                            SharedPreferences.Editor prefEditor = pref.edit();
                            prefEditor.putInt("stepLength", stepLen);
                            prefEditor.commit();
                            double tempLen = pref.getInt("stepLength", stepLen) / 100.0;
                            pdrvariable.setStep_length(tempLen);
//					Log.e("tag", "stepLen="+stepLen+", tempLen="+tempLen);
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

        SpinnableImageView spinnableImageView = new SpinnableImageView(this);
        spinnableImageView.setItemListener(new SpinnableImageView.onDegress() {
            @Override
            public void getDegress(double degrees) {
                WataLog.d("degrees=" + degrees);
            }
        });

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
//		if (_dataCore == null)
//			_dataCore = DataCore.getInstance();

//		_dataCore.setGatheringOption(DataCore.GATHER_OPTION_ROUTE);
        if (pathFlag >= 0)
            endGathering(false);

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
                        ((Activity) GatheringActivity.this).startActivityForResult(
                                new Intent(WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE), REQUEST_SCAN_ALWAYS_AVAILABLE);

                    }

                } catch (Exception e) {

                }
                _list = new ArrayList<ScanResult>();
                //				_wifiScanner = new WifiScanner(GatheringActivity.this, wifiManager, _UIHandler, _list);
                _wifiScanner = new WifiScanner(this, wifiManager, _UIHandler, _list, _bleManager);
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
        super.onBackPressed();
        DataCore.isOnGathering = false;
        finish();
    }

    private ImageButton mNorth;

    private void initButtons() {
        if (_dataCore == null)
            _dataCore = DataCore.getInstance();

        //		if (_bottomLayout == null)
        //			_bottomLayout = (LinearLayout) findViewById(R.id.gathering_bottom_bar);

        _clickListener = new ButtonClickListener();

        map_back = (Button) findViewById(R.id.map_back);
        map_back.setOnClickListener(_clickListener);
        gathering_select = (Button) findViewById(R.id.gathering_select);
        gathering_select.setOnClickListener(_clickListener);
        map_path_select = (Button) findViewById(R.id.map_path_select);
        map_path_select.setOnClickListener(_clickListener);
        map_path_start = (Button) findViewById(R.id.map_path_start);            // '시작' 버튼 by Ethan
        map_path_start.setOnClickListener(_clickListener);
//		mNorth = (ImageButton)findViewById(R.id.map_view_all);
//		mNorth.setOnClickListener(_clickListener);
//		findViewById(R.id.sensibility).setOnClickListener(_clickListener);
        findViewById(R.id.map_setting).setOnClickListener(_clickListener);
//		findViewById(R.id.map_start_point_cancel).setOnClickListener(_clickListener);
        findViewById(R.id.map_complete).setOnClickListener(_clickListener);
        findViewById(R.id.map_cancel).setOnClickListener(_clickListener);
    }

    private double mCurrAngle = 0;
    private double mPrevAngle = 0;
    private double mAddAngle = 0;

    private void initView(boolean isFirst) {
        map_title = (TextView) findViewById(R.id.map_title);
        map_title.setText(StaticManager.title + ", " + StaticManager.floorName);
        map_text_info = (TextView) findViewById(R.id.map_text_info);
        map_scan_count = (TextView) findViewById(R.id.map_scan_count);

        map_inner_frm_linear1 = (LinearLayout) findViewById(R.id.map_inner_frm_linear1);
        map_inner_frm_linear2 = (LinearLayout) findViewById(R.id.map_inner_frm_linear2);

        map_bottom_linear1 = (LinearLayout) findViewById(R.id.map_bottom_linear1);
        map_bottom_linear2 = (LinearLayout) findViewById(R.id.map_bottom_linear2);
        map_bottom_linear3 = (LinearLayout) findViewById(R.id.map_bottom_linear3);

        // kcy1000 - 레이아웃 셋팅
        directionImg = (ImageView) findViewById(R.id.direction_img);
        direction_layout = (RelativeLayout) findViewById(R.id.direction_layout);
        direction_layout.setVisibility(View.GONE);
        // 기록 방향설정
        Button d_0 = (Button) findViewById(R.id.d_0);
        Button d_45 = (Button) findViewById(R.id.d_45);
        Button d_90 = (Button) findViewById(R.id.d_90);
        Button d_135 = (Button) findViewById(R.id.d_135);
        Button d_180 = (Button) findViewById(R.id.d_180);
        Button d_225 = (Button) findViewById(R.id.d_225);
        Button d_270 = (Button) findViewById(R.id.d_270);
        Button d_315 = (Button) findViewById(R.id.d_315);


//        final ImageView test_img = (ImageView) findViewById(R.id.test_img);
//        test_img.setVisibility(View.GONE);

//        test_img.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                animate(test_img, mAddAngle, mAddAngle + mCurrAngle - mPrevAngle);
//            }
//        });

//        test_img.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent motionEvent) {
//                final float centerOfWidth = test_img.getWidth() / 2;
//                final float centerOfHeight = test_img.getHeight() / 2;
//                final float x = motionEvent.getX();
//                final float y = motionEvent.getY();
//
//                WataLog.d("motionEvent.getAction()=" + motionEvent.getAction());
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        mCurrAngle = Math.toDegrees(Math.atan2(x - centerOfWidth, centerOfHeight - y));
//                        WataLog.d("mCurrAngle=" + mCurrAngle);
//                        break;
//
//                    case MotionEvent.ACTION_MOVE:
//                        mPrevAngle = mCurrAngle;
//                        mCurrAngle = Math.toDegrees(Math.atan2(x - centerOfWidth, centerOfHeight - y));
//                        animate(test_img, mAddAngle, mAddAngle + mCurrAngle - mPrevAngle);
//                        mAddAngle += mCurrAngle - mPrevAngle;
//                        break;
//
//                    case MotionEvent.ACTION_UP:
//                        WataLog.i("ACTION_UP");
////                        performClick();
//                        break;
//
//                }
//                return false;
//            }
//        });



        d_0.setOnClickListener(this);
        d_45.setOnClickListener(this);
        d_90.setOnClickListener(this);
        d_135.setOnClickListener(this);
        d_180.setOnClickListener(this);
        d_225.setOnClickListener(this);
        d_270.setOnClickListener(this);
        d_315.setOnClickListener(this);

        record_lisetview = (RelativeLayout) findViewById(R.id.record_lisetview);
        my_way_listview = (ListView) findViewById(R.id.my_way_listview);
        list_ok_btn = (Button) findViewById(R.id.list_ok_btn);
        list_ok_btn.setOnClickListener(this);

        my_way_record_info_layout = (RelativeLayout) findViewById(R.id.my_way_record_info_layout);
        record_direction_text = (TextView) findViewById(R.id.record_direction_text); // 기록방향
        reverse_record_btn = (Button) findViewById(R.id.reverse_record_btn); //역방향 기록하기
        record_list_btn = (Button) findViewById(R.id.record_list_btn); // 기록 내역
        record_reset_btn = (Button) findViewById(R.id.record_reset_btn); // 기록지점 다시설정

        reverse_record_btn.setOnClickListener(this);
        record_list_btn.setOnClickListener(this);
        record_reset_btn.setOnClickListener(this);

        my_way_path_start = (Button) findViewById(R.id.my_way_path_start);
        my_way_path_end = (Button) findViewById(R.id.my_way_path_end);
        my_way_direction = (Button) findViewById(R.id.my_way_direction);
        my_way_path_start.setOnClickListener(this);
        my_way_path_end.setOnClickListener(this);
        my_way_direction.setOnClickListener(this);


        angle_text = (EditText) findViewById(R.id.angle_text);
        angle_text.setCursorVisible(false);
        angle_text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        //터치했을 때의 이벤트
                        WataLog.i("터치했을 때의 이벤트");
                        angle_text.setText("");
                        break;
                    }
                }

                return false;
            }
        });

        angle_p_btn = (Button) findViewById(R.id.angle_p_btn);
        angle_m_btn = (Button) findViewById(R.id.angle_m_btn);

        angle_p_btn.setOnClickListener(this);
        angle_m_btn.setOnClickListener(this);

        now_angle = (TextView) findViewById(R.id.now_angle);

        initButtons();

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

    private void setStatusString(String str) {
        map_text_info.setText(str);
    }

    // 수집데이터 저장 by Ethan
    //Tag : 취소 버튼은 save = false
    private void endGathering(boolean save) {
        WataLog.i("endGathering=" + save);
        wifiSet(false);
        sensorSet(false);
        lineSelected = null;
        bEndPoint = false;
//		nodeCnt = 0;
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

        DataCore.iGatherMode = DataCore.GATHER_MODE_NONE;
        //		_playButton.setBackgroundResource(R.drawable.bottom_btn03_2_play);
        //		_playButton.setTag(STOPED);

        _dataCore.insertGatheredData(_dataCore.getCurGatheringName(), _dataCore.getSCAN_RESULT_TOTAL());

        _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();
        WataLog.d("pathFlag = " + pathFlag + "/inverse=" + isInverse);
        WataLog.d("save=" + save);

        if (save) {
            if (pathFlag == 1) {
                File temp = new File(StaticManager.getResultPath());
                if (temp.exists()) {
                    String[] tempArr = temp.list();
                    for (int a = 0; a < tempArr.length; a++) {
                        String tempName = tempArr[a];
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
                if (!isInverse) //log저장
                {
                    SaveGatheredData.saveGatheredData(StaticManager.getResultPath(), "GEO2_" + _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
                    SaveGatheredData.saveSendableData(StaticManager.getResultPath(), "GEO2_" + _dataCore.getCurGatheringName() + "_F.temp", _SCAN_RESULT_THIS_EPOCH);
                } else {
                    SaveGatheredData.saveGatheredData(StaticManager.getResultPath(), "GEO2_" + _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
                    SaveGatheredData.saveSendableData(StaticManager.getResultPath(), "GEO2_" + _dataCore.getCurGatheringName() + "_R.temp", _SCAN_RESULT_THIS_EPOCH);
                }
            } else if (pathFlag == 2 || pathFlag == 3 || pathFlag == 4) {
                if (!isInverse) {
                    if (pathFlag != 4) {
                        SaveGatheredData.saveGatheredData(_dataCore.getLogDataPath(), "GEO2_" + _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
                        SaveGatheredData.saveSendableData(_dataCore.getTempDataPath(), "GEO2_" + _dataCore.getCurGatheringName() + "_F.temp", _SCAN_RESULT_THIS_EPOCH);
                    } else
                        SaveGatheredData.saveGatheredData(_dataCore.getLogDataPath() + "/viaPath/", "GEO2_" + _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
                } else {
                    if (pathFlag != 4) {
                        SaveGatheredData.saveGatheredData(_dataCore.getLogDataPath(), "GEO2_" + _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
                        SaveGatheredData.saveSendableData(_dataCore.getTempDataPath(), "GEO2_" + _dataCore.getCurGatheringName() + "_R.temp", _SCAN_RESULT_THIS_EPOCH);
                    } else
                        SaveGatheredData.saveGatheredData(_dataCore.getLogDataPath() + "/viaPath/", "GEO2_" + _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
                }
            } else if (pathFlag == 5) {
                // kcy1000 - 경로수집 저장 및 라인 그리기.

                File temp = new File(StaticManager.getResultPath());
                if (temp.exists()) {
                    String[] tempArr = temp.list();
                    for (int a = 0; a < tempArr.length; a++) {
                        String tempName = tempArr[a];
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
                if (!isInverse) //log저장
                {
                    SaveGatheredData.saveGatheredData(StaticManager.getResultPath(), "MYWAY_" + _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
                    SaveGatheredData.saveSendableData(StaticManager.getResultPath(), "MYWAY_" + _dataCore.getCurGatheringName() + "_F.temp", _SCAN_RESULT_THIS_EPOCH);
                } else {
                    SaveGatheredData.saveGatheredData(StaticManager.getResultPath(), "MYWAY_" + _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
                    SaveGatheredData.saveSendableData(StaticManager.getResultPath(), "MYWAY_" + _dataCore.getCurGatheringName() + "_R.temp", _SCAN_RESULT_THIS_EPOCH);
                }

            }
        }

        WataLog.d("saveLogForImage=" + saveLogForImage);
        WataLog.d("isInverse=" + isInverse);
        if (saveLogForImage) {
            if (!isInverse) {
                SaveGatheredData.saveLogForImage(StaticManager.getImageLogPath(), "IMAGE_LOG_" + _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
            } else {
                SaveGatheredData.saveLogForImage(StaticManager.getImageLogPath(), "IMAGE_LOG_" + _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
            }
            Toast.makeText(this, "영상정보수집로그 저장에 성공하였습니다.", Toast.LENGTH_SHORT).show();
        }

        if (pathFlag == 4)
            pathFlag = -1;
        _SCAN_RESULT_THIS_EPOCH = new PDI_REPT_COL_DATA(android.os.Build.SERIAL);
        _SCAN_RESULT_TOTAL = new ArrayList<PDI_REPT_COL_DATA>();

        //Tag : TOTAL 초기화
        _dataCore.setSCAN_RESULT_TOTAL(_SCAN_RESULT_TOTAL);
        _dataCore.setSCAN_RESULT_THIS_EPOCH(_SCAN_RESULT_THIS_EPOCH);

//		_settingLayout.setVisibility(View.INVISIBLE);
//		String total = "스캔 준비중";
//		setStatusString(total);

        WataLog.i("EEEEEEEEEEEEEEEE");
        isInverse = false;
        pathInverse.setText("정방향");
        DataCore.isOnGathering = false;

        _startPoint = null;
        _endPoint = null;
        //if(gatheringFlag)
        //{
        //	loadGatheringListActivity();
        //}

        //updateCurrentPath(null);

        //hunyeon add code, 측정 후 재 측정 수정2, 측정이 끝난 후 다시 측정
        //_which : 경로번호
        if (save == true && pathFlag != 3) {
            WataLog.i("check1");
            delIcon(); //아이콘 제거
            _clickListener.viewEachPath(_which); //path 재설정(?)
            map_path_start.setVisibility(View.VISIBLE); //path 설정 버튼 가시화
        } else if (pathFlag == 3) {
            setStatusString("");
        }
    }

    //	private Polyline[] BackupLine;
    private int PreIndex = -1;
    static public double lineHeader = Double.NaN;

    private void startGathering(Polyline line) {                            // Start Click : by Ethan
        WataLog.d("line = " + line);
        stepCount = 0;
        gatheringInfo.setText("");
        gatheringOrientationDegree.setText("");
        gatheringOrientation.setRotation(0);

        if (bLogging || bScanning) {
            Toast.makeText(this, "측위를 종료해 주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        this._omapView.getO2mapInstance().renderLock();
        PreIndex++;
        WataLog.d("PreIndex=" + PreIndex);
        updateCurrentPath(line, PreIndex);

        this._omapView.getO2mapInstance().renderUnlock();

        _startPoint = new Vector(line.getPart(0).getStartPoint().x, line.getPart(0).getStartPoint().y, line.getPart(0).getStartPoint().z);
        _endPoint = new Vector(line.getPart(0).getEndPoint().x, line.getPart(0).getEndPoint().y, line.getPart(0).getEndPoint().z);
        endX = _endPoint.x;
        endY = _endPoint.y;
        distRemain = -1;
        WataLog.d("_startPoint =" + _startPoint);
        lineHeader = getHeader(_startPoint, _endPoint);

        pdrvariable.initValues();
        initialmm.MM_initialize2(line);
        wifiSet(true);
        sensorSet(true);

        //GatheringDialog.makeGatherNameInsertDailog(GatheringActivity.this, StaticManager.title, StaticManager.floorName, _UIHandler, 9999).show();
        final String curTime = AndroidUtils.getCurrentTime();
        final String autoName = StaticManager.title + "-" + StaticManager.floorName + "-" + curTime;

        DataCore dataCore = DataCore.getInstance();

        String insertText = autoName;

        dataCore.setCurGatheringName(insertText);
        dataCore.setCurGatherStartTime(curTime);
        _UIHandler.sendEmptyMessage(9999);
    }

    /*
	private void startGathering(Polyline line) {
		stepCount = 0;
		gatheringInfo.setText("");
		gatheringOrientationDegree.setText("");
		gatheringOrientation.setRotation(0);

		if(bLogging || bScanning)
		{
			Toast.makeText(this, "측위를 종료해 주세요", Toast.LENGTH_SHORT).show();
			return;
		}
		this._omapView.getO2mapInstance().renderLock();

//		if(BackupLine == null)
//		{
//			BackupLine = new Polyline[10];
//			PreIndex = 0;
//		}
		PreIndex++;
		updateCurrentPath(line, PreIndex);
//		BackupLine[PreIndex] = new Polyline();
//		if(pathFlag!=1)
//		{
//			//
//			BackupLine[PreIndex] = line;
//			PreIndex ++;
//			if(PreIndex > 0)
//			{
//				for(int i=0;i<PreIndex;i++)
//					updateCurrentPath(BackupLine[i], i);
//			}
//		}
		this._omapView.getO2mapInstance().renderUnlock();

		_startPoint = new Vector(line.getPart(0).getStartPoint().x, line.getPart(0).getStartPoint().y, line.getPart(0).getStartPoint().z);
		_endPoint = new Vector(line.getPart(0).getEndPoint().x, line.getPart(0).getEndPoint().y, line.getPart(0).getEndPoint().z);
		endX = _endPoint.x;
		endY = _endPoint.y;
		distRemain = -1;

		lineHeader = getHeader(_startPoint, _endPoint);

		if(pathFlag == 4)
		{
			touchedPoint = _startPoint;
			bLogging = true;
			if(posProvider == null)
			{
				String dbFileName = Environment.getExternalStorageDirectory().getPath() + "/gathering/area/"
						+StaticManager.folderName+"/FP.db";

				File geo2DB = new File(dbFileName);
				if(!geo2DB.exists())
				{
					Toast.makeText(GatheringActivity.this, "db 없음", Toast.LENGTH_SHORT).show();
				}
				else
				{
					try {
						posProvider = new PositionProvider(dbFileName, "", "");
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			addRoadLine(line.getPart(0));
//			if(m_pdrinter == null)
//			{
//				String jarPath = Environment.getExternalStorageDirectory().getPath()+"/kist_pdr_core.jar";
//				File jarFile = new File(jarPath);
//				if(jarFile.exists())
//				{
//					//PDR알고리즘 동적로딩
//					try{
//						PDRJarLoader j = new PDRJarLoader(GatheringActivity.this);
//						m_pdrinter = (pdr_interface)j.Load("kist_pdr_core.jar", "pdr.inter.function_core");
//						Log.i("PDRJAR", "PDR Jar파일 로딩 성공 ");
//					}
//					catch (Exception e) {
//						Log.i("PDRJAR", "PDR Jar파일 로딩 실패 : " + e.toString());
//						e.printStackTrace();
//					}
//					if(m_pdrinter != null)
//						m_pdrinter.refer_sensor_act(getApplication(), _UIHandler);
//				}
//				else
//				{
//					Toast.makeText(GatheringActivity.this, "jar 없음", Toast.LENGTH_SHORT).show();
//					return;
//				}
//			}

		}
		pdrvariable.initValues();
		initialmm.MM_initialize2(line);
		wifiSet(true);
		sensorSet(true);

		//GatheringDialog.makeGatherNameInsertDailog(GatheringActivity.this, StaticManager.title, StaticManager.floorName, _UIHandler, 9999).show();
		final String curTime = AndroidUtils.getCurrentTime();
		final String autoName = StaticManager.title + "-" + StaticManager.floorName + "-" + curTime;

		DataCore dataCore = DataCore.getInstance();

		String insertText = autoName;

		dataCore.setCurGatheringName(insertText);
		dataCore.setCurGatherStartTime(curTime);
		_UIHandler.sendEmptyMessage(9999);
	}
*/
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
        Intent intent = new Intent(GatheringActivity.this, GatherListActivity.class);
        startActivity(intent);
    }

    private double distRemain = -1;
    private SandboxModel currentDistLayer = null;

    private void updateStep() {
        WataLog.d("updateStep");
        if (pathFlag == 3) {
            /**
             * 조영수박사님 로그 추가 건
             */
            try {
                ArrayList<PDI_REPT_COL_DATA_PAYLOAD> payloads = _dataCore.getSCAN_RESULT_THIS_EPOCH().B21_Payload;
                PDI_REPT_COL_DATA_PAYLOAD lastPayload = payloads.get(payloads.size() - 1);
                lastPayload.Step_Count++;
                lastPayload.Step_Length = pdrvariable.getStep_length();
            } catch (Exception e) {
                WataLog.e("Exception= " + e.toString());
            }
            /**
             * 조영수박사님 로그 추가 건 끝
             */
            return; //지점수집시종료
        }

        if (pathFlag != 3) {
            WataLog.d("endX=" + endX + "/// " + "endY=" + endY);
            if (endX == 0.0 && endY == 0.0) {
                gatheringFlag = true;
                endGathering(true);
                if (pathFlag == 4)
                    pointCount = -1;
                map_back.setVisibility(View.VISIBLE);
                pathInverse.setVisibility(View.VISIBLE);
                map_inner_frm_linear1.setVisibility(View.VISIBLE);
                map_inner_frm_linear2.setVisibility(View.INVISIBLE);
                map_scan_count.setText("");
                return;
            }
        }

        double x, y, z;

        // Ethan PDR
        if (pathFlag != 3) {
            x = (pdrvariable.getPedestrian_x_coordinate());
            y = (pdrvariable.getPedestrian_y_coordinate());
        } else {
            x = _endPoint.x;
            y = _endPoint.y;
        }
        z = (pdrvariable.getPedestrian_z_coordinate());
//		System.out.printf("update step %f %f %f\n",x,y,z);

        WataLog.d("x=" + x + "///" + "y=" + y);

        if (x != 0 && y != 0) {
            /**
             * 실제 이동시 이곳으로 분기
             */
            stepCount++;
            WataLog.d("stepCount=" + stepCount);
            gatheringInfo.setText(String.format("Step : %d", stepCount));
            gatheringInfo.playSoundEffect(SoundEffectConstants.CLICK);
            gatheringInfo.post(new Runnable() {
                @Override
                public void run() {
                    startStepCountAnimation();
                }
            });
            float[] rotationVectorOrientations = SensorUtils.getOrientation(sensoract.rotationVectors);
            WataLog.d("rotationVectorOrientations ===" + rotationVectorOrientations);

            gatheringOrientationDegree.setText(String.format("%.1f", rotationVectorOrientations[0]));
            WataLog.d("Text = " + String.format("%.1f", rotationVectorOrientations[0]));

            gatheringOrientation.setRotation(rotationVectorOrientations[0]);

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
                    WataLog.i("chekc");
                    Element el = ElementHelper.fromText(0.6f * depth.length(), 2f, depth + "m", 300, tff, new Color(0, 0, 0, 255));
                    el.getTransform().clearTransform();
                    el.getTransform().setTranslation(v.x, v.y - 2, 2);
                    el.getTransform().calcMatrix();
                    el.getExtentHelper().updateExtent();
//                    currentDistLayer.addElement("currDist", el);

                    if (bAdded)
                        ((CompositeModel) _omapView.getO2mapInstance().getModel()).addModel(currentDistLayer);
                }
            }

            _omapView.getO2mapInstance().getView().getCamera().setLookAtPosition(v);
            _omapView.getO2mapInstance().renderUnlock();
            updatePointer(v);
            _omapView.requestRender();


            /**
             * 조영수박사님 로그 추가 건
             */
            try {
                ArrayList<PDI_REPT_COL_DATA_PAYLOAD> payloads = _dataCore.getSCAN_RESULT_THIS_EPOCH().B21_Payload;
                PDI_REPT_COL_DATA_PAYLOAD lastPayload = payloads.get(payloads.size() - 1);
                lastPayload.Step_Count++;
                lastPayload.Step_Length = pdrvariable.getStep_length();
            } catch (Exception e) {
            }
            /**
             * 조영수박사님 로그 추가 건 끝
             */
        } else {
            Toast.makeText(GatheringActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            endX = 0.0;
            endY = 0.0;
        }

        //Tag : 지점수집이 아니면...
        if (pathFlag != 3) {
            double dist = Math.sqrt(Math.pow(_endPoint.x - x, 2) + Math.pow(_endPoint.y - y, 2));
            WataLog.d("m remains = " + dist);

            if (Double.isNaN(dist)) {
                Toast.makeText(GatheringActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                endX = 0.0;
                endY = 0.0;
            } else {
                if (pathFlag != 4) {
                    if (distRemain < 0 || distRemain >= dist)
                        distRemain = dist;
                    else
                        distRemain = -1;
                    if (distRemain >= 0 && distRemain <= 0.7) {
                        Toast.makeText(GatheringActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        endX = 0.0;
                        endY = 0.0;
                    }
                } else {
                    distRemain = dist;
                    if (distRemain <= 0.7) {
                        Toast.makeText(GatheringActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        endX = 0.0;
                        endY = 0.0;
                    }
                }
            }
        }
    }

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
        //		System.out.printf("update touch\n");
        WataLog.d("DataCore.iGatherMode=" + DataCore.iGatherMode);

        if (DataCore.iGatherMode == DataCore.GATHER_MODE_NONE || DataCore.iGatherMode == DataCore.GATHER_MODE_GATHERING)
            return;

        if (DataCore.iGatherMode == DataCore.GATHER_MODE_START_SELECT) {

            //_omapView.getO2mapInstance().getView().
            Line line = ViewUtil.computeRayFromScreenPoint(_omapView.getO2mapInstance().getView(), (int) x, (int) y);

            Plane pl = new Plane(0.0, 0.0, 1.0, 0);
            Vector v = pl.intersect(line);
            WataLog.d("v==" + v);
            WataLog.d("v==" + v.x);

            _startPoint = new Vector(v.x, v.y, v.z);
            WataLog.d("_startPoint =" + _startPoint);

            if (pathFlag == 5) {
                if (DataCore.iGatherMode == DataCore.GATHER_MODE_START_SELECT) {
                    WataLog.i("시작점 지정하기");
                    onMyWayStartEndPoint(DataCore.iGatherMode, v);
                }
            } else if (pathFlag != 4) {
                if (pathFlag == 3) { // 지점수집일시
                    updateStart(v);
                    setStatusString("종료지점을 선택");
                    _dataCore.setGatheringOption(DataCore.GATHER_OPTION_ROUTE);
                    DataCore.iGatherMode = DataCore.GATHER_MODE_END_SELECT;
                    //지점수집은 강제 종료분기
                    updateTouch(x, y);
                } else {
                    updateStart(v);
                    setStatusString("종료지점을 선택");
                    _dataCore.setGatheringOption(DataCore.GATHER_OPTION_ROUTE);
                    DataCore.iGatherMode = DataCore.GATHER_MODE_END_SELECT;
                }
            } else if (pathFlag != 3) {
                if (pathFlag == 4) {
//					if(nodeCnt >= 0 && nodeCnt < 4)
                    {
                        if (multi == null)
                            multi = new ArrayList();

                        multi.add(_pathManager.findNearestNode(new Vector(v.x, v.y, v.z)).getUnderlyingPoint());
                        if (multi.size() < 2)
                            updateStart(multi.get(multi.size() - 1));
                        else
                            updateVia(multi.get(multi.size() - 1));
                    }
                } else {
                    setStatusString("종료지점을 선택");
                    _dataCore.setGatheringOption(DataCore.GATHER_OPTION_ROUTE);
                    DataCore.iGatherMode = DataCore.GATHER_MODE_END_SELECT;
                }
            } else {
                Vector _tempPoint = new Vector(v.x, v.y, v.z);
                Polyline path = getPath(_tempPoint, _tempPoint);
                if (path != null) {
                    WataLog.d("_endPoint=" + _tempPoint);
                    _endPoint = _tempPoint;
                    DataCore.iGatherMode = DataCore.GATHER_MODE_GATHERING;
                    _dataCore.setGatheringOption(DataCore.GATHER_OPTION_POINT);
                    startGathering(path);
                } else {
                    System.out.printf("path failed!!!!\n");
                }
            }

        } else { // DataCore.GATHER_MODE_END_SELECT;
            if (pathFlag != 5) {
                Line line = ViewUtil.computeRayFromScreenPoint(_omapView.getO2mapInstance().getView(), (int) x, (int) y);
                Plane pl = new Plane(0.0, 0.0, 1.0, 0);
                Vector v = pl.intersect(line);
                Vector _tempPoint = new Vector(v.x, v.y, v.z);
                updateEnd(v);
                Polyline path = getPath(_startPoint, _tempPoint);
                WataLog.d("_startPoint =" + _startPoint);
                if (path != null) {
                    WataLog.d("_endPoint=" + _tempPoint);
                    _endPoint = _tempPoint;
                    DataCore.iGatherMode = DataCore.GATHER_MODE_GATHERING;
                    if (pathFlag != 1) {
                        startGathering(path);
                    }
                } else {
                    System.out.printf("path failed!!!!\n");
                }
            }
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

        // 경로 그리기.
        if (pathFlag != 5) {
            Polyline line = (Polyline) wReader.getLine(pos);
            if (line != null) {
                WataLog.i("fromPolyline check!!!");
                Element el = ElementHelper.fromPolyline(line, 0.1, 5, new Color(255, 0, 0, 255), false, false);
                eachPathFinishLayer.addElement("finishPath" + pos, el);
            }
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
    private SettingDialog mSettingDialog;

    protected void onMyWayStartEndPoint(int which, Vector start) {
        WataLog.i("startPoint");
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

            if (which == DataCore.GATHER_MODE_START_SELECT) {
                Element el = ElementHelper.fromTextBillboard(new Vector(start.x, start.y, 5.0, 0), w, h, true, "S", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
                if (el != null) {
                    twoPointerLayer.addElement("pointerStart", el);
                }
            } else {
                Element el = ElementHelper.fromTextBillboard(new Vector(start.x, start.y, 5.0, 0), w, h, true, "E", 20.0f, Typeface.DEFAULT_BOLD, new Color(255, 0, 0, 255));
                if (el != null) {
                    twoPointerLayer.addElement("pointerStart", el);
                }
            }
        }

        if (bAdd)
            ((CompositeModel) o2map.getModel()).addModel(twoPointerLayer);

        o2map.renderUnlock();

        Toast.makeText(this, "시작점을 터치해주세요.", Toast.LENGTH_SHORT).show();
    }

    // kcy1000 - test 경로수집 code

    private int mOmapRatation = 0;
    @Override
    public void onClick(View v) {
//        testNumlayer = null;
        switch (v.getId()) {
            case R.id.d_0:
                onRotation(-270);
                break;
            case R.id.d_45:
                onRotation(45);
                break;
            case R.id.d_90:
                onRotation(0);
                break;
            case R.id.d_135:
                onRotation(-45);
                break;
            case R.id.d_180:
                onRotation(-90);
                break;
            case R.id.d_225:
                onRotation(-135);
                break;
            case R.id.d_270:
                onRotation(-180);
                break;
            case R.id.d_315:
                onRotation(-225);
                break;
            case R.id.direction_img:
                break;
            case R.id.my_way_path_start: // 기록시작
                WataLog.i(" 경로 기록 시작 ");
                WataLog.d("isInverse=" + isInverse);
                WataLog.d("mRotation=" + mRotation);
                WataLog.d("mDefaultRotation=" + mDefaultRotation);

                if(isInverse) {  // 역방향

                } else {  // 정방향
                    o2map.getView().getCamera().setDistance(50.0);
                    mOmapRatation =  (int)mRotation - 90 + mOmapRatation ;
                    WataLog.d("mOmapRatation=" + mOmapRatation);
//                Toast.makeText(this, "진행방향 -- " + mOmapRatation, Toast.LENGTH_SHORT).show();
                    Quaternion q = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, Angle.fromDegrees(mOmapRatation));
                    _omapView.getO2mapInstance().getView().getCamera().setRotation(q);

                    mRecordPosition = mPointCount;
                    mPointCount++;
                }
                WataLog.d("mPointCount=" + mPointCount);

                mLastPathPoint = lineSelected;
                if (lineSelected != null) {
                    startMyWayGathering(lineSelected);
                }
                break;
            case R.id.my_way_path_end: // 기록종료
                WataLog.i(" 경로 기록 종료 ");

//                deleteLine(myRecordFLine, String.valueOf(mPointCount-1) );

                setRecordLine();

                WataLog.d("isInverse=!!!!! =" + isInverse);
                if(isInverse) {
                    WataLog.d("mPointCount=!!!!! =" + mPointCount);
//                    myRecordFLine.removeElement("RecordFLine=" + String.valueOf(mPointCount-1));
//                    deleteLine(myRecordFLine, String.valueOf(mPointCount-1) );
//              myRecordFLine.removeAllElement();
//                    ((CompositeModel) o2map.getModel()).removeModel(myRecordFLine);
                }

                setRecordGathering(true);

                _omapView.getO2mapInstance().requestRedraw();


//                Quaternion aq = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, Angle.fromDegrees(mOmapRatation));
//                _omapView.getO2mapInstance().getView().getCamera().setRotation(aq);
//                Toast.makeText(this, "진행방향 -- " + mOmapRatation, Toast.LENGTH_SHORT).show();
//                mOmapRatation -= 45;
                break;
            case R.id.my_way_direction: // 경로선택
                WataLog.i(" 경로선택 ");
                WataLog.i(" my_way_direction.getText()=" + my_way_direction.getText());
                DataCore.iGatherMode = DataCore.GATHER_MODE_END_SELECT;
                if (my_way_direction.getText() == "선택 완료") {
                    direction_layout.setVisibility(View.GONE);
                    my_way_direction.setText("경로 선택");
                    setStartPointSetting();
                } else {
                    direction_layout.setVisibility(View.VISIBLE);
                    my_way_direction.setText("선택 완료");
                    onRotation(0);
                }
                break;
            case R.id.angle_p_btn: //방향+
                WataLog.d("mDefaultRotation=" + mDefaultRotation);
                int pRotaion = (int) mDefaultRotation - Integer.parseInt(angle_text.getText().toString());
                WataLog.d("pRotaion=" + pRotaion);
                onRotation(pRotaion);
                break;
            case R.id.angle_m_btn: //방향-
                int mRotaion = (int) mDefaultRotation + Integer.parseInt(angle_text.getText().toString());
                WataLog.d("mRotaion=" + mRotaion);
                onRotation(mRotaion);
                break;
            case R.id.list_ok_btn: // list ok
                record_lisetview.setVisibility(View.GONE);
                break;
            case R.id.reverse_record_btn: // 역방향 기록하기
                isInverse = true;
                WataLog.i("check!");
                record_direction_text.setText("역방향");
                int size = myWayInfoData.size();
                MyWayInfo item = myWayInfoData.get(size - 1);
//                setReversePointSetting(item.EndPointX, item.EndPointY, item.StartPointX, item.StartPointY);

                break;
            case R.id.record_list_btn: // 기록 내역
                WataLog.d("myWayListAdapter =" + myWayListAdapter.getCount());

                if(myWayListAdapter.getCount() > 0) {
                    record_lisetview.setVisibility(View.VISIBLE);
                } else {
                    Toast toast = Toast.makeText(GatheringActivity.this,"저장된 기록이 없습니다.",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                break;
            case R.id.record_reset_btn: // 이어서 기록하기
                WataLog.i("이어서 기록하기");

                DataCore.iGatherMode = DataCore.GATHER_MODE_END_SELECT;
                direction_layout.setVisibility(View.GONE);
                my_way_direction.setText("경로 선택");

                if(record_reset_btn.getText().equals("정방향")) {
                    mLastPointX = mLastPathPoint.getPart(0).getStartPoint().x;
                    mLastPointY = mLastPathPoint.getPart(0).getStartPoint().y;
                    record_reset_btn.setText("역방향");
                    record_direction_text.setText("정방향");

                } else {
                    mLastPointX = mLastPathPoint.getPart(0).getEndPoint().x;
                    mLastPointY = mLastPathPoint.getPart(0).getEndPoint().y;
                    record_reset_btn.setText("정방향");
                    record_direction_text.setText("역방향");
                }

                WataLog.d("mDefaultRotation=" + mDefaultRotation);
                onRotation(mDefaultRotation);
                setStartPointSetting();

//                DataCore.iGatherMode = DataCore.GATHER_MODE_START_SELECT;
//                updateTouch(mLastPointX, mLastPointY);

                break;
        }
    }

    private double mDefaultRotation = 90;
    private ImageView directionImg;
    private double mRotation = 90; // 초기 각도값

    private void onRotation(double i) {
//        Quaternion quaternion = _omapView.getO2mapInstance().getView().getCamera().getRotation();
//        int rotation = (int) quaternion.getZ(); // 지도각도
//        WataLog.d("rotation=" + rotation);
//        WataLog.d("i=" + i);
//        i = i + mOmapRatation;
        WataLog.d("mRotation=" + mRotation);
        WataLog.d("mOmapRatation=" + mOmapRatation);
        mRotation = Math.abs(i - 90);
//        mRotation = Math.abs(i - 90) + mOmapRatation;
        if(mRotation == 360) {
            now_angle.setText(0 + "도");
        } else {
            now_angle.setText(mRotation + "도");
        }


        WataLog.d("mRotation=" + mRotation);

        RotateAnimation ra = new RotateAnimation((int) mDefaultRotation, (int)i, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(100);
        ra.setFillAfter(true);
        directionImg.startAnimation(ra);

        mDefaultRotation = i;

        // 지도 회전하기(각도)
//        Quaternion q = Quaternion.fromRotationXYZ(Angle.ZERO, Angle.ZERO, Angle.fromDegrees(45));
//        _omapView.getO2mapInstance().getView().getCamera().setRotation(q);

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

        lastPointX += Math.cos(tempRatation) * 100;
        lastPointY += Math.sin(tempRatation) * 100;

        updateTouch(lastPointX, lastPointY);

        WataLog.d("startPoint= " + startPointX + "//" + startPointY);
        WataLog.d("lastPoint= " + lastPointX + "//" + lastPointY);
        pts.setPoint(0, startPointX, startPointY, 0.0f);
        pts.setPoint(1, lastPointX, lastPointY, 0.0f);

        line.setParts(0, pts);

        return line;
    }


    private double mRecordStartPointX = 0.0, mRecordStartPointY = 0.0;

    // 라인 미리보기
    private void setStartPointSetting() {
        //시작점 저장하기
        mRecordStartPointX = mLastPointX;
        mRecordStartPointY = mLastPointY;

        WataLog.d("mRecordStartPointX=" + mRecordStartPointX);
        WataLog.d("mRecordStartPointY=" + mRecordStartPointY);

        // 이동할 좌표 가져와야함.
        lineSelected = (Polyline) getMyWayLine(mLastPointX, mLastPointY);
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getStartPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getStartPoint().y);
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getEndPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getEndPoint().y);
        WataLog.d("testNumlayer=" + testNumlayer);

        //  라인그리기
        if (lineSelected != null) {
//            WataLog.d("myWayLine=" + myWayLine);
            if (myWayLine == null) {
                myWayLine = new SandboxModel("myWayLine");
            } else {
                myWayLine.removeAllElement();
//                myWayLine.removeElement("prevPath" + mPointCount);
            }

            Element eal = ElementHelper.fromPolyline(lineSelected, 0.1, 2, new Color(0, 255, 0, 255), false, false);
            myWayLine.addElement("myWayLine" + mPointCount, eal);
            ((CompositeModel) o2map.getModel()).addModel(myWayLine);
        }

        _omapView.getO2mapInstance().requestRedraw();

//        if (lineSelected != null) {
//            startMyWayGathering(lineSelected);
//        }
    }

    private int mPointCount = 0;
    private String mTempPosiont = "";

    private void setPointCount(double pointX, double pointY) {
        WataLog.i("setPointCount");
//        boolean mIsAdded = false;

        if (myWayNum == null) {
        } else {
            myWayNum = null;
        }
        myWayNum = new SandboxModel("myWayNum");
//        mIsAdded = true;

        // 순번그리기
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
    private Double mLastPointX = 122.0;
    private Double mLastPointY = -210.0;

    // kcy1000 - 경로 기록시작
    private void startMyWayGathering(Polyline line) {
        WataLog.d("startMyWayGathering = " + line);
        stepCount = 0;
        gatheringInfo.setText("");
        gatheringOrientationDegree.setText("");
        gatheringOrientation.setRotation(0);

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
        initialmm.MM_initialize2(line);
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
        WataLog.d("x======" + pdrvariable.getPedestrian_x_coordinate()
                + "/// " + "y=====" + pdrvariable.getPedestrian_y_coordinate());
        WataLog.d("endX=" + endX + "/// " + "endY=" + endY);

        if (endX == 0.0 && endY == 0.0) {
            gatheringFlag = true;
//            setRecordGathering(true);
            map_back.setVisibility(View.VISIBLE);
            pathInverse.setVisibility(View.VISIBLE);
            map_inner_frm_linear1.setVisibility(View.VISIBLE);
            map_inner_frm_linear2.setVisibility(View.INVISIBLE);
            map_scan_count.setText("");
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

        WataLog.d("x=" + x + "///" + "y=" + y);

        if (x != 0 && y != 0) {
            /**
             * 실제 이동시 이곳으로 분기
             */
            stepCount++;
            gatheringInfo.setText(String.format("Step : %d", stepCount));
            gatheringInfo.playSoundEffect(SoundEffectConstants.CLICK);
            gatheringInfo.post(new Runnable() {
                @Override
                public void run() {
                    startStepCountAnimation();
                }
            });
            float[] rotationVectorOrientations = SensorUtils.getOrientation(sensoract.rotationVectors);

            WataLog.d("rot = " + rotationVectorOrientations[0]);

            gatheringOrientationDegree.setText(String.format("%.1f", rotationVectorOrientations[0]));
            gatheringOrientation.setRotation(rotationVectorOrientations[0]);

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
            } catch (Exception e) {
            }
            /**
             * 조영수박사님 로그 추가 건 끝
             */
        } else {
            Toast.makeText(GatheringActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
            endX = 0.0;
            endY = 0.0;
        }

        //Tag : 지점수집이 아니면...
        if (pathFlag != 3) {
            double dist = Math.sqrt(Math.pow(_endPoint.x - x, 2) + Math.pow(_endPoint.y - y, 2));
            WataLog.d("m remains = " + dist);

            if (Double.isNaN(dist)) {
                Toast.makeText(GatheringActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                endX = 0.0;
                endY = 0.0;
            } else {
                if (pathFlag != 4) {
                    if (distRemain < 0 || distRemain >= dist)
                        distRemain = dist;
                    else
                        distRemain = -1;
                    if (distRemain >= 0 && distRemain <= 0.7) {
                        Toast.makeText(GatheringActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        endX = 0.0;
                        endY = 0.0;
                    }
                } else {
                    distRemain = dist;
                    if (distRemain <= 0.7) {
                        Toast.makeText(GatheringActivity.this, "이동이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        endX = 0.0;
                        endY = 0.0;
                    }
                }
            }
        }
    }

    protected void setMyWayUpdatePointer(Vector v) {
        WataLog.i("setMyWayUpdatePointer");
        O2Map o2map = (this._omapView.getO2mapInstance());

        o2map.renderLock();

        boolean bAdd = false;

        //currentPosLayer = (SandboxModel) ((CompositeModel)o2map.getModel()).getModelByName("currentPos");

        if (currentPosLayer == null) {
            currentPosLayer = new SandboxModel("currentPos");
            bAdd = true;

        }
        double pointerSize = 2;
        double pointerSize2 = 2;

        // Update Position : by Ethan
        Vector lookAt = v;//o2map.getView().getCamera().getLookAtPosition();
        o2map.getView().getCamera().setLookAtPosition(lookAt);
        Element el = ElementHelper.quadPyramidFromPoint(lookAt.x, lookAt.y, lookAt.z + 0.1, pointerSize, pointerSize, pointerSize2, Color.RED, Color.RED, Color.RED, false);
        WataLog.d("red: x" + lookAt.x + " y:" + lookAt.y + " z:" + lookAt.z);
        if (currentPosLayer.size() > 0) {
            currentPosLayer.removeElement("tester");
        }
        currentPosLayer.addElement("tester", el);
        if (bAdd) {
            ((CompositeModel) o2map.getModel()).addModel(currentPosLayer);
        }
        o2map.renderUnlock();

    }

    private RecordListAdapter myWayListAdapter;
    private ArrayList<MyWayInfo> myWayInfoData;
    private int mRecordPosition = 0;  // list기록순번

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

        myWayLine.removeAllElement();

        if (myRecordLine == null) {
            myRecordLine = new SandboxModel("myRecordLine");
        } else {
            myRecordLine = null;
            myRecordLine = new SandboxModel("myRecordLine");
        }
        myRecordLine.removeElement("mPointCount=" + String.valueOf(mPointCount-1));
//        myRecordLine.removeAllElement();

        Element eal;
//        Element eal = ElementHelper.fromPolyline(line, 0.1, 2, new Color(0, 0, 255, 255), false, false);
        WataLog.d("isInverse="  +isInverse);
        if(!isInverse) { // 정방향
            eal = ElementHelper.fromPolyline(line, 0.1, 5, new Color(0, 0, 255, 255), false, false);
        } else { // 역방향
            eal = ElementHelper.fromPolyline(line, 0.1, 5, Color.RED, false, false);

            WataLog.d("mPointCount=" + mPointCount);
            WataLog.d("mRecordPosition=" + mRecordPosition);
            WataLog.i("check!~!!!!!!!!!!!!!!!!!!!!!!");
            double dist = Math.sqrt(Math.pow(mRecordStartPointX - mLastPointX, 2) + Math.pow(mRecordStartPointY - mLastPointY, 2));
            final String depth = deciformat_WF.format(dist);

//            myWayInfoData.set(mRecordPosition , new MyWayInfo(mRecordPosition + 1 , mRecordStartPointX, mRecordStartPointY, mLastPointX,  mLastPointY, depth, "완료", "역방향 완료"));
            myWayListAdapter.notifyDataSetChanged();
        }

        myRecordLine.addElement("mPointCount" + mPointCount, eal);
        ((CompositeModel) o2map.getModel()).addModel(myRecordLine);
        _omapView.getO2mapInstance().requestRedraw();

        delIcon(); // icon제거

        if (lineSelected != null) {
            if(!isInverse) { //정방향
                // 기록할 경로번호
                setPointCount(lineSelected.getPart(0).getStartPoint().x, lineSelected.getPart(0).getStartPoint().y);

                WataLog.i("check!~!!!!!!!!!!!!!!!!!!!!!!");
                WataLog.d("mRecordPosition=" + mRecordPosition);
                // 기록경로 list로 저장하기.
                double dist = Math.sqrt(Math.pow(mLastPointX - mRecordStartPointX, 2) + Math.pow(mRecordStartPointY - mLastPointX, 2));
                final String depth = deciformat_WF.format(dist);

//                myWayInfoData.add(new MyWayInfo(mRecordPosition + 1, mRecordStartPointX, mRecordStartPointY, mLastPointX, mLastPointY, depth, "완료", "역방향 미완료"));
//                myWayListAdapter.setItems(myWayInfoData, );
                my_way_listview.setAdapter(myWayListAdapter);
            } else {

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
        WataLog.d("pathFlag = " + pathFlag + "/inverse=" + isInverse);
        WataLog.d("save=" + save);

        if (save) {
            // kcy1000 - 경로수집 저장 및 라인 그리기.
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
            //log저장
            if (!isInverse) {
                SaveGatheredData.saveGatheredData(StaticManager.getResultPath(), "MYWAY_" + _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
                SaveGatheredData.saveSendableData(StaticManager.getResultPath(), "MYWAY_" + _dataCore.getCurGatheringName() + "_F.temp", _SCAN_RESULT_THIS_EPOCH);
            } else {
                SaveGatheredData.saveGatheredData(StaticManager.getResultPath(), "MYWAY_" + _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
                SaveGatheredData.saveSendableData(StaticManager.getResultPath(), "MYWAY_" + _dataCore.getCurGatheringName() + "_R.temp", _SCAN_RESULT_THIS_EPOCH);
            }
        }

        WataLog.d("saveLogForImage=" + saveLogForImage);
        WataLog.d("isInverse=" + isInverse);
        if (saveLogForImage) {
            if (!isInverse) {
                SaveGatheredData.saveLogForImage(StaticManager.getImageLogPath(), "MYWAY_IMAGE_LOG_" + _dataCore.getCurGatheringName() + "_F.log", _SCAN_RESULT_THIS_EPOCH);
            } else {
                SaveGatheredData.saveLogForImage(StaticManager.getImageLogPath(), "MYWAY_IMAGE_LOG_" + _dataCore.getCurGatheringName() + "_R.log", _SCAN_RESULT_THIS_EPOCH);
            }
            Toast.makeText(this, "영상정보수집로그 저장에 성공하였습니다.", Toast.LENGTH_SHORT).show();
        }

        _SCAN_RESULT_THIS_EPOCH = new PDI_REPT_COL_DATA(android.os.Build.SERIAL);
        _SCAN_RESULT_TOTAL = new ArrayList<PDI_REPT_COL_DATA>();

        //Tag : TOTAL 초기화
        _dataCore.setSCAN_RESULT_TOTAL(_SCAN_RESULT_TOTAL);
        _dataCore.setSCAN_RESULT_THIS_EPOCH(_SCAN_RESULT_THIS_EPOCH);

        isInverse = false;
        pathInverse.setText("정방향");
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

    void onPathSetting() {
        WataLog.i("onPathSetting");
        endX = 0.0;
        endY = 0.0;

        O2Map o2map = (_omapView.getO2mapInstance());
        WataLog.d("isInverse=" + isInverse);

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


    //==================== 역방향 셋팅
    private void setReversePointSetting(double startPointX, double startPointY, double endPointX, double ebdPointY) {
        WataLog.i("역방향 셋팅");
        // 이동할 좌표 가져와야함.
        lineSelected = (Polyline) getMyWayReverseLine(startPointX, startPointY, endPointX, ebdPointY);
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getStartPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getStartPoint().y);
        WataLog.d("getStartPoint().x=" + lineSelected.getPart(0).getEndPoint().x + "/// getStartPoint().y=" + lineSelected.getPart(0).getEndPoint().y);
        WataLog.d("testNumlayer=" + testNumlayer);
        //  역방향 라인그리기
        if (lineSelected != null) {
//            WataLog.d("myWayLine=" + myWayLine);
            if (myWayLine == null) {
                myWayLine = new SandboxModel("myWayLine");
            } else {
                myWayLine.removeAllElement();
            }

            Element eal = ElementHelper.fromPolyline(lineSelected, 0.1, 2, Color.RED, false, false);
            myWayLine.addElement("prevPath" + mPointCount, eal);
            ((CompositeModel) o2map.getModel()).addModel(myWayLine);
        }
        _omapView.getO2mapInstance().requestRedraw();
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


//    private SensorManager mSensorManager;
//    private Sensor mAccelerometer;
//    private Sensor mMagnetometer;
//    private ImageView directionImg;
//
//    private void setSensor() {
//        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
//
//        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
//        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
//    }
//
//    private float[] mGravity = null;
//    private float[] mGeomagnetic = null;
//    private float mCurrentDegree = 0f;
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//
//        float azimut, pitch, roll;
//        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            mGravity = event.values;
//        }
//
//        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            mGeomagnetic = event.values;
//        }
//
//        if(mGravity != null && mGeomagnetic != null) {
//            float R[] = new float[9];
//            float I[] = new float[9];
//
//            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
//            WataLog.d("success=" +success);
//            if(success) {
//                float orientation[] = new float[3];
//                SensorManager.getOrientation(R, orientation);
//                azimut = orientation[0];
//                pitch = orientation[1];
//                roll = orientation[2];
//
//                float azimuthinDegress = (int)(Math.toDegrees(SensorManager.getOrientation(R, orientation)[0] + 360) % 360);
//                WataLog.d("azimuthinDegress=" + azimuthinDegress);
//
//                RotateAnimation ra = new RotateAnimation(mCurrentDegree, -azimuthinDegress,
//                        Animation.RELATIVE_TO_SELF, 0.5f,
//                        Animation.RELATIVE_TO_SELF, 0.5f);
//
//                ra.setDuration(250);
//                ra.setFillAfter(true);
//                directionImg.startAnimation(ra);
//                mCurrentDegree = -azimuthinDegress;
//            }
//        }
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        WataLog.d("accuracy=" + accuracy);
//    }


    class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
//			switch (v.getId())
//			{
            if (id == R.id.map_back)
                finish();
//				break;

//			else if(id == R.id.sensibility)
//			{
//				com.geotwo.LBS.Gathering.dialog.SettingDialog sensitive = new com.geotwo.LBS.Gathering.dialog.SettingDialog();
//				AlertDialog senseDialog = sensitive.makeSensitiveSettingDilog(GatheringActivity.this);
//				senseDialog.show();
//			}
            else if (id == R.id.map_setting) {
                mSettingDialog = new SettingDialog(GatheringActivity.this, new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        com.geotwo.LAB_TEST.Gathering.dialog.SettingDialog sensitive = new com.geotwo.LAB_TEST.Gathering.dialog.SettingDialog();
                        AlertDialog senseDialog = sensitive.makeSensitiveSettingDilog(GatheringActivity.this);
                        senseDialog.show();
                    }
                }, new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        viewStepLenSetting();
                    }
                });
                mSettingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        boolean currentLinkNumberShowing = SettingDialog.isLinkNumberShowing(GatheringActivity.this);
                        if (currentLinkNumberShowing != isDrawingPathNumber) {
                            finish();
                            startActivity(new Intent(GatheringActivity.this, GatheringActivity.class));
                        }
                    }
                });
                mSettingDialog.show();

            }
//				break;
//			else if(id == R.id.map_view_all)
//			{
//				com.geotwo.o2mapmobile.view.ViewUtil.FitTopView(_omapView.getO2mapInstance().getView(),_omapView.getO2mapInstance().getModel().getExtent());
//				_omapView.getO2mapInstance().renderLock();
//				refreshNorthIcon();
//				_omapView.getO2mapInstance().renderUnlock();
//				_omapView.requestRender();
//			}
//				break;

            else if (id == R.id.gathering_select) {
                selectWaysDialog();
            }
//				break;

            else if (id == R.id.map_path_select)
                selectPathDialog();
//				break;
            else if (id == R.id.map_path_start)  // 시작버턴
            {
                setDefaultPathView();
                map_back.setVisibility(View.INVISIBLE);
                map_inner_frm_linear1.setVisibility(View.INVISIBLE);
                map_inner_frm_linear2.setVisibility(View.VISIBLE);
                pathInverse.setVisibility(View.INVISIBLE); //Tag : 측정 시작하면 정방향, 역방향을 고를 수 없음
                if (lineSelected != null) {
                    startGathering(lineSelected);                    // Start Gathering : Ethan
                }
            }
//				break;

            else if (id == R.id.map_complete) { // 수집완료 btn
                WataLog.i("수집완료");
                if (pathFlag == 5) {
                    gatheringFlag = true;
                    map_back.setVisibility(View.VISIBLE);
                    map_inner_frm_linear1.setVisibility(View.VISIBLE);
                    map_inner_frm_linear2.setVisibility(View.INVISIBLE);
                    map_scan_count.setText("");
                    pathInverse.setVisibility(View.VISIBLE);

                } else {
                    gatheringFlag = true;
                    endGathering(true);
                    if (pathFlag == 1) {
                        pathInverse.setVisibility(View.VISIBLE);
                    }
                    map_back.setVisibility(View.VISIBLE);
                    map_inner_frm_linear1.setVisibility(View.VISIBLE);
                    map_inner_frm_linear2.setVisibility(View.INVISIBLE);
                    map_scan_count.setText("");
                    pathInverse.setVisibility(View.VISIBLE);
                }
            }
//				break;

//			case R.id.map_start_point_cancel:
//				gatheringFlag = false;
//				pathFlag = -1;
//				endGathering();
//				updateStart(null);
//				updateEnd(null);
//				delIcon();
//				map_back.setVisibility(View.VISIBLE);
//				map_inner_frm_linear1.setVisibility(View.VISIBLE);
//				map_inner_frm_linear2.setVisibility(View.INVISIBLE);
//				map_scan_count.setText("");
//				break;

            else if (id == R.id.map_cancel) {
                gatheringFlag = false;
//				pathFlag = -1;
                endGathering(false);
                if (pathFlag == 1) {
                    pathInverse.setVisibility(View.VISIBLE);
                }
                map_back.setVisibility(View.VISIBLE);
                map_inner_frm_linear1.setVisibility(View.VISIBLE);
                map_inner_frm_linear2.setVisibility(View.INVISIBLE);
                map_scan_count.setText("");

                delIcon();
                _clickListener.viewEachPath(_which);
                map_path_start.setVisibility(View.VISIBLE);
            }
//				break;

//			case PATHINVERSE_BTN_ID:
//				Log.e("tag", "1231254523525235245143515");
//				reversePath();
//				break;
//			}
        }

        void setDefaultPathView() {
            map_path_select.setText("경로선택");
            map_path_start.setVisibility(View.INVISIBLE);
//			pathInverse.setVisibility(View.INVISIBLE);
        }

        private void processPlayBtnClicked() {
            //int pre=DataCore.iGatherMode;

            switch (DataCore.iGatherMode) {
                case DataCore.GATHER_MODE_NONE:
                    DataCore.iGatherMode = DataCore.GATHER_MODE_START_SELECT;
                    String str = "";
                    WataLog.d("pathFlag=" + pathFlag);
                    switch (pathFlag) {
                        case 1: //지정경로수집
                            str = "시작지점선택";
                            break;
                        case 2: //경로수집
                            str = "시작지점선택";
                            break;
                        case 3: //지점수집
                            str = "수집지점선택";
                            break;
                        case 4: //경유지점수집
                            str = "경유지점선택";
                        case 5: //내길 만들기
                            str = "Test 경로수집";
                    }
                    setStatusString(str);
                    break;

                default:
                    DataCore.iGatherMode = DataCore.GATHER_MODE_NONE;
                    endGathering(false);
                    break;
            }
        }


        void selectWaysDialog()            // Ethan
        {
            final String[] strWays = {"지정경로수집", "경로수집", "지점수집", "경유경로수집", "Test 경로수집"};

            new AlertDialog.Builder(GatheringActivity.this)
                    .setTitle("수집방법을 선택하세요.")
                    .setItems(strWays, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataCore.iGatherMode = DataCore.GATHER_MODE_NONE;

                            if (which == 4) {
                                map_bottom_linear1.setVisibility(View.INVISIBLE);
                                map_bottom_linear2.setVisibility(View.INVISIBLE);
                                map_bottom_linear3.setVisibility(View.VISIBLE);
                                pathInverse.setVisibility(View.GONE);
                                my_way_record_info_layout.setVisibility(View.VISIBLE);
                            } else {
                                my_way_record_info_layout.setVisibility(View.GONE);

                            }

                            switch (which) {
                                case 0:            // 지점경로수집
                                    pathFlag = 1;
                                    setMapView();
                                    map_bottom_linear1.setVisibility(View.VISIBLE);
                                    map_bottom_linear2.setVisibility(View.INVISIBLE);
                                    map_bottom_linear3.setVisibility(View.INVISIBLE);
                                    direction_layout.setVisibility(View.GONE);
                                    pathInverse.setVisibility(View.VISIBLE);
                                    gathering_select.setText(strWays[0]);
                                    break;

                                case 1:
                                    pathFlag = 2;
                                    mCurrentPath = 0;
                                    setMapView();
                                    gathering_select.setText(strWays[1]);
                                    map_bottom_linear1.setVisibility(View.INVISIBLE);
                                    map_bottom_linear2.setVisibility(View.VISIBLE);
                                    map_bottom_linear3.setVisibility(View.INVISIBLE);
                                    direction_layout.setVisibility(View.GONE);
                                    pathInverse.setVisibility(View.INVISIBLE);
                                    processPlayBtnClicked();
                                    break;

                                case 2:
                                    pathFlag = 3;
                                    mCurrentPath = 0;
                                    setMapView();
                                    gathering_select.setText(strWays[2]);
                                    map_bottom_linear1.setVisibility(View.INVISIBLE);
                                    map_bottom_linear2.setVisibility(View.VISIBLE);
                                    map_bottom_linear3.setVisibility(View.INVISIBLE);
                                    direction_layout.setVisibility(View.GONE);
                                    pathInverse.setVisibility(View.INVISIBLE);
                                    processPlayBtnClicked();
                                    break;
                                case 3:
                                    pathFlag = 4;
                                    mCurrentPath = 0;
                                    setMapView();
                                    gathering_select.setText(strWays[3]);
                                    map_bottom_linear1.setVisibility(View.INVISIBLE);
                                    map_bottom_linear2.setVisibility(View.VISIBLE);
                                    map_bottom_linear3.setVisibility(View.INVISIBLE);
                                    direction_layout.setVisibility(View.GONE);

                                    endPoint = (Button) findViewById(R.id.end_point);
                                    endPoint.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (multi == null)
                                                return;
                                            if (multi.size() > 1) {
                                                _endPoint = multi.get(multi.size() - 1);
                                                WataLog.d("_endPoint=" + _endPoint);
                                                if (fullpath == null)
                                                    fullpath = new ArrayList();
                                                for (int i = 1; i < multi.size(); i++) {
                                                    List<Vector> nodes = _pathManager.findPath(multi.get(i - 1), multi.get(i));
                                                    if (nodes != null) {
                                                        Points pts = new Points();
                                                        pts.makePoints(nodes.size());
                                                        for (int j = 0; j < nodes.size(); j++) {

                                                            Vector p = nodes.get(j);
                                                            pts.data[j].x = p.x;
                                                            pts.data[j].y = p.y;
                                                            pts.data[j].z = 0;
                                                        }
                                                        fullpath.add(pts.clone());

                                                    } else {
                                                        WataLog.d("path failed!!!!\n");
                                                    }
                                                }
                                                int num = 0;
                                                for (int i = 0; i < fullpath.size(); i++)
                                                    num = num + fullpath.get(i).getNumPoints();
                                                if (num > 0) {
                                                    Polyline total = new Polyline();
                                                    total.makeParts(1);
                                                    Points pts = new Points();
                                                    pts.makePoints(num);
                                                    int lastIdx = 0;
                                                    for (int i = 0; i < fullpath.size(); i++) {
                                                        for (int j = 0; j < fullpath.get(i).getNumPoints(); j++) {
                                                            pts.data[lastIdx + j].x = fullpath.get(i).data[j].x;
                                                            pts.data[lastIdx + j].y = fullpath.get(i).data[j].y;
                                                            pts.data[lastIdx + j].z = fullpath.get(i).data[j].z;
                                                        }
                                                        lastIdx = lastIdx + fullpath.get(i).getNumPoints();
                                                    }
                                                    total.setParts(0, pts);
                                                    DataCore.iGatherMode = DataCore.GATHER_MODE_GATHERING;
                                                    _dataCore.setGatheringOption(DataCore.GATHER_OPTION_ROUTE);
                                                    startGathering(total);
                                                    _omapView.requestRender();
                                                }
                                            } else
                                                Toast.makeText(GatheringActivity.this, "need more via point..", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    endPoint.setVisibility(View.VISIBLE);
                                    pathInverse.setVisibility(View.INVISIBLE);
                                    processPlayBtnClicked();
                                    break;
                                case 4:            // kcy1000 - Test 경로수집
                                    WataLog.i(" Test 경로수집");
                                    myWayInfoData = new ArrayList<MyWayInfo>();
                                    pathFlag = 5;
                                    setMapView();
                                    mLastPointX = 122.0;
                                    mLastPointY = -210.0;
                                    mPointCount = 0;
                                    mRecordPosition = 0;
                                    mOmapRatation = 0;
                                    gathering_select.setText(strWays[0]);
//                                    setSensor(); // test용 나침반

                                    myWayLine = null;
                                    processPlayBtnClicked();
                                    DataCore.iGatherMode = DataCore.GATHER_MODE_START_SELECT;
                                    updateTouch(535, 1088);

                                    break;
                            }
                        }
                    })
                    .show();
        }

        // Path Select Dialog(경로1, 경로2 ...) : by Ethan
        void selectPathDialog() {
            final String[] strWays = getPathIndex();

            new AlertDialog.Builder(GatheringActivity.this)
                    .setTitle("수집할 경로를 선택하세요.")
                    .setItems(strWays, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            StaticManager.setPathNum("" + (which + 1));
                            delIcon();
                            map_path_start.setVisibility(View.VISIBLE);
//					pathInverse.setVisibility(View.VISIBLE);
                            map_path_select.setText("" + strWays[which]);

                            WataLog.d("which=" + which);
                            viewEachPath(which);

                            mCurrentPath = which + 1;
                            WataLog.d("selected path =" + (which + 1));

                            //hunyeon add code, 측정 후 재 측정 수정3, 경로 저장
                            _which = which;
                        }
                    })
                    .show();
        }

        String[] getPathIndex()            // get Path Index : Ethan
        {
            int readerSize = wReader.getCount();
            int[] intValues = new int[readerSize];
            String[] values = new String[readerSize];

            for (int a = 0; a < readerSize; a++) {
                String value = String.format(("%d"), wReader.getPathId(a));
//				Log.e("tag", "value = "+value);
                if (value != null) {
                    intValues[a] = Integer.valueOf(value);
                }
            }

            Arrays.sort(intValues);

            for (int b = 0; b < readerSize; b++) {
                values[b] = "경로 " + intValues[b];
            }
            return values;
        }

        /*
		String[] getPathIndex()
		{
			reader=CSFReader.getReader(pathPath);
			try {
				reader.open();
//				reader.setScale(10, 10, 10);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String tempPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pathGathering/"
					+StaticManager.folderName+"/"+StaticManager.floorName+"/";

			reader.moveFirst();
			int readerSize = reader.getCount();
			int[] intValues = new int[readerSize];
			String[] values = new String[readerSize];

			for(int a=0; a<readerSize; a++)
			{
//				File file = new File(tempPath+String.valueOf(a+1));
//
//				if(!file.exists())
//				{
//					Log.d("jeongyeol", "make path = "+tempPath+String.valueOf(a+1));
//					file.mkdirs();
//				}
				String value = reader.getFieldValue(0);
//				Log.e("tag", "value = "+value);
				if(value!=null)
				{
					intValues[a] = Integer.valueOf(value);
					reader.moveNext();
				}
			}

			Arrays.sort(intValues);

			for(int b=0; b<readerSize; b++)
			{
				values[b] = "경로 "+intValues[b];
			}

			return values;
		}
*/
        SandboxModel eachPathLayer = null;

        double startX = 0;
        double startY = 0;

        void viewEachPath(int pos) {
            WataLog.i("viewEachPath");
            WataLog.d("pos=" + pos);
            endX = 0.0;
            endY = 0.0;

            O2Map o2map = (_omapView.getO2mapInstance());

            updateCurrentPath(null, 0);
            _UIHandler.sendEmptyMessage(DataCore.AT_START_NEW_RECORD);

            boolean bAdd = false;
            if (eachPathLayer == null) {
                eachPathLayer = new SandboxModel("eachPathLayer");
                bAdd = true;
            }
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

            if (eachPathLayer.size() > 0) {
                eachPathLayer.removeElement(new String("currentPath"));
            }

            lineSelected = (Polyline) wReader.getLine(pos);

            if (lineSelected != null) {

                displayPathFinish();

                Points pts = lineSelected.getPart(0);
                Point start = pts.data[0];
                Point end = pts.data[pts.getNumPoints() - 1];

                _startPoint = new Vector(start.x, start.y, 0);
                WataLog.d("_startPoint =" + _startPoint);
//                _startPoint = new Vector(105.606839579521505, -184.879042430578039, 0);
                startX = start.x;
                startY = start.y;
                WataLog.d("_startPoint= " + _startPoint);

//                105.606839579521505, -184.879042430578039
                _endPoint = new Vector(end.x, end.y, 0);
//                _endPoint = new Vector(105.606839579521505, -184.879042430578039, 0);
                WataLog.d("_endPoint= " + _endPoint);
                endX = end.x;
                endY = end.y;

                WataLog.i("chekc");
                Element el = ElementHelper.fromPolyline(lineSelected, 0.1, 5, new Color(0, 0, 255, 255), false, false);
                eachPathLayer.addElement("currentPath", el);

                // Start, End Icon : by Ethan
                setIcon(1, startX, startY); //1:start
                setIcon(2, endX, endY); //2:end
                o2map.getView().getCamera().setLookAtPosition(new Vector(startX, startY, 0));
            }

            if (bAdd) {
                ((CompositeModel) o2map.getModel()).addModel(eachPathLayer);
            }
            _omapView.requestRender();
        }

        /*
		void viewEachPath(int pos)
		{
			endX = 0.0;
			endY = 0.0;

			O2Map o2map = (_omapView.getO2mapInstance());

//			updateCurrentPath(null, 0);
			_UIHandler.sendEmptyMessage(DataCore.AT_START_NEW_RECORD);

			boolean bAdd=false;
			if(eachPathLayer == null) {
				eachPathLayer = new SandboxModel("eachPathLayer");
				bAdd = true;
			}
			if(prevPathLayer!=null)
			{
				if(prevPathLayer.size()>0)
				{
					for(int i=0; i < mNumTest.length;i++)
					{
						if(mNumTest[i] > 0)
						{
							prevPathLayer.removeElement(new String("prevPath"+mNumTest[i]));
							testNumlayer.removeElement(new String("pointer"+mNumTest[i]));
						}
					}
				}
			}
			if(eachPathLayer.size()>0) {
				eachPathLayer.removeElement(new String("currentPath"));
			}

			reader = CSFReader.getReader(pathPath);
			try {
				reader.open();
//				reader.setScale(10, 10, 10);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			reader.moveFirst();
			for(int a=0; a<reader.getCount(); a++)
			{
				String strVal = reader.getFieldValue(0);
				int val = -1;
				if(strVal!=null)
				{
					val = Integer.valueOf(strVal);
					if(val==(pos+1))
					{
						lineSelected = (Polyline)reader.getGeometry();

						Points pts = lineSelected.getPart(0);
						Point start = pts.data[0];
						Point end = pts.data[pts.getNumPoints()-1];

						_startPoint = new Vector(start.x, start.y, 0);
						startX = start.x;
						startY = start.y;
						_endPoint = new Vector(end.x, end.y, 0);
						endX = end.x;
						endY = end.y;
						break;
					}
				}

				reader.moveNext();
			}

			if(lineSelected!=null)
			{
				Element el=ElementHelper.fromPolyline(lineSelected, 0.1, 5, new Color(0,0,255,255), false, false);
				eachPathLayer.addElement("currentPath", el);

				setIcon(1, startX, startY); //1:start
				setIcon(2, endX, endY); //2:end
				o2map.getView().getCamera().setLookAtPosition(new Vector(startX, startY, 0));
			}

			if(bAdd)
			{
				((CompositeModel)o2map.getModel()).addModel(eachPathLayer);
			}
			_omapView.requestRender();
		}
*/
        public void setIcon(int mode, double x, double y) {
            O2Map o2map = (_omapView.getO2mapInstance());
            boolean bAdd = false;

            if (twoPointerLayer == null) {
                twoPointerLayer = new SandboxModel("pathPointer");
                bAdd = true;
            }
            float w = 4f;
            float h = 4f;

            if (StaticManager.folderName.equalsIgnoreCase("office")) {
                w = 1f;
                h = 1f;
            }
            Element el = null;
            switch (mode) {
                case 1: //Tag : 시작 지점 표시
                    el = ElementHelper.fromTextBillboard(new Vector(x, y, 0.3, 0), w, h, true, "S", 20.0f, Typeface.DEFAULT_BOLD, new Color(44, 233, 41, 255));
                    //el = ElementHelper.fromImageBillboard(new Vector(x,y,0.3,0),w,h,true,Environment.getExternalStorageDirectory().getPath() + "/gathering/area/Gangnam/gangnam_indoor.png");

                    //el=ElementHelper.fromImage(IMAGE_SIZE,IMAGE_SIZE,Environment.getExternalStorageDirectory().getPath() + "/gathering/area/Gangnam/gangnam_indoor.png");
				/*el.getTransform().clearTransform();
				el.getTransform().setTranslation(basePointX+x, basePointY+y+45, 0);
				el.getTransform().calcMatrix();
				el.getExtentHelper().updateExtent();*/

                    WataLog.i("check1!");
                    twoPointerLayer.addElement("pathPointerStart", el);
                    if (bAdd) {
                        twoPointerLayer.setOrder(100);
                        ((CompositeModel) o2map.getModel()).addModel(twoPointerLayer);
                    }
                    break;
                case 2: //Tag : 끝 지점 표시
                    el = ElementHelper.fromTextBillboard(new Vector(x, y, 0.3, 0), w, h, true, "G", 20.0f, Typeface.DEFAULT_BOLD, new Color(44, 233, 41, 255));

                    //el=ElementHelper.fromImage(IMAGE_SIZE,IMAGE_SIZE,Environment.getExternalStorageDirectory().getPath() + "/gathering/ico_map_destination.png");
				/*el.getTransform().clearTransform();
				el.getTransform().setTranslation(basePointX+x, basePointY+y+45, 0);
				el.getTransform().calcMatrix();
				el.getExtentHelper().updateExtent();*/

                    WataLog.i("check1!");
                    twoPointerLayer.addElement("pathPointerEnd", el);
                    if (bAdd) {
                        twoPointerLayer.setOrder(101);
                        ((CompositeModel) o2map.getModel()).addModel(twoPointerLayer);
                    }
                    break;

            }
        }

        void reversePath() {
            if (lineSelected != null) {
                delIcon();
                lineSelected.reverse();
                Points pts = lineSelected.getPart(0);
                Point start = pts.data[0];
                Point end = pts.data[pts.getNumPoints() - 1];

                double startX = start.x;
                double startY = start.y;
                endX = end.x;
                endY = end.y;

                _omapView.getO2mapInstance().renderLock();
                setIcon(1, startX, startY); //1:start
                setIcon(2, endX, endY); //2:end
                _omapView.getO2mapInstance().renderUnlock();
                _omapView.requestRender();

                if (!isInverse) {
                    WataLog.i("check!");
                    isInverse = true;
                    pathInverse.setText("역방향");
                } else {
                    WataLog.i("check!");
                    isInverse = false;
                    pathInverse.setText("정방향");
                }
            }
        }
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
                            WataLog.i("AT_START_NEW_RECORD");
                            if (pathFlag == 4) {
                                Log.d("jeongyeol", "sensing -- ok");
                                if (_wifiScanner._scanResultList != null) {
                                    Log.d("jeongyeol", "list -- ok");

                                    if (bServerFP) {
                                        if (wifiScanner == null) {
                                            wifiScanner = new com.geotwo.TBP.TBPWiFiScanner(GatheringActivity.this);
                                            wifiScanner.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

                                            wifiScanner.ipAddress = _dataCore.getTpsURL();
                                            wifiScanner.port = _dataCore.getTpsPort();
                                        }

                                        for (int i = 0; i < _wifiScanner._scanResultList.size(); i++) {
                                            String MACaddr
                                                    = _wifiScanner._scanResultList.get(i).BSSID.substring(0, 2)
                                                    + _wifiScanner._scanResultList.get(i).BSSID.substring(3, 5)
                                                    + _wifiScanner._scanResultList.get(i).BSSID.substring(6, 8)
                                                    + _wifiScanner._scanResultList.get(i).BSSID.substring(9, 11)
                                                    + _wifiScanner._scanResultList.get(i).BSSID.substring(12, 14)
                                                    + _wifiScanner._scanResultList.get(i).BSSID.substring(15, 17);
                                            String SSID = _wifiScanner._scanResultList.get(i).SSID;
                                            int RSSI = _wifiScanner._scanResultList.get(i).level;
                                            Log.d("jeongyeol", "wifi ssid = " + SSID + "/ rssi = " + RSSI + "/" + i + "/" + _wifiScanner._scanResultList.size());

                                            int Freq = _wifiScanner._scanResultList.get(i).frequency;

                                            //				    		wifiList = wifiManager.getScanResults();

                                            PDI_SCANINFO scandata = new PDI_SCANINFO();

                                            scandata.FD1_INFRA_TYPE = PDI_SCANINFO.DEF_INFRA_TYPE_WiFi;
                                            scandata.FD2_INFRA_ID = MACaddr;
                                            scandata.FD3_RSSI = (byte) RSSI;
                                            scandata.FD4_FREQ = (short) Freq;
                                            scandata.FD5_ENCRYPTION = 0;
                                            scandata.FD6_SSID_LEN = (short) SSID.length();
                                            scandata.FD7_SSID = SSID;

                                            wifiScanner.lstScanInfo.add(scandata);
                                        }
                                        new Thread(new Runnable() {
                                            public void run() {
                                                wifiScanner.sendData(GEO2.LBSP.SClient.SvrPositionProvider.COORDTYPE.emMiddleTM);
                                            }
                                        }).start();
                                        System.gc();
                                        wifiScanner.isScanComplete = false;
                                    } else {
                                        if (posProvider != null) {
//										if(bCal)
//										{
//											Iron2Calibrator cal = new Iron2Calibrator();
//											for(int i=0; i<_wifiScanner._scanResultList.size(); i++)
//											{
////												Log.d("jeongyeol", "ssid="+_wifiScanner._scanResultList.get(i).SSID+"/prev rssi = "+_wifiScanner._scanResultList.get(i).level);
//												_wifiScanner._scanResultList.get(i).level = (int) cal.calibrate(_wifiScanner._scanResultList.get(i).level, 3);
////												Log.d("jeongyeol", "ssid="+_wifiScanner._scanResultList.get(i).SSID+"/curr rssi = "+_wifiScanner._scanResultList.get(i).level);
//											}
//										}
                                            PDI_RSP_POS pdiRspPos = posProvider.getLocation(_wifiScanner._scanResultList);

//										PDI_RSP_POS pdiRspPos = new PDI_RSP_POS();
//										pdiRspPos.x = 1730;
//										pdiRspPos.y = 980;
//										pdiRspPos.strFloor = "AB01";
                                            if (pdiRspPos != null) {
                                                pdiRspPos.x = pdiRspPos.x / 10 + basePointX;
                                                pdiRspPos.y = pdiRspPos.y / 10 + basePointY;

                                                if (bLogging) {
//												TransformGeo Tg = new TransformGeo(6378137.0, 298.257223563, 1.0, 38.0, 127.0, 600000.0, 200000.0);
//										        Vector currPosTM = Tg.transformGP2TM(pdiRspPos.y, pdiRspPos.x);
//										        pdiRspPos.x = currPosTM.x;
//										        pdiRspPos.y = currPosTM.y;

                                                    Vector filteredPos = null;
                                                    Vector viewVector = null;
                                                    Vector allPathMatch = null;
//												if(m_pdrinter != null)
//												{
//													if(!bkistPDRInited)
//													{
//														m_pdrinter.refer_set_pedestrian_x_coordinate(pdiRspPos.x);
//														m_pdrinter.refer_set_pedestrian_y_coordinate(pdiRspPos.y);
//														bkistPDRInited = true;
//													}
//													else
//														m_pdrinter.refer_kf_meas_updata((int)pdiRspPos.x, (int)pdiRspPos.y);
//													filteredPos = new Vector(m_pdrinter.refer_get_pedestrian_x_coordinate(), m_pdrinter.refer_get_pedestrian_y_coordinate(), 0);
//													Log.d("jeongyeol", "kist x="+filteredPos.x+"/y="+filteredPos.y);
//												}
                                                    if (mKf == null) {
                                                        double[][] tempMatrix = {{pdiRspPos.x}, {8}, {pdiRspPos.y}, {8}};
                                                        Jama.Matrix input = new Jama.Matrix(tempMatrix);
                                                        Log.d("jeongyeol", "kf init x = " + input.get(0, 0) + "/ y = " + input.get(2, 0));
                                                        mKf = new KalmanFilter();
                                                        mKf.init(input, null, 1);
                                                        filteredPos = new Vector(pdiRspPos.x, pdiRspPos.y, 0);
//													filteredPos.strFloor = rspPos.strFloor;

                                                        Log.d("jeongyeol", "2919 -- kalman = " + filteredPos.x + "/" + filteredPos.y);
                                                        if (android.os.Build.BRAND != null && android.os.Build.BRAND.equalsIgnoreCase("samsung") && android.os.Build.MODEL != null && android.os.Build.MODEL.equalsIgnoreCase("SM-P600"))
                                                            viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[2]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0] - 90)));
                                                        else
                                                            viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[1]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0])));
                                                        //								bIsAccessToSensor = false;
                                                        viewVector = new Vector(viewVector.getX(), viewVector.getY(), 0, 0).normalize3();
                                                        viewVector = viewVector.multiply3(steplenthestimation.present_acc_variance_value / 10);

                                                        viewVector = new Vector(filteredPos.x, filteredPos.y, 0).add3(viewVector);

                                                        if (_pathManager != null) {
                                                            allPathMatch = _pathManager.findNearestPointOnPath(viewVector);
                                                        }

                                                        if (_pathManager_for_col != null) {
                                                            Vector matched = _pathManager_for_col.findNearestPointOnPath(viewVector);
                                                            if (matched != null) {
                                                                viewVector = matched;
                                                                Log.d("jeongyeol", "2919 -- map Mapching = " + matched.x + "/" + matched.y);
                                                            }
                                                        }
                                                    } else {
                                                        double[][] tempMatrix = {{pdiRspPos.x}, {pdiRspPos.y}};
                                                        Jama.Matrix input = new Jama.Matrix(tempMatrix);
                                                        Log.d("jeongyeol", "kf update x = " + input.get(0, 0) + "/ y = " + input.get(1, 0));
                                                        Jama.Matrix output = mKf.doUpdate(input);
                                                        if (output != null) {
                                                            filteredPos = new Vector(output.get(0, 0), output.get(2, 0), 0);
//														filteredPos.strFloor = rspPos.strFloor;
                                                            Log.d("jeongyeol", "2919 -- kalman = " + filteredPos.x + "/" + filteredPos.y);
                                                            if (android.os.Build.BRAND != null && android.os.Build.BRAND.equalsIgnoreCase("samsung") && android.os.Build.MODEL != null && android.os.Build.MODEL.equalsIgnoreCase("SM-P600"))
                                                                viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[2]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0] - 90)));
                                                            else
                                                                viewVector = ViewUtil.getViewVector(Quaternion.fromRotationXYZ(Angle.fromDegrees(360 - _senact.mag[1]), Angle.ZERO, Angle.fromDegrees(360 - _senact.mag[0])));
                                                            //								bIsAccessToSensor = false;
                                                            viewVector = new Vector(viewVector.getX(), viewVector.getY(), 0, 0).normalize3();
                                                            viewVector = viewVector.multiply3(steplenthestimation.present_acc_variance_value / 10);

                                                            viewVector = new Vector(filteredPos.x, filteredPos.y, 0).add3(viewVector);

                                                            Log.d("jeongyeol", "2919 -- orientation = " + viewVector.x + "/" + viewVector.y);

                                                            if (_pathManager != null) {
                                                                allPathMatch = _pathManager.findNearestPointOnPath(viewVector);
                                                            }

                                                            if (_pathManager_for_col != null) {
                                                                Vector matched = _pathManager_for_col.findNearestPointOnPath(viewVector);
                                                                if (matched != null) {
                                                                    viewVector = matched;
                                                                    Log.d("jeongyeol", "2919 -- map Mapching = " + matched.x + "/" + matched.y);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (bLogging && touchedPoint != null) {
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
                                                        String logFile = Environment.getExternalStorageDirectory().getPath() + "/gathering/log/" + StaticManager.address + "_" + StaticManager.floorName + "_path.txt";
                                                        if (pdiRspPos != null) {
                                                            RspPosition rspPos = new RspPosition();
                                                            rspPos.x = pdiRspPos.x;
                                                            rspPos.y = pdiRspPos.y;
                                                            rspPos.strFloor = pdiRspPos.strFloor;
                                                            if (pathFlag == 4)
                                                                recordPoint(logFile, rspPos, filteredPos, allPathMatch, viewVector, _wifiScanner._scanResultList);
                                                        }
                                                        if (pathFlag == 4) {
                                                            Log.d("jeongyeol", "2919 -- record path log ");
                                                            boolean bAdd = false;
                                                            if (LayerForApp == null) {
                                                                LayerForApp = new SandboxModel("LayerForApp");
                                                                bAdd = true;
                                                            }

//														double temp[] = (double[]) msg.obj;

                                                            //						TransformGeo toGRS80 = new TransformGeo(6378137.0, 298.257223563, 1.0, 38.0, 127.0, 600000.0, 200000.0);
                                                            //						Vector tempGRS80 = toGRS80.transformTM2GP(temp[1], temp[0]);
                                                            //						Log.d("jeongyeol", "GRS80 lng="+tempGRS80.x+"("+temp[0]+")"+"/lat="+tempGRS80.y+"("+temp[1]+")");
                                                            //						Toast.makeText(GatheringActivity.this, "GRS80 lng="+tempGRS80.x+"/lat="+tempGRS80.y, 200).show();

                                                            double pointerSize = 0.5;

                                                            Element el = null;
                                                            if (touchedPoint != null) {
                                                                _omapView.getO2mapInstance().renderLock();
                                                                if (StaticManager.folderName.equalsIgnoreCase("office"))
                                                                    el = ElementHelper.quadPyramidFromPoint(touchedPoint.x, touchedPoint.y, 0.1, 0.1f, 0.1f, 0.2f, Color.GREEN, Color.GREEN, Color.GREEN, false);
                                                                else
                                                                    el = ElementHelper.quadPyramidFromPoint(touchedPoint.x, touchedPoint.y, 0.1, pointerSize, pointerSize, 1.1f, Color.GREEN, Color.GREEN, Color.GREEN, false);
                                                                WataLog.i("check1!");
                                                                LayerForApp.addElement("NodeForApp" + viaPointIdx, el);
                                                                viaPointIdx++;
                                                                if (bAdd)
                                                                    ((CompositeModel) _omapView.getO2mapInstance().getModel()).addModel(LayerForApp);
                                                                _omapView.getO2mapInstance().renderUnlock();
                                                            }

                                                            Log.d("-------", "1");
                                                            _wifiScanner._wifiManager.startScan();        // Flag 4 : by Ethan

                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.d("-------", "2");
                                                _wifiScanner._wifiManager.startScan();        // Flag 4 : by Ethan
                                            }

                                        }
                                    }
                                    Log.d("jeongyeol", "send -- ok");
                                }
//                            } else if (pathFlag == 5) {

                            } else {
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

                                if (StaticManager.folderName.equalsIgnoreCase("office")) {
                                    el = ElementHelper.quadPyramidFromPoint(temp[0] + 5, temp[1] + 5, 0.1, 0.1f, 0.1f, 0.2f, Color.GREEN, Color.GREEN, Color.GREEN, false);
                                } else {
                                    if (pathFlag == 5) {
                                        pointerSize = 1.5;
                                        el = ElementHelper.quadPyramidFromPoint(temp[0], temp[1], 0.1, pointerSize, pointerSize, 1.1f, Color.RED, Color.RED, Color.RED, false);
                                    } else {
                                        el = ElementHelper.quadPyramidFromPoint(temp[0], temp[1], 0.1, pointerSize, pointerSize, 1.1f, Color.RED, Color.RED, Color.RED, false);
                                    }
                                }

                                WataLog.i("경로 표시");
                                LayerForApp.addElement("NodeForApp" + viaPointIdx, el);
                                viaPointIdx++;
                                if (bAdd) {
                                    ((CompositeModel) _omapView.getO2mapInstance().getModel()).addModel(LayerForApp);
                                }
                                _omapView.getO2mapInstance().renderUnlock();
                            }
                            break;

                        case DataCore.PDR_STAT_CHANGED: // from sensor
                            // 경로기록
                            WataLog.i("PDR_STAT_CHANGED");
                            int scanCnt = _dataCore.getSCAN_RESULT_THIS_EPOCH().B21_Payload.size();
                            if (scanCnt > 1) {
                                WataLog.d("pathFlag=" + pathFlag);
                                if (pathFlag == 5) {
                                    myWayUpdateStep("");
                                } else {
                                    updateStep();
                                }
                            }
                            break;

                        case DataCore.HANDLER_ENDED:
                            _isHandlerEnded = true;
                            break;

                        case DataCore.REFREASH_WIFI_LIST: //wifi			// Scan Step Count : by Ethan
                            WataLog.i("REFREASH_WIFI_LIST");
                            _SCAN_RESULT_TOTAL = _dataCore.getSCAN_RESULT_TOTAL();
                            _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();
                            if (_SCAN_RESULT_THIS_EPOCH.B21_Payload.size() == 2) {
                                Toast.makeText(GatheringActivity.this, "수집을 시작해주세요.", Toast.LENGTH_SHORT).show();
                            }
                            String total = _SCAN_RESULT_THIS_EPOCH.B21_Payload.size() + "회 스캔 완료";
                            map_scan_count.setText(total);
//						setStatusString(total);
                            break;

                        case DataCore.DATA_SEND_ENDED:
                            WataLog.i("DATA_SEND_ENDED");
                            _gatheringEndDialog.dismiss();
                            Toast.makeText(GatheringActivity.this, "전송이 완료되었습니다.", Toast.LENGTH_SHORT).show();
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

                        case DataCore.ON_TOUCH_MOVE_MAP:
//                            WataLog.i("ON_TOUCH_MOVE_MAP");
                            _omapView.getO2mapInstance().renderLock();
                            refreshNorthIcon();
                            _omapView.getO2mapInstance().renderUnlock();

                            break;

                        case DataCore.ON_TOUCH_MAP: // gathering listener
                            WataLog.i("ON_TOUCH_MAP");
                            float x = _mapTouchListener.getPrevX();
                            float y = _mapTouchListener.getPrevY();

                            WataLog.d("x=====" + x + "///// y=====" + y);
                            WataLog.d("bLogging=" + bLogging);
                            WataLog.d("touchedNode=" + touchedNode);

                            if (bLogging && touchedNode == null) {
                                O2Map o2map1 = (_omapView.getO2mapInstance());
                                Line line = ViewUtil.computeRayFromScreenPoint(o2map1.getView(), (int) x, (int) y);

                                Plane pl = new Plane(0.0, 0.0, 1.0, 0);

                                Vector v = pl.intersect(line);

                                //ejcha
                                if (_pathManager_for_nis != null) {
                                    touchedNode = _pathManager_for_nis.findNearestNode(v);
                                    for (int i = 0; i < _pathManager_list.size(); i++) {
                                        if (touchedNode.getUnderlyingPoint().equals(_pathManager_list.get(i).getUnderlyingPoint())) {
                                            Log.d("ejcha", "touched node , _pathManager_list matched index is " + i);
                                            _testTouchPoint = _testpointName.get(i).toString();
                                            break;
                                        }
                                    }
                                } else
                                    touchedNode = new Node(v.x, v.y, v.z);
                                _omapView.getO2mapInstance().renderLock();
                                v = touchedNode.getUnderlyingPoint();
                                if (currPos == null || currentPosLayer == null) {
                                    WataLog.i("chekc");
                                    currPos = ElementHelper.quadPyramidFromPoint(0, 0, 0, 2.5f, 2.5f, 2.5 * 2, new Color(12, 59, 146, 255), new Color(12, 59, 146, 255), new Color(12, 59, 146, 255), false);

                                    O2Map o2map = (_omapView.getO2mapInstance());
                                    boolean bAdded = false;

                                    //currentPosLayer = (SandboxModel) ((CompositeModel)o2map.getModel()).getModelByName("currentPos");

                                    if (currentPosLayer == null) {
                                        currentPosLayer = new SandboxModel("currentPos");
                                        bAdded = true;

                                    }

                                    if (currentPosLayer.size() > 0) {
                                        currentPosLayer.removeElement("curr");
                                    }
                                    currPos.getTransform().clearTransform();
                                    currPos.getTransform().setTranslation(v.x, v.y, 1);
                                    currPos.getTransform().calcMatrix();
                                    currPos.getExtentHelper().updateExtent();
                                    currentPosLayer.addElement("curr", currPos);
                                    if (bAdded)
                                        ((CompositeModel) o2map.getModel()).addModel(currentPosLayer);
                                } else if (currPos != null && currentPosLayer != null) {
                                    currPos.getTransform().clearTransform();
                                    currPos.getTransform().setTranslation(v.x, v.y, 1);
                                    currPos.getTransform().calcMatrix();
                                    currPos.getExtentHelper().updateExtent();
                                }
                                _omapView.getO2mapInstance().renderUnlock();
                                _omapView.requestRender();
                            } else
                                updateTouch(x, y);
                            break;

                        case DataCore.GATHER_MODE_CHANGED:
                            WataLog.i("GATHER_MODE_CHANGED");
                            break;

                        case 9999: //도착지점 선택시
                            map_inner_frm_linear1.setVisibility(View.INVISIBLE);
                            map_inner_frm_linear2.setVisibility(View.VISIBLE);
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
//									if(rspPos != null && (rspPos.errorCode >= 0 || rspPos.errorCode == -100 || rspPos.errorCode == -104))
                                        {
                                            Log.d("jeongyeol", "pathFlag = " + pathFlag);
                                            if (pathFlag == 4)
                                                recordPoint(logFile, rspPos, filteredPos, null, viewVector, _wifiScanner._scanResultList);
                                            else {
                                                Log.d("jeongyeol", "logging = " + pointCount);
                                                recordPoint(logFile, rspPos, filteredPos, null, viewVector, wifiList);
                                            }
                                        }


                                        if (pathFlag == 4) {
                                            Log.d("jeongyeol", "2919 -- record path log ");
                                            boolean bAdd = false;
                                            if (LayerForApp == null) {
                                                LayerForApp = new SandboxModel("LayerForApp");
                                                bAdd = true;
                                            }

//										double temp[] = (double[]) msg.obj;

                                            //						TransformGeo toGRS80 = new TransformGeo(6378137.0, 298.257223563, 1.0, 38.0, 127.0, 600000.0, 200000.0);
                                            //						Vector tempGRS80 = toGRS80.transformTM2GP(temp[1], temp[0]);
                                            //						Log.d("jeongyeol", "GRS80 lng="+tempGRS80.x+"("+temp[0]+")"+"/lat="+tempGRS80.y+"("+temp[1]+")");
                                            //						Toast.makeText(GatheringActivity.this, "GRS80 lng="+tempGRS80.x+"/lat="+tempGRS80.y, 200).show();

                                            double pointerSize = 2;

                                            Element el = null;
                                            if (touchedPoint != null) {
                                                _omapView.getO2mapInstance().renderLock();
                                                if (StaticManager.folderName.equalsIgnoreCase("office"))
                                                    el = ElementHelper.quadPyramidFromPoint(touchedPoint.x, touchedPoint.y, 0.1, 0.1f, 0.1f, 0.2f, Color.GREEN, Color.GREEN, Color.GREEN, false);
                                                else
                                                    el = ElementHelper.quadPyramidFromPoint(touchedPoint.x, touchedPoint.y, 0.1, pointerSize, pointerSize, 1.1f, Color.GREEN, Color.GREEN, Color.GREEN, false);

                                                WataLog.i("check1!");
                                                LayerForApp.addElement("NodeForApp" + viaPointIdx, el);
                                                viaPointIdx++;
                                                if (bAdd)
                                                    ((CompositeModel) _omapView.getO2mapInstance().getModel()).addModel(LayerForApp);
                                                _omapView.getO2mapInstance().renderUnlock();

                                            }
                                            Log.d("-------", "4");
                                            _wifiScanner._wifiManager.startScan();            // Flag 4 : by Ethan
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
//							Log.d("jeongyeol", "2919 -- error = "+rspPos.strError);
                                bIsGetFP = false;
                                if (pathFlag == 4 && bLogging) {
                                    Log.d("-------", "6");
                                    _wifiScanner._wifiManager.startScan();        // Flag 4 & Logging : by Ethan
                                }
                            }

                            break;
                    }

                    if (msg.what == R.id.map_complete) //완료 버튼 눌렀을때
                    {
                        map_back.setVisibility(View.VISIBLE);
                        map_bottom_linear1.setVisibility(View.VISIBLE);
                        map_bottom_linear2.setVisibility(View.INVISIBLE);
//						break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    _isHandlerEnded = true;
                }
            }
        }

    }// end of Handler

    private SandboxModel twoPointerLayer = null;

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

    private Polyline getPath(Vector start, Vector end) {

        Polyline line = null;

        if (pathFlag == 2 || pathFlag == 4) {
            List<Vector> nodes = _pathManager.findPath(start, end);
            if (nodes == null)
                return null;

            Points pts = new Points();
            pts.makePoints(nodes.size());
            for (int i = 0; i < nodes.size(); i++) {

                Vector v = nodes.get(i);
                pts.data[i].x = v.x;
                pts.data[i].y = v.y;
                pts.data[i].z = 0;
            }

            line = new Polyline();
            line.makeParts(1);
            line.setParts(0, pts);
//		}
//		else if(pathFlag==2)
//		{
//			Points pts=new Points();
//			pts.makePoints(2);
//			pts.data[0].x=start.x;
//			pts.data[0].y=start.y;
//			pts.data[0].z=0;
//
//			pts.data[1].x=end.x;
//			pts.data[1].y=end.y;
//			pts.data[1].z=0;
//
//			line=new Polyline();
//			line.makeParts(1);
//			line.setParts(0, pts);
        } else if (pathFlag == 3) {
            Points pts = new Points();
            pts.makePoints(2);
            pts.data[0].x = start.x;
            pts.data[0].y = start.y;
            pts.data[0].z = 0;

            pts.data[1].x = start.x;
            pts.data[1].y = start.y;
            pts.data[1].z = 0;

            line = new Polyline();
            line.makeParts(1);
            line.setParts(0, pts);
        }

        return line;
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

        if (!bCal && pathFlag != 4 && pointCount == (sCnt + 1)) {
            touchedPoint = null;
            touchedNode = null;
            bLogging = false;
            startLog.setText("기록");
            if (calBtn != null)
                calBtn.setVisibility(View.VISIBLE);
            pointCount = -1;
            durTime = SystemClock.uptimeMillis() - startTime;


            Toast.makeText(GatheringActivity.this, " 수집완료." + durTime / 1000 + "초/" + min + "m/" + max + "m/" + avg + "m/" + (100 * sucRate / 30) + "%", Toast.LENGTH_SHORT).show();
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
            if (pathFlag == 4 || bCal) {
                if (bCal)
                    s = s + getTime() + divider + touchedPoint.x +
                            divider + touchedPoint.y +
                            divider + pos_result.strFloor + divider + " // pdr point with calibration";
                else
                    s = s + getTime() + divider + touchedPoint.x +
                            divider + touchedPoint.y +
                            divider + StaticManager.floorName + divider + " // pdr point - no calibration";

//        		touchedPoint = touchedNode.getUnderlyingPoint();
            } else {
                s = s + getTime() + divider + touchedNode.getUnderlyingPoint().x +
                        divider + touchedNode.getUnderlyingPoint().y +
                        divider + StaticManager.floorName + divider + " // touched point"
                ;
                touchedPoint = touchedNode.getUnderlyingPoint();
            }
            pointCount = 0;
        } else if (pos_result != null) {
            if (pathFlag == 4)
                touchedPoint = new Vector(pdrvariable.getPedestrian_x_coordinate(), pdrvariable.getPedestrian_y_coordinate(), 0);
//        	else
//        	{
////        	if(pos_result.errorCode < -103 || pos_result.errorCode > -101)
////        	{
//	        	if(/*filter30 && */pos_result.x != 0 && pos_result.y != 0 && Math.sqrt(Math.pow(pos_result.x-touchedPoint.x, 2)+Math.pow(pos_result.y-touchedPoint.y, 2)) > 30)
//	        	{
//	        		if(SystemClock.uptimeMillis()%4 == 0)
//	        		{
//		        		pos_result.x = touchedPoint.x+SystemClock.uptimeMillis()%30;
//		        		pos_result.y = touchedPoint.y;
//	        		}
//	        		else if(SystemClock.uptimeMillis()%4 == 1)
//	        		{
//	        			pos_result.x = touchedPoint.x;
//		        		pos_result.y = touchedPoint.y+SystemClock.uptimeMillis()%30;
//	        		}
//	        		else if(SystemClock.uptimeMillis()%4 == 2)
//	        		{
//	        			pos_result.x = touchedPoint.x;
//		        		pos_result.y = touchedPoint.y-SystemClock.uptimeMillis()%30;
//	        		}
//	        		else
//	        		{
//		        		pos_result.x = touchedPoint.x-SystemClock.uptimeMillis()%30;
//		        		pos_result.y = touchedPoint.y;
//	        		}
//	        	}
////        	}
//        	}
            s = getTime() + divider;
            if (pathFlag == 4)
                s = s + touchedPoint.x + divider + touchedPoint.y + divider;

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
                Toast.makeText(GatheringActivity.this, (pointCount) + " 포인트 수집.", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(GatheringActivity.this, "db 없음", Toast.LENGTH_SHORT).show();
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
        _mapLayout.addView(mBle);

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
                            if (android.os.Build.BRAND.equalsIgnoreCase("samsung")) {
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
