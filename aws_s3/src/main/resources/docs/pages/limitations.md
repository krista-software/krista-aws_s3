# Guidelines & Limitations

### Guidelines - S3 Bucket operations
*	You can create and access buckets in a specific region that you have configured during the setup process. For example, If you setup region as "us-east-1" then you can only perform operations on buckets that are in the "us-east-1" region.
*	If you want to perform operations on buckets in different regions, you must use the respective invoker associated with those different regions.

### Limitations  - 
**Download file from S3 bucket :**
* When you click a link of .zip or excel file on a web page, the browser usually prompts to download the file or open it with a compatible application. You can choose to download the file to the local device.
*	Text and image files can be downloaded, but the browser's behavior might be different. Text files (.txt) are usually displayed directly in the browser if they are small, while larger text files may prompt a download. Image files (such as .jpg .png) are often displayed directly in the browser when linked. However, you can use right-click function to save them. 

**Upload file to S3 bucket :**
* Please note that you can upload only one file at a time. We do not support multiple file uploads simultaneously. Choose your file, and once it's uploaded, you can select another if needed.
