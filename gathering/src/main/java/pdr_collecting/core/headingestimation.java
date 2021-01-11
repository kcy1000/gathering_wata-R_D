package pdr_collecting.core;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

public class headingestimation {
    static void func_headingestimation() {


		WataLog.d("pdralgorithm.Gyro_event=" + pdralgorithm.Gyro_event);
		WataLog.d("initialmm.final_heading.length=" + initialmm.final_heading.length);

        if (pdralgorithm.Gyro_event < initialmm.final_heading.length) {
            pdrvariable.pedestrian_heading = initialmm.final_heading[pdralgorithm.Gyro_event];
        } else {
            pdrvariable.pedestrian_heading = sensoract.magvalue;
        }
		WataLog.d("  pdrvariable.pedestrian_heading  =" +  pdrvariable.pedestrian_heading);


        if (pdrvariable.pedestrian_heading >= 0) {
            pdrvariable.pedestrian_displayed_heading = pdrvariable.pedestrian_heading * 180 / 3.14;
        } else {
            pdrvariable.pedestrian_displayed_heading = pdrvariable.pedestrian_heading * 180 / 3.14 + 360;
        }
		WataLog.d("  pdrvariable.pedestrian_displayed_heading  =" +  pdrvariable.pedestrian_displayed_heading);

    }
}