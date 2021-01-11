package com.geotwo.LAB_TEST.Gathering;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.wata.LAB_TEST.Gathering.R;

public class CustomAlertDialog extends Dialog implements View.OnClickListener
{
	private Context mContext;
	private String ipAddress ,sInfo;
	private Handler uiHandler;
	private EditText edit_ip, edit_port;
	private int mode , type = 0; // ip 변경 모드인지, socket 변경모드인지 구분하는 구분자 
	 // type :  dialog 타입 구분자  0(default) : ip 변경 모드 , 1 : 경고 메세지 모드
	
	private TextView txt_info, txt_title;
	private View.OnClickListener okEvent;
	
	public CustomAlertDialog(Context context, String ipAddress, Handler handler, int mode) 
	{
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.ipAddress = ipAddress;
		this.uiHandler = handler;
		this.mode = mode;
	}
	
	public CustomAlertDialog(Context context, int type, String sInfo, View.OnClickListener okEvent)
	{
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.mContext = context;
		this.type = type;
		this.sInfo = sInfo;
		this.okEvent = okEvent; 
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.7f;
		getWindow().setAttributes(lpWindow);
		
		setContentView(R.layout.alert_dialog);		
		
		setLayout(type);
	}
	
	private void setLayout(int type)
	{
		txt_title = (TextView)findViewById(R.id.txt_dialogTitle);
		
		if(type <1)
		{
			txt_title.setText("Change IP Address");
			edit_ip = (EditText)findViewById(R.id.edit_ip);
			edit_ip.setVisibility(View.VISIBLE);
			edit_port = (EditText)findViewById(R.id.edit_port);
			if(txt_info != null)
				txt_info.setVisibility(View.GONE);
//			edit_port = (EditText)findViewById(R.id.edit_port);
			
//			if(ipAddress!=null && !ipAddress.equals(""))
//			{
//				String[] address = ipAddress.split(":");
//				edit_ip.setText(address[0]);
//				edit_port.setText(address[1]);
//			}
			SharedPreferences pref = mContext.getSharedPreferences("IP_PREF", 0);
			WataLog.d("mode=" + mode);
			if(mode == 0){
				//DB IP
				edit_port.setVisibility(View.VISIBLE);
				edit_ip.setText(pref.getString("CONNECTING_IP", DataCore.IP_ADDRESS));
				edit_port.setText(pref.getString("CONNECTING_PORT", "8002"));
			}else{
				edit_port.setVisibility(View.VISIBLE);
				edit_ip.setText(pref.getString("SOCKET_IP", DataCore.IP_ADDRESS));
				edit_port.setText(pref.getString("SOCKET_PORT", "2919"));
			}

			findViewById(R.id.alert_ok).setOnClickListener(CustomAlertDialog.this);
			findViewById(R.id.alert_cancel).setOnClickListener(CustomAlertDialog.this);
		}
		else
		{
			txt_title.setText("Alert Info");
			
			if(edit_ip !=null) 
				edit_ip.setVisibility(View.GONE);
			if(edit_port !=null)
				edit_port.setVisibility(View.GONE);
			txt_info = (TextView)findViewById(R.id.txt_info);
			txt_info.setVisibility(View.VISIBLE);
			txt_info.setText(sInfo);
			if(okEvent != null)
				findViewById(R.id.alert_ok).setOnClickListener(okEvent);
			findViewById(R.id.alert_cancel).setOnClickListener(CustomAlertDialog.this);
		}

	}

	@Override
	public void onClick(View v)
	{
		
		if(v.getId() == R.id.alert_ok)
		{			
			if(edit_ip.getText().toString() != null)
			{
				saveAddress();
				uiHandler.sendEmptyMessage(mode);
				cancel();
			}else{
				Toast.makeText(mContext, "변경할 ip주소를 입력하세요", Toast.LENGTH_LONG).show();
			}
		}
		else if(v.getId() == R.id.alert_cancel)
		{
			cancel();
		}
	}
	
	private void saveAddress()
	{
		String strIP = edit_ip.getText().toString().trim();
		String strPort = edit_port.getText().toString().trim();
		SharedPreferences pref = mContext.getSharedPreferences("IP_PREF", 0);
		SharedPreferences.Editor editor = pref.edit();

		if(mode==0)
		{
			editor.putString("CONNECTING_IP", strIP); //+":"+strPort);
			editor.putString("CONNECTING_PORT", strPort); //+":"+strPort);
		}
		else if(mode==1)
		{
			editor.putString("SOCKET_IP", strIP); //+":"+strPort);
			editor.putString("SOCKET_PORT", strPort);
		}
		editor.commit();
	}
}

