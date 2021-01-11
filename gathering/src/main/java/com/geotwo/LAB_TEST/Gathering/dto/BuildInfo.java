package com.geotwo.LAB_TEST.Gathering.dto;

import java.util.Vector;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

public class BuildInfo
{
	private String startFloor = "";
	private String endFloor = "";
	private String fileName = "";
	
	private String GIDex = "";
	private String Latitude = "";
	private String Longitude = "";
	private String Bearing = "";
	private String Addr = ""; 


	public String getAddr()
	{
		return Addr;
	}
	public void setAddr(String addr)
	{
		Addr = addr;
	}
	private Vector<String> floorList = new Vector<String>();
	
	public String getStartFloor()
	{
		return startFloor;
	}
	public void setStartFloor(String startFloor)
	{
		this.startFloor = startFloor;
		onFloorChange();
	}
	public String getEndFloor()
	{
		return endFloor;
	}
	public void setEndFloor(String endFloor)
	{
		this.endFloor = endFloor;
		onFloorChange();
	}
	public String getFileName()
	{
		return fileName;
	}
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	public Vector<String> getFloorList()
	{
		return floorList;
	}
	public void setFloorList(Vector<String> floorList)
	{
		this.floorList = floorList;
	}
	private void onFloorChange()
	{
		if (startFloor.equals("") == false && endFloor.equals("") == false)
		{
			try
			{
				int intStart = Integer.parseInt(startFloor.replace("B", "-"));
				int intEnd = Integer.parseInt(endFloor.replace("B", "-"));
				
				for(int i = intStart ; i <= intEnd ; i++)
				{
					if( i == 0)
					{
						continue;
					}
					floorList.add(String.valueOf(i).replace("-", "B"));
				}
				
			}
			catch (Exception e)
			{
				WataLog.e( e.toString());
			}
		}
	}
	public String getGIDex()
	{
	    return GIDex;
	}
	public void setGIDex(String gIDex)
	{
	    GIDex = gIDex;
	}
	public String getLatitude()
	{
	    return Latitude;
	}
	public void setLatitude(String latitude)
	{
	    Latitude = latitude;
	}
	public String getLongitude()
	{
	    return Longitude;
	}
	public void setLongitude(String longitude)
	{
	    Longitude = longitude;
	}
	public String getBearing()
	{
	    return Bearing;
	}
	public void setBearing(String bearing)
	{
	    Bearing = bearing;
	}
	
}
