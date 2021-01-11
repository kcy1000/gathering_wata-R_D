package geo2.lbsp.ible;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import geo2.lbsp.ible.internal.Preconditions;
import geo2.lbsp.ible.internal.ScanPeriodData;

/*
 *  AndroidManifest.xml 
 *  
 *  permission
 *  <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH" />
    
    service
 *  <service android:name="BLEService" >
		</service>
 */
public class BLEManager {
	private Context context = null;
	private final InternalServiceConnection serviceConnection;
	private final Messenger incomingMessenger;
	private Messenger serviceMessenger;
	private ErrorListener errorListener;
    private ScanPeriodData foregroundScanPeriod;
    private ScanPeriodData backgroundScanPeriod;
    private ServiceReadyCallback callback;
    private final Set<String> rangedRegionIds;
    private final Set<String> monitoredRegionIds;
    private RangingListener rangingListener;
    private MonitoringListener monitoringListener;
    private Region serviceRegion = null;
    private List<Beacon> resultBecons = new ArrayList<Beacon>();
	private List<Beacon> resultBeconsEstimate = null;
    private boolean waitForMessage = false;
	private boolean waitEstimationForMessage = false;
	private boolean connected = false;

	public BLEManager(Context context) {
        this.context = Preconditions.checkNotNull(context).getApplicationContext();
        this.serviceConnection = new InternalServiceConnection();
        this.incomingMessenger = new Messenger(new IncomingHandler());
        this.rangedRegionIds = new HashSet<String>();
        this.monitoredRegionIds = new HashSet<String>();
    }

    public boolean isBluetoothEnabled() {
        if(!this.checkPermissionsAndService()) {
            geo2.lbsp.ible.internal.L.e("AndroidManifest.xml does not contain android.permission.BLUETOOTH or android.permission.BLUETOOTH_ADMIN permissions. BeaconService may be also not declared in AndroidManifest.xml.");
            return false;
        } else {
            BluetoothManager bluetoothManager = (BluetoothManager)this.context.getSystemService("bluetooth");
            BluetoothAdapter adapter = bluetoothManager.getAdapter();
            return adapter != null && adapter.isEnabled();
        }
    }

    public boolean checkPermissionsAndService() {
        PackageManager pm = this.context.getPackageManager();
        int bluetoothPermission = pm.checkPermission("android.permission.BLUETOOTH", this.context.getPackageName());
        int bluetoothAdminPermission = pm.checkPermission("android.permission.BLUETOOTH_ADMIN", this.context.getPackageName());
        Intent intent = new Intent(this.context, geo2.lbsp.ible.service.BLEService.class);
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return bluetoothPermission == 0 && bluetoothAdminPermission == 0 && resolveInfo.size() > 0;
    }

    public void connect(ServiceReadyCallback callback) {
	    connected = false;
        if(!this.checkPermissionsAndService()) {
            geo2.lbsp.ible.internal.L.e("AndroidManifest.xml does not contain android.permission.BLUETOOTH or android.permission.BLUETOOTH_ADMIN permissions. BeaconService may be also not declared in AndroidManifest.xml.");
	        return;
        }

        this.callback = Preconditions.checkNotNull(callback, "callback cannot be null");
        if(this.isConnectedToService()) {
	        callback.onServiceReady();
        }
        boolean bound = this.context.bindService(new Intent(this.context, geo2.lbsp.ible.service.BLEService.class), this.serviceConnection, Context.BIND_AUTO_CREATE);
        if(!bound) {
            geo2.lbsp.ible.internal.L.w("Could not bind service: make sure thatcom.estimote.sdk.service.BeaconService is declared in AndroidManifest.xml");
	        return;
        }
    }

    public void startRanging(Region region) {
        if(!this.isConnectedToService()) {
            geo2.lbsp.ible.internal.L.i("Not starting ranging, not connected to service");
        } else {
            Preconditions.checkNotNull(region, "region cannot be null");

            if(this.rangedRegionIds.contains(region.getIdentifier())) {
                geo2.lbsp.ible.internal.L.i("Region already ranged but that\'s OK: " + region);
            }

            this.rangedRegionIds.add(region.getIdentifier());
            Message startRangingMsg = Message.obtain(null, 1);
            startRangingMsg.getData().putParcelable(geo2.lbsp.ible.service.BLEService.REGION_KEY, region);
            startRangingMsg.replyTo = this.incomingMessenger;

            try {
	            if(this.serviceMessenger == null)
		            return;
                this.serviceMessenger.send(startRangingMsg);
            } catch (RemoteException var4) {
                geo2.lbsp.ible.internal.L.e("Error while starting ranging", var4);
            }

        }
    }

    public void stopRanging(Region region) {
        if(!this.isConnectedToService()) {
            geo2.lbsp.ible.internal.L.i("Not stopping ranging, not connected to service");
        } else {
            Preconditions.checkNotNull(region, "region cannot be null");
            this.internalStopRanging(region.getIdentifier());
        }
    }

    private void internalStopRanging(String regionId) {
        this.rangedRegionIds.remove(regionId);
        Message stopRangingMsg = Message.obtain(null, geo2.lbsp.ible.service.BLEService.MSG_STOP_RANGING);
        stopRangingMsg.getData().putString("regionId", regionId);

        try {
	        if(this.serviceMessenger == null)
		        return;
            this.serviceMessenger.send(stopRangingMsg);
        } catch (RemoteException var4) {
            geo2.lbsp.ible.internal.L.e("Error while stopping ranging", var4);
        }
    }

    public void startService()
    {
    	if(serviceRegion == null)
    		serviceRegion = new Region("ServiceRegion", null, null, null);

    	if(rangedRegionIds.contains(serviceRegion))
    		rangedRegionIds.remove(serviceRegion);

        if(!this.isConnectedToService()) {
            geo2.lbsp.ible.internal.L.i("Not starting service, not connected to service");
        } else {
            this.rangedRegionIds.add(serviceRegion.getIdentifier());
            Message startRangingMsg = Message.obtain(null, geo2.lbsp.ible.service.BLEService.MSG_START_SERVICE);
            startRangingMsg.getData().putParcelable(geo2.lbsp.ible.service.BLEService.REGION_KEY, serviceRegion);
            startRangingMsg.replyTo = this.incomingMessenger;

            try {
	            if(this.serviceMessenger == null)
		            return;
                this.serviceMessenger.send(startRangingMsg);
            } catch (RemoteException var4) {
                geo2.lbsp.ible.internal.L.e("Error while starting ranging", var4);
            }
        }
	    connected = true;
    }

    public void stopService() {
        this.rangedRegionIds.remove("ServiceRegion");
        Message stopRangingMsg = Message.obtain(null, geo2.lbsp.ible.service.BLEService.MSG_STOP_SERVICE);
        stopRangingMsg.getData().putString("regionId", "ServiceRegion");

        try {
	        if(this.serviceMessenger == null)
		        return;
            this.serviceMessenger.send(stopRangingMsg);
        } catch (RemoteException var4) {
            geo2.lbsp.ible.internal.L.e("Error while stopping service", var4);
        }
	    connected = false;
    }

    public void startScan()
    {
        Message beginGatheringMsg = Message.obtain(null, geo2.lbsp.ible.service.BLEService.MSG_BEGIN_GATHERING);
        try {
	        if(this.serviceMessenger == null)
		        return;
            this.serviceMessenger.send(beginGatheringMsg);
        } catch (RemoteException var4) {
            geo2.lbsp.ible.internal.L.e("Error while begin gathering", var4);
        }
    }

	public void hookBeaconEstimateResult()
	{
		Message hookBeaconEstimationMsg = Message.obtain(null, geo2.lbsp.ible.service.BLEService.MSG_HOOK_GATHERED_BECONS_ESTIMATION);
		try {
			if(this.serviceMessenger == null)
				return;
			this.serviceMessenger.send(hookBeaconEstimationMsg);
		} catch (RemoteException var4) {
			geo2.lbsp.ible.internal.L.e("Error while begin gathering", var4);
		}

		resultBeconsEstimate = new ArrayList<Beacon>();
	}

	public List<Beacon> getEstimationScanResults()
	{
		stopScan();
		BLEManager.this.resultBeconsEstimate.clear();
		BLEManager.this.waitEstimationForMessage = true;

//        Message beginGatheringMsg = Message.obtain(new Handler() {
//        	@Override
//        	public void handleMessage(Message msg)
//        	{
//                if(msg.what == BLEService.MSG_GATHERD_BECONS_RESPONSE) {
//                    msg.getData().setClassLoader(RangingResult.class.getClassLoader());
//                    RangingResult gatherd = (RangingResult)msg.getData().getParcelable(BLEService.GATHERD_BECONS_RESULT_KEY);
//                    BLEManager.this.resultBecons.addAll(gatherd.beacons);
//                    BLEManager.this.waitForMessage = false;
//                }
//    		}
//    	}, BLEService.MSG_GET_GATHERED_BECONS);
		try {
			Message beginGatheringMsg = Message.obtain(null, geo2.lbsp.ible.service.BLEService.MSG_GET_GATHERED_ESTIMATION_BECONS);
			if(this.serviceMessenger != null)
				this.serviceMessenger.send(beginGatheringMsg);
		} catch (RemoteException var4) {
			geo2.lbsp.ible.internal.L.e("Error while begin end gathering", var4);
		}

		try {
			while(waitEstimationForMessage)
				Thread.sleep(10);
		} catch(Exception e) {

		}

		return resultBeconsEstimate;
	}

    public List<Beacon> getScanResults()
    {
//    	stopScan();
        BLEManager.this.resultBecons.clear();
        BLEManager.this.waitForMessage = true;

//        Message beginGatheringMsg = Message.obtain(new Handler() {
//        	@Override
//        	public void handleMessage(Message msg)
//        	{
//                if(msg.what == BLEService.MSG_GATHERD_BECONS_RESPONSE) {
//                    msg.getData().setClassLoader(RangingResult.class.getClassLoader());
//                    RangingResult gatherd = (RangingResult)msg.getData().getParcelable(BLEService.GATHERD_BECONS_RESULT_KEY);
//                    BLEManager.this.resultBecons.addAll(gatherd.beacons);
//                    BLEManager.this.waitForMessage = false;
//                }
//    		}
//    	}, BLEService.MSG_GET_GATHERED_BECONS);
        try {
			Message beginGatheringMsg = Message.obtain(null, geo2.lbsp.ible.service.BLEService.MSG_GET_GATHERED_BECONS);
	        if(this.serviceMessenger != null)
				this.serviceMessenger.send(beginGatheringMsg);
        } catch (RemoteException var4) {
            geo2.lbsp.ible.internal.L.e("Error while begin end gathering", var4);
        }

        try {
	        while(waitForMessage)
	        	Thread.sleep(10);
        } catch(Exception e) {

        }

    	return resultBecons;
    }

    public void stopScan()
    {
        Message beginGatheringMsg = Message.obtain(null, geo2.lbsp.ible.service.BLEService.MSG_END_GATHERING);
        try {
	        if(this.serviceMessenger == null)
		        return;
            this.serviceMessenger.send(beginGatheringMsg);
        } catch (RemoteException var4) {
            geo2.lbsp.ible.internal.L.e("Error while begin end gathering", var4);
        }
    }

    private class InternalServiceConnection implements ServiceConnection {
        private InternalServiceConnection() {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        	BLEManager.this.serviceMessenger = new Messenger(service);
            if(BLEManager.this.errorListener != null) {
            	BLEManager.this.registerErrorListenerInService();
            }

            if(BLEManager.this.foregroundScanPeriod != null) {
            	BLEManager.this.setScanPeriod(BLEManager.this.foregroundScanPeriod, 15);
            	BLEManager.this.foregroundScanPeriod = null;
            }

            if(BLEManager.this.backgroundScanPeriod != null) {
            	BLEManager.this.setScanPeriod(BLEManager.this.backgroundScanPeriod, 16);
            	BLEManager.this.backgroundScanPeriod = null;
            }

            if(BLEManager.this.callback != null) {
            	BLEManager.this.callback.onServiceReady();
            	BLEManager.this.callback = null;
            }

        }

        public void onServiceDisconnected(ComponentName name) {
            geo2.lbsp.ible.internal.L.e("Service disconnected, crashed? " + name);
            BLEManager.this.serviceMessenger = null;
        }
    }

    private void registerErrorListenerInService() {
        Message registerMsg = Message.obtain(null, 13);
        registerMsg.replyTo = this.incomingMessenger;

        try {
	        if(this.serviceMessenger == null)
		        return;
            this.serviceMessenger.send(registerMsg);
        } catch (RemoteException var3) {
            geo2.lbsp.ible.internal.L.e("Error while registering error listener");
        }

    }

    private void setScanPeriod(ScanPeriodData scanPeriodData, int msgId) {
        Message scanPeriodMsg = Message.obtain(null, msgId);
        scanPeriodMsg.getData().putParcelable("scanPeriod", scanPeriodData);

        try {
	        if(this.serviceMessenger == null)
		        return;
            this.serviceMessenger.send(scanPeriodMsg);
        } catch (RemoteException var5) {
            geo2.lbsp.ible.internal.L.e("Error while setting scan periods: " + msgId);
        }

    }

    private boolean isConnectedToService() {
        return this.serviceMessenger != null;
    }

	public boolean isConnected() {
		return connected;
	}

    public interface ErrorListener {
        void onError(Integer var1);
    }

//    public interface ServiceReadyCallback {
//        void onServiceReady();
//    }

    public interface RangingListener {
        void onBeaconsDiscovered(Region var1, List<Beacon> var2);
    }

    public interface MonitoringListener {
        void onEnteredRegion(Region var1, List<Beacon> var2);

        void onExitedRegion(Region var1);
    }

    public void setRangingListener(RangingListener listener) {
        this.rangingListener = Preconditions.checkNotNull(listener, "listener cannot be null");
    }

    public void setMonitoringListener(MonitoringListener listener) {
        this.monitoringListener = Preconditions.checkNotNull(listener, "listener cannot be null");
    }

    private class IncomingHandler extends Handler {
        private IncomingHandler() {
        }

        public void handleMessage(Message msg) {
            switch(msg.what) {
	            case geo2.lbsp.ible.service.BLEService.MSG_RANGING_RESPONSE:
	                if(BLEManager.this.rangingListener != null) {
	                    msg.getData().setClassLoader(geo2.lbsp.ible.internal.RangingResult.class.getClassLoader());
	                    geo2.lbsp.ible.internal.RangingResult eddystones3 = msg.getData().getParcelable(geo2.lbsp.ible.service.BLEService.RANGING_RESULT_KEY);
	                    BLEManager.this.rangingListener.onBeaconsDiscovered(eddystones3.region, eddystones3.beacons);
	                }
	                break;
	            case 4:
	            case 5:
	            case 6:
	            case 8:
	            case 9:
	            case 11:
	            case 13:
	            default:
	                geo2.lbsp.ible.internal.L.d("Unknown message: " + msg);
	                break;
	            case 7:
	                break;
	            case 10:
	                break;
	            case geo2.lbsp.ible.service.BLEService.MSG_MONITORING_RESPONSE:
	                if(BLEManager.this.monitoringListener != null) {
	                    msg.getData().setClassLoader(geo2.lbsp.ible.internal.MonitoringResult.class.getClassLoader());
	                    geo2.lbsp.ible.internal.MonitoringResult eddystones1 = msg.getData().getParcelable(geo2.lbsp.ible.service.BLEService.MONITORING_RESULT_KEY);
	                    if(eddystones1.state == Region.State.INSIDE) {
	                    	BLEManager.this.monitoringListener.onEnteredRegion(eddystones1.region, eddystones1.beacons);
	                    } else {
	                    	BLEManager.this.monitoringListener.onExitedRegion(eddystones1.region);
	                    }
	                }
	                break;
	            case geo2.lbsp.ible.service.BLEService.MSG_ERROR_RESPONSE:
	                if(BLEManager.this.errorListener != null) {
	                    Integer eddystones = Integer.valueOf(msg.getData().getInt("errorId"));
	                    BLEManager.this.errorListener.onError(eddystones);
	                }
		            break;
	            case geo2.lbsp.ible.service.BLEService.MSG_GATHERD_BECONS_RESPONSE: { // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ 비콘 메시지 받는곳
		            msg.getData().setClassLoader(geo2.lbsp.ible.internal.RangingResult.class.getClassLoader());
		            geo2.lbsp.ible.internal.RangingResult gatherd = msg.getData().getParcelable(geo2.lbsp.ible.service.BLEService.GATHERD_BECONS_RESULT_KEY);

		            if (BLEManager.this.resultBecons != null) {
                        BLEManager.this.resultBecons.addAll(gatherd.beacons);
                        Log.d("ible", "resultBeacons size : " + BLEManager.this.resultBecons.size());
                    }

		            BLEManager.this.waitForMessage = false;
	            }
		            break;
	            case geo2.lbsp.ible.service.BLEService.MSG_GATHERD_ESTIMATION_BECONS_RESPONSE: {
		            msg.getData().setClassLoader(geo2.lbsp.ible.internal.RangingResult.class.getClassLoader());
		            geo2.lbsp.ible.internal.RangingResult gatherd = msg.getData().getParcelable(geo2.lbsp.ible.service.BLEService.GATHERD_BECONS_RESULT_KEY);

		            if (BLEManager.this.resultBeconsEstimate != null)
			            BLEManager.this.resultBeconsEstimate.addAll(gatherd.beacons);

		            BLEManager.this.waitEstimationForMessage = false;
	            }
		            break;
            }

        }
    }
}


