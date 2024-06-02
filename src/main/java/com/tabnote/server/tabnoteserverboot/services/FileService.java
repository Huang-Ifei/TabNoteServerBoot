package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.services.inteface.FileServiceInterface;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

@Service
public class FileService implements FileServiceInterface {

    @Override
    public boolean insertFileWithOutIdCheck(String name, String base64FileString) {
        File file = new File("tabNoteFiles/" + name);
        if (file.exists()) {
            return false;
        } else {
            try {
                byte[] bytes = base64FileString.getBytes(StandardCharsets.UTF_8);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(bytes);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
