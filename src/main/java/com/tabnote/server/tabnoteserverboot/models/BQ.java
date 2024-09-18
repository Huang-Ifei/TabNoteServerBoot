package com.tabnote.server.tabnoteserverboot.models;

public class BQ {
    private String bq_id;
    private String usr_id;
    private String date_time;
    private String img;
    private String text;
    private String ai_answer;
    private String dxstj;

    public String getBq_id() {
        return bq_id;
    }

    public void setBq_id(String bq_id) {
        this.bq_id = bq_id;
    }

    public String getUsr_id() {
        return usr_id;
    }

    public void setUsr_id(String usr_id) {
        this.usr_id = usr_id;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAi_answer() {
        return ai_answer;
    }

    public void setAi_answer(String ai_answer) {
        this.ai_answer = ai_answer;
    }

    public String getDxstj() {
        return dxstj;
    }

    public void setDxstj(String dxstj) {
        this.dxstj = dxstj;
    }
}
