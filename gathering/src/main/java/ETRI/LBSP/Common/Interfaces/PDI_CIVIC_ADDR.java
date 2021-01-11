package ETRI.LBSP.Common.Interfaces;

import com.geotwo.LAB_TEST.Gathering.GatheringActivity;
import com.geotwo.LAB_TEST.data.StaticManager;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

//PDI: Pre-defined Datatypes for Interface
//Type: PDI_CIVIC_ADDR
public class PDI_CIVIC_ADDR 
{
	int		FD1_CVADDR_LEN;
	String	FD2_CVADDR_LV1 = "와";
	String	FD3_CVADDR_LV2;
	String	FD4_CVADDR_LV3 = "00";
	String	FD5_CVADDR_LV4;
	String	FD6_CVADDR_LV5;
	String	FD7_CVADDR_LV6;
	String	FD8_CVADDR_LV7;
	
	public PDI_CIVIC_ADDR()
	{
		FD1_CVADDR_LEN = 0;
		FD2_CVADDR_LV1 = "와따";
		FD3_CVADDR_LV2 = "";
		FD4_CVADDR_LV3 = "00";
		FD5_CVADDR_LV4 = "";
		FD6_CVADDR_LV5 = "";
		FD7_CVADDR_LV6 = "";
		FD8_CVADDR_LV7 = "";
	}
	
	public void CVADDR ( String address )
	{
		String[] subAddr = null;
		if(address != null)
			subAddr = address.split(" ");
		else
		{
			subAddr = new String[7];
			for ( int i = 0; i < subAddr.length; i++ )
				subAddr[i] = "";
		}
		try
		{
			for ( int i = 0; i < subAddr.length; i++ )
			{
				switch ( i )
				{
				case 0: FD2_CVADDR_LV1 = "와따";//subAddr[i].trim(); break;
				case 1: FD3_CVADDR_LV2 = subAddr[i].trim(); break;
				case 2: FD4_CVADDR_LV3 = String.valueOf(GatheringActivity.mCurrentPath);//subAddr[i].trim(); break;
				case 3: 
					if(StaticManager.worker != null && StaticManager.worker.equalsIgnoreCase("PILOT_KISA"))
						FD5_CVADDR_LV4 = String.valueOf(GatheringActivity._startPoint.x); 
					else
						FD5_CVADDR_LV4 = subAddr[i].trim(); 
					break;
				case 4:
					if(StaticManager.worker != null && StaticManager.worker.equalsIgnoreCase("PILOT_KISA"))
						FD6_CVADDR_LV5 = String.valueOf(GatheringActivity._startPoint.y); 
					else
						FD6_CVADDR_LV5 = subAddr[i].trim(); 
					break;
				case 5:
//					if(StaticManager.worker != null && StaticManager.worker.equalsIgnoreCase("PILOT_KISA"))
//						FD7_CVADDR_LV6 = SimpleGatheringActivity.mPlace;
//					else
//						FD7_CVADDR_LV6 = subAddr[i].trim();
					break;
				case 6: FD8_CVADDR_LV7 = subAddr[i].trim(); break;
				}
			}
		}
		catch ( Exception e )
		{
			
		}
		
//		try {
//			FD1_CVADDR_LEN  = FD2_CVADDR_LV1.getBytes("utf-8").length;
//			FD1_CVADDR_LEN += FD3_CVADDR_LV2.getBytes("utf-8").length;
//			FD1_CVADDR_LEN += FD4_CVADDR_LV3.getBytes("utf-8").length;
//			FD1_CVADDR_LEN += FD5_CVADDR_LV4.getBytes("utf-8").length;
//			FD1_CVADDR_LEN += FD6_CVADDR_LV5.getBytes("utf-8").length;
//			FD1_CVADDR_LEN += FD7_CVADDR_LV6.getBytes("utf-8").length;
//			FD1_CVADDR_LEN += FD8_CVADDR_LV7.getBytes("utf-8").length;
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
	}

	// get methods
	public String toString()
	{
		String CVaddr = FD2_CVADDR_LV1 + "/"
				+ FD3_CVADDR_LV2 + "/"
				+ FD4_CVADDR_LV3 + "/"
				+ FD5_CVADDR_LV4 + "/" 
				+ FD6_CVADDR_LV5 + "/"
				+ FD7_CVADDR_LV6 + "/"
				+ FD8_CVADDR_LV7 + ".";
		
		return CVaddr;
	}
	
	public PDI_CIVIC_ADDR CVADDR() { return this; }
	public String CVADDR_LV1() { return FD2_CVADDR_LV1; }
	public String CVADDR_LV2() { return FD3_CVADDR_LV2; }
	public String CVADDR_LV3() { return FD4_CVADDR_LV3; }
	public String CVADDR_LV4() { return FD5_CVADDR_LV4; }
	public String CVADDR_LV5() { return FD6_CVADDR_LV5; }
	public String CVADDR_LV6() { return FD7_CVADDR_LV6; }
	public String CVADDR_LV7() { return FD8_CVADDR_LV7; }
	
	public byte[] packet()
	{
		try {
			FD1_CVADDR_LEN = 0;
			FD1_CVADDR_LEN  = FD2_CVADDR_LV1.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD3_CVADDR_LV2.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD4_CVADDR_LV3.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD5_CVADDR_LV4.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD6_CVADDR_LV5.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD7_CVADDR_LV6.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD8_CVADDR_LV7.getBytes("utf-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		short length = (short) (2 + 1 * 7 + FD1_CVADDR_LEN);
		
		ByteBuffer buf = ByteBuffer.allocate(length);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		buf.putShort((short) (1 * 7 + FD1_CVADDR_LEN));
		try {
			buf.put((byte) FD2_CVADDR_LV1.getBytes("utf-8").length);
			if ( FD2_CVADDR_LV1.getBytes("utf-8").length > 0 ) buf.put(FD2_CVADDR_LV1.getBytes("utf-8"));
			buf.put((byte) FD3_CVADDR_LV2.getBytes("utf-8").length);
			if ( FD3_CVADDR_LV2.getBytes("utf-8").length > 0 ) buf.put(FD3_CVADDR_LV2.getBytes("utf-8"));
			buf.put((byte) FD4_CVADDR_LV3.getBytes("utf-8").length);
			if ( FD4_CVADDR_LV3.getBytes("utf-8").length > 0 ) buf.put(FD4_CVADDR_LV3.getBytes("utf-8"));
			buf.put((byte) FD5_CVADDR_LV4.getBytes("utf-8").length);
			if ( FD5_CVADDR_LV4.getBytes("utf-8").length > 0 ) buf.put(FD5_CVADDR_LV4.getBytes("utf-8"));
			buf.put((byte) FD6_CVADDR_LV5.getBytes("utf-8").length);
			if ( FD6_CVADDR_LV5.getBytes("utf-8").length > 0 ) buf.put(FD6_CVADDR_LV5.getBytes("utf-8"));
			buf.put((byte) FD7_CVADDR_LV6.getBytes("utf-8").length);
			if ( FD7_CVADDR_LV6.getBytes("utf-8").length > 0 ) buf.put(FD7_CVADDR_LV6.getBytes("utf-8"));
			buf.put((byte) FD8_CVADDR_LV7.getBytes("utf-8").length);
			if ( FD8_CVADDR_LV7.getBytes("utf-8").length > 0 ) buf.put(FD8_CVADDR_LV7.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return buf.array();
	}
	
	public short packetLen()
	{
		try {
			FD1_CVADDR_LEN = 0;
			FD1_CVADDR_LEN  = FD2_CVADDR_LV1.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD3_CVADDR_LV2.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD4_CVADDR_LV3.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD5_CVADDR_LV4.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD6_CVADDR_LV5.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD7_CVADDR_LV6.getBytes("utf-8").length;
			FD1_CVADDR_LEN += FD8_CVADDR_LV7.getBytes("utf-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return (short) (2 + 1 * 7 + FD1_CVADDR_LEN);
	}
	
	public static PDI_CIVIC_ADDR decode ( ByteBuffer packet )
	{
		int offset = 0;
		PDI_CIVIC_ADDR z = new PDI_CIVIC_ADDR();
				
		byte[] buf = new byte[packet.capacity()];
		System.arraycopy(packet.array(), 0, buf, 0, packet.capacity());
		z.FD1_CVADDR_LEN = 0;
    
		try {		
			// FD2_CVADDR_LV1
			int FD2_CVADDR_LV1_LEN = buf[offset];
			offset += 1;
			ByteBuffer tmpBuf = ByteBuffer.allocate(FD2_CVADDR_LV1_LEN);
			tmpBuf.put ( buf, offset, FD2_CVADDR_LV1_LEN );
			z.FD2_CVADDR_LV1 = "와따";//new String(tmpBuf.array(), "utf-8");
			z.FD1_CVADDR_LEN += FD2_CVADDR_LV1_LEN;
			offset += FD2_CVADDR_LV1_LEN;
			
			// FD3_CVADDR_LV2
			int FD3_CVADDR_LV2_LEN = buf[offset];
			offset += 1;
			tmpBuf = ByteBuffer.allocate(FD3_CVADDR_LV2_LEN);
			tmpBuf.put ( buf, offset, FD3_CVADDR_LV2_LEN );
			z.FD3_CVADDR_LV2 = new String(tmpBuf.array(), "utf-8");
			z.FD1_CVADDR_LEN += FD3_CVADDR_LV2_LEN;
			offset += FD3_CVADDR_LV2_LEN;
			
			// FD4_CVADDR_LV3
			int FD4_CVADDR_LV3_LEN = buf[offset];
			offset += 1;
			tmpBuf = ByteBuffer.allocate(FD4_CVADDR_LV3_LEN);
			tmpBuf.put ( buf, offset, FD4_CVADDR_LV3_LEN );
			z.FD4_CVADDR_LV3 = new String(tmpBuf.array(), "utf-8");
			z.FD1_CVADDR_LEN += FD4_CVADDR_LV3_LEN;
			offset += FD4_CVADDR_LV3_LEN;
			
			// FD5_CVADDR_LV4
			int FD5_CVADDR_LV4_LEN = buf[offset];
			offset += 1;
			tmpBuf = ByteBuffer.allocate(FD5_CVADDR_LV4_LEN);
			tmpBuf.put ( buf, offset, FD5_CVADDR_LV4_LEN );
			z.FD5_CVADDR_LV4 = new String(tmpBuf.array(), "utf-8");
			z.FD1_CVADDR_LEN += FD5_CVADDR_LV4_LEN;			
			offset += FD5_CVADDR_LV4_LEN;
			
			// FD6_CVADDR_LV5
			int FD6_CVADDR_LV5_LEN = buf[offset];
			offset += 1;
			tmpBuf = ByteBuffer.allocate(FD6_CVADDR_LV5_LEN);
			tmpBuf.put ( buf, offset, FD6_CVADDR_LV5_LEN );
			z.FD6_CVADDR_LV5 = new String(tmpBuf.array(), "utf-8");
			z.FD1_CVADDR_LEN += FD6_CVADDR_LV5_LEN;			
			offset += FD6_CVADDR_LV5_LEN;
			
			// FD7_CVADDR_LV6
			int FD7_CVADDR_LV6_LEN = buf[offset];
			offset += 1;
			tmpBuf = ByteBuffer.allocate(FD7_CVADDR_LV6_LEN);
			tmpBuf.put ( buf, offset, FD7_CVADDR_LV6_LEN );
			z.FD7_CVADDR_LV6 = new String(tmpBuf.array(), "utf-8");
			z.FD1_CVADDR_LEN += FD7_CVADDR_LV6_LEN;			
			offset += FD7_CVADDR_LV6_LEN;
			
			// FD8_CVADDR_LV7
			int FD8_CVADDR_LV7_LEN = buf[offset];
			offset += 1;
			tmpBuf = ByteBuffer.allocate(FD8_CVADDR_LV7_LEN);
			tmpBuf.put ( buf, offset, FD8_CVADDR_LV7_LEN );
			z.FD8_CVADDR_LV7 = new String(tmpBuf.array(), "utf-8");
			offset += FD8_CVADDR_LV7_LEN;
			z.FD1_CVADDR_LEN += FD8_CVADDR_LV7_LEN;			
	
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return z;
	}
}
