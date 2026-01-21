# Creating AWS User

## Overview

This guide provides step-by-step instructions for creating an AWS IAM (Identity and Access Management) user with the
necessary permissions to use the AWS S3 Extension. You'll learn how to create a user, attach S3 permissions, and
generate access credentials.

## Prerequisites

- AWS account with administrative access
- Access to the AWS Management Console
- Permission to create IAM users and manage policies

## Step-by-Step Guide

### Step 1: Log in to AWS Console

1. Navigate to the [AWS Console](https://aws.amazon.com/console/)
2. Click **Sign In to the Console**
3. Select **Root user** or use an IAM user with administrative privileges
4. Enter your credentials and sign in

### Step 2: Navigate to IAM Service

1. In the AWS Console, locate the search bar at the top
2. Type **IAM** in the search box
3. Click on **IAM** from the search results

![Search IAM](../_media/CreatingAWSUser_search_iam.png)

**Alternative**: You can also find IAM under **Security, Identity, & Compliance** in the Services menu.

### Step 3: Access Users Section

1. In the IAM dashboard, look at the left navigation panel
2. Click on **Users** under **Access management**

![Click Users](../_media/CreatingAWSUser_click_users.png)

This will display a list of existing IAM users in your account.

### Step 4: Create New User

1. Click the **Create user** button (usually located at the top right)

![Creating User](../_media/CreatingAWSUser_create_user.png)

This will start the user creation wizard.

### Step 5: Specify User Details

1. Enter a **User name** for the new IAM user
    - Example: `krista-s3-extension`
    - Use a descriptive name that indicates the purpose
    - User names must be unique within your AWS account

2. Click **Next** to proceed to permissions

![Provide User Name](../_media/CreatingAWSUser_username.png)

**Best Practice**: Use a naming convention like `krista-[service]-[environment]` (e.g., `krista-s3-production`).

### Step 6: Set Permissions

1. Select **Attach policies directly** as the permission option

![Attaching Policy](../_media/CreatingAWSUser_attach_policy.png)

2. In the search box, type **AmazonS3FullAccess**
3. Check the box next to **AmazonS3FullAccess** policy
4. Click **Next** to continue

**What is AmazonS3FullAccess?**

- This AWS managed policy grants full access to all S3 operations
- Allows the user to create, read, update, and delete S3 buckets and objects
- Suitable for the Krista S3 Extension to perform all file operations

**Alternative**: For restricted access, you can create a custom policy with specific permissions (
see [Authentication](Authentication.md)).

### Step 7: Review and Create

1. Review the user details and permissions
2. Verify the user name is correct
3. Confirm **AmazonS3FullAccess** policy is attached
4. Click **Create user** to finalize

![User Created](../_media/CreatingAWSUser_user_created.png)

You'll see a success message confirming the user has been created.

### Step 8: Access the New User

1. You'll be returned to the Users list
2. Click on the **user name** you just created

![User Created List](../_media/CreatingAWSUser_user_list.png)

This opens the user details page.

### Step 9: Create Access Key

1. In the user details page, click on the **Security credentials** tab
2. Scroll down to the **Access keys** section
3. Click **Create access key**

![Access Key](../_media/CreatingAWSUser_access_key.png)

### Step 10: Select Use Case

1. Select **Other** as the use case
    - This is appropriate for third-party integrations like Krista
2. Click **Next**

![Others Next](../_media/CreatingAWSUser_others_next.png)

**Note**: AWS may show a warning about using long-term credentials. This is expected for extension integrations.

### Step 11: Add Description (Optional)

1. Optionally, add a description tag for the access key
    - Example: "Access key for Krista S3 Extension"
2. Click **Create access key**

![Optional](../_media/CreatingAWSUser_optional.png)

### Step 12: Retrieve and Store Credentials

1. You'll see the **Access key** and **Secret access key**
2. **Important**: This is the **only time** you can view the Secret access key
3. Click **Download .csv file** to save the credentials securely

![CSV File](../_media/CreatingAWSUser_csv_file.png)

4. Store the CSV file in a secure location
5. Click **Done** when finished

** Critical**: The Secret access key cannot be retrieved again. If you lose it, you must create a new access key.

## Credentials Summary

After completing these steps, you'll have:

| Credential            | Description             | Example                                    |
|-----------------------|-------------------------|--------------------------------------------|
| **Access Key ID**     | 20-character identifier | `AKIAIOSFODNN7EXAMPLE`                     |
| **Secret Access Key** | 40-character secret key | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY` |

## Next Steps

1. **Configure the Extension**: Use the credentials in [Extension Configuration](ExtensionConfiguration.md)
2. **Select Region**: Determine which AWS region to use (see [Authentication](Authentication.md))
3. **Test Connection**: Verify the credentials work by running a simple catalog request

## Security Best Practices

### 1. Credential Management

- **Store securely**: Keep the CSV file in a secure, encrypted location
- **Never commit**: Don't add credentials to source control or share publicly
- **Rotate regularly**: Change access keys every 90 days
- **Delete unused keys**: Remove access keys that are no longer needed

### 2. User Permissions

- **Principle of least privilege**: Only grant necessary permissions
- **Use managed policies**: Prefer AWS managed policies like `AmazonS3FullAccess`
- **Review regularly**: Audit user permissions periodically
- **Monitor usage**: Use AWS CloudTrail to track API calls

### 3. Access Key Security

- **One key per application**: Create separate access keys for different integrations
- **Disable compromised keys**: Immediately disable any exposed keys
- **Enable MFA**: Consider enabling Multi-Factor Authentication on the IAM user
- **Use IAM roles**: For EC2 instances, use IAM roles instead of access keys

## Troubleshooting

### Issue: Cannot Create User

**Cause**: Insufficient permissions

**Resolution**:

- Ensure you're logged in as root user or IAM user with `IAMFullAccess`
- Check your account's IAM user limit hasn't been reached

### Issue: Cannot Attach Policy

**Cause**: Missing permissions to attach policies

**Resolution**:

- Verify you have `iam:AttachUserPolicy` permission
- Use root user or administrator account

### Issue: Access Key Not Working

**Cause**: Credentials not properly configured or permissions issue

**Resolution**:

- Verify Access Key and Secret Key are correct
- Ensure `AmazonS3FullAccess` policy is attached
- Check the user is active and not disabled
- Wait a few minutes for AWS to propagate the new credentials

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - How to configure the extension with credentials
- [Authentication](/pages/Authentication.md) - Understanding AWS Signature authentication
- [AWS IAM User Guide](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_users.html)
- [AWS Security Best Practices](https://docs.aws.amazon.com/IAM/latest/UserGuide/best-practices.html)

