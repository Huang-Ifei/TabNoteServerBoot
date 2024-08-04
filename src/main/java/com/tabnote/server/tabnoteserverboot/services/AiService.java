package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.AiMapper;
import com.tabnote.server.tabnoteserverboot.models.AiMessages;
import com.tabnote.server.tabnoteserverboot.models.AiMessagesForList;
import com.tabnote.server.tabnoteserverboot.models.NoteAi;
import com.tabnote.server.tabnoteserverboot.models.NoteAiForList;
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

    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tie) {
        this.tabNoteInfiniteEncryption = tie;
    }

    //确定模型
    @Override
    public String modelDefine(JSONObject bodyJson) {
        String model = "models/gemini-1.5-flash-001";
        if (bodyJson.containsKey("model") && bodyJson.containsKey("token")) {
            if (bodyJson.getString("model").startsWith("gemini-1.5-pro")) {
                if (aiRequestCounter.proAiRequestCheck()) {
                    model = "models/gemini-1.5-pro-001";
                    return model;
                }
            }
        }
        if (bodyJson.containsKey("preview") && bodyJson.getBoolean("preview").equals(true)) {
            if (bodyJson.getString("model").startsWith("gemini-1.5-pro")) {
                if (aiRequestCounter.proAiRequestCheck()) {
                    model = "models/gemini-1.5-pro-preview-0514";
                    return model;
                }
            } else {
                model = "models/gemini-1.5-flash-preview-0514";
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
        connection.setReadTimeout(180000);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        os = connection.getOutputStream();
        os.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();
        if (connection.getResponseCode() == 200) {
            if (requestJson.getString("model").equals("models/gemini-1.5-pro-001")) {
                aiRequestCounter.addProAiRequest();
            } else if (requestJson.getString("model").equals("models/gemini-1.5-flash-001")) {
                aiRequestCounter.addFlashAiRequest();
            }
            System.out.println(requestJson.getString("model"));
            response.addHeader("content-type", "text/html;charset=utf-8");

            is = connection.getInputStream();
            if (null != is) {
                Integer totalTokenCount = 0;
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String temp;
                while (null != (temp = br.readLine())) {
                    if (!temp.equals("\n") && !temp.isEmpty()) {
                        StringBuffer responseJSON = new StringBuffer();
                        //不解析"data:"
                        for (int i = 5; i < temp.length(); i++) {
                            responseJSON.append(temp.charAt(i));
                        }
                        System.out.println(responseJSON);
                        JSONObject returnJSON = new JSONObject();
                        JSONObject returnMessage = new JSONObject();
                        //如果因为安全原因被拦截
                        if (responseJSON.toString().contains("\"finishReason\": \"SAFETY\"")) {
                            returnJSON.put("model", requestJson.getString("model"));
                            returnMessage.put("content", "\n**因为安全问题，消息回复被暂停**");
                            returnJSON.put("message", returnMessage);
                            //添加到string buffer里面
                            returnString.append("\n**因为安全问题，消息回复被暂停**");
                            //把封装好的JSON送回
                            response.getWriter().write(returnJSON.toString());
                            response.getWriter().write("\n");
                            response.getWriter().flush();
                        } else {
                            JSONObject tempJSON = JSONObject.parseObject(responseJSON.toString());
                            if (tempJSON != null) {
                                try {
                                    String returnMess = tempJSON.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
                                    //封装
                                    returnJSON.put("model", requestJson.getString("model"));
                                    returnMessage.put("content", returnMess);
                                    returnJSON.put("message", returnMessage);
                                    //添加到string buffer里面
                                    returnString.append(returnMess);
                                    totalTokenCount = tempJSON.getJSONObject("usageMetadata").getInteger("totalTokenCount");
                                    //把封装好的JSON送回
                                    response.getWriter().write(returnJSON.toString());
                                    response.getWriter().write("\n");
                                    response.getWriter().flush();
                                } catch (NullPointerException e) {
                                    break;
                                }
                            }
                        }
                    }
                }
                br.close();
            }
        } else if (connection.getResponseCode() == 500 || connection.getResponseCode() == 429) {
            //500,429响应码降级
            this.downgradeAiModel(requestJson, response, returnString);
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

    //降级处理，要更改返回的model类型为对应类型，并重新交给postAiMessagesToAPI方法
    public void downgradeAiModel(JSONObject requestJson, HttpServletResponse response, StringBuffer returnString) throws Exception {
        if (requestJson.getString("model").equals("models/gemini-1.5-pro-001")) {
            System.out.println("try_change_flash_model");
            requestJson.put("model", "models/gemini-1.5-flash-001");
            postAiMessagesToAPI(requestJson, response, returnString);
        } else if (requestJson.getString("model").equals("models/gemini-1.5-flash-001")) {
            System.out.println("try_change_1.0_model");
            requestJson.put("model", "models/gemini-1.0-pro-002");
            postAiMessagesToAPI(requestJson, response, returnString);
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

    //创建新的对话，返回一个ai ms id
    @Override
    public String createMessages(JSONArray messages, String id, String ip) {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String aiMsId = String.valueOf(messages.hashCode()) + String.valueOf(id.hashCode()) + String.valueOf(ip.hashCode());
        String mainly;
        String firstContent = JSONObject.parseObject(messages.get(0).toString()).getString("content");
        if (firstContent.length() < 20) {
            mainly = firstContent;
        } else {
            mainly = firstContent.substring(0, 18);
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
        return "{\"response\":\"" + aiMsId + "\"}";
    }

    //同步对话内容
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

    //使用id将所有某个对话的所有内容传回
    @Override
    public JSONObject getAiMessages(String aiMsId, String token) {
        JSONObject returnJSON = new JSONObject();
        returnJSON.putArray("messages");
        try {
            AiMessages aiMessages = aiMapper.getUsrAiMessages(aiMsId);
            if (aiMessages != null && aiMessages.getUsr_id().equals(tabNoteInfiniteEncryption.encryptionTokenGetId(token))) {
                System.out.println(aiMessages.getContents());
                return JSONObject.parseObject(aiMessages.getContents());
            } else {
                System.out.println("凭证验证错误或ai的id失效");
                JSONObject returnMessage = new JSONObject();
                returnMessage.put("role", "model");
                returnMessage.put("content", "token_failed");
                returnJSON.getJSONArray("messages").add(returnMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject returnMessage = new JSONObject();
            returnMessage.put("role", "model");
            returnMessage.put("content", "load_failed");
            returnJSON.getJSONArray("messages").add(returnMessage);
        }
        return returnJSON;
    }

    //使用usr id获取所有的对话及其主要的内容
    @Override
    public JSONObject getAiMessagesList(String usrId, String token) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(usrId, token)) {
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

    @Override
    public JSONObject noteAiSync(String note_ai_id, String note, JSONArray note_ticks, String token, String usrId) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(usrId, token)) {
                if (note_ai_id.isEmpty()) {
                    String mainly;
                    if (note.length() < 20) {
                        mainly = note;
                    } else {
                        mainly = note.substring(0, 18);
                    }
                    String new_note_id = usrId.hashCode() + "" + System.currentTimeMillis();
                    aiMapper.addNewNoteAI(new_note_id, usrId, mainly, note, note_ticks.toString());
                    System.out.println(new_note_id);
                    returnJSON.put("note_ai_id", new_note_id);
                } else {
                    if (note_ai_id.startsWith(String.valueOf(usrId.hashCode()))) {
                        aiMapper.changeNoteAI(note, note_ticks.toString(), note_ai_id);
                    }
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

    @Override
    public JSONObject getNoteAiHistory(String usrId, String token) {
        JSONObject returnJSON = new JSONObject();
        returnJSON.putArray("list");
        JSONArray list = returnJSON.getJSONArray("list");
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(usrId, token)) {
                List<NoteAiForList> usrNoteAiList = aiMapper.getUsrNoteAiList(usrId);
                for (NoteAiForList noteAiForList : usrNoteAiList) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("note_ai_id", noteAiForList.getNote_ai_id());
                    jsonObject.put("mainly", noteAiForList.getMainly());
                    jsonObject.put("date_time", noteAiForList.getDate_time());

                    list.add(jsonObject);
                }
                returnJSON.put("response", "success");
            } else {
                returnJSON.put("response", "token_check_failed");
            }
        } catch (NullPointerException ee) {
            returnJSON.put("response", "token_check_failed");
        } catch (Exception e) {
            returnJSON.put("response", "failed");
            e.printStackTrace();
        }
        return returnJSON;
    }

    @Override
    public JSONObject getHistoryNoteAi(String noteAiId, String token) {
        JSONObject returnJSON = new JSONObject();
        returnJSON.putArray("messages");
        try {
            String id = tabNoteInfiniteEncryption.encryptionTokenGetId(token);
            NoteAi noteAi = aiMapper.getUsrNoteAi(noteAiId);
            if (noteAi.getUsr_id().equals(id)) {

                returnJSON.put("note", noteAi.getNote());
                returnJSON.getJSONArray("messages").addAll(JSONArray.parse(noteAi.getAi_mess()));
                returnJSON.put("date_time", noteAi.getDate_time());

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
