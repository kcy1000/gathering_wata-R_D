package com.geotwo.LAB_TEST.Gathering.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class BuildInfoFileDialog
{
	static public AlertDialog iniFileIsNotExist(Context context)
	{
		Builder dialogBuilder = new Builder(context);
		
		dialogBuilder.setTitle("Alert");
		dialogBuilder.setMessage("건물 정보 파일이 존재하지 않습니다. 어플리케이션을 종료합니다.");
		dialogBuilder.setCancelable(false);
		
		dialogBuilder.setPositiveButton("확인", new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				System.exit(0);
			}
		});
		
		return dialogBuilder.create();
	}
	
	static public AlertDialog mapFileIsNotExist(final Activity activity,  Context context , String fileName)
	{
		Builder dialogBuilder = new Builder(activity);
		
		dialogBuilder.setTitle("Alert");
		dialogBuilder.setMessage("건물 지도 파일( " + fileName + ")이 존재하지 않습니다.");
		dialogBuilder.setCancelable(false);
		
		dialogBuilder.setPositiveButton("확인", new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				activity.finish();
			}
		});
		
		return dialogBuilder.create();
	}
	
	

}
