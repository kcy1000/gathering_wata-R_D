package com.geotwo.LAB_TEST.Gathering;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.dto.Ble;
import com.geotwo.LAB_TEST.Gathering.ui.BleSettingListViewAdapter;
import com.geotwo.common.ViewUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wata.LAB_TEST.Gathering.R;

import java.util.ArrayList;

/**
 * Created by geo2 on 2016-12-20.
 */

public class BleIgnoreSettingActivity extends Activity implements View.OnClickListener{
    private ListView bleSettingListView = null;
    private BleSettingListViewAdapter bleSettingListViewAdapter = null;
    private View bleSettingLoadingContainer = null;
    private ProgressBar bleSettingLoading = null;
    private View bleSettingNothing = null;
    private Button bleSettingAdd = null;
    private TextView title = null;
    private TextView bleSettingEmptyText = null;
    private TextView bleSettingHintText = null;
    private View btnBack = null;

    private final String ACTION_MODIFIY = "수정";
    private final String ACTION_DELETE = "제거";

    private final String MESSAGE_TITLE = "BLE 무시설정";
    private final String MESSAGE_HINT = "Hint) 무시 설정에 추가되면 BLE 스캐닝시 해당 값을 일치하는 항목을 무시하고 건너뜁니다.";
    private final String MESSAGE_DISABLED = "추가 설정에 하나 이상의 항목이 있어 비활성화 되었습니다.";

    private boolean disabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_setting);

        ArrayList<Ble> addBleList = BleAddSettingActivity.getBleAddList(this);
        disabled = (addBleList != null && addBleList.size() > 0) ? true : false;
        
        initUi();

        title.setText(MESSAGE_TITLE);
        ViewUtils.setVisibility(bleSettingHintText, View.VISIBLE);
        bleSettingHintText.setText(MESSAGE_HINT);
        if(disabled){
            bleSettingEmptyText.setText(MESSAGE_DISABLED);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateUi();
    }

    private void updateUi() {
        if(disabled){
            hideLoading();
            updateNothing(null);
        } else {
            ArrayList<Ble> bleIgnoreList = getBleIgnoreList(this);
            hideLoading();
            updateNothing(bleIgnoreList);
            bleSettingListViewAdapter.setItems(bleIgnoreList);
            bleSettingListViewAdapter.notifyDataSetChanged();
        }
    }

    private void initUi() {
        bleSettingListView = (ListView) findViewById(R.id.bleSettingListView);
        bleSettingLoadingContainer = findViewById(R.id.bleSettingLoadingContainer);
        bleSettingLoading = (ProgressBar) findViewById(R.id.bleSettingLoading);
        bleSettingNothing = findViewById(R.id.bleSettingNothing);
        bleSettingAdd = (Button) findViewById(R.id.bleSettingAdd);
        title = (TextView) findViewById(R.id.title);
        bleSettingHintText = (TextView) findViewById(R.id.bleSettingHintText);
        bleSettingEmptyText = (TextView) findViewById(R.id.bleSettingEmptyText);
        btnBack = findViewById(R.id.btnBack);
        bleSettingAdd.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        bleSettingListViewAdapter = new BleSettingListViewAdapter(this);
        bleSettingListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Ble ble = (Ble) bleSettingListViewAdapter.getItem(i);

                AlertDialog.Builder dialog = new AlertDialog.Builder(BleIgnoreSettingActivity.this, R.style.dialogBlackTextColor);
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(BleIgnoreSettingActivity.this, android.R.layout.simple_list_item_1){{
                    add(ACTION_MODIFIY);
                    add(ACTION_DELETE);
                }};
                dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String action = adapter.getItem(i);
                        if (action.equals(ACTION_MODIFIY)) {
                            Intent edit = new Intent(BleIgnoreSettingActivity.this, BleIgnoreSettingAddActivity.class);
                            edit.putExtra(BleIgnoreSettingAddActivity.SET_BLE_IGNORE_DATA, ble);
                            startActivity(edit);

                        } else if (action.equals(ACTION_DELETE)) {
                            ArrayList<Ble> bleIgnoreList = getBleIgnoreList(BleIgnoreSettingActivity.this);
                            bleIgnoreList.remove(ble);
                            setBleIgnoreList(BleIgnoreSettingActivity.this, bleIgnoreList);
                            updateUi();

                        }
                    }
                });
                dialog.show();
                return false;
            }
        });
        bleSettingListView.setAdapter(bleSettingListViewAdapter);
    }

    private void setEditMode(boolean isEditmode){
        bleSettingListViewAdapter.setEditMode(isEditmode);
        bleSettingListViewAdapter.notifyDataSetChanged();
    }

    private void showLoading(){
        ViewUtils.setVisibility(bleSettingLoading, View.VISIBLE);
        ViewUtils.setVisibility(bleSettingLoadingContainer, View.VISIBLE);
    }

    private void hideLoading(){
        ViewUtils.setVisibility(bleSettingLoading, View.GONE);
        ViewUtils.setVisibility(bleSettingLoadingContainer, View.GONE);
    }

    private void updateNothing(ArrayList<Ble> bleIgnoreList){
        if(bleIgnoreList != null && bleIgnoreList.size() > 0){
            ViewUtils.setVisibility(bleSettingNothing, View.GONE);
        } else {
            ViewUtils.setVisibility(bleSettingNothing, View.VISIBLE);
        }
    }

    public static ArrayList<Ble> getBleIgnoreList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("BLE_IGNORE_SETTING_KEY", Context.MODE_PRIVATE);
        ArrayList<Ble> out = null;

        String json = sharedPreferences.getString("BLE_IGNORE_SETTING_LIST", "");
        if(json != null && !json.equalsIgnoreCase("null") && json.length() > 0){
            out = new Gson().fromJson(json, new TypeToken<ArrayList<Ble>>(){}.getType());
        } else {
            out = new ArrayList();
        }

        return out;
    }

    public static void setBleIgnoreList(Context context, ArrayList<Ble> bleIgnoreList) {
        if(bleIgnoreList != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("BLE_IGNORE_SETTING_KEY", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("BLE_IGNORE_SETTING_LIST", new Gson().toJson(bleIgnoreList).toString());
            editor.commit();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.bleSettingAdd:
                if(disabled){
                    Toast.makeText(this, MESSAGE_DISABLED, Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent(this, BleIgnoreSettingAddActivity.class));
                }
                break;
            case R.id.btnBack:
                finish();
                break;
        }
    }
}
