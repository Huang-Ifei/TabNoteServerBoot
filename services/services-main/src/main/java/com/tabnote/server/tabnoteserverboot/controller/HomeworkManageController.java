package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.services.inteface.HomeworkService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Controller
@RequestMapping("/homeworkManage")
public class HomeworkManageController {

    private static final Logger log = LoggerFactory.getLogger(HomeworkManageController.class);

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private TabNoteInfiniteEncryption tabNoteInfiniteEncryption;

    // ==================== 教师端 — 作业管理 ====================

    @PostMapping("createHomework")
    public ResponseEntity<String> createHomework(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " create_homework");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.createHomework(usr_id, token,
                    jsonObject.getString("class_id"),
                    jsonObject.getString("title"),
                    jsonObject.getString("description"),
                    jsonObject.getString("deadline"),
                    jsonObject.getString("questions")));
        } catch (Exception e) {
            log.error("Create homework error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("deleteHomework")
    public ResponseEntity<String> deleteHomework(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " delete_homework");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.deleteHomework(usr_id, token, jsonObject.getString("homework_id")));
        } catch (Exception e) {
            log.error("Delete homework error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("updateHomework")
    public ResponseEntity<String> updateHomework(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " update_homework");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.updateHomework(usr_id, token,
                    jsonObject.getString("homework_id"),
                    jsonObject.getString("title"),
                    jsonObject.getString("description"),
                    jsonObject.getString("deadline")));
        } catch (Exception e) {
            log.error("Update homework error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("homeworkDetail")
    public ResponseEntity<String> getHomeworkDetail(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " homework_detail");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.getHomeworkDetail(usr_id, token, jsonObject.getString("homework_id")));
        } catch (Exception e) {
            log.error("Get homework detail error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("classHomeworkList")
    public ResponseEntity<String> getClassHomeworkList(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " class_homework_list");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.getClassHomeworkList(usr_id, token,
                    jsonObject.getString("class_id"),
                    jsonObject.getInteger("index")));
        } catch (Exception e) {
            log.error("Get class homework list error: {}", e.getMessage());
            return sendErr();
        }
    }

    // ==================== 教师端 — 题目管理 ====================

    @PostMapping("addQuestion")
    public ResponseEntity<String> addQuestion(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " add_question");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.addQuestion(usr_id, token,
                    jsonObject.getString("homework_id"),
                    jsonObject.getString("type"),
                    jsonObject.getString("content"),
                    jsonObject.getString("options"),
                    jsonObject.getString("answer"),
                    jsonObject.getDouble("score"),
                    jsonObject.getInteger("auto_grading"),
                    jsonObject.getString("test_cases")));
        } catch (Exception e) {
            log.error("Add question error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("updateQuestion")
    public ResponseEntity<String> updateQuestion(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " update_question");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.updateQuestion(usr_id, token,
                    jsonObject.getString("question_id"),
                    jsonObject.getInteger("question_index"),
                    jsonObject.getString("type"),
                    jsonObject.getString("content"),
                    jsonObject.getString("options"),
                    jsonObject.getString("answer"),
                    jsonObject.getDouble("score"),
                    jsonObject.getInteger("auto_grading"),
                    jsonObject.getString("test_cases")));
        } catch (Exception e) {
            log.error("Update question error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("deleteQuestion")
    public ResponseEntity<String> deleteQuestion(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " delete_question");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.deleteQuestion(usr_id, token, jsonObject.getString("question_id")));
        } catch (Exception e) {
            log.error("Delete question error: {}", e.getMessage());
            return sendErr();
        }
    }

    // ==================== 教师端 — 评分 ====================

    @PostMapping("getSubmissions")
    public ResponseEntity<String> getSubmissions(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " get_submissions");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.getSubmissions(usr_id, token, jsonObject.getString("homework_id")));
        } catch (Exception e) {
            log.error("Get submissions error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("gradeSubmission")
    public ResponseEntity<String> gradeSubmission(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " grade_submission");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.gradeSubmission(usr_id, token,
                    jsonObject.getString("submission_id"),
                    jsonObject.getString("scores")));
        } catch (Exception e) {
            log.error("Grade submission error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("getStudentAnswer")
    public ResponseEntity<String> getStudentAnswer(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " get_student_answer");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.getStudentAnswer(usr_id, token, jsonObject.getString("submission_id")));
        } catch (Exception e) {
            log.error("Get student answer error: {}", e.getMessage());
            return sendErr();
        }
    }

    // ==================== 学生端 ====================

    @PostMapping("myHomeworkList")
    public ResponseEntity<String> myHomeworkList(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " my_homework_list");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.myHomeworkList(usr_id, token,
                    jsonObject.getString("class_id"),
                    jsonObject.getInteger("index")));
        } catch (Exception e) {
            log.error("Get my homework list error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("submitHomework")
    public ResponseEntity<String> submitHomework(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " submit_homework");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.submitHomework(usr_id, token,
                    jsonObject.getString("homework_id"),
                    jsonObject.getString("answers")));
        } catch (Exception e) {
            log.error("Submit homework error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("mySubmission")
    public ResponseEntity<String> mySubmission(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " my_submission");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(homeworkService.mySubmission(usr_id, token, jsonObject.getString("homework_id")));
        } catch (Exception e) {
            log.error("Get my submission error: {}", e.getMessage());
            return sendErr();
        }
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
