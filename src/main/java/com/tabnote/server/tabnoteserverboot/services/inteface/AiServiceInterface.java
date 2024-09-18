package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.models.BQ;
import jakarta.servlet.http.HttpServletResponse;

public interface AiServiceInterface {
    String modelDefine(JSONObject bodyJson);

    JSONObject buildRequestJSON(JSONArray messages, String model);
    //抄送给API
    void postAiMessagesToAPI(JSONObject requestJson, HttpServletResponse response, StringBuffer returnString) throws Exception;

    //将请求JSON变为向ChatGPT API发送的JSON
    JSONObject buildChatGPTRequestJSON(JSONArray messages, String model);

    //抄送给API
    void postAiMessagesToChatGPTAPI(JSONObject requestJson, HttpServletResponse response, StringBuffer returnString) throws Exception;

    String createMessages(JSONArray messages, String id, String ip);

    void changeMessages(JSONArray messages, String aiMsId);

    JSONObject getAiMessages(String aiMsId, String token);

    JSONObject getAiMessagesList(String usrId, String token);

    JSONObject noteAiSync(String note_ai_id, String note, JSONArray note_ticks, String token, String usrId,String note_content);

    JSONObject getNoteAiHistory(String usrId, String token);

    JSONObject getHistoryNoteAi(String noteAiId, String token);

    JSONObject getDXSTJ(String id, String token, String base64Img);

    JSONObject insertBQ(BQ beatQuestion, String usrId, String token);

    JSONObject getBQListByUserId(String usrId, String token);

    JSONObject getBQ(String bqId, String usrId, String token);
}
