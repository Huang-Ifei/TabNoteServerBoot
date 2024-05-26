package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;

public interface NoteServiceInterface {

    //将本地存储的计划与服务器进行同步的方法，返回的JSON不带JSONArray而是以数字为key的JSON
    JSONObject notesRequest(JSONObject jsonObject);
    //将本地存储的历史计划与服务器进行同步的方法，返回的JSON不带JSONArray而是以数字为key的JSON
    JSONObject historyNotesRequest(JSONObject jsonObject);
    //直接获取所有的历史计划
    JSONObject addNote(JSONObject jsonObject);

    JSONObject addHistoryNote(JSONObject jsonObject);

    JSONObject deleteNote(JSONObject jsonObject);

    JSONObject resetNote(JSONObject jsonObject);

    JSONObject finishNote(JSONObject jsonObject);
}
