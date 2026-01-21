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

package app.krista.extensions.essentials.cloudstorage.aws_s3.impl;

import app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.entities.FileDetails;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3Provider;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.connector.S3ProviderFactory;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.util.Constants;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.util.DownloadFileFromS3;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.util.Validator;
import app.krista.extensions.essentials.cloudstorage.aws_s3.service.FileDao;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.*;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class FileDaoImpl implements FileDao {

    public static final String URL_GENERATION_FAILED = "Unable to generate download URL. Please check your inputs and try again.";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileDaoImpl.class);
    private final S3Provider provider;

    @Inject
    public FileDaoImpl(S3ProviderFactory providerFactory) {
        provider = providerFactory.create();
    }

    @Override
    public boolean uploadFile(String bucketName, String fileKey, java.io.File file) {
        try {
            Validator.stringValidator(bucketName, Constants.BUCKET_CREATION_FAILED);
            Validator.stringValidator(fileKey, Constants.EMPTY_FILE_KEY);
            Validator.fileValidator(file, Constants.EMPTY_File);
            S3Object oldObject = provider.getS3Client().getObject(bucketName, fileKey);
            if (oldObject.getKey() != null) {
                throw new RuntimeException("File '" + fileKey + "' already exists in bucket '" + bucketName + "'. Please use a different file name or delete the existing file first.");
            }
        } catch (AmazonS3Exception cause) {
            if (cause.getStatusCode() == 404) {
                if (provider.getS3Client().listBuckets().stream().anyMatch(bucket -> bucket.getName().equals(bucketName))) {
                    provider.getS3Client().putObject(new PutObjectRequest(bucketName, fileKey, file));
                    return true;
                }
            }
            handleS3Exception(bucketName, fileKey, cause);
        }
        return false;
    }

    @Override
    public String downloadFile(String bucketName, String fileKey) {
        Validator.stringValidator(bucketName, Constants.BUCKET_CREATION_FAILED);
        Validator.stringValidator(fileKey, Constants.EMPTY_FILE_KEY);
        try {
            S3Object object = provider.getS3Client().getObject(bucketName, fileKey);
            if (object != null) {
                URL url = provider.getS3Client().generatePresignedUrl(linkGenerator(bucketName, fileKey));
                return "<a href=\"" + url + "\">" + fileKey + "</a>";
            }
        } catch (SdkClientException cause) {
            if (cause instanceof AmazonS3Exception) {
                handleS3Exception(bucketName, fileKey, (AmazonS3Exception) cause);
            }
            LOGGER.error("Unable to generate download URL. Please verify that the bucket name '" + bucketName + "' and file name '" + fileKey + "' are correct and exist in your AWS S3 account.", cause);
            throw new SdkClientException("Unable to generate download URL. Please verify that the bucket name '" + bucketName + "' and file name '" + fileKey + "' are correct and exist in your AWS S3 account.", cause);
        }
        return "Unable to generate download URL. Please check your inputs and try again.";
    }


    @Override
    public String copyFile(String sourceBucketName, String sourceFileName, String destinationBucketName, String destinationFileName) {
        Validator.stringValidator(sourceBucketName, Constants.BUCKET_CREATION_FAILED);
        Validator.stringValidator(sourceFileName, Constants.EMPTY_FILE_KEY);
        Validator.stringValidator(destinationBucketName, Constants.BUCKET_CREATION_FAILED);
        Validator.stringValidator(destinationFileName, Constants.EMPTY_FILE_KEY);
        try {
            if (sourceBucketName.equals(destinationBucketName)) {
                return copyObjectWithUpdatedMetadata(sourceBucketName, sourceFileName);
            }
            provider.getS3Client().copyObject(sourceBucketName, sourceFileName, destinationBucketName, destinationFileName);
            LOGGER.info("File copied successfully");
            return "File copied successfully";
        } catch (AmazonServiceException cause) {
            if (cause instanceof AmazonS3Exception) {
                handleS3Exception(sourceBucketName, sourceFileName, (AmazonS3Exception) cause);
            }
            LOGGER.error("Error occurred while copying file :{}", cause.getMessage());
            throw new AmazonServiceException(cause.getMessage(), cause);
        }
    }

    @Override
    public String moveFile(String sourceBucketName, String sourceFileName, String destinationBucketName) {
        Validator.stringValidator(sourceBucketName, Constants.BUCKET_CREATION_FAILED);
        Validator.stringValidator(sourceFileName, Constants.EMPTY_FILE_KEY);
        Validator.stringValidator(destinationBucketName, Constants.BUCKET_CREATION_FAILED);
        try {
            provider.getS3Client().copyObject(sourceBucketName, sourceFileName, destinationBucketName, sourceFileName);
            provider.getS3Client().deleteObject(sourceBucketName, sourceFileName);
            LOGGER.info("File moved successfully");
            return "File moved successfully";
        } catch (AmazonServiceException cause) {
            if (cause instanceof AmazonS3Exception) {
                handleS3Exception(sourceBucketName, sourceFileName, (AmazonS3Exception) cause);
            }
            LOGGER.error("Error occurred while moving file:{}", cause.getMessage());
            throw new AmazonServiceException(cause.getMessage(), cause);
        }
    }

    @Override
    public String renameFile(String bucketName, String oldFileName, String newFileName) {
        Validator.stringValidator(bucketName, Constants.BUCKET_CREATION_FAILED);
        Validator.stringValidator(oldFileName, Constants.EMPTY_FILE_KEY);
        Validator.stringValidator(newFileName, Constants.EMPTY_FILE_KEY);

        try {
            provider.getS3Client().copyObject(bucketName, oldFileName, bucketName, newFileName);
            provider.getS3Client().deleteObject(bucketName, oldFileName);
            LOGGER.info("File renamed successfully");
            return "File renamed successfully";
        } catch (AmazonServiceException cause) {
            if (cause instanceof AmazonS3Exception) {
                handleS3Exception(bucketName, oldFileName, (AmazonS3Exception) cause);
            }
            LOGGER.error("Error occurred while renaming file:{}", cause.getMessage());
            throw new AmazonServiceException(cause.getMessage(), cause);
        }
    }

    public GeneratePresignedUrlRequest linkGenerator(String bucketName, String fileKey) {
        try {
            Date expiration = new Date();
            long expTimeMillis = expiration.getTime() + Constants.EXPIRATION_TIME;
            expiration.setTime(expTimeMillis);
            return new GeneratePresignedUrlRequest(bucketName, fileKey)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
        } catch (Exception cause) {
            LOGGER.error("Unable to generate pre-signed URL for file '{}' in bucket '{}': {}", fileKey, bucketName, cause.getMessage(), cause);
            throw new RuntimeException("Unable to generate pre-signed URL for file '" + fileKey + "' in bucket '" + bucketName + "'. Please verify your AWS credentials and permissions.");
        }
    }


    public File downloadFileAsLink(String presignedURL) {
        try {
            return DownloadFileFromS3.downloadFile(presignedURL);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    @Override
    public String deleteFile(String bucketName, String fileKey) {
        Validator.stringValidator(bucketName, Constants.BUCKET_CREATION_FAILED);
        Validator.stringValidator(fileKey, Constants.EMPTY_FILE_KEY);
        try {
            if (!provider.getS3Client().doesObjectExist(bucketName, fileKey)) {
                LOGGER.error("File '{}' does not exist in bucket '{}'.", fileKey, bucketName);
                throw new RuntimeException("File '" + fileKey + "' does not exist in bucket '" + bucketName + "'. Please verify the file name and bucket, then try again.");
            }
            provider.getS3Client().deleteObject(bucketName, fileKey);
            LOGGER.info("File '{}' deleted successfully from bucket '{}'.", fileKey, bucketName);
            return "File '" + fileKey + "' has been successfully deleted from bucket '" + bucketName + "'.";
        } catch (AmazonServiceException cause) {
            LOGGER.error("Error occurred while deleting file:{}", cause.getMessage());
            throw new AmazonServiceException(cause.getMessage(), cause);
        }
    }

    @Override
    public List<FileDetails> getFileList(String bucketName) {
        try {
            ObjectListing objectListing = provider.getS3Client().listObjects(bucketName);
            List<FileDetails> fileDetailsList = new ArrayList<>();
            List<S3ObjectSummary> objects = objectListing.getObjectSummaries();

            for (S3ObjectSummary objectSummary : objects) {
                if (!objectSummary.getKey().endsWith("/")) {
                    FileDetails fileObject = new FileDetails();
                    fileObject.fileName = objectSummary.getKey();
                    fileObject.bucketName = objectSummary.getBucketName();
                    fileObject.tag = objectSummary.getETag();
                    fileObject.size = objectSummary.getSize();
                    fileObject.lastModified = objectSummary.getLastModified().getTime();
                    fileObject.storageClass = objectSummary.getStorageClass();
                    fileObject.owner = getOwner(objectSummary.getBucketName(), objectSummary.getKey());
                    fileDetailsList.add(fileObject);
                }
            }
            return fileDetailsList;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while listing files from bucket :{}", cause.getMessage());
            handleS3Exception(bucketName, null, (AmazonS3Exception) cause);
        }
        return List.of();
    }

    public String getOwner(String bucketName, String fileKey) {
        AccessControlList acl = provider.getS3Client().getObjectAcl(bucketName, fileKey);
        Owner owner = acl.getOwner();
        return owner.getDisplayName();

    }


    private void handleS3Exception(String bucketName, String fileKey, AmazonS3Exception cause) {
        String specificMessage;

        switch (cause.getStatusCode()) {
            case 404:
                specificMessage = handleNotFound(cause, fileKey);
                break;
            case 400:
                specificMessage = handleBadRequest(cause, bucketName);
                break;
            default:
                specificMessage = "An unexpected error occurred while accessing bucket '" + bucketName + "' and file '" + fileKey + "'. Please check your AWS credentials, permissions, and network connection, then try again.";
                logAndThrow(specificMessage, cause);
                return;
        }
        if (specificMessage != null) LOGGER.error(specificMessage);
    }

    private String handleNotFound(AmazonS3Exception cause, String fileKey) {
        if ("NoSuchKey".equals(cause.getErrorCode())) {
            String message = "File '" + fileKey + "' was not found. Please verify the file name is correct and exists in the bucket, then try again.";
            LOGGER.error(message);
            throw new RuntimeException(message, cause);
        } else if ("NoSuchBucket".equals(cause.getErrorCode())) {
            String invalidBucket = getInvalidBucketFromXML(cause.getErrorResponseXml());
            String message = "Bucket '" + invalidBucket + "' does not exist. Please verify the bucket name is correct and exists in your AWS account, then try again.";
            LOGGER.error(message);
            throw new RuntimeException(message, cause);
        }
        return null;
    }

    private String handleBadRequest(AmazonS3Exception cause, String bucketName) {
        if ("InvalidRequest".equals(cause.getErrorCode())) {
            String message = "Cannot copy file to itself without changes. To copy a file within the same bucket, please provide a different destination file name or modify the file's metadata.";
            LOGGER.error("Error occurred while copying file: {}", cause.getMessage());
            throw new RuntimeException(message, cause);
        } else if ("InvalidBucketName".equals(cause.getErrorCode())) {
            String message = "Bucket name '" + bucketName + "' is invalid. AWS S3 bucket names must be 3-63 characters long, contain only lowercase letters, numbers, hyphens, and periods. Please use a valid bucket name.";
            LOGGER.error(message);
            throw new RuntimeException(message, cause);
        }
        return null;
    }

    private void logAndThrow(String errorMessage, AmazonS3Exception cause) {
        LOGGER.error(errorMessage + ". Error: {}", cause.getMessage());
        throw new RuntimeException(errorMessage, cause);
    }


    public String getInvalidBucketFromXML(String xmlResponse) {
        Pattern pattern = Pattern.compile("<BucketName>(.*?)</BucketName>");
        Matcher matcher = pattern.matcher(xmlResponse);
        if (matcher.find()) {
            return matcher.group(1);
        } else return null;
    }

    public String copyObjectWithUpdatedMetadata(String bucketName, String objectKey) {
        CopyObjectRequest copyRequest = new CopyObjectRequest(bucketName, objectKey, bucketName, objectKey);
        ObjectMetadata newMetadata = new ObjectMetadata();
        newMetadata.addUserMetadata("copied", "true");
        copyRequest.setNewObjectMetadata(newMetadata);
        provider.getS3Client().copyObject(copyRequest);
        LOGGER.info("File copied successfully");
        return "File copied successfully";
    }

}
