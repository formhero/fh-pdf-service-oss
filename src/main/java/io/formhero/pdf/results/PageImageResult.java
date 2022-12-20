package io.formhero.pdf.results;

import io.formhero.pdf.PageImage;
import io.formhero.pdf.service.PublishableResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ryankimber on 2016-03-14.
 */
@AllArgsConstructor
@Getter
@Setter
public class PageImageResult implements PublishableResult {

    private String type = "pageImageResult";
    private String requestId;
    private String sessionId;
    private PageImage pageImage;

}