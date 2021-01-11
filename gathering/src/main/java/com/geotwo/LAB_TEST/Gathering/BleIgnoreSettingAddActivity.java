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

public class BleIgnoreSettingAddActivity extends Activity implements View.OnClickListener{
    public static final String SET_BLE_IGNORE_DATA = "SET_BLE_IGNORE_DATA";

    private EditText bleSettingAddInputMac = null,
            bleSettingAddInputUuid = null,
            bleSettingAddInputMajor = null,
            bleSettingAddInputMinor = null;
    private Button bleSettingAddBtnCancel = null,
            bleSettingAddBtnApply = null;
    private TextView title = null;
    private View btnBack = null;

    private Ble inBle = null;

    private final String MESSAGE_TITLE = "BLE 무시설정 추가";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_setting_add);

        /**
         * test
         */
//        BleIgnoreSettingActivity.setBleIgnoreList(this, new ArrayList<Ble>(){{
//            Ble tempBle = new Ble();
//            tempBle.mac = "";
//            tempBle.uuid = "b9407f30-f5f8-466e-aff9-25556b57fe6d";
//            tempBle.major = "";
//            tempBle.minor = "";
//            add(tempBle);
//        }});

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
            Ble bleIgnore = (Ble) intent.getSerializableExtra(SET_BLE_IGNORE_DATA);
            if(bleIgnore != null) {
                bleSettingAddInputMac.setText(bleIgnore.mac);
                bleSettingAddInputUuid.setText(bleIgnore.uuid);
                bleSettingAddInputMajor.setText(bleIgnore.major);
                bleSettingAddInputMinor.setText(bleIgnore.minor);
                inBle = bleIgnore;
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

                ArrayList<Ble> bleIgnoreList = BleIgnoreSettingActivity.getBleIgnoreList(this);
                int existBleIgnoreIndexInList = getExistBleIgnoreIndexInList(newBle, bleIgnoreList);
                if(existBleIgnoreIndexInList >= 0){
                    Toast.makeText(this, "같은 무시정보가 존재합니다.", Toast.LENGTH_SHORT).show();
                    // continue activity
                } else {
                    if(inBle != null) { // update
                        int previousIndex = getExistBleIgnoreIndexInList(inBle, bleIgnoreList);
                        if(previousIndex >= 0){ // isExist
                            bleIgnoreList.add(previousIndex, newBle);
                            bleIgnoreList.remove(inBle);
                        } else {
                            bleIgnoreList.add(newBle);
                        }
                    } else { // insert
                        bleIgnoreList.add(newBle);
                    }
                    BleIgnoreSettingActivity.setBleIgnoreList(this, bleIgnoreList);
                    finish();
                    // done finish
                }
                break;
        }
    }

    private int getExistBleIgnoreIndexInList(Ble in, ArrayList<Ble> bleIgnoreList){
        int index = -1;
        if(bleIgnoreList == null) return index;

        int bleIgnoreListSize = bleIgnoreList.size();
        for(int i=0; i<bleIgnoreListSize; i++){
            Ble temporary = bleIgnoreList.get(i);
            if(temporary.equals(in)){
                index = i;
                break;
            }
        }

        return index;
    }
}
