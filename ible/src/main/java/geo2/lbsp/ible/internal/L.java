package geo2.lbsp.ible.internal;


import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

public class L {
    private static final String TAG = "bleSDK";
    private static boolean ENABLE_DEBUG_LOGGING = false;
    private static boolean ENABLE_CRASHLYTICS_LOGGING = false;
    private static Method CRASHLYTICS_LOG_METHOD;

    public L() {
    }

    public static void enableDebugLogging(boolean enableDebugLogging) {
        ENABLE_DEBUG_LOGGING = enableDebugLogging;
    }

    public static void v(String msg) {
        if(ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.v("bleSDK", logMsg);
            logCrashlytics(logMsg);
        }

    }

    public static void d(String msg) {
        if(ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.d("bleSDK", logMsg);
            logCrashlytics(logMsg);
        }

    }

    public static void i(String msg) {
        if(!ENABLE_DEBUG_LOGGING) 
        	return;
        String logMsg = debugInfo() + msg;
        Log.i("bleSDK", logMsg);
        logCrashlytics(logMsg);
    }

    public static void w(String msg) {
        if(!ENABLE_DEBUG_LOGGING) 
        	return;
        String logMsg = debugInfo() + msg;
        Log.w("bleSDK", logMsg);
        logCrashlytics(logMsg);
    }

    public static void e(String msg) {
        if(!ENABLE_DEBUG_LOGGING) 
        	return;
        String logMsg = debugInfo() + msg;
        Log.e("bleSDK", logMsg);
        logCrashlytics(msg);
    }

    public static void e(String msg, Throwable e) {
        if(!ENABLE_DEBUG_LOGGING) 
        	return;
        String logMsg = debugInfo() + msg;
        Log.e("bleSDK", logMsg, e);
        logCrashlytics(msg + " " + throwableAsString(e));
    }

    public static void wtf(String msg) {
        if(!ENABLE_DEBUG_LOGGING) 
        	return;
        String logMsg = debugInfo() + msg;
        Log.wtf("bleSDK", logMsg);
        logCrashlytics(logMsg);
    }

    public static void wtf(String msg, Exception exception) {
        if(!ENABLE_DEBUG_LOGGING) 
        	return;
        String logMsg = debugInfo() + msg;
        Log.wtf("bleSDK", logMsg, exception);
        logCrashlytics(logMsg + " " + throwableAsString(exception));
    }

    private static String debugInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String className = stackTrace[4].getClassName();
        String methodName = Thread.currentThread().getStackTrace()[4].getMethodName();
        int lineNumber = stackTrace[4].getLineNumber();
        return className + "." + methodName + ":" + lineNumber + " ";
    }

    private static String throwableAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static void enableCrashlyticsLogging(boolean enableCrashlytics) {
        if(enableCrashlytics) {
            try {
                Class e = Class.forName("com.crashlytics.android.Crashlytics");
                CRASHLYTICS_LOG_METHOD = e.getMethod("log", String.class);
                ENABLE_CRASHLYTICS_LOGGING = true;
            } catch (ClassNotFoundException var2) {
            } catch (NoSuchMethodException var3) {
            }
        } else {
            ENABLE_CRASHLYTICS_LOGGING = false;
        }

    }

    public static void logCrashlytics(String msg) {
        if(ENABLE_CRASHLYTICS_LOGGING) {
            try {
                CRASHLYTICS_LOG_METHOD.invoke(null, msg);
            } catch (Exception var2) {
            }
        }

    }
}
