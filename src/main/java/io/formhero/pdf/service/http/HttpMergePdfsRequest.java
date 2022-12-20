package io.formhero.pdf.service.http;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfCopy.PageStamp;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import io.formhero.pdf.PdfServiceException;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * Created by ryan.kimber on 2017-12-10.
 */
@Getter
@Setter
public class HttpMergePdfsRequest extends HttpPdfRequest {

    private List<String> base64Files;

    public byte[] getResultingPdf() throws PdfServiceException
    {
        try {
            Document document = new Document();
            ByteArrayOutputStream mergedOutputStream = new ByteArrayOutputStream();

            PdfCopy copy = new PdfCopy(document, mergedOutputStream);
            PageStamp stamp;
            int n;
            int pageNo = 0;
            PdfImportedPage page;

            // Ensure base 64 files is not empty before building the merged document
            if (!base64Files.isEmpty()) {
                document.open();
                for (String base64Pdf : base64Files) {
                    byte[] decoded = Base64.getDecoder().decode(base64Pdf);
                    PdfReader pdf = new PdfReader(new ByteArrayInputStream(decoded));
                    ByteArrayOutputStream flattenedPdfStream = new ByteArrayOutputStream();
                    PdfStamper stamper = new PdfStamper(pdf, flattenedPdfStream);
                    stamper.setFormFlattening(true);
                    stamper.close();

                    PdfReader flattenedPdf = new PdfReader(new ByteArrayInputStream(flattenedPdfStream.toByteArray()));

                    n = flattenedPdf.getNumberOfPages();
                    for (int i = 0; i < n; ) {
                        pageNo++;
                        // Setting flag to keep tagged PDF structure is failing for some reason: this may be fixed in subsequent releases of iText.
                        // page = copy.getImportedPage(pdf, ++i, true); // keepTaggedPdfStructure: true
                        page = copy.getImportedPage(flattenedPdf, ++i);
                        copy.addPage(page);
                    }
                }
                document.close();
            }

            return mergedOutputStream.toByteArray();
        }
        catch(IOException | DocumentException e)
        {
            throw new PdfServiceException("Unable to merge PDFs due to exception: " + e.getClass().getName());
        }
    }
}
