package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;

public interface PlanServiceInterface {
    JSONObject getPlans(String id, String token);

    JSONObject getHisPlans(String id, String token);

    JSONObject addPlan(String plan_id, String id, String token, String content, String link, String date);

    JSONObject addPlanFromWeb(String id, String token, String content, String link, String date);

    JSONObject resetPlan(String plan_id, String token, String id, String content, String link, String date);

    JSONObject deletePlan(String plan_id, String token, String id);

    JSONObject finishPlan(String plan_id, String token, String id, String his_plan_id, String content, String link, String date);

    JSONObject finishPlanFromWeb(String plan_id, String token, String id, String content, String link, String date);
}
