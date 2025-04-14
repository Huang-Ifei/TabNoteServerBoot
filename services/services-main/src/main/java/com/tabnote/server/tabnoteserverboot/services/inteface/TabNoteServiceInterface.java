package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.transaction.annotation.Transactional;

public interface TabNoteServiceInterface {

    JSONObject getClasses();

    //获取推荐标签
    @Transactional(noRollbackFor = NullPointerException.class)
    JSONObject tagsRecommended(String id);

    JSONObject getPageCount();

    JSONObject getPageTabNotes(int page);

    JSONObject likeTabNote(String tabNoteId, String id, String token);

    JSONObject getTabNote(String tabNoteId,String id,String token);

    JSONObject insertTabNote(String token, String usr_id, String ip_address, String class_name, String tab_note_name, String tags, String tab_note, String base64FileString, JSONArray imgs,int display);

    JSONObject deleteTabNote(String tabNoteId);

    JSONObject updateTabNote(String tab_note_id, String ip_address, String tab_note_name, String tags, String tab_note, String date_time);

    JSONObject searchTabNote(String key, Integer page);

    JSONObject searchTabNoteWithCls(String className, String key, Integer page);

    JSONObject searchTabNoteById(String id, Integer page);

    JSONObject searchTabNoteByClass(String className, Integer page);
}
