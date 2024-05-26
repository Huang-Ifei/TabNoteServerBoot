package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.TabNote;
import com.tabnote.server.tabnoteserverboot.models.TabNoteForList;
import com.tabnote.server.tabnoteserverboot.models.TabNoteMessage;
import org.apache.ibatis.annotations.*;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface TabNoteMapper {
    //id由用户id的hashcode和时间戳组成
    @Insert("insert into tab_notes (tab_note_id,usr_id,ip_address,class_name,tab_note_name,tags,tab_note,date_time) values (#{0},#{1},#{2},#{3},#{4},#{5},#{6},#{7});")
    void insertTabNote(@Param("0") String tab_note_id,@Param("1") String usr_id,@Param("2") String ip_address,@Param("3") String class_name,@Param("4") String tab_note_name,@Param("5") String tags,@Param("6") String tab_note,@Param("7") String date_time);
    @Delete("delete from tab_notes where tab_note_id=#{0}")
    void deleteTabNote(@Param("0")String tab_note_id);
    //分类名称，用户id,tab_note_id不可更改
    @Update("update tab_notes set ip_address=#{1},tab_note_name=#{2},tags=#{3},tab_note=#{4},date_time=#{5} where tab_note_id=#{0}")
    void updateTabNote(@Param("0") String tab_note_id,@Param("1") String ip_address,@Param("2") String tab_note_name,@Param("3") String tags,@Param("4") String tab_note,@Param("5") String date_time);
    @Select("select count(*)/20+1 from tab_notes")
    Integer getTabNotePages();
    //需要提前将page页面*20制定start
    @Select("select tab_note_id,usr_id,class_name,tab_note_name,tags,date_time from tab_notes order by date_time desc limit 20 offset #{start}")
    List<TabNoteForList> getTabNote(@Param("start") int start);
    @Select("select * from tab_notes where tab_note_id=#{0}")
    TabNote getTabNoteById(@Param("0")String id);
    @Update("update tab_notes set click=click+1 where tab_note_id=#{0}")
    void clickThis(@Param("0") String tab_note_id);

    @Select("select count(*) from like_note where tab_note_id=#{t_id}")
    Integer getTabNoteLikeCount(@Param("t_id")String tab_note_id);

    @Insert("insert into like_note values (#{t_id},#{u_id},CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE date = CURRENT_TIMESTAMP;")
    void likeNote(@Param("t_id")String tab_note_id,@Param("u_id")String usr_id);

    @Insert("insert into click_note values (#{t_id},#{u_id},CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE date = CURRENT_TIMESTAMP;")
    void clickNote(@Param("t_id")String tab_note_id,@Param("u_id")String usr_id);

}
