package com.geotwo.LAB_TEST.Gathering;

//import geo2.lbsp.ble.BLEManager;
public class SimpleGatheringActivity {
/*
public class SimpleGatheringActivity extends FragmentActivity implements LocationListener{

	private GoogleMap mmap;
    private LocationManager locationManager;
    private String provider;

	private DataCore _dataCore = null;
	private WifiScanner _wifiScanner = null;
	private com.geotwo.TBP.TBPWiFiScanner wifiScanner;

	private List<ScanResult> _list = null;

	private ArrayList<PDI_REPT_COL_DATA> _SCAN_RESULT_TOTAL = null;
	private PDI_REPT_COL_DATA _SCAN_RESULT_THIS_EPOCH = null;

	private boolean bScanning = false, bLogging = false;
	private Thread scanThread = null, _sensorthread = null;

	private sensoract _senact = null;

	private boolean bGathering = false;
	public static String mPlace = "";

	private TextView wifi_scan_count;
	private EditText placeText;

	static public Handler _UIHandler;
	static public boolean bIsGetFP = false;

	BLEManager _bleManager = null;

	private Vector markedPoint = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		sensorSet(true);
		_dataCore = DataCore.getInstance();

		_dataCore.setFloorString(StaticManager.floorName);
		_dataCore.setBuildName(StaticManager.title);

		if(StaticManager.basePointY != null && !StaticManager.basePointY.equalsIgnoreCase("" )&& StaticManager.basePointX != null && !StaticManager.basePointX.equalsIgnoreCase(""))
		{
			GatheringActivity._startPoint = new Vector(Double.valueOf(StaticManager.basePointX), Double.valueOf(StaticManager.basePointY));
		}
		else
			Toast.makeText(this, "지점 대표 좌표를 확인하세요", Toast.LENGTH_LONG).show();

		if (_SCAN_RESULT_THIS_EPOCH == null)
			_SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();

		if (_SCAN_RESULT_TOTAL == null)
			_SCAN_RESULT_TOTAL = _dataCore.getSCAN_RESULT_TOTAL();

		GooglePlayServicesUtil.isGooglePlayServicesAvailable(SimpleGatheringActivity.this);
       	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, true);
        if(provider==null){  //위치정보 설정이 안되어 있으면 설정하는 엑티비티로 이동합니다
         	new AlertDialog.Builder(SimpleGatheringActivity.this)
	        .setTitle("위치서비스 동의")
	        .setNeutralButton("이동" ,new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
				}
			}).setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			}).show();
        }else{   //위치 정보 설정이 되어 있으면 현재위치를 받아옵니다
    		//locationManager.requestLocationUpdates(provider, 1, 1, SimpleGatheringActivity.this);
        	setUpMapIfNeeded();
        }
		_UIHandler = new SimpleGetheringUIHandler();
	}

	public void onDestroy()
	{
		super.onDestroy();

		sensorSet(false);
		if(wifiScanner != null)
			unregisterReceiver ( wifiScanner );
		if(scanThread != null)
		{
			bScanning = false;
			bLogging = false;
			if(scanThread.isAlive())
				scanThread.interrupt();
		}
		if (_wifiScanner != null)
		{
			_wifiScanner.stopStatListener();
			_wifiScanner.stopScan();
			// wifiScanner.setWifiOff();
			_wifiScanner = null;
		}
		StaticManager.setEmptyAll();

//		if (_dataCore == null)
//			_dataCore = DataCore.getInstance();

//		_dataCore.setGatheringOption(DataCore.GATHER_OPTION_ROUTE);

		gatheringRelease();
	}

	private void wifiSet(boolean b)
	{
		if(b) {
			if (_wifiScanner == null){
				WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				_list = new ArrayList<ScanResult>();
				_wifiScanner = new WifiScanner(this, wifiManager, _UIHandler, _list, _bleManager);
//				_wifiScanner = new WifiScanner(this, wifiManager, _UIHandler, _list);
			}

		}else{
			if (_wifiScanner != null){
				_wifiScanner.stopStatListener();
				_wifiScanner.stopScan();
				// wifiScanner.setWifiOff();
				_wifiScanner = null;
			}
		}
	}

	private void sensorSet(boolean b)
	{
		if(b) {
			if (_senact == null){
				_senact = new sensoract(getApplication(), null);

				if (_sensorthread == null)	{
					_sensorthread = new Thread(_senact);
					_sensorthread.start();
				}
			}

		}else{
			if (_sensorthread != null)	{
				try
				{
					_senact.requestStop();
//					_sensorthread.stop();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				_sensorthread = null;
			}

			if (_senact != null){
				_senact = null;
			}
		}
	}

	private void gatheringRelease()
	{
		if (_sensorthread != null)
		{
			_sensorthread.interrupt();
			_sensorthread = null;
		}
		if (_senact != null)
		{
			_senact = null;
		}
		if (_wifiScanner != null)
		{
			_wifiScanner.stopStatListener();
			// wifiScanner.setWifiOff();
			_wifiScanner = null;
		}
	}

	private void endGathering() {
		wifiSet(false);
//		sensorSet(false);
		DataCore.iGatherMode=DataCore.GATHER_MODE_NONE;

		_dataCore.insertGatheredData(_dataCore.getCurGatheringName(), _dataCore.getSCAN_RESULT_TOTAL());

		_SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();

		SaveGatheredData.saveGatheredData(_dataCore.getLogDataPath(), "GEO2_"+_dataCore.getCurGatheringName() + "_simple.log", _SCAN_RESULT_THIS_EPOCH);
		SaveGatheredData.saveSendableData(_dataCore.getTempDataPath(), "GEO2_"+_dataCore.getCurGatheringName() + "_simple.temp", _SCAN_RESULT_THIS_EPOCH);

		_SCAN_RESULT_THIS_EPOCH = new PDI_REPT_COL_DATA(android.os.Build.SERIAL);
		_SCAN_RESULT_TOTAL = new ArrayList<PDI_REPT_COL_DATA>();

		_dataCore.setSCAN_RESULT_TOTAL(_SCAN_RESULT_TOTAL);
		_dataCore.setSCAN_RESULT_THIS_EPOCH(_SCAN_RESULT_THIS_EPOCH);

		mPlace = "";
		runOnUiThread(new Runnable() {
			public void run() {
				if(placeText != null)
					placeText.setText("");
			}
		});

		DataCore.isOnGathering = false;
		GatheringActivity._startPoint = null;
		GatheringActivity._endPoint = null;
	}

	private boolean startGathering() {

		if(bLogging || bScanning)
		{
			Toast.makeText(this, "측위를 졸료해 주세요", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(sensoract.mag_field[0]==0)
		{
			Toast.makeText(this, "센서대기", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(placeText == null || placeText.getText() == null || placeText.getText().toString().equalsIgnoreCase(""))
		{
			Toast.makeText(this, "장소를 설정해 주세요", Toast.LENGTH_SHORT).show();
			return false;
		}

		if(GatheringActivity._startPoint == null)
		{
			Toast.makeText(this, "xml 정좌표를 확인해 주세요", Toast.LENGTH_SHORT).show();
			return false;
		}
		else
			Log.d("jeongyeol", "x="+GatheringActivity._startPoint.x+"/y="+GatheringActivity._startPoint.y);

		GatheringActivity.mCurrentPath = -1;
		mPlace = placeText.getText().toString();
		GatheringActivity._endPoint = GatheringActivity._startPoint;
		GatheringActivity.lineHeader = 0.0;
		Polyline line = new Polyline();
		line.makeParts(1);
		Points pts = new Points();
		pts.makePoints(1);
		pts.data[0].x = GatheringActivity._startPoint.x;
		pts.data[0].y = GatheringActivity._startPoint.y;
		pts.data[0].z = GatheringActivity._startPoint.z;

		line.setParts(0, pts);

		DataCore.iGatherMode = DataCore.GATHER_MODE_GATHERING;
		_dataCore.setGatheringOption(DataCore.GATHER_OPTION_POINT);

		pdrvariable.initValues();
		initialmm.MM_initialize2(line);

		wifiSet(true);

		//GatheringDialog.makeGatherNameInsertDailog(GatheringActivity.this, StaticManager.title, StaticManager.floorName, _UIHandler, 9999).show();
		final String curTime = AndroidUtils.getCurrentTime();
		final String autoName = StaticManager.title + "-" + StaticManager.floorName + "-" + curTime;

    	DataCore dataCore = DataCore.getInstance();

		String insertText = autoName;

		dataCore.setCurGatheringName(insertText);
		dataCore.setCurGatherStartTime(curTime);
		return true;
	}

	public class SimpleGetheringUIHandler extends Handler
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case DataCore.REFREASH_WIFI_LIST: //wifi
				_SCAN_RESULT_TOTAL = _dataCore.getSCAN_RESULT_TOTAL();
				_SCAN_RESULT_THIS_EPOCH = _dataCore.getSCAN_RESULT_THIS_EPOCH();

				String total = _SCAN_RESULT_THIS_EPOCH.B21_Payload.size() + "회 스캔";
				if(wifi_scan_count != null)
					wifi_scan_count.setText(total);
				break;
			case 2919:
				RspPosition rspPos = (RspPosition)msg.obj;
				if(bScanning)
				{
					if(rspPos != null && rspPos.errorCode >= 0) {
						Log.d("jeongyeol", "2919 -- no error = "+rspPos.x+"/"+rspPos.y);
						if(rspPos.x != 0 && rspPos.y != 0)
						{
//							TransformGeo Tg = new TransformGeo(6378137.0, 298.257223563, 1.0, 38.0, 127.0, 600000.0, 200000.0);
//					        Vector currPosTM = Tg.transformGP2TM(rspPos.y, rspPos.x);

				        	Toast.makeText(SimpleGatheringActivity.this, rspPos.GID+"/"+rspPos.address+"/"+rspPos.strFloor, Toast.LENGTH_SHORT).show();
						}
			        }
					else
						Log.d("jeongyeol", "2919 -- error = "+rspPos.strError);

			        bIsGetFP = false;
				}
				else if(bLogging)
				{
//					if(rspPos.GID != null && StaticManager.gid != null && !rspPos.GID.equalsIgnoreCase(StaticManager.gid))
//					{
//						bIsGetFP = false;
//						return;
//					}
//					rspPos.x = rspPos.x/10;
//					rspPos.y = rspPos.y/10;
//					if(rspPos.x != 0 && rspPos.y != 0)// && rspPos.address != null && !rspPos.address.equalsIgnoreCase(""))
//					{
//						TransformGeo Tg = new TransformGeo(6378137.0, 298.257223563, 1.0, 38.0, 127.0, 600000.0, 200000.0);
//				        Vector currPosTM = Tg.transformGP2TM(rspPos.y, rspPos.x);
//				        rspPos.x = currPosTM.x;
//				        rspPos.y = currPosTM.y;
//					}
					File logDir = new File(Environment.getExternalStorageDirectory().getPath() + "/gathering/log/");
					if(!logDir.exists())
						logDir.mkdir();
					String logFile = Environment.getExternalStorageDirectory().getPath() + "/gathering/log/"+StaticManager.gid+"_"+StaticManager.floorName+".txt";
					if(rspPos != null && rspPos.errorCode >= 0 && wifiList != null)
						recordPoint(logFile, rspPos, wifiList);
					bIsGetFP = false;
				}
				break;
			}
		}
	}

	private void setUpMapIfNeeded() {

		if (mmap == null)
		{

			LayoutInflater inflater = getLayoutInflater();
			View v = inflater.inflate(R.layout.gmap_gathering_layout, null);

			FrameLayout _Layout = (FrameLayout) v.findViewById(R.id.map_frm);

			setContentView(v);
//			mmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

			if (mmap != null) {

				setUpMap();
				if(StaticManager.basePointY != null && !StaticManager.basePointY.equalsIgnoreCase("" )&& StaticManager.basePointX != null && !StaticManager.basePointX.equalsIgnoreCase(""))
				{
					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.curr));
					LatLng latLng = new LatLng(Double.valueOf(StaticManager.basePointY), Double.valueOf(StaticManager.basePointX));
					markerOptions.position(latLng);
					markCurrPos(markerOptions);
				}
				mmap.setOnMapClickListener(new OnMapClickListener() {

					@Override
					public void onMapClick(final LatLng latLng) {

						if(DataCore.iGatherMode == DataCore.GATHER_MODE_GATHERING)
							return;
//						Toast.makeText(SimpleGatheringActivity.this, "lng="+latLng.longitude+"/lat="+latLng.latitude, 200).show();
						MarkerOptions markerOptions = new MarkerOptions();

						markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));

						markerOptions.position(latLng);
						mmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

						mmap.addMarker(markerOptions);

						new AlertDialog.Builder(SimpleGatheringActivity.this)
				        .setTitle("lng="+latLng.longitude+"/lat="+latLng.latitude+" 로 대표 좌표 지정")
				        .setNeutralButton("지정" ,new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
									MarkerOptions markerOptions = new MarkerOptions();

									markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.curr));

									markerOptions.position(latLng);
									mmap.clear();

									mmap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

									mmap.addMarker(markerOptions);

									GatheringActivity._startPoint = null;
									GatheringActivity._startPoint = new Vector(latLng.longitude, latLng.latitude, 0);
								}
							})
						.setNegativeButton("취소", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {


							}
					}).show();



					}

				});
			}
//			v.findViewById(R.id.sensibility).setVisibility(View.GONE);
//			v.findViewById(R.id.map_view_all).setVisibility(View.GONE);
//			v.findViewById(R.id.gathering_select).setVisibility(View.GONE);
			TextView title = (TextView)v.findViewById(R.id.map_title);
			if(title != null)
				title.setText(StaticManager.title+"_"+StaticManager.floorName);
			Button map_back = (Button)v.findViewById(R.id.map_back);
			map_back.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(bGathering)
					{
						endGathering();
						bGathering = false;
					}
					finish();
				}
			});

			placeText = (EditText) v.findViewById(R.id.gather_place);
//			_Layout.addView(placeText);

			wifi_scan_count = (TextView) v.findViewById(R.id.scan_count);
			wifi_scan_count.setTextColor(Color.RED);
			wifi_scan_count.setText("0회 스캔");
			wifi_scan_count.setTextSize(16);
//			_Layout.addView(wifi_scan_count);

			Button startGather =  (Button) v.findViewById(R.id.start_gather);
			startGather.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(!bGathering)
					{
						if(startGathering())
						{
							((Button)v).setText("완료");
							bGathering = true;
						}
					}
					else
					{
						((Button)v).setText("수집");
						bGathering = false;
						endGathering();
					}
				}
			});
//			_Layout.addView(startGather);

			final Button startWPS = (Button) v.findViewById(R.id.startFP);
			startWPS.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(!bScanning)
					{
						bScanning = true;
						((TextView)v).setText("측위중");
						scanThread = new Thread(new Runnable() {
							public void run() {
								while(bScanning) {

									if(wifiScanner == null)
									{
										wifiScanner = new com.geotwo.TBP.TBPWiFiScanner(SimpleGatheringActivity.this);
										wifiScanner.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

								        IntentFilter NETWORK_STATE_LISTENER = new IntentFilter ( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION );
								        NETWORK_STATE_LISTENER.addAction ( WifiManager.NETWORK_STATE_CHANGED_ACTION );
								        NETWORK_STATE_LISTENER.addAction ( WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION );
								        NETWORK_STATE_LISTENER.addAction ( WifiManager.SUPPLICANT_STATE_CHANGED_ACTION );
								        NETWORK_STATE_LISTENER.addAction ( ConnectivityManager.CONNECTIVITY_ACTION );

										registerReceiver ( wifiScanner, NETWORK_STATE_LISTENER );
										SharedPreferences pref = getSharedPreferences("IP_PREF", 0);
										//wifiScanner.ipAddress = pref.getString("SOCKET_IP", "58.103.10.174");
										wifiScanner.ipAddress = pref.getString("SOCKET_IP", "14.35.194.146");
									}
									wifiScanner.startScan();
									while(!wifiScanner.isScanComplete) {
										try {
											Thread.sleep(100);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}

									wifiScanner.sendData(GEO2.LBSP.SClient.SvrPositionProvider.COORDTYPE.emReferenced);
									System.gc();
									wifiScanner.isScanComplete = false;
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
				        });
						scanThread.start();
					}
					else
					{
//						if(wifiScanner != null)
//							unregisterReceiver ( wifiScanner );
						bScanning = false;
						((TextView)v).setText("측위");
					}
				}
			});
//			_Layout.addView(startWPS);
			Button startLog = new Button(this);
			RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params4.setMargins(0, 0, 280, 10);
			params4.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params4.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			startLog.setLayoutParams(params4);
			startLog.setText("기록");
			startLog.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(bScanning)
					{
						bScanning = false;
						startWPS.setText("측위");
					}

					if(!bLogging)
					{
//						touchedNode = null;
						bLogging = true;
						((TextView)v).setText("기록중");
						scanThread = new Thread(new Runnable() {
							public void run() {
								while(bLogging) {

									if(wifiScanner == null)
									{
										wifiScanner = new com.geotwo.TBP.TBPWiFiScanner(SimpleGatheringActivity.this);
										wifiScanner.wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

								        IntentFilter NETWORK_STATE_LISTENER = new IntentFilter ( WifiManager.SCAN_RESULTS_AVAILABLE_ACTION );
								        NETWORK_STATE_LISTENER.addAction ( WifiManager.NETWORK_STATE_CHANGED_ACTION );
								        NETWORK_STATE_LISTENER.addAction ( WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION );
								        NETWORK_STATE_LISTENER.addAction ( WifiManager.SUPPLICANT_STATE_CHANGED_ACTION );
								        NETWORK_STATE_LISTENER.addAction ( ConnectivityManager.CONNECTIVITY_ACTION );

										registerReceiver ( wifiScanner, NETWORK_STATE_LISTENER );
										SharedPreferences pref = getSharedPreferences("IP_PREF", 0);
										//wifiScanner.ipAddress = pref.getString("SOCKET_IP", "58.103.10.174");
										wifiScanner.ipAddress = pref.getString("SOCKET_IP", "14.35.194.146");
									}

//									if(touchedNode != null)
									{
										wifiScanner.startScan();
										while(!wifiScanner.isScanComplete) {
											try {
												Thread.sleep(100);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
										}

										if(wifiList != null)
											wifiList.clear();
										wifiList = wifiScanner.wifiManager.getScanResults();
										wifiScanner.sendData(GEO2.LBSP.SClient.SvrPositionProvider.COORDTYPE.emReferenced);
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
					}
					else
					{
//						if(wifiScanner != null)
//							unregisterReceiver ( wifiScanner );
						bLogging = false;
						pointCount = 31;
//						touchedNode = null;
			        	Toast.makeText(SimpleGatheringActivity.this, " 수집종료.", Toast.LENGTH_SHORT).show();
						((TextView)v).setText("기록");
					}
				}
			});
			_Layout.addView(startLog);

		}
	}
	private List<ScanResult> wifiList;
	private int pointCount = 31;

    private void setUpMap() {

    	//mmap.setMyLocationEnabled(true);
    	mmap.getMyLocation();

	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	private void markCurrPos(MarkerOptions markerOptions)
	{
		if(mmap != null && markerOptions != null)
		{
			mmap.clear();
			mmap.animateCamera(CameraUpdateFactory.newLatLng(markerOptions.getPosition()));
			mmap.addMarker(markerOptions);
		}
	}
	
	private void recordPoint(String fileName, RspPosition pos_result, List<ScanResult> wifiList)
	{
		File file = new File(fileName);
        String s = null;
        String divider = "; ";
        
        if(pointCount == 30)
        {
        	s = "---------------------------------------------------------------------";
        	bLogging = false;
        	Toast.makeText(SimpleGatheringActivity.this, " 수집완료.", Toast.LENGTH_SHORT).show();
        }
        else if(pointCount > 30)
        {
        	s = "0; ";
        	s = s+getTime()+divider+StaticManager.basePointX+
        				divider+StaticManager.basePointY+
        				divider+StaticManager.floorName+divider+StaticManager.gid+divider+" // ref point";
//        	touchedPoint = touchedNode.getUnderlyingPoint();
        	pointCount = -1;
        }
        else if(pos_result != null)
        {
        	if(pos_result.strFloor != null && pos_result.address != null)
        		s = getTime()+divider+pos_result.x + divider + pos_result.y + divider + pos_result.address + divider + pos_result.strFloor.substring(0, 4) + divider;
        	else
        		s = getTime()+divider+pos_result.x + divider + pos_result.y + divider + "noAddr" + divider + "noFL" + divider;
        	s = (pointCount+1)+divider+s;
        	String mInfo = "";
        	if(wifiList != null)
        	{
	        	for(int i = 0; i < wifiList.size(); i++)
		        {
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
		        	mInfo = SSID+","+MACaddr+","+RSSI+","+Freq+divider;	
		        	if(Freq < 3000)
		        		s = s+mInfo;
		        }
	        	//pointCount++;
	        	Toast.makeText(SimpleGatheringActivity.this, (pointCount+1)+" 포인트 수집.", Toast.LENGTH_SHORT).show();
        	}
        }
        if(fileName.length()==0)
        {
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
                	  if(out != null)
                		  out.close();
                	  if(file_writer != null)
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
        }
        else   //이어쓰기
        {
    		BufferedWriter buff_writer = null;
			try {
				FileWriter file_writer = new FileWriter(file, true);
				buff_writer = new BufferedWriter(file_writer);
				PrintWriter print_writer = new PrintWriter(buff_writer,true);

	            print_writer.println(s);
	            if(buff_writer != null)
	            	buff_writer.close();
	            if(file_writer != null)
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
        
        if(pointCount < 31)
        	pointCount++;
    }
	
	public String getTime()
    {
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd_HHmmss", Locale.KOREA);
		Date date = new Date();
		String regDate = format.format(date);
		return regDate;
	}
	*/
}
