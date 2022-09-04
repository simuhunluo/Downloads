package com.mundane.downloads;

import android.app.Application;
import com.mundane.downloads.util.T;

/**
 * DownloadApplication
 *
 * @author fangyuan
 * @date 2022-08-28
 */
public class DownloadApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        T.init(this);
    }
}
