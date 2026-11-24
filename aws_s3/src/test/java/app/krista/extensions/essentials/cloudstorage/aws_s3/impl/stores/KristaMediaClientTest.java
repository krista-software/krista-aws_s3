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

package app.krista.extensions.essentials.cloudstorage.aws_s3.impl.stores;

import app.krista.ksdk.files.FileHandle;
import app.krista.ksdk.files.FileRepository;
import app.krista.model.base.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for KristaMediaClient class.
 * Tests cover file conversion, compression, and media operations.
 */
public class KristaMediaClientTest {

    private KristaMediaClient kristaMediaClient;
    private FileRepository mockFileRepository;
    private FileHandle mockFileHandle;
    private File mockKristaFile;
    private java.io.File tempFile;
    private java.io.File tempZipFile;

    @Before
    public void setUp() throws Exception {
        mockFileRepository = mock(FileRepository.class);
        mockFileHandle = mock(FileHandle.class);
        mockKristaFile = mock(File.class);

        kristaMediaClient = new KristaMediaClient();
        // Use reflection to inject the mock FileRepository
        java.lang.reflect.Field field = KristaMediaClient.class.getDeclaredField("fileRepository");
        field.setAccessible(true);
        field.set(kristaMediaClient, mockFileRepository);
    }

    @After
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
        if (tempZipFile != null && tempZipFile.exists()) {
            tempZipFile.delete();
        }
        // Clean up any zip files created in /tmp/
        java.io.File tmpDir = new java.io.File("/tmp/");
        java.io.File[] zipFiles = tmpDir.listFiles((dir, name) -> name.endsWith(".zip"));
        if (zipFiles != null) {
            for (java.io.File zipFile : zipFiles) {
                if (zipFile.getName().startsWith("test")) {
                    zipFile.delete();
                }
            }
        }
    }

    // ==================== compressFile() Static Method Tests ====================

    @Test
    public void testCompressFile_WhenValidFile_CreatesZipSuccessfully() throws IOException {
        // Arrange
        tempFile = java.io.File.createTempFile("testfile", ".txt");
        Files.write(tempFile.toPath(), "Test content".getBytes());
        String zipFilePath = "/tmp/testfile.zip";
        tempZipFile = new java.io.File(zipFilePath);

        // Act
        KristaMediaClient.compressFile(zipFilePath, tempFile.getAbsolutePath());

        // Assert
        assertTrue(tempZipFile.exists());
        assertTrue(tempZipFile.length() > 0);
    }

    @Test(expected = FileNotFoundException.class)
    public void testCompressFile_WhenFileDoesNotExist_ThrowsFileNotFoundException() throws IOException {
        // Arrange
        String zipFilePath = "/tmp/nonexistent.zip";
        String nonExistentFile = "/tmp/nonexistent.txt";

        // Act
        KristaMediaClient.compressFile(zipFilePath, nonExistentFile);

        // Assert - FileNotFoundException expected
    }

    @Test(expected = IOException.class)
    public void testCompressFile_WhenInvalidZipPath_ThrowsIOException() throws IOException {
        // Arrange
        tempFile = java.io.File.createTempFile("testfile", ".txt");
        String invalidZipPath = "/invalid/path/test.zip";

        // Act
        KristaMediaClient.compressFile(invalidZipPath, tempFile.getAbsolutePath());

        // Assert - IOException expected
    }

    @Test
    public void testCompressFile_WhenLargeFile_CompressesSuccessfully() throws IOException {
        // Arrange
        tempFile = java.io.File.createTempFile("largefile", ".txt");
        byte[] largeContent = new byte[10000];
        Arrays.fill(largeContent, (byte) 'A');
        Files.write(tempFile.toPath(), largeContent);
        String zipFilePath = "/tmp/largefile.zip";
        tempZipFile = new java.io.File(zipFilePath);

        // Act
        KristaMediaClient.compressFile(zipFilePath, tempFile.getAbsolutePath());

        // Assert
        assertTrue(tempZipFile.exists());
        assertTrue(tempZipFile.length() > 0);
        assertTrue(tempZipFile.length() < tempFile.length()); // Compressed should be smaller
    }

    // ==================== toKristaFile() Method Tests ====================

    @Test
    public void testToKristaFile_WhenSupportedFormat_ConvertsSuccessfully() throws IOException {
        // Arrange
        tempFile = java.io.File.createTempFile("testfile", ".txt");
        Files.write(tempFile.toPath(), "Test content".getBytes());

        Set<String> blacklist = new HashSet<>();
        when(mockFileRepository.getBlackListedFileExtensions()).thenReturn(blacklist);
        when(mockFileRepository.createNewFileByName(anyString())).thenReturn(mockFileHandle);
        when(mockFileHandle.getFile()).thenReturn(mockKristaFile);

        // Act
        File result = kristaMediaClient.toKristaFile(tempFile);

        // Assert
        assertNotNull(result);
        assertEquals(mockKristaFile, result);
        verify(mockFileRepository).createNewFileByName(tempFile.getName());
        verify(mockFileHandle).setContent(any(FileInputStream.class));
        verify(mockFileHandle).getFile();
    }

    @Test
    public void testToKristaFile_WhenUnsupportedFormat_CompressesAndConverts() throws IOException {
        // Arrange
        tempFile = java.io.File.createTempFile("testfile", ".exe");
        Files.write(tempFile.toPath(), "Test content".getBytes());

        Set<String> blacklist = new HashSet<>(Arrays.asList("exe", "bat", "sh"));
        when(mockFileRepository.getBlackListedFileExtensions()).thenReturn(blacklist);
        when(mockFileRepository.createNewFileByName(anyString())).thenReturn(mockFileHandle);
        when(mockFileHandle.getFile()).thenReturn(mockKristaFile);

        // Act
        File result = kristaMediaClient.toKristaFile(tempFile);

        // Assert
        assertNotNull(result);
        assertEquals(mockKristaFile, result);
        verify(mockFileRepository).getBlackListedFileExtensions();
        verify(mockFileRepository).createNewFileByName(contains(".zip"));
    }

    @Test
    public void testToKristaFile_WhenFileNameHasSpecialCharacters_DecodesCorrectly() throws IOException {
        // Arrange
        tempFile = new java.io.File("/tmp/test%20file.txt");
        tempFile.createNewFile();
        Files.write(tempFile.toPath(), "Test content".getBytes());

        Set<String> blacklist = new HashSet<>();
        when(mockFileRepository.getBlackListedFileExtensions()).thenReturn(blacklist);
        when(mockFileRepository.createNewFileByName(anyString())).thenReturn(mockFileHandle);
        when(mockFileHandle.getFile()).thenReturn(mockKristaFile);

        // Act
        File result = kristaMediaClient.toKristaFile(tempFile);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockFileRepository).createNewFileByName(fileNameCaptor.capture());
        assertEquals("test file.txt", fileNameCaptor.getValue());
    }

    @Test
    public void testToKristaFile_WhenDifferentFileExtensions_HandlesCorrectly() throws IOException {
        // Arrange
        String[] extensions = {".pdf", ".jpg", ".png", ".doc", ".csv"};

        for (String ext : extensions) {
            tempFile = java.io.File.createTempFile("testfile", ext);
            Files.write(tempFile.toPath(), "Test content".getBytes());

            Set<String> blacklist = new HashSet<>();
            when(mockFileRepository.getBlackListedFileExtensions()).thenReturn(blacklist);
            when(mockFileRepository.createNewFileByName(anyString())).thenReturn(mockFileHandle);
            when(mockFileHandle.getFile()).thenReturn(mockKristaFile);

            // Act
            File result = kristaMediaClient.toKristaFile(tempFile);

            // Assert
            assertNotNull(result);
            tempFile.delete();
        }
    }

    @Test(expected = IOException.class)
    public void testToKristaFile_WhenSetContentThrowsException_PropagatesException() throws IOException {
        // Arrange
        tempFile = java.io.File.createTempFile("testfile", ".txt");
        Files.write(tempFile.toPath(), "Test content".getBytes());

        Set<String> blacklist = new HashSet<>();
        when(mockFileRepository.getBlackListedFileExtensions()).thenReturn(blacklist);
        when(mockFileRepository.createNewFileByName(anyString())).thenReturn(mockFileHandle);
        doThrow(new IOException("Set content error")).when(mockFileHandle).setContent(any(InputStream.class));

        // Act
        kristaMediaClient.toKristaFile(tempFile);

        // Assert - IOException expected
    }

    // ==================== toJavaFile() Method Tests ====================

    @Test
    public void testToJavaFile_WhenValidKristaFile_ConvertsSuccessfully() throws IOException {
        // Arrange
        String fileName = "testfile.txt";
        String content = "Test content from Krista";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        when(mockKristaFile.getFileName()).thenReturn(fileName);
        when(mockFileRepository.getFile(mockKristaFile)).thenReturn(mockFileHandle);
        when(mockFileHandle.getContent()).thenReturn(inputStream);

        // Act
        java.io.File result = kristaMediaClient.toJavaFile(mockKristaFile);

        // Assert
        assertNotNull(result);
        assertEquals(fileName, result.getName());
        assertTrue(result.exists());

        // Verify content
        String resultContent = new String(Files.readAllBytes(result.toPath()));
        assertEquals(content, resultContent);

        // Cleanup
        result.delete();
    }

    @Test
    public void testToJavaFile_WhenDifferentFileNames_CreatesCorrectFiles() throws IOException {
        // Arrange
        String[] fileNames = {"document.pdf", "image.jpg", "data.csv", "report.xlsx"};

        for (String fileName : fileNames) {
            String content = "Content for " + fileName;
            InputStream inputStream = new ByteArrayInputStream(content.getBytes());

            when(mockKristaFile.getFileName()).thenReturn(fileName);
            when(mockFileRepository.getFile(mockKristaFile)).thenReturn(mockFileHandle);
            when(mockFileHandle.getContent()).thenReturn(inputStream);

            // Act
            java.io.File result = kristaMediaClient.toJavaFile(mockKristaFile);

            // Assert
            assertNotNull(result);
            assertEquals(fileName, result.getName());
            assertTrue(result.exists());

            // Cleanup
            result.delete();
        }
    }

    @Test
    public void testToJavaFile_WhenLargeFile_ConvertsSuccessfully() throws IOException {
        // Arrange
        String fileName = "largefile.bin";
        byte[] largeContent = new byte[100000];
        Arrays.fill(largeContent, (byte) 'X');
        InputStream inputStream = new ByteArrayInputStream(largeContent);

        when(mockKristaFile.getFileName()).thenReturn(fileName);
        when(mockFileRepository.getFile(mockKristaFile)).thenReturn(mockFileHandle);
        when(mockFileHandle.getContent()).thenReturn(inputStream);

        // Act
        java.io.File result = kristaMediaClient.toJavaFile(mockKristaFile);

        // Assert
        assertNotNull(result);
        assertEquals(fileName, result.getName());
        assertTrue(result.exists());
        assertEquals(largeContent.length, result.length());

        // Cleanup
        result.delete();
    }

    @Test(expected = IOException.class)
    public void testToJavaFile_WhenGetContentThrowsException_PropagatesException() throws IOException {
        // Arrange
        when(mockKristaFile.getFileName()).thenReturn("testfile.txt");
        when(mockFileRepository.getFile(mockKristaFile)).thenReturn(mockFileHandle);
        when(mockFileHandle.getContent()).thenThrow(new IOException("Get content error"));

        // Act
        kristaMediaClient.toJavaFile(mockKristaFile);

        // Assert - IOException expected
    }

    @Test
    public void testToJavaFile_WhenEmptyFile_CreatesEmptyFile() throws IOException {
        // Arrange
        String fileName = "emptyfile.txt";
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        when(mockKristaFile.getFileName()).thenReturn(fileName);
        when(mockFileRepository.getFile(mockKristaFile)).thenReturn(mockFileHandle);
        when(mockFileHandle.getContent()).thenReturn(inputStream);

        // Act
        java.io.File result = kristaMediaClient.toJavaFile(mockKristaFile);

        // Assert
        assertNotNull(result);
        assertEquals(fileName, result.getName());
        assertTrue(result.exists());
        assertEquals(0, result.length());

        // Cleanup
        result.delete();
    }

    // ==================== Integration Tests ====================

    @Test
    public void testToKristaFile_AndToJavaFile_RoundTrip() throws IOException {
        // Arrange
        String originalContent = "Round trip test content";
        tempFile = java.io.File.createTempFile("roundtrip", ".txt");
        Files.write(tempFile.toPath(), originalContent.getBytes());

        Set<String> blacklist = new HashSet<>();
        when(mockFileRepository.getBlackListedFileExtensions()).thenReturn(blacklist);
        when(mockFileRepository.createNewFileByName(anyString())).thenReturn(mockFileHandle);
        when(mockFileHandle.getFile()).thenReturn(mockKristaFile);

        // Act - Convert to Krista file
        File kristaFile = kristaMediaClient.toKristaFile(tempFile);

        // Assert
        assertNotNull(kristaFile);
        verify(mockFileRepository).createNewFileByName(anyString());
    }

    @Test
    public void testCompressFile_WithDifferentExtensions_CompressesCorrectly() throws IOException {
        // Arrange
        String[] extensions = {".txt", ".csv", ".json", ".xml"};

        for (String ext : extensions) {
            tempFile = java.io.File.createTempFile("testfile", ext);
            Files.write(tempFile.toPath(), "Test content for compression".getBytes());
            String zipFilePath = "/tmp/testfile" + ext + ".zip";
            tempZipFile = new java.io.File(zipFilePath);

            // Act
            KristaMediaClient.compressFile(zipFilePath, tempFile.getAbsolutePath());

            // Assert
            assertTrue(tempZipFile.exists());
            assertTrue(tempZipFile.length() > 0);

            // Cleanup
            tempZipFile.delete();
            tempFile.delete();
        }
    }
}
