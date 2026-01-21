# List File from Bucket

## Overview

The **List File from Bucket** catalog request retrieves a list of all files stored in a specified Amazon S3 bucket,
including detailed metadata for each file.

## Request Details

- **Area**: File Operations
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No (List operations are idempotent but not retryable)

## Input Parameters

| Parameter Name | Type | Required | Description                                  | Example               |
|----------------|------|----------|----------------------------------------------|-----------------------|
| Bucket Name    | Text | Yes      | The name of the S3 bucket to list files from | `my-documents-bucket` |

### Parameter Details

#### Bucket Name

- Must be an existing S3 bucket in your AWS account
- Bucket must be in the region specified in the extension configuration
- Case-sensitive
- Must follow S3 bucket naming rules
- Returns all files in the bucket (no pagination in current implementation)

## Output Parameters

| Parameter Name | Type                         | Description                                                      |
|----------------|------------------------------|------------------------------------------------------------------|
| File List      | List of Entity(File Details) | Array of File Details entities containing metadata for each file |

### File Details Entity Structure

Each file in the list contains the following fields:

| Field Name    | Type       | Description                                                          |
|---------------|------------|----------------------------------------------------------------------|
| File Name     | ProperNoun | The key (name) of the file in S3                                     |
| Size          | Number     | File size in bytes                                                   |
| Bucket Name   | ProperNoun | Name of the bucket containing the file                               |
| Last Modified | Date       | Timestamp when the file was last modified (milliseconds since epoch) |
| Owner         | Person     | Display name of the file owner                                       |
| Tag           | Text       | ETag (entity tag) for the file version                               |
| Is Folder     | Boolean    | Indicates if the entry is a folder (always false for files)          |
| Storage Class | Text       | S3 storage class (e.g., STANDARD, GLACIER)                           |

## Validation Rules

| Validation                   | Error Message                                         | Resolution                                |
|------------------------------|-------------------------------------------------------|-------------------------------------------|
| Bucket name is null or empty | "Operation fail due to bucket name is null or empty." | Provide a valid bucket name               |
| Bucket doesn't exist         | "Provided bucket name is not valid: [bucket-name]"    | Verify bucket exists in configured region |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing input parameters

**Common Scenarios**:

- Bucket name is null or empty

**Error Messages**:

- "Operation fail due to bucket name is null or empty."

**Resolution**:

1. Verify bucket name parameter is provided
2. Ensure bucket name is not an empty string

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic violations

**Common Scenarios**:

- Bucket doesn't exist in the AWS account
- Bucket is in a different region than configured

**Error Messages**:

- "Provided bucket name is not valid: [bucket-name]"

**Resolution**:

1. Verify bucket exists in AWS Console
2. Confirm bucket is in the configured region
3. Check bucket name spelling and case

### System Errors (SYSTEM_ERROR)

**Cause**: AWS service errors or network issues

**Common Scenarios**:

- Network connectivity issues
- AWS service unavailable
- Timeout during listing operation

**Resolution**:

1. Check network connectivity
2. Verify AWS service status
3. Retry the operation after a brief delay
4. Review application logs for detailed error information

### Authorization Errors

**Cause**: Insufficient permissions or invalid credentials

**Common Scenarios**:

- IAM user lacks `s3:ListBucket` permission
- Invalid Access Key or Secret Key
- Bucket policy restricts listing

**Error Messages**:

- "Access Denied"
- "Invalid AWS Credentials Provided"

**Resolution**:

1. Verify IAM user has `s3:ListBucket` permission
2. Check Access Key and Secret Key are correct
3. Review bucket policies for access restrictions
4. Ensure IAM user is active and not disabled

## Usage Examples

### Example 1: List All Files in Documents Bucket

**Input**:

```
Bucket Name: "company-documents"
```

**Output**: Array of File Details entities

```json
[
  {
    "fileName": "reports/2024/Q1-report.pdf",
    "size": 2048576,
    "bucketName": "company-documents",
    "lastModified": 1704067200000,
    "owner": "admin-user",
    "tag": "d41d8cd98f00b204e9800998ecf8427e",
    "isFolder": false,
    "storageClass": "STANDARD"
  },
  {
    "fileName": "invoices/invoice-001.pdf",
    "size": 512000,
    "bucketName": "company-documents",
    "lastModified": 1704153600000,
    "owner": "admin-user",
    "tag": "098f6bcd4621d373cade4e832627b4f6",
    #
    #
    #
    Example
    2: List
    Files
    in
    Empty
    Bucket

    **Input**:
```

Bucket Name: "empty-bucket"

```

**Output**: Empty array `[]`

**Result**: No files found in the bucket

### Example 3: List Files with Folder Structure

**Input**:
```

Bucket Name: "organized-storage"

```

**Output**: Array showing files with folder-like structure
```json
[
  {
    "fileName": "2024/january/report.pdf",
    "size": 1024000,
    "bucketName": "organized-storage",
    ...
  },
  {
    "fileName": "2024/february/report.pdf",
    "size": 2048000,
    "bucketName": "organized-storage",
    ...
  }
]
```

**Result**: Files organized with folder prefixes are listed

## Business Rules

1. **All Files Returned**: The operation returns all files in the bucket without pagination.

2. **Folder Filtering**: Entries ending with `/` (folders) are automatically filtered out from the results.

3. **Metadata Included**: Each file includes comprehensive metadata (size, owner, last modified, etc.).

4. **Owner Information**: File owner is retrieved via ACL (Access Control List) for each file.

5. **Sorted Order**: Files are returned in the order provided by S3 (typically alphabetical by key).

6. **No Prefix Filtering**: Returns all files regardless of prefix or folder structure.

## Limitations

1. **No Pagination**: All files are returned in a single request; may be slow for buckets with many files
2. **No Filtering**: Cannot filter by prefix, date, or other criteria
3. **Performance**: Large buckets (thousands of files) may take time to list
4. **No Sorting Options**: Cannot specify custom sort order
5. **Memory Usage**: Large file lists may consume significant memory
6. **ACL Overhead**: Retrieving owner information requires additional API calls per file

## Best Practices

### 1. Use for Small to Medium Buckets

This operation works best for buckets with hundreds to a few thousand files.

### 2. Cache Results When Possible

Cache the file list if you need to reference it multiple times to avoid repeated API calls.

### 3. Filter Results in Workflow

Apply filtering logic in your Krista workflow after retrieving the list.

### 4. Monitor Performance

Be aware of performance implications for buckets with many files.

### 5. Consider Bucket Organization

Organize files with clear naming conventions to make lists more useful.

### 6. Use for Verification

Use this operation to verify file existence before upload, download, or delete operations.

## Common Use Cases

### 1. File Inventory

```
Scenario: Generate inventory of all files in a bucket
Action: List all files and export to report
Result: Complete file inventory with metadata
```

### 2. File Verification

```
Scenario: Verify specific files exist before processing
Action: List files and check for required file names
Result: Confirmation of file presence
```

### 3. Cleanup Operations

```
Scenario: Identify old files for deletion
Action: List files and filter by last modified date
Result: List of files to delete based on age
```

### 4. Storage Analysis

```
Scenario: Analyze storage usage and file distribution
Action: List files and calculate total size by folder
Result: Storage usage report by category
```

## Related Catalog Requests

- [Upload File](/pages/UploadFile.md) - Upload files that will appear in the list
- [Download File as URL](/pages/DownloadFileAsUrl.md) - Download files from the list
- [Download as a File](/pages/DownloadAsAFile.md) - Retrieve files from the list
- [Delete File](/pages/DeleteFile.md) - Delete files identified in the list
- [Copy S3 File](/pages/CopyS3File.md) - Copy files from the list
- [Move S3 File](/pages/MoveS3File.md) - Move files from the list

## Technical Implementation

### Helper Class

- **Class**: `FileOperationsArea`
- **Package**: `app.krista.extensions.essentials.cloudstorage.aws_s3.catalog`
- **Method**: `listFileFromBucket(String bucketName)`

### Validation Flow

1. **Input Validation**: Validates bucket name (implicit via S3 API)
2. **List Objects**: Retrieves all objects from bucket using `listObjects()`
3. **Filter Folders**: Excludes entries ending with `/`
4. **Build File Details**: Creates FileDetails entity for each file
5. **Retrieve Owner**: Gets owner information via ACL for each file

### Service Delegation

- **Service**: `FileDaoImpl`
- **Method**: `getFileList(String bucketName)`
- **Returns**: List<FileDetails>

### AWS S3 Operations

- `s3:ListBucket` - List all objects in the bucket
- `s3:GetObjectAcl` - Retrieve owner information for each file

### File Details Population

- **File Name**: From `S3ObjectSummary.getKey()`
- **Bucket Name**: From `S3ObjectSummary.getBucketName()`
- **Tag**: From `S3ObjectSummary.getETag()`
- **Size**: From `S3ObjectSummary.getSize()`
- **Last Modified**: From `S3ObjectSummary.getLastModified().getTime()`
- **Storage Class**: From `S3ObjectSummary.getStorageClass()`
- **Owner**: From `getOwner()` method using ACL

### Telemetry Metrics

No specific telemetry metrics are tracked for this operation.

## Troubleshooting

### Issue: Empty List Returned for Non-Empty Bucket

**Cause**: Bucket is in different region or all entries are folders

**Solution**:

1. Verify bucket is in the configured region
2. Check bucket in AWS Console to confirm files exist
3. Ensure files don't all end with `/` (folders)
4. Verify IAM user has `s3:ListBucket` permission

### Issue: Operation Times Out

**Cause**: Bucket contains too many files

**Solution**:

1. Consider using AWS CLI or SDK with pagination for very large buckets
2. Organize files into multiple smaller buckets
3. Increase timeout settings if possible
4. Use S3 Inventory feature for very large buckets

### Issue: Missing Owner Information

**Cause**: Insufficient permissions to read ACLs

**Solution**:

1. Verify IAM user has `s3:GetObjectAcl` permission
2. Check bucket policy allows ACL reads
3. Review error logs for specific permission errors

### Issue: Access Denied Error

**Cause**: Insufficient permissions to list bucket

**Solution**:

1. Verify IAM user has `s3:ListBucket` permission
2. Check bucket policy doesn't restrict listing
3. Ensure IAM user is active
4. Verify credentials are correct

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup and credentials
- [Authentication](/pages/authentication.md) - AWS Signature authentication
- [AWS S3 ListObjects API](https://docs.aws.amazon.com/AmazonS3/latest/API/API_ListObjects.html)
- [AWS S3 GetObjectAcl API](https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObjectAcl.html)

