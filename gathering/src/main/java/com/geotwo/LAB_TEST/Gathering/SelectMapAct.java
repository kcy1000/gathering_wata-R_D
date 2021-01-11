package com.geotwo.LAB_TEST.Gathering;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.data.FloorInfo;
import com.geotwo.LAB_TEST.data.StaticManager;
import com.geotwo.common.SpinnerAdapter;
import com.wata.LAB_TEST.Gathering.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;

public class SelectMapAct extends Activity {
    private final String AREA_DIR = Environment.getExternalStorageDirectory().getPath() + "/gathering/area";
    private ArrayList<String> mapArr;
    private ListView selectmap_list;
    private MapInfoAdapter adapter;
    private EditText mEditSerch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_selectmap);
        WataLog.i("onCreate");
        inputData();
        setLayout();

        findViewById(R.id.selectmap_back).setOnClickListener(btnEvent);

    }

    private void inputData() {

        mapArr = new ArrayList<String>();

        File dir = new File(AREA_DIR);

        if (dir.exists()) {
            String[] strList = dir.list();
            int cnt = 0;
            for (int a = 0; a < strList.length; a++) {
                if (strList[a].contains("_")) {
                    String[] splitData = strList[a].split("_");
                    String station = splitData[1];
                    String city = splitData[0];
                    if (splitData.length == 2) {
                        mapArr.add(station + "(" + city + ")");
                        WataLog.d("get name = " + station + "(" + city + ")");
                    } else {
                        String line = splitData[2];
                        mapArr.add(station + "(" + city + "/" + line + ")");
                        WataLog.d("get name = " + line + "/" + station + "(" + city + ")");
                    }
                } else {
                    mapArr.add(strList[a]);
                }
            }
        }

        Collections.sort(mapArr);
    }

    private void setLayout() {
        selectmap_list = (ListView) findViewById(R.id.selectmap_list);
        adapter = new MapInfoAdapter(SelectMapAct.this, R.layout.new_selectmap_row, mapArr);
        selectmap_list.setAdapter(adapter);

        mEditSerch = (EditText) findViewById(R.id.Edit_serch);
        TextWatcher watcher = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (mEditSerch.isFocusable()) {
                    String temp = s.toString();

                    adapter.setType(temp);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        };

        mEditSerch.addTextChangedListener(watcher);
    }

    private OnClickListener btnEvent = new OnClickListener() {
        @Override
        public void onClick(View v) {
//			switch(v.getId())
//			{
            if (v.getId() == R.id.selectmap_back)
                finish();
//				break;
//			}
        }
    };

    class MapInfoAdapter extends ArrayAdapter<String> implements OnClickListener {
        ArrayList<String> items, Searchitems;
        Context context = null;
        private String savedType = "ALL";

        public MapInfoAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
            super(context, textViewResourceId, items);
            this.context = context;

            this.items = new ArrayList<String>();
            this.Searchitems = new ArrayList<String>();

            insertData(items, this.items);
        }

        private void insertData(ArrayList<String> items, ArrayList<String> list) {
            // TODO Auto-generated method stub
            for (int i = 0; i < items.size(); i++) {
                list.add(items.get(i));
            }
        }


        public void setType(String type) {
            // TODO Auto-generated method stub
            if (!type.equals(""))
                savedType = type;
            else
                savedType = "ALL";
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            int size = 0;
            int fullsize = items.size();

            if (Searchitems != null)
                Searchitems.clear();

            if (savedType.equalsIgnoreCase("ALL")) {
                size = fullsize;
                insertData(items, Searchitems);
            } else {
                for (int i = 0; i < fullsize; i++) {
                    String temp = items.get(i);
                    if (temp.contains(savedType)) {
                        size++;
                        Searchitems.add(temp);
                    }
                }
            }
            return size;
        }


        @Override
        public View getView(int position, View v, ViewGroup parent) {
            if (v == null) {
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(R.layout.new_selectmap_row, null);
            }

            String strName = Searchitems.get(position);

            if (strName != null) {
                TextView selectmap_row_text = (TextView) v.findViewById(R.id.selectmap_row_text);
                selectmap_row_text.setText(strName);

                WataLog.d("name = " + strName);
            }

            v.setTag(position);
            v.setOnClickListener(this);

            return v;
        }

        @Override
        public void onClick(View v) {
            int pos = Integer.valueOf(v.getTag().toString());
            WataLog.d("pos=" + pos);
            String stationName = null;
            String cityName = null;
            String folderName = null;
            String lineName = null;
            if (Searchitems.get(pos).contains("(") && Searchitems.get(pos).contains(")")) {
                stationName = Searchitems.get(pos).substring(0, Searchitems.get(pos).indexOf("("));
                String[] cityName_tmp = Searchitems.get(pos).substring(Searchitems.get(pos).indexOf("(") + 1, Searchitems.get(pos).indexOf(")")).split("/");
                cityName = cityName_tmp[0];
                if (cityName_tmp.length == 1) {
                    folderName = cityName + "_" + stationName;
                } else {
                    lineName = cityName_tmp[1];
                    folderName = cityName + "_" + stationName + "_" + lineName;
                }

                WataLog.d("select folderName = " + folderName);
            } else {
                folderName = Searchitems.get(pos);
            }

            StaticManager.setEmptyAll();
            if (folderName == null || !folderName.equalsIgnoreCase("new_area")) {
                WataLog.i("chekc!!!!!! --1");
                WataLog.d("folderName = " + folderName);
                parseXml(folderName);
                WataLog.d("title = " + StaticManager.title);
                WataLog.d("worker = " + StaticManager.worker);
                WataLog.d("workdate = " + StaticManager.workdate);
                WataLog.d("gatheringdate = " + StaticManager.gatheringdate);
                if (StaticManager.floorInfo != null) {
                    WataLog.e("StaticManager.floorInfo.size() = " + StaticManager.floorInfo.size());
                }

                Intent select = new Intent();
                setResult(RESULT_OK, select);

                finish();
            } else {
                WataLog.i("chekc!!!!!! --2");
                StaticManager.folderName = folderName;
                LayoutInflater inflater = getLayoutInflater();
                View input = inflater.inflate(R.layout.gid_layout, null);

                final Spinner gid = (Spinner) input.findViewById(R.id.input_gid);
                final EditText floor = (EditText) input.findViewById(R.id.input_floor);
                final EditText build = (EditText) input.findViewById(R.id.input_build);
                String[] arr = selectGIDDialog();
                if (arr != null) {
                    SpinnerAdapter adapter = new SpinnerAdapter(SelectMapAct.this, android.R.layout.simple_spinner_item, arr);
                    gid.setAdapter(adapter);
                    //				gid.setOnClickListener(new OnClickListener(){
                    //					@Override
                    //					public void onClick(View v) {
                    //						// TODO Auto-generated method stub
                    //						selectGIDDialog();
                    //					}
                    //				});
                    new AlertDialog.Builder(SelectMapAct.this)
                            .setTitle("GID 및 층 지정")
                            .setView(input)
                            .setNeutralButton("지정", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    StaticManager.gid = (String) gid.getSelectedItem();
                                    StaticManager.floorName = floor.getText().toString();
                                    StaticManager.worker = "PILOT_KISA";
                                    StaticManager.title = build.getText().toString();
                                    WataLog.d("title = " + StaticManager.title);
                                    WataLog.d("worker = " + StaticManager.worker);
                                    WataLog.d("gid = " + StaticManager.gid);
                                    //							Log.i("tag", "gatheringdate = "+StaticManager.gatheringdate);
                                    Intent select = new Intent();
                                    setResult(RESULT_OK, select);
                                    finish();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {


                                }
                            }).show();
                }
            }
        }

        private String[] selectGIDDialog() {
            String refName = Environment.getExternalStorageDirectory().getPath() + "/gathering/gids.txt";
            String[] lineArr = null;
            File refFile = new File(refName);
            StringBuffer strBuf = null;
            if (refFile.exists()) {
                if (strBuf == null) {
                    strBuf = new StringBuffer();
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new FileReader(refFile));
                        String str = "";
                        while ((str = br.readLine()) != null) {
                            strBuf.append(str);
                            strBuf.append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (br != null) {
                            try {
                                br.close();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                    }
                    lineArr = strBuf.toString().split("\n");
                }

//				if(lineArr!=null)
//				{
//					final String[] gids = lineArr; 
//					new AlertDialog.Builder(SelectMapAct.this)
//					.setTitle("GID를 선택하세요.")
//					.setItems(lineArr, new DialogInterface.OnClickListener()
//					{	
//						@Override
//						public void onClick(DialogInterface dialog, int which) 
//						{
//							if(which < gids.length)
//							{
//								Log.d("jeongyeol", gids[which]);
//							}
//						}
//					})
//					.show();
//				}
            }
            return lineArr;
        }

        public void parseXml(String folderName) {
        	WataLog.d("folderName=" + folderName);
            try {
                XmlPullParserFactory parserFac = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserFac.newPullParser();
                String xmlName = null;
                if (folderName.contains("_")) {
                    xmlName = folderName.split("_")[1];
                    parser.setInput(new FileInputStream(new File(AREA_DIR + "/" + folderName + "/" + xmlName + ".xml")), "UTF-8");
                } else {
                    parser.setInput(new FileInputStream(new File(AREA_DIR + "/" + folderName + "/" + folderName + ".xml")), "UTF-8");
                }


                int parserEvent = parser.getEventType();
                String tag = "";
                boolean flag1 = false, flag2 = false, flag3 = false, flag4 = false, flag5 = false, flag6 = false, flag7 = false, flag8 = false,
                        flag9 = false, flag10 = false;
                boolean nameFlag = false, wallFlag = false, baseFlag = false, poiFlag = false, pathFlag = false,
                        latFlag = false, lonFlag = false, bearFlag = false, Acc_threshold = false;
                FloorInfo info = null;
                ArrayList<FloorInfo> floorArr = new ArrayList<FloorInfo>();

                while (parserEvent != XmlPullParser.END_DOCUMENT) {
                    switch (parserEvent) {
                        case XmlPullParser.START_TAG:

                            tag = parser.getName();
//						Log.i("tag", "START_TAG = "+tag);

                            if (tag != null && !tag.equals("")) {
                                if (tag.equalsIgnoreCase("title")) {
                                    flag1 = true;
                                } else if (tag.equalsIgnoreCase("foldername")) {
                                    flag2 = true;
                                } else if (tag.equalsIgnoreCase("worker")) {
                                    flag3 = true;
                                } else if (tag.equalsIgnoreCase("workdate")) {
                                    flag4 = true;
                                } else if (tag.equalsIgnoreCase("gatheringdate")) {
                                    flag5 = true;
                                } else if (tag.equalsIgnoreCase("space")) {
                                    flag6 = true;
                                } else if (tag.equalsIgnoreCase("address")) {
                                    flag7 = true;
                                } else if (tag.equalsIgnoreCase("gid")) {
                                    flag8 = true;
                                } else if (tag.equalsIgnoreCase("Geo_BasePoint_x")) {
                                    flag9 = true;
                                } else if (tag.equalsIgnoreCase("Geo_BasePoint_y")) {
                                    flag10 = true;
                                } else if (tag.equalsIgnoreCase("floor")) {
                                    info = new FloorInfo();
                                } else if (tag.equalsIgnoreCase("name")) {
                                    nameFlag = true;
                                } else if (tag.equalsIgnoreCase("wall")) {
                                    wallFlag = true;
                                } else if (tag.equalsIgnoreCase("base")) {
                                    baseFlag = true;
                                } else if (tag.equalsIgnoreCase("poi")) {
                                    poiFlag = true;
                                } else if (tag.equalsIgnoreCase("path")) {
                                    pathFlag = true;
                                } else if (tag.equalsIgnoreCase("lat")) {
                                    latFlag = true;
                                } else if (tag.equalsIgnoreCase("lon")) {
                                    lonFlag = true;
                                } else if (tag.equalsIgnoreCase("bearing")) {
                                    bearFlag = true;
                                } else if (tag.equalsIgnoreCase("Acc_threshold")) {
                                    Acc_threshold = true;
                                }
                            }

                            break;

                        case XmlPullParser.TEXT:

                            if (flag1) {
                                StaticManager.setTitle(parser.getText());
                            } else if (flag2) {
                                StaticManager.setFolderName(parser.getText());
                            } else if (flag3) {
                                StaticManager.setWorker(parser.getText());
                            } else if (flag4) {
                                StaticManager.setWorkdate(parser.getText());
                            } else if (flag5) {
                                StaticManager.setGatheringdate(parser.getText());
                            } else if (flag7) {
                                StaticManager.setAddress(parser.getText());
                            } else if (flag8) {
                                WataLog.i("check 66666");
                                WataLog.d("parser.getText()="+parser.getText());
                                StaticManager.setGid(parser.getText());

                            } else if (flag9) {
                                StaticManager.setBasePointX(parser.getText());
                            } else if (flag10) {
                                StaticManager.setBasePointY(parser.getText());
                            } else if (Acc_threshold) {
                                StaticManager.Acc_threshold = parser.getText();
                            } else if (flag6) {
                                if (nameFlag) {
                                    info.setName(parser.getText());
                                } else if (wallFlag) {
                                    info.setWall(parser.getText());
                                } else if (baseFlag) {
                                    info.setBase(parser.getText());
                                } else if (poiFlag) {
                                    info.setPoi(parser.getText());
                                } else if (pathFlag) {
                                    info.setPath(parser.getText());
                                } else if (latFlag) {
                                    info.setLat(parser.getText());
                                } else if (lonFlag) {
                                    info.setLon(parser.getText());
                                } else if (bearFlag) {
                                    info.setBearing(parser.getText());
                                }
                            }

                            break;

                        case XmlPullParser.END_TAG:

                            tag = parser.getName();
//						Log.i("tag", "END_TAG = "+tag);

                            if (tag != null && !tag.equals("")) {
                                if (tag.equalsIgnoreCase("title")) {
                                    flag1 = false;
                                } else if (tag.equalsIgnoreCase("foldername")) {
                                    flag2 = false;
                                } else if (tag.equalsIgnoreCase("worker")) {
                                    flag3 = false;
                                } else if (tag.equalsIgnoreCase("workdate")) {
                                    flag4 = false;
                                } else if (tag.equalsIgnoreCase("gatheringdate")) {
                                    flag5 = false;
                                } else if (tag.equalsIgnoreCase("address")) {
                                    flag7 = false;
                                } else if (tag.equalsIgnoreCase("gid")) {
                                    flag8 = false;
                                } else if (tag.equalsIgnoreCase("Geo_BasePoint_x")) {
                                    flag9 = false;
                                } else if (tag.equalsIgnoreCase("Geo_BasePoint_y")) {
                                    flag10 = false;
                                } else if (tag.equalsIgnoreCase("space")) {
                                    StaticManager.setFloorInfo(floorArr);
                                    flag6 = false;
                                } else if (tag.equalsIgnoreCase("floor")) {
                                    floorArr.add(info);
                                    info = null;
                                } else if (tag.equalsIgnoreCase("name")) {
                                    nameFlag = false;
                                } else if (tag.equalsIgnoreCase("wall")) {
                                    wallFlag = false;
                                } else if (tag.equalsIgnoreCase("base")) {
                                    baseFlag = false;
                                } else if (tag.equalsIgnoreCase("poi")) {
                                    poiFlag = false;
                                } else if (tag.equalsIgnoreCase("path")) {
                                    pathFlag = false;
                                } else if (tag.equalsIgnoreCase("lat")) {
                                    latFlag = false;
                                } else if (tag.equalsIgnoreCase("lon")) {
                                    lonFlag = false;
                                } else if (tag.equalsIgnoreCase("bearing")) {
                                    bearFlag = false;
                                } else if (tag.equalsIgnoreCase("Acc_threshold")) {
                                    Acc_threshold = false;
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
}
