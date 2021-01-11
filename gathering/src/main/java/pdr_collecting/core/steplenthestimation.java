package pdr_collecting.core;

import android.util.Log;

public class steplenthestimation {
	
	
	static int gyro_event_count=0;
	static double pri_x_node=0;
	static double pri_y_node=0;
	static double post_x_node=0;
	static double post_y_node=0;
	static int g_step_count=0;
	static double g_moved_distance=0;
	static double scale_vector[] = {0.7,0.5,0.7};;
	public static double present_acc_variance_value=0;
	static double initial_acc_variance_value=9.8552;
	static double present_step_frequency=0;
	static double initial_step_frequency=0.0898;
	static double present_gyro_integral=0;
	static double initial_gyro_integral=0.2574;
	
	//자이로 이벤트를 이용한 파라미터 update 관련 변수
	static int flag_parameter_updata=0;
	static double summed_acc_variance_for_SL=0;
	static double summed_step_frequency_for_SL=0;
	static double summed_gyro_integral_for_SL=0;
	
	static void func_steplengthestimation(double acc, double step_fre, double gyro_intg){
		
		present_acc_variance_value = acc;
		if(step_fre !=0){
		present_step_frequency = 1/step_fre;
		}else{
			present_step_frequency=0;
		}
			
		present_gyro_integral = gyro_intg;
		
		if( present_acc_variance_value != 0 )
		{
			//실시간 걸음걸이 추정 보류
//		pdrvariable.present_step_length = scale_vector[0]*(present_acc_variance_value-initial_acc_variance_value)+
//				scale_vector[1]*(present_step_frequency-initial_step_frequency)+
//				scale_vector[2]*(present_gyro_integral-initial_gyro_integral)+pdrvariable.nominal_step_length;
		}else
		{
			pdrvariable.present_step_length = pdrvariable.nominal_step_length;
		}
		
		Log.i("acc_i"," "+present_acc_variance_value+" " +present_step_frequency+ " "+present_gyro_integral + " "+pdrvariable.nominal_step_length);
		
		
		if(flag_parameter_updata == 1)
		{
			summed_acc_variance_for_SL +=present_acc_variance_value;
			summed_step_frequency_for_SL +=present_step_frequency;
			summed_gyro_integral_for_SL +=present_gyro_integral;
		}
		
	}

	static void gyro_based_step_est(double temp_x,double temp_y,int step_count){
		gyro_event_count++;
		if(gyro_event_count==1){
			//초기값 설정, 노드와 걸음수
			
			pri_x_node=temp_x;
			pri_y_node=temp_y;
			g_step_count=step_count;
			
			flag_parameter_updata=1;
			
		}else if(gyro_event_count==2){
			//현재 자이로 발생 이벤트 노드 update
			
			flag_parameter_updata=0;
			
			post_x_node=temp_x;
			post_y_node=temp_y;
			
			//코너노드를 이용한 거리 계산
			g_moved_distance = Math.sqrt(Math.pow(post_x_node-pri_x_node,2) + Math.pow(post_y_node-pri_y_node,2));
			
			//걸음걸이 계산
			pdrvariable.nominal_step_length = g_moved_distance/(step_count-(double)g_step_count);
			
			
			//현재노드값 update
			pri_x_node = post_x_node;
			pri_y_node = post_y_node;
			
			//파라미터 update
			initial_acc_variance_value = summed_acc_variance_for_SL/(step_count-(double)g_step_count);
			initial_step_frequency = summed_step_frequency_for_SL/(step_count-(double)g_step_count);
			initial_gyro_integral = summed_gyro_integral_for_SL/(step_count-(double)g_step_count);
			
			//걸음수 update
			g_step_count=step_count;
		}
	}
}