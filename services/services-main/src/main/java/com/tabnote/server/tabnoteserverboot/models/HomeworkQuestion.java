package com.tabnote.server.tabnoteserverboot.models;

public class HomeworkQuestion {

    private String question_id;
    private String homework_id;
    private Integer question_index;
    private String type;
    private String content;
    private String options;
    private String answer;
    private Double score;
    private Integer auto_grading;
    private String test_cases;
    private String create_time;
    private String update_time;

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getHomework_id() {
        return homework_id;
    }

    public void setHomework_id(String homework_id) {
        this.homework_id = homework_id;
    }

    public Integer getQuestion_index() {
        return question_index;
    }

    public void setQuestion_index(Integer question_index) {
        this.question_index = question_index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getAuto_grading() {
        return auto_grading;
    }

    public void setAuto_grading(Integer auto_grading) {
        this.auto_grading = auto_grading;
    }

    public String getTest_cases() {
        return test_cases;
    }

    public void setTest_cases(String test_cases) {
        this.test_cases = test_cases;
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
}
