package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.services.inteface.PlanServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PlanController.class);

    PlanServiceInterface planService;

    @Autowired
    public void setPlanService(PlanServiceInterface planService) {
        this.planService = planService;
    }

    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tabNoteInfiniteEncryption) {
        this.tabNoteInfiniteEncryption = tabNoteInfiniteEncryption;
    }

    @PostMapping("/get_plans")
    public ResponseEntity<String> getPlans(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        log.info("MesType.get_plans:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.getPlans(jsonObject.getString("id"), jsonObject.getString("token")));
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("/get_his_plans")
    public ResponseEntity<String> getHisPlans(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        log.info("MesType.get_his_plans:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.getHisPlans(jsonObject.getString("id"), jsonObject.getString("token")));
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("/add_plan")
    public ResponseEntity<String> addPlan(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        log.info("MesType.add_plan:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.addPlan(jsonObject.getString("plan_id"), jsonObject.getString("id"), jsonObject.getString("token"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date")));
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("/add_plan_web")
    public ResponseEntity<String> addPlanFromWeb(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        log.info("MesType.add_plan_web:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.addPlanFromWeb( jsonObject.getString("id"), jsonObject.getString("token"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date")));
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("/change_plan")
    public ResponseEntity<String> changePlan(@RequestBody String requestBody,HttpServletRequest request) throws Exception {
        log.info("MesType.change_plan:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.resetPlan(jsonObject.getString("plan_id"), jsonObject.getString("token"), jsonObject.getString("id"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date")));
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("/delete_plan")
    public ResponseEntity<String> deletePlan(@RequestBody String requestBody,HttpServletRequest request) throws Exception {
        log.info("MesType.delete_plan:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.deletePlan(jsonObject.getString("plan_id"), jsonObject.getString("token"), jsonObject.getString("id")));
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("/finish_plan")
    public ResponseEntity<String> finishPlan(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        log.info("MesType.finish_plan:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.finishPlanFromWeb(jsonObject.getString("plan_id"), jsonObject.getString("token"), jsonObject.getString("id"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date")));
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("/finish_plan_web")
    public ResponseEntity<String> finishPlanFromWeb(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        log.info("MesType.finish_plan_web:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.finishPlanFromWeb(jsonObject.getString("plan_id"), jsonObject.getString("token"), jsonObject.getString("id"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date")));
        } catch (Exception e) {
            log.error(e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("/synchronous_plans")
    public ResponseEntity<String> synchronousPlans(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        log.info("MesType.synchronous_plans:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(planService.synchronousPlans(jsonObject.getJSONArray("plans"), jsonObject.getString("id"), jsonObject.getString("token")));
        }catch (Exception e) {
            log.error(e.getMessage());
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
