package com.geotwo.common;

import Jama.Matrix;

public class KalmanFilter {
	
	public Matrix measured_value; //Zk
	public Matrix estimated_value; //X^k
	
	private int dt = 1;
	
	private Matrix sysModel_A;
	private Matrix sysModel_Q;
	private Matrix sysModel_H;
	private Matrix sysModel_R;
	
	private Matrix expect_estimated; //X^-k
	private Matrix covariance_matrix; //Pk
	private Matrix expect_covariance_matrix; //P-k
	
	private Matrix kalman_gain; //Kk
	
	private boolean bIsInited = false;
	private boolean bIsProcessing = false;
	
	public void init(Matrix x, Matrix p, int type)
	{
		if(!bIsInited)
		{
			estimated_value = x;
			
			double [][] temp_z = {{x.get(0, 0)}, {x.get(2, 0)}};
			measured_value = new Matrix(temp_z);
			
			double [][] temp_a = {{1, dt, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, dt}, {0, 0, 0, 1}};
			sysModel_A = new Matrix(temp_a);
			
			double [][] temp_h = {{1, 0, 0, 0}, {0, 0, 1, 0}};
			sysModel_H = new Matrix(temp_h);
			
			//if(type > 0)
			{
				double [][] temp_q = {{0.001, 0, 0, 0}, {0, 0.001, 0, 0}, {0, 0, 0.001, 0}, {0, 0, 0, 0.001}};
				sysModel_Q = new Matrix(temp_q);
			}
//			else
//			{
//				double [][] temp_q = {{0.01, 0, 0, 0}, {0, 0.01, 0, 0}, {0, 0, 0.01, 0}, {0, 0, 0, 0.01}};
//				sysModel_Q = new Matrix(temp_q);
//			}	
			
			if(type > 0)
			{
				double [][] temp_r = {{0.1, 0}, {0, 0.1}};
				sysModel_R = new Matrix(temp_r);
			}
			else
			{
				double [][] temp_r = {{1, 0}, {0, 1}};
				sysModel_R = new Matrix(temp_r);
			}
			
			if(p != null)
				covariance_matrix = p;
			else
			{
				covariance_matrix = sysModel_Q.times(100);
			}
			bIsInited = true;
		}
	}
	
	private void expect_X_P()
	{
		expect_estimated = sysModel_A.times(estimated_value);
		expect_covariance_matrix = (sysModel_A.times(covariance_matrix)).times(sysModel_A.transpose()).plus(sysModel_Q);
	}
	
	private void calculateKalmanGain()
	{
		kalman_gain = (expect_covariance_matrix.times(sysModel_H.transpose()))
				.times(((((sysModel_H.times(expect_covariance_matrix))).times(sysModel_H.transpose())).plus(sysModel_R)).inverse());
	}
	
	private void calculateEstimatedVal()
	{
		estimated_value = expect_estimated.plus(kalman_gain.times(measured_value.minus(sysModel_H.times(expect_estimated))));
	}
	
	private void calculateCovarianceVal()
	{
		covariance_matrix = expect_covariance_matrix.minus((kalman_gain.times(sysModel_H)).times(expect_covariance_matrix));
	}
	
	public Matrix doUpdate(Matrix z)
	{
		if(bIsInited && !bIsProcessing)
		{
			bIsProcessing = true;
			measured_value = z;
			expect_X_P();
			calculateKalmanGain();
			calculateEstimatedVal();
			calculateCovarianceVal();
			bIsProcessing = false;
			return estimated_value;
		}
		else
			return null;
	}
}
