package com.tabnote.server.tabnoteserverboot.models;

public class MessageMessage {
    private String message_id;
    private String usr_id;
    private String ip_address;
    private String reply_message_id;
    private String message;
    private String date_time;
    private String from_tab_mess;

    public String getFrom_tab_mess() {
        return from_tab_mess;
    }

    public void setFrom_tab_mess(String from_tab_mess) {
        this.from_tab_mess = from_tab_mess;
    }

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

    public String getReply_message_id() {
        return reply_message_id;
    }

    public void setReply_message_id(String reply_message_id) {
        this.reply_message_id = reply_message_id;
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
