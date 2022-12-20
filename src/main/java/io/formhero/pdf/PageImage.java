package io.formhero.pdf;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter(AccessLevel.PACKAGE)
public class PageImage
{
	public PageImage(int pageNumber, float height, float width, String mimeType)
	{
		this.pageNumber = pageNumber;
		this.height = height;
		this.width = width;
		this.mimeType = mimeType;
	}

	private int pageNumber;
	private float height, width;
	private String mimeType;
	private byte[] highResImage;

	private byte[] thumbnail144;
	private byte[] thumbnail576;
}

