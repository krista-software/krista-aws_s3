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

package catalog;

import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.FileDaoImpl;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3Provider;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3ProviderFactory;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URISyntaxException;

import static app.krista.extensions.essentials.cloudstorage.aws_s3.impl.FileDaoImpl.URL_GENERATION_FAILED;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class FileOperationsAreaTest {

    S3Provider provider;
    S3ProviderFactory providerFactory;
    AmazonS3 awsS3;
    FileDaoImpl fileImpl;

    java.io.File nullFile;

    File validFile;
    File nonExistentFile;

    @Before
    public void setUp() throws URISyntaxException {
        provider = Mockito.mock(S3Provider.class);
        providerFactory = Mockito.mock(S3ProviderFactory.class);
        awsS3 = Mockito.mock(AmazonS3.class);
        when(providerFactory.create()).thenReturn(provider);
        when(provider.getS3Client()).thenReturn(awsS3);
        fileImpl = new FileDaoImpl(providerFactory);
        validFile = new java.io.File(ClassLoader.getSystemClassLoader().getResource("validFile.txt").toURI());
        nonExistentFile = new java.io.File("nonExistentFile.txt");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsValidationException() {
        Assert.assertFalse(fileImpl.uploadFile("", "validFile.txt", validFile));
        Assert.assertFalse(fileImpl.uploadFile("myBucket", "", validFile));
        Assert.assertFalse(fileImpl.uploadFile("myBucket", "validFile.txt", nullFile));
        Assert.assertFalse(fileImpl.uploadFile("myBucket", "nonExistentFile.txt", nonExistentFile));
        Assert.assertFalse(fileImpl.uploadFile("myBucket", "validFile.txt", validFile));
    }



    @Test
    public void testDownloadFileObjectNotFound() {
        Mockito.when(provider.getS3Client().getObject("my-bucket", "non-existent.txt")).thenReturn(null);

        String result = fileImpl.downloadFile("my-bucket", "non-existent.txt");

        assertEquals("Unable to generate download URL. Please check your inputs and try again.", result);
    }



    @Test
    public void testDownloadFileNoObjectFound() {
        when(provider.getS3Client().getObject("valid-bucket", "non-existent-file.txt")).thenReturn(null);

        String expectedOutput = URL_GENERATION_FAILED;
        String result = fileImpl.downloadFile("valid-bucket", "non-existent-file.txt");

        assertEquals(expectedOutput, result);
    }



}













