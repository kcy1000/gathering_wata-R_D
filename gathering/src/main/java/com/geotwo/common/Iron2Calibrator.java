package com.geotwo.common;

import android.util.Log;

public class Iron2Calibrator {

	public Iron2Calibrator() {
		// TODO Auto-generated constructor stub
	}
	
	final double[] note3F = {0.8650, -8.2613};
	final double[] gpro2F = {0.5613, -19.499};
	final double[] gals5F = {0.6491, -23.232};
	final double[] g3F = {0.8226, -17.469};
	
	final double[] note3F_C = {0.6321, -13.832};
	final double[] gpro2F_C = {0.6116, -13.211};
	final double[] gals5F_C = {0.6864, -16.151};
	final double[] g3F_C = {0.7722, -13.227};
	
	final double[] note3F_exp = {-4.588, -42.912};
	final double[] gpro2F_exp = {-7.833, -42.715};
	final double[] gals5F_exp = {-7.201, -29.638};
	final double[] g3F_exp = {-4.814, -32.907};
	
	final double[] note3F_exp_C = {-3.616, -42.250};
	final double[] gpro2F_exp_C = {-4.553, -42.556};
	final double[] gals5F_exp_C = {-5.373, -30.207};
	final double[] g3F_exp_C = {-4.251, -32.073};
	
	final double[] iron2F_exp = {-4.934, -41.715};
	final double[] iron2F_exp_C = {-4.802, -33.988};
	
	final static int MODE_EXP_OPEN = 0;
	final static int MODE_EXP_CLOSED = 1;
	final static int MODE_LINEAR_OPEN = 2;
	final static int MODE_LINEAR_CLOSED = 3;
	
	public double calibrate(int rssi, int mode)
	{
//		Log.d("jeongyeol", android.os.Build.MODEL);
		double res = 0;
		if(android.os.Build.MODEL != null)
		{
			switch(mode)
			{
			case 0: //exp open
				if(rssi >= -60)
				{
					if(android.os.Build.MODEL.startsWith("SM-N900"))
						res = (iron2F_exp[0]/note3F_exp[0])*(rssi - note3F_exp[1])+iron2F_exp[1];
					else if(android.os.Build.MODEL.startsWith("SM-G900"))
						res = (iron2F_exp[0]/gals5F_exp[0])*(rssi - gals5F_exp[1])+iron2F_exp[1];
				}
				break;
			case 1: //exp closed
				if(rssi >= -60)
				{
					if(android.os.Build.MODEL.startsWith("SM-N900"))
						res = (iron2F_exp_C[0]/note3F_exp_C[0])*(rssi - note3F_exp_C[1])+iron2F_exp_C[1];
					else if(android.os.Build.MODEL.startsWith("SM-G900"))
						res = (iron2F_exp_C[0]/gals5F_exp_C[0])*(rssi - gals5F_exp_C[1])+iron2F_exp_C[1];
				}
				break;
			case 2: //linear open
				if(android.os.Build.MODEL.startsWith("SM-N900"))
					res = note3F[0]*rssi+note3F[1];
				else if(android.os.Build.MODEL.startsWith("SM-G900"))
					res = gals5F[0]*rssi+gals5F[1];
				break;
			case 3: //linear closed
				if(android.os.Build.MODEL.startsWith("SM-N900"))
					res = note3F_C[0]*rssi+note3F_C[1];
				else if(android.os.Build.MODEL.startsWith("SM-G900"))
					res = gals5F_C[0]*rssi+gals5F_C[1];
				break;
			default:
				break;
			}
		}
		if(res == 0)
			return rssi;
		else
			return res;
	}
}
