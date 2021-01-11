package com.geotwo.LAB_TEST.Gathering.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import com.geotwo.LAB_TEST.Gathering.GatheringActivity;
import com.wata.LAB_TEST.Gathering.R;

public class GatheringStartDialog extends Dialog implements OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {

	private View.OnClickListener clickEvent;
	private EditText ScanCount;
	private Context mContext;
	private ToggleButton UseStatistics;
	private RadioGroup RdLogType;
	private RadioButton logType;
	private Button BtnStart;

	public GatheringStartDialog(Context context, View.OnClickListener clickEvent) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.clickEvent = clickEvent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.7f;
		getWindow().setAttributes(lpWindow);

		setContentView(R.layout.gather_start_dialog);

		setLayout();
	}

	private void setLayout() {
		// TODO Auto-generated method stub
		RdLogType = (RadioGroup) findViewById(R.id.RgLogType);
		ScanCount = (EditText)findViewById(R.id.Edit_Scancount);
		UseStatistics = (ToggleButton)findViewById(R.id.Btn_Use);
		UseStatistics.setOnCheckedChangeListener(this);
		UseStatistics.setChecked(GatheringActivity.bUseStatistics);
		RdLogType.setOnCheckedChangeListener(this);

		BtnStart = (Button)findViewById(R.id.BtnStart);
		if(clickEvent != null)
			BtnStart.setOnClickListener(clickEvent);
	}

	public int getScanCount()
	{
		int sCnt = -1;
		if(ScanCount.getText().toString().length() >0)
			sCnt = Integer.valueOf(ScanCount.getText().toString());

		return sCnt;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if(buttonView.getId() == R.id.Btn_Use) {
			GatheringActivity.bUseStatistics = isChecked;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if(checkedId == R.id.logType_onlySuc){
			GatheringActivity.bLogTypeOnlySuc = true;
		}else if(checkedId == R.id.logType){
			GatheringActivity.bLogTypeOnlySuc = false;
		}
	}
}
