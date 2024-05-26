package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.PlanMapper;
import com.tabnote.server.tabnoteserverboot.models.Plan;
import com.tabnote.server.tabnoteserverboot.services.inteface.PlanServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanService implements PlanServiceInterface {

    AccountMapper accountMapper;
    PlanMapper planMapper;
    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
    @Autowired
    public void setPlanMapper(PlanMapper planMapper) {
        this.planMapper = planMapper;
    }

    @Override
    public JSONObject getPlans(String id, String token){
        JSONObject jsonObject = new JSONObject();
        jsonObject.putArray("plans");
        try{
            if (accountMapper.tokenCheckIn(token).equals(id)){
                List<Plan> maps = planMapper.getPlans(id);
                System.out.println(maps.size());
                for (Plan map : maps){
                    JSONObject note = new JSONObject();
                    note.put("plan_id", map.getPlan_id());
                    note.put("content", map.getContent());
                    note.put("link", map.getLink());
                    note.put("date", map.getDate());
                    System.out.println(note);
                    jsonObject.getJSONArray("plans").add(note);
                }
                jsonObject.put("response","success");
            }else {
                jsonObject.put("response","token_check_failed");
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonObject.put("response","failed");
        }
        return jsonObject;
    }

    @Override
    public JSONObject getHisPlans(String id, String token){
        JSONObject jsonObject = new JSONObject();
        jsonObject.putArray("his_notes");
        try{
            if (accountMapper.tokenCheckIn(token).equals(id)){
                List<Plan> maps = planMapper.getHisPlans(id);
                for (Plan map : maps){
                    JSONObject note = new JSONObject();
                    note.put("plan_id", map.getPlan_id());
                    note.put("content", map.getContent());
                    note.put("link", map.getLink());
                    note.put("date", map.getDate());
                    System.out.println(note);
                    jsonObject.getJSONArray("notes").add(note);
                }
                jsonObject.put("response","success");
            }else {
                jsonObject.put("response","token_check_failed");
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonObject.put("response","failed");
        }
        return jsonObject;
    }

    @Override
    public JSONObject addPlan(String plan_id, String id, String token, String content, String link, String date){
        JSONObject jsonObject = new JSONObject();
        try {
            if (accountMapper.tokenCheckIn(token).equals(id)){
                planMapper.addPlan(plan_id, id, content, link, date);
                jsonObject.put("response","success");
            }else {
                jsonObject.put("response","token_check_failed");
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonObject.put("response","failed");
        }
        return jsonObject;
    }

    @Override
    public JSONObject addPlanFromWeb(String id, String token, String content, String link, String date){
        JSONObject jsonObject = new JSONObject();
        try {
            if (accountMapper.tokenCheckIn(token).equals(id)){
                String plan_id = content.hashCode()+""+System.currentTimeMillis();
                planMapper.addPlan(plan_id, id, content, link, date);
                jsonObject.put("response","success");
            }else {
                jsonObject.put("response","token_check_failed");
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonObject.put("response","failed");
        }
        return jsonObject;
    }

    @Override
    public JSONObject resetPlan(String plan_id, String token, String id, String content, String link, String date){
        JSONObject jsonObject = new JSONObject();
        try {
            if (accountMapper.tokenCheckIn(token).equals(id)){
                System.out.println(plan_id);
                planMapper.resetPlan(plan_id,content,link,date);
                jsonObject.put("response","success");
            }else {
                jsonObject.put("response","token_check_failed");
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonObject.put("response","failed");
        }
        return jsonObject;
    }

    @Override
    public JSONObject deletePlan(String plan_id, String token, String id){
        JSONObject jsonObject = new JSONObject();
        try {
            if (accountMapper.tokenCheckIn(token).equals(id)){
                planMapper.deletePlan(plan_id);
                jsonObject.put("response","success");
            }else {
                jsonObject.put("response","token_check_failed");
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonObject.put("response","failed");
        }
        return jsonObject;
    }

    @Override
    public JSONObject finishPlan(String plan_id, String token, String id, String his_plan_id, String content, String link, String date){
        JSONObject jsonObject = new JSONObject();
        try {
            if (accountMapper.tokenCheckIn(token).equals(id)){
                try{
                    planMapper.deletePlan(plan_id);
                }catch (Exception e){
                    e.printStackTrace();
                }
                planMapper.addHisPlan(his_plan_id, id, content, link, date);
                jsonObject.put("response","success");
            }else {
                jsonObject.put("response","token_check_failed");
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonObject.put("response","failed");
        }
        return jsonObject;
    }

    @Override
    public JSONObject finishPlanFromWeb(String plan_id, String token, String id, String content, String link, String date){
        JSONObject jsonObject = new JSONObject();
        try {
            if (accountMapper.tokenCheckIn(token).equals(id)){
                try{
                    planMapper.deletePlan(plan_id);
                }catch (Exception e){
                    e.printStackTrace();
                }

                String his_plan_id = content.hashCode()+""+System.currentTimeMillis();
                planMapper.addHisPlan(his_plan_id, id, content, link, date);
                jsonObject.put("response",his_plan_id);
            }else {
                jsonObject.put("response","token_check_failed");
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonObject.put("response","failed");
        }
        return jsonObject;
    }
}
