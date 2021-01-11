package pdr_collecting.core;


import android.content.Context;
import android.content.SharedPreferences;

import com.geotwo.LAB_TEST.Gathering.GatheringActivity;
import com.geotwo.LAB_TEST.Gathering.PathDrawingActivity;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;


public class newPdralgorithm {

    static int epoch = 0;
    static int Gyro_event = 0; //헤딩 순서를 결정하는 매우 중요한 변수
    //실시간 걸음길이를 위한 변수
    static int previous_epoch_for_step_length = 0;


    public static void getsensordata(Context context) {
        if (initialmm.final_heading == null)
            return;
        //초기위치 입력 관련
        if (pdrvariable.step_count == 0) {
            Gyro_event = 0;
            pdrvariable.D_pedestrian_x_coordinate = pdrvariable.initial_pedestrian_x_coordinate;
            pdrvariable.D_pedestrian_y_coordinate = pdrvariable.initial_pedestrian_y_coordinate;
            pdrvariable.D_pedestrian_z_coordinate = pdrvariable.initial_pedestrian_z_coordinate;
        }

        pdrvariable.step_count++;
        pdrvariable.present_step_length = pdrvariable.getStep_length();
        //방향 추정
        headingestimation.func_headingestimation();
        //위치 계산
        relativepositionestimation.func_relativepositionestimation();

        pdrvariable.moved_distance += pdrvariable.present_step_length;
        previous_epoch_for_step_length=epoch;
    }



}