# Download File as URL

## Overview

The **Download File as URL** catalog request generates a pre-signed URL for downloading a file from an Amazon S3 bucket.
This URL is valid for 7 days and allows temporary access to the file without requiring AWS credentials.

## Request Details

- **Area**: File Operations
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No (URL generation is idempotent but not retryable)

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

| Parameter Name | Type     | Description                                                            |
|----------------|----------|------------------------------------------------------------------------|
| URL            | RichText | HTML anchor tag containing the pre-signed URL for downloading the file |

### Output Format

The output is an HTML anchor tag in the format:

```html
<a href="https://bucket-name.s3.region.amazonaws.com/file-key?...">file-name</a>
```

The URL includes:

- S3 bucket endpoint
- File key
- AWS signature parameters
- Expiration timestamp (7 days from generation)

## Validation Rules

| Validation                       | Error Message                                            | Resolution                                |
|----------------------------------|----------------------------------------------------------|-------------------------------------------|
| Bucket name is null or empty     | "Operation fail due to bucket name is null or empty."    | Provide a valid bucket name               |
| File name (key) is null or empty | "Operation fail due to file Key is null or empty."       | Provide a valid file name/key             |
| File doesn't exist in bucket     | "Provided file key is not present in bucket: [file-key]" | Verify file exists in the bucket          |
| Bucket doesn't exist             | "Provided bucket name is not valid: [bucket-name]"       | Verify bucket exists in configured region |

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

**Error Messages**:

- "Provided file key is not present in bucket: [file-key]"
- "Provided bucket name is not valid: [bucket-name]"

**Resolution**:

1. Verify file exists using "List File from Bucket" request
2. Check bucket exists in AWS Console
3. Confirm bucket is in the configured region
4. Verify file key spelling and case

### System Errors (SYSTEM_ERROR)

**Cause**: AWS service errors or URL generation failures

**Common Scenarios**:

- Failed to generate pre-signed URL
- Network connectivity issues
- AWS service unavailable

**Error Messages**:

- "Unable to generate download URL. Please verify that the bucket name '[bucket-name]' and file name '[file-key]' are
  correct and exist in your AWS S3 account."
- "Unable to generate pre-signed URL for file '[file-key]' in bucket '[bucket-name]'. Please verify your AWS credentials
  and permissions."

**Resolution**:

1. Verify bucket name and file key are correct
2. Check network connectivity
3. Retry the operation
4. Check AWS service status

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

### Example 1: Generate Download Link for PDF Report

**Input**:

```
Bucket Name: "company-documents"
File Name: "reports/2024/monthly-report.pdf"
```

**Output**:

```html

<a href="https://company-documents.s3.us-east-1.amazonaws.com/reports/2024/monthly-report.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&...">reports/2024/monthly-report.pdf</a>
```

**Result**: Pre-signed URL valid for 7 days, allowing anyone with the link to download the file

### Example 2: Generate Link for Image File

### Example 3: File Not Found Error

**Input**:

```
Bucket Name: "my-bucket"
File Name: "nonexistent-file.pdf"
```

**Error**: "Provided file key is not present in bucket: nonexistent-file.pdf"

**Resolution**: Verify the file exists in the bucket using "List File from Bucket" request

## Business Rules

1. **URL Expiration**: Pre-signed URLs are valid for 7 days (168 hours) from generation time.

2. **No Authentication Required**: Anyone with the URL can download the file during the validity period.

3. **URL Uniqueness**: Each request generates a new URL with different signature parameters.

4. **File Accessibility**: The file must exist and be accessible at the time of URL generation.

5. **Region-Specific**: URLs are region-specific and include the bucket's region in the endpoint.

6. **Read-Only Access**: Pre-signed URLs only allow downloading (GET operation), not modification.

## Limitations

1. **URL Expiration**: URLs expire after 7 days and cannot be extended
2. **No Access Control**: Anyone with the URL can download the file
3. **Single File Only**: Cannot generate URLs for multiple files in one request
4. **No Custom Expiration**: Expiration time is fixed at 7 days
5. **No Download Tracking**: Cannot track who downloads the file using the URL
6. **URL Length**: Very long file keys may result in very long URLs

## Best Practices

### 1. Secure URL Distribution

Only share pre-signed URLs through secure channels (encrypted email, HTTPS, etc.).

### 2. Monitor URL Usage

Track when URLs are generated and shared to maintain security awareness.

### 3. Regenerate Expired URLs

Implement workflows to regenerate URLs when they expire if continued access is needed.

### 4. Verify File Exists

Always verify the file exists before generating a URL to avoid errors.

### 5. Use Short File Keys

Keep file keys reasonably short to avoid excessively long URLs.

### 6. Document URL Expiration

Inform users that URLs expire after 7 days when sharing them.

### 7. Consider Security Implications

Be aware that anyone with the URL can download the file during the validity period.

## Common Use Cases

### 1. Temporary File Sharing

```
Scenario: Share a document with external users temporarily
Action: Generate pre-signed URL and share via email
Result: Recipients can download the file for 7 days without AWS credentials
```

### 2. Web Application Integration

```
Scenario: Display downloadable files in a web application
Action: Generate URLs for files and embed in web pages
Result: Users can download files directly from the application
```

### 3. Report Distribution

```
Scenario: Distribute generated reports to stakeholders
Action: Generate download links for monthly reports
Result: Stakeholders receive secure, time-limited access to reports
```

### 4. Media Delivery

```
Scenario: Provide temporary access to media files
Action: Generate URLs for images, videos, or audio files
Result: Media files accessible without exposing AWS credentials
```

## Related Catalog Requests

- [Upload File](UploadFile.md) - Upload files to generate download URLs for
- [Download as a File](DownloadAsAFile.md) - Download files as Krista file objects
- [List File from Bucket](ListFileFromBucket.md) - Verify files exist before generating URLs
- [Delete File](DeleteFile.md) - Remove files when no longer needed
- [Copy S3 File](CopyS3File.md) - Copy files to different buckets
- [Move S3 File](MoveS3File.md) - Move files between buckets

## Technical Implementation

### Helper Class

- **Class**: `FileOperationsArea`
- **Package**: `app.krista.extensions.essentials.cloudstorage.aws_s3.catalog`
- **Method**: `downloadFileAsLink(String bucketName, String fileName)`

### Validation Flow

1. **Input Validation**:
    - Validates bucket name using `Validator.stringValidator()`
    - Validates file key using `Validator.stringValidator()`
2. **File Existence Check**: Attempts to get object from S3
3. **URL Generation**: Creates pre-signed URL with 7-day expiration
4. **HTML Formatting**: Wraps URL in anchor tag

### Service Delegation

- **Service**: `FileDaoImpl`
- **Method**: `downloadFile(String bucketName, String fileKey)`
- **Returns**: String containing HTML anchor tag with pre-signed URL

### AWS S3 Operations

- `s3:GetObject` - Verify file exists
- `s3:GeneratePresignedUrl` - Generate temporary download URL

### URL Generation Details

- **Expiration Time**: 7 days (604,800,000 milliseconds)
- **HTTP Method**: GET
- **Signature Version**: AWS Signature Version 4
- **URL Format**: `https://[bucket].s3.[region].amazonaws.com/[key]?[signature-params]`

### Telemetry Metrics

No specific telemetry metrics are tracked for this operation.

## Troubleshooting

### Issue: "Failed to generate download URL"

**Cause**: File doesn't exist or incorrect bucket/file name

**Solution**:

1. Verify file exists using "List File from Bucket" request
2. Check bucket name spelling and case
3. Verify file key spelling and case
4. Confirm bucket is in the configured region

### Issue: URL Doesn't Work After Generation

**Cause**: URL may have expired or file was deleted

**Solution**:

1. Check if URL has expired (7 days from generation)
2. Verify file still exists in the bucket
3. Generate a new URL if expired
4. Ensure file wasn't deleted after URL generation

### Issue: Access Denied When Using URL

**Cause**: Bucket policy or IAM permissions changed

**Solution**:

1. Verify bucket policy allows public access via pre-signed URLs
2. Check IAM user still has `s3:GetObject` permission
3. Ensure bucket hasn't been made private
4. Generate a new URL with current credentials

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup and credentials
- [Authentication](/pages/Authentication.md) - AWS Signature authentication
- [AWS S3 Pre-signed URLs Documentation](https://docs.aws.amazon.com/AmazonS3/latest/userguide/ShareObjectPreSignedURL.html)

