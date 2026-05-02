package com.tabnote.server.tabnoteserverboot.services.inteface;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.core.io.Resource;

public interface ClassManageServiceInterface {

    JSONObject createClass(String usr_id, String token, String class_name, String description);

    JSONObject deleteClass(String usr_id, String token, String class_id);

    JSONObject updateClass(String usr_id, String token, String class_id, String class_name, String description);

    JSONObject getClassDetail(String usr_id, String token, String class_id);

    JSONObject getClassList(String usr_id, String token, Integer index);

    JSONObject createTeacher(String usr_id, String token, String teacher_usr_id, String title);

    JSONObject deleteTeacher(String usr_id, String token, String teacher_id);

    JSONObject updateTeacher(String usr_id, String token, String teacher_id, String title);

    JSONObject getTeacherDetail(String usr_id, String token, String teacher_id);

    JSONObject getTeacherList(String usr_id, String token, Integer index);

    JSONObject createStudent(String usr_id, String token, String student_usr_id, String student_no);

    JSONObject deleteStudent(String usr_id, String token, String student_id);

    JSONObject updateStudent(String usr_id, String token, String student_id, String student_no);

    JSONObject getStudentDetail(String usr_id, String token, String student_id);

    JSONObject getStudentList(String usr_id, String token, Integer index);

    JSONObject addStudentToClass(String usr_id, String token, String class_id, String student_no, String role);

    JSONObject removeStudentFromClass(String usr_id, String token, String class_id, String student_usr_id);

    JSONObject updateStudentRoleInClass(String usr_id, String token, String class_id, String student_usr_id, String role);

    JSONObject getClassStudents(String usr_id, String token, String class_id);

    JSONObject getClassTeachers(String usr_id, String token, String class_id);

    JSONObject getStudentClasses(String usr_id, String token, String student_usr_id);

    JSONObject getTeacherClasses(String usr_id, String token, String teacher_usr_id);

    JSONObject batchCreateStudents(String usr_id, String token, byte[] excelBytes);

    JSONObject batchAddStudentsToClass(String usr_id, String token, String class_id, byte[] excelBytes);

    Resource downloadStudentTemplate();

    Resource downloadStudentClassTemplate();

    JSONObject batchRegisterStudents(String usr_id, String token, byte[] excelBytes, String address);

    Resource downloadRegisterTemplate();
}