package com.geotwo.LAB_TEST.Gathering;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.common.AndroidUtils;
import com.wata.LAB_TEST.Gathering.R;

public class SettingActivity extends Activity
{
	
	TextView 	ipTextView 		= null;
	TextView	portTextView	= null;
	
	
	ImageButton inputButton 	= null;
	onClickConfirmButton confirmButtonListener = null;
	
	
	DataCore 	dataCore = null;
	Intent 		intent	 = null;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        
        initializing();
        
    }
	
	
    private void initializing()
    {
    	if(dataCore == null)
    	{
    		dataCore = DataCore.getInstance();
    	}
    	
    	if(confirmButtonListener == null)
    	{
    		confirmButtonListener = new onClickConfirmButton();
    	}
    		
    	ipTextView = (TextView)findViewById(R.id.setting_ip_text_view);
    	portTextView = (TextView)findViewById(R.id.setting_port_text_view);
    	
    	inputButton = (ImageButton)findViewById(R.id.setting_input_image_button);
    	
    	
    	String URL = dataCore.getURL();
    	ipTextView.setText(URL);
    	
    	portTextView.setText(dataCore.getPORT() + "");
    	
    	inputButton.setOnClickListener(confirmButtonListener);
    }
    
    
    
	class onClickConfirmButton implements View.OnClickListener
	{
		@Override
		public void onClick(View v) 
		{
			CharSequence charURL = ipTextView.getText();
			CharSequence charPort = portTextView.getText();
			DataCore dataCore = DataCore.getInstance();
			
			int port = -2000;
			
			if(dataCore == null)
				dataCore = DataCore.getInstance();
			
			if(charURL.length() < 1)
			{
				Toast.makeText(SettingActivity.this, "URL을 입력해 주세요.", Toast.LENGTH_SHORT);
				return;
			}
			
			String URL =  charURL.toString();
	    	try 
	    	{
	    		if(URL.length() > 0)
	    		{
//	    			dataCore.setURL(URL);
	    			AndroidUtils.saveStringToFile(SettingActivity.this, "server.data", URL);
	    		}
			} 
	    	catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			
			if(charPort.length() < 1)
			{
				Toast.makeText(SettingActivity.this, "포트를 입력해 주세요.", Toast.LENGTH_SHORT);
				return;
			}
			else
			{
				try 
				{
					int tempPort = Integer.parseInt(charPort.toString());
					WataLog.d("tempport"+tempPort);
					
					port = tempPort;
				} 
				catch (Exception e) 
				{}
				
				if(port == -2000)
				{
					Toast.makeText(SettingActivity.this, "입력된 포트를 확인해 주세요.", Toast.LENGTH_SHORT);
    				return;
				}
			}
			
	    	try 
	    	{
	    		dataCore.setPORT(port);
	    		AndroidUtils.saveStringToFile(SettingActivity.this, "port.data", Integer.toString(port));
			} 
	    	catch (Exception e) 
			{
				e.printStackTrace();
			}
	    	
	    	finish();
			
		}
	}
    

}
