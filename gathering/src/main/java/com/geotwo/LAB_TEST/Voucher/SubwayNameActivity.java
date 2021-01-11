package com.geotwo.LAB_TEST.Voucher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geotwo.LAB_TEST.Gathering.Retrofit.RetrofitExService;
import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.Voucher.Retrofit.SubwayInfo;
import com.wata.LAB_TEST.Gathering.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SubwayNameActivity extends AppCompatActivity implements OnClickListener {

    private AreaAdapter areaAdapter;
    private GridView mGridView;

    private String lineNumber, areaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WataLog.i("onCreate");
        setContentView(R.layout.subway_lin_activity);


        Intent intent = getIntent();
        WataLog.i("intent=" + intent);
        if (intent != null) {
            lineNumber = intent.getExtras().getString("line_nm");
            areaId = intent.getExtras().getString("area_Id");
            WataLog.d("areaId=" + areaId);
            WataLog.d("lineNumber=" + lineNumber);

        }
        areaAdapter = new AreaAdapter(getApplicationContext());
        mSubwayInfo.clear();
        getSubwayInfo();

        mGridView = (GridView) findViewById(R.id.line_list_gridview);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WataLog.d("position=" + position);
                Intent intent = new Intent(SubwayNameActivity.this, VoucherActivity.class);
//                intent.putExtra("sub_name", mSubwayInfo.get(position).getkorSubNm());
//                intent.putExtra("num_line", mSubwayInfo.get(position).getNumline());
//                intent.putExtra("center_x", mSubwayInfo.get(position).getCenterX());
//                intent.putExtra("center_y", mSubwayInfo.get(position).getCenterY());

                startActivity(intent);
            }
        });
    }

    private ArrayList<SubwayInfo> mSubwayInfo = new ArrayList<SubwayInfo>();

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

    }

    class AreaAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<SubwayInfo> items = new ArrayList<SubwayInfo>();

        public AreaAdapter(Context context) {
            this.context = context;
        }

        public void setItems(ArrayList<SubwayInfo> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AreaAdapter.ViewHolder holder = null;
            if (convertView == null) {
                holder = new AreaAdapter.ViewHolder();
                convertView = View.inflate(context, R.layout.line_row, null);

                WataLog.i("check!");
                holder.area_layout = (RelativeLayout) convertView.findViewById(R.id.area_layout);
                holder.box_info_text = (TextView) convertView.findViewById(R.id.box_info_text);
                convertView.setTag(holder);

            } else {
                holder = (AreaAdapter.ViewHolder) convertView.getTag();
            }
            holder.box_info_text.setText(items.get(position).getkorSubNm());
            return convertView;
        }

        private class ViewHolder {
            public TextView box_info_text;
            public RelativeLayout area_layout;
        }
    }

    private void getSubwayInfo() {
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
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                WataLog.d(response.isSuccessful() + "");
                try {
                    String result = response.body().string();
                    WataLog.d(result); //받아온 데이터

                    JSONArray jsonArray = new JSONArray(result);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String area_nm = jsonObject.getString("area_nm");
                        String line_nm = jsonObject.getString("line_nm");
                        String kor_sub_nm = jsonObject.getString("kor_sub_nm");
                        long xpos = jsonObject.getLong("xpos");
                        long ypos = jsonObject.getLong("ypos");
                        int numline = jsonObject.getInt("numline");
//                        mSubwayInfo.add(new SubwayInfo(area_nm, line_nm, kor_sub_nm, xpos, ypos, numline, ""));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    WataLog.e("exception =" + e.toString());
                }
                areaAdapter.setItems(mSubwayInfo);
                mGridView.setAdapter(areaAdapter);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                WataLog.d("onFailure");
            }
        });
    }

}