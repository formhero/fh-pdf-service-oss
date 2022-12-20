package io.formhero.pdf.metadata;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by ryankimber on 2016-03-13.
 */
@Getter
@Setter
public class PdfPageInfo implements Serializable {

    private int pageNumber;
    private int rotation;
    private float width;
    private float height;
}
