package geo2.lbsp.ible.internal;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class RangingResult implements Parcelable {
    public final geo2.lbsp.ible.Region region;
    public final List<geo2.lbsp.ible.Beacon> beacons;

	public static final Creator<RangingResult> CREATOR = new Creator<RangingResult>() {
    	@Override
        public RangingResult createFromParcel(Parcel source) {
            ClassLoader classLoader = this.getClass().getClassLoader();
            geo2.lbsp.ible.Region region = source.readParcelable(classLoader);
            ArrayList<geo2.lbsp.ible.Beacon> beacons = source.readArrayList(classLoader);
            return new RangingResult(region, beacons);
        }

    	@Override
        public RangingResult[] newArray(int size) {
            return new RangingResult[size];
        }
    };

	public RangingResult(geo2.lbsp.ible.Region region, Collection<geo2.lbsp.ible.Beacon> beacons) {
        this.region = Preconditions.checkNotNull(region, "region cannot be null");
        this.beacons = Collections.unmodifiableList(new ArrayList<geo2.lbsp.ible.Beacon>(Preconditions.checkNotNull(beacons, "beacons cannot be null")));
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            RangingResult that = (RangingResult)o;
            return this.beacons.equals(that.beacons) && this.region.equals(that.region);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.region.hashCode();
        result = 31 * result + this.beacons.hashCode();
        return result;
    }

    public String toString() {
        return "RangingResult{region=" + this.region + ", beacons=" + this.beacons + '}';
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.region, flags);
        dest.writeList(this.beacons);
    }
}
