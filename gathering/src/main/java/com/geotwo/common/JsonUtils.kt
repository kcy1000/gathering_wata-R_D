package com.geotwo.common

import android.content.Context;
import android.os.Environment
import android.util.Log;
import com.geotwo.LAB_TEST.Gathering.util.WataLog

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

object JsonUtils {

    val TAG = javaClass.simpleName
    var fileName = "gangnam_wata_path.json"


    fun saveData(context: Context, mJsonResponse: String) {
        try {
            Log.d(TAG, " path : " + context.filesDir.path)

            //val file = FileWriter(context.filesDir.path + "/" + fileName)
            val file = FileWriter(Environment.getExternalStorageDirectory().path+"/LBSPTracLab/config_wata_lbsp.json")

            file.write(mJsonResponse)
            file.flush()
            file.close()
        } catch (e: IOException) {
            Log.e("TAG", "Error in Writing: " + e.localizedMessage)
        }
    }

    fun getData(path : String ): String? {
        try {
            //val file = File(context.filesDir.path + "/" + fileName)
            val file = File(path)
            if( file.exists() )
            {
                Log.d(TAG,"file is existed")
                val isFile = FileInputStream(file)
                val size = isFile.available()
                val buffer = ByteArray(size)
                isFile.read(buffer)
                isFile.close()

                return String(buffer)
            }
            else
            {
                WataLog.d("no file")
                return null
            }

        } catch (e: IOException) {
            WataLog.d( "Error in Reading: " + e.localizedMessage)
            return null
        }
    }

}