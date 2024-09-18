package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.services.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }

    FileService fileService;
    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/tabNoteImg")
    public ResponseEntity<byte[]> getTabNoteImg(@RequestParam String name) throws Exception {
        System.out.println("tabNoteImg");
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Path.of("tabNoteImgs/" + name + ".jpg"));
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
        } catch (Exception e) {
            bytes = new byte[0];
            e.printStackTrace();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }

    @PostMapping("/insertTabNoteImg")
    public ResponseEntity<String> insertTabNoteImg(@RequestBody String body) throws Exception {
        System.out.println("insert tabNoteImg");
        try {
            JSONObject json = JSONObject.parseObject(body);
            return ResponseEntity.ok().body(fileService.insertImgWithOutIdCheck(json.getString("img")));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok().body("failed");
        }
    }

    @PostMapping("/upload_tab_note_img")
    public ResponseEntity<String> uploadImg(HttpServletRequest request) {
        System.out.println("upload_tb_img");
        try{

            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

            System.out.println(multipartRequest.getMultiFileMap().toSingleValueMap().values().toArray().length);

            MultipartFile file = multipartRequest.getFile(multipartRequest.getFileNames().next());

            if(file.getSize()>1048576){
                return sendErr();
            }
            return sendMes(fileService.saveImg(file));
        }catch (Exception e){
            e.printStackTrace();
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
                e.printStackTrace();
                bytes = new byte[0];
            }
        }
        catch (Exception e){
            bytes = new byte[0];
            e.printStackTrace();
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
                e.printStackTrace();
                bytes = new byte[0];
            }
        } catch (Exception e) {
            bytes = new byte[0];
            e.printStackTrace();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
    }
}
