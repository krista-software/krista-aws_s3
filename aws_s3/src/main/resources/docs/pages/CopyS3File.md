# Copy S3 File

## Overview

The **Copy S3 File** catalog request copies a file from one S3 bucket to another, or within the same bucket with a
different name. The original file remains unchanged.

## Request Details

- **Area**: File Operations
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No (Copy operations are not retryable)

## Input Parameters

| Parameter Name          | Type | Required | Description                                          | Example                   |
|-------------------------|------|----------|------------------------------------------------------|---------------------------|
| Source File Name        | Text | Yes      | The key (name) of the source file to copy            | `reports/2024/report.pdf` |
| Source Bucket Name      | Text | Yes      | The name of the bucket containing the source file    | `source-bucket`           |
| Destination Bucket Name | Text | Yes      | The name of the bucket where the file will be copied | `destination-bucket`      |

### Parameter Details

#### Source File Name (File Key)

- Must be an existing file in the source bucket
- Case-sensitive
- Can include folder structure (e.g., `documents/2024/report.pdf`)
- Maximum length: 1024 bytes

#### Source Bucket Name

- Must be an existing S3 bucket in your AWS account
- Bucket must be in the region specified in the extension configuration
- Case-sensitive
- Must follow S3 bucket naming rules

#### Destination Bucket Name

- Must be an existing S3 bucket in your AWS account
- Can be the same as source bucket (for renaming within bucket)
- Bucket must be in the region specified in the extension configuration
- Case-sensitive
- Must follow S3 bucket naming rules

## Output Parameters

| Parameter Name | Type | Description                                    |
|----------------|------|------------------------------------------------|
| Response       | Text | Success message indicating the file was copied |

### Output Format

The output is a text message in the format:

```
File copied successfully
```

## Validation Rules

| Validation                               | Error Message                                                                                                                                                        | Resolution                                   |
|------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------|
| Source file name is null or empty        | "Operation fail due to file Key is null or empty."                                                                                                                   | Provide a valid source file name             |
| Source bucket name is null or empty      | "Operation fail due to bucket name is null or empty."                                                                                                                | Provide a valid source bucket name           |
| Destination bucket name is null or empty | "Operation fail due to bucket name is null or empty."                                                                                                                | Provide a valid destination bucket name      |
| Source file doesn't exist                | "Provided file key is not present in bucket: [file-key]"                                                                                                             | Verify source file exists                    |
| Same bucket copy without metadata change | "Cannot copy file to itself without changes. To copy a file within the same bucket, please provide a different destination file name or modify the file's metadata." | Use different destination or change metadata |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing input parameters

**Common Scenarios**:

- Source file name is null or empty
- Source bucket name is null or empty
- Destination bucket name is null or empty

**Error Messages**:

- "File name (key) is required. Please provide a valid file name and try again."
- "Bucket name is required. Please provide a valid bucket name and try again."

**Resolution**:

1. Verify all required parameters are provided
2. Ensure all parameters are not empty strings

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic violations

**Common Scenarios**:

- Source file doesn't exist in the source bucket
- Attempting to copy file to itself without metadata changes
- Source or destination bucket doesn't exist

**Error Messages**:

- "File '[file-key]' was not found. Please verify the file name is correct and exists in the bucket, then try again."
- "Cannot copy file to itself without changes. To copy a file within the same bucket, please provide a different
  destination file name or modify the file's metadata."

**Resolution**:

1. Verify source file exists using "List File from Bucket" request
2. For same-bucket copies, ensure destination name is different
3. Check both buckets exist in AWS Console
4. Confirm buckets are in the configured region

### System Errors (SYSTEM_ERROR)

**Cause**: AWS service errors or network issues

**Common Scenarios**:

- Network connectivity issues during copy
- AWS service unavailable
- Timeout during large file copy
- Insufficient storage quota

**Error Messages**:

- AmazonServiceException with specific error details

**Resolution**:

1. Check network connectivity
2. Verify AWS service status
3. Retry the operation after a brief delay
4. For large files, ensure sufficient timeout
5. Review application logs for detailed error information

### Authorization Errors

**Cause**: Insufficient permissions or invalid credentials

**Common Scenarios**:

- IAM user lacks `s3:GetObject` permission on source
- IAM user lacks `s3:PutObject` permission on destination
- Invalid Access Key or Secret Key
- Bucket policies restrict copying

**Error Messages**:

- "Access Denied"
- "Invalid AWS Credentials Provided"

**Resolution**:

1. Verify IAM user has `s3:GetObject` permission on source bucket
2. Verify IAM user has `s3:PutObject` permission on destination bucket
3. Check Access Key and Secret Key are correct
4. Review bucket policies for access restrictions
5. Ensure IAM user is active and not disabled

## Usage Examples

### Example 1: Copy File Between Buckets

**Input**:

```
Source File Name: "reports/2024/Q1-report.pdf"
Source Bucket Name: "company-documents"


### Example 3: Copy File to Different Folder Structure

**Input**:
```

Source File Name: "temp/processing.csv"
Source Bucket Name: "data-bucket"
Destination Bucket Name: "data-bucket"

```

**Output**: `File copied successfully`

**Result**: File is copied within the same bucket (implementation handles same-bucket copy by updating metadata)

## Business Rules

1. **Original File Preserved**: The source file remains unchanged after the copy operation.

2. **Same-Bucket Copy Handling**: When copying within the same bucket, the implementation updates metadata to comply with S3 requirements.

3. **File Existence Check**: The operation verifies the source file exists before attempting to copy.

4. **Metadata Preservation**: File metadata (content type, storage class, etc.) is preserved in the copy.

5. **Synchronous Operation**: Copy is synchronous and may take time for large files.

6. **Destination Overwrite**: If a file with the same name exists in the destination, it will be overwritten.

## Limitations

1. **Large File Performance**: Very large files may take significant time to copy
2. **Same Region Only**: Both buckets must be in the same AWS region (as configured)
3. **No Progress Tracking**: Cannot track copy progress for large files
4. **No Partial Copy**: Cannot copy portions of a file
5. **Metadata Update Required**: Same-bucket copies require metadata update
6. **No Batch Copy**: Cannot copy multiple files in one request

## Best Practices

### 1. Verify Source File Exists
Always verify the source file exists before attempting to copy to avoid errors.

### 2. Check Destination Space
Ensure the destination bucket has sufficient storage quota for the file.

### 3. Use for Backup
Use copy operations to create backups before modifying or deleting files.

### 4. Consider Cross-Region Replication
For cross-region copies, consider using S3 Cross-Region Replication instead.

### 5. Monitor Large File Copies
Be aware of timeout implications when copying very large files.

### 6. Implement Error Handling
Always implement proper error handling to manage copy failures gracefully.

### 7. Verify Permissions
Ensure IAM user has both read permissions on source and write permissions on destination.

## Common Use Cases

### 1. File Backup
```

Scenario: Create backup copies of important files
Action: Copy files from production bucket to backup bucket
Result: Files safely backed up in separate bucket

```

### 2. Data Migration
```

Scenario: Migrate files between buckets for reorganization
Action: Copy files to new bucket structure
Result: Files available in new organization structure

```

### 3. File Duplication
```

Scenario: Create duplicate files for different processing workflows
Action: Copy file within same bucket with different name
Result: Multiple copies available for different purposes

```

### 4. Archive Creation
```

Scenario: Archive old files to long-term storage bucket
Action: Copy files to archive bucket with different storage class
Result: Files archived for long-term retention

```

## Related Catalog Requests

- [Upload File](UploadFile.md) - Upload files that can later be copied
- [Download File as URL](DownloadFileAsUrl.md) - Download files before copying
- [List File from Bucket](ListFileFromBucket.md) - Verify files before copying
- [Delete File](DeleteFile.md) - Delete original after copying (for move operation)
- [Move S3 File](MoveS3File.md) - Move files (copy + delete)
- [Rename S3 File](RenameS3File.md) - Rename files (copy + delete within bucket)

## Technical Implementation

### Helper Class
- **Class**: `FileOperationsArea`
- **Package**: `app.krista.extensions.essentials.cloudstorage.aws_s3.catalog`
- **Method**: `copyS3File(String sourceFileName, String sourceBucketName, String destinationBucketName)`

### Validation Flow
1. **Input Validation**:
   - Validates source file name using `Validator.stringValidator()`
   - Validates source bucket name using `Validator.stringValidator()`
   - Validates destination bucket name using `Validator.stringValidator()`
2. **File Existence Check**: Verifies source file exists
3. **Same-Bucket Detection**: Checks if source and destination buckets are the same
4. **Copy Execution**: Executes copy with appropriate metadata handling

### Service Delegation
- **Service**: `FileDaoImpl`
- **Method**: `copyFile(String sourceFileName, String sourceBucketName, String destinationBucketName)`
- **Returns**: String message "File copied successfully"

### AWS S3 Operations
- `s3:GetObject` - Verify source file exists
- `s3:PutObject` - Write file to destination bucket
- `s3:CopyObject` - Copy file between buckets

### Same-Bucket Copy Handling
When source and destination buckets are the same:
1. Creates new `ObjectMetadata` with updated metadata
2. Sets `Content-Type` to force metadata change
3. Uses `copyObject()` with new metadata to comply with S3 requirements

### Error Handling
- Throws RuntimeException if source file doesn't exist
- Throws AmazonServiceException for AWS service errors
- Logs errors with detailed information

### Telemetry Metrics
No specific telemetry metrics are tracked for this operation.

## Troubleshooting

### Issue: "Illegal Copy Request" Error

**Cause**: Attempting to copy file to itself without metadata changes

**Solution**:
1. Ensure destination file name is different from source when copying within same bucket
2. The implementation handles this by updating metadata, but verify parameters are correct
3. Check that you're not accidentally using the same name for source and destination

### Issue: Source File Not Found

**Cause**: Source file doesn't exist in the specified bucket

**Solution**:
1. Use "List File from Bucket" to verify source file exists
2. Check source file name spelling and case sensitivity
3. Verify source bucket name is correct
4. Confirm bucket is in the configured region

### Issue: Access Denied on Copy

**Cause**: Insufficient permissions on source or destination

**Solution**:
1. Verify IAM user has `s3:GetObject` permission on source bucket
2. Verify IAM user has `s3:PutObject` permission on destination bucket
3. Check bucket policies for both source and destination
4. Ensure IAM user is active

### Issue: Copy Operation Times Out

**Cause**: File is too large and copy takes too long

**Solution**:
1. Increase timeout settings if possible
2. For very large files, consider using AWS CLI or SDK directly
3. Monitor AWS service status for any issues
4. Verify network connectivity is stable

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and credentials
- [Authentication](Authentication.md) - AWS Signature authentication
- [AWS S3 CopyObject API](https://docs.aws.amazon.com/AmazonS3/latest/API/API_CopyObject.html)
- [AWS S3 Cross-Region Replication](https://docs.aws.amazon.com/AmazonS3/latest/userguide/replication.html)

