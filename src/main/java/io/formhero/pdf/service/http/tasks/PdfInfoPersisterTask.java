package io.formhero.pdf.service.http.tasks;

import io.formhero.pdf.metadata.PdfInfo;
import io.formhero.storage.StorageFactory;
import io.formhero.storage.StorageProvider;
import io.formhero.util.FhConfigException;
import io.formhero.util.FhCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

public class PdfInfoPersisterTask implements Callable<Throwable>
{
    private static final Logger log = LogManager.getLogger(PdfInfoPersisterTask.class.getName());

    private String bucket;
    private String fileFolder;
    private PdfInfo pdfInfo;
    private String sessionId;
    private String requestId;

    private StorageProvider storage;

    public PdfInfoPersisterTask(String bucket, String fileFolder, PdfInfo pdfInfo, String sessionId, String requestId) throws FhConfigException
    {
        super();
        this.bucket = bucket;
        this.fileFolder = fileFolder;
        this.pdfInfo = pdfInfo;
        this.sessionId = sessionId;
        this.requestId = requestId;
        this.storage = StorageFactory.getStorageProvider();
    }

    public Throwable call() {
        try {
            long stopwatch = System.currentTimeMillis();

            // Write the JSON data to both storage and Redis
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String pdfInfoAsString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(pdfInfo);
            FhCache.set("application/json", sessionId, requestId, pdfInfoAsString);

            storage.putPdfMetaData(bucket, fileFolder, pdfInfo);
            log.warn("Saved PDF metadata to storage, timer: " + (System.currentTimeMillis() - stopwatch) + "ms");
            return null;
        }
        catch (Throwable e) {
            return e;
        }
    }
}
