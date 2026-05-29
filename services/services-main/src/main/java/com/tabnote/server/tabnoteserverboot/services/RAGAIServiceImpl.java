package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.define.AiSysPrompt;
import com.tabnote.server.tabnoteserverboot.mappers.BookMapper;
import com.tabnote.server.tabnoteserverboot.models.Book;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import com.tabnote.server.tabnoteserverboot.services.inteface.RAGAIServiceInterface;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.tabnote.server.tabnoteserverboot.define.AiInfo.modelList;

@Service
public class RAGAIServiceImpl implements RAGAIServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(RAGAIServiceImpl.class);

    private RagService ragService;
    private AiServiceInterface aiService;
    private BookMapper bookMapper;

    @Autowired
    public void setRagService(RagService ragService) {
        this.ragService = ragService;
    }

    @Autowired
    public void setAiService(AiServiceInterface aiService) {
        this.aiService = aiService;
    }

    @Autowired
    public void setBookMapper(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    @Override
    public int ragChat(JSONArray messages, String subject, HttpServletResponse response, StringBuffer returnString, String cdn_ai_ms, String ai_ms_id) throws Exception {
        String lastUserMessage = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            JSONObject msg = messages.getJSONObject(i);
            if ("user".equals(msg.getString("role"))) {
                lastUserMessage = msg.getString("content");
                break;
            }
        }

        writeReasoning("开始读取用户需求...", cdn_ai_ms, response, ai_ms_id);

        List<String> ragWords = generateRagWords(subject, messages);
        if (ragWords.isEmpty()) {
            ragWords.add(lastUserMessage);
            writeReasoning("开始进行数据检索...", cdn_ai_ms, response, ai_ms_id);
        } else {
            writeReasoning("生成数据检索关键词：" + JSONObject.toJSONString(ragWords), cdn_ai_ms, response, ai_ms_id);
        }

        JSONObject ragResult = ragService.searchContentBatch(subject, ragWords, 5, 0.35);
        System.out.println(ragResult);
        StringBuffer ragData = new StringBuffer();
        if (ragResult.getString("message") != null && ragResult.getString("message").equals("success")) {

            writeReasoning("RAG数据库查找成功：",cdn_ai_ms,response,ai_ms_id);

            JSONArray data = ragResult.getJSONArray("data");
            if (data != null) {
                java.util.Set<String> seenIds = new java.util.HashSet<>();
                for (int i = 0; i < data.size(); i++) {
                    JSONArray batch = data.getJSONArray(i);
                    if (batch != null) {
                        for (int j = 0; j < batch.size(); j++) {
                            JSONObject item = batch.getJSONObject(j);
                            String id = item.getString("id");
                            if (id != null && seenIds.add(id)) {
                                ragData.append(item.getJSONObject("entity").getString("text")).append("\n\n");
                                writeReasoning(item.getJSONObject("entity").getString("text")+"\n\n", cdn_ai_ms, response, ai_ms_id);
                            }
                        }
                    }
                }
            }
        }

        JSONObject userContentJson = new JSONObject();
        userContentJson.put(AiSysPrompt.ragDialoguePrompt1, ragData.toString());
        userContentJson.put(AiSysPrompt.ragDialoguePrompt2, lastUserMessage);

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
            if ("user".equals(role) && i == getLastUserMessageIndex(messages)) {
                aiMsg.put("content", userContentJson.toJSONString());
            } else {
                aiMsg.put("content", msg.get("content"));
            }
            aiMessages.add(aiMsg);
        }

        JSONObject requestJson = new JSONObject();
        requestJson.put("model", modelList[14]);
        requestJson.put("stream", true);
        requestJson.put("include_usage", true);
        requestJson.put("messages", aiMessages);

        JSONArray finalMessages = new JSONArray();
        JSONObject sysMsg = new JSONObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", AiSysPrompt.basicPrompt);
        finalMessages.add(sysMsg);
        finalMessages.addAll(aiMessages);
        requestJson.put("messages", finalMessages);

        return aiService.postAiMessagesToDeepSeekAPI(requestJson, response, returnString, cdn_ai_ms, ai_ms_id);
    }

    private List<String> generateRagWords(String subject, JSONArray messages) {
        try {
            String bookName = subject;
            Book book = bookMapper.getBookById(subject);
            if (book != null && book.getBook_name() != null) {
                bookName = book.getBook_name();
            }

            String sysPrompt = AiSysPrompt.ragWordSysPrompt.replace("${bookName}", bookName);

            StringBuilder userContext = new StringBuilder();
            for (int i = 0; i < messages.size(); i++) {
                JSONObject msg = messages.getJSONObject(i);
                String role = msg.getString("role");
                String content = msg.getString("content");
                userContext.append(role).append(": ").append(content).append("\n");
            }

            JSONArray aiMessages = new JSONArray();
            JSONObject sysMsg = new JSONObject();
            sysMsg.put("role", "system");
            sysMsg.put("content", sysPrompt);
            aiMessages.add(sysMsg);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userContext.toString());
            aiMessages.add(userMsg);

            JSONObject requestJson = new JSONObject();
            requestJson.put("model", modelList[14]);
            requestJson.put("stream", true);
            requestJson.put("include_usage", true);
            requestJson.put("messages", aiMessages);

            StringBuffer sb = new StringBuffer();
            aiService.postAiMessagesToDeepSeekAPI(requestJson, null, sb, "", "");
            String aiResponse = sb.toString().trim();
            if (aiResponse.isEmpty()) {
                return new ArrayList<>();
            }

            JSONObject result = JSONObject.parseObject(aiResponse);
            if (result != null && result.containsKey("ragWord")) {
                JSONArray ragWordArray = result.getJSONArray("ragWord");
                List<String> ragWords = new ArrayList<>();
                if (ragWordArray != null) {
                    for (int i = 0; i < ragWordArray.size(); i++) {
                        String word = ragWordArray.getString(i);
                        if (word != null && !word.trim().isEmpty()) {
                            ragWords.add(word.trim());
                        }
                    }
                }
                return ragWords;
            }
        } catch (Exception e) {
            log.error("Generate RAG words failed: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private void writeReasoning(String reasonString, String ca_id, HttpServletResponse response, String ai_ms_id) {
        try {
            JSONObject returnJSON = new JSONObject();
            JSONObject returnMessage = new JSONObject();
            returnJSON.put("model", "TabNoteSmartRAGThinking");
            returnMessage.put("reasoning_content", reasonString + "\n");
            returnJSON.put("message", returnMessage);
            returnJSON.put("response", "stream");
            returnJSON.put("ai_ms_id", ai_ms_id);
            returnJSON.put("cdn_ai_id", ca_id);
            aiService.write(returnJSON.toString(), ca_id, response);
        } catch (Exception e) {
        log.error("回写思考链出错:"+e.getMessage());
        }
    }

    private int getLastUserMessageIndex(JSONArray messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.getJSONObject(i).getString("role"))) {
                return i;
            }
        }
        return -1;
    }
}
