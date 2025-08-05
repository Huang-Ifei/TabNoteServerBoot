package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.services.inteface.FileServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@CrossOrigin
@Controller
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    FileServiceInterface fileService;
    @Autowired
    public void setFileService(FileServiceInterface fileService) {
        this.fileService = fileService;
    }
    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tabNoteInfiniteEncryption) {
        this.tabNoteInfiniteEncryption = tabNoteInfiniteEncryption;
    }

    @GetMapping("file_select")
    public ResponseEntity<Resource> getTabNoteFile(@RequestParam String name, HttpServletRequest request) throws Exception {
        log.info("download file" + tabNoteInfiniteEncryption.proxyGetIp(request));
        // 创建Resource对象
        Resource resource = new FileSystemResource("tabNoteFiles/" + name +".zip");
        // 设置下载文件头信息
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + resource.getFilename());
        try {
            long contentLength = resource.contentLength();
            headers.setContentLength(contentLength);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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
