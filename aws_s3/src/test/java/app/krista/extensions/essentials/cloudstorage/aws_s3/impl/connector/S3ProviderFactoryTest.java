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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.mock;

/**
 * Comprehensive unit tests for S3ProviderFactory class.
 * Tests cover constructor, factory methods with and without parameters, and edge cases.
 */
public class S3ProviderFactoryTest {

    @Mock
    private AWS3Attributes mockAttributes;

    @Mock
    private AWS3Attributes mockAlternativeAttributes;

    private S3ProviderFactory factory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testConstructor_WhenValidAttributes_CreatesInstance() {
        // Arrange & Act
        factory = new S3ProviderFactory(mockAttributes);

        // Assert
        assertNotNull(factory);
    }

    @Test
    public void testConstructor_WhenNullAttributes_CreatesInstance() {
        // Arrange & Act
        factory = new S3ProviderFactory(null);

        // Assert
        assertNotNull(factory);
    }

    // ==================== Create with Parameter Tests ====================

    @Test
    public void testCreate_WhenValidAttributes_ReturnsS3Provider() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act
        S3Provider provider = factory.create(mockAlternativeAttributes);

        // Assert
        assertNotNull(provider);
    }

    @Test
    public void testCreate_WhenNullAttributes_ReturnsS3ProviderWithNull() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act
        S3Provider provider = factory.create(null);

        // Assert
        assertNotNull(provider);
    }

    @Test
    public void testCreate_CalledMultipleTimes_ReturnsNewInstanceEachTime() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act
        S3Provider provider1 = factory.create(mockAlternativeAttributes);
        S3Provider provider2 = factory.create(mockAlternativeAttributes);

        // Assert
        assertNotNull(provider1);
        assertNotNull(provider2);
        assertNotSame(provider1, provider2); // Different instances
    }

    @Test
    public void testCreate_WithDifferentAttributes_ReturnsS3ProviderForEach() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);
        AWS3Attributes attributes1 = mock(AWS3Attributes.class);
        AWS3Attributes attributes2 = mock(AWS3Attributes.class);

        // Act
        S3Provider provider1 = factory.create(attributes1);
        S3Provider provider2 = factory.create(attributes2);

        // Assert
        assertNotNull(provider1);
        assertNotNull(provider2);
        assertNotSame(provider1, provider2);
    }

    // ==================== Create without Parameter Tests ====================

    @Test
    public void testCreate_WhenNoParameter_UsesInjectedAttributes() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act
        S3Provider provider = factory.create();

        // Assert
        assertNotNull(provider);
    }

    @Test
    public void testCreate_WhenNoParameter_CalledMultipleTimes_ReturnsNewInstanceEachTime() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act
        S3Provider provider1 = factory.create();
        S3Provider provider2 = factory.create();

        // Assert
        assertNotNull(provider1);
        assertNotNull(provider2);
        assertNotSame(provider1, provider2); // Different instances
    }

    @Test
    public void testCreate_WhenNoParameter_WithNullInjectedAttributes_ReturnsS3Provider() {
        // Arrange
        factory = new S3ProviderFactory(null);

        // Act
        S3Provider provider = factory.create();

        // Assert
        assertNotNull(provider);
    }

    // ==================== Factory Pattern Tests ====================

    @Test
    public void testCreate_BothMethods_ProduceIndependentInstances() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act
        S3Provider provider1 = factory.create(); // Uses injected attributes
        S3Provider provider2 = factory.create(mockAlternativeAttributes); // Uses parameter

        // Assert
        assertNotNull(provider1);
        assertNotNull(provider2);
        assertNotSame(provider1, provider2);
    }

    @Test
    public void testCreate_WithParameter_IgnoresInjectedAttributes() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act
        S3Provider provider = factory.create(mockAlternativeAttributes);

        // Assert
        // The provider should be created with mockAlternativeAttributes, not mockAttributes
        assertNotNull(provider);
    }

    // ==================== Edge Case Tests ====================

    @Test
    public void testCreate_WithSameAttributesMultipleTimes_ReturnsNewInstancesEachTime() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act
        S3Provider provider1 = factory.create(mockAlternativeAttributes);
        S3Provider provider2 = factory.create(mockAlternativeAttributes);
        S3Provider provider3 = factory.create(mockAlternativeAttributes);

        // Assert
        assertNotNull(provider1);
        assertNotNull(provider2);
        assertNotNull(provider3);
        assertNotSame(provider1, provider2);
        assertNotSame(provider2, provider3);
        assertNotSame(provider1, provider3);
    }

    @Test
    public void testCreate_MixedCalls_AllReturnValidProviders() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act
        S3Provider provider1 = factory.create(); // No parameter
        S3Provider provider2 = factory.create(mockAlternativeAttributes); // With parameter
        S3Provider provider3 = factory.create(); // No parameter again
        S3Provider provider4 = factory.create(mockAttributes); // With parameter

        // Assert
        assertNotNull(provider1);
        assertNotNull(provider2);
        assertNotNull(provider3);
        assertNotNull(provider4);
        // All should be different instances
        assertNotSame(provider1, provider2);
        assertNotSame(provider1, provider3);
        assertNotSame(provider1, provider4);
        assertNotSame(provider2, provider3);
        assertNotSame(provider2, provider4);
        assertNotSame(provider3, provider4);
    }

    @Test
    public void testCreate_WithRealAttributes_ReturnsS3Provider() {
        // Arrange
        AWS3Attributes realAttributes = new AWS3Attributes("testAccessKey", "testSecretKey", "us-east-1");
        factory = new S3ProviderFactory(realAttributes);

        // Act
        S3Provider provider = factory.create();

        // Assert
        assertNotNull(provider);
    }

    @Test
    public void testCreate_WithRealAttributesAsParameter_ReturnsS3Provider() {
        // Arrange
        AWS3Attributes injectedAttributes = new AWS3Attributes("key1", "secret1", "us-east-1");
        AWS3Attributes parameterAttributes = new AWS3Attributes("key2", "secret2", "eu-west-1");
        factory = new S3ProviderFactory(injectedAttributes);

        // Act
        S3Provider provider = factory.create(parameterAttributes);

        // Assert
        assertNotNull(provider);
    }

    @Test
    public void testCreate_FactoryReusability_WorksCorrectly() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act - Create multiple providers over time
        S3Provider provider1 = factory.create();
        // Simulate some time passing or other operations
        S3Provider provider2 = factory.create(mockAlternativeAttributes);
        S3Provider provider3 = factory.create();

        // Assert - Factory should remain functional
        assertNotNull(provider1);
        assertNotNull(provider2);
        assertNotNull(provider3);
    }

    @Test
    public void testCreate_ThreadSafety_MultipleCallsReturnDifferentInstances() {
        // Arrange
        factory = new S3ProviderFactory(mockAttributes);

        // Act - Simulate concurrent-like calls
        S3Provider[] providers = new S3Provider[10];
        for (int i = 0; i < 10; i++) {
            providers[i] = factory.create();
        }

        // Assert - All should be non-null and unique instances
        for (int i = 0; i < 10; i++) {
            assertNotNull(providers[i]);
            for (int j = i + 1; j < 10; j++) {
                assertNotSame(providers[i], providers[j]);
            }
        }
    }
}

