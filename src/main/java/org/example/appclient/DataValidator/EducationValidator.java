package org.example.appclient.DataValidator;

import java.util.regex.Pattern;

public class EducationValidator {
    public static boolean schoolNameValidator(String schoolName) {
        return schoolName != null;
    }

    public static boolean fieldValidator(String field) {
        return field != null;
    }

    public static boolean dateValidator(String date) {
        if (date == null) {
            return false;
        }

        String datePattern = "^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";
        return Pattern.matches(datePattern, date);
    }

    public static boolean gradeValidator(String grade) {
        if (grade == null || grade.isEmpty()) {
            return false;
        }

        try {
            double gradeValue = Double.parseDouble(grade);
            return gradeValue >= 0 && gradeValue <= 20;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
