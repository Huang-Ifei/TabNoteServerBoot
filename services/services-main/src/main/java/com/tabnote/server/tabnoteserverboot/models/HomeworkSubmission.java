package com.tabnote.server.tabnoteserverboot.models;

public class HomeworkSubmission {

    private String submission_id;
    private String homework_id;
    private String student_id;
    private String submit_time;
    private Double total_score;
    private Integer graded;
    private Integer is_late;
    private String student_name;

    public String getSubmission_id() {
        return submission_id;
    }

    public void setSubmission_id(String submission_id) {
        this.submission_id = submission_id;
    }

    public String getHomework_id() {
        return homework_id;
    }

    public void setHomework_id(String homework_id) {
        this.homework_id = homework_id;
    }

    public String getStudent_id() {
        return student_id;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public String getSubmit_time() {
        return submit_time;
    }

    public void setSubmit_time(String submit_time) {
        this.submit_time = submit_time;
    }

    public Double getTotal_score() {
        return total_score;
    }

    public void setTotal_score(Double total_score) {
        this.total_score = total_score;
    }

    public Integer getGraded() {
        return graded;
    }

    public void setGraded(Integer graded) {
        this.graded = graded;
    }

    public Integer getIs_late() {
        return is_late;
    }

    public void setIs_late(Integer is_late) {
        this.is_late = is_late;
    }

    public String getStudent_name() {
        return student_name;
    }

    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }
}
