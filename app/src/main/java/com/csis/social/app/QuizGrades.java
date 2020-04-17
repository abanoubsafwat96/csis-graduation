package com.csis.social.app;

import java.io.Serializable;
import java.util.Map;

class QuizGrades implements Serializable {
    public String title;
    public Map<String, String> grades_map;

    public QuizGrades() {
    }
}
