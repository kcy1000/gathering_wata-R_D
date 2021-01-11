package com.geotwo.LAB_TEST.Gathering.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.DataCore;
import com.wata.LAB_TEST.Gathering.R;

public class OptionDialog 
{
	
	public static AlertDialog makeOptionBtnDialog(final Context context, final Handler UIHandler)
	{
		Builder dialogBuilder = new Builder(context);
		
		final String[] settingItems = context.getResources().getStringArray(R.array.option_btn_items);
		
		dialogBuilder.setItems(settingItems, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				DataCore dataCore = DataCore.getInstance();
				switch (which) 
				{
					case DataCore.GATHER_OPTION_ROUTE:
						dataCore.setGatheringOption(DataCore.GATHER_OPTION_ROUTE);
						UIHandler.sendEmptyMessage(DataCore.GATHER_MODE_CHANGED);
						Toast.makeText(context, "경로 수집", Toast.LENGTH_SHORT).show();
						break;
						
					case DataCore.GATHER_OPTION_POINT:
						dataCore.setGatheringOption(DataCore.GATHER_OPTION_POINT);
						UIHandler.sendEmptyMessage(DataCore.GATHER_MODE_CHANGED);
						Toast.makeText(context, "지점 수집", Toast.LENGTH_SHORT).show();
						break;
	
					case DataCore.GATHER_OPTION_AUTO:
						dataCore.setGatheringOption(DataCore.GATHER_OPTION_AUTO);
						UIHandler.sendEmptyMessage(DataCore.GATHER_MODE_CHANGED);
						Toast.makeText(context, "자동 수집", Toast.LENGTH_SHORT).show();
						break;
				}
			}
		});
		
		return dialogBuilder.create();
		
		
	}

	
}
