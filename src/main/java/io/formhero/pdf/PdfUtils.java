package io.formhero.pdf;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import io.formhero.pdf.metadata.PdfFieldInfo;
import io.formhero.pdf.overlays.AbstractPdfStamp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PdfUtils {

	public static byte[] mergeFormDataIntoPdf(Map<String, PdfFieldInfo> formData, byte[] pdfBytes) throws PdfServiceException
	{
		try {
			return FormDataMerger.mergeFormDataIntoPdf(formData, pdfBytes);
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
			throw new PdfServiceException("IOException while merging form data into PDF", ioe);
		}
		catch(DocumentException de) {
			throw new PdfServiceException("DocumentException while merging form data into PDF", de);
		}
	}

	@SuppressWarnings("deprecation")
	public static byte[] removeRestrictions(byte[] pdfBytes) throws IOException, DocumentException
	{
		InputStream pdfInputStream = new ByteArrayInputStream(pdfBytes); 
		return PasswordRemover.removeRestrictions(pdfInputStream);
	}
	
	@SuppressWarnings("deprecation")
	public static byte[] removePasswordAndRestrictions(byte[] pdfBytes, String password) throws IOException, DocumentException
	{
		InputStream pdfInputStream = new ByteArrayInputStream(pdfBytes); 
		return PasswordRemover.removePasswordAndRestrictions(pdfInputStream, password);
	}
	
	public static List<PageImage> renderAsPngs(byte[] pdfBytes) throws IOException
	{
		try {
			PageImageFactory pageImageFactory = new PageImageFactory(pdfBytes);
			return pageImageFactory.convertPagesToImages();
		}
		catch (IOException ioe) {
			throw ioe; //TODO - probably throw our own exception type.
		}
	}
	
	public static PageImage renderPageAsPng(byte[] pdfBytes, int pageNumber) throws IOException
	{
		try {
			PageImageFactory pageImageFactory = new PageImageFactory(pdfBytes);
			return pageImageFactory.convertPageToImage(pageNumber);
		}
		catch(IOException ioe) {
			throw ioe; //TODO - probably throw our own exception type.
		}
	}

	public static byte[] doOverlays(byte[] pdfBytes, List<AbstractPdfStamp> pdfOverlays)
	{
		try {
			PdfReader pdfReader = new PdfReader(pdfBytes);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfStamper pdfStamper = new PdfStamper(pdfReader, baos);

			for (AbstractPdfStamp overlay : pdfOverlays) {

				Set<Integer> pageNumbers = buildPageList(overlay, pdfReader.getNumberOfPages());
				try {
					for(Integer pageNumber : pageNumbers){

						PdfContentByte content = pdfStamper.getOverContent(pageNumber);
						overlay.apply(content);
					}
				}
				catch (Exception ioe) {
					//Just keep going, but note the error.
					ioe.printStackTrace();
				}
			}

			pdfStamper.close();
			pdfBytes = baos.toByteArray();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}

		return pdfBytes;
	}

	public static byte[] doUnderlays(byte[] pdfBytes, List<AbstractPdfStamp> pdfUnderlays)
	{
		try {
			PdfReader pdfReader = new PdfReader(pdfBytes);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfStamper pdfStamper = new PdfStamper(pdfReader, baos);

			for (AbstractPdfStamp underLay : pdfUnderlays) {
				Set<Integer> pageNumbers = buildPageList(underLay, pdfReader.getNumberOfPages());
				try {
					for (Integer pageNumber : pageNumbers){
						PdfContentByte content = pdfStamper.getUnderContent(pageNumber);
						underLay.apply(content);
					}
				}
				catch (Exception ioe) {
					//Just keep going, but note the error.
					ioe.printStackTrace();
				}
			}

			pdfStamper.close();
			pdfBytes = baos.toByteArray();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}

		return pdfBytes;
	}

	private static void addNumbers(int start, int end, Set<Integer> pageNumberList)
	{
		for (int i = start; i <= end; i++) {
			pageNumberList.add(new Integer(i));
		}
	}

	private static void addEvenNumbers(int start, int end, Set<Integer> pageNumberList)
	{
		for (int i = start; i <= end; i++) {
			if (i % 2 == 0) pageNumberList.add(new Integer(i));
		}
	}

	private static void addOddNumbers(int start, int end, Set<Integer> pageNumberList)
	{
		for (int i = start; i <= end; i++) {
			if (i % 2 == 1) pageNumberList.add(new Integer(i));
		}
	}

	private static Set<Integer> buildPageList(AbstractPdfStamp pdfOverlay, int numberOfPages)
	{
		Set<Integer> pageNumbers = new HashSet<>();
		Set<Integer> notThesePages = new HashSet<>();
		pageNumbers.add(1);

		//What's the pseudo code?
		String[] pageNumberList = pdfOverlay.getPages().split(",");

		for (int i = 0; i < pageNumberList.length; i++) {
			switch (pageNumberList[i].toLowerCase()) {
				case "all":
					addNumbers(1, numberOfPages, pageNumbers);
					break;

				case "even":
					addEvenNumbers(1, numberOfPages, pageNumbers);
					break;

				case "odd":
					addOddNumbers(1, numberOfPages, pageNumbers);
					break;

				case "!even":
					addEvenNumbers(1, numberOfPages, notThesePages);
					break;

				case "!odd":
					addOddNumbers(1, numberOfPages, notThesePages);
					break;

				default:
					try {
						//determine if it's negated and if it's a range.
						boolean isNegated = pageNumberList[i].startsWith("!");
						if (isNegated) pageNumberList[i] = pageNumberList[i].substring(1);
						boolean isRange = pageNumberList[i].indexOf('-') > -1;

						if (isRange) { //It's two numbers separated by '-'
							String[] values = pageNumberList[i].split("-");
							int startNumber = Integer.parseInt(values[0]);
							int endNumber = Integer.parseInt(values[1]);
							if(isNegated) addNumbers(startNumber, endNumber, notThesePages);
							else addNumbers(startNumber, endNumber, pageNumbers);
						}
						else { //Simple, it's just one number
							if(isNegated) notThesePages.add(Integer.parseInt(pageNumberList[i]));
							else pageNumbers.add(Integer.parseInt(pageNumberList[i]));
						}

					}
					catch (Throwable t) {
						//TODO - log this somewhere - we should tell them there's a config problem with this page range.
					}
			}
		}

		pageNumbers.removeAll(notThesePages);

		return pageNumbers;
	}
}
