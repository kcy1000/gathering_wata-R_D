package geo2.lbsp.ible;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import geo2.lbsp.ible.internal.Objects;
import geo2.lbsp.ible.internal.Preconditions;


public class Region implements Parcelable {
    private final String identifier;
    private final UUID proximityUUID;
    private final Integer major;
    private final Integer minor;
    @SuppressWarnings("unchecked")
	public static final Creator<Region> CREATOR = new Creator() {
        public Region createFromParcel(Parcel source) {
            return new Region(source);
        }

        public Region[] newArray(int size) {
            return new Region[size];
        }
    };

    public Region(String identifier, UUID proximityUUID, Integer major, Integer minor) {
        this.identifier = Preconditions.checkNotNull(identifier);
        this.proximityUUID = proximityUUID;
        this.major = major;
        this.minor = minor;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public UUID getProximityUUID() {
        return this.proximityUUID;
    }

    public Integer getMajor() {
        return this.major;
    }

    public Integer getMinor() {
        return this.minor;
    }

    public String toString() {
        return Objects.toStringHelper(this).add("identifier", this.identifier).add("proximityUUID", this.proximityUUID).add("major", this.major).add("minor", this.minor).toString();
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            Region region;
            label45: {
                region = (Region)o;
                if(this.major != null) {
                    if(this.major.equals(region.major)) {
                        break label45;
                    }
                } else if(region.major == null) {
                    break label45;
                }

                return false;
            }

            label38: {
                if(this.minor != null) {
                    if(this.minor.equals(region.minor)) {
                        break label38;
                    }
                } else if(region.minor == null) {
                    break label38;
                }

                return false;
            }

            if(this.proximityUUID != null) {
                if(this.proximityUUID.equals(region.proximityUUID)) {
                    return true;
                }
            } else if(region.proximityUUID == null) {
                return true;
            }

            return false;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.proximityUUID != null?this.proximityUUID.hashCode():0;
        result = 31 * result + (this.major != null?this.major.hashCode():0);
        result = 31 * result + (this.minor != null?this.minor.hashCode():0);
        return result;
    }

    private Region(Parcel parcel) {
        this.identifier = parcel.readString();
        this.proximityUUID = (UUID)parcel.readValue(UUID.class.getClassLoader());
        Integer majorTemp = Integer.valueOf(parcel.readInt());
        if(majorTemp.intValue() == -1) {
            majorTemp = null;
        }

        this.major = majorTemp;
        Integer minorTemp = Integer.valueOf(parcel.readInt());
        if(minorTemp.intValue() == -1) {
            minorTemp = null;
        }

        this.minor = minorTemp;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.identifier);
        dest.writeValue(this.proximityUUID);
        dest.writeInt(this.major == null?-1:this.major.intValue());
        dest.writeInt(this.minor == null?-1:this.minor.intValue());
    }

    public enum State {
        INSIDE,
        OUTSIDE;

        State() {
        }
    }
}