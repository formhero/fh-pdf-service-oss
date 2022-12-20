package io.formhero.pdf.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.formhero.pdf.PdfServiceException;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddTemplateStep extends AbstractPdfStep {

	private static final long serialVersionUID = -5705073779640233122L;
	private static final ObjectMapper MAPPER = new ObjectMapper();

	public AddTemplateStep() {
	}

	private String outputFileName;

	private String template;
	
	public List<ResourceBuildResult> buildResult(List<ResourceBuildResult> previousResults) throws PdfServiceException
	{
		//String dataAsString = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(getData());
		ResourceBuildResult buildResult = new ResourceBuildResult(getRequestId(), getSessionId(), APPLICATION_JSON, template.getBytes());
		buildResult.setOutputFileName(getOutputFileName());
		previousResults.add(buildResult);
		return previousResults;
	}
}
