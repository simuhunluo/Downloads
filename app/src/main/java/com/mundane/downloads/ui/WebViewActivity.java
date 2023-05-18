package com.mundane.downloads.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.mundane.downloads.R;
import com.mundane.downloads.util.LogUtils;

public class WebViewActivity extends AppCompatActivity {
    
    private WebView mWebView;
    private View mBtnCallJs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        mWebView = findViewById(R.id.webview);
        WebSettings webSettings = mWebView.getSettings();
        //与js交互必须设置
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl("file:///android_asset/test.html");
        
        mBtnCallJs = findViewById(R.id.btn_call_js);
        
        String queryStr =
                "device_platform=webapp&aid=6383&channel=channel_pc_web&sec_user_id=MS4wLjABAAAARLFTwj6HKXBEEbtmsjaFtGcUv_5OxKCeCOhcUQHJ0XU&max_cursor=1660469714000&locate_query=false&show_live_replay_strategy=1&count=10&publish_video_strategy_type=2&pc_client_type=1&version_code=170400&version_name=17.4.0&cookie_enabled=true&screen_width=1920&screen_height=1080&browser_language=zh-CN&browser_platform=Win32&browser_name=Chrome&browser_version=109.0.0.0&browser_online=true&engine_name=Blink&engine_version=109.0.0.0&os_name=Windows&os_version=10&cpu_core_num=8&device_memory=8&platform=PC&downlink=10&effective_type=4g&round_trip_time=50";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36";

        mBtnCallJs.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.evaluateJavascript("javascript:get_xb('" + queryStr + "', '" + userAgent + "')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        //此处为 js 返回的结果
                        LogUtils.d("返回的结果是：" + value);
                    }
                });
            }
        });
    }
}
