package com.geotwo.LAB_TEST.Gathering.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

public class GpsDialog
{
	
	public static AlertDialog makeGpsCheckDialog(final Context context)
	{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		if(Build.VERSION.SDK_INT >= 23) {
			final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

			if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				//Start searching for location and update the location text when update available

				alertDialog.setTitle("GPS 사용유무셋팅");
				alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다.\n 설정창으로 가시겠습니까?");

				// OK 를 누르게 되면 설정창으로 이동합니다.
				alertDialog.setPositiveButton("Settings",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

								context.startActivity(intent);
							}
						});
				// Cancle 하면 종료 합니다.
				alertDialog.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
								System.exit(0);
							}
						});

				alertDialog.setCancelable(false);
				return alertDialog.create();
			}
		}
		return null;
	}
}
