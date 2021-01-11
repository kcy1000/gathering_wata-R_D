package com.geotwo.LAB_TEST.Voucher;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.telephony.NeighboringCellInfo;
import android.util.Log;

import com.geotwo.LAB_TEST.Cell.CellManager;
import com.geotwo.LAB_TEST.Cell.wtCellInfo;
import com.geotwo.LAB_TEST.Gathering.DataCore;
import com.geotwo.LAB_TEST.Gathering.MainActivity;
import com.geotwo.LAB_TEST.Gathering.PathDrawingActivity;
import com.geotwo.LAB_TEST.Gathering.dto.Ble;
import com.geotwo.LAB_TEST.Gathering.dto.BuildInfo;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.common.Proj4;
import com.geotwo.common.SensorUtils;
import com.geotwo.o2mapmobile.geometry.Vector;

import org.proj4.CRSRegistry;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA_VOUCHER;
import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA_VOUCHER_PAYLOAD;
import ETRI.LBSP.Common.Interfaces.PDI_SCANINFO_A;
import ETRI.LBSP.Common.Interfaces.PDI_SCANINFO_B;
import ETRI.LBSP.Common.Interfaces.PDI_SCANINFO_C;
import ETRI.LBSP.Common.Interfaces.PDI_SCANINFO_D;
import geo2.lbsp.ible.BLEManager;
import geo2.lbsp.ible.Beacon;
import geo2.lbsp.ible.MacAddress;
import pdr_collecting.core.initialmm;
import pdr_collecting.core.pdralgorithm;
import pdr_collecting.core.pdrvariable;
import pdr_collecting.core.sensoract;
import timber.log.Timber;

//import geo2.lbsp.ble.BLEManager;


public class VoucherWifiScanner {
    boolean _isScanning = false;

    public WifiManager _wifiManager = null;
    private WifiRttManager mWifiRttManager;

    WifiManager.WifiLock wifiLock = null;
    Context _context = null;
    VoucherDataCore _dataCore = null;
    ConnectivityManager _connMgr = null;

    WifiScanListener _scanListener = null;
    WifiStatChangedListener _statChangeListner = null;

    public List<ScanResult> _scanResultList = null;

    IntentFilter _statChangedFilter = null;
    IntentFilter _scanableStatFilter = null;

    ArrayList<PDI_REPT_COL_DATA_VOUCHER> _SCAN_RESULT_TOTAL = null;
    PDI_REPT_COL_DATA_VOUCHER _SCAN_RESULT_THIS_EPOCH = null;

    Handler _UIHandler = null;

    long _previousGatherTime = 0;

    double pdr_mag_heading = 0;
    double pdr_gyro_heading = 0;
    double pdr_step_frequency = 0;
    double pdr_step_length = 0;

    DecimalFormat deciformat_H = new DecimalFormat("#.####");
    DecimalFormat deciformat_WF = new DecimalFormat("#.##");

    BLEManager _bleManager = null;
    CellManager _cellManager = null;

    final double rad2deg = 180 / Math.PI;

    private List<ScanResult> WifiRttAPs;
    private RttRangingResultCallback mRttRangingResultCallback;
    final Handler mRangeRequestDelayHandler = new Handler();
    private int mMillisecondsDelayBeforeNewRangingRequest = 0;
    private boolean bStop = true;
    private HashMap<String, ArrayList<RangingResult>> historicalDistances;

    private static final boolean isApi28 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;

    /**
     * @param appContext Application's Own Context
     * @param manager    Device's Wifi Manager
     * @param handler    VIew's UI Hander
     * @param list       Data Collection For Scanned Data
     */
    public VoucherWifiScanner(Context appContext, WifiManager manager, Handler handler, List<ScanResult> list, BLEManager bleManager) {
        _wifiManager = manager;
        _context = appContext;
        _scanResultList = list;
        _UIHandler = handler;
        _bleManager = bleManager;
        if( isApi28 ) {
            mWifiRttManager = (WifiRttManager) _context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
        }
        mRttRangingResultCallback = new RttRangingResultCallback();

        bleIgnoreList = new ArrayList();
        bleAddList = new ArrayList();

        if (_dataCore == null) {
            _dataCore = VoucherDataCore.getInstance();
        }
//		wifiLock = _wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "MyWifiLock");
//		if(!android.os.Build.BRAND.contains("LG") && !android.os.Build.BRAND.contains("lg") && _wifiManager.isWifiEnabled())
//			_wifiManager.setWifiEnabled(false);
        //ejcha 주파수 셋팅
        WataLog.d("주파수 밴드 ? " + MainActivity.iFreq);
        setFrequencyBand(MainActivity.iFreq);

        // If Device's Wifi Status Is On Unabled
        if ((Build.BRAND.contains("LG") || Build.BRAND.contains("lg")) && _wifiManager.isWifiEnabled() == false) {

            // If Wifi Status Changed Sucessfully, You Can Receive Result From
            // Network State Changed Receiver
            //_wifiManager.setWifiEnabled(true);
        }

        if (_connMgr == null) {
            _connMgr = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        if (_SCAN_RESULT_THIS_EPOCH == null) {
            _SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();
            WataLog.d("_dataCore.getSCAN_RESULT_THIS_EPOCH()=" + _dataCore.getSCAN_RESULT_THIS_EPOCH());
        }

        if (_SCAN_RESULT_TOTAL == null) {
            _SCAN_RESULT_TOTAL = _dataCore.getSCAN_RESULT_TOTAL();
        }

        // ---------------------------------------------------------------------------------------
        BuildInfo info = DataCore.getInstance().getBuildInfo(DataCore.getInstance().getBuildName());
        WataLog.d("info = " + info);
        String GIDex = info.getGIDex();//"02-6000-0114_CONVENT_COEX"; // COEX
        WataLog.d("GIDex = " + GIDex);

        try {
            _SCAN_RESULT_THIS_EPOCH.B2_GID_refP_Latitude = Integer.parseInt(info.getLatitude());
            _SCAN_RESULT_THIS_EPOCH.B3_GID_refP_Longitude = Integer.parseInt(info.getLongitude());
            _SCAN_RESULT_THIS_EPOCH.B4_GID_refP_Bearing = Integer.parseInt(info.getBearing());
            _SCAN_RESULT_THIS_EPOCH.B9_ColStartP_CVADDR(info.getAddr());

            WataLog.d("info.getLatitude() = " + info.getLatitude());
            WataLog.d("info.getLatitude() = " + info.getLongitude());

//            _SCAN_RESULT_THIS_EPOCH.B9_ColStartP_CVADDR("kcy1000");
            WataLog.d("info.getAddr() =" + info.getAddr());

        } catch (Exception e) {
            WataLog.d("Exception = " + e.toString());
            e.printStackTrace();
        }
        // ---------------------------------------------------------------------------------------

        System.arraycopy(GIDex.getBytes(), 0, _SCAN_RESULT_THIS_EPOCH.B1_GID, 0, PDI_REPT_COL_DATA_VOUCHER.B1_GID_LEN);

        // 기록라인 시작과 끝점
        _SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X = VoucherActivity.START_POINT_X;
        _SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y = VoucherActivity.START_POINT_Y;
        _SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X = VoucherActivity.END_POINT_X;
        _SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y = VoucherActivity.END_POINT_Y;

        // 의미없는것 같음.
        _SCAN_RESULT_THIS_EPOCH.B14_ColLinkID = VoucherActivity.NOW_POSITION + 1;

        WataLog.d("스캔 시작지점 " + _SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X + "/" + _SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y);
        WataLog.d("스캔 끝지점 " + _SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X + "/" + _SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y);

        if (!VoucherActivity.REVERSE_RECORDING) {
            _SCAN_RESULT_THIS_EPOCH.B15_ColLinkFlag = 'F';
        } else {
            _SCAN_RESULT_THIS_EPOCH.B15_ColLinkFlag = 'R';
            // 이미 gathering activity에서 변환됨
            // 주석처리
            // -->
//			double sx = _SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X;
//			double sy = _SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y;
//			_SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X = _SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X;
//			_SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y = _SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y;
//			_SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X = sx;
//			_SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y = sy;
        }

        // 방향정보
        _SCAN_RESULT_THIS_EPOCH.B16_ColLinkHeading = (float) VoucherActivity.lineHeader;

        WataLog.d("방향정보=" + _SCAN_RESULT_THIS_EPOCH.B16_ColLinkHeading);

        _SCAN_RESULT_THIS_EPOCH.B8_ColStartP_F(_dataCore.getFloorString());
        WataLog.d("_dataCore.getFloorString()=" + _dataCore.getFloorString());
        _SCAN_RESULT_THIS_EPOCH.B11_ColDevModel(Build.MODEL);
        _SCAN_RESULT_THIS_EPOCH.B17_ColOpt = PDI_REPT_COL_DATA_VOUCHER.DEF_ColOpt_BY_ColDevice;


        // 좌표변환은 수행
//		TransformGeo toGRS80 = new TransformGeo(6378137.0, 298.257223563, 1.0, 38.0, 127.0, 600000.0, 200000.0);
//		Vector tempGRS80 = toGRS80.transformTM2GP(GatheringMyWayActivity._startPoint.y, GatheringMyWayActivity._startPoint.x);

        // 좌표변환은 수행
        Proj4 proj4 = new Proj4(VoucherActivity.START_POINT_X, VoucherActivity.START_POINT_Y, CRSRegistry.EPSG_5186, CRSRegistry.EPSG_4326);
        Vector tempGRS80 = proj4.transformToVector();

        _SCAN_RESULT_THIS_EPOCH.B18_ColStartP_GRS_X = tempGRS80.y;
        _SCAN_RESULT_THIS_EPOCH.B19_ColStartP_GRS_Y = tempGRS80.x;

        _dataCore.setSCAN_RESULT_THIS_EPOCH(_SCAN_RESULT_THIS_EPOCH);

        _statChangedFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        _scanableStatFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        if (_scanListener == null) {
            _scanListener = new WifiScanListener();
        }

        if (_statChangeListner == null) {
            _statChangeListner = new WifiStatChangedListener();
        }

        _isScanning = true;
        // Regist Receiver For Network State Changed
        _context.registerReceiver(_scanListener, _scanableStatFilter);

        // Regist Receiver For Wifi Scan Changed To Able
        _context.registerReceiver(_statChangeListner, _statChangedFilter);
    }

    public void setCellManager(CellManager cellManager)
    {
        _cellManager = cellManager;
    }

    /**
     * @return Collection Of Scanned Data List
     */
    public List<ScanResult> getScanResult() {
        return _scanResultList;
    }

    public void stopScan() {
        _context.unregisterReceiver(_scanListener);
        _isScanning = false;
    }

    public void setWifiOff() {
        stopScan();
        _wifiManager.setWifiEnabled(false);
    }

    public void stopStatListener() {
        _context.unregisterReceiver(_statChangeListner);
    }

    public String getDateTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        return sdf.format(cal.getTime());
    }

    PDI_REPT_COL_DATA_VOUCHER_PAYLOAD epoch_scanData = null;

    // Scan Wifi && BLE : Ethan
    // Inner Class For Wifi Able To Get Wifi Scanned Data
    class WifiScanListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            //Log.d("WifiScanListener", "onReceive");

            Thread wifiDataThread = new Thread(new Runnable() {

                @SuppressLint("NewApi")
                @Override
                public void run() {
//                    try {
//                        WataLog.d("_SCAN_RESULT_THIS_EPOCH.B21_Payload.size()=" + _SCAN_RESULT_THIS_EPOCH.B21_Payload.size());
                    //Wifi 2회 수집전에 PDR 발생시 무효처리
                    if (_SCAN_RESULT_THIS_EPOCH.B21_Payload.size() < 2) {
                        Constance.WIFI_INITIALIZE = true;
                        pdrvariable.initValues();
//                        WataLog.i("CCCCCCCCCCCCCCCCCCCCCCCCC");
                        initialmm.MM_initialize3(VoucherActivity.POLY_LINE);
                    }

                    //PDI_REPT_COL_DATA_VOUCHER_PAYLOAD epoch_scanData = new PDI_REPT_COL_DATA_VOUCHER_PAYLOAD();
                    epoch_scanData = new PDI_REPT_COL_DATA_VOUCHER_PAYLOAD();

                    // Get Scanned Data List
                    // TM 상대좌표 10cm 단위 리턴
                    WataLog.d("_dataCore.getGatheringOption()=" + _dataCore.getGatheringOption());
//                    if (_dataCore.getGatheringOption() == DataCore.GATHER_OPTION_POINT) {
//                        epoch_scanData.FD2_ColP_X = pdrvariable.getInitial_pedestrian_x_coordinate();
//                        epoch_scanData.FD3_ColP_Y = pdrvariable.getInitial_pedestrian_y_coordinate();
//                    } else {
//                        epoch_scanData.FD2_ColP_X = pdrvariable.getPedestrian_x_coordinate();
//                        epoch_scanData.FD3_ColP_Y = pdrvariable.getPedestrian_y_coordinate();
//                    }

                    //Nan 초기화
//                    if (Double.isNaN(epoch_scanData.FD2_ColP_X)) {
//                        epoch_scanData.FD2_ColP_X = 0;
//                    }
//                    if (Double.isNaN(epoch_scanData.FD3_ColP_Y)) {
//                        epoch_scanData.FD3_ColP_Y = 0;
//                    }

                    if (epoch_scanData.FD2_ColP_X == 0 || epoch_scanData.FD2_ColP_X == 0.0) {
                        epoch_scanData.FD2_ColP_X = VoucherActivity.SCAN_POINT_X;
                        WataLog.d("epoch_scanData.FD2_ColP_X =" + epoch_scanData.FD2_ColP_X);
                    }
                    if (epoch_scanData.FD3_ColP_Y == 0 || epoch_scanData.FD3_ColP_Y == 0.0) {
                        epoch_scanData.FD3_ColP_Y = VoucherActivity.SCAN_POINT_Y;
                        WataLog.d("epoch_scanData.FD3_ColP_Y =" + epoch_scanData.FD3_ColP_Y);
                    }

                    WataLog.d("xxxxx=" + epoch_scanData.FD2_ColP_X + "/yyyyy=" + epoch_scanData.FD3_ColP_Y);

                    _scanResultList = _wifiManager.getScanResults();
                    List<Beacon> unMergedBeaconList = _bleManager.getScanResults();
                    List<Beacon> beaconList = getMergedList(unMergedBeaconList);
                    WataLog.d("_UIHandler=" + _UIHandler);
                    if (_UIHandler != null) {
                        Message msg = _UIHandler.obtainMessage();
                        msg.what = DataCore.AT_RECORD_SENSING;
                        double[] colP = new double[2];
                        if (epoch_scanData.FD2_ColP_X != 0.0) {
                            colP[0] = epoch_scanData.FD2_ColP_X;
                        } else {
                            colP[0] = VoucherActivity.SCAN_POINT_X;
                            WataLog.d("VoucherActivity.SCAN_POINT_X=" + VoucherActivity.SCAN_POINT_X);
                        }
                        if (epoch_scanData.FD3_ColP_Y != 0.0) {
                            colP[1] = epoch_scanData.FD3_ColP_Y;
                        } else {
                            colP[1] = VoucherActivity.SCAN_POINT_Y;
                            WataLog.d("VoucherActivity.SCAN_POINT_X=" + VoucherActivity.SCAN_POINT_Y);
                        }
                        msg.obj = colP;
                        _UIHandler.sendMessage(msg);
                    }

                    WataLog.d("_dataCore.getFloorString()=" + _dataCore.getFloorString());
                    epoch_scanData.FD8_ColP_F(_dataCore.getFloorString());

                    WataLog.d("getDateTime()=" + getDateTime());
                    epoch_scanData.FD6_ColTime = getDateTime();
                    pdr_gyro_heading = pdralgorithm.getGyro_integral();

                    pdr_step_frequency = pdralgorithm.get_step_frequency();
                    pdr_step_length = pdrvariable.getStep_length();
                    pdr_mag_heading = pdrvariable.getPedestrian_heading();

                    WataLog.d("sensoract.mag_field[0]=" + sensoract.mag_field[0]);
                    WataLog.d("sensoract.mag_field[1]=" + sensoract.mag_field[1]);
                    float FD9_ColMag_X = 0;
                    float FD10_ColMag_Y = 0;
                    if(sensoract.mag_field[0] < 99999 || sensoract.mag_field[0] < -9999){
                        FD9_ColMag_X = sensoract.mag_field[0];
                    } else {
                        FD9_ColMag_X = 0;
                    }

                    if(sensoract.mag_field[1] < 99999 || sensoract.mag_field[1] < -9999) {
                        FD10_ColMag_Y = sensoract.mag_field[1];
                    } else {
                        FD10_ColMag_Y = 0;
                    }
//                    float FD10_ColMag_Y = sensoract.mag_field[1];

                    float FD11_ColMag_Z = sensoract.mag_field[2];
                    float FD12_ColMag_Heading = (float) (sensoract.magvalue * rad2deg);
                    float FD13_ColMag_Baro = (float) sensoract.pressure;

                    WifiRttAPs = new ArrayList<>();

                    ArrayList SSID_LOG = new ArrayList();
                    ArrayList FREQ_LOG = new ArrayList();
                    for (int i = 0; i < _scanResultList.size(); i++) {

                        // Wi-Fi RTT
                        ScanResult scanResult = _scanResultList.get(i);
                        if (scanResult.is80211mcResponder() && WifiRttAPs.size() < RangingRequest.getMaxPeers()) {
                            WifiRttAPs.add(scanResult);
                            Timber.d("RTT : "+scanResult.toString());
                        }

                        //수정 추가
                        long age = (SystemClock.elapsedRealtimeNanos() / 1000) - _scanResultList.get(i).timestamp;
                        long now_sec = (age / 1000) / 1000;
                        long hour = now_sec / 3600;
                        long min = (now_sec % 3600) / 60;
                        long sec = now_sec % 60;

                        if (hour > 0 || min > 0 || sec > 1.0)
                            continue;

                        PDI_SCANINFO_A scanInfo = new PDI_SCANINFO_A();

                        String MACaddr = _scanResultList.get(i).BSSID.substring(0, 2) + _scanResultList.get(i).BSSID.substring(3, 5)
                                + _scanResultList.get(i).BSSID.substring(6, 8) + _scanResultList.get(i).BSSID.substring(9, 11)
                                + _scanResultList.get(i).BSSID.substring(12, 14) + _scanResultList.get(i).BSSID.substring(15, 17);

                        String SSID = _scanResultList.get(i).SSID;
                        int RSSI = _scanResultList.get(i).level;
                        int Freq = _scanResultList.get(i).frequency;

//                        WataLog.d("_scanResultList.get(i).venueName = "  +_scanResultList.get(i).venueName.toString());
//                        WataLog.d("_scanResultList.get(i).SSID = "  +_scanResultList.get(i).SSID);
//                        WataLog.d("_scanResultList.get(i).operatorFriendlyName = "  +_scanResultList.get(i).operatorFriendlyName);
//                        WataLog.d("_scanResultList.get(i).BSSID = "  +_scanResultList.get(i).BSSID);
//                        WataLog.d(" (short) Freq = "  + (short) Freq);

                        SSID_LOG.add(_scanResultList.get(i).SSID);
                        FREQ_LOG.add((short)Freq);

                        scanInfo.FD1_INFRA_TYPE = PDI_SCANINFO_A.DEF_INFRA_TYPE_WiFi;
                        scanInfo.FD2_INFRA_ID = MACaddr;
                        scanInfo.FD3_RSSI = (byte) RSSI;
                        scanInfo.FD4_FREQ = (short) Freq;
                        scanInfo.FD5_ENCRYPTION = 0;

//                        WataLog.d("(short) SSID.length()=" + (short) SSID.length());
                        scanInfo.FD6_SSID_LEN = (short) SSID.length();
//                        WataLog.d("SSID=" + SSID);
                        scanInfo.FD7_SSID = SSID;

                        scanInfo.FDx_STEP_LEN = (int) Double.parseDouble(deciformat_WF.format(pdr_step_length));
                        //scanInfo.FDx_STEP_LEN = 1;
                        scanInfo.FDx_STEP_FREQ = Double.parseDouble(deciformat_WF.format(pdr_step_frequency));
                        scanInfo.FDx_GYRO = Double.parseDouble(deciformat_H.format(pdr_gyro_heading));
                        scanInfo.FDx_MAGNETO = Double.parseDouble(deciformat_H.format(pdr_mag_heading));

                        //Log.d("etri.lbsp.cdps.tester", "wifiManager.startScan() again " +scanInfo.FDx_STEP_FREQ+ " "+ scanInfo.FDx_GYRO+ " "+scanInfo.FDx_MAGNETO);

                        epoch_scanData.FD8_SCANINFO_A_List.add(scanInfo);
                        // Log.e("CDPS_Tester","Wifi Data Scanned");
                    }

                    //  Start ranging
                    if ( WifiRttAPs.size() > 0 ){
                        startRangingRequest(WifiRttAPs);
                    }

                    VoucherActivity.setWifi5G(SSID_LOG, FREQ_LOG);

                    for (Beacon beacon : beaconList) {
                        PDI_SCANINFO_B scanInfo = new PDI_SCANINFO_B();
                        scanInfo.FD1_INFRA_TYPE = PDI_SCANINFO_A.DEF_INFRA_TYPE_Bluetooth;
                        scanInfo.FD2_INFRA_ID = beacon.getMacAddress().toStandardString().replace(":", "");
                        scanInfo.FD3_RSSI = (byte) beacon.getRssi();
                        scanInfo.FD4_FREQ = (short) beacon.getMeasuredPower();
                        scanInfo.FD5_ENCRYPTION = 0;
                        scanInfo.FD6_SSID_LEN = (short) beacon.getName().length();
                        scanInfo.FD7_SSID = beacon.getName();

                        UUID uid = beacon.getProximityUUID();
                        scanInfo.FD8_UUID = beacon.getProximityUUID().toString();
                        scanInfo.FD8_UUID = scanInfo.FD8_UUID.replace("-", "");
                        scanInfo.FD9_Major = (short) beacon.getMajor();
                        scanInfo.FD10_Minior = (short) beacon.getMinor();
                        scanInfo.FD11_TXPow = (byte) beacon.getMeasuredPower();

                        //PDR information
                        scanInfo.FDx_STEP_LEN = (int) Double.parseDouble(deciformat_WF.format(pdr_step_length));
                        scanInfo.FDx_STEP_FREQ = Double.parseDouble(deciformat_WF.format(pdr_step_frequency));
                        scanInfo.FDx_GYRO = Double.parseDouble(deciformat_H.format(pdr_gyro_heading));
                        scanInfo.FDx_MAGNETO = Double.parseDouble(deciformat_H.format(pdr_mag_heading));

                        epoch_scanData.FD8_SCANINFO_A_List.add(scanInfo);
                    }

                    // Cellular Scan by Ethan
                    /*
                    int asu = signal.getAsuLevel();
                    if (asu == NeighboringCellInfo.UNKNOWN_RSSI)
                        asu = wtCellInfo.UNKNOWN_SIGNAL;
                    int dbm = signal.getDbm();
                    int ta = signal.getTimingAdvance();
                    int rsrp = isApi26 ? signal.getRsrp() : wtCellInfo.UNKNOWN_SIGNAL;
                    int rsrq = isApi26 ? signal.getRsrq() : wtCellInfo.UNKNOWN_SIGNAL;
                    int rssi = isApi29 ? signal.getRssi() : wtCellInfo.UNKNOWN_SIGNAL;
                    int rssnr = isApi26 ? signal.getRssnr() : wtCellInfo.UNKNOWN_SIGNAL;
                    int cqi = isApi26 ? signal.getCqi() : wtCellInfo.UNKNOWN_SIGNAL;
                    */
                    ArrayList<wtCellInfo> _listCell = _cellManager.scanCellInfo();
                    for ( wtCellInfo _cell : _listCell )
                    {
                        PDI_SCANINFO_C scanInfo = new PDI_SCANINFO_C();

                        scanInfo.FD1_INFRA_TYPE = PDI_SCANINFO_A.DEF_INFRA_TYPE_Cell;
                        scanInfo.FD2_INFRA_ID =  String.valueOf(_cell.getPsc());
                        scanInfo.FD3_RSSI = (byte) _cell.getRsrp();
                        scanInfo.FD4_FREQ = (short) _cell.getAsu();
                        scanInfo.FD5_ENCRYPTION = 0;
                        scanInfo.FD6_SSID_LEN = (short) String.valueOf(_cell.getPsc()).length();
                        scanInfo.FD7_SSID = String.valueOf(_cell.getPsc());

                        //PDR information
                        scanInfo.FDx_STEP_LEN = (int) Double.parseDouble(deciformat_WF.format(pdr_step_length));
                        scanInfo.FDx_STEP_FREQ = Double.parseDouble(deciformat_WF.format(pdr_step_frequency));
                        scanInfo.FDx_GYRO = Double.parseDouble(deciformat_H.format(pdr_gyro_heading));
                        scanInfo.FDx_MAGNETO = Double.parseDouble(deciformat_H.format(pdr_mag_heading));

                        scanInfo.cid = _cell.getCid();
                        scanInfo.rsrp = _cell.getRsrp();
                        scanInfo.psc = _cell.getPsc();

                        epoch_scanData.FD8_SCANINFO_A_List.add(scanInfo);
                    }


                    epoch_scanData.FD7_SCANINFO_A_CNT = (short) epoch_scanData.FD8_SCANINFO_A_List.size();    //수정 추가
                    epoch_scanData.FD9_ColMag_X = FD9_ColMag_X;
                    epoch_scanData.FD10_ColMag_Y = FD10_ColMag_Y;
                    epoch_scanData.FD11_ColMag_Z = FD11_ColMag_Z;
                    epoch_scanData.FD12_ColMag_Heading = FD12_ColMag_Heading;
                    epoch_scanData.FD13_ColMag_Baro = FD13_ColMag_Baro;

                    // 좌표변환은 수행
//                    Proj4 proj4 = new Proj4(epoch_scanData.FD2_ColP_X, epoch_scanData.FD3_ColP_Y, CRSRegistry.EPSG_5186, CRSRegistry.EPSG_4326);
//                    Vector tempGRS80 = proj4.transformToVector();

//						TransformGeo toGRS80 = new TransformGeo(6378137.0, 298.257223563, 1.0, 38.0, 127.0, 600000.0, 200000.0);
//						Vector tempGRS80 = toGRS80.transformTM2GP(epoch_scanData.FD3_ColP_Y, epoch_scanData.FD2_ColP_X);

                    epoch_scanData.FD14_GRS_X = 0.0;
                    epoch_scanData.FD15_GRS_Y = 0.0;

//						epoch_scanData.FD16_TM_X = epoch_scanData.FD2_ColP_X;
//						epoch_scanData.FD17_TM_Y = epoch_scanData.FD3_ColP_Y;

                    //kcy1000 - 확인안됨
                    // set relative coordinate.
//                        epoch_scanData.FD2_ColP_X = (epoch_scanData.FD2_ColP_X - VoucherActivity.basePointX) * 10;
//                        epoch_scanData.FD3_ColP_Y = (epoch_scanData.FD3_ColP_Y - VoucherActivity.basePointY) * 10;

                    /**
                     * 조영수박사님 로그 추가 건
                     */
                    float[] rotationVectorOrientations = SensorUtils.getOrientation(sensoract.rotationVectors);
                    epoch_scanData.FD16_Sensor_Accelerometer_X = sensoract.accvalue[0];
                    epoch_scanData.FD17_Sensor_Accelerometer_Y = sensoract.accvalue[1];
                    epoch_scanData.FD18_Sensor_Accelerometer_Z = sensoract.accvalue[2];
                    epoch_scanData.FD19_Sensor_Gravity_X = sensoract.gravityFields[0];
                    epoch_scanData.FD20_Sensor_Gravity_Y = sensoract.gravityFields[1];
                    epoch_scanData.FD21_Sensor_Gravity_Z = sensoract.gravityFields[2];
                    if (rotationVectorOrientations != null) {
                        epoch_scanData.FD27_Sensor_RotationVector_Yaw = rotationVectorOrientations[0];
                        epoch_scanData.FD26_Sensor_RotationVector_Pitch = rotationVectorOrientations[1];
                        epoch_scanData.FD25_Sensor_RotationVector_Roll = rotationVectorOrientations[2];
                    } else {
                        epoch_scanData.FD27_Sensor_RotationVector_Yaw = 0f;
                        epoch_scanData.FD26_Sensor_RotationVector_Pitch = 0f;
                        epoch_scanData.FD25_Sensor_RotationVector_Roll = 0f;
                    }
//						Log.e("test", "X : "+epoch_scanData.sensorRotationVectorYaw);
//						Log.e("test", "Y : "+epoch_scanData.sensorRotationVectorPitch);
//						Log.e("test", "Z : "+epoch_scanData.sensorRotationVectorRoll);
//						Log.e("test", "===========");

                    //수정 추가
                    if (epoch_scanData.FD7_SCANINFO_A_CNT > 0) {
                        _SCAN_RESULT_THIS_EPOCH.B21_Payload.add(epoch_scanData);
                    }
                    _dataCore.setSCAN_RESULT_TOTAL(_SCAN_RESULT_TOTAL);

                    // TODO : When Button Click Save this Data : by Ethan
                    _SCAN_RESULT_TOTAL.add(_SCAN_RESULT_THIS_EPOCH);

                    // Send Message To UI For Notice Scanned Data Changed
                    if (_UIHandler != null) {
//                        WataLog.d("_UIHandler===" + _UIHandler);
                        _UIHandler.sendEmptyMessage(DataCore.REFREASH_WIFI_LIST);
                    }

                    // Forced Garbage Collecting
                    System.gc();
//                    WataLog.d("startScan");

                    _wifiManager.startScan();            // onReceive : by Ethan
                    _bleManager.startScan();
//                    } catch (Exception e){
//                        WataLog.e("Exception=" + e.toString());
//                        return;
//                    }
                }
            });
            wifiDataThread.start();
        }


        /**
         * BLE 검색 설정에 하나 이상의 항목이 존재 하면 해당 항목에 일치하는 항목만 가져오고
         * 그렇지 않은 경우 BLE 무시 설정에 추가된 리스트 항목에 일치하는 항목을 제거하고 가져온다
         *
         * @param beaconList
         * @return 재정렬된 비콘 리스트
         */
        private List<Beacon> getMergedList(List<Beacon> beaconList) {
            List<Beacon> out = null;
            if (bleAddList != null && bleAddList.size() > 0) {
                out = getBleAddedList(beaconList);
            } else if (bleIgnoreList != null && bleIgnoreList.size() > 0) {
                out = getBleIgnoredList(beaconList);
            } else {
                out = beaconList;
            }

            return out;
        }

        /**
         * BLE 검색 설정에 추가된 리스트 항목에 일치하는 항목만 가져온다
         *
         * @param beconList
         * @return 재정렬된 비콘 리스트
         */
        private List<Beacon> getBleAddedList(List<Beacon> beconList) {
            if (bleAddList == null) return beconList;

            List<Beacon> out = new ArrayList();
            for (Beacon beacon : beconList) {
                MacAddress mac = beacon.getMacAddress();
                UUID uuid = beacon.getProximityUUID();
                int major = beacon.getMajor();
                int minor = beacon.getMinor();

                Ble targetBleAdd = new Ble();
                targetBleAdd.mac = mac.toStandardString();
                targetBleAdd.uuid = uuid.toString();
                targetBleAdd.major = String.format("%d", major);
                targetBleAdd.minor = String.format("%d", minor);
                //check is ignore ble
                int bleAddListSize = bleAddList.size();
                for (int i = 0; i < bleAddListSize; i++) {
                    Ble bleAdd = bleAddList.get(i);
                    ArrayList<Boolean> conditions = new ArrayList();

                    if (bleAdd.mac.length() > 0) {
                        conditions.add(bleAdd.mac.equals(targetBleAdd.mac));
                    }
                    if (bleAdd.uuid.length() > 0) {
                        conditions.add(bleAdd.uuid.equals(targetBleAdd.uuid));
                    }
                    if (bleAdd.major.length() > 0) {
                        conditions.add(bleAdd.major.equals(targetBleAdd.major));
                    }
                    if (bleAdd.minor.length() > 0) {
                        conditions.add(bleAdd.minor.equals(targetBleAdd.minor));
                    }

                    boolean flag = false;
                    if (conditions.size() > 0) {
                        flag = conditions.get(0);
                    }
                    for (boolean condition : conditions) {
                        flag &= condition;
                    }

                    if (flag) {
                        if (out.indexOf(beacon) < 0) {
                            out.add(beacon);
                        }
                    }
                }
            }

            return out;
        }

        /**
         * BLE 무시 설정에 추가된 리스트 항목에 일치하는 항목을 제거하고 가져온다
         *
         * @param beconList
         * @return 재정렬된 비콘 리스트
         */
        private List<Beacon> getBleIgnoredList(List<Beacon> beconList) {
            if (bleIgnoreList == null) return beconList;

            List<Beacon> out = new ArrayList();
            for (Beacon beacon : beconList) {
                MacAddress mac = beacon.getMacAddress();
                UUID uuid = beacon.getProximityUUID();
                int major = beacon.getMajor();
                int minor = beacon.getMinor();

                Ble targetBleIgnore = new Ble();
                targetBleIgnore.mac = mac.toStandardString();
                targetBleIgnore.uuid = uuid.toString();
                targetBleIgnore.major = String.format("%d", major);
                targetBleIgnore.minor = String.format("%d", minor);
                //check is ignore ble
                int bleIgnoreListSize = bleIgnoreList.size();
                for (int i = 0; i < bleIgnoreListSize; i++) {
                    Ble bleIgnore = bleIgnoreList.get(i);
                    ArrayList<Boolean> conditions = new ArrayList();

                    if (bleIgnore.mac.length() > 0) {
                        conditions.add(bleIgnore.mac.equals(targetBleIgnore.mac));
                    }
                    if (bleIgnore.uuid.length() > 0) {
                        conditions.add(bleIgnore.uuid.equals(targetBleIgnore.uuid));
                    }
                    if (bleIgnore.major.length() > 0) {
                        conditions.add(bleIgnore.major.equals(targetBleIgnore.major));
                    }
                    if (bleIgnore.minor.length() > 0) {
                        conditions.add(bleIgnore.minor.equals(targetBleIgnore.minor));
                    }

                    boolean flag = false;
                    if (conditions.size() > 0) {
                        flag = conditions.get(0);
                    }

                    for (boolean condition : conditions) {
                        flag &= condition;
                    }

                    if (!flag) {
                        if (out.indexOf(beacon) < 0) {
                            out.add(beacon);
                        }
                    }
                }
            }

            return out;
        }

        private void sleepForMachingPeriod(long currentTime, int getheringPeriod) {
            try {
                long sleepTime = _previousGatherTime + (getheringPeriod * 1000) - currentTime;
                Log.e("sleepForMachingPeriod", "sleep time is " + sleepTime);
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                WataLog.d(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Inner Class For Device's Wifi State Is Changed
    class WifiStatChangedListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // If Can't Scan Wifi Data
			/*if (_wifiManager.isWifiEnabled() == false &&  _isScanning == true)
			{
				// Try Again To Wifi State Chage
//				_wifiManager.setWifiEnabled(true);
			}
			else */
            if (/*_wifiManager.isWifiEnabled() == true && */ _isScanning == true) {
                // Start Wifi Scan
                _previousGatherTime = System.currentTimeMillis();
                _wifiManager.startScan();            // onReceive - Changed : by Ethan
                _bleManager.startScan();
            } else if (/*_wifiManager.isWifiEnabled() == false && */ _isScanning == false) {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        while (true) {
                            NetworkInfo netWorkInfo = _connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                            if (netWorkInfo.isConnected()) {
                                break;
                            }
                        }
                        if (_UIHandler != null)
                            _UIHandler.sendEmptyMessage(DataCore.NETWORK_READY);
                    }
                });

                // thread.start();
            }

        }
    }

    @SuppressLint("MissingPermission")
    private void startRangingRequest(List<ScanResult> scanResults) {
        if( isApi28 ) {
            RangingRequest rangingRequest = new RangingRequest.Builder().addAccessPoints(scanResults).build();

            mWifiRttManager.startRanging(rangingRequest, _context.getMainExecutor(), mRttRangingResultCallback);
        }
    }

    // Class that handles callbacks for all RangingRequests and issues new RangingRequests.
    private class RttRangingResultCallback extends RangingResultCallback {

        private void queueNextRangingRequest() {
            mRangeRequestDelayHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            startRangingRequest(WifiRttAPs);
                        }
                    },
                    mMillisecondsDelayBeforeNewRangingRequest);
        }

        @Override
        public void onRangingFailure(int code) {
            Timber.d("onRangingFailure() code: " + code);
            queueNextRangingRequest();
        }

        @Override
        public void onRangingResults(@NonNull List<RangingResult> rangingResultsList) {
            Timber.d("onRangingResults(): " + rangingResultsList);

            //  Ensure we have more APs in the list of ranging results than were present in the configuration
            if (rangingResultsList.size() > 0) {

                //  Sort the received ranging results by MAC address
                //  (order needs to match the order in the config, previously sorted by MAC address)
/*
                Collections.sort(rangingResultsList, new Comparator<RangingResult>() {
                    @Override
                    public int compare(RangingResult o1, RangingResult o2) {
                        return o1.getMacAddress().toString().compareTo(o2.getMacAddress().toString());
                    }
                });
*/
                //  Check that the received ranging results are valid and appropriate
                List<RangingResult> rangingResultsOfInterest = new ArrayList<>();
                rangingResultsOfInterest.clear();

                for (int i = 0; i < rangingResultsList.size(); i++)
                {
                    RangingResult rangingResult = rangingResultsList.get(i);

                    if (rangingResult.getStatus() == RangingResult.STATUS_SUCCESS) {
                        rangingResultsOfInterest.add(rangingResult);
                    } else if (rangingResult.getStatus() == RangingResult.STATUS_RESPONDER_DOES_NOT_SUPPORT_IEEE80211MC) {
                        Timber.d("RangingResult failed (AP doesn't support IEEE80211 MC.");
                    } else {
                        Timber.d("RangingResult failed. (" + rangingResult.getMacAddress().toString() + ")");
                    }

                }
                //  rangingResultsOfInterest now contains the list of APs from whom we have received valid ranging results

                //  Check that every AP in our configuration returned a valid ranging result
                //  Potential enhancement: could remove any APs from the building map that we couldn't range to (need at least 2)
                /*if (rangingResultsOfInterest.size() != 0)
                {
                    Timber.d("Could not find all the APs defined in the configuration to range off of");
                    if (!bStop)
                        queueNextRangingRequest();
                    return;
                }
                */

                //for (int i = 0; i < rangingResultsOfInterest.size(); i++)
                for(RangingResult _result : rangingResultsOfInterest)
                {
                    //ArrayList temp = historicalDistances.get(rangingResultsOfInterest.get(i).getMacAddress().toString());
                    //temp.add(rangingResultsOfInterest.get(i));

                    //Timber.d("Distance to " + rangingResultsOfInterest.get(i).getMacAddress().toString() +
                    //        " [Ave]: " + (int)weighted_average(historicalDistances.get(rangingResultsOfInterest.get(i).getMacAddress().toString())) + "mm");
                    String _mac_address = _result.getMacAddress().toString();
                    Timber.d("RTT Distance to " + _mac_address +
                            " : " + _result.getDistanceMm() + "mm");

                    String MACaddr = _mac_address.substring(0, 2) + _mac_address.substring(3, 5)
                            + _mac_address.substring(6, 8) + _mac_address.substring(9, 11)
                            + _mac_address.substring(12, 14) + _mac_address.substring(15, 17);

                    //String SSID = _scanResultList.get(i).SSID;
                    int RSSI = _result.getRssi();
                    //int Freq = _result._scanResultList.get(i).frequency;

                    PDI_SCANINFO_D scanInfo = new PDI_SCANINFO_D();

                    scanInfo.FD1_INFRA_TYPE = PDI_SCANINFO_A.DEF_INFRA_TYPE_WiFi_RTT;
                    scanInfo.FD2_INFRA_ID = MACaddr;
                    scanInfo.FD3_RSSI = (byte) RSSI;
                    scanInfo.FD4_FREQ = (short) 0;
                    scanInfo.FD5_ENCRYPTION = 0;
                    scanInfo.FD6_SSID_LEN = (short) 0;

                    scanInfo.FDx_STEP_LEN = (int) Double.parseDouble(deciformat_WF.format(pdr_step_length));
                    //scanInfo.FDx_STEP_LEN = 1;
                    scanInfo.FDx_STEP_FREQ = Double.parseDouble(deciformat_WF.format(pdr_step_frequency));
                    scanInfo.FDx_GYRO = Double.parseDouble(deciformat_H.format(pdr_gyro_heading));
                    scanInfo.FDx_MAGNETO = Double.parseDouble(deciformat_H.format(pdr_mag_heading));

                    scanInfo.Distance = _result.getDistanceMm();
                    //Log.d("etri.lbsp.cdps.tester", "wifiManager.startScan() again " +scanInfo.FDx_STEP_FREQ+ " "+ scanInfo.FDx_GYRO+ " "+scanInfo.FDx_MAGNETO);

                    epoch_scanData.FD8_SCANINFO_A_List.add(scanInfo);
                }


                //  historicalDistances now contains an arraylist of historic distances for each AP
                //  because of an earlier check, we know that every AP in the building map has an associated
                //  entry in the history of observed ranging results
                //  Create the positions and distances arrays required by the multilateration algorithm

            }
            else
            {
                Timber.d("Could not find enough Ranging Results");
            }
            //if (!bStop)
            //    queueNextRangingRequest();
        }
    }

    private double weighted_average(List<RangingResult> rangingResults) {
        //  https://en.wikipedia.org/wiki/Weighted_arithmetic_mean#Variance_weights
        double weighted_numerator = 0.0;
        double weighted_demoninator = 0.0;
        for (int i = 0; i < rangingResults.size(); i++)
        {
            weighted_numerator += (rangingResults.get(i).getDistanceMm() * (1.0 / (rangingResults.get(i).getDistanceStdDevMm() ^ 2)));
            weighted_demoninator += (1.0 / (rangingResults.get(i).getDistanceStdDevMm() ^ 2));
        }
        return weighted_numerator / weighted_demoninator;
    }


    public boolean setFrequencyBand(int band)       // band �� �ǹ�: 0: �ڵ�, 1: 5 Ghz, 2: 2.4Ghz
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) // Version 14
        {
            return false;
        }

        boolean bRet = true;

        Method[] methods = _wifiManager.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("setFrequencyBand")) {
                try {
                    method.invoke(_wifiManager, band, false);
                    WataLog.d("set freq is succeed with " + band);
                } catch (Exception e) {
                    e.printStackTrace();
                    bRet = false;
                    WataLog.d("set freq is failed");
                }
                break;
            }
        }
        return bRet;
    }

    private ArrayList<Ble> bleIgnoreList = null;

    public void setBleIgnoreList(ArrayList<Ble> bleIgnoreList) {
        this.bleIgnoreList = bleIgnoreList;
    }

    private ArrayList<Ble> bleAddList = null;

    public void setBleAddList(ArrayList<Ble> bleAddList) {
        this.bleAddList = bleAddList;
    }
}
