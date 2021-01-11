package com.geotwo.LAB_TEST.Gathering.savedata;

import com.geotwo.LAB_TEST.Gathering.DataCore;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA;
import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA_VOUCHER;


public class LoadGatheredData {
	
	static public ArrayList<File> getSavedSendableData()
	{
		ArrayList<File> fileNameList = new ArrayList<File>();
		DataCore dataCore = DataCore.getInstance();
		
		
		String dataPathString = dataCore.getTempDataPath();
		
		File dataPath = new File(dataPathString);
		
		File[] files = null;
		if(dataPath.exists() == true)
		{
			files =  dataPath.listFiles();
		}
			
		if(files != null) {
			for(int i = 0 ; i < files.length ; i++) {
				if(files[i].getName().endsWith("temp"))	{
					fileNameList.add(files[i]);
				}
			}
		}
		
		return fileNameList;
	}

	static public ArrayList<File> getSavedSendableLocationData()
	{
		ArrayList<File> fileNameList = new ArrayList<File>();
		DataCore dataCore = DataCore.getInstance();


		String dataPathString = dataCore.getLocationLogDataPath();

		File dataPath = new File(dataPathString);

		File[] files = null;
		if(dataPath.exists() == true)
		{
			files =  dataPath.listFiles();
		}

		if(files != null)
		{
			for(int i = 0 ; i < files.length ; i++)
			{

				if(files[i].getName().endsWith("txt"))
				{
					fileNameList.add(files[i]);
				}
			}
		}

		return fileNameList;
	}

	static public ArrayList<File> getSavedSendableLogData()
	{
		ArrayList<File> fileNameList = new ArrayList<File>();
		DataCore dataCore = DataCore.getInstance();


		String dataPathString = dataCore.getLocationLogDataPath();

		File dataPath = new File(dataPathString);

		File[] files = null;
		if(dataPath.exists() == true)
		{
			files =  dataPath.listFiles();
		}

		if(files != null)
		{
			for(int i = 0 ; i < files.length ; i++)
			{

				if(files[i].getName().endsWith("txt"))
				{
					fileNameList.add(files[i]);
				}
			}
		}

		return fileNameList;
	}

	static public PDI_REPT_COL_DATA decodeSavedSendableData(File loadFile)
	{
		PDI_REPT_COL_DATA data = null;
		
		try {
			
			byte[] fileByte = fileToByteArray(loadFile);
			
			ByteBuffer byteBuffer = ByteBuffer.wrap(fileByte);
			
			if(loadFile.getName().substring(loadFile.getName().indexOf(".")+1, loadFile.getName().length()).equals("temp"))
				data = PDI_REPT_COL_DATA.decode(byteBuffer);
			else if(loadFile.getName().substring(loadFile.getName().indexOf(".")+1, loadFile.getName().length()).equals("temp_nomag"))
				data = PDI_REPT_COL_DATA.decode2(byteBuffer);
			WataLog.d( "log name="+loadFile.getName());

		} catch (Exception e) {
			e.printStackTrace();
			WataLog.e("Exception=" + e.toString());
		}
		
		return data;
	}


	static public PDI_REPT_COL_DATA_VOUCHER decodeSavedSendableVoucherData(File loadFile)
	{
		PDI_REPT_COL_DATA_VOUCHER data = null;

		try {

			byte[] fileByte = fileToByteArray(loadFile);

			ByteBuffer byteBuffer = ByteBuffer.wrap(fileByte);

			if(loadFile.getName().substring(loadFile.getName().indexOf(".")+1, loadFile.getName().length()).equals("temp"))
				data = PDI_REPT_COL_DATA_VOUCHER.decode(byteBuffer);
			else if(loadFile.getName().substring(loadFile.getName().indexOf(".")+1, loadFile.getName().length()).equals("temp_nomag"))
				data = PDI_REPT_COL_DATA_VOUCHER.decode2(byteBuffer);
			WataLog.d( "log name="+loadFile.getName());

		} catch (Exception e) {
			e.printStackTrace();
			WataLog.e("Exception=" + e.toString());
		}

		return data;
	}
	
	
	static private byte[] fileToByteArray(File loadFile)
	{
		InputStream is;
		byte[] bytes = null;
		
		try {
			
			is = new FileInputStream(loadFile);
			
			// Get the size of the file
	        long length = loadFile.length();
	    
	        if (length > Integer.MAX_VALUE) {
	            // File is too large
	        }
	    
	        // Create the byte array to hold the data
	        bytes = new byte[(int)length];
	    
	        // Read in the bytes
	        int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length
	               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	            offset += numRead;
	        }
	    
	        // Ensure all the bytes have been read in
	        if (offset < bytes.length) {
	            throw new IOException("Could not completely read file " + loadFile.getName());
	        }
	    
	        // Close the input stream and return bytes
	        is.close();
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bytes;
        
	}
	
	
	
}
