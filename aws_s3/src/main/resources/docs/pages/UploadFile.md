# Upload File

## Overview

The **Upload File** catalog request uploads a file from the Krista platform to a specified Amazon S3 bucket. This
operation creates a new object in the bucket with the provided file key (name) and file content.

## Request Details

- **Area**: File Operations
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ❌ No (File operations are not retryable to prevent duplicate uploads)

## Input Parameters

| Parameter   | Type | Required | Description                                               | Example                      |
|-------------|------|----------|-----------------------------------------------------------|------------------------------|
| Bucket Name | Text | Yes      | The name of the S3 bucket where the file will be uploaded | `my-documents-bucket`        |
| File Name   | Text | Yes      | The key (name) for the file in the S3 bucket              | `reports/monthly-report.pdf` |
| File        | File | Yes      | The file object to upload                                 | File object from Krista      |

### Parameter Details

#### Bucket Name

- Must be an existing S3 bucket in your AWS account
- Bucket names are globally unique across all AWS accounts
- Must follow S3 bucket naming rules (lowercase, no spaces, etc.)
- The bucket must be in the region specified in the extension configuration

#### File Name (File Key)

- Can include forward slashes (`/`) to simulate folder structure
- Example: `documents/2024/report.pdf` creates a "folder" structure
- Must be unique within the bucket (uploading with an existing key will fail)
- Case-sensitive
- Maximum length: 1024 bytes

#### File

- Any file type is supported (PDF, images, documents, etc.)
- Maximum file size: 5 GB for single upload
- File is uploaded as-is without modification

## Output Parameters

| Parameter | Type    | Description                                   |
|-----------|---------|-----------------------------------------------|
| Success   | Boolean | `true` if upload succeeded, `false` otherwise |

## Validation Rules

| Validation                        | Error Message                                                                                                                     | Resolution                                              |
|-----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------|
| Bucket name is null or empty      | "Bucket name is required. Please provide a valid bucket name and try again."                                                      | Provide a valid bucket name                             |
| File name (key) is null or empty  | "File name (key) is required. Please provide a valid file name and try again."                                                    | Provide a valid file name/key                           |
| File is null or doesn't exist     | "File is required. Please provide a valid file that exists and try again."                                                        | Provide a valid file object                             |
| File key already exists in bucket | "File '[file-key]' already exists in bucket '[bucket-name]'. Please use a different file name or delete the existing file first." | Use a different file name or delete existing file first |
| Bucket doesn't exist              | "Bucket '[bucket-name]' does not exist. Please verify the bucket name is correct and exists in your AWS account, then try again." | Verify bucket exists in configured region               |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing input parameters

**Common Scenarios**:

- Bucket name is null or empty
- File name (key) is null or empty
- File object is null or doesn't exist

**Error Messages**:

- "Bucket name is required. Please provide a valid bucket name and try again."
- "File name (key) is required. Please provide a valid file name and try again."
- "File is required. Please provide a valid file that exists and try again."

**Resolution**:

1. Verify all required parameters are provided
2. Ensure bucket name and file name are not empty strings
3. Confirm file object is valid and exists

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic violations

**Common Scenarios**:

- File key already exists in the bucket
- Attempting to upload to a non-existent bucket

**Error Messages**:

- "File '[file-key]' already exists in bucket '[bucket-name]'. Please use a different file name or delete the existing
  file first."
- "Bucket '[bucket-name]' does not exist. Please verify the bucket name is correct and exists in your AWS account, then
  try again."

**Resolution**:

1. Check if file with same key exists before uploading
2. Verify bucket exists in AWS Console
3. Use a different file name or delete existing file first
4. Confirm bucket is in the configured region

### System Errors (SYSTEM_ERROR)

**Cause**: AWS service errors or network issues

**Common Scenarios**:

- Network connectivity issues
- AWS service unavailable
- Timeout during upload

**Resolution**:

1. Check network connectivity
2. Verify AWS service status
3. Retry the operation after a brief delay
4. Check AWS CloudWatch for service issues

### Authorization Errors

**Cause**: Insufficient permissions or invalid credentials

**Common Scenarios**:

- IAM user lacks `s3:PutObject` permission
- Invalid Access Key or Secret Key
- Bucket policy restricts access

**Error Messages**:

- "Access Denied"
- "Invalid AWS Credentials Provided"

**Resolution**:

1. Verify IAM user has `AmazonS3FullAccess` policy or `s3:PutObject` permission
2. Check Access Key and Secret Key are correct
3. Review bucket policies for access restrictions
4. Ensure IAM user is active and not disabled

## Usage Examples

### Example 1: Upload a PDF Report

**Scenario**: Upload a monthly report to the reports folder

**Input**:

- Bucket Name: `company-documents`
- File Name: `reports/2024/monthly-report.pdf`
- File: [PDF file object]

**Output**: `true`

**Result**: File is uploaded to `company-documents` bucket with key `reports/2024/monthly-report.pdf`

### Example 2: Upload an Image

**Scenario**: Upload a product image

**Input**:

- Bucket Name: `product-images`
- File Name: `products/electronics/laptop-001.jpg`
- File: [Image file object]

**Output**: `true`

**Result**: Image is uploaded with folder-like structure

### Example 3: Upload Fails - Duplicate Key

**Scenario**: Attempt to upload a file with an existing key

**Input**:

- Bucket Name: `my-bucket`
- File Name: `document.pdf`
- File: [PDF file object]

**Output**: `false`

**Error**: "Provided file key is already present in bucket my-bucket"

**Resolution**: Use a different file name or delete the existing file first

## Business Rules

1. **File Overwrite Protection**: The operation will NOT overwrite existing files. If a file with the same key exists,
   the upload fails.

2. **Folder Simulation**: S3 doesn't have true folders, but using `/` in the file key creates a folder-like structure in
   the S3 console.

3. **Bucket Region**: The bucket must exist in the AWS region specified in the extension configuration.

4. **Permissions Required**: The IAM user must have `s3:PutObject` permission for the target bucket.

5. **File Metadata**: Files are uploaded with default metadata. Custom metadata is not supported in this version.

## Limitations

1. **Maximum File Size**: 5 GB per file (S3 single upload limit)
2. **No Overwrite**: Cannot overwrite existing files (must delete first)
3. **No Multipart Upload**: Large files (>5GB) require multipart upload (not supported)
4. **No Custom Metadata**: Cannot set custom metadata or storage class
5. **No Encryption Options**: Uses bucket's default encryption settings
6. **Synchronous Operation**: Upload is synchronous; large files may take time

## Best Practices

### 1. Use Descriptive File Keys

Include folder structure and descriptive names for better organization:

- ✅ Good: `invoices/2024/Q1/invoice-12345.pdf`
- ❌ Bad: `file1.pdf`

### 2. Validate File Size

Ensure files are under 5 GB before uploading to avoid failures.

### 3. Verify Bucket Exists

Confirm the bucket exists and is in the correct region before attempting upload.

### 4. Handle Duplicate Keys

Check if a file with the same key exists before uploading to avoid errors. Use the "List File from Bucket" request to
verify.

### 5. Use Consistent Naming Conventions

Establish and follow a naming convention for file keys across your organization.

### 6. Monitor Storage Costs

Track S3 storage usage regularly to manage costs effectively. Consider using S3 lifecycle policies for cost
optimization.

### 7. Implement Error Handling

Always implement proper error handling in your workflows to gracefully handle upload failures.

## Common Use Cases

### 1. Document Management System

```
Scenario: Upload invoices, contracts, and reports to organized folders
Action: Use folder structure like "documents/invoices/2024/Q1/invoice-001.pdf"
Result: Well-organized document repository accessible from Krista workflows
```

### 2. Media Storage for Applications

```
Scenario: Store product images, videos, and audio files
Action: Upload media files with descriptive keys like "media/products/electronics/laptop-001.jpg"
Result: Centralized media storage for web applications and mobile apps
```

### 3. Automated Backup System

```
Scenario: Backup critical data from Krista workflows
Action: Schedule automated uploads of backup files with timestamps
Result: Reliable backup system with versioned files
```

### 4. Data Export and Archival

```
Scenario: Export processed data and historical records
Action: Upload CSV, JSON, or XML files for long-term storage
Result: Archived data available for compliance and analysis
```

## Related Catalog Requests

- [Download File as URL](/pages/DownloadFileAsUrl.md) - Generate download link for uploaded files
- [Download as a File](/pages/DownloadAsAFile.md) - Download uploaded files back to Krista
- [Delete File](/pages/DeleteFile.md) - Remove uploaded files from S3
- [List File from Bucket](/pages/ListFileFromBucket.md) - View all uploaded files in a bucket
- [Copy S3 File](/pages/CopyS3File.md) - Copy uploaded files to another bucket
- [Move S3 File](/pages/MoveS3File.md) - Move uploaded files to another bucket
- [Rename S3 File](/pages/RenameS3File.md) - Rename uploaded files

## Technical Implementation

### Helper Class

- **Class**: `FileOperationsArea`
- **Package**: `app.krista.extensions.essentials.cloudstorage.aws_s3.catalog`
- **Method**: `uploadFile(String bucketName, String fileName, File file)`

### Validation Flow

1. **Input Validation**:
    - Validates bucket name using `Validator.stringValidator()`
    - Validates file key using `Validator.stringValidator()`
    - Validates file object using `Validator.fileValidator()`
2. **Duplicate Check**: Attempts to get existing object with same key
3. **Bucket Verification**: Checks if bucket exists in account
4. **Upload Execution**: Uses `PutObjectRequest` to upload file

### Service Delegation

- **Service**: `FileDaoImpl`
- **Method**: `uploadFile(String bucketName, String fileKey, File file)`
- **Returns**: Boolean indicating success or failure

### AWS S3 Operations

- `s3:GetObject` - Check if file key already exists
- `s3:ListBuckets` - Verify bucket exists in account
- `s3:PutObject` - Upload the file to S3

### Telemetry Metrics

No specific telemetry metrics are tracked for this operation.

## Troubleshooting

### Issue: Upload Returns False

**Possible Causes**:

- File key already exists in bucket
- Bucket doesn't exist
- Wrong region configured

**Resolution**:

1. Check if file with same key exists (use List File from Bucket)
2. Verify bucket exists in AWS Console
3. Confirm region in extension configuration matches bucket region

### Issue: Access Denied Error

**Cause**: IAM user lacks upload permissions

**Resolution**:

1. Verify IAM user has `AmazonS3FullAccess` policy
2. Check bucket policy doesn't restrict uploads
3. Ensure IAM user is active

### Issue: File Too Large

**Cause**: File exceeds 5 GB limit

**Resolution**:

- Split file into smaller parts
- Use AWS CLI or SDK for multipart upload
- Consider alternative storage for very large files

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup and credentials
- [Authentication](/pages/authentication.md) - AWS Signature authentication
- [AWS S3 PutObject API](https://docs.aws.amazon.com/AmazonS3/latest/API/API_PutObject.html)

