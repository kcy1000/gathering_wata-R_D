package geo2.lbsp.ible.internal;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class MonitoringResult implements Parcelable {
    public final geo2.lbsp.ible.Region region;
    public final geo2.lbsp.ible.Region.State state;
    public final List<geo2.lbsp.ible.Beacon> beacons;
    public static final Creator<Object> CREATOR = new Creator<Object>() {
    	@Override
        public MonitoringResult createFromParcel(Parcel source) {
            ClassLoader classLoader = this.getClass().getClassLoader();
            geo2.lbsp.ible.Region region = source.readParcelable(classLoader);
            geo2.lbsp.ible.Region.State event = geo2.lbsp.ible.Region.State.values()[source.readInt()];
            ArrayList<geo2.lbsp.ible.Beacon> beacons = source.readArrayList(classLoader);
            return new MonitoringResult(region, event, beacons);
        }

    	@Override
        public MonitoringResult[] newArray(int size) {
            return new MonitoringResult[size];
        }
    };

    public MonitoringResult(geo2.lbsp.ible.Region region, geo2.lbsp.ible.Region.State state, Collection<geo2.lbsp.ible.Beacon> beacons) {
        this.region = Preconditions.checkNotNull(region, "region cannot be null");
        this.state = Preconditions.checkNotNull(state, "state cannot be null");
        this.beacons = new ArrayList<geo2.lbsp.ible.Beacon>(beacons);
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            MonitoringResult that = (MonitoringResult)o;
            return this.state == that.state && this.region.equals(that.region);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.region.hashCode();
        result = 31 * result + this.state.hashCode();
        return result;
    }

    public String toString() {
        return "MonitoringResult{region=" + this.region + ", state=" + this.state + ", beacons=" + this.beacons + '}';
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.region, flags);
        dest.writeInt(this.state.ordinal());
        dest.writeList(this.beacons);
    }
}
