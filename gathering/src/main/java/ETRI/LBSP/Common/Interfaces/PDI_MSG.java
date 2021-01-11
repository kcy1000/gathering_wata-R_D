package ETRI.LBSP.Common.Interfaces;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PDI_MSG
{
	public static final int H1_PSNID_LEN = 16;
	public static final int MSG_HEADER_LEN = H1_PSNID_LEN + 2 + 4;	
	
	public static final short MSGID_REPT_COL_DATA	= 0x42BA; 
	public static final short MSGID_REQ_POS_DB		= 0x11C1;	
	
	public byte[]	H1_PSNID;
	public short	H2_MSGTYPE;
	public int		H3_MSG_LEN;

	public PDI_MSG()
	{
		H1_PSNID = new byte[H1_PSNID_LEN];
	}
	
	// set mothods
	public PDI_MSG ( String PersonalID, short MsgType ) 
	{
		H1_PSNID = new byte[H1_PSNID_LEN];
		int PersonalID_len = PersonalID.length();

		System.arraycopy ( PersonalID.getBytes(), 0, H1_PSNID, 0, PersonalID_len < H1_PSNID_LEN ? PersonalID_len : H1_PSNID_LEN);
		
		H2_MSGTYPE = MsgType;
	}
	
	// get methods
	public String H1_PSNID()
	{
		return new String ( H1_PSNID );
	}

	public String H2_MSGTYPE()
	{
		switch ( H2_MSGTYPE )
		{
		case MSGID_REPT_COL_DATA: return "REPT_COL_DATA (0x42BA)";
		case MSGID_REQ_POS_DB: return "REQ_POS_DB (0x11C1)";
		}
		
		return "Unknown";
	}
	
	public byte[] header()
	{
		int length = headerLen();
		
		ByteBuffer buf = ByteBuffer.allocate(length);
		buf.order(ByteOrder.BIG_ENDIAN);
		
		buf.put(H1_PSNID);
		buf.putShort(H2_MSGTYPE);
		buf.putInt(H3_MSG_LEN);
		
		return buf.array();
	}
	
	public short headerLen()
	{
		return (short) (16 + 2 + 4);
	}
	
	public void decodeHeader ( ByteBuffer packet )
	{
		int offset = 0;

		byte[] buf = new byte[MSG_HEADER_LEN];
		System.arraycopy(packet.array(), 0, buf, 0, MSG_HEADER_LEN);
		
		// H1_PSNID
		System.arraycopy ( buf, offset, H1_PSNID, 0, H1_PSNID_LEN );
		offset += H1_PSNID_LEN;
		
		// H2_MSGTYPE
		ByteBuffer tmpBuf = ByteBuffer.allocate(2);
		tmpBuf.put ( buf, offset, 2 );
		H2_MSGTYPE = tmpBuf.getShort(0);
		offset += 2;
		
		// H3_MSG_LEN
		tmpBuf = ByteBuffer.allocate(4);
		tmpBuf.put ( buf, offset, 4 );
		H3_MSG_LEN = tmpBuf.getInt(0);
		
		System.out.println ( "  - PDI_MSG_HEADER, decode(), H1_PSNID=" + H1_PSNID() );
		System.out.println ( "  - PDI_MSG_HEADER, decode(), H2_MSGTYPE=" + H2_MSGTYPE() );
		System.out.println ( "  - PDI_MSG_HEADER, decode(), H3_MSG_LEN=" + H3_MSG_LEN );
	}	
}
