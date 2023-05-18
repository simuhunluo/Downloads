package com.mundane.downloads.ui;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import com.mundane.downloads.R;
import com.mundane.downloads.base.BaseActivity;
import com.mundane.downloads.bean.DouyinDataBean;
import com.mundane.downloads.exception.MyException;
import com.mundane.downloads.util.LogUtils;
import com.mundane.downloads.util.RefreshUtil;
import com.mundane.downloads.util.RegexUtil;
import com.mundane.downloads.util.T;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class PlayVideoActivity extends BaseActivity {
    
    private JzvdStd mVideoView;
    
    private View mTvDownload;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        mVideoView = findViewById(R.id.mp_video);
        mTvDownload = findViewById(R.id.tv_download);
        
        DouyinDataBean douyinData = JsonListActivity.getDouyinData(getIntent());
        if (douyinData == null) {
            return;
        }
        setUp(douyinData);
        mTvDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                download(douyinData);
            }
        });
    }
    
    private void download(DouyinDataBean douyinData) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> observableEmitter) throws Exception {
                try {
                    downloadVideo(douyinData, observableEmitter);
                } catch (MyException exception) {
                    observableEmitter.onError(exception);
                }
                observableEmitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                // 订阅线程  订阅的那一刻在订阅线程中执行
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                if (mProgressDialogFragment == null) {
                    createAndShowDialog();
                }
            }
            
            @Override
            public void onNext(Integer progress) {
                // “主线程”执行的方法
                updateProgress(progress);
            }
            
            @Override
            public void onError(Throwable e) {
                // "主线程"执行的方法
                e.printStackTrace();
                hideDialog();
            }
            
            @Override
            public void onComplete() {
                // "主线程"执行的方法
                LogUtils.d("onComplete");
                T.show("下载完成");
                hideDialog();
            }
        });
    }
    
    private void downloadVideo(DouyinDataBean douyinData, ObservableEmitter<Integer> e) throws MyException {
        String playApi = douyinData.playApi;
        String title = RegexUtil.replaceTitle(douyinData.desc);
        try {
            title = RegexUtil.replaceTitle(title);
            Connection.Response document = Jsoup.connect(playApi).ignoreContentType(true).maxBodySize(0).timeout(0).execute();
            BufferedInputStream intputStream = document.bodyStream();
            int contentLength = Integer.parseInt(document.header("Content-Length"));
            File appDir = new File(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator);
            if (!appDir.exists()) {
                appDir.mkdirs();
            }
            File fileSavePath = new File(appDir, title + ".mp4");
            // 如果保存文件夹不存在,那么则创建该文件夹
            File fileParent = fileSavePath.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            if (fileSavePath.exists()) { //如果文件存在，则删除原来的文件
                fileSavePath.delete();
            }
            FileOutputStream fs = new FileOutputStream(fileSavePath);
            byte[] buffer = new byte[8 * 1024];
            int byteRead;
            int count = 0;
            while ((byteRead = intputStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
                count += byteRead;
                int progress = (int) (count * 100.0 / contentLength);
                e.onNext(progress);
            }
            intputStream.close();
            fs.close();
            RefreshUtil.scanFile(this, fileSavePath.getAbsolutePath());
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new MyException(exception.getMessage());
        }
    }
    
    @Override
    public void onBackPressed() {
        if (Jzvd.backPress()) {
            return;
        }
        super.onBackPressed();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }
    
    private void setUp(DouyinDataBean data) {
        mVideoView.setUp(data.playApi, "");
        mVideoView.startVideoAfterPreloading();
    }
    
}
