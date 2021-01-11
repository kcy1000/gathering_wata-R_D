package com.geotwo.LAB_TEST.Gathering;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.savedata.LoadGatheredData;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.data.StaticManager;
import com.wata.LAB_TEST.Gathering.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import ETRI.LBSP.Common.Interfaces.ConnectorRMCS;
import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA;

public class ResultList_Path_Act extends Activity {
    private String TAG = "ResultList_Path_Act";
    private String locationName, floorName;
    private TextView file_num;
    private ListView result_path_listview;
    private String[] pathList;
    private ResultPathAdapter adapter;
    private final String savePath = Environment.getExternalStorageDirectory().getPath()
            + "/gathering/saveData/pathGathering/";
//            + "/gathering/saveData/subway/";
    private DataCore dataCore;
    private File sendFile;

    static public double BaseX, BaseY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gather_result_layout_location);

        Intent fromList = getIntent();
        locationName = fromList.getStringExtra("locationName");
        floorName = fromList.getStringExtra("floorName");

        setLayout();

        if (dataCore == null) {
            dataCore = DataCore.getInstance();
        }
    }

    private void setLayout() {
        TextView result_loc_title = (TextView) findViewById(R.id.result_loc_title);
        WataLog.d("locationName=" + locationName);
        result_loc_title.setText("locationName");

        RelativeLayout result_loc_back = (RelativeLayout) findViewById(R.id.result_loc_back);
        result_loc_back.setOnClickListener(btnEvent);

        inputData();

        result_path_listview = (ListView) findViewById(R.id.result_loc_listview);
        if (pathList != null) {
            adapter = new ResultPathAdapter(ResultList_Path_Act.this, R.layout.gather_result_list_row, pathList);
            result_path_listview.setAdapter(adapter);
        }
    }

    private void inputData() {

        WataLog.d(savePath + locationName + "/" + floorName + "/");
        File pathFile = new File(savePath + locationName + "/" + floorName + "/");
        if (pathFile.exists()) {
//			pathList = pathFile.list();
            String[] tempStrArr = pathFile.list();
            WataLog.d("inputData : " + tempStrArr.length);

            try {
                if (tempStrArr.length > 0) {
                    String[] tempIntArr = new String[tempStrArr.length];
                    WataLog.d("tempIntArr : " + tempIntArr);

                    for (int a = 0; a < tempStrArr.length; a++) {
                        if(!"LINE".equals(tempIntArr[a])){
                            tempIntArr[a] = tempStrArr[a];
                        }
                        //tempIntArr[a] = Integer.valueOf("111");
                        WataLog.d("tempStrArr : " + tempIntArr[a] + ", " + a);
                    }

                    Arrays.sort(tempIntArr);

                    pathList = new String[tempIntArr.length ];
                    for (int b = 0; b < tempIntArr.length; b++) {
                        if(!"LINE".equals(tempIntArr[b])){
                            pathList[b] = "" + tempIntArr[b];
                        }
                        WataLog.e("pathList[b] = " + pathList[b]);
                    }

				 /*
				pathList = new String[tempStrArr.length];
				for(int b=0; b<tempStrArr.length; b++)
				{
					pathList[b] = ""+tempStrArr[b];
					Log.e(TAG, "pathList[b] = "+pathList[b]);
				}
				*/

                }
            } catch (Exception e) {
                WataLog.e("Exception=" + e.toString());
            }

        }
    }

    private OnClickListener btnEvent = new OnClickListener() {
        @Override
        public void onClick(View v) {
//			switch(v.getId())
//			{
            if (v.getId() == R.id.result_loc_back)
                finish();
//				break;

//			case R.id.result_path_send_all:
//				break;
//			}
        }
    };

    class ResultPathAdapter extends ArrayAdapter<String> implements OnClickListener {
        String[] items;
        Context context = null;

        public ResultPathAdapter(Context context, int textViewResourceId, String[] items) {
            super(context, textViewResourceId, items);

            this.items = items;
            this.context = context;
        }


        @Override
        public View getView(int position, View v, ViewGroup parent) {
            if (v == null) {
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.gather_result_list_row, null);

            }

            LinearLayout file_list_layout = (LinearLayout) v.findViewById(R.id.file_list_layout);
            String pathName = items[position];
            WataLog.d("pathName=" + pathName);

            if (pathName != null && !"".equals(pathName)) {
                file_list_layout.setVisibility(View.VISIBLE);
                TextView gather_row_name_text = (TextView) v.findViewById(R.id.file_name);
                gather_row_name_text.setText(pathName);

                TextView file_num = (TextView) v.findViewById(R.id.file_num);
                String num = String.valueOf(position + 1);
                file_num.setText(num);

                WataLog.d("num=" + num);

                File file = new File(savePath + locationName + "/" + floorName + "/" + pathName + "/");
                WataLog.d("file =" + savePath + locationName + "/" + floorName + "/" + pathName + "/");
                WataLog.d("floorName =" + floorName);
                WataLog.d("pathName =" + pathName);

                String[] tempArr = file.list();
                WataLog.d("tempArr =" + tempArr);

                if (tempArr.length > 1) {
                    gather_row_name_text.setTextColor(Color.BLACK);
                } else {
                    gather_row_name_text.setTextColor(Color.GRAY);
                }
            } else {
                file_list_layout.setVisibility(View.GONE);
            }

            v.setTag(position);
            v.setOnClickListener(this);

            return v;
        }

        @Override
        public void onClick(View v) {
            WataLog.e("ResultListPathAct url = " + dataCore.getURL());
            int pos = Integer.valueOf(v.getTag().toString());
            String pathName = items[pos];

            WataLog.d("floorName=" + floorName);
            StaticManager.setFloorName(floorName);
            StaticManager.setPathNum(pathName);
            parseBasePointFromXml(locationName);

            WataLog.d("record basex =" + BaseX + "/basey =" + BaseY);
            File file = new File(savePath + locationName + "/" + floorName + "/" + pathName + "/");
            WataLog.e("pathName =" + pathName);
            String[] fileList = file.list();
            if (fileList.length <= 0) {
//                result_path_filename.setText("저장된 파일이 없습니다.");
            }

            for (int a = 0; a < fileList.length; a++) {
                String tempName = "";//fileList[a].replace(".temp", "");
                if (fileList[a].contains(".log"))
                    continue;
//				tempName = tempName.replace(".log", "");
//                result_path_filename.setText(tempName);
                WataLog.d("fileList[a]=" + fileList[a]);
                sendResult(fileList[a]);
            }
        }

        private void sendResult(final String fileName) {
            new AlertDialog.Builder(ResultList_Path_Act.this)
                    .setTitle(getString(R.string.send_file))
                    .setMessage(fileName + getString(R.string.send_file_message_1))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendFile = new File(StaticManager.getResultPath() + fileName);

                            WataLog.d("fileName=" + fileName);
                            WataLog.d("StaticManager.getResultPath() + fileName =" + StaticManager.getResultPath() + fileName);

                            PDI_REPT_COL_DATA selectedData = LoadGatheredData.decodeSavedSendableData(sendFile);
                            if (fileName.substring(fileName.indexOf(".") + 1, fileName.length()).equalsIgnoreCase("temp")) {
                                sendScanRslt(selectedData, 0);
                            } else {
                                sendScanRslt(selectedData, 1);
                            }
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }

        private void sendScanRslt(final PDI_REPT_COL_DATA SCAN_RESULT_TOTAL, final int type) {
            Thread networkThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    ConnectorRMCS rmcsConnector = new ConnectorRMCS(dataCore.getURL(), dataCore.getRmcsPort());

                    try {
                        rmcsConnector.connect();
                        rmcsConnector.sendMessage(SCAN_RESULT_TOTAL, type);
                        rmcsConnector.disconnect();
                        UIHandler.sendEmptyMessage(DataCore.DATA_SEND_ENDED);
                    } catch (Exception e) {
//						Log.e("Gathering", "sendScanRslt", e);
                        e.printStackTrace();
                        UIHandler.sendEmptyMessage(DataCore.DATA_SEND_FAILED);
                    }
                }
            });
            networkThread.start();
        }
    }

    Handler UIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DataCore.ON_CLICK_CANCLE_ON_UPLOAD:
                    break;
                case DataCore.ON_CLICK_CANCLE_ON_LOCATION_UPLOAD:
                    break;
//    			case DataCore.ON_CLICK_OK_ON_DELETE:
//    				precessDeleteData();
//    				break;

                case DataCore.ON_CLICK_CANCLE_ON_DELETE:
                    break;

                case DataCore.DATA_SEND_ENDED:
                    processDataSendEnded();
                    break;

                case DataCore.DATA_SEND_FAILED:
                    Toast.makeText(ResultList_Path_Act.this, "전송이 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        private void processDataSendEnded() {
            try {
                Toast.makeText(ResultList_Path_Act.this, "전송이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                String name = sendFile.getName();
                String rename = name.replace(".temp", "");
                rename = rename.replace(".log", "");

                File log = new File(StaticManager.getResultPath() + rename + ".log");
                File temp = new File(StaticManager.getResultPath() + rename + ".temp");

                log.renameTo(new File(StaticManager.getResultPath() + "완료_" + log.getName()));
                temp.renameTo(new File(StaticManager.getResultPath() + "완료_" + temp.getName()));
                sendFile = null;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                inputData();
                adapter.notifyDataSetChanged();
            }
        }
		
		/*private void precessDeleteData()
		{
			try
			{
				File selectedKey = fileList.get(selectedIdx);
				if(selectedKey.delete() == true)
				{
					fileList.remove(selectedIdx);
					Toast.makeText(GatherListActivity.this, "데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
				}
				else
				{
					Toast.makeText(GatherListActivity.this, "데이터 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				inputData();
				adapter.notifyDataSetChanged();
			}
		}*/
    };

    static public void parseBasePointFromXml(String folderName) {
        try {
            XmlPullParserFactory parserFac = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserFac.newPullParser();
            parser.setInput(new FileInputStream(new File(Environment.getExternalStorageDirectory().getPath() + "/gathering/area" + "/" + folderName + "/" + folderName + ".xml")), "UTF-8");
            int parserEvent = parser.getEventType();
            String tag = "";
            boolean flag9 = false, flag10 = false;

            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG:

                        tag = parser.getName();
//					Log.i("tag", "START_TAG = "+tag);

                        if (tag != null && !tag.equals("")) {
                            if (tag.equalsIgnoreCase("Geo_BasePoint_x")) {
                                flag9 = true;
                            } else if (tag.equalsIgnoreCase("Geo_BasePoint_y")) {
                                flag10 = true;
                            }
                        }

                        break;

                    case XmlPullParser.TEXT:

                        if (flag9) {
                            BaseX = (Double.valueOf(parser.getText())) * 10;
                        } else if (flag10) {
                            BaseY = (Double.valueOf(parser.getText())) * 10;
                        }

                        break;

                    case XmlPullParser.END_TAG:

                        tag = parser.getName();
//					Log.i("tag", "END_TAG = "+tag);

                        if (tag != null && !tag.equals("")) {
                            if (tag.equalsIgnoreCase("Geo_BasePoint_x")) {
                                flag9 = false;
                            } else if (tag.equalsIgnoreCase("Geo_BasePoint_y")) {
                                flag10 = false;
                            }
                        }
                        break;
                }

                parserEvent = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
