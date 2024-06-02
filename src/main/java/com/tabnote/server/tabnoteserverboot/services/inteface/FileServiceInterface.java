package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;

public interface FileServiceInterface {

    int insertFileWithOutIdCheck(String base64FileString);

    int insertImgWithOutIdCheck(String base64FileString);
}
