package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;

public interface TabNoteServiceInterface {

    JSONObject getClasses();

    JSONObject getPageCount();

    JSONObject getPageTabNotes(int page);

    JSONObject clickTabNote(String tabNoteId, String id, String token);

    JSONObject likeTabNote(String tabNoteId, String id, String token);

    JSONObject getTabNote(String tabNoteId);

    JSONObject insertTabNote(String token, String usr_id, String ip_address, String class_name, String tab_note_name, String tags, String tab_note);

    JSONObject deleteTabNote(String tabNoteId);

    JSONObject updateTabNote(String tab_note_id, String ip_address, String tab_note_name, String tags, String tab_note, String date_time);
}
