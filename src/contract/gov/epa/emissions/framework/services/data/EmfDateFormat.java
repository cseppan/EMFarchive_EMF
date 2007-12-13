package gov.epa.emissions.framework.services.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmfDateFormat {

    public static final String PATTERN_yyyyMMddHHmm = "yyyy/MM/dd HH:mm";
    
    public static final String PATTERN_MMddYYYY_HHmm = "MM/dd/yyyy HH:mm";

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat();
    

    public static String format_YYYY_MM_DD_HH_MM(Date date) {
        dateFormatter.applyPattern(PATTERN_yyyyMMddHHmm);
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_MM_DD_YYYY(Date date) {
        dateFormatter.applyPattern("MM/dd/yyyy");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_MM_DD_YYYY_HH_mm(Date date) {
        dateFormatter.applyPattern(PATTERN_MMddYYYY_HHmm);
        return date == null ? "" : dateFormatter.format(date);
    }

    public static Date parse_MM_DD_YYYY_HH_mm(String date) throws ParseException {
        dateFormatter.applyPattern(PATTERN_MMddYYYY_HHmm);
        return date == null ? null : dateFormatter.parse(date);
    }
    
    public static String format_YYYY(Date date) {
        dateFormatter.applyPattern("yyyy");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static Date parse_YYYY(String date) throws ParseException {
        dateFormatter.applyPattern("yyyy");
        return date == null ? null : dateFormatter.parse(date);
    }

    public static Date parse_MMddyyyy(String date) throws ParseException {
        dateFormatter.applyPattern("MM/dd/yyyy");
        return date == null ? null : dateFormatter.parse(date);
    }

    public static String format_ddMMMyyyy(Date date) {
        dateFormatter.applyPattern("ddMMMyyyy");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_MM_DD_YYYY_HH_mm_ss(Date date) {
        dateFormatter.applyPattern("MM/dd/yyyy HH:mm:ss");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_MMDDYYYYHHmmss(Date date) {
        dateFormatter.applyPattern("MM-dd-yyyy-HHmmss");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static Date parse_YYYY_MM_DD_HH_MM(String date) throws ParseException {
        dateFormatter.applyPattern("yyyy/MM/dd HH:mm");
        return date == null ? null : dateFormatter.parse(date);
    }

    public static String format_YYYY_MM_DD_HH_MM_ss_SS(Date date) {
        dateFormatter.applyPattern("yyyy/MM/dd HH:mm:ss:SS");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_yyyy_MM_dd_HHmmssSS(Date date) {
        dateFormatter.applyPattern("yyyy-MM-dd-HH:mm:ss:SS");
        return date == null ? "" : dateFormatter.format(date);
    }

    public static String format_YYYYMMDDHHMMSS(Date date) {
        dateFormatter.applyPattern("yyyyMMddHHmmss");
        return date == null ? "" : dateFormatter.format(date);
    }

}
