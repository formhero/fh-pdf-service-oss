package io.formhero.pdf.steps;

import io.formhero.storage.StorageException;
import io.formhero.storage.StorageFactory;
import io.formhero.util.FhConfigException;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class GenerateImagesAndMetaDataStep extends AbstractPdfStep {

	private static final long serialVersionUID = -5705073779640233122L;

	public GenerateImagesAndMetaDataStep() {
	}

	@Setter
	private byte[] pdfBytes;
	
	@Getter @Setter
	private String fileFolder;

	@Getter @Setter
	private String fileName;

	@Getter @Setter
	private String bucket;
	
	public byte[] getPdfBytes() throws StorageException
	{
		if(pdfBytes != null && pdfBytes.length > 0) return pdfBytes;
		else
		{
			try {
				//We're supposed to be getting the PDF from Amazon S3.
				pdfBytes = StorageFactory.getStorageProvider().getObject(getBucket(), getFileFolder(), getFileName());
				return pdfBytes;
			}
			catch(FhConfigException fhce)
			{
				throw new StorageException("Unable to load resource from storage due to FhConfigException:", fhce);
			}
		}
	}
	
	public List<ResourceBuildResult> buildResult(List<ResourceBuildResult> previousResults)
	{
		return previousResults;
	}
	
}
