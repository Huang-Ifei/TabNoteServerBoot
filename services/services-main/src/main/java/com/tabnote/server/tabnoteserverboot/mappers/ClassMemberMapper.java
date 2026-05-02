package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.Student;
import com.tabnote.server.tabnoteserverboot.models.Teacher;
import org.apache.ibatis.annotations.*;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface ClassMemberMapper {

    @Insert("insert into class_teacher (id, class_id, teacher_id, join_time) values (#{0}, #{1}, #{2}, #{3})")
    void addTeacherToClass(@Param("0") String id, @Param("1") String class_id, @Param("2") String teacher_id, @Param("3") String join_time);

    @Delete("delete from class_teacher where class_id = #{0} and teacher_id = #{1}")
    void removeTeacherFromClass(@Param("0") String class_id, @Param("1") String teacher_id);

    @Select("select t.*, u.name as user_name from teacher t inner join class_teacher ct on t.teacher_id = ct.teacher_id left join user u on t.usr_id = u.id where ct.class_id = #{0}")
    List<Teacher> getTeachersByClassId(@Param("0") String class_id);

    @Select("select c.* from class c inner join class_teacher ct on c.class_id = ct.class_id where ct.teacher_id = #{0}")
    List<HashMap<String, String>> getClassesByTeacherId(@Param("0") String teacher_id);

    @Insert("insert into class_student (id, class_id, student_id, join_time, role) values (#{0}, #{1}, #{2}, #{3}, #{4})")
    void addStudentToClass(@Param("0") String id, @Param("1") String class_id, @Param("2") String student_id, @Param("3") String join_time, @Param("4") String role);

    @Delete("delete from class_student where class_id = #{0} and student_id = #{1}")
    void removeStudentFromClass(@Param("0") String class_id, @Param("1") String student_id);

    @Update("update class_student set role = #{2} where class_id = #{0} and student_id = #{1}")
    void updateStudentRole(@Param("0") String class_id, @Param("1") String student_id, @Param("2") String role);

    @Select("select s.*, cs.role, cs.join_time, u.name as user_name from student s inner join class_student cs on s.student_id = cs.student_id left join user u on s.usr_id = u.id where cs.class_id = #{0}")
    List<HashMap<String, String>> getStudentsByClassId(@Param("0") String class_id);

    @Select("select c.*, cs.role, cs.join_time from class c inner join class_student cs on c.class_id = cs.class_id where cs.student_id = #{0}")
    List<HashMap<String, String>> getClassesByStudentId(@Param("0") String student_id);

    @Select("select teacher_id from class_teacher where class_id = #{0}")
    List<String> getTeacherIdsByClassId(@Param("0") String class_id);
}