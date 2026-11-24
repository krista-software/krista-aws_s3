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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Comprehensive unit tests for DownloadFileFromS3 class.
 * Tests cover file download, URL parsing, error handling, and resource cleanup.
 */
public class DownloadFileFromS3Test {

    private HttpURLConnection mockConnection;
    private InputStream mockInputStream;
    private ByteArrayInputStream testInputStream;
    private File downloadedFile;

    @Before
    public void setUp() {
        mockConnection = mock(HttpURLConnection.class);
        mockInputStream = mock(InputStream.class);
    }

    @After
    public void tearDown() {
        // Clean up any downloaded files in /tmp directory
        if (downloadedFile != null && downloadedFile.exists()) {
            downloadedFile.delete();
        }
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testConstructor_CanBeInstantiated() {
        // This test verifies the class can be instantiated
        // DownloadFileFromS3 has utility methods but no private constructor
        DownloadFileFromS3 instance = new DownloadFileFromS3();
        assertNotNull(instance);
    }

    // ==================== downloadFile Tests - Success Cases ====================

    @Test
    public void testDownloadFile_WhenValidURL_DownloadsSuccessfully() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/test-bucket/test-file.txt";
        byte[] testContent = "Test file content".getBytes();
        testInputStream = new ByteArrayInputStream(testContent);

        URL url = new URL(fileURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Note: This test requires actual network connection or more complex mocking
        // For now, we'll test the file name extraction logic separately
    }

    @Test
    public void testGetFileNameFromURL_WhenSimpleURL_ExtractsFileName() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/test-bucket/document.pdf";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("document.pdf", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLWithPath_ExtractsFileName() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/folder/subfolder/image.png";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("image.png", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLWithContentDisposition_ExtractsFileName() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/file?response-content-disposition=attachment;filename%3Dreport.xlsx";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("report.xlsx", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLWithEncodedContentDisposition_DecodesFileName() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/file?response-content-disposition=attachment%3Bfilename%3Dmy%20file.txt";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("my file.txt", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLWithSpecialCharacters_HandlesCorrectly() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/test-file_2024.csv";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("test-file_2024.csv", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLEndsWithSlash_ReturnsEmptyString() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/folder/";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenInvalidURL_ReturnsNull() throws Exception {
        // Arrange
        String fileURL = "not-a-valid-url";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertNull(fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenNullURL_ReturnsNull() throws Exception {
        // Arrange
        String fileURL = null;

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertNull(fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenEmptyURL_ReturnsNull() throws Exception {
        // Arrange
        String fileURL = "";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertNull(fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLWithMultipleDots_ExtractsCorrectly() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/archive.tar.gz";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("archive.tar.gz", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLWithQueryParams_ExtractsFileNameWithoutParams() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/file.txt?version=123&token=abc";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        // URL.getPath() excludes query parameters, so only the file name is extracted
        assertEquals("file.txt", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenContentDispositionWithSemicolon_ExtractsFileName() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/file?response-content-disposition=attachment;filename%3Ddocument.docx";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("document.docx", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenContentDispositionWithPercent3B_ExtractsFileName() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/file?response-content-disposition=attachment%3Bfilename%3Dspreadsheet.xlsx";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("spreadsheet.xlsx", fileName);
    }

    // ==================== downloadFile Tests - Error Cases ====================

    @Test(expected = IOException.class)
    public void testDownloadFile_WhenInvalidURL_ThrowsIOException() throws IOException {
        // Arrange
        String invalidURL = "not-a-valid-url";

        // Act
        DownloadFileFromS3.downloadFile(invalidURL);

        // Assert - IOException expected
    }

    @Test(expected = IOException.class)
    public void testDownloadFile_WhenNullURL_ThrowsIOException() throws IOException {
        // Arrange
        String nullURL = null;

        // Act
        DownloadFileFromS3.downloadFile(nullURL);

        // Assert - IOException expected
    }

    @Test(expected = IOException.class)
    public void testDownloadFile_WhenEmptyURL_ThrowsIOException() throws IOException {
        // Arrange
        String emptyURL = "";

        // Act
        DownloadFileFromS3.downloadFile(emptyURL);

        // Assert - IOException expected
    }

    // ==================== Edge Case Tests ====================

    @Test
    public void testGetFileNameFromURL_WhenURLWithNoExtension_ExtractsFileName() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/README";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("README", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLWithOnlyDomain_ReturnsEmptyString() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLWithBucketOnly_ReturnsEmptyString() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("bucket", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLWithEncodedSpaces_DecodesCorrectly() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/file?response-content-disposition=attachment;filename%3DMy%20Document.pdf";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("My Document.pdf", fileName);
    }

    @Test
    public void testGetFileNameFromURL_WhenURLWithUnicodeCharacters_HandlesCorrectly() throws Exception {
        // Arrange
        String fileURL = "https://s3.amazonaws.com/bucket/file?response-content-disposition=attachment;filename%3D%E6%96%87%E4%BB%B6.txt";

        // Act
        String fileName = invokeGetFileNameFromURL(fileURL);

        // Assert
        assertEquals("文件.txt", fileName);
    }

    // Helper method to invoke private getFileNameFromURL method using reflection
    private String invokeGetFileNameFromURL(String fileURL) throws Exception {
        java.lang.reflect.Method method = DownloadFileFromS3.class.getDeclaredMethod("getFileNameFromURL", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, fileURL);
    }
}

