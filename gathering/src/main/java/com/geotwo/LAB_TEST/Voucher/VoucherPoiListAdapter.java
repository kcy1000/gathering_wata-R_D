package com.geotwo.LAB_TEST.Voucher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geotwo.LAB_TEST.Gathering.dto.PoiInfo;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.wata.LAB_TEST.Gathering.R;

import java.util.ArrayList;


public class VoucherPoiListAdapter extends BaseAdapter {
    private Context context = null;
    private ArrayList<PoiInfo> items = null;
    private boolean editMode = false;

    public VoucherPoiListAdapter(Context context) {
        this.context = context;
        items = new ArrayList();
    }

    public void setItems(ArrayList<PoiInfo> poiItems) {
        this.items = poiItems;
    }

    @Override
    public int getCount() {
        if (items == null) return 0;
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
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.voucher_poi_list, null);

            holder.poi_num = (TextView) convertView.findViewById(R.id.poi_num);
            holder.poi_name = (TextView) convertView.findViewById(R.id.poi_name);
            holder.poi_edit = (RelativeLayout) convertView.findViewById(R.id.poi_edit_layout);
            holder.poi_point_delete = (RelativeLayout) convertView.findViewById(R.id.poi_point_delete);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String poiNum = String.valueOf(items.get(position).Number);
        holder.poi_num.setText(poiNum);

        String PoiText = String.valueOf(items.get(position).PoiText);
        holder.poi_name.setText(PoiText);


        holder.poi_edit.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WataLog.i("내용 수정");
                        if (mListener != null) {
                            mListener.onPoiNameEdit(items.get(position), position);
                        }
                    }
                }
        );


        holder.poi_point_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WataLog.i("삭제하기");
                if (mListener != null) {
                    mListener.onPoiDelete(items.get(position), position);
                }
            }
        });
        return convertView;
    }

    private class ViewHolder {
        public TextView poi_num;
        public TextView poi_name;
        public RelativeLayout poi_edit, poi_point_delete;

    }

    public interface OnItemClickListner {
        void onPoiDelete(PoiInfo items, int position);

        void onPoiNameEdit(PoiInfo items, int position);
    }

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListner mListener = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListner listener) {
        this.mListener = listener;
    }

}