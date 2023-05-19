package com.mundane.downloads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import com.mundane.downloads.R;

public class AboutMeActivity extends AppCompatActivity {
    
    private View mCvReward;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);
        
        mCvReward = findViewById(R.id.cv_reward);
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
    
}
