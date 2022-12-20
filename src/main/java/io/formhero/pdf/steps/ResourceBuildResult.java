package io.formhero.pdf.steps;

import io.formhero.pdf.service.PublishableResult;
import io.formhero.storage.StorageException;
import lombok.Getter;
import lombok.Setter;

/* This class represents the result of a single PDFAction. 
 * PDF Actions themselves are passed a List of PDF Action Results and return a list of PDF Actions results.
 * This allows the action to perform an action on the entire list, to add to the list, or to modify the list.
 * 
 * Examples include:
 *   - our FlattenPdfsAction (which flattens all the PDFs in the list, but doesn't add anything
 *   - our MergePdfsAction (which merges all of the PDF Action results in the list into a single item in the list
 *   - our AddPdfStep, which adds a PDF to the list.
 */

@Getter
@Setter
public class ResourceBuildResult implements PublishableResult
{
	private String requestId;
	private String sessionId;

	private String resultId;
	private String outputFileName;
	private String type;
	private String mediaType;
	private byte[] data;
	
	public ResourceBuildResult() {
		// TODO Auto-generated constructor stub
	}
	
	public ResourceBuildResult(String requestId, String sessionId, String mediaType, byte[] data)
	{
		super();
		this.requestId = requestId;
		this.sessionId = sessionId;
		this.mediaType = mediaType;
		this.data = data;
	}


	protected static ResourceBuildResult errorInstance(AddPdfStep pdfStep, StorageException se)
	{
		ResourceBuildResult br = new ResourceBuildResult();
		br.requestId = pdfStep.getRequestId();
		br.sessionId = pdfStep.getSessionId();
		br.outputFileName = pdfStep.getOutputFileName() + ".error.txt";
		br.type = pdfStep.getType();
		br.mediaType = "text/plain";
		br.data = se.getMessage().getBytes();

		return br;
	}
}
