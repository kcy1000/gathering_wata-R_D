package com.geotwo.LAB_TEST.Gathering;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.dto.Ble;
import com.wata.LAB_TEST.Gathering.R;

import java.util.ArrayList;

/**
 * Created by geo2 on 2016-12-20.
 */

public class BleAddSettingAddActivity extends Activity implements View.OnClickListener{
    public static final String SET_BLE_ADD_DATA = "SET_BLE_ADD_DATA";

    private EditText bleSettingAddInputMac = null,
            bleSettingAddInputUuid = null,
            bleSettingAddInputMajor = null,
            bleSettingAddInputMinor = null;
    private Button bleSettingAddBtnCancel = null,
            bleSettingAddBtnApply = null;
    private TextView title = null;
    private View btnBack = null;

    private Ble inBle = null;

    private final String MESSAGE_TITLE = "BLE 검색설정 추가";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_setting_add);

        initUi();
        updateIntentData();

        title.setText(MESSAGE_TITLE);
    }

    private void initUi() {
        bleSettingAddInputMac = (EditText) findViewById(R.id.bleSettingAddInputMac);
        bleSettingAddInputUuid = (EditText) findViewById(R.id.bleSettingAddInputUuid);
        bleSettingAddInputMajor = (EditText) findViewById(R.id.bleSettingAddInputMajor);
        bleSettingAddInputMinor = (EditText) findViewById(R.id.bleSettingAddInputMinor);
        bleSettingAddBtnCancel = (Button) findViewById(R.id.bleSettingAddBtnCancel);
        bleSettingAddBtnApply = (Button) findViewById(R.id.bleSettingAddBtnApply);
        title = (TextView) findViewById(R.id.title);
        btnBack = findViewById(R.id.btnBack);

        bleSettingAddBtnCancel.setOnClickListener(this);
        bleSettingAddBtnApply.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    private void updateIntentData() {
        Intent intent = getIntent();
        if(intent != null){
            Ble ble = (Ble) intent.getSerializableExtra(SET_BLE_ADD_DATA);
            if(ble != null) {
                bleSettingAddInputMac.setText(ble.mac);
                bleSettingAddInputUuid.setText(ble.uuid);
                bleSettingAddInputMajor.setText(ble.major);
                bleSettingAddInputMinor.setText(ble.minor);
                inBle = ble;
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnBack:
                finish();
                break;
            case R.id.bleSettingAddBtnCancel:
                finish();
                break;
            case R.id.bleSettingAddBtnApply:
                String mac = bleSettingAddInputMac.getText().toString();
                String uuid = bleSettingAddInputUuid.getText().toString();
                String major = bleSettingAddInputMajor.getText().toString();
                String minor = bleSettingAddInputMinor.getText().toString();
                if(mac.length() == 0 && uuid.length() == 0 && major.length() == 0 && minor.length() == 0){
                    Toast.makeText(this, "아무것도 입력하지 않았습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Ble newBle = new Ble();
                newBle.mac = mac;
                newBle.uuid = uuid;
                newBle.major = major;
                newBle.minor = minor;

                ArrayList<Ble> bleAddList = BleAddSettingActivity.getBleAddList(this);
                int existBleAddIndexInList = getExistBleAddIndexInList(newBle, bleAddList);
                if(existBleAddIndexInList >= 0){
                    Toast.makeText(this, "같은 검색정보가 존재합니다.", Toast.LENGTH_SHORT).show();
                    // continue activity
                } else {
                    if(inBle != null) { // update
                        int previousIndex = getExistBleAddIndexInList(inBle, bleAddList);
                        if(previousIndex >= 0){ // isExist
                            bleAddList.add(previousIndex, newBle);
                            bleAddList.remove(inBle);
                        } else {
                            bleAddList.add(newBle);
                        }
                    } else { // insert
                        bleAddList.add(newBle);
                    }
                    BleAddSettingActivity.setBleAddList(this, bleAddList);
                    finish();
                    // done finish
                }
                break;
        }
    }

    private int getExistBleAddIndexInList(Ble in, ArrayList<Ble> bleAddList){
        int index = -1;

        int bleAddListSize = bleAddList.size();
        for(int i=0; i<bleAddListSize; i++){
            Ble temporary = bleAddList.get(i);
            if(temporary.equals(in)){
                index = i;
                break;
            }
        }

        return index;
    }
}
