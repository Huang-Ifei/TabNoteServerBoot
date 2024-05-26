package com.tabnote.server.tabnoteserverboot.models;

public class TabNoteForList {
    private String tab_note_id;
    private String usr_id;
    private String class_name;
    private String tab_note_name;
    private String tags;
    private String date_time;
    private Integer click;

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

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }
}
