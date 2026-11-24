# Release Notes - Krista AWS S3 Extension

## Overview

This document provides version history, bug fixes, new features, and known issues for the Krista AWS S3 Extension.

---
## Version 2.0.6

**Release Date**: December 2025

### Version Information

| Component                  | Version       |
|----------------------------|---------------|
| Extension Version          | 2.0.6         |
| Developer                  | Simran Sethi  |
| Krista Service APIs (Java) | 1.0.117       |
| Global Catalog Version     | GC-2026.02.02 |
| Java Version               | 21            |
| AWS SDK for Java           | 1.12.547      |

---

#### 🔧 Resolved Issues

- **Marked Entity Support Store as True**: Updated entity configuration to enable proper entity store support

---

## Version 2.0.4

**Release Date**: November 2025

### Version Information

| Component                  | Version      |
|----------------------------|--------------|
| Extension Version          | 2.0.6        |
| Developer                  | Simran Sethi |
| Krista Service APIs (Java) | 1.0.117      |
| Global Catalog Version     | GC-2025.12.2 |
| Java Version               | 21           |
| AWS SDK for Java           | 1.12.547     |

### What's New

####  Platform Upgrades

- **Java 21 Migration**: Successfully migrated from Java 11 to Java 21
    - Updated build configuration to target Java 21
    - Ensured full compatibility with Krista platform Java 21 requirements

####  Documentation Enhancements

- **Comprehensive Documentation Updates**: Completely refreshed extension documentation
    - Updated API reference documentation with detailed examples
    - Enhanced catalog request documentation with clear parameter descriptions
    - Added troubleshooting guides for common scenarios
    - Improved code samples and usage examples
    - Updated configuration guides with best practices

####  User-Friendly Error Messages

- **Enhanced Error Messages for File Operations**: Completely revamped error messages across all file operations for
  better clarity and actionability

####  Test Coverage & Quality

- **100% Test Coverage**: All 195 test cases passing successfully
    - **51 FileDaoImpl tests**: Comprehensive coverage of all file operations (upload, download, copy, move, rename,
      delete)
    - **15 AWS3Extension tests**: Connection validation, attribute management, and configuration testing
    - **27 StorageBucket tests**: Bucket creation, deletion, search, and management operations
    - **36 S3Provider tests**: AWS client connectivity, authentication, and provider factory testing
    - **23 DownloadFileFromS3 tests**: File download utilities, URL parsing, and content disposition handling
    - **16 KristaMediaClient tests**: File conversion, compression, and media handling
    - **Additional tests**: Catalog operations, attributes management, and utility functions

### Known Issues

None

---

## Version 2.0.3

### Version Information

| Component                  | Version         |
|----------------------------|-----------------|
| Extension Version          | 2.0.3           |
| Developer                  | Aradhya Gudadhe |
| Krista Service APIs (Java) | 1.0.102         |
| Global Catalog Version     | 1.0.51          |

### What's New

#### 🔧 Resolved Issues

- **[KE-1846]**: Fixed detailed error message display for the Download File as URL request
    - Now returns clear, actionable error messages when file download fails

- **[KE-1850]**: Fixed Upload File request to return proper error responses when invalid inputs are provided
    - Added comprehensive input validation with descriptive error messages

- **[KE-1853]**: Fixed issue where invalid destination bucket name was triggering an error for the source bucket
    - Improved bucket validation logic to correctly identify and report which bucket (source or destination) has the
      issue

- **[KE-1843]**: Fixed incorrect error message when attempting to copy a file from the bucket
    - Updated error handling to provide specific messages for different copy operation failures
    - Added validation to prevent copying a file to itself without changes

- **[KE-1841]**: Fixed 'List file from bucket' request returning empty output
    - Corrected file listing logic to properly retrieve and return all files from the bucket
    - Improved handling of folder exclusion in file listings

### Known Issues

None

---

## Note

For detailed API documentation and usage examples, please refer to the extension documentation.
