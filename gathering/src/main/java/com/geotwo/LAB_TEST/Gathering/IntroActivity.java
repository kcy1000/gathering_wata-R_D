package com.geotwo.LAB_TEST.Gathering;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.dialog.GpsDialog;
import com.geotwo.LAB_TEST.Gathering.dto.IntroInfo;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.GPS.GPSActivity;
import com.geotwo.LAB_TEST.Voucher.SubwayAreaActivity;
import com.geotwo.LAB_TEST.Voucher.VoucherActivity;
import com.wata.LAB_TEST.Gathering.R;

import java.io.File;
import java.util.ArrayList;

import pdr_collecting.core.pdrvariable;

public class IntroActivity extends AppCompatActivity {

    private ArrayList<IntroInfo> mIntroInfo = new ArrayList<IntroInfo>();
    private IntroAdapter introAdapter;
    private GridView mGridView;
    private String mPoiName = "";
    private RelativeLayout introLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_activity);
        checkPermission();

        introLayout = (RelativeLayout) findViewById(R.id.intro_layout);

        introAdapter = new IntroAdapter(getApplicationContext());
        mGridView = (GridView) findViewById(R.id.file_list_gridview);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WataLog.d("position=" + position);
                if (position == 0) {
                    Intent intent = new Intent(IntroActivity.this, PathDrawingAddressActivity.class);
                    startActivity(intent);
                } else if(position == 1) {
                    Intent intent = new Intent(IntroActivity.this, GatherListActivity.class);
                    startActivity(intent);
                } else if(position == 2) {
                    Intent intent = new Intent(IntroActivity.this, SubwayAreaActivity.class);
//                    Intent intent = new Intent(IntroActivity.this, VoucherActivity.class);
                    startActivity(intent);
//                } else if(position == 3) {
//                    Intent intent = new Intent(IntroActivity.this, GPSActivity.class);
//                    startActivity(intent);
                } else {
//                    StaticManager.setTitle("PathDrawing");
//                    StaticManager.setFolderName("PathDrawing");
//                    StaticManager.setAddress("PathDrawing");
//                    StaticManager.setBasePointX("202351.273872");
//                    StaticManager.setBasePointX("544170.137029");
//                    StaticManager.setGid("1168010100108210001S01300");
//                    StaticManager.setPosition(0);
//                    StaticManager.setFloorName("AB01");
//                    Intent intent = new Intent(IntroActivity.this, PathDrawingActivity.class);
                    Intent intent = new Intent(IntroActivity.this, PathDrawingActivity.class);
//                    WataLog.d("mPoiName=" + mPoiName + "_" + mFloorsLever + "층");
                    intent.putExtra("load_file", true);
                    intent.putExtra("file_name", mIntroInfo.get(position).getAddress());
                    startActivity(intent);
                }
            }
        });

//        postHttps("https://api.watalbs.com/dev/", 100000,  100000);
    }


    @Override
    protected void onResume() {
        super.onResume();

        mIntroInfo.clear();
        mIntroInfo.add(new IntroInfo("", "", ""));
        mIntroInfo.add(new IntroInfo("", "", ""));
        mIntroInfo.add(new IntroInfo("", "", ""));

        fileList = getGatheringPathData();

//        mIntroInfo.add(new IntroInfo("1", "성남시", "1층"));
//        mIntroInfo.add(new IntroInfo("1", "성남시", "1층"));
//        mIntroInfo.add(new IntroInfo("1", "성남시", "1층"));
        introAdapter.setItems(mIntroInfo);
        mGridView.setAdapter(introAdapter);

        setStepFirst();
    }

    private void setStepFirst() {
        SharedPreferences pref = getSharedPreferences("stepPref", 0);
        double stepLen = pref.getInt("stepLength", 65) / 100.0;
        pdrvariable.setStep_length(stepLen);
    }

    /**
     * Permission check.
     */
    private final int MY_PERMISSION = 1;

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                        shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // Explain to the user why we need to write the permission.
                    Toast.makeText(this, "Read/Write external storage, Access location", Toast.LENGTH_SHORT).show();
                }

                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE,
                        Manifest.permission.READ_PHONE_STATE,
//						Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.CHANGE_NETWORK_STATE}, MY_PERMISSION);

                // MY_PERMISSION_REQUEST_STORAGE is an
                // app-defined int constant

            } else {
                // 다음 부분은 항상 허용일 경우에 해당이 됩니다.
//                writeFile();
                onPermissionGranted();
            }
        }
        if (GpsDialog.makeGpsCheckDialog(this) != null) {
            GpsDialog.makeGpsCheckDialog(this).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION:
                if (grantResults.length > 0) {
                    int grant = PackageManager.PERMISSION_GRANTED;
                    for (int tempGrant : grantResults) {
                        if (tempGrant == PackageManager.PERMISSION_DENIED) {
                            grant = tempGrant;
                            break;
                        }
                    }
                    if (grant == PackageManager.PERMISSION_GRANTED) {
                        onPermissionGranted();
                    } else {
                        Toast.makeText(this, "앱 설정에서 권한 설정을 해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private void onPermissionGranted() {
        setDirectories();
    }

    private void setDirectories() {
        String areaPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/area/";
        File areaDir = new File(areaPath);
        if (!areaDir.exists()) {
            areaDir.mkdirs();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                AlertDialog.Builder alBuilder = new AlertDialog.Builder(this);
                alBuilder.setMessage(getString(R.string.finish_app_message));
                alBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                alBuilder.show();
                break;
        }
        return true;
    }

    private ArrayList<File> fileList = null;

    private ArrayList<File> getGatheringPathData() {
        ArrayList<File> items = new ArrayList<File>();
        String dataPath = Environment.getExternalStorageDirectory().getPath() + "/gathering/saveData/subway/";
        File pathFolder = new File(dataPath);
        WataLog.d("dataPath = " + dataPath);
        if (pathFolder.exists()) {
            String[] list = pathFolder.list();
            if (list != null) {
                WataLog.d("list = " + list.length);

//                for (int a = 0 ; a <= list.length - 1; a++) {
//
//                    File item = new File(dataPath + "/" + list[a]);
//                    items.add(item);
//                    WataLog.d("list[a]=" + list[a]);
//
//                    mIntroInfo.add(new IntroInfo(String.valueOf(a + 1), list[a], ""));
//                }
                for (int a = list.length - 1; a >= 0; a--) {

                    File item = new File(dataPath + "/" + list[a]);
                    items.add(item);
                    WataLog.d("list[a]=" + list[a]);

                    mIntroInfo.add(new IntroInfo(String.valueOf(a + 1), list[a], ""));
                }
            }
        }

        return items;
    }


    class IntroAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<IntroInfo> items = new ArrayList<IntroInfo>();

        public IntroAdapter(Context context) {
            this.context = context;
        }

        public void setItems(ArrayList<IntroInfo> items) {
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
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(context, R.layout.intro_row, null);

                WataLog.i("check!");
                holder.file_layout = (RelativeLayout) convertView.findViewById(R.id.file_layout);
                holder.box_info_text = (TextView) convertView.findViewById(R.id.box_info_text);
                holder.new_layout = (RelativeLayout) convertView.findViewById(R.id.new_layout);
                holder.file_num = (TextView) convertView.findViewById(R.id.file_num);
                holder.address_name = (TextView) convertView.findViewById(R.id.address_name);
                holder.floors_lever = (TextView) convertView.findViewById(R.id.floors_lever);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (position == 0) {
                holder.new_layout.setVisibility(View.VISIBLE);
                holder.file_layout.setVisibility(View.GONE);
                holder.box_info_text.setText(getString(R.string.new_box));
            } else if(position == 1) {
                holder.new_layout.setVisibility(View.VISIBLE);
                holder.file_layout.setVisibility(View.GONE);
                holder.box_info_text.setText(getString(R.string.send_file));
            } else if(position == 2) {
                holder.new_layout.setVisibility(View.VISIBLE);
                holder.file_layout.setVisibility(View.GONE);
                holder.box_info_text.setText(getString(R.string.subway_title));
//                holder.box_info_text.setText("GPS 데이터수집");
//            } else if(position == 3) {
//                holder.new_layout.setVisibility(View.VISIBLE);
//                holder.file_layout.setVisibility(View.GONE);
//                holder.box_info_text.setText("GPS 데이터수집");
            } else {
                holder.new_layout.setVisibility(View.GONE);
                holder.file_layout.setVisibility(View.VISIBLE);

                holder.file_num.setText(items.get(position).FileNum);
                holder.address_name.setText(items.get(position).Address);

            }

            return convertView;
        }

        private class ViewHolder {
            public TextView file_num, address_name, floors_lever, box_info_text;
            public RelativeLayout file_layout, new_layout;
        }
    }



}
