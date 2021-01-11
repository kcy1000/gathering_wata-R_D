package GEO2.LBSP.SClient;

import java.nio.ByteBuffer;

import GEO2.LBSP.SClient.SvrPositionProvider.*;


public class RspPosition {
	
	public static final int		ESTIMATE_SYSTEM_FAILED = -100;
	public static final int		ESTIMATE_FLOOR_EST_FAILED = -101;
	public static final int		ESTIMATE_INFRA_FIND_FAILED = -102;
	public static final int		ESTIMATE_INFRA_MATCH_FAILED = -103;
	public static final int		ESTIMATE_DB_FAILED = -104;
	public static final int		SERVER_NOT_CONNECTED = -105;
	public static final int	 	SEND_INFRAINFO_FAILED = -106;
	
	boolean validation;
	
	public String GID;
	public double x;
	public double y;
	public double matchRatio;
	
	public String strFloor;
	public String address;
	public int errorCode = 0;
	public String strError = "";
	
	public RspPosition() 
	{
		validation = false;
	}
	
	public void decode(ByteBuffer packet, COORDTYPE type) throws Exception
	{
//		if(type != COORDTYPE.emReferenced)
//		{
//			decode(packet);
//			return;
//		}
		int nPos = 0;
		
		{
			ByteBuffer tmpBuf = ByteBuffer.allocate(25);
			tmpBuf.put ( packet.array(), 0, 25);
			GID = new String(tmpBuf.array(), "us-ascii");
		}
		x = packet.getDouble(25);
		y = packet.getDouble(33);
		matchRatio = packet.getDouble(41);
		
		// floor
		int nSz = packet.getInt(49);
		if(nSz > 0) {			
			ByteBuffer tmpBuf = ByteBuffer.allocate(nSz);
			tmpBuf.put ( packet.array(), 53, nSz );
			strFloor = new String(tmpBuf.array(), "us-ascii");
		}
		
		// address
		nPos = 53 + nSz;
		nSz = packet.getInt(nPos);
		nPos += 4;
		if(nSz > 0) {			
			ByteBuffer tmpBuf = ByteBuffer.allocate(nSz);
			tmpBuf.put ( packet.array(), nPos, nSz );
			address = new String(tmpBuf.array(), "utf-8");//"us-ascii");
		}

		// error contents.
		nPos += nSz;
		errorCode = packet.getInt(nPos);
		if(errorCode != 0)
		{
			nPos += 4;
			nSz = packet.getInt(nPos);
			nPos += 4;
			if(nSz > 0) {			
				ByteBuffer tmpBuf = ByteBuffer.allocate(nSz);
				tmpBuf.put ( packet.array(), nPos, nSz );
				strError = new String(tmpBuf.array(), "us-ascii");
			}
		}

	}
	
	public void decode(ByteBuffer packet) throws Exception
	{
		int nPos = 0;
		
		{
			ByteBuffer tmpBuf = ByteBuffer.allocate(12);
			tmpBuf.put ( packet.array(), 0, 12);
			GID = new String(tmpBuf.array(), "us-ascii");
		}
		
		x = packet.getDouble(12);
		y = packet.getDouble(20);
		matchRatio = packet.getDouble(28);
		int nSz = packet.getInt(36);
		if(nSz > 0) {			
			ByteBuffer tmpBuf = ByteBuffer.allocate(nSz);
			tmpBuf.put ( packet.array(), 40, nSz );
			strFloor = new String(tmpBuf.array(), "us-ascii");
		}
		
		nPos = 40 + nSz;
		errorCode = packet.getInt(nPos);
		nPos += 4;
		nSz = packet.getInt(nPos);
		nPos += 4;
		if(nSz > 0) {			
			ByteBuffer tmpBuf = ByteBuffer.allocate(nSz);
			tmpBuf.put ( packet.array(), nPos, nSz );
			strError = new String(tmpBuf.array(), "us-ascii");
		}
	}
}
