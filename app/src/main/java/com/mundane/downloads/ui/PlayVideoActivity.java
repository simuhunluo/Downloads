package com.mundane.downloads.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import cn.jzvd.Jzvd;
import cn.jzvd.JzvdStd;
import com.mundane.downloads.R;
import com.mundane.downloads.bean.DouyinDataBean;
import com.mundane.downloads.dto.Video;
import com.mundane.downloads.exception.MyException;
import com.mundane.downloads.ui.dialog.LoadingDialogFragment;
import com.mundane.downloads.util.LogUtils;
import com.mundane.downloads.util.ParseUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlayVideoActivity extends AppCompatActivity {
    
    private static final String PREFIX = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=";
    
    private static final int TYPE_VIDEO = 4;
    private static final int TYPE_PIC = 2;
    private JzvdStd mVideoView;
    
    private LoadingDialogFragment mLoadingDialogFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);
        mVideoView = findViewById(R.id.mp_video);
        
        DouyinDataBean douyinData = JsonListActivity.getDouyinData(getIntent());
        if (douyinData == null) {
            return;
        }
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
