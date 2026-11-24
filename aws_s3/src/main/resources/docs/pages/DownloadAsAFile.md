# Download as a File

## Overview

The **Download as a File** catalog request downloads a file from an Amazon S3 bucket and returns it as a Krista file
object. This allows you to retrieve files from S3 and use them directly in Krista workflows.

## Request Details

- **Area**: File Operations
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No (File download operations are not retryable)

## Input Parameters

| Parameter Name | Type | Required | Description                                   | Example                      |
|----------------|------|----------|-----------------------------------------------|------------------------------|
| Bucket Name    | Text | Yes      | The name of the S3 bucket containing the file | `my-documents-bucket`        |
| File Name      | Text | Yes      | The key (name) of the file in the S3 bucket   | `reports/monthly-report.pdf` |

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

| Parameter Name | Type | Description                                               |
|----------------|------|-----------------------------------------------------------|
| File           | File | Krista file object containing the downloaded file content |

### Output Details

The output is a Krista file object that can be:

- Used in subsequent workflow steps
- Stored in Krista's file system
- Processed by other catalog requests
- Displayed to users

## Validation Rules

| Validation                       | Error Message                                                                                                                     | Resolution                                |
|----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|
| Bucket name is null or empty     | "Bucket name is required. Please provide a valid bucket name and try again."                                                      | Provide a valid bucket name               |
| File name (key) is null or empty | "File name (key) is required. Please provide a valid file name and try again."                                                    | Provide a valid file name/key             |
| File doesn't exist in bucket     | "File '[file-key]' was not found. Please verify the file name is correct and exists in the bucket, then try again."               | Verify file exists in the bucket          |
| Bucket doesn't exist             | "Bucket '[bucket-name]' does not exist. Please verify the bucket name is correct and exists in your AWS account, then try again." | Verify bucket exists in configured region |

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
- Bucket doesn't exist
- URL extraction fails from pre-signed URL

**Error Messages**:

- "Provided file key is not present in bucket: [file-key]"
- "Provided bucket name is not valid: [bucket-name]"
- "Failed to generate download URL"

**Resolution**:

1. Verify file exists using "List File from Bucket" request
2. Check bucket exists in AWS Console
3. Confirm bucket is in the configured region
4. Verify file key spelling and case

### System Errors (SYSTEM_ERROR)

**Cause**: AWS service errors, network issues, or file download failures

**Common Scenarios**:

- Network connectivity issues during download
- AWS service unavailable
- File download timeout
- Temporary file creation failure

**Error Messages**:

- RuntimeException with cause details

**Resolution**:

1. Check network connectivity
2. Verify AWS service status
3. Retry the operation after a brief delay
4. Check available disk space for temporary files
5. Review application logs for detailed error information

### Authorization Errors

**Cause**: Insufficient permissions or invalid credentials

**Common Scenarios**:

- IAM user lacks `s3:GetObject` permission
- Invalid Access Key or Secret Key

**Error Messages**:

- "Access Denied"
- "Invalid AWS Credentials Provided"

**Resolution**:

1. Verify IAM user has `s3:GetObject` permission
2. Check Access Key and Secret Key are correct
3. Review bucket policies for access restrictions

## Usage Examples

### Example 1: Download PDF Report

**Input**:

```
Bucket Name: "company-documents"
File Name: "reports/2024/monthly-report.pdf"
```

**Output**: Krista file object containing the PDF file

**Result**: File can be used in subsequent workflow steps or displayed to users

### Example 2: Download Image File

**Input**:

```
Bucket Name: "product-images"
File Name: "products/electronics/laptop-001.jpg"
```

**Output**: Krista file object containing the image

**Result**: Image file available for processing or display

### Example 3: Download CSV Data File

**Input**:

```
Bucket Name: "data-exports"
File Name: "exports/2024/customer-data.csv"
```

**Output**: Krista file object containing the CSV data

**Result**: CSV file ready for data processing in Krista workflows

## Business Rules

1. **File Download Process**: The operation first generates a pre-signed URL, then downloads the file using that URL.

2. **Temporary Storage**: Downloaded files are temporarily stored in `/tmp/krista/extension/s3/` directory.

3. **File Conversion**: The downloaded file is converted from Java File to Krista File object.

4. **Synchronous Operation**: File download is synchronous and may take time for large files.

5. **No Size Limit**: Unlike upload, there's no explicit size limit, but large files may cause timeout issues.

## Limitations

1. **Large File Downloads**: Very large files may cause timeout or memory issues
2. **Temporary Storage**: Requires disk space for temporary file storage
3. **Synchronous Only**: No support for asynchronous or background downloads
4. **No Progress Tracking**: Cannot track download progress for large files
5. **Network Dependent**: Requires stable network connection for large file downloads
6. **No Resume Support**: Cannot resume interrupted downloads

## Best Practices

### 1. Verify File Exists

Always verify the file exists before attempting to download to avoid errors.

### 2. Handle Large Files Carefully

For large files, consider using "Download File as URL" instead and downloading externally.

### 3. Implement Error Handling

Always implement proper error handling to manage download failures gracefully.

### 4. Monitor Disk Space

Ensure sufficient disk space is available for temporary file storage.

### 5. Clean Up Resources

The extension handles cleanup, but be aware of temporary file storage.

### 6. Use Appropriate File Types

This operation works with any file type, but consider file size and network bandwidth.

## Common Use Cases

### 1. Document Processing

```
Scenario: Download documents for processing in Krista workflows
Action: Download PDF, Word, or Excel files from S3
Result: Files available for data extraction or transformation
```

### 2. Image Processing

```
Scenario: Download images for manipulation or analysis
Action: Download image files from S3 storage
Result: Images ready for resizing, format conversion, or analysis
```

### 3. Data Import

```
Scenario: Import data files for processing
Action: Download CSV, JSON, or XML files
Result: Data files ready for parsing and import into systems
```

### 4. Backup Retrieval

```
Scenario: Retrieve backup files for restoration
Action: Download backup files from S3 archive
Result: Backup files available for system restoration
```

## Related Catalog Requests

- [Upload File](/pages/UploadFile.md) - Upload files to S3 for later download
- [Download File as URL](/pages/DownloadFileAsUrl.md) - Generate download URLs instead of downloading directly
- [List File from Bucket](/pages/ListFileFromBucket.md) - Verify files exist before downloading
- [Delete File](/pages/DeleteFile.md) - Remove files after downloading
- [Copy S3 File](/pages/CopyS3File.md) - Copy files between buckets
- [Move S3 File](/pages/MoveS3File.md) - Move files between buckets

## Technical Implementation

### Helper Class

- **Class**: `FileOperationsArea`
- **Package**: `app.krista.extensions.essentials.cloudstorage.aws_s3.catalog`
- **Method**: `downloadAsAFile(String bucketName, String fileName)`

### Validation Flow

1. **Input Validation**:
    - Validates bucket name using `Validator.stringValidator()`
    - Validates file key using `Validator.stringValidator()`
2. **Pre-signed URL Generation**: Generates temporary download URL
3. **URL Extraction**: Extracts actual URL from HTML anchor tag
4. **File Download**: Downloads file from pre-signed URL to temporary location
5. **File Conversion**: Converts Java File to Krista File object

### Service Delegation

- **Service**: `FileDaoImpl`
- **Methods**:
    - `downloadFile(String bucketName, String fileKey)` - Generate pre-signed URL
    - `downloadFileAsLink(String presignedURL)` - Download file from URL
- **Returns**: Krista File object

### AWS S3 Operations

- `s3:GetObject` - Verify file exists and retrieve metadata
- `s3:GeneratePresignedUrl` - Generate temporary download URL

### File Processing

- **Temporary Directory**: `/tmp/krista/extension/s3/`
- **URL Extraction**: Uses `Validator.extractURLFromAnchorTag()` to parse HTML
- **File Conversion**: Uses `KristaMediaClient.toKristaFile()` for conversion

### Telemetry Metrics

No specific telemetry metrics are tracked for this operation.

## Troubleshooting

### Issue: File Download Fails

**Cause**: File doesn't exist, network issues, or insufficient permissions

**Solution**:

1. Verify file exists using "List File from Bucket" request
2. Check network connectivity
3. Verify IAM user has `s3:GetObject` permission
4. Check bucket and file name spelling

### Issue: Out of Memory Error

**Cause**: File is too large to download into memory

**Solution**:

1. Use "Download File as URL" for very large files
2. Increase available memory for the application
3. Consider downloading file externally using the URL

### Issue: Temporary File Creation Failed

**Cause**: Insufficient disk space or permissions

**Solution**:

1. Check available disk space in `/tmp` directory
2. Verify application has write permissions to temporary directory
3. Clean up old temporary files if needed

### Issue: File Conversion Error

**Cause**: IOException during file conversion

**Solution**:

1. Check file integrity in S3
2. Verify file is not corrupted
3. Review application logs for detailed error information
4. Retry the operation

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup and credentials
- [Authentication](/pages/authentication.md) - AWS Signature authentication
- [AWS S3 GetObject API](https://docs.aws.amazon.com/AmazonS3/latest/API/API_GetObject.html)

