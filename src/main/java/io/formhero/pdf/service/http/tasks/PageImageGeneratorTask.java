package io.formhero.pdf.service.http.tasks;

import io.formhero.pdf.PageImage;
import io.formhero.pdf.PageImageFactory;
import io.formhero.storage.StorageFactory;
import io.formhero.storage.StorageProvider;
import io.formhero.util.FhConfigException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.Callable;

public class PageImageGeneratorTask implements Callable<Throwable>
{
    private static final Logger log = LogManager.getLogger(PageImageGeneratorTask.class.getName());

    private String bucket;
    private String fileFolder;
    private PageImageFactory pageImageFactory;
    private int pageNo;

    private StorageProvider storage;

    public PageImageGeneratorTask(String bucket, String fileFolder, PageImageFactory pageImageFactory, int pageNo) throws FhConfigException
    {
        super();
        this.bucket = bucket;
        this.fileFolder = fileFolder;
        this.pageImageFactory = pageImageFactory;
        this.pageNo = pageNo;
        this.storage = StorageFactory.getStorageProvider();
    }

    public Throwable call()
    {
        try {
            long stopwatch = System.currentTimeMillis();

            PageImage pageImage = pageImageFactory.convertPageToImage(pageNo);
            log.warn("Page " + (pageNo + 1) +  " images generated, timer: " + (System.currentTimeMillis() - stopwatch) + "ms");

            storage.putPageImage(bucket, fileFolder, pageImage);
            log.warn("Page " + (pageNo + 1) + " images saved, timer: " + (System.currentTimeMillis() - stopwatch) + "ms");
            return null;
        } catch (FhConfigException | IOException e) {
            log.error("Exception generating images for page " + (pageNo + 1), e);
            return e;
        }
    }
}