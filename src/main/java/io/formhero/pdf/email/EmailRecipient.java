package io.formhero.pdf.email;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by ryankimber on 2016-03-16.
 */
@Getter
@Setter
public class EmailRecipient implements Serializable {

    private String email;
    private String name;
}
