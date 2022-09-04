package com.mundane.downloads.util;

/**
 * LogUtils
 *
 * @author fangyuan
 * @date 2022-08-28
 */
import android.util.Log;

/**
 * 日志控制
 */
public class LogUtils {
    private final static String TAG = "mundane";
    private final static boolean sOpenLog = true;
    
    public static void d(String logKey, String msg) {
        d(logKey, msg, 2);
    }
    
    /**
     *
     * @param logKey
     * @param msg
     * @param stackIndex 1:当前位置，2：上级栈位置，0：logcat 的位置（没有意义）
     */
    public static void d(String logKey, String msg, int stackIndex) {
        if (sOpenLog) {
            StackTraceElement ste = new Throwable().getStackTrace()[stackIndex];
            String log = build(msg, ste);
            //            Log.d(logKey, log);
            Log.d(TAG, "[" + logKey + "]" + log);
        }
    }
    
    public static void d(String msg) {
        if (sOpenLog) {
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            String log = build(msg, ste);
            Log.d(TAG, log);
        }
    }
    
    public static void i(String logKey, String msg, int stackIndex) {
        if (sOpenLog) {
            StackTraceElement ste = new Throwable().getStackTrace()[stackIndex];
            String log = build(msg, ste);
            Log.i(TAG, "[" + logKey + "]" + log);
            //            Log.i(logKey, log);
            
        }
    }
    
    public static void i(String logKey, String msg) {
        i(logKey, msg, 2);
    }
    
    public static void v(String logKey, String msg, int stackIndex) {
        if (sOpenLog) {
            StackTraceElement ste = new Throwable().getStackTrace()[stackIndex];
            String log = build(msg, ste);
            Log.v(TAG, "[" + logKey + "]" + log);
        }
    }
    
    
    public static void v(String logKey, String msg) {
        v(logKey, msg, 2);
    }
    
    public static void w(String logKey, String msg) {
        if (sOpenLog) {
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            String log = build(logKey, msg, ste);
            Log.w(TAG, "[" + logKey + "]" + log);
        }
    }
    
    
    /**
     * 打印error级别的log
     *
     * @param tag tag标签
     */
    public static void e(String tag, Throwable tr) {
        if (sOpenLog) {
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            String log = build(tag, "", ste, tr);
            Log.e(tag, log, tr);
            //            Log.e(tag, "[" + Thread.currentThread().getId() + "]" + tr.getMessage(), tr);
        }
    }
    
    public static void e(String logKey, String msg) {
        if (sOpenLog) {
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            String log = build(logKey, msg, ste);
            Log.e(logKey, log);
        }
    }
    
    public static void e(String logKey, String msg, Throwable e) {
        if (sOpenLog) {
            StackTraceElement ste = new Throwable().getStackTrace()[1];
            String log = build(logKey, msg, ste);
            Log.e(TAG, log, e);
        }
    }
    
    /**
     * 打印调用栈信息
     *
     * @param tag tag标签
     * @param str 内容
     */
    public static void t(String tag, String str) {
        if (sOpenLog) {
            LogUtils.i(tag, "DebugInfo: " + str);
            Throwable e = new Throwable(tag);
            e.printStackTrace();
        }
    }
    
    /**
     * 制作打log位置的文件名与文件行号详细信息
     *
     * @param log
     * @param ste
     * @return
     */
    private static String build(String log, StackTraceElement ste) {
        StringBuilder buf = new StringBuilder();
        
        buf.append("[").append(Thread.currentThread().getId()).append("]");
        
        if (ste.isNativeMethod()) {
            buf.append("(Native Method)");
        } else {
            String fName = ste.getFileName();
            
            if (fName == null) {
                buf.append("(Unknown Source)");
            } else {
                int lineNum = ste.getLineNumber();
                buf.append('(');
                buf.append(fName);
                if (lineNum >= 0) {
                    buf.append(':');
                    buf.append(lineNum);
                }
                buf.append("):");
            }
        }
        buf.append(log);
        return buf.toString();
    }
    
    private static String build(String logKey, String msg, StackTraceElement ste) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(logKey).append("]").append(build(msg, ste));
        return sb.toString();
    }
    
    private static String build(String logKey, String msg, StackTraceElement ste, Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(logKey).append("]").append(ste.toString()).append(":").append(msg).append("\r\n").append("e:").append(e.getMessage());
        return sb.toString();
    }
}
