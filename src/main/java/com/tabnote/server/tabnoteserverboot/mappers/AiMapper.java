package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.AiMessages;
import com.tabnote.server.tabnoteserverboot.models.AiMessagesForList;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiMapper {
    @Insert("INSERT INTO ai_messages (ai_ms_id,mainly,usr_id,contents,date_time) VALUES (#{ai_ms_id},#{mainly},#{usr_id},#{contents},#{date_time});")
    void addNewAiMessages(@Param("ai_ms_id")String ai_ms_id ,@Param("mainly")String mainly,@Param("usr_id")String usr_id,@Param("contents")String contents,@Param("date_time")String date_time);
    @Update("UPDATE ai_messages SET contents=#{contents} ,date_time=#{date_time} WHERE ai_ms_id=#{ai_ms_id}")
    void changeAiMessages(@Param("contents")String contents,@Param("date_time")String date_time,@Param("ai_ms_id")String ai_ms_id);
    @Select("SELECT ai_ms_id,mainly,date_time FROM ai_messages WHERE usr_id=#{usr_id} ORDER BY date_time DESC")
    List<AiMessagesForList> getUsrAiList(@Param("usr_id")String usr_id);
    @Select("SELECT * FROM ai_messages WHERE ai_ms_id=#{ai_ms_id}")
    AiMessages getUsrAiMessages(@Param("ai_ms_id")String ai_ms_id);
    @Delete("DELETE FROM ai_messages WHERE usr_id=#{ai_ms_id}")
    void deleteAiMessages(@Param("ai_ms_id")String ai_ms_id);
}
