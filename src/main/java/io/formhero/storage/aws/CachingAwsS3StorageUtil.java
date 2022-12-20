package io.formhero.storage.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import io.formhero.aws.AwsCredentials;
import io.formhero.pdf.PageImage;
import io.formhero.pdf.metadata.PdfInfo;
import io.formhero.storage.CachedFile;
import io.formhero.storage.StorageProvider;
import io.formhero.util.FhConfigException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by ryan.kimber on 2017-10-03.
 */
public class CachingAwsS3StorageUtil extends StorageProvider
{
    private static final Logger log = LogManager.getLogger(CachingAwsS3StorageUtil.class.getName());
    private static AmazonS3 s3Client = null;

    private static AmazonS3 getS3Client() throws FhConfigException {
        if (s3Client == null) {
            if(System.getenv("AWS_CONFIGURATION") != null)
            {
                AwsCredentials credentials = AwsCredentials.loadFromFile(System.getenv("AWS_CONFIGURATION"));
                AWSStaticCredentialsProvider awsCredentials = credentials.getAwsCredentials();
                AmazonS3ClientBuilder s3ClientBuilder = AmazonS3Client.builder();
                s3ClientBuilder.withCredentials(awsCredentials);
                s3ClientBuilder.setRegion(credentials.getRegion());
                return s3ClientBuilder.build();
            }
            else
            {
                s3Client = AmazonS3ClientBuilder.standard().build();
            }
        }
        return s3Client;
    }

    public byte[] getObject(String bucketName, String folderName, String fileName) throws AwsS3StorageException {
        long start = System.currentTimeMillis();

        String type = folderName.indexOf('/') == -1 ? folderName : folderName.substring(0, folderName.indexOf('/', 0));
        String objectKey = folderName + "/" + fileName;
        String cacheKey = bucketName + "::" + objectKey;
        CachedFile cachedFile = getCache(type).get(cacheKey);

        if (cachedFile != null) {
            return cachedFile.getBytes();
        } else {
            // Get content from storage
            try {
                AmazonS3 s3Client = getS3Client();
                S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketName, objectKey));
                byte[] objectBytes = IOUtils.toByteArray(s3Object.getObjectContent());
                cachedFile = new CachedFile(bucketName, objectKey, objectBytes);
                getCache(type).put(cacheKey, cachedFile);
                log.info("Retrieving " + objectKey + " from " + bucketName + " took " + (System.currentTimeMillis() - start) + "ms.");
                return cachedFile.getBytes();

            } catch (CacheException ce) {
                throw new AwsS3StorageException("Error putting file into our cache!", ce);
            } catch (Throwable t) {
                throw new AwsS3StorageException("Error while retrieving " + bucketName + " :: " + objectKey + " from S3:", t);
            }
        }
    }

    public void putPageImage(String bucketName, String folderName, PageImage pageImage) throws AwsS3StorageException, FhConfigException
    {
        folderName += "/images/";
        saveImage(bucketName, folderName, "page-" + pageImage.getPageNumber() + ".300dpi.png", pageImage.getHighResImage());
        saveImage(bucketName, folderName, "page-" + pageImage.getPageNumber() + ".thumb144.png", pageImage.getThumbnail144());
        saveImage(bucketName, folderName, "page-" + pageImage.getPageNumber() + ".thumb576.png", pageImage.getThumbnail576());
    }

    public void putPdfMetaData(String bucketName, String folderName, PdfInfo metadata) throws AwsS3StorageException, FhConfigException
    {
        log.info("Attempting to save field info to " + bucketName + "::/" + folderName + "...");
        try {
            AmazonS3 s3Client = getS3Client();
            String metadataAsString = super.convertMetaDataToString(metadata);
            String outputFilePath = folderName.trim() + "/pdfInfo.json";
            //Ensure there aren't any double-slashes. AWS S3 doesn't like that.
            outputFilePath = outputFilePath.replaceAll("(\\/{2,})", "/");
            ByteArrayInputStream byteStream = new ByteArrayInputStream(metadataAsString.getBytes());
            ObjectMetadata s3MetaData = new ObjectMetadata();
            s3MetaData.setContentLength(metadataAsString.getBytes().length);
            s3MetaData.setContentType("application/json");
            s3Client.putObject(new PutObjectRequest(bucketName, outputFilePath, byteStream, s3MetaData));
        } catch (IOException e) {
            throw new AwsS3StorageException("Error saving " + bucketName + " :: " + folderName, e);
        } finally {
            log.info("Saved " + folderName + " from " + bucketName);
        }
    }

    private static void saveImage(String bucketName, String objectFolder, String fileName, byte[] bytes) throws AwsS3StorageException, FhConfigException
    {
        String outputFilePath = objectFolder.trim() + "/" + fileName.trim();
        //Ensure there aren't any double-slashes. They mess up S3 paths.
        outputFilePath = outputFilePath.replaceAll("(\\/{2,})", "/");
        log.info("Attempting to save " + outputFilePath + "...");
        AmazonS3 s3Client = getS3Client();
        ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
        ObjectMetadata s3MetaData = new ObjectMetadata();
        s3MetaData.setContentLength(bytes.length);
        s3MetaData.setContentType("image/png");
        s3Client.putObject(new PutObjectRequest(bucketName, outputFilePath, byteStream, s3MetaData));
    }

    public static void main(String[] args)
    {
        String path = "some/path//that///has////duplicate/slashes///in/it";
        path = path.replaceAll("(\\/{2,})", "/");
        System.out.println("This is the substituted value:\r\n" + path);
    }

}
