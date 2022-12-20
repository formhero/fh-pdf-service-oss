package io.formhero.pdf.service.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.formhero.pdf.email.EmailDestination;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ryankimber on 2016-03-15.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible=true, defaultImpl = Destination.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailDestination.class, name = Destination.EMAIL)
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Destination {

    public static final String EMAIL = "sendEmail";

    private String type;
}
