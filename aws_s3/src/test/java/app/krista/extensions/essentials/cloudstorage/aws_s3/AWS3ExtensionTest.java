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

package app.krista.extensions.essentials.cloudstorage.aws_s3;

import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3Provider;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3ProviderFactory;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Owner;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AWS3Extension class.
 * Tests all public methods including connection validation and attribute updates.
 */
public class AWS3ExtensionTest {

    private AWS3Extension extension;
    private AWS3Attributes mockAttributes;
    private S3ProviderFactory mockProviderFactory;
    private S3Provider mockProvider;
    private AmazonS3 mockS3Client;

    @Before
    public void setUp() {
        mockAttributes = Mockito.mock(AWS3Attributes.class);
        mockProviderFactory = Mockito.mock(S3ProviderFactory.class);
        mockProvider = Mockito.mock(S3Provider.class);
        mockS3Client = Mockito.mock(AmazonS3.class);

        extension = new AWS3Extension(mockAttributes, mockProviderFactory);
    }

    @Test
    public void testConstructor_WhenValidDependencies_CreatesInstance() {
        // Assert
        assertNotNull(extension);
    }

    @Test
    public void testCustomTabs_ReturnsDocumentationTab() {
        // Act
        Map<String, String> tabs = extension.customTabs();

        // Assert
        assertNotNull(tabs);
        assertEquals(1, tabs.size());
        assertTrue(tabs.containsKey("Documentation"));
        assertEquals("static/docs", tabs.get("Documentation"));
    }

    @Test
    public void testValidateConnection_WhenValidCredentials_CompletesSuccessfully() {
        // Arrange
        Map<String, Object> connectionAttributes = new HashMap<>();
        connectionAttributes.put(AWS3Attributes.CLIENT_ACCESS_KEY, "REDACTED_SECRET");
        connectionAttributes.put(AWS3Attributes.CLIENT_SECRET, "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        connectionAttributes.put(AWS3Attributes.CLIENT_REGION, "us-east-1");

        AWS3Attributes testAttributes = new AWS3Attributes(connectionAttributes);
        Owner mockOwner = new Owner();
        mockOwner.setDisplayName("test-owner");

        when(mockProviderFactory.create(any(AWS3Attributes.class))).thenReturn(mockProvider);
        when(mockProvider.getS3Client()).thenReturn(mockS3Client);
        when(mockS3Client.getS3AccountOwner()).thenReturn(mockOwner);

        // Act
        extension.validateConnection(connectionAttributes);

        // Assert
        verify(mockProviderFactory).create(any(AWS3Attributes.class));
        verify(mockProvider).getS3Client();
        verify(mockS3Client).getS3AccountOwner();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateConnection_WhenInvalidCredentials_ThrowsIllegalArgumentException() {
        // Arrange
        Map<String, Object> connectionAttributes = new HashMap<>();
        connectionAttributes.put(AWS3Attributes.CLIENT_ACCESS_KEY, "INVALID_KEY");
        connectionAttributes.put(AWS3Attributes.CLIENT_SECRET, "INVALID_SECRET");
        connectionAttributes.put(AWS3Attributes.CLIENT_REGION, "us-east-1");

        when(mockProviderFactory.create(any(AWS3Attributes.class))).thenReturn(mockProvider);
        when(mockProvider.getS3Client()).thenReturn(mockS3Client);
        when(mockS3Client.getS3AccountOwner()).thenThrow(new AmazonServiceException("Invalid credentials"));

        // Act
        extension.validateConnection(connectionAttributes);

        // Assert - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateConnection_WhenS3ClientThrowsException_ThrowsIllegalArgumentException() {
        // Arrange
        Map<String, Object> connectionAttributes = new HashMap<>();
        connectionAttributes.put(AWS3Attributes.CLIENT_ACCESS_KEY, "REDACTED_SECRET");
        connectionAttributes.put(AWS3Attributes.CLIENT_SECRET, "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        connectionAttributes.put(AWS3Attributes.CLIENT_REGION, "us-east-1");

        when(mockProviderFactory.create(any(AWS3Attributes.class))).thenReturn(mockProvider);
        when(mockProvider.getS3Client()).thenThrow(new RuntimeException("Connection failed"));

        // Act
        extension.validateConnection(connectionAttributes);

        // Assert - exception expected
    }

    @Test
    public void testTestConnection_WhenValidCredentials_CompletesSuccessfully() {
        // Arrange
        Owner mockOwner = new Owner();
        mockOwner.setDisplayName("test-owner");

        when(mockProviderFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getS3Client()).thenReturn(mockS3Client);
        when(mockS3Client.getS3AccountOwner()).thenReturn(mockOwner);

        // Act
        extension.testConnection();

        // Assert
        verify(mockProviderFactory).create();
        verify(mockProvider).getS3Client();
        verify(mockS3Client).getS3AccountOwner();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTestConnection_WhenInvalidCredentials_ThrowsIllegalArgumentException() {
        // Arrange
        when(mockProviderFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getS3Client()).thenReturn(mockS3Client);
        when(mockS3Client.getS3AccountOwner()).thenThrow(new AmazonServiceException("Invalid credentials"));

        // Act
        extension.testConnection();

        // Assert - exception expected
    }

    @Test
    public void testAttributesUpdated_WhenNewAttributes_UpdatesAttributes() {
        // Arrange
        Map<String, Object> oldAttributes = new HashMap<>();
        oldAttributes.put(AWS3Attributes.CLIENT_ACCESS_KEY, "OLD_KEY");
        oldAttributes.put(AWS3Attributes.CLIENT_SECRET, "OLD_SECRET");
        oldAttributes.put(AWS3Attributes.CLIENT_REGION, "us-east-1");

        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(AWS3Attributes.CLIENT_ACCESS_KEY, "NEW_KEY");
        newAttributes.put(AWS3Attributes.CLIENT_SECRET, "NEW_SECRET");
        newAttributes.put(AWS3Attributes.CLIENT_REGION, "us-west-2");

        // Act
        extension.attributesUpdated(oldAttributes, newAttributes);

        // Assert
        verify(mockAttributes).update(newAttributes);
    }

    @Test
    public void testAttributesUpdated_WhenEmptyAttributes_UpdatesWithEmptyMap() {
        // Arrange
        Map<String, Object> oldAttributes = new HashMap<>();
        Map<String, Object> newAttributes = new HashMap<>();

        // Act
        extension.attributesUpdated(oldAttributes, newAttributes);

        // Assert
        verify(mockAttributes).update(newAttributes);
    }

    @Test
    public void testValidateConnection_WhenExceptionContainsMessage_IncludesMessageInError() {
        // Arrange
        Map<String, Object> connectionAttributes = new HashMap<>();
        connectionAttributes.put(AWS3Attributes.CLIENT_ACCESS_KEY, "INVALID_KEY");
        connectionAttributes.put(AWS3Attributes.CLIENT_SECRET, "INVALID_SECRET");
        connectionAttributes.put(AWS3Attributes.CLIENT_REGION, "invalid-region");

        String exceptionMessage = "The security token included in the request is invalid";
        when(mockProviderFactory.create(any(AWS3Attributes.class))).thenReturn(mockProvider);
        when(mockProvider.getS3Client()).thenReturn(mockS3Client);
        when(mockS3Client.getS3AccountOwner()).thenThrow(new AmazonServiceException(exceptionMessage));

        // Act & Assert
        try {
            extension.validateConnection(connectionAttributes);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unable to connect to AWS S3"));
            assertTrue(e.getMessage().contains(exceptionMessage));
        }
    }

    @Test
    public void testTestConnection_WhenExceptionOccurs_IncludesMessageInError() {
        // Arrange
        String exceptionMessage = "Network timeout";
        when(mockProviderFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getS3Client()).thenReturn(mockS3Client);
        when(mockS3Client.getS3AccountOwner()).thenThrow(new RuntimeException(exceptionMessage));

        // Act & Assert
        try {
            extension.testConnection();
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unable to connect to AWS S3"));
            assertTrue(e.getMessage().contains(exceptionMessage));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTestConnection_WhenProviderFactoryThrowsException_ThrowsIllegalArgumentException() {
        // Arrange
        when(mockProviderFactory.create()).thenThrow(new RuntimeException("Factory error"));

        // Act
        extension.testConnection();

        // Assert - exception expected
    }

    @Test
    public void testValidateConnection_WhenNullAttributesMap_CreatesAttributesAndValidates() {
        // Arrange
        Map<String, Object> connectionAttributes = new HashMap<>();
        connectionAttributes.put(AWS3Attributes.CLIENT_ACCESS_KEY, null);
        connectionAttributes.put(AWS3Attributes.CLIENT_SECRET, null);
        connectionAttributes.put(AWS3Attributes.CLIENT_REGION, null);

        Owner mockOwner = new Owner();
        mockOwner.setDisplayName("test-owner");

        when(mockProviderFactory.create(any(AWS3Attributes.class))).thenReturn(mockProvider);
        when(mockProvider.getS3Client()).thenReturn(mockS3Client);
        when(mockS3Client.getS3AccountOwner()).thenReturn(mockOwner);

        // Act
        extension.validateConnection(connectionAttributes);

        // Assert
        verify(mockProviderFactory).create(any(AWS3Attributes.class));
        verify(mockProvider).getS3Client();
        verify(mockS3Client).getS3AccountOwner();
    }

    @Test
    public void testCustomTabs_CalledMultipleTimes_ReturnsConsistentResult() {
        // Act
        Map<String, String> tabs1 = extension.customTabs();
        Map<String, String> tabs2 = extension.customTabs();

        // Assert
        assertEquals(tabs1, tabs2);
        assertEquals("static/docs", tabs1.get("Documentation"));
        assertEquals("static/docs", tabs2.get("Documentation"));
    }

    @Test
    public void testAttributesUpdated_WithNullOldAttributes_UpdatesSuccessfully() {
        // Arrange
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(AWS3Attributes.CLIENT_ACCESS_KEY, "NEW_KEY");
        newAttributes.put(AWS3Attributes.CLIENT_SECRET, "NEW_SECRET");
        newAttributes.put(AWS3Attributes.CLIENT_REGION, "us-west-2");

        // Act
        extension.attributesUpdated(null, newAttributes);

        // Assert
        verify(mockAttributes).update(newAttributes);
    }
}

