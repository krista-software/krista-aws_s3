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

import java.util.ArrayList;
import java.util.List;

public class CatalogTypes {

    private CatalogTypes() {}

    public static StorageBucket toKristaBucket(Bucket bucket) {
        if (bucket == null) {
            return Constants.EMPTY_BUCKET;
        }
        StorageBucket storageBucket = new StorageBucket();
        storageBucket.name = bucket.getName();
        storageBucket.owner = String.valueOf(bucket.getOwner().getDisplayName());
        storageBucket.createdDate = bucket.getCreationDate().getTime();
        return storageBucket;
    }

    public static List<StorageBucket> toKristaBuckets(List<Bucket> bucketList) {
        List<StorageBucket> storageBucketList = new ArrayList<>();

        for (Bucket bucket : bucketList) {
            StorageBucket storageBucket = toKristaBucket(bucket);
            storageBucketList.add(storageBucket);
        }
        return storageBucketList;
    }

}
