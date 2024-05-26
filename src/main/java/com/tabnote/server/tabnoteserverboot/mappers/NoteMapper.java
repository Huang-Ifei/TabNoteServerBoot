package com.tabnote.server.tabnoteserverboot.mappers;

import org.apache.ibatis.annotations.*;

import java.util.HashMap;
import java.util.List;
@Mapper
public interface NoteMapper {
    @Insert("INSERT INTO notes (`user_id`, `content`, `link`, `date`) VALUES (#{id}, #{content}, #{link}, #{date});")
    void addNote(@Param("id") String id, @Param("content") String content, @Param("link") String link, @Param("date") String date);
    @Select("SELECT * FROM notes WHERE user_id = #{id} AND content = #{content} AND link = #{link} AND date = #{date};")
    List<HashMap<String, String>> noteFind(@Param("id") String id, @Param("content") String content, @Param("link") String link, @Param("date") String date);
    @Select("SELECT * FROM notes WHERE user_id = #{id};")
    List<HashMap<String, String>> notesRequest(@Param("id") String id);
    @Select("SELECT * FROM history_notes WHERE user_id = #{id};")
    List<HashMap<String, String>> historyNotesRequest(@Param("id") String id);
    @Insert("INSERT INTO history_notes (`user_id`, `content`, `link`, `date`) VALUES (#{id}, #{content}, #{link}, #{date});")
    void addHistoryNote(@Param("id") String id, @Param("content") String content, @Param("link") String link, @Param("date") String date);
    @Select("SELECT * FROM history_notes WHERE user_id = #{id} AND content = #{content} AND link = #{link} AND date = #{date};")
    List<HashMap<String, String>> historyNoteFind(@Param("id") String id, @Param("content") String content, @Param("link") String link, @Param("date") String date);
    @Delete("DELETE FROM notes WHERE user_id = #{id} AND content = #{content} AND link = #{link} AND date = #{date};")
    void deleteNote(@Param("id") String id, @Param("content") String content, @Param("link") String link, @Param("date") String date);
    @Update("UPDATE notes SET user_id=#{id}, content= #{new_content}, link= #{new_link}, date=#{new_date}  WHERE user_id = #{id} AND content = #{old_content} AND link = #{old_link} AND date = #{old_date};")
    void resetNote(@Param("id") String id, @Param("old_content") String old_content, @Param("new_content") String new_content, @Param("old_link") String old_link, @Param("new_link") String new_link, @Param("old_date") String old_date, @Param("new_date") String new_date);
}
