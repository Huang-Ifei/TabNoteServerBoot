package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.MessageMessage;
import com.tabnote.server.tabnoteserverboot.models.TabNoteMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper {
    //id由用户id的hashcode和时间戳组成
    @Insert("insert into tab_notes_messages (message_id, usr_id, ip_address, tab_note_id, message, date_time) VALUES (#{0},#{1},#{2},#{3},#{4},CURRENT_TIMESTAMP)")
    void insertTabNoteMessage(@Param("0") String message_id,@Param("1") String usr_id,@Param("2") String ip_address,@Param("3") String tab_note_id,@Param("4") String message);
    @Select("select * from tab_notes_messages where tab_note_id=#{0} order by date_time desc limit 10 offset #{start}")
    List<TabNoteMessage> getTabNoteMessages(@Param("0") String tab_note_id,@Param("start") int start);
    @Insert("insert into messages_messages (message_id, usr_id, ip_address, reply_message_id, message, date_time,from_tab_mess) VALUES (#{0},#{1},#{2},#{3},#{4},CURRENT_TIMESTAMP,#{5})")
    void insertMessageMessage(@Param("0") String message_id,@Param("1") String usr_id,@Param("2") String ip_address,@Param("3") String reply_message_id,@Param("4") String message,@Param("5") String from_tab_mess);
    @Select("select name from user where id=(select usr_id from messages_messages where message_id=#{0}) ")
    String getWhichReply(@Param("0")String reply_mess_id);
    @Select("select * from messages_messages where from_tab_mess=#{0} order by date_time desc limit 3 offset #{1}")
    List<MessageMessage> getMessageMessages(@Param("0") String from_tab_mess, @Param("1") Integer start);
    @Insert("insert into like_mess values (#{mm_id},#{u_id},CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE date_time = CURRENT_TIMESTAMP;")
    void likeMess(@Param("mm_id")String mess_mess_id,@Param("u_id")String usr_id);
    @Insert("insert into like_tab_mess values (#{tm_id},#{u_id},CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE date_time = CURRENT_TIMESTAMP;")
    void likeTabMess(@Param("tm_id")String tab_mess_id,@Param("u_id")String usr_id);
    @Select("select count(*) from like_tab_mess where tab_message_id=#{tm_id}")
    Integer getTabMessLikeCount(@Param("tm_id")String tab_mess_id);
    @Select("select count(*) from like_mess where message_id=#{mm_id}")
    Integer getMessMessLikeCount(@Param("mm_id")String mess_mess_id);
    @Select("select count(*) from like_tab_mess where tab_message_id=#{tm_id} and usr_id=#{usr_id}")
    Integer tabMessIsLiked(@Param("tm_id")String tab_mess_id,@Param("usr_id")String usr_id);
    @Select("select count(*) from like_mess where message_id=#{mm_id} and usr_id=#{usr_id}")
    Integer messMessIsLiked(@Param("mm_id")String mess_mess_id,@Param("usr_id")String usr_id);
}
