package com.geotwo.LAB_TEST.Gathering;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.geotwo.LAB_TEST.Gathering.dto.BuildInfo;
import com.wata.LAB_TEST.Gathering.R;

public class MapSelectActivity extends Activity
{
    private final static int RES_OK = 10;
    private final static int RES_BACK = 30;

    Spinner _stationSpinner = null;
    Spinner _floorSpinner = null;

    ImageView _stationImgView = null;
    ImageView _floorImgView = null;

    ArrayAdapter<String> _stationArrAdap = null;
    ArrayAdapter<String> _floorArrAdap = null;

    ImageButton _btnOK = null;
    ImageButton _btnBack = null;

    List<String> _stationList = null;
    List<String> _etriFloorList = null;
    List<String> _youngsanFloorList = null;
    List<String> _coexFloorList = null;

    MapSelectButtonClickListener _onButtonClickListener = null;
    DataCore _dataCore = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.map_select_layout);

	initializing();
    }

    @Override
    public void onResume()
    {
	DataCore.isOnGathering = false;
	super.onResume();
    }

    public void initializing()
    {

	if (_dataCore == null)
	{
	    _dataCore = DataCore.getInstance();
	}

	_dataCore.readBuildList(this);

	_stationList = Collections.list(_dataCore.getBuildNameList().elements());

	// if(stationList == null)
	// {
	// stationList = new ArrayList<String>();
	// stationList.add("지도 선택");
	// stationList.add("용산역");
	// stationList.add("한국전자통신연구원");
	// stationList.add("코엑스");
	// }
	// if(etriFloorList == null)
	// {
	// etriFloorList = new ArrayList<String>();
	// etriFloorList.add("층 선택");
	// etriFloorList.add("1층");
	// etriFloorList.add("2층");
	// etriFloorList.add("3층");
	// etriFloorList.add("4층");
	// }
	// if(youngsanFloorList == null)
	// {
	// youngsanFloorList = new ArrayList<String>();
	// youngsanFloorList.add("층 선택");
	// youngsanFloorList.add("B2층");
	// youngsanFloorList.add("B1층");
	// youngsanFloorList.add("1층");
	// youngsanFloorList.add("2층");
	// youngsanFloorList.add("3층");
	// youngsanFloorList.add("4층");
	// }
	// if(coexFloorList == null)
	// {
	// coexFloorList = new ArrayList<String>();
	// coexFloorList.add("층 선택");
	// coexFloorList.add("B1층");
	// coexFloorList.add("1층");
	// coexFloorList.add("2층");
	// coexFloorList.add("3층");
	// coexFloorList.add("4층");
	// }

	if (_onButtonClickListener == null)
	{
	    _onButtonClickListener = new MapSelectButtonClickListener();
	}
	if (_btnOK == null)
	{
	    _btnOK = (ImageButton) findViewById(R.id.mapselect_ok);
	    _btnOK.setOnClickListener(_onButtonClickListener);
	}
	if (_btnBack == null)
	{
	    _btnBack = (ImageButton) findViewById(R.id.mapselect_back);
	    _btnBack.setOnClickListener(_onButtonClickListener);
	}

	if (_stationSpinner == null)
	{
	    _stationSpinner = (Spinner) findViewById(R.id.mapselect_spinner_station);
	    _stationSpinner.setPrompt("지도를 선택하세요.");

	    _stationArrAdap = new ArrayAdapter<String>(this, R.layout.spinner_text_item, _stationList);

	    _stationSpinner.setAdapter(_stationArrAdap);

	    _stationSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
	    {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
		    initFloorList();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0)
		{
		}
	    });

	}

	if (_stationImgView == null)
	{
	    _stationImgView = (ImageView) findViewById(R.id.mapselect_imageview_station);
	    _stationImgView.setOnClickListener(new OnClickListener()
	    {

		@Override
		public void onClick(View v)
		{
		    _stationSpinner.performClick();
		}
	    });
	}
	if (_floorImgView == null)
	{
	    _floorImgView = (ImageView) findViewById(R.id.mapselect_imageview_floor);
	    _floorImgView.setOnClickListener(new OnClickListener()
	    {

		@Override
		public void onClick(View v)
		{
		    _floorSpinner.performClick();
		}
	    });
	}

    }

    private void initFloorList()
    {
	String stationString = (String) _stationSpinner.getSelectedItem();

	_floorSpinner = (Spinner) findViewById(R.id.mapselect_spinner_floor);
	_floorSpinner.setPrompt("층을 선택하세요.");

	if (_dataCore == null)
	{
	    _dataCore = DataCore.getInstance();
	}

	HashMap<String, BuildInfo> buildInfos = _dataCore.getBuildInfoList();
	BuildInfo selectedBuilding = buildInfos.get(stationString);

	_floorArrAdap = new ArrayAdapter<String>(this, R.layout.spinner_text_item, Collections.list(selectedBuilding.getFloorList().elements()));

	_floorSpinner.setAdapter(_floorArrAdap);
    }

    public class MapSelectButtonClickListener implements OnClickListener
    {

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
//	    switch (v.getId())
//	    {
	    if(id == R.id.mapselect_back)
	    {
			MapSelectActivity.this.setResult(RES_BACK);
			finish();
	    }
//		break;

	    else if(id == R.id.mapselect_ok)
	    {
		String stationString = (String) _stationSpinner.getSelectedItem();
		String floorString = (String) _floorSpinner.getSelectedItem();

		if (stationString.equals("지도 선택") == false && floorString.equals("층 선택") == false)
		{
		    if (_dataCore == null)
		    {
			_dataCore = DataCore.getInstance();
		    }

		    HashMap<String, BuildInfo> buildInfos = _dataCore.getBuildInfoList();

		    Intent intent = new Intent();
		    intent.putExtra("station", stationString);
		    intent.putExtra("floor", floorString);
		    intent.putExtra("fileName", buildInfos.get(stationString).getFileName());
		    MapSelectActivity.this.setResult(RES_OK, intent);
		    finish();

		}
		else
		{
		    if (stationString.equals("지도 선택") == true)
		    {
			Toast.makeText(MapSelectActivity.this, "지도를 선택해 주세요", Toast.LENGTH_SHORT).show();
		    }
		    else if (floorString.equals("층 선택") == true)
		    {
			Toast.makeText(MapSelectActivity.this, "층을 선택해 주세요", Toast.LENGTH_SHORT).show();
		    }
		}

//		break;
	    }

	}

    }

}
