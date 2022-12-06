package com.mundane.downloads.dto;

import java.util.List;

/**
 * Pic
 *
 * @author fangyuan
 * @date 2022-12-06
 */
public class Pic {
    
    public Pic() {
    }
    
    public Pic(List<String> picList, String desc) {
        this.picList = picList;
        this.desc = desc;
    }
    
    private List<String> picList;
    
    private String desc;
    
    public List<String> getPicList() {
        return picList;
    }
    
    public void setPicList(List<String> picList) {
        this.picList = picList;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
