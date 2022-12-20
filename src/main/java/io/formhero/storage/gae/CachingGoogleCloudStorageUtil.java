package io.formhero.storage.gae;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;
import io.formhero.pdf.PageImage;
import io.formhero.pdf.metadata.PdfInfo;
import io.formhero.storage.CachedFile;
import io.formhero.storage.StorageException;
import org.apache.commons.jcs.access.exception.CacheException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by ryankimber on 2015-11-19.
 */
public class CachingGoogleCloudStorageUtil extends io.formhero.storage.StorageProvider {
    private static final Logger log = LogManager.getLogger(CachingGoogleCloudStorageUtil.class.getName());
    /** Global configuration of Google Cloud Storage OAuth 2.0 scope. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = System.getProperty("GCE_APPLICATION_NAME", "formhero-io");
    private static final String STORAGE_SCOPE = "https://www.googleapis.com/auth/devstorage.read_write";
    private static GoogleCredential credential;
    private static Storage theStorageService;

    private static Storage getStorageService() throws IOException, GeneralSecurityException {
        if (null == theStorageService) {
            synchronized(log) {
                if(theStorageService == null) {
                    GoogleCredential credential = GoogleCredential.getApplicationDefault();
                    // Depending on the environment that provides the default credentials (e.g. Compute Engine,
                    // App Engine), the credentials may require us to specify the scopes we need explicitly.
                    // Check for this case, and inject the Cloud Storage scope if required.
                    if (credential.createScopedRequired()) {
                        credential = credential.createScoped(StorageScopes.all());
                    }
                    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                    theStorageService = new Storage.Builder(httpTransport, JSON_FACTORY, credential)
                            .setApplicationName(APPLICATION_NAME).build();
                }
            }
        }
        return theStorageService;
    }

    public byte[] getObject(String bucketName, String folderName, String fileName) throws StorageException {
        long start = System.currentTimeMillis();

        String type = folderName.indexOf('/') == -1 ? folderName : folderName.substring(0, folderName.indexOf('/', 0));
        String objectKey = folderName + "/" + fileName;
        String cacheKey = bucketName + "::" + objectKey;
        CachedFile cachedFile = getCache(type).get(cacheKey);

        if (cachedFile != null) {
            return cachedFile.getBytes();
        } else {

            try {
                Storage storageService = getStorageService();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                storageService.objects().get(bucketName, objectKey).executeMediaAndDownloadTo(baos);
                cachedFile = new CachedFile(bucketName, objectKey, baos.toByteArray());
                getCache(type).put(cacheKey, cachedFile);
                return cachedFile.getBytes();

            } catch (CacheException ce)
            {
                throw new StorageException("Error putting file into our cache!", ce);
            }
            catch(StorageException se)
            {
                throw se;
            } catch (Throwable t) {
                throw new StorageException("Error while retrieving " + bucketName + " :: " + objectKey + " from storage:", t);
            } finally {
                log.info("Retrieving " + objectKey + " from " + bucketName + " took " + (System.currentTimeMillis() - start) + "ms.");
            }
        }
    }

    public void putPageImage(String bucketName, String folderName, PageImage pageImage) throws GoogleCloudStorageException {

        folderName += "/images/";
        saveImage(bucketName, folderName, "page-" + pageImage.getPageNumber() + ".300dpi.png", pageImage.getHighResImage());
        saveImage(bucketName, folderName, "page-" + pageImage.getPageNumber() + ".thumb144.png", pageImage.getThumbnail144());
        saveImage(bucketName, folderName, "page-" + pageImage.getPageNumber() + ".thumb576.png", pageImage.getThumbnail576());
    }

    public void putPdfMetaData(String bucketName, String folderName, PdfInfo metadata) throws GoogleCloudStorageException
    {
        try {
            Storage storageService = getStorageService();

            StorageObject storageObject = new StorageObject();
            storageObject.setName(folderName + "/pdfInfo.json");

            String metadataAsString = super.convertMetaDataToString(metadata);
            InputStreamContent content = new InputStreamContent("application/json", new ByteArrayInputStream(metadataAsString.getBytes()));
            content.setLength(metadataAsString.length());
            Storage.Objects.Insert insertObject = storageService.objects().insert(bucketName, storageObject, content);

            insertObject.execute();
        }
        catch(GeneralSecurityException | IOException e)
        {
            throw new GoogleCloudStorageException("Unable to save image to GCS:", e);
        }

    }

    private static void saveImage(String bucketName, String objectFolder, String fileName, byte[] bytes) throws GoogleCloudStorageException
    {
        try {
            Storage storageService = getStorageService();

            StorageObject storageObject = new StorageObject();
            storageObject.setName(objectFolder + fileName);

            InputStreamContent content = new InputStreamContent("image/png", new ByteArrayInputStream(bytes));
            content.setLength(bytes.length);
            Storage.Objects.Insert insertObject = storageService.objects().insert(bucketName, storageObject, content);

            insertObject.execute();
        }
        catch(GeneralSecurityException | IOException e)
        {
            throw new GoogleCloudStorageException("Unable to save image to GCS:", e);
        }
    }
}
