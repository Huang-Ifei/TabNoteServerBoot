package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.services.inteface.FileServiceInterface;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class FileService implements FileServiceInterface {

    @Override
    public JSONObject saveImg(MultipartFile file) {
        JSONObject res = new JSONObject();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //取SHA-256加密的前11位为名字
            byte[] hash = digest.digest(file.getBytes());
            String name = Base64.getEncoder().encodeToString(hash).substring(0,21);
            name = name.replace('/','-');
            name = name.replace('+','-');
            name = name.replace('?','-');
            name = name.replace('=','-');
            name = name.replace('&','-');
            System.out.println(name+"::"+Base64.getEncoder().encodeToString(hash));
            //压缩为JPG
            BufferedImage image = ImageIO.read(file.getInputStream());
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            newImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
            File localFile = new File("tabNoteImgs/" + name + ".jpg");
            if (!localFile.exists()){
                System.out.println("tabNoteImgs/" + name + ".jpg");
                FileOutputStream fos = new FileOutputStream(localFile);
                ImageIO.write(newImage, "jpg", fos);
                fos.close();
            }
            res.put("errno",0);
            JSONObject data = new JSONObject();
            data.put("url","http://101.42.31.139:7845/tabNoteImg?name="+name);
            res.put("data",data);
        } catch (Exception e) {
            res.put("errno",1);
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public int insertFileWithOutIdCheck(String base64FileString) {
        int name = base64FileString.hashCode();
        if (base64FileString.startsWith("data:application/x-zip-compressed;base64,")) {
            base64FileString = base64FileString.substring("data:application/x-zip-compressed;base64,".length());
        }
        byte[] bytes = Base64.getDecoder().decode(base64FileString);
        File file = new File("tabNoteFiles/" + name + ".zip");
        if (file.exists()) {
            return name;
        } else {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, false));
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
    public String insertImgWithOutIdCheck(String base64FileString) {
        String name = String.valueOf(base64FileString.hashCode());

        if (base64FileString.startsWith("data:image/jpeg;base64,")) {
            base64FileString = base64FileString.substring("data:image/jpeg;base64,".length());
        }
        byte[] bytes = Base64.getDecoder().decode(base64FileString);

        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //取SHA-256加密的前11位为名字
            byte[] hash = digest.digest(bytes);
            name = Base64.getEncoder().encodeToString(hash).substring(0,21);
            name = name.replace('/','-');
            name = name.replace('+','-');
            name = name.replace('?','-');
            name = name.replace('=','-');
            name = name.replace('&','-');
            System.out.println(name+"::"+Base64.getEncoder().encodeToString(hash));
        }catch (Exception e){
            e.printStackTrace();
        }

        File file = new File("tabNoteImgs/" + name + ".jpg");
        if (file.exists()) {
            return name;
        } else {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, false));
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
