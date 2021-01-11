package geo2.lbsp.ible.internal;


import android.bluetooth.BluetoothAdapter;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class BluetoothLeUtils {
    public BluetoothLeUtils() {
    }

    static String toString(SparseArray<byte[]> array) {
        if(array == null) {
            return "null";
        } else if(array.size() == 0) {
            return "{}";
        } else {
            StringBuilder buffer = new StringBuilder();
            buffer.append('{');

            for(int i = 0; i < array.size(); ++i) {
                buffer.append(array.keyAt(i)).append("=").append(Arrays.toString(array.valueAt(i)));
            }

            buffer.append('}');
            return buffer.toString();
        }
    }

    static <T> String toString(Map<T, byte[]> map) {
        if(map == null) {
            return "null";
        } else if(map.isEmpty()) {
            return "{}";
        } else {
            StringBuilder buffer = new StringBuilder();
            buffer.append('{');
            Iterator it = map.entrySet().iterator();

            while(it.hasNext()) {
                Entry entry = (Entry)it.next();
                Object key = entry.getKey();
                buffer.append(key).append("=").append(Arrays.toString(map.get(key)));
                if(it.hasNext()) {
                    buffer.append(", ");
                }
            }

            buffer.append('}');
            return buffer.toString();
        }
    }

    static boolean equals(SparseArray<byte[]> array, SparseArray<byte[]> otherArray) {
        if(array == otherArray) {
            return true;
        } else if(array != null && otherArray != null) {
            if(array.size() != otherArray.size()) {
                return false;
            } else {
                for(int i = 0; i < array.size(); ++i) {
                    if(array.keyAt(i) != otherArray.keyAt(i) || !Arrays.equals(array.valueAt(i), otherArray.valueAt(i))) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    static void checkAdapterStateOn(BluetoothAdapter adapter) {
        if(adapter == null || adapter.getState() != 12) {
            throw new IllegalStateException("BT Adapter is not turned ON");
        }
    }
}
