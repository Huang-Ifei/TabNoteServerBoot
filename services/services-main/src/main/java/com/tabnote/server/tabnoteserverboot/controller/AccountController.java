package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.define.MesType;
import com.tabnote.server.tabnoteserverboot.services.inteface.AccountServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    AccountServiceInterface accountService;
    @Autowired
    public void setAccountService(AccountServiceInterface accountService) {
        this.accountService = accountService;
    }
    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tabNoteInfiniteEncryption) {
        this.tabNoteInfiniteEncryption = tabNoteInfiniteEncryption;
    }

    @GetMapping("/account_id_check")
    public ResponseEntity<String> accountIdCheck(@RequestParam String id, HttpServletRequest request){
        log.info("accountIdCheck:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try{
            return sendMes(accountService.idCheck(id));
        }catch (Exception e){
            return sendErr();
        }
    }
    @PostMapping("/account_password_check")
    public ResponseEntity<String> accountPasswordCheck(@RequestBody String requestBody, HttpServletRequest request){
        log.info("accountPDCheck:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try{
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(accountService.passwordCheck(jsonObject.getString("password")));
        }catch (Exception e){
            return sendErr();
        }
    }
    @GetMapping("/account_name_check")
    public ResponseEntity<String> accountNameCheck(@RequestParam String name, HttpServletRequest request){
        log.info("accountNameCheck:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        try{
            return sendMes(accountService.nameCheck(name));
        }catch (Exception e){
            return sendErr();
        }
    }

    @PostMapping("/account_img_set")
    public ResponseEntity<String> accountImgSet(@RequestBody String requestBody, HttpServletRequest request){
        log.info("accountImgSet:" + tabNoteInfiniteEncryption.proxyGetIp(request));
        if (requestBody.length()>500*1024){
            return sendErr();
        }
        try{
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(accountService.setAccountImg(jsonObject.getString("id"),jsonObject.getString("token"),jsonObject.getString("base64Img")));
        }catch (Exception e){
            return sendErr();
        }
    }

    @PostMapping("/account")
    public ResponseEntity<String> accountController(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(requestBody);
        int mesType = jsonObject.getIntValue("mesType");
        if (mesType == MesType.signUp) {
            log.info("MesType.signUp:" + tabNoteInfiniteEncryption.proxyGetIp(request));
            return this.sendMes(accountService.signUp(jsonObject.getString("id"),jsonObject.getString("password"),jsonObject.getString("name"),tabNoteInfiniteEncryption.proxyGetIp(request)));
        } else if (mesType == MesType.logIn) {
            log.info("MesType.logIn:" + tabNoteInfiniteEncryption.proxyGetIp(request));
            return this.sendMes(accountService.login(jsonObject.getString("id"), jsonObject.getString("password"),tabNoteInfiniteEncryption.proxyGetIp(request)));
        } else if (mesType == MesType.cancelLogIn) {
            log.info("MesType.cancelLogIn" + tabNoteInfiniteEncryption.proxyGetIp(request));
            return this.sendMes(accountService.deleteToken(jsonObject));
        } else if (mesType == MesType.resetName) {
            log.info("MesType.resetName" + tabNoteInfiniteEncryption.proxyGetIp(request));
            return this.sendMes(accountService.resetName(jsonObject));
        } else if (mesType == MesType.resetID) {
            log.info("MesType.resetID" + tabNoteInfiniteEncryption.proxyGetIp(request));
            return this.sendMes(accountService.resetID(jsonObject));
        } else if (mesType == MesType.resetPassword) {
            log.info("MesType.resetPassword" + tabNoteInfiniteEncryption.proxyGetIp(request));
            return this.sendMes(accountService.resetPassword(jsonObject));
        } else if (mesType == MesType.getTokensById) {
            log.info("MesType.getTokensById" + tabNoteInfiniteEncryption.proxyGetIp(request));
            return this.sendMes(accountService.getTokensById(jsonObject.getString("id"),jsonObject.getString("token")));
        } else {
            log.info("MesType.err" + tabNoteInfiniteEncryption.proxyGetIp(request));
            return sendErr();
        }
    }
    @PostMapping("/ch")
    public ResponseEntity<String> chController(@RequestBody String requestBody, HttpServletRequest request) throws Exception {
        JSONObject jsonObject = JSONObject.parseObject(requestBody);
        String id = jsonObject.getString("id");
        String token = jsonObject.getString("token");
        return sendMes(accountService.getCHList(id,token));
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
