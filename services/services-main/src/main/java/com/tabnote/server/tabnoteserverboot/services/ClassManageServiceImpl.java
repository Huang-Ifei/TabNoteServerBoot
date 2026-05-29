package com.tabnote.server.tabnoteserverboot.services;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.*;
import com.tabnote.server.tabnoteserverboot.mq.publisher.QuotaDeductionPublisher;
import com.tabnote.server.tabnoteserverboot.models.RankAndQuota;
import com.tabnote.server.tabnoteserverboot.models.SchoolClass;
import com.tabnote.server.tabnoteserverboot.models.Student;
import com.tabnote.server.tabnoteserverboot.models.Teacher;
import com.tabnote.server.tabnoteserverboot.services.inteface.AccountServiceInterface;
import com.tabnote.server.tabnoteserverboot.services.inteface.ClassManageServiceInterface;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ClassManageServiceImpl implements ClassManageServiceInterface {

    private static final Logger log = LoggerFactory.getLogger(ClassManageServiceImpl.class);

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClassMemberMapper classMemberMapper;

    @Autowired
    private VipMapper vipMapper;

    @Autowired
    private QuotaDeductionPublisher quotaDeductionPublisher;

    @Autowired
    private TabNoteInfiniteEncryption tabNoteInfiniteEncryption;

    @Autowired
    private AccountServiceInterface accountService;

    private JSONObject tokenFailed() {
        JSONObject result = new JSONObject();
        result.put("success", false);
        result.put("message", "token_check_failed");
        return result;
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    @Transactional
    public JSONObject createClass(String usr_id, String token, String class_name, String description) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            RankAndQuota rankAndQuota = quotaDeductionPublisher.getQuotaAndRank(usr_id);
            if (rankAndQuota.getRank() < 10) {
                result.put("success", false);
                result.put("message", "等级不足");
                return result;
            }

            Teacher teacher = teacherMapper.getTeacherByUsrId(usr_id);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "您不是教师");
                return result;
            }

            String class_id = Math.abs(usr_id.hashCode()) + "_" + System.currentTimeMillis();
            String currentTime = getCurrentTimestamp();

            classMapper.insertClass(class_id, class_name, description, currentTime, currentTime, 0);

            String relation_id = UUID.randomUUID().toString();
            classMemberMapper.addTeacherToClass(relation_id, class_id, teacher.getTeacher_id(), currentTime);

            result.put("success", true);
            result.put("class_id", class_id);
            result.put("message", "班级创建成功");
            log.info("Class created: class_id={}, teacher_usr_id={}, class_name={}", class_id, usr_id, class_name);
        } catch (Exception e) {
            log.error("Create class failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "班级创建失败: " + e.getMessage());
            throw e;
        }
        return result;
    }

    @Override
    public JSONObject deleteClass(String usr_id, String token, String class_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            SchoolClass schoolClass = classMapper.getClassById(class_id);
            if (schoolClass == null) {
                result.put("success", false);
                result.put("message", "班级不存在");
                return result;
            }

            RankAndQuota rankAndQuota = quotaDeductionPublisher.getQuotaAndRank(usr_id);
            if (rankAndQuota.getRank() < 10) {
                result.put("success", false);
                result.put("message", "等级不足");
                return result;
            }

            classMapper.deleteClass(class_id);
            result.put("success", true);
            result.put("message", "班级删除成功");
            log.info("Class deleted: class_id={}", class_id);
        } catch (Exception e) {
            log.error("Delete class failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "班级删除失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject updateClass(String usr_id, String token, String class_id, String class_name, String description) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            SchoolClass schoolClass = classMapper.getClassById(class_id);
            if (schoolClass == null) {
                result.put("success", false);
                result.put("message", "班级不存在");
                return result;
            }

            String currentTime = getCurrentTimestamp();
            classMapper.updateClass(class_id, class_name, description, currentTime);

            result.put("success", true);
            result.put("message", "班级更新成功");
            log.info("Class updated: class_id={}", class_id);
        } catch (Exception e) {
            log.error("Update class failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "班级更新失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getClassDetail(String usr_id, String token, String class_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            SchoolClass schoolClass = classMapper.getClassById(class_id);
            if (schoolClass == null) {
                result.put("success", false);
                result.put("message", "Class not found");
                return result;
            }

            JSONObject classJson = new JSONObject();
            classJson.put("class_id", schoolClass.getClass_id());
            classJson.put("class_name", schoolClass.getClass_name());
            classJson.put("description", schoolClass.getDescription());
            classJson.put("create_time", schoolClass.getCreate_time());
            classJson.put("update_time", schoolClass.getUpdate_time());
            classJson.put("display", schoolClass.getDisplay());

            result.put("success", true);
            result.put("class", classJson);
            log.info("Class detail retrieved: class_id={}", class_id);
        } catch (Exception e) {
            log.error("Get class detail failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "班级详情获取失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getClassList(String usr_id, String token, Integer index) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            int offset = index != null ? index : 0;
            int limit = 20;

            List<SchoolClass> classes = classMapper.getClassListByTeacherId(usr_id, offset, limit);

            JSONArray classArray = new JSONArray();
            for (SchoolClass schoolClass : classes) {
                JSONObject classJson = new JSONObject();
                classJson.put("class_id", schoolClass.getClass_id());
                classJson.put("class_name", schoolClass.getClass_name());
                classJson.put("description", schoolClass.getDescription());
                classJson.put("create_time", schoolClass.getCreate_time());
                classArray.add(classJson);
            }

            result.put("success", true);
            result.put("classes", classArray);
            log.info("Class list retrieved: index={}", index);
        } catch (Exception e) {
            log.error("Get class list failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "班级列表获取失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional
    public JSONObject createTeacher(String usr_id, String token, String teacher_usr_id, String title) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            RankAndQuota rankAndQuota = quotaDeductionPublisher.getQuotaAndRank(usr_id);
            if (rankAndQuota.getRank() < 12) {
                result.put("success", false);
                result.put("message", "VIP rank must be >= 12 to create teacher");
                return result;
            }

            Teacher existing = teacherMapper.getTeacherByUsrId(teacher_usr_id);
            if (existing != null) {
                result.put("success", false);
                result.put("message", "Teacher already exists for this user");
                return result;
            }

            String teacher_id = Math.abs(teacher_usr_id.hashCode()) + "_" + System.currentTimeMillis();
            String currentTime = getCurrentTimestamp();

            teacherMapper.insertTeacher(teacher_id, teacher_usr_id, title, currentTime);

            String endTime = vipMapper.selectEndTimeById(teacher_usr_id);
            String baseStart = endTime != null ? endTime : getCurrentTimestamp();
            for (int i = 0; i < 12; i++) {
                vipMapper.addYearlyVip(teacher_usr_id, baseStart, 200000000, 10);
                baseStart = vipMapper.selectEndTimeById(teacher_usr_id);
            }

            result.put("success", true);
            result.put("teacher_id", teacher_id);
            result.put("message", "教师创建成功，包含12年VIP权限");
            log.info("教师创建成功: teacher_id={}, usr_id={}", teacher_id, teacher_usr_id);
        } catch (Exception e) {
            log.error("Create teacher failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "教师创建失败: " + e.getMessage());
            throw e;
        }
        return result;
    }

    @Override
    public JSONObject deleteTeacher(String usr_id, String token, String teacher_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Teacher teacher = teacherMapper.getTeacherById(teacher_id);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "教师不存在或已被删除");
                return result;
            }

            RankAndQuota rankAndQuota = quotaDeductionPublisher.getQuotaAndRank(usr_id);
            if (rankAndQuota.getRank() < 12) {
                result.put("success", false);
                result.put("message", "VIP等级必须大于等于12才能删除教师");
                return result;
            }

            teacherMapper.deleteTeacher(teacher_id);
            result.put("success", true);
            result.put("message", "教师删除成功");
            log.info("教师删除成功: teacher_id={}", teacher_id);
        } catch (Exception e) {
            log.error("Delete teacher failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to delete teacher: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject updateTeacher(String usr_id, String token, String teacher_id, String title) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Teacher teacher = teacherMapper.getTeacherById(teacher_id);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "教师不存在或已被删除");
                return result;
            }

            teacherMapper.updateTeacher(teacher_id, title);
            result.put("success", true);
            result.put("message", "教师更新成功");
            log.info("教师更新成功: teacher_id={}", teacher_id);
        } catch (Exception e) {
            log.error("Update teacher failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "教师更新失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getTeacherDetail(String usr_id, String token, String teacher_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Teacher teacher = teacherMapper.getTeacherById(teacher_id);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "教师不存在或已被删除");
                return result;
            }

            JSONObject teacherJson = new JSONObject();
            teacherJson.put("teacher_id", teacher.getTeacher_id());
            teacherJson.put("usr_id", teacher.getUsr_id());
            teacherJson.put("title", teacher.getTitle());
            teacherJson.put("create_time", teacher.getCreate_time());

            result.put("success", true);
            result.put("teacher", teacherJson);
            log.info("教师详情获取成功: teacher_id={}", teacher_id);
        } catch (Exception e) {
            log.error("Get teacher detail failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "教师详情获取失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getTeacherList(String usr_id, String token, Integer index) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            int offset = index != null ? index : 0;
            int limit = 20;

            List<Teacher> teachers = teacherMapper.getTeacherList(offset, limit);

            JSONArray teacherArray = new JSONArray();
            for (Teacher teacher : teachers) {
                JSONObject teacherJson = new JSONObject();
                teacherJson.put("teacher_id", teacher.getTeacher_id());
                teacherJson.put("usr_id", teacher.getUsr_id());
                teacherJson.put("title", teacher.getTitle());
                teacherJson.put("create_time", teacher.getCreate_time());
                teacherArray.add(teacherJson);
            }

            result.put("success", true);
            result.put("teachers", teacherArray);
            log.info("教师列表获取成功: index={}", index);
        } catch (Exception e) {
            log.error("教师列表获取失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "教师列表获取失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional
    public JSONObject createStudent(String usr_id, String token, String student_usr_id, String student_no) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            RankAndQuota rankAndQuota = quotaDeductionPublisher.getQuotaAndRank(usr_id);
            if (rankAndQuota.getRank() < 10) {
                result.put("success", false);
                result.put("message", "VIP等级必须大于等于10才能创建学生");
                return result;
            }

            Student existing = studentMapper.getStudentByStudentNo(student_no);
            if (existing != null) {
                result.put("success", false);
                result.put("message", "学生已存在: " + student_no);
                return result;
            }

            String student_id = Math.abs(student_usr_id.hashCode()) + "_" + System.currentTimeMillis();
            String currentTime = getCurrentTimestamp();

            studentMapper.insertStudent(student_id, student_usr_id, student_no, currentTime);

            String endTime = vipMapper.selectEndTimeById(student_usr_id);
            String baseStart = endTime != null ? endTime : getCurrentTimestamp();
            for (int i = 0; i < 48; i++) {
                vipMapper.addVip(student_usr_id, baseStart, 11400000, 5);
                baseStart = vipMapper.selectEndTimeById(student_usr_id);
            }

            result.put("success", true);
            result.put("student_id", student_id);
            result.put("message", "学生创建成功，4年VIP已添加");
            log.info("学生创建成功: student_id={}, usr_id={}", student_id, student_usr_id);
        } catch (Exception e) {
            log.error("学生创建失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "学生创建失败: " + e.getMessage());
            throw e;
        }
        return result;
    }

    @Override
    public JSONObject deleteStudent(String usr_id, String token, String student_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Student student = studentMapper.getStudentById(student_id);
            if (student == null) {
                result.put("success", false);
                result.put("message", "学生不存在");
                return result;
            }

            RankAndQuota rankAndQuota = quotaDeductionPublisher.getQuotaAndRank(usr_id);
            if (rankAndQuota.getRank() < 10) {
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            studentMapper.deleteStudent(student_id);
            result.put("success", true);
            result.put("message", "学生删除成功");
            log.info("学生删除成功: student_id={}", student_id);
        } catch (Exception e) {
            log.error("学生删除失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "学生删除失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject updateStudent(String usr_id, String token, String student_id, String student_no) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Student student = studentMapper.getStudentById(student_id);
            if (student == null) {
                result.put("success", false);
                result.put("message", "学生不存在");
                return result;
            }

            studentMapper.updateStudent(student_id, student_no);
            result.put("success", true);
            result.put("message", "学生更新成功");
            log.info("学生更新成功: student_id={}", student_id);
        } catch (Exception e) {
            log.error("学生更新失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "学生更新失败: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getStudentDetail(String usr_id, String token, String student_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Student student = studentMapper.getStudentById(student_id);
            if (student == null) {
                result.put("success", false);
                result.put("message", "Student not found");
                return result;
            }

            JSONObject studentJson = new JSONObject();
            studentJson.put("student_id", student.getStudent_id());
            studentJson.put("usr_id", student.getUsr_id());
            studentJson.put("student_no", student.getStudent_no());
            studentJson.put("create_time", student.getCreate_time());

            result.put("success", true);
            result.put("student", studentJson);
            log.info("Student detail retrieved: student_id={}", student_id);
        } catch (Exception e) {
            log.error("Get student detail failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to get student detail: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getStudentList(String usr_id, String token, Integer index) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            int offset = index != null ? index : 0;
            int limit = 20;

            List<Student> students = studentMapper.getStudentList(offset, limit);

            JSONArray studentArray = new JSONArray();
            for (Student student : students) {
                JSONObject studentJson = new JSONObject();
                studentJson.put("student_id", student.getStudent_id());
                studentJson.put("usr_id", student.getUsr_id());
                studentJson.put("student_no", student.getStudent_no());
                studentJson.put("create_time", student.getCreate_time());
                studentArray.add(studentJson);
            }

            result.put("success", true);
            result.put("students", studentArray);
            log.info("Student list retrieved: index={}", index);
        } catch (Exception e) {
            log.error("Get student list failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to get student list: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject addStudentToClass(String usr_id, String token, String class_id, String student_no, String role) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Teacher teacher = teacherMapper.getTeacherByUsrId(usr_id);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "Teacher not found");
                return result;
            }

            List<String> teacherIds = classMemberMapper.getTeacherIdsByClassId(class_id);
            boolean isTeacher = false;
            for (String tid : teacherIds) {
                if (tid.equals(teacher.getTeacher_id())) {
                    isTeacher = true;
                    break;
                }
            }
            if (!isTeacher) {
                result.put("success", false);
                result.put("message", "Only class teachers can add students");
                return result;
            }

            Student student = studentMapper.getStudentByStudentNo(student_no);
            if (student == null) {
                result.put("success", false);
                result.put("message", "Student not found");
                return result;
            }

            String id = UUID.randomUUID().toString();
            String currentTime = getCurrentTimestamp();
            String studentRole = role != null ? role : "member";

            classMemberMapper.addStudentToClass(id, class_id, student.getStudent_id(), currentTime, studentRole);

            result.put("success", true);
            result.put("message", "Student added to class successfully");
            log.info("Student added to class: class_id={}, student_no={}", class_id, student_no);
        } catch (Exception e) {
            log.error("Add student to class failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to add student to class: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject removeStudentFromClass(String usr_id, String token, String class_id, String student_usr_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Teacher teacher = teacherMapper.getTeacherByUsrId(usr_id);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "Teacher not found");
                return result;
            }

            List<String> teacherIds = classMemberMapper.getTeacherIdsByClassId(class_id);
            boolean isTeacher = false;
            for (String tid : teacherIds) {
                if (tid.equals(teacher.getTeacher_id())) {
                    isTeacher = true;
                    break;
                }
            }
            if (!isTeacher) {
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            Student student = studentMapper.getStudentByUsrId(student_usr_id);
            if (student == null) {
                result.put("success", false);
                result.put("message", "学生不存在");
                return result;
            }

            classMemberMapper.removeStudentFromClass(class_id, student.getStudent_id());

            result.put("success", true);
            result.put("message", "Student removed from class successfully");
            log.info("Student removed from class: class_id={}, student_usr_id={}", class_id, student_usr_id);
        } catch (Exception e) {
            log.error("Remove student from class failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to remove student from class: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject updateStudentRoleInClass(String usr_id, String token, String class_id, String student_usr_id, String role) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Teacher teacher = teacherMapper.getTeacherByUsrId(usr_id);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "教师不存在");
                return result;
            }

            List<String> teacherIds = classMemberMapper.getTeacherIdsByClassId(class_id);
            boolean isTeacher = false;
            for (String tid : teacherIds) {
                if (tid.equals(teacher.getTeacher_id())) {
                    isTeacher = true;
                    break;
                }
            }
            if (!isTeacher) {
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            Student student = studentMapper.getStudentByUsrId(student_usr_id);
            if (student == null) {
                result.put("success", false);
                result.put("message", "学生不存在");
                return result;
            }

            classMemberMapper.updateStudentRole(class_id, student.getStudent_id(), role);

            result.put("success", true);
            result.put("message", "Student role updated successfully");
            log.info("Student role updated: class_id={}, student_usr_id={}, role={}", class_id, student_usr_id, role);
        } catch (Exception e) {
            log.error("Update student role failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to update student role: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getClassStudents(String usr_id, String token, String class_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            List<HashMap<String, String>> students = classMemberMapper.getStudentsByClassId(class_id);

            JSONArray studentArray = new JSONArray();
            for (HashMap<String, String> map : students) {
                JSONObject studentJson = new JSONObject();
                studentJson.putAll(map);
                studentArray.add(studentJson);
            }

            result.put("success", true);
            result.put("students", studentArray);
            log.info("Class students retrieved: class_id={}", class_id);
        } catch (Exception e) {
            log.error("Get class students failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to get class students: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getClassTeachers(String usr_id, String token, String class_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            List<Teacher> teachers = classMemberMapper.getTeachersByClassId(class_id);

            JSONArray teacherArray = new JSONArray();
            for (Teacher teacher : teachers) {
                JSONObject teacherJson = new JSONObject();
                teacherJson.put("teacher_id", teacher.getTeacher_id());
                teacherJson.put("usr_id", teacher.getUsr_id());
                teacherJson.put("title", teacher.getTitle());
                teacherJson.put("user_name", teacher.getUser_name());
                teacherJson.put("create_time", teacher.getCreate_time());
                teacherArray.add(teacherJson);
            }

            result.put("success", true);
            result.put("teachers", teacherArray);
            log.info("Class teachers retrieved: class_id={}", class_id);
        } catch (Exception e) {
            log.error("Get class teachers failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to get class teachers: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getStudentClasses(String usr_id, String token, String student_usr_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Student student = studentMapper.getStudentByUsrId(student_usr_id);
            if (student == null) {
                result.put("success", false);
                result.put("message", "Student not found");
                return result;
            }

            List<HashMap<String, String>> classes = classMemberMapper.getClassesByStudentId(student.getStudent_id());

            JSONArray classArray = new JSONArray();
            for (HashMap<String, String> map : classes) {
                JSONObject classJson = new JSONObject();
                classJson.putAll(map);
                classArray.add(classJson);
            }

            result.put("success", true);
            result.put("classes", classArray);
            log.info("Student classes retrieved: student_usr_id={}", student_usr_id);
        } catch (Exception e) {
            log.error("Get student classes failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to get student classes: " + e.getMessage());
        }
        return result;
    }

    @Override
    public JSONObject getTeacherClasses(String usr_id, String token, String teacher_usr_id) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Teacher teacher = teacherMapper.getTeacherByUsrId(teacher_usr_id);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "Teacher not found");
                return result;
            }

            List<HashMap<String, String>> classes = classMemberMapper.getClassesByTeacherId(teacher.getTeacher_id());

            JSONArray classArray = new JSONArray();
            for (HashMap<String, String> map : classes) {
                JSONObject classJson = new JSONObject();
                classJson.putAll(map);
                classArray.add(classJson);
            }

            result.put("success", true);
            result.put("classes", classArray);
            log.info("Teacher classes retrieved: teacher_usr_id={}", teacher_usr_id);
        } catch (Exception e) {
            log.error("Get teacher classes failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to get teacher classes: " + e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional
    public JSONObject batchCreateStudents(String usr_id, String token, byte[] excelBytes) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            RankAndQuota rankAndQuota = quotaDeductionPublisher.getQuotaAndRank(usr_id);
            if (rankAndQuota.getRank() < 10) {
                result.put("success", false);
                result.put("message", "等级不足");
                return result;
            }

            Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(excelBytes));
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                workbook.close();
                result.put("success", false);
                result.put("message", "Excel文件为空或没有数据行");
                return result;
            }

            int successCount = 0;
            int failCount = 0;
            JSONArray failedRows = new JSONArray();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String student_no = getCellStringValue(row.getCell(0));
                String student_usr_id = getCellStringValue(row.getCell(1));

                if (student_no == null || student_no.isBlank()) {
                    JSONObject failRow = new JSONObject();
                    failRow.put("row", i + 1);
                    failRow.put("reason", "学号不能为空");
                    failedRows.add(failRow);
                    failCount++;
                    continue;
                }
                if (student_usr_id == null || student_usr_id.isBlank()) {
                    JSONObject failRow = new JSONObject();
                    failRow.put("row", i + 1);
                    failRow.put("student_no", student_no);
                    failRow.put("reason", "usr_id不能为空");
                    failedRows.add(failRow);
                    failCount++;
                    continue;
                }

                try {
                    Student existing = studentMapper.getStudentByStudentNo(student_no);
                    if (existing != null) {
                        JSONObject failRow = new JSONObject();
                        failRow.put("row", i + 1);
                        failRow.put("student_no", student_no);
                        failRow.put("reason", "学生已存在");
                        failedRows.add(failRow);
                        failCount++;
                        continue;
                    }

                    String student_id = Math.abs(student_usr_id.hashCode()) + "_" + System.currentTimeMillis() + "_" + i;
                    String currentTime = getCurrentTimestamp();

                    studentMapper.insertStudent(student_id, student_usr_id, student_no, currentTime);

                    String endTime = vipMapper.selectEndTimeById(student_usr_id);
                    String baseStart = endTime != null ? endTime : getCurrentTimestamp();
                    for (int j = 0; j < 48; j++) {
                        vipMapper.addVip(student_usr_id, baseStart, 11400000, 5);
                        baseStart = vipMapper.selectEndTimeById(student_usr_id);
                    }

                    successCount++;
                } catch (Exception e) {
                    JSONObject failRow = new JSONObject();
                    failRow.put("row", i + 1);
                    failRow.put("student_no", student_no);
                    failRow.put("reason", e.getMessage());
                    failedRows.add(failRow);
                    failCount++;
                }
            }

            workbook.close();
            result.put("success", true);
            result.put("total", successCount + failCount);
            result.put("success_count", successCount);
            result.put("fail_count", failCount);
            result.put("failed_rows", failedRows);
            result.put("message", "Batch create completed: " + successCount + " success, " + failCount + " failed");
            log.info("Batch create students: success={}, fail={}, operator={}", successCount, failCount, usr_id);
        } catch (IOException e) {
            log.error("Read excel failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to read Excel file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Batch create students failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to batch create students: " + e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional
    public JSONObject batchAddStudentsToClass(String usr_id, String token, String class_id, byte[] excelBytes) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            Teacher teacher = teacherMapper.getTeacherByUsrId(usr_id);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "Only teachers can add students to class");
                return result;
            }

            List<String> teacherIds = classMemberMapper.getTeacherIdsByClassId(class_id);
            boolean isTeacher = false;
            for (String tid : teacherIds) {
                if (tid.equals(teacher.getTeacher_id())) {
                    isTeacher = true;
                    break;
                }
            }
            if (!isTeacher) {
                result.put("success", false);
                result.put("message", "只有班级教师才能添加学生");
                return result;
            }

            Workbook workbook = WorkbookFactory.create(new java.io.ByteArrayInputStream(excelBytes));
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                workbook.close();
                result.put("success", false);
                result.put("message", "Excel文件为空或没有数据行");
                return result;
            }

            int successCount = 0;
            int failCount = 0;
            JSONArray failedRows = new JSONArray();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String student_no = getCellStringValue(row.getCell(0));
                String role = getCellStringValue(row.getCell(1));

                if (student_no == null || student_no.isBlank()) {
                    JSONObject failRow = new JSONObject();
                    failRow.put("row", i + 1);
                    failRow.put("reason", "学号不能为空");
                    failedRows.add(failRow);
                    failCount++;
                    continue;
                }

                try {
                    Student student = studentMapper.getStudentByStudentNo(student_no);
                    if (student == null) {
                        JSONObject failRow = new JSONObject();
                        failRow.put("row", i + 1);
                        failRow.put("student_no", student_no);
                        failRow.put("reason", "学生不存在");
                        failedRows.add(failRow);
                        failCount++;
                        continue;
                    }

                    String studentRole = (role != null && !role.isBlank()) ? role : "member";
                    String id = UUID.randomUUID().toString();
                    String currentTime = getCurrentTimestamp();

                    classMemberMapper.addStudentToClass(id, class_id, student.getStudent_id(), currentTime, studentRole);
                    successCount++;
                } catch (Exception e) {
                    JSONObject failRow = new JSONObject();
                    failRow.put("row", i + 1);
                    failRow.put("student_no", student_no);
                    failRow.put("reason", e.getMessage());
                    failedRows.add(failRow);
                    failCount++;
                }
            }

            workbook.close();
            result.put("success", true);
            result.put("total", successCount + failCount);
            result.put("success_count", successCount);
            result.put("fail_count", failCount);
            result.put("failed_rows", failedRows);
            result.put("message", "Batch add completed: " + successCount + " success, " + failCount + " failed");
            log.info("Batch add students to class: class_id={}, success={}, fail={}, operator={}", class_id, successCount, failCount, usr_id);
        } catch (IOException e) {
            log.error("Read excel failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to read Excel file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Batch add students to class failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to batch add students: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Resource downloadStudentTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("学生信息");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"student_no（学号）", "usr_id（账号）"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("2024001");
            exampleRow.createCell(1).setCellValue("example_usr_001");

            Row exampleRow2 = sheet.createRow(2);
            exampleRow2.createCell(0).setCellValue("2024002");
            exampleRow2.createCell(1).setCellValue("example_usr_002");

            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(1, 6000);

            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            log.error("Generate student template failed: {}", e.getMessage());
            throw new RuntimeException("Failed to generate template", e);
        }
    }

    @Override
    public Resource downloadStudentClassTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("学生-班级信息");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"student_no（学号）", "role（角色，默认member，可选monitor）"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("2024001");
            exampleRow.createCell(1).setCellValue("member");

            Row exampleRow2 = sheet.createRow(2);
            exampleRow2.createCell(0).setCellValue("2024002");
            exampleRow2.createCell(1).setCellValue("monitor");

            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(1, 8000);

            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            log.error("Generate student-class template failed: {}", e.getMessage());
            throw new RuntimeException("Failed to generate template", e);
        }
    }

    @Override
    @Transactional
    public JSONObject batchRegisterStudents(String usr_id, String token, byte[] excelBytes, String address) {
        if (!tabNoteInfiniteEncryption.encryptionTokenCheckIn(usr_id, token)) {
            return tokenFailed();
        }
        JSONObject result = new JSONObject();
        try {
            RankAndQuota rankAndQuota = quotaDeductionPublisher.getQuotaAndRank(usr_id);
            if (rankAndQuota.getRank() < 10) {
                result.put("success", false);
                result.put("message", "VIP rank must be >= 10 to batch register students");
                return result;
            }

            Workbook workbook = WorkbookFactory.create(new java.io.ByteArrayInputStream(excelBytes));
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                workbook.close();
                result.put("success", false);
                result.put("message", "Excel file is empty or has no data rows");
                return result;
            }

            int successCount = 0;
            int failCount = 0;
            JSONArray failedRows = new JSONArray();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String student_no = getCellStringValue(row.getCell(0));
                String name = getCellStringValue(row.getCell(1));

                if (student_no == null || student_no.isBlank()) {
                    JSONObject failRow = new JSONObject();
                    failRow.put("row", i + 1);
                    failRow.put("reason", "学号不能为空");
                    failedRows.add(failRow);
                    failCount++;
                    continue;
                }
                if (name == null || name.isBlank()) {
                    JSONObject failRow = new JSONObject();
                    failRow.put("row", i + 1);
                    failRow.put("student_no", student_no);
                    failRow.put("reason", "姓名不能为空");
                    failedRows.add(failRow);
                    failCount++;
                    continue;
                }

                try {
                    String id = "stu_" + student_no;
                    String encryptedPassword = rsaEncryptWithCurrentPublicKey(student_no);
                    JSONObject signUpResult = accountService.signUp(id, encryptedPassword, name, address);

                    if (!"success".equals(signUpResult.getString("response"))) {
                        JSONObject failRow = new JSONObject();
                        failRow.put("row", i + 1);
                        failRow.put("student_no", student_no);
                        failRow.put("name", name);
                        failRow.put("reason", signUpResult.getString("response"));
                        failedRows.add(failRow);
                        failCount++;
                        continue;
                    }

                    JSONObject createResult = createStudent(usr_id, token, id, student_no);
                    if (!createResult.getBoolean("success")) {
                        JSONObject failRow = new JSONObject();
                        failRow.put("row", i + 1);
                        failRow.put("student_no", student_no);
                        failRow.put("name", name);
                        failRow.put("reason", "注册成功但创建学生信息失败: " + createResult.getString("message"));
                        failedRows.add(failRow);
                        failCount++;
                        continue;
                    }

                    successCount++;
                } catch (Exception e) {
                    JSONObject failRow = new JSONObject();
                    failRow.put("row", i + 1);
                    failRow.put("student_no", student_no);
                    failRow.put("reason", e.getMessage());
                    failedRows.add(failRow);
                    failCount++;
                }
            }

            workbook.close();
            result.put("success", true);
            result.put("total", successCount + failCount);
            result.put("success_count", successCount);
            result.put("fail_count", failCount);
            result.put("failed_rows", failedRows);
            result.put("message", "Batch register completed: " + successCount + " success, " + failCount + " failed");
            log.info("Batch register students: success={}, fail={}, operator={}", successCount, failCount, usr_id);
        } catch (IOException e) {
            log.error("Read excel failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to read Excel file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Batch register students failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to batch register students: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Resource downloadRegisterTemplate() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("批量注册学生");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"student_no（学号）", "name（姓名）"};
            for (int j = 0; j < headers.length; j++) {
                Cell cell = headerRow.createCell(j);
                cell.setCellValue(headers[j]);
                cell.setCellStyle(headerStyle);
            }

            Row noteRow = sheet.createRow(1);
            noteRow.createCell(0).setCellValue("2024001");
            noteRow.createCell(1).setCellValue("张三");

            Row noteRow2 = sheet.createRow(2);
            noteRow2.createCell(0).setCellValue("2024002");
            noteRow2.createCell(1).setCellValue("李四");

            sheet.setColumnWidth(0, 8000);
            sheet.setColumnWidth(1, 6000);

            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            log.error("Generate register template failed: {}", e.getMessage());
            throw new RuntimeException("Failed to generate template", e);
        }
    }

    private String rsaEncryptWithCurrentPublicKey(String plainText) {
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(tabNoteInfiniteEncryption.getPublicKey())));
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("RSA encrypt failed: " + e.getMessage(), e);
        }
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }
}