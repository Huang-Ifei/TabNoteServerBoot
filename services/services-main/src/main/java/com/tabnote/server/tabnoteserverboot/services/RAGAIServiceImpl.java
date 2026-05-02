package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.CDNAI;
import com.tabnote.server.tabnoteserverboot.component.TabNoteMixGateway;
import com.tabnote.server.tabnoteserverboot.define.AiInfo;
import com.tabnote.server.tabnoteserverboot.define.AiSysPrompt;
import com.tabnote.server.tabnoteserverboot.services.inteface.RAGAIServiceInterface;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.tabnote.server.tabnoteserverboot.define.AiInfo.*;

@Service
public class RAGAIServiceImpl implements RAGAIServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(RAGAIServiceImpl.class);

    private RagService ragService;
    private TabNoteMixGateway tabNoteMixGateway;

    @Autowired
    public void setRagService(RagService ragService) {
        this.ragService = ragService;
    }

    @Autowired
    public void setTabNoteMixGateway(TabNoteMixGateway tabNoteMixGateway) {
        this.tabNoteMixGateway = tabNoteMixGateway;
    }

    @Autowired
    CDNAI cdnai;

    @Override
    public int ragChat(JSONArray messages, String subject, HttpServletResponse response, StringBuffer returnString, String cdn_ai_ms, String ai_ms_id) throws Exception {
        // 获取用户最后一次请求
        String lastUserMessage = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            JSONObject msg = messages.getJSONObject(i);
            if ("user".equals(msg.getString("role"))) {
                lastUserMessage = msg.getString("content");
                break;
            }
        }

        // 调用RAG搜索获取参考信息
        JSONObject ragResult = ragService.searchContent(subject, lastUserMessage, 10, 0);
        StringBuffer ragData = new StringBuffer();
        if (ragResult.getString("message") != null && ragResult.getString("message").equals("success")) {
            JSONArray data = ragResult.getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject item = data.getJSONObject(i);
                ragData.append(item.getJSONObject("entity").getString("text")).append("\n\n");
            }
        }

        // 构建JSON格式的用户请求，替换最后一次用户消息
        JSONObject userContentJson = new JSONObject();
        userContentJson.put(AiSysPrompt.ragDialoguePrompt1, ragData.toString());
        userContentJson.put(AiSysPrompt.ragDialoguePrompt2, lastUserMessage);

        // 构建发送给AI的messages
        JSONArray aiMessages = new JSONArray();
        for (int i = 0; i < messages.size(); i++) {
            JSONObject msg = messages.getJSONObject(i);
            JSONObject aiMsg = new JSONObject();
            String role = msg.getString("role");
            if ("model".equals(role)) {
                aiMsg.put("role", "assistant");
            } else {
                aiMsg.put("role", role);
            }
            // 最后一轮用户消息替换为JSON格式
            if ("user".equals(role) && i == getLastUserMessageIndex(messages)) {
                aiMsg.put("content", userContentJson.toJSONString());
            } else {
                aiMsg.put("content", msg.get("content"));
            }
            aiMessages.add(aiMsg);
        }

        // 构建请求JSON，使用basicPrompt作为系统提示词，modelList[11]作为模型
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", modelList[11]);
        requestJson.put("stream", true);
        requestJson.put("include_usage", true);
        requestJson.put("messages", aiMessages);

        // 在最前面插入system消息
        JSONArray finalMessages = new JSONArray();
        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", AiSysPrompt.basicPrompt);
        finalMessages.add(sysMsg);
        finalMessages.addAll(aiMessages);
        requestJson.put("messages", finalMessages);

        // 发送请求到DeepSeek API
        return postToDeepSeek(requestJson, response, returnString, cdn_ai_ms, ai_ms_id);
    }

    private int getLastUserMessageIndex(JSONArray messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.getJSONObject(i).getString("role"))) {
                return i;
            }
        }
        return -1;
    }

    private int postToDeepSeek(JSONObject requestJson, HttpServletResponse response, StringBuffer returnString, String ca_id, String ai_ms_id) throws Exception {
        String url = "https://api.siliconflow.cn/v1/chat/completions";

        String gateWayUrl = tabNoteMixGateway.getGateWayHost();
        if (!gateWayUrl.isEmpty()) {
            url = gateWayUrl + "/api_get";
        }

        URL uRL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) uRL.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(300000);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        connection.setRequestProperty("Authorization", "Bearer " + siliconFlowDeepSeek_API_KEY);

        OutputStream os = connection.getOutputStream();
        log.info("RAG request to deepseek: {}", requestJson.toString());
        os.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        if (connection.getResponseCode() == 200) {
            if (response != null) {
                response.addHeader("content-type", "application/json;charset=utf-8");
            }

            InputStream is = connection.getInputStream();
            if (is != null) {
                int quotaCost = 0;
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String temp;
                while ((temp = br.readLine()) != null) {
                    if (!temp.equals("\n") && !temp.isEmpty()) {
                        if (temp.equals("[DONE]") || temp.equals("data: [DONE]")) {
                            break;
                        }

                        JSONObject tempJSON;
                        if (temp.startsWith("data: ")) {
                            tempJSON = JSONObject.parseObject(temp.substring(6));
                        } else {
                            tempJSON = JSONObject.parseObject(temp);
                        }

                        if (tempJSON != null) {
                            try {
                                JSONArray choices = tempJSON.getJSONArray("choices");
                                if (choices.isEmpty()) {
                                    quotaCost = countQuota(tempJSON, requestJson);
                                } else {
                                    String returnMess = choices.getJSONObject(0).getJSONObject("delta").getString("content");
                                    String thinkingMess = choices.getJSONObject(0).getJSONObject("delta").getString("reasoning_content");
                                    if (returnMess != null || thinkingMess != null) {
                                        JSONObject returnJSON = new JSONObject();
                                        JSONObject returnMessage = new JSONObject();
                                        returnJSON.put("model", requestJson.getString("model"));
                                        if (returnMess != null) {
                                            returnMessage.put("content", returnMess);
                                            returnString.append(returnMess);
                                        }
                                        if (thinkingMess != null) {
                                            returnMessage.put("reasoning_content", thinkingMess);
                                        }
                                        returnJSON.put("message", returnMessage);
                                        quotaCost = countQuota(tempJSON, requestJson);
                                        if (response != null) {
                                            writeAll(returnJSON, ca_id, response, ai_ms_id);
                                        }
                                    }
                                }
                            } catch (NullPointerException e) {
                                log.error(e.getMessage());
                                break;
                            }
                        }
                    }
                }
                if (response != null) {
                    writeAll(null, ca_id, response, ai_ms_id);
                }
                br.close();
                return quotaCost;
            }
        } else {
            returnString.delete(0, returnString.length());
            log.error("RAG deepseek err: {}", connection.getResponseCode());
            JSONObject returnJSON = new JSONObject();
            JSONObject returnMessage = new JSONObject();
            returnMessage.put("content", "failed" + connection.getResponseCode());
            returnJSON.put("model", requestJson.getString("model"));
            returnJSON.put("message", returnMessage);
            if (response != null) {
                writeAll(returnJSON, ca_id, response, ai_ms_id);
            }
        }
        return 0;
    }

    public void writeAll(JSONObject modelMessageJSON, String ca_id, HttpServletResponse response, String ai_ms_id){

        if (modelMessageJSON == null) {
            modelMessageJSON = new JSONObject();
            modelMessageJSON.put("ai_ms_id", ai_ms_id);
            modelMessageJSON.put("cdn_ai_id", ca_id);
            modelMessageJSON.put("model", null);
            modelMessageJSON.put("message", null);
            modelMessageJSON.put("response","success");
        }else{
            modelMessageJSON.put("response","stream");
            modelMessageJSON.put("ai_ms_id", ai_ms_id);
            modelMessageJSON.put("cdn_ai_id", ca_id);
        }

        write(modelMessageJSON.toString(), ca_id, response);
    }

    public void write(String s, String ca_id, HttpServletResponse response){
        try{
            response.getWriter().write(s);
            response.getWriter().write("\n");
            response.getWriter().flush();
        }catch(Exception e) {
            log.error(e.getMessage());
        }finally{
            cdnai.sendToTACADS(ca_id,s);
        }
    }

    private int countQuota(JSONObject tempJSON, JSONObject requestJson) {
        int quotaCost = 0;
        if (tempJSON.containsKey("usage") && tempJSON.get("usage") != null) {
            quotaCost = tempJSON.getJSONObject("usage").getInteger("prompt_tokens")
                    + tempJSON.getJSONObject("usage").getInteger("completion_tokens") * 4;
        }
        return quotaCost;
    }
}