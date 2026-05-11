package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.*;
import com.tabnote.server.tabnoteserverboot.models.Homework;
import com.tabnote.server.tabnoteserverboot.models.HomeworkAnswer;
import com.tabnote.server.tabnoteserverboot.models.HomeworkQuestion;
import com.tabnote.server.tabnoteserverboot.models.HomeworkSubmission;
import com.tabnote.server.tabnoteserverboot.models.Student;
import com.tabnote.server.tabnoteserverboot.models.Teacher;
import com.tabnote.server.tabnoteserverboot.services.inteface.HomeworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Service
public class HomeworkServiceImpl implements HomeworkService {

    private static final Logger log = LoggerFactory.getLogger(HomeworkServiceImpl.class);

    @Autowired
    private HomeworkMapper homeworkMapper;

    @Autowired
    private HomeworkQuestionMapper homeworkQuestionMapper;

    @Autowired
    private HomeworkSubmissionMapper homeworkSubmissionMapper;

    @Autowired
    private HomeworkAnswerMapper homeworkAnswerMapper;

    @Autowired
    private ClassMemberMapper classMemberMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TabNoteInfiniteEncryption tabNoteInfiniteEncryption;

    private JSONObject tokenFailed() {
        JSONObject result = new JSONObject();
        result.put("success", false);
        result.put("message", "token_check_failed");
        return result;
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private boolean isClassTeacher(String usr_id, String class_id) {
        Teacher teacher = teacherMapper.getTeacherByUsrId(usr_id);
        if (teacher == null) return false;
        List<String> teacherIds = classMemberMapper.getTeacherIdsByClassId(class_id);
        for (String tid : teacherIds) {
            if (tid.equals(teacher.getTeacher_id())) {
                return true;
            }
        }
        return false;
    }

    private boolean isClassStudent(String usr_id, String class_id) {
        Student student = studentMapper.getStudentByUsrId(usr_id);
        if (student == null) return false;
        List<HashMap<String, String>> classes = classMemberMapper.getClassesByStudentId(student.getStudent_id());
        for (HashMap<String, String> map : classes) {
            if (class_id.equals(map.get("class_id"))) {
                return true;
            }
        }
        return false;
    }

    // ==================== 教师端 ====================

    @Override
    @Transactional
    public JSONObject createHomework(String usr_id, String token, String class_id, String title, String description, String deadline, String questions) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            if (!isClassTeacher(usr_id, class_id)) {
                result.put("success", false);
                result.put("message", "只有班级教师才能创建作业");
                return result;
            }

            String homework_id = Math.abs(usr_id.hashCode()) + "_" + System.currentTimeMillis();
            String currentTime = getCurrentTimestamp();
            JSONArray questionArray = JSONArray.parseArray(questions);

            homeworkMapper.insertHomework(homework_id, class_id, title, description, deadline, 0.0, currentTime, currentTime, 0);

            double totalScore = 0;
            for (int i = 0; i < questionArray.size(); i++) {
                JSONObject q = questionArray.getJSONObject(i);
                String question_id = Math.abs(usr_id.hashCode()) + "_" + System.currentTimeMillis() + "_" + i;
                Integer question_index = q.getInteger("question_index") != null ? q.getInteger("question_index") : (i + 1);
                String type = q.getString("type");
                String content = q.getString("content");
                String options = q.getString("options");
                String answer = q.getString("answer");
                Double score = q.getDouble("score") != null ? q.getDouble("score") : 0.0;
                Integer auto_grading = q.getInteger("auto_grading") != null ? q.getInteger("auto_grading") : 0;
                String test_cases = q.getString("test_cases");

                homeworkQuestionMapper.insertQuestion(question_id, homework_id, question_index, type, content, options, answer, score, auto_grading, test_cases, currentTime, currentTime);
                totalScore += score;
            }

            homeworkMapper.updateHomeworkTotalScore(homework_id, totalScore, currentTime);

            result.put("success", true);
            result.put("homework_id", homework_id);
            result.put("message", "作业创建成功");
            log.info("Homework created: homework_id={}, class_id={}, title={}", homework_id, class_id, title);
        } catch (Exception e) {
            log.error("Create homework failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "作业创建失败: " + e.getMessage());
            throw e;
        }
        return result;
    }

    @Override
    @Transactional
    public JSONObject deleteHomework(String usr_id, String token, String homework_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Homework homework = homeworkMapper.getHomeworkById(homework_id);
            if (homework == null) {
                result.put("success", false);
                result.put("message", "作业不存在");
                return result;
            }

            if (!isClassTeacher(usr_id, homework.getClass_id())) {
                result.put("success", false);
                result.put("message", "只有班级教师才能删除作业");
                return result;
            }

            homeworkQuestionMapper.deleteQuestionsByHomeworkId(homework_id);
            homeworkMapper.deleteHomework(homework_id);

            result.put("success", true);
            result.put("message", "作业删除成功");
            log.info("Homework deleted: homework_id={}", homework_id);
        } catch (Exception e) {
            log.error("Delete homework failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "作业删除失败: " + e.getMessage());
            throw e;
        }
        return result;
    }

    @Override
    public JSONObject updateHomework(String usr_id, String token, String homework_id, String title, String description, String deadline) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Homework homework = homeworkMapper.getHomeworkById(homework_id);
            if (homework == null) {
                result.put("success", false);
                result.put("message", "作业不存在");
                return result;
            }

            if (!isClassTeacher(usr_id, homework.getClass_id())) {
                result.put("success", false);
                result.put("message", "只有班级教师才能修改作业");
                return result;
            }

            String currentTime = getCurrentTimestamp();
            homeworkMapper.updateHomework(homework_id, title, description, deadline, currentTime);

            result.put("success", true);
            result.put("message", "作业更新成功");
            log.info("Homework updated: homework_id={}", homework_id);
        } catch (Exception e) {
            log.error("Update homework failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "作业更新失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getHomeworkDetail(String usr_id, String token, String homework_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Homework homework = homeworkMapper.getHomeworkById(homework_id);
            if (homework == null) {
                result.put("success", false);
                result.put("message", "作业不存在");
                return result;
            }

            JSONObject homeworkJson = new JSONObject();
            homeworkJson.put("homework_id", homework.getHomework_id());
            homeworkJson.put("class_id", homework.getClass_id());
            homeworkJson.put("title", homework.getTitle());
            homeworkJson.put("description", homework.getDescription());
            homeworkJson.put("deadline", homework.getDeadline());
            homeworkJson.put("total_score", homework.getTotal_score());
            homeworkJson.put("create_time", homework.getCreate_time());
            homeworkJson.put("update_time", homework.getUpdate_time());

            List<HomeworkQuestion> questions = homeworkQuestionMapper.getQuestionsByHomeworkId(homework_id);
            JSONArray questionArray = new JSONArray();
            for (HomeworkQuestion q : questions) {
                JSONObject qJson = new JSONObject();
                qJson.put("question_id", q.getQuestion_id());
                qJson.put("question_index", q.getQuestion_index());
                qJson.put("type", q.getType());
                qJson.put("content", q.getContent());
                qJson.put("options", q.getOptions());
                qJson.put("answer", q.getAnswer());
                qJson.put("score", q.getScore());
                qJson.put("auto_grading", q.getAuto_grading());
                qJson.put("test_cases", q.getTest_cases());
                questionArray.add(qJson);
            }

            result.put("success", true);
            result.put("homework", homeworkJson);
            result.put("questions", questionArray);
        } catch (Exception e) {
            log.error("Get homework detail failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取作业详情失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getClassHomeworkList(String usr_id, String token, String class_id, Integer index) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            int offset = index != null ? index : 0;
            int limit = 20;

            List<Homework> homeworkList = homeworkMapper.getHomeworkListByClassId(class_id, offset, limit);
            JSONArray homeworkArray = new JSONArray();
            for (Homework h : homeworkList) {
                JSONObject hJson = new JSONObject();
                hJson.put("homework_id", h.getHomework_id());
                hJson.put("class_id", h.getClass_id());
                hJson.put("title", h.getTitle());
                hJson.put("description", h.getDescription());
                hJson.put("deadline", h.getDeadline());
                hJson.put("total_score", h.getTotal_score());
                hJson.put("create_time", h.getCreate_time());
                homeworkArray.add(hJson);
            }

            Integer total = homeworkMapper.getHomeworkCountByClassId(class_id);

            result.put("success", true);
            result.put("homework_list", homeworkArray);
            result.put("total", total);
        } catch (Exception e) {
            log.error("Get class homework list failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取作业列表失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject addQuestion(String usr_id, String token, String homework_id, String type, String content, String options, String answer, Double score, Integer auto_grading, String test_cases) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Homework homework = homeworkMapper.getHomeworkById(homework_id);
            if (homework == null) {
                result.put("success", false);
                result.put("message", "作业不存在");
                return result;
            }

            if (!isClassTeacher(usr_id, homework.getClass_id())) {
                result.put("success", false);
                result.put("message", "只有班级教师才能添加题目");
                return result;
            }

            String question_id = Math.abs(usr_id.hashCode()) + "_" + System.currentTimeMillis();
            String currentTime = getCurrentTimestamp();

            List<HomeworkQuestion> existingQuestions = homeworkQuestionMapper.getQuestionsByHomeworkId(homework_id);
            int nextIndex = existingQuestions.size() + 1;
            Double actualScore = score != null ? score : 0.0;
            Integer actualAutoGrading = auto_grading != null ? auto_grading : 0;

            homeworkQuestionMapper.insertQuestion(question_id, homework_id, nextIndex, type, content, options, answer, actualScore, actualAutoGrading, test_cases, currentTime, currentTime);

            Double newTotal = homeworkQuestionMapper.getTotalScoreByHomeworkId(homework_id);
            homeworkMapper.updateHomeworkTotalScore(homework_id, newTotal, currentTime);

            result.put("success", true);
            result.put("question_id", question_id);
            result.put("message", "题目添加成功");
        } catch (Exception e) {
            log.error("Add question failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "题目添加失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject updateQuestion(String usr_id, String token, String question_id, Integer question_index, String type, String content, String options, String answer, Double score, Integer auto_grading, String test_cases) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            HomeworkQuestion question = homeworkQuestionMapper.getQuestionById(question_id);
            if (question == null) {
                result.put("success", false);
                result.put("message", "题目不存在");
                return result;
            }

            Homework homework = homeworkMapper.getHomeworkById(question.getHomework_id());
            if (homework == null || !isClassTeacher(usr_id, homework.getClass_id())) {
                result.put("success", false);
                result.put("message", "只有班级教师才能修改题目");
                return result;
            }

            String currentTime = getCurrentTimestamp();
            homeworkQuestionMapper.updateQuestion(question_id, question_index, type, content, options, answer, score, auto_grading, test_cases, currentTime);

            Double newTotal = homeworkQuestionMapper.getTotalScoreByHomeworkId(question.getHomework_id());
            homeworkMapper.updateHomeworkTotalScore(question.getHomework_id(), newTotal, currentTime);

            result.put("success", true);
            result.put("message", "题目更新成功");
        } catch (Exception e) {
            log.error("Update question failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "题目更新失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject deleteQuestion(String usr_id, String token, String question_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            HomeworkQuestion question = homeworkQuestionMapper.getQuestionById(question_id);
            if (question == null) {
                result.put("success", false);
                result.put("message", "题目不存在");
                return result;
            }

            Homework homework = homeworkMapper.getHomeworkById(question.getHomework_id());
            if (homework == null || !isClassTeacher(usr_id, homework.getClass_id())) {
                result.put("success", false);
                result.put("message", "只有班级教师才能删除题目");
                return result;
            }

            homeworkQuestionMapper.deleteQuestion(question_id);

            Double newTotal = homeworkQuestionMapper.getTotalScoreByHomeworkId(question.getHomework_id());
            String currentTime = getCurrentTimestamp();
            homeworkMapper.updateHomeworkTotalScore(question.getHomework_id(), newTotal, currentTime);

            result.put("success", true);
            result.put("message", "题目删除成功");
        } catch (Exception e) {
            log.error("Delete question failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "题目删除失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getSubmissions(String usr_id, String token, String homework_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Homework homework = homeworkMapper.getHomeworkById(homework_id);
            if (homework == null) {
                result.put("success", false);
                result.put("message", "作业不存在");
                return result;
            }

            if (!isClassTeacher(usr_id, homework.getClass_id())) {
                result.put("success", false);
                result.put("message", "只有班级教师才能查看提交");
                return result;
            }

            List<HomeworkSubmission> submissions = homeworkSubmissionMapper.getSubmissionsByHomeworkId(homework_id);
            JSONArray submissionArray = new JSONArray();
            for (HomeworkSubmission s : submissions) {
                JSONObject sJson = new JSONObject();
                sJson.put("submission_id", s.getSubmission_id());
                sJson.put("student_id", s.getStudent_id());
                sJson.put("student_name", s.getStudent_name());
                sJson.put("submit_time", s.getSubmit_time());
                sJson.put("total_score", s.getTotal_score());
                sJson.put("graded", s.getGraded());
                sJson.put("is_late", s.getIs_late());
                submissionArray.add(sJson);
            }

            result.put("success", true);
            result.put("submissions", submissionArray);
        } catch (Exception e) {
            log.error("Get submissions failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取提交列表失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional
    public JSONObject gradeSubmission(String usr_id, String token, String submission_id, String scores) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            List<HomeworkAnswer> answers = homeworkAnswerMapper.getAnswersBySubmissionId(submission_id);
            if (answers.isEmpty()) {
                result.put("success", false);
                result.put("message", "提交不存在");
                return result;
            }

            HomeworkQuestion firstQuestion = homeworkQuestionMapper.getQuestionById(answers.get(0).getQuestion_id());
            if (firstQuestion == null) {
                result.put("success", false);
                result.put("message", "题目信息异常");
                return result;
            }

            Homework homework = homeworkMapper.getHomeworkById(firstQuestion.getHomework_id());
            if (homework == null || !isClassTeacher(usr_id, homework.getClass_id())) {
                result.put("success", false);
                result.put("message", "只有班级教师才能评分");
                return result;
            }

            JSONArray scoreArray = JSONArray.parseArray(scores);
            String currentTime = getCurrentTimestamp();
            double totalScore = 0;
            int gradedCount = 0;

            for (int i = 0; i < scoreArray.size(); i++) {
                JSONObject scoreObj = scoreArray.getJSONObject(i);
                String question_id = scoreObj.getString("question_id");
                Double score = scoreObj.getDouble("score");

                for (HomeworkAnswer answer : answers) {
                    if (answer.getQuestion_id().equals(question_id)) {
                        Integer isCorrect = null;
                        HomeworkQuestion question = homeworkQuestionMapper.getQuestionById(question_id);
                        if (question != null && score != null) {
                            if (score.equals(question.getScore())) {
                                isCorrect = 1;
                            } else if (score == 0) {
                                isCorrect = 0;
                            }
                        }
                        homeworkAnswerMapper.updateAnswerScore(score, isCorrect, currentTime, submission_id, question_id);
                        if (score != null) {
                            totalScore += score;
                            gradedCount++;
                        }
                        break;
                    }
                }
            }

            int graded = gradedCount >= answers.size() ? 1 : 0;
            homeworkSubmissionMapper.updateSubmissionScore(submission_id, totalScore, graded);

            result.put("success", true);
            result.put("total_score", totalScore);
            result.put("graded", graded);
            result.put("message", "评分成功");
            log.info("Submission graded: submission_id={}, total_score={}", submission_id, totalScore);
        } catch (Exception e) {
            log.error("Grade submission failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "评分失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getStudentAnswer(String usr_id, String token, String submission_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            List<HomeworkAnswer> answers = homeworkAnswerMapper.getAnswersBySubmissionId(submission_id);
            if (answers.isEmpty()) {
                result.put("success", false);
                result.put("message", "作答记录不存在");
                return result;
            }

            JSONArray answerArray = new JSONArray();
            for (HomeworkAnswer a : answers) {
                JSONObject aJson = new JSONObject();
                aJson.put("answer_id", a.getAnswer_id());
                aJson.put("question_id", a.getQuestion_id());
                aJson.put("question_index", a.getQuestion_index());
                aJson.put("question_type", a.getQuestion_type());
                aJson.put("question_content", a.getQuestion_content());
                aJson.put("question_options", a.getQuestion_options());
                aJson.put("question_answer", a.getQuestion_answer());
                aJson.put("question_score", a.getQuestion_score());
                aJson.put("student_answer", a.getStudent_answer());
                aJson.put("score", a.getScore());
                aJson.put("is_correct", a.getIs_correct());
                answerArray.add(aJson);
            }

            result.put("success", true);
            result.put("answers", answerArray);
        } catch (Exception e) {
            log.error("Get student answer failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取学生作答失败: " + e.getMessage());
        }
        return result;
    }

    // ==================== 学生端 ====================

    @Override
    @Transactional
    public JSONObject submitHomework(String usr_id, String token, String homework_id, String answers) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Homework homework = homeworkMapper.getHomeworkById(homework_id);
            if (homework == null) {
                result.put("success", false);
                result.put("message", "作业不存在");
                return result;
            }

            if (!isClassStudent(usr_id, homework.getClass_id())) {
                result.put("success", false);
                result.put("message", "只有班级学生才能提交作业");
                return result;
            }

            Student student = studentMapper.getStudentByUsrId(usr_id);
            if (student == null) {
                result.put("success", false);
                result.put("message", "学生信息不存在");
                return result;
            }

            HomeworkSubmission existing = homeworkSubmissionMapper.getSubmissionByHomeworkAndStudent(homework_id, student.getStudent_id());
            if (existing != null) {
                result.put("success", false);
                result.put("message", "您已提交过该作业");
                return result;
            }

            String currentTime = getCurrentTimestamp();
            String deadline = homework.getDeadline();
            int isLate = 0;
            if (deadline != null && !deadline.isEmpty()) {
                try {
                    LocalDateTime deadlineTime = LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    if (LocalDateTime.now().isAfter(deadlineTime)) {
                        isLate = 1;
                    }
                } catch (Exception ignored) {
                }
            }

            String submission_id = Math.abs(usr_id.hashCode()) + "_sub_" + System.currentTimeMillis();
            homeworkSubmissionMapper.insertSubmission(submission_id, homework_id, student.getStudent_id(), currentTime, 0.0, 0, isLate);

            JSONArray answerArray = JSONArray.parseArray(answers);
            double autoScoreTotal = 0;
            int autoGradedCount = 0;

            for (int i = 0; i < answerArray.size(); i++) {
                JSONObject aObj = answerArray.getJSONObject(i);
                String question_id = aObj.getString("question_id");
                String student_answer = aObj.getString("student_answer");

                String answer_id = Math.abs(usr_id.hashCode()) + "_ans_" + System.currentTimeMillis() + "_" + i;
                Double score = null;
                Integer isCorrect = null;

                HomeworkQuestion question = homeworkQuestionMapper.getQuestionById(question_id);
                if (question != null && ("single_choice".equals(question.getType()) || "multiple_choice".equals(question.getType()))) {
                    if (question.getAnswer() != null) {
                        String correctAnswer = question.getAnswer().trim();
                        if (correctAnswer.equals(student_answer)) {
                            score = question.getScore();
                            isCorrect = 1;
                        } else {
                            score = 0.0;
                            isCorrect = 0;
                        }
                    }
                    autoGradedCount++;
                }

                homeworkAnswerMapper.insertAnswer(answer_id, submission_id, question_id, student_answer, score, isCorrect, currentTime, currentTime);
                if (score != null) {
                    autoScoreTotal += score;
                }
            }

            List<HomeworkQuestion> allQuestions = homeworkQuestionMapper.getQuestionsByHomeworkId(homework_id);
            if (autoGradedCount >= allQuestions.size()) {
                homeworkSubmissionMapper.updateSubmissionScore(submission_id, autoScoreTotal, 1);
            } else {
                homeworkSubmissionMapper.updateSubmissionScore(submission_id, autoScoreTotal, 0);
            }

            result.put("success", true);
            result.put("submission_id", submission_id);
            result.put("is_late", isLate);
            result.put("auto_score", autoScoreTotal);
            result.put("message", isLate == 1 ? "作业提交成功（迟交）" : "作业提交成功");
            log.info("Homework submitted: homework_id={}, student_usr_id={}, is_late={}", homework_id, usr_id, isLate);
        } catch (Exception e) {
            log.error("Submit homework failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "作业提交失败: " + e.getMessage());
            throw e;
        }
        return result;
    }

    @Override
    public JSONObject myHomeworkList(String usr_id, String token, String class_id, Integer index) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            int offset = index != null ? index : 0;
            int limit = 20;

            List<Homework> homeworkList = homeworkMapper.getHomeworkListByClassId(class_id, offset, limit);
            JSONArray homeworkArray = new JSONArray();

            Student student = studentMapper.getStudentByUsrId(usr_id);

            for (Homework h : homeworkList) {
                JSONObject hJson = new JSONObject();
                hJson.put("homework_id", h.getHomework_id());
                hJson.put("title", h.getTitle());
                hJson.put("description", h.getDescription());
                hJson.put("deadline", h.getDeadline());
                hJson.put("total_score", h.getTotal_score());
                hJson.put("create_time", h.getCreate_time());

                if (student != null) {
                    HomeworkSubmission submission = homeworkSubmissionMapper.getSubmissionByHomeworkAndStudent(h.getHomework_id(), student.getStudent_id());
                    if (submission != null) {
                        hJson.put("submitted", 1);
                        hJson.put("submission_id", submission.getSubmission_id());
                        hJson.put("my_score", submission.getTotal_score());
                        hJson.put("graded", submission.getGraded());
                        hJson.put("is_late", submission.getIs_late());
                    } else {
                        hJson.put("submitted", 0);
                    }
                } else {
                    hJson.put("submitted", 0);
                }

                homeworkArray.add(hJson);
            }

            Integer total = homeworkMapper.getHomeworkCountByClassId(class_id);

            result.put("success", true);
            result.put("homework_list", homeworkArray);
            result.put("total", total);
        } catch (Exception e) {
            log.error("Get my homework list failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取我的作业列表失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject mySubmission(String usr_id, String token, String homework_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Student student = studentMapper.getStudentByUsrId(usr_id);
            if (student == null) {
                result.put("success", false);
                result.put("message", "学生信息不存在");
                return result;
            }

            HomeworkSubmission submission = homeworkSubmissionMapper.getSubmissionByHomeworkAndStudent(homework_id, student.getStudent_id());
            if (submission == null) {
                result.put("success", false);
                result.put("message", "未提交该作业");
                return result;
            }

            List<HomeworkAnswer> answerList = homeworkAnswerMapper.getAnswersBySubmissionId(submission.getSubmission_id());
            JSONArray answerArray = new JSONArray();
            for (HomeworkAnswer a : answerList) {
                JSONObject aJson = new JSONObject();
                aJson.put("question_id", a.getQuestion_id());
                aJson.put("question_index", a.getQuestion_index());
                aJson.put("question_type", a.getQuestion_type());
                aJson.put("question_content", a.getQuestion_content());
                aJson.put("question_options", a.getQuestion_options());
                aJson.put("student_answer", a.getStudent_answer());
                aJson.put("score", a.getScore());
                aJson.put("is_correct", a.getIs_correct());
                answerArray.add(aJson);
            }

            JSONObject submissionJson = new JSONObject();
            submissionJson.put("submission_id", submission.getSubmission_id());
            submissionJson.put("submit_time", submission.getSubmit_time());
            submissionJson.put("total_score", submission.getTotal_score());
            submissionJson.put("graded", submission.getGraded());
            submissionJson.put("is_late", submission.getIs_late());
            submissionJson.put("answers", answerArray);

            result.put("success", true);
            result.put("submission", submissionJson);
        } catch (Exception e) {
            log.error("Get my submission failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "获取我的提交失败: " + e.getMessage());
        }
        return result;
    }
}
