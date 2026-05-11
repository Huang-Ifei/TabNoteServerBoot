package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.Homework;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HomeworkMapper {

    @Insert("insert into homework (homework_id, class_id, title, description, deadline, total_score, create_time, update_time, display) values (#{0}, #{1}, #{2}, #{3}, #{4}, #{5}, #{6}, #{7}, #{8})")
    void insertHomework(@Param("0") String homework_id, @Param("1") String class_id, @Param("2") String title, @Param("3") String description, @Param("4") String deadline, @Param("5") Double total_score, @Param("6") String create_time, @Param("7") String update_time, @Param("8") Integer display);

    @Delete("delete from homework where homework_id = #{0}")
    void deleteHomework(@Param("0") String homework_id);

    @Update("update homework set title = #{1}, description = #{2}, deadline = #{3}, update_time = #{4} where homework_id = #{0}")
    void updateHomework(@Param("0") String homework_id, @Param("1") String title, @Param("2") String description, @Param("3") String deadline, @Param("4") String update_time);

    @Update("update homework set total_score = #{1}, update_time = #{2} where homework_id = #{0}")
    void updateHomeworkTotalScore(@Param("0") String homework_id, @Param("1") Double total_score, @Param("2") String update_time);

    @Select("select * from homework where homework_id = #{0}")
    Homework getHomeworkById(@Param("0") String homework_id);

    @Select("select * from homework where class_id = #{0} and display = 0 order by create_time desc limit #{limit} offset #{offset}")
    List<Homework> getHomeworkListByClassId(@Param("0") String class_id, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from homework where class_id = #{0} and display = 0")
    Integer getHomeworkCountByClassId(@Param("0") String class_id);
}
