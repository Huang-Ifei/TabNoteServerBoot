package com.tabnote.server.tabnoteserverboot.models;

import org.apache.ibatis.annotations.Param;

public class AiMessages {
    String ai_ms_id ;
    String mainly;
    String usr_id;
    String contents;
    String date_time;

    public String getAi_ms_id() {
        return ai_ms_id;
    }

    public String getMainly() {
        return mainly;
    }

    public String getUsr_id() {
        return usr_id;
    }

    public String getContents() {
        return contents;
    }

    public void setAi_ms_id(String ai_ms_id) {
        this.ai_ms_id = ai_ms_id;
    }

    public void setMainly(String mainly) {
        this.mainly = mainly;
    }

    public void setUsr_id(String usr_id) {
        this.usr_id = usr_id;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }
}
