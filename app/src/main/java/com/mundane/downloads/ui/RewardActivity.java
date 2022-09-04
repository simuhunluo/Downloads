package com.mundane.downloads.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import com.mundane.downloads.R;
import com.mundane.downloads.util.SaveImageUtils;
import com.mundane.downloads.util.T;

public class RewardActivity extends AppCompatActivity {
    
    private ImageView mIvRewardWx;
    private ImageView mIvRewardAlipay;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);
        
        mIvRewardWx = findViewById(R.id.iv_reward_wx);
        mIvRewardAlipay = findViewById(R.id.iv_reward_alipay);
        mIvRewardWx.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveImage(mIvRewardWx);
                return true;
            }
        });
        mIvRewardAlipay.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveImage(mIvRewardAlipay);
                return true;
            }
        });
    }
    
    private void saveImage(ImageView imageView) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        if (SaveImageUtils.saveImageToGallery(this, bitmap)) {
            T.show("图片保存成功");
        } else {
            T.show("图片保存失败");
        }
    }
}
