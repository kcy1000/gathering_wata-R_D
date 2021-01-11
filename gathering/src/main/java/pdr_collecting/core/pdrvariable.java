package pdr_collecting.core;


import android.content.SharedPreferences;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

public class pdrvariable{
	
	static int step_count = 0;
	static double nominal_step_length = 0.65;
	static double present_step_length= 0.0;
	//초기 보행자 x,y,z 위치
	static double initial_pedestrian_x_coordinate = 0;
	static double initial_pedestrian_y_coordinate = 0;
	static double initial_pedestrian_z_coordinate = 0;
	//맵매칭 전 보행자 x,y,z 위치
	static double I_pedestrian_x_coordinate = 0;
	static double I_pedestrian_y_coordinate = 0;
	static double I_pedestrian_z_coordinate = 0;
	
	//실제 포지셔닝 double 보행자 x,y,z 위치
	static double D_pedestrian_x_coordinate = 0;
	static double D_pedestrian_y_coordinate = 0;
	static double D_pedestrian_z_coordinate = 0;
	static double pedestrian_heading=0;
	static double pedestrian_displayed_heading=0;
	static double map_bearing_degree=231;
	
	
	static double threshold_stepdetection = 8.5;
	
	static double moved_distance = 0;
	
	static double degree2radian = 3.14/180;
	static double radian2degree = 180/3.14;
	
	public static void initValues()
	{
		step_count = 0;
		nominal_step_length = 0.65;
		//초기 보행자 x,y,z 위치
		initial_pedestrian_x_coordinate = 0;
		initial_pedestrian_y_coordinate = 0;
		initial_pedestrian_z_coordinate = 0;
		
		//맵매칭 전 보행자 x,y,z 위치
		I_pedestrian_x_coordinate = 0;
		I_pedestrian_y_coordinate = 0;
		I_pedestrian_z_coordinate = 0;
		pedestrian_heading=0;
		threshold_stepdetection = 8.5;
		moved_distance = 0;
	}
	
	
	public static void initCordinateValue()
	{
		step_count = 0;
		nominal_step_length = 0.5;
		//초기 보행자 x,y,z 위치
		initial_pedestrian_x_coordinate = 0;
		initial_pedestrian_y_coordinate = 0;
		initial_pedestrian_z_coordinate = 0;
		//맵매칭 전 보행자 x,y,z 위치
		I_pedestrian_x_coordinate = 0;
		I_pedestrian_y_coordinate = 0;
		I_pedestrian_z_coordinate = 0;
	}
	
	
	public static int getStep_count() {
		WataLog.d("step_count=" + step_count);
		return step_count;
	}
	public static void setStep_count(int step_count) {
		WataLog.d("step_count=" + step_count);
		pdrvariable.step_count = step_count;
	}
	public static double getStep_length() {
		return nominal_step_length;
	}
	public static void setStep_length(double step_length) {
		pdrvariable.nominal_step_length = step_length;
	}

	public static double getPresent_Step_length() {
		return present_step_length;
	}
	public static void setPresent_Step_length(double step_length) {
		pdrvariable.present_step_length = step_length;
	}

	public static double getInitial_pedestrian_x_coordinate() {
		return initial_pedestrian_x_coordinate;
	}
	public static void setInitial_pedestrian_x_coordinate(
			double initial_pedestrian_x_coordinate) {
		pdrvariable.initial_pedestrian_x_coordinate = initial_pedestrian_x_coordinate;
	}
	public static double getInitial_pedestrian_y_coordinate() {
		return initial_pedestrian_y_coordinate;
	}
	public static void setInitial_pedestrian_y_coordinate(
			double initial_pedestrian_y_coordinate) {
		pdrvariable.initial_pedestrian_y_coordinate = initial_pedestrian_y_coordinate;
	}
	public static double getInitial_pedestrian_z_coordinate() {
		return initial_pedestrian_z_coordinate;
	}
	public static void setInitial_pedestrian_z_coordinate(
			double initial_pedestrian_z_coordinate) {
		pdrvariable.initial_pedestrian_z_coordinate = initial_pedestrian_z_coordinate;
	}
	public static double getPedestrian_x_coordinate() {
		return I_pedestrian_x_coordinate;
	}
	public static void setPedestrian_x_coordinate(double pedestrian_x_coordinate) {
		pdrvariable.I_pedestrian_x_coordinate = pedestrian_x_coordinate;
	}
	public static double getPedestrian_y_coordinate() {
		return I_pedestrian_y_coordinate;
	}
	public static void setPedestrian_y_coordinate(double pedestrian_y_coordinate) {
		pdrvariable.I_pedestrian_y_coordinate = pedestrian_y_coordinate;
	}
	public static double getPedestrian_z_coordinate() {
		return I_pedestrian_z_coordinate;
	}
	public static void setPedestrian_z_coordinate(double pedestrian_z_coordinate) {
		pdrvariable.I_pedestrian_z_coordinate = pedestrian_z_coordinate;
	}
	public static double getPedestrian_heading() {
		return pedestrian_heading;
	}
	public static void setPedestrian_heading(double pedestrian_heading) {
		pdrvariable.pedestrian_heading = pedestrian_heading;
	}
	public static double getThreshold_stepdetection() {
		return threshold_stepdetection;
	}
	public static void setThreshold_stepdetection(double threshold_stepdetection) {
		pdrvariable.threshold_stepdetection = threshold_stepdetection;
	}
	public static double getMoved_distance() {
		return moved_distance;
	}
	public static void setMoved_distance(double moved_distance) {
		pdrvariable.moved_distance = moved_distance;
	}
	public static double getDegree2radian() {
		return degree2radian;
	}
	public static void setDegree2radian(double degree2radian) {
		pdrvariable.degree2radian = degree2radian;
	}
	public static double getRadian2degree() {
		return radian2degree;
	}
	public static void setRadian2degree(double radian2degree) {
		pdrvariable.radian2degree = radian2degree;
	}
	public static double getPedestrian_Diplayed_heading() {
		return pedestrian_displayed_heading;
	}
	public static void getPedestrian_Diplayed_heading(double pedestrian_displayed_heading) {
		pdrvariable.pedestrian_displayed_heading = pedestrian_displayed_heading;
	}
}