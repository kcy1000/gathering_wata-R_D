package com.geotwo.LAB_TEST.Gathering;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.dialog.GpsDialog;
import com.geotwo.LAB_TEST.Gathering.dto.BuildInfo;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.data.FloorInfo;
import com.geotwo.LAB_TEST.data.StaticManager;
import com.geotwo.common.KalmanFilter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.firebase.analytics.FirebaseAnalytics;
import com.wata.LAB_TEST.Gathering.R;

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapLayout;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.sf.jazzlib.ZipEntry;
import net.sf.jazzlib.ZipInputStream;

import org.w3c.dom.Text;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import GEO2.LBSP.SClient.RspPosition;
import pdr_collecting.core.pdrvariable;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {
    public WifiManager wifiManager;
    private final static String TAG = "LBS DEMO APP";
    private final static int RES_OK = 10;
    private final static int RES_BACK = 30;
    private final static int FROM_MAP_SELECTOR = 10000;
    private final static int FROM_MAP_NAVIGATION = 20000;
    private final String AREA_DIR = Environment.getExternalStorageDirectory().getPath() + "/gathering/area";
    static public Handler _UIHandler;
    private LinearLayout main_linear;
    private TextView main_name, address_textview;
    private ListView main_list;
    private ArrayList<FloorInfo> floorArr;
    private FloorInfoAdapter adapter;
    private com.geotwo.TBP.TBPWiFiScanner wifiScanner;
    private KalmanFilter mKf = null;
    private RspPosition rspPos;
    public static boolean bScanning = false;
    private ArrayList<String> mapArr;
    public static int iFreq = 0; // 2 = 2.4GHz, 0 = 2.4 and 5 GHz
    private Button BtwoFreq, BfiveFreq;
    DataCore _dataCore = null;
    MainButtonClickListener _mainButtonClickListener = null;
    ArrayList inDoorDataList = new ArrayList();
    ArrayList<String> selResult = new ArrayList<String>();
    Menu _mMenu = null;
    Intent _intent = null;
    Thread scanThread = null;
    private TextView text_ip, text_ap;
    //public static String CONNECTING_IP = "58.103.10.174";
    //public static String SOCKET_IP	   = "58.103.10.174";

    public static String CONNECTING_IP = DataCore.IP_ADDRESS;
    public static String SOCKET_IP = DataCore.IP_ADDRESS;

//    private MapView mMapView;

    // Firebase
//    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        WataLog.i("onCreate");

        checkPermission();
        initializing();

        setLayout();

        setStepFirst();

        WataLog.d("url = " + _dataCore.getURL());

//        getHashKey();

//        setKakaoMapview();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setLocation();

        iFreq = 0;
        BtwoFreq.setSelected(false);
        BfiveFreq.setSelected(true);

//		MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder("LOCAL_API_KEY", mapPoint, reverseGeoCodingResultListener, contextActivity);
//		reverseGeoCoder.startFindingAddress();
    }

//    private void setKakaoMapview() {
//
//        MapLayout mapLayout = new MapLayout(this);
//        mMapView = mapLayout.getMapView();
//        mMapView.setDaumMapApiKey("1a8235e232a962da2c7c14604b98b226");
////		mMapView.setOpenAPIKeyAuthenticationResultListener(this);
////        mMapView.setMapViewEventListener(this);
////        mMapView.setPOIItemEventListener(this);
//        mMapView.setMapType(MapView.MapType.Standard);
//        mMapView.setZoomLevel(8, true);
//        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.kakao_map_view);
//        mapViewContainer.addView(mapLayout);
//
//        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
//
//        mMarker = new MapPOIItem();
//    }

    private MapPOIItem mMarker;
    public double longitude; //경도
    public double latitude; //위도
    public double altitude; //고도
    public float accuracy; //정확도
    public String provider; //위치제공자
    public String currentLocation; // 그래서 최종 위치
    public LocationManager lm;

    public void setLocation() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocation();
    }

    public void getLocation() {
        try {
            // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                    100, // 통지사이의 최소 시간간격 (miliSecond)
                    1, // 통지사이의 최소 변경거리 (m)
                    mLocationListener);
            //txtCurrentPositionInfo.setText("위치정보 미수신중");
            //lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.
        } catch (SecurityException ex) {

        }

    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.
            longitude = location.getLongitude(); //경도
            latitude = location.getLatitude();   //위도
            altitude = location.getAltitude();   //고도
            accuracy = location.getAccuracy();    //정확도
            provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            WataLog.d("accuracy:" + accuracy);
            WataLog.d("latitude:" + latitude);
            WataLog.d("longitude:" + longitude);
            currentLocation = getCompleteAddressString(getApplicationContext(), latitude, longitude);
            // 지도를 움직인다
//            setDaumMapCurrentLocation(latitude, longitude);
            mPoiName = currentLocation;
            lm.removeUpdates(mLocationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.

        }

        public void onProviderDisabled(String provider) {
            // Disabled시
            WataLog.d("onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            WataLog.d("onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            WataLog.d("onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };

//    public void setDaumMapCurrentLocation(double latitude, double longitude) {
//        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), true);
//        mMapView.setZoomLevel(2, true);
//        mMapView.zoomIn(true);
//        mMapView.zoomOut(true);
//        setDaumMapCurrentMarker();
////		createDefaultMarker(mMapView);
//    }

    private MapPOIItem mDefaultMarker;
    private static final MapPoint DEFAULT_MARKER_POINT = MapPoint.mapPointWithGeoCoord(37.4020737, 127.1086766);
    private String mPoiName = "";

//    public void setDaumMapCurrentMarker() {
//        WataLog.i("setDaumMapCurrentMarker");
//        mPoiName = getCompleteAddressString(getApplicationContext(), latitude, longitude);
//        mMarker.setItemName(mPoiName);
//        mMarker.setTag(0);
//        mMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
//        mMarker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
//        mMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//
//        mMapView.addPOIItem(mMarker);
//        address_textview.setText(mPoiName);
//
////		mMapView.selectPOIItem(mMarker, true);
////		mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), false);
//
//    }

//    @Override
//    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
//        WataLog.i("onPOIItemSelected");
//    }
//
//    @Override
//    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
//        WataLog.i("onCalloutBalloonOfPOIItemTouched");
//
//        AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
//        alBuilder.setMessage("이 위치 지도를 생성하시겠습니까?");
//        alBuilder.setPositiveButton("예", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                StaticManager.setTitle("PathDrawing");
//                StaticManager.setFolderName("PathDrawing");
//                StaticManager.setAddress("PathDrawing");
//
//                StaticManager.setBasePointX("202351.273872");
//                StaticManager.setBasePointX("544170.137029");
//                StaticManager.setGid("1168010100108210001S01300");
//                StaticManager.setPosition(0);
////                StaticManager.setFloorName("TEST_1");
//                Intent intent = new Intent(MainActivity.this, PathDrawingActivity.class);
//                intent.putExtra("file_name", mPoiName);
//
//                startActivity(intent);
//            }
//        });
//        alBuilder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                return;
//            }
//        });
////		alBuilder.setTitle("");
//        alBuilder.show();
//
//    }
//
//    @Override
//    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
//        WataLog.i("onCalloutBalloonOfPOIItemTouched");
//    }
//
//    @Override
//    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
//        WataLog.i("onDraggablePOIItemMoved");
//    }


    // CalloutBalloonAdapter 인터페이스 구현
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            WataLog.d("poiItem=" + poiItem.getItemName());
            ((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageResource(R.drawable.ic_launcher);
            ((TextView) mCalloutBalloon.findViewById(R.id.title)).setText(poiItem.getItemName());
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }

//    @Override
//    public void onMapViewInitialized(net.daum.mf.map.api.MapView mapView) {
//        WataLog.i("onMapViewInitialized");
//
//    }
//
//    @Override
//    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {
//        WataLog.i("mapPoint = " + mapPoint.getMapPointGeoCoord());
//    }
//
//    @Override
//    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
//        WataLog.i("onMapViewZoomLevelChanged");
//    }
//
//    @Override
//    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
//        WataLog.i("mapPoint = " + mapPoint.getMapPointGeoCoord());
//        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
////		MapPoint.PlainCoordinate mapPointScreenLocation = mapPoint.getMapPointScreenLocation();
////		WataLog.d(String.format("MapView onMapViewDragStarted (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude));
////        mPoiName = getCompleteAddressString(getApplicationContext(), mapPointGeo.latitude, mapPointGeo.longitude);
////        WataLog.d("mPoiName=" + mPoiName);
////        address_textview.setText(mPoiName);
////        setMarkerPoint(mPoiName, mapPointGeo.latitude, mapPointGeo.longitude);
//    }
//
//    @Override
//    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
//        WataLog.i("onMapViewDoubleTapped");
//    }
//
//    @Override
//    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
//        WataLog.i("onMapViewLongPressed");
//    }
//
//    @Override
//    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
//        WataLog.i("mapPoint = " + mapPoint.getMapPointGeoCoord().latitude);
//
//
////		setMarkerPoint(mPoiName, mapPointGeo.latitude, mapPointGeo.longitude);
//
//    }
//
//    @Override
//    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
//        WataLog.i("mapPoint = " + mapPoint.getMapPointGeoCoord());
//    }
//
//    @Override
//    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
//        WataLog.i("mapPoint = " + mapPoint.getMapPointGeoCoord());
//    }

    public static String getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            WataLog.d("addresses=" + addresses);

            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                WataLog.d(strReturnedAddress.toString());

            } else {
                WataLog.i("No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            WataLog.i("Canont get Address!");
        }

        // "대한민국 " 글자 지워버림
        if (!"".equals(strAdd) && strAdd.length() > 5) {
            strAdd = strAdd.substring(5);
            WataLog.d("strAdd=" + strAdd);
        }
        return strAdd;
    }

//    private void setMarkerPoint(String poiName, double latitude, double longitude) {
//        mMapView.removeAllPOIItems();
//
//        mMarker.setItemName(poiName);
//        mMarker.setTag(0);
//        mMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude));
//        mMarker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
//        mMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//
//        mMapView.selectPOIItem(mMarker, true);
//        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(latitude, longitude), false);
//        mMapView.addPOIItem(mMarker);
//    }

    private void setDirectories() {
        String areaPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/area/";
        File areaDir = new File(areaPath);
        if (!areaDir.exists()) {
            areaDir.mkdirs();
        }
    }

    private void setStepFirst() {
        SharedPreferences pref = getSharedPreferences("stepPref", 0);
        int stepLen = pref.getInt("stepLength", 70);
        pdrvariable.setStep_length(stepLen);

//        int pathdrawing_stepLength = pref.getInt("pathdrawing_stepLength", 70);
//        pdrvariable.setPresent_Step_length(pathdrawing_stepLength);

//		Log.e("tag", "stepLen = "+pdrvariable.getNominal_step_length());
    }

    @Override
    protected void onStart() {
        super.onStart();
        WataLog.d("onStart");
        WataLog.d("onStart : call mFusedLocationClient.requestLocationUpdates");
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
//        }
//        if (mMap != null) {
//            mMap.setMyLocationEnabled(true);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (mFusedLocationClient != null) {
//            WataLog.d("onStop : call stopLocationUpdates");
//            mFusedLocationClient.removeLocationUpdates(locationCallback);
//        }
    }

    private int mFloorsLever = 0;

    private void setLayout() {

//        googleMapSetting();
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
//        mapFragment.getMapAsync(this);

//        main_linear = (LinearLayout) findViewById(R.id.main_linear);
//        main_name = (TextView) findViewById(R.id.main_name);
//        main_list = (ListView) findViewById(R.id.main_list);
        text_ip = (TextView) findViewById(R.id.text_ip);
        text_ap = (TextView) findViewById(R.id.text_ap);

        findViewById(R.id.downloadMap).setOnClickListener(_mainButtonClickListener);
        findViewById(R.id.curLocation).setOnClickListener(_mainButtonClickListener);
        findViewById(R.id.my_location).setOnClickListener(_mainButtonClickListener);
        findViewById(R.id.selectMap).setOnClickListener(_mainButtonClickListener);
        findViewById(R.id.viewInfo).setOnClickListener(_mainButtonClickListener);
        findViewById(R.id.exit).setOnClickListener(_mainButtonClickListener);
        findViewById(R.id.ip_change).setOnClickListener(_mainButtonClickListener);
        findViewById(R.id.ap_change).setOnClickListener(_mainButtonClickListener);

        //kcy1000 - 지도생성진입
        findViewById(R.id.make_map_btn).setOnClickListener(_mainButtonClickListener);
        // 카카오지도
//        kakao_map_layout = (RelativeLayout) findViewById(R.id.kakao_map_view);
        //지도생성 주소
        address_textview = (TextView) findViewById(R.id.address_textview);

        String[] stringMin = new String[100];
        for (int i = 0; i < stringMin.length; i++) {
            stringMin[i] = Integer.toString(i - 9);
        }

        //층수확인
        NumberPicker floors_lever_picker = (NumberPicker) findViewById(R.id.floors_lever_picker);
        floors_lever_picker.setMinValue(0);
        floors_lever_picker.setMaxValue(99);
        floors_lever_picker.setDisplayedValues(stringMin);
        floors_lever_picker.setWrapSelectorWheel(false);

        floors_lever_picker.setValue(10);
        floors_lever_picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                WataLog.d("newVal=" + newVal);
                mFloorsLever = newVal - 9;
            }
        });

        setTextIP();
        setTextAP();

        BtwoFreq = (Button) findViewById(R.id.twoFreq);
        BtwoFreq.setOnClickListener(_mainButtonClickListener);
        BfiveFreq = (Button) findViewById(R.id.fiveFreq);
        BfiveFreq.setOnClickListener(_mainButtonClickListener);

        if (iFreq < 1) {
            BfiveFreq.setSelected(true);
            BtwoFreq.setSelected(false);
        } else {
            WataLog.d(String.valueOf(iFreq));
            BtwoFreq.setSelected(true);
            BfiveFreq.setSelected(false);
        }
    }

    //DB IP
    private void setTextIP() {
        WataLog.d("setTextIP");
        SharedPreferences pref = getSharedPreferences("IP_PREF", 0);
        String ip = pref.getString("CONNECTING_IP", "");
        String port = pref.getString("CONNECTING_PORT", DataCore.DATA_UP_LORD_PORT);

        if (ip == null || ip.equals("")) {
            text_ip.setText(CONNECTING_IP + ":" + port);
            _dataCore.setURL(CONNECTING_IP);
            _dataCore.setRmcsPort(port);
        } else {
            CONNECTING_IP = pref.getString("CONNECTING_IP", "");
            text_ip.setText(CONNECTING_IP + ":" + port);
            _dataCore.setURL(CONNECTING_IP);
            _dataCore.setRmcsPort(port);
        }
    }

    private void setTextAP() {
        SharedPreferences pref = getSharedPreferences("IP_PREF", 0);
        String ap = pref.getString("SOCKET_IP", "");
        String port = pref.getString("SOCKET_PORT", "2919");
        if (ap == null || ap.equals("")) {
            text_ap.setText(SOCKET_IP + ":" + port);
            _dataCore.setTpsURL(SOCKET_IP);
            _dataCore.setTpsPort(port);
        } else {
            SOCKET_IP = pref.getString("SOCKET_IP", "");
            text_ap.setText(SOCKET_IP + ":" + port);
            _dataCore.setTpsURL(SOCKET_IP);
            _dataCore.setTpsPort(port);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        System.exit(0);
//        WataLog.d("exit this application...");
    }

    private void initializing() {
        /**
        * Firebase Crashlystics 초기화
        * @author Ted
        * @since 2020/10/29 3:24 PM
        **/
//        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        mFirebaseAnalytics.setUserId("DeviceID");
        if (_dataCore == null) {
            _dataCore = DataCore.getInstance();
        }

        if (_mainButtonClickListener == null) {
            _mainButtonClickListener = new MainButtonClickListener();
        }

        loadSavedDatas();
    }

    private CustomAlertDialog mDialog;

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        // TODO Auto-generated method stub
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_BACK:
//                mDialog = new CustomAlertDialog(MainActivity.this, 1, "앱을 종료하시겠습니까?", new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // TODO Auto-generated method stub
//                        MainActivity.this.finish();
//                        ActivityCompat.finishAffinity(MainActivity.this);
//                        System.exit(0);
//                    }
//                });
//
//                mDialog.show();
//                break;
//        }
//        return true;
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FROM_MAP_SELECTOR) {

            if (resultCode == RESULT_OK) {
                main_name.setText(StaticManager.title);
//				floorArr = StaticManager.floorInfo;
//				adapter.notifyDataSetChanged();
                if (StaticManager.folderName != null && StaticManager.folderName.equalsIgnoreCase("new_area") && StaticManager.worker.equalsIgnoreCase("PILOT_KISA")) {
                    if (floorArr != null) {
                        floorArr.clear();
                        floorArr = null;
                    }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    Intent simple = new Intent(MainActivity.this, SimpleGatheringActivity.class);
                    startActivity(simple);
                } else {
                    if (StaticManager.floorInfo != null) {
                        floorArr = StaticManager.floorInfo;
                    } else {
                        floorArr = new ArrayList<FloorInfo>();
                    }

                    adapter = new FloorInfoAdapter(MainActivity.this, R.layout.new_selectmap_row, floorArr);
                    main_list.setAdapter(adapter);

//                    make_map_layout.setVisibility(View.VISIBLE);
                }
            }

            // 정상적으로 데이터를 입력한경우
			/*else if (resultCode == RES_OK)
			{
				String station = data.getStringExtra("station");
				String floor = data.getStringExtra("floor");
				String fileName = data.getStringExtra("fileName");

				Intent intent = new Intent(MainActivity.this, GatheringActivity.class);
				intent.putExtra("station", station);
				intent.putExtra("floor", floor);
				intent.putExtra("fileName", fileName);

				_dataCore.setSCAN_RESULT_THIS_EPOCH(new PDI_REPT_COL_DATA(android.os.Build.SERIAL));
				_dataCore.setSCAN_RESULT_TOTAL(new ArrayList<PDI_REPT_COL_DATA>());
				pdrvariable.initValues();

				startActivity(intent);
				Log.e(TAG, "Selected floor is " + floor + " on " + station);
			}*/
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        _mMenu = menu;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settingmenu, _mMenu);

        menu.getItem(0).setTitle("설정");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString() == "설정") {
            _intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(_intent);
        }
        return true;
    }

    private void loadSavedDatas() {
        byte[] serverData;
        byte[] portData;

        try {
            FileInputStream fis = openFileInput("server.data");

            serverData = new byte[fis.available()];

            while (fis.read(serverData) != -1) {
            }

            String serverUrl = new String(serverData);

            if (serverUrl.length() > 1) {
//				_dataCore.setURL(serverUrl);
            }
        } catch (Exception e) {
        }

        try {
            FileInputStream fis = openFileInput("port.data");

            portData = new byte[fis.available()];

            while (fis.read(portData) != -1) {
            }

            String stringPort = new String(portData);
            int port = Integer.parseInt(stringPort);

            if (port != -2000) {
                _dataCore.setPORT(port);
            }
        } catch (Exception e) {
        }
    }

    private void setDownloadMapDialog() {
        new getIndoorList().execute();
    }


    class getIndoorList extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPostExecute(Integer serverResponseCode) {
            if (serverResponseCode != 200) {
                Toast.makeText(MainActivity.this, "서버접속에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            } else {

                final boolean[] mBoolean;

                mBoolean = new boolean[inDoorDataList.size()];
                CharSequence[] cs = (CharSequence[]) inDoorDataList.toArray(new CharSequence[inDoorDataList.size()]);
                for (int i = 0; i < inDoorDataList.size(); i++) mBoolean[i] = false;

                // List Adapter 생성
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        MainActivity.this,
                        android.R.layout.select_dialog_multichoice);

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                        MainActivity.this);
                alertBuilder.setIcon(R.drawable.ic_launcher);
                alertBuilder.setTitle("업데이트할 도면을 선택하세요.").
                        setMultiChoiceItems(cs, mBoolean, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    selResult.add((String) inDoorDataList.get(which));
                                } else if (selResult.contains(inDoorDataList.get(which))) {
                                    selResult.remove(selResult.indexOf(inDoorDataList.get(which)));
                                }
                            }
                        });
                // 버튼 생성
                alertBuilder.setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                                inDoorDataList.clear();
                            }
                        });
                alertBuilder.setPositiveButton("업데이트",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                downloadMap();
                                inDoorDataList.clear();
                            }
                        });
                alertBuilder.show();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {

            URL url = null;
            int serverResponseCode = 0;
            try {

                //서버 폴더리스트 가져오기
                String line;
                url = new URL("http://" + _dataCore.getURL() + ":8000/gathering/downloadMap.php");
                HttpURLConnection mUrlConnection = (HttpURLConnection) url.openConnection();
                mUrlConnection.setDoInput(true);
                mUrlConnection.setDoOutput(true); // Allow Outputs
                mUrlConnection.setUseCaches(false); // Don't use a Cached Copy

                InputStream is = new BufferedInputStream(mUrlConnection.getInputStream());
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuffer response = new StringBuffer();
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\n');
                }
                rd.close();
                serverResponseCode = mUrlConnection.getResponseCode();
                String rspString = URLDecoder.decode(response.toString(), "euc-kr");
                String[] rspData = rspString.split("\r");
                for (int i = 0; i < rspData.length; i++) {
                    String[] parse_data_city = rspData[i].split("&");
                    //1:서울 2:가산디지털단지,공덕,군자...
                    for (int j = 1; j < parse_data_city.length; j++) { //2
                        String[] parse_data_dong = parse_data_city[j].split(",");
                        for (int k = 0; k < parse_data_dong.length; k++) { //2
                            String[] parseData = parse_data_dong[k].split("_");
                            if (parseData.length == 2) {
                                inDoorDataList.add(parseData[1] + "(" + parseData[0] + ")");
                            } else {
                                inDoorDataList.add(parseData[1] + "(" + parseData[0] + "/" + parseData[2] + ")");
                            }

                        }
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return serverResponseCode;
        }
    }

    private void downloadMap() {
        new getDownloadMap().execute();
    }

    private void extractZip(String _zipFile, String _location) throws IOException {
        FileInputStream fin = new FileInputStream(_zipFile);
        ZipInputStream zin = new ZipInputStream(fin);
        ZipEntry ze = null;
        try {
            while ((ze = zin.getNextEntry()) != null) {
                WataLog.d("Unzipping " + ze.getName());
                if (ze.isDirectory()) {
                    File f = new File(_location + ze.getName());
                    if (!f.isDirectory()) {
                        f.mkdirs();
                    }
                } else {
                    FileOutputStream fout = null;
                    try {
                        fout = new FileOutputStream(_location + ze.getName());
                        byte b[] = new byte[1024];
                        int n;
                        while ((n = zin.read(b)) > 0) {
                            fout.write(b, 0, n);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (fout != null) fout.close();
                    }
                }
            }

            fin.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            zin.close();
            new File(_zipFile).delete();
        }
    }

    class getDownloadMap extends AsyncTask<Void, Void, Integer> {

        @Override
        protected void onPostExecute(Integer serverResponseCode) {
            if (serverResponseCode != 200) {
                Toast.makeText(MainActivity.this, "다운로드에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            URL url = null;
            String address = null;
            File file = null;
            String mapName = null;
            String line = null;
            String path = Environment.getExternalStorageDirectory().getPath() + "/gathering/";

            int serverResponseCode = 0;
            try {
                for (int i = 0; i < selResult.size(); i++) {
                    int startP = selResult.get(i).lastIndexOf("(") + 1;
                    int endP = selResult.get(i).lastIndexOf(")");
                    String[] city_tmp = selResult.get(i).substring(startP, endP).split("/");
                    String city = city_tmp[0];
                    if (city_tmp.length == 1) {
                        mapName = selResult.get(i).replace("(" + city + ")", "");
                        address = "http://" + _dataCore.getURL() + ":8000/gathering/downloadMap/gathering/" + city + "/" + city + "_" + mapName + ".zip";
                        file = new File(path, city + "_" + mapName + ".zip");
                    } else {
                        line = city_tmp[1];
                        mapName = selResult.get(i).replace("(" + city + "/" + line + ")", "");
                        address = "http://" + _dataCore.getURL() + ":8000/gathering/downloadMap/gathering/" + city + "/" + city + "_" + mapName + "_" + line + ".zip";
                        file = new File(path, city + "_" + mapName + "_" + line + ".zip");
                    }
                    url = new URL(address);

                    HttpURLConnection mUrlConnection = (HttpURLConnection) url.openConnection();
                    mUrlConnection.setDoInput(true);
                    mUrlConnection.setDoOutput(true); // Allow Outputs
                    mUrlConnection.setUseCaches(false); // Don't use a Cached Copy

                    FileOutputStream fos = new FileOutputStream(file);
                    InputStream is = mUrlConnection.getInputStream();
                    serverResponseCode = mUrlConnection.getResponseCode();
                    int downloadedSize = 0;
                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;
                    while ((bufferLength = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, bufferLength);
                        downloadedSize += bufferLength;

                    }
                    fos.close();
                    mUrlConnection.disconnect();

                    //압축해제
//					File f = new File(path+"/area/"+mapName);
//					if(!f.exists()) f.mkdir();
//					extractZip(path+city+"_"+mapName+".zip",path+"area/"+mapName+"/");
                    if (city_tmp.length == 1) {
                        extractZip(path + city + "_" + mapName + ".zip", path + "area/");
                    } else {
                        extractZip(path + city + "_" + mapName + "_" + line + ".zip", path + "area/");
                    }

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return serverResponseCode;
        }
    }

    /**
     * Permission check.
     */
    private final int MY_PERMISSION = 1;

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // Explain to the user why we need to write the permission.
                    Toast.makeText(this, "Read/Write external storage, Access location", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
//						Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.CHANGE_NETWORK_STATE}, MY_PERMISSION);

                // MY_PERMISSION_REQUEST_STORAGE is an
                // app-defined int constant

            } else {
                // 다음 부분은 항상 허용일 경우에 해당이 됩니다.
//                writeFile();
                onPermissionGranted();
            }
        }
        if (GpsDialog.makeGpsCheckDialog(this) != null) {
            GpsDialog.makeGpsCheckDialog(this).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION:
                if (grantResults.length > 0) {
                    int grant = PackageManager.PERMISSION_GRANTED;
                    for (int tempGrant : grantResults) {
                        if (tempGrant == PackageManager.PERMISSION_DENIED) {
                            grant = tempGrant;
                            break;
                        }
                    }
                    if (grant == PackageManager.PERMISSION_GRANTED) {
                        onPermissionGranted();
                    } else {
                        Toast.makeText(this, "앱 설정에서 권한 설정을 해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void onPermissionGranted() {
        setDirectories();
    }

    public class MainButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();

            WataLog.d("id=== " + id);
            if (id == R.id.downloadMap) {
                setDownloadMapDialog();
            }
            if (id == R.id.my_location) {
                // kcy1000
                // 카카오맵
//                kakao_map_layout.setVisibility(View.VISIBLE);
//                main_linear.setVisibility(View.GONE);
//                getLocation();

                // 구글맵

//                startLocationUpdates();
//                mMap.getUiSettings().setMyLocationButtonEnabled(true);
//                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
            if (id == R.id.curLocation) {
                if (!bScanning) {

                    bScanning = true;
                    scanThread = new Thread(new Runnable() {
                        public void run() {
                            int scanFailCnt = 0;
                            while (bScanning) {
                                wifiScanner = new com.geotwo.TBP.TBPWiFiScanner(MainActivity.this);
                                wifiScanner.wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                IntentFilter NETWORK_STATE_LISTENER = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                                NETWORK_STATE_LISTENER.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                                NETWORK_STATE_LISTENER.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
                                NETWORK_STATE_LISTENER.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
                                NETWORK_STATE_LISTENER.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

                                registerReceiver(wifiScanner, NETWORK_STATE_LISTENER);
                                wifiScanner.ipAddress = _dataCore.getTpsURL();
                                wifiScanner.port = _dataCore.getTpsPort();
                                wifiScanner.startScan();                // "현재 위치" 버튼 클릭

                                while (!wifiScanner.isScanComplete) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                                rspPos = wifiScanner.sendData_main(GEO2.LBSP.SClient.SvrPositionProvider.COORDTYPE.emMiddleTM);
                                System.gc();

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                if (!(rspPos == null)) {
                                    bScanning = false;
                                    File dir = new File(AREA_DIR);
                                    if (dir.exists()) {
                                        String[] strList = dir.list();

                                        for (int a = 0; a < strList.length; a++) {
                                            parseXml(strList[a]);
                                            String gid = StaticManager.gid;
                                            if (gid.equals(rspPos.GID)) {
                                                StaticManager.setFloorName(rspPos.strFloor);
//												gActivity.setBscannning(false);
                                                Intent goMap = new Intent(MainActivity.this, GatheringActivity.class);
                                                goMap.putExtra("bScanning", "false");
                                                startActivity(goMap);
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    scanFailCnt++;
                                    if (scanFailCnt == 3) {
                                        bScanning = false;
                                        scanFailCnt = 0;
                                    }
                                }

                            }
                        }
                    });
                    scanThread.start();
                }
            }
            if (id == R.id.selectMap) {
                Intent mapSelectIntent = new Intent(MainActivity.this, SelectMapAct.class);
                startActivityForResult(mapSelectIntent, FROM_MAP_SELECTOR);

                main_linear.setVisibility(View.VISIBLE);
//
//				Intent mapSelectIntent = new Intent(MainActivity.this, MapSelectActivity.class);
//				startActivityForResult(mapSelectIntent, FROM_MAP_SELECTOR);
            }
//				break;

            else if (id == R.id.make_map_btn) { //지도만들기

//                if ("".equals(mPoiName)) {
//                    Toast.makeText(MainActivity.this, getString(R.string.none_address_message_1), Toast.LENGTH_SHORT).show();
//                } else {

//                    StaticManager.setTitle("PathDrawing");
//                    StaticManager.setFolderName("PathDrawing");
//                    StaticManager.setAddress("PathDrawing");
//
//                    StaticManager.setBasePointX("202351.273872");
//                    StaticManager.setBasePointX("544170.137029");
//                    StaticManager.setGid("1168010100108210001S01300");
//                    StaticManager.setPosition(0);
//                    StaticManager.setFloorName("AB01");
//                    Intent intent = new Intent(MainActivity.this, PathDrawingActivity.class);
//                    WataLog.d("mPoiName=" + mPoiName + "_" + mFloorsLever + "층");
//                    intent.putExtra("file_name", mPoiName + "_" + mFloorsLever+ "층");
//                    startActivity(intent);
                mPoiName = "tsetw";
                mFloorsLever = 1;
                    Intent intent = new Intent(MainActivity.this, SelectMapActivity.class);
                    WataLog.d("mPoiName=" + mPoiName + "_" + mFloorsLever + "층");
                    intent.putExtra("file_name", mPoiName + "_" + mFloorsLever + "층");
                    intent.putExtra("floors_lever", mFloorsLever + "층");
                    startActivity(intent);

//                }

            } else if (id == R.id.viewInfo) {
                Intent intent = new Intent(MainActivity.this, GatherListActivity.class);
                startActivity(intent);
            }
//	    		break;

            else if (id == R.id.exit) {
                mDialog = new CustomAlertDialog(MainActivity.this, 1, "앱을 종료하시겠습니까?", new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        MainActivity.this.finish();
                        ActivityCompat.finishAffinity(MainActivity.this);
                        System.exit(0);
                    }
                });
                mDialog.show();
            }
//	    		break;

            else if (id == R.id.ip_change)
                new CustomAlertDialog(MainActivity.this, CONNECTING_IP, uiHandler, 0).show();
//	    		break;

            else if (id == R.id.ap_change)
                new CustomAlertDialog(MainActivity.this, SOCKET_IP, uiHandler, 1).show();
//	    		break;
//			}
            else if (id == R.id.twoFreq) {
                iFreq = 2;
                BtwoFreq.setSelected(true);
                BfiveFreq.setSelected(false);
                WataLog.d("(2)two setiFreq = " + iFreq);
            } else if (id == R.id.fiveFreq) {
                iFreq = 0;
                BtwoFreq.setSelected(false);
                BfiveFreq.setSelected(true);
                WataLog.d("(0)five setiFreq = " + iFreq);
            }
        }
    }

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    setTextIP();
                    break;

                case 1:
                    setTextAP();
                    break;
            }
        }
    };

    class FloorInfoAdapter extends ArrayAdapter<FloorInfo> implements OnClickListener {
        ArrayList<FloorInfo> items;
        Context context = null;

        public FloorInfoAdapter(Context context, int textViewResourceId, ArrayList<FloorInfo> items) {
            super(context, textViewResourceId, items);

            this.items = items;
            this.context = context;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            if (v == null) {
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.new_selectmap_row, null);
            }

            FloorInfo data = items.get(position);

            if (data != null) {
                String strName = data.getName();

                TextView main_row_text = (TextView) v.findViewById(R.id.selectmap_row_text);
                main_row_text.setText(strName);

//				Log.i("tag", "name = "+strName);
//				Log.i("tag", "wall = "+data.getWall());
//				Log.i("tag", "base = "+data.getBase());
//				Log.i("tag", "poi = "+data.getPoi());
//				Log.i("tag", "path = "+data.getPath());
            }

            v.setTag(position);
            v.setOnClickListener(this);

            return v;
        }

        @Override
        public void onClick(View v) {
            final int pos = Integer.valueOf(v.getTag().toString());
            WataLog.d("position = " + pos);

            //v.setBackgroundColor(Color.DKGRAY);
            StaticManager.setPosition(pos);
            WataLog.d("tems.get(pos).getName()=" + items.get(pos).getName());
            StaticManager.setFloorName(items.get(pos).getName());

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("선택한 지역으로 진행합니다.")
                    .setMessage(StaticManager.title + ", " + items.get(pos).getName())
                    .setPositiveButton("선택완료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (StaticManager.worker != null && StaticManager.worker.equalsIgnoreCase("PILOT_KISA")) {
                                Intent simple = new Intent(MainActivity.this, SimpleGatheringActivity.class);
                                startActivity(simple);
                            } else {
                                Intent goMap = new Intent(MainActivity.this, GatheringActivity.class);
                                startActivity(goMap);
                            }
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        }
    }

    @Override
    public void onDestroy() {
        StaticManager.basePointX = "";
        StaticManager.basePointY = "";
        StaticManager.address = "";
        StaticManager.folderName = "";
        StaticManager.gid = "";
        StaticManager.floorName = "";
        StaticManager.title = "";
        StaticManager.worker = "";

//        if (mDialog != null) mDialog.dismiss();
//        mMapView = null;

        super.onDestroy();
    }

    public void parseXml(String folderName) {
        try {

            XmlPullParserFactory parserFac = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFac.newPullParser();
            String xmlName = null;
            if (folderName.contains("_")) {
                xmlName = folderName.split("_")[1];
                parser.setInput(new FileInputStream(new File(AREA_DIR + "/" + folderName + "/" + xmlName + ".xml")), "UTF-8");
            } else {
                parser.setInput(new FileInputStream(new File(AREA_DIR + "/" + folderName + "/" + folderName + ".xml")), "UTF-8");
            }
            int parserEvent = parser.getEventType();
            String tag = "";
            boolean flag1 = false, flag2 = false, flag3 = false, flag4 = false, flag5 = false, flag6 = false, flag7 = false, flag8 = false,
                    flag9 = false, flag10 = false;
            boolean nameFlag = false, wallFlag = false, baseFlag = false, poiFlag = false, pathFlag = false,
                    latFlag = false, lonFlag = false, bearFlag = false, Acc_threshold = false;
            FloorInfo info = null;
            ArrayList<FloorInfo> floorArr = new ArrayList<FloorInfo>();

            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG:

                        tag = parser.getName();
//						Log.i("tag", "START_TAG = "+tag);

                        if (tag != null && !tag.equals("")) {
                            if (tag.equalsIgnoreCase("title")) {
                                flag1 = true;
                            } else if (tag.equalsIgnoreCase("foldername")) {
                                flag2 = true;
                            } else if (tag.equalsIgnoreCase("worker")) {
                                flag3 = true;
                            } else if (tag.equalsIgnoreCase("workdate")) {
                                flag4 = true;
                            } else if (tag.equalsIgnoreCase("gatheringdate")) {
                                flag5 = true;
                            } else if (tag.equalsIgnoreCase("space")) {
                                flag6 = true;
                            } else if (tag.equalsIgnoreCase("address")) {
                                flag7 = true;
                            } else if (tag.equalsIgnoreCase("gid")) {
                                flag8 = true;
                            } else if (tag.equalsIgnoreCase("Geo_BasePoint_x")) {
                                flag9 = true;
                            } else if (tag.equalsIgnoreCase("Geo_BasePoint_y")) {
                                flag10 = true;
                            } else if (tag.equalsIgnoreCase("floor")) {
                                info = new FloorInfo();
                            } else if (tag.equalsIgnoreCase("name")) {
                                nameFlag = true;
                            } else if (tag.equalsIgnoreCase("wall")) {
                                wallFlag = true;
                            } else if (tag.equalsIgnoreCase("base")) {
                                baseFlag = true;
                            } else if (tag.equalsIgnoreCase("poi")) {
                                poiFlag = true;
                            } else if (tag.equalsIgnoreCase("path")) {
                                pathFlag = true;
                            } else if (tag.equalsIgnoreCase("lat")) {
                                latFlag = true;
                            } else if (tag.equalsIgnoreCase("lon")) {
                                lonFlag = true;
                            } else if (tag.equalsIgnoreCase("bearing")) {
                                bearFlag = true;
                            } else if (tag.equalsIgnoreCase("Acc_threshold")) {
                                Acc_threshold = true;
                            }
                        }

                        break;

                    case XmlPullParser.TEXT:

                        if (flag1) {
                            StaticManager.setTitle(parser.getText());
                        } else if (flag2) {
                            StaticManager.setFolderName(parser.getText());
                        } else if (flag3) {
                            StaticManager.setWorker(parser.getText());
                        } else if (flag4) {
                            StaticManager.setWorkdate(parser.getText());
                        } else if (flag5) {
                            StaticManager.setGatheringdate(parser.getText());
                        } else if (flag7) {
                            StaticManager.setAddress(parser.getText());
                        } else if (flag8) {
                            WataLog.i("chekc 7777777");
                            StaticManager.setGid(parser.getText());
                        } else if (flag9) {
                            StaticManager.setBasePointX(parser.getText());
                        } else if (flag10) {
                            StaticManager.setBasePointY(parser.getText());
                        } else if (Acc_threshold) {
                            StaticManager.Acc_threshold = parser.getText();
                        } else if (flag6) {
                            if (nameFlag) {
                                info.setName(parser.getText());
                            } else if (wallFlag) {
                                info.setWall(parser.getText());
                            } else if (baseFlag) {
                                info.setBase(parser.getText());
                            } else if (poiFlag) {
                                info.setPoi(parser.getText());
                            } else if (pathFlag) {
                                info.setPath(parser.getText());
                            } else if (latFlag) {
                                info.setLat(parser.getText());
                            } else if (lonFlag) {
                                info.setLon(parser.getText());
                            } else if (bearFlag) {
                                info.setBearing(parser.getText());
                            }
                        }

                        break;

                    case XmlPullParser.END_TAG:

                        tag = parser.getName();
//						Log.i("tag", "END_TAG = "+tag);

                        if (tag != null && !tag.equals("")) {
                            if (tag.equalsIgnoreCase("title")) {
                                flag1 = false;
                            } else if (tag.equalsIgnoreCase("foldername")) {
                                flag2 = false;
                            } else if (tag.equalsIgnoreCase("worker")) {
                                flag3 = false;
                            } else if (tag.equalsIgnoreCase("workdate")) {
                                flag4 = false;
                            } else if (tag.equalsIgnoreCase("gatheringdate")) {
                                flag5 = false;
                            } else if (tag.equalsIgnoreCase("address")) {
                                flag7 = false;
                            } else if (tag.equalsIgnoreCase("gid")) {
                                flag8 = false;
                            } else if (tag.equalsIgnoreCase("Geo_BasePoint_x")) {
                                flag9 = false;
                            } else if (tag.equalsIgnoreCase("Geo_BasePoint_y")) {
                                flag10 = false;
                            } else if (tag.equalsIgnoreCase("space")) {
                                StaticManager.setFloorInfo(floorArr);
                                flag6 = false;
                            } else if (tag.equalsIgnoreCase("floor")) {
                                floorArr.add(info);
                                info = null;
                            } else if (tag.equalsIgnoreCase("name")) {
                                nameFlag = false;
                            } else if (tag.equalsIgnoreCase("wall")) {
                                wallFlag = false;
                            } else if (tag.equalsIgnoreCase("base")) {
                                baseFlag = false;
                            } else if (tag.equalsIgnoreCase("poi")) {
                                poiFlag = false;
                            } else if (tag.equalsIgnoreCase("path")) {
                                pathFlag = false;
                            } else if (tag.equalsIgnoreCase("lat")) {
                                latFlag = false;
                            } else if (tag.equalsIgnoreCase("lon")) {
                                lonFlag = false;
                            } else if (tag.equalsIgnoreCase("bearing")) {
                                bearFlag = false;
                            } else if (tag.equalsIgnoreCase("Acc_threshold")) {
                                Acc_threshold = false;
                            }
                        }
                        break;
                }

                parserEvent = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
////        setDefaultLocation();
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//
//            @Override
//            public void onMapClick(LatLng latLng) {
//                WataLog.d("onMapClick :");
//                currentPosition = new LatLng(latLng.latitude, latLng.longitude);
//                setMarker(latLng);
//            }
//        });
//
//        startLocationUpdates();
//
//    }

//    private void setMarker(LatLng latLng) {
//        WataLog.d("latLng=" + latLng.latitude + "//" + latLng.longitude);
//
//        String markerTitle = getCurrentAddress(latLng);
//        String markerSnippet = "위도:" + String.valueOf(latLng.latitude) + " 경도:" + String.valueOf(latLng.longitude);
//        WataLog.d("markerTitle : " + markerTitle);
//        WataLog.d("onLocationResult : " + markerSnippet);
//        mPoiName = markerTitle;
//        address_textview.setText(markerTitle);
//        setCurrentLocation(latLng, markerTitle, markerSnippet);
////        mCurrentLocatiion = latLng;
//    }

    //    private Location mCurrentLocatiion;
//    private LatLng currentPosition;
//    LocationCallback locationCallback = new LocationCallback() {
//        @Override
//        public void onLocationResult(LocationResult locationResult) {
//            super.onLocationResult(locationResult);
//
//            List<Location> locationList = locationResult.getLocations();
//            if (locationList.size() > 0) {
//                location = locationList.get(locationList.size() - 1);
//                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
//                setMarker(currentPosition);
//            }
//        }
//    };

//    //지오코더... GPS를 주소로 변환
//    public String getCurrentAddress(LatLng latlng) {
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//        List<Address> addresses;
//        try {
//            addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1);
//        } catch (IOException ioException) {
//            //네트워크 문제
//            Toast.makeText(this, getString(R.string.gps_address_error_msg_1), Toast.LENGTH_LONG).show();
//            return  getString(R.string.gps_address_error_msg_1);
//        } catch (IllegalArgumentException illegalArgumentException) {
//            Toast.makeText(this, getString(R.string.gps_address_error_msg_2), Toast.LENGTH_LONG).show();
//            return getString(R.string.gps_address_error_msg_2);
//        }
//        if (addresses == null || addresses.size() == 0) {
//            Toast.makeText(this, getString(R.string.gps_address_error_msg_3), Toast.LENGTH_LONG).show();
//            return getString(R.string.gps_address_error_msg_3);
//        } else {
//            Address address = addresses.get(0);
//            String addressName = address.getAddressLine(0).substring(5);
//            WataLog.d("addressName=" + addressName);
//            return addressName;
//        }
//
//    }
//
//    private Marker currentMarker = null;
//
//    public void setCurrentLocation(LatLng latlng, String markerTitle, String markerSnippet) {
//        if (currentMarker != null) currentMarker.remove();
////        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latlng);
//        markerOptions.title(markerTitle);
//        markerOptions.snippet(markerSnippet);
//        markerOptions.draggable(true);
//
//        currentMarker = mMap.addMarker(markerOptions);
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latlng);
//        mMap.moveCamera(cameraUpdate);
//
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
//    }
//
//    private void startLocationUpdates() {
//        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
//
//        if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
//            WataLog.d("startLocationUpdates : 퍼미션 안가지고 있음");
//            return;
//        }
//        WataLog.d("startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");
//        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
//    }
//
//    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
//    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
//    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
//    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
//    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소
//    private static final int PERMISSIONS_REQUEST_CODE = 100;
//    boolean needRequest = false;
//
//    private GoogleMap mMap;
//    private RelativeLayout make_map_layout, kakao_map_layout;
//    private FusedLocationProviderClient mFusedLocationClient;
//    private LocationRequest locationRequest;
//    private Location location;
//
//    private void googleMapSetting() {
//        locationRequest = new LocationRequest()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
////                .setInterval(UPDATE_INTERVAL_MS)
////                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(locationRequest);
//
//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//    }
//
//
//    private void getHashKey() {
//        PackageInfo packageInfo = null;
//        try {
//            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        if (packageInfo == null) {
//            WataLog.i("해쉬키값 null");
//        }
//        for (Signature signature : packageInfo.signatures) {
//            try {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                WataLog.d("KeyHash ==" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            } catch (NoSuchAlgorithmException e) {
//                WataLog.e("Unable to get MessageDigest. signature=" + signature + e);
//            }
//        }
//    }

}
