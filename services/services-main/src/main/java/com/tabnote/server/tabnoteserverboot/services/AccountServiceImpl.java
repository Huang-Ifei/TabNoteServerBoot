package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.Cryptic;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.ResetIdMapper;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.services.inteface.AccountServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountServiceInterface {

    AccountMapper mapper;
    @Autowired
    public void setMapper(AccountMapper mapper) {
        this.mapper = mapper;
    }
    ResetIdMapper resetIdMapper;
    @Autowired
    public void setResetIdMapper(ResetIdMapper resetIdMapper) {
        this.resetIdMapper = resetIdMapper;
    }
    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tie) {
        this.tabNoteInfiniteEncryption = tie;
    }
    VipMapper vipMapper;
    @Autowired
    public void setVipMapper(VipMapper vipMapper) {
        this.vipMapper = vipMapper;
    }

    //账号查重
    @Override
    public JSONObject idCheck(String id) {
        JSONObject json = new JSONObject();
        try {
            if (id!=null && id.length()<=16 && id.length()>=5) {
                HashMap<String, String> idCheck = mapper.searchById(id);
                if (idCheck != null) {
                    json.put("response", "have_this_account");
                    return json;
                }else {
                    json.put("response", "no_this_account");
                    return json;
                }
            }else{
                json.put("response", "id_is_bad");
            }
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            json.put("response", "server_error");
            return json;
        }
    }
    @Override
    public JSONObject passwordCheck(String password) {
        JSONObject json = new JSONObject();
        try {
            String pwd = tabNoteInfiniteEncryption.decrypt(password);
            if ( pwd !=null  &&  pwd.length()<=16 &&  pwd.length()>=5) {
                json.put("response", "ok");
            }else{
                json.put("response", "password_is_bad");
            }
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            json.put("response", "server_error");
            return json;
        }
    }

    @Override
    public JSONObject nameCheck(String name) {
        JSONObject json = new JSONObject();
        try {
            if (name!=null  && name.length()<=16 && name.length()>=2) {
                HashMap<String, String> nameCheck = mapper.searchByName(name);
                if (nameCheck != null) {
                    json.put("response", "have_this_name");
                }else {
                    json.put("response", "no_this_name");
                }
            }else{
                json.put("response", "name_is_bad");
            }
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            json.put("response", "server_error");
            return json;
        }
    }
    @Override
    public JSONObject login(String id, String password,String address) {
        JSONObject jsonObject = new JSONObject();
        try {

            if (id.isEmpty() || password.isEmpty()) {
                jsonObject.put("response", "请正确输入");
                return jsonObject;
            }
            HashMap<String, String> hashMap = mapper.searchById(id);
            String token = hashMap.hashCode()  + tabNoteInfiniteEncryption.getTokenInput();

            if (tabNoteInfiniteEncryption.encryptionPasswordCheckIn(hashMap.get("password"),password)) {
                LocalDate localDate = LocalDate.now();
                String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate);

                try {
                    mapper.addToken(token, id, date);
                } catch (Exception e) {
                    jsonObject.put("response", "登录间隔太短");
                    return jsonObject;
                }

                jsonObject.put("response", "success");
                jsonObject.put("name", hashMap.get("name"));
                Cryptic cryptic = new Cryptic();
                jsonObject.put("token", cryptic.encrypt(token));
                return jsonObject;
            } else {
                jsonObject.put("response", "账号或密码错误");
                return jsonObject;
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("response", "账号不存在");
            return jsonObject;
        }
    }
    @Override
    public JSONObject setAccountImg(String id, String token,String base64Img){
        JSONObject returnJson = new JSONObject();
        try{
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)) {
                //删除前缀
                if (base64Img.startsWith("data:image/jpeg;base64,")) {
                    base64Img = base64Img.substring("data:image/jpeg;base64,".length());
                }
                byte [] bytes = Base64.getDecoder().decode(base64Img);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File("accountImg/"+id+".jpg")));
                bos.write(bytes);
                bos.flush();
                bos.close();
                returnJson.put("response", "success");
                return returnJson;
            }else {
                returnJson.put("response", "id_token_check_in_failed");
                return returnJson;
            }
        }catch (Exception e){
            e.printStackTrace();
            returnJson.put("response", "failed");
            return returnJson;
        }
    }
    @Override
    public JSONObject signUp(String id, String password, String name, String address) {
        JSONObject jsonObject = new JSONObject();
        try {

            String pwd = tabNoteInfiniteEncryption.decrypt(password);

            if (id.equals("") || pwd.equals("") || name.equals("")) {
                jsonObject.put("response", "请正确输入");
                return jsonObject;
            }
            String prohibitedCharsRegex = "[/?#&=;%+<> ]";

            // 使用 String 的 matches 方法来检查是否含有任何违规字符
            if (id.matches(".*" + prohibitedCharsRegex + ".*")||pwd.matches(".*" + prohibitedCharsRegex + ".*")||name.matches(".*" + prohibitedCharsRegex + ".*")) {
                jsonObject.put("response", "存在违规的字符:/?#&=;%+<> ");
                return jsonObject;
            }
            HashMap<String, String> idCheck = mapper.searchById(id);
            HashMap<String, String> nameCheck = mapper.searchByName(name);
            if (idCheck != null) {
                jsonObject.put("response", "账号已存在");
                return jsonObject;
            } else if (nameCheck != null) {
                jsonObject.put("response", "名字已存在");
                return jsonObject;
            }
            LocalDate localDate = LocalDate.now();
            String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate);
            mapper.signUp(id, pwd, name, date);

            HashMap<String, String> hashMap = mapper.searchById(id);
            String token = hashMap.hashCode() + "" + address.hashCode();
            mapper.addToken(token, id, date);
            jsonObject.put("response", "success");
            Cryptic cryptic = new Cryptic();
            jsonObject.put("token",cryptic.encrypt(token));
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            jsonObject.put("response", "可能存在非法字符");
            return jsonObject;
        }
    }

    @Override
    public JSONObject deleteToken(JSONObject jsonObject) {
        String token = jsonObject.getString("token");
        JSONObject json = new JSONObject();
        try {
            mapper.deleteToken(token);
            json.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            json.put("response", e.toString());
        }
        return json;
    }
    @Override
    public JSONObject resetName(JSONObject jsonObject) {
        String name = jsonObject.getString("name");
        JSONObject json = new JSONObject();
        try {
            if (name==null || name.length()>=16 || name.length()<=2) {
                json.put("response", "请正确输入");
                return json;
            }
            //token确认
            String id = tabNoteInfiniteEncryption.encryptionTokenGetId(jsonObject.getString("token"));
            if (id==null||id.isEmpty()) {
                json.put("response", "登录已失效，请重新登录");
                return json;
            }

            HashMap<String, String> nameCheck = mapper.searchByName(name);
            if (nameCheck == null) {
                mapper.resetName(id, name);
                json.put("response", "success");
                json.put("name",name);
            }else {
                json.put("response", "have_this_name");
            }
        } catch (Exception e) {
            json.put("response", "失败");
            e.printStackTrace();
        }
        return json;
    }

    @Transactional
    @Override
    public JSONObject resetID(JSONObject jsonObject) {

        JSONObject json = new JSONObject();
        try {
            String new_id = jsonObject.getString("id");
            if (new_id==null || new_id.length()>=16 || new_id.length()<=2) {
                json.put("response", "请正确输入");
                return json;
            }
            //token确认
            String id = tabNoteInfiniteEncryption.encryptionTokenGetId(jsonObject.getString("token"));
            if (id==null||id.isEmpty()) {
                json.put("response", "登录已失效，请重新登录");
                return json;
            }
            //重新设置id并更改token对应的id
            HashMap<String, String> idCheck = mapper.searchById(jsonObject.getString("id"));
            if (idCheck == null) {
                resetIdMapper.resetID(id,new_id);
                resetIdMapper.updateToken(new_id,jsonObject.getString("token"));
                resetIdMapper.updateAiMId(id,new_id);
                resetIdMapper.updateHisNoteId(id,new_id);
                resetIdMapper.updateMessagesId(id,new_id);
                resetIdMapper.updateNoteId(id,new_id);
                resetIdMapper.updateMessMessId(id,new_id);
                resetIdMapper.updateTabNoteId(id,new_id);
                resetIdMapper.updatePlan(id,new_id);
                resetIdMapper.updateClickNoteId(id,new_id);
                resetIdMapper.updateLikeNote(id,new_id);
                resetIdMapper.updateTabMess(id,new_id);
                resetIdMapper.updateMessMess(id,new_id);
                resetIdMapper.updateNoteAi(id,new_id);
                resetIdMapper.updateBQId(id,new_id);
                resetIdMapper.updateVIPId(id,new_id);
                resetIdMapper.updateLCId(id,new_id);
                resetIdMapper.updateCHId(id,new_id);

                File accountImg = new File("accountImg/"+id+".jpg");
                if (accountImg.exists()){
                    accountImg.renameTo(new File("accountImg/"+new_id+".jpg"));
                }
                json.put("response", "success");
                json.put("id",new_id);
            }else {
                json.put("response", "have_this_id");
            }

        } catch (Exception e) {
            json.put("response", "失败");
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public JSONObject resetPassword(JSONObject jsonObject) {

        JSONObject json = new JSONObject();
        try {
            String id = jsonObject.getString("id");
            String old_password = jsonObject.getString("old_password");
            String password = jsonObject.getString("password");

            if (password==null || password.length()>=16 || password.length()<=2 || id.isEmpty()) {
                json.put("response", "请正确输入");
                return json;
            }
            HashMap<String, String> hashMap = mapper.searchById(id);

            if (hashMap.get("password").equals(old_password)){
                mapper.resetPassword(id,password);
                json.put("response","success");
                json.put("password",password);
            }else {
                json.put("response","密码错误");
            }

        } catch (Exception e) {
            json.put("response", "失败");
            e.printStackTrace();
        }
        return json;
    }
    @Override
    public JSONObject getTokensById(String id,String token){
        JSONObject json = new JSONObject();
        try{
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
                json.put("response","登录状态确认失败");
                return json;
            }
            List<HashMap<String,String>> tokens = mapper.getTokensById(id);
            json.putArray("tokens");
            for (HashMap<String,String> map :tokens){
                JSONObject jsonObject = new JSONObject(map);
                json.getJSONArray("tokens").add(jsonObject);
            }
            json.put("response","success");
        }catch (Exception e){
            json.put("response","数据错误");
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public JSONObject getCHList(String id, String token){
        JSONObject json = new JSONObject();
        JSONArray arrayList = json.putArray("list");
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
                json.put("response","登录状态确认失败");
                return json;
            }
            List<HashMap<String, Object>> hashMaps = vipMapper.selectConsumptionHistory(id);
            hashMaps.forEach(hashMap ->
                arrayList.add(JSONObject.from(hashMap))
            );
            json.put("response","success");
        }catch (Exception e){
            json.put("response","error");
            e.printStackTrace();
        }
        return json;
    }
}
