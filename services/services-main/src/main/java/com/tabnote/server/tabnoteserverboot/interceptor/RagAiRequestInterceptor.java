package com.tabnote.server.tabnoteserverboot.interceptor;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.SecurityComponent;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.models.RankAndQuota;
import com.tabnote.server.tabnoteserverboot.mq.publisher.QuotaDeductionPublisher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RagAiRequestInterceptor implements HandlerInterceptor {

    private final TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    private final SecurityComponent securityComponent;

    @Autowired
    public RagAiRequestInterceptor(TabNoteInfiniteEncryption tabNoteInfiniteEncryption, SecurityComponent securityComponent) {
        this.tabNoteInfiniteEncryption = tabNoteInfiniteEncryption;
        this.securityComponent = securityComponent;
    }

    private QuotaDeductionPublisher quotaDeductionPublisher;
    @Autowired
    public void setQuotaDeductionPublisher(QuotaDeductionPublisher quotaDeductionPublisher) {
        this.quotaDeductionPublisher = quotaDeductionPublisher;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        response.addHeader("content-type", "application/json;charset=utf-8");
        String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isEmpty()) {
            return false;
        } else {
            request.setAttribute("body", body);
        }
        JSONObject bodyJson = JSONObject.parseObject(body);

        if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(bodyJson.getString("id"), bodyJson.getString("token"))) {
            RankAndQuota raq = quotaDeductionPublisher.getQuotaAndRank(bodyJson.getString("id"));
            if (raq == null || !raq.passAFABasic()) {
                returnRagAdminMess(response, "你的账户没有高级功能授权或者额度已经用完，请访问：https://tabnote.cn/afa 获取授权");
                return false;
            }

            if (securityComponent.haveProblemWord(body, request.getRemoteAddr(), bodyJson.getString("id"), "RAG_AI请求")) {
                returnRagAdminMess(response, "您的请求存在安全系统触发词，本次请求将会被记录，以下为AI生成内容，AI可能存在幻觉请仔细甄别：\n\n");
            }

            return true;
        } else {
            returnRagAdminMess(response, "您的账户验证出现错误，请重新尝试或者重新登录");
            return false;
        }
    }

    private void returnRagAdminMess(HttpServletResponse response, String s) throws IOException {
        JSONObject returnJSON = new JSONObject();
        JSONObject returnMessage = new JSONObject();
        returnMessage.put("content", s);
        returnJSON.put("ai_ms_id", null);
        returnJSON.put("cdn_ai_id", null);
        returnJSON.put("model", "server_security_admin");
        returnJSON.put("message", returnMessage);
        returnJSON.put("response", "admin_message");
        response.getWriter().write(returnJSON.toString());
        response.getWriter().write("\n");
        response.getWriter().flush();
    }
}