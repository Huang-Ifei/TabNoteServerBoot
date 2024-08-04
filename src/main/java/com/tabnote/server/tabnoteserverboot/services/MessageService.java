package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.MessageMapper;
import com.tabnote.server.tabnoteserverboot.models.MessageMessage;
import com.tabnote.server.tabnoteserverboot.models.TabNoteMessage;
import com.tabnote.server.tabnoteserverboot.redis.MessLikeCount;
import com.tabnote.server.tabnoteserverboot.services.inteface.MessageServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService implements MessageServiceInterface {
    AccountMapper accountMapper;
    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    MessageMapper messageMapper;
    @Autowired
    public void setMessageMapper(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    MessLikeCount messLikeCount;
    @Autowired
    public void setMessLikeCount(MessLikeCount messLikeCount) {
        this.messLikeCount = messLikeCount;
    }
    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;
    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tie) {
        this.tabNoteInfiniteEncryption = tie;
    }

    @Override
    public JSONObject getTabNoteMessage(String tab_note_id, Integer start,String usr_id) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putArray("messages");
        try {
            List<TabNoteMessage> tabNoteMessages = messageMapper.getTabNoteMessages(tab_note_id, start);
            for(TabNoteMessage tabNoteMessage : tabNoteMessages) {
                JSONObject tabNoteMessageJson = new JSONObject();
                tabNoteMessageJson.put("tab_note_id", tabNoteMessage.getTab_note_id());
                tabNoteMessageJson.put("message_id", tabNoteMessage.getMessage_id());
                tabNoteMessageJson.put("message", tabNoteMessage.getMessage());
                tabNoteMessageJson.put("usr_id", tabNoteMessage.getUsr_id());
                tabNoteMessageJson.put("usr_name",accountMapper.getNameById(tabNoteMessage.getUsr_id()));
                tabNoteMessageJson.put("date_time", tabNoteMessage.getDate_time());
                tabNoteMessageJson.put("ip_address", tabNoteMessage.getIp_address());
                tabNoteMessageJson.put("liked",messageMapper.tabMessIsLiked(tabNoteMessage.getMessage_id(),usr_id));
                tabNoteMessageJson.put("like_this",messLikeCount.getTabMessLikeCount(tabNoteMessage.getMessage_id()));
                jsonObject.getJSONArray("messages").add(tabNoteMessageJson);
            }
            jsonObject.put("response","success");
        }catch (Exception e){
            e.printStackTrace();
            jsonObject.put("response","failed");
        }
        return jsonObject;
    }

    @Override
    public JSONObject insertTabNoteMessage(String id, String token, String ip_address, String tab_note_id, String message) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
                String tm = id.hashCode()+""+System.currentTimeMillis();
                messageMapper.insertTabNoteMessage(tm, id, ip_address, tab_note_id, message);
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
    public JSONObject getMessageMessage(String from_tab_mess, Integer start,String usr_id) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putArray("messages");
        try {
            List<MessageMessage> messageMessages = messageMapper.getMessageMessages(from_tab_mess, start);

            for(MessageMessage messageMessage : messageMessages) {
                JSONObject messageJson = new JSONObject();
                messageJson.put("message_id", messageMessage.getMessage_id());
                messageJson.put("message", messageMessage.getMessage());
                messageJson.put("usr_id", messageMessage.getUsr_id());
                messageJson.put("usr_name",accountMapper.getNameById(messageMessage.getUsr_id()));
                messageJson.put("date_time", messageMessage.getDate_time());
                messageJson.put("ip_address", messageMessage.getIp_address());
                messageJson.put("liked",messageMapper.messMessIsLiked(messageMessage.getMessage_id(),usr_id));
                messageJson.put("like_this",messLikeCount.getMessMessLikeCount(messageMessage.getMessage_id()));
                if (messageMessage.getReply_message_id().equals(messageMessage.getFrom_tab_mess())){
                    messageJson.put("reply_usr","");
                }else {
                    messageJson.put("reply_usr",messageMapper.getWhichReply(messageMessage.getReply_message_id()));
                }
                jsonObject.getJSONArray("messages").add(messageJson);
            }

            jsonObject.put("response","success");
        }catch (Exception e){
            e.printStackTrace();
            jsonObject.put("response","failed");
        }
        return jsonObject;
    }

    @Override
    public JSONObject insertMessageMessage(String id, String token, String ip_address, String reply_message_id, String message,String from_tab_mess) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)){
                String mm = id.hashCode()+""+System.currentTimeMillis();
                messageMapper.insertMessageMessage(mm, id, ip_address, reply_message_id, message,from_tab_mess);
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
    public JSONObject likeTabMess(String message_id, String id, String token) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)) {
                messLikeCount.likeTabMess(message_id, id);
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
    public JSONObject likeMessMess(String message_id, String id, String token) {
        JSONObject returnJSON = new JSONObject();
        try {
            if (tabNoteInfiniteEncryption.encryptionTokenCheckIn(id,token)) {
                messLikeCount.likeMessMess(message_id, id);
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
}
