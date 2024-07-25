package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.multipart.MultipartFile;

public interface FileServiceInterface {

    JSONObject saveImg(MultipartFile file);

    int insertFileWithOutIdCheck(String base64FileString);

    int insertImgWithOutIdCheck(String base64FileString);
}
