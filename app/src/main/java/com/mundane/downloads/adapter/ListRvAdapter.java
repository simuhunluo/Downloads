package com.mundane.downloads.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mundane.downloads.R;
import com.mundane.downloads.bean.DouyinDataBean;
import java.util.List;

/**
 * ListRvAdapter
 *
 * @author fangyuan
 * @date 2022-10-05
 */
public class ListRvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    // 普通布局
    public static final int TYPE_ITEM = 1;
    // 脚布局
    public static final int TYPE_FOOTER = 2;
    
    private List<DouyinDataBean> mDataList;
    
    public ListRvAdapter(List<DouyinDataBean> dataList) {
        this.mDataList = dataList;
    }
    
    public interface OnItemClickListener {
        void onItemClicked(int position, DouyinDataBean bean);
    }
    
    private OnItemClickListener mOnItemClickListener;
    
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
    
    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).type;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //进行判断显示类型，来创建返回不同的View
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_rv, parent, false);
            return new ListRvAdapterHolder(view);
        } else if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_refresh_footer, parent, false);
            return new FootViewHolder(view);
        }
        return null;
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        
        if (holder instanceof ListRvAdapterHolder) {
            ListRvAdapterHolder itemHolder = (ListRvAdapterHolder) holder;
            DouyinDataBean bean = mDataList.get(position);
            ImageView ivCover = itemHolder.ivCover;
            Glide.with(ivCover.getContext())
                    .load(bean.coverUrl)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .fitCenter()
                    .into(ivCover);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClicked(position, mDataList.get(position));
                    }
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }
    
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    // 如果当前是footer的位置，那么该item占据2个单元格，正常情况下占据1个单元格
                    return getItemViewType(position) == TYPE_FOOTER ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }
    
    public void addLoadItem() {
        if (mDataList.size() > 0 && mDataList.get(mDataList.size() - 1).type == TYPE_FOOTER) {
            return;
        }
        DouyinDataBean footItem = new DouyinDataBean();
        footItem.type = TYPE_FOOTER;
        mDataList.add(footItem);
        notifyItemInserted(mDataList.size());
    }
    
    public void removeLoadItem() {
        if (mDataList.size() == 0) {
            return;
        }
        if (mDataList.get(mDataList.size() - 1).type != TYPE_FOOTER) {
            return;
        }
        mDataList.remove(mDataList.size() - 1);
    }
    
    static class ListRvAdapterHolder extends RecyclerView.ViewHolder {
        
        ImageView ivCover;
        
        public ListRvAdapterHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
        }
    }
    
    static class FootViewHolder extends RecyclerView.ViewHolder {
        
        ProgressBar pbLoading;
        TextView tvLoading;
        LinearLayout llEnd;
        
        FootViewHolder(View itemView) {
            super(itemView);
            pbLoading = itemView.findViewById(R.id.pb_loading);
            tvLoading = itemView.findViewById(R.id.tv_loading);
            llEnd = itemView.findViewById(R.id.ll_end);
        }
    }
    
}
