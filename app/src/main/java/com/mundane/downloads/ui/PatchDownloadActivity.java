package com.mundane.downloads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import com.mundane.downloads.R;
import com.mundane.downloads.exception.MyException;
import com.mundane.downloads.ui.dialog.LoadingDialogFragment;
import com.mundane.downloads.util.ClipUtil;
import com.mundane.downloads.util.LogUtils;
import com.mundane.downloads.util.ParseUtil;
import com.mundane.downloads.util.StringUtils;
import com.mundane.downloads.util.T;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PatchDownloadActivity extends AppCompatActivity {
    
    private View mTvDownload;
    private EditText mEtUserPage;
    private LoadingDialogFragment mLoadingDialogFragment;
    
    private static final String AWESOME_URL = "AWESOME_URL";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patch_download);
        mTvDownload = findViewById(R.id.tv_download);
        mEtUserPage = findViewById(R.id.et_user_page);
        mTvDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboardDataToEt();
            }
        });
    }
    
    private void setClipboardDataToEt() {
        String clipboardData = ClipUtil.getClipboardData(this);
        if (StringUtils.isEmpty(clipboardData)) {
            T.show("剪切板内容为空");
            return;
        }
        setValueAndSelection(mEtUserPage, clipboardData);
        startParse();
    }
    
    public static void setValueAndSelection(EditText editText, String text) {
        if (StringUtils.isEmpty(text)) {
            return;
        }
        editText.setText(text);
        editText.setSelection(text.length());
    }
    
    private void startParse() {
        String text = mEtUserPage.getText().toString();
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                //1、“异步线程” 执行耗时操作
                //2、“执行完毕” 调用onNext触发回调，通知观察者
                String url = ParseUtil.parseUrl(text);
                if (url == null) {
                    e.onError(new MyException("url is null"));
                    return;
                }
                String awesomeUrl = ParseUtil.getAwesomeUrl(url);
                if (awesomeUrl == null) {
                    e.onError(new MyException("awesomeUrl is null"));
                    return;
                }
                e.onNext(awesomeUrl);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                // 订阅线程  订阅的那一刻在订阅线程中执行
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                if (mLoadingDialogFragment == null) {
                    createAndShowDialog();
                }
            }
            
            @Override
            public void onNext(String awesomeUrl) {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                // “主线程”执行的方法
                openList(awesomeUrl);
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
    
    private void openList(String awesomeUrl) {
        Intent intent = new Intent(this, JsonListActivity.class);
        intent.putExtra(AWESOME_URL, awesomeUrl);
        startActivity(intent);
    }
    
    public static String getAwesomeUrlFromIntent(Intent intent) {
        String awesomeUrl = (String) intent.getSerializableExtra(AWESOME_URL);
        return awesomeUrl;
    }
    
    private void hideDialog() {
        if (mLoadingDialogFragment != null) {
            mLoadingDialogFragment.dismiss();
        }
        mLoadingDialogFragment = null;
    }
    
    private void createAndShowDialog() {
        mLoadingDialogFragment = LoadingDialogFragment.newInstance();
        mLoadingDialogFragment.setCancelable(false);
        mLoadingDialogFragment.show(getSupportFragmentManager(), "");
    }
}
