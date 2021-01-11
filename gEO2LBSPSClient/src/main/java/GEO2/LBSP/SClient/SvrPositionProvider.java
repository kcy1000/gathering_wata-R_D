package GEO2.LBSP.SClient;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class SvrPositionProvider 
{
	public enum COORDTYPE {
		emMiddleTM ,
		emWGS84,
		emReferenced
	}
	
	final int 									FD1_VER = 1;
	public ArrayList<PDI_SCANINFO>				FD2_SCANINFO_A_List;
	private Socket								ServerConnector;
	private String								ServerAddress;
	private static final int 					BUFFER_SIZE = 1024;
	private Thread								socketThread;
	public RspPosition							reqOutdoorPos = new RspPosition();
	
	private COORDTYPE							coordType = COORDTYPE.emWGS84;
	private int 								port = 2919;
	final int                       			default_port = 8002;
			
	public void setCoordType(COORDTYPE type)
	{
		coordType = type;
	}
	
	public SvrPositionProvider(String address, int _port)
	{
		ServerAddress = address;
		port = _port;
		if(port == 0){
			port = default_port;
		}
		FD2_SCANINFO_A_List = new ArrayList<PDI_SCANINFO>();
		
		socketThread = new Thread(new Runnable()
        {
			public void run()
			{
				if(!connect())
					return;
				if(sendInfraInfo()) {
					readPos();
				}
				
				disconnect();
			}
		});
	}

	private void readPos()
	{
		try {
			byte[] recvBuf = new byte[BUFFER_SIZE];
			BufferedInputStream bIn = new BufferedInputStream ( ServerConnector.getInputStream(), BUFFER_SIZE );
			int readBufLen = bIn.read ( recvBuf );
			if(readBufLen > 0)
			{
				ByteBuffer tmpBuf = ByteBuffer.allocate(readBufLen);
				tmpBuf.order(ByteOrder.BIG_ENDIAN);
				tmpBuf.put(recvBuf, 0, readBufLen);
				reqOutdoorPos.decode(tmpBuf, coordType);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private byte[] packet()
	{	
		int length = packetLen();
		ByteBuffer buf = ByteBuffer.allocate(length + 12);
		short v = (short)FD2_SCANINFO_A_List.size();
		buf.putInt(FD1_VER);
		buf.putShort((short)coordType.ordinal());
		
		buf.putShort(v);
		buf.putInt(length);
		
		for ( PDI_SCANINFO e : FD2_SCANINFO_A_List ) {
			buf.put(e.packet());
		}
		return buf.array();
	}
	
	private int packetLen()
	{
		int len = 0;  
		for ( PDI_SCANINFO e : FD2_SCANINFO_A_List ) {
			len += e.packetLen();
		}
		return len;
	}
	
	public void clear()
	{
		FD2_SCANINFO_A_List.clear();
	}

	public RspPosition requestPos()
	{
		socketThread.start();
		
		try {
			while(socketThread.isAlive()) {
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reqOutdoorPos;
	}
	
	public void addWifiScanInfo(ArrayList<PDI_SCANINFO>	lstScanInfo)
	{
		FD2_SCANINFO_A_List.clear();
		FD2_SCANINFO_A_List.addAll(lstScanInfo);
	}
	
	private boolean sendInfraInfo()
	{
		byte[] sendData = packet();
		int offset = 0;
			
		int sendDataLen = 0;
		
		try {
			BufferedOutputStream bOut = new BufferedOutputStream ( ServerConnector.getOutputStream(), BUFFER_SIZE );
			while ( true ) {
				if ( offset >= sendData.length ) 
					break;
				
				if ( sendData.length - offset > BUFFER_SIZE ) 
					sendDataLen = BUFFER_SIZE;
				else 
					sendDataLen = sendData.length - offset;
	
				byte[] sendBuf = new byte[sendDataLen];				
				System.arraycopy ( sendData, offset, sendBuf, 0, sendDataLen );
				offset += sendDataLen;
	
				bOut.write ( sendBuf );
				bOut.flush();				
			}
		} catch (Exception e) {
			reqOutdoorPos.errorCode = RspPosition.SEND_INFRAINFO_FAILED;
			reqOutdoorPos.strError = "Failed to transmit infrainfo.";
			return false;
		}
		
		return true;
	}
	
	private boolean connect()
	{
		try
		{
			ServerConnector = new Socket();
			ServerConnector.connect(new InetSocketAddress(ServerAddress, port), 1000);
			ServerConnector.setTcpNoDelay(true);
		} catch ( IOException e ) {
			System.out.println(e.getMessage());
			reqOutdoorPos.errorCode = RspPosition.SERVER_NOT_CONNECTED;
			reqOutdoorPos.strError = "Can not connect to TPS.";
			return false;
		}
		return true;
	}
	
	private void disconnect()
	{
		try
		{
			if(ServerConnector != null && ServerConnector.isConnected()) {
				ServerConnector.close();
			}
			ServerConnector = null;
		}
		catch ( IOException e ) {

		}
	}
}








