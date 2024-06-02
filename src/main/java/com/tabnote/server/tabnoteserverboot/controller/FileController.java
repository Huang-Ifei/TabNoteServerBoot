package com.tabnote.server.tabnoteserverboot.controller;


import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@CrossOrigin
@Controller()
public class FileController {
    @PostMapping("insert_tab_note_file")
    public ResponseEntity<String> accountImgSet(@RequestBody String requestBody, HttpServletRequest request) {
        System.out.println("insertFile:" + request.getRemoteAddr());
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendErr();
            //return sendMes(accountService.setAccountImg(jsonObject.getString("id"),jsonObject.getString("token"),jsonObject.getString("base64Img")));
        } catch (Exception e) {
            return sendErr();
        }
    }

    @GetMapping("get_tab_note_file")
    public ResponseEntity<Resource> getTabNoteFile(@RequestParam String tab_note_id, HttpServletRequest request) {
        System.out.println("download file" + request.getRemoteAddr());

        // 获取文件路径
        String filePath = "tabNoteFiles/"+tab_note_id;

        // 创建Resource对象
        Resource resource = new FileSystemResource(filePath);

        // 设置下载文件头信息
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + resource.getFilename());

        // 创建ResponseEntity对象
        ResponseEntity<Resource> response = new ResponseEntity<>(resource, headers, HttpStatus.OK);

        // 返回ResponseEntity对象
        return response;
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
