# Move S3 File

## Overview

The **Move S3 File** catalog request moves a file from one S3 bucket to another by copying it to the destination and
then deleting the original. This is a two-step operation that results in the file existing only in the destination
bucket.

## Request Details

- **Area**: File Operations
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No (Move operations are not retryable to prevent data loss)

## Input Parameters

| Parameter Name          | Type | Required | Description                                         | Example                   |
|-------------------------|------|----------|-----------------------------------------------------|---------------------------|
| Source Bucket Name      | Text | Yes      | The name of the bucket containing the source file   | `source-bucket`           |
| Source File Name        | Text | Yes      | The key (name) of the source file to move           | `reports/2024/report.pdf` |
| Destination Bucket Name | Text | Yes      | The name of the bucket where the file will be moved | `destination-bucket`      |

### Parameter Details

#### Source Bucket Name

- Must be an existing S3 bucket in your AWS account
- Bucket must be in the region specified in the extension configuration
- Case-sensitive
- Must follow S3 bucket naming rules

#### Source File Name (File Key)

- Must be an existing file in the source bucket
- Case-sensitive
- Can include folder structure (e.g., `documents/2024/report.pdf`)
- Maximum length: 1024 bytes

#### Destination Bucket Name

- Must be an existing S3 bucket in your AWS account
- Should be different from source bucket (for same-bucket moves, use "Rename S3 File")
- Bucket must be in the region specified in the extension configuration
- Case-sensitive
- Must follow S3 bucket naming rules

## Output Parameters

| Parameter Name | Type | Description                                   |
|----------------|------|-----------------------------------------------|
| Response       | Text | Success message indicating the file was moved |

### Output Format

The output is a text message:

```
File moved successfully
```

## Validation Rules

| Validation                               | Error Message                                            | Resolution                              |
|------------------------------------------|----------------------------------------------------------|-----------------------------------------|
| Source bucket name is null or empty      | "Operation fail due to bucket name is null or empty."    | Provide a valid source bucket name      |
| Source file name is null or empty        | "Operation fail due to file Key is null or empty."       | Provide a valid source file name        |
| Destination bucket name is null or empty | "Operation fail due to bucket name is null or empty."    | Provide a valid destination bucket name |
| Source file doesn't exist                | "Provided file key is not present in bucket: [file-key]" | Verify source file exists               |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing input parameters

**Common Scenarios**:

- Source bucket name is null or empty
- Source file name is null or empty
- Destination bucket name is null or empty

**Error Messages**:

- "Bucket name is required. Please provide a valid bucket name and try again."
- "File name (key) is required. Please provide a valid file name and try again."

**Resolution**:

1. Verify all required parameters are provided
2. Ensure all parameters are not empty strings

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic violations

**Common Scenarios**:

- Source file doesn't exist in the source bucket
- Source or destination bucket doesn't exist
- Copy succeeds but delete fails (file exists in both locations)

**Error Messages**:

- "File '[file-key]' was not found. Please verify the file name is correct and exists in the bucket, then try again."
- "File '[file-key]' does not exist in bucket '[bucket-name]'. Please verify the file name and bucket, then try again."

**Resolution**:

1. Verify source file exists using "List File from Bucket" request
2. Check both buckets exist in AWS Console
3. Confirm buckets are in the configured region
4. If copy succeeded but delete failed, manually delete the source file

### System Errors (SYSTEM_ERROR)

**Cause**: AWS service errors or network issues

**Common Scenarios**:

- Network connectivity issues during copy or delete
- AWS service unavailable
- Timeout during large file copy
- Partial failure (copy succeeds, delete fails)

**Error Messages**:

- AmazonServiceException with specific error details

**Resolution**:

1. Check network connectivity
2. Verify AWS service status
3. Retry the operation after a brief delay
4. If partial failure occurs, verify file state in both buckets
5. Review application logs for detailed error information

### Authorization Errors

**Cause**: Insufficient permissions or invalid credentials

**Common Scenarios**:

- IAM user lacks `s3:GetObject` permission on source
- IAM user lacks `s3:PutObject` permission on destination
- IAM user lacks `s3:DeleteObject` permission on source
- Invalid Access Key or Secret Key

**Error Messages**:

- "Access Denied"
- "Invalid AWS Credentials Provided"

**Resolution**:

1. Verify IAM user has `s3:GetObject` permission on source bucket
2. Verify IAM user has `s3:PutObject` permission on destination bucket
3. Verify IAM user has `s3:DeleteObject` permission on source bucket
4. Check Access Key and Secret Key are correct
5. Review bucket policies for access restrictions

## Usage Examples

### Example 1: Move File to Archive Bucket

**Input**:

```
Source Bucket Name: "active-documents"
Source File Name: "reports/2023/old-report.pdf"
Destination Bucket Name: "archive-bucket"


### Example 2: Move File Between Project Buckets

**Input**:
```

Source Bucket Name: "project-a-files"
Source File Name: "shared/data.csv"
Destination Bucket Name: "project-b-files"

```

**Output**: `File moved successfully`

**Result**: File is transferred from project-a-files to project-b-files

### Example 3: Move File to Different Folder Structure

**Input**:
```

Source Bucket Name: "temp-storage"
Source File Name: "uploads/temp-file.pdf"
Destination Bucket Name: "permanent-storage"

```

**Output**: `File moved successfully`

**Result**: File is moved from temporary to permanent storage

## Business Rules

1. **Two-Step Operation**: Move is implemented as copy followed by delete.

2. **Original File Deleted**: The source file is permanently deleted after successful copy.

3. **Atomic Failure**: If copy fails, the original file remains unchanged.

4. **Partial Failure Risk**: If copy succeeds but delete fails, file exists in both locations.

5. **File Existence Check**: The operation verifies the source file exists before attempting to move.

6. **Destination Overwrite**: If a file with the same name exists in the destination, it will be overwritten.

## Limitations

1. **Not Atomic**: Move is not atomic; partial failures can occur
2. **Large File Performance**: Very large files may take significant time to move
3. **Same Region Only**: Both buckets must be in the same AWS region (as configured)
4. **No Progress Tracking**: Cannot track move progress for large files
5. **No Rollback**: If delete fails after copy, manual cleanup is required
6. **No Batch Move**: Cannot move multiple files in one request

## Best Practices

### 1. Verify Source File Exists
Always verify the source file exists before attempting to move to avoid errors.

### 2. Backup Important Files
For critical files, create a backup before moving in case of partial failure.

### 3. Verify Destination Space
Ensure the destination bucket has sufficient storage quota for the file.

### 4. Monitor for Partial Failures
Implement monitoring to detect cases where copy succeeds but delete fails.

### 5. Use Copy for Safety
If you need to preserve the original, use "Copy S3 File" instead of move.

### 6. Implement Error Handling
Always implement proper error handling to manage move failures gracefully.

### 7. Verify Permissions
Ensure IAM user has read/delete permissions on source and write permissions on destination.

## Common Use Cases

### 1. Archive Old Files
```

Scenario: Move old files to archive bucket for long-term storage
Action: Move files from active bucket to archive bucket
Result: Files archived and removed from active storage

```

### 2. Reorganize Storage
```

Scenario: Reorganize files between buckets for better organization
Action: Move files to appropriate buckets based on category
Result: Files organized in correct buckets

```

### 3. Promote Files
```

Scenario: Move files from staging to production bucket
Action: Move validated files from staging to production
Result: Files available in production, removed from staging

```

### 4. Cleanup Temporary Storage
```

Scenario: Move processed files from temp to permanent storage
Action: Move files after processing completes
Result: Temp storage cleaned up, files preserved permanently

```

## Related Catalog Requests

- [Copy S3 File](CopyS3File.md) - Copy files without deleting original
- [Delete File](DeleteFile.md) - Delete files (used internally by move)
- [Rename S3 File](RenameS3File.md) - Rename files within same bucket
- [Upload File](UploadFile.md) - Upload files that may later be moved
- [List File from Bucket](ListFileFromBucket.md) - Verify files before moving
- [Download File as URL](DownloadFileAsUrl.md) - Download files before moving

## Technical Implementation

### Helper Class
- **Class**: `FileOperationsArea`
- **Package**: `app.krista.extensions.essentials.cloudstorage.aws_s3.catalog`
- **Method**: `moveS3File(String sourceBucketName, String sourceFileName, String destinationBucketName)`

### Validation Flow
1. **Input Validation**:
   - Validates source bucket name using `Validator.stringValidator()`
   - Validates source file name using `Validator.stringValidator()`
   - Validates destination bucket name using `Validator.stringValidator()`
2. **File Existence Check**: Verifies source file exists
3. **Copy Operation**: Copies file to destination bucket
4. **Delete Operation**: Deletes original file from source bucket

### Service Delegation
- **Service**: `FileDaoImpl`
- **Method**: `moveFile(String sourceBucketName, String sourceFileName, String destinationBucketName)`
- **Returns**: String message "File moved successfully"

### AWS S3 Operations
- `s3:GetObject` - Verify source file exists
- `s3:PutObject` - Write file to destination bucket
- `s3:CopyObject` - Copy file to destination
- `s3:DeleteObject` - Delete file from source bucket

### Operation Sequence
1. Validate all input parameters
2. Verify source file exists using `doesObjectExist()`
3. Copy file using `copyObject()` or `copyFile()` method
4. Delete source file using `deleteObject()`
5. Return success message

### Error Handling
- Throws RuntimeException if source file doesn't exist
- Throws AmazonServiceException for AWS service errors
- Logs errors with detailed information
- **Note**: If copy succeeds but delete fails, file will exist in both locations

### Telemetry Metrics
No specific telemetry metrics are tracked for this operation.

## Troubleshooting

### Issue: File Exists in Both Locations After Move

**Cause**: Copy succeeded but delete failed (partial failure)

**Solution**:
1. Verify file exists in destination bucket
2. Manually delete file from source bucket using "Delete File" request
3. Review error logs to understand why delete failed
4. Check IAM user has `s3:DeleteObject` permission on source bucket

### Issue: Source File Not Found

**Cause**: Source file doesn't exist in the specified bucket

**Solution**:
1. Use "List File from Bucket" to verify source file exists
2. Check source file name spelling and case sensitivity
3. Verify source bucket name is correct
4. Confirm bucket is in the configured region

### Issue: Access Denied on Move

**Cause**: Insufficient permissions on source or destination

**Solution**:
1. Verify IAM user has `s3:GetObject` permission on source bucket
2. Verify IAM user has `s3:PutObject` permission on destination bucket
3. Verify IAM user has `s3:DeleteObject` permission on source bucket
4. Check bucket policies for both source and destination
5. Ensure IAM user is active

### Issue: Move Operation Times Out

**Cause**: File is too large and copy takes too long

**Solution**:
1. Increase timeout settings if possible
2. For very large files, consider using AWS CLI or SDK directly
3. Monitor AWS service status for any issues
4. Verify network connectivity is stable
5. Consider using "Copy S3 File" followed by manual "Delete File"

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and credentials
- [Authentication](Authentication.md) - AWS Signature authentication
- [AWS S3 CopyObject API](https://docs.aws.amazon.com/AmazonS3/latest/API/API_CopyObject.html)
- [AWS S3 DeleteObject API](https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteObject.html)

