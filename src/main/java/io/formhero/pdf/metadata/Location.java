package io.formhero.pdf.metadata;

import com.itextpdf.text.pdf.AcroFields;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by ryankimber on 2016-03-13.
 */

@AllArgsConstructor
@Getter
@Setter
public class Location implements Serializable {
    private int page;
    private float x1, y1;
    private float x2, y2;

    public Location(AcroFields.FieldPosition position)
    {
        this.page = position.page - 1;
        this.x1 = position.position.getLeft();
        this.x2 = position.position.getRight();
        this.y1 = position.position.getTop();
        this.y2 = position.position.getBottom();
    }
}
