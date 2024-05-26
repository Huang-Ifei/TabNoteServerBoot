package com.tabnote.server.tabnoteserverboot.models;

public class TabNoteMessage {
    private String message_id;
    private String usr_id;
    private String ip_address;
    private String tab_note_id;
    private String message;
    private String date_time;

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
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

    public String getTab_note_id() {
        return tab_note_id;
    }

    public void setTab_note_id(String tab_note_id) {
        this.tab_note_id = tab_note_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

}
