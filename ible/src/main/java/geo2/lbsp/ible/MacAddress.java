package geo2.lbsp.ible;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

import geo2.lbsp.ible.internal.Preconditions;
import geo2.lbsp.ible.okio.ByteString;

public class MacAddress implements Parcelable {
    private final byte[] address;
    @SuppressWarnings("unchecked")
	public static final Creator<MacAddress> CREATOR = new Creator() {
        public MacAddress createFromParcel(Parcel in) {
            byte[] address = new byte[in.readInt()];
            in.readByteArray(address);
            return new MacAddress(address);
        }

        public MacAddress[] newArray(int size) {
            return new MacAddress[size];
        }
    };

    private MacAddress(byte[] address) {
        Preconditions.checkNotNull(address);
        Preconditions.checkArgument(address.length >= 6, "MAC must be at least 6 bytes long");
        this.address = new byte[address.length];
        System.arraycopy(address, 0, this.address, 0, address.length);
    }

    public static MacAddress fromString(String macAddress) {
        Preconditions.checkNotNull(macAddress);
        String noColons = macAddress.replaceAll(":|-|\\.", "");
        Preconditions.checkArgument(macAddress.length() / 2 >= 6, "MAC address must be at least 6 bytes long");
        return new MacAddress(ByteString.decodeHex(noColons.toLowerCase()).toByteArray());
    }

    public static MacAddress fromBytes(byte[] address) {
        return new MacAddress(address);
    }

    public byte[] toBytes() {
        byte[] returnedAddress = new byte[this.address.length];
        System.arraycopy(this.address, 0, returnedAddress, 0, this.address.length);
        return returnedAddress;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append(String.format("%02X", new Object[]{Byte.valueOf(this.address[0])}));

        for(int i = 1; i < this.address.length; ++i) {
            sb.append(':');
            sb.append(String.format("%02X", new Object[]{Byte.valueOf(this.address[i])}));
        }

        sb.append(']');
        return sb.toString();
    }

    public String toStandardString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("%02X", new Object[]{Byte.valueOf(this.address[0])}));

        for(int i = 1; i < this.address.length; ++i) {
            sb.append(':');
            sb.append(String.format("%02X", new Object[]{Byte.valueOf(this.address[i])}));
        }

        return sb.toString();
    }

    public String toHexString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("%02x", new Object[]{Byte.valueOf(this.address[0])}));

        for(int i = 1; i < this.address.length; ++i) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(this.address[i])}));
        }

        return sb.toString();
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            MacAddress that = (MacAddress)o;
            return Arrays.equals(this.address, that.address);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.address);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.address.length);
        dest.writeByteArray(this.address);
    }
}
