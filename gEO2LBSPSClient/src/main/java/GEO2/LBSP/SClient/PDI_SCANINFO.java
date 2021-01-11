package GEO2.LBSP.SClient;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

//PDI: Pre-defined Datatypes for Interface
//Type: PDI_SCANINFO_A
public class PDI_SCANINFO implements Comparable<Object>
{
	public static final byte DEF_INFRA_TYPE_WiFi 		= 'W';
	public static final byte DEF_INFRA_TYPE_Cell 		= 'C';
	public static final byte DEF_INFRA_TYPE_NFC 		= 'N';
	public static final byte DEF_INFRA_TYPE_Bluetooth	= 'B';	
	
	public byte			FD1_INFRA_TYPE;
	public String		FD2_INFRA_ID;		// fixed to 12bytes MAC address
	public byte			FD3_RSSI;
	public short		FD4_FREQ;			// not Channel: same channel # existing in 2.4GHz/5GHz
	public byte			FD5_ENCRYPTION;
	public short		FD6_SSID_LEN;
	public String		FD7_SSID;
	
	public int			FDx_STEP_LEN;		// Extended. Additional Data - Step Length
	public double		FDx_STEP_FREQ;		// Extended. Additional Data - Step Frequency
	public double		FDx_GYRO;			// Extended. Additional Data - Gyro alue
	public double		FDx_MAGNETO;		// Extended. Additional Data - Magnetometer value
	public String		FDx_Extended;		// Extended. Any Additional Data on Collecting
	
	public PDI_SCANINFO clone()
	{
		PDI_SCANINFO e = new PDI_SCANINFO();

		e.FD1_INFRA_TYPE = this.FD1_INFRA_TYPE;
		e.FD2_INFRA_ID = this.FD2_INFRA_ID;
		e.FD3_RSSI = this.FD3_RSSI;
		e.FD4_FREQ = this.FD4_FREQ;
		e.FD5_ENCRYPTION = this.FD5_ENCRYPTION;
		e.FD6_SSID_LEN = this.FD6_SSID_LEN;
		e.FD7_SSID = this.FD7_SSID;
		
		e.FDx_STEP_LEN = this.FDx_STEP_LEN;
		e.FDx_STEP_FREQ = this.FDx_STEP_FREQ;
		e.FDx_GYRO = this.FDx_GYRO;
		e.FDx_MAGNETO = this.FDx_MAGNETO;
		e.FDx_Extended = this.FDx_Extended;
		
		return e;
	}
	
	public String FD1_INFRA_TYPE()
	{
		switch ( FD1_INFRA_TYPE )
		{
		case DEF_INFRA_TYPE_WiFi: 		return "Wi-Fi";
		case DEF_INFRA_TYPE_Cell: 		return "Cellular";
		case DEF_INFRA_TYPE_NFC: 		return "NFC";
		case DEF_INFRA_TYPE_Bluetooth: 	return "Bluetooth";
		}
		
		return "Unknown";
	}
	
	public String FD2_INFRA_ID()
	{	
		return new String(FD2_INFRA_ID);
	}
	
	public String FD7_SSID()
	{	
		return new String(FD7_SSID);
	}
	
	public byte[] packet()
	{
		int length = packetLen();
		ByteBuffer buf = ByteBuffer.allocate(length);
		buf.order(ByteOrder.BIG_ENDIAN);
	
		try
		{
			buf.put(FD1_INFRA_TYPE);
			buf.put(FD2_INFRA_ID.getBytes("us-ascii"));
			buf.put(FD3_RSSI);
			buf.putShort(FD4_FREQ);
			buf.put(FD5_ENCRYPTION);
			buf.putShort(FD6_SSID_LEN);
			buf.put(FD7_SSID.getBytes("us-ascii"));
		}
		catch ( UnsupportedEncodingException e )
		{
			e.printStackTrace();
		}

		return buf.array();
	}
	
	public int packetLen()
	{
		FD6_SSID_LEN = (short) FD7_SSID.length();

		return 1 + 12 + 1 + 2 + 1 + 2 + FD6_SSID_LEN;
	}
	
	public int compareTo(Object obj)
	{
		if ( FD3_RSSI < ((PDI_SCANINFO)obj).FD3_RSSI ) return 1;
		else if ( FD3_RSSI == ((PDI_SCANINFO)obj).FD3_RSSI ) return 0;
		return -1;
	}
}
