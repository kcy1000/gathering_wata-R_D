package com.geotwo.LAB_TEST.Gathering;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.wata.LAB_TEST.Gathering.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ResultList_Location_Act extends Activity 
{
	private String locationName;
	private ListView result_loc_listview;
	private String[] floorList;
	private ResultLocAdapter adapter;
	private final String savePath = Environment.getExternalStorageDirectory().getPath()
									+"/gathering/saveData/pathGathering/";
//	private final String savePath = Environment.getExternalStorageDirectory().getPath()
//									+"/gathering/saveData/subway/";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gather_result_layout_location);
		
		Intent fromList = getIntent();
		locationName = fromList.getStringExtra("locationName");
		
		setLayout();
	}
	
	private void setLayout()
	{
		TextView result_loc_title = (TextView)findViewById(R.id.result_loc_title);
		result_loc_title.setText(locationName);

		RelativeLayout result_loc_back = (RelativeLayout)findViewById(R.id.result_loc_back);
		result_loc_back.setOnClickListener(btnEvent);
		
//		Button result_loc_send_all = (Button)findViewById(R.id.result_loc_send_all);
//		result_loc_send_all.setOnClickListener(btnEvent);
		
		inputData();
		result_loc_listview = (ListView)findViewById(R.id.result_loc_listview);
		if(floorList!=null)
		{
			adapter = new ResultLocAdapter(ResultList_Location_Act.this, R.layout.gather_result_list_row, floorList);
			result_loc_listview.setAdapter(adapter);
		}
	}

	private void inputData()
	{
		File locationFile = new File(savePath+locationName+"/");
		WataLog.d( "locationFile path = "+locationFile.getPath());
		WataLog.d( "locationFile.exists() = "+locationFile.exists());
		if(locationFile.exists()) {
			floorList = locationFile.list();
			Collections.reverse(Arrays.asList(floorList));
			WataLog.d("floorList size = "+floorList.length);
		}
	}
	
	private OnClickListener btnEvent = new OnClickListener()
	{
		@Override
		public void onClick(View v) 
		{
//			switch(v.getId())
//			{
			if(v.getId() == R.id.result_loc_back)
				finish();
//				break;
				
//			case R.id.result_loc_send_all:
//				break;
//			}
		}
	};
	
	class ResultLocAdapter extends ArrayAdapter<String> implements OnClickListener
	{
		String[] items;
		Context context = null;
		
		public ResultLocAdapter(Context context, int textViewResourceId, String[] items) 
		{
			super(context, textViewResourceId, items);
			
			this.items = items;
			this.context = context;
		}
		
		@Override
		public View getView(int position, View v, ViewGroup parent)
		{
			if(v==null)
			{
				LayoutInflater li = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.gather_result_list_row, null);
			}
			
			String strName = items[position];
			WataLog.d("strName=" + strName);
			if(strName!=null)
			{
				TextView gather_row_name_text = (TextView)v.findViewById(R.id.file_name);
				gather_row_name_text.setText(strName);

				TextView file_num = (TextView)v.findViewById(R.id.file_num);
				String num = String.valueOf(position + 1);
				file_num.setText(num);
			}
			
			v.setTag(position);
			v.setOnClickListener(this);
			
			return v;
		}

		@Override
		public void onClick(View v)
		{
			int pos = Integer.valueOf(v.getTag().toString());
			String floorName = items[pos];
			
			Intent goPath = new Intent(ResultList_Location_Act.this, ResultList_Path_Act.class);
			goPath.putExtra("locationName", locationName);
			goPath.putExtra("floorName", floorName);

			WataLog.d("locationName=" + locationName);
			WataLog.d("floorName=" + floorName);
			startActivity(goPath);
		}
	}
}
