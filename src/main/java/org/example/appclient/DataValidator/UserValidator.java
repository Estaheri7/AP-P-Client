package org.example.appclient.DataValidator;

public class UserValidator {
    public static boolean nameValidator(String name) {
        return name != null && name.matches("^[a-zA-Z]+$");
    }

    public static boolean emailValidator(String email) {
        return email != null && email.matches("^\\w+([-+.']\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
    }

    public static boolean passwordValidator(String password) {
        return password != null && password.matches("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$") && password.length() >= 8;
    }
}