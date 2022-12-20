package io.formhero.pdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ryankimber on 2017-03-24.
 */
public class TryLoadingNonFormPdf {

    public static void main(String[] args) {

        try
        {
            File file = new File("./src/main/resources/no-fields.pdf");
            System.out.println("Absolute path: " + file.getAbsolutePath());
            PdfReader pdfReader = new PdfReader(new FileInputStream("./src/main/resources/no-fields.pdf"));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfStamper stamper = new PdfStamper(pdfReader, baos);
            AcroFields fields = stamper.getAcroFields();
            Map<String, AcroFields.Item> fieldMap = fields.getFields();
            int fieldCount = fieldMap.size();

            if(fieldCount > 0) fields.setGenerateAppearances(true);


            Set<String> formKeys = new HashSet<String>();
            formKeys.add("no-such-field");

            for (String fieldName : formKeys) {
                try
                {
                    fields.setField(fieldName,  "fake-value");
                }
                catch(Throwable t)
                {
                    System.out.println("Failed to set " + fieldName + " due to exception:" + t.getMessage());
                    t.printStackTrace();
                }

            }

            stamper.setFormFlattening(true);
            stamper.close();
            pdfReader.close();

        }
        catch(IOException | DocumentException ioe)
        {
            System.out.println("Error:" + ioe.getMessage());
            ioe.printStackTrace();
        }
    }
}
