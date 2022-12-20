package io.formhero.pdf.service.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.formhero.pdf.PdfServiceException;
import io.formhero.pdf.PdfUtils;
import io.formhero.pdf.metadata.PdfFieldInfo;
import io.formhero.pdf.overlays.AbstractPdfStamp;
import io.formhero.storage.StorageException;
import io.formhero.storage.StorageFactory;
import io.formhero.util.FhConfigException;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ryan.kimber on 2017-12-10.
 */
@Getter
@Setter
public class HttpMergePdfDataRequest extends HttpPdfRequest
{
    private static final Logger log = LogManager.getLogger(HttpMergePdfDataRequest.class.getName());

    private String bucket;
    private String fileFolder;
    private String fileName;

    @JsonProperty("base64Pdf")
    private byte[] pdfBytes;
    private List<AbstractPdfStamp> overlays;
    private List<AbstractPdfStamp> underlays;
    private Map<String, PdfFieldInfo> formData = new HashMap<String, PdfFieldInfo>();


    /**
     * This method uses the PDF bytes from the payload object; otherwise take the PDF from S3
     * storage at the specified bucket, folder and file.
     *
     * @return the byte array
     */
    public byte[] getPdfBytes() throws StorageException, IllegalArgumentException
    {
        if (pdfBytes != null && pdfBytes.length > 0) {
            log.info("using PDF from payload"); // debug
            return pdfBytes;
        }
        else {
            if (pdfBytes == null && fileFolder == null) throw new IllegalArgumentException("The mergePdfAction must have a fileFolder or include PDF bytes.");
            if (pdfBytes == null && fileName == null) throw new IllegalArgumentException("The mergePdfAction must have a fileName or include PDF bytes.");
            else {
                try {
                    log.info("getting PDF from S3 bucket " + getBucket() +
                        "/" + getFileFolder() + "/" + getFileName() +
                        " for session " + getSessionId()); // debug
                    pdfBytes = StorageFactory.getStorageProvider().getObject(bucket, fileFolder, fileName);
                    return pdfBytes;
                }
                catch (FhConfigException fhce) {
                    throw new StorageException("Unable to load resource from storage due to FhConfigException:", fhce);
                }
            }
        }
    }

    public byte[] getResultingPdf() throws PdfServiceException
    {
        try {
            byte[] resultPdfBytes = PdfUtils.mergeFormDataIntoPdf(this.formData, getPdfBytes());
            if (this.overlays != null && this.overlays.size() > 0) resultPdfBytes = PdfUtils.doOverlays(resultPdfBytes, overlays);
            if (this.underlays != null && this.underlays.size() > 0) resultPdfBytes = PdfUtils.doUnderlays(resultPdfBytes, underlays);
            return resultPdfBytes;
        }
        catch (IllegalArgumentException | IOException e) {
            throw new PdfServiceException("Error while building PDF caused by " + e.getClass().getName() + ", error is: " + e.getMessage(), e);
        }
    }
}
