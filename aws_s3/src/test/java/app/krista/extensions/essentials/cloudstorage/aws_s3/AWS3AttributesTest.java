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

import app.krista.extension.executor.Invoker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AWS3Attributes class.
 * Tests all constructors, getters, and update methods.
 */
public class AWS3AttributesTest {

    private static final String TEST_ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE";
    private static final String TEST_SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
    private static final String TEST_REGION = "us-east-1";

    private Map<String, Object> testAttributes;

    @Before
    public void setUp() {
        testAttributes = new HashMap<>();
        testAttributes.put(AWS3Attributes.CLIENT_ACCESS_KEY, TEST_ACCESS_KEY);
        testAttributes.put(AWS3Attributes.CLIENT_SECRET, TEST_SECRET_KEY);
        testAttributes.put(AWS3Attributes.CLIENT_REGION, TEST_REGION);
    }

    @Test
    public void testConstructorWithMapAttributes_WhenValidAttributes_SetsAllFields() {
        // Act
        AWS3Attributes attributes = new AWS3Attributes(testAttributes);

        // Assert
        assertEquals(TEST_ACCESS_KEY, attributes.getAccessKey());
        assertEquals(TEST_SECRET_KEY, attributes.getSecretKey());
        assertEquals(TEST_REGION, attributes.getRegion());
    }

    @Test
    public void testConstructorWithMapAttributes_WhenNullAttributes_SetsNullFields() {
        // Arrange
        Map<String, Object> nullAttributes = new HashMap<>();

        // Act
        AWS3Attributes attributes = new AWS3Attributes(nullAttributes);

        // Assert
        assertNull(attributes.getAccessKey());
        assertNull(attributes.getSecretKey());
        assertNull(attributes.getRegion());
    }

    @Test
    public void testConstructorWithStrings_WhenValidStrings_SetsAllFields() {
        // Act
        AWS3Attributes attributes = new AWS3Attributes(TEST_ACCESS_KEY, TEST_SECRET_KEY, TEST_REGION);

        // Assert
        assertEquals(TEST_ACCESS_KEY, attributes.getAccessKey());
        assertEquals(TEST_SECRET_KEY, attributes.getSecretKey());
        assertEquals(TEST_REGION, attributes.getRegion());
    }

    @Test
    public void testConstructorWithStrings_WhenNullStrings_SetsNullFields() {
        // Act
        AWS3Attributes attributes = new AWS3Attributes(null, null, null);

        // Assert
        assertNull(attributes.getAccessKey());
        assertNull(attributes.getSecretKey());
        assertNull(attributes.getRegion());
    }

    @Test
    public void testConstructorWithInvoker_WhenValidInvoker_SetsAllFields() {
        // Arrange
        Invoker mockInvoker = Mockito.mock(Invoker.class);
        when(mockInvoker.getAttributes()).thenReturn(testAttributes);

        // Act
        AWS3Attributes attributes = new AWS3Attributes(mockInvoker);

        // Assert
        assertEquals(TEST_ACCESS_KEY, attributes.getAccessKey());
        assertEquals(TEST_SECRET_KEY, attributes.getSecretKey());
        assertEquals(TEST_REGION, attributes.getRegion());
    }

    @Test
    public void testCreateStaticMethod_WhenValidAttributes_ReturnsNewInstance() {
        // Act
        AWS3Attributes attributes = AWS3Attributes.create(testAttributes);

        // Assert
        assertNotNull(attributes);
        assertEquals(TEST_ACCESS_KEY, attributes.getAccessKey());
        assertEquals(TEST_SECRET_KEY, attributes.getSecretKey());
        assertEquals(TEST_REGION, attributes.getRegion());
    }

    @Test
    public void testUpdate_WhenNewAttributes_UpdatesAllFields() {
        // Arrange
        AWS3Attributes attributes = new AWS3Attributes(TEST_ACCESS_KEY, TEST_SECRET_KEY, TEST_REGION);
        Map<String, Object> newAttributes = new HashMap<>();
        String newAccessKey = "NEWAKIAIOSFODNN7EXAMPLE";
        String newSecretKey = "NEWwJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";
        String newRegion = "us-west-2";
        newAttributes.put(AWS3Attributes.CLIENT_ACCESS_KEY, newAccessKey);
        newAttributes.put(AWS3Attributes.CLIENT_SECRET, newSecretKey);
        newAttributes.put(AWS3Attributes.CLIENT_REGION, newRegion);

        // Act
        attributes.update(newAttributes);

        // Assert
        assertEquals(newAccessKey, attributes.getAccessKey());
        assertEquals(newSecretKey, attributes.getSecretKey());
        assertEquals(newRegion, attributes.getRegion());
    }

    @Test
    public void testUpdate_WhenNullAttributes_UpdatesToNull() {
        // Arrange
        AWS3Attributes attributes = new AWS3Attributes(TEST_ACCESS_KEY, TEST_SECRET_KEY, TEST_REGION);
        Map<String, Object> nullAttributes = new HashMap<>();

        // Act
        attributes.update(nullAttributes);

        // Assert
        assertNull(attributes.getAccessKey());
        assertNull(attributes.getSecretKey());
        assertNull(attributes.getRegion());
    }

    @Test
    public void testConstants_HaveCorrectValues() {
        // Assert
        assertEquals("accessKey", AWS3Attributes.CLIENT_ACCESS_KEY);
        assertEquals("secretKey", AWS3Attributes.CLIENT_SECRET);
        assertEquals("region", AWS3Attributes.CLIENT_REGION);
    }
}

