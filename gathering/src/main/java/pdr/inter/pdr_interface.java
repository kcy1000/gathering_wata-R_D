package pdr.inter;

import android.app.Application;
import android.os.Handler;

public interface pdr_interface {

	// 2013 10 21
	// Kalman filter
	public void refer_kf_meas_updata( int wifi_pos_x, int wifi_pos_y);
	
	// PDR information 
	public double refer_get_pedestrian_x_coordinate();
	public double refer_get_pedestrian_y_coordinate();
	public double refer_get_pedestrian_z_coordinate();
	
	public void refer_set_pedestrian_x_coordinate(double set_pedestrian_y_coordinate);
	public void refer_set_pedestrian_y_coordinate(double set_pedestrian_y_coordinate);
	public void refer_set_pedestrian_z_coordinate(double set_pedestrian_y_coordinate);
	
	// Sensor Calibration
	
	public void refer_mag_calibration_start();
	public void refer_mag_calibration_end();
	
	public void refer_gyro_calibration_start();
	public void refer_gyro_calibration_end();
	
	// Sensor act
	public void refer_sensor_act(Application application, Handler mhandler);
	
	public boolean refer_get_mag_cal_flag();
	public boolean refer_get_gyro_cal_flag();
	
	public void refer_set_mag_cal_flag(boolean set_mag_cal_flag);
	public void refer_set_gyro_cal_flag(boolean set_gyro_cal_flag);
	
	
	//Kalman filter R 
	public void refer_set_kalman_R(int wifi_stand_var);
	
	//Height 
	public boolean refer_get_initial_height_cal_flag();
	public void refer_set_initial_height_cal_flag(boolean set_initial_height_cal_flag);
	public void refer_set_first_averaging_flag(boolean set_first_averaging_flag);
	public int refer_get_present_floor_value();
	public void refer_initial_floor_value(int wifi_z_pos);
	
	
	//2013-11-11 추가
	//Kalman filter heading R (for magnetic weighting)
	public void refer_set_kalman_heading_R(double magnetic_error_var);	
}
