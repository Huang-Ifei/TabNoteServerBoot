package com.tabnote.server.tabnoteserverboot.models;

import com.alibaba.fastjson2.JSONObject;

public class TabNote {

    //由usr_id的hash code和date_time组成的tab_note_id
    private String tab_note_id;
    private String usr_id;
    private String ip_address;
    private String class_name;
    private String tab_note_name;
    private String tags;
    private String tab_note;
    private String date_time;
    private Integer click;
    private String file;
    private String images;
    private Integer display;

    public String getFile() {
        if (file==null){
            return "";
        }
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public JSONObject getImages() {
        if (images==null|| images.isEmpty()){
            JSONObject json = new JSONObject();
            JSONObject imgJson = new JSONObject();
            imgJson.putArray("images");
            return json;
        }
        return JSONObject.parseObject(images);
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getClick() {
        return click;
    }

    public void setClick(Integer click) {
        this.click = click;
    }

    public String getTab_note_id() {
        return tab_note_id;
    }

    public void setTab_note_id(String tab_note_id) {
        this.tab_note_id = tab_note_id;
    }

    public String getUsr_id() {
        return usr_id;
    }

    public void setUsr_id(String usr_id) {
        this.usr_id = usr_id;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getClass_name() {
        return class_name;
    }

    public void setClass_name(String class_name) {
        this.class_name = class_name;
    }

    public String getTab_note_name() {
        return tab_note_name;
    }

    public void setTab_note_name(String tab_note_name) {
        this.tab_note_name = tab_note_name;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getTab_note() {
        return tab_note;
    }

    public void setTab_note(String tab_note) {
        this.tab_note = tab_note;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public int getDisplay() {
        if (display==null){
            return 0;
        }
        return display;
    }

    public void setDisplay(int display) {
        this.display = display;
    }
}
