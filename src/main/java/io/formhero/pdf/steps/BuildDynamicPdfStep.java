package io.formhero.pdf.steps;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.formhero.pdf.PdfServiceException;
import io.formhero.pdf.overlays.AbstractPdfStamp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BuildDynamicPdfStep extends AbstractPdfStep {

	private static final long serialVersionUID = -5705073779640233122L;

	public BuildDynamicPdfStep() {

	}

	private String outputFileName;
	private String template;

	@JsonProperty("base64Pdf")
	private byte[] pdfBytes;
	private List<AbstractPdfStamp> overlays;
	private List<AbstractPdfStamp> underlays;
	
	public List<ResourceBuildResult> buildResult(List<ResourceBuildResult> previousResults) throws PdfServiceException
	{
		try {
			//TODO - It would be faster if mergeFormDataIntoPdf and doOverlays were combined so that PdfReader is only created once and we only have
			//one serialization back into a byte array. This implementation is creating a PDF Reader, converting it to bytes, then creating another, then again to bytes.
			//byte[] pdfBytes = getTemplate().getBytes();
			//byte [] pdfBytes = PdfUtils.mergeFormDataIntoPdf(this.formData, getPdfBytes());
			//if(overlays != null && overlays.size() > 0) pdfBytes = PdfUtils.doOverlays(pdfBytes, overlays);

			ResourceBuildResult buildResult = new ResourceBuildResult(getRequestId(), getSessionId(), getMimeType(), pdfBytes);
			//buildResult.setResultId(getResultId());
			if(getOutputFileName() != null) buildResult.setOutputFileName(getOutputFileName());
			else buildResult.setOutputFileName(getFileName());
			previousResults.add(buildResult);
			return previousResults;
		}
		catch(Throwable t)
		{
			t.printStackTrace();
			throw new PdfServiceException("Error in BuildDynamicPdfStep: " + t.getMessage(), t);
		}
	}
}
