package com.mundane.downloads.bean;

import java.io.Serializable;

/**
 * DouyinDataBean
 *
 * @author fangyuan
 * @date 2022-10-05
 */
public class DouyinDataBean implements Serializable {
    
    // 封面的图片地址
    public String coverUrl;
    
    public String awemeId;
    
    public Integer awemeType;
    
    @Override
    public String toString() {
        return "DouyinDataBean{" + "coverUrl='" + coverUrl + '\'' + ", awemeId='" + awemeId + '\'' + ", awemeType=" + awemeType + '}';
    }
}
