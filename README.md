# fh-pdf-refinery

The fh-pdf-refinery receives HTTP requests for these endpoints:

- /generatePageImage - Import the PDF template and generate metadata description and GIF images for Studio.
  The PDF is retrieved from S3 storage, then the generated metadata and page images are stored to the same S3 bucket.
  To improve performance, a thread pool is used to render images of each page independently.
- /fill - Merge request data to the PDF template. The PDF is received as a base-64 byte array, and form data is a JSON
  object, then the form data is merged into the PDF and returned as a base-64 byte array response.
- /merge - Merge request data and combine PDFs into one bundle. The PDFs are received as an array of base-64 byte
  arrays, then combined into a base-64 byte array response.
- /examine - Examine the PDF and reveal the form field data. The PDf is received as a base-64 byte array, then the
  form data fields are extracted and returned as a JSON object.

## Building

`mvn package`

## Configuration

PDF Service will look for configuration in mongodb. Several environment variables must be set to bootstrap this process.
These are listed in the `local-run.sh` script.

The `docker-compose.yaml` file can be used to launch a local instance of mongodb. In this instance, you will need to:

1. Create a database called `pdf-service`
2. Create a collection within that database called `formhero-configs`
3. Create a document in the collection with appropriate config (see below)

A basic configuration document looks like:

```json
{
  "environmentOwner": "test",
  "environment": "test",
  "config": {
    "app": {
      "name": "pdf-service"
    },
    "redis": {
      "connectOptions": {
        "host": "localhost",
        "port": "6379"
      }
    }
  }
}
```
