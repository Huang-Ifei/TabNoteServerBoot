package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.Teacher;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TeacherMapper {

    @Insert("insert into teacher (teacher_id, usr_id, title, create_time) values (#{0}, #{1}, #{2}, #{3})")
    void insertTeacher(@Param("0") String teacher_id, @Param("1") String usr_id, @Param("2") String title, @Param("3") String create_time);

    @Delete("delete from teacher where teacher_id = #{0}")
    void deleteTeacher(@Param("0") String teacher_id);

    @Update("update teacher set title = #{1} where teacher_id = #{0}")
    void updateTeacher(@Param("0") String teacher_id, @Param("1") String title);

    @Select("select * from teacher where teacher_id = #{0}")
    Teacher getTeacherById(@Param("0") String teacher_id);

    @Select("select * from teacher where usr_id = #{0}")
    Teacher getTeacherByUsrId(@Param("0") String usr_id);

    @Select("select * from teacher order by create_time desc limit #{limit} offset #{offset}")
    List<Teacher> getTeacherList(@Param("offset") int offset, @Param("limit") int limit);
}