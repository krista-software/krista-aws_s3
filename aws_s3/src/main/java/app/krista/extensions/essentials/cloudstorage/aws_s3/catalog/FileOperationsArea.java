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

import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.cloudstorage.aws_s3.catalog.entities.FileDetails;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.FileDaoImpl;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.cloudstorage.aws_s3.impl.util.Validator;
import app.krista.model.base.File;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@Domain(id = "catEntryDomain_b77b40c4-1319-4b32-9b55-07d360453974",
        name = "Cloud Storage",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "7142ded5-a987-4bcc-98fb-933e38b7c176")
public class FileOperationsArea {

    private final FileDaoImpl fileDao;

    private final KristaMediaClient kristaMediaClient;

    @Inject
    public FileOperationsArea(FileDaoImpl fileDao, KristaMediaClient kristaMediaClient) {
        this.fileDao = fileDao;
        this.kristaMediaClient = kristaMediaClient;
    }


    @CatalogRequest(
            id = "localDomainRequest_e2f2e185-f358-4daf-8550-549de9fca0eb",
            name = "Upload File",
            description = "Upload file in specific bucket",
            area = "File Operations",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Uploaded", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public Boolean uploadFile(
            @Field.Text(name = "Bucket Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bucketName,
            @Field.Text(name = "File Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String fileName,
            @Field.File(name = "File", multipleFileUpload = false, required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) app.krista.model.base.File file) throws IOException {
        System.out.println("File extension "+ file.getExtension());
        java.io.File ioFile = kristaMediaClient.toJavaFile(file);
        return fileDao.uploadFile(bucketName, fileName, ioFile);
    }

    @CatalogRequest(
            id = "localDomainRequest_e36cd993-c952-465b-b0a1-510afec71215",
            name = "Download file as url",
            description = "File Downloaded using url",
            area = "File Operations",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "URL", type = "RichText", required = false, attributes = {@Attribute(name = "visualWidth", value = "L")}, options = {})
    public String downloadFileAsLink(
            @Field.Text(name = "Bucket Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bucketName,
            @Field.Text(name = "File Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String fileName) {
        return fileDao.downloadFile(bucketName, fileName);
    }

    @CatalogRequest(
            id = "localDomainRequest_6c5c53be-5d88-40bf-a577-27ddf1ca2d79",
            name = "Download as a file",
            description = "File download from S3 bucket",
            area = "File Operations",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.File(name = "File", multipleFileUpload = false, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public File downloadAsAFile(
            @Field.Text(name = "Bucket Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bucketName,
            @Field.Text(name = "File Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String fileName) {
        String preSignedUrl = fileDao.downloadFile(bucketName, fileName);
        String extractedURL = Validator.extractURLFromAnchorTag(preSignedUrl);
        java.io.File file = fileDao.downloadFileAsLink(extractedURL);
        try {
            return kristaMediaClient.toKristaFile(file);
        } catch (IOException cause) {
            throw new RuntimeException(cause);
        }

    }

    @CatalogRequest(
            id = "localDomainRequest_19abec6a-58fe-4575-9f02-3d70296e4f63",
            name = "Delete File",
            description = "Delete File from S3 bucket",
            area = "File Operations",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Response", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String deleteFile(
            @Field.Text(name = "Bucket Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bucketName,
            @Field.Text(name = "File Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String fileName) {
        return fileDao.deleteFile(bucketName, fileName);
    }


    @CatalogRequest(
            id = "localDomainRequest_1391bd2d-e7cb-47a7-a417-ab8c7c1b3fef",
            name = "List file from bucket",
            description = "List files from S3mbucket",
            area = "File Operations",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "File List", type = "[ Entity(File Details) ]", required = false)
    public List<FileDetails> listFileFromBucket(
            @Field.Text(name = "Bucket Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bucketName) {
        return fileDao.getFileList(bucketName);
    }

    @CatalogRequest(
            id = "localDomainRequest_45536ddb-e238-4042-be3f-491a14aae5cf",
            name = "Copy S3 File",
            description = "Copy S3 File from source bucket to destination bucket",
            area = "File Operations",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Response", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String copyS3File(
            @Field.Text(name = "Source File Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String sourceFileName,
            @Field.Text(name = "Source Bucket Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String sourceBucketName,
            @Field.Text(name = "Destination Bucket Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String destinationBucketName) {
        return fileDao.copyFile(sourceBucketName, sourceFileName, destinationBucketName, sourceFileName);
    }

    @CatalogRequest(
            id = "localDomainRequest_fbb88d0a-acce-4a9d-aeb6-3467ce0f4baf",
            name = "Move S3 File",
            description = "Move S3 file from bucket",
            area = "File Operations",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Response", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String moveS3File(
            @Field.Text(name = "Source Bucket Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String sourceBucketName,
            @Field.Text(name = "Source File Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String sourceFileName,
            @Field.Text(name = "Destination Bucket Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String destinationBucketName) {
        return fileDao.moveFile(sourceBucketName, sourceFileName, destinationBucketName);
    }

    @CatalogRequest(
            id = "localDomainRequest_b1eeb7cb-1af2-4d0f-bd8b-7ba5a865cab7",
            name = "Rename S3 File",
            description = "Rename S3 file",
            area = "File Operations",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Response", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public String renameS3File(
            @Field.Text(name = "Bucket Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String bucketName,
            @Field.Text(name = "Old File Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String oldFileName,
            @Field.Text(name = "New File Name", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String newFileName) {
        return fileDao.renameFile(bucketName, oldFileName, newFileName);
    }

}
