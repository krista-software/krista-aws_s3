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

package app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.stores;

import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extension.util.EntityStore;
import app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.CatalogTypes;
import app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.entities.StorageBucket;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3Provider;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3ProviderFactory;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.util.Constants;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.*;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StorageBucketStore implements EntityStore<StorageBucket> {

    private S3Provider provider;


    @Inject
    public StorageBucketStore(S3ProviderFactory providerFactory) {
        provider = providerFactory.create();
    }

    /**
     * @param map input map with {@link StorageBucket} entity fields.
     * @return {@link StorageBucket} for the given input
     */
    @Override
    public StorageBucket create(Map<String, Object> map) throws IOException {
        try {
            String bucketName = (String) map.get("Name");
            if (provider.getS3Client().doesBucketExistV2(bucketName.toLowerCase())) {
                throw new IllegalArgumentException("Bucket '" + bucketName + "' already exists. Please choose a different bucket name and try again.");
            } else {
                return createNewBucket(map);
            }
        } catch (SdkClientException cause) {
            throw new RuntimeException("Unable to create bucket. Please verify your AWS credentials and permissions, then try again. Error details: " + cause.getMessage());
        }
    }

    private StorageBucket createNewBucket(Map<String, Object> map) {
        String bucketName = (String) map.get("Name");
        Bucket awsBucket = provider.getS3Client().createBucket(bucketName.toLowerCase());
        Owner owner = new Owner();
        owner.setDisplayName((String) map.get("Owner"));
        awsBucket.setOwner(owner);
        awsBucket.setCreationDate(new Date());
        return CatalogTypes.toKristaBucket(awsBucket);
    }

    /**
     * @param bucketName Primary Key of the {@link StorageBucket}
     * @return {@link StorageBucket} for the given bucket Name.
     * @throws IOException if there is any interruption during execution of Amazon S3 Api.
     */
    @Override
    public StorageBucket get(String bucketName) throws IOException {
        try {
            Bucket named_bucket = null;
            List<Bucket> buckets = provider.getS3Client().listBuckets();
            for (Bucket bucket : buckets) {
                if (bucket.getName().equals(bucketName.toLowerCase())) {
                    named_bucket = bucket;
                    return CatalogTypes.toKristaBucket(named_bucket);
                }
            }
            throw new IOException("Bucket '" + bucketName + "' was not found. Please verify the bucket name is correct and exists in your AWS account.");
        } catch (AmazonS3Exception cause) {
            if (cause.getStatusCode() == Constants.CONFLICT) {
                throw new IOException("A conflict occurred while accessing bucket '" + bucketName + "'. The bucket may be in use by another operation. Please try again in a moment.");
            } else {
                throw new IOException("Unable to access bucket '" + bucketName + "'. Please verify your AWS credentials and permissions. Error details: " + cause.getMessage(), cause);
            }
        }
    }


    @Override
    public StorageBucket update(StorageBucket storageBucket) {
        return null;
    }


    @Override
    public void delete(String bucketName) {
        try {
            ListObjectsV2Request listRequest = new ListObjectsV2Request()
                    .withBucketName(bucketName);

            ListObjectsV2Result result = provider.getS3Client().listObjectsV2(listRequest);

            if (result.getObjectSummaries().isEmpty()) {
                provider.getS3Client().deleteBucket(bucketName);
            } else {
                throw new IllegalArgumentException("Bucket '" + bucketName + "' is not empty and cannot be deleted. Please delete all files in the bucket first, then try again.");
            }
        } catch (AmazonS3Exception cause) {
            throw new AmazonS3Exception("Unable to delete bucket '" + bucketName + "'. Please verify your AWS credentials and permissions. Error details: " + cause.getMessage());
        } catch (Exception cause) {
            throw new AmazonS3Exception("An unexpected error occurred while deleting bucket '" + bucketName + "'. Please try again or contact support if the issue persists.");

        }
    }


    @Override
    public boolean contains(String bucketName) {
        return false;
    }


    @Deprecated
    @Override
    public List<StorageBucket> search(List<SearchCondition> conditions, long pageIndex, int pageSize) {
        return Collections.emptyList();
    }

    /**
     * @param searchQuery list of search conditions and query clauses given as input
     * @param pageIndex   page number of the query results
     * @param pageSize    number of results per page
     * @return returns {@link List<StorageBucket>} as per the given query
     * @throws Exception if there is any error in the given input
     */
    @Override
    public List<StorageBucket> search(SearchQuery searchQuery, long pageIndex, int pageSize) {

        List<StorageBucket> bucketList = CatalogTypes.toKristaBuckets(provider.getS3Client().listBuckets());

        try {
            if (searchQuery != null && searchQuery.getSearchConditions() != null) {

                List<SearchCondition> searchConditions = searchQuery.getSearchConditions();
                for (SearchCondition searchCondition : searchConditions) {
                    if (searchCondition.getFieldName().equals("Name")) {
                        return bucketList.stream().
                                filter(bucket -> bucket.name.contains((CharSequence) searchCondition.
                                        getOperand())).collect(Collectors.toList());
                    }
                }
            }
            if (bucketList.size() == 1) {
                return provider.getS3Client().listBuckets().stream()
                        .map(CatalogTypes::toKristaBucket)
                        .skip(Math.max((pageIndex - 1) * pageSize, 0))
                        .limit(pageSize)
                        .collect(Collectors.toList());
            } else {
                return bucketList;
            }
        } catch (Exception cause) {
            System.err.println("An error occurred: " + cause.getMessage());
            return Collections.emptyList();
        }
    }


    @Override
    public long count(List<SearchCondition> list) {
        throw new UnsupportedOperationException("This method is not supported");
    }

    @Override
    public List<String> lookup(Map<String, Object> map, List<String> list) {
        throw new UnsupportedOperationException("This method is not supported");
    }

}
