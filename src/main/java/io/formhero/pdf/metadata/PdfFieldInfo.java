package io.formhero.pdf.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryankimber on 2016-03-13.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PdfFieldInfo implements Serializable{

    public static final String OPTION_REMAIN_EDITABLE = "remainEditable";

    private List<Location> locations = new ArrayList<Location>();
    private String name;
    private String type;
    private String defaultValue;
    private String value;
    private List<String> options = null;

    public void addOption(String value) {
        if(options == null) options = new ArrayList<String>();
        options.add(value);
    }

    public boolean hasOption(String value)
    {
        if(options == null) return false;
        else return options.contains(value);
    }

    public void addLocation(Location location)
    {
        locations.add(location);
    }
}
