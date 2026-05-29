package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.HomeworkSubmission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HomeworkSubmissionMapper {

    @Insert("insert into homework_submission (submission_id, homework_id, student_id, submit_time, total_score, graded, is_late) values (#{0}, #{1}, #{2}, #{3}, #{4}, #{5}, #{6})")
    void insertSubmission(@Param("0") String submission_id, @Param("1") String homework_id, @Param("2") String student_id, @Param("3") String submit_time, @Param("4") Double total_score, @Param("5") Integer graded, @Param("6") Integer is_late);

    @Select("select hs.*, u.name as student_name from homework_submission hs inner join student s on hs.student_id = s.student_id left join user u on s.usr_id = u.id where hs.homework_id = #{0} order by hs.submit_time asc")
    List<HomeworkSubmission> getSubmissionsByHomeworkId(@Param("0") String homework_id);

    @Select("select * from homework_submission where homework_id = #{0} and student_id = #{1}")
    HomeworkSubmission getSubmissionByHomeworkAndStudent(@Param("0") String homework_id, @Param("1") String student_id);

    @Update("update homework_submission set total_score = #{1}, graded = #{2} where submission_id = #{0}")
    void updateSubmissionScore(@Param("0") String submission_id, @Param("1") Double total_score, @Param("2") Integer graded);

    @Delete("delete from homework_submission where submission_id = #{0}")
    void deleteSubmission(@Param("0") String submission_id);

    @Select("select hs.*, h.title as homework_title, h.class_id from homework_submission hs inner join homework h on hs.homework_id = h.homework_id where hs.student_id = #{0} order by hs.submit_time desc")
    List<HomeworkSubmission> getSubmissionsByStudentId(@Param("0") String student_id);

    @Select("select hs.*, h.title as homework_title, h.class_id from homework_submission hs inner join homework h on hs.homework_id = h.homework_id where hs.student_id = #{0} and h.class_id = #{1} order by hs.submit_time desc")
    List<HomeworkSubmission> getSubmissionsByStudentAndClass(@Param("0") String student_id, @Param("1") String class_id);
}
