package io.formhero.pdf.results;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by ryankimber on 2016-05-13.
 */
@Getter
@Setter
public class ErrorResult implements Serializable {
    private String errorMessage;
    private String causeMessage;
    private String result = "error";
}
