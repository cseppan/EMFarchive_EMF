package gov.epa.emissions.framework.services.data;

import java.text.ParseException;
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
    
    public static String format_YYYY(Date date) {
        dateFormatter.applyPattern("yyyy");
        return dateFormatter.format(date);
    }

    public static Date parse_YYYY(String date) throws ParseException {
        dateFormatter.applyPattern("yyyy");
        return dateFormatter.parse(date);
    }

    public static Date parse_MMddyyyy(String date) throws ParseException {
        dateFormatter.applyPattern("MM/dd/yyyy");
        return dateFormatter.parse(date);
    }

    public static String format_ddMMMyyyy(Date date) {
        dateFormatter.applyPattern("ddMMMyyyy");
        return dateFormatter.format(date);
    }

    public static String format_MM_DD_YYYY_HH_mm_ss(Date date) {
        dateFormatter.applyPattern("MM/dd/yyyy HH:mm:ss");
        return dateFormatter.format(date);
    }

}
