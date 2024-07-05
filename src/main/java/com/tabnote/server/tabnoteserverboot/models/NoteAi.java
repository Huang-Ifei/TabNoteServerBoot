package com.tabnote.server.tabnoteserverboot.models;

public class NoteAi {
    private String note_ai_id;
    private String usr_id;
    private String mainly;
    private String note;
    private String ai_mess;
    private String date_time;

    public String getNote_ai_id() {
        return note_ai_id;
    }

    public void setNote_ai_id(String note_ai_id) {
        this.note_ai_id = note_ai_id;
    }

    public String getUsr_id() {
        return usr_id;
    }

    public void setUsr_id(String usr_id) {
        this.usr_id = usr_id;
    }

    public String getMainly() {
        return mainly;
    }

    public void setMainly(String mainly) {
        this.mainly = mainly;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getAi_mess() {
        return ai_mess;
    }

    public void setAi_mess(String ai_mess) {
        this.ai_mess = ai_mess;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }
}
