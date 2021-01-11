package com.geotwo.LAB_TEST.Voucher

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
//    private var progressPopup: ProgressPopup? = null

//    fun showProgressPopup()  {
////        dismissProgressPopup()
//
//        if (!this.isFinishing) {
//            if (progressPopup == null) {
//                progressPopup = ProgressPopup(this)
//            }
//            if (!progressPopup?.isShowing!!) {
//                progressPopup!!.show()
//                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//            }
//        }
//    }
//
//    fun dismissProgressPopup() {
//        if (!this.isFinishing) {
//            if (progressPopup != null) {
//                if (progressPopup?.isShowing!!) {
//                    progressPopup?.dismiss()
//                    window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//                }
////            progressPopup = null
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // kcy1000 - 캡쳐 방지
//        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()

//        dismissProgressPopup()
    }

//    protected fun requestHttpQueue(onPrepareRequest: () -> Call<String>, onResponse: (code: Int, body: String?) -> Unit) {
//            HttpQueueHandler.offer(HttpQueue(onPrepareRequest, onResponse, {
//            }))
//    }

    override fun onBackPressed() {
//        MasterLog.i("onBackPressed")
        super.onBackPressed()
        return
    }

//    private fun isHookCheck(): Boolean {
//        var isHookCheck = RoutingCheck.antihook(applicationContext)
////        MasterLog.d("isHookCheck= $isHookCheck")
////        return isHookCheck
//        return false
//    }
//
//    private fun isRoutingCheck(): Boolean {
//        var routingCheck = RoutingCheck.checkSuperUser()
////        MasterLog.d("routingCheck= $routingCheck")
////        return routingCheck
//        return false
//    }
//
//    private fun isExecCmdCheck(): Boolean {
//        var execCmd = RoutingCheck.execCmd()
////        MasterLog.d("execCmd= $execCmd")
//        return execCmd
//    }
//
//    private fun removeFile(fileName: String) {
//        var mPath = "/data/data/" + this.getPackageName() + fileName
////        MasterLog.d("mPath= $mPath")
//        try {
//            val mFile = File(mPath)
//            mFile.delete()
//        } catch (e: Exception) {
//            MasterLog.e("Exception= ${e.toString()}")
//        }
//    }
//
//    fun onErrorMsgPopup(message: String) {
//        runOnUiThread {
//            dismissProgressPopup()
//        }
//        Handler(Looper.getMainLooper()).post {
//            val popup = OneButtonPopup(this, message, View.OnClickListener {
//                finish()
//            })
//            popup.show()
//        }
//    }
//
//    fun onMessagePopup(message: String) {
//        runOnUiThread {
//            dismissProgressPopup()
//        }
//
//        Handler(Looper.getMainLooper()).post {
//            val popup = OneButtonPopup(this, message, View.OnClickListener {
//            })
//            popup.show()
//        }
//    }


//    fun onKillProcess() {
//        dismissProgressPopup()
//
//        PreferenceHandler.remove(baseContext, PreferenceConstant.Key.SERVICE_ID)
//        PreferenceHandler.remove(baseContext, PreferenceConstant.Key.PUSH_ID)
//        android.os.Process.killProcess(android.os.Process.myPid())
//    }

//    fun setResultMsg(resultCode: Int, msg: String, code: String) {
//
//        val intent = Intent(Intent.ACTION_VIEW)
//        val bundle = Bundle()
//        bundle.putString(Constance.MESSAGE, msg)
//        bundle.putString(Constance.LOGIN_CODE, code)
//        intent.putExtras(bundle)
//        setResult(resultCode, intent)
//
//    }
//
//    fun setResultMsg(resultCode: Int, msg: String) {
//        val intent = Intent(Intent.ACTION_VIEW)
//        val bundle = Bundle()
//        bundle.putString(Constance.MESSAGE, msg)
//        intent.putExtras(bundle)
//        setResult(resultCode, intent)
//
//    }
//
//    fun getMapView(): MapView {
//        val mapView = MapView(this)
//        return mapView
//    }
//
//    fun getUserClass(): String? {
//        return PreferenceHandler.getString(applicationContext, PreferenceConstant.Key.USER_ClSS)
//    }
//
//    fun getUserMode(): String? {
//        return PreferenceHandler.getString(applicationContext, PreferenceConstant.Key.USER_MODE)
//    }


}