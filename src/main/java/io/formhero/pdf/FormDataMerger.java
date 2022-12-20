package io.formhero.pdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import io.formhero.pdf.metadata.PdfFieldInfo;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class FormDataMerger {
	private static boolean debug = true;
	
	public static byte[] mergeFormDataIntoPdf(Map<String, PdfFieldInfo> formData, byte[] pdfBytes) throws IOException, DocumentException {
		ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
		//Do the data merge using iText.
		PdfReader reader = new PdfReader(bais);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfStamper stamper = new PdfStamper(reader, baos);
		AcroFields fields = stamper.getAcroFields();
		Map<String, AcroFields.Item> fieldMap = fields.getFields();

		//If there are no fields, calling setGenerateAppearances(true) throws an exception.
		if (fieldMap.size() > 0)
			fields.setGenerateAppearances(true);

		Set<String> formKeys = formData.keySet();
		for (String fieldName : formKeys) {
			try	{
				if (formData.get( fieldName ).getValue() != null)
					fields.setField(fieldName,  formData.get( fieldName ).getValue());
			} catch(Throwable t) {
				System.out.println("Failed to set " + fieldName + " due to exception:");
				t.printStackTrace();
			}
		}

	    // Set ALL the fields to read-only
		Set<String> fieldKeys = fieldMap.keySet();
		for (String fieldName : fieldKeys) {
			// We generally want to set fields to readOnly, but not if a flag has been set for "remainEditable"
			PdfFieldInfo fieldInfo = formData.get(fieldName);
			if (fieldInfo == null || !fieldInfo.hasOption(PdfFieldInfo.OPTION_REMAIN_EDITABLE)) {
				fields.setFieldProperty(fieldName, "setfflags", PdfFormField.FF_READ_ONLY, null);
			}
		}

		// Try setting the fields to read-only instead of flattening the form, in order to preserve accessibility tags
		// stamper.setFormFlattening(true);
		stamper.close();
		reader.close();

		return baos.toByteArray();
	}
	
	private static String getFieldType(int fieldType) {
		switch (fieldType) {
			case AcroFields.FIELD_TYPE_CHECKBOX:
				return "checkbox";
			case AcroFields.FIELD_TYPE_COMBO: 
				return "combo";
			case AcroFields.FIELD_TYPE_LIST:
				return "list";
			case AcroFields.FIELD_TYPE_NONE: 
				return "none";
			case AcroFields.FIELD_TYPE_PUSHBUTTON:
				return "pushbutton";
			case AcroFields.FIELD_TYPE_RADIOBUTTON:
				return "radiobutton";
			case AcroFields.FIELD_TYPE_SIGNATURE:
				return "signature";
			case AcroFields.FIELD_TYPE_TEXT: 
				return "text";
			default:
				return "unknown";
		}
	}
}
