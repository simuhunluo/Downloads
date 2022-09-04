package com.mundane.downloads.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.mundane.downloads.R;
import com.mundane.downloads.dto.Video;
import com.mundane.downloads.exception.MyException;
import com.mundane.downloads.ui.dialog.ProgressDialogFragment;
import com.mundane.downloads.util.ClipUtil;
import com.mundane.downloads.util.LogUtils;
import com.mundane.downloads.util.ParseUtil;
import com.mundane.downloads.util.RefreshUtil;
import com.mundane.downloads.util.StringUtils;
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
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class IndexActivity extends AppCompatActivity {
    
    private TextView mTvDownload;
    
    public static final String PREFIX = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=";
    private ProgressDialogFragment mProgressDialogFragment;
    
    private static final int REQUEST_CODE = 100;
    private TextView mTvAbout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        mTvDownload = findViewById(R.id.tv_download);
        mTvAbout = findViewById(R.id.tv_about);
        mTvDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });
        mTvAbout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openAbout();
            }
        });
        
        checkPermission();
    }
    
    private void openAbout() {
        Intent intent = new Intent(this, AboutMeActivity.class);
        startActivity(intent);
    }
    
    private void checkPermission() {
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            try {
                String[] PERMISSIONS_STORAGE = { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE };
                int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_CODE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                T.show("请授予存储权限");
                finish();
            }
        }
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            String clipboardData = ClipUtil.getClipboardData(this);
            if (!StringUtils.isEmpty(clipboardData)) {
                T.show(clipboardData);
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    private void download() {
        String clipboardData = ClipUtil.getClipboardData(this);
        if (StringUtils.isEmpty(clipboardData)) {
            T.show("剪切板内容为空");
            return;
        }
        final String text = clipboardData;
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                //1、“异步线程” 执行耗时操作
                //2、“执行完毕” 调用onNext触发回调，通知观察者
                String url = ParseUtil.parseUrl(text);
                String videoId = ParseUtil.parseVideoId(url);
                if (videoId == null) {
                    e.onError(new MyException("videoId is null"));
                    return;
                }
                LogUtils.d("videoId = " + videoId);
                url = PREFIX + videoId;
                String jsonStr = ParseUtil.getJsonStr(url);
                if (jsonStr == null) {
                    e.onError(new MyException("jsonStr is null"));
                    return;
                }
                int awemeType = ParseUtil.getAwemeType(jsonStr);
                System.out.println("awesomeType = " + awemeType);
                if (awemeType == 4) {
                    Video video = ParseUtil.getVideo(jsonStr, videoId);
                    if (video == null) {
                        e.onError(new MyException("video is null"));
                        return;
                    }
                    downloadVideo(video, e);
                } else if (awemeType == 2) {
                    List<String> picList = ParseUtil.getPicList(jsonStr);
                    downloadPic(videoId, picList, e);
                }
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
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                // “主线程”执行的方法
                LogUtils.d("progress = " + progress);
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
    
    private void downloadPic(String videoId, List<String> picList, ObservableEmitter<Integer> observableEmitter) {
        Collections.reverse(picList);
        int size = picList.size();
        try {
            for (int index = 0; index < size; index++) {
                int count = index + 1;
                downloadPic(videoId, count, picList.get(index));
                int progress = (int) (count * 100.0 / size);
                observableEmitter.onNext(progress);
            }
        } catch (MyException e1) {
            observableEmitter.onError(e1);
        }
        observableEmitter.onComplete();
    }
    
    public void downloadPic(String videoId, int count, String picUrl) throws MyException {
        try {
            Connection.Response document = Jsoup.connect(picUrl).ignoreContentType(true).maxBodySize(30000000).timeout(10000).execute();
            BufferedInputStream intputStream = document.bodyStream();
            File appDir = new File(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            File fileSavePath = new File(appDir, videoId + "_" + count + ".png");
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
            while ((byteRead = intputStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
            }
            intputStream.close();
            fs.close();
            RefreshUtil.scanFile(IndexActivity.this, fileSavePath.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyException(e.getMessage());
        }
    }
    
    private void hideDialog() {
        if (mProgressDialogFragment != null) {
            mProgressDialogFragment.dismiss();
        }
        mProgressDialogFragment = null;
    }
    
    private void updateProgress(int progress) {
        if (mProgressDialogFragment == null) {
            createAndShowDialog();
        }
        mProgressDialogFragment.updateProgress(progress);
    }
    
    private void createAndShowDialog() {
        mProgressDialogFragment = ProgressDialogFragment.newInstance();
        mProgressDialogFragment.setCancelable(false);
        mProgressDialogFragment.show(getSupportFragmentManager(), "");
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
            Connection.Response document = Jsoup.connect(videoAddress)
                    .ignoreContentType(true)
                    .maxBodySize(30000000)
                    .timeout(30000)
                    .execute();
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
            RefreshUtil.scanFile(IndexActivity.this, fileSavePath.getAbsolutePath());
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new MyException(exception.getMessage());
        }
    }
}
