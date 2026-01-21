# Supported Requests

The Krista AWS S3 extension supports the following requests.

## Catalog Requests 

A Catalog request is normalized action defined in the catalog.<br>The catalog is distribution mechanism 
to share created work with other Krista users.The catalog requests made to systems are normalized or common for all domains so that 
conversations can be unchanged and user can just switch the extensions to achieve the connectivity with different systems.

### File Operations Area

* **Download file as url**

Description : A file can be downloaded using a url that remains valid for a period of 7 days from the time it is generated.

Input Parameters :

| **Name**    | **Type** | **Mandatory** | **Example** |
|-------------|----------|---------------|-------------|
| Bucket Name | Text     | True          | mediabucket |
| File Name   | Text     | True          | newzip      |

Output Parameters :

| **Name** | **Type**  |
|----------|-----------|
| URL      | Rich Text |



**Response**

| Bucket Name | File Key | Output                            |
|-------------|----------|-----------------------------------|
| valid       | valid    | Downloadable link                 |
| invalid     | valid    | Message: "Bucket does not exist"  |
| valid       | invalid  | Message:"File key does not exist" |


* **Download file from bucket :** The file name for downloading a file is the full path to the file within the bucket. For example, if the file "example.jpg" is in a folder named "images," the file key would be "images/example.jpg.



* **Download File As File**

Description : A file downloaded using bucket name and file name.

Input Parameters :

| **Name**    | **Type** | **Mandatory** | **Example** |
|-------------|----------|---------------|-------------|
| Bucket Name | Text     | True          | mediabucket |
| File Name   | Text     | True          | newzip      |

Output Parameters :

| **Name** | **Type** |
|----------|----------|
| File     | File     |

* **Upload File**

Description : The process of uploading a file to a bucket, which allows users to store any type of file in the AWS cloud storage service.

File format : Any type of file can be uploaded in S3 bucket using this method.

**Rule for Providing File Key :** When naming your file, include the file type. For instance, if you're uploading a JSON file, name it "newfile.json".

Input Parameters :

| **Name**    | **Type** | **Mandatory** | **Example**            |
|-------------|----------|---------------|------------------------|
| Bucket Name | Text     | True          | mediabucket            |
| File Key    | Text     | True          | newzip                 |
| File        | File     | True          | Provide file to upload |

Output Parameters :

| **Name**    | **Type** |
|-------------|----------|
| Is Uploaded | Switch   |


**Response**

| Bucket Name | File Key | File    | Output                            |
|-------------|----------|---------|-----------------------------------|
| valid       | valid    | valid   | File uploaded in bucket           |
| invalid     | valid    | valid   | Message:"Bucket does not exist"   |


* **Upload file in folder :** When uploading a file to S3, the file key is typically the combination of the folder path and the file name within the bucket. For example, if you have a folder named "images" and you are uploading an image file named "example.jpg," the file key would be "images/example.jpg."


* **Delete File**

Description : A file deleted using file name and bucket name.

Input Parameters :

| **Name**    | **Type** | **Mandatory** | **Example** |
|-------------|----------|---------------|-------------|
| Bucket Name | Text     | True          | mediabucket |
| File Name   | Text     | True          | newzip      |

Output Parameters :

| **Name** | **Type** |
|----------|----------|
| Response | Text     |


* **List File From Bucket**

Description : Get list of files present in given bucket.

Input Parameters :

| **Name**    | **Type** | **Mandatory** | **Example** |
|-------------|----------|---------------|-------------|
| Bucket Name | Text     | True          | mediabucket |

Output Parameters :

| **Name**  | **Type**           |
|-----------|--------------------|
| File List | List<File Details> |



* **Copy S3 File**

Description : Copy file from one bucket to another using Source Bucket Name,Source File Name and Destination Bucket Name.

Input Parameters :

| **Name**                | **Type** | **Mandatory** | **Example** |
|-------------------------|----------|---------------|-------------|
| Source Bucket Name      | Text     | True          | mediabucket |
| Source File Name        | Text     | True          | newzip      |
| Destination Bucket Name | Text     | True          | newBucket   |

Output Parameters :

| **Name** | **Type** |
|----------|----------|
| Response | Text     |




* **Move S3 File**

Description : Move file from one bucket to another using Source Bucket Name,Source File Name and Destination Bucket Name.

Input Parameters :

| **Name**                | **Type** | **Mandatory** | **Example** |
|-------------------------|----------|---------------|-------------|
| Source Bucket Name      | Text     | True          | mediabucket |
| Source File Name        | Text     | True          | newzip      |
| Destination Bucket Name | Text     | True          | newBucket   |

Output Parameters :

| **Name** | **Type** |
|----------|----------|
| Response | Text     |


* **Rename S3 File**

Description : Rename file from one bucket to another using bucket name,old file name,new file name.

Input Parameters :

| **Name**      | **Type** | **Mandatory** | **Example** |
|---------------|----------|---------------|-------------|
| Bucket Name   | Text     | True          | mediabucket |
| Old File Name | Text     | True          | newzip      |
| New File Name | Text     | True          | acd.pdf     |

Output Parameters :

| **Name** | **Type** |
|----------|----------|
| Response | Text     |


------------------------------------------------------------------------------------------------------

 ## Entity Requests

 Entity requests are the actions that are implemented to store data with a specific data structure.


### Storage Bucket

* Fields :
    1. Name : It is bucket name which is primary key.
    2. Owner : It is owner name of bucket.
    3. created Date : Bucket creation date.
    4. Id : Id of bucket.

*  **Entity Request for Storage Bucket** :
1. Create Bucket
2. Delete Bucket
3. Get Bucket
4.   Search Bucket

-----------------------------------------------------------------------------------------


* **Create Bucket**

Description : The act of creating a bucket in a cloud storage service, which establishes a dedicated storage container.


Input Parameters :

| **Name**    | **Type** | **Mandatory** |
|-------------|----------|---------------|
| Bucket Name | Text     | True          |



Output Parameters :

| **Name** | **Type**        |
|----------|-----------------|
| Bucket   | AmazonS3 Bucket |

**Bucket Naming Rules :**

* Bucket names must be between 3 (min) and 63 (max) characters long.
* Bucket names can consist only of lowercase letters, numbers, dots (.), and hyphens (-).
* Bucket names must begin and end with a letter or number.



-------------------------------------------------------------------------------------------------------

* **Delete Bucket**

Description : The act of deleting a bucket in a cloud storage service, which allows users to remove a storage container.

Input Parameters :

| **Name**    | **Type** | **Mandatory** |
|-------------|----------|---------------|
| Bucket Name | Text     | True          |



Output Parameters :

| **Name** | **Type** |
|----------|----------|
| Message  | Text     |


------------------------------------------------------------------------------------------------------

* **Search Buckets**

Description : The act of searching a bucket in a cloud storage service,
which allows users to retrieve a list of all available buckets within a cloud storage environment.

Input Parameters :

| **Name**    | **Type** | **Mandatory** |
|-------------|----------|---------------|
| Bucket Name | Text     | True          |


Output Parameters :

| **Name**    | **Type**     |
|-------------|--------------|
| Bucket List | List<Bucket> |


------------------------------------------------------------------------------------------------------


* **Get Bucket**

Description : The action of retrieving a bucket in a cloud storage service allows users to access a bucket by specifying its name.


Input Parameters :

| **Name**    | **Type** | **Mandatory** |
|-------------|----------|---------------|
| Bucket Name | Text     | True          |

Output Parameters :

| **Name** | **Type**         |
|----------|------------------|
| Bucket   | Amazon S3 Bucket |



----------------------------------------------------------------------------------------------

### File Details

* This entity is used only for data representation.
* Entity requests are not supported for this entity.



   



