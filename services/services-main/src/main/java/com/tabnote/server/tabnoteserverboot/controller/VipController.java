package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.models.Vip;
import com.tabnote.server.tabnoteserverboot.redis.VipAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@CrossOrigin
@Controller
@RequestMapping("vip")
public class VipController {

    VipMapper vipMapper;

    @Autowired
    public void setVipMapper(VipMapper vipMapper) {
        this.vipMapper = vipMapper;
    }

    VipAuth vipAuth;

    @Autowired
    public void setVipAuth(VipAuth vipAuth) {
        this.vipAuth = vipAuth;
    }

    @GetMapping("rank")
    public ResponseEntity<String> publicKey(@RequestParam String id) {
        System.out.println("getVIP");
        JSONObject json = new JSONObject();
        Integer getRank = vipMapper.selectRankByUserId(id).getRank();
        if (getRank == null) {
            json.put("rank", 0);
        } else {
            json.put("rank", getRank);
        }
        return sendMes(json);
    }

    @GetMapping("check")
    public ResponseEntity<String> checkVip(@RequestParam String id, @RequestParam String auth) throws Exception {
        JSONObject json = new JSONObject();
        int rank = vipAuth.checkVipAuth(auth);
        if (rank == 0) {
            json.put("rank", 0);
            json.put("response", "error");
        } else {
            String s = vipMapper.selectEndTimeById(id);
            if (s == null) {
                vipMapper.addNewVip(id, vipAuth.rankToQuota(rank), rank);
            } else {
                vipMapper.addVip(id, s, vipAuth.rankToQuota(rank), rank);
            }
            vipAuth.deleteVipAuth(auth);
            json.put("rank", rank);
            json.put("id",id);
            json.put("response", "success");
        }
        return sendMes(json);
    }

    @GetMapping("new_auth")
    public ResponseEntity<String> getAVipAuth(@RequestParam String key, @RequestParam int rank) throws Exception {
        JSONObject json = new JSONObject();
        if (key.equals("hyfloveysj")) {
            String auth = "err";
            if (rank == 2) {
                auth = vipAuth.setVipAuth(2);
                json.put("rank", 2);
            } else if (rank == 4) {
                auth = vipAuth.setVipAuth(4);
                json.put("rank", 4);
            } else if (rank == 6) {
                auth = vipAuth.setVipAuth(6);
                json.put("rank", 6);
            }
            json.put("auth", auth);

            return sendMes(json);
        } else {
            json.put("rank", 0);
            return sendMes(json);
        }
    }

    @GetMapping("info")
    public ResponseEntity<String> info(@RequestParam String id) throws Exception {
        JSONObject json = new JSONObject();
        try{
            List<Vip> vipList = vipMapper.selectVipListById(id);
            JSONArray list = json.putArray("list");
            for (Vip vip : vipList) {
                list.add(JSONObject.from(vip));
            }
            json.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            json.put("response","failed");
        }
        return sendMes(json);
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
