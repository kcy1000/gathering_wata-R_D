package com.geotwo.TBP;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.GatheringActivity;
import com.geotwo.LAB_TEST.Gathering.MainActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import GEO2.LBSP.SClient.PDI_SCANINFO;
import GEO2.LBSP.SClient.RspPosition;
import GEO2.LBSP.SClient.SvrPositionProvider;
import GEO2.LBSP.SClient.SvrPositionProvider.COORDTYPE;

public class TBPWiFiScanner extends BroadcastReceiver
{
	public WifiManager				wifiManager;
	List<ScanResult> 				wifiList;
	int								scanIdx;
	public boolean					isScanThreadRun = false, isScanComplete = false;
	long							startScanTime;
	long							elapsedScanTime;
	public boolean					bContinuous = false;
	int								nGetheringCount = 1;
	Activity					parent;
	public ArrayList<PDI_SCANINFO>		lstScanInfo = new ArrayList<PDI_SCANINFO>();
	public boolean					bLogSending = false;
	Handler 						_UIHandler = null;
	int								nAvgSize = 1;
	int								nAvgCurrent = 0;
//	String							ipAddress = "58.103.10.174";
	public int						logIndex = -1;
	public String					ipAddress = "14.35.194.145";
	public int 						port = 2919;
	
	public TBPWiFiScanner(Activity parent)
	{
		this.parent = parent;
		if(parent instanceof GatheringActivity)
			this._UIHandler = ((GatheringActivity)parent)._UIHandler;
//		else if(parent instanceof SimpleGatheringActivity)
//			this._UIHandler = ((SimpleGatheringActivity)parent)._UIHandler;
		else if(parent instanceof MainActivity)
			this._UIHandler = ((MainActivity)parent)._UIHandler;

		Log.d("ejcha2","mainactivity ifreq1111 ? "+ GatheringActivity.bScanning);
		Log.d("ejcha2","mainactivity ifreq2222 ? "+ MainActivity.bScanning);
		//if(GatheringActivity.bScanning != true && MainActivity.bScanning != true) setFrequencyBand(MainActivity.iFreq);
	}
	
	public void startScan()
	{
		startScanTime = System.currentTimeMillis();
		
		scanIdx = 0;
		isScanThreadRun = true;
		if (wifiManager.isWifiEnabled() == false)
		{

			// If Wifi Status Changed Sucessfully, You Can Receive Result From
			// Network State Changed Receiver
			wifiManager.setWifiEnabled(true);
		}
		Log.d("whdrms","startScan");
		wifiManager.startScan();
	}
	
	public void stopScan()
	{
		isScanThreadRun = false;
	}

	public RspPosition sendData(COORDTYPE type)
	{
		SvrPositionProvider scanData_L = new SvrPositionProvider(ipAddress, port);
		if(type == null)
			type = COORDTYPE.emMiddleTM;  //수정
		//	type = SvrPositionProvider.COORDTYPE.emReferenced; 
		scanData_L.setCoordType(type);//SvrPositionProvider.COORDTYPE.emReferenced);
		if(lstScanInfo.size() > 0) {
		
			//수정 주석처리
			//	scanData_L.FD2_SCANINFO_A_List.clear();
			//	scanData_L.FD2_SCANINFO_A_List.addAll(lstScanInfo);
			//	scanData_L.requestPos();
		
			//수정 추가
			scanData_L.addWifiScanInfo(lstScanInfo);
			RspPosition reqPos = scanData_L.requestPos();

			if(parent instanceof GatheringActivity)
				((GatheringActivity)parent).bIsGetFP = true;
//			else if(parent instanceof SimpleGatheringActivity)
//				((SimpleGatheringActivity)parent).bIsGetFP = true;
    		Message msg = new Message();
			msg.what = 2919; 
			//msg.obj = scanData_L.reqOutdoorPos; //수정 주석처리
			//	msg.arg1 = scanData_L.FD2_SCANINFO_A_List.size(); 
			
			msg.obj = reqPos; //수정 추가
			msg.arg1 = lstScanInfo.size(); //수정 추가

	        _UIHandler.sendMessage(msg);

	        if(parent instanceof GatheringActivity)
	        {
				try {
					while(((GatheringActivity)parent).bIsGetFP)
						Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
//	        else if(parent instanceof SimpleGatheringActivity)
//	        {
//	        	try {
//					while(((SimpleGatheringActivity)parent).bIsGetFP)
//						Thread.sleep(100);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//	        }
//			Log.d("jeongyeol", "org x="+scanData_L.reqOutdoorPos.x+"/y="+scanData_L.reqOutdoorPos.y);
			//return scanData_L.reqOutdoorPos; //수정 주석처리
			return reqPos;
		} 
		return null;
	}


	public RspPosition sendData_main(COORDTYPE type)
	{
		SvrPositionProvider scanData_L = new SvrPositionProvider(ipAddress, port);
		if(type == null)
			type = COORDTYPE.emMiddleTM;  //수정
		//	type = SvrPositionProvider.COORDTYPE.emReferenced;
		scanData_L.setCoordType(type);//SvrPositionProvider.COORDTYPE.emReferenced);
		if(lstScanInfo.size() > 0) {

			//수정 주석처리
			//	scanData_L.FD2_SCANINFO_A_List.clear();
			//	scanData_L.FD2_SCANINFO_A_List.addAll(lstScanInfo);
			//	scanData_L.requestPos();

			//수정 추가
			scanData_L.addWifiScanInfo(lstScanInfo);
			RspPosition reqPos = scanData_L.requestPos();

			if(parent instanceof GatheringActivity)
				((GatheringActivity)parent).bIsGetFP = true;
//			else if(parent instanceof SimpleGatheringActivity)
//				((SimpleGatheringActivity)parent).bIsGetFP = true;
			Message msg = new Message();
			msg.what = 2919;
			//msg.obj = scanData_L.reqOutdoorPos; //수정 주석처리
			//	msg.arg1 = scanData_L.FD2_SCANINFO_A_List.size();

			msg.obj = reqPos; //수정 추가
			msg.arg1 = lstScanInfo.size(); //수정 추가


			if(parent instanceof GatheringActivity)
			{
				try {
					while(((GatheringActivity)parent).bIsGetFP)
						Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
//			else if(parent instanceof SimpleGatheringActivity)
//			{
//				try {
//					while(((SimpleGatheringActivity)parent).bIsGetFP)
//						Thread.sleep(100);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			Log.d("jeongyeol", "org x="+scanData_L.reqOutdoorPos.x+"/y="+scanData_L.reqOutdoorPos.y);
			//return scanData_L.reqOutdoorPos; //수정 주석처리
			return reqPos;
		}
		return null;
	}

	//수정 주석처리
	public void reSendData()
	{
		if(lstScanInfo.size() <= 0) {
			Toast.makeText(parent, "데이터 없음", Toast.LENGTH_SHORT).show();
			return;
		}
		
		SvrPositionProvider scanData_L = new SvrPositionProvider(ipAddress,port);
		scanData_L.FD2_SCANINFO_A_List.addAll(lstScanInfo);
        scanData_L.requestPos();
        //parent.drawPoint(scanData_L.reqOutdoorPos.x, scanData_L.reqOutdoorPos.y);
        Toast.makeText(parent, "x:" + scanData_L.reqOutdoorPos.x + " y:" + scanData_L.reqOutdoorPos.y, Toast.LENGTH_SHORT).show();	
	}
	
	public void onReceive ( Context c, Intent intent )
	{
		final String action = intent.getAction();

		if (!action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) 
			return;

		if ( isScanThreadRun != true ) 
			return;

		if(lstScanInfo.size() > 0)
			lstScanInfo.clear();

		wifiList = wifiManager.getScanResults();
		if(wifiList.size() == 0){
			Toast.makeText(parent, "GPS를 On으로 변경해주세요", Toast.LENGTH_SHORT).show();
		}

		elapsedScanTime = (System.currentTimeMillis() - startScanTime) / 1000;

        for(int i = 0; i < wifiList.size(); i++) {
        	String MACaddr 
        		= wifiList.get(i).BSSID.substring(0, 2)
        		+ wifiList.get(i).BSSID.substring(3, 5)
        		+ wifiList.get(i).BSSID.substring(6, 8)
        		+ wifiList.get(i).BSSID.substring(9, 11)
        		+ wifiList.get(i).BSSID.substring(12, 14)
        		+ wifiList.get(i).BSSID.substring(15, 17);
        	String SSID = wifiList.get(i).SSID;
        	int RSSI = wifiList.get(i).level;
        	Log.d("jeongyeol", "wifi ssid = "+SSID+"/ rssi = "+RSSI);
        	
        	int Freq = wifiList.get(i).frequency;

        	if(Freq > 5000)
        		Log.d("", "");
//    		wifiList = wifiManager.getScanResults();

    		PDI_SCANINFO scandata = new PDI_SCANINFO();

    		scandata.FD1_INFRA_TYPE = PDI_SCANINFO.DEF_INFRA_TYPE_WiFi;
    		scandata.FD2_INFRA_ID = MACaddr;
    		scandata.FD3_RSSI = (byte) RSSI;
    		scandata.FD4_FREQ = (short) Freq;
    		scandata.FD5_ENCRYPTION = 0;
    		scandata.FD6_SSID_LEN = (short) SSID.length();
    		scandata.FD7_SSID = SSID;

    		lstScanInfo.add(scandata);
        }
        isScanComplete = true;
        stopScan();
	}

	public boolean setFrequencyBand ( int band )       // band �� �ǹ�: 0: �ڵ�, 1: 5 Ghz, 2: 2.4Ghz
	{
		if ( android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH ) // Version 14
		{
			return false;
		}

		boolean bRet = true;

		Method[] methods = wifiManager.getClass().getDeclaredMethods();
		for ( Method method : methods )
		{
			if ( method.getName().equals("setFrequencyBand") )
			{
				try
				{
					method.invoke ( wifiManager, band, false );
					Log.d("jeongyeol", "set freq is succeed with "+band);
				}
				catch ( Exception e )
				{
					e.printStackTrace();
					bRet = false;
					Log.d("jeongyeol", "set freq is failed");
				}

				break;
			}
		}

		return bRet;
	}

	public String getDateTime_long()
	{
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	    
	    return sdf.format(cal.getTime());
	}
	
	public String getDateTime_short()
	{
		Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	    
	    return sdf.format(cal.getTime());
	}	
	
	class LogReadThread extends Thread {
		public String fileName = "";
		public void run() {
			try {
				InputStreamReader inStreamRdr = new InputStreamReader(new FileInputStream(fileName), "EUC-KR");
				BufferedReader fIn = new BufferedReader(inStreamRdr);
				String readStr = fIn.readLine();
				
				PDI_SCANINFO scandata = new PDI_SCANINFO();
				String[] tokenedStr = readStr.split(",");
				scandata.FD1_INFRA_TYPE = PDI_SCANINFO.DEF_INFRA_TYPE_WiFi;
				scandata.FD2_INFRA_ID = tokenedStr[3].trim();
				scandata.FD3_RSSI = (byte) Byte.parseByte(tokenedStr[4].trim());
				scandata.FD4_FREQ = (short) Short.parseShort(tokenedStr[6].trim());
				scandata.FD5_ENCRYPTION = 0;
				scandata.FD6_SSID_LEN = (short) tokenedStr[5].trim().length();
				scandata.FD7_SSID = tokenedStr[5].trim();
				scandata.FDx_MAGNETO = Double.parseDouble(tokenedStr[18].trim());
			
				ArrayList<PDI_SCANINFO> lstScanInfoA = new ArrayList<PDI_SCANINFO>();
				lstScanInfoA.add(scandata);
				bLogSending = true;
				logIndex = 0;
				while((readStr = fIn.readLine()) != null && bLogSending) {
					readStr = readStr.trim();
					if(readStr.compareTo("") == 0) {
						logIndex++;
		    			//setAvgRSSI(lstScanInfoA); //수정 주석해제
		    			continue;
		    		}
					tokenedStr = readStr.split(",");
					
					scandata = new PDI_SCANINFO();
					scandata.FD1_INFRA_TYPE = PDI_SCANINFO.DEF_INFRA_TYPE_WiFi;
					scandata.FD2_INFRA_ID = tokenedStr[3].trim();
					scandata.FD3_RSSI = (byte) Byte.parseByte(tokenedStr[4].trim());
					scandata.FD4_FREQ = (short) Short.parseShort(tokenedStr[6].trim());
					scandata.FD5_ENCRYPTION = 0;
					scandata.FD6_SSID_LEN = (short) tokenedStr[5].trim().length();
					scandata.FD7_SSID = tokenedStr[5].trim();
		
		    		lstScanInfoA.add(scandata);
				}
				inStreamRdr.close();
				
				if(lstScanInfoA.size() > 0) {
					logIndex++;
	    			//setAvgRSSI(lstScanInfoA); //수정 주석해제
	    		}
			} catch(Exception e) {
				e.printStackTrace();
			}
			bLogSending = false;
			logIndex = -1;
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		Message msg = _UIHandler.obtainMessage();
    		RspPosition pos = new RspPosition();
    		pos.errorCode = -1;
    		pos.strError = "log end.";
    		msg.obj = pos;
			msg.what = 2919; 
	        _UIHandler.sendMessage(msg);
		}
	}
	
	
	//수정 주석해제
	//public void setAvgRSSI (ArrayList<PDI_SCANINFO>	lstScanInfoA)
//	{
//		nAvgCurrent++;
//		
//		for(PDI_SCANINFO e: lstScanInfoA) {
//			int idx = -1;
//			if((idx = lstScanInfo.indexOf(e)) != -1) {
//				PDI_SCANINFO tE = lstScanInfo.get(idx);
//				int sum = tE.FD3_RSSI + e.FD3_RSSI;
//				tE.FD3_RSSI = (byte)((sum) / 2);
//				double alpha = ((double)(nAvgCurrent - 1.0) / (double)nAvgCurrent);
//				double avg = (alpha*(double)tE.FD3_RSSI)+(1.0-alpha)*(double)e.FD3_RSSI;
//				//System.out.println("alpha:" + alpha + " avg:" + avg);
//				tE.FD3_RSSI = (byte)avg;				
//			} else {
//				lstScanInfo.add(e);
//			}
//		}
//		
//		if(nAvgCurrent >= nAvgSize) {
//			sendData();
//			lstScanInfo.clear();
//			nAvgCurrent = 0;
//		}
//		lstScanInfoA.clear();
//	}
	
	
	public void logSend(String fileName) 
	{
		if(true || bLogSending)
			return;
		
		LogReadThread logReadThread = new LogReadThread();
        logReadThread.fileName = fileName;
        logReadThread.setDaemon(true);
        logReadThread.start();
	}
}

