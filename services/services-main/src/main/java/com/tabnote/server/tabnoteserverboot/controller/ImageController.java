package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.services.inteface.FileServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@CrossOrigin
@Controller
public class ImageController {
    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }

    FileServiceInterface fileService;
    @Autowired
    public void setFileService(FileServiceInterface fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/tabNoteImg")
    public ResponseEntity<byte[]> getTabNoteImg(@RequestParam String name) throws Exception {
        log.info("tabNoteImg");
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Path.of("tabNoteImgs/" + name + ".jpg"));
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
        } catch (Exception e) {
            bytes = new byte[0];
            log.error(e.getMessage());
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }

    @PostMapping("/insertTabNoteImg")
    public ResponseEntity<String> insertTabNoteImg(@RequestBody String body) throws Exception {
        log.info("insert tabNoteImg");
        try {
            JSONObject json = JSONObject.parseObject(body);
            return ResponseEntity.ok().body(fileService.insertImgWithOutIdCheck(json.getString("img")));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.ok().body("failed");
        }
    }

    @PostMapping("/upload_tab_note_img")
    public ResponseEntity<String> uploadImg(HttpServletRequest request) {
        log.info("upload_tb_img");
        try{

            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

            log.info(""+multipartRequest.getMultiFileMap().toSingleValueMap().values().toArray().length);

            MultipartFile file = multipartRequest.getFile(multipartRequest.getFileNames().next());

            if(file.getSize()>5000000){
                return sendErr();
            }
            return sendMes(fileService.saveImg(file));
        }catch (Exception e){
            log.error(e.getMessage());
            return sendErr();
        }
    }

    @GetMapping("/image")
    public ResponseEntity<byte[]> getImage(@RequestParam String name) throws Exception {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Path.of("images/" + name));
        } catch (Exception e) {
            try {
                bytes = Files.readAllBytes(Path.of("images/TabNoteBook.jpg"));
            } catch (IOException ex) {
                bytes = new byte[0];
            }
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }

    @GetMapping("/accountImg")
    public ResponseEntity<byte[]> getAccountImg(@RequestParam String id)  throws Exception {
        byte[] bytes;
        try{
            bytes = Files.readAllBytes(Path.of("accountImg/"+id+".jpg"));
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
        }catch (NoSuchFileException e){
            try {
                bytes = Files.readAllBytes(Path.of("accountImg/basic.jpg"));
            } catch (IOException ex) {
                log.error(e.getMessage());
                bytes = new byte[0];
            }
        }
        catch (Exception e){
            bytes = new byte[0];
            log.error(e.getMessage());
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }
    @GetMapping("/usrImg")
    public ResponseEntity<byte[]> getUsrImg(@RequestParam String name) throws Exception {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Path.of("usrImg/" + name));
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
        } catch (NoSuchFileException e) {
            try {
                bytes = Files.readAllBytes(Path.of("usrImg/basic.jpg"));
            } catch (IOException ex) {
                log.error(e.getMessage());
                bytes = new byte[0];
            }
        } catch (Exception e) {
            bytes = new byte[0];
            log.error(e.getMessage());
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }
}
