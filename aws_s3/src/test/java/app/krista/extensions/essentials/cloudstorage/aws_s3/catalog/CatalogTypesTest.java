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

package app.krista.extensions.essentials.cloudstorage.aws_s3.catalog;

import app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.entities.StorageBucket;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.util.Constants;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Owner;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Comprehensive unit tests for CatalogTypes class.
 * Tests cover conversion methods, null handling, and edge cases.
 */
public class CatalogTypesTest {

    private Bucket mockBucket;
    private Owner mockOwner;
    private Date testDate;

    @Before
    public void setUp() {
        testDate = new Date(1609459200000L); // 2021-01-01 00:00:00 UTC
        mockOwner = new Owner();
        mockOwner.setDisplayName("Test Owner");
        mockOwner.setId("owner-123");
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testConstructor_CannotBeInstantiated() {
        // This test verifies the private constructor exists
        // CatalogTypes should be a utility class with only static methods
        try {
            java.lang.reflect.Constructor<CatalogTypes> constructor = CatalogTypes.class.getDeclaredConstructor();
            assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (Exception e) {
            // Expected - private constructor
        }
    }

    // ==================== toKristaBucket Tests ====================

    @Test
    public void testToKristaBucket_WhenValidBucket_ReturnsStorageBucket() {
        // Arrange
        mockBucket = new Bucket("test-bucket");
        mockBucket.setOwner(mockOwner);
        mockBucket.setCreationDate(testDate);

        // Act
        StorageBucket result = CatalogTypes.toKristaBucket(mockBucket);

        // Assert
        assertNotNull(result);
        assertEquals("test-bucket", result.name);
        assertEquals("Test Owner", result.owner);
        assertEquals(Long.valueOf(1609459200000L), result.createdDate);
    }

    @Test
    public void testToKristaBucket_WhenNullBucket_ReturnsEmptyBucket() {
        // Act
        StorageBucket result = CatalogTypes.toKristaBucket(null);

        // Assert
        assertNotNull(result);
        assertSame(Constants.EMPTY_BUCKET, result);
    }

    @Test
    public void testToKristaBucket_WhenBucketWithDifferentName_ReturnsCorrectName() {
        // Arrange
        mockBucket = new Bucket("my-production-bucket");
        mockBucket.setOwner(mockOwner);
        mockBucket.setCreationDate(testDate);

        // Act
        StorageBucket result = CatalogTypes.toKristaBucket(mockBucket);

        // Assert
        assertEquals("my-production-bucket", result.name);
    }

    @Test
    public void testToKristaBucket_WhenBucketWithDifferentOwner_ReturnsCorrectOwner() {
        // Arrange
        Owner differentOwner = new Owner();
        differentOwner.setDisplayName("John Doe");
        differentOwner.setId("owner-456");

        mockBucket = new Bucket("test-bucket");
        mockBucket.setOwner(differentOwner);
        mockBucket.setCreationDate(testDate);

        // Act
        StorageBucket result = CatalogTypes.toKristaBucket(mockBucket);

        // Assert
        assertEquals("John Doe", result.owner);
    }

    @Test
    public void testToKristaBucket_WhenBucketWithDifferentDate_ReturnsCorrectDate() {
        // Arrange
        Date differentDate = new Date(1640995200000L); // 2022-01-01 00:00:00 UTC
        mockBucket = new Bucket("test-bucket");
        mockBucket.setOwner(mockOwner);
        mockBucket.setCreationDate(differentDate);

        // Act
        StorageBucket result = CatalogTypes.toKristaBucket(mockBucket);

        // Assert
        assertEquals(Long.valueOf(1640995200000L), result.createdDate);
    }

    @Test
    public void testToKristaBucket_CalledMultipleTimes_ReturnsNewInstanceEachTime() {
        // Arrange
        mockBucket = new Bucket("test-bucket");
        mockBucket.setOwner(mockOwner);
        mockBucket.setCreationDate(testDate);

        // Act
        StorageBucket result1 = CatalogTypes.toKristaBucket(mockBucket);
        StorageBucket result2 = CatalogTypes.toKristaBucket(mockBucket);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2); // Different instances
        assertEquals(result1.name, result2.name);
        assertEquals(result1.owner, result2.owner);
        assertEquals(result1.createdDate, result2.createdDate);
    }

    @Test
    public void testToKristaBucket_WhenBucketWithSpecialCharactersInName_HandlesCorrectly() {
        // Arrange
        mockBucket = new Bucket("my-bucket-2024.test");
        mockBucket.setOwner(mockOwner);
        mockBucket.setCreationDate(testDate);

        // Act
        StorageBucket result = CatalogTypes.toKristaBucket(mockBucket);

        // Assert
        assertEquals("my-bucket-2024.test", result.name);
    }

    // ==================== toKristaBuckets Tests ====================

    @Test
    public void testToKristaBuckets_WhenEmptyList_ReturnsEmptyList() {
        // Arrange
        List<Bucket> emptyList = new ArrayList<>();

        // Act
        List<StorageBucket> result = CatalogTypes.toKristaBuckets(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.size());
    }

    @Test
    public void testToKristaBuckets_WhenSingleBucket_ReturnsListWithOneBucket() {
        // Arrange
        mockBucket = new Bucket("single-bucket");
        mockBucket.setOwner(mockOwner);
        mockBucket.setCreationDate(testDate);
        List<Bucket> bucketList = Arrays.asList(mockBucket);

        // Act
        List<StorageBucket> result = CatalogTypes.toKristaBuckets(bucketList);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("single-bucket", result.get(0).name);
        assertEquals("Test Owner", result.get(0).owner);
        assertEquals(Long.valueOf(1609459200000L), result.get(0).createdDate);
    }

    @Test
    public void testToKristaBuckets_WhenMultipleBuckets_ReturnsListWithAllBuckets() {
        // Arrange
        Bucket bucket1 = new Bucket("bucket-1");
        bucket1.setOwner(mockOwner);
        bucket1.setCreationDate(testDate);

        Owner owner2 = new Owner();
        owner2.setDisplayName("Owner 2");
        owner2.setId("owner-2");
        Bucket bucket2 = new Bucket("bucket-2");
        bucket2.setOwner(owner2);
        bucket2.setCreationDate(new Date(1640995200000L));

        Owner owner3 = new Owner();
        owner3.setDisplayName("Owner 3");
        owner3.setId("owner-3");
        Bucket bucket3 = new Bucket("bucket-3");
        bucket3.setOwner(owner3);
        bucket3.setCreationDate(new Date(1672531200000L));

        List<Bucket> bucketList = Arrays.asList(bucket1, bucket2, bucket3);

        // Act
        List<StorageBucket> result = CatalogTypes.toKristaBuckets(bucketList);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());

        assertEquals("bucket-1", result.get(0).name);
        assertEquals("Test Owner", result.get(0).owner);
        assertEquals(Long.valueOf(1609459200000L), result.get(0).createdDate);

        assertEquals("bucket-2", result.get(1).name);
        assertEquals("Owner 2", result.get(1).owner);
        assertEquals(Long.valueOf(1640995200000L), result.get(1).createdDate);

        assertEquals("bucket-3", result.get(2).name);
        assertEquals("Owner 3", result.get(2).owner);
        assertEquals(Long.valueOf(1672531200000L), result.get(2).createdDate);
    }

    @Test
    public void testToKristaBuckets_WhenListContainsNullBucket_HandlesNullCorrectly() {
        // Arrange
        mockBucket = new Bucket("valid-bucket");
        mockBucket.setOwner(mockOwner);
        mockBucket.setCreationDate(testDate);

        List<Bucket> bucketList = Arrays.asList(mockBucket, null);

        // Act
        List<StorageBucket> result = CatalogTypes.toKristaBuckets(bucketList);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("valid-bucket", result.get(0).name);
        assertSame(Constants.EMPTY_BUCKET, result.get(1)); // Null bucket converted to EMPTY_BUCKET
    }

    @Test
    public void testToKristaBuckets_WhenListContainsOnlyNulls_ReturnsListOfEmptyBuckets() {
        // Arrange
        List<Bucket> bucketList = Arrays.asList(null, null, null);

        // Act
        List<StorageBucket> result = CatalogTypes.toKristaBuckets(bucketList);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertSame(Constants.EMPTY_BUCKET, result.get(0));
        assertSame(Constants.EMPTY_BUCKET, result.get(1));
        assertSame(Constants.EMPTY_BUCKET, result.get(2));
    }

    @Test
    public void testToKristaBuckets_WhenLargeList_ProcessesAllBuckets() {
        // Arrange
        List<Bucket> bucketList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Bucket bucket = new Bucket("bucket-" + i);
            Owner owner = new Owner();
            owner.setDisplayName("Owner " + i);
            owner.setId("owner-" + i);
            bucket.setOwner(owner);
            bucket.setCreationDate(new Date(1609459200000L + i * 1000));
            bucketList.add(bucket);
        }

        // Act
        List<StorageBucket> result = CatalogTypes.toKristaBuckets(bucketList);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.size());
        for (int i = 0; i < 100; i++) {
            assertEquals("bucket-" + i, result.get(i).name);
            assertEquals("Owner " + i, result.get(i).owner);
            assertEquals(Long.valueOf(1609459200000L + i * 1000), result.get(i).createdDate);
        }
    }

    @Test
    public void testToKristaBuckets_PreservesOrder_MaintainsOriginalSequence() {
        // Arrange
        Bucket bucket1 = new Bucket("alpha-bucket");
        bucket1.setOwner(mockOwner);
        bucket1.setCreationDate(testDate);

        Bucket bucket2 = new Bucket("beta-bucket");
        bucket2.setOwner(mockOwner);
        bucket2.setCreationDate(testDate);

        Bucket bucket3 = new Bucket("gamma-bucket");
        bucket3.setOwner(mockOwner);
        bucket3.setCreationDate(testDate);

        List<Bucket> bucketList = Arrays.asList(bucket1, bucket2, bucket3);

        // Act
        List<StorageBucket> result = CatalogTypes.toKristaBuckets(bucketList);

        // Assert
        assertEquals("alpha-bucket", result.get(0).name);
        assertEquals("beta-bucket", result.get(1).name);
        assertEquals("gamma-bucket", result.get(2).name);
    }
}
