package com.geotwo.LAB_TEST.Gathering.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.geotwo.LAB_TEST.Gathering.DataCore;
import com.geotwo.common.AndroidUtils;
import com.wata.LAB_TEST.Gathering.R;

public class GatheringDialog
{	
    public static AlertDialog makeGatherNameInsertDailog(Context context, String buildName , String floor, final Handler UIHandler, final int mode)
    {
		final LinearLayout nameinsert = (LinearLayout) View.inflate(context, R.layout.gathering_name_dialog, null);
	
		Builder registerDialogBuilder = new Builder(context);
	
		registerDialogBuilder.setView(nameinsert);
		registerDialogBuilder.setMessage("수집할 위치를 입력해 주세요.");
		registerDialogBuilder.setCancelable(false);
		EditText nameText = (EditText) nameinsert.findViewById(R.id.name);
	
		final String curTime = AndroidUtils.getCurrentTime();
//		final String autoName = curTime + "-" + buildName + "-" + floor;
		final String autoName = buildName + "-" + floor + "-" + curTime;
		nameText.setText(autoName);
	
		registerDialogBuilder.setCancelable(true);
	
		registerDialogBuilder.setPositiveButton("확인", new OnClickListener()
		{
		    public void onClick(DialogInterface dialog, int which)
		    {
		    	DataCore dataCore = DataCore.getInstance();
		    	
				EditText nameText = (EditText) nameinsert.findViewById(R.id.name);
		
				String insertText = nameText.getText().toString();
		
				if (insertText.equals(""))
				    insertText = autoName;
		
				nameText.setText(insertText);
				dataCore.setCurGatheringName(insertText);
				dataCore.setCurGatherStartTime(curTime);
//				UIHandler.sendEmptyMessage(DataCore.ON_CLICK_OK_ON_START);
//				switch(mode)
//				{
				if(mode == 9999)
					UIHandler.sendEmptyMessage(9999);
//					break;
				else if(mode == R.id.map_complete)
					UIHandler.sendEmptyMessage(R.id.map_complete);
//					break;
//				}
		    }
		});
		
		registerDialogBuilder.setNegativeButton("취소", new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				UIHandler.sendEmptyMessage(DataCore.ON_CLICK_CANCLE_ON_START);
			}
		});
		
		AlertDialog createdDialog = registerDialogBuilder.create();
		createdDialog.setCancelable(false);
		
		return createdDialog;
    }
    
    
	
}
