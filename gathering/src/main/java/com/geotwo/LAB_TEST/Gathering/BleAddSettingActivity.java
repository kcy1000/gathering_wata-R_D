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

public class BleAddSettingActivity extends Activity implements View.OnClickListener{
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

    private final String MESSAGE_TITLE = "BLE 검색설정";
    private final String MESSAGE_HINT = "Hint) 검색 설정에 추가되면 'BLE 무시 설정'의 항목들은 무시되며, 해당 값을 일치하는 항목만 검색되도록 설정 합니다.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_setting);

        initUi();

        title.setText(MESSAGE_TITLE);
        ViewUtils.setVisibility(bleSettingHintText, View.VISIBLE);
        bleSettingHintText.setText(MESSAGE_HINT);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateUi();
    }

    private void updateUi() {
        ArrayList<Ble> bleAddList = getBleAddList(this);
        hideLoading();
        updateNothing(bleAddList);
        bleSettingListViewAdapter.setItems(bleAddList);
        bleSettingListViewAdapter.notifyDataSetChanged();
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

                AlertDialog.Builder dialog = new AlertDialog.Builder(BleAddSettingActivity.this, R.style.dialogBlackTextColor);
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(BleAddSettingActivity.this, android.R.layout.simple_list_item_1){{
                    add(ACTION_MODIFIY);
                    add(ACTION_DELETE);
                }};
                dialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String action = adapter.getItem(i);
                        if (action.equals(ACTION_MODIFIY)) {
                            Intent edit = new Intent(BleAddSettingActivity.this, BleAddSettingAddActivity.class);
                            edit.putExtra(BleAddSettingAddActivity.SET_BLE_ADD_DATA, ble);
                            startActivity(edit);

                        } else if (action.equals(ACTION_DELETE)) {
                            ArrayList<Ble> bleAddList = getBleAddList(BleAddSettingActivity.this);
                            bleAddList.remove(ble);
                            setBleAddList(BleAddSettingActivity.this, bleAddList);
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

    private void updateNothing(ArrayList<Ble> bleAddList){
        if(bleAddList != null && bleAddList.size() > 0){
            ViewUtils.setVisibility(bleSettingNothing, View.GONE);
        } else {
            ViewUtils.setVisibility(bleSettingNothing, View.VISIBLE);
        }
    }

    public static ArrayList<Ble> getBleAddList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("BLE_ADD_SETTING_KEY", Context.MODE_PRIVATE);
        ArrayList<Ble> out = null;

        String json = sharedPreferences.getString("BLE_ADD_SETTING_LIST", "");
        if(json != null && !json.equalsIgnoreCase("null") && json.length() > 0){
            out = new Gson().fromJson(json, new TypeToken<ArrayList<Ble>>(){}.getType());
        } else {
            out = new ArrayList();
        }

        return out;
    }

    public static void setBleAddList(Context context, ArrayList<Ble> bleAddList) {
        if(bleAddList != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("BLE_ADD_SETTING_KEY", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("BLE_ADD_SETTING_LIST", new Gson().toJson(bleAddList).toString());
            editor.commit();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.bleSettingAdd:
                startActivity(new Intent(this, BleAddSettingAddActivity.class));
                break;
            case R.id.btnBack:
                finish();
                break;
        }
    }
}
