package com.geotwo.LAB_TEST.Gathering.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.geotwo.LAB_TEST.Gathering.dto.Ble;
import com.geotwo.common.ViewUtils;
import com.wata.LAB_TEST.Gathering.R;

import java.util.ArrayList;

/**
 * Created by hyuck on 2017. 1. 9..
 */

public class BleSettingListViewAdapter extends BaseAdapter {
    private Context context = null;
    private ArrayList<Ble> items = null;
    private boolean editMode = false;

    public BleSettingListViewAdapter(Context context) {
        this.context = context;
        items = new ArrayList();
    }

    public void setItems(ArrayList<Ble> items) {
        this.items = items;
    }

    @Override
    public int getCount() {
        if(items == null) return 0;
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isEditMode() {
        return editMode;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;

        if(convertView == null){
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.item_ble_setting, null);

            holder.itemBleSettingMac = (TextView) convertView.findViewById(R.id.itemBleSettingMac);
            holder.itemBleSettingUuid = (TextView) convertView.findViewById(R.id.itemBleSettingUuid);
            holder.itemBleSettingMajor = (TextView) convertView.findViewById(R.id.itemBleSettingMajor);
            holder.itemBleSettingMinor = (TextView) convertView.findViewById(R.id.itemBleSettingMinor);
            holder.itemBleSettingCheckBox = (CheckBox) convertView.findViewById(R.id.itemBleSettingCheckBox);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Ble item = items.get(i);

        holder.itemBleSettingMac.setText(item.mac);
        holder.itemBleSettingUuid.setText(item.uuid);
        holder.itemBleSettingMajor.setText(item.major);
        holder.itemBleSettingMinor.setText(item.minor);

        if(editMode) {
            ViewUtils.setVisibility(holder.itemBleSettingCheckBox, View.VISIBLE);
            holder.itemBleSettingCheckBox.setChecked(item.isIgnored);
        } else {
            ViewUtils.setVisibility(holder.itemBleSettingCheckBox, View.GONE);
        }
        holder.itemBleSettingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                item.tag = b;
            }
        });

        return convertView;
    }

    private class ViewHolder {
        public TextView itemBleSettingMac;
        public TextView itemBleSettingUuid;
        public TextView itemBleSettingMajor;
        public TextView itemBleSettingMinor;
        public CheckBox itemBleSettingCheckBox;
    }
}