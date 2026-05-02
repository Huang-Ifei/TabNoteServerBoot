package com.tabnote.server.tabnoteserverboot.models;

public class RagAiMessagesForList {
    private String rag_ms_id;
    private String book_id;
    private String mainly;
    private String date_time;

    public String getRag_ms_id() { return rag_ms_id; }
    public void setRag_ms_id(String rag_ms_id) { this.rag_ms_id = rag_ms_id; }
    public String getBook_id() { return book_id; }
    public void setBook_id(String book_id) { this.book_id = book_id; }
    public String getMainly() { return mainly; }
    public void setMainly(String mainly) { this.mainly = mainly; }
    public String getDate_time() { return date_time; }
    public void setDate_time(String date_time) { this.date_time = date_time; }
}