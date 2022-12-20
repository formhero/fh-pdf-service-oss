package io.formhero.pdf.steps;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.formhero.pdf.PdfServiceException;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible=true)
@JsonSubTypes({
	@Type(value = AddTemplateStep.class, name = "json"),
	@Type(value = AddTemplateStep.class, name = "xml"),
    @Type(value = AddPdfStep.class, name = "addPdf"),
	@Type(value = BuildDynamicPdfStep.class, name = "dynamic-pdf"),
	@Type(value = GenerateImagesAndMetaDataStep.class, name = "generateImagesAndMetaData"),
    @Type(value = MergePdfStep.class, name = "mergePdf"),
	@Type(value = AddPdfStep.class, name = "mergePdfData")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractPdfStep implements Serializable {

	private static final long serialVersionUID = -2391802642982598491L;
	
	public static final String APPLICATION_PDF = "application/pdf";
	public static final String APPLICATION_JSON = "application/json";
	public static final String APPLICATION_XML = "application/xml";
	
	public AbstractPdfStep() {
		// TODO Auto-generated constructor stub
	}

	@Getter @Setter
	private String requestId;

	@Getter @Setter
	private String sessionId;

	@Getter @Setter
	private String pdfSource;

	@Getter @Setter
	private String fileFolder;

	@Getter @Setter
	private String fileName;

	@Getter @Setter
	private String type;

	public String getMimeType()
	{
		switch(type)
		{
			case "addPdf":
			case "dynamic-pdf":
				return APPLICATION_PDF;
			case "json":
				return APPLICATION_JSON;
			case "xml":
				return APPLICATION_XML;
			default:
				return APPLICATION_PDF;

		}
	}

	public abstract List<ResourceBuildResult> buildResult(List<ResourceBuildResult> previousResults) throws PdfServiceException;

}
