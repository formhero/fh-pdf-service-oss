package io.formhero.pdf;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Examine the PDF field values.
 */
public class DataExaminer {

  /**
   * Examine the form data fields of the specified PDF.
   *
   * @param pdfBytes input PDF byte array
   * @return map of name/value pairs
   */
  public Map<String, String> examineData(byte[] pdfBytes) throws PdfServiceException {

    // Error collection
    Map<String, String> formData = new HashMap<String, String>();

    PdfReader reader = null;
    try {
      // Read PDF file
      ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
      reader = new PdfReader(bais);
      AcroFields fields = reader.getAcroFields();

      // Translate ACRO object into map
      Set<String> fieldNames = fields.getFields().keySet();
      for (String fieldName: fieldNames) {
        String fieldValue = fields.getField(fieldName);
        formData.put(fieldName, fieldValue);
      }

    } catch (Exception e) {
      throw new PdfServiceException("Failed to examine PDF", e);
    } finally {
      if (reader != null)
        reader.close();
    }

    // Report errors
    return formData;
  }
}