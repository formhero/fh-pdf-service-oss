package io.formhero.pdf.metadata;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryankimber on 2016-03-13.
 */
@Getter
@Setter
public class PdfInfo implements Serializable
{
    private int numberOfPages;
    private List<PdfFieldInfo> fields = new ArrayList<PdfFieldInfo>();
    private List<PdfPageInfo> pages = new ArrayList<PdfPageInfo>();

    void addPageInfo(PdfPageInfo pageInfo)
    {
        pages.add(pageInfo);
    }

    void addPdfField(PdfFieldInfo info)
    {
        fields.add(info);
    }
}
