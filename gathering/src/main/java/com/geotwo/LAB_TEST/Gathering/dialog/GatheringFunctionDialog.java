package com.geotwo.LAB_TEST.Gathering.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.wata.LAB_TEST.Gathering.R;


/**
 * Created by hyuck on 2017. 1. 5..
 */

public class GatheringFunctionDialog extends Dialog implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private CheckBox dialogGatheringFunctionSaveLogForImage;
    private ListView dialogGatheringFunctionListView;
    private ArrayAdapter<String> dialogGatheringFunctionListViewAdapter;
    private View dialogGatheringFunctionConfirm;
    private View dialogGatheringFunctionCancel;

    private Context context = null;

    private boolean saveLogForImage = false;
    private String functions[];

    private AdapterView.OnItemClickListener onFunctionClickListener = null;

    public GatheringFunctionDialog(Context context) {
        super(context);
        initialize(context);
    }

    public GatheringFunctionDialog(Context context, int themeResId) {
        super(context, themeResId);
        initialize(context);
    }

    protected GatheringFunctionDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initialize(context);
    }

    private void initialize(Context context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        this.context = context;
        setContentView(R.layout.dialog_gathering_function);

        saveLogForImage = isSaveLogForImage(this.context);

        dialogGatheringFunctionSaveLogForImage = (CheckBox) findViewById(R.id.dialogGatheringFunctionSaveLogForImage);
        dialogGatheringFunctionListView = (ListView) findViewById(R.id.dialogGatheringFunctionListView);
//        dialogGatheringFunctionConfirm = findViewById(R.id.dialogGatheringFunctionConfirm);
//        dialogGatheringFunctionCancel = findViewById(R.id.dialogGatheringFunctionCancel);

        dialogGatheringFunctionSaveLogForImage.setOnCheckedChangeListener(this);
//        dialogGatheringFunctionConfirm.setOnClickListener(this);
//        dialogGatheringFunctionCancel.setOnClickListener(this);

        dialogGatheringFunctionListViewAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1){
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String item = dialogGatheringFunctionListViewAdapter.getItem(position);

                TextView text1 = (TextView) view.findViewById(android.R.id.text1);

                if(item.equals("영상로그정보측위") && !saveLogForImage){
                    text1.setTextColor(Color.GRAY);
                    text1.setAlpha(0.68f);
                    text1.setTextSize(16);
                    view.setClickable(true);
                } else {
                    text1.setTextColor(Color.BLACK);
                    text1.setAlpha(0.68f);
                    text1.setTextSize(16);
                    view.setClickable(false);
                }

                return view;
            }
        };
        dialogGatheringFunctionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(onFunctionClickListener != null){
                    onFunctionClickListener.onItemClick(adapterView, view, i, l);
                    dismiss();
                }
            }
        });
        dialogGatheringFunctionListView.setAdapter(dialogGatheringFunctionListViewAdapter);
        dialogGatheringFunctionListView.setDivider(new ColorDrawable(Color.parseColor("#ececec")));
        dialogGatheringFunctionListView.setDividerHeight(1);

        dialogGatheringFunctionSaveLogForImage.setChecked(saveLogForImage);
    }

    public void setFunctions(String[] functions){
        this.functions = functions;
        if(this.functions != null && this.functions.length > 0)
            updateFunctions();
    }

    private void updateFunctions() {
        dialogGatheringFunctionListViewAdapter.clear();
        dialogGatheringFunctionListViewAdapter.addAll(functions);
        dialogGatheringFunctionListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        setSaveLogForImage(GatheringFunctionDialog.this.context, saveLogForImage);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
//            case R.id.dialogGatheringFunctionConfirm:
//                setSaveLogForImage(context, saveLogForImage);
//                break;
//            case R.id.dialogGatheringFunctionCancel:
//                dismiss();
//                break;
        }
    }

    public void setOnFunctionClickListener(AdapterView.OnItemClickListener onFunctionClickListener) {
        this.onFunctionClickListener = onFunctionClickListener;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        saveLogForImage = b;
        dialogGatheringFunctionListViewAdapter.notifyDataSetChanged();
    }

    private void setSaveLogForImage(Context context, boolean saveLogForImage){
        SharedPreferences sharedPreferences = context.getSharedPreferences("GATHERING_FUNCTION_KEY", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("GATHERING_FUNCTION_SAVE_LOG", saveLogForImage);
        editor.commit();
    }

    public static boolean isSaveLogForImage(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("GATHERING_FUNCTION_KEY", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("GATHERING_FUNCTION_SAVE_LOG", false);
    }
}
