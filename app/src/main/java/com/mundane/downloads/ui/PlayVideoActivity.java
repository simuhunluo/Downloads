package com.mundane.downloads.ui;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import com.mundane.downloads.R;
import com.mundane.downloads.bean.DouyinDataBean;
import com.mundane.downloads.dto.Video;
import com.mundane.downloads.exception.MyException;
import com.mundane.downloads.ui.dialog.LoadingDialogFragment;
import com.mundane.downloads.util.LogUtils;
import com.mundane.downloads.util.ParseUtil;
import com.mundane.downloads.util.RefreshUtil;
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

public class PlayVideoActivity extends AppCompatActivity {
    
    private static final String PREFIX = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=";
    
    private static final int TYPE_VIDEO = 4;
    private static final int TYPE_PIC = 2;
    private JzvdStd mVideoView;
    
    private LoadingDialogFragment mLoadingDialogFragment;
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
        mTvDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (douyinData.awemeType == TYPE_VIDEO) {
                    download(douyinData);
                }
            }
        });
        Observable.create(new ObservableOnSubscribe<Video>() {
            @Override
            public void subscribe(ObservableEmitter<Video> e) throws Exception {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                //1、“异步线程” 执行耗时操作
                //2、“执行完毕” 调用onNext触发回调，通知观察者
                String url = PREFIX + douyinData.awemeId;
                String videJsonStr = ParseUtil.getJsonStr(url);
                if (videJsonStr == null) {
                    e.onError(new MyException("videJsonStr is null"));
                    return;
                }
                if (douyinData.awemeType == TYPE_VIDEO) {
                    Video video = ParseUtil.getVideo(videJsonStr, douyinData.awemeId);
                    if (video == null) {
                        System.out.println("video is null");
                        return;
                    }
                    e.onNext(video);
                }
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Video>() {
            @Override
            public void onSubscribe(Disposable d) {
                // 订阅线程  订阅的那一刻在订阅线程中执行
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
            }
            
            @Override
            public void onNext(Video video) {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                // “主线程”执行的方法
                setUp(video);
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
                hideDialog();
            }
        });
    }
    
    private void download(DouyinDataBean douyinData) {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                //1、“异步线程” 执行耗时操作
                //2、“执行完毕” 调用onNext触发回调，通知观察者
                String url = PREFIX + douyinData.awemeId;
                String jsonStr = ParseUtil.getJsonStr(url);
                if (jsonStr == null) {
                    e.onError(new MyException("jsonStr is null"));
                    return;
                }
                int awemeType = ParseUtil.getAwemeType(jsonStr);
                if (awemeType == 4) {
                    Video video = ParseUtil.getVideo(jsonStr, douyinData.awemeId);
                    if (video == null) {
                        e.onError(new MyException("video is null"));
                        return;
                    }
                    downloadVideo(video, e);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                // 订阅线程  订阅的那一刻在订阅线程中执行
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                showDialog();
            }
            
            @Override
            public void onNext(Integer progress) {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
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
    
    public void downloadVideo(Video video, ObservableEmitter<Integer> observableEmitter) {
        String originVideoAddress = video.getVideoAddress();
        String videoId = video.getVideoId();
        String videoAddress1080 = originVideoAddress.replace("720p", "1080p");
        try {
            if (ParseUtil.getContentLengthByAddress(videoAddress1080) > ParseUtil.getContentLengthByAddress(originVideoAddress)) {
                download(videoAddress1080, videoId, observableEmitter);
            } else {
                download(originVideoAddress, videoId, observableEmitter);
            }
        } catch (MyException e1) {
            observableEmitter.onError(e1);
        }
        observableEmitter.onComplete();
    }
    
    private void download(String videoAddress, String videoId, ObservableEmitter<Integer> e) throws MyException {
        try {
            Connection.Response document = Jsoup.connect(videoAddress).ignoreContentType(true).maxBodySize(30000000).timeout(30000).execute();
            BufferedInputStream intputStream = document.bodyStream();
            int contentLength = Integer.parseInt(document.header("Content-Length"));
            File appDir = new File(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            File fileSavePath = new File(appDir, videoId + ".mp4");
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
            RefreshUtil.scanFile(PlayVideoActivity.this, fileSavePath.getAbsolutePath());
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
    
    private void setUp(Video video) {
        mVideoView.setUp(video.getVideoAddress(), video.getDesc());
        mVideoView.startVideoAfterPreloading();
    }
    
    private void hideDialog() {
        if (mLoadingDialogFragment != null) {
            mLoadingDialogFragment.dismiss();
        }
    }
    
    private void showDialog() {
        if (mLoadingDialogFragment == null) {
            mLoadingDialogFragment = LoadingDialogFragment.newInstance();
            mLoadingDialogFragment.setCancelable(false);
        }
        mLoadingDialogFragment.show(getSupportFragmentManager(), "");
    }
}
