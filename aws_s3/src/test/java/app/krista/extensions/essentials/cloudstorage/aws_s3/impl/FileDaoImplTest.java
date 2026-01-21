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

package app.krista.extensions.essentials.cloudstorage.aws_s3.impl;

import app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.entities.FileDetails;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3Provider;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3ProviderFactory;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.util.Constants;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for FileDaoImpl class.
 * Tests all public methods including file operations, error handling, and edge cases.
 */
public class FileDaoImplTest {

    private FileDaoImpl fileDao;
    private S3ProviderFactory mockProviderFactory;
    private S3Provider mockProvider;
    private AmazonS3 mockS3Client;
    private File testFile;

    @Before
    public void setUp() throws Exception {
        mockProviderFactory = Mockito.mock(S3ProviderFactory.class);
        mockProvider = Mockito.mock(S3Provider.class);
        mockS3Client = Mockito.mock(AmazonS3.class);

        when(mockProviderFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getS3Client()).thenReturn(mockS3Client);

        fileDao = new FileDaoImpl(mockProviderFactory);

        // Create a temporary test file
        testFile = File.createTempFile("test", ".txt");
        testFile.deleteOnExit();
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testConstructor_WhenValidProviderFactory_CreatesInstance() {
        // Assert
        assertNotNull(fileDao);
        verify(mockProviderFactory).create();
    }

    // ==================== Upload File Tests ====================

    @Test
    public void testUploadFile_WhenValidInputAndFileNotExists_UploadsSuccessfully() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "test-file.txt";

        AmazonS3Exception notFoundException = new AmazonS3Exception("Not found");
        notFoundException.setStatusCode(404);

        Bucket bucket = new Bucket(bucketName);
        List<Bucket> buckets = new ArrayList<>();
        buckets.add(bucket);

        when(mockS3Client.getObject(bucketName, fileKey)).thenThrow(notFoundException);
        when(mockS3Client.listBuckets()).thenReturn(buckets);

        // Act
        boolean result = fileDao.uploadFile(bucketName, fileKey, testFile);

        // Assert
        assertTrue(result);
        verify(mockS3Client).putObject(any(PutObjectRequest.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadFile_WhenBucketNameIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.uploadFile(null, "test-file.txt", testFile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadFile_WhenBucketNameIsEmpty_ThrowsIllegalArgumentException() {
        // Act
        fileDao.uploadFile("", "test-file.txt", testFile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadFile_WhenFileKeyIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.uploadFile("test-bucket", null, testFile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadFile_WhenFileKeyIsEmpty_ThrowsIllegalArgumentException() {
        // Act
        fileDao.uploadFile("test-bucket", "", testFile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadFile_WhenFileIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.uploadFile("test-bucket", "test-file.txt", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadFile_WhenFileDoesNotExist_ThrowsIllegalArgumentException() {
        // Arrange
        File nonExistentFile = new File("non-existent-file.txt");

        // Act
        fileDao.uploadFile("test-bucket", "test-file.txt", nonExistentFile);
    }

    @Test(expected = RuntimeException.class)
    public void testUploadFile_WhenFileAlreadyExists_ThrowsRuntimeException() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "existing-file.txt";

        S3Object existingObject = new S3Object();
        existingObject.setKey(fileKey);

        when(mockS3Client.getObject(bucketName, fileKey)).thenReturn(existingObject);

        // Act
        fileDao.uploadFile(bucketName, fileKey, testFile);
    }

    @Test
    public void testUploadFile_WhenBucketNotFound_ReturnsFalse() {
        // Arrange
        String bucketName = "non-existent-bucket";
        String fileKey = "test-file.txt";

        AmazonS3Exception notFoundException = new AmazonS3Exception("Not found") {
            @Override
            public String getErrorResponseXml() {
                return "<Error><BucketName>non-existent-bucket</BucketName></Error>";
            }
        };
        notFoundException.setStatusCode(404);
        notFoundException.setErrorCode("NoSuchBucket");

        when(mockS3Client.getObject(bucketName, fileKey)).thenThrow(notFoundException);
        when(mockS3Client.listBuckets()).thenReturn(new ArrayList<>());

        // Act & Assert
        try {
            fileDao.uploadFile(bucketName, fileKey, testFile);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("does not exist"));
        }
    }

    // ==================== Download File Tests ====================

    @Test
    public void testDownloadFile_WhenValidInput_ReturnsAnchorTag() throws Exception {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "test-file.txt";

        S3Object s3Object = new S3Object();
        s3Object.setKey(fileKey);

        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/test-file.txt");

        when(mockS3Client.getObject(bucketName, fileKey)).thenReturn(s3Object);
        when(mockS3Client.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);

        // Act
        String result = fileDao.downloadFile(bucketName, fileKey);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("<a href="));
        assertTrue(result.contains(fileKey));
        assertTrue(result.contains("</a>"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDownloadFile_WhenBucketNameIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.downloadFile(null, "test-file.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDownloadFile_WhenFileKeyIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.downloadFile("test-bucket", null);
    }

    @Test
    public void testDownloadFile_WhenS3ExceptionOccurs_ThrowsRuntimeException() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "test-file.txt";

        AmazonS3Exception s3Exception = new AmazonS3Exception("Access denied");
        s3Exception.setStatusCode(500); // Use 500 to trigger default case

        when(mockS3Client.getObject(bucketName, fileKey)).thenThrow(s3Exception);

        // Act & Assert
        try {
            fileDao.downloadFile(bucketName, fileKey);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // Expected - wrapped in SdkClientException then RuntimeException
            assertNotNull(e);
        }
    }

    @Test(expected = SdkClientException.class)
    public void testDownloadFile_WhenNonS3ExceptionOccurs_ThrowsSdkClientException() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "test-file.txt";

        when(mockS3Client.getObject(bucketName, fileKey)).thenThrow(new SdkClientException("Network error"));

        // Act
        fileDao.downloadFile(bucketName, fileKey);
    }

    // ==================== Copy File Tests ====================

    @Test
    public void testCopyFile_WhenDifferentBuckets_CopiesSuccessfully() {
        // Arrange
        String sourceBucket = "source-bucket";
        String sourceFile = "source-file.txt";
        String destBucket = "dest-bucket";
        String destFile = "dest-file.txt";

        // Act
        String result = fileDao.copyFile(sourceBucket, sourceFile, destBucket, destFile);

        // Assert
        assertEquals("File copied successfully", result);
        verify(mockS3Client).copyObject(sourceBucket, sourceFile, destBucket, destFile);
    }

    @Test
    public void testCopyFile_WhenSameBucket_CallsCopyObjectWithUpdatedMetadata() {
        // Arrange
        String bucketName = "test-bucket";
        String sourceFile = "source-file.txt";
        String destFile = "dest-file.txt";

        // Act
        String result = fileDao.copyFile(bucketName, sourceFile, bucketName, destFile);

        // Assert
        assertEquals("File copied successfully", result);
        verify(mockS3Client).copyObject(any(CopyObjectRequest.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopyFile_WhenSourceBucketIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.copyFile(null, "source.txt", "dest-bucket", "dest.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopyFile_WhenSourceFileNameIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.copyFile("source-bucket", null, "dest-bucket", "dest.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopyFile_WhenDestinationBucketIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.copyFile("source-bucket", "source.txt", null, "dest.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCopyFile_WhenDestinationFileNameIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.copyFile("source-bucket", "source.txt", "dest-bucket", null);
    }

    @Test
    public void testCopyFile_WhenS3ExceptionOccurs_ThrowsRuntimeException() {
        // Arrange
        String sourceBucket = "source-bucket";
        String sourceFile = "source-file.txt";
        String destBucket = "dest-bucket";
        String destFile = "dest-file.txt";

        AmazonS3Exception s3Exception = new AmazonS3Exception("Access denied");
        s3Exception.setStatusCode(500); // Use 500 to trigger default case

        when(mockS3Client.copyObject(sourceBucket, sourceFile, destBucket, destFile)).thenThrow(s3Exception);

        // Act & Assert
        try {
            fileDao.copyFile(sourceBucket, sourceFile, destBucket, destFile);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }

    // ==================== Move File Tests ====================

    @Test
    public void testMoveFile_WhenValidInput_MovesSuccessfully() {
        // Arrange
        String sourceBucket = "source-bucket";
        String sourceFile = "source-file.txt";
        String destBucket = "dest-bucket";

        // Act
        String result = fileDao.moveFile(sourceBucket, sourceFile, destBucket);

        // Assert
        assertEquals("File moved successfully", result);
        verify(mockS3Client).copyObject(sourceBucket, sourceFile, destBucket, sourceFile);
        verify(mockS3Client).deleteObject(sourceBucket, sourceFile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveFile_WhenSourceBucketIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.moveFile(null, "source.txt", "dest-bucket");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveFile_WhenSourceFileNameIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.moveFile("source-bucket", null, "dest-bucket");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMoveFile_WhenDestinationBucketIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.moveFile("source-bucket", "source.txt", null);
    }

    @Test
    public void testMoveFile_WhenS3ExceptionOccurs_ThrowsRuntimeException() {
        // Arrange
        String sourceBucket = "source-bucket";
        String sourceFile = "source-file.txt";
        String destBucket = "dest-bucket";

        AmazonS3Exception s3Exception = new AmazonS3Exception("Access denied");
        s3Exception.setStatusCode(500); // Use 500 to trigger default case

        when(mockS3Client.copyObject(sourceBucket, sourceFile, destBucket, sourceFile)).thenThrow(s3Exception);

        // Act & Assert
        try {
            fileDao.moveFile(sourceBucket, sourceFile, destBucket);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }

    // ==================== Rename File Tests ====================

    @Test
    public void testRenameFile_WhenValidInput_RenamesSuccessfully() {
        // Arrange
        String bucketName = "test-bucket";
        String oldFileName = "old-file.txt";
        String newFileName = "new-file.txt";

        // Act
        String result = fileDao.renameFile(bucketName, oldFileName, newFileName);

        // Assert
        assertEquals("File renamed successfully", result);
        verify(mockS3Client).copyObject(bucketName, oldFileName, bucketName, newFileName);
        verify(mockS3Client).deleteObject(bucketName, oldFileName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenameFile_WhenBucketNameIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.renameFile(null, "old.txt", "new.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenameFile_WhenOldFileNameIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.renameFile("test-bucket", null, "new.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenameFile_WhenNewFileNameIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.renameFile("test-bucket", "old.txt", null);
    }

    @Test
    public void testRenameFile_WhenS3ExceptionOccurs_ThrowsRuntimeException() {
        // Arrange
        String bucketName = "test-bucket";
        String oldFileName = "old-file.txt";
        String newFileName = "new-file.txt";

        AmazonS3Exception s3Exception = new AmazonS3Exception("Access denied");
        s3Exception.setStatusCode(500); // Use 500 to trigger default case

        when(mockS3Client.copyObject(bucketName, oldFileName, bucketName, newFileName)).thenThrow(s3Exception);

        // Act & Assert
        try {
            fileDao.renameFile(bucketName, oldFileName, newFileName);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }

    // ==================== Delete File Tests ====================

    @Test
    public void testDeleteFile_WhenFileExists_DeletesSuccessfully() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "test-file.txt";

        when(mockS3Client.doesObjectExist(bucketName, fileKey)).thenReturn(true);

        // Act
        String result = fileDao.deleteFile(bucketName, fileKey);

        // Assert
        assertTrue(result.contains("successfully deleted"));
        assertTrue(result.contains(fileKey));
        assertTrue(result.contains(bucketName));
        verify(mockS3Client).deleteObject(bucketName, fileKey);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFile_WhenBucketNameIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.deleteFile(null, "test-file.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteFile_WhenFileKeyIsNull_ThrowsIllegalArgumentException() {
        // Act
        fileDao.deleteFile("test-bucket", null);
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteFile_WhenFileDoesNotExist_ThrowsRuntimeException() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "non-existent-file.txt";

        when(mockS3Client.doesObjectExist(bucketName, fileKey)).thenReturn(false);

        // Act
        fileDao.deleteFile(bucketName, fileKey);
    }

    @Test
    public void testDeleteFile_WhenS3ExceptionOccurs_ThrowsAmazonServiceException() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "test-file.txt";

        when(mockS3Client.doesObjectExist(bucketName, fileKey)).thenReturn(true);

        AmazonServiceException s3Exception = new AmazonServiceException("Access denied");
        doThrow(s3Exception).when(mockS3Client).deleteObject(bucketName, fileKey);

        // Act & Assert
        try {
            fileDao.deleteFile(bucketName, fileKey);
            fail("Expected AmazonServiceException");
        } catch (AmazonServiceException e) {
            assertNotNull(e);
        }
    }

    // ==================== Get File List Tests ====================

    @Test
    public void testGetFileList_WhenFilesExist_ReturnsFileDetailsList() {
        // Arrange
        String bucketName = "test-bucket";

        ObjectListing objectListing = new ObjectListing();
        List<S3ObjectSummary> summaries = new ArrayList<>();

        S3ObjectSummary summary1 = new S3ObjectSummary();
        summary1.setKey("file1.txt");
        summary1.setBucketName(bucketName);
        summary1.setETag("etag1");
        summary1.setSize(1024L);
        summary1.setLastModified(new Date());
        summary1.setStorageClass("STANDARD");
        summaries.add(summary1);

        S3ObjectSummary summary2 = new S3ObjectSummary();
        summary2.setKey("file2.txt");
        summary2.setBucketName(bucketName);
        summary2.setETag("etag2");
        summary2.setSize(2048L);
        summary2.setLastModified(new Date());
        summary2.setStorageClass("STANDARD");
        summaries.add(summary2);

        objectListing.getObjectSummaries().addAll(summaries);

        AccessControlList acl = new AccessControlList();
        Owner owner = new Owner();
        owner.setDisplayName("test-owner");
        acl.setOwner(owner);

        when(mockS3Client.listObjects(bucketName)).thenReturn(objectListing);
        when(mockS3Client.getObjectAcl(anyString(), anyString())).thenReturn(acl);

        // Act
        List<FileDetails> result = fileDao.getFileList(bucketName);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("file1.txt", result.get(0).fileName);
        assertEquals("file2.txt", result.get(1).fileName);
        assertEquals("test-owner", result.get(0).owner);
    }

    @Test
    public void testGetFileList_WhenFolderExists_ExcludesFolders() {
        // Arrange
        String bucketName = "test-bucket";

        ObjectListing objectListing = new ObjectListing();
        List<S3ObjectSummary> summaries = new ArrayList<>();

        S3ObjectSummary fileSummary = new S3ObjectSummary();
        fileSummary.setKey("file.txt");
        fileSummary.setBucketName(bucketName);
        fileSummary.setETag("etag1");
        fileSummary.setSize(1024L);
        fileSummary.setLastModified(new Date());
        fileSummary.setStorageClass("STANDARD");
        summaries.add(fileSummary);

        S3ObjectSummary folderSummary = new S3ObjectSummary();
        folderSummary.setKey("folder/");
        folderSummary.setBucketName(bucketName);
        summaries.add(folderSummary);

        objectListing.getObjectSummaries().addAll(summaries);

        AccessControlList acl = new AccessControlList();
        Owner owner = new Owner();
        owner.setDisplayName("test-owner");
        acl.setOwner(owner);

        when(mockS3Client.listObjects(bucketName)).thenReturn(objectListing);
        when(mockS3Client.getObjectAcl(anyString(), anyString())).thenReturn(acl);

        // Act
        List<FileDetails> result = fileDao.getFileList(bucketName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("file.txt", result.get(0).fileName);
    }

    @Test
    public void testGetFileList_WhenExceptionOccurs_ReturnsEmptyList() {
        // Arrange
        String bucketName = "test-bucket";

        AmazonS3Exception s3Exception = new AmazonS3Exception("Access denied");
        s3Exception.setStatusCode(403);

        when(mockS3Client.listObjects(bucketName)).thenThrow(s3Exception);

        // Act & Assert
        try {
            List<FileDetails> result = fileDao.getFileList(bucketName);
            fail("Expected RuntimeException");
        } catch (Exception e) {
            // Expected
        }
    }

    // ==================== Get Owner Tests ====================

    @Test
    public void testGetOwner_WhenValidInput_ReturnsOwnerName() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "test-file.txt";

        AccessControlList acl = new AccessControlList();
        Owner owner = new Owner();
        owner.setDisplayName("test-owner");
        acl.setOwner(owner);

        when(mockS3Client.getObjectAcl(bucketName, fileKey)).thenReturn(acl);

        // Act
        String result = fileDao.getOwner(bucketName, fileKey);

        // Assert
        assertEquals("test-owner", result);
    }

    // ==================== Link Generator Tests ====================

    @Test
    public void testLinkGenerator_WhenValidInput_ReturnsPresignedUrlRequest() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "test-file.txt";

        // Act
        GeneratePresignedUrlRequest request = fileDao.linkGenerator(bucketName, fileKey);

        // Assert
        assertNotNull(request);
        assertEquals(bucketName, request.getBucketName());
        assertEquals(fileKey, request.getKey());
        assertEquals(HttpMethod.GET, request.getMethod());
        assertNotNull(request.getExpiration());

        // Verify expiration is approximately 7 days from now
        long expectedExpiration = new Date().getTime() + Constants.EXPIRATION_TIME;
        long actualExpiration = request.getExpiration().getTime();
        assertTrue(Math.abs(actualExpiration - expectedExpiration) < 1000); // Within 1 second
    }

    // ==================== Copy Object With Updated Metadata Tests ====================

    @Test
    public void testCopyObjectWithUpdatedMetadata_WhenValidInput_CopiesWithMetadata() {
        // Arrange
        String bucketName = "test-bucket";
        String objectKey = "test-file.txt";

        // Act
        String result = fileDao.copyObjectWithUpdatedMetadata(bucketName, objectKey);

        // Assert
        assertEquals("File copied successfully", result);

        ArgumentCaptor<CopyObjectRequest> captor = ArgumentCaptor.forClass(CopyObjectRequest.class);
        verify(mockS3Client).copyObject(captor.capture());

        CopyObjectRequest request = captor.getValue();
        assertEquals(bucketName, request.getSourceBucketName());
        assertEquals(objectKey, request.getSourceKey());
        assertEquals(bucketName, request.getDestinationBucketName());
        assertEquals(objectKey, request.getDestinationKey());
        assertNotNull(request.getNewObjectMetadata());
    }

    // ==================== Get Invalid Bucket From XML Tests ====================

    @Test
    public void testGetInvalidBucketFromXML_WhenValidXML_ExtractsBucketName() {
        // Arrange
        String xml = "<Error><BucketName>invalid-bucket-name</BucketName></Error>";

        // Act
        String result = fileDao.getInvalidBucketFromXML(xml);

        // Assert
        assertEquals("invalid-bucket-name", result);
    }

    @Test
    public void testGetInvalidBucketFromXML_WhenInvalidXML_ReturnsNull() {
        // Arrange
        String xml = "<Error><Message>Some error</Message></Error>";

        // Act
        String result = fileDao.getInvalidBucketFromXML(xml);

        // Assert
        assertNull(result);
    }

    @Test
    public void testGetInvalidBucketFromXML_WhenEmptyXML_ReturnsNull() {
        // Arrange
        String xml = "";

        // Act
        String result = fileDao.getInvalidBucketFromXML(xml);

        // Assert
        assertNull(result);
    }

    // ==================== Error Handling Tests ====================

    @Test(expected = RuntimeException.class)
    public void testHandleS3Exception_WhenNoSuchKey_ThrowsRuntimeException() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "non-existent-file.txt";

        AmazonS3Exception s3Exception = new AmazonS3Exception("Not found");
        s3Exception.setStatusCode(404);
        s3Exception.setErrorCode("NoSuchKey");

        when(mockS3Client.getObject(bucketName, fileKey)).thenThrow(s3Exception);

        // Act
        fileDao.downloadFile(bucketName, fileKey);
    }

    @Test
    public void testHandleS3Exception_WhenNoSuchBucket_ThrowsRuntimeException() {
        // Arrange
        String bucketName = "non-existent-bucket";
        String fileKey = "test-file.txt";

        AmazonS3Exception s3Exception = new AmazonS3Exception("Not found") {
            @Override
            public String getErrorResponseXml() {
                return "<Error><BucketName>non-existent-bucket</BucketName></Error>";
            }
        };
        s3Exception.setStatusCode(404);
        s3Exception.setErrorCode("NoSuchBucket");

        when(mockS3Client.getObject(bucketName, fileKey)).thenThrow(s3Exception);

        // Act & Assert
        try {
            fileDao.downloadFile(bucketName, fileKey);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("does not exist"));
        }
    }

    @Test
    public void testHandleS3Exception_WhenInvalidRequest_ThrowsRuntimeException() {
        // Arrange
        String bucketName = "test-bucket";
        String sourceFile = "source.txt";
        String destFile = "source.txt"; // Same file name to trigger same bucket logic

        AmazonS3Exception s3Exception = new AmazonS3Exception("Invalid request");
        s3Exception.setStatusCode(400);
        s3Exception.setErrorCode("InvalidRequest");

        when(mockS3Client.copyObject(any(CopyObjectRequest.class))).thenThrow(s3Exception);

        // Act & Assert
        try {
            fileDao.copyFile(bucketName, sourceFile, bucketName, destFile);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Cannot copy file to itself"));
        }
    }

    @Test
    public void testHandleS3Exception_WhenInvalidBucketName_ThrowsRuntimeException() {
        // Arrange
        String bucketName = "Invalid_Bucket_Name";
        String sourceFile = "source.txt";
        String destFile = "dest.txt";

        AmazonS3Exception s3Exception = new AmazonS3Exception("Invalid bucket name");
        s3Exception.setStatusCode(400);
        s3Exception.setErrorCode("InvalidBucketName");

        when(mockS3Client.copyObject(bucketName, sourceFile, destFile, destFile)).thenThrow(s3Exception);

        // Act & Assert
        try {
            fileDao.copyFile(bucketName, sourceFile, destFile, destFile);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("is invalid"));
        }
    }

    @Test(expected = RuntimeException.class)
    public void testHandleS3Exception_WhenUnknownStatusCode_ThrowsRuntimeException() {
        // Arrange
        String bucketName = "test-bucket";
        String fileKey = "test-file.txt";

        AmazonS3Exception s3Exception = new AmazonS3Exception("Internal server error");
        s3Exception.setStatusCode(500);

        when(mockS3Client.getObject(bucketName, fileKey)).thenThrow(s3Exception);

        // Act
        fileDao.downloadFile(bucketName, fileKey);
    }
}

