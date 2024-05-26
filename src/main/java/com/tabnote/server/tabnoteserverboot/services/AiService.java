package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.AiMapper;
import com.tabnote.server.tabnoteserverboot.models.AiMessages;
import com.tabnote.server.tabnoteserverboot.models.AiMessagesForList;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.tabnote.server.tabnoteserverboot.define.AiList.*;

@Service
public class AiService implements AiServiceInterface {
    AiMapper aiMapper;
    AiRequestCounter aiRequestCounter;
    AccountMapper accountMapper;

    @Autowired
    public void setAiMapper(AiMapper aiMapper) {
        this.aiMapper = aiMapper;
    }

    @Autowired
    public void setAiRequestCounter(AiRequestCounter aiRequestCounter) {
        this.aiRequestCounter = aiRequestCounter;
    }

    @Autowired
    public void setMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    //确定模型
    @Override
    public String modelDefine(JSONObject bodyJson) {
        String model = "models/gemini-1.5-flash-latest";
        if (bodyJson.containsKey("model") && bodyJson.containsKey("token")) {
            if (bodyJson.getString("model").equals("gemini-1.5-pro-latest")) {
                if (aiRequestCounter.proAiRequestCheck()) {
                    model = "models/gemini-1.5-pro-latest";
                    return model;
                }
            }
        }
        if (!aiRequestCounter.flashAiRequestCheck()) {
            model = "models/gemini-pro";
        }
        return model;
    }

    //将请求JSON变为向API发送的JSON
    @Override
    public JSONObject buildRequestJSON(JSONArray messages, String model) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", model);
        requestJson.putArray("contents");
        JSONArray contents = requestJson.getJSONArray("contents");
        for (int i = 0; i < messages.size(); i++) {
            JSONObject message = messages.getJSONObject(i);
            if (message.getString("role").equals("user")) {
                JSONObject userJson = new JSONObject();
                userJson.put("role", "user");
                userJson.putArray("parts");
                JSONObject parts = new JSONObject();
                parts.put("text", message.getString("content"));
                userJson.getJSONArray("parts").add(parts);
                contents.add(userJson);
            } else if (message.getString("role").equals("assistant")) {
                JSONObject userJson = new JSONObject();
                userJson.put("role", "model");
                userJson.putArray("parts");
                JSONObject parts = new JSONObject();
                parts.put("text", message.getString("content"));
                userJson.getJSONArray("parts").add(parts);
                contents.add(userJson);
            } else {
                JSONObject userJson = new JSONObject();
                userJson.put("role", message.getString("role"));
                userJson.putArray("parts");
                JSONObject parts = new JSONObject();
                parts.put("text", message.getString("content"));
                userJson.getJSONArray("parts").add(parts);
                contents.add(userJson);
            }
        }
        return requestJson;
    }

    //抄送给API
    @Override
    public void postAiMessagesToAPI(JSONObject requestJson, HttpServletResponse response, StringBuffer returnString) throws Exception {
        String url = STREAM_API_URL + GOOGLE_API_KEY;
        URL uRL = new URL(url);
        HttpURLConnection connection;
        OutputStream os;
        InputStream is;
        BufferedReader br;
        connection = (HttpURLConnection) uRL.openConnection(proxy);
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(100000);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        os = connection.getOutputStream();
        os.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();
        if (connection.getResponseCode() == 200) {
            if (requestJson.getString("model").equals("models/gemini-1.5-pro-latest")) {
                aiRequestCounter.addProAiRequest();
            } else if (requestJson.getString("model").equals("models/gemini-1.5-flash-latest")) {
                aiRequestCounter.addFlashAiRequest();
            }
            response.addHeader("content-type", "text/html;charset=utf-8");

            is = connection.getInputStream();
            if (null != is) {
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String temp;
                while (null != (temp = br.readLine())) {
                    if (!temp.equals("\n") && !temp.isEmpty()) {
                        StringBuffer responseJSON = new StringBuffer();
                        for (int i = 5; i < temp.length(); i++) {
                            responseJSON.append(temp.charAt(i));
                        }
                        System.out.println(responseJSON);
                        //如果因为安全原因被拦截，返回重复信息
                        if(responseJSON.toString().contains("\"finishReason\": \"SAFETY\"")){
                            JSONObject returnJSON = new JSONObject();
                            JSONObject returnMessage = new JSONObject();
                            returnJSON.put("model", requestJson.getString("model"));
                            returnMessage.put("content", "因为安全问题被驳回");
                            returnJSON.put("message", returnMessage);
                            //添加到string buffer里面
                            returnString.append("因为安全问题被驳回");
                            //把封装好的JSON送回
                            response.getWriter().write(returnJSON.toString());
                            response.getWriter().write("\n");
                            response.getWriter().flush();
                        }else{
                            JSONObject tempJSON = JSONObject.parseObject(responseJSON.toString());
                            if (tempJSON != null ) {
                                try{
                                    String returnMess = tempJSON.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                                    //封装
                                    JSONObject returnJSON = new JSONObject();
                                    JSONObject returnMessage = new JSONObject();
                                    returnJSON.put("model", requestJson.getString("model"));
                                    returnMessage.put("content", returnMess);
                                    returnJSON.put("message", returnMessage);
                                    //添加到string buffer里面
                                    returnString.append(returnMess);
                                    //把封装好的JSON送回
                                    response.getWriter().write(returnJSON.toString());
                                    response.getWriter().write("\n");
                                    response.getWriter().flush();
                                }catch (NullPointerException e){
                                    break;
                                }
                            }
                        }
                    }
                }
                br.close();
            }
        } else if (connection.getResponseCode() == 500||connection.getResponseCode() == 429) {
            //500,429响应码降级
            this.downgradeAiModel(requestJson,response,returnString);
        } else {
            returnString.delete(0, returnString.length());
            System.out.println("err:" + connection.getResponseCode());

            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            System.out.println(new String(bis.readAllBytes()));

            JSONObject returnJSON = new JSONObject();
            JSONObject returnMessage = new JSONObject();

            returnMessage.put("content", "failed" + connection.getResponseCode());
            returnJSON.put("model", requestJson.getString("model"));
            returnJSON.put("message", returnMessage);
            response.getWriter().write(returnJSON.toString());
            response.getWriter().write("\n");
            response.getWriter().flush();
        }
    }

    public void downgradeAiModel(JSONObject requestJson, HttpServletResponse response, StringBuffer returnString) throws Exception {
        if (requestJson.getString("model").equals("models/gemini-1.5-pro-latest")) {
            System.out.println("try_change_flash_model");
            requestJson.put("model", "models/gemini-1.5-flash-latest");
            postAiMessagesToAPI(requestJson, response,returnString);
        } else if (requestJson.getString("model").equals("models/gemini-1.5-flash-latest")) {
            System.out.println("try_change_1.0_model");
            requestJson.put("model", "models/gemini-pro");
            postAiMessagesToAPI(requestJson, response,returnString);
        } else {
            //封装
            JSONObject returnJSON = new JSONObject();
            JSONObject returnMessage = new JSONObject();
            returnJSON.put("model", requestJson.getString("model"));
            returnMessage.put("content", "请求速度过快，请稍后重试");
            returnJSON.put("message", returnMessage);

            response.getWriter().write(returnJSON.toString());
            response.getWriter().write("\n");
            response.getWriter().flush();
        }
    }

    @Override
    public String createMessages(JSONArray messages, String id, String ip) {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String aiMsId = String.valueOf(messages.hashCode()) + String.valueOf(id.hashCode()) +String.valueOf(ip.hashCode());
        String mainly;
        String firstContent = JSONObject.parseObject(messages.get(0).toString()).getString("content");
        if ( firstContent.length()<20){
            mainly = firstContent;
        }else{
            mainly = firstContent.substring(0, 20);
        }
        JSONObject contents = new JSONObject();
        contents.putArray("messages");
        contents.getJSONArray("messages").add(messages);
        try {
            aiMapper.addNewAiMessages(aiMsId, mainly, id, contents.toString(), dateTime);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "{\"response\":\""+aiMsId+"\"}";
    }

    @Override
    public void changeMessages(JSONArray messages, String aiMsId) {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        JSONObject contents = new JSONObject();
        contents.putArray("messages");
        contents.getJSONArray("messages").add(messages);
        try {
            aiMapper.changeAiMessages(contents.toString(), dateTime, String.valueOf(aiMsId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject getAiMessages(String aiMsId,String token) {
        JSONObject returnJSON = new JSONObject();
        returnJSON.putArray("messages");
        try {
            String id = accountMapper.tokenCheckIn(token);
            AiMessages aiMessages = aiMapper.getUsrAiMessages(aiMsId);
            if (aiMessages!=null&&aiMessages.getUsr_id().equals(id)){
                System.out.println(aiMessages.getContents());
                return JSONObject.parseObject(aiMessages.getContents());
            }else {
                System.out.println("凭证验证错误或ai的id失效");
                JSONObject returnMessage = new JSONObject();
                returnMessage.put("role", "model");
                returnMessage.put("content", "token_failed");
                returnJSON.getJSONArray("messages").add(returnMessage);
            }
        }catch (Exception e){
            e.printStackTrace();
            JSONObject returnMessage = new JSONObject();
            returnMessage.put("role", "model");
            returnMessage.put("content", "load_failed");
            returnJSON.getJSONArray("messages").add(returnMessage);
        }
        return returnJSON;
    }

    @Override
    public JSONObject getAiMessagesList(String usrId, String token) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (accountMapper.tokenCheckIn(token).equals(usrId)) {
                returnJSON.putArray("list");

                List<AiMessagesForList> list = aiMapper.getUsrAiList(usrId);
                for (AiMessagesForList aim : list) {

                    JSONObject aimJSON = new JSONObject();
                    aimJSON.put("ai_ms_id", aim.getAi_ms_id());
                    aimJSON.put("mainly", aim.getMainly());

                    returnJSON.getJSONArray("list").add(aimJSON);
                }
                returnJSON.put("response", "success");
            } else {
                returnJSON.put("response", "token_check_failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
        return returnJSON;
    }
}
