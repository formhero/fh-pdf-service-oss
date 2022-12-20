package io.formhero.pdf.service.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.formhero.pdf.DataExaminer;
import io.formhero.pdf.PdfServiceException;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Getter
@Setter
public class HttpExaminePdfDataRequest extends HttpPdfRequest
{
  private static final Logger log = LogManager.getLogger(HttpExaminePdfDataRequest.class.getName());

  @JsonProperty("base64Pdf")
  private byte[] pdfBytes;

  public Map<String, String> getFormDataJson() throws PdfServiceException
  {
    if (pdfBytes == null || pdfBytes.length == 0)
      throw new PdfServiceException("Missing PDF in examine request object");

    try {
      DataExaminer examiner = new DataExaminer();
      return examiner.examineData(pdfBytes);
    }
    catch (IllegalArgumentException e) {
      throw new PdfServiceException("Error while examining PDF caused by " + e.getClass().getName() + ", error is: " + e.getMessage(), e);
    }
  }
}
