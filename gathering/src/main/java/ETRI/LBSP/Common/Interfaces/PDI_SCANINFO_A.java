package ETRI.LBSP.Common.Interfaces;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

//PDI: Pre-defined Datatypes for Interface
//Type: PDI_SCANINFO_A
public class PDI_SCANINFO_A implements Comparable<Object>
{
	public static final byte DEF_INFRA_TYPE_WiFi 		= 'W';			// 87
	public static final byte DEF_INFRA_TYPE_WiFi_RTT 	= 'R';			// 82
	public static final byte DEF_INFRA_TYPE_Cell 		= 'C';			// 67
	public static final byte DEF_INFRA_TYPE_NFC 		= 'N';			// 78
	public static final byte DEF_INFRA_TYPE_Bluetooth	= 'B';			// 66
	
	public byte			FD1_INFRA_TYPE;		// 87
	public String		FD2_INFRA_ID;		// "0a23aa022492" fixed to 12bytes MAC address
	public byte			FD3_RSSI;			// -48
	public short		FD4_FREQ;			// 5260 not Channel: same channel # existing in 2.4GHz/5GHz
	public byte			FD5_ENCRYPTION;		// 0
	public short		FD6_SSID_LEN;		// 18
	public String		FD7_SSID;			// "T wifi zone_secure"
	
	public int			FDx_STEP_LEN;		// 0 Extended. Additional Data - Step Length
	public double		FDx_STEP_FREQ;		// 0.0 Extended. Additional Data - Step Frequency
	public double		FDx_GYRO;			// 0.0 Extended. Additional Data - Gyro alue
	public double		FDx_MAGNETO;		// 0.0 Extended. Additional Data - Magnetometer value
	public String		FDx_Extended;		// null Extended. Any Additional Data on Collecting
	
	public PDI_SCANINFO_A clone()
	{
		PDI_SCANINFO_A e = new PDI_SCANINFO_A();

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
	
	public PDI_SCANINFO_B convert_SCANINFO_A_to_SCANINFO_B()
	{
		PDI_SCANINFO_B b = new PDI_SCANINFO_B();
		
		b.FD1_INFRA_TYPE = this.FD1_INFRA_TYPE;
		b.FD2_INFRA_ID = this.FD2_INFRA_ID;
		b.FD3_RSSI = this.FD3_RSSI;
		
		return b;
	}
	
	
	public String FD1_INFRA_TYPE()
	{
		switch ( FD1_INFRA_TYPE )
		{
		case DEF_INFRA_TYPE_WiFi: 		return "Wi-Fi";
		case DEF_INFRA_TYPE_Cell: 		return "Cellular";
		case DEF_INFRA_TYPE_NFC: 		return "NFC";
		case DEF_INFRA_TYPE_Bluetooth: 	return "Bluetooth";
		case DEF_INFRA_TYPE_WiFi_RTT: 	return "RTT";
		}
		
		return "Unknown";
	}
	
	public String FD2_INFRA_ID()
	{	
		return new String(FD2_INFRA_ID);
	}
	
	public String FD7_SSID()
	{
		WataLog.d("FD7_SSID=" + FD7_SSID);

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
//		int len = 1 + 12 + 1 + 2 + 1 + 2 + FD6_SSID_LEN;
//		WataLog.d("FD6_SSID_LEN ="  + (len));
		return 1 + 12 + 1 + 2 + 1 + 2 + FD6_SSID_LEN;
	}
	
	@Override
	public int compareTo(Object obj)
	{
		if ( FD3_RSSI < ((PDI_SCANINFO_A)obj).FD3_RSSI ) return 1;
		else if ( FD3_RSSI == ((PDI_SCANINFO_A)obj).FD3_RSSI ) return 0;
		return -1;
	}
}
