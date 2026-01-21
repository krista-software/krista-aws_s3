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

---------------------------------------------------------

    
* **Create Bucket**


Description : The act of creating a bucket in a cloud storage service, which establishes a dedicated storage container.

Input Parameters :

| **Name**    | **Type** | **Mandatory** |
|-------------|----------|---------------|
| Bucket Name | Text     | True          |



Output Parameters :

| **Name** | **Type**         |
|----------|------------------|
| Bucket   | Amazon S3 Bucket |

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

Description : The act of searching buckets in a cloud storage service, 
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


-----------------------------------------------------------------------------------------

### File Details

* This entity is used only for data representation.
* Entity requests are not supported for this entity.

   


