package io.formhero.pdf.overlays;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Created by ryankimber on 2015-11-24.
 */
public class TextStamp extends AbstractPdfStamp {

    private static final Logger log = LogManager.getLogger(TextStamp.class.getName());
    @Getter @Setter
    private String content;

    public void applyStamp(PdfContentByte content) throws DocumentException, IOException
    {
        try {
            Font font = getItextFont();
            Chunk chunk = new Chunk(this.getContent(), font);
            Phrase phrase = new Phrase(chunk);
            content.setColorStroke(getItextColor());
            ColumnText.showTextAligned(content, Element.ALIGN_LEFT, phrase, this.getxPosition(), this.getyPosition(), this.getRotation());
        }
        catch(Throwable t)
        {
            log.error("Unable to add text overlay to PDF due to exception:", t);
        }
    }
}
