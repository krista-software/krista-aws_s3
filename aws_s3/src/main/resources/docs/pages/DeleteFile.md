# Delete File

## Overview

The **Delete File** catalog request permanently removes a file from an Amazon S3 bucket. This operation cannot be undone
unless versioning is enabled on the bucket.

## Request Details

- **Area**: File Operations
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No (Deletion operations are not retryable to prevent accidental multiple deletions)

## Input Parameters

| Parameter Name | Type | Required | Description                                   | Example                  |
|----------------|------|----------|-----------------------------------------------|--------------------------|
| Bucket Name    | Text | Yes      | The name of the S3 bucket containing the file | `my-documents-bucket`    |
| File Name      | Text | Yes      | The key (name) of the file to delete          | `reports/old-report.pdf` |

### Parameter Details

#### Bucket Name

- Must be an existing S3 bucket in your AWS account
- Bucket must be in the region specified in the extension configuration
- Case-sensitive
- Must follow S3 bucket naming rules

#### File Name (File Key)

- Must be an existing file in the specified bucket
- Case-sensitive
- Can include folder structure (e.g., `documents/2024/report.pdf`)
- Maximum length: 1024 bytes

## Output Parameters

| Parameter Name | Type | Description                                     |
|----------------|------|-------------------------------------------------|
| Response       | Text | Success message indicating the file was deleted |

### Output Format

The output is a text message in the format:

```
[file-name] deleted successfully from [bucket-name]
```

Example: `reports/old-report.pdf deleted successfully from my-documents-bucket`

## Validation Rules

| Validation                       | Error Message                                                                                                         | Resolution                                    |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------|-----------------------------------------------|
| Bucket name is null or empty     | "Bucket name is required. Please provide a valid bucket name and try again."                                          | Provide a valid bucket name                   |
| File name (key) is null or empty | "File name (key) is required. Please provide a valid file name and try again."                                        | Provide a valid file name/key                 |
| File doesn't exist in bucket     | "File '[file-key]' does not exist in bucket '[bucket-name]'. Please verify the file name and bucket, then try again." | Verify file exists before attempting deletion |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing input parameters

**Common Scenarios**:

- Bucket name is null or empty
- File name (key) is null or empty

**Error Messages**:

- "Operation fail due to bucket name is null or empty."
- "Operation fail due to file Key is null or empty."

**Resolution**:

1. Verify all required parameters are provided
2. Ensure bucket name and file name are not empty strings

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic violations

**Common Scenarios**:

- File doesn't exist in the specified bucket
- Attempting to delete from a non-existent bucket

**Error Messages**:

- "Provided file key is not present in bucket : [file-key]"
- "File '[file-key]' does not exist in bucket '[bucket-name]'."

**Resolution**:

1. Verify file exists using "List File from Bucket" request before deletion
2. Check bucket exists in AWS Console
3. Confirm bucket is in the configured region
4. Verify file key spelling and case

### System Errors (SYSTEM_ERROR)

**Cause**: AWS service errors or network issues

**Common Scenarios**:

- Network connectivity issues
- AWS service unavailable
- Timeout during deletion

**Error Messages**:

- AmazonServiceException with specific error details

**Resolution**:

1. Check network connectivity
2. Verify AWS service status
3. Retry the operation after a brief delay
4. Review application logs for detailed error information

### Authorization Errors

**Cause**: Insufficient permissions or invalid credentials

**Common Scenarios**:

- IAM user lacks `s3:DeleteObject` permission
- Invalid Access Key or Secret Key
- Bucket policy restricts deletion

**Error Messages**:

- "Access Denied"
- "Invalid AWS Credentials Provided"

**Resolution**:

1. Verify IAM user has `s3:DeleteObject` permission
2. Check Access Key and Secret Key are correct
3. Review bucket policies for deletion restrictions
4. Ensure IAM user is active and not disabled

## Usage Examples

### Example 1: Delete Old Report

**Input**:

```
Bucket Name: "company-documents"
File Name: "reports/2023/old-report.pdf"
```

**Output**: `reports/2023/old-report.pdf deleted successfully from company-documents`

**Result**: File is permanently removed from the bucket

### Example 2: Delete Temporary File

**Input**:

```
Bucket Name: "temp-storage"
File Name: "temp/processing-file.tmp"
```

## Business Rules

1. **Permanent Deletion**: Files are permanently deleted and cannot be recovered unless bucket versioning is enabled.

2. **File Existence Check**: The operation verifies file existence before attempting deletion.

3. **No Folder Deletion**: This operation only deletes individual files, not folders or multiple files.

4. **Synchronous Operation**: Deletion is synchronous and completes immediately for most files.

5. **No Confirmation**: There is no confirmation prompt; deletion happens immediately when the request is executed.

## Limitations

1. **No Undo**: Deleted files cannot be recovered unless bucket versioning is enabled
2. **Single File Only**: Cannot delete multiple files in one request
3. **No Folder Deletion**: Cannot delete folders or all files with a prefix
4. **No Recycle Bin**: S3 doesn't have a recycle bin; deletion is immediate and permanent
5. **Versioning Required for Recovery**: File recovery requires S3 versioning to be enabled on the bucket

## Best Practices

### 1. Verify Before Deleting

Always verify the file exists and is the correct file before deletion using "List File from Bucket".

### 2. Enable Bucket Versioning

Enable S3 bucket versioning for critical buckets to allow file recovery if needed.

### 3. Implement Confirmation

Implement confirmation steps in your workflows before executing deletion operations.

### 4. Log Deletions

Maintain logs of file deletions for audit and compliance purposes.

### 5. Use Lifecycle Policies

For automated cleanup, consider using S3 lifecycle policies instead of manual deletion.

### 6. Backup Important Files

Ensure important files are backed up before deletion.

### 7. Implement Access Controls

Restrict deletion permissions to authorized users only.

## Common Use Cases

### 1. Cleanup Temporary Files

```
Scenario: Remove temporary files after processing
Action: Delete temporary files from processing bucket
Result: Storage space freed up and costs reduced
```

### 2. Remove Outdated Documents

```
Scenario: Delete old reports or documents
Action: Delete files older than retention period
Result: Compliance with data retention policies
```

### 3. Error Recovery

```
Scenario: Remove corrupted or failed upload files
Action: Delete incomplete or corrupted files
Result: Clean storage state for retry operations
```

### 4. Data Lifecycle Management

```
Scenario: Implement data lifecycle policies
Action: Delete files after specified retention period
Result: Automated data management and cost optimization
```

## Related Catalog Requests

- [Upload File](/pages/UploadFile.md) - Upload files that may later need deletion
- [List File from Bucket](/pages/ListFileFromBucket.md) - Verify files before deletion
- [Download File as URL](/pages/DownloadFileAsUrl.md) - Download files before deletion
- [Download as a File](/pages/DownloadAsAFile.md) - Retrieve files before deletion
- [Copy S3 File](/pages/CopyS3File.md) - Copy files before deletion as backup
- [Move S3 File](/pages/MoveS3File.md) - Move files instead of deleting

## Technical Implementation

### Helper Class

- **Class**: `FileOperationsArea`
- **Package**: `app.krista.extensions.essentials.cloudstorage.aws_s3.catalog`
- **Method**: `deleteFile(String bucketName, String fileName)`

### Validation Flow

1. **Input Validation**:
    - Validates bucket name using `Validator.stringValidator()`
    - Validates file key using `Validator.stringValidator()`
2. **File Existence Check**: Verifies file exists using `doesObjectExist()`
3. **Deletion Execution**: Deletes file using `deleteObject()`
4. **Success Logging**: Logs successful deletion

### Service Delegation

- **Service**: `FileDaoImpl`
- **Method**: `deleteFile(String bucketName, String fileKey)`
- **Returns**: String message confirming deletion

### AWS S3 Operations

- `s3:GetObject` - Check if file exists (via `doesObjectExist`)
- `s3:DeleteObject` - Delete the file from S3

### Error Handling

- Throws RuntimeException if file doesn't exist
- Throws AmazonServiceException for AWS service errors
- Logs errors with detailed information

### Telemetry Metrics

No specific telemetry metrics are tracked for this operation.

## Troubleshooting

### Issue: "File does not exist" Error

**Cause**: Attempting to delete a file that doesn't exist

**Solution**:

1. Use "List File from Bucket" to verify file exists
2. Check file key spelling and case sensitivity
3. Verify you're looking in the correct bucket
4. Confirm bucket is in the configured region

### Issue: Access Denied Error

**Cause**: Insufficient permissions to delete files

**Solution**:

1. Verify IAM user has `s3:DeleteObject` permission
2. Check bucket policy doesn't restrict deletions
3. Ensure IAM user is active
4. Review AWS CloudTrail logs for permission details

### Issue: Deletion Succeeds But File Still Appears

**Cause**: S3 eventual consistency or caching

**Solution**:

1. Wait a few seconds and check again
2. S3 is eventually consistent for some operations
3. Clear any caching layers
4. Verify deletion in AWS Console

### Issue: Cannot Delete File in Use

**Cause**: File may be locked or in use by another process

**Solution**:

1. Ensure no other processes are accessing the file
2. Check for S3 Object Lock if enabled
3. Verify bucket doesn't have retention policies preventing deletion
4. Review bucket configuration for deletion restrictions

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md) - Setup and credentials
- [Authentication](pages/authentication.md) - AWS Signature authentication
- [AWS S3 DeleteObject API](https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteObject.html)
- [AWS S3 Versioning](https://docs.aws.amazon.com/AmazonS3/latest/userguide/Versioning.html)

