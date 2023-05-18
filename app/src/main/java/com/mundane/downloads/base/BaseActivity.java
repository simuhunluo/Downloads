package com.mundane.downloads.base;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import com.mundane.downloads.exception.MyException;
import com.mundane.downloads.ui.dialog.ProgressDialogFragment;
import com.mundane.downloads.util.RefreshUtil;
import com.mundane.downloads.util.RegexUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

/**
 * BaseActivity
 *
 * @author fangyuan
 * @date 2023-05-18
 */
public class BaseActivity extends AppCompatActivity {
    
    public ProgressDialogFragment mProgressDialogFragment;
    
    
    public void hideDialog() {
        if (mProgressDialogFragment != null) {
            mProgressDialogFragment.dismiss();
        }
        mProgressDialogFragment = null;
    }
    
    public void updateProgress(int progress) {
        if (mProgressDialogFragment == null) {
            createAndShowDialog();
        }
        mProgressDialogFragment.updateProgress(progress);
    }
    
    public void createAndShowDialog() {
        mProgressDialogFragment = ProgressDialogFragment.newInstance();
        mProgressDialogFragment.setCancelable(false);
        mProgressDialogFragment.show(getSupportFragmentManager(), "");
    }
    
    public void downloadPic(String title, int count, String picUrl) throws MyException {
        try {
            // 将匹配到的非法字符以空替换
            title = RegexUtil.replaceTitle(title);
            Connection.Response document = Jsoup.connect(picUrl).ignoreContentType(true).maxBodySize(0).timeout(0).execute();
            BufferedInputStream inputStream = document.bodyStream();
            File appDir = new File(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera" + File.separator);
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            File fileSavePath = new File(appDir, title + "_" + count + ".jpeg");
            // 如果保存文件夹不存在,那么则创建该文件夹
            File fileParent = fileSavePath.getParentFile();
            if (!fileParent.exists()) {
                fileParent.mkdirs();
            }
            if (fileSavePath.exists()) { //如果文件存在，则删除原来的文件
                fileSavePath.delete();
            }
            FileOutputStream fs = new FileOutputStream(fileSavePath);
            byte[] buffer = new byte[8 * 1024];
            int byteRead;
            while ((byteRead = inputStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteRead);
            }
            inputStream.close();
            fs.close();
            RefreshUtil.scanFile(this, fileSavePath.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new MyException(e.getMessage());
        }
    }
}
