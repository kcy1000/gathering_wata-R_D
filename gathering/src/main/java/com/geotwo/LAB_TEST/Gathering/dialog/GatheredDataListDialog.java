package com.geotwo.LAB_TEST.Gathering.dialog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;

import com.geotwo.LAB_TEST.Gathering.DataCore;

import java.io.File;
import java.util.ArrayList;

public class GatheredDataListDialog
{
	Handler 			_UIHandler 	= null;
	ArrayList<File>		_fileList 	= null;
	Context 			_context	= null;
	
	
	public GatheredDataListDialog(Context context, Handler UIHandler, ArrayList<File> fileList)
	{
		_context = context;
		_UIHandler = UIHandler;
		_fileList = fileList;
	}
	
	
    public AlertDialog makeUpLoadConfirmDialog()
    {
    	Builder dialogBuilder = new Builder(_context);
    	
    	dialogBuilder.setMessage("수집된 데이터를 전송하시겠습니까?");
    	dialogBuilder.setPositiveButton("전송", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				_UIHandler.sendEmptyMessage(DataCore.ON_CLICK_OK_ON_UPLOAD);
			}
		});
    	
    	dialogBuilder.setNegativeButton("취소", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				_UIHandler.sendEmptyMessage(DataCore.ON_CLICK_CANCLE_ON_UPLOAD);
			}
		});
    	
    	return dialogBuilder.create();
    }

	public AlertDialog makeLocationUpLoadConfirmDialog()
	{
		Builder dialogBuilder = new Builder(_context);

		dialogBuilder.setMessage("측위결과 데이터를 전송하시겠습니까?");
		dialogBuilder.setPositiveButton("전송", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				_UIHandler.sendEmptyMessage(DataCore.ON_CLICK_OK_ON_LOCATION_UPLOAD);
			}
		});

		dialogBuilder.setNegativeButton("취소", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				_UIHandler.sendEmptyMessage(DataCore.ON_CLICK_CANCLE_ON_LOCATION_UPLOAD);
			}
		});

		return dialogBuilder.create();
	}

    public AlertDialog makeDeleteConfirmDialog(String fileName)
    {
    	Builder dialogBuilder = new Builder(_context);
    	
    	dialogBuilder.setTitle("삭제");
    	dialogBuilder.setMessage(fileName + "\n를 삭제하시겠습니까?");
    	dialogBuilder.setPositiveButton("확인", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				_UIHandler.sendEmptyMessage(DataCore.ON_CLICK_OK_ON_DELETE);
			}
		});
    	
    	dialogBuilder.setNegativeButton("취소", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				_UIHandler.sendEmptyMessage(DataCore.ON_CLICK_CANCLE_ON_DELETE);
			}
		});
    	
    	return dialogBuilder.create();
    }
    
}
