package geo2.lbsp.ible;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import geo2.lbsp.ible.internal.Objects;


public class Beacon implements Parcelable {
    private final UUID proximityUUID;
    private final String name;
    private final MacAddress macAddress;
    private final int major;
    private final int minor;
    private final int measuredPower;
    private final int rssi;
    private final long timestemp;
    @SuppressWarnings("unchecked")
	public static final Creator<Beacon> CREATOR = new Creator() {
        public Beacon createFromParcel(Parcel source) {
            return new Beacon(source);
        }

        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };

    public Beacon(UUID proximityUUID, String name, MacAddress macAddress, int major, int minor, int measuredPower, int rssi, long timestemp) {
        if(name == null) {
        	name = "";
        }
        this.proximityUUID = proximityUUID;
        this.name = name;
        this.macAddress = macAddress;
        this.major = major;
        this.minor = minor;
        this.measuredPower = measuredPower;
        this.rssi = rssi;
        this.timestemp = timestemp;
    }

    public UUID getProximityUUID() {
        return this.proximityUUID;
    }

    public String getName() {
        return this.name;
    }

    public MacAddress getMacAddress() {
        return this.macAddress;
    }

    public int getMajor() {
        return this.major;
    }

    public int getMinor() {
        return this.minor;
    }

    public int getMeasuredPower() {
        return this.measuredPower;
    }

    public int getRssi() {
        return this.rssi;
    }

    public long getTimestemp() {
        return this.timestemp;
    }

    public String toString() {
        return Objects.toStringHelper(this).add("macAddress", this.macAddress).add("proximityUUID", this.proximityUUID).add("major", this.major).add("minor", this.minor).add("measuredPower", this.measuredPower).add("rssi", this.rssi).add("timestemp", this.timestemp).toString();
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            Beacon beacon = (Beacon)o;
            return this.major == beacon.major && (this.minor == beacon.minor && this.proximityUUID.equals(beacon.proximityUUID));
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.proximityUUID.hashCode();
        result = 31 * result + this.major;
        result = 31 * result + this.minor;
        return result;
    }

    private Beacon(Parcel parcel) {
        this.proximityUUID = (UUID)parcel.readValue(UUID.class.getClassLoader());
        this.name = parcel.readString();
        this.macAddress = (MacAddress)parcel.readValue(MacAddress.class.getClassLoader());
        this.major = parcel.readInt();
        this.minor = parcel.readInt();
        this.measuredPower = parcel.readInt();
        this.rssi = parcel.readInt();
        this.timestemp = parcel.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.proximityUUID);
        dest.writeString(this.name);
        dest.writeValue(this.macAddress);
        dest.writeInt(this.major);
        dest.writeInt(this.minor);
        dest.writeInt(this.measuredPower);
        dest.writeInt(this.rssi);
        dest.writeLong(this.timestemp);
    }
}