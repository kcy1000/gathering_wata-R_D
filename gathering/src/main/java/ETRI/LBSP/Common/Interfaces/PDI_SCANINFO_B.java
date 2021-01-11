package ETRI.LBSP.Common.Interfaces;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

//PDI: Pre-defined Datatypes for Interface
//Type: PDI_SCANINFO_A
public class PDI_SCANINFO_B extends PDI_SCANINFO_A implements Comparable<Object>
{
	// 37 byte
	public String       FD8_UUID; // 32 byte
	public short        FD9_Major;
	public short        FD10_Minior;
	public byte         FD11_TXPow;

	public PDI_SCANINFO_B clone()
	{
		PDI_SCANINFO_B e = new PDI_SCANINFO_B();

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

		e.FD8_UUID = this.FD8_UUID;
		e.FD9_Major = this.FD9_Major;
		e.FD10_Minior = this.FD10_Minior;
		e.FD11_TXPow = this.FD11_TXPow;

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

			buf.put(FD8_UUID.getBytes("us-ascii"));
			buf.putShort(FD9_Major);
			buf.putShort(FD10_Minior);
			buf.put(FD11_TXPow);
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

		return 1 + 12 + 1 + 2 + 1 + 2 + FD6_SSID_LEN + 37;
	}

	@Override
	public int compareTo(Object obj)
	{
		if ( FD3_RSSI < ((PDI_SCANINFO_A)obj).FD3_RSSI ) return 1;
		else if ( FD3_RSSI == ((PDI_SCANINFO_A)obj).FD3_RSSI ) return 0;
		return -1;
	}
}
