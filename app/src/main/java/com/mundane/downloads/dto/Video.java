package com.mundane.downloads.dto;

/**
 * Video
 *
 * @author fangyuan
 * @date 2022-08-28
 */
public class Video {
    private String videoAddress;
    
    private String desc;
    
    private Integer height;
    
    private Integer width;
    
    private String videoId;
    
    public String getVideoId() {
        return videoId;
    }
    
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
    
    public Integer getHeight() {
        return height;
    }
    
    public void setHeight(Integer height) {
        this.height = height;
    }
    
    public Integer getWidth() {
        return width;
    }
    
    public void setWidth(Integer width) {
        this.width = width;
    }
    
    public String getVideoAddress() {
        return videoAddress;
    }
    
    public void setVideoAddress(String videoAddress) {
        this.videoAddress = videoAddress;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    @Override
    public String toString() {
        return "Video{" + "videoAddress='" + videoAddress + '\'' + ", desc='" + desc + '\'' + ", height=" + height + ", width=" + width + '}';
    }
}
