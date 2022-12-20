package io.formhero.pdf.service.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by ryan.kimber on 2017-12-10.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible=true, defaultImpl = HttpPdfRequest.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = HttpMergePdfDataRequest.class, name = HttpPdfRequest.MERGE_PDF_DATA),
    @JsonSubTypes.Type(value = HttpMergePdfsRequest.class, name = HttpPdfRequest.MERGE_PDFS),
    @JsonSubTypes.Type(value = HttpGeneratePageImagesRequest.class, name = HttpPdfRequest.GENERATE_PAGE_IMAGES),
    @JsonSubTypes.Type(value = HttpExaminePdfDataRequest.class, name = HttpPdfRequest.EXAMINE_PDF_DATA)
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class HttpPdfRequest implements Serializable
{
    public static final String MERGE_PDF_DATA = "mergePdfData";
    public static final String MERGE_PDFS = "mergePdfFiles";
    public static final String GENERATE_PAGE_IMAGES = "generateImagesAndMetaData";
    public static final String EXAMINE_PDF_DATA = "examinePdfData";

    protected String requestId;
    protected String sessionId;
    protected String type;
}
