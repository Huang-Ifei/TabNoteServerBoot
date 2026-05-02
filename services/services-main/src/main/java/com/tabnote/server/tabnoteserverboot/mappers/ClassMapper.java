package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.SchoolClass;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClassMapper {

    @Insert("insert into class (class_id, class_name, description, create_time, update_time, display) values (#{0}, #{1}, #{2}, #{3}, #{4}, #{5})")
    void insertClass(@Param("0") String class_id, @Param("1") String class_name, @Param("2") String description, @Param("3") String create_time, @Param("4") String update_time, @Param("5") Integer display);

    @Delete("delete from class where class_id = #{0}")
    void deleteClass(@Param("0") String class_id);

    @Update("update class set class_name = #{1}, description = #{2}, update_time = #{3} where class_id = #{0}")
    void updateClass(@Param("0") String class_id, @Param("1") String class_name, @Param("2") String description, @Param("3") String update_time);

    @Select("select * from class where class_id = #{0}")
    SchoolClass getClassById(@Param("0") String class_id);

    @Select("select * from class order by create_time desc limit #{limit} offset #{offset}")
    List<SchoolClass> getClassList(@Param("offset") int offset, @Param("limit") int limit);

    @Select("select * from `class` a inner join `class_teacher` b on a.`class_id` = b.`class_id` inner join teacher c on b.teacher_id = c.teacher_id where c.usr_id = #{0} and a.display = 0 order by a.create_time desc limit #{limit} offset #{offset}")
    List<SchoolClass> getClassListByTeacherId(@Param("0") String teacher_id, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select count(*) from class")
    Integer getClassCount();
}