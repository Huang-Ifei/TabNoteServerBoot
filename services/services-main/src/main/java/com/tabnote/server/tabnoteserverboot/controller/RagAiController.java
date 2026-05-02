package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.CDNAI;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.AiMapper;
import com.tabnote.server.tabnoteserverboot.mq.publisher.QuotaDeductionPublisher;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import com.tabnote.server.tabnoteserverboot.services.inteface.RAGAIServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
@RequestMapping("rag")
public class RagAiController {

    private static final Logger log = LoggerFactory.getLogger(RagAiController.class);

    @Autowired
    private RAGAIServiceInterface ragAiService;

    @Autowired
    private AiServiceInterface aiService;

    @Autowired
    private TabNoteInfiniteEncryption tabNoteInfiniteEncryption;

    @Autowired
    private CDNAI cdnai;

    @Autowired
    private QuotaDeductionPublisher quotaDeductionPublisher;

    @Autowired
    private AiMapper aiMapper;

    @PostMapping("chat")
    public void ragChat(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("{}:rag_chat", tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject bodyJson = JSONObject.parseObject(request.getAttribute("body").toString());
            log.info("rag_chat收到JSON对象：{}", bodyJson);

            JSONArray messages = bodyJson.getJSONArray("messages");
            String subject = bodyJson.getString("subject");
            String usr_id = bodyJson.getString("id");
            String ip = tabNoteInfiniteEncryption.proxyGetIp(request);

            String ai_ms_id = bodyJson.getString("ai_ms_id");
            if (ai_ms_id == null || ai_ms_id.isEmpty()) {
                ai_ms_id = String.valueOf(messages.hashCode()) + String.valueOf(usr_id.hashCode()) + String.valueOf(ip.hashCode());
            }

            String ca_id = aiService.newAndResponseCAID(null);
            cdnai.newTACADS(ca_id);

            StringBuffer sb = new StringBuffer();
            int quotaCost = ragAiService.ragChat(messages, subject, response, sb, ca_id, ai_ms_id);
            log.info("rag_chat结果：{}", sb);

            try {
                String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String mainly = "";
                for (int i = 0; i < messages.size(); i++) {
                    String content = messages.getJSONObject(i).getString("content");
                    if (content != null && !content.startsWith("[")) {
                        mainly = content.length() < 20 ? content : content.substring(0, 18);
                        break;
                    }
                }
                JSONObject assistantMsg = new JSONObject();
                assistantMsg.put("role", "assistant");
                assistantMsg.put("content", sb.toString());
                messages.add(assistantMsg);

                JSONObject contents = new JSONObject();
                contents.putArray("messages");
                contents.getJSONArray("messages").add(messages);
                if(bodyJson.getString("ai_ms_id") != null && !bodyJson.getString("ai_ms_id").isEmpty()){
                    aiMapper.updateRagAiMessages(ai_ms_id, contents.toString(), dateTime);
                }else{
                    aiMapper.addRagAiMessages(ai_ms_id, subject, mainly, usr_id, contents.toString(), dateTime);
                }
            } catch (Exception e) {
                log.error("保存RAG对话历史失败: {}", e.getMessage());
            }

            quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);
        } catch (Exception e) {
            log.error(e.getMessage());
            aiService.returnErrMess(response, e.toString());
        }
        response.getWriter().close();
    }

    @PostMapping("historyList")
    public ResponseEntity<String> ragHistoryList(@RequestBody String requestBody, HttpServletRequest request) {
        log.info("{}:rag_history_list", tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject body = JSONObject.parseObject(requestBody);
            String usr_id = body.getString("id");
            String token = body.getString("token");
            String book_id = body.getString("book_id");

            JSONObject returnJSON = new JSONObject();
            returnJSON.putArray("list");

            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
                List<com.tabnote.server.tabnoteserverboot.models.RagAiMessagesForList> list;
                if (book_id != null && !book_id.isEmpty()) {
                    list = aiMapper.getRagAiListByBookId(usr_id, book_id);
                } else {
                    list = aiMapper.getRagAiList(usr_id);
                }
                for (com.tabnote.server.tabnoteserverboot.models.RagAiMessagesForList item : list) {
                    JSONObject json = new JSONObject();
                    json.put("rag_ms_id", item.getRag_ms_id());
                    json.put("book_id", item.getBook_id());
                    json.put("mainly", item.getMainly());
                    json.put("date_time", item.getDate_time());
                    returnJSON.getJSONArray("list").add(json);
                }
                returnJSON.put("response", "success");
            } else {
                returnJSON.put("response", "token_check_failed");
            }
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(returnJSON.toString());
        } catch (Exception e) {
            log.error("RAG history list error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("err");
        }
    }

    @PostMapping("historyDetail")
    public ResponseEntity<String> ragHistoryDetail(@RequestBody String requestBody, HttpServletRequest request) {
        log.info("{}:rag_history_detail", tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject body = JSONObject.parseObject(requestBody);
            String rag_ms_id = body.getString("rag_ms_id");
            String token = body.getString("token");

            JSONObject returnJSON = new JSONObject();

            String usr_id = tabNoteInfiniteEncryption.encryptionTokenGetId(token);
            if (usr_id == null || usr_id.isEmpty()) {
                returnJSON.put("response", "token_check_failed");
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(returnJSON.toString());
            }

            com.tabnote.server.tabnoteserverboot.models.RagAiMessages ragAiMessages = aiMapper.getRagAiMessages(rag_ms_id);
            if (ragAiMessages != null && ragAiMessages.getUsr_id().equals(usr_id)) {
                returnJSON.put("rag_ms_id", ragAiMessages.getRag_ms_id());
                returnJSON.put("book_id", ragAiMessages.getBook_id());
                returnJSON.put("mainly", ragAiMessages.getMainly());
                returnJSON.put("date_time", ragAiMessages.getDate_time());
                returnJSON.put("contents", JSONObject.parseObject(ragAiMessages.getContents()));
                returnJSON.put("response", "success");
            } else {
                returnJSON.put("response", "not_found_or_no_permission");
            }
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(returnJSON.toString());
        } catch (Exception e) {
            log.error("RAG history detail error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("err");
        }
    }
}