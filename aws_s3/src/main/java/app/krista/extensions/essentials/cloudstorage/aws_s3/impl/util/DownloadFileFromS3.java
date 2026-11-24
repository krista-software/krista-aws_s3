/*
 * Aws S3 Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.cloudstorage.aws_s3.impl.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadFileFromS3 {

    public static File downloadFile(String fileURL) throws IOException {
        URL url = null;
        HttpURLConnection httpConnection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            url = new URL(fileURL);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            // Check if the response code is HTTP OK (200)
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Get file name from the URL or content-disposition header
                String fileName = getFileNameFromURL(fileURL);
                if (fileName == null || fileName.isEmpty()) {
                    throw new IOException("Unable to determine the file name from the download URL. Please verify the URL is valid and try again.");
                }
                // Get input stream from connection
                inputStream = new BufferedInputStream(httpConnection.getInputStream());
                // Create file to save the downloaded content in /tmp directory
                File downloadedFile = new File("/tmp/" + fileName);
                fileOutputStream = new FileOutputStream(downloadedFile);
                // Buffer to hold the data
                byte[] buffer = new byte[1024];
                int bytesRead;
                // Read data from input stream and write to file
                while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("File downloaded successfully: " + downloadedFile.getAbsolutePath());
                return downloadedFile;
            } else {
                throw new IOException("Unable to download file. Server returned HTTP response code " + httpConnection.getResponseCode() + ". Please verify the download URL is valid and accessible, then try again.");
            }
        } catch (IOException e) {
            System.out.println("Download failed: " + e.getMessage());
            throw e;
        } finally {
            // Close the streams
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    System.out.println("Error closing FileOutputStream: " + e.getMessage());
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.out.println("Error closing InputStream: " + e.getMessage());
                }
            }
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private static String getFileNameFromURL(String fileURL) {
        try {
            // Try to extract file name from response-content-disposition
            Pattern pattern = Pattern.compile("response-content-disposition=attachment(?:%3B|;).*filename%3D([^&]+)");
            Matcher matcher = pattern.matcher(fileURL);
            if (matcher.find()) {
                return URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8.name());
            }
            // If not found, get file name from URL path
            String path = new URL(fileURL).getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        } catch (Exception e) {
            System.out.println("Error extracting file name from URL: " + e.getMessage());
            return null;
        }
    }
}
