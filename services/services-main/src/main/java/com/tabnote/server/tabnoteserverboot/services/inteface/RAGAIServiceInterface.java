package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface RAGAIServiceInterface {

    int ragChat(JSONArray messages, String subject, HttpServletResponse response, StringBuffer returnString, String cdn_ai_ms, String ai_ms_id) throws Exception;
}