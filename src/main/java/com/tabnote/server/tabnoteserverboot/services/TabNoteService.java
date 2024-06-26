package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.ClassMapper;
import com.tabnote.server.tabnoteserverboot.mappers.TabNoteMapper;
import com.tabnote.server.tabnoteserverboot.models.TabNote;
import com.tabnote.server.tabnoteserverboot.models.TabNoteForList;
import com.tabnote.server.tabnoteserverboot.services.inteface.TabNoteServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Service
public class TabNoteService implements TabNoteServiceInterface {
    TabNoteMapper tabNoteMapper;
    ClassMapper classMapper;
    AccountMapper accountMapper;
    FileService fileService;

    @Autowired
    public void setTabNoteMapper(TabNoteMapper tabNoteMapper) {
        this.tabNoteMapper = tabNoteMapper;
    }

    @Autowired
    public void setClassMapper(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public JSONObject getClasses() {
        JSONObject json = new JSONObject();
        try {
            List<HashMap<String, String>> classes = classMapper.getClasses();
            json.putArray("classes");
            for (HashMap<String, String> map : classes) {
                json.getJSONArray("classes").add(map.get("class_name").toString());
            }
            json.put("response", "success");
        } catch (Exception e) {
            json.put("response", "failed");
            e.printStackTrace();
        }
        return json;
    }


    @Override
    public JSONObject getPageCount() {
        JSONObject returnJSON = new JSONObject();
        try {
            int count = tabNoteMapper.getTabNotePages();
            if (count % 20 == 0){
                count = count / 20;
            }else{
                count = count / 20 +1;
            }
            returnJSON.put("pagesCount", count);
            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
        return returnJSON;
    }

    @Override
    public JSONObject getPageTabNotes(int page) {
        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.getTabNote(start);
            returnJSON.putArray("list");
            for (TabNoteForList tabNoteForList : list) {
                JSONObject tabNoteJSON = new JSONObject();

                tabNoteJSON.put("tab_note_id", tabNoteForList.getTab_note_id());
                tabNoteJSON.put("usr_id", tabNoteForList.getUsr_id());
                tabNoteJSON.put("usr_name", accountMapper.getNameById(tabNoteForList.getUsr_id()));
                tabNoteJSON.put("class_name", tabNoteForList.getClass_name());
                tabNoteJSON.put("tab_note_name", tabNoteForList.getTab_note_name());
                tabNoteJSON.put("tags", tabNoteForList.getTags());
                tabNoteJSON.put("like_this", tabNoteMapper.getTabNoteLikeCount(tabNoteForList.getTab_note_id()));
                tabNoteJSON.put("click", tabNoteForList.getClick());
                tabNoteJSON.put("date_time", tabNoteForList.getDate_time());

                returnJSON.getJSONArray("list").add(tabNoteJSON);
            }
            int count = tabNoteMapper.getTabNotePages();
            if (count % 20 == 0){
                count = count / 20;
            }else{
                count = count / 20 +1;
            }
            returnJSON.put("pages", count);
            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
        return returnJSON;
    }

    @Override
    public JSONObject clickTabNote(String tabNoteId, String id, String token) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (accountMapper.tokenCheckIn(token).equals(id)) {
                tabNoteMapper.clickNote(tabNoteId, id);
                tabNoteMapper.clickThis(tabNoteId);
                returnJSON.put("response", "success");
            } else {
                returnJSON.put("response", "token_check_failed");
            }
        } catch (DuplicateKeyException e) {
            returnJSON.put("response", "click");
        } catch (Exception e) {
            returnJSON.put("response", "failed");
            e.printStackTrace();
        }
        return returnJSON;
    }

    @Override
    public JSONObject likeTabNote(String tabNoteId, String id, String token) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (accountMapper.tokenCheckIn(token).equals(id)) {
                tabNoteMapper.likeNote(tabNoteId, id);
            } else {
                returnJSON.put("response", "token_check_failed");
            }
        } catch (DuplicateKeyException e) {
            returnJSON.put("response", "click");
        } catch (Exception e) {
            returnJSON.put("response", "failed");
            e.printStackTrace();
        }
        return returnJSON;
    }

    @Override
    public JSONObject getTabNote(String tabNoteId) {
        JSONObject returnJSON = new JSONObject();

        try {
            TabNote tabNote = tabNoteMapper.getTabNoteById(tabNoteId);
            returnJSON.put("usr_id", tabNote.getUsr_id());
            returnJSON.put("usr_name", accountMapper.getNameById(tabNote.getUsr_id()));
            returnJSON.put("ip_address", tabNote.getIp_address());
            returnJSON.put("class_name", tabNote.getClass_name());
            returnJSON.put("tab_note_name", tabNote.getTab_note_name());
            returnJSON.put("tags", tabNote.getTags());
            returnJSON.put("like_this", tabNoteMapper.getTabNoteLikeCount(tabNoteId));
            returnJSON.put("click", tabNote.getClick());
            returnJSON.put("tab_note", tabNote.getTab_note());
            returnJSON.put("file",tabNote.getFile());
            returnJSON.put("imgs",tabNote.getImages());
            returnJSON.put("date_time", tabNote.getDate_time());

            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }

        return returnJSON;
    }

    @Override
    public JSONObject insertTabNote(String token, String usr_id, String ip_address, String class_name, String tab_note_name, String tags, String tab_note, String base64FileString, JSONArray imgs) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (accountMapper.tokenCheckIn(token).equals(usr_id)) {
                String date_time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String tab_note_id = usr_id.hashCode() + "" + System.currentTimeMillis();

                if (base64FileString.isEmpty() && imgs.isEmpty()) {
                    tabNoteMapper.insertTabNote(tab_note_id, usr_id, ip_address, class_name, tab_note_name, tags, tab_note, date_time);
                    returnJSON.put("response", "success");
                } else {
                    String fileName = "";
                    JSONObject imgJson = new JSONObject();
                    imgJson.putArray("images");

                    try{
                        if (!imgs.isEmpty()){
                            for(int i=0;i<imgs.size();i++){
                                imgJson.getJSONArray("images").add(fileService.insertImgWithOutIdCheck(imgs.getString(i)));
                            }
                        }
                    }catch (Exception e){
                        returnJSON.put("response", "img_insert_failed");
                        return returnJSON;
                    }

                    try{
                        if (!base64FileString.isEmpty()){
                            fileName = String.valueOf(fileService.insertFileWithOutIdCheck(base64FileString));
                        }
                    }catch (Exception e){
                        returnJSON.put("response", "img_insert_failed");
                        return returnJSON;
                    }
                    tabNoteMapper.insertTabNoteWithFile(tab_note_id, usr_id, ip_address, class_name, tab_note_name, tags, tab_note, date_time,fileName,imgJson.toString());
                    returnJSON.put("response", "success");
                }
            } else {
                returnJSON.put("response", "token_check_failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }

        return returnJSON;
    }

    @Override
    public JSONObject deleteTabNote(String tabNoteId) {
        JSONObject returnJSON = new JSONObject();

        try {
            tabNoteMapper.deleteTabNote(tabNoteId);
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }

        return returnJSON;
    }

    @Override
    public JSONObject updateTabNote(String tab_note_id, String ip_address, String tab_note_name, String tags, String tab_note, String date_time) {
        JSONObject returnJSON = new JSONObject();

        try {
            tabNoteMapper.updateTabNote(tab_note_id, ip_address, tab_note_name, tags, tab_note, date_time);
            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }

        return returnJSON;
    }


    @Override
    public JSONObject searchTabNote(String key, Integer page) {
        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.searchTabNote(key, start);
            returnJSON.putArray("list");
            for (TabNoteForList tabNoteForList : list) {
                JSONObject tabNoteJSON = new JSONObject();

                tabNoteJSON.put("tab_note_id", tabNoteForList.getTab_note_id());
                tabNoteJSON.put("usr_id", tabNoteForList.getUsr_id());
                tabNoteJSON.put("usr_name", accountMapper.getNameById(tabNoteForList.getUsr_id()));
                tabNoteJSON.put("class_name", tabNoteForList.getClass_name());
                tabNoteJSON.put("tab_note_name", tabNoteForList.getTab_note_name());
                tabNoteJSON.put("tags", tabNoteForList.getTags());
                tabNoteJSON.put("like_this", tabNoteMapper.getTabNoteLikeCount(tabNoteForList.getTab_note_id()));
                tabNoteJSON.put("click", tabNoteForList.getClick());
                tabNoteJSON.put("date_time", tabNoteForList.getDate_time());

                returnJSON.getJSONArray("list").add(tabNoteJSON);
            }
            int count = tabNoteMapper.searchTabNotePages(key);
            if (count % 20 == 0){
                count = count / 20;
            }else{
                count = count / 20 +1;
            }
            returnJSON.put("pages", count);

            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
        return returnJSON;
    }

    @Override
    public JSONObject searchTabNoteWithCls(String className, String key, Integer page) {
        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.searchTabNoteWithCls(className, key, start);
            returnJSON.putArray("list");
            for (TabNoteForList tabNoteForList : list) {
                JSONObject tabNoteJSON = new JSONObject();

                tabNoteJSON.put("tab_note_id", tabNoteForList.getTab_note_id());
                tabNoteJSON.put("usr_id", tabNoteForList.getUsr_id());
                tabNoteJSON.put("usr_name", accountMapper.getNameById(tabNoteForList.getUsr_id()));
                tabNoteJSON.put("class_name", tabNoteForList.getClass_name());
                tabNoteJSON.put("tab_note_name", tabNoteForList.getTab_note_name());
                tabNoteJSON.put("tags", tabNoteForList.getTags());
                tabNoteJSON.put("like_this", tabNoteMapper.getTabNoteLikeCount(tabNoteForList.getTab_note_id()));
                tabNoteJSON.put("click", tabNoteForList.getClick());
                tabNoteJSON.put("date_time", tabNoteForList.getDate_time());

                returnJSON.getJSONArray("list").add(tabNoteJSON);
            }
            int count = tabNoteMapper.searchTabNoteWithClsPages(className, key);
            if (count % 20 == 0){
                count = count / 20;
            }else{
                count = count / 20 +1;
            }
            returnJSON.put("pages", count);

            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
        return returnJSON;
    }


    @Override
    public JSONObject searchTabNoteById(String id, Integer page) {
        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.searchTabNoteById(id, start);
            returnJSON.putArray("list");
            for (TabNoteForList tabNoteForList : list) {
                JSONObject tabNoteJSON = new JSONObject();

                tabNoteJSON.put("tab_note_id", tabNoteForList.getTab_note_id());
                tabNoteJSON.put("usr_id", tabNoteForList.getUsr_id());
                tabNoteJSON.put("usr_name", accountMapper.getNameById(tabNoteForList.getUsr_id()));
                tabNoteJSON.put("class_name", tabNoteForList.getClass_name());
                tabNoteJSON.put("tab_note_name", tabNoteForList.getTab_note_name());
                tabNoteJSON.put("tags", tabNoteForList.getTags());
                tabNoteJSON.put("like_this", tabNoteMapper.getTabNoteLikeCount(tabNoteForList.getTab_note_id()));
                tabNoteJSON.put("click", tabNoteForList.getClick());
                tabNoteJSON.put("date_time", tabNoteForList.getDate_time());

                returnJSON.getJSONArray("list").add(tabNoteJSON);
            }
            int count = tabNoteMapper.searchTabNoteByIdPages(id);
            if (count % 20 == 0){
                count = count / 20;
            }else{
                count = count / 20 +1;
            }
            returnJSON.put("pages", count);

            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
        return returnJSON;
    }

    @Override
    public JSONObject searchTabNoteByClass(String className, Integer page) {
        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.searchTabNoteByClass(className, start);
            returnJSON.putArray("list");
            for (TabNoteForList tabNoteForList : list) {
                JSONObject tabNoteJSON = new JSONObject();

                tabNoteJSON.put("tab_note_id", tabNoteForList.getTab_note_id());
                tabNoteJSON.put("usr_id", tabNoteForList.getUsr_id());
                tabNoteJSON.put("usr_name", accountMapper.getNameById(tabNoteForList.getUsr_id()));
                tabNoteJSON.put("class_name", tabNoteForList.getClass_name());
                tabNoteJSON.put("tab_note_name", tabNoteForList.getTab_note_name());
                tabNoteJSON.put("tags", tabNoteForList.getTags());
                tabNoteJSON.put("like_this", tabNoteMapper.getTabNoteLikeCount(tabNoteForList.getTab_note_id()));
                tabNoteJSON.put("click", tabNoteForList.getClick());
                tabNoteJSON.put("date_time", tabNoteForList.getDate_time());

                returnJSON.getJSONArray("list").add(tabNoteJSON);
            }
            int count = tabNoteMapper.searchTabNoteByClassPages(className);
            if (count % 20 == 0){
                count = count / 20;
            }else{
                count = count / 20 +1;
            }
            returnJSON.put("pages", count);

            returnJSON.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
        return returnJSON;
    }
}
