# Aws S3 Extension for Krista

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



## License

This project is licensed under the **GNU General Public License v3.0**.

```
Aws S3 Extension for Krista
Copyright (C) 2025 Krista Software

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

For the full license text, see the [LICENSE](LICENSE) file or visit https://www.gnu.org/licenses/gpl-3.0.html.
