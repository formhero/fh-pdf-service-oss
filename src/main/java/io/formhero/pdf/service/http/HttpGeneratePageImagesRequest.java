package io.formhero.pdf.service.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.formhero.pdf.PageImageFactory;
import io.formhero.pdf.PdfServiceException;
import io.formhero.pdf.metadata.PdfInfo;
import io.formhero.pdf.metadata.PdfMetaDataFactory;
import io.formhero.pdf.service.http.tasks.PageImageGeneratorTask;
import io.formhero.pdf.service.http.tasks.PdfInfoPersisterTask;
import io.formhero.storage.StorageException;
import io.formhero.storage.StorageFactory;
import io.formhero.util.FhConfigException;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by ryan.kimber on 2017-12-10.
 * Implemented by stuart.thompson on 2018-09-06.
 */
@Getter
@Setter
public class HttpGeneratePageImagesRequest extends HttpPdfRequest {

    private static final Logger log = LogManager.getLogger(HttpGeneratePageImagesRequest.class.getName());

    // Create thread pool for rendering PDF pages concurrently
    // Ensure threadCount is reasonably sized to min: 4, max: 16, prefer: #CPUs
    private int cpuCount = Runtime.getRuntime().availableProcessors();
    private int cpuMaxCount = (cpuCount > 16) ? 16 : cpuCount;
    private int threadCount = (cpuMaxCount < 4) ? 4 : cpuMaxCount;
    private ExecutorService executor = Executors.newWorkStealingPool(threadCount);

    private String bucket;
    private String fileFolder;
    private String fileName;

    @JsonProperty("base64Pdf")
    private byte[] pdfBytes;

    public byte[] getPdfBytes() throws StorageException, IllegalArgumentException
    {
        if (pdfBytes != null && pdfBytes.length > 0) {
            return pdfBytes;
        }
        else {
            if (pdfBytes == null && fileFolder == null) {
                throw new IllegalArgumentException("The addPdfAction must have a fileFolder.");
            }
            if (pdfBytes == null && fileName == null) {
                throw new IllegalArgumentException("The addPdfAction must have a fileName.");
            }
            else {
                try {
                    pdfBytes = StorageFactory.getStorageProvider().getObject(bucket, fileFolder, fileName);
                    return pdfBytes;
                }
                catch (FhConfigException fhce) {
                    throw new StorageException("Unable to load resource from storage due to FhConfigException:", fhce);
                }
            }
        }
    }

    /**
     * Multi-threaded implementation to generate PDF info, image and thumbnails.
     * Each PDF page is processed in parallel using threadpool.
     *
     * @throws PdfServiceException
     */
    public void buildPdfPageImage() throws PdfServiceException
    {
        long stopwatch = System.currentTimeMillis();

        try {
            // Get PDF from storage and generate PDF info
            PdfInfo pdfInfo = PdfMetaDataFactory.buildPdfInfo(getPdfBytes());
            log.warn("PDF info generation complete, timer: " + (System.currentTimeMillis() - stopwatch) + "ms");

            // Add task to persist PDF to both storage and Redis
            ArrayList<Callable<Throwable>> taskList = new ArrayList<Callable<Throwable>>();
            taskList.add(new PdfInfoPersisterTask(bucket, fileFolder, pdfInfo, sessionId, requestId));

            // Add task for each PDF page to persist image and thumbnails
            PageImageFactory pageImageFactory = new PageImageFactory(pdfBytes);
            for (int pageNo = 0; pageNo < pdfInfo.getNumberOfPages(); pageNo++) {
                log.info("Adding PageImageGeneratorTask for page " + (pageNo + 1));
                taskList.add(new PageImageGeneratorTask(bucket, fileFolder, pageImageFactory, pageNo));
            }

            // Execute the task list and wait for all threads to complete
            log.info("Executor threadpool #cpus " + cpuCount + ", #threads: " + threadCount);
            boolean allSucceeded = true;
            Throwable firstError = null;
            List<Future<Throwable>> results = executor.invokeAll(taskList);
            for (Future<Throwable> result : results) {
                if (result.get() != null) {
                    allSucceeded = false;
                    firstError = result.get();
                    break;
                }
            }

            pageImageFactory.close();

            if (!allSucceeded) {
                throw new PdfServiceException("Error generating at least one page image", firstError);
            }
            log.warn("All PDF info and page images generated and persisted");
        }
        catch (StorageException | FhConfigException se) {
            throw new PdfServiceException("Error while storing PDF caused by " + se.getClass().getName() + ", error is: " + se.getMessage(), se);
        }
        catch (IOException | InterruptedException | ExecutionException e) {
            throw new PdfServiceException("Error while building PDF caused by " + e.getClass().getName() + ", error is: " + e.getMessage(), e);
        }
        finally {
            log.warn("Converted PDF to metadata and images, timer: " + (System.currentTimeMillis() - stopwatch) + "ms");
        }
    }
}
