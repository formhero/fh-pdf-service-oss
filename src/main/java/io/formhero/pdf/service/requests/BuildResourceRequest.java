package io.formhero.pdf.service.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.formhero.pdf.steps.AbstractPdfStep;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ryankimber on 2016-03-13.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible=true, defaultImpl = BuildResourceRequest.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BuildResourcesForEmailRequest.class, name = BuildResourceRequest.BUILD_RESOURCES_FOR_EMAIL),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BuildResourceRequest implements Serializable
{
    public static final String BUILD_RESOURCES_FOR_EMAIL = "buildResourcesForEmail";

    private String requestId;
    private String sessionId;
    private String type;
    private List<Destination> destinations;
    private List<AbstractPdfStep> buildSteps;
}
