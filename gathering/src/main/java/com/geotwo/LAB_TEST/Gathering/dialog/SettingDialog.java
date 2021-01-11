package com.geotwo.LAB_TEST.Gathering.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

import com.geotwo.LAB_TEST.Gathering.DataCore;
import com.geotwo.LAB_TEST.Gathering.GatherListActivity;
import com.wata.LAB_TEST.Gathering.R;

import pdr_collecting.core.pdrvariable;

public class SettingDialog 
{
	final static private int SETTING_SET_SENSITIVE 	= 0;
	final static private int SETTING_SET_PERIOD 	= 1;
	final static private int SETTING_SET_FEET 		= 2;

	
	final static private int SETTING_BTN_OPEN_SETTING 	= 0;
	final static private int SETTING_BTN_SAVE 			= 1;
	final static private int SETTING_BTN_EXIT 			= 2;
	
	
	final static private int SENSITIVE_8_3 = 0;
	final static private int SENSITIVE_8_5 = 1;
	final static private int SENSITIVE_8_7 = 2;
	final static private int SENSITIVE_8_9 = 3;
	final static private int SENSITIVE_9_1 = 4;
	final static private int SENSITIVE_9_3 = 5;
	final static private int SENSITIVE_9_4 = 6;
	
	final static private int FEET_30 = 0;
	final static private int FEET_35 = 1;
	final static private int FEET_40 = 2;
	final static private int FEET_45 = 3;
	final static private int FEET_50 = 4;
	final static private int FEET_55 = 5;
	final static private int FEET_60 = 6;
	final static private int FEET_65 = 7;
	final static private int FEET_70 = 8;
	
	final static private int PERIOD_FAST = 0;
	final static private int PERIOD_2 = 1;
	final static private int PERIOD_3 = 2;
	final static private int PERIOD_4 = 3;
	final static private int PERIOD_5 = 4;
	
	
	
	
	static public int checkedSensitiveItem = SENSITIVE_9_3;
	static public int checkedPeriodItem = 0;
	static public int checkedFeetItem = 4;
	
	
	
	static public AlertDialog makeSettingBtnDialog(final Context context, final Activity activity)
	{
		Builder dialogBuilder = new Builder(context);
		
		final String[] settingItems = context.getResources().getStringArray(R.array.setting_btn_items);
		
		dialogBuilder.setItems(settingItems, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				switch (which) 
				{
					case SETTING_BTN_OPEN_SETTING:
						SettingDialog.makeSettingDialog(context).show();
						break;
	
					case SETTING_BTN_SAVE:
						
						Intent intent = new Intent(context, GatherListActivity.class);
						context.startActivity(intent);
						
						break;
						
					case SETTING_BTN_EXIT:
						activity.finish();
						break;
				}
				
				
			}
		});
		
		
		
		return dialogBuilder.create();
	}
	
	
	
	
	
	static public AlertDialog makeSettingDialog(final Context context)
	{
		Builder dialogBuilder = new Builder(context);
		
		
		final String[] settingItems = context.getResources().getStringArray(R.array.setting_items);
		
		dialogBuilder.setItems(settingItems, new OnClickListener()
		{	
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				switch (which) 
				{
					case SETTING_SET_PERIOD:
						makeGatheringPeriodSettingDialog(context).show();
						break;
					case SETTING_SET_FEET:
						makeGatheringFeetSettingDialog(context).show();
						break;
					case SETTING_SET_SENSITIVE:
						makeSensitiveSettingDilog(context).show();
						break;
				}
				
				
			}
		});
		
		
		
		return dialogBuilder.create();
	}
	
	
	
	static public AlertDialog makeSensitiveSettingDilog(Context context)
	{
		
		Builder dialogBuilder = new Builder(context);
		
		String title = context.getResources().getString(R.string.sensitive_title);
		
		dialogBuilder.setTitle(title);
		dialogBuilder.setSingleChoiceItems(R.array.sensitive, checkedSensitiveItem, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				checkedSensitiveItem = which;
			}
		});
		dialogBuilder.setPositiveButton("확인", new OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				
				switch (checkedSensitiveItem) 
				{
					case SENSITIVE_8_3:
						pdrvariable.setThreshold_stepdetection(8.3);
						break;
						
					case SENSITIVE_8_5:
						pdrvariable.setThreshold_stepdetection(8.5);
						break;
						
					case SENSITIVE_8_7:
						pdrvariable.setThreshold_stepdetection(8.7);
						break;
						
					case SENSITIVE_8_9:
						pdrvariable.setThreshold_stepdetection(8.9);
						break;
						
					case SENSITIVE_9_1:
						pdrvariable.setThreshold_stepdetection(9.1);
						break;
						
					case SENSITIVE_9_3:
						pdrvariable.setThreshold_stepdetection(9.3);
						break;
						
					case SENSITIVE_9_4:
						pdrvariable.setThreshold_stepdetection(9.4);
						break;
				}
			}
		});
		
		dialogBuilder.setNegativeButton("취소", null);
		AlertDialog createdDialog = dialogBuilder.create();
		
		return dialogBuilder.create();
	}
	
	
	
	static public AlertDialog makeGatheringFeetSettingDialog(Context context)
	{
		
		Builder dialogBuilder = new Builder(context);
		
		
		String title = context.getResources().getString(R.string.feet_title);
		
		
		dialogBuilder.setTitle(title);
		dialogBuilder.setSingleChoiceItems(R.array.feet, checkedFeetItem, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				checkedFeetItem = which;
			}
		});
		
		dialogBuilder.setPositiveButton("확인", new OnClickListener() 
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				
				switch (checkedFeetItem) 
				{
				case FEET_30:
					pdrvariable.setStep_length(3);
					break;
				case FEET_35:
					pdrvariable.setStep_length(3.5);
					break;
				case FEET_40:
					pdrvariable.setStep_length(4);
					break;
				case FEET_45:
					pdrvariable.setStep_length(4.5);
					break;
				case FEET_50:
					pdrvariable.setStep_length(5);
					break;
				case FEET_55:
					pdrvariable.setStep_length(5.5);
					break;
				case FEET_60:
					pdrvariable.setStep_length(6);
					break;
				case FEET_65:
					pdrvariable.setStep_length(6.5);
					break;
				case FEET_70:
					pdrvariable.setStep_length(7);
					break;
				}
			}
		});
		
		dialogBuilder.setNegativeButton("취소", null);
		
		
		return dialogBuilder.create();
	}
	
	
	
	static public AlertDialog makeGatheringPeriodSettingDialog(Context context)
	{
		
		Builder dialogBuilder = new Builder(context);
		
		
		String title = context.getResources().getString(R.string.delay_time_title);
		
		
		dialogBuilder.setTitle(title);
		dialogBuilder.setSingleChoiceItems(R.array.gathering_period, checkedPeriodItem, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				checkedPeriodItem = which;
			}
		});
		
		dialogBuilder.setPositiveButton("확인", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				DataCore dataCore = null;
				dataCore = DataCore.getInstance();
				
				switch (checkedPeriodItem) {
				case PERIOD_FAST:
					dataCore.setGatheringPeriod(0);
					break;
					
				case PERIOD_2:
					dataCore.setGatheringPeriod(2);
					break;
					
				case PERIOD_3:
					dataCore.setGatheringPeriod(3);
					break;
					
				case PERIOD_4:
					dataCore.setGatheringPeriod(4);
					break;
					
				case PERIOD_5:
					dataCore.setGatheringPeriod(5);
					break;

				}
			}
		});
		
		dialogBuilder.setNegativeButton("취소", null);
		
		return dialogBuilder.create();
	}
	
	
}
