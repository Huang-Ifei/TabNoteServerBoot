package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.PlanMapper;
import com.tabnote.server.tabnoteserverboot.models.Plan;
import com.tabnote.server.tabnoteserverboot.services.inteface.PlanServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Service
public class PlanServiceImpl implements PlanServiceInterface {

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

    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tie) {
        this.tabNoteInfiniteEncryption = tie;
    }

    @Override
    public JSONObject getPlans(String id, String token){
        JSONObject jsonObject = new JSONObject();
        jsonObject.putArray("plans");
        try{
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
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
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
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
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
                try{
                    planMapper.addPlan(plan_id, id, content, link, date);
                }catch (DuplicateKeyException e){

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
    public JSONObject addPlanFromWeb(String id, String token, String content, String link, String date){
        JSONObject jsonObject = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
                String plan_id = id.hashCode()+""+content.hashCode()+link.hashCode()+date.hashCode();
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
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
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
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
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
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
                planMapper.addHisPlan(his_plan_id);
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
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
                planMapper.addHisPlan(plan_id);
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
    public JSONObject synchronousPlans(JSONArray plans, String id, String token){
        JSONObject jsonObject = new JSONObject();
        jsonObject.putArray("return_plan");
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
                List<Plan> cloudPlans = planMapper.getAllPlans(id);

                for(int i=0;i<plans.size();i++){
                    JSONObject plan = plans.getJSONObject(i);
                    String plan_id = plan.getString("plan_id");
                    String content = plan.getString("content");
                    String link = plan.getString("link");
                    String date = plan.getString("date");
                    boolean done = plan.getBoolean("done");

                    boolean find = false;

                    for(Plan p : cloudPlans){
                        if (p.getPlan_id().equals(plan_id)){
                            find = true;
                            if (p.getContent().equals(content)&&p.getDate().equals(date)&&p.getLink().equals(link)&&p.getDone()==done){
                                cloudPlans.remove(p);
                            }else{
                                //内容以客户端为准变更为已云端为准
//                                planMapper.resetPlan(plan_id,content,link,date);
//                                cloudPlans.get(count).setPlan_id(plan_id);
//                                cloudPlans.get(count).setContent(content);
//                                cloudPlans.get(count).setLink(link);
//                                cloudPlans.get(count).setDate(date);
                                //是否完成以两个任意一个完成了就完成了,因为返回的是云端数据，这里只要不删除云端数据，返回的就会按照云端的来,所以如果是云端没done那么就要done，如果是客户端没done自然会返回数据
                                if (!p.getDone()&&done){
                                    //云端未完成，客户端已完成
                                    planMapper.addHisPlan(plan_id);
                                    //其他信息不一致，返回云端信息，一致就删掉
                                    if(p.getContent().equals(content)&&p.getDate().equals(date)&&p.getLink().equals(link)){
                                        cloudPlans.remove(p);
                                    }else{
                                        p.setDone(true);
                                    }
                                }
                            }
                            break;
                        }
                    }

                    if (!find){
                        planMapper.addPlan(plan_id,id,content,link,date);
                    }
                }

                for (Plan map : cloudPlans){
                    JSONObject note = new JSONObject();
                    note.put("plan_id", map.getPlan_id());
                    note.put("content", map.getContent());
                    note.put("link", map.getLink());
                    note.put("date", map.getDate());
                    note.put("done",map.getDone());
                    jsonObject.getJSONArray("return_plan").add(note);
                }
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
