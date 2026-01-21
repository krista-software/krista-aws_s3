# AWS S3 Extension for Krista

## Overview

The AWS S3 Extension enables seamless integration between the Krista platform and Amazon Simple Storage Service (S3),
providing comprehensive file and bucket management capabilities. This extension allows you to upload, download, copy,
move, rename, and delete files across S3 buckets, as well as list bucket contents—all through simple catalog requests
within your Krista workflows.

## Key Features

-  **File Upload & Download**: Upload files to S3 buckets and download them as URLs or file objects
-  **File Management**: Copy, move, rename, and delete files across buckets
-  **Bucket Operations**: List files from buckets with detailed metadata
-  **Secure Authentication**: AWS Signature Version 4 (SigV4) authentication
-  **Pre-signed URLs**: Generate temporary download links valid for 7 days
-  **Easy Integration**: Simple catalog requests for all file operations
-  **Multi-Region Support**: Works with S3 buckets in any AWS region

## Quick Start Guide

### 1. Prerequisites

- AWS account with IAM user access
- Access Key ID and Secret Access Key
- S3 bucket(s) created in your desired AWS region
- Krista platform access

### 2. Configuration Steps

1. **Create IAM User**: Follow the [Creating AWS User](/pages/CreatingAWSUser.md) guide to create an IAM user with S3
   permissions
2. **Generate Credentials**: Obtain your Access Key ID and Secret Access Key
3. **Configure Extension**: Enter your credentials in the extension configuration (
   see [Extension Configuration](/pages/ExtensionConfiguration.md))
4. **Test Connection**: Verify the connection by listing files from an existing bucket

### 3. Start Using

Once configured, you can immediately start using the file operations catalog requests to manage your S3 storage in your
Krista workflows.

## Documentation Structure

### Getting Started

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup parameters and configuration
- [Authentication](/pages/authentication.md) - AWS Signature authentication details
- [Creating AWS User](/pages/CreatingAWSUser.md) - Step-by-step IAM user creation guide

### File Operations

All file operations are organized under the **File Operations** area:

- [Upload File](/pages/UploadFile.md) - Upload files to S3 buckets
- [Download File as URL](/pages/DownloadFileAsUrl.md) - Generate pre-signed download URLs
- [Download as a File](/pages/DownloadAsAFile.md) - Download files as file objects
- [Delete File](/pages/DeleteFile.md) - Remove files from S3 buckets
- [List File from Bucket](/pages/ListFileFromBucket.md) - List all files in a bucket
- [Copy S3 File](/pages/CopyS3File.md) - Copy files between buckets
- [Move S3 File](/pages/MoveS3File.md) - Move files between buckets
- [Rename S3 File](/pages/RenameS3File.md) - Rename files within a bucket

### Additional Resources

- [Limitations](/pages/limitations.md) - Known limitations and constraints
- [Release Notes](/pages/release_notes.md) - Version history and updates

## Support & Resources

- **Extension Version**: 2.0.6
- **Java Version**: Java 21
- **AWS S3 API Documentation**: [AWS S3 Developer Guide](https://docs.aws.amazon.com/s3/)
- **AWS Regions
  **: [AWS Regional Services](https://aws.amazon.com/about-aws/global-infrastructure/regional-product-services/)

## Quick Links

- [AWS Console](https://aws.amazon.com/console/)
- [IAM User Management](https://console.aws.amazon.com/iam/)
- [S3 Bucket Management](https://s3.console.aws.amazon.com/s3/)
- [AWS S3 Documentation](https://docs.aws.amazon.com/s3/)

---

**Need Help?** Refer to the [Extension Configuration](/pages/ExtensionConfiguration.md) guide or check
the [Limitations](/pages/limitations.md) page for common issues and constraints.