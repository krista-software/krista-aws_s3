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

package app.krista.extensions.essentials.cloudstorage.aws_s3.impl.util;

import app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.entities.StorageBucket;

import java.time.Duration;

public final class Constants {

    private Constants() {
    }

    public static final long EXPIRATION_TIME = Duration.ofDays(7).toMillis();
    public static final String TEMP_DIR = "/tmp/krista/extension/s3/";

    public static final int CONFLICT = 409;

    public static final String BUCKET_CREATION_FAILED = "Bucket name is required. Please provide a valid bucket name and try again.";

    public static final String EMPTY_FILE_KEY = "File name (key) is required. Please provide a valid file name and try again.";
    public static final String EMPTY_File = "File is required. Please provide a valid file that exists and try again.";

    public static final StorageBucket EMPTY_BUCKET = new StorageBucket();

}
