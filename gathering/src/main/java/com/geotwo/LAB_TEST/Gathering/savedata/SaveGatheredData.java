package com.geotwo.LAB_TEST.Gathering.savedata;

import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA;
import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA_PAYLOAD;
import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA_VOUCHER;
import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA_VOUCHER_PAYLOAD;
import ETRI.LBSP.Common.Interfaces.PDI_SCANINFO_A;
import ETRI.LBSP.Common.Interfaces.PDI_SCANINFO_C;
import ETRI.LBSP.Common.Interfaces.PDI_SCANINFO_D;

/**
 * 수집정보 저장
 */
public class SaveGatheredData {

    /**
     * 영상정보수집로그
     */
    public static void saveLogForImage(String filePath, String fileName, PDI_REPT_COL_DATA pdi_rept_col_data) {
        BufferedWriter out = null;
        FileWriter fw = null;

        try {

            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }

            Date now = new Date();
            file = new File(filePath + String.format("STEP_LOG_%s_%s.log", new SimpleDateFormat("yyyyMMdd").format(now), new SimpleDateFormat("HHmmss").format(now)));
            out = new BufferedWriter(new FileWriter(file, false));

            /*
             * 스캔된 횟수만큼 라인을 생성한다.
             */
            StringBuffer sb = null;
            int macCount = 0;
            int floorNum = 0;
            String floorStr = null;
            String floor = null;
            String col_f = null;
            String af = "AF"; // 지상
            String ab = "AB"; // 지하
            String strUnder = "지하"; // 지하층수 앞에 붙이는 문자
            String strNext = "층"; // 층수 다음에 붙이는 문자
            String lastString = ";\t"; //하나의 값마다 끝에 붙이는 구분 기호
            for (PDI_REPT_COL_DATA_PAYLOAD payload : pdi_rept_col_data.B21_Payload) {

                col_f = payload.FD8_ColP_F(); //층명
                floor = col_f;

                /*
                 * Log String 만들기 시작
                 */
                sb = new StringBuffer();
                sb.append("time : " + payload.FD9_ColTime() + lastString); // 수집시간
                sb.append("x : " + payload.FD2_ColP_X + lastString); // X
                sb.append("y : " + payload.FD3_ColP_Y + lastString); // Y
                sb.append("linkheading : " + pdi_rept_col_data.B16_ColLinkHeading + lastString); // linkheading

                out.write(sb.toString());
                out.newLine();
            }

            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 수집정보 Log 파일 생성
     *
     * @param filePath
     * @param fileName
     * @param SCAN_RESULT_THIS_EPOCH
     */
    static public void saveGatheredData(String filePath, String fileName, PDI_REPT_COL_DATA SCAN_RESULT_THIS_EPOCH) {

        BufferedWriter out = null;
        FileWriter fw = null;

        try {
/*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues()
                with(values) {
                    put(MediaStore.Images.Media.TITLE, fileName)
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/my_folder")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }

                val uri = context.getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                val fos = context.contentResolver.openOutputStream(uri!!)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos?.run {
                    flush()
                    close()
                }
            }
*/
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(filePath + fileName);
            out = new BufferedWriter(new FileWriter(file, false));

            /*
             * 스캔된 횟수만큼 라인을 생성한다.
             */
            StringBuffer sb = null;
            int macCount = 0;
            int floorNum = 0;
            String floorStr = null;
            String floor = null;
            String col_f = null;
            String af = "AF"; // 지상
            String ab = "AB"; // 지하
            String strUnder = "지하"; // 지하층수 앞에 붙이는 문자
            String strNext = "층"; // 층수 다음에 붙이는 문자
            String lastString = ";\t"; //하나의 값마다 끝에 붙이는 구분 기호

            for (PDI_REPT_COL_DATA_PAYLOAD payload : SCAN_RESULT_THIS_EPOCH.B21_Payload) {

                col_f = payload.FD8_ColP_F(); //층명
                floor = col_f;

//				/*
//				 * 층 문자 만들기. AF(지상), AB(지하) 로 시작할 경우에만 실행한다.
//				 */
//				if (col_f != null && (col_f.startsWith(af) || col_f.startsWith(ab))) {
//
//					/*
//					 * 층명에서 층 숫자만 추출한다. (앞 두자리 제거)
//					 */
//					floorStr = col_f.substring(2, col_f.length());
//
//					try {
//
//						floorNum = Integer.parseInt(floorStr);
//					} catch (Exception e) {
//
//						Log.e("SaveGatheredData", "invalid floor." + col_f);
//						floor = col_f;
//						continue;
//					}
//
//					floor = floorNum + strNext;
//
//					/*
//					 * 지하 층인 경우 앞에 '지하'를 붙인다.
//					 */
//					if (col_f.startsWith(ab)) {
//
//						floor = strUnder + floor;
//					}
//				} else {
//
//					Log.e("SaveGatheredData", "invalid floor." + col_f);
//					floor = col_f;
//				}

                /*
                 * Log String 만들기 시작
                 */
                sb = new StringBuffer();
                sb.append(payload.FD9_ColTime() + lastString); // 수집시간
                sb.append(payload.FD2_ColP_X + lastString); // X
                sb.append(payload.FD3_ColP_Y + lastString); // Y
                sb.append(payload.FD14_GRS_X + lastString); //GRS_X
                sb.append(payload.FD15_GRS_Y + lastString); //GRS_Y

                /**
                 * 조영수박사님 로그 추가 건
                 */
                sb.append(payload.FD16_Sensor_Accelerometer_X + lastString); // TYPE_ACCELEROMETER X
                sb.append(payload.FD17_Sensor_Accelerometer_Y + lastString); // TYPE_ACCELEROMETER Y
                sb.append(payload.FD18_Sensor_Accelerometer_Z + lastString); // TYPE_ACCELEROMETER Z

                sb.append(payload.FD19_Sensor_Gravity_X + lastString); // TYPE_GRAVITY X
                sb.append(payload.FD20_Sensor_Gravity_Y + lastString); // TYPE_GRAVITY Y
                sb.append(payload.FD21_Sensor_Gravity_Z + lastString); // TYPE_GRAVITY Z

                sb.append(payload.FD9_ColMag_X + lastString); // TYPE_MAGNETIC_FIELD X
                sb.append(payload.FD10_ColMag_Y + lastString); // TYPE_MAGNETIC_FIELD Y
                sb.append(payload.FD11_ColMag_Z + lastString); // TYPE_MAGNETIC_FIELD Z

                sb.append(payload.FD25_Sensor_RotationVector_Roll + lastString); // TYPE_ROTATION_VECTOR X (deg)
                sb.append(payload.FD26_Sensor_RotationVector_Pitch + lastString); // TYPE_ROTATION_VECTOR Y (deg)
                sb.append(payload.FD27_Sensor_RotationVector_Yaw + lastString); // TYPE_ROTATION_VECTOR Z (deg)
                /**
                 * 조영수박사님 로그 추가 건 끝
                 */

//				sb.append(payload.FD9_ColMag_X + lastString); // magx
//				sb.append(payload.FD10_ColMag_Y + lastString); // magy
//				sb.append(payload.FD11_ColMag_Z + lastString); // magz
//				sb.append(payload.FD16_GRAVITY_X + lastString); // gravity x
//				sb.append(payload.FD17_GRAVITY_Y + lastString); // gravity y
//				sb.append(payload.FD18_GRAVITY_Z + lastString); // gravity z
//				sb.append(payload.FD19_AZIMUTH+ lastString); // azimuth
//				sb.append(payload.FD20_PITCH+ lastString); // pitch
//				sb.append(payload.FD21_ROLL+ lastString); // roll
//				sb.append(payload.FD22_UNCALIBRATED_MAG_AZIMUTH+ lastString); // azimuth in uncalibrated magnetics
//				sb.append(payload.FD23_UNCALIBRATED_MAG_PITCH+ lastString); // pitch in uncalibrated magnetics
//				sb.append(payload.FD24_UNCALIBRATED_MAG_ROLL+ lastString); // roll in uncalibrated magnetics
//				sb.append(payload.FD25_ColUnCalibratedMag_X+ lastString); // magx uncalibrated
//				sb.append(payload.FD26_ColUnCalibratedMag_Y+ lastString); // magy uncalibrated
//				sb.append(payload.FD27_ColUnCalibratedMag_Z+ lastString); // magz uncalibrated
//				sb.append(payload.FD28_ROTATION_VECTOR_AZIMUTH+ lastString); // azimuth in rotation vector
//				sb.append(payload.FD29_ROTATION_VECTOR_PITCH+ lastString); // pitch in rotation vector
//				sb.append(payload.FD30_ROTATION_VECTOR_ROLL+ lastString); // roll in rotation vector

                sb.append(payload.FD13_ColMag_Baro + lastString); // baro
                sb.append(SCAN_RESULT_THIS_EPOCH.B16_ColLinkHeading + lastString); // linkheading
//				sb.append(payload.FD12_ColMag_Heading + lastString); // magheading
                sb.append(floor + lastString); // floor
                /**
                 * 조영수박사님 로그 추가 건
                 */
                double totalStepLength;
                try {
                    totalStepLength = payload.Step_Count * payload.Step_Length;
                } catch (Exception e) {
                    totalStepLength = 0d;
                }
                sb.append(totalStepLength + lastString); // 보폭길이 합이라는 의미는 수집 시 Wi-Fi 스캔 주기 내 M번의 보행수가 검출되고 설정한 보폭길이가 0.7 (m)라고 하면, 0.7 x M (m)를 의미합니다
                /**
                 * 조영수박사님 로그 추가 건 끝
                 */
                sb.append(payload.FD8_SCANINFO_A_List.size()); // mac count

                /*
                 * Mac 정보가 하나도 없으면 맨 끝이 되기 때문에 세미콜론을 붙이지 않는다.
                 */
                if (payload.FD8_SCANINFO_A_List != null && payload.FD8_SCANINFO_A_List.size() != 0) {
                    sb.append(lastString);
                }

                /*
                 * Mac 정보만큼 나열한다.
                 */
                macCount = 0;
                for (PDI_SCANINFO_A mac : payload.FD8_SCANINFO_A_List) {
                    sb.append(mac.FD1_INFRA_TYPE + lastString);
                    sb.append(mac.FD2_INFRA_ID + lastString); // mac addr
                    sb.append(mac.FD3_RSSI); // rssi

                    /*
                     *  맨끝에는 세미콜론을 붙이지 않는다.
                     */
                    if (macCount < payload.FD8_SCANINFO_A_List.size() - 1) {

                        sb.append(lastString);
                    }

                    macCount++;
                }

                out.write(sb.toString());
                out.newLine();
            }

            out.flush();
            Log.e("jeongyeol", "Log data save ended");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 바우처전용 Log 파일 생성
     *
     * @param filePath
     * @param fileName
     * @param SCAN_RESULT_THIS_EPOCH
     */
    static public void saveVoucherData(String filePath, String fileName, PDI_REPT_COL_DATA_VOUCHER SCAN_RESULT_THIS_EPOCH) {

        BufferedWriter out = null;
        FileWriter fw = null;

        try {

            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(filePath + fileName);
            out = new BufferedWriter(new FileWriter(file, false));

            /*
             * 스캔된 횟수만큼 라인을 생성한다.
             */
            StringBuffer sb = null;
            int macCount = 0;
            int floorNum = 0;
            String floorStr = null;
            String floor = null;
            String col_f = null;
            String af = "AF"; // 지상
            String ab = "AB"; // 지하
            String strUnder = "지하"; // 지하층수 앞에 붙이는 문자
            String strNext = "층"; // 층수 다음에 붙이는 문자
            String lastString = ";\t"; //하나의 값마다 끝에 붙이는 구분 기호

            for (PDI_REPT_COL_DATA_VOUCHER_PAYLOAD payload : SCAN_RESULT_THIS_EPOCH.B21_Payload) {

                col_f = payload.FD8_ColP_F(); //층명
                floor = col_f;

                /*
                 * Log String 만들기 시작
                 */
                sb = new StringBuffer();
                sb.append(payload.FD9_ColTime() + lastString); // 수집시간
                sb.append(payload.FD2_ColP_X + lastString); // X
                sb.append(payload.FD3_ColP_Y + lastString); // Y
                sb.append(payload.FD14_GRS_X + lastString); //GRS_X
                sb.append(payload.FD15_GRS_Y + lastString); //GRS_Y

                /**
                 * 조영수박사님 로그 추가 건
                 */
                sb.append(payload.FD16_Sensor_Accelerometer_X + lastString); // TYPE_ACCELEROMETER X
                sb.append(payload.FD17_Sensor_Accelerometer_Y + lastString); // TYPE_ACCELEROMETER Y
                sb.append(payload.FD18_Sensor_Accelerometer_Z + lastString); // TYPE_ACCELEROMETER Z

                sb.append(payload.FD19_Sensor_Gravity_X + lastString); // TYPE_GRAVITY X
                sb.append(payload.FD20_Sensor_Gravity_Y + lastString); // TYPE_GRAVITY Y
                sb.append(payload.FD21_Sensor_Gravity_Z + lastString); // TYPE_GRAVITY Z

                sb.append(payload.FD9_ColMag_X + lastString); // TYPE_MAGNETIC_FIELD X
                sb.append(payload.FD10_ColMag_Y + lastString); // TYPE_MAGNETIC_FIELD Y
                sb.append(payload.FD11_ColMag_Z + lastString); // TYPE_MAGNETIC_FIELD Z

                sb.append(payload.FD25_Sensor_RotationVector_Roll + lastString); // TYPE_ROTATION_VECTOR X (deg)
                sb.append(payload.FD26_Sensor_RotationVector_Pitch + lastString); // TYPE_ROTATION_VECTOR Y (deg)
                sb.append(payload.FD27_Sensor_RotationVector_Yaw + lastString); // TYPE_ROTATION_VECTOR Z (deg)
                /**
                 * 조영수박사님 로그 추가 건 끝
                 */

                sb.append(payload.FD13_ColMag_Baro + lastString); // baro
                sb.append(SCAN_RESULT_THIS_EPOCH.B16_ColLinkHeading + lastString); // linkheading
//				sb.append(payload.FD12_ColMag_Heading + lastString); // magheading
                sb.append(floor + lastString); // floor
                /**
                 * 조영수박사님 로그 추가 건
                 */
                double totalStepLength;
                try {
                    totalStepLength = payload.Step_Count * payload.Step_Length;
                } catch (Exception e) {
                    totalStepLength = 0d;
                }
                sb.append(totalStepLength + lastString); // 보폭길이 합이라는 의미는 수집 시 Wi-Fi 스캔 주기 내 M번의 보행수가 검출되고 설정한 보폭길이가 0.7 (m)라고 하면, 0.7 x M (m)를 의미합니다
                /**
                 * 조영수박사님 로그 추가 건 끝
                 */
                sb.append(payload.FD8_SCANINFO_A_List.size()); // mac count

                /*
                 * Mac 정보가 하나도 없으면 맨 끝이 되기 때문에 세미콜론을 붙이지 않는다.
                 */
                if (payload.FD8_SCANINFO_A_List != null && payload.FD8_SCANINFO_A_List.size() != 0) {
                    sb.append(lastString);
                }

                /*
                 * Mac 정보만큼 나열한다.
                */
                macCount = 0;
                for (PDI_SCANINFO_A mac : payload.FD8_SCANINFO_A_List) {
                    sb.append(mac.FD1_INFRA_TYPE + lastString);
                    sb.append(mac.FD2_INFRA_ID + lastString); // mac addr
                    if( mac.FD1_INFRA_TYPE == PDI_SCANINFO_A.DEF_INFRA_TYPE_Cell )
                    {
                        sb.append(((PDI_SCANINFO_C)mac).rsrp);
                    }
                    else if( mac.FD1_INFRA_TYPE == PDI_SCANINFO_A.DEF_INFRA_TYPE_WiFi_RTT )
                    {
                        sb.append(((PDI_SCANINFO_D)mac).Distance);
                    }
                    else sb.append(mac.FD3_RSSI); // rssi

                    /*
                     *  맨끝에는 세미콜론을 붙이지 않는다.
                     */
                    if (macCount < payload.FD8_SCANINFO_A_List.size() - 1) {

                        sb.append(lastString);
                    }

                    macCount++;
                }

                out.write(sb.toString());
                out.newLine();
            }

            out.flush();
            WataLog.d("Log data save ended");
        } catch (Exception e) {
            e.printStackTrace();
            WataLog.e("Exception=" + e.toString());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    WataLog.e("Exception=" + e.toString());
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    WataLog.e("Exception=" + e.toString());
                }
            }
        }
    }


    /**
     * 바우처전용 Log 파일 생성
     *
     * @param filePath
     * @param fileName
     * @param SCAN_RESULT_THIS_EPOCH
     */
    static public void saveLteRttData(String filePath, String fileName, PDI_REPT_COL_DATA_VOUCHER SCAN_RESULT_THIS_EPOCH) {

        BufferedWriter out = null;
        FileWriter fw = null;

        try {

            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(filePath + fileName);
            out = new BufferedWriter(new FileWriter(file, false));

            /*
             * 스캔된 횟수만큼 라인을 생성한다.
             */
            StringBuffer sb = null;
            int macCount = 0;
            int floorNum = 0;
            String floorStr = null;
            String floor = null;
            String col_f = null;
            String af = "AF"; // 지상
            String ab = "AB"; // 지하
            String strUnder = "지하"; // 지하층수 앞에 붙이는 문자
            String strNext = "층"; // 층수 다음에 붙이는 문자
            String lastString = ";\t"; //하나의 값마다 끝에 붙이는 구분 기호

            for (PDI_REPT_COL_DATA_VOUCHER_PAYLOAD payload : SCAN_RESULT_THIS_EPOCH.B21_Payload) {

                col_f = payload.FD8_ColP_F(); //층명
                floor = col_f;

                /*
                 * Log String 만들기 시작
                 */
                sb = new StringBuffer();
                WataLog.d("payload.FD9_ColTime()=" + payload.FD9_ColTime());
                sb.append(payload.FD9_ColTime() + lastString); // 수집시간
                sb.append(payload.FD2_ColP_X + lastString); // X
                sb.append(payload.FD3_ColP_Y + lastString); // Y
                sb.append(payload.FD14_GRS_X + lastString); //GRS_X
                sb.append(payload.FD15_GRS_Y + lastString); //GRS_Y

                /**
                 * 조영수박사님 로그 추가 건
                 */
                sb.append(payload.FD16_Sensor_Accelerometer_X + lastString); // TYPE_ACCELEROMETER X
                sb.append(payload.FD17_Sensor_Accelerometer_Y + lastString); // TYPE_ACCELEROMETER Y
                sb.append(payload.FD18_Sensor_Accelerometer_Z + lastString); // TYPE_ACCELEROMETER Z

                sb.append(payload.FD19_Sensor_Gravity_X + lastString); // TYPE_GRAVITY X
                sb.append(payload.FD20_Sensor_Gravity_Y + lastString); // TYPE_GRAVITY Y
                sb.append(payload.FD21_Sensor_Gravity_Z + lastString); // TYPE_GRAVITY Z

                sb.append(payload.FD9_ColMag_X + lastString); // TYPE_MAGNETIC_FIELD X
                sb.append(payload.FD10_ColMag_Y + lastString); // TYPE_MAGNETIC_FIELD Y
                sb.append(payload.FD11_ColMag_Z + lastString); // TYPE_MAGNETIC_FIELD Z

                sb.append(payload.FD25_Sensor_RotationVector_Roll + lastString); // TYPE_ROTATION_VECTOR X (deg)
                sb.append(payload.FD26_Sensor_RotationVector_Pitch + lastString); // TYPE_ROTATION_VECTOR Y (deg)
                sb.append(payload.FD27_Sensor_RotationVector_Yaw + lastString); // TYPE_ROTATION_VECTOR Z (deg)
                /**
                 * 조영수박사님 로그 추가 건 끝
                 */

                sb.append(payload.FD13_ColMag_Baro + lastString); // baro
                sb.append(SCAN_RESULT_THIS_EPOCH.B16_ColLinkHeading + lastString); // linkheading
//				sb.append(payload.FD12_ColMag_Heading + lastString); // magheading
                sb.append(floor + lastString); // floor
                /**
                 * 조영수박사님 로그 추가 건
                 */
                double totalStepLength;
                try {
                    totalStepLength = payload.Step_Count * payload.Step_Length;
                } catch (Exception e) {
                    totalStepLength = 0d;
                }
                sb.append(totalStepLength + lastString); // 보폭길이 합이라는 의미는 수집 시 Wi-Fi 스캔 주기 내 M번의 보행수가 검출되고 설정한 보폭길이가 0.7 (m)라고 하면, 0.7 x M (m)를 의미합니다
                /**
                 * 조영수박사님 로그 추가 건 끝
                 */
                sb.append(payload.FD8_SCANINFO_A_List.size()); // mac count

                /*
                 * Mac 정보가 하나도 없으면 맨 끝이 되기 때문에 세미콜론을 붙이지 않는다.
                 */
                if (payload.FD8_SCANINFO_A_List != null && payload.FD8_SCANINFO_A_List.size() != 0) {
                    sb.append(lastString);
                }

                /*
                 * Mac 정보만큼 나열한다.
                 */
                macCount = 0;
                for (PDI_SCANINFO_A mac : payload.FD8_SCANINFO_A_List) {
                    sb.append(mac.FD1_INFRA_TYPE + lastString);
                    sb.append(mac.FD2_INFRA_ID + lastString); // mac addr
                    sb.append(mac.FD3_RSSI); // rssi

                    /*
                     *  맨끝에는 세미콜론을 붙이지 않는다.
                     */
                    if (macCount < payload.FD8_SCANINFO_A_List.size() - 1) {

                        sb.append(lastString);
                    }

                    macCount++;
                }

                WataLog.d("sb.toString()=" + sb.toString());

                out.write(sb.toString());
                out.newLine();
            }

            out.flush();
            WataLog.d("Log data save ended");
        } catch (Exception e) {
            e.printStackTrace();
            WataLog.e("Exception=" + e.toString());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    WataLog.e("Exception=" + e.toString());
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    WataLog.e("Exception=" + e.toString());
                }
            }
        }
    }


    static public void saveVoucherSendableData(String filePath, String fileName, PDI_REPT_COL_DATA_VOUCHER SCAN_RESULT_THIS_EPOCH) {
        FileOutputStream fo = null, fo_nomagent = null;
        File file = null, file_nomagent = null;

        try {
            file = new File(filePath);

            if (!file.exists())
                file.mkdirs();

            file = new File(filePath + fileName);

            fo = new FileOutputStream(file, false);

            SCAN_RESULT_THIS_EPOCH.B20_ColPoint_CNT = (short) SCAN_RESULT_THIS_EPOCH.B21_Payload.size();
            Log.e("saveSendableData", "colPoint count is " + SCAN_RESULT_THIS_EPOCH.B20_ColPoint_CNT);

//			SCAN_RESULT_THIS_EPOCH.B22_ColPoint_CNT = (short) SCAN_RESULT_THIS_EPOCH.B21_Payload.size();
//			Log.e("saveSendableData", "colPoint count is " + SCAN_RESULT_THIS_EPOCH.B22_ColPoint_CNT);


            if (SCAN_RESULT_THIS_EPOCH.B21_Payload.size() > 0) {
//				if(SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD2_ColP_X == 0)
//					SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X = GatheringActivity._startPoint.x;
//				else
//					SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X = SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD2_ColP_X;
//				if(SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD3_ColP_Y == 0)
//					SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y = GatheringActivity._startPoint.y;
//				else
//					SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y = SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD3_ColP_Y;

                WataLog.d("SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD8_ColP_F()=" + SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD8_ColP_F());
                SCAN_RESULT_THIS_EPOCH.B8_ColStartP_F(SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD8_ColP_F());

                // db에 맞춰 형변환
//				SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X = (int) SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X;
//				SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y = (int) SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y;
//
//				SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X = (int) SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X;
//				SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y = (int) SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y;
            }

            Log.e("sp", "start " + SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X + "/" + SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y);
            Log.e("sp", "end   " + SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X + "/" + SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y);

            byte[] packedData = SCAN_RESULT_THIS_EPOCH.packet();

            fo.write(packedData);

            fo.flush();

        } catch (Exception e) {
            e.printStackTrace();
            WataLog.e("exception =" + e.toString());
        } finally {
            if (fo != null) {
                try {
                    fo.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }
            if (fo_nomagent != null) {
                try {
                    fo_nomagent.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }
        }
    }


    static public void saveSendableData(String filePath, String fileName, PDI_REPT_COL_DATA SCAN_RESULT_THIS_EPOCH) {
        FileOutputStream fo = null, fo_nomagent = null;
        File file = null, file_nomagent = null;

        try {
            file = new File(filePath);

            if (!file.exists())
                file.mkdirs();

            file = new File(filePath + fileName);

            fo = new FileOutputStream(file, false);

            SCAN_RESULT_THIS_EPOCH.B20_ColPoint_CNT = (short) SCAN_RESULT_THIS_EPOCH.B21_Payload.size();
            Log.e("saveSendableData", "colPoint count is " + SCAN_RESULT_THIS_EPOCH.B20_ColPoint_CNT);

//			SCAN_RESULT_THIS_EPOCH.B22_ColPoint_CNT = (short) SCAN_RESULT_THIS_EPOCH.B21_Payload.size();
//			Log.e("saveSendableData", "colPoint count is " + SCAN_RESULT_THIS_EPOCH.B22_ColPoint_CNT);


            if (SCAN_RESULT_THIS_EPOCH.B21_Payload.size() > 0) {
//				if(SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD2_ColP_X == 0)
//					SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X = GatheringActivity._startPoint.x;
//				else
//					SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X = SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD2_ColP_X;
//				if(SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD3_ColP_Y == 0)
//					SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y = GatheringActivity._startPoint.y;
//				else
//					SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y = SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD3_ColP_Y;
                SCAN_RESULT_THIS_EPOCH.B8_ColStartP_F(SCAN_RESULT_THIS_EPOCH.B21_Payload.get(0).FD8_ColP_F());

                // db에 맞춰 형변환
//				SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X = (int) SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X;
//				SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y = (int) SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y;
//
//				SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X = (int) SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X;
//				SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y = (int) SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y;
            }

            Log.e("sp", "start " + SCAN_RESULT_THIS_EPOCH.B5_ColStartP_X + "/" + SCAN_RESULT_THIS_EPOCH.B6_ColStartP_Y);
            Log.e("sp", "end   " + SCAN_RESULT_THIS_EPOCH.B12_ColEndP_X + "/" + SCAN_RESULT_THIS_EPOCH.B13_ColEndP_Y);

            byte[] packedData = SCAN_RESULT_THIS_EPOCH.packet();

            fo.write(packedData);

            fo.flush();

        } catch (Exception e) {
            e.printStackTrace();
            WataLog.e("exception =" + e.toString());
        } finally {
            if (fo != null) {
                try {
                    fo.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }
            if (fo_nomagent != null) {
                try {
                    fo_nomagent.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }
        }
    }

//    static public void savePOIData( String filePath, String fileName, String poiInfo) {
//        BufferedWriter out = null;
//        FileWriter fw = null;
//        try {
//
//            File file = new File(filePath);
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//
//            file = new File(filePath + fileName);
//            out = new BufferedWriter(new FileWriter(file, false));
//            out.write(poiInfo);
//            out.flush();
//        } catch (Exception e) {
//            e.printStackTrace();
//            WataLog.e("Exception=" + e.toString());
//        } finally {
//            if (out != null) {
//                try {
//                    out.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    WataLog.e("Exception=" + e.toString());
//                }
//            }
//            if (fw != null) {
//                try {
//                    fw.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    WataLog.e("Exception=" + e.toString());
//                }
//            }
//        }
//    }

    static public void saveData( String filePath, String fileName, String info) {
        BufferedWriter out = null;
        FileWriter fw = null;
        try {

            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(filePath + fileName);
            out = new BufferedWriter(new FileWriter(file, false));
            out.write(info);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            WataLog.e("Exception=" + e.toString());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    WataLog.e("Exception=" + e.toString());
                }
            }
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    WataLog.e("Exception=" + e.toString());
                }
            }
        }
    }

}
