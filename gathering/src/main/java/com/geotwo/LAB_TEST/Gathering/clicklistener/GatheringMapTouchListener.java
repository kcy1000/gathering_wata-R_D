package com.geotwo.LAB_TEST.Gathering.clicklistener;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.geotwo.LAB_TEST.Gathering.DataCore;

public class GatheringMapTouchListener implements View.OnTouchListener
{
	private float prevX = 0;
	private float prevY = 0;

	private Handler _UIHandler = null;

	public boolean isOnePointTouch = false;

	public GatheringMapTouchListener(Handler UIHandler)
	{
		_UIHandler = UIHandler;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		int action = event.getAction();
//		Log.e("action", "action = "+action);
		float x = event.getX();
		float y = event.getY();
		switch (action)
		{
		case MotionEvent.ACTION_DOWN:
			if(!isOnePointTouch)
			{
				prevX = x;
				prevY = y;
			}
			
			isOnePointTouch = true;
			
			break;

		case MotionEvent.ACTION_MOVE:
			if ((prevX < x + 5 && prevX > x - 5) && (prevY < y + 5 && prevY > y - 5))
			{
				isOnePointTouch = true;
			}
			else
			{
				_UIHandler.sendEmptyMessage(DataCore.ON_TOUCH_MOVE_MAP);
				isOnePointTouch = false;
			}
			
			break;

		case MotionEvent.ACTION_POINTER_DOWN:
			isOnePointTouch = false;
			break;
			
		case MotionEvent.ACTION_POINTER_UP:
			isOnePointTouch = false;
			break;

		case MotionEvent.ACTION_UP:

			if (isOnePointTouch == true)
			{
				_UIHandler.sendEmptyMessage(DataCore.ON_TOUCH_MAP);
				isOnePointTouch = false;
			}

			break;

		}
//	    Log.e("action", "isOnePointTouch = "+isOnePointTouch);
		return false;
	}


	public float getPrevX()
	{
		return prevX;
	}
	public void setPrevX(float prevX)
	{
		this.prevX = prevX;
	}

	public float getPrevY()
	{
		return prevY;
	}
	public void setPrevY(float prevY)
	{
		this.prevY = prevY;
	}
}
