package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.TabNote;
import com.tabnote.server.tabnoteserverboot.models.TabNoteForList;
import com.tabnote.server.tabnoteserverboot.models.TabNoteMessage;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface TabNoteMapper {
    //id由用户id的hashcode和时间戳组成
    @Insert("insert into tab_notes (tab_note_id,usr_id,ip_address,class_name,tab_note_name,tags,tab_note,date_time,display) values (#{0},#{1},#{2},#{3},#{4},#{5},#{6},#{7},#{8});")
    void insertTabNote(@Param("0") String tab_note_id,@Param("1") String usr_id,@Param("2") String ip_address,@Param("3") String class_name,@Param("4") String tab_note_name,@Param("5") String tags,@Param("6") String tab_note,@Param("7") String date_time,@Param("8")int display);

    @Insert("insert into tab_notes (tab_note_id,usr_id,ip_address,class_name,tab_note_name,tags,tab_note,date_time,file,images,display) values (#{0},#{1},#{2},#{3},#{4},#{5},#{6},#{7},#{8},#{9},#{10});")
    void insertTabNoteWithFile(@Param("0") String tab_note_id,@Param("1") String usr_id,@Param("2") String ip_address,@Param("3") String class_name,@Param("4") String tab_note_name,@Param("5") String tags,@Param("6") String tab_note,@Param("7") String date_time,@Param("8") String file,@Param("9")String imgs,@Param("10")int display);

    @Delete("delete from tab_notes where tab_note_id=#{0}")
    void deleteTabNote(@Param("0")String tab_note_id);
    //分类名称，用户id,tab_note_id不可更改
    @Update("update tab_notes set ip_address=#{1},tab_note_name=#{2},tags=#{3},tab_note=#{4},date_time=#{5} where tab_note_id=#{0}")
    void updateTabNote(@Param("0") String tab_note_id,@Param("1") String ip_address,@Param("2") String tab_note_name,@Param("3") String tags,@Param("4") String tab_note,@Param("5") String date_time);
    @Select("select count(*) from tab_notes")
    Integer getTabNotePages();
    //需要提前将page页面*20制定start
    @Select("select tab_note_id,usr_id,class_name,tab_note_name,tags,date_time from tab_notes order by date_time desc limit 20 offset #{start}")
    List<TabNoteForList> getTabNote(@Param("start") int start);
    @Select("select * from tab_notes where tab_note_id=#{0}")
    TabNote getTabNoteById(@Param("0")String id);
    @Update("update tab_notes set click=click+1 where tab_note_id=#{0}")
    void clickThis(@Param("0") String tab_note_id);

    @Select("select count(*) from tab_notes where lower(tab_note_name) like concat('%',#{key},'%') or lower(class_name) like concat('%',#{key},'%') or lower(tags) like concat('%',#{key},'%') or usr_id in (select id from user where name like concat('%',#{key},'%')) ")
    Integer searchTabNotePages(@Param("key")String keyword);
    @Select("select tab_note_id,usr_id,class_name,tab_note_name,tags,date_time from tab_notes where lower(tab_note_name) like concat('%',#{key},'%') or lower(class_name) like concat('%',#{key},'%') or lower(tags) like concat('%',#{key},'%') or usr_id in (select id from user where name like concat('%',#{key},'%')) order by date_time desc limit 20 offset #{start}")
    List<TabNoteForList> searchTabNote(@Param("key")String keyword,@Param("start")Integer start);

    @Select("select count(*) from tab_notes where lower(tab_note_name) like concat('%',#{key},'%') or lower(class_name) like concat('%',#{key},'%') or lower(tags) like concat('%',#{key},'%') or usr_id in (select id from user where name like concat('%',#{key},'%')) and class_name=#{class_name}")
    Integer searchTabNoteWithClsPages(@Param("class_name")String className,@Param("key")String keyword);
    @Select("select tab_note_id,usr_id,class_name,tab_note_name,tags,date_time from tab_notes where (lower(tab_note_name) like concat('%',#{key},'%') or lower(class_name) like concat('%',#{key},'%') or lower(tags) like concat('%',#{key},'%') or usr_id in (select id from user where name like concat('%',#{key},'%'))) and class_name=#{class_name} order by date_time desc limit 20 offset #{start}")
    List<TabNoteForList> searchTabNoteWithCls(@Param("class_name")String className,@Param("key")String keyword,@Param("start")Integer start);


    @Select("select count(*) from tab_notes where usr_id in (select id from user where name like concat('%',#{name},'%')) ")
    Integer searchTabNoteByNamePages(@Param("name")String name);
    @Select("select tab_note_id,usr_id,class_name,tab_note_name,tags,date_time from tab_notes where usr_id in(select id from user where name like concat('%',#{name},'%')) order by date_time desc limit 20 offset #{start}")
    List<TabNoteForList> searchTabNoteByName(@Param("name")String name,@Param("start")Integer start);

    @Select("select count(*) from tab_notes where usr_id=#{id} ")
    Integer searchTabNoteByIdPages(@Param("id")String id);
    @Select("select tab_note_id,usr_id,class_name,tab_note_name,tags,date_time from tab_notes where usr_id=#{id} order by date_time desc limit 20 offset #{start}")
    List<TabNoteForList> searchTabNoteById(@Param("id")String id,@Param("start")Integer start);

    @Select("select count(*) from tab_notes where class_name=#{class_name} ")
    Integer searchTabNoteByClassPages(@Param("class_name")String className);
    @Select("select tab_note_id,usr_id,class_name,tab_note_name,tags,date_time from tab_notes where class_name=#{class_name} order by date_time desc limit 20 offset #{start}")
    List<TabNoteForList> searchTabNoteByClass(@Param("class_name")String className,@Param("start")Integer start);


    @Select("select count(*) from like_note where tab_note_id=#{t_id};")
    Integer getTabNoteLikeCount(@Param("t_id")String tab_note_id);

    @Select("select count(*) from like_note where usr_id=#{u_id} and tab_note_id=#{t_id}")
    Integer isLiked(@Param("t_id")String tab_note_id,@Param("u_id")String usr_id);

    @Insert("insert into like_note values (#{t_id},#{u_id},CURRENT_TIMESTAMP);")
    void likeNote(@Param("t_id")String tab_note_id,@Param("u_id")String usr_id);

    @Insert("insert into click_note values (#{t_id},#{u_id},CURRENT_TIMESTAMP) ON DUPLICATE KEY UPDATE date = CURRENT_TIMESTAMP;")
    void clickNote(@Param("t_id")String tab_note_id,@Param("u_id")String usr_id);

    //获取标签
    @Select("select tab_note_id from click_note where usr_id=#{usr_id} order by date desc limit 1;")
    String selectLastTabNoteId(@Param("usr_id")String usr_id);
    @Select("select usr_id,tags,date_time from tab_notes where tab_note_id=#{tab_note_id};")
    Map<String,Object> selectUTD(@Param("tab_note_id")String tab_note_id);
    @Select("select tab_note_name from tab_notes where usr_id = #{usr_id} and date_time > #{date_time} order by date_time limit 1")
    String selectUserNextTNN(@Param("usr_id")String usr_id,@Param("date_time") Timestamp date_time);
    @Select("select tab_note_name from tab_notes where tags like concat('%',#{tag},'%') and date_time > #{date_time} order by date_time limit 1")
    String selectTagNextTNN(@Param("tag")String tag,@Param("date_time")Timestamp date_time);
    @Select("select l.tags from (select tags,tab_note_id from tab_notes order by date_time desc limit 200) l join (select tab_note_id from click_note where usr_id = #{usr_id} order by date desc limit 15) r on l.tab_note_id = r.tab_note_id")
    List<String> selectUserLastTags(@Param("usr_id")String usr_id);
    @Select("select l.tags from (select tags,tab_note_id from tab_notes order by date_time desc limit 200) l join (select tab_note_id from click_note order by date desc limit 50) r on l.tab_note_id = r.tab_note_id")
    List<String> selectAllLastTags();
}
