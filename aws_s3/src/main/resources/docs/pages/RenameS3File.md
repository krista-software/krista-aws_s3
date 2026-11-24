# Rename S3 File

## Overview

The **Rename S3 File** catalog request renames a file within an S3 bucket by copying it with the new name and then
deleting the original. This is a two-step operation that results in the file existing only with the new name.

## Request Details

- **Area**: File Operations
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No (Rename operations are not retryable to prevent data loss)

## Input Parameters

| Parameter Name | Type | Required | Description                                  | Example               |
|----------------|------|----------|----------------------------------------------|-----------------------|
| Bucket Name    | Text | Yes      | The name of the bucket containing the file   | `my-documents-bucket` |
| Old File Name  | Text | Yes      | The current key (name) of the file to rename | `old-report.pdf`      |
| New File Name  | Text | Yes      | The new key (name) for the file              | `new-report.pdf`      |

### Parameter Details

#### Bucket Name

- Must be an existing S3 bucket in your AWS account
- Bucket must be in the region specified in the extension configuration
- Case-sensitive
- Must follow S3 bucket naming rules

#### Old File Name (Current File Key)

- Must be an existing file in the bucket
- Case-sensitive
- Can include folder structure (e.g., `documents/2024/report.pdf`)
- Maximum length: 1024 bytes

#### New File Name (New File Key)

- The desired new name for the file
- Case-sensitive
- Can include folder structure to move file to different folder
- Maximum length: 1024 bytes
- Must be different from old file name

## Output Parameters

| Parameter Name | Type | Description                                     |
|----------------|------|-------------------------------------------------|
| Response       | Text | Success message indicating the file was renamed |

### Output Format

The output is a text message:

```
File renamed successfully
```

## Validation Rules

| Validation                     | Error Message                                            | Resolution                    |
|--------------------------------|----------------------------------------------------------|-------------------------------|
| Bucket name is null or empty   | "Operation fail due to bucket name is null or empty."    | Provide a valid bucket name   |
| Old file name is null or empty | "Operation fail due to file Key is null or empty."       | Provide a valid old file name |
| New file name is null or empty | "Operation fail due to file Key is null or empty."       | Provide a valid new file name |
| Old file doesn't exist         | "Provided file key is not present in bucket: [file-key]" | Verify old file exists        |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing input parameters

**Common Scenarios**:

- Bucket name is null or empty
- Old file name is null or empty
- New file name is null or empty

**Error Messages**:

- "Bucket name is required. Please provide a valid bucket name and try again."
- "File name (key) is required. Please provide a valid file name and try again."

**Resolution**:

1. Verify all required parameters are provided
2. Ensure all parameters are not empty strings

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic violations

**Common Scenarios**:

- Old file doesn't exist in the bucket
- Bucket doesn't exist
- Copy succeeds but delete fails (file exists with both names)

**Error Messages**:

- "File '[file-key]' was not found. Please verify the file name is correct and exists in the bucket, then try again."
- "File '[file-key]' does not exist in bucket '[bucket-name]'. Please verify the file name and bucket, then try again."

**Resolution**:

1. Verify old file exists using "List File from Bucket" request
2. Check bucket exists in AWS Console
3. Confirm bucket is in the configured region
4. If copy succeeded but delete failed, manually delete the old file

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
4. If partial failure occurs, verify file state in bucket
5. Review application logs for detailed error information

### Authorization Errors

**Cause**: Insufficient permissions or invalid credentials

**Common Scenarios**:

- IAM user lacks `s3:GetObject` permission
- IAM user lacks `s3:PutObject` permission
- IAM user lacks `s3:DeleteObject` permission
- Invalid Access Key or Secret Key

**Error Messages**:

- "Access Denied"
- "Invalid AWS Credentials Provided"

**Resolution**:

1. Verify IAM user has `s3:GetObject` permission on bucket
2. Verify IAM user has `s3:PutObject` permission on bucket
3. Verify IAM user has `s3:DeleteObject` permission on bucket
4. Check Access Key and Secret Key are correct
5. Review bucket policies for access restrictions

## Usage Examples

### Example 1: Simple File Rename

**Input**:

```
Bucket Name: "company-documents"
Old File Name: "draft-report.pdf"
New File Name: "final-report.pdf"


### Example 3: Rename with Extension Change

**Input**:
```

Bucket Name: "data-files"
Old File Name: "report.txt"
New File Name: "report.csv"

```

**Output**: `File renamed successfully`

**Result**: File extension changed from .txt to .csv

## Business Rules

1. **Two-Step Operation**: Rename is implemented as copy followed by delete within the same bucket.

2. **Original File Deleted**: The old file is permanently deleted after successful copy.

3. **Atomic Failure**: If copy fails, the original file remains unchanged.

4. **Partial Failure Risk**: If copy succeeds but delete fails, file exists with both names.

5. **File Existence Check**: The operation verifies the old file exists before attempting to rename.

6. **Destination Overwrite**: If a file with the new name already exists, it will be overwritten.

7. **Folder Movement**: Can be used to move files between folders by changing the path in the new name.

## Limitations

1. **Not Atomic**: Rename is not atomic; partial failures can occur
2. **Large File Performance**: Very large files may take significant time to rename
3. **No Progress Tracking**: Cannot track rename progress for large files
4. **No Rollback**: If delete fails after copy, manual cleanup is required
5. **No Batch Rename**: Cannot rename multiple files in one request
6. **Same Bucket Only**: Can only rename files within the same bucket

## Best Practices

### 1. Verify Old File Exists
Always verify the old file exists before attempting to rename to avoid errors.

### 2. Check New Name Availability
Verify the new file name doesn't already exist if you want to avoid overwriting.

### 3. Backup Important Files
For critical files, create a backup before renaming in case of partial failure.

### 4. Use Meaningful Names
Follow consistent naming conventions for better file organization.

### 5. Monitor for Partial Failures
Implement monitoring to detect cases where copy succeeds but delete fails.

### 6. Implement Error Handling
Always implement proper error handling to manage rename failures gracefully.

### 7. Consider Folder Structure
Use rename to reorganize files into appropriate folder structures.

## Common Use Cases

### 1. Update File Names
```

Scenario: Update file names to follow new naming convention
Action: Rename files with standardized names
Result: Files follow consistent naming pattern

```

### 2. Change File Extensions
```

Scenario: Correct file extension after format conversion
Action: Rename file with correct extension
Result: File has appropriate extension for its format

```

### 3. Reorganize Folder Structure
```

Scenario: Move files to different folders within bucket
Action: Rename files with new folder path
Result: Files organized in correct folder structure

```

### 4. Version Management
```

Scenario: Add version numbers or dates to file names
Action: Rename files to include version information
Result: Files have clear version identification

```

## Related Catalog Requests

- [Copy S3 File](CopyS3File.md) - Copy files (used internally by rename)
- [Delete File](DeleteFile.md) - Delete files (used internally by rename)
- [Move S3 File](MoveS3File.md) - Move files between buckets
- [Upload File](UploadFile.md) - Upload files that may later be renamed
- [List File from Bucket](ListFileFromBucket.md) - Verify files before renaming
- [Download File as URL](DownloadFileAsUrl.md) - Download files before renaming

## Technical Implementation

### Helper Class
- **Class**: `FileOperationsArea`
- **Package**: `app.krista.extensions.essentials.cloudstorage.aws_s3.catalog`
- **Method**: `renameS3File(String bucketName, String oldFileName, String newFileName)`

### Validation Flow
1. **Input Validation**:
   - Validates bucket name using `Validator.stringValidator()`
   - Validates old file name using `Validator.stringValidator()`
   - Validates new file name using `Validator.stringValidator()`
2. **File Existence Check**: Verifies old file exists
3. **Copy Operation**: Copies file to new name within same bucket
4. **Delete Operation**: Deletes old file

### Service Delegation
- **Service**: `FileDaoImpl`
- **Method**: `renameFile(String bucketName, String oldFileName, String newFileName)`
- **Returns**: String message "File renamed successfully"

### AWS S3 Operations
- `s3:GetObject` - Verify old file exists
- `s3:PutObject` - Write file with new name
- `s3:CopyObject` - Copy file to new name
- `s3:DeleteObject` - Delete old file

### Operation Sequence
1. Validate all input parameters
2. Verify old file exists using `doesObjectExist()`
3. Copy file to new name using `copyObject()` with metadata update
4. Delete old file using `deleteObject()`
5. Return success message

### Same-Bucket Copy Handling
Since rename is always within the same bucket:
1. Creates new `ObjectMetadata` with updated metadata
2. Sets `Content-Type` to force metadata change
3. Uses `copyObject()` with new metadata to comply with S3 requirements

### Error Handling
- Throws RuntimeException if old file doesn't exist
- Throws AmazonServiceException for AWS service errors
- Logs errors with detailed information
- **Note**: If copy succeeds but delete fails, file will exist with both names

### Telemetry Metrics
No specific telemetry metrics are tracked for this operation.

## Troubleshooting

### Issue: File Exists with Both Names After Rename

**Cause**: Copy succeeded but delete failed (partial failure)

**Solution**:
1. Verify file exists with new name in bucket
2. Manually delete old file using "Delete File" request
3. Review error logs to understand why delete failed
4. Check IAM user has `s3:DeleteObject` permission on bucket

### Issue: Old File Not Found

**Cause**: Old file doesn't exist in the specified bucket

**Solution**:
1. Use "List File from Bucket" to verify old file exists
2. Check old file name spelling and case sensitivity
3. Verify bucket name is correct
4. Confirm bucket is in the configured region

### Issue: Access Denied on Rename

**Cause**: Insufficient permissions

**Solution**:
1. Verify IAM user has `s3:GetObject` permission on bucket
2. Verify IAM user has `s3:PutObject` permission on bucket
3. Verify IAM user has `s3:DeleteObject` permission on bucket
4. Check bucket policy for access restrictions
5. Ensure IAM user is active

### Issue: Rename Operation Times Out

**Cause**: File is too large and copy takes too long

**Solution**:
1. Increase timeout settings if possible
2. For very large files, consider using AWS CLI or SDK directly
3. Monitor AWS service status for any issues
4. Verify network connectivity is stable
5. Consider using "Copy S3 File" followed by manual "Delete File"

### Issue: New File Name Already Exists

**Cause**: File with new name already exists and was overwritten

**Solution**:
1. This is expected behavior; new file overwrites existing file
2. To prevent overwriting, check file existence before renaming
3. Use "List File from Bucket" to verify new name is available
4. Consider using versioning on the bucket to preserve old versions

## See Also

- [Extension Configuration](ExtensionConfiguration.md) - Setup and credentials
- [Authentication](authentication.md) - AWS Signature authentication
- [AWS S3 CopyObject API](https://docs.aws.amazon.com/AmazonS3/latest/API/API_CopyObject.html)
- [AWS S3 DeleteObject API](https://docs.aws.amazon.com/AmazonS3/latest/API/API_DeleteObject.html)

