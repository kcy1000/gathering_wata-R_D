package com.geotwo.LAB_TEST.Gathering;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.geotwo.LAB_TEST.Gathering.util.Constance;
import com.geotwo.LAB_TEST.Gathering.util.WataLog;
import com.geotwo.LAB_TEST.data.StaticManager;
import com.google.android.gms.maps.model.LatLng;
import com.wata.LAB_TEST.Gathering.R;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class SelectMapActivity extends AppCompatActivity {

    private File tempFile;
    private RelativeLayout selectImgLayout, makeMapBtn;
    private LinearLayout selectMapLayout;
    private String mAddress, mapId;
    private LatLng mMyLatlng;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_map_activity);

        ImageView backKey = (ImageView) findViewById(R.id.arrow_left_img);
        TextView addressTextview = (TextView) findViewById(R.id.address_textview);
        TextView floorsLeverText = (TextView) findViewById(R.id.floors_lever_text);


        RelativeLayout none_map_layout = (RelativeLayout) findViewById(R.id.none_map_layout);
        RelativeLayout yes_map_layout = (RelativeLayout) findViewById(R.id.yes_map_layout);
        selectMapLayout = (LinearLayout) findViewById(R.id.select_map_layout);
        selectMapLayout.setVisibility(View.VISIBLE);
        selectImgLayout = (RelativeLayout) findViewById(R.id.select_img_layout);
        selectImgLayout.setVisibility(View.GONE);
        makeMapBtn = (RelativeLayout) findViewById(R.id.make_map_btn);

        Intent intent = getIntent();
        if (intent != null) {
            try{
                mAddress = intent.getExtras().getString("file_name");
                addressTextview.setText(mAddress);

                String floorsLever = intent.getExtras().getString("floors_lever");
                floorsLeverText.setText(floorsLever);

                double longitude = intent.getDoubleExtra("longitude", 0.0);
                double latitude = intent.getDoubleExtra("latitude", 0.0);
                mMyLatlng = new LatLng(latitude, longitude);
            } catch (Exception e) {
                WataLog.e("exception =" + e.toString());
                addressTextview.setText("");
                floorsLeverText.setText("");
            }


        }

        none_map_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pathdrawing 페이지 이동

                gotoPage();
            }
        });

        yes_map_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 갤러리 페이지 이동
                goToAlbum();

            }
        });

        makeMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 기록 페이지
                gotoPage();
            }
        });

        backKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, Constance.PICK_FROM_ALBUM);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void gotoPage() {
        StaticManager.setTitle("PathDrawing");
        StaticManager.setFolderName("PathDrawing");
        StaticManager.setAddress("PathDrawing");

        StaticManager.setBasePointX("202351.273872");
        StaticManager.setBasePointX("544170.137029");
        StaticManager.setGid("1168010100108210001S01300");
        StaticManager.setPosition(0);
        StaticManager.setFloorName("AB01");
        Intent intent = new Intent(SelectMapActivity.this, PathDrawingActivity.class);
        intent.putExtra("file_name", mAddress);
        intent.putExtra("map_id", "-1");
        intent.putExtra("longitude", mMyLatlng.longitude);
        intent.putExtra("latitude", mMyLatlng.latitude);
        intent.putExtra("image_name", mFileName);

        startActivity(intent);
    }

    private String mFileName = "";
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        WataLog.d("requestCode=" + requestCode);
//        Uri fileUri = Uri.parse("data.getData().getPath()");
        if(data != null) {
            Uri uri = data.getData();
            mFileName = getRealPathFromURI(uri);

            if (requestCode == Constance.PICK_FROM_ALBUM && data != null) {
                selectMapLayout.setVisibility(View.GONE);
                selectImgLayout.setVisibility(View.VISIBLE);

                Uri photoUri = data.getData();
                Cursor cursor = null;
                try {
                    String[] proj = {MediaStore.Images.Media.DATA};
                    assert photoUri != null;
                    cursor = getContentResolver().query(photoUri, proj, null, null, null);

                    assert cursor != null;
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    tempFile = new File(cursor.getString(column_index));

                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                setImage();
            }
        }
    }

    private void setImage() {
        ImageView mapImage = findViewById(R.id.map_select_image);
        BitmapFactory.Options options = new BitmapFactory.Options();
        final Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        mapImage.setImageBitmap(originalBm);

//        new AsyncTask<Void, Void, String>() {
//            @Override
//            protected String doInBackground(Void... voids) {
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                originalBm.compress(Bitmap.CompressFormat.JPEG, 70, stream);
//                byte[] byteFormat = stream.toByteArray();
//                WataLog.d("byteFormat=" + byteFormat);
//
//                // Get the Base64 string
//                String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
//                WataLog.d("imgString=" + imgString);
//                return imgString;
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                WataLog.d("onPostExecute=" + s);
//            }
//        }.execute();

    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        CursorLoader cursorLoader = new CursorLoader(this, contentURI, null, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        WataLog.d("cursor=" + cursor);

        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME);
//            WataLog.d("result=" + cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)));
            result = cursor.getString(idx);
            cursor.close();
        }
        WataLog.d("result=" + result);
        return result;
    }

}
