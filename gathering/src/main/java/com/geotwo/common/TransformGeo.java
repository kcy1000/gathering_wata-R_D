package com.geotwo.common;

import android.util.Log;

import com.geotwo.o2mapmobile.geometry.Vector;

public class TransformGeo {

	public static double DEG2RAD = Math.PI / 180.0;
	public static double RAD2DEG = 180.0 / Math.PI;
	private final double targetMajor; 

	private final double targetScaleFactor;
	private final double targetEs;
	private final double targetE2s; 

	private final double targetLambdaOrigin;
	private final double targetM0;
	private final double targetDeltaXN; 
	private final double targetDeltaYE; 

	public TransformGeo(double targetMajor, double targetF, double targetScaleFactor, double latitudeOfOrigin, double centralMeridian, double falseNorthing, double falseEasting) {
		this.targetMajor = targetMajor;
		targetF = (targetF > 1) ? 1.0 / targetF : targetF;
		double b = targetMajor*(1.0-targetF); 
		this.targetScaleFactor = targetScaleFactor;
		this.targetEs = (targetMajor*targetMajor - b*b) / (targetMajor*targetMajor);
		this.targetE2s = (targetMajor*targetMajor - b*b) / (b*b);

		this.targetLambdaOrigin = centralMeridian*DEG2RAD;
		this.targetM0 = targetMajor * mlfn(m0Func(this.targetEs), m1Func(this.targetEs), m2Func(this.targetEs), m3Func(this.targetEs), latitudeOfOrigin*DEG2RAD);
		
		this.targetDeltaXN = falseNorthing;
		this.targetDeltaYE = falseEasting;
	}
	

	public Vector transformGP2TM(double latitude, double longitude) {
		return transformGP2TM(latitude*DEG2RAD, longitude*DEG2RAD, targetLambdaOrigin, targetMajor, targetEs, targetE2s, targetScaleFactor, targetM0, targetDeltaXN, targetDeltaYE);
	}
	private Vector transformGP2TM(double phi, double lambda, double lambdaOrigin, double a, double e_2, double e2_2, double k0, double M0, double deltaXN, double deltaYE) {
		double T = Math.pow(Math.tan(phi), 2);
		double C = e_2 / (1.0 - e_2) * Math.pow(Math.cos(phi), 2);
		double A = (lambda-lambdaOrigin)*Math.cos(phi);
		double N = a / Math.sqrt(1.0 - e_2 * Math.pow(Math.sin(phi), 2));
		double M = a * mlfn(m0Func(e_2), m1Func(e_2), m2Func(e_2), m3Func(e_2), phi);
		double tmYE = deltaYE + k0 * N * ( A + Math.pow(A, 3)/6.0*(1.0-T+C) + Math.pow(A, 5)/120.0*(5.0-18.0*T+T*T+72.0*C-58.0*e2_2) );
		double tmXN = deltaXN + k0 * ( M - M0 + N*Math.tan(phi)* ( A*A*0.5  +  Math.pow(A, 4)/24.0*(5.0-T+9.0*C+4.0*C*C)  +  Math.pow(A, 6)/720.0*(61.0-58.0*T+T*T+600.0*C-330.0*e2_2) ) );
		Log.d("jeongyeol", "lat "+phi*RAD2DEG+"/lng "+lambda*RAD2DEG+"/x "+tmXN+"/y "+tmYE);
		return new Vector(tmYE, tmXN, 0);
	}
	private static double m0Func(double x) {
		return 1.0 - 0.25 * x * (1.0 + x / 16.0 * (3.0 + 1.25 * x));
	}
	private static double m1Func(double x) {
		return 0.375 * x * (1.0 + 0.25 * x * (1.0 + 0.46875 * x));
	}
	private static double m2Func(double x) {
		return 0.05859375 * x * x * (1.0 + 0.75 * x);
	}
	private static double m3Func(double x) {
		return x * x * x * (35.0 / 3072.0);
	}
	private static double mlfn(double m0, double m1, double m2, double m3, double phi) {
		return m0 * phi - m1 * Math.sin(2.0 * phi) + m2 * Math.sin(4.0 * phi) - m3 * Math.sin(6.0 * phi);		
	}
    public Vector transformTM2GP(double XN, double YE) {
    	return transformTM2GP(XN, YE, targetLambdaOrigin, targetMajor, targetEs, targetE2s, targetScaleFactor, targetM0, targetDeltaXN, targetDeltaYE);
    }
    private Vector transformTM2GP(double XN, double YE, double lambdaOrigin, double a, double e_2, double e2_2, double k0, double M0, double deltaXN, double deltaYE) {
		double M = M0 + (XN - deltaXN) / k0;
		double mu1 = M / (a * (1 - e_2/4 - 3*e_2*e_2/64 - 5*Math.pow(e_2, 3)/256));
		double e1 = (1 - Math.sqrt(1-e_2)) / (1 + Math.sqrt(1-e_2));
		double phi1 = mu1 + (3*e1/2 - 27*Math.pow(e1, 3)/32)*Math.sin(2*mu1) + (21*e1*e1/16 - 55*Math.pow(e1, 4)/32)*Math.sin(4*mu1) + (151*Math.pow(e1, 3)/96)*Math.sin(6*mu1) + (1097*Math.pow(e1, 4)/512)*Math.sin(8*mu1);
		double R1 = (a*(1-e_2)) / (Math.pow(1 - e_2*Math.pow(Math.sin(phi1), 2), 3/2)) ;
		double C1 = e2_2 * Math.pow(Math.cos(phi1), 2);
		double T1 = Math.pow(Math.tan(phi1), 2);
		double N1 = a / Math.sqrt(1 - e_2*Math.pow(Math.sin(phi1), 2));
		double D = (YE - deltaYE) / (N1 * k0);
		double phi = phi1 - (N1*Math.tan(phi1)/R1) * (D*D/2 - Math.pow(D, 4)/24*(5+3*T1+10*C1-4*C1*C1-9*e2_2) + Math.pow(D, 6)/720*(61+90*T1+298*C1+45*T1*T1-252*e2_2-3*C1*C1));
		double lambda = lambdaOrigin + 1/Math.cos(phi1)*(D - Math.pow(D, 3)/6*(1+2*T1+C1) + Math.pow(D, 5)/120*(5-2*C1+28*T1-3*C1*C1+8*e2_2+24*T1*T1));
		return new Vector(lambda*RAD2DEG, phi*RAD2DEG, 0);
    }
    
}