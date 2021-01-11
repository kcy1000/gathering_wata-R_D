package geo2.lbsp.ible.internal;

import android.os.ParcelUuid;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ScanRecord {
    private static final String TAG = "ScanRecord1";
    private static final int DATA_TYPE_FLAGS = 1;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 2;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 3;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 4;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 5;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 6;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 7;
    private static final int DATA_TYPE_LOCAL_NAME_SHORT = 8;
    private static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 9;
    private static final int DATA_TYPE_TX_POWER_LEVEL = 10;
    private static final int DATA_TYPE_SERVICE_DATA = 22;
    private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 255;
    private final int mAdvertiseFlags;
    private final List<ParcelUuid> mServiceUuids;
    private final SparseArray<byte[]> mManufacturerSpecificData;
    private final Map<ParcelUuid, byte[]> mServiceData;
    private final int mTxPowerLevel;
    private final String mDeviceName;
    private final byte[] mBytes;

    public int getAdvertiseFlags() {
        return this.mAdvertiseFlags;
    }

    public List<ParcelUuid> getServiceUuids() {
        return this.mServiceUuids;
    }

    public SparseArray<byte[]> getManufacturerSpecificData() {
        return this.mManufacturerSpecificData;
    }

    public byte[] getManufacturerSpecificData(int manufacturerId) {
        return this.mManufacturerSpecificData.get(manufacturerId);
    }

    public Map<ParcelUuid, byte[]> getServiceData() {
        return this.mServiceData;
    }

    public byte[] getServiceData(ParcelUuid serviceDataUuid) {
        return serviceDataUuid == null?null: this.mServiceData.get(serviceDataUuid);
    }

    public int getTxPowerLevel() {
        return this.mTxPowerLevel;
    }

    public String getDeviceName() {
        return this.mDeviceName;
    }

    public byte[] getBytes() {
        return this.mBytes;
    }

    private ScanRecord(List<ParcelUuid> serviceUuids, SparseArray<byte[]> manufacturerData, Map<ParcelUuid, byte[]> serviceData, int advertiseFlags, int txPowerLevel, String localName, byte[] bytes) {
        this.mServiceUuids = serviceUuids;
        this.mManufacturerSpecificData = manufacturerData;
        this.mServiceData = serviceData;
        this.mDeviceName = localName;
        this.mAdvertiseFlags = advertiseFlags;
        this.mTxPowerLevel = txPowerLevel;
        this.mBytes = bytes;
    }

    @SuppressWarnings("unchecked")
	public static ScanRecord parseFromBytes(byte[] scanRecord) {
        if(scanRecord == null) {
            return null;
        } else {
            int currentPos = 0;
            int advertiseFlag = -1;
            ArrayList<ParcelUuid> serviceUuids = new ArrayList<ParcelUuid>();
            String localName = null;
            int txPowerLevel = -2147483648;
            SparseArray<byte[]> manufacturerData = new SparseArray<byte[]>();
            HashMap<ParcelUuid, byte[]> serviceData = new HashMap<ParcelUuid, byte[]>();

            try {
                int dataLength;
                for(; currentPos < scanRecord.length; currentPos += dataLength) {
                    int e = scanRecord[currentPos++] & 255;
                    if(e == 0) {
                        break;
                    }

                    dataLength = e - 1;
                    int fieldType = scanRecord[currentPos++] & 255;
                    switch(fieldType) {
                    case 1:
                        advertiseFlag = scanRecord[currentPos] & 255;
                        break;
                    case 2:
                    case 3:
                        parseServiceUuid(scanRecord, currentPos, dataLength, 2, serviceUuids);
                        break;
                    case 4:
                    case 5:
                        parseServiceUuid(scanRecord, currentPos, dataLength, 4, serviceUuids);
                        break;
                    case 6:
                    case 7:
                        parseServiceUuid(scanRecord, currentPos, dataLength, 16, serviceUuids);
                        break;
                    case 8:
                    case 9:
                        localName = new String(extractBytes(scanRecord, currentPos, dataLength));
                        break;
                    case 10:
                        txPowerLevel = scanRecord[currentPos];
                        break;
                    case 22:
                        byte serviceUuidLength = 2;
                        byte[] serviceDataUuidBytes = extractBytes(scanRecord, currentPos, serviceUuidLength);
                        ParcelUuid serviceDataUuid = BluetoothUuid.parseUuidFrom(serviceDataUuidBytes);
                        byte[] serviceDataArray = extractBytes(scanRecord, currentPos + serviceUuidLength, dataLength - serviceUuidLength);
                        serviceData.put(serviceDataUuid, serviceDataArray);
                        break;
                    case 255:
                        int manufacturerId = ((scanRecord[currentPos + 1] & 255) << 8) + (scanRecord[currentPos] & 255);
                        byte[] manufacturerDataBytes = extractBytes(scanRecord, currentPos + 2, dataLength - 2);
                        manufacturerData.put(manufacturerId, manufacturerDataBytes);
                    }
                }

                if(serviceUuids.isEmpty()) {
                    serviceUuids = null;
                }

                return new ScanRecord(serviceUuids, manufacturerData, serviceData, advertiseFlag, txPowerLevel, localName, scanRecord);
            } catch (Exception var17) {
                Log.e("ScanRecord", "unable to parse scan record: " + Arrays.toString(scanRecord));
                return new ScanRecord(null, null, null, -1, -2147483648, null, scanRecord);
            }
        }
    }

    public String toString() {
        return "ScanRecord [mAdvertiseFlags=" + this.mAdvertiseFlags + ", mServiceUuids=" + this.mServiceUuids + ", mManufacturerSpecificData=" + BluetoothLeUtils.toString(this.mManufacturerSpecificData) + ", mServiceData=" + BluetoothLeUtils.toString(this.mServiceData) + ", mTxPowerLevel=" + this.mTxPowerLevel + ", mDeviceName=" + this.mDeviceName + "]";
    }

    private static int parseServiceUuid(byte[] scanRecord, int currentPos, int dataLength, int uuidLength, List<ParcelUuid> serviceUuids) {
        while(dataLength > 0) {
            byte[] uuidBytes = extractBytes(scanRecord, currentPos, uuidLength);
            serviceUuids.add(BluetoothUuid.parseUuidFrom(uuidBytes));
            dataLength -= uuidLength;
            currentPos += uuidLength;
        }

        return currentPos;
    }

    public static byte[] extractBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }
}
