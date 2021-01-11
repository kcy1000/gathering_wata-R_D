package com.geotwo.LAB_TEST.Gathering.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geotwo.LAB_TEST.Gathering.dto.MyWayInfo;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.wata.LAB_TEST.Gathering.R;

import java.util.ArrayList;


public class RecordListAdapter extends BaseAdapter {
    private Context context = null;
    private ArrayList<MyWayInfo> items = null;
    private boolean editMode = false;

    public RecordListAdapter(Context context) {
        this.context = context;
        items = new ArrayList();
    }

    public void setItems(ArrayList<MyWayInfo> items) {
        this.items = items;
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
            convertView = View.inflate(context, R.layout.record_list, null);

            holder.num = (TextView) convertView.findViewById(R.id.num);
            holder.poi_name = (TextView) convertView.findViewById(R.id.poi_name);

            holder.start_record_point_btn = (RelativeLayout) convertView.findViewById(R.id.start_record_point_btn);
            holder.end_record_point_btn = (RelativeLayout) convertView.findViewById(R.id.end_record_point_btn);

//            holder.record_check_btn = (Button) convertView.findViewById(R.id.record_check_btn);
            holder.reverse_record_check_btn = (RelativeLayout) convertView.findViewById(R.id.reverse_record_check_btn);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String recordNum = String.valueOf(items.get(position).Number);
        holder.num.setText(recordNum);

        WataLog.d("역방향기록확인 = " + items.get(position).RecordR);

        if("완료".equals(items.get(position).RecordR)) {
            WataLog.i("check1!");
            holder.reverse_record_check_btn.setBackgroundResource(R.drawable.round_background_00ca9d);
        } else {
            WataLog.i("check2222222!");
            holder.reverse_record_check_btn.setBackgroundResource(R.drawable.round_background_9d9d9d);
        }
        holder.num.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WataLog.i("라인선택 ");
                if (mListener != null) {
                    mListener.onPoiSelectLine(items.get(position), position);
                }
            }
        });

        holder.reverse_record_check_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WataLog.i("역방향 기록하기");
                if (mListener != null) {
                    mListener.onReverseRecord(items.get(position), position);
                }
            }
        });

        holder.start_record_point_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WataLog.i("기록 시작시점으로 이동");
                        if (mListener != null) {
                            mListener.onRecordStartPoint(items.get(position), position);
                        }
                    }
                }
        );

        holder.end_record_point_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WataLog.i("기록 종료시점으로 이동");
                        if (mListener != null) {
                            mListener.onRecordEndPoint(items.get(position), position);
                        }
                    }
                }
        );

        return convertView;
    }

    private class ViewHolder {
        public TextView num, poi_name;
        public RelativeLayout start_record_point_btn, end_record_point_btn, reverse_record_check_btn;
    }

    public interface OnItemClickListner {
        void onRecordStartPoint(MyWayInfo items, int position);
        void onRecordEndPoint(MyWayInfo items, int position);
        void onPoiSelectLine(MyWayInfo items, int position);
        void onReverseRecord(MyWayInfo items, int position);
    }

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListner mListener = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListner listener) {
        this.mListener = listener;
    }

}