package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.AiMapper;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.models.*;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static com.tabnote.server.tabnoteserverboot.define.AiList.*;

@Service
public class AiService implements AiServiceInterface {
    AiMapper aiMapper;
    AiRequestCounter aiRequestCounter;
    AccountMapper accountMapper;
    VipMapper vipMapper;

    @Autowired
    public void setVipMapper(VipMapper vipMapper) {
        this.vipMapper = vipMapper;
    }

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

    //将请求JSON变为向ChatGPT API发送的JSON
    @Override
    public JSONObject buildChatGPTRequestJSON(JSONArray messages, String model) {
        JSONObject requestJson = new JSONObject();
        if (model.equals("gpt-4o") || model.equals("gpt-4o-2024-08-06")) {
            requestJson.put("model", modelList[0]);
        } else if (model.equals("gpt-4o-mini")) {
            requestJson.put("model", modelList[1]);
        } else if (model.equals("o1-mini")) {
            requestJson.put("model", modelList[2]);
        }

        requestJson.put("stream", true);
        JSONObject usageUpJson = new JSONObject();
        usageUpJson.put("include_usage", true);
        requestJson.put("stream_options", usageUpJson);
        requestJson.putArray("messages");
        JSONArray contents = requestJson.getJSONArray("messages");

        if (!model.equals("o1-mini")) {
            JSONObject sysJson = new JSONObject();
            sysJson.put("role", "system");
            sysJson.put("content", "You are a helpful assistant!您的第一语言设定为中文。");
            contents.add(sysJson);
        }

        for (int i = 0; i < messages.size(); i++) {
            JSONObject message = messages.getJSONObject(i);
            JSONObject userJson = new JSONObject();
            //原先Gemini的兼容，chatgpt不可使用model角色
            if (message.getString("role").equals("model")) {
                userJson.put("role", "assistant");
            } else {
                userJson.put("role", message.getString("role"));
            }
            userJson.put("content", message.get("content"));
            contents.add(userJson);
        }
        return requestJson;
    }

    //抄送给ChatGPT API
    @Override
    public int postAiMessagesToChatGPTAPI(JSONObject requestJson, HttpServletResponse response, StringBuffer returnString) throws Exception {
        String url = "https://api.openai.com/v1/chat/completions";
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
        connection.setRequestProperty("Authorization", "Bearer " + CHATGPT_API_KEY);
        os = connection.getOutputStream();
        System.out.println("request" + requestJson.toString());
        os.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();
        if (connection.getResponseCode() == 200) {
            if (response != null){
                response.addHeader("content-type", "text/html;charset=utf-8");
            }

            is = connection.getInputStream();
            if (null != is) {
                int quotaCost = 0;
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                String temp;
                while (null != (temp = br.readLine())) {
                    if (!temp.equals("\n") && !temp.isEmpty()) {
                        if (temp.equals("data: [DONE]")) {
                            break;
                        }
                        StringBuffer responseJSON = new StringBuffer();
                        //不解析"data:"
                        for (int i = 5; i < temp.length(); i++) {
                            responseJSON.append(temp.charAt(i));
                        }
                        JSONObject returnJSON = new JSONObject();
                        JSONObject returnMessage = new JSONObject();

                        JSONObject tempJSON = JSONObject.parseObject(responseJSON.toString());
                        if (tempJSON != null) {
                            try {
                                JSONArray choices = tempJSON.getJSONArray("choices");
                                //如果choices是空的，那么这就是最后一个计数条计算quota
                                if (choices.isEmpty()) {
                                    quotaCost = countQuota(tempJSON, requestJson);
                                } else {
                                    //找到回报的信息
                                    String returnMess = choices.getJSONObject(0).getJSONObject("delta").getString("content");
                                    if (returnMess != null) {
                                        //封装
                                        returnJSON.put("model", requestJson.getString("model"));
                                        returnMessage.put("content", returnMess);
                                        returnJSON.put("message", returnMessage);
                                        //添加到string buffer里面
                                        returnString.append(returnMess);
                                        //如果usage不等于null就可以计算quota了
                                        quotaCost = countQuota(tempJSON, requestJson);
                                        //把封装好的JSON送回
                                        if (response != null) {
                                            response.getWriter().write(returnJSON.toString());
                                            response.getWriter().write("\n");
                                            response.getWriter().flush();
                                        }
                                    }
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                }
                //把封装好的JSON送回
                if (response != null) {
                    response.getWriter().write("{\"response\":\"success\"}");
                    response.getWriter().write("\n");
                    response.getWriter().flush();
                }
                br.close();
                return quotaCost;
            }
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
            if (response != null) {
                response.getWriter().write(returnJSON.toString());
                response.getWriter().write("\n");
                response.getWriter().flush();
            }
        }
        return 0;
    }

    public int countQuota(JSONObject tempJSON, JSONObject requestJson) {
        int quotaCost = 0;
        if (tempJSON.containsKey("usage") && tempJSON.get("usage") != null) {
            if (requestJson.getString("model").equals(modelList[0])) {
                quotaCost = tempJSON.getJSONObject("usage").getInteger("prompt_tokens") * 18 + tempJSON.getJSONObject("usage").getInteger("completion_tokens") * 70;
            } else if (requestJson.getString("model").equals(modelList[1])) {
                quotaCost = tempJSON.getJSONObject("usage").getInteger("prompt_tokens") + tempJSON.getJSONObject("usage").getInteger("completion_tokens") * 4;
            } else if (requestJson.getString("model").equals(modelList[2])) {
                quotaCost = tempJSON.getJSONObject("usage").getInteger("prompt_tokens") * 20 + tempJSON.getJSONObject("usage").getInteger("completion_tokens") * 80;
            }
        }
        return quotaCost;
    }

    //创建新的对话，返回一个ai ms id
    @Override
    public String createMessages(JSONArray messages, String id, String ip) {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String aiMsId = String.valueOf(messages.hashCode()) + String.valueOf(id.hashCode()) + String.valueOf(ip.hashCode());
        String mainly = "";
        JSONObject contents = new JSONObject();

        String firstContent = JSONObject.parseObject(messages.get(0).toString()).getString("content");
        if (firstContent.startsWith("[")) {
            //JSONObject.parseObject(messages.get(0).toString()).getJSONArray("content").getJSONObject(1).getString("text");
            mainly = JSONObject.parseObject(messages.get(1).toString()).getString("content");
        } else if (firstContent.length() < 20) {
            mainly = firstContent;
        } else {
            mainly = firstContent.substring(0, 18);
        }
        contents.putArray("messages");
        contents.getJSONArray("messages").add(messages);

        System.out.println(aiMsId + ":::" + mainly);
        try {
            aiMapper.addNewAiMessages(aiMsId, mainly, id, contents.toString(), dateTime);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "{\"ai_ms_id\":\"" + aiMsId + "\"}";
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
    public JSONObject noteAiSync(String note_ai_id, String note, JSONArray note_ticks, String token, String usrId, String note_content) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(usrId, token)) {
                if (note_ai_id.isEmpty()) {
                    String mainly;
                    if (note_content.length() < 20) {
                        mainly = note_content;
                    } else {
                        mainly = note_content.substring(0, 18);
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

    @Override
    public JSONObject getDXSTJ(String id, String token, String base64Img) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (base64Img.startsWith("data:image/jpeg;base64,")) {
                base64Img = base64Img.substring("data:image/jpeg;base64,".length());
            }
            byte[] imageBytes = Base64.getDecoder().decode(base64Img);

            //构建MD5的码
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            byte[] md5Hash = md5Digest.digest(imageBytes);
            String imgMD5 = bytesToHex(md5Hash);

            System.out.println(imgMD5);

            //
            String boundary = "Boundary-" + new Random().nextInt(1000000);
            String newline = "\r\n";
            String boundaryMarker = "--" + boundary;

            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append(boundaryMarker).append(newline)
                    .append("Content-Disposition: form-data; name=\"appId\"").append(newline)
                    .append(newline)
                    .append("collegepcpi").append(newline)
                    .append(boundaryMarker).append(newline)
                    .append("Content-Disposition: form-data; name=\"picMD5\"").append(newline)
                    .append(newline)
                    .append(imgMD5).append(newline)
                    .append(boundaryMarker).append(newline)
                    .append("Content-Disposition: form-data; name=\"image\"; filename=\"blob.jpeg\"").append(newline)
                    .append("Content-Type: image/jpeg").append(newline)
                    .append(newline);
            byte[] imagePartHeader = bodyBuilder.toString().getBytes("UTF-8");

            String endMarker = newline + boundaryMarker + "--" + newline;

            byte[] body = new byte[imagePartHeader.length + imageBytes.length + endMarker.getBytes("UTF-8").length];
            System.arraycopy(imagePartHeader, 0, body, 0, imagePartHeader.length);
            System.arraycopy(imageBytes, 0, body, imagePartHeader.length, imageBytes.length);
            System.arraycopy(endMarker.getBytes("UTF-8"), 0, body, imagePartHeader.length + imageBytes.length, endMarker.getBytes("UTF-8").length);
            //构建请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://www.daxuesoutijiang.com/dxtools/pc/picsearch"))
                    .header("Cookie", "DXUSS=BGAIFUwSksvNT-Z0M64W91e8a6xeHWmwZUDJ-FnVwUndmeGOmpUfLXORiYaqFeMQ")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            //打印
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject json = JSONObject.parseObject(response.body());
            //查找到的内容
            JSONObject qsData = JSONObject.parseObject(json.getJSONObject("data").getJSONObject("feInfo").getString("questionData"));
            //获取关键内容并封装
            String text = qsData.getString("text");
            JSONArray jsonArray = returnJSON.putArray("questionList");
            jsonArray.addAll(qsData.getJSONArray("questionList"));

            returnJSON.put("text", text);
            returnJSON.put("questionList", jsonArray);

            returnJSON.put("response", "success");
            System.out.println(returnJSON);
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }

        return returnJSON;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public JSONObject insertBQWithOutTokenCheck(BQ beatQuestion) {
        JSONObject returnJSON = new JSONObject();
        try {
            aiMapper.insertBQ(beatQuestion);
            returnJSON.put("response", "success");

        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }

        return returnJSON;
    }

    @Override
    public JSONObject insertBQ(BQ beatQuestion, String usrId, String token) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(usrId, token)) {
                aiMapper.insertBQ(beatQuestion);
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
    public JSONObject getBQListByUserId(String usrId, String token, int index) {
        JSONObject returnJSON = new JSONObject();
        JSONArray jsonArray = returnJSON.putArray("list");
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(usrId, token)) {
                List<BQForList> bqListByUserId = aiMapper.getBQListByUserId(usrId, index);

                for (BQForList bq : bqListByUserId) {
                    JSONObject json = JSONObject.from(bq);
                    jsonArray.add(json);
                }

                returnJSON.put("response", "success");
            } else {
                returnJSON.put("response", "token_check_failed");
            }
        } catch (Exception e) {
            returnJSON.put("response", "failed");
            e.printStackTrace();
        }

        return returnJSON;
    }

    @Override
    public JSONObject getBQ(String bqId, String usrId, String token) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(usrId, token)) {
                BQ bq = aiMapper.getBQById(usrId, bqId);
                returnJSON = JSONObject.from(bq);
                returnJSON.put("dxstj", JSONArray.parse(bq.getDxstj()));

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
    public void returnAdminMess(HttpServletResponse response, String s) throws IOException {
        JSONObject returnJSON = new JSONObject();
        JSONObject returnMessage = new JSONObject();
        returnJSON.put("model", "server_security_admin");
        returnMessage.put("content", s);
        returnJSON.put("message", returnMessage);
        //把封装好的JSON送回
        response.getWriter().write(returnJSON.toString());
        response.getWriter().write("\n");
        response.getWriter().flush();
    }

    @Override
    public void returnErrMess(HttpServletResponse response, String e)throws Exception{
        String errMess = e;
        errMess = errMess.replaceAll("Server returned HTTP response code: 400 for URL: https://api.openai.com/v1/chat/completions","连接AI服务提供商时出错，请稍后重试");
        errMess = errMess.replaceAll("openai", "AI服务提供商");
        errMess = errMess.replaceAll("chatgpt", "AI服务");
        errMess = errMess.replaceAll("chatGPT", "AI服务");
        errMess = errMess.replaceAll("https://api.openai.com/v1/chat/completions","AI服务提供商");

        this.returnAdminMess(response, "failed,服务器内部错误：" + errMess);
    }

    @Transactional
    @Override
    public void useQuota(int quotaCost, String id){
        try{
            //使用for update悲观锁
            String vip_id = vipMapper.selectVipIdByUserId(id);
            vipMapper.useQuota(quotaCost,vip_id);
        } catch (Exception e) {
            throw e;
        }
    }
}
