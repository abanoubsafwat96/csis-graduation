package com.csis.social.app.models;

import java.io.Serializable;
import java.util.Map;

public class QuizGrades implements Serializable {
    public String title;
    public Map<String, String> grades_map;

    public QuizGrades() {
    }
}
