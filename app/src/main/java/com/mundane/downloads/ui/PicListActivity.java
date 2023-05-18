package com.mundane.downloads.ui;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import com.mundane.downloads.R;
import com.mundane.downloads.adapter.PicListRvAdapter;
import com.mundane.downloads.adapter.itemdecoration.ListRvItemDecoration;
import com.mundane.downloads.base.BaseActivity;
import com.mundane.downloads.bean.DouyinDataBean;
import com.mundane.downloads.bean.DouyinPicBean;
import com.mundane.downloads.exception.MyException;
import com.mundane.downloads.util.LogUtils;
import com.mundane.downloads.util.T;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class PicListActivity extends BaseActivity {
    
    private RecyclerView mRv;
    
    private List<DouyinPicBean> mDataList = new ArrayList<>();
    
    private PicListRvAdapter mListRvAdapter;
    
    private View mTvDownload;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_list);
        
        DouyinDataBean douyinData = JsonListActivity.getDouyinData(getIntent());
        if (douyinData == null) {
            return;
        }
        for (String url : douyinData.imageList) {
            DouyinPicBean douyinPicBean = new DouyinPicBean();
            douyinPicBean.url = url;
            mDataList.add(douyinPicBean);
        }
        mRv = findViewById(R.id.rv);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRv.setLayoutManager(layoutManager);
        mRv.setItemAnimator(null);
        mRv.addItemDecoration(new ListRvItemDecoration(this));
        mListRvAdapter = new PicListRvAdapter(mDataList);
        mRv.setAdapter(mListRvAdapter);
        
        mTvDownload = findViewById(R.id.tv_download);
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
                    downloadPic(douyinData, observableEmitter);
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
    
    private void downloadPic(DouyinDataBean douyinData, ObservableEmitter<Integer> observableEmitter) throws MyException {
        try {
            List<String> imageList = douyinData.imageList;
            for (int index = 0; index < imageList.size(); index++) {
                int count = index + 1;
                downloadPic(douyinData.desc, count, imageList.get(index));
                int progress = (int) (count * 100.0 / imageList.size());
                observableEmitter.onNext(progress);
            }
        } catch (MyException e) {
            e.printStackTrace();
            throw new MyException(e.getMessage());
        }
    }
}
