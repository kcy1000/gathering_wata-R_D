package pdr_collecting.core;


//package com.mnsoft.indnavi.Modules.PDR;

import com.geotwo.LAB_TEST.Gathering.GatheringActivity;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;

//import com.mnsoft.indnavi.Common.Constants;
//import com.mnsoft.indnavi.Main.INDNAVIStorage;

public class pdralgorithm{

	//window size
	static int stepwinsize = 14;

	//step detection algorithm variable
	static int epoch=0;
	static double[] accsave = new double[stepwinsize+1];
	static double accnorm = 0;
	static int stepcompare = 0;
	//자이로 이벤트 위한 변수. Z축 자이로 값이 계속 쌓임
	static double[] MapGyro= new double[sensoract.MapGyroSize];
	static double Gyro_Avg=0;
	static int Gyro_flag=0;
	static int Gyro_event=0; //헤딩 순서를 결정하는 매우 중요한 변수
	static int Gyro_event_flag=0;

	//실시간 걸음길이를 위한 변수
	static int step_length_save_length = 20;
	static double[] acc_save_step_length = new double[step_length_save_length];
	static double[] gyro_save_step_length = new double[step_length_save_length];
	static int step_frequency=0;
	static int previous_epoch_for_step_length=0;
	static double acc_var_mean=0;
	static double acc_mean=0;
	static double acc_sum=0;
	static double gyro_integral=0;
	static double gyro_sum=0;

	static double non_gyro_corner_dis = 1;
	static boolean minus_step_flag=false;


	public static void getsensordata(double acc1, double acc2, double acc3, double gyro2, double gyro3)
	{
		if(initialmm.final_heading == null)
			return;
		//초기위치 입력 관련
		if(pdrvariable.step_count == 0)
		{
			Gyro_event=0;
			pdrvariable.D_pedestrian_x_coordinate = pdrvariable.initial_pedestrian_x_coordinate;
			pdrvariable.D_pedestrian_y_coordinate = pdrvariable.initial_pedestrian_y_coordinate;
			pdrvariable.D_pedestrian_z_coordinate = pdrvariable.initial_pedestrian_z_coordinate;

		}

		/**
		 * Corner point matching using Gyroscope event
		 */
		Gyro_flag++;
		Gyro_event_flag++;
		Gyro_Avg=0; //초기화
		if (Gyro_flag <= sensoract.MapGyroSize)
		{
			MapGyro[Gyro_flag-1]=gyro3;

			for(int i=0;i<Gyro_flag-1;i++)
			{
				Gyro_Avg+=MapGyro[i];
			}
			Gyro_Avg = Gyro_Avg/Gyro_flag;
		}else
		{
			for(int i=0; i<sensoract.MapGyroSize-1; i++)
			{
				MapGyro[i]=MapGyro[i+1];
			}
			MapGyro[sensoract.MapGyroSize-1]=gyro3;

			for(int i=0;i<sensoract.MapGyroSize-1;i++)
			{
				Gyro_Avg+=MapGyro[i];
			}
			Gyro_Avg = Gyro_Avg/sensoract.MapGyroSize;
		}

//		Log.i("gyro_event"," "+Gyro_Avg);

		if(Math.abs(Gyro_Avg) > 0.5 & Gyro_event_flag>sensoract.MapGyroSize*5){

			Gyro_event_flag=0;

			relativepositionestimation.Matching_corner();
		}

		//코엑스와 같이 링크사이의 방향이 90도 미만인 부분이 많을때
		//45도 까지는 노드에 가까이 같을때 방향을 업데이트해줘서 자이로이벤트를 인위적으로
		//발생시키는 횟수를 줄인다 2012-12-07
		if( Gyro_event < initialmm.final_heading.length - 1)
		{
			//현재 방향과 이후 방향과의 차이가 45도 미만일때 if 문 만족
//			Log.d("jeongyeol", "gyro angel="+Math.abs( initialmm.final_heading[pdralgorithm.Gyro_event] - initialmm.final_heading[pdralgorithm.Gyro_event+1]));
			if(GatheringActivity.pathFlag == 2 || GatheringActivity.pathFlag == 4 || Math.abs( initialmm.final_heading[pdralgorithm.Gyro_event] - initialmm.final_heading[pdralgorithm.Gyro_event+1]) < 0.0785 )
			{
				//현재위치와 코너(방향이 변하면 모두 코너라고 칭함)와의 거리가 일정 threshold 안으로 들어올때 if문 만족
				double temp_distance=0;

				temp_distance = Math.sqrt(Math.pow( (pdrvariable.D_pedestrian_x_coordinate - initialmm.final_nodr_for_MM[Gyro_event+1][0]),2) +
						Math.pow((pdrvariable.D_pedestrian_y_coordinate - initialmm.final_nodr_for_MM[Gyro_event+1][1]),2));

				//거리 threshold 1 meter
				if(temp_distance < non_gyro_corner_dis)
				{
					//방향 업데이트됨
					Gyro_event++;

					//현재 위치 보정됨
					pdrvariable.D_pedestrian_x_coordinate = initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event][0];
					pdrvariable.D_pedestrian_y_coordinate = initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event][1];

					//non_gyro_corner_dis 만큼 거리가 당겨 졌기 때문에
					//다음에 발생되는 3개의 걸음에서 이만큼을 보상해준다
					minus_step_flag=true;

				}

			}

		}

		/**
		 * Step Detection Algorithm
		 */
			epoch++;
			stepcompare=0;
			accnorm = Math.sqrt((acc1*acc1) + (acc2*acc2) + (acc3*acc3) ) ;
//			WataLog.d("accnorm=" +accnorm);

			if(epoch < stepwinsize)
				{
					accsave[epoch-1] = accnorm;
				}
			else
				{
				for(int i=0; i<stepwinsize; i++)
					{
						accsave[i] = accsave[i+1];
					}
					accsave[stepwinsize]=accnorm;
				}

			//실시간 걸음길이를 위한 acc_value 획득
			//실시간 걸음길이를 위한 gyro_integral 획득
			if(epoch < step_length_save_length)
				{
				acc_save_step_length[epoch-1] = accnorm;
				gyro_save_step_length[epoch-1] = Math.abs(gyro2);
				}
			else
				{
				for(int i=0; i<step_length_save_length-1; i++)
					{
					acc_save_step_length[i] = acc_save_step_length[i+1];
					gyro_save_step_length[i] = gyro_save_step_length[i+1];
					}
				acc_save_step_length[step_length_save_length-1]=accnorm;
				gyro_save_step_length[step_length_save_length-1]=Math.abs(gyro2);
				}

			//걸음 발생 판단
//			WataLog.d("epoch=" + epoch);
//			WataLog.d("stepwinsize=" + stepwinsize*2);
			if(epoch > stepwinsize*2)
			{
				//down peak detection
				if ((accsave[8] <= accsave[9]) && (accsave[8] <= accsave[7])) {
//					WataLog.d( "pdr - "+"8:"+accsave[8]+"/7:"+accsave[7]+"/9:"+accsave[9]);

					if(accsave[8] < pdrvariable.threshold_stepdetection )
					{
						for(int i=2; i<7; i++)
						{
							//window size 14
							if(accsave[8] > accsave[i])
							{
								stepcompare = stepcompare +1;
							}
						}
						for(int i=13 ; i>9; i--)
						{
							if(accsave[8] > accsave[i])
							{
								stepcompare = stepcompare +1;
							}
						}
						WataLog.d( "pdr - "+stepcompare);
						if(stepcompare == 0)
						{
							if(accsave[8] != accsave[9])
							{
								//스텝 증가
								pdrvariable.step_count++;

								WataLog.d( "pdr - "+pdrvariable.nominal_step_length+" "+pdrvariable.step_count);

								//걸음길이 추정을 위한 변수 (걸음 주파수)
								/* 걸음이 발생되면 현재 발생된 걸음을 기준으로
								 * 그 걸음에서의 걸음주파수, 가속도 분산값, 자이로 적분값 호출
								 * 세개의 파라미터값을 함수의 파라미터로 전달 y=f(a,f,v);
								 * 계산된 걸음걸이는 위치 계산에 전달됨
								 */

								step_frequency = epoch-previous_epoch_for_step_length;

								if(step_frequency <step_length_save_length)
								{
									acc_sum=0;
									gyro_sum=0;
									for(int i=step_length_save_length-step_frequency; i<step_length_save_length; i++){
										acc_sum+=acc_save_step_length[i];
										gyro_sum+=gyro_save_step_length[i];

									}

									gyro_integral=gyro_sum/step_frequency;
									acc_sum=0;
									for(int i=step_length_save_length-step_frequency; i<step_length_save_length; i++){
										acc_sum+=Math.abs(acc_save_step_length[i]-acc_mean);

									}

									acc_var_mean=acc_sum/step_frequency;

									steplenthestimation.func_steplengthestimation(acc_var_mean,step_frequency,gyro_integral);

								}
								else
								{
									steplenthestimation.func_steplengthestimation(0,0,0);
								}

								//방향 추정
								headingestimation.func_headingestimation();

								//위치 계산
								relativepositionestimation.func_relativepositionestimation();


								pdrvariable.moved_distance += pdrvariable.present_step_length;
								previous_epoch_for_step_length=epoch;
							}

						}
					}
				}
			}
	}

	// GEO2 추가
	public static double getGyro_integral() {
		return gyro_integral;
	}

	public static int get_step_frequency() {
		return step_frequency;
	}

}