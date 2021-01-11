package pdr_collecting.core;
//package com.mnsoft.indnavi.Modules.PDR;

import android.util.Log;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

//import com.
public class relativepositionestimation {


    static double Gyro_matching_Threshold = 5; //5m
    static double pri_x_node = 0; //자이로 이벤트가 발생했는데 이전 발생 위치와 같다고 인정하지않음
    static double pri_y_node = 0;
    static int minus_step_count = 0;

    static void func_relativepositionestimation() {
        WataLog.i("func_relativepositionestimation");
        WataLog.d("pdralgorithm.minus_step_flag=" + pdralgorithm.minus_step_flag);

        if (!pdralgorithm.minus_step_flag) {
            //Double_pedestrian_coordinate
            WataLog.d("pdrvariable.nominal_step_length=" + pdrvariable.nominal_step_length);
            WataLog.d("pdrvariable.getStep_length=" + pdrvariable.getStep_length());

            WataLog.d("Math.sin(pdrvariable.pedestrian_heading)=" + Math.sin(pdrvariable.pedestrian_heading));

            pdrvariable.D_pedestrian_x_coordinate = pdrvariable.D_pedestrian_x_coordinate + pdrvariable.present_step_length * Math.sin(pdrvariable.pedestrian_heading);
            pdrvariable.D_pedestrian_y_coordinate = pdrvariable.D_pedestrian_y_coordinate + pdrvariable.present_step_length * Math.cos(pdrvariable.pedestrian_heading);

        } else {
            WataLog.d(" " + minus_step_count + " " + pdralgorithm.minus_step_flag);
            minus_step_count++;
            pdrvariable.D_pedestrian_x_coordinate = pdrvariable.D_pedestrian_x_coordinate + (pdrvariable.present_step_length - pdralgorithm.non_gyro_corner_dis / 3) * Math.sin(pdrvariable.pedestrian_heading);
            pdrvariable.D_pedestrian_y_coordinate = pdrvariable.D_pedestrian_y_coordinate + (pdrvariable.present_step_length - pdralgorithm.non_gyro_corner_dis / 3) * Math.cos(pdrvariable.pedestrian_heading);

            if (minus_step_count == 3) {
                minus_step_count = 0;
                pdralgorithm.minus_step_flag = false;
            }
        }
        WataLog.d("pdrvariable.D_pedestrian_x_coordinate=" + pdrvariable.D_pedestrian_x_coordinate);
        WataLog.d("pdrvariable.D_pedestrian_y_coordinate=" + pdrvariable.D_pedestrian_y_coordinate);
        // PDF X Y : Ethan
        //Int_pedestrian_coordinate
        pdrvariable.I_pedestrian_x_coordinate = pdrvariable.D_pedestrian_x_coordinate;
        pdrvariable.I_pedestrian_y_coordinate = pdrvariable.D_pedestrian_y_coordinate;

    }

    //자이로 이벤트 이용 코너포인트 맵매칭
    //시작과 목적지 노드는 맵매칭 포인트에서 제거
    static void Matching_corner() {

        double temp_Dis_PreLo_MMcorner = 0;

        temp_Dis_PreLo_MMcorner = Math.sqrt(Math.pow(pdrvariable.D_pedestrian_x_coordinate - initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event + 1][0], 2)
                + Math.pow(pdrvariable.D_pedestrian_y_coordinate - initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event + 1][1], 2));

        //자이로 이벤트 발생 Threshold 설정 최소:2미터 최대 7미터
        double Temp_Gyro_matching_Threshold = 0;

        Temp_Gyro_matching_Threshold = Math.sqrt(Math.pow(initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event][0] - initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event + 1][0], 2)
                + Math.pow(initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event][1] - initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event + 1][1], 2));


        if (Temp_Gyro_matching_Threshold < 3) {
            Gyro_matching_Threshold = 2;
        } else if (Temp_Gyro_matching_Threshold >= 3 && Temp_Gyro_matching_Threshold <= 30) {
            Gyro_matching_Threshold = 5;
        } else {
            Gyro_matching_Threshold = 7;
        }

//        Log.i("Temp_Gyro_matching_Threshold", " " + Temp_Gyro_matching_Threshold);

        //찾아진 최단거리 노드가 일정 threshold 안으로 들어오면 gyro event 발생되었다고 판단
        if ((temp_Dis_PreLo_MMcorner < Gyro_matching_Threshold)) {
            if (initialmm.final_heading.length > 1) // GEO2추가
            {
                if (Math.abs(initialmm.final_heading[pdralgorithm.Gyro_event] - initialmm.final_heading[pdralgorithm.Gyro_event + 1]) >= 0.785) {
                    pdralgorithm.Gyro_event++;

                    pdrvariable.D_pedestrian_x_coordinate = initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event][0];
                    pdrvariable.D_pedestrian_y_coordinate = initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event][1];

                    steplenthestimation.gyro_based_step_est(initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event][0], initialmm.final_nodr_for_MM[pdralgorithm.Gyro_event][1], pdrvariable.step_count);
                }
            }
        }
    }


}