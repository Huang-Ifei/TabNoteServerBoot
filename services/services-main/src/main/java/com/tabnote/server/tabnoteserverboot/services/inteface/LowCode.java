package com.tabnote.server.tabnoteserverboot.services.inteface;


import com.alibaba.fastjson2.JSONObject;
import org.springframework.core.io.Resource;

public interface LowCode {
    JSONObject insertHuffmanLCID(String usr_id,String token, String language, String environment, String save);

    Resource getFile(String lc_id);
}
