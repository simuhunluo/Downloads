package com.mundane.downloads.bean;

import java.io.Serializable;
import java.util.List;

/**
 * DouyinDataBean
 *
 * @author fangyuan
 * @date 2022-10-05
 */
public class DouyinDataBean implements Serializable {
    // 布局类型
    public int type;
    
    // 封面的图片地址
    public String coverUrl;
    
    // 0: 视频，68: 图片
    public Integer awemeType;
    
    // 无水印图片地址
    public List<String> imageList;
    
    // 无水印视频地址
    public String playApi;
    
    public String desc;
    
    
}
