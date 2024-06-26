package org.example.appclient.util;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class JwtManager {
    private static final Gson gson = new Gson();
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

    public static Object decodeJwtPayload(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT token.");
        }

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        return gson.fromJson(payloadJson, Map.class).get("sub");
    }
}
