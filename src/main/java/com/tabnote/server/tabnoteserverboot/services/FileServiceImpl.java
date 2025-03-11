package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.services.inteface.FileServiceInterface;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class FileServiceImpl implements FileServiceInterface {

    @Override
    public JSONObject saveImg(MultipartFile file) {
        JSONObject res = new JSONObject();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //取SHA-256加密的前11位为名字
            byte[] hash = digest.digest(file.getBytes());
            String name = Base64.getEncoder().encodeToString(hash).substring(0, 21);
            name = name.replace('/', '-');
            name = name.replace('+', '-');
            name = name.replace('?', '-');
            name = name.replace('=', '-');
            name = name.replace('&', '-');
            System.out.println(name + "::" + Base64.getEncoder().encodeToString(hash));
            //压缩为JPG
            BufferedImage image = ImageIO.read(file.getInputStream());
            BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            newImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
            File localFile = new File("tabNoteImgs/" + name + ".jpg");
            if (!localFile.exists()) {
                System.out.println("tabNoteImgs/" + name + ".jpg");
                FileOutputStream fos = new FileOutputStream(localFile);
                ImageIO.write(newImage, "jpg", fos);
                fos.close();
            }
            res.put("errno", 0);
            JSONObject data = new JSONObject();
            data.put("url", "/api/tabNoteImg?name=" + name);
            res.put("data", data);
        } catch (Exception e) {
            res.put("errno", 1);
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

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64FileString);
        } catch (Exception e) {
            e.printStackTrace();
            return "failed";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //取SHA-256加密的前21位为名字
            byte[] hash = digest.digest(bytes);
            name = Base64.getEncoder().encodeToString(hash).substring(0, 21);
            name = name.replace('/', '-');
            name = name.replace('+', '-');
            name = name.replace('?', '-');
            name = name.replace('=', '-');
            name = name.replace('&', '-');
            System.out.println(name + "::" + Base64.getEncoder().encodeToString(hash));
        } catch (Exception e) {
            e.printStackTrace();
            return "failed";
        }

        File file_lq = new File("tabNoteImgs/" + name + "_LQ.jpg");
        if (!file_lq.exists()) {
            try {
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));

                // 获取图像的原始宽度和高度
                int originalWidth = image.getWidth();
                int originalHeight = image.getHeight();

                // 计算调整后的宽度和高度，以保持纵横比不变，同时确保它们不超过 1000
                float aspectRatio = (float)originalWidth / originalHeight;
                int targetWidth = originalWidth;
                int targetHeight = originalHeight;

                if (originalWidth > 1000 || originalHeight > 1000) {
                    if (aspectRatio >= 1) { // 宽高比大于或等于1的情况
                        targetWidth = 1000;
                        targetHeight = Math.round(1000 / aspectRatio);
                    } else {
                        targetHeight = 1000;
                        targetWidth = Math.round(1000 * aspectRatio);
                    }
                }

                // 调整图像大小
                Image scaledImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
                BufferedImage bufferedScaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);

                // 将缩放后的图像绘制到 BufferedImage 上
                Graphics2D g2d = bufferedScaledImage.createGraphics();
                g2d.drawImage(scaledImage, 0, 0, null);
                g2d.dispose();

                ImageOutputStream ios = ImageIO.createImageOutputStream(new FileOutputStream(file_lq, false));

                ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                writer.setOutput(ios);

                ImageWriteParam param = writer.getDefaultWriteParam();
                if (param.canWriteCompressed()) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(0.8f);
                }
                writer.write(null, new IIOImage(bufferedScaledImage, null, null), param);
                ios.close();
                writer.dispose();

            } catch (Exception e) {
                e.printStackTrace();
                return "failed";
            }
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
                return "failed";
            }
        }
    }

}
