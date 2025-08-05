package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.*;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.AiMapper;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.models.BQ;
import com.tabnote.server.tabnoteserverboot.models.RankAndQuota;
import com.tabnote.server.tabnoteserverboot.mq.publisher.QuotaDeductionPublisher;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static com.tabnote.server.tabnoteserverboot.define.AiInfo.modelList;

@CrossOrigin
@Controller
@RequestMapping("ai")
public class AiController {

    private static Logger log = LoggerFactory.getLogger(AiController.class);

    private AiServiceInterface aiService;
    @Autowired
    public void setAiService(AiServiceInterface aiService) {
        this.aiService = aiService;
    }

    private AiMapper aiMapper;
    @Autowired
    public void setAiMapper(AiMapper aiMapper) {
        this.aiMapper = aiMapper;
    }

    AccountMapper accountMapper;
    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
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

    DiscoveryClient discoveryClient;

    @Autowired
    public void setDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    RestTemplate restTemplate;

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    TabNoteDefinitelyVectorCache tabNoteDefinitelyVectorCache;
    @Autowired
    public void setTabNoteDefinitelyVectorCache(TabNoteDefinitelyVectorCache tabNoteDefinitelyVectorCache) {
        this.tabNoteDefinitelyVectorCache = tabNoteDefinitelyVectorCache;
    }
    CDNAI cdnai;
    @Autowired
    public void setCDNAI(CDNAI cdnai) {
        this.cdnai = cdnai;
    }


    //笔记型AI的接口
    @PostMapping("note")
    public void getNoteAiRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        log.info("{}:note_ai", tabNoteInfiniteEncryption.proxyGetIp(request));

        try {
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject((String) request.getAttribute("body"));
            //确定模型
            String model = bodyJson.getString("model");
            //将请求JSON变为向API发送的JSON
            JSONArray messages = bodyJson.getJSONArray("messages");
            JSONObject requestJson = aiService.buildChatGPTRequestJSON(messages, model);
            StringBuffer sb = new StringBuffer();
            String ca_id = aiService.newAndResponseCAID(response);
            cdnai.newTACADS(ca_id);

            //抄送给API
            int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, sb,ca_id);
            log.info("Note AI结果："+sb);
            response.getWriter().write("");
            response.getWriter().flush();

            quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);
        } catch (Exception e) {
            log.error(e.getMessage());
            String errMess = e.toString();
            aiService.returnErrMess(response, errMess);
        }
        response.getWriter().close();
    }

    //不进行数据库操作的gpt接口
    @PostMapping("gpt")
    public void sendChatGPTMesses(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("{}:gpt", tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject((String) request.getAttribute("body"));
            //确定模型
            String model = bodyJson.getString("model");
            //将请求JSON变为向API发送的JSON
            JSONArray messages = bodyJson.getJSONArray("messages");
            JSONObject requestJson = aiService.buildChatGPTRequestJSON(messages, model);
            StringBuffer sb = new StringBuffer();
            String ca_id = aiService.newAndResponseCAID(response);
            cdnai.newTACADS(ca_id);
            //抄送给API
            int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, sb,ca_id);
            log.info("GPT AI结果："+sb);
            response.getWriter().write("");
            response.getWriter().flush();

            quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);
        } catch (Exception e) {
            log.error(e.getMessage());
            String errMess = e.toString();
            aiService.returnErrMess(response, errMess);
        }
        response.getWriter().close();
    }

    //bq接口
    @PostMapping("bq")
    public void sendBQMess(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("{}:bq", tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject((String) request.getAttribute("body"));
            RankAndQuota raq = quotaDeductionPublisher.getQuotaAndRank(bodyJson.getString("id"));
            //确定模型
            String model = bodyJson.getString("model");
            String ca_id = aiService.newAndResponseCAID(response);
            cdnai.newTACADS(ca_id);
            //初始化
            StringBuffer answer = new StringBuffer();
            String hitDataId = "";
            int qC1 = 0;

            //AAM以及AM工作流
            if (model.equals("AAM") || model.equals("AM")) {
                log.info("BQ执行:" + model);
                StringBuffer sb = new StringBuffer();
                //如果text为空那么就会进行识别，并且进行缓存查找，如果缓存被判定为命中，则返回缓存命中标准格式给客户端，如果不为空直接使用客户端发上来的text跳过识别部分
                if (bodyJson.getString("text").isEmpty()) {
                    //如果为缓存不受信状态会有text，缓存受信则不会发送信息，第一次则text为空
                    //如果是第一次发送请求则进行标准操作OCR识别
                    bodyJson.put("text", ocr.getOCR(bodyJson.getString("imgHigh")));

                    //将请求JSON变为向API发送的JSON（识别图片内容）
                    JSONObject requestJson = aiService.buildChatGPTRequestJSON(aiService.buildBQImgRequestToJSONArray(bodyJson, "latex"), modelList[9]);

                    try {
                        qC1 = aiService.postAiMessagesToChatGPTAPI(requestJson, null, sb,"");
                    } catch (Exception e) {
                        log.error("gpt-4o识别出错了!!");
                        log.error(e.getMessage());
                    }
                    //没成功重试一下
                    if (qC1 == 0 || sb.isEmpty()) {
                        qC1 = aiService.postAiMessagesToChatGPTAPI(requestJson, null, sb,"");
                    }
                    log.info("BQ结果："+sb);
                    bodyJson.put("text",sb.toString());
                    hitDataId = tabNoteDefinitelyVectorCache.getBQVectorCache(bodyJson.getString("text"));
                }else{
                    sb.append(bodyJson.getString("text"));
                    aiMapper.deleteBQByText(bodyJson.getString("id"),bodyJson.getString("text"));
                }

                //判断hit非空则执行hit操作，找到目标则不修改hit为空，没找到缓存id的数据库字段则修改hit为空
                if(!hitDataId.isEmpty()){
                    if(!aiService.returnVector(response,hitDataId,bodyJson.getString("text"),answer)){
                        hitDataId="";
                    }else{
                        quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), qC1);
                    }
                }

                //以下操作先判断hit为空操作
                if(hitDataId.isEmpty()){
                    //通过识别出来的内容构建O1格式的JSON
                    JSONArray rqArray = aiService.buildO1Message(sb);
                    int qC2 = 0;

                    //尝试硅基流动API
                    if (answer.isEmpty()) {
                        try {
                            if (!sb.isEmpty()) {
                                log.info("使用Silicon DeepSeek API");
                                qC2 = aiService.postAiMessagesToDeepSeekAPI(aiService.buildChatGPTRequestJSON(rqArray, modelList[4]), response, answer,ca_id);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    //DeepSeek解题成功
                    if (!answer.isEmpty() || qC2 != 0) {
                        log.info("扣费：gpt4o用于识别："+qC1+"；deepSeek用于解题："+qC2);
                        quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), qC1 + qC2);
                    } else {
                        JSONObject requestJSON = aiService.buildChatGPTRequestJSON(aiService.buildBQImgRequestToJSONArray(bodyJson, "solve"), modelList[8]);

                        //抄送给API
                        int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJSON, response, answer,ca_id);
                        log.info("BQ DeepSeek结题结果："+answer);
                        response.getWriter().write("");
                        response.getWriter().flush();
                        log.info("扣费：gpt4o用于识别："+qC1+"（这一费用用户不承担）；gpt4o用于解题："+quotaCost);
                        quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);
                    }
                }
            }

            //如果不是AAM、AM或者出现了超时未解答的问题，使用基础的4o/4.1模型可以识别图片直接执行
            if (answer.isEmpty() && hitDataId.isEmpty()) {
                log.info("直接解题");
                //将请求JSON变为向API发送的JSON
                JSONObject requestJson = aiService.buildChatGPTRequestJSON(aiService.buildBQImgRequestToJSONArray(bodyJson, "solve"), modelList[8]);

                //抄送给API
                int quotaCost = 0;
                try {
                    quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, answer,ca_id);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }

                log.info("GPT4o直接解题结果："+answer);
                response.getWriter().write("");
                response.getWriter().flush();

                log.info("扣费：仅GPT4o，gpt4o用于解题："+quotaCost);
                quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);
            }

            //加入历史
            BQ bq = new BQ();
            String BQID = UUID.randomUUID().toString();


            bq.setUsr_id(bodyJson.getString("id"));
            bq.setText(bodyJson.getString("text"));
            bq.setAi_answer(answer.toString());
            bq.setDxstj(bodyJson.getJSONArray("dxstjJsonArray").toString());
            bq.setImg(bodyJson.getString("img"));
            bq.setBq_id(BQID);
            aiService.insertBQWithOutTokenCheck(bq);
            response.getWriter().close();
            if(!answer.isEmpty()){
                tabNoteDefinitelyVectorCache.addToBQVectorCache(BQID, bodyJson.getString("text"));
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            String errMess = e.toString();
            aiService.returnErrMess(response, errMess);
        }
        response.getWriter().close();
    }

    //AI对话接口
    @PostMapping("messages")
    public void sendMesses(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("{}:Ai_Messages", tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject((String) request.getAttribute("body"));
            log.info("messages接口收到JSON对象："+bodyJson);
            String ca_id = aiService.newAndResponseCAID(response);
            cdnai.newTACADS(ca_id);
            //确定模型
            String model = bodyJson.getString("model");
            //将请求JSON变为向API发送的JSON
            JSONArray messages = bodyJson.getJSONArray("messages");
            JSONObject requestJson = aiService.buildChatGPTRequestJSON(messages, model);
            StringBuffer sb = new StringBuffer();
            //抄送给API
            int quotaCost = aiService.postAiMessagesToChatGPTAPI(requestJson, response, sb,ca_id);
            log.info("messages接口结果："+sb);
            response.getWriter().write("");
            response.getWriter().flush();

            quotaDeductionPublisher.quotaCost(bodyJson.getString("id"), quotaCost);

            //如果ai反馈非空且，数据库操作
            if (!sb.isEmpty()) {
                if ( bodyJson.getString("ai_ms_id").isEmpty()) {
                    JSONObject messageJson = new JSONObject();
                    messageJson.put("role", "assistant");
                    messageJson.put("content", sb.toString());
                    messages.add(messageJson);
                    response.getWriter().write(aiService.createMessages(messages, bodyJson.getString("id"), tabNoteInfiniteEncryption.proxyGetIp(request),ca_id));
                    response.getWriter().flush();
                } else {
                    JSONObject messageJson = new JSONObject();
                    messageJson.put("role", "assistant");
                    messageJson.put("content", sb.toString());
                    messages.add(messageJson);
                    aiService.changeMessages(messages, bodyJson.getString("ai_ms_id"));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            aiService.returnErrMess(response, e.toString());
        }
        response.getWriter().close();
    }

    //大学搜题酱接口
    @PostMapping("dxstj")
    public ResponseEntity<String> getDXSTJ(HttpServletRequest request, @RequestBody String body) throws Exception {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + ":dxstj");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");
        String base64Img = requestJson.getString("img");

        return sendMes(aiService.getDXSTJ(id, token, base64Img));
    }

    //对话AI的历史记录
    @PostMapping("get_history")
    public ResponseEntity<String> getAiHistory(HttpServletRequest request, @RequestBody String body) throws Exception {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + ":get_ai_history");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");

        return sendMes(aiService.getAiMessagesList(id, token));
    }

    //获取某个对话的信息
    @PostMapping("history")
    public ResponseEntity<String> getAiHistoryMessages(HttpServletRequest request, @RequestBody String body) throws Exception {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + ":get_history_ai_messages");
        JSONObject requestJson = JSONObject.parseObject(body);
        String ai_ms_id = requestJson.getString("ai_ms_id");
        String token = requestJson.getString("token");
        return sendMes(aiService.getAiMessages(ai_ms_id, token));
    }

    //笔记同步
    @PostMapping("note_sync")
    public ResponseEntity<String> noteAiSync(HttpServletRequest request, @RequestBody String body) throws Exception {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + ":note_sync");
        try {
            JSONObject bodyJson = JSONObject.parseObject(body);
            return sendMes(aiService.noteAiSync(bodyJson.getString("note_ai_id"), bodyJson.getString("note"), bodyJson.getJSONArray("note_ticks"), bodyJson.getString("token"), bodyJson.getString("id"), bodyJson.getString("note_content")));
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendErr();
        }
    }

    //笔记历史
    @PostMapping("get_note_history")
    public ResponseEntity<String> getAiNoteHistory(HttpServletRequest request, @RequestBody String body) throws Exception {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + ":get_ai_note_history");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");

        return sendMes(aiService.getNoteAiHistory(id, token));
    }

    //获取历史上的笔记
    @PostMapping("note_history")
    public ResponseEntity<String> getAiHistoryNote(HttpServletRequest request, @RequestBody String body) throws Exception {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + ":get_history_ai_note");
        JSONObject requestJson = JSONObject.parseObject(body);
        String note_ai_id = requestJson.getString("note_ai_id");
        String token = requestJson.getString("token");
        return sendMes(aiService.getHistoryNoteAi(note_ai_id, token));
    }

    //增加AI搜题历史
    @PostMapping("insertBQ")
    public ResponseEntity<String> insertBQ(HttpServletRequest request, @RequestBody String body) throws Exception {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + ":insertBQ");
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
            log.error(e.getMessage());
            return sendErr();
        }
    }

    //获取AI搜题历史记录
    @PostMapping("BQList")
    public ResponseEntity<String> getBQList(HttpServletRequest request, @RequestBody String body) throws Exception {
        try {
            log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + ":BQList");
            JSONObject requestJson = JSONObject.parseObject(body);
            String id = requestJson.getString("id");
            String token = requestJson.getString("token");
            int index = requestJson.getInteger("index");
            return sendMes(aiService.getBQListByUserId(id, token, index));
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendErr();
        }
    }

    //获取单条AI搜题的详细历史记录
    @PostMapping("BQ")
    public ResponseEntity<String> getBQ(HttpServletRequest request, @RequestBody String body) throws Exception {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + ":BQ");
        try {
            JSONObject requestJson = JSONObject.parseObject(body);
            String id = requestJson.getString("id");
            String token = requestJson.getString("token");
            String bq_id = requestJson.getString("bq_id");
            return sendMes(aiService.getBQ(bq_id, id, token));
        } catch (Exception e) {
            log.error(e.getMessage());
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
