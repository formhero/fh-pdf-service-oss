package io.formhero.pdf.metadata;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import io.formhero.pdf.PdfServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by ryankimber on 2016-03-13.
 */
public class PdfMetaDataFactory
{
    private static final Logger log = LogManager.getLogger(PdfMetaDataFactory.class.getName());
    private static RandomAccessSourceFactory sourceFactory = new RandomAccessSourceFactory();

    public static PdfInfo buildPdfInfo(byte[] pdfBytes) throws PdfServiceException {
        PdfReader reader = null;
        try {
            //PDFReader doesn't parse all of the PDF on construction if you use RandomAccessFileOrArray
            reader = new PdfReader(new RandomAccessFileOrArray(sourceFactory.createSource(pdfBytes)), null);
            log.warn("Done creating the reader...");

            AcroFields fields = reader.getAcroFields();
            log.warn("Grabbed the fields...");

            // Populate PDF info
            PdfInfo pdfInfo = new PdfInfo();
            pdfInfo.setNumberOfPages(reader.getNumberOfPages());
            for (int i = 0; i < pdfInfo.getNumberOfPages(); i++) {
                pdfInfo.addPageInfo(buildPageInfo(i, reader));
            }

            Set<String> fldNames = fields.getFields().keySet();
            for (String fldName : fldNames) {
                pdfInfo.addPdfField(buildPdfFieldInfo(fldName, fields));
            }
            return pdfInfo;
        }
        catch(IOException ioe) {
            throw new PdfServiceException("Unable to read PDF:", ioe);
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    /**
     * Populate the page info object from the PDF content.
     *
     * @param pageIndex PDF page number
     * @param pdfReader PDF reader
     * @return page info object
     */
    private static PdfPageInfo buildPageInfo(int pageIndex, PdfReader pdfReader) {
        PdfPageInfo pageInfo = new PdfPageInfo();
        int pageNumber = pageIndex + 1;
        pageInfo.setPageNumber(pageNumber);

        pageInfo.setRotation(pdfReader.getPageRotation(pageNumber));
        Rectangle pageSize = pdfReader.getPageSize(pageNumber);
        pageInfo.setWidth(pageSize.getWidth());
        pageInfo.setHeight(pageSize.getHeight());

        //We could get extended data here from PDF Box / some other interrogator for each page.
        return pageInfo;
    }

    /**
     * Populate the page field info object from the PDF content.
     *
     * @param fieldName form field name
     * @param acroFields PDF form fields
     * @return page field info object
     */
    private static PdfFieldInfo buildPdfFieldInfo(String fieldName, AcroFields acroFields) {
        PdfFieldInfo fieldInfo = new PdfFieldInfo();
        fieldInfo.setName(fieldName);

        boolean hasOptions = false;

        switch (acroFields.getFieldType(fieldName)) {
            case AcroFields.FIELD_TYPE_CHECKBOX:
                fieldInfo.setType("checkbox");
                hasOptions = true;
                break;
            case AcroFields.FIELD_TYPE_COMBO:
                fieldInfo.setType("combobox");
                hasOptions = true;
                break;
            case AcroFields.FIELD_TYPE_LIST:
                fieldInfo.setType("list");
                hasOptions = true;
                break;
            case AcroFields.FIELD_TYPE_NONE:
                fieldInfo.setType("none");
                break;
            case AcroFields.FIELD_TYPE_PUSHBUTTON:
                fieldInfo.setType("button");
                hasOptions = true;
                break;
            case AcroFields.FIELD_TYPE_RADIOBUTTON:
                fieldInfo.setType("radioButton");
                hasOptions = true;
                break;
            case AcroFields.FIELD_TYPE_SIGNATURE:
                fieldInfo.setType("signature");
                break;
            case AcroFields.FIELD_TYPE_TEXT:
                fieldInfo.setType("text");
                break;
            default:
                fieldInfo.setType("unknown");
                break;
        }

        if (hasOptions) {
            String[] options = acroFields.getAppearanceStates(fieldName);
            for (int i = 0; i < options.length; i++) {
                fieldInfo.addOption(options[i]);
            }
        }

        List<AcroFields.FieldPosition> positions = acroFields.getFieldPositions(fieldName);
        for (int i = 0; i < positions.size(); i++) {
            AcroFields.FieldPosition position = positions.get(i);
            fieldInfo.addLocation(new Location(position));
        }
        return fieldInfo;
    }
}
