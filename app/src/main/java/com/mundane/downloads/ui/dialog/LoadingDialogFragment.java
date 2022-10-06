package com.mundane.downloads.ui.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mundane.downloads.R;

/**
 * Created by mundane on 2018/11/14 下午5:36
 */
public class LoadingDialogFragment extends DialogFragment {
    
    private ProgressBar mPb;
    private TextView mTv;
    
    public static LoadingDialogFragment newInstance() {
        LoadingDialogFragment dialogFragment = new LoadingDialogFragment();
        return dialogFragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Dialog_FullScreen);
    }
    
    @Override
    public void onStart() {
        super.onStart();
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_loading, container, false);
        return rootView;
    }
    
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPb = view.findViewById(R.id.pb);
    }
}
