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
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

@Service
public class S3ProviderFactory {

    private final AWS3Attributes attributes;

    @Inject
    public S3ProviderFactory(AWS3Attributes attributes) {
        this.attributes = attributes;
    }

    public S3Provider create(AWS3Attributes attributes) {
        return new S3Provider(attributes);
    }

    public S3Provider create() {
        return new S3Provider(attributes);
    }
}
