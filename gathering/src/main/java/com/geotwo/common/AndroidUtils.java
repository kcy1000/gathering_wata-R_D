package com.geotwo.common;

/**
 * 
 * @author Jaehyun Wang
 * @version 1.0
 * 
 * 
 */

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AndroidUtils
{
	/**
	 * 
	 * 
	 * @param appContext
	 *            어플리케이션 Context
	 * @return 해당 기기의 전화번호 (010xxxxxxxx)
	 */
	static public String getPhoneNum(Context appContext)
	{
		TelephonyManager telManager = (TelephonyManager) appContext.getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNum = telManager.getLine1Number();

		if (phoneNum == null)
		{
			phoneNum = "";
		}
		else
		{
			phoneNum = phoneNum.substring(phoneNum.length() - 8, phoneNum.length());
			phoneNum = "010" + phoneNum;
		}

		return phoneNum;

	}

	/**
	 * 
	 * @param size
	 *            원하는 dip 사이즈
	 * @param metrics
	 *            현재 device의 display 정보
	 * @return
	 */
	public static int getDPI(int size, DisplayMetrics metrics)
	{
		return (size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
	}

	/**
	 * 
	 * @param context
	 *            해당 앱의 Context
	 * @param stringResource
	 *            Dialog내에 보여줄 StringResource
	 * @param drawableResource
	 *            StringResource와 함께 보여줄 Drawable
	 * @param onClickListener
	 *            DialogInterface.OnClickListener
	 * 
	 * @return Builder 완성된 Dialog Builder
	 */
	static public Builder SingleChoiceDialog(final Context context, int stringResource, int drawableResource,
			DialogInterface.OnClickListener onClickListener)
	{

		String[] stringItem = context.getResources().getStringArray(stringResource);
		String[] drawableItem = context.getResources().getStringArray(drawableResource);

		final Items[] items = new Items[stringItem.length];

		for (int i = 0; i < items.length; i++)
		{
			items[i] = new Items(stringItem[i], context.getResources().getIdentifier(drawableItem[i], "drawable",
					context.getPackageName()));
		}

		Builder builder = new Builder(context);
		builder.setTitle("Menu");

		ListAdapter dialogListAdapter = new ArrayAdapter<Items>(context, android.R.layout.select_dialog_item, android.R.id.text1, items)
		{
			public View getView(int position, View convertView, ViewGroup parent)
			{
				// User super class to create the View
				View v = super.getView(position, convertView, parent);
				TextView tv = (TextView) v.findViewById(android.R.id.text1);

				// Put the image on the TextView
				tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

				// Add margin between image and text (support various screen
				// densities)
				int dp5 = (int) (5 * context.getResources().getDisplayMetrics().density + 0.5f);
				tv.setCompoundDrawablePadding(dp5);

				return v;
			}

		};

		builder.setAdapter(dialogListAdapter, onClickListener);

		return builder;

	}

	static public Builder SingleChoiceDialog2(final Context context, int titleResource, int stringResource,
			DialogInterface.OnClickListener onClickListener)
	{

		String stringTitle = context.getResources().getString(titleResource);
		String[] stringItem = context.getResources().getStringArray(stringResource);

		final Items[] items = new Items[stringItem.length];

		for (int i = 0; i < items.length; i++)
			items[i] = new Items(stringItem[i], 0);

		Builder builder = new Builder(context);
		builder.setTitle(stringTitle);

		ListAdapter dialogListAdapter = new ArrayAdapter<Items>(context, android.R.layout.select_dialog_item, android.R.id.text1, items)
		{
			public View getView(int position, View convertView, ViewGroup parent)
			{
				// User super class to create the View
				View v = super.getView(position, convertView, parent);
				TextView tv = (TextView) v.findViewById(android.R.id.text1);

				// Put the image on the TextView
				tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

				// Add margin between image and text (support various screen
				// densities)
				int dp5 = (int) (5 * context.getResources().getDisplayMetrics().density + 0.5f);
				tv.setCompoundDrawablePadding(dp5);

				return v;
			}

		};

		builder.setAdapter(dialogListAdapter, onClickListener);

		return builder;
	}

	static public String getString(final Context context, int resId)
	{
		return context.getResources().getString(resId);
	}

	static public void ConfirmDialog(final Context context, String title, String text, boolean isKorean,
			DialogInterface.OnClickListener confirmClickListener, DialogInterface.OnClickListener cancelClickListener)
	{
		try
		{
			Builder alertBuilder = new Builder(context);

			if (isKorean == true)
			{
				alertBuilder.setNegativeButton("취소", cancelClickListener);
				alertBuilder.setPositiveButton("확인", confirmClickListener);
			}
			else
			{
				alertBuilder.setNegativeButton("Cancel", cancelClickListener);
				alertBuilder.setPositiveButton("Confirm", confirmClickListener);
			}

			alertBuilder.setTitle(title);
			alertBuilder.setMessage(text);
			alertBuilder.create().show();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static public void ConfirmDialog(final Context context, int titleResource, int stringResource, boolean isKorean,
			DialogInterface.OnClickListener confirmClickListener, DialogInterface.OnClickListener cancelClickListener)
	{
		String title = context.getResources().getString(titleResource);
		String text = context.getResources().getString(stringResource);

		ConfirmDialog(context, title, text, isKorean, confirmClickListener, cancelClickListener);
	}

	private static class Items
	{
		public final String text;
		public final int icon;

		public Items(String text, Integer icon)
		{
			this.text = text;
			this.icon = icon;
		}

		@Override
		public String toString()
		{
			return text;
		}
	}

	static public void Toast(final Context context, int textRes, int type)
	{
		String text = context.getResources().getString(textRes);
		Toast.makeText(context, text, type).show();
	}

	static public void ExecuteIntent2(Activity activity, Uri uri)
	{
		try
		{
			Intent myIntent = new Intent(Intent.ACTION_VIEW);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			myIntent.setData(uri);
			activity.startActivity(myIntent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static public void ExecuteIntent(Activity activity, Uri uri)
	{
		try
		{
			Intent myIntent = new Intent(Intent.ACTION_VIEW);
			myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(uri.toString());
			String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			myIntent.setDataAndType(uri, mimetype);
			activity.startActivity(myIntent);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static public void DownloadFromUrl(String aURL, String fileName)
	{
		// this is the downloader method
		try
		{
			URL url = new URL(aURL); // you can write here any link
			File file = new File(fileName);

			long startTime = System.currentTimeMillis();
			Log.d("DownloadFromUrl", "download begining");
			Log.d("DownloadFromUrl", "download url:" + url);
			Log.d("DownloadFromUrl", "downloaded file name:" + fileName);

			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1)
			{
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.close();
			Log.d("DownloadFromUrl", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");

		}
		catch (IOException e)
		{
			Log.d("ImageManager", "Error: " + e);
		}
	}

	public static String getCurrentTime()
	{
		Calendar calendar = Calendar.getInstance();
		String strTime = String
				.format("%d-%02d-%02d-%02d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
						calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
		return strTime;
	}

	public static String getYYYYMMDDhhmmss()
	{
		String result = "";

		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
		Date currentTime = new Date();
		result = formatter.format(currentTime);

		System.out.println(" dTime : " + result);

		return result;
	}

	public static void unzip(String src, String dest)
	{
		final int BUFFER_SIZE = 4096;

		BufferedOutputStream bufferedOutputStream = null;
		FileInputStream fileInputStream;
		try
		{
			fileInputStream = new FileInputStream(src);
			ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
			ZipEntry zipEntry;

			while ((zipEntry = zipInputStream.getNextEntry()) != null)
			{
				String zipEntryName = zipEntry.getName();
				File file = new File(dest + zipEntryName);
				System.out.println("zip : " + dest + zipEntryName);
				System.out.println("zip path : " + file.getParent());

				// if (file.exists())
				// {
				// }
				// else
				{
					if (zipEntry.isDirectory())
					{
						try
						{
							file.mkdirs();
						}
						catch (Exception ei)
						{
							ei.printStackTrace();
						}
					}
					else
					{
						try
						{
							File f2 = new File(file.getParent());
							f2.mkdirs();
						}
						catch (Exception ei)
						{
							ei.printStackTrace();
						}

						byte buffer[] = new byte[BUFFER_SIZE];
						FileOutputStream fileOutputStream = new FileOutputStream(file);
						bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
						int count;

						while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1)
						{
							bufferedOutputStream.write(buffer, 0, count);
						}

						bufferedOutputStream.flush();
						bufferedOutputStream.close();
					}
				}
			}
			zipInputStream.close();
			File s = new File(src);
			s.delete();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void copyFile(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[4096];
		int read;
		while ((read = in.read(buffer)) != -1)
			out.write(buffer, 0, read);
	}

	public static void fileCopy(String src, String dest, boolean isDeleteSrc)
	{
		BufferedInputStream bufferedInputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		try
		{
			bufferedInputStream = new BufferedInputStream(new FileInputStream(src));
			bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(dest));

			copyFile(bufferedInputStream, bufferedOutputStream);

			bufferedOutputStream.flush();

			bufferedInputStream.close();
			bufferedOutputStream.close();

			if (isDeleteSrc == true)
			{
				File s = new File(src);
				s.delete();
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void saveStringToFile(Context context, String fileName, String content)
	{
		try
		{
			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);
			
			fos.write(content.getBytes());
			
			fos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
