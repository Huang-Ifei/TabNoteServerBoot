package com.tabnote.server.tabnoteserverboot.mappers;

import com.tabnote.server.tabnoteserverboot.models.HomeworkQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HomeworkQuestionMapper {

    @Insert("insert into homework_question (question_id, homework_id, question_index, type, content, options, answer, score, auto_grading, test_cases, create_time, update_time) values (#{0}, #{1}, #{2}, #{3}, #{4}, #{5}, #{6}, #{7}, #{8}, #{9}, #{10}, #{11})")
    void insertQuestion(@Param("0") String question_id, @Param("1") String homework_id, @Param("2") Integer question_index, @Param("3") String type, @Param("4") String content, @Param("5") String options, @Param("6") String answer, @Param("7") Double score, @Param("8") Integer auto_grading, @Param("9") String test_cases, @Param("10") String create_time, @Param("11") String update_time);

    @Delete("delete from homework_question where question_id = #{0}")
    void deleteQuestion(@Param("0") String question_id);

    @Delete("delete from homework_question where homework_id = #{0}")
    void deleteQuestionsByHomeworkId(@Param("0") String homework_id);

    @Update("update homework_question set question_index = #{1}, type = #{2}, content = #{3}, options = #{4}, answer = #{5}, score = #{6}, auto_grading = #{7}, test_cases = #{8}, update_time = #{9} where question_id = #{0}")
    void updateQuestion(@Param("0") String question_id, @Param("1") Integer question_index, @Param("2") String type, @Param("3") String content, @Param("4") String options, @Param("5") String answer, @Param("6") Double score, @Param("7") Integer auto_grading, @Param("8") String test_cases, @Param("9") String update_time);

    @Select("select * from homework_question where homework_id = #{0} order by question_index asc")
    List<HomeworkQuestion> getQuestionsByHomeworkId(@Param("0") String homework_id);

    @Select("select * from homework_question where question_id = #{0}")
    HomeworkQuestion getQuestionById(@Param("0") String question_id);

    @Select("select * from homework_question where homework_id = #{0} order by question_index asc limit #{limit} offset #{offset}")
    List<HomeworkQuestion> getQuestionsByHomeworkIdWithPage(@Param("0") String homework_id, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select coalesce(sum(score), 0) from homework_question where homework_id = #{0}")
    Double getTotalScoreByHomeworkId(@Param("0") String homework_id);
}
