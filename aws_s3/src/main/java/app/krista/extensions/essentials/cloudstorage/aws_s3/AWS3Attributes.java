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
import app.krista.extension.util.InvokerAttributes;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;

@Service
public class AWS3Attributes {

    public static final String CLIENT_ACCESS_KEY = "accessKey";
    public static final String CLIENT_SECRET = "secretKey";
    public static final String CLIENT_REGION = "region";
    private String accessKey;
    private String secretKey;
    private String region;

    public static AWS3Attributes create(Map<String, Object> attributes) {
        return new AWS3Attributes(attributes);
    }

    @Inject
    public AWS3Attributes(Invoker invoker) {
        this(InvokerAttributes.getStringOrNull(invoker.getAttributes(), CLIENT_ACCESS_KEY), InvokerAttributes.getStringOrNull(invoker.getAttributes(), CLIENT_SECRET), InvokerAttributes.getStringOrNull(invoker.getAttributes(), CLIENT_REGION));
    }

    public AWS3Attributes(Map<String, Object> attributes) {
        this(InvokerAttributes.getStringOrNull(attributes, CLIENT_ACCESS_KEY), InvokerAttributes.getStringOrNull(attributes, CLIENT_SECRET), InvokerAttributes.getStringOrNull(attributes, CLIENT_REGION));
    }

    public AWS3Attributes(String accessKey, String secretKey, String region) {
        this.accessKey = accessKey;
        this.region = region;
        this.secretKey = secretKey;
    }

    public void update(Map<String, Object> newAttributes) {
        accessKey = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_ACCESS_KEY);
        secretKey = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_SECRET);
        region = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_REGION);
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getRegion() {
        return region;
    }

}


