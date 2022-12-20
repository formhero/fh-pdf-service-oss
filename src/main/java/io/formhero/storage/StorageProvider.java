package io.formhero.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import io.formhero.pdf.PageImage;
import io.formhero.pdf.metadata.PdfInfo;
import io.formhero.util.FhConfigException;
import org.apache.commons.jcs.JCS;
import org.apache.commons.jcs.access.CacheAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by ryan.kimber on 2017-10-03.
 */
public abstract class StorageProvider {

    private static final Logger log = LogManager.getLogger(StorageProvider.class.getName());
    public abstract byte[] getObject(String bucketName, String folderName, String fileName) throws StorageException, FhConfigException;
    public abstract void putPageImage(String bucketName, String folderName, PageImage pageImage) throws StorageException, FhConfigException;
    public abstract void putPdfMetaData(String bucketName, String folderName, PdfInfo metadata) throws StorageException, FhConfigException;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static ObjectMapper mapper = new ObjectMapper();

    private static CacheAccess<String, CachedFile> imageCache = null;
    private static CacheAccess<String, CachedFile> pdfCache = null;

    protected static CacheAccess<String, CachedFile> getImageCache() {
        if(imageCache == null) {
            try {
                imageCache = JCS.getInstance("imageCache");
            } catch (Exception e) {
                log.error("Unable to initialize the Overlay Image File Cache: ", e);
                System.exit(1);
            }
        }

        return imageCache;
    }

    protected static CacheAccess<String, CachedFile> getPdfCache() {
        if(pdfCache == null) {
            try {
                pdfCache = JCS.getInstance("pdfCache");
            } catch (Exception e) {
                log.error("Unable to initialize the PDF File Cache: ", e);
                System.exit(1);
            }
        }

        return pdfCache;
    }

    protected static CacheAccess<String, CachedFile> getCache(String cacheName) {

        switch(cacheName)
        {
            case "pdf-templates":
                return getPdfCache();
            case "formhero-images":
            case "pdf-overlay-images":
                return getImageCache();
            default:
                return getFalseCache();
        }
    }

    protected static CacheAccess<String, CachedFile> getFalseCache()
    {
        try {
            return JCS.getInstance("default");
        } catch (Exception e) {
            log.error("Unable to initialize the default File Cache: ", e);
            System.exit(1);
        }
        return null;
    }

    protected String convertMetaDataToString(PdfInfo metadata) throws JsonProcessingException {
        return mapper.writeValueAsString(metadata);
    }
}
