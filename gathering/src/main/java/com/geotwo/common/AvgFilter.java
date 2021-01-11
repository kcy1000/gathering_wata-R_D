package com.geotwo.common;

import android.util.Log;
import Jama.Matrix;

public class AvgFilter {

	public Matrix avg_value; 
	
	private int seqNum = 0;
	private boolean bInited = false;
	
	public void init(Matrix initial)
	{
		if(!bInited)
		{
			seqNum = 1;
			avg_value = initial;
			bInited = true;
		}
	}
	
	public Matrix doUpdate(Matrix input)
	{
		if(bInited)
		{
			seqNum++;
			
			double p1 = 1/(double)seqNum;
			double p2 = ((double)seqNum-1)/(double)seqNum;
			
			//Log.d("jeongyeol", "p1="+p1+"/p2="+p2);
			
			if(seqNum > 1)
			{
				avg_value = (input.times(p1)).plus(avg_value.times(p2));
				//Log.d("jeongyeol", "avg proc");
			}
			
			return avg_value;
		}
		else
			return null;
	}
}
