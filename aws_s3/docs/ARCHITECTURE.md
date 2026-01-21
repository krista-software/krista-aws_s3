# AWS S3 Extension - Architecture Documentation

**Extension Name:** AWS S3  
**Version:** 2.0.6  
**Domain:** Cloud Storage  
**Ecosystem:** Essentials

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Layer Descriptions](#2-layer-descriptions)
3. [Component Diagrams](#3-component-diagrams)
4. [Data Flow](#4-data-flow)
5. [Performance Limitations](#5-performance-limitations)
6. [Error Scenarios](#6-error-scenarios)
7. [Dependency Graph](#7-dependency-graph)

---

## 1. Architecture Overview

The AWS S3 Extension follows a **5-layer architecture** pattern that separates concerns and promotes maintainability, testability, and loose coupling.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         EXTENSION LAYER                                  │
│  AWS3Extension.java - Lifecycle management, validation, configuration   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          CATALOG LAYER                                   │
│  FileOperationsArea.java - Catalog requests (8 file operations)         │
│  StorageBucketStore.java - Entity store for bucket CRUD                 │
│  Entities: StorageBucket.java, FileDetails.java                         │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          SERVICE LAYER                                   │
│  FileDao.java - Interface defining file operation contracts             │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                       IMPLEMENTATION LAYER                               │
│  FileDaoImpl.java - Core S3 operations implementation                   │
│  KristaMediaClient.java - Krista ↔ Java file conversion                 │
│  DownloadFileFromS3.java - HTTP file download utility                   │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         CONNECTOR LAYER                                  │
│  S3Provider.java - AWS S3 client wrapper                                │
│  S3ProviderFactory.java - Factory for S3Provider instances             │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      EXTERNAL SERVICES                                   │
│  Amazon S3 (AWS Cloud) - Object storage service                         │
│  Krista File Repository - Internal file management                      │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Layer Descriptions

### 2.1 Extension Layer

**Purpose:** Entry point and lifecycle management

| Component | Responsibility |
|-----------|----------------|
| `AWS3Extension` | Extension registration, attribute validation, connection testing |

**Lifecycle Handlers:**
- `VALIDATE_ATTRIBUTES` - Validates AWS credentials format
- `TEST_CONNECTION` - Tests S3 connectivity using `getS3AccountOwner()`
- `INVOKER_UPDATED` - Refreshes credentials when updated
- `CUSTOM_TABS` - Provides documentation links

### 2.2 Catalog Layer

**Purpose:** Exposes functionality to Krista platform

| Component | Responsibility |
|-----------|----------------|
| `FileOperationsArea` | Defines 8 catalog requests for file operations |
| `StorageBucketStore` | EntityStore implementation for bucket CRUD |
| `StorageBucket` | Entity representing S3 bucket |
| `FileDetails` | Entity representing file metadata |

**Catalog Requests:**
1. Upload File
2. Download file as URL
3. Download as a File
4. Delete File
5. List file from bucket
6. Copy S3 File
7. Move S3 File
8. Rename S3 File

### 2.3 Service Layer

**Purpose:** Defines contracts for business operations

| Component | Responsibility |
|-----------|----------------|
| `FileDao` | Interface defining file operation methods |

### 2.4 Implementation Layer

**Purpose:** Core business logic implementation

| Component | Responsibility |
|-----------|----------------|
| `FileDaoImpl` | Implements FileDao with S3 operations |
| `KristaMediaClient` | Converts between Krista File and java.io.File |
| `DownloadFileFromS3` | Downloads files via HTTP from pre-signed URLs |

### 2.5 Connector Layer

**Purpose:** External service integration

| Component | Responsibility |
|-----------|----------------|
| `S3Provider` | Creates and manages AmazonS3 client |
| `S3ProviderFactory` | Factory pattern for S3Provider instances |

---

## 3. Component Diagrams

### 3.1 Class Dependency Diagram

```
┌──────────────────────────────────────────────────────────────────────────┐
│                            AWS3Extension                                  │
│  ┌────────────────────────────────────────────────────────────────────┐  │
│  │ @Extension(version="2.0.5", name="AWS S3")                         │  │
│  │ Fields: accessKey, secretKey (secured), region (secured)          │  │
│  └────────────────────────────────────────────────────────────────────┘  │
│                                    │                                      │
│                    ┌───────────────┴───────────────┐                     │
│                    ▼                               ▼                      │
│  ┌─────────────────────────────┐  ┌─────────────────────────────────┐   │
│  │    FileOperationsArea       │  │      StorageBucketStore         │   │
│  │  ┌───────────────────────┐  │  │  ┌───────────────────────────┐  │   │
│  │  │ @Inject FileDaoImpl   │  │  │  │ @Inject S3ProviderFactory │  │   │
│  │  │ @Inject MediaClient   │  │  │  │ implements EntityStore    │  │   │
│  │  └───────────────────────┘  │  │  └───────────────────────────┘  │   │
│  └──────────────┬──────────────┘  └──────────────┬──────────────────┘   │
│                 │                                 │                       │
│                 ▼                                 ▼                       │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                         FileDaoImpl                                  │ │
│  │  ┌─────────────────────────────────────────────────────────────┐   │ │
│  │  │ @Inject S3ProviderFactory                                    │   │ │
│  │  │ Methods: uploadFile, downloadFile, copyFile, moveFile, etc. │   │ │
│  │  └─────────────────────────────────────────────────────────────┘   │ │
│  └──────────────────────────────────┬──────────────────────────────────┘ │
│                                     │                                     │
│                                     ▼                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                      S3ProviderFactory                               │ │
│  │  ┌─────────────────────────────────────────────────────────────┐   │ │
│  │  │ @Inject AWS3Attributes                                       │   │ │
│  │  │ Creates S3Provider instances                                 │   │ │
│  │  └─────────────────────────────────────────────────────────────┘   │ │
│  └──────────────────────────────────┬──────────────────────────────────┘ │
│                                     │                                     │
│                                     ▼                                     │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                         S3Provider                                   │ │
│  │  ┌─────────────────────────────────────────────────────────────┐   │ │
│  │  │ getS3Client() → AmazonS3                                     │   │ │
│  │  │ Uses: BasicAWSCredentials, AWSStaticCredentialsProvider     │   │ │
│  │  └─────────────────────────────────────────────────────────────┘   │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────────────┘
```

### 3.2 File Conversion Flow

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Krista File   │────▶│ KristaMediaClient│────▶│  java.io.File   │
│  (Platform)     │     │  toJavaFile()    │     │  (Local Temp)   │
└─────────────────┘     └──────────────────┘     └────────┬────────┘
                                                          │
                                                          ▼
                                                 ┌─────────────────┐
                                                 │   S3 Upload     │
                                                 │  putObject()    │
                                                 └─────────────────┘

┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   S3 Object     │────▶│ Pre-signed URL   │────▶│ HTTP Download   │
│  (AWS Cloud)    │     │ generatePresigned│     │ DownloadFromS3  │
└─────────────────┘     └──────────────────┘     └────────┬────────┘
                                                          │
                                                          ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│   Krista File   │◀────│ KristaMediaClient│◀────│  java.io.File   │
│  (Platform)     │     │  toKristaFile()  │     │  (Local Temp)   │
└─────────────────┘     └──────────────────┘     └─────────────────┘
```

---

## 4. Data Flow

### 4.1 Upload File Flow

```
User Request
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│ FileOperationsArea.uploadFile()                                  │
│  1. Extract parameters: bucketName, fileName, kristaFile        │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│ KristaMediaClient.toJavaFile()                                   │
│  1. Get file from Krista repository                             │
│  2. Write to temp directory (/tmp/)                             │
│  3. Return java.io.File                                         │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│ FileDaoImpl.uploadFile()                                         │
│  1. Validate inputs (bucket, fileName, file)                    │
│  2. Check if file already exists → throw error if exists        │
│  3. Call s3Client.putObject(bucket, key, file)                  │
│  4. Return true on success                                      │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│ S3Provider.getS3Client()                                         │
│  1. Create BasicAWSCredentials                                  │
│  2. Build AmazonS3 client with region                           │
│  3. Execute putObject request                                   │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
Amazon S3 (AWS Cloud)
```

### 4.2 Download File as URL Flow

```
User Request
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│ FileOperationsArea.downloadFileAsUrl()                           │
│  1. Extract parameters: bucketName, fileName                    │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
┌─────────────────────────────────────────────────────────────────┐
│ FileDaoImpl.downloadFileAsLink()                                 │
│  1. Validate inputs                                             │
│  2. Verify file exists using getObject()                        │
│  3. Create GeneratePresignedUrlRequest                          │
│     - Method: GET                                               │
│     - Expiration: 7 days                                        │
│  4. Generate pre-signed URL                                     │
│  5. Return HTML anchor: <a href="URL">fileName</a>              │
└─────────────────────────────────────────────────────────────────┘
     │
     ▼
Response: RichText with clickable download link
```

---

## 5. Performance Limitations

### 5.1 Known Limitations

| Limitation | Description | Impact | Workaround |
|------------|-------------|--------|------------|
| **Single File Upload** | Only one file per upload request | Cannot batch upload | Use multiple sequential requests |
| **No Multipart Upload** | Large files (>5GB) not supported | Upload timeout for large files | Split files or use AWS Console |
| **No Connection Pooling** | S3 client created per request | Higher latency | Accept current behavior |
| **Full Bucket Listing** | All files fetched before pagination | Memory pressure for large buckets | Use S3 console for large buckets |
| **No Folder Operations** | Folders excluded from listing | Cannot manage folder structure | Use file key prefixes |
| **No Versioning** | Only current version accessible | Cannot access file history | Use AWS Console for versions |
| **No Lifecycle Policies** | Cannot configure via extension | Manual policy management | Configure in AWS Console |

### 5.2 Performance Characteristics

| Operation | Typical Latency | Factors |
|-----------|-----------------|---------|
| Upload File | 1-10s | File size, network speed, region |
| Download URL | 100-500ms | S3 API response time |
| Download File | 1-30s | File size, network speed |
| Delete File | 100-500ms | S3 API response time |
| List Files | 500ms-5s | Number of files in bucket |
| Copy File | 100ms-10s | File size (server-side copy) |

### 5.3 Resource Constraints

| Resource | Limit | Notes |
|----------|-------|-------|
| Pre-signed URL Expiration | 7 days | Hardcoded in Constants.java |
| Temp Directory | `/tmp/` | System temp, may have space limits |
| S3 Extension Temp | `/tmp/krista/extension/s3/` | Extension-specific temp storage |
| Max File Size | AWS S3 limit (5TB) | Single PUT limited to 5GB |

---

## 6. Error Scenarios

### 6.1 Error Classification

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         ERROR CATEGORIES                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────────┐  │
│  │ VALIDATION      │  │ AWS S3 ERRORS   │  │ SYSTEM ERRORS           │  │
│  │ ERRORS          │  │                 │  │                         │  │
│  ├─────────────────┤  ├─────────────────┤  ├─────────────────────────┤  │
│  │ • Empty bucket  │  │ • NoSuchKey     │  │ • Network timeout       │  │
│  │ • Empty file key│  │ • NoSuchBucket  │  │ • Connection refused    │  │
│  │ • Null file     │  │ • AccessDenied  │  │ • Disk space full       │  │
│  │ • Invalid input │  │ • InvalidBucket │  │ • Memory exhaustion     │  │
│  └─────────────────┘  │ • BucketExists  │  └─────────────────────────┘  │
│                       └─────────────────┘                                │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.2 Error Scenarios Matrix

| Scenario | HTTP Code | Error Code | User Message | Resolution |
|----------|-----------|------------|--------------|------------|
| File not found | 404 | NoSuchKey | "File 'X' was not found in bucket 'Y'" | Verify file name exists |
| Bucket not found | 404 | NoSuchBucket | "Bucket 'X' does not exist" | Create bucket or verify name |
| Access denied | 403 | AccessDenied | "Access denied to bucket 'X'" | Check IAM permissions |
| Invalid bucket name | 400 | InvalidBucketName | "Bucket name 'X' is invalid" | Use valid naming (3-63 chars, lowercase) |
| Bucket already exists | 409 | BucketAlreadyExists | "Bucket 'X' already exists" | Use different bucket name |
| File already exists | - | Custom | "File 'X' already exists in bucket 'Y'" | Delete existing or use different name |
| Bucket not empty | - | Custom | "Bucket 'X' is not empty" | Delete all files first |
| Invalid credentials | 403 | InvalidAccessKeyId | "Unable to connect to AWS S3" | Verify access key and secret |
| Invalid region | - | Custom | "Unable to connect to AWS S3" | Verify region code |

### 6.3 Error Handling Flow

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        REQUEST PROCESSING                                │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────────┐
                    │     Input Validation          │
                    │  (Validator.stringValidator)  │
                    └───────────────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                    ▼                               ▼
            ┌───────────┐                   ┌───────────────┐
            │  Valid    │                   │   Invalid     │
            └─────┬─────┘                   └───────┬───────┘
                  │                                 │
                  ▼                                 ▼
        ┌─────────────────┐               ┌─────────────────────┐
        │ Execute S3 Op   │               │ Throw Validation    │
        └────────┬────────┘               │ Exception           │
                 │                        └─────────────────────┘
     ┌───────────┴───────────┐
     │                       │
     ▼                       ▼
┌─────────┐           ┌─────────────┐
│ Success │           │ S3 Exception│
└────┬────┘           └──────┬──────┘
     │                       │
     ▼                       ▼
┌─────────────┐       ┌─────────────────────────────────┐
│ Return      │       │ Parse AmazonS3Exception         │
│ Result      │       │ - Extract error code            │
│             │       │ - Map to user-friendly message  │
└─────────────┘       │ - Throw with HTTP status        │
                      └─────────────────────────────────┘
```

### 6.4 Complete Error Message Reference

```
VALIDATION ERRORS:
├── "Bucket name is required. Please provide a valid bucket name and try again."
├── "File name (key) is required. Please provide a valid file name and try again."
└── "File is required. Please provide a valid file that exists and try again."

CONNECTION ERRORS:
└── "Unable to connect to AWS S3. Please verify your Access Key, Secret Key, and Region are correct."

BUCKET ERRORS:
├── "Bucket 'X' already exists. Please choose a different bucket name and try again."
├── "Bucket 'X' was not found. Please verify the bucket name is correct and exists in your AWS account."
├── "Bucket 'X' is not empty and cannot be deleted. Please delete all files in the bucket first."
└── "Bucket name 'X' is invalid. AWS S3 bucket names must be 3-63 characters long, contain only lowercase letters, numbers, hyphens, and periods."

FILE ERRORS:
├── "File 'X' already exists in bucket 'Y'. Please use a different file name or delete the existing file first."
├── "File 'X' was not found. Please verify the file name is correct and exists in the bucket."
├── "File 'X' does not exist in bucket 'Y'. Please verify the file name and bucket, then try again."
└── "Cannot copy file to itself without changes. To copy a file within the same bucket, please provide a different destination file name."

DOWNLOAD ERRORS:
├── "Unable to generate download URL. Please verify that the bucket name and file name are correct."
├── "Unable to determine the file name from the download URL. Please verify the URL is valid."
└── "Unable to download file. Server returned HTTP response code X."
```

---

## 7. Dependency Graph

### 7.1 External Dependencies

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         AWS S3 Extension                                 │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
            ┌───────────────────────┼───────────────────────┐
            │                       │                       │
            ▼                       ▼                       ▼
┌───────────────────┐   ┌───────────────────┐   ┌───────────────────────┐
│   AWS SDK S3      │   │   Krista APIs     │   │   HK2 Framework       │
│   1.12.547        │   │   1.0.117         │   │   3.0.3               │
├───────────────────┤   ├───────────────────┤   ├───────────────────────┤
│ • AmazonS3        │   │ • Extension       │   │ • @Service            │
│ • AmazonS3Client  │   │ • CatalogRequest  │   │ • @Inject             │
│ • S3Object        │   │ • EntityStore     │   │ • ServiceLocator      │
│ • PutObjectReq    │   │ • FileRepository  │   │                       │
│ • GeneratePresign │   │ • Attributes      │   │                       │
└───────────────────┘   └───────────────────┘   └───────────────────────┘
            │                       │                       │
            └───────────────────────┼───────────────────────┘
                                    │
                                    ▼
                        ┌───────────────────────┐
                        │   Java 21 Runtime     │
                        └───────────────────────┘
```

### 7.2 Internal Package Structure

```
app.krista.extensions.essentials.cloudstorage.aws_s3
├── AWS3Extension.java              # Extension entry point
├── AWS3Attributes.java             # Configuration holder
│
├── catalog/
│   ├── CatalogTypes.java           # Type converters
│   ├── FileOperationsArea.java     # Catalog requests
│   │
│   ├── entities/
│   │   ├── StorageBucket.java      # Bucket entity
│   │   └── FileDetails.java        # File metadata entity
│   │
│   └── stores/
│       └── StorageBucketStore.java # Bucket EntityStore
│
├── service/
│   └── FileDao.java                # Service interface
│
└── impl/
    ├── FileDaoImpl.java            # Service implementation
    │
    ├── connector/
    │   ├── S3Provider.java         # S3 client wrapper
    │   └── S3ProviderFactory.java  # Provider factory
    │
    ├── stores/
    │   └── KristaMediaClient.java  # File conversion
    │
    └── util/
        ├── Constants.java          # Application constants
        ├── Validator.java          # Input validation
        └── DownloadFileFromS3.java # HTTP downloader
```

---

*Architecture documentation for AWS S3 Extension v2.0.6*

