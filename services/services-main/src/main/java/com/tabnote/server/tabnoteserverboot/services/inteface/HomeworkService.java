package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;

public interface HomeworkService {

    JSONObject createHomework(String usr_id, String token, String class_id, String title, String description, String deadline, String questions);

    JSONObject deleteHomework(String usr_id, String token, String homework_id);

    JSONObject updateHomework(String usr_id, String token, String homework_id, String title, String description, String deadline);

    JSONObject getHomeworkDetail(String usr_id, String token, String homework_id);

    JSONObject getClassHomeworkList(String usr_id, String token, String class_id, Integer index);

    JSONObject addQuestion(String usr_id, String token, String homework_id, String type, String content, String options, String answer, Double score, Integer auto_grading, String test_cases);

    JSONObject updateQuestion(String usr_id, String token, String question_id, Integer question_index, String type, String content, String options, String answer, Double score, Integer auto_grading, String test_cases);

    JSONObject deleteQuestion(String usr_id, String token, String question_id);

    JSONObject getSubmissions(String usr_id, String token, String homework_id);

    JSONObject gradeSubmission(String usr_id, String token, String submission_id, String scores);

    JSONObject getStudentAnswer(String usr_id, String token, String submission_id);

    JSONObject submitHomework(String usr_id, String token, String homework_id, String answers);

    JSONObject myHomeworkList(String usr_id, String token, String class_id, Integer index);

    JSONObject mySubmission(String usr_id, String token, String homework_id);

    JSONObject homeworkStats(String usr_id, String token, String homework_id);

    JSONObject classHomeworkStats(String usr_id, String token, String class_id);

    int learningAnalysis(String usr_id, String token, String class_id, String userRequest, jakarta.servlet.http.HttpServletResponse response, StringBuffer returnString, String cdn_ai_ms, String ai_ms_id) throws Exception;
}
