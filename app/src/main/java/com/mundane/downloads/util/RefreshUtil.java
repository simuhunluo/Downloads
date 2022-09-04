package com.mundane.downloads.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.io.File;

/**
 * RefreshUtil
 *
 * @author fangyuan
 * @date 2022-08-28
 */
public class RefreshUtil {
    /**
     * 针对系统文夹只需要扫描,不用插入内容提供者,不然会重复
     *
     * @param context 上下文
     * @param filePath 文件路径
     */
    public static void scanFile(Context context, String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(intent);
    }
}
