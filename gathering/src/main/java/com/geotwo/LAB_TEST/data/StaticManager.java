package com.geotwo.LAB_TEST.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

public class StaticManager 
{
	public static String title;
	public static String folderName;
	public static String folderLevel;
	public static String address;
	public static String gid;
	public static String worker;
	public static String workdate;
	public static String gatheringdate;
	public static ArrayList<FloorInfo> floorInfo;
	public static int position;
	public static String floorName;
	public static String pathNum;
	public static String pathDrawingName;
	public static String basePointX;
	public static String basePointY;
	public static String Acc_threshold;

	public static String subwayName;
//	public final static String SAVE_PATH = "/sdcard/Download/gatheringaps/save/";
	
	public static void setTitle(String title) {
		WataLog.d("title=" + title);
		StaticManager.title = title;
	}
	public static void setFolderName(String folderName) {
		WataLog.d("folderName=" + folderName);
		StaticManager.folderName = folderName;
	}
	public static void setWorker(String worker) {
		WataLog.d("worker=" + worker);
		StaticManager.worker = worker;
	}
	public static void setWorkdate(String workdate) {
		WataLog.d("workdate=" + workdate);
		StaticManager.workdate = workdate;
	}
	public static void setGatheringdate(String gatheringdate) {
		WataLog.d("gatheringdate=" + gatheringdate);
		StaticManager.gatheringdate = gatheringdate;
	}
	public static void setFloorInfo(ArrayList<FloorInfo> floorInfo) {
		WataLog.d("floorInfo=" + floorInfo);
		StaticManager.floorInfo = floorInfo;
	}
	
	public static void setEmptyAll()
	{
//		StaticManager.basePointX = "";
//		StaticManager.basePointY = "";
//		StaticManager.address = "";
//		StaticManager.folderName = "";
//		StaticManager.gid = "";
//		StaticManager.floorName = "";
//		StaticManager.title = "";
//		StaticManager.worker = "";
		StaticManager.workdate = "";
		StaticManager.gatheringdate = "";
		StaticManager.floorInfo = null;
	}
	
	public static String getTime()
    {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
		Date date = new Date();
		String str = format.format(date);
		
		return str;
	}
	public static void setPosition(int position) {
		WataLog.d("position=" + position);
		StaticManager.position = position;
	}
	public static void setFloorName(String floorName) {
		WataLog.d("floorName=" + floorName);
		StaticManager.floorName = floorName;
	}
	public static void setPathNum(String pathNum) {
		WataLog.d("pathNum=" + pathNum);
		StaticManager.pathNum = pathNum;
	}
	public static void setAddress(String address) {
		WataLog.d("address=" + address);
		StaticManager.address = address;
	}

	public static void setFloorLevel(String floorLevel) {
		WataLog.d("floorLevel=" + floorLevel);
		StaticManager.folderLevel = floorLevel;
	}

	public static void setGid(String gid) {
		WataLog.d("gid=" + gid);
		StaticManager.gid = gid;
	}
	public static void setBasePointX(String basePointX) {
		WataLog.d("setBasePointX=" + basePointX);
		StaticManager.basePointX = basePointX;
	}
	public static void setBasePointY(String basePointY) {
		WataLog.d("setBasePointY=" + basePointY);
		StaticManager.basePointY = basePointY;
	}
	public static String getResultPath() {
		WataLog.d("getResultPath");
		return Environment.getExternalStorageDirectory().getPath() +"/gathering/saveData/pathGathering/"
				+StaticManager.folderName+"/"+StaticManager.floorName+"/"+StaticManager.pathNum+"/";
	}

	public static void setPathDrawingName(String pathDrawingName) {
		WataLog.d("pathDrawingName=" + pathDrawingName);
		StaticManager.pathDrawingName = pathDrawingName;
	}

	public static void setSubwayName(String subwayName) {
		WataLog.d("pathDrawingName=" + subwayName);
		StaticManager.subwayName = subwayName;
	}

	public static String getResultPathDrawingPath() {
		WataLog.d("getResultPath");
		return Environment.getExternalStorageDirectory().getPath() +"/gathering/saveData/pathGathering/"
				+StaticManager.folderName+"/"+StaticManager.pathDrawingName+"/" + StaticManager.pathNum+"/";
	}

	public static String getResultVoucherPath() {
		WataLog.d("getResultPath");
//				return context.getExternalFilesDir().getPath() +"/gathering/saveData/subway/"
//				+StaticManager.folderName+"/"+StaticManager.floorName+"/" + StaticManager.subwayName +"/" + StaticManager.folderLevel+"/";

		return Environment.getExternalStorageDirectory().getPath() +"/gathering/saveData/subway/"
				+StaticManager.folderName+"/"+StaticManager.floorName+"/" + StaticManager.subwayName +"/" + StaticManager.folderLevel+"/";
	}

	public static String getResultVoucherPOIPath() {
		WataLog.d("getResultVoucherPOIPath");
		return Environment.getExternalStorageDirectory().getPath() +"/gathering/saveData/subway/"
				+StaticManager.folderName+"/"+StaticManager.floorName+"/" + StaticManager.subwayName+"/"+ StaticManager.folderLevel+"/"  + "POI"+"/";
	}

	public static String getResultPOIPath() {
		WataLog.d("getResultPath");
		return Environment.getExternalStorageDirectory().getPath() +"/gathering/saveData/pathGathering/"
				+StaticManager.folderName+"/"+StaticManager.pathDrawingName+"/" + "POI"+"/";
	}

	public static String getResultLinePath() {
		WataLog.d("getResultPath");
		return Environment.getExternalStorageDirectory().getPath() +"/gathering/saveData/pathGathering/"
				+StaticManager.folderName+"/"+StaticManager.pathDrawingName+"/" + "LINE"+"/";
	}

	public static String getImageLogPath(){
		WataLog.d("getImageLogPath");
		return Environment.getExternalStorageDirectory().getPath() +"/gathering/imageLog/"
				+StaticManager.folderName+"/"+StaticManager.floorName+"/"+StaticManager.pathNum+"/";
	}
}
