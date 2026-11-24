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

package app.krista.extensions.essentials.cloudstorage.aws_s3.service;

import app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.entities.FileDetails;

import java.util.List;

public interface FileDao {

    /**
     * This function upload file in given bucket
     *
     * @param bucketName Name of bucket.
     * @param fileKey    Path where file is going to be uploaded.
     * @param file       File to be uploaded.
     * @return {@link String}
     */
    boolean uploadFile(String bucketName, String fileKey, java.io.File file);

    /**
     * This function return downloadable link
     *
     * @param bucketName Name of bucket.
     * @param fileKey    path of file in bucket.
     * @return {@link String}
     */
    String downloadFile(String bucketName, String fileKey);

    String copyFile(String sourceBucketName, String sourceFileName, String destinationBucketName, String destinationFileName);

    String moveFile(String sourceBucketName, String sourceFileName, String destinationBucketName);

    String renameFile(String bucketName, String oldFileName, String newFileName);

    String deleteFile(String bucketName, String fileKey);

    List<FileDetails> getFileList(String bucketName);
}
