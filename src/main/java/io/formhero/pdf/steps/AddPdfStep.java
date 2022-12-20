package io.formhero.pdf.steps;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AddPdfStep extends AbstractPdfStep {

	private static final long serialVersionUID = -5705073779640233122L;
	private static final Logger log = LogManager.getLogger(AddPdfStep.class.getName());

	public AddPdfStep() {
	}

	private String bucket;
	private String fileFolder;
	private String resultId;
	private String fileName;
	private String outputFileName;

	private boolean useAsIs = false;

	@JsonProperty("base64Pdf")
	private byte[] pdfBytes;
	private List<AbstractPdfStamp> overlays;
	private List<AbstractPdfStamp> underlays;
	
	public byte[] getPdfBytes() throws StorageException, IllegalArgumentException
	{
		if(pdfBytes != null && pdfBytes.length > 0) return pdfBytes;
		else
		{
			//TODO - At the moment, we're assuming that the first PDF action will have a file key in it. 
			//     - We should at least enforce that if we're going to rely on it and throw a nice exception if it's missing.
			if(fileFolder == null) throw new IllegalArgumentException("The addPdfAction must have a fileFolder.");
			if(fileName == null) throw new IllegalArgumentException("The addPdfAction must have a fileName.");
			else
			{
				try {
					pdfBytes = StorageFactory.getStorageProvider().getObject(bucket, fileFolder, fileName);
					return pdfBytes;
				}
				catch(FhConfigException fhce)
				{
					throw new StorageException("Unable to load resource from storage due to FhConfigException:", fhce);
				}
			}
		}
	}

	private Map<String, PdfFieldInfo> formData = new HashMap<String, PdfFieldInfo>();
	
	public void setFormField(String key, String value) {
		PdfFieldInfo fieldInfo = new PdfFieldInfo();
		fieldInfo.setName(key);
		fieldInfo.setValue(value);
		formData.put(key, fieldInfo);
	}
	
	public List<ResourceBuildResult> buildResult(List<ResourceBuildResult> previousResults) throws PdfServiceException
	{

		try {
			byte [] pdfBytes = null;
			if(useAsIs) {
				pdfBytes = getPdfBytes();
			}
			else {
				//TODO - It would be faster if mergeFormDataIntoPdf and doOverlays were combined so that PdfReader is only created once and we only have
				//one serialization back into a byte array. This implementation is creating a PDF Reader, converting it to bytes, then creating another, then again to bytes.
				pdfBytes = PdfUtils.mergeFormDataIntoPdf(this.formData, getPdfBytes());
				if (overlays != null && overlays.size() > 0) pdfBytes = PdfUtils.doOverlays(pdfBytes, overlays);
				if (underlays != null && underlays.size() > 0) pdfBytes = PdfUtils.doUnderlays(pdfBytes, underlays);
			}
			ResourceBuildResult buildResult = new ResourceBuildResult(getRequestId(), getSessionId(), APPLICATION_PDF, pdfBytes);
			//In case anyone calls us again, let's use the bytes we've already generated.
			this.pdfBytes = pdfBytes;
			useAsIs = true;
			buildResult.setResultId(getResultId());
			if(getOutputFileName() != null) buildResult.setOutputFileName(getOutputFileName());
			else buildResult.setOutputFileName(getFileName());
			previousResults.add(buildResult);
			return previousResults;
		}
		catch(StorageException se)
		{
			log.error("Unable to build pdf due to storage exception, building an error document instead: " + se.getMessage(), se);
			ResourceBuildResult errorBuildResult = ResourceBuildResult.errorInstance(this, se);
			previousResults.add(errorBuildResult);
 			return previousResults;
		}
	}
}
