package gov.epa.emissions.framework.client.data;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EmfDateFormat {

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat();

    public static String format() {
        return "MM/dd/yyyy HH:mm";
    }

    public static String format_MM_DD_YYYY(Date date) {
        dateFormatter.applyPattern("MM/dd/yyyy");
        return dateFormatter.format(date);
    }
    
    public static String format_MM_DD_YYYY_HH_mm(Date date) {
        dateFormatter.applyPattern("MM/dd/yyyy HH:mm");
        return dateFormatter.format(date);
    }
    
    public static String format_YYYY(Date date){
        dateFormatter.applyPattern("yyyy");
        return dateFormatter.format(date);
    }

}
