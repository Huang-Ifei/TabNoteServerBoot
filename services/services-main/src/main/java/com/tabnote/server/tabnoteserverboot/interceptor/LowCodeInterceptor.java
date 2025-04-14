package com.tabnote.server.tabnoteserverboot.interceptor;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.SecurityComponent;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.models.RankAndQuota;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class LowCodeInterceptor implements HandlerInterceptor {
    private final TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    private final VipMapper vipMapper;
    private final AiServiceInterface aiService;

    @Autowired
    public LowCodeInterceptor(TabNoteInfiniteEncryption tabNoteInfiniteEncryption, VipMapper vipMapper, AiServiceInterface aiService) {
        this.tabNoteInfiniteEncryption = tabNoteInfiniteEncryption;
        this.vipMapper = vipMapper;
        this.aiService = aiService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {


        response.addHeader("content-type", "application/json;charset=utf-8");
        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            return false;
        }else {
            request.setAttribute("body", body);
        }
        //变成JSON对象
        JSONObject bodyJson = JSONObject.parseObject(body);
        //check是否有此账户
        if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(bodyJson.getString("id"), bodyJson.getString("token"))) {
            //没有授权的拒绝执行
            RankAndQuota raq = vipMapper.selectRankByUserId(bodyJson.getString("id"));
            if (raq == null || !raq.passAFABasic()) {
                aiService.returnAdminMess(response, "你的账户没有高级功能授权或者额度已经用完，请访问：https://tabnote.cn/afa 获取授权，You need get AFA to request, please open: https://tabnote.cn/afa to get it.");
                return false;
            }
            return true;
        }else {
            aiService.returnAdminMess(response, "您的账户验证出现错误，请重新尝试或者重新登录,account check failed please request again or try login again.");
            return false;
        }
    }
}
