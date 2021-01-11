package ETRI.LBSP.Common.Interfaces;

import com.geotwo.LAB_TEST.Cell.CellType;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.geotwo.LAB_TEST.Cell.wtCellInfo.UNKNOWN_CID;
import static com.geotwo.LAB_TEST.Cell.wtCellInfo.UNKNOWN_CID_LONG;
import static com.geotwo.LAB_TEST.Cell.wtCellInfo.UNKNOWN_SIGNAL;

//PDI: Pre-defined Datatypes for Interface
//Type: PDI_SCANINFO_A
public class PDI_SCANINFO_C extends PDI_SCANINFO_A implements Comparable<Object>
{
	// 37 byte
	/**
	 * Cell ID.
	 */
	private int cellId;
	/**
	 * Cell Signal ID.
	 */
	private int cellSignalId;
	/**
	 * Mobile Country Code.
	 */
	private int mcc = UNKNOWN_CID;
	/**
	 * Mobile Network Code.
	 */
	private int mnc = UNKNOWN_CID;
	/**
	 * Location Area Code.
	 */
	private int lac = UNKNOWN_CID;
	/**
	 * Cell Tower ID.
	 */
	public long cid = UNKNOWN_CID_LONG;
	/**
	 * Primary Scrambling Code.
	 */
	public int psc = UNKNOWN_CID;
	/**
	 * Network Type.
	 */
	private CellType cellType = CellType.Unknown;
	/**
	 * Is cell neighboring.
	 */
	private boolean neighboring = false;
	/**
	 * Timing Advance.
	 */
	private int ta = UNKNOWN_SIGNAL;
	/**
	 * Arbitrary Strength Unit Level.
	 */
	private int asu = UNKNOWN_SIGNAL;
	/**
	 * Signal Strength in dBm.
	 */
	private int dbm = UNKNOWN_SIGNAL;
	/**
	 * Reference Signal Received Power in dBm.
	 */
	public int rsrp = UNKNOWN_SIGNAL;
	/**
	 * Reference Signal Received Quality in dB.
	 */
	private int rsrq = UNKNOWN_SIGNAL;
	/**
	 * Received Signal Strength Indication in dBm.
	 */
	private int rssi = UNKNOWN_SIGNAL;
	/**
	 * Reference Signal Signal-to-Noise Ratio in dB.
	 */
	private int rssnr = UNKNOWN_SIGNAL;
	/**
	 * Channel Quality Indicator.
	 */
	private int cqi = UNKNOWN_SIGNAL;
	/**
	 * Received Signal Code Power in dBm.
	 */
	private int rscp = UNKNOWN_SIGNAL;
	/**
	 * Channel State Information (CSI) Reference Signal Received Power in dBm.
	 */
	private int csiRsrp = UNKNOWN_SIGNAL;
	/**
	 * Channel State Information (CSI) Reference Signal Received Quality in dB.
	 */
	private int csiRsrq = UNKNOWN_SIGNAL;
	/**
	 * Channel State Information (CSI) Signal-to-Noise and Interference Ratio in dB.
	 */
	private int csiSinr = UNKNOWN_SIGNAL;
	/**
	 * Synchronization Signal (SS) Reference Signal Received Power in dBm.
	 */
	private int ssRsrp = UNKNOWN_SIGNAL;
	/**
	 * Synchronization Signal (SS) Reference Signal Received Quality in dB.
	 */
	private int ssRsrq = UNKNOWN_SIGNAL;
	/**
	 * Synchronization Signal (SS) Signal-to-Noise and Interference Ratio in dB.
	 */
	private int ssSinr = UNKNOWN_SIGNAL;
	/**
	 * CDMA RSSI value in dBm.
	 */
	private int cdmaDbm = UNKNOWN_SIGNAL;
	/**
	 * CDMA Ec/Io value in dB*10.
	 */
	private int cdmaEcio = UNKNOWN_SIGNAL;
	/**
	 * EVDO RSSI value in dBm.
	 */
	private int evdoDbm = UNKNOWN_SIGNAL;
	/**
	 * EVDO Ec/Io value in dB*10.
	 */
	private int evdoEcio = UNKNOWN_SIGNAL;
	/**
	 * Signal to noise ratio.
	 */
	private int evdoSnr = UNKNOWN_SIGNAL;

	public PDI_SCANINFO_C clone()
	{
		PDI_SCANINFO_C e = new PDI_SCANINFO_C();

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

		e.cellId = this.cellId;
		e.cellSignalId = this.cellSignalId;
		e.mcc = this.mcc;
		e.mnc = this.mnc;
		e.lac = this.lac;
		e.cid = this.cid;
		e.psc = this.psc;
		e.cellType = this.cellType;
		e.neighboring = this.neighboring;
		e.ta = this.ta;
		e.asu = this.asu;
		e.dbm = this.dbm;
		e.rsrp = this.rsrp;
		e.rsrq = this.rsrq;
		e.rssi = this.rssi;
		e.rssnr = this.rssnr;
		e.cqi = this.cqi;
		e.rscp = this.rscp;
		e.csiRsrp = this.csiRsrp;
		e.csiRsrq = this.csiRsrq;
		e.csiSinr = this.csiSinr;
		e.ssRsrp = this.ssRsrp;
		e.ssRsrq = this.ssRsrq;
		e.ssSinr = this.ssSinr;
		e.cdmaDbm = this.cdmaDbm;
		e.cdmaEcio = this.cdmaEcio;
		e.evdoDbm = this.evdoDbm;
		e.evdoEcio = this.evdoEcio;
		e.evdoSnr = this.evdoSnr;

		return e;
	}

	public PDI_SCANINFO_C convert_SCANINFO_A_to_SCANINFO_C()
	{
		PDI_SCANINFO_C c = new PDI_SCANINFO_C();

		c.FD1_INFRA_TYPE = this.FD1_INFRA_TYPE;
		c.FD2_INFRA_ID = this.FD2_INFRA_ID;
		c.FD3_RSSI = this.FD3_RSSI;

		return c;
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

			buf.putLong(cid);
			buf.putInt(psc);
			buf.putInt(rscp);
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

		return 1 + 12 + 1 + 2 + 1 + 2 + FD6_SSID_LEN + 8 + 4 + 4;
	}

	@Override
	public int compareTo(Object obj)
	{
		if ( FD3_RSSI < ((PDI_SCANINFO_A)obj).FD3_RSSI ) return 1;
		else if ( FD3_RSSI == ((PDI_SCANINFO_A)obj).FD3_RSSI ) return 0;
		return -1;
	}
}
