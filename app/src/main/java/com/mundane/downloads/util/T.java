package com.mundane.downloads.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast统一管理类
 */
public class T {
    private static Context sContext;
    
    private T() {
    
    }
    
    public static void init(Context context) {
        sContext = context;
    }
    
    public static void show(CharSequence msg) {
        if (sContext == null) {
            return;
        }
        try {
            Toast.makeText(sContext, msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
