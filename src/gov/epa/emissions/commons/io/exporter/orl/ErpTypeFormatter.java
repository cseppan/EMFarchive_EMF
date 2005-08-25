package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import corejava.Format;

/**
 * Emissions Release Point Type
 */
public class ErpTypeFormatter implements Formatter {

    public static final Format ERPTYPE_FORMAT = new Format("%2s");

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        // FIXME: the column that ERPTYPE field corresponds to ?
        String value = data.getString(9);
        String evalValue = (value == null) ? "-9" : ERPTYPE_FORMAT.format(value);
        
        writer.print(evalValue);
    }

}
