package com.mundane.downloads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.mundane.downloads.R;
import com.mundane.downloads.adapter.ListRvAdapter;
import com.mundane.downloads.adapter.ListRvAdapter.OnItemClickListener;
import com.mundane.downloads.adapter.itemdecoration.ListRvItemDecoration;
import com.mundane.downloads.bean.DouyinDataBean;
import com.mundane.downloads.util.T;
import java.util.ArrayList;
import java.util.List;

public class JsonListActivity extends AppCompatActivity {
    
    private RecyclerView mRv;
    private ListRvAdapter mListRvAdapter;
    
    private static final String DATA = "DATA";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_list);
        mRv = findViewById(R.id.rv);
        
        String jsonStr = PatchDownloadActivity.getJsonInfoFromIntent(getIntent());
        if (jsonStr == null) {
            T.show("jsonStr is null");
            return;
        }
        JSONObject json = new JSONObject(jsonStr);
        JSONArray awemeList = json.getJSONArray("aweme_list");
        List<DouyinDataBean> dataList = new ArrayList<>();
        for (Object o : awemeList) {
            JSONObject jsonObject = (JSONObject) o;
            JSONArray jsonArray = jsonObject.getJSONObject("video").getJSONObject("cover").getJSONArray("url_list");
            String coverUrl = jsonArray.get(0).toString();
            String awemeId = jsonObject.getStr("aweme_id");
            Integer awemeType = jsonObject.getInt("aweme_type");
    
            DouyinDataBean douyinDataBean = new DouyinDataBean();
            douyinDataBean.coverUrl = coverUrl;
            douyinDataBean.awemeId = awemeId;
            douyinDataBean.awemeType = awemeType;
            dataList.add(douyinDataBean);
        }
        mRv.setLayoutManager(new GridLayoutManager(this, 3));
        mListRvAdapter = new ListRvAdapter(dataList);
        mListRvAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(int position, DouyinDataBean bean) {
                openPlayVideo(bean);
            }
        });
        mRv.setAdapter(mListRvAdapter);
        mRv.addItemDecoration(new ListRvItemDecoration(this));
    
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
