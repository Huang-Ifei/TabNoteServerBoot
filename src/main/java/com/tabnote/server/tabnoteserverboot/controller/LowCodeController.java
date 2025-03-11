package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.services.inteface.LowCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@CrossOrigin
@Controller
@RequestMapping("low_code")
public class LowCodeController {
    LowCode lowCode;
    @Autowired
    public void setLowCode(LowCode lowCode) {
        this.lowCode = lowCode;
    }

    @PostMapping("huffman")
    public ResponseEntity<String> huffman(HttpServletRequest request) {
        JSONObject jsonObject = JSONObject.parseObject((String) request.getAttribute("body"));
        return sendMes(lowCode.insertHuffmanLCID(jsonObject.getString("id"), jsonObject.getString("token"), jsonObject.getString("language"), jsonObject.getString("environment"), jsonObject.getString("save")));
    }

    @GetMapping("file")
    public ResponseEntity<Resource> file(@RequestParam String lc_id) {
        Resource resource = lowCode.getFile(lc_id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + resource.getFilename());
        try {
            long contentLength = resource.contentLength();
            headers.setContentLength(contentLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 创建ResponseEntity对象
        ResponseEntity<Resource> response = new ResponseEntity<>(resource, headers, HttpStatus.OK);
        return response;
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
