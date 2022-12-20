package io.formhero.pdf.overlays;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import io.formhero.storage.StorageFactory;
import io.formhero.util.FhConfigException;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

/**
 * Created by ryankimber on 2015-11-24.
 */
@Getter
@Setter
public class ImageStamp extends AbstractPdfStamp
{
    private String bucket;
    private String fileFolder;
    private String fileName;
    private Float height;
    private Float width;
    private boolean maintainProportions;

    public void applyStamp(PdfContentByte content) throws DocumentException, IOException
    {
        try {
            Image image = Image.getInstance(StorageFactory.getStorageProvider().getObject(bucket, fileFolder, fileName));
            image.setAbsolutePosition(getxPosition(), getyPosition());
            image.setRotation(this.getRotation());
            resizeIfRequired(image);
            content.addImage(image);
        }
        catch(FhConfigException e)
        {
            throw new IOException("Unable to get file from storage provider due to FhConfigException: ", e);
        }
    }

    private void resizeIfRequired(Image image)
    {
        if(height == null && width == null) return;

        if(width == null) width = image.getWidth();
        if(height == null) height = image.getHeight();
        image.scaleToFit(width, height);
    }
}
