package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentMapper {

    @Insert("insert into student (student_id, usr_id, student_no, create_time) values (#{0}, #{1}, #{2}, #{3})")
    void insertStudent(@Param("0") String student_id, @Param("1") String usr_id, @Param("2") String student_no, @Param("3") String create_time);

    @Delete("delete from student where student_id = #{0}")
    void deleteStudent(@Param("0") String student_id);

    @Update("update student set student_no = #{1} where student_id = #{0}")
    void updateStudent(@Param("0") String student_id, @Param("1") String student_no);

    @Select("select * from student where student_id = #{0}")
    Student getStudentById(@Param("0") String student_id);

    @Select("select * from student where usr_id = #{0}")
    Student getStudentByUsrId(@Param("0") String usr_id);

    @Select("select * from student where student_no = #{0}")
    Student getStudentByStudentNo(@Param("0") String student_no);

    @Select("select * from student order by create_time desc limit #{limit} offset #{offset}")
    List<Student> getStudentList(@Param("offset") int offset, @Param("limit") int limit);
}