package com.geotwo.ble;

import java.util.Comparator;

@SuppressWarnings("rawtypes")
public class e_BLEScandata
{
	public boolean	isUpdated = false;
	public String 	SSID;
	public String 	MACaddr;
	public String	UUID;
	public int		Major;
	public int		Minor;
	public int 		RSSI;
	public double	Distance;
	public int		TxPower;
	public float	Interval;
	public String	Proximity;
	public int		MeasuredPower;
	public long		LastScanTime;		// System.currentTimeMillis();
	public int		mCnt = 0;
	
	public static final Comparator compareRSSI = new Comparator()
	{
		// ��������
		public int compare ( Object o1, Object o2 )
		{
			int lhs = ((e_BLEScandata)o1).RSSI;
			int rhs = ((e_BLEScandata)o2).RSSI;
			
			if ( lhs < rhs ) return 1;
			else if ( lhs > rhs ) return -1;
			return 0;
		}
	};
	
	public static final Comparator compareSSID = new Comparator()
	{
		// ��������
		@SuppressWarnings("unchecked")
		public int compare ( Object o1, Object o2 )
		{
			String lhs = ((e_BLEScandata)o1).SSID;
			String rhs = ((e_BLEScandata)o2).SSID;
			
			if(lhs != null && rhs != null)
			{
				if ( lhs.compareTo(rhs) > 0 ) return 1;
				else if ( lhs.compareTo(rhs) < 0 ) return -1;
			}
			return compareMACaddr.compare(o1, o2);
		}
	};
	
	public static final Comparator compareMACaddr = new Comparator()
	{
		// ��������
		public int compare ( Object o1, Object o2 )
		{
			String lhs = ((e_BLEScandata)o1).MACaddr;
			String rhs = ((e_BLEScandata)o2).MACaddr;
			
			if ( lhs.compareTo(rhs) > 0 ) return 1;
			else if ( lhs.compareTo(rhs) < 0 ) return -1;
			return 0;
		}
	};
}
