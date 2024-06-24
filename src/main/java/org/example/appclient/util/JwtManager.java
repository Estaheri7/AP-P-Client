package org.example.appclient.util;

public class JwtManager {
    private static String jwtToken;

    public static void setJwtToken(String token) {
        jwtToken = token;
    }

    public static String getJwtToken() {
        return jwtToken;
    }

    public static boolean isJwtTokenAvailable() {
        return jwtToken != null;
    }
}
