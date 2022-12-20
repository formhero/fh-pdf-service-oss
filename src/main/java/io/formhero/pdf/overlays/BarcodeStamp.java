package io.formhero.pdf.overlays;

import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;
import com.itextpdf.text.pdf.qrcode.ErrorCorrectionLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO - We haven't implemented this class at all! This is just a placeholder with, probably, the right variables
 *
 * Created by ryankimber on 2015-11-24.
 */
@Getter
@Setter
public class BarcodeStamp extends AbstractPdfStamp
{
    private static final Logger log = LogManager.getLogger(BarcodeStamp.class.getName());
    private String barcodeType;
    private String content;
    private Float height = 1f;  //It should be noted that each barcode type is going to have a minimum size
    private Float width = 1f;   // below which, it won't be readable.
    private String color = "BLACK";
    private String backgroundColor = "WHITE";
    private String errorCorrection = "M";
    private String characterSet = "ISO-8859-1";

    private static String[] allowedQRErrorCorrection = {"L","M","Q","H"};
    private static String[] allowedQRCharset = {"Cp437","Shift_JIS","ISO-8859-1","ISO-8859-16"};
    
    public void applyStamp(PdfContentByte content) throws DocumentException, IOException
    {
        if(getContent() == null || getContent().length() == 0) return;
        try {
            java.awt.Image awtImage = getBarcodeImage(content);
            if(awtImage != null) {
                Image image = Image.getInstance(awtImage, Color.WHITE);
                image.setAbsolutePosition(getxPosition(), getyPosition());
                image.setRotation(this.getRotation());
                resizeIfRequired(image);
                content.addImage(image);
                return;
                //content.addImage(image, getWidth(), 0, 0, getHeight(), getxPosition(), getyPosition());
            }
            else log.error("getBarcodeImage(...) returned null for " + getBarcodeType());
        }
        catch(Throwable t)
        {
            log.error("Unable to add barcode overlay to PDF due to exception. Placing error text instead:", t);
        }

        //Handle case where we did not successfully generate a barcode.
        Font font = new Font(Font.FontFamily.HELVETICA, 20f);
        font.setColor(BaseColor.RED);
        Chunk chunk = new Chunk("ERROR CREATING \r\n" + getBarcodeType() + " BARCODE", font);
        chunk.setBackground(BaseColor.BLACK);
        content.setColorStroke(BaseColor.RED);
        ColumnText ct = new ColumnText(content);
        ct.setSimpleColumn(new Phrase(chunk), this.getxPosition(), this.getyPosition(), this.getxPosition() + 250, this.getyPosition()+100, 15, Element.ALIGN_MIDDLE);
        ct.go();
        //ColumnText.showTextAligned(content, Element.ALIGN_LEFT, new Phrase(chunk), this.getxPosition(), this.getyPosition(), this.getRotation());
    }

    private void resizeIfRequired(Image image)
    {
        if(height == null && width == null) return;
        if(width == null) width = image.getWidth();
        if(height == null) height = image.getHeight();
        image.scaleToFit(width, height);
    }

    private Color convertColor(String color, Color defaultColor)
    {

        return ColorHelper.parseColor(color.toUpperCase(), defaultColor);

    }

    private java.awt.Image getBarcodeImage(PdfContentByte contentByte) throws BadElementException, UnsupportedEncodingException {

        PdfTemplate template;
        Rectangle size;
        switch(getBarcodeType())
        {
            case "BARCODE-39":
                Barcode39 bc39 = new Barcode39();
                bc39.setCode(getContent());
                return bc39.createAwtImage(convertColor(getBackgroundColor(), Color.WHITE), convertColor(getColor(), Color.BLACK));
            case "BARCODE-128":
                Barcode128 bc128 = new Barcode128();
                bc128.setCode(getContent());
                bc128.setCodeType(Barcode128.CODE128);
                return bc128.createAwtImage(convertColor(getBackgroundColor(), Color.WHITE), convertColor(getColor(), Color.BLACK));
            case "BARCODE-128-RAW":
                Barcode128 bc128Raw = new Barcode128();
                bc128Raw.setCodeType(Barcode.CODE128_RAW);
                bc128Raw.setCode(getContent());
                return bc128Raw.createAwtImage(convertColor(getBackgroundColor(), Color.WHITE), convertColor(getColor(), Color.BLACK));
            case "BARCODE-128-UCC":
                Barcode128 bc128Ucc = new Barcode128();
                bc128Ucc.setCodeType(Barcode.CODE128_UCC);
                bc128Ucc.setCode(getContent());
                return bc128Ucc.createAwtImage(convertColor(getBackgroundColor(), Color.WHITE), convertColor(getColor(), Color.BLACK));
            case "CODABAR":
                BarcodeCodabar bcCodaBar = new BarcodeCodabar();
                bcCodaBar.setCodeType(Barcode.CODABAR);
                bcCodaBar.setCode(getContent());
                bcCodaBar.setGuardBars(true);
                bcCodaBar.setAltText("ALT TEXT");
                return bcCodaBar.createAwtImage(convertColor(getBackgroundColor(), Color.WHITE), convertColor(getColor(), Color.BLACK));
            case "DATAMATRIX":
                BarcodeDatamatrix bcDatamatrix = new BarcodeDatamatrix();
                bcDatamatrix.generate(getContent());
                return bcDatamatrix.createAwtImage(convertColor(getColor(), Color.BLACK), convertColor(getBackgroundColor(), Color.WHITE));
            case "EAN-8":
                BarcodeEAN bcEAN8 = new BarcodeEAN();
                bcEAN8.setCodeType(Barcode.EAN8);
                bcEAN8.setCode(getContent());
                return bcEAN8.createAwtImage(convertColor(getBackgroundColor(), Color.WHITE), convertColor(getColor(), Color.BLACK));
            case "EAN-13":
                BarcodeEAN bcEAN13 = new BarcodeEAN();
                bcEAN13.setCodeType(Barcode.EAN13);
                bcEAN13.setCode(getContent());
                return bcEAN13.createAwtImage(convertColor(getBackgroundColor(), Color.WHITE), convertColor(getColor(), Color.BLACK));
            case "PDF-417":
                BarcodePDF417 pdf417 = new BarcodePDF417();
                pdf417.setText(getContent());
                return pdf417.createAwtImage(convertColor(getBackgroundColor(), Color.WHITE), convertColor(getColor(), Color.BLACK));
            case "POSTNET":
                BarcodePostnet bcPost = new BarcodePostnet();
                bcPost.setCode(getContent());
                return bcPost.createAwtImage(convertColor(getBackgroundColor(), Color.WHITE), convertColor(getColor(), Color.BLACK));
            case "QR-Code":
                Map<EncodeHintType, Object> hints = new HashMap<>();
                //Character set
                if(checkQRCharacterSet(getCharacterSet())){
                    hints.put(EncodeHintType.CHARACTER_SET, getCharacterSet());
                }
                //Error-correction level
                if(checkQRErrorCorrectionAllowed(errorCorrection)){
                    ErrorCorrectionLevel errorCorrectionLevel = getQRErrorCorrectionLevel(errorCorrection);
                    hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
                }
                BarcodeQRCode qrCode = new BarcodeQRCode(getContent(), 1, 1, hints);
                return qrCode.createAwtImage(convertColor(getColor(), Color.BLACK), convertColor(getBackgroundColor(), Color.WHITE));
        }

        //If we haven't done anything, return null;
        return null;
    }

    private static boolean checkQRErrorCorrectionAllowed(String toCheck){
        for(int i = 0; i<allowedQRErrorCorrection.length;i++){
            if(toCheck.toUpperCase().equals(allowedQRErrorCorrection[i])){
                return true;
            }
        }
        return false;
    }

    private static boolean checkQRCharacterSet(String toCheck){
        for(int i = 0; i<allowedQRCharset.length;i++){
            if(toCheck.equals(allowedQRCharset[i])){
                return true;
            }
        }
        return false;
    }

    private static ErrorCorrectionLevel getQRErrorCorrectionLevel(String level){
        switch(level) {
            case "L":
                return ErrorCorrectionLevel.L;
            case "M":
                return ErrorCorrectionLevel.M;
            case "Q":
                return ErrorCorrectionLevel.Q;
            case "H":
                return ErrorCorrectionLevel.H;
        }
        return null;

    }
}
