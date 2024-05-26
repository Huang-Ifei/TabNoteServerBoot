package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;

public interface AccountServiceInterface {
    //查重
    JSONObject idCheck(String id);
    //查重
    JSONObject passwordCheck(String password);
    //查重
    JSONObject nameCheck(String name);

    JSONObject login(String id, String password, String address);

    JSONObject setAccountImg(String id, String token, String base64Img);

    JSONObject signUp(String id, String password, String name, String address);

    JSONObject deleteToken(JSONObject jsonObject);

    JSONObject resetName(JSONObject jsonObject);

    JSONObject resetID(JSONObject jsonObject);

    JSONObject resetPassword(JSONObject jsonObject);

    JSONObject getTokensById(String id,String token);
}
