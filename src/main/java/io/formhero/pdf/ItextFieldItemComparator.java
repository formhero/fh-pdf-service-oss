package io.formhero.pdf;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.FieldPosition;

import java.util.Comparator;

public class ItextFieldItemComparator implements Comparator<String> {

	private static final float FUZZY_TOLERANCE = 5F;
	private AcroFields acroFields;
	
	public ItextFieldItemComparator(AcroFields acroFields) {
		this.acroFields = acroFields;
	}

	@Override
	public int compare(String o1, String o2) {
		
		//We are only comparing the first location of a given field.
		FieldPosition itemOnePosition = acroFields.getFieldPositions(o1).get(0);
		FieldPosition itemTwoPosition = acroFields.getFieldPositions(o2).get(0);
		
		/* 
		 * Important Note!! PDFs oddly position elements verically from the bottom of the page up,
		 * so we need to consider higher numbers to be closer to the top, while for left-to-right,
		 * lower numbers are closer to the left.
		 */
		
		int pageComparison = Integer.compare(itemOnePosition.page,  itemTwoPosition.page);
		if(pageComparison != 0) return pageComparison;
		else
		{
			//They are on the same page, so compare their Y position (0 is higher)
			// Since this is about positioning on a page, a few pixels different should be considered equal
			// for most cases.
			if(isFuzzyEqual(itemOnePosition.position.getTop(), itemTwoPosition.position.getTop(), FUZZY_TOLERANCE))
			{
				//They are relatively at the same height on the page - compare X location.
				if(isFuzzyEqual(itemOnePosition.position.getLeft(), itemTwoPosition.position.getLeft(), FUZZY_TOLERANCE))
				{
					//They are also at relatively the same X position - so compare absolute top position.
					//-1 * is to compensate for the backwards bottom-to-top coordinate system in PDF
					int absoluteTopCompare = -1 * Float.compare(itemOnePosition.position.getTop(), itemTwoPosition.position.getTop());
					//Finally, if the top isn't equal, return that, else return absolute left comparison.
					return absoluteTopCompare != 0 ? absoluteTopCompare : Float.compare(itemOnePosition.position.getLeft(), itemTwoPosition.position.getLeft());
					
				}
				//They aren't at the same X position - so go by that.
				return Float.compare(itemOnePosition.position.getLeft(), itemTwoPosition.position.getLeft());
			}
			//-1 * to compensate for the backwards bottom-to-top coordinate system in PDF.
			else return -1 * Float.compare(itemOnePosition.position.getTop(), itemTwoPosition.position.getTop());	
		}
	}
	
	private boolean isFuzzyEqual(float v1, float v2, float tolerance)
	{
		return (Math.abs(v1 - v2) < tolerance);
	}

}
