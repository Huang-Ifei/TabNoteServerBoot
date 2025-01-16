package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.SecurityComponent;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.models.BQ;
import com.tabnote.server.tabnoteserverboot.models.RankAndQuota;
import com.tabnote.server.tabnoteserverboot.mq.publisher.QuotaDeductionPublisher;
import com.tabnote.server.tabnoteserverboot.services.AiService;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

import static com.tabnote.server.tabnoteserverboot.define.AiList.modelList;

@CrossOrigin
@Controller
@RequestMapping("ai")
public class AiController {
    AiServiceInterface aiService;

    @Autowired
    public void setAiMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    AccountMapper accountMapper;

    @Autowired
    public void setAiService(AiService aiService) {
        this.aiService = aiService;
    }

    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;

    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tabNoteInfiniteEncryption) {
        this.tabNoteInfiniteEncryption = tabNoteInfiniteEncryption;
    }

    VipMapper vipMapper;

    @Autowired
    public void setVipMapper(VipMapper vipMapper) {
        this.vipMapper = vipMapper;
    }

    SecurityComponent securityComponent;

    @Autowired
    public void setSecurityComponent(SecurityComponent securityComponent) {
        this.securityComponent = securityComponent;
    }

    QuotaDeductionPublisher quotaDeductionPublisher;

    @Autowired
    public void setQuotaDeductionPublisher(QuotaDeductionPublisher quotaDeductionPublisher) {
        this.quotaDeductionPublisher = quotaDeductionPublisher;
    }

    //笔记型AI的接口
    @PostMapping("note")
    public void getNoteAiRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println(request.getRemoteAddr() + ":note_ai");
        try {
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject((String) request.getAttribute("body"));
            //确定模型
            String model = bodyJson.getString("model");
            //将请求JSON变为向API发送的JSON
            JSONArray messages = bodyJson.getJSONArray("messages");
            JSONObject requestJson = aiService.buildChatGPTRequestJSON(messages, model);
            StringBuffer sb = new StringBuffer();
            //抄送给API
            int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, sb);
            System.out.println(sb);
            response.getWriter().write("");
            response.getWriter().flush();

            quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);
        } catch (Exception e) {
            e.printStackTrace();
            String errMess = e.toString();
            aiService.returnErrMess(response, errMess);
        }
        response.getWriter().close();
    }

    //不进行数据库操作的gpt接口
    @PostMapping("gpt")
    public void sendChatGPTMesses(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println(request.getRemoteAddr() + ":gpt");
        try {
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject((String) request.getAttribute("body"));
            //确定模型
            String model = bodyJson.getString("model");
            //将请求JSON变为向API发送的JSON
            JSONArray messages = bodyJson.getJSONArray("messages");
            JSONObject requestJson = aiService.buildChatGPTRequestJSON(messages, model);
            StringBuffer sb = new StringBuffer();
            //抄送给API
            int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, sb);
            System.out.println(sb);
            response.getWriter().write("");
            response.getWriter().flush();

            quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);
        } catch (Exception e) {
            e.printStackTrace();
            String errMess = e.toString();
            aiService.returnErrMess(response, errMess);
        }
        response.getWriter().close();
    }

    //bq接口
    @PostMapping("bq")
    public void sendBQMess(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println(request.getRemoteAddr() + ":bq");
        try {
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject((String) request.getAttribute("body"));
            RankAndQuota raq = vipMapper.selectRankByUserId(bodyJson.getString("id"));
            //确定模型
            String model = bodyJson.getString("model");
            //基础的4o模型可以识别图片直接执行
            if (model.equals("gpt-4o") || model.equals(modelList[0]) || model.equals(modelList[1])) {
                //将请求JSON变为向API发送的JSON
                JSONArray userContent = new JSONArray();
                JSONObject imgJSON = new JSONObject();
                imgJSON.put("type", "image_url");
                JSONObject imageUrlJson = new JSONObject();
                imageUrlJson.put("url", bodyJson.getString("img"));
                imgJSON.put("image_url", imageUrlJson);
                userContent.add(imgJSON);
                JSONObject textJson = new JSONObject();
                textJson.put("type", "text");
                textJson.put("text", "图中有一道或若干道题目，请你告诉我它/它们的答案；[如果是数学题目（包括运筹学，高等数学）请您结合题目内容告诉我每一步的解题步骤]，以下是我提前使用OCR对图片进行识别，读取出来的内容，供您参考校对，以免出现识别错误：" + bodyJson.getString("text"));
                userContent.add(textJson);
                JSONObject roleJson = new JSONObject();
                roleJson.put("role", "user");
                roleJson.put("content", userContent);
                JSONArray messages = new JSONArray();
                messages.add(roleJson);
                JSONObject requestJson = aiService.buildChatGPTRequestJSON(messages, modelList[0]);

                StringBuffer sb = new StringBuffer();
                //抄送给API
                int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, sb);
                System.out.println(sb);
                response.getWriter().write("");
                response.getWriter().flush();

                quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);

                //加入历史
                BQ bq = new BQ();
                bq.setUsr_id(bodyJson.getString("id"));
                bq.setText(bodyJson.getString("text"));
                bq.setAi_answer(sb.toString());
                bq.setDxstj(bodyJson.getJSONArray("dxstjJsonArray").toString());
                bq.setImg(bodyJson.getString("img"));
                bq.setBq_id(UUID.randomUUID().toString());
                aiService.insertBQWithOutTokenCheck(bq);
            } else if (model.equals(modelList[2])) {
                if (!raq.passAFAPlus()) {
                    aiService.returnAdminMess(response, "使用o1-mini GPT+工作流需要您至少获取高级授权,You need to get at least AFA+ to use the o1-mini GPT+ workflow.");
                    return;
                }
                StringBuffer sb = new StringBuffer();

                //将请求JSON变为向API发送的JSON
                JSONArray userContent = new JSONArray();
                JSONObject imgJSON = new JSONObject();
                imgJSON.put("type", "image_url");
                JSONObject imageUrlJson = new JSONObject();
                imageUrlJson.put("url", bodyJson.getString("img"));
                imgJSON.put("image_url", imageUrlJson);
                userContent.add(imgJSON);
                JSONObject textJson = new JSONObject();
                textJson.put("type", "text");
                textJson.put("text", "识别图中题目，告诉我题干，存在图片时请注意要描述图片里的内容，存在公式时请描述公示内容，存在表格时描述表格内容;以下是我先行使用OCR识别出来的结果，请根据图片内容进行修正（例如补充图片，表格的内容）：" + bodyJson.getString("text"));
                userContent.add(textJson);
                JSONObject roleJson = new JSONObject();
                roleJson.put("role", "user");
                roleJson.put("content", userContent);
                JSONArray messages = new JSONArray();
                messages.add(roleJson);
                JSONObject requestJson = aiService.buildChatGPTRequestJSON(messages, modelList[0]);

                int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, null, sb);
                System.out.println(sb);

                JSONArray o1Messages = new JSONArray();
                JSONObject o1Message = new JSONObject();
                o1Message.put("role", "user");
                o1Message.put("content", "图中有一道或若干道题目，我已经通过GPT4o识别出来图中题目的题干，请根据识别出来的题干，告诉我它/它们的答案：" + sb.toString());
                o1Messages.add(o1Message);
                JSONObject o1Request = aiService.buildChatGPTRequestJSON(o1Messages, model);

                StringBuffer sb1 = new StringBuffer();
                //抄送给API
                int quotaCost2 = aiService.postAiMessagesToChatGPTAPI(o1Request, response, sb1);
                System.out.println(sb1);

                quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost + quotaCost2);

                //加入历史
                BQ bq = new BQ();
                bq.setUsr_id(bodyJson.getString("id"));
                bq.setText(bodyJson.getString("text"));
                bq.setAi_answer(sb1.toString());
                bq.setDxstj(bodyJson.getJSONArray("dxstjJsonArray").toString());
                bq.setImg(bodyJson.getString("img"));
                bq.setBq_id(UUID.randomUUID().toString());
                aiService.insertBQWithOutTokenCheck(bq);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errMess = e.toString();
            aiService.returnErrMess(response, errMess);
        }
        response.getWriter().close();
    }

    //AI对话接口
    @PostMapping("messages")
    public void sendMesses(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println(request.getRemoteAddr() + ":Ai_Messages");
        try {
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject((String) request.getAttribute("body"));
            //确定模型
            String model = bodyJson.getString("model");
            //将请求JSON变为向API发送的JSON
            JSONArray messages = bodyJson.getJSONArray("messages");
            JSONObject requestJson = aiService.buildChatGPTRequestJSON(messages, model);
            StringBuffer sb = new StringBuffer();
            //抄送给API
            int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, sb);
            System.out.println(sb);
            response.getWriter().write("");
            response.getWriter().flush();

            quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);

            //如果ai反馈非空且，数据库操作
            if (!sb.isEmpty()) {
                if (!bodyJson.containsKey("ai_ms_id") || (bodyJson.containsKey("ai_ms_id") && bodyJson.getString("ai_ms_id").isEmpty())) {
                    JSONObject messageJson = new JSONObject();
                    messageJson.put("role", "assistant");
                    messageJson.put("content", sb.toString());
                    messages.add(messageJson);
                    response.getWriter().write(aiService.createMessages(messages, bodyJson.getString("id"), request.getRemoteAddr() + request.getRemotePort()));
                    response.getWriter().flush();
                } else if ((bodyJson.containsKey("ai_ms_id") && !bodyJson.getString("ai_ms_id").isEmpty())) {
                    JSONObject messageJson = new JSONObject();
                    messageJson.put("role", "assistant");
                    messageJson.put("content", sb.toString());
                    messages.add(messageJson);
                    aiService.changeMessages(messages, bodyJson.getString("ai_ms_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            aiService.returnErrMess(response, e.toString());
        }
        response.getWriter().close();
    }

    //大学搜题酱接口
    @PostMapping("dxstj")
    public ResponseEntity<String> getDXSTJ(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":dxstj");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");
        String base64Img = requestJson.getString("img");

        return sendMes(aiService.getDXSTJ(id, token, base64Img));
    }

    //对话AI的历史记录
    @PostMapping("get_history")
    public ResponseEntity<String> getAiHistory(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":get_ai_history");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");

        return sendMes(aiService.getAiMessagesList(id, token));
    }

    //获取某个对话的信息
    @PostMapping("history")
    public ResponseEntity<String> getAiHistoryMessages(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":get_history_ai_messages");
        JSONObject requestJson = JSONObject.parseObject(body);
        String ai_ms_id = requestJson.getString("ai_ms_id");
        String token = requestJson.getString("token");
        return sendMes(aiService.getAiMessages(ai_ms_id, token));
    }

    //笔记同步
    @PostMapping("note_sync")
    public ResponseEntity<String> noteAiSync(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":note_sync");
        try {
            JSONObject bodyJson = JSONObject.parseObject(body);
            return sendMes(aiService.noteAiSync(bodyJson.getString("note_ai_id"), bodyJson.getString("note"), bodyJson.getJSONArray("note_ticks"), bodyJson.getString("token"), bodyJson.getString("id"), bodyJson.getString("note_content")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    //笔记历史
    @PostMapping("get_note_history")
    public ResponseEntity<String> getAiNoteHistory(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":get_ai_note_history");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");

        return sendMes(aiService.getNoteAiHistory(id, token));
    }

    //获取历史上的笔记
    @PostMapping("note_history")
    public ResponseEntity<String> getAiHistoryNote(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":get_history_ai_note");
        JSONObject requestJson = JSONObject.parseObject(body);
        String note_ai_id = requestJson.getString("note_ai_id");
        String token = requestJson.getString("token");
        return sendMes(aiService.getHistoryNoteAi(note_ai_id, token));
    }

    //增加AI搜题历史
    @PostMapping("insertBQ")
    public ResponseEntity<String> insertBQ(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":insertBQ");
        try {
            JSONObject requestJson = JSONObject.parseObject(body);
            String id = requestJson.getString("usr_id");
            String token = requestJson.getString("token");
            BQ bq = new BQ();
            bq.setUsr_id(id);
            bq.setText(requestJson.getString("text"));
            bq.setAi_answer(requestJson.getString("ai_answer"));
            bq.setDxstj(requestJson.getJSONArray("dxstj").toString());
            bq.setImg(requestJson.getString("img"));
            bq.setBq_id(UUID.randomUUID().toString());
            return sendMes(aiService.insertBQ(bq, id, token));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    //获取AI搜题历史记录
    @PostMapping("BQList")
    public ResponseEntity<String> getBQList(HttpServletRequest request, @RequestBody String body) throws Exception {
        try {
            System.out.println(request.getRemoteAddr() + ":BQList");
            JSONObject requestJson = JSONObject.parseObject(body);
            String id = requestJson.getString("id");
            String token = requestJson.getString("token");
            int index = requestJson.getInteger("index");
            return sendMes(aiService.getBQListByUserId(id, token, index));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    //获取单条AI搜题的详细历史记录
    @PostMapping("BQ")
    public ResponseEntity<String> getBQ(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":BQ");
        try {
            JSONObject requestJson = JSONObject.parseObject(body);
            String id = requestJson.getString("id");
            String token = requestJson.getString("token");
            String bq_id = requestJson.getString("bq_id");
            return sendMes(aiService.getBQ(bq_id, id, token));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
