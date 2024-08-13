package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.define.AiList;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.AiMapper;
import com.tabnote.server.tabnoteserverboot.services.AiService;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import static com.tabnote.server.tabnoteserverboot.define.AiList.*;

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

    @GetMapping("list")
    public ResponseEntity<String> getAiList(HttpServletRequest request) {
        System.out.println("GetAiList" + request.getRemoteAddr());
        JSONObject jsonObject = new JSONObject();
        jsonObject.putArray("ai_list");
        for (int i = 0; i < name.length; i++) {
            JSONObject json = new JSONObject();
            json.put("id", i);
            json.put("name", name[i]);
            jsonObject.getJSONArray("ai_list").add(json);
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jsonObject.toString());
    }

    @GetMapping("talk")
    public void sendMess(@RequestParam String content, HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println(request.getRemoteAddr() + ":Ai_talk");
        try {
            //配置代理
            String url = GENERATE_API_URL  + GOOGLE_API_KEY;
            URL uRL = new URL(url);
            String requestBody = "{\"model\": \"models/gemini-pro\",\"contents\":[{\"parts\":[{\"text\":\"" + content + "\"}]}]}";
            OutputStream os;
            InputStream is;
            BufferedReader br;

            HttpURLConnection conn = (HttpURLConnection) uRL.openConnection(proxy);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(30000);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            os = conn.getOutputStream();
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            os.flush();
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String temp;
            while (null != (temp = br.readLine())) {
                System.out.println(temp);
                response.getWriter().write(temp);
                response.getWriter().write("\n");
                response.getWriter().flush();
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("failed");
        }
    }

    @PostMapping("note")
    public void getNoteAiRequest(HttpServletRequest request,HttpServletResponse response,@RequestBody String body) throws Exception{
        System.out.println(request.getRemoteAddr()+":note_ai");
        try {
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject(body);
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(bodyJson.getString("id"),bodyJson.getString("token"))) {
                //确定模型
                String model = aiService.modelDefine(bodyJson);
                //将请求JSON变为向API发送的JSON
                JSONArray messages = bodyJson.getJSONArray("messages");
                JSONObject requestJson = aiService.buildRequestJSON(messages,model);
                StringBuffer sb = new StringBuffer();
                //抄送给API
                aiService.postAiMessagesToAPI(requestJson,response,sb);
                response.getWriter().write("");
                response.getWriter().flush();
                //如果ai反馈非空且，数据库操作
                if (!sb.isEmpty()){

                }
            }else {
                JSONObject returnJSON = new JSONObject();
                JSONObject returnMessage = new JSONObject();
                returnJSON.put("model", "server_security_admin");
                returnMessage.put("content", "Token check failed，Please login again，token过期请重新登录");
                returnJSON.put("message", returnMessage);
                //把封装好的JSON送回
                response.getWriter().write(returnJSON.toString());
                response.getWriter().write("\n");
                response.getWriter().flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject returnJSON = new JSONObject();
            JSONObject returnMessage = new JSONObject();

            returnMessage.put("content","failed");
            returnJSON.put("message",returnMessage);
            response.getWriter().write(returnJSON.toString());
            response.getWriter().write("\n");
            response.getWriter().flush();
        }
        response.getWriter().close();
    }

    //分布式AI计算信息获取端口
    @PostMapping("dc")
    public void getAIDCRequest(HttpServletRequest request,HttpServletResponse response,@RequestBody String body) throws Exception{
        System.out.println(request.getRemoteAddr()+":AIDC");
        Socket socket = null;
        BufferedReader br = null;
        BufferedOutputStream bos = null;
        try {
            response.addHeader("content-type", "text/html;charset=utf-8");
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject(body);
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(bodyJson.getString("id"),bodyJson.getString("token"))) {
                //将请求JSON变为向API发送的JSON
                JSONArray messages = bodyJson.getJSONArray("messages");
                String s = JSONObject.parseObject(messages.get(0).toString()).getString("content");
                s += "(！！！注意！！！如果不是英语问题，请使用中文回答)";
                StringBuffer sb = new StringBuffer();
                //抄送给分布式计算中心
                socket = new Socket("127.0.0.1", 11713);
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bos = new BufferedOutputStream(socket.getOutputStream());
                bos.write(s.getBytes());
                bos.flush();
                socket.shutdownOutput();
                String temp;
                while (null != (temp = br.readLine())) {
                    //如果是电脑信息
                    if (temp.startsWith("{\"computerName\":")){
                        response.getWriter().write(temp);
                        response.getWriter().write("\n");
                        response.getWriter().flush();
                    }else{
                        //普通ai信息
                        JSONObject json = JSONObject.parseObject(temp);
                        String c = json.getString("response");
                        //封装
                        JSONObject returnJSON = new JSONObject();
                        JSONObject returnMessage = new JSONObject();
                        returnJSON.put("model", "gemma");
                        returnMessage.put("content", c);
                        returnJSON.put("message", returnMessage);
                        //把封装好的JSON送回
                        response.getWriter().write(returnJSON.toString());
                        response.getWriter().write("\n");
                        response.getWriter().flush();
                    }
                }
                bos.close();
                br.close();
                socket.close();

                response.getWriter().write("");
                response.getWriter().flush();
                //如果ai反馈非空且，数据库操作
                if (!sb.isEmpty()){

                }
            }else {
                JSONObject returnJSON = new JSONObject();
                JSONObject returnMessage = new JSONObject();
                returnJSON.put("model", "server_security_admin");
                returnMessage.put("content", "Token check failed，Please login again，token过期请重新登录");
                returnJSON.put("message", returnMessage);
                //把封装好的JSON送回
                response.getWriter().write(returnJSON.toString());
                response.getWriter().write("\n");
                response.getWriter().flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject returnJSON = new JSONObject();
            JSONObject returnMessage = new JSONObject();

            returnMessage.put("content","failed");
            returnJSON.put("message",returnMessage);
            response.getWriter().write(returnJSON.toString());
            response.getWriter().write("\n");
            response.getWriter().flush();
        } finally {
            if (bos != null) {
                bos.close();
            }
            if (br != null) {
                br.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
        response.getWriter().close();
    }

    @PostMapping("messages")
    public void sendMesses(HttpServletRequest request, HttpServletResponse response, @RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":Ai_Messages");
        try {
            response.addHeader("content-type", "text/html;charset=utf-8");
            //变成JSON对象
            JSONObject bodyJson = JSONObject.parseObject(body);
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(bodyJson.getString("id"),bodyJson.getString("token"))) {
                //确定模型
                String model = aiService.modelDefine(bodyJson);
                //将请求JSON变为向API发送的JSON
                JSONArray messages = bodyJson.getJSONArray("messages");
                JSONObject requestJson = aiService.buildRequestJSON(messages,model);
                StringBuffer sb = new StringBuffer();
                //抄送给API
                aiService.postAiMessagesToAPI(requestJson,response,sb);
                response.getWriter().write("");
                response.getWriter().flush();
                //如果ai反馈非空且，数据库操作
                if (!sb.isEmpty()){
                    if (messages.size() == 1 && bodyJson.containsKey("id") && (bodyJson.containsKey("ai_ms_id")&&bodyJson.getString("ai_ms_id").isEmpty())) {
                        JSONObject messageJson = new JSONObject();
                        messageJson.put("role","model");
                        messageJson.put("content",sb.toString());
                        messages.add(messageJson);
                        response.getWriter().write(aiService.createMessages(messages,bodyJson.getString("id"),request.getRemoteAddr()+request.getRemotePort()));
                        response.getWriter().flush();
                    } else if (messages.size() > 1 || (bodyJson.containsKey("ai_ms_id")&&!bodyJson.getString("ai_ms_id").isEmpty()) ){
                        JSONObject messageJson = new JSONObject();
                        messageJson.put("role","model");
                        messageJson.put("content",sb.toString());
                        messages.add(messageJson);
                        aiService.changeMessages(messages,bodyJson.getString("ai_ms_id"));
                    }
                }
            }else {
                JSONObject returnJSON = new JSONObject();
                JSONObject returnMessage = new JSONObject();
                returnJSON.put("model", "server_security_admin");
                returnMessage.put("content", "Token check failed，Please login again");
                returnJSON.put("message", returnMessage);
                //把封装好的JSON送回
                response.getWriter().write(returnJSON.toString());
                response.getWriter().write("\n");
                response.getWriter().flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JSONObject returnJSON = new JSONObject();
            JSONObject returnMessage = new JSONObject();

            returnMessage.put("content","failed");
            returnJSON.put("message",returnMessage);
            response.getWriter().write(returnJSON.toString());
            response.getWriter().write("\n");
            response.getWriter().flush();
        }
        response.getWriter().close();
    }

    @PostMapping("get_history")
    public ResponseEntity<String> getAiHistory(HttpServletRequest request,@RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":get_ai_history");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");

        return sendMes(aiService.getAiMessagesList(id,token));
    }

    @PostMapping("history")
    public ResponseEntity<String> getAiHistoryMessages(HttpServletRequest request,@RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":get_history_ai_messages");
        JSONObject requestJson = JSONObject.parseObject(body);
        String ai_ms_id = requestJson.getString("ai_ms_id");
        String token = requestJson.getString("token");
        return sendMes(aiService.getAiMessages(ai_ms_id,token));
    }

    @PostMapping("note_sync")
    public ResponseEntity<String> noteAiSync(HttpServletRequest request,@RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":note_sync");
        try {
            JSONObject bodyJson = JSONObject.parseObject(body);
            return sendMes(aiService.noteAiSync(bodyJson.getString("note_ai_id"),bodyJson.getString("note"),bodyJson.getJSONArray("note_ticks"),bodyJson.getString("token"),bodyJson.getString("id"),bodyJson.getString("note_content")));
        }catch (Exception e){
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("get_note_history")
    public ResponseEntity<String> getAiNoteHistory(HttpServletRequest request,@RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":get_ai_note_history");
        JSONObject requestJson = JSONObject.parseObject(body);

        String id = requestJson.getString("id");
        String token = requestJson.getString("token");

        return sendMes(aiService.getNoteAiHistory(id,token));
    }

    @PostMapping("note_history")
    public ResponseEntity<String> getAiHistoryNote(HttpServletRequest request,@RequestBody String body) throws Exception {
        System.out.println(request.getRemoteAddr() + ":get_history_ai_note");
        JSONObject requestJson = JSONObject.parseObject(body);
        String note_ai_id = requestJson.getString("note_ai_id");
        String token = requestJson.getString("token");
        return sendMes(aiService.getHistoryNoteAi(note_ai_id,token));
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
