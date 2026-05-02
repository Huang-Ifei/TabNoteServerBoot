package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.services.inteface.ClassManageServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin
@Controller
@RequestMapping("/classManage")
public class ClassManageController {

    private static final Logger log = LoggerFactory.getLogger(ClassManageController.class);

    @Autowired
    private ClassManageServiceInterface classManageService;

    @Autowired
    private TabNoteInfiniteEncryption tabNoteInfiniteEncryption;

    // ==================== 班级管理 ====================

    @PostMapping("createClass")
    public ResponseEntity<String> createClass(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " create_class");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.createClass(usr_id, token, jsonObject.getString("class_name"), jsonObject.getString("description")));
        } catch (Exception e) {
            log.error("Create class error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("deleteClass")
    public ResponseEntity<String> deleteClass(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " delete_class");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.deleteClass(usr_id, token, jsonObject.getString("class_id")));
        } catch (Exception e) {
            log.error("Delete class error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("updateClass")
    public ResponseEntity<String> updateClass(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " update_class");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.updateClass(usr_id, token, jsonObject.getString("class_id"), jsonObject.getString("class_name"), jsonObject.getString("description")));
        } catch (Exception e) {
            log.error("Update class error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("classDetail")
    public ResponseEntity<String> getClassDetail(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " class_detail");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.getClassDetail(usr_id, token, jsonObject.getString("class_id")));
        } catch (Exception e) {
            log.error("Get class detail error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("classList")
    public ResponseEntity<String> getClassList(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " class_list");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.getClassList(usr_id, token, jsonObject.getInteger("index")));
        } catch (Exception e) {
            log.error("Get class list error: {}", e.getMessage());
            return sendErr();
        }
    }

    // ==================== 教师管理 ====================

    @PostMapping("createTeacher")
    public ResponseEntity<String> createTeacher(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " create_teacher");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.createTeacher(usr_id, token, jsonObject.getString("teacher_usr_id"), jsonObject.getString("title")));
        } catch (Exception e) {
            log.error("Create teacher error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("deleteTeacher")
    public ResponseEntity<String> deleteTeacher(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " delete_teacher");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.deleteTeacher(usr_id, token, jsonObject.getString("teacher_id")));
        } catch (Exception e) {
            log.error("Delete teacher error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("updateTeacher")
    public ResponseEntity<String> updateTeacher(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " update_teacher");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.updateTeacher(usr_id, token, jsonObject.getString("teacher_id"), jsonObject.getString("title")));
        } catch (Exception e) {
            log.error("Update teacher error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("teacherDetail")
    public ResponseEntity<String> getTeacherDetail(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " teacher_detail");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.getTeacherDetail(usr_id, token, jsonObject.getString("teacher_id")));
        } catch (Exception e) {
            log.error("Get teacher detail error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("teacherList")
    public ResponseEntity<String> getTeacherList(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " teacher_list");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.getTeacherList(usr_id, token, jsonObject.getInteger("index")));
        } catch (Exception e) {
            log.error("Get teacher list error: {}", e.getMessage());
            return sendErr();
        }
    }

    // ==================== 学生管理 ====================

    @PostMapping("createStudent")
    public ResponseEntity<String> createStudent(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " create_student");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.createStudent(usr_id, token, jsonObject.getString("student_usr_id"), jsonObject.getString("student_no")));
        } catch (Exception e) {
            log.error("Create student error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("deleteStudent")
    public ResponseEntity<String> deleteStudent(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " delete_student");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.deleteStudent(usr_id, token, jsonObject.getString("student_id")));
        } catch (Exception e) {
            log.error("Delete student error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("updateStudent")
    public ResponseEntity<String> updateStudent(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " update_student");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.updateStudent(usr_id, token, jsonObject.getString("student_id"), jsonObject.getString("student_no")));
        } catch (Exception e) {
            log.error("Update student error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("studentDetail")
    public ResponseEntity<String> getStudentDetail(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " student_detail");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.getStudentDetail(usr_id, token, jsonObject.getString("student_id")));
        } catch (Exception e) {
            log.error("Get student detail error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("studentList")
    public ResponseEntity<String> getStudentList(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " student_list");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.getStudentList(usr_id, token, jsonObject.getInteger("index")));
        } catch (Exception e) {
            log.error("Get student list error: {}", e.getMessage());
            return sendErr();
        }
    }

    // ==================== 班级成员管理 ====================

    @PostMapping("addStudentToClass")
    public ResponseEntity<String> addStudentToClass(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " add_student_to_class");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.addStudentToClass(usr_id, token, jsonObject.getString("class_id"), jsonObject.getString("student_no"), jsonObject.getString("role")));
        } catch (Exception e) {
            log.error("Add student to class error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("removeStudentFromClass")
    public ResponseEntity<String> removeStudentFromClass(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " remove_student_from_class");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.removeStudentFromClass(usr_id, token, jsonObject.getString("class_id"), jsonObject.getString("student_usr_id")));
        } catch (Exception e) {
            log.error("Remove student from class error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("updateStudentRole")
    public ResponseEntity<String> updateStudentRole(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " update_student_role");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.updateStudentRoleInClass(usr_id, token, jsonObject.getString("class_id"), jsonObject.getString("student_usr_id"), jsonObject.getString("role")));
        } catch (Exception e) {
            log.error("Update student role error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("classStudents")
    public ResponseEntity<String> getClassStudents(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " class_students");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.getClassStudents(usr_id, token, jsonObject.getString("class_id")));
        } catch (Exception e) {
            log.error("Get class students error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("classTeachers")
    public ResponseEntity<String> getClassTeachers(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " class_teachers");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.getClassTeachers(usr_id, token, jsonObject.getString("class_id")));
        } catch (Exception e) {
            log.error("Get class teachers error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("studentClasses")
    public ResponseEntity<String> getStudentClasses(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " student_classes");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.getStudentClasses(usr_id, token, jsonObject.getString("student_usr_id")));
        } catch (Exception e) {
            log.error("Get student classes error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("teacherClasses")
    public ResponseEntity<String> getTeacherClasses(@RequestBody String requestBody, HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " teacher_classes");
        try {
            JSONObject jsonObject = JSONObject.parseObject(requestBody);
            String usr_id = jsonObject.getString("usr_id");
            String token = jsonObject.getString("token");

            return sendMes(classManageService.getTeacherClasses(usr_id, token, jsonObject.getString("teacher_usr_id")));
        } catch (Exception e) {
            log.error("Get teacher classes error: {}", e.getMessage());
            return sendErr();
        }
    }

    // ==================== Excel批量操作 ====================

    @PostMapping("batchCreateStudents")
    public ResponseEntity<String> batchCreateStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam("usr_id") String usr_id,
            @RequestParam("token") String token,
            HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " batch_create_students");
        try {
            if (file.isEmpty()) {
                return sendErr();
            }
            return sendMes(classManageService.batchCreateStudents(usr_id, token, file.getBytes()));
        } catch (Exception e) {
            log.error("Batch create students error: {}", e.getMessage());
            return sendErr();
        }
    }

    @PostMapping("batchAddStudentsToClass")
    public ResponseEntity<String> batchAddStudentsToClass(
            @RequestParam("file") MultipartFile file,
            @RequestParam("usr_id") String usr_id,
            @RequestParam("token") String token,
            @RequestParam("class_id") String class_id,
            HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " batch_add_students_to_class");
        try {
            if (file.isEmpty()) {
                return sendErr();
            }
            return sendMes(classManageService.batchAddStudentsToClass(usr_id, token, class_id, file.getBytes()));
        } catch (Exception e) {
            log.error("Batch add students to class error: {}", e.getMessage());
            return sendErr();
        }
    }

    @GetMapping("downloadStudentTemplate")
    public ResponseEntity<Resource> downloadStudentTemplate(HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " download_student_template");
        try {
            Resource resource = classManageService.downloadStudentTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student_template.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            log.error("Download student template error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("downloadStudentClassTemplate")
    public ResponseEntity<Resource> downloadStudentClassTemplate(HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " download_student_class_template");
        try {
            Resource resource = classManageService.downloadStudentClassTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"student_class_template.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            log.error("Download student-class template error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("batchRegisterStudents")
    public ResponseEntity<String> batchRegisterStudents(
            @RequestParam("file") MultipartFile file,
            @RequestParam("usr_id") String usr_id,
            @RequestParam("token") String token,
            HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " batch_register_students");
        try {
            if (file.isEmpty()) {
                return sendErr();
            }
            String address = tabNoteInfiniteEncryption.proxyGetIp(request);
            return sendMes(classManageService.batchRegisterStudents(usr_id, token, file.getBytes(), address));
        } catch (Exception e) {
            log.error("Batch register students error: {}", e.getMessage());
            return sendErr();
        }
    }

    @GetMapping("downloadRegisterTemplate")
    public ResponseEntity<Resource> downloadRegisterTemplate(HttpServletRequest request) {
        log.info(tabNoteInfiniteEncryption.proxyGetIp(request) + " download_register_template");
        try {
            Resource resource = classManageService.downloadRegisterTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"register_template.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            log.error("Download register template error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}