package com.geotwo.LAB_TEST.Gathering.util;

import com.geotwo.LAB_TEST.Gathering.DataCore;

public class Constance {
	// 개발 true 운영 false
	public final static boolean LOG_STATE = true;

	public static String CONNECTING_IP = DataCore.IP_ADDRESS;
	public static String SOCKET_IP = DataCore.IP_ADDRESS;

	public static Boolean BEFORE = false;

//	public static String DEVE_SERVER = "http://13.209.43.191:3000";
public static String DEVE_SERVER = "http://54.180.223.80:3000";
	public static String DEVE_WEB_GO_SERVER = "http://54.180.223.80:8080/";

	public static String DEVE_JUDI_SERVER = "http://13.209.245.238:8081";

	public static boolean WIFI_INITIALIZE = false;

	public static int RESPONSE_SUCCESS = 200;

	public static final String KEY = "PATH_DRAWING_SETTING";
	public static final String SETTING_MAP_ROTATION = "SETTING_MAP_ROTATION";
	public static final String SETTING_G_SENSOR_ROTATION = "SETTING_G_SENSOR";
	public static final String SETTING_FREQUENCY = "SETTING_FREQUENCY";
	public static final String SETTING_STEP_LENGTH = "SETTING_STEP_LENGTH";

	public static final String CENTER_BTN = "center_btn";
	public static final String LEFT_BTN = "left_btn";
	public static final String RIGHT_BTN = "right_btn";

	public static final String START_RECORD = "start_record";
	public static final String STOP_RECORD = "stop_record";
	public static final String STEP_RECORD = "step_record";
	public static final String SEND_RECORD = "send_record";

	public static final int PICK_FROM_ALBUM = 1;

}
