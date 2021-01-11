package com.geotwo.LAB_TEST.Gathering;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.dialog.GatheredDataListDialog;
import com.geotwo.LAB_TEST.Gathering.savedata.LoadGatheredData;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.data.StaticManager;
import com.geotwo.o2mapmobile.util.Color;
import com.wata.LAB_TEST.Gathering.R;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ETRI.LBSP.Common.Interfaces.ConnectorRMCS;
import ETRI.LBSP.Common.Interfaces.PDI_REPT_COL_DATA;

public class GatherListActivity extends Activity {
    ListView listView = null;
    DataCore dataCore = null;
    GatherListAdapter listAdapter = null;
    GatheredDataListDialog gaterDialog = null;
    ArrayList<File> fileList = null;

    int selectedIdx = 0;
    int serverResponseCode = 0;

    private TextView result_tab1, result_tab2, result_tab3;
    private boolean isTab1 = false;
    private boolean isTab2 = false;
    private boolean isTab3 = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gather_result_layout);

        initView();

        findViewById(R.id.result_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        isTab1 = true;

        if (gaterDialog == null) {
            gaterDialog = new GatheredDataListDialog(this, UIHandler, fileList);
        }

        if (dataCore == null) {
            dataCore = DataCore.getInstance();
        }

        if (listAdapter == null) {
//    		fileList = LoadGatheredData.getSavedSendableData();
            fileList = getGatheringPathData();
            listAdapter = new GatherListAdapter(this, R.layout.gather_result_list_row, fileList);
        }

        if (listView == null) {
            listView = (ListView) findViewById(R.id.gather_list_list_view);
            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    WataLog.d("isTab1= " + isTab1);
                    if (isTab1) {
                        String locationName = fileList.get(arg2).getName();
                        Intent goLoc = new Intent(GatherListActivity.this, ResultList_Location_Act.class);
                        StaticManager.setFolderName(locationName);
                        WataLog.d("locationName= " + locationName);
                        goLoc.putExtra("locationName", locationName);

                        startActivity(goLoc);
                    }
                    if (isTab2) {
                        selectedIdx = arg2;
                        gaterDialog.makeUpLoadConfirmDialog().show();
                        WataLog.d("GatheringListActivity url = " + dataCore.getURL());
                    }
                    if (isTab3) {
                        selectedIdx = arg2;
                        gaterDialog.makeLocationUpLoadConfirmDialog().show();
                        WataLog.d("tab3 click!");
                    }
                }
            });

            listView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if (!isTab1) {
                        selectedIdx = arg2;
                        File selectedFile = fileList.get(selectedIdx);
                        String fileName = selectedFile.getName();
                        if (fileName.endsWith("temp")) {
                            fileName = fileName.replace(".temp", "");
                        } else {
                            fileName = fileName.replace(".txt", "");
                        }
                        gaterDialog.makeDeleteConfirmDialog(fileName).show();
                    }

                    return true;
                }
            });
        }

        result_tab1 = (TextView) findViewById(R.id.result_tab1);
        result_tab2 = (TextView) findViewById(R.id.result_tab2);
        result_tab3 = (TextView) findViewById(R.id.result_tab3);

        result_tab1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isTab1 = true;
                isTab2 = false;
                isTab3 = false;
                result_tab1.setBackgroundColor(new Color(245, 245, 245, 255).getInt());
                result_tab2.setBackgroundColor(new Color(220, 220, 220, 255).getInt());
                result_tab3.setBackgroundColor(new Color(220, 220, 220, 255).getInt());
                fileList = getGatheringPathData();
                listAdapter = new GatherListAdapter(GatherListActivity.this, R.layout.gather_result_list_row, fileList);
                listView.setAdapter(listAdapter);
            }
        });

        result_tab2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isTab1 = false;
                isTab2 = true;
                isTab3 = false;
                result_tab1.setBackgroundColor(new Color(220, 220, 220, 255).getInt());
                result_tab2.setBackgroundColor(new Color(245, 245, 245, 255).getInt());
                result_tab3.setBackgroundColor(new Color(220, 220, 220, 255).getInt());
                fileList = LoadGatheredData.getSavedSendableData();
                listAdapter = new GatherListAdapter(GatherListActivity.this, R.layout.gather_result_list_row, fileList);
                listView.setAdapter(listAdapter);
            }
        });

        result_tab3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isTab1 = false;
                isTab2 = false;
                isTab3 = true;
                result_tab1.setBackgroundColor(new Color(220, 220, 220, 255).getInt());
                result_tab2.setBackgroundColor(new Color(220, 220, 220, 255).getInt());
                result_tab3.setBackgroundColor(new Color(245, 245, 245, 255).getInt());
                fileList = LoadGatheredData.getSavedSendableLogData();
                listAdapter = new GatherListAdapter(GatherListActivity.this, R.layout.gather_result_list_row, fileList);
                listView.setAdapter(listAdapter);
            }
        });
    }

    private ArrayList<File> getGatheringPathData() {
        ArrayList<File> items = new ArrayList<File>();
        String dataPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/pathGathering";
//        String dataPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/subway";
        File pathFolder = new File(dataPath);
//    	Log.i("tag", "dataPath = "+dataPath);
        if (pathFolder.exists()) {
            String[] list = pathFolder.list();
            WataLog.d("list.length = " + list.length);
            if (list != null) {
                for (int a = 0; a < list.length; a++) {
                    File item = new File(dataPath + "/" + list[a]);
                    items.add(item);
                    WataLog.d("dataPath+/+list[a] = " + dataPath + "/" + list[a]);
                }
            }
        }

        return items;
    }

    Handler UIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DataCore.ON_CLICK_OK_ON_UPLOAD:
                    processUpload();
                    break;
                case DataCore.ON_CLICK_CANCLE_ON_UPLOAD:
                    break;
                case DataCore.ON_CLICK_CANCLE_ON_LOCATION_UPLOAD:
                    break;
                case DataCore.ON_CLICK_OK_ON_LOCATION_UPLOAD:
                    processLocationUpload();
                    break;
                case DataCore.ON_CLICK_OK_ON_DELETE:
                    precessDeleteData();
                    break;
                case DataCore.ON_CLICK_CANCLE_ON_DELETE:
                    break;

                case DataCore.DATA_SEND_ENDED:
                    processDataSendEnded();
                    break;
                case DataCore.DATA_SEND_FAILED:
                    Toast.makeText(GatherListActivity.this, "전송이 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        private void processDataSendEnded() {
            try {
                Toast.makeText(GatherListActivity.this, "전송이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                File selectedKey = fileList.get(selectedIdx);
                String name = selectedKey.getName();

                if (!name.substring(0, 2).equalsIgnoreCase("완료"))
                    selectedKey.renameTo(new File(dataCore.getTempDataPath() + "완료_" + name));
//				if(selectedKey.delete() == true)
//					fileList.remove(selectedIdx);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fileList = LoadGatheredData.getSavedSendableData();
                listAdapter = new GatherListAdapter(GatherListActivity.this, R.layout.gather_result_list_row, fileList);
                listView.setAdapter(listAdapter);
//				for(int a=0; a<fileList.size(); a++)
//				{
//					Log.i("tag", "notifyDataSetChanged => "+fileList.get(a).getName());
//				}
//				listAdapter.notifyDataSetChanged();
            }
        }

        private void processLocationDataSendEnded() {
            try {
                Toast.makeText(GatherListActivity.this, "전송이 완료되었습니다.", Toast.LENGTH_SHORT).show();

                File selectedKey = fileList.get(selectedIdx);
                String name = selectedKey.getName();

                if (!name.substring(0, 2).equalsIgnoreCase("완료"))
                    selectedKey.renameTo(new File(dataCore.getTempDataPath() + "완료_" + name));
//				if(selectedKey.delete() == true)
//					fileList.remove(selectedIdx);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fileList = LoadGatheredData.getSavedSendableLocationData();
                listAdapter = new GatherListAdapter(GatherListActivity.this, R.layout.gather_result_list_row, fileList);
                listView.setAdapter(listAdapter);
            }
        }


        private void processUpload() {
            File selectedFile = fileList.get(selectedIdx);
            if (selectedFile.getName().substring(0, 2).equalsIgnoreCase("완료")) {
                String area = selectedFile.getName().substring(
                        selectedFile.getName().indexOf("_", selectedFile.getName().indexOf("_") + 1) + 1, selectedFile.getName().indexOf("-"));
                WataLog.d("area =" + area);
//				
//				if(area.equalsIgnoreCase("인천공항"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Incheonairport");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("사당역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("sadang");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("시청역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("cityhall");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("서울역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("seoul");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("종로5가"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("jongro5ga");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("종각역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("jonggak");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("지오투"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("office");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("충무로"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Chungmuro");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("동대문"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Dongdaemoon");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("을지로입구역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Euliiro1ga");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("낙성대"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Nakseongdae");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("삼성역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Samsung");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("서울역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("seoul");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("신도림"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Sindorim");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("종합운동장역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Sportscomplex");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("역삼역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Yeoksam");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("교대"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Gyodae");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("강남"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Gangnam");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("서울대입구"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Seoulnat'luniv");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("남부터미널"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Nambuterminal");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("서초"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Seocho");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("신천"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Sincheon");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("봉천"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Bongcheon");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("방배"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Bangbae");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("선릉"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Seonreung");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("오석관"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("oseok");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("현동홀"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("hyundong");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("올네이션즈홀"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("allnations");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("효암관"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("hyoam");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("국제언어교육원"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("ILC");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("느헤미아"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("nehemiah");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("뉴턴홀"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("newton");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("학생회관"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("studentunion");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//			}
//			else
//			{
//				String area = selectedFile.getName().substring(selectedFile.getName().indexOf("_")+1, selectedFile.getName().indexOf("-"));
//				Log.d("jeongyeol", "area ="+area);
//				
//				if(area.equalsIgnoreCase("인천공항"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Incheonairport");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("사당역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("sadang");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("시청역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("cityhall");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("서울역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("seoul");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("종로5가"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("jongro5ga");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("종각역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("jonggak");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("지오투"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("office");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//					
//				}
//				else if(area.equalsIgnoreCase("충무로"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Chungmuro");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("동대문"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Dongdaemoon");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("을지로입구역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Euliiro1ga");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("낙성대"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Nakseongdae");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("삼성역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Samsung");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("서울역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("seoul");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("신도림"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Sindorim");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("종합운동장역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Sportscomplex");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("역삼역"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Yeoksam");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("교대"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Gyodae");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("강남"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Gangnam");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("서울대입구"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Seoulnat'luniv");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("남부터미널"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Nambuterminal");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("서초"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Seocho");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("신천"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Sincheon");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("봉천"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Bongcheon");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("방배"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Bangbae");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("선릉"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("Seonreung");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("오석관"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("oseok");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("현동홀"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("hyundong");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("올네이션즈홀"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("allnations");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("효암관"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("hyoam");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("국제언어교육원"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("ILC");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("느헤미아"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("nehemiah");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("뉴턴홀"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("newton");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
//				else if(area.equalsIgnoreCase("학생회관"))
//				{
//					ResultList_Path_Act.parseBasePointFromXml("studentunion");
//					Log.e("jeongyeol", "record2 basex ="+ResultList_Path_Act.BaseX+"/basey ="+ResultList_Path_Act.BaseY);
//				}
            }
            PDI_REPT_COL_DATA selectedData = LoadGatheredData.decodeSavedSendableData(selectedFile);
            if (selectedFile.getName().substring(selectedFile.getName().indexOf(".") + 1, selectedFile.getName().length()).equalsIgnoreCase("temp"))
                sendScanRslt(selectedData, 0);
            else
                sendScanRslt(selectedData, 1);
            //processDataSendEnded();
        }

        private void processLocationUpload() {
            new serverFileUpload().execute();

//			PDI_REPT_COL_DATA selectedData = LoadGatheredData.decodeSavedSendableData(selectedFile);
//			if(selectedFile.getName().substring(selectedFile.getName().indexOf(".")+1, selectedFile.getName().length()).equalsIgnoreCase("temp"))
//				sendScanRslt(selectedData, 0);
//			else
//				sendScanRslt(selectedData, 1);

            //processDataSendEnded();
        }

        class serverFileUpload extends AsyncTask<Void, Void, Integer> {
            File selectedFile_ = fileList.get(selectedIdx);
            String selectedFile = String.valueOf(fileList.get(selectedIdx));
            String selectedFileName = fileList.get(selectedIdx).getName();

            @Override
            protected void onPostExecute(Integer serverResponseCode) {
                if (serverResponseCode == 200) {
                    if (!selectedFileName.substring(0, 2).equalsIgnoreCase("완료")) {
                        selectedFile_.renameTo(new File(dataCore.getLocationLogDataPath() + "완료_" + selectedFile_.getName()));
                        processLocationDataSendEnded();
                    }
                }
            }

            @Override
            protected Integer doInBackground(Void... params) {
                String serverUrl = "http://" + dataCore.getURL() + ":8000/gathering/upload.php?type=indoor";
                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;

                try {
                    SimpleDateFormat sDateFomat = new SimpleDateFormat("yy.MM.dd", Locale.KOREA);
                    Date modifiedDate_tmp = new Date(selectedFile_.lastModified());
                    String modifiedDate = sDateFomat.format(modifiedDate_tmp);
                    String selectedFileNameEncode = URLEncoder.encode(modifiedDate + "_" + selectedFileName.replaceAll("완료_", ""), "UTF-8");
                    FileInputStream fis = new FileInputStream(selectedFile);
                    URL url = new URL(serverUrl);
                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("uploaded_file", selectedFileNameEncode);

                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + selectedFileNameEncode + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    // create a buffer of  maximum size
                    bytesAvailable = fis.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fis.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {

                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fis.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fis.read(buffer, 0, bufferSize);

                    }

                    // send multipart form data necesssary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    Log.d("whdrms", conn.getResponseMessage());
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);

                    if (serverResponseCode == 200) {
                        Log.d("whdrms", "Upload success");
                    }

                    //close the streams //
                    fis.close();
                    dos.flush();
                    dos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Upload file to server", "error1: " + e.getMessage(), e);
                }
                return serverResponseCode;
            }
        }

        private void precessDeleteData() {
            try {
                File selectedKey = fileList.get(selectedIdx);

                if (selectedKey.delete() == true) {
                    fileList.remove(selectedIdx);
                    Toast.makeText(GatherListActivity.this, "데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(GatherListActivity.this, "데이터 삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fileList = LoadGatheredData.getSavedSendableData();
                listAdapter.notifyDataSetChanged();
            }
        }
    };

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
//					Log.e("Gathering", "sendScanRslt", e);
                    e.printStackTrace();
                    UIHandler.sendEmptyMessage(DataCore.DATA_SEND_FAILED);
                }
            }
        });
        networkThread.start();
    }

    class GatherListAdapter extends ArrayAdapter<File> {
        int resource = 0;
        String locationName = "";

        public GatherListAdapter(Context context, int resource, List<File> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout row = null;

            if (convertView == null) {
                row = new RelativeLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi;
                vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource, row, true);
            } else {
                row = (RelativeLayout) convertView;
            }

            TextView fileName = (TextView) row.findViewById(R.id.file_name);
            TextView file_num = (TextView) row.findViewById(R.id.file_num);
            String num = String.valueOf(position + 1);
            file_num.setText(num);

            File currentFile = super.getItem(position);
            WataLog.d("currentFile=" + currentFile);
            locationName = currentFile.getName();
            fileName.setText(locationName);
            WataLog.d("locationName=" + locationName);

            return row;
        }
    }

}
