package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson.JSONObject;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import com.tabnote.server.tabnoteserverboot.services.inteface.HomeworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomeworkServiceImpl  implements HomeworkService {
    @Autowired
    AiServiceInterface aiService;

    public JSONObject createHomework(JSONObject userContent){
        JSONObject response = new JSONObject();
//        userContent.getString("")


        response.put("response","success");
        return response;
    }
}
