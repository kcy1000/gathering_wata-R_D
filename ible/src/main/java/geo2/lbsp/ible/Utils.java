package geo2.lbsp.ible;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;

import java.util.Arrays;
import java.util.UUID;

import geo2.lbsp.ible.internal.Preconditions;
import geo2.lbsp.ible.internal.ScanRecord;
import geo2.lbsp.ible.okio.ByteString;


public class Utils {
    private static final ParcelUuid BEACON_SERVICE_DATA = ParcelUuid.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    private static final int APPLE_MANUFACTURER_ID = 76;
    private static final int BOOTLOADER_SERVICE_DATA_LENGTH = 7;
    private static final int MANUFACTURER_SPECIFIC_DATA_LENGTH = 23;

    public Utils() {
    }

    public static Beacon beaconFromLeScan(BluetoothDevice device, int rssi, ScanRecord scanRecord, long timestemp) {
//    	if(scanRecord.getManufacturerSpecificData() != null && scanRecord.getManufacturerSpecificData(76) != null &&
//    			scanRecord.getManufacturerSpecificData(76).length == 23 && scanRecord.getManufacturerSpecificData(76)[0] == 2 &&
//    			scanRecord.getManufacturerSpecificData(76)[1] == 21) {

	    if(scanRecord.getManufacturerSpecificData() != null && scanRecord.getManufacturerSpecificData(76) != null &&
			    scanRecord.getManufacturerSpecificData(76).length >= 21) {
            byte[] manufacturerSpecificData = scanRecord.getManufacturerSpecificData(76);
            byte[] proximityUUIDBytes = Arrays.copyOfRange(manufacturerSpecificData, 2, 18);
            byte[] majorBytes = Arrays.copyOfRange(manufacturerSpecificData, 18, 20);
            byte[] minorBytes = Arrays.copyOfRange(manufacturerSpecificData, 20, 22);
            byte measuredPower = manufacturerSpecificData[manufacturerSpecificData.length - 1];
            String proximityUUID = String.format("%s-%s-%s-%s-%s", ByteString.of(Arrays.copyOfRange(proximityUUIDBytes, 0, 4)).hex(), ByteString.of(Arrays.copyOfRange(proximityUUIDBytes, 4, 6)).hex(), ByteString.of(Arrays.copyOfRange(proximityUUIDBytes, 6, 8)).hex(), ByteString.of(Arrays.copyOfRange(proximityUUIDBytes, 8, 10)).hex(), ByteString.of(Arrays.copyOfRange(proximityUUIDBytes, 10, 16)).hex());
            int major = unsignedByteToInt(majorBytes[0]) * 256 + unsignedByteToInt(majorBytes[1]);
            int minor = unsignedByteToInt(minorBytes[0]) * 256 + unsignedByteToInt(minorBytes[1]);
            return new Beacon(UUID.fromString(proximityUUID), device.getName(), MacAddress.fromString(device.getAddress()), major, minor, measuredPower, rssi, timestemp);
    	}
    	return null;
    	// Remove Moving BLE. 
//       	return new Beacon(null, device.getName(), MacAddress.fromString(device.getAddress()), -1, -1, -1, rssi);
    }

    public static String normalizeProximityUUID(String proximityUUID) {
        String withoutDashes = proximityUUID.replace("-", "").toLowerCase();
        Preconditions.checkArgument(withoutDashes.length() == 32, "Proximity UUID must be 32 characters without dashes");
        return String.format("%s-%s-%s-%s-%s", withoutDashes.substring(0, 8), withoutDashes.substring(8, 12), withoutDashes.substring(12, 16), withoutDashes.substring(16, 20), withoutDashes.substring(20, 32));
    }

    public static boolean isBeaconInRegion(Beacon beacon, Region region) {
        return (region.getProximityUUID() == null || beacon.getProximityUUID().equals(region.getProximityUUID())) && (region.getMajor() == null || beacon.getMajor() == region.getMajor().intValue()) && (region.getMinor() == null || beacon.getMinor() == region.getMinor().intValue());
    }

    public static double computeAccuracy(Beacon beacon) {
        return computeAccuracy(beacon.getRssi(), beacon.getMeasuredPower());
    }

    private static double computeAccuracy(int rssi, int measuredPower) {
        if(rssi == 0) {
            return -1.0D;
        } else {
            double ratio = (double)rssi / (double)measuredPower;
            double rssiCorrection = 0.96D + Math.pow((double)Math.abs(rssi), 3.0D) % 10.0D / 150.0D;
            return ratio <= 1.0D?Math.pow(ratio, 9.98D) * rssiCorrection:(0.103D + 0.89978D * Math.pow(ratio, 7.71D)) * rssiCorrection;
        }
    }

    public static Proximity proximityFromAccuracy(double accuracy) {
        return accuracy < 0.0D ? Proximity.UNKNOWN :
        	(accuracy < 0.5D? Proximity.IMMEDIATE : (accuracy <= 3.0D? Proximity.NEAR: Proximity.FAR));
    }

    public static Proximity computeProximity(Beacon beacon) {
        return proximityFromAccuracy(computeAccuracy(beacon));
    }

    public static int parseInt(String numberAsString) {
        try {
            return Integer.parseInt(numberAsString);
        } catch (NumberFormatException var2) {
            return 0;
        }
    }

    public static void restartBluetooth(Context context, final RestartCompletedListener listener) {
        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService("bluetooth");
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                    int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                     
                    if(state == BluetoothAdapter.STATE_OFF) {
                        adapter.enable();
                    } else if(state == BluetoothAdapter.STATE_ON) {
                        context.unregisterReceiver(this);
                        listener.onRestartCompleted();
                    }
                }

            }
        }, intentFilter);
        adapter.disable();
    }
    
    public static void startBluetooth(Context context, final StartCompletedListener listener) {
        BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService("bluetooth");
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        IntentFilter intentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                    int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
                    if(state == BluetoothAdapter.STATE_ON) {
                    	context.unregisterReceiver(this);
                    	adapter.enable();
                        listener.onStartCompleted();
                    }
                }

            }
        }, intentFilter);
        adapter.enable();
    }

	public static void stopBluetooth(Context context, final StartCompletedListener listener) {
		BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService("bluetooth");
		final BluetoothAdapter adapter = bluetoothManager.getAdapter();
		IntentFilter intentFilter = new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED");
		context.registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
					int state = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1);
					if(state == BluetoothAdapter.STATE_OFF) {
						adapter.disable();
						listener.onStartCompleted();
					}
				}

			}
		}, intentFilter);
		adapter.disable();
	}

    private static int unsignedByteToInt(byte value) {
        return value & 255;
    }

    public enum Proximity {
        UNKNOWN,
        IMMEDIATE,
        NEAR,
        FAR;

        Proximity() {
        }
    }

    public interface RestartCompletedListener {
        void onRestartCompleted();
    }

}