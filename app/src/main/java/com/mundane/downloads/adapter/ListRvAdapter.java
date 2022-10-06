package com.mundane.downloads.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.mundane.downloads.R;
import com.mundane.downloads.bean.DouyinDataBean;
import java.util.List;

/**
 * ListRvAdapter
 *
 * @author fangyuan
 * @date 2022-10-05
 */
public class ListRvAdapter extends RecyclerView.Adapter<ListRvAdapter.ListRvAdapterHolder> {
    
    private List<DouyinDataBean> mDataList;
    
    public ListRvAdapter(List<DouyinDataBean> dataList) {
        this.mDataList = dataList;
    }
    
    public interface OnItemClickListener{
        void onItemClicked(int position, DouyinDataBean bean);
    }
    
    private OnItemClickListener mOnItemClickListener;
    
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
    
    @NonNull
    @Override
    public ListRvAdapterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_rv, parent, false);
        return new ListRvAdapterHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ListRvAdapterHolder holder, int position) {
        DouyinDataBean bean = mDataList.get(position);
        ImageView ivCover = holder.ivCover;
        Glide.with(ivCover.getContext()).load(bean.coverUrl).fitCenter().into(ivCover);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(position, mDataList.get(position));
                }
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }
    
    static class ListRvAdapterHolder extends RecyclerView.ViewHolder {
        
        ImageView ivCover;
        
        public ListRvAdapterHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
        }
    }
}
