package com.tabnote.server.tabnoteserverboot.services;

import com.tabnote.server.tabnoteserverboot.services.inteface.FileServiceInterface;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class FileService implements FileServiceInterface {

    @Override
    public int insertFileWithOutIdCheck(String base64FileString) {
        int name = base64FileString.hashCode();
        if (base64FileString.startsWith("data:application/x-zip-compressed;base64,")) {
            base64FileString = base64FileString.substring("data:application/x-zip-compressed;base64,".length());
        }
        byte [] bytes = Base64.getDecoder().decode(base64FileString);
        File file = new File("tabNoteFiles/" + name +".zip");
        if (file.exists()) {
            return name;
        } else {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file,false));
                bos.write(bytes);
                bos.flush();
                bos.close();
                return name;
            } catch (Exception e) {
                e.printStackTrace();
                return name;
            }
        }
    }

    @Override
    public int insertImgWithOutIdCheck(String base64FileString) {
        int name = base64FileString.hashCode();
        if (base64FileString.startsWith("data:image/jpeg;base64,")) {
            base64FileString = base64FileString.substring("data:image/jpeg;base64,".length());
        }
        byte [] bytes = Base64.getDecoder().decode(base64FileString);
        File file = new File("tabNoteImgs/" + name +".jpg");
        if (file.exists()) {
            return name;
        } else {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file,false));
                bos.write(bytes);
                bos.flush();
                bos.close();
                return name;
            } catch (Exception e) {
                e.printStackTrace();
                return name;
            }
        }
    }

}
