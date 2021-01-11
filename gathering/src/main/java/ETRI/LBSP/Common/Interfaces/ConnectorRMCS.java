package ETRI.LBSP.Common.Interfaces;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ConnectorRMCS
{
	Socket							ServerConnector;
	String							ServerAddress;
	final String 					RMCSSendSocketTAG = "RMCS Socket DBG";
//	final int                       default_port = 8002;
	final int                       default_port = 8004;
	private static final int 		BUFFER_SIZE = 1024;
	int 							Port = 8002;


	public ConnectorRMCS ( String _ServerAddress, int _Port)
	{
		ServerAddress = _ServerAddress;
		Port = _Port;
		if(Port == 0)
			Port = default_port;
	}

	public void connect()
	{
		String tempAddress = ServerAddress.substring(ServerAddress.length()-1, ServerAddress.length());
		if(tempAddress.equals(":"))
		{
			ServerAddress = ServerAddress.substring(0, ServerAddress.length()-1);
		}
		WataLog.e("after ServerAddress = "+ServerAddress);
		try
		{
			InetAddress serverAddr = InetAddress.getByName(ServerAddress);	// Formax test server
			ServerConnector = new Socket(serverAddr, Port);
			WataLog.d( "connect success !!!");
		} catch ( IOException e ) {
			WataLog.e("connect failed... " + e.toString());
		}
	}

	public void disconnect()
	{
		try
		{
			ServerConnector.close();
		}
		catch ( IOException e ) {

		}
	}

	public void sendMessage ( PDI_REPT_COL_DATA REPT_COL_DATA , int type)
	{
		try
		{
			System.out.println ( "[RMCSSendSocketTAG] Send ScanResults to RMCS." );

			BufferedOutputStream bOut = new BufferedOutputStream ( ServerConnector.getOutputStream(), BUFFER_SIZE );

			byte[] sendData = null;

			if(type == 0)
				sendData = REPT_COL_DATA.packet();
			else if(type == 1)
				sendData = REPT_COL_DATA.packet_nomagnet();

			WataLog.d("sendData=" + sendData);
			System.out.println ( "[RMCSSendSocketTAG] Data length to send = " + sendData.length );

			int offset = 0;
			int sendDataLen = 0;
			while ( sendData != null )
			{
				if ( offset >= sendData.length ) break;

				if ( sendData.length - offset > BUFFER_SIZE ) sendDataLen = BUFFER_SIZE;
				else sendDataLen = sendData.length - offset;

				byte[] sendBuf = new byte[sendDataLen];
				System.arraycopy ( sendData, offset, sendBuf, 0, sendDataLen );
				offset += sendDataLen;

				bOut.write ( sendBuf );
				bOut.flush();
			}
		}
		catch ( IOException e ) {
			e.printStackTrace();
			WataLog.d("send failed...");
		}
		finally
		{
			try
			{
				ServerConnector.close();
			}
			catch ( IOException e ) {
				e.printStackTrace();
			}
		}
	}
}