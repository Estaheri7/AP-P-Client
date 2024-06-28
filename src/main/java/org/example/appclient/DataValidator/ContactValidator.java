package org.example.appclient.DataValidator;

import java.util.regex.Pattern;

public class ContactValidator {
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(
            "^\\+?[0-9. ()-]{7,15}$"
    );

    public static boolean viewLinkValidator(String viewLink) {
        return viewLink != null && !viewLink.isEmpty();
    }

    public static boolean phoneNumberValidator(String phoneNumber) {
        return phoneNumber != null && PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
    }

    public static boolean dateValidator(String date) {
        return EducationValidator.dateValidator(date);
    }
}
