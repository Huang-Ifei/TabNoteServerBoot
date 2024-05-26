package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;

public interface MessageServiceInterface {
    JSONObject getTabNoteMessage(String tab_note_id,Integer start);
    JSONObject insertTabNoteMessage(String id, String token,String ip_address,String tab_note_id,String message);

    JSONObject getMessageMessage(String reply_message_id, Integer start);

    JSONObject insertMessageMessage(String id, String token, String ip_address, String reply_message_id, String message, String from_tab_mess);

    JSONObject likeTabMess(String message_id, String id, String token);

    JSONObject likeMessMess(String message_id, String id, String token);
}
