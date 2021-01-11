package ETRI.LBSP.Common.Interfaces;

import com.geotwo.LAB_TEST.Gathering.GatheringActivity;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
//PDI: Pre-defined Datatypes for Interface
//Type: REPT_COL_DATA
public class PDI_REPT_COL_DATA extends PDI_MSG
{
	public final static int B1_GID_LEN = 25;

	public static final byte DEF_ColOpt_BY_ColDevice 		= 0x10;
	public static final byte DEF_ColOpt_BY_MobilePayment	= 0x20;

	public byte[]			B1_GID;
	public int				B2_GID_refP_Latitude;
	public int				B3_GID_refP_Longitude;
	public int				B4_GID_refP_Bearing;
	public double			B5_ColStartP_X;
	public double			B6_ColStartP_Y;
	private short			B7_ColStartP_F_LEN;
	private String			B8_ColStartP_F;
	private PDI_CIVIC_ADDR	B9_ColStartP_CVADDR;
	private short			B10_ColDevModel_LEN;
	private String			B11_ColDevModel;

	// 추가
	public double 			B12_ColEndP_X;
	public double 			B13_ColEndP_Y;

	//PDI_REPT_COL_DATA_PAYLOAD에서 옮겨옴
	public int				B14_ColLinkID;
	public byte				B15_ColLinkFlag;     // 정방향,역방향
	public float			B16_ColLinkHeading;  // 라인각도
	public byte				B17_ColOpt;
	public double           B18_ColStartP_GRS_X;
	public double           B19_ColStartP_GRS_Y;
	public short           B20_ColPoint_CNT;

//	public double           B20_ColStartP_TM_X;
//	public double           B21_ColStartP_TM_Y;
//	public short			B22_ColPoint_CNT;
	public ArrayList<PDI_REPT_COL_DATA_PAYLOAD> B21_Payload;
	public ArrayList<PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET> B22_Payload;


	private PDI_REPT_COL_DATA()
	{
		B1_GID = new byte[B1_GID_LEN];
		B9_ColStartP_CVADDR = new PDI_CIVIC_ADDR();
		B21_Payload = new ArrayList<PDI_REPT_COL_DATA_PAYLOAD>();
		B22_Payload = new ArrayList<PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET>();
	}

	public PDI_REPT_COL_DATA ( String devSerial )
	{
		super ( devSerial, MSGID_REPT_COL_DATA );

		B1_GID = new byte[B1_GID_LEN];
		B9_ColStartP_CVADDR = new PDI_CIVIC_ADDR();
		B21_Payload = new ArrayList<PDI_REPT_COL_DATA_PAYLOAD>();
		B22_Payload = new ArrayList<PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET>();
	}

	// get methods
	public String B1_GID() { return new String ( B1_GID ); }
	public String B8_ColStartP_F() { return new String ( B8_ColStartP_F ); }
	public PDI_CIVIC_ADDR B9_ColStartP_CVADDR() { return B9_ColStartP_CVADDR.CVADDR(); }
	public String B11_ColDevModel() { return B11_ColDevModel; }
	public String B12_ColOpt()
	{
		switch ( B17_ColOpt )
		{
		case DEF_ColOpt_BY_ColDevice: return "by Collection Device";
		case DEF_ColOpt_BY_MobilePayment: return "by Mobile Payment System";
		}

		return "Unknown";
	}

	// set methods
	public void B8_ColStartP_F ( String ColStartP_F )
	{
		B8_ColStartP_F = ColStartP_F;

		try {
			B7_ColStartP_F_LEN = (short) B8_ColStartP_F.getBytes("utf-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void B9_ColStartP_CVADDR ( String ColStartP_CVADDR )
	{
		B9_ColStartP_CVADDR.FD4_CVADDR_LV3 = String.valueOf(GatheringActivity.mCurrentPath);
//		if(StaticManager.worker != null && StaticManager.worker.equalsIgnoreCase("PILOT_KISA"))
//			B9_ColStartP_CVADDR.FD5_CVADDR_LV4 = String.valueOf(GatheringActivity._startPoint.x);
//		if(StaticManager.worker != null && StaticManager.worker.equalsIgnoreCase("PILOT_KISA"))
//			B9_ColStartP_CVADDR.FD6_CVADDR_LV5 = String.valueOf(GatheringActivity._startPoint.y);
//		if(StaticManager.worker != null && StaticManager.worker.equalsIgnoreCase("PILOT_KISA"))
//			B9_ColStartP_CVADDR.FD7_CVADDR_LV6 = SimpleGatheringActivity.mPlace;
		B9_ColStartP_CVADDR.CVADDR ( ColStartP_CVADDR );
	}

	public void B11_ColDevModel ( String ColDevModel ) {
		B11_ColDevModel = ColDevModel;
		try {
			B10_ColDevModel_LEN = (short) B11_ColDevModel.getBytes("us-ascii").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	// encode / decode methods
	public byte[] packet()
	{
		try { // kcy1000- null error 발생한적 있음.
			B7_ColStartP_F_LEN = (short) B8_ColStartP_F.getBytes("utf-8").length;			// B8_ColStartP_F - 설명> 층정보 예시값> AB01
			B10_ColDevModel_LEN = (short) B11_ColDevModel.getBytes("us-ascii").length;		// B11_ColDevModel - 설명> 핸드폰기종. 예시값> SM-N976N
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		int packetLen_excl_Header = 25 + 4 * 3 + 8 * 2 + 2 + B7_ColStartP_F_LEN;
		packetLen_excl_Header	 += B9_ColStartP_CVADDR.packetLen();					// B9_ColStartP_CVADDR - 설명> ???. 예시값> 지오투/Gangnam/1///
		packetLen_excl_Header 	 += 2 + B10_ColDevModel_LEN;
		packetLen_excl_Header 	 += 8 + 8 + 4 + 1 + 4; //추가됨
		packetLen_excl_Header 	 += 1 + 2;
		packetLen_excl_Header 	 += 8 + 8; //B18_ColStartP_GRS_X, B19_ColStartP_GRS_Y 추가됨
		for ( PDI_REPT_COL_DATA_PAYLOAD e : B21_Payload )
		{
			packetLen_excl_Header += e.packetLen();
		}
		packetLen_excl_Header += 8+8;
		int packetLen_incl_Header = headerLen() + packetLen_excl_Header;

		H3_MSG_LEN = packetLen_excl_Header;

		ByteBuffer buf = ByteBuffer.allocate(packetLen_incl_Header);
		buf.order(ByteOrder.BIG_ENDIAN);

		buf.put(header());
		buf.put(B1_GID);								// B1_GID - 설명> 건물에 대한 ID로, 행정안전부에서 고시한 도로명주소코드(25byte)를 그대로 준용하도록 설계(지금은 구현하는 기관마다 임의의 값을 사용) 예시값> 1079
		buf.putInt(B2_GID_refP_Latitude);				// B2_GID_refP_Latitude - 설명> 위도 예시값> 0
		buf.putInt(B3_GID_refP_Longitude);				// B3_GID_refP_Longitude - 설명> 경도 예시값> 0
		buf.putInt(B4_GID_refP_Bearing);				// B4_GID_refP_Bearing - 설명> Bearing 예시값> 0
		buf.putDouble(B5_ColStartP_X);					// B5_ColStartP_X - 설명> 시작점 X 예시값> -1760.2020
		buf.putDouble(B6_ColStartP_Y);					// B6_ColStartP_Y - 설명> 시작점 Y 예시값> 976.32

		buf.putShort(B7_ColStartP_F_LEN);				// B7_ColStartP_F_LEN - 설명> 방향 값 오브젝트 길이 예시값> 4
		buf.put(B8_ColStartP_F.getBytes());				// B8_ColStartP_F - 설명> 방향값(정방향, 역방향) 예시값> AB01
		buf.put(B9_ColStartP_CVADDR.packet());			// B9_ColStartP_CVADDR - 설명> Civic Address, 수집지점의 주소(사람이 이해하기 쉬운 문자열 형태) 예시값> 지오투/Gangnam/1////.
		buf.putShort(B10_ColDevModel_LEN);				// 예시값> 8
		buf.put(B11_ColDevModel.getBytes());			// B11_ColDevModel - 설명> 핸드폰기. 예시값> SM-N976N
		buf.putDouble(B12_ColEndP_X);					// B12_ColEndP_X - 설명> 끝점 X. 예시값> 1068.21
		buf.putDouble(B13_ColEndP_Y);					// B13_ColEndP_Y - 설명> 끝점 Y. 예시값> -1995.86

		buf.putInt(B14_ColLinkID);						// B14_ColLinkID - 설명> 링크 id. 예시값> 1
		buf.put(B15_ColLinkFlag);						// B15_ColLinkFlag - 설명> 수집방향(정방향/역방향)에 대한 구분으로 ascii 값 70은 'F' (forward)에 해당. 예시값> 70
		buf.putFloat(B16_ColLinkHeading);				// B16_ColLinkHeading - 설명> 링크 각도(정북으로 부터). 예시값> 158.70

		buf.put(B17_ColOpt);							// B17_ColOpt - 설명> 수집한 장치의 특징을 나타내는 옵션으로 모바일 단말로 수집 시 0x10(십진법: 16)을 사용함. 예시값> 16
		buf.putDouble(B18_ColStartP_GRS_X);				// B18_ColStartP_GRS_X - 설명> 수집시작지점(X좌표)을 GRS80좌표계로 표시한 값. 예시값> 32.5723
		buf.putDouble(B19_ColStartP_GRS_Y);				// B19_ColStartP_GRS_Y - 설명> 수집시작지점(Y좌표)을 GRS80좌표계로 표시한 값. 예시값> 124.8713
		buf.putShort(B20_ColPoint_CNT);					// B20_ColPoint_CNT - 설명> 수집 지점 개수. 예시값> 109
//		buf.putDouble(B20_ColStartP_TM_X);
//		buf.putDouble(B21_ColStartP_TM_Y);
//		buf.putShort(B22_ColPoint_CNT);

		for ( PDI_REPT_COL_DATA_PAYLOAD e : B21_Payload )
		{
			buf.put(e.packet());
		}

		return buf.array();
	}

	public byte[] packet_nomagnet()
	{
		try {
			B7_ColStartP_F_LEN = (short) B8_ColStartP_F.getBytes("utf-8").length;
			B10_ColDevModel_LEN = (short) B11_ColDevModel.getBytes("us-ascii").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		int packetLen_excl_Header = 25 + 4 * 3 + 8 * 2 + 2 + B7_ColStartP_F_LEN;
		packetLen_excl_Header	 += B9_ColStartP_CVADDR.packetLen();
		packetLen_excl_Header 	 += 2 + B10_ColDevModel_LEN;
		packetLen_excl_Header 	 += 8 + 8 + 4 + 1 + 4; //추가됨
		packetLen_excl_Header 	 += 1 + 2;
		for ( PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET e : B22_Payload )
		{
			packetLen_excl_Header += e.packetLen();
		}
		packetLen_excl_Header += 8+8;

		int packetLen_incl_Header = headerLen() + packetLen_excl_Header;

		H3_MSG_LEN = packetLen_excl_Header;

		ByteBuffer buf = ByteBuffer.allocate(packetLen_incl_Header);
		buf.order(ByteOrder.BIG_ENDIAN);

		buf.put(header());
		buf.put(B1_GID);
		buf.putInt(B2_GID_refP_Latitude);
		buf.putInt(B3_GID_refP_Longitude);
		buf.putInt(B4_GID_refP_Bearing);
		buf.putDouble(B5_ColStartP_X);
		buf.putDouble(B6_ColStartP_Y);

		buf.putShort(B7_ColStartP_F_LEN);
		buf.put(B8_ColStartP_F.getBytes());
		buf.put(B9_ColStartP_CVADDR.packet());
		buf.putShort(B10_ColDevModel_LEN);
		buf.put(B11_ColDevModel.getBytes());

		//추가됨
		buf.putDouble(B12_ColEndP_X);
		buf.putDouble(B13_ColEndP_Y);

		buf.putInt(B14_ColLinkID);
		buf.put(B15_ColLinkFlag);
		buf.putFloat(B16_ColLinkHeading);

		buf.put(B17_ColOpt);

		// 추가됨
		buf.putDouble(B18_ColStartP_GRS_X);
		buf.putDouble(B19_ColStartP_GRS_Y);
//		buf.putDouble(B20_ColStartP_TM_X);
//		buf.putDouble(B21_ColStartP_TM_Y);

		buf.putShort(B20_ColPoint_CNT);

		for ( PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET e : B22_Payload )
		{
			buf.put(e.packet());
		}

		return buf.array();
	}

	// magnetic
	public static PDI_REPT_COL_DATA decode ( ByteBuffer packet )
	{
		int offset = 0;
		PDI_REPT_COL_DATA z = new PDI_REPT_COL_DATA();

		byte[] buf = new byte[packet.capacity()];
		System.arraycopy(packet.array(), 0, buf, 0, packet.capacity());

		// Header
		z.decodeHeader ( packet );
		offset += z.headerLen();

		// B1_GID
		System.arraycopy ( buf, offset, z.B1_GID, 0, B1_GID_LEN );
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B1_GID=" + z.B1_GID() );
		offset += PDI_REPT_COL_DATA.B1_GID_LEN;

		// B2_GID_refP_Latitude
		ByteBuffer tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.B2_GID_refP_Latitude = tmpBuf.getInt(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B2_GID_refP_Latitude=" + z.B2_GID_refP_Latitude );
		offset += 4;

		// B3_GID_refP_Longitude
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 4 );
		z.B3_GID_refP_Longitude = tmpBuf.getInt(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B3_GID_refP_Longitude=" + z.B3_GID_refP_Longitude );
		offset += 4;

		// B4_GID_refP_Bearing
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 4 );
		z.B4_GID_refP_Bearing = tmpBuf.getInt(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B4_GID_refP_Bearing=" + z.B4_GID_refP_Bearing );
		offset += 4;

		// B5_ColStartP_Y
		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.B6_ColStartP_Y = tmpBuf.getDouble(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B6_ColStartP_Y=" + z.B6_ColStartP_Y );
		offset += 8;

		// B6_ColStartP_X
		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.B5_ColStartP_X = tmpBuf.getDouble(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B5_ColStartP_X=" + z.B5_ColStartP_X );
		offset += 8;

		// B7_ColStartP_F_LEN
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		z.B7_ColStartP_F_LEN = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B7_ColStartP_F_LEN=" + z.B7_ColStartP_F_LEN );
		offset += 2;

		// B8_ColStartP_F
		tmpBuf = ByteBuffer.allocate(z.B7_ColStartP_F_LEN);
		tmpBuf.put ( buf, offset, z.B7_ColStartP_F_LEN );
		try {
			z.B8_ColStartP_F = new String(tmpBuf.array(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B8_ColStartP_F=" + z.B8_ColStartP_F );
		offset += z.B7_ColStartP_F_LEN;

		// B9_ColStartP_CVADDR
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		short B9_ColStartP_CVADDR_LEN = (short) (tmpBuf.getShort(0));
		offset += 2;

		tmpBuf = ByteBuffer.allocate(B9_ColStartP_CVADDR_LEN);
		tmpBuf.put ( buf, offset, B9_ColStartP_CVADDR_LEN);
		z.B9_ColStartP_CVADDR = PDI_CIVIC_ADDR.decode ( tmpBuf );
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B9_ColStartP_CVADDR=" + z.B9_ColStartP_CVADDR.CVADDR() );
		offset += B9_ColStartP_CVADDR_LEN;

		// B10_ColDevModel_LEN
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		z.B10_ColDevModel_LEN = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B10_ColDevModel_LEN=" + z.B10_ColDevModel_LEN );
		offset += 2;

		// B11_ColDevModel
		tmpBuf = ByteBuffer.allocate(z.B10_ColDevModel_LEN);
		tmpBuf.put ( buf, offset, z.B10_ColDevModel_LEN );
		try {
			z.B11_ColDevModel = new String(tmpBuf.array(), "us-ascii");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B11_ColDevModel=" + z.B11_ColDevModel );
		offset += z.B10_ColDevModel_LEN;

		// B12 ColEndPointX
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 8 );
		z.B12_ColEndP_X =   tmpBuf.getDouble(0);
		//z.B12_ColEndP_X =Double.parseDouble(String.format("%.8f", tmpBuf.getDouble(0)));
		//double d = Double.parseDouble(String.format("%.10f", tmpBuf.getDouble(0)));

		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B12_ColEndP_X=" + z.B12_ColEndP_X );
		offset += 8;

		// B13 ColEndPointY
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 8 );
		z.B13_ColEndP_Y =tmpBuf.getDouble(0);
				//Double.parseDouble(String.format("%.8f", tmpBuf.getDouble(0)));
				//
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B13_ColEndP_Y=" + z.B13_ColEndP_Y );
		offset += 8;

//		Log.d("sp", "send start " + z.B5_ColStartP_X + "/" + z.B6_ColStartP_Y);
//		Log.d("sp", "send end   " + z.B12_ColEndP_X + "/" + z.B13_ColEndP_Y);

		// B14 ColLinkID
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 4 );
		z.B14_ColLinkID = tmpBuf.getInt(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), FD14_ColLinkID=" + z.B14_ColLinkID );
		offset += 4;

		// B15 ColLinkFlag
		z.B15_ColLinkFlag = buf[offset];
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), FD15_ColLinkFlag=" + z.B15_ColLinkFlag );
		offset += 1;

		// B16 ColLinkHeading
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 4 );
		z.B16_ColLinkHeading = tmpBuf.getFloat(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), FD16_ColLinkHeading=" + z.B16_ColLinkHeading );
		offset += 4;

		// B17_ColOpt
		z.B17_ColOpt = buf[offset];
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B17_ColOpt=" + z.B17_ColOpt );
		offset += 1;

		// B18_ColStartP_GRS_X
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 8 );
		z.B18_ColStartP_GRS_X = tmpBuf.getDouble(0);
		offset += 8;

		// B19_ColStartP_GRS_Y
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 8 );
		z.B19_ColStartP_GRS_Y = tmpBuf.getDouble(0);
		offset += 8;

		// B20_ColStartP_TM_X
//		tmpBuf = ByteBuffer.allocate(8);
//		tmpBuf.clear();
//		tmpBuf.put ( buf, offset, 8 );
//		z.B20_ColStartP_TM_X = tmpBuf.getDouble(0);
//		offset += 8;
//
//		// B21_ColStartP_TM_Y
//		tmpBuf = ByteBuffer.allocate(8);
//		tmpBuf.clear();
//		tmpBuf.put ( buf, offset, 8 );
//		z.B21_ColStartP_TM_Y = tmpBuf.getDouble(0);
//		offset += 8;

		// B22_ColPoint_CNT
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		z.B20_ColPoint_CNT = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode(), B18_ColPoint_CNT=" + z.B20_ColPoint_CNT );

		offset += 2;


		// B21_Payload
		for ( int i = 1; i <= z.B20_ColPoint_CNT; i++ )
		{
			tmpBuf = ByteBuffer.allocate(4);
			tmpBuf.put ( buf, offset, 4 );
			int PDI_REPT_COL_DATA_PAYLOAD_SIZE = tmpBuf.getInt(0);
//			System.out.println ( "  - PDI_REPT_COL_DATA_PAYLOAD [#" + i + "] SIZE=" + PDI_REPT_COL_DATA_PAYLOAD_SIZE );

			tmpBuf = ByteBuffer.allocate(PDI_REPT_COL_DATA_PAYLOAD_SIZE);
			tmpBuf.put ( buf, offset, PDI_REPT_COL_DATA_PAYLOAD_SIZE );
			PDI_REPT_COL_DATA_PAYLOAD p = PDI_REPT_COL_DATA_PAYLOAD.decode ( tmpBuf );

			offset += PDI_REPT_COL_DATA_PAYLOAD_SIZE;

			z.B21_Payload.add(p);
		}
		return z;
	}

	// no magnetic
	public static PDI_REPT_COL_DATA decode2 ( ByteBuffer packet )
	{
		int offset = 0;
		PDI_REPT_COL_DATA z = new PDI_REPT_COL_DATA();

		byte[] buf = new byte[packet.capacity()];
		System.arraycopy(packet.array(), 0, buf, 0, packet.capacity());

		// Header
		z.decodeHeader ( packet );
		offset += z.headerLen();

		// B1_GID
		System.arraycopy ( buf, offset, z.B1_GID, 0, B1_GID_LEN );
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B1_GID=" + z.B1_GID() );
		offset += PDI_REPT_COL_DATA.B1_GID_LEN;

		// B2_GID_refP_Latitude
		ByteBuffer tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		z.B2_GID_refP_Latitude = tmpBuf.getInt(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B2_GID_refP_Latitude=" + z.B2_GID_refP_Latitude );
		offset += 4;

		// B3_GID_refP_Longitude
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 4 );
		z.B3_GID_refP_Longitude = tmpBuf.getInt(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B3_GID_refP_Longitude=" + z.B3_GID_refP_Longitude );
		offset += 4;

		// B4_GID_refP_Bearing
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 4 );
		z.B4_GID_refP_Bearing = tmpBuf.getInt(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B4_GID_refP_Bearing=" + z.B4_GID_refP_Bearing );
		offset += 4;

		// B5_ColStartP_X
		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.B5_ColStartP_X = tmpBuf.getDouble(0);//  - (int)ResultList_Path_Act.BaseX;
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B5_ColStartP_X=" + z.B5_ColStartP_X );
		offset += 8;

		// B6_ColStartP_Y
		tmpBuf.clear();
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.put ( buf, offset, 8 );
		z.B6_ColStartP_Y = tmpBuf.getDouble(0);// - (int)ResultList_Path_Act.BaseY;
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B6_ColStartP_Y=" + z.B6_ColStartP_Y );
		offset += 8;

		// B7_ColStartP_F_LEN
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		z.B7_ColStartP_F_LEN = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B7_ColStartP_F_LEN=" + z.B7_ColStartP_F_LEN );
		offset += 2;

		// B8_ColStartP_F
		tmpBuf = ByteBuffer.allocate(z.B7_ColStartP_F_LEN);
		tmpBuf.put ( buf, offset, z.B7_ColStartP_F_LEN );
		try {
			z.B8_ColStartP_F = new String(tmpBuf.array(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B8_ColStartP_F=" + z.B8_ColStartP_F );
		offset += z.B7_ColStartP_F_LEN;

		// B9_ColStartP_CVADDR
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		short B9_ColStartP_CVADDR_LEN = (short) (tmpBuf.getShort(0));
		offset += 2;

		tmpBuf = ByteBuffer.allocate(B9_ColStartP_CVADDR_LEN);
		tmpBuf.put ( buf, offset, B9_ColStartP_CVADDR_LEN);
		z.B9_ColStartP_CVADDR = PDI_CIVIC_ADDR.decode ( tmpBuf );
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B9_ColStartP_CVADDR=" + z.B9_ColStartP_CVADDR.CVADDR() );
		offset += B9_ColStartP_CVADDR_LEN;

		// B10_ColDevModel_LEN
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		z.B10_ColDevModel_LEN = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B10_ColDevModel_LEN=" + z.B10_ColDevModel_LEN );
		offset += 2;

		// B11_ColDevModel
		tmpBuf = ByteBuffer.allocate(z.B10_ColDevModel_LEN);
		tmpBuf.put ( buf, offset, z.B10_ColDevModel_LEN );
		try {
			z.B11_ColDevModel = new String(tmpBuf.array(), "us-ascii");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B11_ColDevModel=" + z.B11_ColDevModel );
		offset += z.B10_ColDevModel_LEN;

		// B17_ColOpt
		z.B17_ColOpt = buf[offset];
		offset += 1;

		// B18_ColStartP_GRS_X
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 8 );
		z.B18_ColStartP_GRS_X = tmpBuf.getDouble(0);
		offset += 8;

		// B19_ColStartP_GRS_Y
		tmpBuf = ByteBuffer.allocate(8);
		tmpBuf.clear();
		tmpBuf.put ( buf, offset, 8 );
		z.B19_ColStartP_GRS_Y = tmpBuf.getDouble(0);
		offset += 8;

		// B20_ColStartP_TM_X
//		tmpBuf = ByteBuffer.allocate(8);
//		tmpBuf.clear();
//		tmpBuf.put ( buf, offset, 8 );
//		z.B20_ColStartP_TM_X = tmpBuf.getDouble(0);
//		offset += 8;
//
//		// B21_ColStartP_TM_Y
//		tmpBuf = ByteBuffer.allocate(8);
//		tmpBuf.clear();
//		tmpBuf.put ( buf, offset, 8 );
//		z.B21_ColStartP_TM_Y = tmpBuf.getDouble(0);
//		offset += 8;

		// B22_ColPoint_CNT
		tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		z.B20_ColPoint_CNT = tmpBuf.getShort(0);
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), B18_ColPoint_CNT=" + z.B20_ColPoint_CNT );
		offset += 2;

		// B21_Payload
		System.out.println ( "  - PDI_REPT_COL_DATA, decode2(), (total recv - offset)=" + (packet.capacity() - offset) );

		for ( int i = 1; i <= z.B20_ColPoint_CNT; i++ )
		{
			tmpBuf = ByteBuffer.allocate(4);
			tmpBuf.put ( buf, offset, 4 );
			int PDI_REPT_COL_DATA_PAYLOAD_SIZE = tmpBuf.getInt(0);
			System.out.println ( " 2 - PDI_REPT_COL_DATA_PAYLOAD [#" + i + "] SIZE=" + PDI_REPT_COL_DATA_PAYLOAD_SIZE );

			tmpBuf = ByteBuffer.allocate(PDI_REPT_COL_DATA_PAYLOAD_SIZE);
			tmpBuf.put ( buf, offset, PDI_REPT_COL_DATA_PAYLOAD_SIZE );
			PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET p = PDI_REPT_COL_DATA_PAYLOAD_NOMAGNET.decode ( tmpBuf );

			offset += PDI_REPT_COL_DATA_PAYLOAD_SIZE;

			z.B22_Payload.add(p);
		}
		return z;
	}
}
