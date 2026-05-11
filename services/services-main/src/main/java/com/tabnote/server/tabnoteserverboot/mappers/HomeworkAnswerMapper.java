package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.HomeworkAnswer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HomeworkAnswerMapper {

    @Insert("insert into homework_answer (answer_id, submission_id, question_id, student_answer, score, is_correct, create_time, update_time) values (#{0}, #{1}, #{2}, #{3}, #{4}, #{5}, #{6}, #{7})")
    void insertAnswer(@Param("0") String answer_id, @Param("1") String submission_id, @Param("2") String question_id, @Param("3") String student_answer, @Param("4") Double score, @Param("5") Integer is_correct, @Param("6") String create_time, @Param("7") String update_time);

    @Update("update homework_answer set score = #{0}, is_correct = #{1}, update_time = #{2} where submission_id = #{3} and question_id = #{4}")
    void updateAnswerScore(@Param("0") Double score, @Param("1") Integer is_correct, @Param("2") String update_time, @Param("3") String submission_id, @Param("4") String question_id);

    @Update("update homework_answer set score = #{0}, is_correct = #{1}, update_time = #{2} where answer_id = #{3}")
    void updateAnswerScoreById(@Param("0") Double score, @Param("1") Integer is_correct, @Param("2") String update_time, @Param("3") String answer_id);

    @Select("select ha.*, hq.content as question_content, hq.type as question_type, hq.options as question_options, hq.answer as question_answer, hq.score as question_score, hq.question_index as question_index from homework_answer ha inner join homework_question hq on ha.question_id = hq.question_id where ha.submission_id = #{0} order by hq.question_index asc")
    List<HomeworkAnswer> getAnswersBySubmissionId(@Param("0") String submission_id);

    @Select("select * from homework_answer where submission_id = #{0} and question_id = #{1}")
    HomeworkAnswer getAnswerBySubmissionAndQuestion(@Param("0") String submission_id, @Param("1") String question_id);

    @Delete("delete from homework_answer where submission_id = #{0}")
    void deleteAnswersBySubmissionId(@Param("0") String submission_id);

    @Select("select coalesce(sum(score), 0) from homework_answer where submission_id = #{0}")
    Double getTotalScoreBySubmissionId(@Param("0") String submission_id);

    @Select("select count(*) from homework_answer where submission_id = #{0} and score is not null")
    Integer getGradedCountBySubmissionId(@Param("0") String submission_id);
}
