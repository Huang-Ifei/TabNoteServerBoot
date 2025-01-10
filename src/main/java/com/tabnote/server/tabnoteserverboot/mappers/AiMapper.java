package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiMapper {
    @Insert("INSERT INTO ai_messages (ai_ms_id,mainly,usr_id,contents,date_time) VALUES (#{ai_ms_id},#{mainly},#{usr_id},#{contents},#{date_time});")
    void addNewAiMessages(@Param("ai_ms_id") String ai_ms_id, @Param("mainly") String mainly, @Param("usr_id") String usr_id, @Param("contents") String contents, @Param("date_time") String date_time);

    @Update("UPDATE ai_messages SET contents=#{contents} ,date_time=#{date_time} WHERE ai_ms_id=#{ai_ms_id}")
    void changeAiMessages(@Param("contents") String contents, @Param("date_time") String date_time, @Param("ai_ms_id") String ai_ms_id);

    @Select("SELECT ai_ms_id,mainly,date_time FROM ai_messages WHERE usr_id=#{usr_id} ORDER BY date_time DESC")
    List<AiMessagesForList> getUsrAiList(@Param("usr_id") String usr_id);

    @Select("SELECT * FROM ai_messages WHERE ai_ms_id=#{ai_ms_id}")
    AiMessages getUsrAiMessages(@Param("ai_ms_id") String ai_ms_id);

    @Delete("DELETE FROM ai_messages WHERE usr_id=#{ai_ms_id}")
    void deleteAiMessages(@Param("ai_ms_id") String ai_ms_id);

    @Insert("INSERT INTO note_ai (note_ai_id, usr_id, mainly, note, ai_mess, date_time) VALUES (#{0},#{1},#{mainly} ,#{2},#{3},CURRENT_TIMESTAMP);")
    void addNewNoteAI(@Param("0") String note_ai_id, @Param("1") String usr_id, @Param("mainly") String mainly, @Param("2") String note, @Param("3") String ai_mess);

    @Update("UPDATE note_ai SET note = #{note} , ai_mess=#{ai_mess} , date_time = CURRENT_TIMESTAMP WHERE note_ai_id = #{note_ai_id}")
    void changeNoteAI(@Param("note") String note, @Param("ai_mess") String ai_mess, @Param("note_ai_id") String note_ai_id);

    @Select("SELECT note_ai_id,mainly,date_time FROM note_ai WHERE usr_id = #{usr_id} ORDER BY date_time DESC")
    List<NoteAiForList> getUsrNoteAiList(@Param("usr_id") String usr_id);

    @Select("SELECT  * FROM note_ai WHERE note_ai_id = #{note_ai_id}")
    NoteAi getUsrNoteAi(@Param("note_ai_id") String note_ai_id);

    @Insert("INSERT INTO beat_questions (bq_id, usr_id, date_time, img, text, ai_answer, dxstj) " +
            "VALUES (#{bq_id}, #{usr_id}, current_timestamp , #{img}, #{text}, #{ai_answer}, #{dxstj})")
    void insertBQ(BQ beatQuestion);

    @Select("SELECT bq_id,date_time,img FROM beat_questions WHERE usr_id = #{usrId} ORDER BY date_time DESC LIMIT 10 OFFSET #{index}")
    List<BQForList> getBQListByUserId(@Param("usrId") String usrId, @Param("index") int index);

    @Select("SELECT * FROM beat_questions WHERE bq_id=#{bqId} AND usr_id=#{usrId}")
    BQ getBQById(@Param("usrId") String usrId, @Param("bqId") String bqId);
}
