package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;

public interface FileServiceInterface {
    boolean insertFileWithOutIdCheck(String name,String base64FileString) ;
}
