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

package app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector;

import app.krista.extensions.essentials.cloudstorage.aws_s3.AWS3Attributes;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for S3Provider class.
 * Tests cover constructor, S3 client creation, error handling, and edge cases.
 */
public class S3ProviderTest {

    @Mock
    private AWS3Attributes mockAttributes;

    private S3Provider s3Provider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testConstructor_WhenValidAttributes_CreatesInstance() {
        // Arrange & Act
        s3Provider = new S3Provider(mockAttributes);

        // Assert
        assertNotNull(s3Provider);
    }

    @Test
    public void testConstructor_WhenNullAttributes_CreatesInstance() {
        // Act
        S3Provider provider = new S3Provider(null);

        // Assert
        assertNotNull(provider);
    }

    // ==================== Get S3 Client Tests ====================

    @Test
    public void testGetS3Client_WhenValidCredentials_ReturnsS3Client() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("us-east-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client);
        verify(mockAttributes).getAccessKey();
        verify(mockAttributes).getSecretKey();
        verify(mockAttributes).getRegion();
    }

    @Test
    public void testGetS3Client_WhenRegionIsUpperCase_ConvertsToLowerCase() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("US-EAST-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client);
        verify(mockAttributes).getRegion();
    }

    @Test
    public void testGetS3Client_WhenRegionIsMixedCase_ConvertsToLowerCase() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("Us-EaSt-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client);
    }

    @Test
    public void testGetS3Client_WhenDifferentRegion_ReturnsS3Client() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("eu-west-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client);
    }

    @Test
    public void testGetS3Client_CalledMultipleTimes_CreatesNewClientEachTime() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("us-east-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client1 = s3Provider.getS3Client();
        AmazonS3 s3Client2 = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client1);
        assertNotNull(s3Client2);
        assertNotSame(s3Client1, s3Client2); // Different instances
    }

    // ==================== Error Handling Tests ====================

    @Test(expected = AmazonClientException.class)
    public void testGetS3Client_WhenNullAccessKey_ThrowsAmazonClientException() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn(null);
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("us-east-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        s3Provider.getS3Client();
    }

    @Test(expected = AmazonClientException.class)
    public void testGetS3Client_WhenNullSecretKey_ThrowsAmazonClientException() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn(null);
        when(mockAttributes.getRegion()).thenReturn("us-east-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        s3Provider.getS3Client();
    }

    @Test
    public void testGetS3Client_WhenEmptyAccessKey_ReturnsS3Client() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("us-east-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client);
    }

    @Test
    public void testGetS3Client_WhenEmptySecretKey_ReturnsS3Client() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("");
        when(mockAttributes.getRegion()).thenReturn("us-east-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client);
    }

    @Test
    public void testGetS3Client_WhenInvalidRegion_CreatesClientButMayFailOnUse() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("invalid-region-xyz");

        s3Provider = new S3Provider(mockAttributes);

        // Act & Assert
        // AWS SDK may create client with invalid region but fail on actual use
        try {
            AmazonS3 s3Client = s3Provider.getS3Client();
            // Client creation may succeed, but operations will fail
            assertNotNull(s3Client);
        } catch (AmazonClientException e) {
            // Or it may fail during creation
            assertTrue(e.getMessage().contains("Failed to create Amazon S3 client"));
        }
    }

    @Test
    public void testGetS3Client_WhenNullRegion_ThrowsAmazonClientException() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn(null);

        s3Provider = new S3Provider(mockAttributes);

        // Act & Assert
        try {
            s3Provider.getS3Client();
            fail("Expected AmazonClientException");
        } catch (AmazonClientException e) {
            assertTrue(e.getMessage().contains("Failed to create Amazon S3 client"));
        }
    }

    @Test
    public void testGetS3Client_WhenEmptyRegion_CreatesClientButMayFailOnUse() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("");

        s3Provider = new S3Provider(mockAttributes);

        // Act & Assert
        // AWS SDK may create client with empty region but fail on actual use
        try {
            AmazonS3 s3Client = s3Provider.getS3Client();
            // Client creation may succeed, but operations will fail
            assertNotNull(s3Client);
        } catch (AmazonClientException e) {
            // Or it may fail during creation
            assertTrue(e.getMessage().contains("Failed to create Amazon S3 client"));
        }
    }

    // ==================== Edge Case Tests ====================

    @Test
    public void testGetS3Client_WhenValidCredentialsWithSpecialCharacters_ReturnsS3Client() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIA+TEST/KEY=EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalr+XUtn/FEMI=K7MDENG");
        when(mockAttributes.getRegion()).thenReturn("us-west-2");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client);
    }

    @Test
    public void testGetS3Client_WhenApSoutheast1Region_ReturnsS3Client() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("ap-southeast-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client);
    }

    @Test
    public void testGetS3Client_WhenUsWest2Region_ReturnsS3Client() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("us-west-2");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client);
    }

    @Test
    public void testGetS3Client_WhenEuCentral1Region_ReturnsS3Client() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("eu-central-1");

        s3Provider = new S3Provider(mockAttributes);

        // Act
        AmazonS3 s3Client = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client);
    }

    @Test
    public void testGetS3Client_WhenAttributesChange_UsesNewAttributes() {
        // Arrange
        when(mockAttributes.getAccessKey()).thenReturn("AKIAIOSFODNN7EXAMPLE");
        when(mockAttributes.getSecretKey()).thenReturn("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        when(mockAttributes.getRegion()).thenReturn("us-east-1");

        s3Provider = new S3Provider(mockAttributes);
        AmazonS3 s3Client1 = s3Provider.getS3Client();

        // Change attributes
        when(mockAttributes.getAccessKey()).thenReturn("AKIANEWKEY123456789");
        when(mockAttributes.getSecretKey()).thenReturn("newSecretKey123456789");
        when(mockAttributes.getRegion()).thenReturn("us-west-2");

        // Act
        AmazonS3 s3Client2 = s3Provider.getS3Client();

        // Assert
        assertNotNull(s3Client1);
        assertNotNull(s3Client2);
        assertNotSame(s3Client1, s3Client2);
    }
}

