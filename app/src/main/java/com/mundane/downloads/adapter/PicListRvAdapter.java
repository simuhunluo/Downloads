package com.mundane.downloads.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mundane.downloads.R;
import com.mundane.downloads.adapter.PicListRvAdapter.PicListRvAdapterHolder;
import com.mundane.downloads.bean.DouyinPicBean;
import java.util.List;

/**
 * ListRvAdapter
 *
 * @author fangyuan
 * @date 2022-10-05
 */
public class PicListRvAdapter extends RecyclerView.Adapter<PicListRvAdapterHolder> {
    
    private List<DouyinPicBean> mDataList;
    
    public PicListRvAdapter(List<DouyinPicBean> dataList) {
        this.mDataList = dataList;
    }
    
    @NonNull
    @Override
    public PicListRvAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //进行判断显示类型，来创建返回不同的View
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_rv, parent, false);
        return new PicListRvAdapterHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PicListRvAdapterHolder holder, int position) {
        
        DouyinPicBean bean = mDataList.get(position);
        ImageView ivCover = holder.ivCover;
        Glide.with(ivCover.getContext())
                .load(bean.url)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .fitCenter()
                .into(ivCover);
    }
    
    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }
    
    static class PicListRvAdapterHolder extends RecyclerView.ViewHolder {
        
        ImageView ivCover;
        
        public PicListRvAdapterHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
        }
    }
}
