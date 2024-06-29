package org.example.appclient.Controllers;

import org.example.appclient.util.JwtManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MediaController {
    public static void uploadFile(File file, String url, String unique) throws IOException {
        String fullURL = "http://localhost:8080" + url + "/" + unique;
        HttpURLConnection connection = (HttpURLConnection) new URL(fullURL).openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Authorization", "Bearer " + JwtManager.getJwtToken());
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=---ContentBoundary");

        try (OutputStream outputStream = connection.getOutputStream();
             FileInputStream fileInputStream = new FileInputStream(file)) {

            String boundary = "---ContentBoundary";
            String crlf = "\r\n";
            String twoHyphens = "--";

            // Write the file content
            outputStream.write((twoHyphens + boundary + crlf).getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"" + crlf).getBytes());
            outputStream.write(("Content-Type: " + HttpURLConnection.guessContentTypeFromName(file.getName()) + crlf).getBytes());
            outputStream.write(("Content-Transfer-Encoding: binary" + crlf + crlf).getBytes());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.write((crlf + twoHyphens + boundary + twoHyphens + crlf).getBytes());
            outputStream.flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            System.out.println("Media uploaded successfully.");
        } else {
            System.out.println("Failed to upload image: " + connection.getResponseMessage());
        }
    }

    public static void extractFile(File file, String url) throws IOException {

    }
}
