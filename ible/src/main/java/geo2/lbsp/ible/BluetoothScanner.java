package geo2.lbsp.ible;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

import geo2.lbsp.ible.internal.Preconditions;
import geo2.lbsp.ible.internal.ThreadedHandler;

public class BluetoothScanner {
    private static final String SCAN_START_ACTION_NAME = "startScan";
    private static final String AFTER_SCAN_ACTION_NAME = "afterScan";
    private static final Intent SCAN_START_INTENT = new Intent("startScan");
    private static final Intent AFTER_SCAN_INTENT = new Intent("afterScan");
    private BluetoothAdapter adapter = null;
    private AlarmManager alarmManager = null;
    private ScannerCallback callback = null;
    private LeScanCallback leScanCallback = null;
    private Context context = null;
    private ThreadedHandler handler = null;
    private Runnable afterScanCycleTask = null;
    private BroadcastReceiver scanStartBroadcastReceiver;
    private PendingIntent scanStartBroadcastPendingIntent;
    private BroadcastReceiver afterScanBroadcastReceiver;
    private PendingIntent afterScanBroadcastPendingIntent;
    private boolean isScanning;

    private android.bluetooth.le.ScanCallback leScanCallback21 = null;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class InternalLeScanCallback21 extends android.bluetooth.le.ScanCallback
    {
        private InternalLeScanCallback21() {
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);
            BluetoothScanner.this.callback.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
        }


        @Override
        public void onScanFailed(int errorCode)
        {
            BluetoothScanner.this.isScanning = false;
            geo2.lbsp.ible.internal.L.wtf("Bluetooth adapter did not start le scan");
            BluetoothScanner.this.callback.onError(-1);
            super.onScanFailed(errorCode);
        }


        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            super.onBatchScanResults(results);
        }
    }

    private class InternalLeScanCallback implements LeScanCallback {
        private InternalLeScanCallback() {
        }

        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            BluetoothScanner.this.callback.onLeScan(device, rssi, scanRecord);
        }
    }

    public interface ScannerCallback {
        void onLeScan(BluetoothDevice var1, int var2, byte[] var3);

        void onScanCycleCompleted();

        void onError(int var1);

        long scanPeriodTimeMillis();

        long scanWaitTimeMillis();
    }

    private Runnable createCycleTask(final ScannerCallback callback) {
        return new Runnable() {
            public void run() {
                BluetoothScanner.this.stop();
                callback.onScanCycleCompleted();
                BluetoothScanner.this.start();
//                if(callback.scanWaitTimeMillis() == 0L) {
//                    BluetoothScanner.this.start();
//                } else {
//                    BluetoothScanner.this.setAlarm(BluetoothScanner.this.scanStartBroadcastPendingIntent, callback.scanWaitTimeMillis());
//                }

            }
        };
    }

    public BluetoothScanner(Context context, ThreadedHandler handler, ScannerCallback callback) {
        this.context = Preconditions.checkNotNull(context, "context == null");
        this.callback = Preconditions.checkNotNull(callback, "callback == null");
        this.handler = Preconditions.checkNotNull(handler, "handler == null");
        this.adapter = ((BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        this.alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        this.leScanCallback = new InternalLeScanCallback();
        this.afterScanCycleTask = this.createCycleTask(callback);
        this.afterScanBroadcastPendingIntent = PendingIntent.getBroadcast(context, 0, AFTER_SCAN_INTENT, 0);
        this.scanStartBroadcastPendingIntent = PendingIntent.getBroadcast(context, 0, SCAN_START_INTENT, 0);
        this.scanStartBroadcastReceiver = this.createScanStartBroadcastReceiver();
        this.afterScanBroadcastReceiver = this.createAfterScanBroadcastReceiver();
        context.registerReceiver(this.scanStartBroadcastReceiver, new IntentFilter("startScan"));
        context.registerReceiver(this.afterScanBroadcastReceiver, new IntentFilter("afterScan"));

        this.leScanCallback21 = new InternalLeScanCallback21();
    }
    
    private BroadcastReceiver createAfterScanBroadcastReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                BluetoothScanner.this.handler.post(BluetoothScanner.this.afterScanCycleTask);
            }
        };
    }

    private BroadcastReceiver createScanStartBroadcastReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                BluetoothScanner.this.handler.post(new Runnable() {
                    public void run() {
                        BluetoothScanner.this.start();
                    }
                });
            }
        };
    }

    @SuppressLint("MissingPermission")
    public boolean start() {
        if(this.isScanning) {
            geo2.lbsp.ible.internal.L.d("Scanning already in progress, not starting one more");
            return false;
        } else if(!this.adapter.isEnabled()) {
            geo2.lbsp.ible.internal.L.d("Bluetooth is disabled, not starting scanning");
            return false;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if(!this.adapter.startLeScan(this.leScanCallback)) {
                geo2.lbsp.ible.internal.L.wtf("Bluetooth adapter did not start le scan");
                this.callback.onError(-1);
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY ).build();
            this.adapter.getBluetoothLeScanner().startScan(null, scanSettings, this.leScanCallback21);
        }
        this.isScanning = true;
        this.removeAfterScanCycleCallback();
        //this.setAlarm(this.afterScanBroadcastPendingIntent, this.callback.scanPeriodTimeMillis());
        return true;
    }

    // ble scan 이전 버젼용
    @SuppressLint("MissingPermission")
    public boolean start_() {
        if(this.isScanning) {
            geo2.lbsp.ible.internal.L.d("Scanning already in progress, not starting one more");
            return false;
        } else if(!this.adapter.isEnabled()) {
            geo2.lbsp.ible.internal.L.d("Bluetooth is disabled, not starting scanning");
            return false;
        } else if(!this.adapter.startLeScan(this.leScanCallback)) {
                geo2.lbsp.ible.internal.L.wtf("Bluetooth adapter did not start le scan");
                this.callback.onError(-1);
                return false;
        }
        this.isScanning = true;
        this.removeAfterScanCycleCallback();
        //this.setAlarm(this.afterScanBroadcastPendingIntent, this.callback.scanPeriodTimeMillis());
        return true;
    }

    @SuppressLint("MissingPermission")
    public void stop() {
        try {
            this.isScanning = false;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                this.adapter.stopLeScan(this.leScanCallback);
            } else {
                this.adapter.getBluetoothLeScanner().stopScan(this.leScanCallback21);
            }
        } catch (Exception var2) {
            geo2.lbsp.ible.internal.L.wtf("BluetoothAdapter throws unexpected exception", var2);
        }

        this.removeAfterScanCycleCallback();
    }
    
    public void destroy() {
        this.context.unregisterReceiver(this.scanStartBroadcastReceiver);
        this.context.unregisterReceiver(this.afterScanBroadcastReceiver);
        this.removeAfterScanCycleCallback();
    }
    
    private void removeAfterScanCycleCallback() {
        this.handler.removeCallbacks(this.afterScanCycleTask);
        this.alarmManager.cancel(this.afterScanBroadcastPendingIntent);
        this.alarmManager.cancel(this.scanStartBroadcastPendingIntent);
    }
    
    private void setAlarm(PendingIntent pendingIntent, long delayMillis) {
        this.alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delayMillis, pendingIntent);
    }
}
