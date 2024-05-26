package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.define.MesType;
import com.tabnote.server.tabnoteserverboot.services.AccountService;
import com.tabnote.server.tabnoteserverboot.services.inteface.AccountServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
public class AccountController {
    AccountServiceInterface accountService;
    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/account_id_check")
    public ResponseEntity<String> accountIdCheck(@RequestParam String id, HttpServletRequest request){
        System.out.println("accountIdCheck:" + request.getRemoteAddr());
        try{
            return sendMes(accountService.idCheck(id));
        }catch (Exception e){
            return sendErr();
        }
    }
    @PostMapping("/account_password_check")
    public ResponseEntity<String> accountPasswordCheck(@RequestBody String requestBody, HttpServletRequest request){
        System.out.println("accountPDCheck:" + request.getRemoteAddr());
        try{
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            return sendMes(accountService.passwordCheck(jsonObject.getString("password")));
        }catch (Exception e){
            return sendErr();
        }
    }
    @GetMapping("/account_name_check")
    public ResponseEntity<String> accountNameCheck(@RequestParam String name, HttpServletRequest request){
        System.out.println("accountNameCheck:" + request.getRemoteAddr());
        try{
            return sendMes(accountService.nameCheck(name));
        }catch (Exception e){
            return sendErr();
        }
    }

    @PostMapping("/account_img_set")
    public ResponseEntity<String> accountImgSet(@RequestBody String requestBody, HttpServletRequest request){
        System.out.println("accountImgSet:" + request.getRemoteAddr());
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
            System.out.println("MesType.signUp:" + request.getRemoteAddr());
            return this.sendMes(accountService.signUp(jsonObject.getString("id"),jsonObject.getString("password"),jsonObject.getString("name"),request.getRemoteAddr()));
        } else if (mesType == MesType.logIn) {
            System.out.println("MesType.logIn:" + request.getRemoteAddr());
            return this.sendMes(accountService.login(jsonObject.getString("id"), jsonObject.getString("password"),request.getRemoteAddr()+request.getRemotePort()));
        } else if (mesType == MesType.cancelLogIn) {
            System.out.println("MesType.cancelLogIn" + request.getRemoteAddr());
            return this.sendMes(accountService.deleteToken(jsonObject));
        } else if (mesType == MesType.resetName) {
            System.out.println("MesType.resetName" + request.getRemoteAddr());
            return this.sendMes(accountService.resetName(jsonObject));
        } else if (mesType == MesType.resetID) {
            System.out.println("MesType.resetID" + request.getRemoteAddr());
            return this.sendMes(accountService.resetID(jsonObject));
        } else if (mesType == MesType.resetPassword) {
            System.out.println("MesType.resetPassword" + request.getRemoteAddr());
            return this.sendMes(accountService.resetPassword(jsonObject));
        } else if (mesType == MesType.getTokensById) {
            System.out.println("MesType.getTokensById" + request.getRemoteAddr());
            return this.sendMes(accountService.getTokensById(jsonObject.getString("id"),jsonObject.getString("token")));
        } else {
            System.out.println("MesType.err" + request.getRemoteAddr());
            return sendErr();
        }
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
