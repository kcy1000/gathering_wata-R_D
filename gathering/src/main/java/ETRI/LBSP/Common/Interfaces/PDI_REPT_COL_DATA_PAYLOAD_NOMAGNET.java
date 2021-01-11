package ETRI.LBSP.Common.Interfaces;

import android.util.Log;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


//PDI: Pre-defined Datatypes for Interface
//Type: PDI_REPT_COL_DATA_PAYLOAD
public class PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET
{
	public int				FD1_Payload_SIZE;
	public double				FD2_ColP_X;
	public double				FD3_ColP_Y;
	private short			FD4_ColP_F_LEN;
	private String			FD5_ColP_F;
	public int				FD6_Sensor_Altitude;
	public short			FD7_Sensor_Heading;	
	public String			FD8_ColTime;	// fixed to 14bytes: YYYYMMDDhhmmss
	public short			FD9_SCANINFO_A_CNT;
	public ArrayList<PDI_SCANINFO_A>
							FD10_SCANINFO_A_List;
	public double				FD11_GRS_X;
	public double				FD12_GRS_Y;

	public PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET()
	{
		FD10_SCANINFO_A_List = new ArrayList<PDI_SCANINFO_A>();
	}
	
	// get methods
	public String FD5_ColP_F() { return FD5_ColP_F; }
	public String FD7_ColTime() { return FD8_ColTime; }

	// set methods
	public void FD5_ColP_F ( String ColP_F )
	{
		FD5_ColP_F = ColP_F;
		
		try
		{
			FD4_ColP_F_LEN = (short) FD5_ColP_F.getBytes("utf-8").length;
		} catch ( UnsupportedEncodingException e ) {
			e.printStackTrace();
		}		
	}
	
	
	public byte[] packet()
	{
		byte[] FD8_ColTimeByte = new byte[14];
		System.arraycopy(FD8_ColTime.getBytes(), 0, FD8_ColTimeByte, 0, 14);
		int length = packetLen();
		
		ByteBuffer buf = ByteBuffer.allocate(length);
		buf.order(ByteOrder.BIG_ENDIAN);

		try
		{
			buf.putInt(length);
//			FD11_TM_CEN_X = FD2_ColP_X;
//			FD12_TM_CEN_Y = FD3_ColP_Y;
			
			buf.putDouble(FD2_ColP_X);
			buf.putDouble(FD3_ColP_Y);
			
			buf.putShort(FD4_ColP_F_LEN);
			buf.put(FD5_ColP_F.getBytes("utf-8"));
			buf.putInt(FD6_Sensor_Altitude);
			buf.putShort(FD7_Sensor_Heading);
			buf.put(FD8_ColTimeByte);
			buf.putShort(FD9_SCANINFO_A_CNT);
			for ( PDI_SCANINFO_A e : FD10_SCANINFO_A_List )
			{
				buf.put(e.packet());
			}
			buf.putDouble(FD11_GRS_X);
			buf.putDouble(FD12_GRS_Y);
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		
//		Log.i("PDI_REPT_COL_DATA_PAYLOAD, packet()", "length=" + length);	
		
		return buf.array();
	}
	
	public int packetLen()
	{
		try
		{
			FD4_ColP_F_LEN = (short) FD5_ColP_F.getBytes("utf-8").length;
		} catch ( UnsupportedEncodingException e ) {
			e.printStackTrace();
		}
		
		int length = 4 + 8 + 8 + 2 + FD4_ColP_F_LEN + 4 + 2 + 14 + 2;
		for ( PDI_SCANINFO_A e : FD10_SCANINFO_A_List )
		{
			length += e.packetLen();
		}
		length += 8+8;
		
		return length;
	}
	
	public static PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET decode ( ByteBuffer packet )
	{
		int offset = 0;
		PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET z = new PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET();
		
		byte[] buf = new byte[packet.capacity()];
		System.arraycopy(packet.array(), 0, buf, 0, packet.capacity());

		// FD1_Payload_SIZE
		ByteBuffer tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD1_Payload_SIZE = tmpBuf.getInt(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD1_Payload_SIZE=" + z.FD1_Payload_SIZE );
		offset += 4;
		
		// FD2_ColP_X
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.FD2_ColP_X = tmpBuf.getDouble(0);// - (int)ResultList_Path_Act.BaseX;
		Log.d("jeongyeol", "etri data x ="+z.FD2_ColP_X);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD2_ColP_X=" + z.FD2_ColP_X );
		offset += 8;
		
		// FD3_ColP_Y
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.FD3_ColP_Y = tmpBuf.getDouble(0);// - (int)ResultList_Path_Act.BaseY;
		Log.d("jeongyeol", "etri data y ="+z.FD3_ColP_Y);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD3_ColP_Y=" + z.FD3_ColP_Y );
		offset += 8;
		
		// FD4_ColP_F_LEN
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		WataLog.d("tmpBuf.getShort(0)="  +tmpBuf.getShort(0));
		z.FD4_ColP_F_LEN = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD4_ColP_F_LEN=" + z.FD4_ColP_F_LEN );
		offset += 2;

		// FD5_ColP_F
		tmpBuf = ByteBuffer.allocate(z.FD4_ColP_F_LEN);
		tmpBuf.put ( buf, offset, z.FD4_ColP_F_LEN );
		try {
			z.FD5_ColP_F = new String(tmpBuf.array(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD5_ColP_F=" + z.FD5_ColP_F );
		offset += z.FD4_ColP_F_LEN;		
		
		// FD6_Sensor_Altitude
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD6_Sensor_Altitude = tmpBuf.getInt(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD6_Sensor_Altitude=" + z.FD6_Sensor_Altitude );
		offset += 4;
		
		// FD7_Sensor_Heading
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		z.FD7_Sensor_Heading = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD7_Sensor_Heading=" + z.FD7_Sensor_Heading );
		offset += 2;		

		// FD8_ColTime
		byte[] FD7_ColTimeByte = new byte[14];
		System.arraycopy ( buf, offset, FD7_ColTimeByte, 0, 14);
		z.FD8_ColTime = new String(FD7_ColTimeByte);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD7_ColTime=" + z.FD8_ColTime );
		offset += 14;
		
		// FD9_SCANINFO_A_CNT
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		z.FD9_SCANINFO_A_CNT = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD8_SCANINFO_A_CNT=" + z.FD9_SCANINFO_A_CNT );
		offset += 2;
		
		// FD10_SCANINFO_A List
		for ( int i = 0; i < z.FD9_SCANINFO_A_CNT; i++ )
		{
			PDI_SCANINFO_A a = new PDI_SCANINFO_A();
			
			// FD1_INFRA_TYPE
			a.FD1_INFRA_TYPE = buf[offset];
			offset += 1;
			
			// FD2_INFRA_ID
			byte[] FD2_INFRA_ID_Byte = new byte[12];
			System.arraycopy(buf, offset, FD2_INFRA_ID_Byte, 0, 12);
			a.FD2_INFRA_ID = new String(FD2_INFRA_ID_Byte);
			offset += 12;
			
			// FD3_RSSI
			a.FD3_RSSI = buf[offset];
			offset += 1;
			
			// FD4_FREQ
			tmpBuf = ByteBuffer.allocate(2);
			tmpBuf.put ( buf, offset, 2 );
			a.FD4_FREQ = tmpBuf.getShort(0);	
			offset += 2;
			
			// FD5_ENCRYPTION
			a.FD5_ENCRYPTION = buf[offset];
			offset += 1;
			
			// FD6_SSID_LEN
			tmpBuf = ByteBuffer.allocate(2);
			tmpBuf.put ( buf, offset, 2 );
			a.FD6_SSID_LEN = tmpBuf.getShort(0);	
			offset += 2;
			
			// FD7_SSID
			tmpBuf = ByteBuffer.allocate(a.FD6_SSID_LEN);
			tmpBuf.put ( buf, offset, a.FD6_SSID_LEN );
			try {
				a.FD7_SSID = new String(tmpBuf.array(), "us-ascii");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			System.out.println ( "  - PDI_SCANINFO_A, decode(), FD7_SSID=" + a.FD7_SSID );
			offset += a.FD6_SSID_LEN;
			
			z.FD10_SCANINFO_A_List.add(a);
		}
		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.FD11_GRS_X = tmpBuf.getDouble(0);
		offset += 8;
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD11_TM_CEN_X=" + z.FD11_GRS_X );

		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.FD12_GRS_Y = tmpBuf.getDouble(0);
		offset += 8;
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD12_TM_CEN_Y=" + z.FD12_GRS_Y );
		return z;
	}
}
