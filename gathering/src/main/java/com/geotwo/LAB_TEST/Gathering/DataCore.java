package com.geotwo.LAB_TEST.Gathering;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Environment;
import android.util.Log;

import com.geotwo.LAB_TEST.Gathering.dialog.BuildInfoFileDialog;
import com.geotwo.LAB_TEST.Gathering.dto.BuildInfo;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.data.StaticManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import ETRI.LBSP.Common.Interfaces.ConnectorRMCS;
import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA;

public class DataCore
{
	private static DataCore instance = null;

	//	private String IP_ADDRESS = "14.35.194.146";

	public static String IP_ADDRESS = "3.34.75.68";  // kcy1000 - 로그파일 업로드 ip
	public static String DATA_UP_LORD_PORT = "8004";
	private String CDPS_SERVER_DOMAIN = "moberan.iptime.org";
	private int CDPS_PORT_NUM = 8002;
	public static int DEFAULT_FLOOR_NUM = 4;

	private String URL = "";
	private String TPS_URL = "";
	private int RMCS_PORT = 0;
	private int TPS_PORT = 0;
	private int PORT = 0;


	public final static int RES_OK = 10;
	public final static int RES_BACK = 30;
	public final static int PDR_STAT_CHANGED = 100; //sensor
	public final static int AT_RECORD_SENSING = 101;
	public final static int AT_START_NEW_RECORD = 102;
	public final static int HANDLER_ENDED = 200;
	public final static int REFREASH_WIFI_LIST = 300;
	public final static int DATA_SEND_READY = 400;
	public final static int NETWORK_READY = 500;
	public final static int DATA_SEND_ENDED = 600;
	public final static int DATA_SEND_FAILED = 700;
	public final static int GATHER_MODE_CHANGED = 800;

	public final static int FROM_MAP_SELECTOR = 10000;
	public final static int FROM_MAP_NAVIGATION = 20000;

	public final static int ON_CLICK_OK_ON_UPLOAD = 1;
	public final static int ON_CLICK_CANCLE_ON_UPLOAD = 2;
	public final static int ON_CLICK_OK_ON_DELETE = 3;
	public final static int ON_CLICK_CANCLE_ON_DELETE = 4;
	public final static int ON_CLICK_OK_ON_START = 5;
	public final static int ON_CLICK_CANCLE_ON_START = 6;
	public final static int ON_CLICK_OK_ON_LOCATION_UPLOAD = 7;
	public final static int ON_CLICK_CANCLE_ON_LOCATION_UPLOAD = 8;

	public final static int ON_TOUCH_MAP = 11;
	public final static int ON_TOUCH_MOVE_MAP = 13;

	public final static int GATHER_OPTION_ROUTE = 0;
	public final static int GATHER_OPTION_POINT = 1;
	public final static int GATHER_OPTION_AUTO = 2;

	public int gatheringOption = GATHER_OPTION_ROUTE;

	public static boolean isOnGathering = false;

	ConnectorRMCS rmcsConnector = null;
	ArrayList<PDI_REPT_COL_DATA> SCAN_RESULT_TOTAL = null;
	PDI_REPT_COL_DATA SCAN_RESULT_THIS_EPOCH = null;
	HashMap<String, ArrayList<PDI_REPT_COL_DATA>> SCAN_RESULT_MAP = null;

	Vector<String> buildNameList = new Vector<String>();
	HashMap<String, BuildInfo> buildInfoList = new HashMap<String, BuildInfo>();

	private String floorString = "";
	private String buildName = "";
	private String curGatheringName = "";
	private String curGatherStartTime = "";

	private List<ScanResult> scanResultList = null;
	private int gatheringPeriod = 0;

	//경로수집 데이터
//	public String pathLogData = null;
//	public String pathTempData = null;
	
	//두점수집, 지점수집 데이터
	public String logDataPath = null;
	public String tempDataPath = null;
	//측위로그 데이터
	public String locationLogDataPath = null;

	public static final int GATHER_MODE_NONE = 1;
	public static final int GATHER_MODE_START_SELECT = 2;
	public static final int GATHER_MODE_END_SELECT = 3;
	public static final int GATHER_MODE_GATHERING = 4;
	
	public static int iGatherMode =  GATHER_MODE_NONE;
	

	private DataCore()
	{
		SCAN_RESULT_TOTAL = new ArrayList<PDI_REPT_COL_DATA>();
		SCAN_RESULT_THIS_EPOCH = new PDI_REPT_COL_DATA(android.os.Build.SERIAL);
		SCAN_RESULT_MAP = new HashMap<String, ArrayList<PDI_REPT_COL_DATA>>();
		rmcsConnector = new ConnectorRMCS(getURL(),getRmcsPort());
//		URL = CDPS_SERVER_DOMAIN;
		PORT = CDPS_PORT_NUM;

//		logDataPath = Environment.getExternalStorageDirectory().getPath() + "/LoggedData/";
//		tempDataPath = Environment.getExternalStorageDirectory().getPath() + "/TempData/";
//		pathLogData = Environment.getExternalStorageDirectory().getPath() +"/Download/gathering/saveData/pathGathering/";
//		pathTempData = Environment.getExternalStorageDirectory().getPath()+"/Download/gathering/saveData/pathGathering/";
		logDataPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pointGathering/log/";
		tempDataPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pointGathering/";
		locationLogDataPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/log/";
		gatheringOption = GATHER_OPTION_ROUTE;
	}

	static public DataCore getInstance()
	{
		if (instance == null)
		{
			instance = new DataCore();
		}

		return instance;
	}

	public String getIP_ADDRESS()
	{
		return IP_ADDRESS;
	}

	public void setIP_ADDRESS(String iP_ADDRESS)
	{
		IP_ADDRESS = iP_ADDRESS;
	}

	public ArrayList<PDI_REPT_COL_DATA> getSCAN_RESULT_TOTAL()
	{
		return SCAN_RESULT_TOTAL;
	}

	public void setSCAN_RESULT_TOTAL(ArrayList<PDI_REPT_COL_DATA> sCAN_RESULT_TOTAL)
	{
		SCAN_RESULT_TOTAL = sCAN_RESULT_TOTAL;
	}

	public PDI_REPT_COL_DATA getSCAN_RESULT_THIS_EPOCH()
	{
		return SCAN_RESULT_THIS_EPOCH;
	}

	public void setSCAN_RESULT_THIS_EPOCH(PDI_REPT_COL_DATA sCAN_RESULT_THIS_EPOCH)
	{
		SCAN_RESULT_THIS_EPOCH = sCAN_RESULT_THIS_EPOCH;
	}

	public String getFloorString()
	{
		return floorString;
	}

	public void setFloorString(String floorString)
	{
		this.floorString = floorString;
	}

	public String getBuildName()
	{
		return buildName;
	}

	public void setBuildName(String buildName)
	{
		this.buildName = buildName;
	}

	public List<ScanResult> getScanResultList()
	{
		return scanResultList;
	}

	public void setScanResultList(List<ScanResult> scanResultList)
	{
		this.scanResultList = scanResultList;
	}

//	public void sendScanRslt()
//	{
//		rmcsConnector.connect();
//
//		for (int i = 0; i < SCAN_RESULT_TOTAL.size(); i++)
//		{
//			rmcsConnector.sendMessage(SCAN_RESULT_TOTAL.get(i));
//		}
//
//		rmcsConnector.disconnect();
//	}

	public String getURL()
	{
		if(URL == null || URL.equalsIgnoreCase(""))
			return Constance.CONNECTING_IP;
		else
			return URL;
	}

	public void setURL(String uRL)
	{
		URL = uRL;
	}

	public String getTpsURL()
	{
		if(TPS_URL == null || TPS_URL.equalsIgnoreCase(""))
			return Constance.SOCKET_IP;
		else
			return TPS_URL;
	}

	public void setTpsURL(String uRL)
	{
		TPS_URL = uRL;
	}

	
	public int getRmcsPort()
	{
		return RMCS_PORT;
	}

	public void setRmcsPort(String port){
		if(port == null || port.equalsIgnoreCase(""))
		{
			RMCS_PORT = 0;
		}else{
			RMCS_PORT = Integer.parseInt(port);
		}
	}
	public int getTpsPort()
	{
		return TPS_PORT;
	}

	public void setTpsPort(String port){
		if(port == null || port.equalsIgnoreCase(""))
		{
			TPS_PORT = 0;
		}else{
			TPS_PORT = Integer.parseInt(port);
		}
	}

	public String getCurGatheringName()
	{
		return curGatheringName;
	}

	public void setCurGatheringName(String curGatheringName)
	{
		this.curGatheringName = curGatheringName;
	}

	public void insertGatheredData(String gatherName, ArrayList<PDI_REPT_COL_DATA> RESULT)
	{
		SCAN_RESULT_MAP.put(gatherName, RESULT);
	}

	public HashMap<String, ArrayList<PDI_REPT_COL_DATA>> getSCAN_RESULT_MAP()
	{
		return SCAN_RESULT_MAP;
	}

	public void setSCAN_RESULT_MAP(HashMap<String, ArrayList<PDI_REPT_COL_DATA>> sCAN_RESULT_MAP)
	{
		SCAN_RESULT_MAP = sCAN_RESULT_MAP;
	}

	public int getGatheringPeriod()
	{
		return gatheringPeriod;
	}

	public void setGatheringPeriod(int gatheringPeriod)
	{
		this.gatheringPeriod = gatheringPeriod;
	}

	public String getCurGatherStartTime()
	{
		return curGatherStartTime;
	}

	public void setCurGatherStartTime(String curGatherStartTime)
	{
		this.curGatherStartTime = curGatherStartTime;
	}

	public int getPORT()
	{
		return PORT;
	}

	public void setPORT(int pORT)
	{
		PORT = pORT;
	}
//--- 경로수집
	/*public String getPathLogData()
	{
		return pathLogData;
	}

	public void setPathLogData(String pathLogData)
	{
		this.pathLogData = pathLogData;
	}

	public String getPathTempData()
	{
		return pathTempData;
	}

	public void setPathTempData(String pathTempData)
	{
		this.pathTempData = pathTempData;
	}*/
//---
	public String getLogDataPath()
	{
		return logDataPath;
	}

	public void setLogDataPath(String logDataPath)
	{
		this.logDataPath = logDataPath;
	}

	public String getTempDataPath()
	{
		return tempDataPath;
	}

	public String getLocationLogDataPath()
	{
		return locationLogDataPath;
	}

	public void setTempDataPath(String tempDataPath)
	{
		this.tempDataPath = tempDataPath;
	}

	public int getGatheringOption()
	{
		return gatheringOption;
	}

	public void setGatheringOption(int gatheringOption)
	{
		this.gatheringOption = gatheringOption;
	}

	public Vector<String> getBuildNameList()
	{
		return buildNameList;
	}

	public void setBuildNameList(Vector<String> buildNameList)
	{
		this.buildNameList = buildNameList;
	}

	public BuildInfo getBuildInfo(String buildName)
	{
		BuildInfo data = new BuildInfo();
//		WataLog.d( "StaticManager.position="+StaticManager.position+", gid="+StaticManager.floorInfo.get(StaticManager.position).getGid());
		WataLog.i("check!999!!!");
		WataLog.d("StaticManager.gid=" + StaticManager.gid);

		data.setGIDex(StaticManager.gid);
		if(StaticManager.floorInfo != null)
		{
			data.setLatitude(StaticManager.floorInfo.get(StaticManager.position).getLat());
			data.setLongitude(StaticManager.floorInfo.get(StaticManager.position).getLon());
			data.setBearing(StaticManager.floorInfo.get(StaticManager.position).getBearing());
		}
		else
		{
			data.setLatitude("0");
			data.setLongitude("0");
			data.setBearing("0");
		}
		data.setAddr(StaticManager.folderName);
		
		return data; //buildInfoList.get(buildName);
	}

	public HashMap<String, BuildInfo> getBuildInfoList()
	{
		return buildInfoList;
	}

	public void setBuildInfoList(HashMap<String, BuildInfo> buildInfoList)
	{
		this.buildInfoList = buildInfoList;
	}

	public void readBuildList(Context context)
	{
		buildNameList = new Vector<String>();
		buildInfoList = new HashMap<String, BuildInfo>();

		File buildInfoFile = new File(Environment.getExternalStorageDirectory().getPath() + "/em3D/build_info.ini");

		if (buildInfoFile.exists() == true)
		{
			String buildInfoString = readFileToString(buildInfoFile);
			buildNameList = parseBuildList(buildInfoString);
			buildInfoList = parseBuildInfo(buildInfoString, buildNameList);

		}
		else
		{
			BuildInfoFileDialog.iniFileIsNotExist(context).show();
		}
	}

	public String readFileToString(File file)
	{
		String res = "";
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(file));
			StringBuffer str = new StringBuffer();
			String line = br.readLine();
			while (line != null)
			{
				str.append(line);
				str.append("\n");
				line = br.readLine();
			}

			res = str.toString();
		}
		catch (Exception E)
		{
			Log.e("MapSelectActivity", E.toString());
		}
		finally
		{
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException ioe)
				{
					Log.e("MapSelectActivity", ioe.toString());
				}
			}
			br = null;
		}

		return res;
	}

	public Vector<String> parseBuildList(String buildInfoString)
	{
		Vector<String> buildList = new Vector<String>();

		try
		{
			String[] newLineSplit = buildInfoString.split("\n");
			String buildNamesWithcomma = newLineSplit[0].split("=")[1];
			String[] buildNames = buildNamesWithcomma.split(",");

			for (int i = 0; i < buildNames.length; i++)
			{
				buildList.add(buildNames[i].trim());
			}

		}
		catch (Exception e)
		{
			Log.e("DataCore", e.toString());
		}

		return buildList;
	}

	public HashMap<String, BuildInfo> parseBuildInfo(String buildInfoString, Vector<String> buildList)
	{
		HashMap<String, BuildInfo> buildInfoList = new HashMap<String, BuildInfo>();

		try
		{
			String[] newLineSplit = buildInfoString.split("\n");

			for (int i = 0; i < buildList.size(); i++)
			{
				String buildName = buildList.get(i);

				for (int j = 1; j < newLineSplit.length; j++)
				{
					Log.e("parseBuildInfo", newLineSplit[j]);

					if (newLineSplit[j].startsWith(buildName + "_START_FLOOR"))
					{
						BuildInfo buildInfo = new BuildInfo();

						String startFloor = newLineSplit[j].replace(buildName + "_START_FLOOR=", "").trim();
						String endFloor = newLineSplit[j + 1].replace(buildName + "_END_FLOOR=", "").trim();
						String fileName = newLineSplit[j + 2].replace(buildName + "_FILE_NAME=", "").trim();

						String GIDex = newLineSplit[j + 3].replace(buildName + "_GIDex=", "").trim();
						String Latitude = newLineSplit[j + 4].replace(buildName + "_Latitude=", "").trim();
						String Longitude = newLineSplit[j + 5].replace(buildName + "_Longitude=", "").trim();
						String Bearing = newLineSplit[j + 6].replace(buildName + "_Bearing=", "").trim();
						String Addr = newLineSplit[j + 7].replace(buildName + "_Addr=", "").trim();

						buildInfo.setStartFloor(startFloor);
						buildInfo.setEndFloor(endFloor);
						buildInfo.setFileName(fileName);
						WataLog.i("check8888!!!");
						buildInfo.setGIDex(GIDex);
						buildInfo.setLatitude(Latitude);
						buildInfo.setLongitude(Longitude);
						buildInfo.setBearing(Bearing);
						buildInfo.setAddr(Addr);

						buildInfoList.put(buildName, buildInfo);

						break;
					}
				}
			}

		}
		catch (Exception e)
		{
			Log.e("DataCore", e.toString());
		}

		return buildInfoList;
	}

	public int parseFloor(String sfloor)
	{
		int floor = 1;
		
		try
		{
			if (sfloor.startsWith("B") == true || sfloor.startsWith("b") == true)
			{
				floor = -1 * Integer.parseInt(sfloor.substring(1));
			}
			else
			{
				floor = Integer.parseInt(sfloor);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return floor;
	}

}
