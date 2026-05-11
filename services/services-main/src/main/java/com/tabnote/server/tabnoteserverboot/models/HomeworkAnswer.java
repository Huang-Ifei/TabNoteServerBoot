package com.tabnote.server.tabnoteserverboot.models;

public class HomeworkAnswer {

    private String answer_id;
    private String submission_id;
    private String question_id;
    private String student_answer;
    private Double score;
    private Integer is_correct;
    private String create_time;
    private String update_time;

    private String question_content;
    private String question_type;
    private String question_options;
    private String question_answer;
    private Double question_score;
    private Integer question_index;

    public String getAnswer_id() {
        return answer_id;
    }

    public void setAnswer_id(String answer_id) {
        this.answer_id = answer_id;
    }

    public String getSubmission_id() {
        return submission_id;
    }

    public void setSubmission_id(String submission_id) {
        this.submission_id = submission_id;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getStudent_answer() {
        return student_answer;
    }

    public void setStudent_answer(String student_answer) {
        this.student_answer = student_answer;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getIs_correct() {
        return is_correct;
    }

    public void setIs_correct(Integer is_correct) {
        this.is_correct = is_correct;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getQuestion_content() {
        return question_content;
    }

    public void setQuestion_content(String question_content) {
        this.question_content = question_content;
    }

    public String getQuestion_type() {
        return question_type;
    }

    public void setQuestion_type(String question_type) {
        this.question_type = question_type;
    }

    public String getQuestion_options() {
        return question_options;
    }

    public void setQuestion_options(String question_options) {
        this.question_options = question_options;
    }

    public String getQuestion_answer() {
        return question_answer;
    }

    public void setQuestion_answer(String question_answer) {
        this.question_answer = question_answer;
    }

    public Double getQuestion_score() {
        return question_score;
    }

    public void setQuestion_score(Double question_score) {
        this.question_score = question_score;
    }

    public Integer getQuestion_index() {
        return question_index;
    }

    public void setQuestion_index(Integer question_index) {
        this.question_index = question_index;
    }
}
