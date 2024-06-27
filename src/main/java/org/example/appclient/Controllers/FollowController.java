package org.example.appclient.Controllers;

import org.example.appclient.util.JwtManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FollowController {
    public static void follow(String followed) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/follow/" + followed);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                connection.setDoOutput(true);

                connection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void unfollow(String followed) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/unfollow/" + followed);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
                connection.setDoOutput(true);
                connection.getResponseCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean hasFollowed(String followed) {
        if (JwtManager.isJwtTokenAvailable()) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://localhost:8080/hasFollowed/" + followed);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());

                return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
