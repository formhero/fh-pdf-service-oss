package io.formhero.pdf.overlays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.IOException;

@Getter @Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible=true)
@JsonSubTypes({
        @Type(value = BarcodeStamp.class, name = "barcode"),
        @Type(value = ImageStamp.class, name = "image"),
        @Type(value = TextStamp.class, name = "text")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractPdfStamp
{
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private Float xPosition;
    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private Float yPosition;

    @Getter @Setter
    private float fontSize = 15f;
    @Getter @Setter
    private String font = "HELVETICA";
    @Getter @Setter
    private String color = "BLACK";

    /*
         pages can be any of the following:
                'all' - will put the overlay on all pages
                'even' - will put the overlay on all even pages
                'odd' - will put the overlay on all odd pages
                '1,2,4,6' - an arbitrary list of page numbers
                'all, !2' - will stamp all of the pages except 2
                '2+'  - will stamp all of the pages from 2 on
                '4-8' - will stamp all of the pages from 4-8
                'all, !2-4' - will ensure 2-4 do not get stamped.
                '!2-4, all' - will produce the same result as above. Negations override additions.

        It should be noted that negations override additions. That is, order is irrelevant - if you put !8 somewhere in the list,
        8 will not receive the stamp, even if you've put 4-8, or all in the list.
     */
    private String pages; //Can be 'even, odd, all, or a list of page numbers - ie 1,4,5, etc. Y
    private float rotation = 0;
    private float opacity = 100f;

    public abstract void applyStamp(PdfContentByte content) throws DocumentException, IOException;

    public void apply(PdfContentByte content) throws DocumentException, IOException {
        PdfGState gs1 = new PdfGState();
        gs1.setFillOpacity(getOpacity());
        content.saveState();
        content.setGState(gs1);
        applyStamp(content);
        content.restoreState();
    }

    public void setxPosition(Float value)
    {
        this.xPosition = value;
    }

    public void setyPosition(Float value)
    {
        this.yPosition = value;
    }

    public Float getxPosition()
    {
        return xPosition;
    }

    public Float getyPosition()
    {
        return yPosition;
    }

    protected Font getItextFont()
    {
        Font theFont = null;
        switch(getFont().toUpperCase())
        {
            case "COURIER":
                theFont = new Font(Font.FontFamily.COURIER, getFontSize());
                theFont.setColor(getItextColor());
                break;
            case "HELVETICA":
                theFont =  new Font(Font.FontFamily.HELVETICA, getFontSize());
                theFont.setColor(getItextColor());
                break;
            case "TIMES":
                theFont =  new Font(Font.FontFamily.TIMES_ROMAN, getFontSize());
                theFont.setColor(getItextColor());
                break;
            case "SYMBOL":
                theFont =  new Font(Font.FontFamily.SYMBOL, getFontSize());
                theFont.setColor(getItextColor());
                break;
            case "ZAPFDINGBATS":
                theFont =  new Font(Font.FontFamily.ZAPFDINGBATS, getFontSize());
                theFont.setColor(getItextColor());
                break;
            default:
                theFont =  new Font(Font.FontFamily.HELVETICA, getFontSize());
                theFont.setColor(getItextColor());
        }

        return theFont;
    }

    protected BaseColor getItextColor()
    {
        Color awtColor = ColorHelper.parseColor(getColor(), Color.BLACK);
        return new BaseColor(awtColor.getRed(), awtColor.getGreen(), awtColor.getGreen(), awtColor.getAlpha());
    }

}
