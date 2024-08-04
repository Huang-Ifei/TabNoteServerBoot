package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.services.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
public class MessageController {

    MessageService messageService;

    @Autowired
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("tab_mess_add")
    public ResponseEntity<String> addTabMess(@RequestBody String requestBody, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr() + "add_tab_mess");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);

            return sendMes(messageService.insertTabNoteMessage(jsonObject.getString("id"), jsonObject.getString("token"), request.getRemoteAddr(), jsonObject.getString("tab_note_id"), jsonObject.getString("message")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("mess_mess_add")
    public ResponseEntity<String> addMessMess(@RequestBody String requestBody, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr() + "add_mess_mess");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(messageService.insertMessageMessage(jsonObject.getString("id"), jsonObject.getString("token"), request.getRemoteAddr(), jsonObject.getString("reply_message_id"), jsonObject.getString("message"), jsonObject.getString("from_tab_mess")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("tab_note_mess")
    public ResponseEntity<String> getTabNoteMess(@RequestBody String requestBody,  HttpServletRequest request) {
        System.out.println(request.getRemoteAddr() + "tab_note_mess");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(messageService.getTabNoteMessage(jsonObject.getString("tab_note_id"), jsonObject.getInteger("start"),jsonObject.getString("usr_id")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("mess_mess")
    public ResponseEntity<String> getMessMess(@RequestBody String requestBody,  HttpServletRequest request) {
        System.out.println(request.getRemoteAddr() + "mess_mess");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(messageService.getMessageMessage(jsonObject.getString("from_tab_mess"), jsonObject.getInteger("start"),jsonObject.getString("usr_id")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("tab_mess_like")
    public ResponseEntity<String> likeTabMess(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        System.out.println(request.getRemoteAddr() + "tab_mess_like");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(messageService.likeTabMess(jsonObject.getString("mess_id"), jsonObject.getString("id"), jsonObject.getString("token")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("mess_mess_like")
    public ResponseEntity<String> likeMessMess(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        System.out.println(request.getRemoteAddr() + "mess_mess_like");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(messageService.likeMessMess(jsonObject.getString("mess_id"), jsonObject.getString("id"), jsonObject.getString("token")));
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
