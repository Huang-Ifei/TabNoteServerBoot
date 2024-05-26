package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.define.MesType;
import com.tabnote.server.tabnoteserverboot.services.NoteService;
import com.tabnote.server.tabnoteserverboot.services.inteface.NoteServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@Controller
public class NoteController {
    NoteServiceInterface noteService;

    @Autowired
    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/note")
    public ResponseEntity<String> noteController(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(requestBody);
        int mesType = jsonObject.getIntValue("mesType");
        if (mesType == MesType.notesRequest) {
            System.out.println("MesType.notesRequest:" + request.getRemoteAddr());
            return sendMes(noteService.notesRequest(jsonObject));
        } else if (mesType == MesType.historyNotesRequest) {
            System.out.println("MesType.historyNotesRequest:" + request.getRemoteAddr());
            return sendMes(noteService.historyNotesRequest(jsonObject));
        } else if (mesType == MesType.addNote) {
            System.out.println("MesType.addNote:" + request.getRemoteAddr());
            return sendMes(noteService.addNote(jsonObject));
        } else if (mesType == MesType.addHisNote) {
            System.out.println("MesType.addHisNote:" + request.getRemoteAddr());
            return sendMes(noteService.addHistoryNote(jsonObject));
        } else if (mesType == MesType.deleteNote) {
            System.out.println("MesType.deleteNote:" + request.getRemoteAddr());
            return sendMes(noteService.deleteNote(jsonObject));
        } else if (mesType == MesType.resetNote) {
            System.out.println("MesType.resetNote:" + request.getRemoteAddr());
            return sendMes(noteService.resetNote(jsonObject));
        } else if (mesType == MesType.finishNote) {
            System.out.println("MesType.finishNote:" + request.getRemoteAddr());
            return sendMes(noteService.finishNote(jsonObject));
        } else {
            System.out.println("MesType.err" + request.getRemoteAddr());
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
