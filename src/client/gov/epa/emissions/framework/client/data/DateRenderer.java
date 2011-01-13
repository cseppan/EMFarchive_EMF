package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.util.CustomDateFormat;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.table.DefaultTableCellRenderer;

public class DateRenderer extends DefaultTableCellRenderer {

    public DateRenderer() { super(); }

    public void setValue(Object value) {
        if ( value == null){
            setText("");
            return;
        }
//        if ( value instanceof Timestamp) {
//            Date date = new Date( ((Timestamp) value).getTime());
//            setText( formatter.format(date));
//        } else if ( value instanceof Time) {
//            Date date = new Date( ((Time) value).getTime());
//            setText( formatter.format(date));
//        } else 
        if ( value instanceof Calendar) {
            setText( CustomDateFormat.format_yyyy_MM_dd_HHmmss(((Calendar) value).getTime()));
        } else if ( value instanceof Date) { // sql.Date, sql.Time, sql.Timestamp are subclasses of Util.Date
            setText( CustomDateFormat.format_yyyy_MM_dd_HHmmss((Date)value));
        } else {
            throw new IllegalArgumentException("Cannot format given Object of " + value.getClass() + ".");
        }
    }

}
