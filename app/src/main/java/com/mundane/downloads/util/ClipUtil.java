package com.mundane.downloads.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * ClipUtil
 *
 * @author fangyuan
 * @date 2022-08-28
 */
public class ClipUtil {
    
    public static String getClipboardData(Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            CharSequence text = clipData.getItemAt(0).getText();
            return text.toString();
        }
        return null;
    }
    
}
