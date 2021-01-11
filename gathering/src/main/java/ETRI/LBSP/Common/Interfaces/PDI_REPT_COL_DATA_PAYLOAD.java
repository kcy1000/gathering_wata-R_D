package ETRI.LBSP.Common.Interfaces;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


//PDI: Pre-defined Datatypes for Interface
//Type: PDI_REPT_COL_DATA_PAYLOAD
public class PDI_REPT_COL_DATA_PAYLOAD
{
	// v2.11
	public int				FD1_Payload_SIZE;				// 수집점 정보 개수 - 2142
	public double			FD2_ColP_X;						// X - 976.34
	public double			FD3_ColP_Y;						// Y - -1760.20
	public short			FD4_ColP_F_LEN;					// 방향 값 길이 - 4
	private String			FD5_ColP_F;						// 방향 값 - "AB01"
	public String			FD6_ColTime;					// 수집시간 - "20200103202821" fixed to 14bytes: YYYYMMDDhhmmss
	public short			FD7_SCANINFO_A_CNT;				// 스캔 정보 길이 - 57
	public ArrayList<PDI_SCANINFO_A> FD8_SCANINFO_A_List;	// 스캔 정보 배열 - size 57
	public float			FD9_ColMag_X;					// 센서 마그네틱 값 X - -580.44
	public float			FD10_ColMag_Y;					// 센서 마그네틱 값 Y - 43.86
	public float			FD11_ColMag_Z;					// 센서 마그네틱 값 Z - 199.3199
	public float			FD12_ColMag_Heading;			// 헤딩 값 - 131.40681
	public float			FD13_ColMag_Baro;				// 센서 Barometer 값 - 1027.1741
	public double			FD14_GRS_X;						// 위도 - 124.87
	public double			FD15_GRS_Y;						// 경도 - 32.57
//	public double			FD16_TM_X;
//	public double			FD17_TM_Y;

	/**
	 * 조영수박사님 로그 추가 건
	 */
	public float FD16_Sensor_Accelerometer_X;				// -0.88588166
	public float FD17_Sensor_Accelerometer_Y;				// 4.539545
	public float FD18_Sensor_Accelerometer_Z;				// 8.73192
	public float FD19_Sensor_Gravity_X;						// -0.1946
	public float FD20_Sensor_Gravity_Y;						// 5.28
	public float FD21_Sensor_Gravity_Z;						// 8.25
	public float FD25_Sensor_RotationVector_Roll;			// 1.35
	public float FD26_Sensor_RotationVector_Pitch;			// -32.59
	public float FD27_Sensor_RotationVector_Yaw;			// 131.57
	public int Step_Count = 0; // 0 보폭길이 합이라는 의미는 수집 시 Wi-Fi 스캔 주기 내 M번의 보행수가 검출되고 설정한 보폭길이가 0.7 (m)라고 하면, 0.7 x M (m)를 의미합니다
	public double Step_Length = 0d; // 0.0 보폭길이 합이라는 의미는 수집 시 Wi-Fi 스캔 주기 내 M번의 보행수가 검출되고 설정한 보폭길이가 0.7 (m)라고 하면, 0.7 x M (m)를 의미합니다
	/**
	 * 조영수박사님 로그 추가 건 끝
	 */

	public PDI_REPT_COL_DATA_PAYLOAD()
	{
		FD8_SCANINFO_A_List = new ArrayList<PDI_SCANINFO_A>();
	}

	// get methods
	public String FD8_ColP_F() { return FD5_ColP_F; }
	public String FD9_ColTime() { return FD6_ColTime; }

	// set methods
	public void FD8_ColP_F ( String ColP_F )
	{
		FD5_ColP_F = ColP_F;

		try
		{
			FD4_ColP_F_LEN = (short) FD5_ColP_F.getBytes("utf-8").length;
			WataLog.d("FD4_ColP_F_LEN=" + FD4_ColP_F_LEN);

		} catch ( UnsupportedEncodingException e ) {
			e.printStackTrace();
		}
	}


	public byte[] packet()
	{
		byte[] FD6_ColTimeByte = new byte[14];
		System.arraycopy(FD6_ColTime.getBytes(), 0, FD6_ColTimeByte, 0, 14); 	// 20200103202818
		int length = packetLen();

		ByteBuffer buf = ByteBuffer.allocate(length);
		buf.order(ByteOrder.BIG_ENDIAN);

		try
		{
			buf.putInt(length);
			buf.putDouble(FD2_ColP_X);		// 976.23
			buf.putDouble(FD3_ColP_Y);		// -1760.20

			buf.putShort(FD4_ColP_F_LEN);
			buf.put(FD5_ColP_F.getBytes("utf-8"));   // AB01
			buf.put(FD6_ColTimeByte);
			buf.putShort(FD7_SCANINFO_A_CNT);		// 8
			for ( PDI_SCANINFO_A e : FD8_SCANINFO_A_List )
			{
				buf.put(e.packet());
			}
			buf.putFloat(FD9_ColMag_X);			// -584.64
			buf.putFloat(FD10_ColMag_Y);		// 43.68
			buf.putFloat(FD11_ColMag_Z);		// 195.65
			buf.putFloat(FD12_ColMag_Heading);	// 132.56
			buf.putFloat(FD13_ColMag_Baro);		// 1027.21

			buf.putDouble(FD14_GRS_X);			// 124.87
			buf.putDouble(FD15_GRS_Y);			// 32.57

//			buf.putDouble(FD16_TM_X);
//			buf.putDouble(FD17_TM_Y);

			/**
			 * 조영수박사님 로그 추가 건
			 */
			buf.putFloat(FD16_Sensor_Accelerometer_X);			// -1.1923
			buf.putFloat(FD17_Sensor_Accelerometer_Y);			// 3.99
			buf.putFloat(FD18_Sensor_Accelerometer_Z);			// 7.87
			buf.putFloat(FD19_Sensor_Gravity_X);				// -0.58
			buf.putFloat(FD20_Sensor_Gravity_Y);				// 5.16
			buf.putFloat(FD21_Sensor_Gravity_Z);				// 8.31
			buf.putFloat(FD25_Sensor_RotationVector_Roll);		// 4.02
			buf.putFloat(FD26_Sensor_RotationVector_Pitch);		// -31.80
			buf.putFloat(FD27_Sensor_RotationVector_Yaw);		// 132.89
			/**
			 * 조영수박사님 로그 추가 건 끝
			 */
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
			FD4_ColP_F_LEN = (short) FD5_ColP_F.getBytes("utf-8").length;      // "AB01"
		} catch ( UnsupportedEncodingException e ) {
			e.printStackTrace();
		}

		/* v2.0 ????
		 * BP1	4 	Payload Size
		 * BP2	4 	Col P X
		 * BP3	4 	Col P Y
		 * BP4	2	Col P F Len
		 * BP5	F	Col P F
		 * BP6	4	Altitude
		 * BP7	2	Heading
		 * BP8	14 	Col Time
		 * BP9	2	Num SCANINFO_A
		 * BP10	L	SCANINFO_A List
		int length = 4 + 4 + 4 + 2 + FD4_ColP_F_LEN + 4 + 2 + 14 + 2 + L
		 */

		/* v2.1 ????
		 * 4	FD1_Payload_SIZE
		 * 4	FD2_ColLinkID
		 * 1	FD3_ColLinkFlag
		 * 4	FD4_ColLinkHeading
		 * 4	FD5_ColP_X
		 * 4	FD6_ColP_Y
		 * 2	FD7_ColP_F_LEN
		 * F	FD8_ColP_F
		 * 14	FD9_ColTime
		 * 2	FD10_SCANINFO_A_CNT
		 * L	FD11_SCANINFO_A_List
		 * 4	FD12_ColMag_X
		 * 4	FD13_ColMag_Y
		 * 4	FD14_ColMag_Z
		 * 4	FD15_ColMag_Heading
		 * 4	FD16_ColMag_Baro;
		int length = 4 + 2 + 1 + 2 + 4 + 4 + 2 + FD4_ColP_F_LEN + 14 + 2 + (L) + 4 + 4 + 4 + 2 + 4;
		 */

		/* v2.11 조영수 박사님 로그 추가건
		 * +	[CONTENTS]
		 * >>
		 * 4	FD16_Sensor_Accelerometer_X
		 * 4	FD17_Sensor_Accelerometer_Y
		 * 4	FD18_Sensor_Accelerometer_Z
		 * 4	FD19_Sensor_Gravity_X
		 * 4	FD20_Sensor_Gravity_Y
		 * 4	FD21_Sensor_Gravity_Z
		 * 4	FD22_Sensor_Magneticfield_X
		 * 4	FD23_Sensor_Magneticfield_Y
		 * 4	FD24_Sensor_Magneticfield_Z
		 * 4	FD25_Sensor_RotationVector_Roll
		 * 4	FD26_Sensor_RotationVector_Pitch
		 * 4	FD27_Sensor_RotationVector_Yaw
		 * 4	Step_Count
		 * 8	Step_Length
		 * >>
		 *
		int length = 4 + 2 + 1 + 2 + 4 + 4 + 2 + FD4_ColP_F_LEN + 14 + 2 + (L) + 4 + 4 + 4 + 2 + 4 +
			(4 + 4 + 4) + (4 + 4 + 4) + (4 + 4 + 4) + (4 + 4 + 4) + 4 + 8;
		 */
		int length = /*4 + 4 + 1 +*/ 4 + 8 + 8 + 2 + FD4_ColP_F_LEN + 14 + 2 + 4 + 4 + 4 + 4 + 4;
		for ( PDI_SCANINFO_A e : FD8_SCANINFO_A_List )
		{
			length += e.packetLen();
		}
		length += 8+8; //GRS_X, GRS_Y
//		length += 8+8; //TM_X, TM_Y
		length += (4 + 4 + 4) + (4 + 4 + 4) + (4 + 4 + 4) + (4 + 4 + 4) + 4 + 8; // 조영수 박사님 로그 추가건 v2.11
		return length;
	}

	public static PDI_REPT_COL_DATA_PAYLOAD decode ( ByteBuffer packet )
	{
		int offset = 0;
		PDI_REPT_COL_DATA_PAYLOAD z = new PDI_REPT_COL_DATA_PAYLOAD();

		byte[] buf = new byte[packet.capacity()];
		System.arraycopy(packet.array(), 0, buf, 0, packet.capacity());

		// FD1_Payload_SIZE
		ByteBuffer tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD1_Payload_SIZE = tmpBuf.getInt(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD1_Payload_SIZE=" + z.FD1_Payload_SIZE );
		offset += 4;

		// FD5_ColP_X
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.FD2_ColP_X = tmpBuf.getDouble(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD2_ColP_X=" + z.FD2_ColP_X );
		offset += 8;

		// FD6_ColP_Y
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.FD3_ColP_Y = tmpBuf.getDouble(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD3_ColP_Y=" + z.FD3_ColP_Y );
		offset += 8;

		// FD7_ColP_F_LEN
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		WataLog.d("tmpBuf.getShort(0)="  +tmpBuf.getShort(0));
		z.FD4_ColP_F_LEN = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD4_ColP_F_LEN=" + z.FD4_ColP_F_LEN );
		offset += 2;

		// FD8_ColP_F
		tmpBuf = ByteBuffer.allocate(z.FD4_ColP_F_LEN);
		tmpBuf.put ( buf, offset, z.FD4_ColP_F_LEN );
		try {
			z.FD5_ColP_F = new String(tmpBuf.array(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD5_ColP_F=" + z.FD5_ColP_F );
		offset += z.FD4_ColP_F_LEN;

		// FD9_ColTime
		byte[] FD9_ColTimeByte = new byte[14];
		System.arraycopy ( buf, offset, FD9_ColTimeByte, 0, 14);
		z.FD6_ColTime = new String(FD9_ColTimeByte);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD6_ColTime=" + z.FD6_ColTime );
		offset += 14;

		// FD10_SCANINFO_A_CNT
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		z.FD7_SCANINFO_A_CNT = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD7_SCANINFO_A_CNT=" + z.FD7_SCANINFO_A_CNT );
		offset += 2;

		int nFind = 0;
		// FD11_SCANINFO_A_List
		for ( int i = 0; i < z.FD7_SCANINFO_A_CNT; i++ )
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
			WataLog.d(" a.FD6_SSID_LEN=" +  a.FD6_SSID_LEN);
			tmpBuf.put ( buf, offset, a.FD6_SSID_LEN );
			try {
				a.FD7_SSID = new String(tmpBuf.array(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			System.out.println ( "  - PDI_SCANINFO_A, decode(), FD7_SSID=" + a.FD7_SSID );
			offset += a.FD6_SSID_LEN;

			if(a.FD1_INFRA_TYPE == 'W') {
				z.FD8_SCANINFO_A_List.add(a);
			} else if(a.FD1_INFRA_TYPE == 'B') {
				PDI_SCANINFO_B b = a.convert_SCANINFO_A_to_SCANINFO_B();
				tmpBuf = ByteBuffer.allocate(32);
				tmpBuf.put(buf, offset, 32);
				offset += 32;
				try {
					b.FD8_UUID = new String(tmpBuf.array(), "us-ascii");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				tmpBuf = ByteBuffer.allocate(2);
				tmpBuf.put(buf, offset, 2);
				offset += 2;
				b.FD9_Major = tmpBuf.getShort(0);

				tmpBuf = ByteBuffer.allocate(2);
				tmpBuf.put(buf, offset, 2);
				offset += 2;
				b.FD10_Minior = tmpBuf.getShort(0);

				b.FD11_TXPow = buf[offset];
				offset += 1;
				z.FD8_SCANINFO_A_List.add(b);
			}
		}

		// FD9_ColMag_X
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD9_ColMag_X = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD9_ColMag_X=" + z.FD9_ColMag_X );
		offset += 4;

		// FD10_ColMag_Y
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD10_ColMag_Y = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD10_ColMag_Y=" + z.FD10_ColMag_Y );
		offset += 4;

		// FD11_ColMag_Z
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD11_ColMag_Z = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD11_ColMag_Z=" + z.FD11_ColMag_Z );
		offset += 4;

		// FD15_ColMag_Heading
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD12_ColMag_Heading = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD15_ColMag_Heading=" + z.FD12_ColMag_Heading );
		offset += 4;

		// FD16_ColMag_Baro
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD13_ColMag_Baro = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD16_ColMag_Baro=" + z.FD13_ColMag_Baro );
		offset += 4;

		// FD14_GRS_X
		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.FD14_GRS_X = tmpBuf.getDouble(0);
		offset += 8;
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD14_GRS_X=" + z.FD14_GRS_X );
		// B15_GRS_Y
		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.FD15_GRS_Y = tmpBuf.getDouble(0);
		offset += 8;
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD15_GRS_Y=" + z.FD15_GRS_Y );

		// FD16_TM_X
//		tmpBuf.clear();
//		tmpBuf = ByteBuffer.allocate(8);
//		tmpBuf.put ( buf, offset, 8 );
//		z.FD16_TM_X = tmpBuf.getDouble(0);
//		offset += 8;
//		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD16_TM_X=" + z.FD16_TM_X );
//		// B17_TM_Y
//		tmpBuf.clear();
//		tmpBuf = ByteBuffer.allocate(8);
//		tmpBuf.put ( buf, offset, 8 );
//		z.FD17_TM_Y = tmpBuf.getDouble(0);
//		offset += 8;
//		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD17_TM_Y=" + z.FD17_TM_Y );

		/**
		 * 조영수박사님 로그 추가 건
		 */
		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD16_Sensor_Accelerometer_X = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD16_Sensor_Accelerometer_X=" + z.FD16_Sensor_Accelerometer_X );
		offset += 4;

		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD17_Sensor_Accelerometer_Y = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD17_Sensor_Accelerometer_Y=" + z.FD17_Sensor_Accelerometer_Y );
		offset += 4;

		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD18_Sensor_Accelerometer_Z = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD18_Sensor_Accelerometer_Z=" + z.FD18_Sensor_Accelerometer_Z );
		offset += 4;

		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD19_Sensor_Gravity_X = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD19_Sensor_Gravity_X=" + z.FD19_Sensor_Gravity_X );
		offset += 4;

		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD20_Sensor_Gravity_Y = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD20_Sensor_Gravity_Y=" + z.FD20_Sensor_Gravity_Y );
		offset += 4;

		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD21_Sensor_Gravity_Z = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD21_Sensor_Gravity_Z=" + z.FD21_Sensor_Gravity_Z );
		offset += 4;

		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD25_Sensor_RotationVector_Roll = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD25_Sensor_RotationVector_Roll=" + z.FD25_Sensor_RotationVector_Roll );
		offset += 4;

		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD26_Sensor_RotationVector_Pitch = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD26_Sensor_RotationVector_Pitch=" + z.FD26_Sensor_RotationVector_Pitch );
		offset += 4;

		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.FD27_Sensor_RotationVector_Yaw = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD, decode(), FD27_Sensor_RotationVector_Yaw=" + z.FD27_Sensor_RotationVector_Yaw );
		offset += 4;

		/**
		 * 조영수박사님 로그 추가 건 끝
		 */
		return z;
	}

//	private static void excute(ByteBuffer tmpBuf, Object value, int offset) {
//	}
}
