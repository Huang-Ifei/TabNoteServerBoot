package com.tabnote.server.tabnoteserverboot.mappers;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ResetIdMapper {

    @Update("update ai_messages set usr_id=#{newId} where usr_id=#{oldId}")
    void updateAiMId(@Param("oldId") String oldId, @Param("newId") String newId);
    @Update("UPDATE beat_questions SET usr_id = #{id}  WHERE usr_id =#{old_id};")
    void updateBQId(@Param("old_id") String old_id, @Param("id") String id);
    @Update("update click_note set usr_id=#{newId} where usr_id=#{oldId}")
    void updateClickNoteId(@Param("oldId")String oldId ,@Param("newId") String newId);
    @Update("update history_notes set user_id=#{newId} where user_id=#{oldId}")
    void updateHisNoteId(@Param("oldId") String oldId, @Param("newId") String newId);
    @Update("update like_mess set usr_id=#{newId} where usr_id=#{oldId}")
    void updateMessMess(@Param("oldId")String oldId ,@Param("newId") String newId);
    @Update("update like_note set usr_id=#{newId} where usr_id=#{oldId}")
    void updateLikeNote(@Param("oldId")String oldId ,@Param("newId") String newId);
    @Update("update like_tab_mess set usr_id=#{newId} where usr_id=#{oldId}")
    void updateTabMess(@Param("oldId")String oldId ,@Param("newId") String newId);
    @Update("update messages_messages set usr_id=#{newId} where usr_id=#{oldId}")
    void updateMessMessId(@Param("oldId") String oldId, @Param("newId") String newId);
    @Update("update note_ai set usr_id=#{newId} where usr_id=#{oldId}")
    void updateNoteAi(@Param("oldId")String oldId ,@Param("newId") String newId);
    @Update("update notes set user_id=#{newId} where user_id=#{oldId}")
    void updateNoteId(@Param("oldId") String oldId, @Param("newId") String newId);
    @Update("update plans set usr_id=#{newId} where usr_id=#{oldId}")
    void updatePlan(@Param("oldId") String oldId, @Param("newId") String newId);
    @Update("update tab_notes set usr_id=#{newId} where usr_id=#{oldId}")
    void updateTabNoteId(@Param("oldId") String oldId, @Param("newId") String newId);
    @Update("update tab_notes_messages set usr_id=#{newId} where usr_id=#{oldId}")
    void updateMessagesId(@Param("oldId") String oldId, @Param("newId") String newId);
    @Update("UPDATE tokens SET id = #{id} WHERE token = #{token};")
    void updateToken(@Param("id") String id, @Param("token") String token);
    @Update("UPDATE user SET id = #{id}  WHERE id =#{old_id};")
    void resetID(@Param("old_id") String old_id, @Param("id") String id);
    @Update("UPDATE vip SET usr_id = #{newId} WHERE usr_id=#{oldId}")
    void updateVIPId(@Param("oldId") String oldId, @Param("newId") String newId);
}
