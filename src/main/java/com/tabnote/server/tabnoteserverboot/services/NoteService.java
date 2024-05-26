package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.ClassMapper;
import com.tabnote.server.tabnoteserverboot.mappers.NoteMapper;
import com.tabnote.server.tabnoteserverboot.services.inteface.NoteServiceInterface;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class NoteService implements NoteServiceInterface {
    NoteMapper mapper;
    AccountMapper accountMapper;
    @Autowired
    public void setMapper(NoteMapper mapper) {
        this.mapper = mapper;
    }
    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public JSONObject notesRequest(JSONObject jsonObject) {

        JSONObject json = new JSONObject();
        JSONObject j;
        //token确认
        String id = accountMapper.tokenCheckIn(jsonObject.getString("token"));
        if (id == null||id.isEmpty()) return json;

        List<Integer> newNotes = new ArrayList<>();
        List<HashMap<String, String>> list = new ArrayList<>();
        try {
            //把数据库中的所有内容读取出来
            list = mapper.notesRequest(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //remove传入的信息与数据库中相同的
        for (int i = 0; i < jsonObject.size() - 2; i++) {
            j = jsonObject.getJSONObject(i + "");
            try {
                List<HashMap<String, String>> maps = mapper.noteFind(id, j.getString("content"), j.getString("link"), j.getString("date"));
                if (maps.size() == 0) {
                    newNotes.add(i);
                } else {
                    for (int m = 0; m < maps.size(); m++) {
                        HashMap<String, String> map = maps.get(m);
                        if (list.contains(map)) list.remove(map);
                    }
                }
            } catch (TooManyResultsException e) {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            //写入数据库剩余的内容
            for (int i = 0; i < newNotes.size(); i++) {
                j = jsonObject.getJSONObject(newNotes.get(i) + "");
                mapper.addNote(id, j.getString("content"), j.getString("link"), j.getString("date"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < list.size(); i++) {
            HashMap<String, String> map = list.get(i);
            j = new JSONObject();
            j.put("content", map.get("content"));
            j.put("link", map.get("link"));
            j.put("date", map.get("date"));
            json.put(i + "", j.toString());
        }
        return json;
    }
    @Override
    public JSONObject historyNotesRequest(JSONObject jsonObject) {

        JSONObject json = new JSONObject();
        JSONObject j;
        //token确认
        String id = accountMapper.tokenCheckIn(jsonObject.getString("token"));
        if (id == null||id.isEmpty()) return json;

        List<Integer> newNotes = new ArrayList<>();
        List<HashMap<String, String>> list = new ArrayList<>();
        try {
            //把数据库中的所有内容读取出来
            list = mapper.historyNotesRequest(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //remove传入的信息与数据库中相同的
        for (int i = 0; i < jsonObject.size() - 2; i++) {
            j = jsonObject.getJSONObject(i + "");
            try {
                List<HashMap<String, String>> maps = mapper.historyNoteFind(id, j.getString("content"), j.getString("link"), j.getString("date"));
                if (maps.size() == 0) {
                    newNotes.add(i);
                } else {
                    for (int m = 0; m < maps.size(); m++) {
                        HashMap<String, String> map = maps.get(m);
                        if (list.contains(map)) list.remove(map);

                    }
                }
            } catch (TooManyResultsException e) {

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            //写入数据库剩余的内容
            for (int i = 0; i < newNotes.size(); i++) {
                j = jsonObject.getJSONObject(newNotes.get(i) + "");
                mapper.addHistoryNote(id, j.getString("content"), j.getString("link"), j.getString("date"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < list.size(); i++) {
            HashMap<String, String> map = list.get(i);
            j = new JSONObject();
            j.put("content", map.get("content"));
            j.put("link", map.get("link"));
            j.put("date", map.get("date"));
            json.put(i + "", j.toString());
        }
        return json;
    }
    @Override
    public JSONObject addNote(JSONObject jsonObject) {
        JSONObject json = new JSONObject();
        try {
            //token确认
            String id = accountMapper.tokenCheckIn(jsonObject.getString("token"));
            if (id == null||id.isEmpty()) return json;

            mapper.addNote(id, jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date"));
            json.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            json.put("response", "failed");
        }
        return json;
    }
    @Override
    public JSONObject addHistoryNote(JSONObject jsonObject) {
        JSONObject json = new JSONObject();
        try {
            //token确认
            String id = accountMapper.tokenCheckIn(jsonObject.getString("token"));
            if (id == null||id.isEmpty()) return json;

            mapper.addHistoryNote(id, jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date"));
            json.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            json.put("response", "failed");
        }
        return json;
    }
    @Override
    public JSONObject deleteNote(JSONObject jsonObject) {
        JSONObject json = new JSONObject();
        try {
            //token确认
            String id = accountMapper.tokenCheckIn(jsonObject.getString("token"));
            if (id == null||id.isEmpty()) return json;

            mapper.deleteNote(id, jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date"));
            json.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            json.put("response", "failed");
        }
        return json;
    }
    @Override
    public JSONObject resetNote(JSONObject jsonObject) {
        JSONObject json = new JSONObject();
        try {
            //token确认
            String id = accountMapper.tokenCheckIn(jsonObject.getString("token"));
            if (id == null||id.isEmpty()) return json;

            mapper.resetNote(id, jsonObject.getString("old_content"), jsonObject.getString("new_content"), jsonObject.getString("old_link"), jsonObject.getString("new_link"), jsonObject.getString("old_date"), jsonObject.getString("new_date"));
            json.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            json.put("response", "failed");
        }
        return json;
    }
    @Override
    public JSONObject finishNote(JSONObject jsonObject) {
        JSONObject json = new JSONObject();
        try {
            //token确认
            String id = accountMapper.tokenCheckIn(jsonObject.getString("token"));
            if (id == null||id.isEmpty()) return json;

            mapper.deleteNote(id, jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date"));
            mapper.addHistoryNote(jsonObject.getString("id"), jsonObject.getString("content"), jsonObject.getString("link"), jsonObject.getString("date"));
            json.put("response", "success");
        } catch (Exception e) {
            e.printStackTrace();
            json.put("response", "failed");
        }
        return json;
    }
}
