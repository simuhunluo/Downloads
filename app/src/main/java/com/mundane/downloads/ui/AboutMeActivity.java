package com.mundane.downloads.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import com.mundane.downloads.R;
import com.mundane.downloads.util.SaveImageUtils;
import com.mundane.downloads.util.T;

public class AboutMeActivity extends AppCompatActivity {
    
    private ImageView mIvAuthorWx;
    private View mCvReward;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);
        
        mIvAuthorWx = findViewById(R.id.iv_author_wx);
        mCvReward = findViewById(R.id.cv_reward);
        mIvAuthorWx.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                saveImage();
                return true;
            }
        });
        mCvReward.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openReward();
            }
        });
    }
    
    private void openReward() {
        Intent intent = new Intent(this, RewardActivity.class);
        startActivity(intent);
    }
    
    private void saveImage() {
        Bitmap bitmap = ((BitmapDrawable) mIvAuthorWx.getDrawable()).getBitmap();
        if (SaveImageUtils.saveImageToGallery(this, bitmap)) {
            T.show("图片保存成功");
        } else {
            T.show("图片保存失败");
        }
    }
}
