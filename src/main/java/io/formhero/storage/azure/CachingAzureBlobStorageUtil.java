package io.formhero.storage.azure;

import io.formhero.pdf.PageImage;
import io.formhero.pdf.metadata.PdfInfo;
import io.formhero.storage.CachedFile;
import io.formhero.storage.StorageProvider;
import io.formhero.util.FhConfig;
import io.formhero.util.FhConfigException;
import org.apache.commons.jcs.access.exception.CacheException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.microsoft.azure.storage.*;
import com.microsoft.azure.storage.blob.*;

/**
 * Created by stuart.thompson on 2021-10-14.
 */
public class CachingAzureBlobStorageUtil extends StorageProvider {
    private static final Logger log = LogManager.getLogger(CachingAzureBlobStorageUtil.class.getName());

    // Established name for PDF artifacts
    private static final String BLOB_CONTAINER_NAME = "pdf-templates";
    private static final String BLOB_CONTAINER_PATH = BLOB_CONTAINER_NAME + "/";

    // Singleton variables
    private static FhConfig fhConfig;
    private static CloudBlobContainer _blobContainer;

    public CachingAzureBlobStorageUtil(FhConfig config) {
        this.fhConfig = config;
    }

    /**
     * Get the blob storage container, using settings from templateStorage configuration.
     */
    private static CloudBlobContainer getBlobContainer() throws StorageException, Exception
    {
        if (_blobContainer == null) {
            // Format connection string from FhConfig.templateStorage
            String accountName = fhConfig.getString("templateStorage.accountName");
            String accountKey = fhConfig.getString("templateStorage.accountKey");
            StringBuilder connectionString = new StringBuilder("DefaultEndpointsProtocol=https");
            connectionString.append(";AccountName=").append(accountName);
            connectionString.append(";AccountKey=").append(accountKey);
            connectionString.append(";EndpointSuffix=core.windows.net");
            log.debug("Azure connection: " + connectionString.toString());

            log.debug("creating new Azure blob container");
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(connectionString.toString());
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

            // Ensure PDF blob container exists
            _blobContainer = blobClient.getContainerReference(BLOB_CONTAINER_NAME);
            _blobContainer.createIfNotExists();
            log.debug("created new Azure blob container");
        } else {
            log.debug("reusing existing Azure blob container");
        }
        return _blobContainer;
    }

    /**
     * Get the content from blob storage, in byte array form.
     *
     * @param bucketName the storage bucket (replaced in Azure by storage account)
     * @param folderName storage folder name
     * @param fileName image filename
     * @return content in byte array form
     */
    public byte[] getObject(String bucketName, String folderName, String fileName) throws AzureBlobStorageException, FhConfigException
    {
        long start = System.currentTimeMillis();

        // Adjust for Azure blob container path name
        folderName = folderName.replaceFirst(BLOB_CONTAINER_PATH, "");
        String type = folderName.indexOf('/') == -1 ? folderName : folderName.substring(0, folderName.indexOf('/', 0));
        String objectKey = folderName + "/" + fileName;
        String cacheKey = bucketName + "::" + objectKey;
        CachedFile cachedFile = getCache(type).get(cacheKey);

        if (cachedFile != null) {
            log.debug("retrieve from cache " + objectKey);
            return cachedFile.getBytes();
        } else {
            // Get content from storage
            log.debug("retrieve from Azure " + objectKey);
            BlobInputStream inputStream = null;
            byte[] objectBytes = null;
            try {
                // Read content from blob storage
                CloudBlobContainer blobContainer = getBlobContainer();
                CloudBlockBlob blobRef = blobContainer.getBlockBlobReference(objectKey);
                if (blobRef.exists()) {
                    long length = blobRef.getProperties().getLength();
                    inputStream = blobRef.openInputStream();
                    objectBytes = new byte[(int) length];
                    inputStream.read(objectBytes);
                    log.debug("retrieved from Azure " + objectKey);

                    // Add content to cache
                    cachedFile = new CachedFile(bucketName, objectKey, objectBytes);
                    getCache(type).put(cacheKey, cachedFile);
                    log.info("Retrieving " + objectKey + " from " + bucketName + " took " + (System.currentTimeMillis() - start) + "ms.");
                    return cachedFile.getBytes();
                } else {
                    log.warn("File at " + objectKey + " does not exist");
                    throw new AzureBlobStorageException("Error retrieving " + bucketName + " :: " + objectKey + " from Azure - does not exist");
                }

            } catch (StorageException se) {
                throw new AzureBlobStorageException("Error saving blob storage " + folderName, se);
            } catch (CacheException ce) {
                throw new AzureBlobStorageException("Error putting file into our cache!", ce);
            } catch (Throwable t) {
                throw new AzureBlobStorageException("Error retrieving " + bucketName + " :: " + objectKey + " from Azure:", t);
            } finally {
                // Cleanup
                if (inputStream != null) {
                    try { inputStream.close(); } catch (Throwable t) {}
                }
            }
        }
    }

    /**
     * Save the thumbnail page images object of the PDF to the specified folder name.
     * Each image is serialized into a byte array and persisted.
     *
     * @param bucketName the storage bucket (replaced in Azure by storage account)
     * @param folderName storage folder name
     * @param pageImage thumbnail page images object
     */
    public void putPageImage(String bucketName, String folderName, PageImage pageImage) throws AzureBlobStorageException, FhConfigException
    {
        folderName += "/images/";
        _saveContent(bucketName, folderName, "page-" + pageImage.getPageNumber() + ".300dpi.png", pageImage.getHighResImage());
        _saveContent(bucketName, folderName, "page-" + pageImage.getPageNumber() + ".thumb144.png", pageImage.getThumbnail144());
        _saveContent(bucketName, folderName, "page-" + pageImage.getPageNumber() + ".thumb576.png", pageImage.getThumbnail576());
    }

    /**
     * Save the metadata description of the PDF to the specified folder name.
     * The metadata object is serialized into a byte array and persisted.
     *
     * @param bucketName the storage bucket (replaced in Azure by storage account)
     * @param folderName storage folder name
     * @param metadata metadata object describing the PDF
     */
    public void putPdfMetaData(String bucketName, String folderName, PdfInfo metadata) throws AzureBlobStorageException, FhConfigException
    {
        String metadataAsString = null;
        try {
            metadataAsString = super.convertMetaDataToString(metadata);
        } catch (Exception e) {
            throw new AzureBlobStorageException("Error parsing metadata " + folderName, e);
        }
        _saveContent(bucketName, folderName, "pdfInfo.json", metadataAsString.getBytes());
    }

    /**
     * Save the image content which is in byte array form.
     *
     * @param bucketName the storage bucket (replaced in Azure by storage account)
     * @param folderName storage folder name
     * @param fileName image filename
     * @param bytes image byte array
     */
    private static void _saveContent(String bucketName, String folderName, String fileName, byte[] bytes) throws AzureBlobStorageException, FhConfigException
    {
        // Adjust for Azure blob container path name
        folderName = folderName.replaceFirst(BLOB_CONTAINER_PATH, "");
        log.debug("save content to " + folderName + "/" + fileName);

        BlobOutputStream outputStream = null;
        try {
            // Ensure there aren't any double-slashes. They mess up S3 paths.
            String outputFilePath = folderName.trim() + "/" + fileName.trim();
            outputFilePath = outputFilePath.replaceAll("(\\/{2,})", "/");

            // Write content to blob storage
            CloudBlobContainer blobContainer = getBlobContainer();
            CloudBlockBlob blobRef = blobContainer.getBlockBlobReference(outputFilePath);
            outputStream = blobRef.openOutputStream();
            outputStream.write(bytes);
            log.debug("saved image to " + folderName);
        } catch (StorageException se) {
            throw new AzureBlobStorageException("Error saving blob storage " + folderName, se);
        } catch (Exception e) {
            throw new AzureBlobStorageException("Error saving " + folderName, e);
        } finally {
            // Cleanup
            if (outputStream != null) {
                try { outputStream.close(); } catch (Throwable t) {}
            }
        }
    }
}
