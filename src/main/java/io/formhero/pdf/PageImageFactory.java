package io.formhero.pdf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PageImageFactory
{
	private static final Logger log = LogManager.getLogger(PageImageFactory.class.getName());
	private byte[] pdfBytes;
	private PDDocument document;
	private PDFRenderer imageRenderer;

	public PageImageFactory(byte[] pdfBytes) throws IOException
	{
		super();
		ImageIO.setUseCache(false);
		this.pdfBytes = pdfBytes;
		this.document = PDDocument.load(new ByteArrayInputStream(pdfBytes), (String) null);
		this.imageRenderer = new PDFRenderer(this.document);
	}

	public void close() throws IOException
	{
		document.close();
	}

	/**
	 * Converts the given pdf into a list of DocumentImages
	 * @return
	 * @throws IOException
	 */
	public List<PageImage> convertPagesToImages() throws IOException
	{
        //ByteBuffer buf = null;
        //buf = ByteBuffer.wrap(pdfBytes);
        
		List<PageImage> pageImages = new ArrayList<PageImage>();

		for (int pageIndex = 0; pageIndex < document.getPages().getCount(); pageIndex++)
		{ 
		    //BufferedImage bim = getBufferedImageOfPage(pageIndex, resolution);
			//byte[] pageImage = convertBufferedImageToPngByteArray(bim);
		    PDRectangle pdBox = document.getPage(pageIndex).getCropBox();

			BufferedImage hiresBim = getBufferedImageOfPage(pageIndex, 300);
			BufferedImage stdBim = getBufferedImageOfPage(pageIndex, 100);
			byte[] hiResPageImage = convertBufferedImageToPngByteArray(hiresBim);
			PageImage newPageImage = new PageImage(pageIndex, pdBox.getHeight(), pdBox.getWidth(), "image/png");
			newPageImage.setHighResImage(hiResPageImage);
			buildThumbnails(newPageImage, stdBim);
			pageImages.add(newPageImage);
		}

        return pageImages;
	}

	public void buildThumbnails(PageImage pageImage, BufferedImage bufferedImage) throws IOException
	{
		pageImage.setThumbnail144(buildThumbnail(bufferedImage, 144));
		pageImage.setThumbnail576(buildThumbnail(bufferedImage, 576));
	}

	private byte[] buildThumbnail(BufferedImage image, int maxDimension) throws IOException
	{
		log.info("Into buildThumbnail ("+ maxDimension + ")...");
		BufferedImage thumb = Scalr.resize(image,
				Scalr.Method.ULTRA_QUALITY,
				Scalr.Mode.AUTOMATIC,
				maxDimension,
				maxDimension,
				Scalr.OP_ANTIALIAS);
		log.info("Built bufferedImage thumb, trying to create byteArray...");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(thumb, "png", baos);
		return baos.toByteArray();
	}

	public PageImage convertPageToImage(int pageIndex) throws IOException
	{ 
		log.info("Getting pdPage for page " + (pageIndex + 1));
		PDPage pdPage = document.getDocumentCatalog().getPages().get(pageIndex);
		log.info("Creating cropbox for page " + (pageIndex + 1));
		PDRectangle pdBox = pdPage.getCropBox();


		BufferedImage hiresBim = getBufferedImageOfPage(pageIndex, 300);
		BufferedImage stdBim = getBufferedImageOfPage(pageIndex, 100);
		byte[] hiResPageImage = convertBufferedImageToPngByteArray(hiresBim);
		PageImage newPageImage = new PageImage(pageIndex, pdBox.getHeight(), pdBox.getWidth(), "image/png");
		newPageImage.setHighResImage(hiResPageImage);
		buildThumbnails(newPageImage, stdBim);
		return newPageImage;
	}
	
	private BufferedImage getBufferedImageOfPage(int pageIndex, int dpi) throws IOException
	{
		//TODO - consider rendering the image in very high DPI and then down-sampling - it looks like if we render in low DPI a lot of detail
		// gets left out / isn't dithered properly.
		if(dpi > 600) dpi = 600;
		if(dpi < 36) dpi = 36;
		return imageRenderer.renderImageWithDPI(pageIndex, dpi);
	}
	
	private byte[] convertBufferedImageToPngByteArray(BufferedImage bim) throws IOException
	{
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(bim, "png", baos);
	    return baos.toByteArray();
	}
}

