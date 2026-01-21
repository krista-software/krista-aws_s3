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

package app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.stores;

import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.entities.StorageBucket;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3Provider;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3ProviderFactory;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for StorageBucketStore class.
 * Tests cover all EntityStore methods and S3 bucket operations.
 */
public class StorageBucketStoreTest {

    private StorageBucketStore storageBucketStore;
    private S3ProviderFactory mockProviderFactory;
    private S3Provider mockProvider;
    private AmazonS3 mockS3Client;
    private Bucket mockBucket;
    private Owner mockOwner;

    @Before
    public void setUp() {
        mockProviderFactory = mock(S3ProviderFactory.class);
        mockProvider = mock(S3Provider.class);
        mockS3Client = mock(AmazonS3.class);
        mockBucket = mock(Bucket.class);
        mockOwner = new Owner();
        mockOwner.setDisplayName("Test Owner");
        mockOwner.setId("owner-123");

        when(mockProviderFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getS3Client()).thenReturn(mockS3Client);

        storageBucketStore = new StorageBucketStore(mockProviderFactory);
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testConstructor_WhenValidProviderFactory_CreatesInstance() {
        // Arrange
        S3ProviderFactory newMockProviderFactory = mock(S3ProviderFactory.class);
        S3Provider newMockProvider = mock(S3Provider.class);
        when(newMockProviderFactory.create()).thenReturn(newMockProvider);

        // Act
        StorageBucketStore store = new StorageBucketStore(newMockProviderFactory);

        // Assert
        assertNotNull(store);
        verify(newMockProviderFactory).create();
    }

    // ==================== create() Method Tests ====================

    @Test
    public void testCreate_WhenBucketDoesNotExist_CreatesNewBucket() throws IOException {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("Name", "new-bucket");
        map.put("Owner", "John Doe");

        when(mockS3Client.doesBucketExistV2("new-bucket")).thenReturn(false);
        when(mockS3Client.createBucket("new-bucket")).thenReturn(mockBucket);
        when(mockBucket.getName()).thenReturn("new-bucket");
        when(mockBucket.getOwner()).thenReturn(mockOwner);
        when(mockBucket.getCreationDate()).thenReturn(new Date());

        // Act
        StorageBucket result = storageBucketStore.create(map);

        // Assert
        assertNotNull(result);
        assertEquals("new-bucket", result.name);
        verify(mockS3Client).doesBucketExistV2("new-bucket");
        verify(mockS3Client).createBucket("new-bucket");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_WhenBucketAlreadyExists_ThrowsIllegalArgumentException() throws IOException {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("Name", "existing-bucket");
        map.put("Owner", "John Doe");

        when(mockS3Client.doesBucketExistV2("existing-bucket")).thenReturn(true);

        // Act
        storageBucketStore.create(map);

        // Assert - IllegalArgumentException expected
    }

    @Test
    public void testCreate_WhenBucketNameIsUpperCase_ConvertsToLowerCase() throws IOException {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("Name", "MY-BUCKET");
        map.put("Owner", "John Doe");

        when(mockS3Client.doesBucketExistV2("my-bucket")).thenReturn(false);
        when(mockS3Client.createBucket("my-bucket")).thenReturn(mockBucket);
        when(mockBucket.getName()).thenReturn("my-bucket");
        when(mockBucket.getOwner()).thenReturn(mockOwner);
        when(mockBucket.getCreationDate()).thenReturn(new Date());

        // Act
        StorageBucket result = storageBucketStore.create(map);

        // Assert
        assertNotNull(result);
        verify(mockS3Client).doesBucketExistV2("my-bucket");
        verify(mockS3Client).createBucket("my-bucket");
    }

    @Test(expected = RuntimeException.class)
    public void testCreate_WhenS3ClientThrowsException_ThrowsRuntimeException() throws IOException {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("Name", "test-bucket");
        map.put("Owner", "John Doe");

        when(mockS3Client.doesBucketExistV2(anyString())).thenThrow(new AmazonServiceException("S3 error"));

        // Act
        storageBucketStore.create(map);

        // Assert - RuntimeException expected
    }

    @Test
    public void testCreate_WhenDifferentOwner_SetsOwnerCorrectly() throws IOException {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        map.put("Name", "test-bucket");
        map.put("Owner", "Jane Smith");

        when(mockS3Client.doesBucketExistV2("test-bucket")).thenReturn(false);
        when(mockS3Client.createBucket("test-bucket")).thenReturn(mockBucket);
        when(mockBucket.getName()).thenReturn("test-bucket");
        when(mockBucket.getOwner()).thenReturn(mockOwner);
        when(mockBucket.getCreationDate()).thenReturn(new Date());

        // Act
        StorageBucket result = storageBucketStore.create(map);

        // Assert
        assertNotNull(result);
        verify(mockBucket).setOwner(any(Owner.class));
    }

    // ==================== get() Method Tests ====================

    @Test
    public void testGet_WhenBucketExists_ReturnsStorageBucket() throws IOException {
        // Arrange
        String bucketName = "test-bucket";
        Bucket bucket1 = new Bucket("test-bucket");
        bucket1.setOwner(mockOwner);
        bucket1.setCreationDate(new Date());

        List<Bucket> buckets = Arrays.asList(bucket1);
        when(mockS3Client.listBuckets()).thenReturn(buckets);

        // Act
        StorageBucket result = storageBucketStore.get(bucketName);

        // Assert
        assertNotNull(result);
        assertEquals("test-bucket", result.name);
        verify(mockS3Client).listBuckets();
    }

    @Test(expected = IOException.class)
    public void testGet_WhenBucketDoesNotExist_ThrowsIOException() throws IOException {
        // Arrange
        String bucketName = "non-existent-bucket";
        List<Bucket> buckets = new ArrayList<>();
        when(mockS3Client.listBuckets()).thenReturn(buckets);

        // Act
        storageBucketStore.get(bucketName);

        // Assert - IOException expected
    }

    @Test
    public void testGet_WhenBucketNameIsUpperCase_FindsLowerCaseBucket() throws IOException {
        // Arrange
        String bucketName = "TEST-BUCKET";
        Bucket bucket1 = new Bucket("test-bucket");
        bucket1.setOwner(mockOwner);
        bucket1.setCreationDate(new Date());

        List<Bucket> buckets = Arrays.asList(bucket1);
        when(mockS3Client.listBuckets()).thenReturn(buckets);

        // Act
        StorageBucket result = storageBucketStore.get(bucketName);

        // Assert
        assertNotNull(result);
        assertEquals("test-bucket", result.name);
    }

    @Test
    public void testGet_WhenMultipleBuckets_FindsCorrectOne() throws IOException {
        // Arrange
        String bucketName = "bucket-2";
        Bucket bucket1 = new Bucket("bucket-1");
        bucket1.setOwner(mockOwner);
        bucket1.setCreationDate(new Date());

        Bucket bucket2 = new Bucket("bucket-2");
        bucket2.setOwner(mockOwner);
        bucket2.setCreationDate(new Date());

        Bucket bucket3 = new Bucket("bucket-3");
        bucket3.setOwner(mockOwner);
        bucket3.setCreationDate(new Date());

        List<Bucket> buckets = Arrays.asList(bucket1, bucket2, bucket3);
        when(mockS3Client.listBuckets()).thenReturn(buckets);

        // Act
        StorageBucket result = storageBucketStore.get(bucketName);

        // Assert
        assertNotNull(result);
        assertEquals("bucket-2", result.name);
    }

    @Test(expected = IOException.class)
    public void testGet_WhenAmazonS3ExceptionWithConflict_ThrowsIOException() throws IOException {
        // Arrange
        String bucketName = "conflict-bucket";
        AmazonS3Exception exception = new AmazonS3Exception("Conflict");
        exception.setStatusCode(409);
        when(mockS3Client.listBuckets()).thenThrow(exception);

        // Act
        storageBucketStore.get(bucketName);

        // Assert - IOException expected with conflict message
    }

    @Test(expected = IOException.class)
    public void testGet_WhenAmazonS3ExceptionWithOtherError_ThrowsIOException() throws IOException {
        // Arrange
        String bucketName = "error-bucket";
        AmazonS3Exception exception = new AmazonS3Exception("Access Denied");
        exception.setStatusCode(403);
        when(mockS3Client.listBuckets()).thenThrow(exception);

        // Act
        storageBucketStore.get(bucketName);

        // Assert - IOException expected
    }

    // ==================== update() Method Tests ====================

    @Test
    public void testUpdate_ReturnsNull() {
        // Arrange
        StorageBucket bucket = new StorageBucket();
        bucket.name = "test-bucket";

        // Act
        StorageBucket result = storageBucketStore.update(bucket);

        // Assert
        assertNull(result);
    }

    // ==================== delete() Method Tests ====================

    @Test
    public void testDelete_WhenBucketIsEmpty_DeletesSuccessfully() {
        // Arrange
        String bucketName = "empty-bucket";
        ListObjectsV2Result emptyResult = new ListObjectsV2Result();
        emptyResult.getObjectSummaries().clear();

        when(mockS3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(emptyResult);

        // Act
        storageBucketStore.delete(bucketName);

        // Assert
        verify(mockS3Client).listObjectsV2(any(ListObjectsV2Request.class));
        verify(mockS3Client).deleteBucket(bucketName);
    }

    @Test(expected = AmazonS3Exception.class)
    public void testDelete_WhenBucketIsNotEmpty_ThrowsAmazonS3Exception() {
        // Arrange
        String bucketName = "non-empty-bucket";
        ListObjectsV2Result result = new ListObjectsV2Result();
        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setKey("file.txt");
        result.getObjectSummaries().add(summary);

        when(mockS3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(result);

        // Act
        storageBucketStore.delete(bucketName);

        // Assert - AmazonS3Exception expected (wraps IllegalArgumentException)
    }

    @Test(expected = AmazonS3Exception.class)
    public void testDelete_WhenS3ExceptionOccurs_ThrowsAmazonS3Exception() {
        // Arrange
        String bucketName = "error-bucket";
        when(mockS3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(new AmazonS3Exception("S3 error"));

        // Act
        storageBucketStore.delete(bucketName);

        // Assert - AmazonS3Exception expected
    }

    @Test(expected = AmazonS3Exception.class)
    public void testDelete_WhenGenericExceptionOccurs_ThrowsAmazonS3Exception() {
        // Arrange
        String bucketName = "error-bucket";
        when(mockS3Client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenThrow(new RuntimeException("Generic error"));

        // Act
        storageBucketStore.delete(bucketName);

        // Assert - AmazonS3Exception expected
    }

    // ==================== contains() Method Tests ====================

    @Test
    public void testContains_AlwaysReturnsFalse() {
        // Arrange
        String bucketName = "any-bucket";

        // Act
        boolean result = storageBucketStore.contains(bucketName);

        // Assert
        assertFalse(result);
    }

    // ==================== search() Deprecated Method Tests ====================

    @Test
    public void testSearchDeprecated_ReturnsEmptyList() {
        // Arrange
        List<SearchCondition> conditions = new ArrayList<>();

        // Act
        List<StorageBucket> result = storageBucketStore.search(conditions, 1, 10);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== search() with SearchQuery Tests ====================

    @Test
    public void testSearch_WhenNoSearchQuery_ReturnsAllBuckets() {
        // Arrange
        Bucket bucket1 = new Bucket("bucket-1");
        bucket1.setOwner(mockOwner);
        bucket1.setCreationDate(new Date());

        Bucket bucket2 = new Bucket("bucket-2");
        bucket2.setOwner(mockOwner);
        bucket2.setCreationDate(new Date());

        List<Bucket> buckets = Arrays.asList(bucket1, bucket2);
        when(mockS3Client.listBuckets()).thenReturn(buckets);

        // Act
        List<StorageBucket> result = storageBucketStore.search((SearchQuery) null, 1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testSearch_WhenSearchByName_FiltersCorrectly() {
        // Arrange
        Bucket bucket1 = new Bucket("test-bucket-1");
        bucket1.setOwner(mockOwner);
        bucket1.setCreationDate(new Date());

        Bucket bucket2 = new Bucket("prod-bucket-2");
        bucket2.setOwner(mockOwner);
        bucket2.setCreationDate(new Date());

        List<Bucket> buckets = Arrays.asList(bucket1, bucket2);
        when(mockS3Client.listBuckets()).thenReturn(buckets);

        SearchQuery searchQuery = mock(SearchQuery.class);
        SearchCondition condition = mock(SearchCondition.class);
        when(condition.getFieldName()).thenReturn("Name");
        when(condition.getOperand()).thenReturn("test");
        when(searchQuery.getSearchConditions()).thenReturn(Arrays.asList(condition));

        // Act
        List<StorageBucket> result = storageBucketStore.search(searchQuery, 1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-bucket-1", result.get(0).name);
    }

    @Test
    public void testSearch_WhenSingleBucket_AppliesPagination() {
        // Arrange
        Bucket bucket1 = new Bucket("single-bucket");
        bucket1.setOwner(mockOwner);
        bucket1.setCreationDate(new Date());

        List<Bucket> buckets = Arrays.asList(bucket1);
        when(mockS3Client.listBuckets()).thenReturn(buckets);

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getSearchConditions()).thenReturn(new ArrayList<>());

        // Act
        List<StorageBucket> result = storageBucketStore.search(searchQuery, 1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mockS3Client, times(2)).listBuckets(); // Called twice for pagination logic
    }

    @Test(expected = RuntimeException.class)
    public void testSearch_WhenExceptionOccursBeforeTry_ThrowsException() {
        // Arrange
        when(mockS3Client.listBuckets()).thenThrow(new RuntimeException("S3 error"));

        SearchQuery searchQuery = mock(SearchQuery.class);

        // Act
        storageBucketStore.search(searchQuery, 1, 10);

        // Assert - RuntimeException expected (thrown before try block)
    }

    @Test
    public void testSearch_WhenEmptySearchConditions_ReturnsAllBuckets() {
        // Arrange
        Bucket bucket1 = new Bucket("bucket-1");
        bucket1.setOwner(mockOwner);
        bucket1.setCreationDate(new Date());

        Bucket bucket2 = new Bucket("bucket-2");
        bucket2.setOwner(mockOwner);
        bucket2.setCreationDate(new Date());

        List<Bucket> buckets = Arrays.asList(bucket1, bucket2);
        when(mockS3Client.listBuckets()).thenReturn(buckets);

        SearchQuery searchQuery = mock(SearchQuery.class);
        when(searchQuery.getSearchConditions()).thenReturn(new ArrayList<>());

        // Act
        List<StorageBucket> result = storageBucketStore.search(searchQuery, 1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testSearch_WhenNonNameField_ReturnsAllBuckets() {
        // Arrange
        Bucket bucket1 = new Bucket("bucket-1");
        bucket1.setOwner(mockOwner);
        bucket1.setCreationDate(new Date());

        List<Bucket> buckets = Arrays.asList(bucket1);
        when(mockS3Client.listBuckets()).thenReturn(buckets);

        SearchQuery searchQuery = mock(SearchQuery.class);
        SearchCondition condition = mock(SearchCondition.class);
        when(condition.getFieldName()).thenReturn("Owner");
        when(condition.getOperand()).thenReturn("test");
        when(searchQuery.getSearchConditions()).thenReturn(Arrays.asList(condition));

        // Act
        List<StorageBucket> result = storageBucketStore.search(searchQuery, 1, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ==================== count() Method Tests ====================

    @Test(expected = UnsupportedOperationException.class)
    public void testCount_ThrowsUnsupportedOperationException() {
        // Arrange
        List<SearchCondition> conditions = new ArrayList<>();

        // Act
        storageBucketStore.count(conditions);

        // Assert - UnsupportedOperationException expected
    }

    // ==================== lookup() Method Tests ====================

    @Test(expected = UnsupportedOperationException.class)
    public void testLookup_ThrowsUnsupportedOperationException() {
        // Arrange
        Map<String, Object> map = new HashMap<>();
        List<String> list = new ArrayList<>();

        // Act
        storageBucketStore.lookup(map, list);

        // Assert - UnsupportedOperationException expected
    }
}
