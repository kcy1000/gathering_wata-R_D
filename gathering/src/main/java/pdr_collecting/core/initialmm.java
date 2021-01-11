package pdr_collecting.core;

import com.geotwo.LAB_TEST.Gathering.GatheringActivity;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.o2mapmobile.shape.Points;
import com.geotwo.o2mapmobile.shape.Polyline;
//import com.mnsoft.dsn1.iEngineJNI;

public class initialmm {

    //static iEngineJNI miEng;

    static int route_decision_flag = 0; //0:수동경로 1:자동경로 2:네비게이션

    static double[] node_array; //엠엔소프트에서 호출
    static double[][] parsed_node_array; //3열식 파싱
    static double[][] vector_node_array; //파싱된 노드 벡터로 변환
    static double[][] Deg_Ch_node_array;

    static int Deg_ch_flag = 0;

    //오직 맵매칭을 위한 변수
    static double[][] final_nodr_for_MM; //헤딩이 변하는 노드 찾기 (자이로이벤트 발생 후보 노드)
    static double[] final_heading; //해당경로에 따른 보행자 최종 방향, 자이로이벤트 발생후 다음 경로로 업데이트		// Ethan


    static final double r2d = 180 / Math.PI;
    static final double d2r = Math.PI / 180;
    static final double DegMin3 = 0.05; //노드가 일직선상에 놓여있지 않기때문에 경로 변경이 일정 threshold 이상 변해야만 경로 변경이라고 인정함
    static final double pi = 3.14;
    static int input_node_array = 0;

    static String map_path = null;


    static public void MM_initialize2(Polyline srcLine) {
        WataLog.d("srcLine=" + srcLine);
        if (srcLine == null) return;

        int ptCount = srcLine.getPart(0).getNumPoints();

        Points points = new Points();
        points.makePoints(2);
        points.setPoint(0, srcLine.getPart(0).data[0].x, srcLine.getPart(0).data[0].y, 0.);
        points.setPoint(1, srcLine.getPart(0).data[ptCount - 1].x, srcLine.getPart(0).data[ptCount - 1].y, 0.);

        Polyline line = new Polyline();
        line.makeParts(1);
        line.setParts(0, points);

        node_array = new double[(line.getNumPoints() + 2) * 3];

        double startX, startY, startZ;

        for (int i = 0; i < line.getPart(0).getNumPoints(); i++) {

            node_array[(i * 3) + 0] = line.getPart(0).data[i].x;
            node_array[(i * 3) + 2] = line.getPart(0).data[i].y;
            node_array[(i * 3) + 1] = 0;

        }


        node_array[line.getPart(0).getNumPoints() * 3] = line.getPart(0).data[line.getPart(0).getNumPoints() - 1].x;
        node_array[line.getPart(0).getNumPoints() * 3 + 2] = line.getPart(0).data[line.getPart(0).getNumPoints() - 1].y;
        node_array[line.getPart(0).getNumPoints() * 3 + 1] = 0;

        node_array[(line.getPart(0).getNumPoints() + 1) * 3] = line.getPart(0).data[line.getPart(0).getNumPoints() - 1].x;
        node_array[(line.getPart(0).getNumPoints() + 1) * 3 + 2] = line.getPart(0).data[line.getPart(0).getNumPoints() - 1].y;
        node_array[(line.getPart(0).getNumPoints() + 1) * 3 + 1] = 0;

        startX = node_array[0];
        startY = node_array[2];
        startZ = node_array[1];

        pdrvariable.setInitial_pedestrian_x_coordinate(startX);
        pdrvariable.setInitial_pedestrian_y_coordinate(startY);
        pdrvariable.setInitial_pedestrian_z_coordinate(startZ);

        parsed_node_array = new double[node_array.length / 3][4];

        input_node_array = 0;
        try {
            for (int i = 0; i < node_array.length / 3; i++) {
                for (int j = 0; j < 3; j++) {
                    parsed_node_array[i][j] = node_array[input_node_array];
                    input_node_array++;

                }
            }

            vector_node_array = new double[parsed_node_array.length - 1][4];
            Deg_Ch_node_array = new double[parsed_node_array.length - 1][3];
            Deg_ch_flag = 0;
            for (int i = 0; i < parsed_node_array.length - 1; i++) {
                vector_node_array[i][0] = parsed_node_array[i + 1][0] - parsed_node_array[i][0]; //X ��ǥ
                vector_node_array[i][1] = parsed_node_array[i + 1][2] - parsed_node_array[i][2]; //Y ��ǥ
                vector_node_array[i][2] = Math.atan(Math.abs(vector_node_array[i][1]) / Math.abs(vector_node_array[i][0])); //���� ���п� ���� ���� ����

                //�ȵ���̵� �������� ��ü
                if (vector_node_array[i][0] >= 0 & vector_node_array[i][1] >= 0) {
                    vector_node_array[i][3] = pi / 2 - vector_node_array[i][2];
                } else if (vector_node_array[i][0] <= 0 & vector_node_array[i][1] >= 0) {
                    vector_node_array[i][3] = pi / 2 - (pi - vector_node_array[i][2]);
                } else if (vector_node_array[i][0] <= 0 & vector_node_array[i][1] <= 0) {
                    vector_node_array[i][3] = pi / 2 - (pi + vector_node_array[i][2]);
                } else if (vector_node_array[i][0] >= 0 & vector_node_array[i][1] <= 0) {
                    vector_node_array[i][3] = pi / 2 - (2 * pi - vector_node_array[i][2]);
                }
            }

            //������ ���ϴ� ��带 �ľ��� -> �̰����� ���̷� �̺�Ʈ �߻� ��� ����
            for (int i = 0; i < parsed_node_array.length - 2; i++) {
                //for (int i=0; i<parsed_node_array.length-1; i++) {
                if (i == 0) {
                    Deg_Ch_node_array[Deg_ch_flag][2] = vector_node_array[0][3]; //ó�� ���� ����
                }
                if (GatheringActivity.pathFlag == 2 || GatheringActivity.pathFlag == 4 || DegMin3 < Math.sqrt(Math.pow(Math.cos(vector_node_array[i][3]) - Math.cos(vector_node_array[i + 1][3]), 2) + Math.pow(Math.sin(vector_node_array[i][3]) - Math.sin(vector_node_array[i + 1][3]), 2))) {

                    Deg_ch_flag++;
                    Deg_Ch_node_array[Deg_ch_flag][0] = parsed_node_array[i + 1][0];
                    Deg_Ch_node_array[Deg_ch_flag][1] = parsed_node_array[i + 1][2];

                    Deg_Ch_node_array[Deg_ch_flag][2] = vector_node_array[i + 1][3]; // ���� �ȵ���̵� ����

                }
            }

            final_nodr_for_MM = new double[Deg_ch_flag + 2][2]; //���� ���⺯ȯ���� 2�� ����
            final_heading = new double[Deg_ch_flag + 1];//�� ���ⰳ���� ������ȯ���ڿ� 1�� ������

            final_nodr_for_MM[0][0] = parsed_node_array[0][0]; //���۳��
            final_nodr_for_MM[0][1] = parsed_node_array[0][2];
            final_heading[0] = Deg_Ch_node_array[0][2];
            final_nodr_for_MM[Deg_ch_flag + 1][0] = parsed_node_array[parsed_node_array.length - 1][0]; //������ ���
            final_nodr_for_MM[Deg_ch_flag + 1][1] = parsed_node_array[parsed_node_array.length - 1][2];
//	    final_heading[Deg_ch_flag] = Deg_Ch_node_array[Deg_ch_flag][2];
            WataLog.d("Deg_ch_flag=" + Deg_ch_flag);
            for (int i = 0; i < Deg_ch_flag; i++) {

                final_nodr_for_MM[i + 1][0] = Deg_Ch_node_array[i + 1][0];
                final_nodr_for_MM[i + 1][1] = Deg_Ch_node_array[i + 1][1];
                final_heading[i + 1] = Deg_Ch_node_array[i + 1][2];
                WataLog.d("final_heading[" +  (i+1) + " ]=" + final_heading[i + 1]);
            }
        } catch (Exception e) {
            WataLog.e("Exception = " + e.toString());
        }

        //text������ ���� parameter ����
//        String PositioningInfraData_PATH = Environment.getExternalStorageDirectory().toString();
//        InputStreamReader inStreamRdr = null;

//		try {
//			inStreamRdr = new InputStreamReader(new FileInputStream(PositioningInfraData_PATH + "/pdr_para/pdrpara_coll.txt"), "EUC-KR");
//			if(inStreamRdr!=null){
//				String readStr=null;
//				BufferedReader fIn = new BufferedReader(inStreamRdr);
//				try {
//					readStr = fIn.readLine();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				pdrparameter.inputparameter(readStr);
//			}
//
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }

    private static boolean treardCheck = false;
    static public void MM_initialize3(Polyline srcLine) {
//        WataLog.d("srcLine=" + srcLine);
        if (srcLine == null) return;

        int ptCount = srcLine.getPart(0).getNumPoints();

        Points points = new Points();
        points.makePoints(2);
        points.setPoint(0, srcLine.getPart(0).data[0].x, srcLine.getPart(0).data[0].y, 0.);
        points.setPoint(1, srcLine.getPart(0).data[ptCount - 1].x, srcLine.getPart(0).data[ptCount - 1].y, 0.);

        Polyline line = new Polyline();
        line.makeParts(1);
        line.setParts(0, points);

        node_array = new double[(line.getNumPoints() + 2) * 3];

        double startX, startY, startZ;

        for (int i = 0; i < line.getPart(0).getNumPoints(); i++) {

            node_array[(i * 3) + 0] = line.getPart(0).data[i].x;
            node_array[(i * 3) + 2] = line.getPart(0).data[i].y;
            node_array[(i * 3) + 1] = 0;

        }


        node_array[line.getPart(0).getNumPoints() * 3] = line.getPart(0).data[line.getPart(0).getNumPoints() - 1].x;
        node_array[line.getPart(0).getNumPoints() * 3 + 2] = line.getPart(0).data[line.getPart(0).getNumPoints() - 1].y;
        node_array[line.getPart(0).getNumPoints() * 3 + 1] = 0;

        node_array[(line.getPart(0).getNumPoints() + 1) * 3] = line.getPart(0).data[line.getPart(0).getNumPoints() - 1].x;
        node_array[(line.getPart(0).getNumPoints() + 1) * 3 + 2] = line.getPart(0).data[line.getPart(0).getNumPoints() - 1].y;
        node_array[(line.getPart(0).getNumPoints() + 1) * 3 + 1] = 0;

        startX = node_array[0];
        startY = node_array[2];
        startZ = node_array[1];

        pdrvariable.setInitial_pedestrian_x_coordinate(startX);
        pdrvariable.setInitial_pedestrian_y_coordinate(startY);
        pdrvariable.setInitial_pedestrian_z_coordinate(startZ);

        parsed_node_array = new double[node_array.length / 3][4];

        input_node_array = 0;
//        WataLog.d("node_array.length=" + node_array.length);
//        WataLog.d("node_array.length=" + node_array.length / 3);

        try {
//            WataLog.d("treardCheck=" + treardCheck);
//            if(!treardCheck) {
                for (int i = 0; i < node_array.length / 3; i++) {
//                    treardCheck = true;
                    for (int j = 0; j < 3; j++) {
//                        WataLog.d("parsed_node_array[" + i + "]" + "[" + j + "]=" + parsed_node_array[i][j]);
//                        WataLog.d("node_array[input_node_array]=" + node_array[input_node_array]);
//                        WataLog.d("input_node_array=" + input_node_array);
//                        WataLog.d("node_array=" + node_array.length);
//                        WataLog.d("parsed_node_array=" + parsed_node_array.length);

                        parsed_node_array[i][j] = node_array[input_node_array];
                        input_node_array++;
                    }
                }
//                treardCheck = true;
//            }

            vector_node_array = new double[parsed_node_array.length - 1][4];
            Deg_Ch_node_array = new double[parsed_node_array.length - 1][3];
            Deg_ch_flag = 0;
            for (int i = 0; i < parsed_node_array.length - 1; i++) {
                vector_node_array[i][0] = parsed_node_array[i + 1][0] - parsed_node_array[i][0]; //X ��ǥ
                vector_node_array[i][1] = parsed_node_array[i + 1][2] - parsed_node_array[i][2]; //Y ��ǥ
                vector_node_array[i][2] = Math.atan(Math.abs(vector_node_array[i][1]) / Math.abs(vector_node_array[i][0])); //���� ���п� ���� ���� ����

                //�ȵ���̵� �������� ��ü
                if (vector_node_array[i][0] >= 0 & vector_node_array[i][1] >= 0) {
                    vector_node_array[i][3] = pi / 2 - vector_node_array[i][2];
                } else if (vector_node_array[i][0] <= 0 & vector_node_array[i][1] >= 0) {
                    vector_node_array[i][3] = pi / 2 - (pi - vector_node_array[i][2]);
                } else if (vector_node_array[i][0] <= 0 & vector_node_array[i][1] <= 0) {
                    vector_node_array[i][3] = pi / 2 - (pi + vector_node_array[i][2]);
                } else if (vector_node_array[i][0] >= 0 & vector_node_array[i][1] <= 0) {
                    vector_node_array[i][3] = pi / 2 - (2 * pi - vector_node_array[i][2]);
                }
            }

            //������ ���ϴ� ��带 �ľ��� -> �̰����� ���̷� �̺�Ʈ �߻� ��� ����
            for (int i = 0; i < parsed_node_array.length - 2; i++) {
                //for (int i=0; i<parsed_node_array.length-1; i++) {
                if (i == 0) {
                    Deg_Ch_node_array[Deg_ch_flag][2] = vector_node_array[0][3]; //ó�� ���� ����
                }
                if (GatheringActivity.pathFlag == 2 || GatheringActivity.pathFlag == 4 || DegMin3 < Math.sqrt(Math.pow(Math.cos(vector_node_array[i][3]) - Math.cos(vector_node_array[i + 1][3]), 2) + Math.pow(Math.sin(vector_node_array[i][3]) - Math.sin(vector_node_array[i + 1][3]), 2))) {

                    Deg_ch_flag++;
                    Deg_Ch_node_array[Deg_ch_flag][0] = parsed_node_array[i + 1][0];
                    Deg_Ch_node_array[Deg_ch_flag][1] = parsed_node_array[i + 1][2];

                    Deg_Ch_node_array[Deg_ch_flag][2] = vector_node_array[i + 1][3]; // ���� �ȵ���̵� ����

                }
            }

            final_nodr_for_MM = new double[Deg_ch_flag + 2][2]; //���� ���⺯ȯ���� 2�� ����
            final_heading = new double[Deg_ch_flag + 1];//�� ���ⰳ���� ������ȯ���ڿ� 1�� ������

            final_nodr_for_MM[0][0] = parsed_node_array[0][0]; //���۳��
            final_nodr_for_MM[0][1] = parsed_node_array[0][2];
            final_heading[0] = Deg_Ch_node_array[0][2];
            final_nodr_for_MM[Deg_ch_flag + 1][0] = parsed_node_array[parsed_node_array.length - 1][0]; //������ ���
            final_nodr_for_MM[Deg_ch_flag + 1][1] = parsed_node_array[parsed_node_array.length - 1][2];
//	    final_heading[Deg_ch_flag] = Deg_Ch_node_array[Deg_ch_flag][2];

//            WataLog.d("Deg_ch_flag=" + Deg_ch_flag);
            for (int i = 0; i < Deg_ch_flag; i++) {

                final_nodr_for_MM[i + 1][0] = Deg_Ch_node_array[i + 1][0];
                final_nodr_for_MM[i + 1][1] = Deg_Ch_node_array[i + 1][1];
                final_heading[i + 1] = Deg_Ch_node_array[i + 1][2];

                WataLog.d("final_heading[" +  (i+1) + " ]=" + final_heading[i + 1]);
            }

            Constance.WIFI_INITIALIZE = false;
        } catch (Exception e) {
            WataLog.e("Exception = " + e.toString());
        }
    }

}