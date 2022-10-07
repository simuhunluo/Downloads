package com.mundane.downloads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.mundane.downloads.R;
import com.mundane.downloads.adapter.ListRvAdapter;
import com.mundane.downloads.adapter.ListRvAdapter.OnItemClickListener;
import com.mundane.downloads.adapter.itemdecoration.ListRvItemDecoration;
import com.mundane.downloads.adapter.listener.EndlessRecyclerOnScrollListener;
import com.mundane.downloads.bean.DouyinDataBean;
import com.mundane.downloads.exception.MyException;
import com.mundane.downloads.util.LogUtils;
import com.mundane.downloads.util.ParseUtil;
import com.mundane.downloads.util.T;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JsonListActivity extends AppCompatActivity {
    
    private RecyclerView mRv;
    private ListRvAdapter mListRvAdapter;
    
    private static final String DATA = "DATA";
    
    private static final String LIST_URL_PREFIX = "https://www.iesdouyin.com/web/api/v2/aweme/post/?reflow_source=reflow_page&sec_uid=";
    
    private String mSecUid;
    
    private String mMaxCursor = "0";
    
    private Set<String> mMaxCursorSet = new HashSet<>();
    
    private List<DouyinDataBean> mDataList = new ArrayList<>();
    
    private boolean isLoading = false;
    
    private Handler mHandler = new Handler();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_list);
        mRv = findViewById(R.id.rv);
        
        String secUid = PatchDownloadActivity.getSecUidFromIntent(getIntent());
        if (secUid == null) {
            T.show("secUid is null");
            return;
        }
        mSecUid = secUid;
        mMaxCursorSet.add("0");
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRv.setLayoutManager(layoutManager);
        mListRvAdapter = new ListRvAdapter(mDataList);
        mListRvAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(int position, DouyinDataBean bean) {
                openPlayVideo(bean);
            }
        });
        mRv.setAdapter(mListRvAdapter);
        mRv.addItemDecoration(new ListRvItemDecoration(this));
        mRv.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                if (isLoading) {
                    return;
                }
                if (mMaxCursorSet.contains(mMaxCursor)) {
                    return;
                }
                fetchData();
            }
        });
        mRv.setHasFixedSize(true);
        // 请求第一页数据
        fetchUntilFillScreen();
    }
    
    // 一直请求知道填满屏幕或者到最后一页
    private void fetchUntilFillScreen() {
        if (isLoading) {
            return;
        }
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                //1、“异步线程” 执行耗时操作
                //2、“执行完毕” 调用onNext触发回调，通知观察者
                String listUrl = LIST_URL_PREFIX + mSecUid + "&count=21&max_cursor=" + mMaxCursor;
                String listJsonStr = ParseUtil.getJsonStr(listUrl);
                if (listJsonStr == null) {
                    e.onError(new MyException("listJsonStr is null"));
                    return;
                }
                e.onNext(listJsonStr);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                // 订阅线程  订阅的那一刻在订阅线程中执行
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                isLoading = true;
            }
        
            @Override
            public void onNext(String jsonStr) {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                // “主线程”执行的方法
                addDataToListUntilFillScreen(jsonStr);
            }
        
            @Override
            public void onError(Throwable e) {
                // "主线程"执行的方法
                e.printStackTrace();
                isLoading = false;
            }
        
            @Override
            public void onComplete() {
                // "主线程"执行的方法
                LogUtils.d("onComplete");
                isLoading = false;
            }
        });
    }
    
    private void fetchData() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                //1、“异步线程” 执行耗时操作
                //2、“执行完毕” 调用onNext触发回调，通知观察者
                String listUrl = LIST_URL_PREFIX + mSecUid + "&count=21&max_cursor=" + mMaxCursor;
                String listJsonStr = ParseUtil.getJsonStr(listUrl);
                if (listJsonStr == null) {
                    e.onError(new MyException("listJsonStr is null"));
                    return;
                }
                e.onNext(listJsonStr);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                // 订阅线程  订阅的那一刻在订阅线程中执行
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                isLoading = true;
            }
            
            @Override
            public void onNext(String jsonStr) {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                // “主线程”执行的方法
                addDataToList(jsonStr);
            }
            
            @Override
            public void onError(Throwable e) {
                // "主线程"执行的方法
                e.printStackTrace();
                isLoading = false;
            }
            
            @Override
            public void onComplete() {
                // "主线程"执行的方法
                LogUtils.d("onComplete");
                isLoading = false;
            }
        });
    }
    
    private void addDataToListUntilFillScreen(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        mMaxCursor = json.getStr("max_cursor");
        LogUtils.d("max_cursor = " + mMaxCursor);
        JSONArray awemeList = json.getJSONArray("aweme_list");
        List<DouyinDataBean> dataList = new ArrayList<>();
        for (Object o : awemeList) {
            JSONObject jsonObject = (JSONObject) o;
            JSONObject video = jsonObject.getJSONObject("video");
            if (video == null) {
                LogUtils.d("video is null");
                continue;
            }
            JSONArray jsonArray = video.getJSONObject("cover").getJSONArray("url_list");
            String coverUrl = jsonArray.get(0).toString();
            String awemeId = jsonObject.getStr("aweme_id");
            Integer awemeType = jsonObject.getInt("aweme_type");
        
            DouyinDataBean douyinDataBean = new DouyinDataBean();
            douyinDataBean.coverUrl = coverUrl;
            douyinDataBean.awemeId = awemeId;
            douyinDataBean.awemeType = awemeType;
            douyinDataBean.type = ListRvAdapter.TYPE_ITEM;
            dataList.add(douyinDataBean);
        }
        int positionStart = mDataList.size();
        mDataList.addAll(dataList);
        mListRvAdapter.notifyItemRangeInserted(positionStart, dataList.size());
    
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 判断是否滑动到了最后一个item
                if (lastItemVisible() && !mMaxCursorSet.contains(mMaxCursor)) {
                    fetchUntilFillScreen();
                }
            }
        }, 1000);
    }
    
    private void addDataToList(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        LogUtils.d("max_cursor = " + mMaxCursor);
        mMaxCursor = json.getStr("max_cursor");
        JSONArray awemeList = json.getJSONArray("aweme_list");
        List<DouyinDataBean> dataList = new ArrayList<>();
        for (Object o : awemeList) {
            JSONObject jsonObject = (JSONObject) o;
            JSONObject video = jsonObject.getJSONObject("video");
            if (video == null) {
                LogUtils.d("video is null");
                continue;
            }
            JSONArray jsonArray = video.getJSONObject("cover").getJSONArray("url_list");
            String coverUrl = jsonArray.get(0).toString();
            String awemeId = jsonObject.getStr("aweme_id");
            Integer awemeType = jsonObject.getInt("aweme_type");
            
            DouyinDataBean douyinDataBean = new DouyinDataBean();
            douyinDataBean.coverUrl = coverUrl;
            douyinDataBean.awemeId = awemeId;
            douyinDataBean.awemeType = awemeType;
            douyinDataBean.type = ListRvAdapter.TYPE_ITEM;
            dataList.add(douyinDataBean);
        }
        int positionStart = mDataList.size();
        mDataList.addAll(dataList);
        mListRvAdapter.notifyItemRangeInserted(positionStart, dataList.size());
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
    
    private boolean lastItemVisible() {
        LinearLayoutManager manager = (LinearLayoutManager) mRv.getLayoutManager();
        int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();
        int itemCount = manager.getItemCount();
        return lastItemPosition == itemCount - 1;
    }
    
    private void openPlayVideo(DouyinDataBean bean) {
        Intent intent = new Intent(this, PlayVideoActivity.class);
        intent.putExtra(DATA, bean);
        startActivity(intent);
    }
    
    public static DouyinDataBean getDouyinData(Intent intent) {
        DouyinDataBean data = (DouyinDataBean) intent.getSerializableExtra(DATA);
        return data;
    }
}
