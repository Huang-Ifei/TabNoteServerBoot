package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletResponse;

public interface AiServiceInterface {
    String modelDefine(JSONObject bodyJson);

    JSONObject buildRequestJSON(JSONArray messages, String model);
    //抄送给API
    void postAiMessagesToAPI(JSONObject requestJson, HttpServletResponse response, StringBuffer returnString) throws Exception;

    String createMessages(JSONArray messages, String id, String ip);

    void changeMessages(JSONArray messages, String aiMsId);

    JSONObject getAiMessages(String aiMsId, String token);

    JSONObject getAiMessagesList(String usrId, String token);
}
