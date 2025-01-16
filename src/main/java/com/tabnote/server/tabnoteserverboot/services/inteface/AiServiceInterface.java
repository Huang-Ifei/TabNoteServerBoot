package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.models.BQ;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

public interface AiServiceInterface {

    //将请求JSON变为向ChatGPT API发送的JSON
    JSONObject buildChatGPTRequestJSON(JSONArray messages, String model);

    //抄送给API
    int postAiMessagesToChatGPTAPI(JSONObject requestJson, HttpServletResponse response, StringBuffer returnString) throws Exception;

    String createMessages(JSONArray messages, String id, String ip);

    void changeMessages(JSONArray messages, String aiMsId);

    JSONObject getAiMessages(String aiMsId, String token);

    JSONObject getAiMessagesList(String usrId, String token);

    JSONObject noteAiSync(String note_ai_id, String note, JSONArray note_ticks, String token, String usrId,String note_content);

    JSONObject getNoteAiHistory(String usrId, String token);

    JSONObject getHistoryNoteAi(String noteAiId, String token);

    JSONObject getDXSTJ(String id, String token, String base64Img);

    JSONObject insertBQWithOutTokenCheck(BQ beatQuestion);

    JSONObject insertBQ(BQ beatQuestion, String usrId, String token);

    JSONObject getBQListByUserId(String usrId, String token,int index);

    JSONObject getBQ(String bqId, String usrId, String token);

    void returnAdminMess(HttpServletResponse response,String s)throws IOException;

    void returnErrMess(HttpServletResponse response, String e)throws Exception;

    @Transactional
    void useQuota(int quotaCost, String id);
}
