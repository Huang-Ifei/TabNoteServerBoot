package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.models.TabNote;
import com.tabnote.server.tabnoteserverboot.services.TabNoteService;
import com.tabnote.server.tabnoteserverboot.services.inteface.TabNoteServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
public class TabNoteController {

    TabNoteServiceInterface tabNoteService;

    @Autowired
    public void setTabNoteService(TabNoteService tabNoteService) {
        this.tabNoteService = tabNoteService;
    }

    @GetMapping("getClasses")
    public ResponseEntity<String> getClasses(HttpServletRequest request) {
        System.out.println(request.getRemoteAddr() + "get_classes");
        try {
            return sendMes(tabNoteService.getClasses());
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("tab_note_add")
    public ResponseEntity<String> addTabNote(@RequestBody String requestBody, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr() + "add_tab_note");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(tabNoteService.insertTabNote(jsonObject.getString("token"), jsonObject.getString("id"), request.getRemoteAddr(), jsonObject.getString("class_name"), jsonObject.getString("tab_note_name"), jsonObject.getString("tags"), jsonObject.getString("tab_note")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("tab_note_update")
    public ResponseEntity<String> updateTabNote(@RequestBody String requestBody, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr() + "tab_note_update");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(tabNoteService.updateTabNote(jsonObject.getString("tab_note_id"), request.getRemoteAddr(), jsonObject.getString("tab_note_name"), jsonObject.getString("tags"), jsonObject.getString("tab_note"), jsonObject.getString("date_time")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("tab_note_page")
    public ResponseEntity<String> getTabNotePage(@RequestBody String requestBody, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr() + "tab_note_page");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);

            if (jsonObject.getString("type").equals("main")){
                return sendMes(tabNoteService.getPageTabNotes(jsonObject.getInteger("page")));
            } else if (jsonObject.getString("type").equals("search")){
                return sendMes(tabNoteService.searchTabNote(jsonObject.getString("key_word"), jsonObject.getInteger("page")));
            } else if (jsonObject.getString("type").equals("search_by_class")){
                return sendMes(tabNoteService.searchTabNoteByClass(jsonObject.getString("class_name"), jsonObject.getInteger("page")));
            } else if (jsonObject.getString("type").equals("search_by_id")){
                return sendMes(tabNoteService.searchTabNoteById(jsonObject.getString("id"), jsonObject.getInteger("page")));
            } else {
                return sendErr();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @GetMapping("tab_note")
    public ResponseEntity<String> getTabNote(@RequestParam String id, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr() + "tab_note");
        try {
            return sendMes(tabNoteService.getTabNote(id));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("tab_note_click")
    public ResponseEntity<String> clickTabNote(@RequestBody String requestBody, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr() + "tab_note_click");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(tabNoteService.clickTabNote(jsonObject.getString("tab_note_id"), jsonObject.getString("id"), jsonObject.getString("token")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("tab_note_like")
    public ResponseEntity<String> likeTabNote(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        System.out.println(request.getRemoteAddr() + "tab_note_like");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(tabNoteService.likeTabNote(jsonObject.getString("tab_note_id"), jsonObject.getString("id"), jsonObject.getString("token")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
