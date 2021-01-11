package geo2.lbsp.ible.service;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import geo2.lbsp.ible.Beacon;
import geo2.lbsp.ible.BluetoothScanner;
import geo2.lbsp.ible.BluetoothScanner.ScannerCallback;
import geo2.lbsp.ible.internal.MonitoringResult;
import geo2.lbsp.ible.internal.Preconditions;
import geo2.lbsp.ible.internal.RangingResult;
import geo2.lbsp.ible.internal.ScanPeriodData;
import geo2.lbsp.ible.internal.ScanRecord;
import geo2.lbsp.ible.internal.ThreadedHandler;

public class BLEService extends Service {
    public static final int MSG_START_RANGING = 1;
    public static final int MSG_STOP_RANGING = 2;
    public static final int MSG_RANGING_RESPONSE = 3;
    public static final int MSG_START_MONITORING = 4;
    public static final int MSG_STOP_MONITORING = 5;
    public static final int MSG_START_EDDYSTONE_SCANNING = 6;
    public static final int MSG_EDDYSTONE_SCAN_RESPONSE = 7;
    public static final int MSG_STOP_EDDYSTONE_SCANNING = 8;
    public static final int MSG_START_NEARABLE_SCANNING = 9;
    public static final int MSG_NEARABLE_SCAN_RESPONSE = 10;
    public static final int MSG_STOP_NEARABLE_SCANNING = 11;
    public static final int MSG_MONITORING_RESPONSE = 12;
    public static final int MSG_REGISTER_ERROR_LISTENER = 13;
    public static final int MSG_ERROR_RESPONSE = 14;
    public static final int MSG_SET_FOREGROUND_SCAN_PERIOD = 15;
    public static final int MSG_SET_BACKGROUND_SCAN_PERIOD = 16;
    
    public static final int MSG_START_SERVICE = 101;
    public static final int MSG_STOP_SERVICE = 102;
    public static final int MSG_BEGIN_GATHERING = 103;
    public static final int MSG_END_GATHERING = 104;
    public static final int MSG_GET_GATHERED_BECONS = 105;
    public static final int MSG_GATHERD_BECONS_RESPONSE = 106;
	public static final int MSG_HOOK_GATHERED_BECONS_ESTIMATION = 107;
	public static final int MSG_GET_GATHERED_ESTIMATION_BECONS = 108;
	public static final int MSG_GATHERD_ESTIMATION_BECONS_RESPONSE = 109;

    public static final int ERROR_COULD_NOT_START_LOW_ENERGY_SCANNING = -1;
    public static final String REGION_KEY = "region";
    public static final String REGION_ID_KEY = "regionId";
    public static final String RANGING_RESULT_KEY = "rangingResult";
    public static final String MONITORING_RESULT_KEY = "monitoringResult";
    public static final String SCAN_PERIOD_KEY = "scanPeriod";
    public static final String ERROR_ID_KEY = "errorId";

    public static final String GATHERD_BECONS_RESULT_KEY = "gathredBeaconsResult";

	private final Messenger messenger = new Messenger(new IncomingHandler());
	private ThreadedHandler handler;
	private BluetoothScanner bluetoothScanner;
	private BroadcastReceiver bluetoothBroadcastReceiver;
    private ScanPeriodData foregroundScanPeriod;
    private ScanPeriodData backgroundScanPeriod;
    private geo2.lbsp.ible.BeaconScanner beaconScanner = new geo2.lbsp.ible.BeaconScanner();
    private final geo2.lbsp.ible.internal.RegionObserver rangingObserver = new geo2.lbsp.ible.internal.RegionObserver();
    private Messenger errorReplyTo;

	private geo2.lbsp.ible.BeaconScanner beaconScannerForEstimation = null;

    private boolean gathering = true;
    private boolean serviceMode = false;

    private void invokeCallbacks(List<geo2.lbsp.ible.internal.MonitoringRegion> enteredMonitors, List<geo2.lbsp.ible.internal.MonitoringRegion> exitedMonitors, List<geo2.lbsp.ible.internal.RangingRegion> rangedRegions) {
        try {
            Iterator e = rangedRegions.iterator();

            Message monitoringResponseMsg;
            while(e.hasNext()) {
                geo2.lbsp.ible.internal.RangingRegion didEnterMonitor = (geo2.lbsp.ible.internal.RangingRegion)e.next();
                monitoringResponseMsg = Message.obtain(null, BLEService.MSG_RANGING_RESPONSE);
                monitoringResponseMsg.getData().putParcelable(RANGING_RESULT_KEY, new RangingResult(didEnterMonitor.region, didEnterMonitor.getSortedBeacons()));
                didEnterMonitor.replyTo.send(monitoringResponseMsg);
            }

            e = enteredMonitors.iterator();

            geo2.lbsp.ible.internal.MonitoringRegion didEnterMonitor1;
            while(e.hasNext()) {
                didEnterMonitor1 = (geo2.lbsp.ible.internal.MonitoringRegion)e.next();
                monitoringResponseMsg = Message.obtain(null, BLEService.MSG_MONITORING_RESPONSE);
                monitoringResponseMsg.getData().putParcelable(MONITORING_RESULT_KEY, new MonitoringResult(didEnterMonitor1.region, geo2.lbsp.ible.Region.State.INSIDE, didEnterMonitor1.getSortedBeacons()));
                didEnterMonitor1.replyTo.send(monitoringResponseMsg);
            }

            e = exitedMonitors.iterator();

            while(e.hasNext()) {
                didEnterMonitor1 = (geo2.lbsp.ible.internal.MonitoringRegion)e.next();
                monitoringResponseMsg = Message.obtain(null, BLEService.MSG_MONITORING_RESPONSE);
                monitoringResponseMsg.getData().putParcelable(MONITORING_RESULT_KEY, new MonitoringResult(didEnterMonitor1.region, geo2.lbsp.ible.Region.State.OUTSIDE, Collections.<Beacon>emptyList()));
                didEnterMonitor1.replyTo.send(monitoringResponseMsg);
            }
        } catch (RemoteException var7) {
            geo2.lbsp.ible.internal.L.e("Error while delivering responses", var7);
        }
    }

    // important!!!!!!!!!!!! @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ scan된 ble정보를 넘기는 코드.
    private void invokeGatheredBeaconCallback()
    {
    	BLEService.this.checkNotOnUiThread();
        long now = System.currentTimeMillis();

        geo2.lbsp.ible.BeaconScanner.SingleScan singleScan = BLEService.this.beaconScanner.newCycle(now);
        // clear for rescan. by spegel
        BLEService.this.beaconScanner.observations.clear();

        geo2.lbsp.ible.internal.RegionObserver.Observation observation = BLEService.this.rangingObserver.process(singleScan, now);
        Iterator<geo2.lbsp.ible.internal.RangingRegion> e = observation.rangedRegions.iterator();

//        Log.d("ible", "invokeGatheredBeaconCallback : " +singleScan.beacons.size());
        try {
	        Message gatheredBeaconResponseMsg;
	        int cnt = 1;
	        while(e.hasNext()) {
	            geo2.lbsp.ible.internal.RangingRegion didEnterMonitor = e.next();
	            gatheredBeaconResponseMsg = Message.obtain(null, BLEService.MSG_GATHERD_BECONS_RESPONSE);
	            gatheredBeaconResponseMsg.getData().putParcelable(GATHERD_BECONS_RESULT_KEY, new RangingResult(didEnterMonitor.region, didEnterMonitor.getSortedBeacons()));
	            didEnterMonitor.replyTo.send(gatheredBeaconResponseMsg);
	        }

        } catch (RemoteException var7) {
            geo2.lbsp.ible.internal.L.e("Error while delivering responses", var7);
        }
    }

	private void invokeGatheredEstimationBeaconCallback()
	{
		if(BLEService.this.beaconScannerForEstimation == null)
			return;

		BLEService.this.checkNotOnUiThread();
		long now = System.currentTimeMillis();
		geo2.lbsp.ible.BeaconScanner.SingleScan singleScan = BLEService.this.beaconScannerForEstimation.newCycle(now);
		geo2.lbsp.ible.internal.RegionObserver.Observation observation = BLEService.this.rangingObserver.process(singleScan, now);

		Iterator<geo2.lbsp.ible.internal.RangingRegion> e = observation.rangedRegions.iterator();
		try {
			Message gatheredBeaconResponseMsg;
			while(e.hasNext()) {
				geo2.lbsp.ible.internal.RangingRegion didEnterMonitor = e.next();
				gatheredBeaconResponseMsg = Message.obtain(null, BLEService.MSG_GATHERD_ESTIMATION_BECONS_RESPONSE);
				gatheredBeaconResponseMsg.getData().putParcelable(GATHERD_BECONS_RESULT_KEY, new RangingResult(didEnterMonitor.region, didEnterMonitor.getSortedBeacons()));
				didEnterMonitor.replyTo.send(gatheredBeaconResponseMsg);
			}
		} catch (RemoteException var7) {
			geo2.lbsp.ible.internal.L.e("Error while delivering responses", var7);
		}
	}

	private void hookBeaconEstimation()
	{
		beaconScannerForEstimation = new geo2.lbsp.ible.BeaconScanner();
	}

    private ScannerCallback createScannerCallback() {
        return new ScannerCallback() {
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanBytes) {
            	if(serviceMode && !gathering)
            		return;

                BLEService.this.handler.post(new Runnable() {
                    public void run() {
                        long now = System.currentTimeMillis();
                        ScanRecord scanRecord = ScanRecord.parseFromBytes(scanBytes);
                        Beacon beacon = geo2.lbsp.ible.Utils.beaconFromLeScan(device, rssi, scanRecord, now);
                        if(beacon != null) {
                        	BLEService.this.beaconScanner.found(beacon, now);
                        	//Log.d("ible", "(" + BLEService.this.beaconScanner.observations.size() + ")"
                            //                              + beacon.getMacAddress() + ":" + beacon.getRssi());
	                        if(BLEService.this.beaconScannerForEstimation != null)
								BLEService.this.beaconScannerForEstimation.found(beacon, now);
                        }
                    }
                });
            }

            public void onScanCycleCompleted() {
            	if(serviceMode)
            		return;

            	BLEService.this.checkNotOnUiThread();
                long now = System.currentTimeMillis();
                geo2.lbsp.ible.BeaconScanner.SingleScan singleScan = BLEService.this.beaconScanner.newCycle(now);
                geo2.lbsp.ible.internal.RegionObserver.Observation observation = BLEService.this.rangingObserver.process(singleScan, now);
                BLEService.this.invokeCallbacks(observation.enteredRegions, observation.exitedRegions, observation.rangedRegions);
            }

            public void onError(int errorId) {
            	BLEService.this.sendError(Integer.valueOf(errorId));
            }

            public long scanPeriodTimeMillis() {
                return BLEService.this.scanPeriodTimeMillis();
            }

            public long scanWaitTimeMillis() {
                return BLEService.this.scanWaitTimeMillis();
            }
        };
    }
    
    private BroadcastReceiver createBluetoothBroadcastReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                    int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                    if(state == 10) {
                    	BLEService.this.handler.post(new Runnable() {
                            public void run() {
                                geo2.lbsp.ible.internal.L.i("Bluetooth is OFF: stopping scanning");
                                BLEService.this.stopScanning();
                            }
                        });
                    } else if(state == 12) {
                    	BLEService.this.handler.post(new Runnable() {
                            public void run() {
                                if(BLEService.this.isObserving()) {
                                    geo2.lbsp.ible.internal.L.i("Bluetooth is ON: resuming scanning");
                                    BLEService.this.startScanning();
                                }

                            }
                        });
                    }
                }

            }
        };
    }
    
    public BLEService() {
        this.foregroundScanPeriod = new ScanPeriodData(TimeUnit.SECONDS.toMillis(1L), TimeUnit.SECONDS.toMillis(0L));
        this.backgroundScanPeriod = new ScanPeriodData(TimeUnit.SECONDS.toMillis(5L), TimeUnit.SECONDS.toMillis(30L));
    }
    
	@Override
	public IBinder onBind(Intent arg0) {
		return this.messenger.getBinder();
	}

    public void onCreate() {
        super.onCreate();
        geo2.lbsp.ible.internal.L.i("Creating service");
        this.handler = new ThreadedHandler("BeaconServiceThread", 10);
        this.bluetoothScanner = new BluetoothScanner(this.getApplicationContext(), this.handler, this.createScannerCallback());
        this.bluetoothBroadcastReceiver = this.createBluetoothBroadcastReceiver();
        this.registerReceiver(this.bluetoothBroadcastReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
    }

    public void onDestroy() {
        geo2.lbsp.ible.internal.L.i("Service destroyed");
        this.unregisterReceiver(this.bluetoothBroadcastReceiver);
        this.stopScanning();
        this.bluetoothScanner.destroy();
        this.handler.quit();
        super.onDestroy();
    }
    
    private boolean isObserving() {
        return this.rangingObserver.isObserving();
    }

    private boolean isRanging() {
        return this.rangingObserver.isRanging();
    }
    
    private void checkNotOnUiThread() {
        Preconditions.checkArgument(Looper.getMainLooper().getThread() != Thread.currentThread(),
        		"This cannot be run on UI thread, starting BLE scan can be expensive");
        Preconditions.checkNotNull(Boolean.valueOf(this.handler.getLooper() == Looper.myLooper()),
        		"It must be executed on service\'s handlerThread");
    }
    
    private void startScanning() {
        if(!this.isObserving()) {
            geo2.lbsp.ible.internal.L.d("Not starting scanning, no monitored on ranged regions");
        } else {
            if(!this.bluetoothScanner.start()) {
                geo2.lbsp.ible.internal.L.d("Could not start Bluetooth scanning");
            }

        }
    }
    
    private void stopScanning() {
        this.bluetoothScanner.stop();
        this.beaconScanner.clear();
	    if(this.beaconScannerForEstimation != null)
		    this.beaconScannerForEstimation.clear();
    }
    
    private void startService(geo2.lbsp.ible.internal.RangingRegion rangingRegion) {
        this.checkNotOnUiThread();
        geo2.lbsp.ible.internal.L.v("Start ranging: " + rangingRegion.region);
        this.rangingObserver.add(rangingRegion);
        this.startScanning();
    }
    
    private void stopService(String regionId) {
        geo2.lbsp.ible.internal.L.v("Stopping ranging: " + regionId);
        this.checkNotOnUiThread();
        this.rangingObserver.removeByRangingId(regionId);
        if(!this.isObserving()) {
            this.stopScanning();
        }

    }
    
    private void startRanging(geo2.lbsp.ible.internal.RangingRegion rangingRegion) {
        this.checkNotOnUiThread();
        geo2.lbsp.ible.internal.L.v("Start ranging: " + rangingRegion.region);
        this.rangingObserver.add(rangingRegion);
        this.startScanning();
    }

    private void stopRanging(String regionId) {
        geo2.lbsp.ible.internal.L.v("Stopping ranging: " + regionId);
        this.checkNotOnUiThread();
        this.rangingObserver.removeByRangingId(regionId);
        if(!this.isObserving()) {
            this.stopScanning();
        }

    }
    
    private void startMonitoring() {
        this.checkNotOnUiThread();
        geo2.lbsp.ible.internal.L.v("Starting monitoring");

        this.startScanning();
    }

    private void stopMonitoring() {
        geo2.lbsp.ible.internal.L.v("Stopping monitoring");
        this.checkNotOnUiThread();
        if(!this.isObserving()) {
            this.stopScanning();
        }
    }
    
    private void stopMonitoring(String regionId) {
        geo2.lbsp.ible.internal.L.v("Stopping monitoring: " + regionId);
        this.checkNotOnUiThread();
        this.rangingObserver.removeByMonitoringId(regionId);
        if(!this.isObserving()) {
            this.stopScanning();
        }

    }
    
    private void sendError(Integer errorId) {
        if(this.errorReplyTo != null) {
            Message errorMsg = Message.obtain(null, 14);
            errorMsg.getData().putInt(ERROR_ID_KEY, errorId.intValue());

            try {
                this.errorReplyTo.send(errorMsg);
            } catch (RemoteException var4) {
                geo2.lbsp.ible.internal.L.e("Error while reporting message, funny right?", var4);
            }
        }
    }
    
    private long scanPeriodTimeMillis() {
        return this.isRanging()?this.foregroundScanPeriod.scanPeriodMillis:this.backgroundScanPeriod.scanPeriodMillis;
    }

    private long scanWaitTimeMillis() {
        return this.isRanging()?this.foregroundScanPeriod.waitTimeMillis:this.backgroundScanPeriod.waitTimeMillis;
    }
    
    private class IncomingHandler extends Handler {
        private IncomingHandler() {
        }

        public void handleMessage(Message msg) {
            final int what = msg.what;
            final Bundle bundle = msg.getData();
            final Messenger replyTo = msg.replyTo;
            BLEService.this.handler.post(new Runnable() {
                public void run() {
                    switch(what) {
	                    case MSG_START_RANGING:
	                        bundle.setClassLoader(geo2.lbsp.ible.Region.class.getClassLoader());
	                        geo2.lbsp.ible.internal.RangingRegion rangingRegion = new geo2.lbsp.ible.internal.RangingRegion((geo2.lbsp.ible.Region)bundle.getParcelable(REGION_KEY), replyTo);
	                        BLEService.this.startRanging(rangingRegion);
	                        break;
	                    case MSG_STOP_RANGING:
	                    	BLEService.this.stopRanging(bundle.getString(REGION_ID_KEY));
	                        break;
	                    case 3:
	                    case 7:
	                    case 10:
	                    case 12:
	                    case 14:
	                    default:
	                        geo2.lbsp.ible.internal.L.d("Unknown message: what=" + what + " bundle=" + bundle);
	                        break;
	                    case MSG_START_MONITORING:
	                    	BLEService.this.startMonitoring();
	                        break;
	                    case MSG_STOP_MONITORING:
	                        String monitoredRegionId = bundle.getString("regionId");
	                        BLEService.this.stopMonitoring(monitoredRegionId);
	                        break;
	                    case 6:
	                        break;
	                    case 8:
	                        break;
	                    case 9:
	                        break;
	                    case 11:
	                        break;
	                    case MSG_REGISTER_ERROR_LISTENER:
	                    	BLEService.this.errorReplyTo = replyTo;
	                        break;
	                    case MSG_SET_FOREGROUND_SCAN_PERIOD:
	                        bundle.setClassLoader(ScanPeriodData.class.getClassLoader());
	                        BLEService.this.foregroundScanPeriod = bundle.getParcelable(SCAN_PERIOD_KEY);
	                        geo2.lbsp.ible.internal.L.d("Setting foreground scan period: " + BLEService.this.foregroundScanPeriod);
	                        break;
	                    case MSG_SET_BACKGROUND_SCAN_PERIOD:
	                        bundle.setClassLoader(ScanPeriodData.class.getClassLoader());
	                        BLEService.this.backgroundScanPeriod = bundle.getParcelable(SCAN_PERIOD_KEY);
	                        geo2.lbsp.ible.internal.L.d("Setting background scan period: " + BLEService.this.backgroundScanPeriod);
	                        break;
	                    case MSG_START_SERVICE:
	                        bundle.setClassLoader(geo2.lbsp.ible.Region.class.getClassLoader());
	                        geo2.lbsp.ible.internal.RangingRegion serviceRegion = new geo2.lbsp.ible.internal.RangingRegion((geo2.lbsp.ible.Region)bundle.getParcelable(REGION_KEY), replyTo);
	                        BLEService.this.startService(serviceRegion);
	                        BLEService.this.serviceMode = true;
	                    	break;
	                    case MSG_STOP_SERVICE:
	                        String serviceRegionId = bundle.getString("regionId");
	                        BLEService.this.stopService(serviceRegionId);
	                        BLEService.this.serviceMode = false;
	                    	break;
	                    case MSG_BEGIN_GATHERING:
//	                        BLEService.this.gathering = true;
	                    	break;
	                    case MSG_END_GATHERING:
//	                        BLEService.this.gathering = false;
	                    	break;
	                    case MSG_GET_GATHERED_BECONS:
	                    	BLEService.this.invokeGatheredBeaconCallback();
	                    	break;
	                    case MSG_HOOK_GATHERED_BECONS_ESTIMATION:
		                    BLEService.this.hookBeaconEstimation();
		                    break;
	                    case MSG_GET_GATHERED_ESTIMATION_BECONS:
		                    BLEService.this.invokeGatheredEstimationBeaconCallback();
		                    break;

                    }

                }
            });
        }
    }
}
