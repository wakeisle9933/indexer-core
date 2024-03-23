<img width="650" alt="quick search indexer" src="https://github.com/wakeisle9933/indexer-core/assets/73478472/f153c51b-ffb1-43f5-bb43-bdcea6887def">

**Quickly process up to 200 indexes daily with just a click by submitting your domain's sitemap!**

## Usage

1. Create a project on [Google Cloud](https://console.cloud.google.com/welcome?hl=en&organizationId=0&supportedpurview=project).

2. Register for the [Google Web Search Indexing API](https://console.cloud.google.com/marketplace/product/google/indexing.googleapis.com?project=mcg-quickindexer).

3. In Google Cloud, navigate to IAM & Admin > [Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts?hl=en) and create a Service Account.

4. Go to APIs & Services > [Credentials](https://console.cloud.google.com/apis/credentials?referrer=search&hl=en), click on Create Credentials, then select Service Account and generate a JSON file.

5. Rename the generated Crentials.json file to `credential.json` and place it in the `src/main/resources/credential` directory.

6. Now, by submitting your sitemap and clicking Send API, you can create up to 200 indexes for free (it may take up to 10 minutes).

## Q&A

**Q) The Google Indexing API is designated for Jobposting & Broadcasting purposes; can it be used for other purposes?**

**A)** Google states that using the API for purposes other than specified may result in penalties. However, many cases have proven that it can be used comfortably without any penalties. But, It's important to note that being penalty-free today does not guarantee safety tomorrow. Therefore, this API should only be used when the website has low traffic initially. As the site's traffic increases, Google will automatically create indexes within an hour, eliminating the need for its use.
