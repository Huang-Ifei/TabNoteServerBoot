package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.services.PlanService;
import com.tabnote.server.tabnoteserverboot.services.inteface.PlanServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin
@Controller
public class PlanController {
    PlanServiceInterface planService;

    @Autowired
    public void setPlanService(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping("/get_plans")
    public ResponseEntity<String> getPlans(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        System.out.println("MesType.get_plans:" + request.getRemoteAddr());
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.getPlans(jsonObject.getString("id"), jsonObject.getString("token")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("/get_his_plans")
    public ResponseEntity<String> getHisPlans(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        System.out.println("MesType.get_his_plans:" + request.getRemoteAddr());
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.getHisPlans(jsonObject.getString("id"), jsonObject.getString("token")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("/add_plan")
    public ResponseEntity<String> addPlan(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        System.out.println("MesType.add_plan:" + request.getRemoteAddr());
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.addPlan(jsonObject.getString("plan_id"), jsonObject.getString("id"), jsonObject.getString("token"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("/add_plan_web")
    public ResponseEntity<String> addPlanFromWeb(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        System.out.println("MesType.add_plan_web:" + request.getRemoteAddr());
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.addPlanFromWeb( jsonObject.getString("id"), jsonObject.getString("token"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("/change_plan")
    public ResponseEntity<String> changePlan(@RequestBody String requestBody,HttpServletRequest request) throws Exception {
        System.out.println("MesType.change_plan:" + request.getRemoteAddr());
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.resetPlan(jsonObject.getString("plan_id"), jsonObject.getString("token"), jsonObject.getString("id"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("/delete_plan")
    public ResponseEntity<String> deletePlan(@RequestBody String requestBody,HttpServletRequest request) throws Exception {
        System.out.println("MesType.delete_plan:" + request.getRemoteAddr());
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.deletePlan(jsonObject.getString("plan_id"), jsonObject.getString("token"), jsonObject.getString("id")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("/finish_plan")
    public ResponseEntity<String> finishPlan(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        System.out.println("MesType.finish_plan:" + request.getRemoteAddr());
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.finishPlanFromWeb(jsonObject.getString("plan_id"), jsonObject.getString("token"), jsonObject.getString("id"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("/finish_plan_web")
    public ResponseEntity<String> finishPlanFromWeb(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        System.out.println("MesType.finish_plan_web:" + request.getRemoteAddr());
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.finishPlanFromWeb(jsonObject.getString("plan_id"), jsonObject.getString("token"), jsonObject.getString("id"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date")));
        } catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }

    @PostMapping("/synchronous_plans")
    public ResponseEntity<String> synchronousPlans(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        System.out.println("MesType.synchronous_plans:" + request.getRemoteAddr());
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.synchronousPlans(jsonObject.getJSONArray("plans"), jsonObject.getString("id"), jsonObject.getString("token")));
        }catch (Exception e) {
            e.printStackTrace();
            return sendErr();
        }
    }


    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
