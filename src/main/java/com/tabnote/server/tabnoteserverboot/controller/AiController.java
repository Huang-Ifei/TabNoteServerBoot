package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.OCR;
import com.tabnote.server.tabnoteserverboot.component.SecurityComponent;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.models.BQ;
import com.tabnote.server.tabnoteserverboot.models.RankAndQuota;
import com.tabnote.server.tabnoteserverboot.mq.publisher.QuotaDeductionPublisher;
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
    public void setAiService(AiServiceInterface aiService) {
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

    OCR ocr;
    @Autowired
    public void setOCR(OCR ocr) {
        this.ocr = ocr;
    }

    //笔记型AI的接口
    @PostMapping("note")
    public void getNoteAiRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":note_ai");

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
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":gpt");
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
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":bq");
        try {
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject((String) request.getAttribute("body"));
            RankAndQuota raq = vipMapper.selectRankByUserId(bodyJson.getString("id"));
            //确定模型
            String model = bodyJson.getString("model");
            //进行OCR识别
            bodyJson.put("text",ocr.getOCR(bodyJson.getString("imgHigh")));

            StringBuffer answer = new StringBuffer();
            //AAM以及AM工作流
            if (model.equals("AAM")||model.equals("AM")){
                System.out.println("执行"+model);
                StringBuffer sb = new StringBuffer();
                //将请求JSON变为向API发送的JSON（识别图片内容）
                JSONObject requestJson = aiService.buildChatGPTRequestJSON(aiService.buildBQImgRequestToJSONArray(bodyJson,"latex"), modelList[0]);
                int qC1 = 0;
                try{
                    qC1 = aiService.postAiMessagesToChatGPTAPI(requestJson, null, sb);
                }catch (Exception e){
                    e.printStackTrace();
                    qC1 = aiService.postAiMessagesToChatGPTAPI(requestJson, null, sb);
                }
                if(qC1==0||sb.isEmpty()){
                    qC1 = aiService.postAiMessagesToChatGPTAPI(requestJson, null, sb);
                }
                System.out.println(sb);
                JSONArray rqArray = aiService.buildO1Message(sb);
                System.out.println("DeepSeek原生API");
                int qC2 = 0;
                try {
                    qC2 = aiService.postAiMessagesToDeepSeekAPI(aiService.buildChatGPTRequestJSON(rqArray,modelList[3]), response, answer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(answer.isEmpty()||qC2==0) {
                    try {
                        System.out.println("使用Silicon DeepSeek API");
                        qC2 = aiService.postAiMessagesToDeepSeekAPI(aiService.buildChatGPTRequestJSON(rqArray, modelList[4]), response, answer);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if(!answer.isEmpty()||qC2!=0){
                    quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), qC1 + qC2);
                }else if(raq.passAFAPlus()){
                    int qC3 = 0;
                    try {
                        qC3 = aiService.postAiMessagesToChatGPTAPI(aiService.buildChatGPTRequestJSON(rqArray,modelList[2]), response, answer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (!answer.isEmpty()||qC3!=0){
                        quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), qC1+qC3);
                    }else{
                        //将请求JSON变为向API发送的JSON
                        JSONObject requestJSON = aiService.buildChatGPTRequestJSON(aiService.buildBQImgRequestToJSONArray(bodyJson,"solve"), modelList[0]);

                        //抄送给API
                        int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJSON, response, answer);
                        System.out.println(answer);
                        response.getWriter().write("");
                        response.getWriter().flush();

                        quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);
                    }
                }else{
                    //将请求JSON变为向API发送的JSON
                    JSONObject requestJSON = aiService.buildChatGPTRequestJSON(aiService.buildBQImgRequestToJSONArray(bodyJson,"solve"), modelList[0]);

                    //抄送给API
                    int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJSON, response, answer);
                    System.out.println(answer);
                    response.getWriter().write("");
                    response.getWriter().flush();

                    quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);
                }

            //基础的4o模型可以识别图片直接执行
            }else if (model.equals("gpt-4o") || model.equals(modelList[0]) || model.equals(modelList[1])) {
                //将请求JSON变为向API发送的JSON
                JSONObject requestJson = aiService.buildChatGPTRequestJSON(aiService.buildBQImgRequestToJSONArray(bodyJson,"solve"), modelList[0]);

                //抄送给API
                int quotaCost = 0;
                try{
                    quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, answer);
                } catch (Exception e) {
                    e.printStackTrace();
                    quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, answer);
                }
                if (quotaCost==0||answer.isEmpty()){
                    quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, answer);
                }

                System.out.println(answer);
                response.getWriter().write("");
                response.getWriter().flush();

                quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);

                //O1-MINI工作流
            } else if (model.equals(modelList[2])) {
                if (!raq.passAFAPlus()) {
                    aiService.returnAdminMess(response, "使用o1-mini GPT+工作流需要您至少获取高级授权,You need to get at least AFA+ to use the o1-mini GPT+ workflow.");
                    return;
                }
                StringBuffer sb = new StringBuffer();

                //将请求JSON变为向API发送的JSON
                JSONObject requestJson = aiService.buildChatGPTRequestJSON(aiService.buildBQImgRequestToJSONArray(bodyJson,"latex"), modelList[0]);

                int quotaCost = 0;
                try{
                    quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, null, sb);
                } catch (Exception e) {
                    e.printStackTrace();
                    quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, null, sb);
                }
                if (quotaCost==0||sb.isEmpty()){
                    quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, null, sb);
                }
                System.out.println(sb);

                //抄送给API
                int quotaCost2 = aiService.postAiMessagesToChatGPTAPI(aiService.buildChatGPTRequestJSON(aiService.buildO1Message(sb),modelList[2]), response, answer);
                System.out.println(answer);

                quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost + quotaCost2);
            }

            //加入历史
            BQ bq = new BQ();
            bq.setUsr_id(bodyJson.getString("id"));
            bq.setText(bodyJson.getString("text"));
            bq.setAi_answer(answer.toString());
            bq.setDxstj(bodyJson.getJSONArray("dxstjJsonArray").toString());
            bq.setImg(bodyJson.getString("img"));
            bq.setBq_id(UUID.randomUUID().toString());
            aiService.insertBQWithOutTokenCheck(bq);

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
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":Ai_Messages");
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
                    response.getWriter().write(aiService.createMessages(messages, bodyJson.getString("id"), tabNoteInfiniteEncryption.proxyGetIp(request)));
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
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":dxstj");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");
        String base64Img = requestJson.getString("img");

        return sendMes(aiService.getDXSTJ(id, token, base64Img));
    }

    //对话AI的历史记录
    @PostMapping("get_history")
    public ResponseEntity<String> getAiHistory(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":get_ai_history");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");

        return sendMes(aiService.getAiMessagesList(id, token));
    }

    //获取某个对话的信息
    @PostMapping("history")
    public ResponseEntity<String> getAiHistoryMessages(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":get_history_ai_messages");
        JSONObject requestJson = JSONObject.parseObject(body);
        String ai_ms_id = requestJson.getString("ai_ms_id");
        String token = requestJson.getString("token");
        return sendMes(aiService.getAiMessages(ai_ms_id, token));
    }

    //笔记同步
    @PostMapping("note_sync")
    public ResponseEntity<String> noteAiSync(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":note_sync");
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
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":get_ai_note_history");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");

        return sendMes(aiService.getNoteAiHistory(id, token));
    }

    //获取历史上的笔记
    @PostMapping("note_history")
    public ResponseEntity<String> getAiHistoryNote(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":get_history_ai_note");
        JSONObject requestJson = JSONObject.parseObject(body);
        String note_ai_id = requestJson.getString("note_ai_id");
        String token = requestJson.getString("token");
        return sendMes(aiService.getHistoryNoteAi(note_ai_id, token));
    }

    //增加AI搜题历史
    @PostMapping("insertBQ")
    public ResponseEntity<String> insertBQ(HttpServletRequest request, @RequestBody String body) throws Exception {
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":insertBQ");
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
            System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":BQList");
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
        System.out.println(tabNoteInfiniteEncryption.proxyGetIp(request) + ":BQ");
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
