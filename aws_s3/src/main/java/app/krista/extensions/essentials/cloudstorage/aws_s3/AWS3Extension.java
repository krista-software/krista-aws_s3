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

import app.krista.extension.impl.anno.*;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3Provider;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3ProviderFactory;
import com.amazonaws.services.s3.AmazonS3;

import javax.inject.Inject;
import java.util.Map;

@Field.Text(value = AWS3Attributes.CLIENT_ACCESS_KEY)
@Field.Text(value = AWS3Attributes.CLIENT_SECRET, isSecured = true)
@Field.Text(value = AWS3Attributes.CLIENT_REGION, isSecured = true)
@Extension(version = "2.0.6", name = "AWS S3")
@Java(version = Java.Version.JAVA_21)
@StaticResource(path = "docs", file = "docs")
public final class AWS3Extension {

    private final AWS3Attributes aws3Attributes;
    private final S3ProviderFactory providerFactory;

    @Inject
    public AWS3Extension(AWS3Attributes aws3Attributes, S3ProviderFactory providerFactory) {
        this.aws3Attributes = aws3Attributes;
        this.providerFactory = providerFactory;
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateConnection(Map<String, Object> connectionAttributes) {
        AWS3Attributes attributes = AWS3Attributes.create(connectionAttributes);
        testConnection(attributes);
    }

    private void testConnection(AWS3Attributes attributes) {
        try {
            S3Provider s3Provider = attributes != null ? providerFactory.create(attributes) : providerFactory.create();
            AmazonS3 client = s3Provider.getS3Client();
            client.getS3AccountOwner();
        } catch (Exception cause) {
            throw new IllegalArgumentException("Unable to connect to AWS S3. Please verify your Access Key, Secret Key, and Region are correct. Error details: " + cause.getMessage());
        }
    }


    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        testConnection(null);
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
    public void attributesUpdated(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {
        aws3Attributes.update(newAttributes);
    }
}

