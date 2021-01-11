package geo2.lbsp.ible.internal;

import android.os.Parcel;
import android.os.Parcelable;

public final class ScanPeriodData implements Parcelable {
    public final long scanPeriodMillis;
    public final long waitTimeMillis;
    @SuppressWarnings("unchecked")
	public static final Creator<ScanPeriodData> CREATOR = new Creator() {
        public ScanPeriodData createFromParcel(Parcel source) {
            long scanPeriodMillis = source.readLong();
            long waitTimeMillis = source.readLong();
            return new ScanPeriodData(scanPeriodMillis, waitTimeMillis);
        }

        public ScanPeriodData[] newArray(int size) {
            return new ScanPeriodData[size];
        }
    };

    public ScanPeriodData(long scanPeriodMillis, long waitTimeMillis) {
        this.scanPeriodMillis = scanPeriodMillis;
        this.waitTimeMillis = waitTimeMillis;
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            ScanPeriodData that = (ScanPeriodData)o;
            return this.scanPeriodMillis == that.scanPeriodMillis && this.waitTimeMillis == that.waitTimeMillis;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = (int)(this.scanPeriodMillis ^ this.scanPeriodMillis >>> 32);
        result = 31 * result + (int)(this.waitTimeMillis ^ this.waitTimeMillis >>> 32);
        return result;
    }

    public String toString() {
        return "ScanPeriodData{scanPeriodMillis=" + this.scanPeriodMillis + ", waitTimeMillis=" + this.waitTimeMillis + '}';
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.scanPeriodMillis);
        dest.writeLong(this.waitTimeMillis);
    }
}