package ETRI.LBSP.Common.Interfaces;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

//PDI: Pre-defined Datatypes for Interface
//Type: PDI_SCANINFO_A
public class PDI_SCANINFO_D extends PDI_SCANINFO_A implements Comparable<Object>
{
	// 37 byte
	public int        Distance;

	public PDI_SCANINFO_D clone()
	{
		PDI_SCANINFO_D e = new PDI_SCANINFO_D();

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

		e.Distance = this.Distance;

		return e;
	}

	public PDI_SCANINFO_D convert_SCANINFO_A_to_SCANINFO_D()
	{
		PDI_SCANINFO_D b = new PDI_SCANINFO_D();

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
			if(FD7_SSID != null)
				buf.put(FD7_SSID.getBytes("us-ascii"));

			buf.putInt(Distance);
		}
		catch ( UnsupportedEncodingException e )
		{
			e.printStackTrace();
		}

		return buf.array();
	}

	public int packetLen()
	{
		FD6_SSID_LEN = 0;

		if(FD7_SSID != null)
			FD6_SSID_LEN = (short) FD7_SSID.length();

		return 1 + 12 + 1 + 2 + 1 + 2 + FD6_SSID_LEN + 4;
	}

	@Override
	public int compareTo(Object obj)
	{
		if ( FD3_RSSI < ((PDI_SCANINFO_A)obj).FD3_RSSI ) return 1;
		else if ( FD3_RSSI == ((PDI_SCANINFO_A)obj).FD3_RSSI ) return 0;
		return -1;
	}
}
