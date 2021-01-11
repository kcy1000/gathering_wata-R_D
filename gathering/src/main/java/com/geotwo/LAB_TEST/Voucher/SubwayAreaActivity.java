package com.geotwo.LAB_TEST.Voucher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.PathDrawingActivity;
import com.geotwo.LAB_TEST.Gathering.Retrofit.RetrofitExService;
import com.geotwo.LAB_TEST.Gathering.dto.PoiInfo;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.Voucher.Retrofit.OfficeInfo;
import com.geotwo.LAB_TEST.Voucher.Retrofit.SubwayData;
import com.geotwo.LAB_TEST.Voucher.Retrofit.SubwayFloorInfo;
import com.geotwo.LAB_TEST.Voucher.Retrofit.SubwayInfo;
import com.google.gson.Gson;
import com.wata.LAB_TEST.Gathering.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import com.geotwo.o2mapmobile.shape.CSFReader;

//import geo2.lbsp.ble.BLEManager;

//import geo2.lbsp.ble.BLEManager;

public class SubwayAreaActivity extends AppCompatActivity implements OnClickListener {

    private TextView titleText;
    private View officeLayout;
    private LinearLayout subwayLayout;
    private Spinner officeAreaSpinner, officeName, officeFloors;
    private String DATA_MODE = "subway";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WataLog.i("onCreate");
        setContentView(R.layout.subway_area_activity);

        TextView area_title_layout = (TextView) findViewById(R.id.area_title_layout);
        area_title_layout.setOnClickListener(this);

        area_spinner = (Spinner) findViewById(R.id.area_spinner);
        area_spinner.setPrompt("지역을 선택하세요.");

        line_num_spinner = (Spinner) findViewById(R.id.line_num_spinner);
        line_num_spinner.setPrompt("노선을 선택하세요.");

        name_spinner = (Spinner) findViewById(R.id.name_spinner);
        name_spinner.setPrompt("지화철을 선택하세요.");

        floors_lever_spinner = (Spinner) findViewById(R.id.floors_lever_spinner);
        floors_lever_spinner.setPrompt("층수를 선택하세요.");

        RelativeLayout make_map_btn = (RelativeLayout) findViewById(R.id.make_map_btn);
        make_map_btn.setOnClickListener(this);

        subwayLayout = (LinearLayout)findViewById(R.id.subway_layout);
        officeLayout = (View)findViewById(R.id.office_layout);

        RelativeLayout company_subway_btn = (RelativeLayout)findViewById(R.id.company_subway_btn);
        company_subway_btn.setOnClickListener(this);

        titleText = (TextView)findViewById(R.id.title_text);

        // office 설정
        officeAreaSpinner = (Spinner) officeLayout.findViewById(R.id.office_area_spinner);
        officeAreaSpinner.setPrompt("기업위치를 선택하세요.");

        officeName  = (Spinner) officeLayout.findViewById(R.id.office_spinner);
        officeName.setPrompt("기업명을 선택하세요.");

        officeFloors = (Spinner) officeLayout.findViewById(R.id.office_floors_lever_spinner);
        officeFloors.setPrompt("층수를 선택하세요.");

        areaList.clear();
        getSubwayArea();

    }

    private ArrayList<SubwayData> areaList = new ArrayList<SubwayData>();
    private ArrayList<String> areaNameList = new ArrayList<String>();;

    private String mSubWayName , mLineNum, mFloorsLever, mStationNo, mLineName;
    private String mCenterX, mCenterY;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.make_map_btn:
                if("subway".equals(DATA_MODE)) {
                    onCheckSettingDialog(mLineNum + " " + mSubWayName + " " + mFloorsLever + "을 수집하시겠습니까?");
                } else {
                    onCheckSettingDialog(mCompanyNm  + " " + mFloorInfo + "을 수집하시겠습니까?");
                }

                break;
            case R.id.company_subway_btn:
                WataLog.d("DATA_MODE=" + DATA_MODE);

                if("subway".equals(DATA_MODE)) {
                    DATA_MODE = "office";
                    titleText.setText(getString(R.string.subway));
                    officeLayout.setVisibility(View.VISIBLE);
                    subwayLayout.setVisibility(View.GONE);
                    getOfficeArea();
                } else {
                    DATA_MODE = "subway";
                    titleText.setText(getString(R.string.company));
                    subwayLayout.setVisibility(View.VISIBLE);
                    officeLayout.setVisibility(View.GONE);
                    getSubwayArea();
                }


                break;
            case R.id.area_title_layout:
                // test 모드
                Intent intent = new Intent(SubwayAreaActivity.this, VoucherActivity.class);

                // 지역정보
                intent.putExtra("s_id",S_Id.replaceAll(" ", ""));
                // 노선정보
                intent.putExtra("num_line",mLineNum.replaceAll(" ", ""));
                // 지하철이름
                intent.putExtra("sub_name", mSubWayName.replaceAll(" ", ""));
                //호선이름
                intent.putExtra("line_name", mLineName.replaceAll(" ", ""));

                // 지하철이름
                intent.putExtra("station_no", mStationNo.replaceAll(" ", ""));
                //중심점
                intent.putExtra("center_x", mCenterX);
                intent.putExtra("center_y", mCenterY);
                //층수
                intent.putExtra("floors_lever",mFloorsLever.replaceAll(" ", ""));
                intent.putExtra("fileurl", fileurl);
                intent.putExtra("cx", cx);
                intent.putExtra("cy", cy);
                intent.putExtra("rotate", rotate);
                intent.putExtra("scalex", scalex);
                intent.putExtra("scaley", scaley);
                intent.putExtra("gid", gId);

                intent.putExtra("sx", "14149288");
                intent.putExtra("sy", "4495570");

                startActivity(intent);

                break;
        }
    }

    private void onCheckSettingDialog(final String message) {
        AlertDialog.Builder alBuilder = new AlertDialog.Builder(SubwayAreaActivity.this);
        alBuilder.setMessage(message);
        alBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(SubwayAreaActivity.this, VoucherActivity.class);
                // 타입
                intent.putExtra("data_type", DATA_MODE);

                if("subway".equals(DATA_MODE)) {
                    // 지역정보
                    intent.putExtra("s_id",S_Id.replaceAll(" ", ""));
                    // 노선정보
                    intent.putExtra("num_line",mLineNum.replaceAll(" ", ""));
                    // 지하철이름
                    intent.putExtra("sub_name", mSubWayName.replaceAll(" ", ""));
                    //호선이름
                    intent.putExtra("line_name", mLineName.replaceAll(" ", ""));

                    // 지하철이름
                    intent.putExtra("station_no", mStationNo.replaceAll(" ", ""));
                    //중심점
                    intent.putExtra("center_x", mCenterX);
                    intent.putExtra("center_y", mCenterY);
                    //층수
                    intent.putExtra("floors_lever", mFloorsLever.replaceAll(" ", ""));
                    intent.putExtra("fileurl", fileurl);
                    intent.putExtra("cx", cx);
                    intent.putExtra("cy", cy);
                    intent.putExtra("rotate", rotate);
                    intent.putExtra("scalex", scalex);
                    intent.putExtra("scaley", scaley);
                    intent.putExtra("gid", gId);

                    intent.putExtra("sx", sX);
                    intent.putExtra("sy", sY);

                    WataLog.d("mFloorsLever=" + mFloorsLever);
                    if(!"".equals(mFloorsLever) && mFloorsLever != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(SubwayAreaActivity.this, "수집할 층 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                    }


                } else {
//                    mOGid, mAreaNm, mAreaNo, mCompanyNm, mCopanyNo, mFloorInfo, mOCenterX, mOCenterY, mOSx, mOSy;


                    // Gid
                    intent.putExtra("gid", mOGid);
                    // Oid
                    intent.putExtra("oid", O_Id);

                    //중심점
                    intent.putExtra("center_x", mCenterX);
                    intent.putExtra("center_y", mCenterY);
                    //지역코드
                    intent.putExtra("area_no", mAreaNo);
                    // 지역명
                    intent.putExtra("area_nm", mAreaNm);
                    //기업명
                    intent.putExtra("company_nm", mCompanyNm);
                    //기업코드
                    intent.putExtra("copany_no", mCopanyNo);
                    // 층정보
                    intent.putExtra("floor_info",  mFloorInfo);
                    //원점
                    intent.putExtra("sx", mOSx);
                    intent.putExtra("sy", mOSy);

                    startActivity(intent);
                }

            }
        });
        alBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alBuilder.show();
    }

    private void getSubwayArea() {
        WataLog.i("getSubwayArea");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        WataLog.d("retrofitExService=" + retrofitExService);

        retrofitExService.subwayArea().enqueue(new Callback<ResponseBody>() {
            //        retrofitExService.mapGpsSetting("139.7001198", "35.6895075").enqueue(new Callback<List<SubwayAreaData>>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터

                    JSONArray jsonArray = new JSONArray(result);
                    areaList.clear();
                    areaNameList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        WataLog.d(jsonObject.getString("area_nm"));

                        areaList.add(new SubwayData(jsonObject.getString("area_id"), jsonObject.getString("area_nm")));
                        areaNameList.add("  " +  jsonObject.getString("area_nm"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }

//                areaAdapter.setItems(areaList);
//                mGridView.setAdapter(areaAdapter);
                setAreaList();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("call=" + call.toString());
                WataLog.d("call=" + call.request());
                WataLog.d("call=" + t.getMessage());
                WataLog.d("call=" + t.getLocalizedMessage());


            }
        });
    }





    private Spinner area_spinner, line_num_spinner, name_spinner, floors_lever_spinner;
    private String S_Id, O_Id;
    // 지역설정
    private void setAreaList() {

        ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text_item, areaNameList);
        area_spinner.setAdapter(areaAdapter);
        area_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WataLog.d("position=" + position);
                S_Id = areaList.get(position).getId();
                WataLog.d("S_Id=" + S_Id);
                getSubwayLine(S_Id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private void getSubwayLine(String areaId) {
        areaId = areaId.replaceAll(" ", "");
        WataLog.i("getSubwayLine");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        WataLog.d("retrofitExService=" + retrofitExService);
        retrofitExService.subwayLine(areaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터
                    lineNameList.clear();
                    lineNumList.clear();
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        WataLog.d(jsonObject.getString("line_nm"));
                        lineNameList.add("  " + jsonObject.getString("line_nm"));
                        lineNumList.add(jsonObject.getString("line_no"));
                    }

                    setLinNumList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }

            @Override
            public void onFailure( Call<ResponseBody> call, Throwable t) {
                WataLog.d("onFailure");
            }
        });
    }


    private ArrayList<String> lineNameList = new ArrayList<String>();
    private ArrayList<String> lineNumList = new ArrayList<String>();
    // 노선설정
    private void setLinNumList() {
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text_item, lineNameList);

        line_num_spinner.setAdapter(areaAdapter);

        line_num_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WataLog.d("position=" + position);
                mLineNum = lineNumList.get(position);
                mLineName = lineNameList.get(position);
                WataLog.d("mLineNum=" + mLineNum);
                getSubwayInfo(S_Id, mLineNum);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }


    private ArrayList<SubwayInfo> mSubwayInfo = new ArrayList<SubwayInfo>();
    //노선정보
    private void getSubwayInfo(String areaId, String lineNumber) {
        areaId = areaId.replaceAll(" ", "");
        lineNumber = lineNumber.replaceAll(" ", "");

        WataLog.d("areaId=" + areaId);
        WataLog.d("lineNumber=" + lineNumber);
        WataLog.i("getSubwayInfo");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        WataLog.d("retrofitExService=" + retrofitExService);
        retrofitExService.subwayStation(areaId, lineNumber).enqueue(new Callback<ResponseBody>() {
            //        retrofitExService.mapGpsSetting("139.7001198", "35.6895075").enqueue(new Callback<List<SubwayAreaData>>() {
            @Override
            public void onResponse( Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터

                    JSONArray jsonArray = new JSONArray(result);
                    mSubwayInfo.clear();
                    subwayNameArray.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        "area_no":0,"area_nm":"수도권","line_nm":"1","station_no":"1","kor_sub_nm":"가능역","xpos":14142510.184761,"ypos":4543944.64277253},
                        String area_no = jsonObject.getString("area_no");
                        String area_nm = jsonObject.getString("area_nm");
                        String line_nm = jsonObject.getString("line_nm");
                        String station_no = jsonObject.getString("station_no");
                        String kor_sub_nm = jsonObject.getString("kor_sub_nm");
                        String xpos = jsonObject.getString("xpos");
                        String ypos = jsonObject.getString("ypos");

                        mSubwayInfo.add(new SubwayInfo(area_no, area_nm, line_nm,station_no, kor_sub_nm, xpos, ypos));
                        subwayNameArray.add("  " + kor_sub_nm);
                    }
                    setSubwayNameList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("onFailure");
            }
        });
    }

    private ArrayList<String> subwayNameArray = new ArrayList<String>();
    // 지하철설정
    private void setSubwayNameList() {
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text_item, subwayNameArray);
        name_spinner.setAdapter(areaAdapter);
        name_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WataLog.d("position=" + position);
                String stationNo = mSubwayInfo.get(position).getStationNo();

                mSubWayName = mSubwayInfo.get(position).getkorSubNm();
                WataLog.d("mSubWayName=" + mSubWayName);
                mStationNo = mSubwayInfo.get(position).getStationNo();
                mCenterX = mSubwayInfo.get(position).getCenterX();
                mCenterY = mSubwayInfo.get(position).getCenterY();

                //kcy1000
                if(Constance.BEFORE) {
                    getSubwayFloors(S_Id, mLineNum, stationNo);
                } else {
                    getLidarSubwayGuideFloors(S_Id, mLineNum, stationNo);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }


    //    http://13.209.43.191:3000/subwayGuideFloors?&areano=0&lineno=2호선&stationnm=강남역

    private ArrayList<SubwayFloorInfo> mSubwayFloors = new ArrayList<SubwayFloorInfo>();

    private void getSubwayFloors(String areaId, String lineNumber, String stationNo) {
        areaId = areaId.replaceAll(" ", "");
        lineNumber = lineNumber.replaceAll(" ", "");
        stationNo = stationNo.replaceAll(" ", "");

        WataLog.d("areaId=" + areaId);
        WataLog.d("lineNumber=" + lineNumber);
        WataLog.i("getSubwayInfo");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        WataLog.d("retrofitExService=" + retrofitExService);

        retrofitExService.subwayGuideFloors(areaId, lineNumber, stationNo).enqueue(new Callback<ResponseBody>() {
            //        retrofitExService.mapGpsSetting("139.7001198", "35.6895075").enqueue(new Callback<List<SubwayAreaData>>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터

                    JSONArray jsonArray = new JSONArray(result);
                    floorsLever.clear();
                    mSubwayFloors.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String gid = jsonObject.getString("gid");
                        String file_nm = jsonObject.getString("file_nm");
                        String floor = jsonObject.getString("floor");
                        String cx = jsonObject.getString("cx");
                        String cy = jsonObject.getString("cy");
                        String rotate = jsonObject.getString("rotate");
                        String scale = jsonObject.getString("scale");
                        String scalex = jsonObject.getString("scalex");
                        String scaley = jsonObject.getString("scaley");

                        String sx = jsonObject.getString("sx");
                        String sy = jsonObject.getString("sy");

                        floorsLever.add("  " + floor);
                        mSubwayFloors.add(new SubwayFloorInfo(gid, file_nm, floor, cx, cy, rotate, scale, scalex, scaley, sx, sy));

                    }
                    setFloorsList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("onFailure");
            }
        });
    }

    private void getLidarSubwayGuideFloors(String areaId, String lineNumber, String stationNo) {
        areaId = areaId.replaceAll(" ", "");
        lineNumber = lineNumber.replaceAll(" ", "");
        stationNo = stationNo.replaceAll(" ", "");

        WataLog.d("areaId=" + areaId);
        WataLog.d("lineNumber=" + lineNumber);
        WataLog.i("getSubwayInfo");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        WataLog.d("retrofitExService=" + retrofitExService);

        WataLog.d("areaId=" + areaId);
        WataLog.d("lineNumber=" + lineNumber);
        WataLog.d("stationNo=" + stationNo);

        retrofitExService.lidarSubwayGuideFloors(areaId, lineNumber, stationNo).enqueue(new Callback<ResponseBody>() {
            //        retrofitExService.mapGpsSetting("139.7001198", "35.6895075").enqueue(new Callback<List<SubwayAreaData>>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터

                    JSONArray jsonArray = new JSONArray(result);
                    floorsLever.clear();
                    mSubwayFloors.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String gid = jsonObject.getString("gid");
                        String floor = jsonObject.getString("floor");

                        floorsLever.add("  " + floor);
                        mSubwayFloors.add(new SubwayFloorInfo(gid, "", floor, cx, cy, rotate, "", scalex, scaley, "", ""));

                    }
                    setFloorsList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("onFailure");
            }
        });
    }



    private ArrayList<String> floorsLever = new ArrayList<String>();
    private String fileurl = "";
    private String cx = "";
    private String cy = "";
    private String rotate = "";
    private String scalex = "";
    private String scaley = "";
    private String gId = "";
    private String sX = "";
    private String sY = "";

    // 층수설정
    private void setFloorsList() {
        WataLog.i("층수 설정");
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text_item, floorsLever);
        floors_lever_spinner.setAdapter(areaAdapter);
        floors_lever_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WataLog.d("position=" + position);
                mFloorsLever = floorsLever.get(position);
                fileurl = mSubwayFloors.get(position).getFleNm();
                cx = mSubwayFloors.get(position).getCx();
                cy = mSubwayFloors.get(position).getCy();
                rotate = mSubwayFloors.get(position).getRotate();
                scalex = mSubwayFloors.get(position).getScalex();
                scaley = mSubwayFloors.get(position).getScaley();
                gId = mSubwayFloors.get(position).getGid();

                sX = mSubwayFloors.get(position).getSx();
                sY = mSubwayFloors.get(position).getSy();


                WataLog.d("fileurl=" + fileurl);
                WataLog.d("cx=" + cx);
                WataLog.d("cy=" + cy);
                WataLog.d("rotate=" + rotate);
                WataLog.d("scalex=" + scalex);
                WataLog.d("scaley=" + scaley);

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

// ======================================================================================================================================


    private void getOfficeArea() {
        WataLog.i("getOfficeArea");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        WataLog.d("retrofitExService=" + retrofitExService);

        retrofitExService.officeArea().enqueue(new Callback<ResponseBody>() {
            //        retrofitExService.mapGpsSetting("139.7001198", "35.6895075").enqueue(new Callback<List<SubwayAreaData>>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터

                    JSONArray jsonArray = new JSONArray(result);
                    areaList.clear();
                    areaNameList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        WataLog.d(jsonObject.getString("area_nm"));

                        areaList.add(new SubwayData(jsonObject.getString("area_id"), jsonObject.getString("area_nm")));
                        areaNameList.add("  " +  jsonObject.getString("area_nm"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }

//                areaAdapter.setItems(areaList);
//                mGridView.setAdapter(areaAdapter);
                setOfficeAreaList();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("call=" + call.toString());
                WataLog.d("call=" + call.request());
                WataLog.d("call=" + t.getMessage());
                WataLog.d("call=" + t.getLocalizedMessage());


            }
        });
    }

    private void setOfficeAreaList() {
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text_item, areaNameList);
        officeAreaSpinner.setAdapter(areaAdapter);
        officeAreaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WataLog.d("position=" + position);
                O_Id = areaList.get(position).getId();
                WataLog.d("S_Id=" + S_Id);
                getOfficeName(O_Id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private ArrayList<String> companyNm = new ArrayList<String>();
    private ArrayList<String> companyNo = new ArrayList<String>();

    //기업이름
    private void getOfficeName(String areaId) {
        areaId = areaId.replaceAll(" ", "");
        WataLog.d("areaId=" + areaId);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        WataLog.d("retrofitExService=" + retrofitExService);
        retrofitExService.officeInfo(areaId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse( Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터
                    companyNm.clear();
                    companyNo.clear();
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        WataLog.d(jsonObject.getString("company_nm"));
                        companyNm.add("  " + jsonObject.getString("company_nm"));
                        companyNo.add(jsonObject.getString("company_no"));
                    }

                    setOfficeNameList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("onFailure");
            }
        });
    }

//    private ArrayList<SubwayData> areaList = new ArrayList<SubwayData>();
//    private ArrayList<String> areaNameList = new ArrayList<String>();;

    private String mOfficeName , mOfficeNo, mOfficeFloorsLever, mOfficeFloor;
//    private String mCenterX, mCenterY;

    // 기업이름
    private void setOfficeNameList() {
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text_item, companyNm);
        officeName.setAdapter(areaAdapter);
        officeName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WataLog.d("position=" + position);
                mOfficeNo = companyNo.get(position);
                mOfficeName = companyNm.get(position);
                WataLog.d("mOfficeNo=" + mOfficeNo);
                getOfficeFloorInfo(O_Id, mOfficeNo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private ArrayList<OfficeInfo> mOfficeInfo = new ArrayList<OfficeInfo>();
    private ArrayList<String> mOfficeFloors = new ArrayList<String>();
    //기업 층 정보
    private void getOfficeFloorInfo(String areaId, String officeNo) {
        areaId = areaId.replaceAll(" ", "");
        officeNo = officeNo.replaceAll(" ", "");

        WataLog.d("areaId=" + areaId);
        WataLog.d("officeNo=" + officeNo);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constance.DEVE_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitExService retrofitExService = retrofit.create(RetrofitExService.class);
        WataLog.d("retrofitExService=" + retrofitExService);
        retrofitExService.officeFloor(areaId, officeNo).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse( Call<ResponseBody> call, Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터

                    JSONArray jsonArray = new JSONArray(result);
                    mOfficeInfo.clear();
                    mOfficeFloors.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//    [{"area_no":1,"area_nm":"부산","company_nm":"BCC","company_no":"0","floor_info":"1","cx":14374183,"cy":4187390,"sx":"14372683","sy":"14372683"},
                        String gid = jsonObject.getString("gid");
                        String area_no = jsonObject.getString("area_no");
                        String area_nm = jsonObject.getString("area_nm");
                        String company_nm = jsonObject.getString("company_nm");
                        String company_no = jsonObject.getString("company_no");
                        String floor_info = jsonObject.getString("floor_info");
                        String centerX = jsonObject.getString("xpos");
                        String centerY = jsonObject.getString("ypos");
                        String sx = jsonObject.getString("sx");
                        String sy = jsonObject.getString("sy");

                        mOfficeInfo.add(new OfficeInfo(gid, area_no, area_nm, company_nm,company_no, floor_info, centerX, centerY, sx, sy));
                        mOfficeFloors.add("  " + floor_info);
                    }
                    setOfficeFloor();
                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                WataLog.d("onFailure");
            }
        });
    }


    private String  mOGid, mAreaNm, mAreaNo, mCompanyNm, mCopanyNo, mFloorInfo, mOCenterX, mOCenterY, mOSx, mOSy;
    // 기업정보
    private void setOfficeFloor() {
        ArrayAdapter<String> areaAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text_item, mOfficeFloors);
        officeFloors.setAdapter(areaAdapter);
        officeFloors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                WataLog.d("position=" + position);

                mOGid = mOfficeInfo.get(position).getGid();
                mAreaNo = mOfficeInfo.get(position).getAreaNo();
                mAreaNm = mOfficeInfo.get(position).getAreaNm();
                mCompanyNm = mOfficeInfo.get(position).getCompanyNm();
                mCopanyNo = mOfficeInfo.get(position).getCompanyNo();
                mFloorInfo = mOfficeInfo.get(position).getFloorInfo();
                mCenterX = mOfficeInfo.get(position).getCenterX();
                mCenterY = mOfficeInfo.get(position).getCenterY();
                mOSx = mOfficeInfo.get(position).getSx();
                mOSy = mOfficeInfo.get(position).getSy();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

}