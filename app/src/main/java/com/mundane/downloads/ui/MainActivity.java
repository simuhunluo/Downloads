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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.mundane.downloads.R;
import com.mundane.downloads.base.BaseActivity;
import com.mundane.downloads.exception.MyException;
import com.mundane.downloads.util.ClipUtil;
import com.mundane.downloads.util.LogUtils;
import com.mundane.downloads.util.ParseUtil;
import com.mundane.downloads.util.RefreshUtil;
import com.mundane.downloads.util.RegexUtil;
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
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class MainActivity extends BaseActivity {
    
    private TextView mTvDownload;
    
    
    private static final int REQUEST_CODE = 100;
    private TextView mTvAbout;
    private TextView mTvPatchDownload;
    
    public static final String VIDEO_URL = "https://www.douyin.com/video/";
    
    public static final String NOTE_URL = "https://www.douyin.com/note/";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        mTvDownload = findViewById(R.id.tv_download);
        mTvAbout = findViewById(R.id.tv_about);
        mTvPatchDownload = findViewById(R.id.tv_patch_download);
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
        mTvPatchDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                patchDownload();
            }
        });
        
        checkPermission();
    }
    
    private void patchDownload() {
        Intent intent = new Intent(this, PatchDownloadActivity.class);
        startActivity(intent);
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
                String awesomeUrl = ParseUtil.getAwesomeUrl(url);
                LogUtils.d("awesomeUrl = " + awesomeUrl);
                if (awesomeUrl.startsWith(VIDEO_URL)) {
                    downloadVideo(awesomeUrl, e);
                } else if (awesomeUrl.startsWith(NOTE_URL)) {
                    downloadNote(awesomeUrl, e);
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
    
    private void downloadPic(JSONObject awesomeInfo, ObservableEmitter<Integer> observableEmitter) throws MyException {
        JSONObject detail = awesomeInfo.getJSONObject("aweme").getJSONObject("detail");
        String title = detail.getStr("desc");
        JSONArray images = detail.getJSONArray("images");
        
        try {
            for (int index = 0; index < images.size(); index++) {
                int count = index + 1;
                JSONObject image = (JSONObject) images.get(index);
                JSONArray urlList = image.getJSONArray("urlList");
                downloadPic(title, count, urlList.get(urlList.size() - 1).toString());
                int progress = (int) (count * 100.0 / images.size());
                observableEmitter.onNext(progress);
            }
        } catch (MyException e) {
            e.printStackTrace();
            throw new MyException(e.getMessage());
        }
    }
    
    
    
    
    public void downloadVideo(String awesomeUrl, ObservableEmitter<Integer> observableEmitter) {
        JSONObject data = ParseUtil.getData(awesomeUrl);
        
        JSONObject awesomeInfo = ParseUtil.getAwesomeInfo(data);
        try {
            download(awesomeInfo, observableEmitter);
        } catch (MyException e1) {
            observableEmitter.onError(e1);
        }
        observableEmitter.onComplete();
    }
    
    private void downloadNote(String awesomeUrl, ObservableEmitter<Integer> observableEmitter) {
        JSONObject data = ParseUtil.getData(awesomeUrl);
        
        JSONObject awesomeInfo = ParseUtil.getAwesomeInfo(data);
        try {
            downloadPic(awesomeInfo, observableEmitter);
        } catch (MyException e) {
            observableEmitter.onError(e);
        }
        observableEmitter.onComplete();
    
    }
    
    private void download(JSONObject awesomeInfo, ObservableEmitter<Integer> e) throws MyException {
        JSONObject detail = awesomeInfo.getJSONObject("aweme").getJSONObject("detail");
        String playApi = detail.getJSONObject("video").getStr("playApi");
        String title = detail.getStr("desc");
        playApi = "https:" + playApi;
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
            RefreshUtil.scanFile(MainActivity.this, fileSavePath.getAbsolutePath());
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new MyException(exception.getMessage());
        }
    }
}
