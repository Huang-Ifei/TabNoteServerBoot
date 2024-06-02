package com.tabnote.server.tabnoteserverboot.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@CrossOrigin
@Controller
public class ImageController {
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
