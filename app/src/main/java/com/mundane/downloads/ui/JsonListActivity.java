package com.mundane.downloads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONNull;
import cn.hutool.json.JSONObject;
import com.mundane.downloads.R;
import com.mundane.downloads.adapter.ListRvAdapter;
import com.mundane.downloads.adapter.ListRvAdapter.OnItemClickListener;
import com.mundane.downloads.adapter.itemdecoration.ListRvItemDecoration;
import com.mundane.downloads.adapter.listener.EndlessRecyclerOnScrollListener;
import com.mundane.downloads.bean.DouyinDataBean;
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonListActivity extends AppCompatActivity {
    
    private RecyclerView mRv;
    private ListRvAdapter mListRvAdapter;
    
    private static final String DATA = "DATA";
    
    private List<DouyinDataBean> mDataList = new ArrayList<>();
    
    private boolean isLoading = false;
    
    private Handler mHandler = new Handler();
    private WebView mWebView;
    private String mUid;
    private Long mMaxCursor;
    private String mAwesomeUrl;
    private Integer mHasMore = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_list);
        mWebView = findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        //与js交互必须设置
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl("file:///android_asset/test.html");
        mRv = findViewById(R.id.rv);
        
        mAwesomeUrl = PatchDownloadActivity.getAwesomeUrlFromIntent(getIntent());
        if (mAwesomeUrl == null) {
            T.show("awesomeUrl is null");
            return;
        }
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mRv.setLayoutManager(layoutManager);
        mRv.setItemAnimator(null);
        mListRvAdapter = new ListRvAdapter(mDataList);
        mListRvAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(int position, DouyinDataBean bean) {
                if (StringUtils.isEmpty(bean.playApi)) {
                    openPicDownload(bean);
                } else {
                    openPlayVideo(bean);
                }
            }
        });
        mRv.setAdapter(mListRvAdapter);
        mRv.addItemDecoration(new ListRvItemDecoration(this));
        mRv.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                if (isLoading || mHasMore == 0) {
                    return;
                }
                fetchData();
            }
        });
        mRv.setHasFixedSize(true);
        // 请求第一页数据
        fetchFirstPage();
    }
    
    private void openPicDownload(DouyinDataBean bean) {
        Intent intent = new Intent(this, PicListActivity.class);
        intent.putExtra(DATA, bean);
        startActivity(intent);
    }
    
    private void openPlayVideo(DouyinDataBean bean) {
        Intent intent = new Intent(this, PlayVideoActivity.class);
        intent.putExtra(DATA, bean);
        startActivity(intent);
    }
    
    private void fetchFirstPage() {
        if (isLoading) {
            return;
        }
        Observable.create(new ObservableOnSubscribe<JSONObject>() {
            @Override
            public void subscribe(ObservableEmitter<JSONObject> e) throws Exception {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                //1、“异步线程” 执行耗时操作
                //2、“执行完毕” 调用onNext触发回调，通知观察者
                JSONObject response = ParseUtil.getData(mAwesomeUrl);
                JSONObject postInfo = ParseUtil.getPostInfo(response);
                e.onNext(postInfo);
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<JSONObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                // 订阅线程  订阅的那一刻在订阅线程中执行
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                isLoading = true;
            }
            
            @Override
            public void onNext(JSONObject postInfo) {
                JSONObject post = postInfo.getJSONObject("post");
                mUid = postInfo.getStr("uid");
                mMaxCursor = post.getLong("maxCursor");
                mHasMore = post.getInt("hasMore");
                
                JSONArray data = post.getJSONArray("data");
                addFirstPageDataToList(data);
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
        LogUtils.d("滑动到了最底部");
        if (mUid == null || mMaxCursor == null) {
            return;
        }
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("device_platform", "webapp");
        payload.put("aid", "6383");
        payload.put("channel", "channel_pc_web");
        payload.put("sec_user_id", mUid);
        payload.put("max_cursor", mMaxCursor.toString());
        payload.put("locate_query", "false");
        payload.put("show_live_replay_strategy", "1");
        payload.put("count", "10");
        payload.put("publish_video_strategy_type", "2");
        payload.put("pc_client_type", "1");
        payload.put("version_code", "170400");
        payload.put("version_name", "17.4.0");
        payload.put("cookie_enabled", "true");
        payload.put("screen_width", "1920");
        payload.put("screen_height", "1080");
        payload.put("browser_language", "zh-CN");
        payload.put("browser_platform", "Win32");
        payload.put("browser_name", "Chrome");
        payload.put("browser_version", "109.0.0.0");
        payload.put("browser_online", "true");
        payload.put("engine_name", "Blink");
        payload.put("engine_version", "109.0.0.0");
        payload.put("os_name", "Windows");
        payload.put("os_version", "10");
        payload.put("cpu_core_num", "8");
        payload.put("device_memory", "8");
        payload.put("platform", "PC");
        payload.put("downlink", "10");
        payload.put("effective_type", "4g");
        payload.put("round_trip_time", "50");
        String queryParams = getQueryParams(payload);
        
        String script = "javascript:get_xb('" + queryParams + "', '" + ParseUtil.getUserAgent() + "')";
        mWebView.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                // 移除双引号
                String xBogus = value.replace("\"", "");
                if (StringUtils.isEmpty(xBogus)) {
                    return;
                }
                //此处为 js 返回的结果
                payload.put("X-Bogus", xBogus);
                getPageInfo(payload);
            }
        });
    }
    
    private void getPageInfo(Map<String, String> payload) {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                LogUtils.d("currentThread = " + Thread.currentThread().getName());
                //1、“异步线程” 执行耗时操作
                //2、“执行完毕” 调用onNext触发回调，通知观察者
                String response = ParseUtil.getPageInfo(payload, mAwesomeUrl);
                e.onNext(response);
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
            public void onNext(String response) {
                if (StringUtils.isEmpty(response)) {
                    return;
                }
                JSONObject jsonObject = new JSONObject(response);
                mHasMore = jsonObject.getInt("has_more");
                mMaxCursor = jsonObject.getLong("max_cursor");
                JSONArray awemeList = jsonObject.getJSONArray("aweme_list");
                addPageDataToList(awemeList);
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
    
    private void addFirstPageDataToList(JSONArray data) {
        List<DouyinDataBean> dataList = new ArrayList<>();
        
        for (Object o : data) {
            JSONObject dataItem = (JSONObject) o;
            JSONObject video = dataItem.getJSONObject("video");
            Integer awemeType = dataItem.getInt("awemeType");
            String desc = dataItem.getStr("desc");
    
            DouyinDataBean douyinDataBean = new DouyinDataBean();
            douyinDataBean.coverUrl = video.getJSONArray("coverUrlList").get(0).toString();
            douyinDataBean.awemeType = awemeType;
            douyinDataBean.desc = desc;
            douyinDataBean.type = ListRvAdapter.TYPE_ITEM;
            JSONArray images = new JSONArray();
            if (dataItem.get("images") != JSONNull.NULL) {
                images = dataItem.getJSONArray("images");
            }
            
            if (images.isEmpty()) { // 视频
                String playApi = "https:" + video.getStr("playApi");
                douyinDataBean.playApi = playApi;
            } else if (douyinDataBean.awemeType == 68) { // 图片
                List<String> imageList = new ArrayList<>();
                for (Object o1 : images) {
                    JSONObject image = (JSONObject) o1;
                    JSONArray urlList = image.getJSONArray("urlList");
                    imageList.add(urlList.get(urlList.size() - 1).toString());
                }
                douyinDataBean.imageList = imageList;
            }
            dataList.add(douyinDataBean);
        }
        int positionStart = mDataList.size();
        mDataList.addAll(dataList);
        mListRvAdapter.notifyItemRangeInserted(positionStart, dataList.size());
    }
    
    private void addPageDataToList(JSONArray awemeList) {
        List<DouyinDataBean> dataList = new ArrayList<>();
        
        for (Object o : awemeList) {
            JSONObject dataItem = (JSONObject) o;
            JSONObject video = dataItem.getJSONObject("video");
            Integer awemeType = dataItem.getInt("aweme_type");
            String desc = dataItem.getStr("desc");
    
            DouyinDataBean douyinDataBean = new DouyinDataBean();
            JSONObject cover = video.getJSONObject("cover");
            douyinDataBean.coverUrl = cover.getJSONArray("url_list").get(0).toString();
            douyinDataBean.awemeType = awemeType;
            douyinDataBean.desc = desc;
            douyinDataBean.type = ListRvAdapter.TYPE_ITEM;
            JSONArray images = new JSONArray();
            if (dataItem.get("images") != JSONNull.NULL) {
                images = dataItem.getJSONArray("images");
            }
            if (images.isEmpty()) { // 视频
                String playApi = video.getJSONObject("play_addr").getJSONArray("url_list").get(0).toString();
                douyinDataBean.playApi = playApi;
            } else { // 图片
                List<String> imageList = new ArrayList<>();
                for (Object o1 : images) {
                    JSONObject image = (JSONObject) o1;
                    JSONArray urlList = image.getJSONArray("url_list");
                    imageList.add(urlList.get(urlList.size() - 1).toString());
                }
                douyinDataBean.imageList = imageList;
            }
            dataList.add(douyinDataBean);
        }
        int positionStart = mDataList.size();
        mDataList.addAll(dataList);
        mListRvAdapter.notifyItemRangeInserted(positionStart, dataList.size());
    }
    
    private String getQueryParams(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    result.append("&");
                }
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
    
    
    
    public static DouyinDataBean getDouyinData(Intent intent) {
        DouyinDataBean data = (DouyinDataBean) intent.getSerializableExtra(DATA);
        return data;
    }
}
