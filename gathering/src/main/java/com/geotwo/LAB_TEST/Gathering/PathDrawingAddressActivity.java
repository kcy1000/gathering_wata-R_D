package com.geotwo.LAB_TEST.Gathering;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.Retrofit.Data;
import com.geotwo.LAB_TEST.Gathering.Retrofit.RetrofitExService;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.data.StaticManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.wata.LAB_TEST.Gathering.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PathDrawingAddressActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private TextView addressTextview, floorsLever;
    private String mFloorsLever = "1";
    private String mAddressInfo = "";
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationClient;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    private boolean testLat = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.path_drawing_address_activity);

        //지도생성 주소
        addressTextview = (TextView) findViewById(R.id.address_textview);

        RelativeLayout arrow_left_img = (RelativeLayout) findViewById(R.id.arrow_left_img);
        arrow_left_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 설정
        RelativeLayout make_map_btn = (RelativeLayout) findViewById(R.id.make_map_btn);
        make_map_btn.setOnClickListener(this);

        // 내위치
        RelativeLayout my_location = (RelativeLayout) findViewById(R.id.my_location);
        my_location.setOnClickListener(this);

        Button sinjugu = (Button)findViewById(R.id.sinjugu);
        sinjugu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WataLog.i("check!!!");
//                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
//                getCurrentAddress(currentPosition);
                testLat = true;
                mMyLatlng = new LatLng(Double.parseDouble("35.6895075"), Double.parseDouble("139.7001198"));
                setMapSetting("139.7001198", "35.6895075");
                setWebView("setMapCenterCoord", "139.7001198"+ "|" +"35.6895075");

                WataLog.d("mMyLatlng=" + mMyLatlng);

            }
        });

        floorsLever = (TextView) findViewById(R.id.floors_lever_picker);
        floorsLever.setText("1");


        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // SupportMapFragment을 통해 레이아웃에 만든 fragment의 ID를 참조하고 구글맵을 호출한다.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this); //getMapAsync must be called on the main thread.

//        String[] stringMin = new String[100];
//        for (int i = 0; i < stringMin.length; i++) {
//            stringMin[i] = Integer.toString(i - 9);
//        }

        //층수확인
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
//                mFloorsLever = newVal - 9;
//            }
//        });
    }

    private GoogleMap mMap;
    private LatLng currentPosition;
    private Location location;

    @Override //구글맵을 띄울준비가 됬으면 자동호출된다.
    public void onMapReady(GoogleMap googleMap) {
        WataLog.d("googleMap=" + googleMap.getCameraPosition());
        mMap = googleMap;
        //지도타입 - 일반
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        startLocationUpdates();
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0  &&  !testLat)  {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                // 신주쿠 Test 영상
//                testLat = true;
//                mMyLatlng = new LatLng(Double.parseDouble("35.6895075"), Double.parseDouble("139.7001198"));
//                setMapSetting("139.7001198", "35.6895075");
//                WataLog.d("mMyLatlng=" + mMyLatlng);
//                String address = getCurrentAddress(mMyLatlng);

                String address = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude()) + " 경도:" + String.valueOf(location.getLongitude());

                WataLog.d("onLocationResult : " + markerSnippet);
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

        mMap.setMyLocationEnabled(true);
//https://webnautes.tistory.com/1249   kcy1000 구글맵 예제
    }

    private LatLng mMyLatlng;

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            mMap.setMyLocationEnabled(true);
        }

        webViewSetting();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
    private WebView mAddressWebView;
    private String myUrl = Constance.DEVE_WEB_GO_SERVER + "watta_map/basemap.htm";
    private AddressWebBridge mAddressWebBridge;
    private int count = 0;

    private void webViewSetting() {
        // 웹뷰 셋팅팅
        mAddressWebView = (WebView) findViewById(R.id.address_webview);
        mAddressWebBridge = new AddressWebBridge(PathDrawingAddressActivity.this);
        mAddressWebView.getSettings().setJavaScriptEnabled(true);
        mAddressWebView.addJavascriptInterface(mAddressWebBridge, "WATA_GPS");

        mAddressWebView.setWebViewClient(new WebViewClientClass());
        mAddressWebView.setWebChromeClient(new WebChromeClient() {
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
        mAddressWebView.loadUrl(myUrl);
    }


    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            WataLog.d("url=" + url);
            view.loadUrl(url);
            return true;
        }
    }

    class AddressWebBridge {

        private PathDrawingAddressActivity pathDrawing;

        public AddressWebBridge(PathDrawingAddressActivity pathDrawingAddressActivity) {
            pathDrawing = pathDrawingAddressActivity;
        }

        @JavascriptInterface
        public void getCenterCoord(String address) {
            WataLog.d("address=" + address);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                }
            });
        }

        @JavascriptInterface
        public void setMyLocation(String tess) {
            WataLog.d("setMyLocation!");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setWebView("setMapCenterCoord", mMyLatlng.longitude + "|" + mMyLatlng.latitude);
                }
            });
        }

    }


//    private void setWebView(String webId) {
//        mAddressWebView.loadUrl("javascript:" + webId + "()");
//    }

    private void setWebView(String webId, String value) {
        mAddressWebView.loadUrl("javascript:" + webId + "('" + value + "')");
    }

    //    //지오코더... GPS를 주소로 변환
    public String getCurrentAddress(LatLng latlng) {

        String latitude = String.valueOf(latlng.latitude);
        String longitude = String.valueOf(latlng.longitude);

        setWebView("setMapCenterCoord", longitude + "|" + latitude);

        WataLog.d("longitude=" + longitude);
        WataLog.d("latitude=" + latitude);
//        35.6791005,139.7654938
        mMyLatlng = new LatLng(latlng.latitude, latlng.longitude);
        setMapSetting(longitude, latitude);
//        setMapSetting("35.6791005", "139.7654938");
        Geocoder geocoder = new Geocoder(this, Locale.US);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, getString(R.string.gps_address_error_msg_1), Toast.LENGTH_LONG).show();
            return getString(R.string.gps_address_error_msg_1);
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, getString(R.string.gps_address_error_msg_2), Toast.LENGTH_LONG).show();
            return getString(R.string.gps_address_error_msg_2);
        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, getString(R.string.gps_address_error_msg_3), Toast.LENGTH_LONG).show();
            return getString(R.string.gps_address_error_msg_3);
        } else {
            Address address = addresses.get(0);
            String addressName = address.getAddressLine(0).substring(5);
            addressName = addressName.replace("," , "");
//            Sampyeong-dong, Bundang-gu, Seongnam-si, Gyeonggi-do, South Korea

            WataLog.d("addressName=" + addressName);
            mAddressInfo = addressName;
            addressTextview.setText(addressName);
            return addressName;
        }

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.make_map_btn: { // 설정완료
//                mAddressInfo = "test";
                if (!"".equals(mAddressInfo)) {
//                    MapID = 3;
                    if (MapID > 0) {
                        StaticManager.setTitle("PathDrawing");
                        StaticManager.setFolderName("PathDrawing");
                        StaticManager.setAddress("PathDrawing");
                        StaticManager.setBasePointX("202351.273872");
                        StaticManager.setBasePointX("544170.137029");
                        StaticManager.setGid("1168010100108210001S01300");
                        StaticManager.setPosition(0);
                        StaticManager.setFloorName("AB01");

                        mFloorsLever = floorsLever.getText().toString();
                        WataLog.d("mMyLatlng=" + mMyLatlng);
                        Intent intent = new Intent(PathDrawingAddressActivity.this, PathDrawingActivity.class);
                        intent.putExtra("file_name", mAddressInfo + "_" + mFloorsLever + "floor");
                        intent.putExtra("map_id", String.valueOf(MapID));
                        intent.putExtra("longitude", mMyLatlng.longitude);
                        intent.putExtra("latitude", mMyLatlng.latitude);
                        startActivity(intent);
                    } else {
                        mFloorsLever = floorsLever.getText().toString();
                        Intent intent = new Intent(PathDrawingAddressActivity.this, SelectMapActivity.class);
                        WataLog.d("mPoiName=" + mAddressInfo + "_" + mFloorsLever + "floor");
                        intent.putExtra("file_name", mAddressInfo + "_" + mFloorsLever + "floor");
                        intent.putExtra("floors_lever", mFloorsLever + "floor");
                        intent.putExtra("map_id", String.valueOf(MapID));
                        intent.putExtra("longitude", mMyLatlng.longitude);
                        intent.putExtra("latitude", mMyLatlng.latitude);
                        startActivity(intent);
                    }

                } else {
                    Toast.makeText(PathDrawingAddressActivity.this, getString(R.string.none_address_message_1), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.my_location: {
                WataLog.i("my_location");
                testLat = false;
                startLocationUpdates();
                if (mMyLatlng != null) {
                    setMapSetting(String.valueOf(mMyLatlng.longitude ), String.valueOf(mMyLatlng.latitude));
                    setWebView("setMapCenterCoord", mMyLatlng.longitude + "|" + mMyLatlng.latitude);
                }
                break;
            }
        }
    }

    private int MapID = -1;

    private void setMapSetting(String coordx, String coordy) {
        WataLog.d("setMapSetting");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
//        WataLog.d("retrofitExService=" + retrofitExService);

        MapID = -1;
//        @35.6895075,139.7001198,
        retrofitExService.mapGpsSetting(coordx, coordy).enqueue(new Callback<List<Data>>() {
//        retrofitExService.mapGpsSetting("139.7001198", "35.6895075").enqueue(new Callback<List<Data>>() {
            @Override
            public void onResponse(@NonNull Call<List<Data>> call, @NonNull Response<List<Data>> response) {
                WataLog.d(response.isSuccessful() + "");
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        WataLog.i("response==" + response.body());
                        if (response.body().size() > 0) {
                            MapID = response.body().get(0).getId();
                            WataLog.i("MapID==" + MapID);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Data>> call, @NonNull Throwable t) {
              WataLog.d("test", "onFailure");
            }
        });

    }

//    private void setMapSetting2(String coordx, String coordy) {
////        http://13.209.43.191:3000/indoormap?coordx=15551308.0000&coordy=4257982.00
//        // 로그인 api 호출
//        SignService.getRetrofit(getApplicationContext()).mapGpsSetting("15551308.0000", "4257982.00").enqueue(new Callback<ResData>() {
//            @Override
//            public void onResponse(Call<ResData> call, Response<ResData> response) {
//                WataLog.d("call = " + call);
//                WataLog.d("response.body() = " + response.body());
//                if(response.body() != null) {
//                    if (response.body().res) {
//
////                    JSONArray jsonArray = new JSONArray(new Gson().toJson(response.body().data));
////                    WataLog.d( "첫번째 단어 유형의 idx : " + jsonArray.getJSONObject(0).getString("idx"));
////                    WataLog.d( "첫번쨰 단어 유형의 name : " + jsonArray.getJSONObject(0).getString("name"));
//
//                    } else {
//                        WataLog.d("로그인 실패");
//                        WataLog.d("메세지 : " + response.body().msg);
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResData> call, Throwable t) {
//                WataLog.d("서버 통신 실패");
//                WataLog.d("메세지 : " + t.getMessage());
//            }
//        });
//    }



//    private void onGPSListner() {
//        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(PathDrawingAddressActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
//
//        } else {
//            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            WataLog.d("location=" + location);
//
//            if (location != null) {
//                String provider = location.getProvider();
//                double longitude = location.getLongitude();
//                double latitude = location.getLatitude();
//                double altitude = location.getAltitude();
//
//                mMyLatlng = new LatLng(location.getLatitude(), location.getLongitude());
////                LatLng latlng = new LatLng(35.6791005, 139.7654938);
//                WataLog.d(mMyLatlng.longitude + "|" + mMyLatlng.latitude);
//                setWebView("setMapCenterCoord", mMyLatlng.longitude + "|" + mMyLatlng.latitude);
//
//                mAddressInfo = getCurrentAddress(mMyLatlng);
//                addressTextview.setText(mAddressInfo);
//
//                WataLog.d("위치정보 : " + provider + "\n" +
//                        "위도 : " + longitude + "\n" +
//                        "경도 : " + latitude + "\n" +
//                        "고도  : " + altitude);
//
//                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
//                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);
//
//            } else {
//
//            }
//        }
//    }
//
//    final LocationListener gpsLocationListener = new LocationListener() {
//        public void onLocationChanged(Location location) {
//
//            String provider = location.getProvider();
//            double longitude = location.getLongitude();
//            double latitude = location.getLatitude();
//            double altitude = location.getAltitude();
//
//            mMyLatlng = new LatLng(location.getLatitude(), location.getLongitude());
//            WataLog.d(mMyLatlng.longitude + "|" + mMyLatlng.latitude);
//            setWebView("setMapCenterCoord", mMyLatlng.longitude + "|" + mMyLatlng.latitude);
//
//            mAddressInfo = getCurrentAddress(mMyLatlng);
//            addressTextview.setText(mAddressInfo);
////            addressTextview.setText("Tōkyō-to, Shinjuku City, Shinjuku, 3-chōme−38−1");
//
//            WataLog.d("위치정보 : " + provider + "\n" +
//                    "위도 : " + longitude + "\n" +
//                    "경도 : " + latitude + "\n" +
//                    "고도  : " + altitude);
//
//        }
//
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//
//        public void onProviderEnabled(String provider) {
//        }
//
//        public void onProviderDisabled(String provider) {
//        }
//    };

}
