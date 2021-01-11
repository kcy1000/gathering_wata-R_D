package com.terry.view.swipeanimationbutton;

import android.content.Context;
import android.util.Log;

public class WataLog {

    private static String TAG_TITLE = "wata";
    private static boolean LOG_STATE = true;
    public static void v(Context context, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.v(context.toString(), a_strLogMsg);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void v(Class c, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.v(c.getName(), a_strLogMsg);
        }
    }

    public static void v(String a_strLogTitle, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.v(a_strLogTitle, a_strLogMsg);
        }
    }

    public static void v(String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.v(TAG_TITLE, a_strLogMsg);
        }
    }

    public static void i(Context context, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.i(context.toString(), a_strLogMsg);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void i(Class c, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.i(c.getName(), a_strLogMsg);
        }
    }

    public static void i(String a_strLogTitle, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;

            Log.i(a_strLogTitle, a_strLogMsg);
        }
    }

    public static void i(String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.i(TAG_TITLE, a_strLogMsg);
        }
    }

    public static void d(Context context, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            WataLog.d( a_strLogMsg);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void d(Class c, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.d(c.getName(), a_strLogMsg);
        }
    }

    public static void d(String a_strLogTitle, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.d(a_strLogTitle, a_strLogMsg);
        }
    }

    public static void d(String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.d(TAG_TITLE, a_strLogMsg);
        }
    }

    public static void e(Context context, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.e(context.toString(), a_strLogMsg);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void e(Class c, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.e(c.getName(), a_strLogMsg);
        }
    }

    public static void e(String a_strLogTitle, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.e(a_strLogTitle, a_strLogMsg);
        }
    }

    public static void e(String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.e(TAG_TITLE, a_strLogMsg);
        }
    }


    @SuppressWarnings("rawtypes")
    public static void w(Class c, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.w(c.getName(), a_strLogMsg);
        }
    }

    public static void w(String a_strLogTitle, String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.w(a_strLogTitle, a_strLogMsg);
        }
    }

    public static void w(String a_strLogMsg) {
        if (LOG_STATE) {
            Exception e = new Exception();
            StackTraceElement callerElement = e.getStackTrace()[1];
            a_strLogMsg = callerElement.getFileName() + " " + +callerElement.getLineNumber() + "] " + a_strLogMsg;
            Log.w(TAG_TITLE, a_strLogMsg);
        }
    }

}
