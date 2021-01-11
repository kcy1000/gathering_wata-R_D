package com.geotwo.LAB_TEST.Voucher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geotwo.LAB_TEST.Gathering.dto.MyWayInfo;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.Voucher.Retrofit.SubwayLineInfo;
import com.wata.LAB_TEST.Gathering.R;

import java.util.ArrayList;


public class RecordAdapter extends BaseAdapter {
    private Context context = null;
    private ArrayList<SubwayLineInfo> items = null;
    private boolean editMode = false;

    public RecordAdapter(Context context) {
        this.context = context;
        items = new ArrayList();
    }

    public void setItems(ArrayList<SubwayLineInfo> items) {
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
            convertView = View.inflate(context, R.layout.voucher_record_list, null);

            holder.num = (TextView) convertView.findViewById(R.id.num);
            holder.forward_check_text = (TextView) convertView.findViewById(R.id.forward_check_text);
            holder.reverse_check_text = (TextView) convertView.findViewById(R.id.reverse_check_text);

            holder.record_btn = (RelativeLayout) convertView.findViewById(R.id.record_btn);
            holder.forward_check = (RelativeLayout) convertView.findViewById(R.id.forward_check);
            holder.reverse_check = (RelativeLayout) convertView.findViewById(R.id.reverse_check);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.num.setText(String.valueOf(position + 1));


        if(context.getString(R.string.completion).equals(items.get(position).getCompletionF())) {
            holder.forward_check.setBackgroundResource(R.drawable.round_background_00ca9d);
            holder.forward_check_text.setText("정방향 완료");
        } else {
            holder.forward_check.setBackgroundResource(R.drawable.round_background_9d9d9d);
            holder.forward_check_text.setText("정방향 수집하기");
        }

        if(context.getString(R.string.completion).equals(items.get(position).getCompletionR())) {
            holder.reverse_check.setBackgroundResource(R.drawable.round_background_00ca9d);
            holder.reverse_check_text.setText("역방향 완료");
        } else {
            holder.reverse_check.setBackgroundResource(R.drawable.round_background_9d9d9d);
            holder.reverse_check_text.setText("역방향 수집하기");
        }

        holder.forward_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WataLog.i("정방향 기록하기");
                if (mListener != null) {
                    mListener.onRecordStartPoint(items.get(position), position);
                }
            }
        });

        holder.reverse_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WataLog.i("역방향 기록하기");
                if (mListener != null) {
                    mListener.onRecordEndPoint(items.get(position), position);
                }
            }
        });

        return convertView;
    }

    private class ViewHolder {
        public TextView num, forward_check_text, reverse_check_text;
        public RelativeLayout record_btn, forward_check, reverse_check;
    }

    public interface OnItemClickListner {
        void onRecordStartPoint(SubwayLineInfo items, int position);
        void onRecordEndPoint(SubwayLineInfo items, int position);
    }

    // 리스너 객체 참조를 저장하는 변수
    private OnItemClickListner mListener = null;

    // OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메서드
    public void setOnItemClickListener(OnItemClickListner listener) {
        this.mListener = listener;
    }

}