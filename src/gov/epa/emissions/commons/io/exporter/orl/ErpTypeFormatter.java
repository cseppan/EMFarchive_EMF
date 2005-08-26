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
        String value = data.getString("erptype");
        String evalValue = (value == null) ? "-9" : ERPTYPE_FORMAT.format(value);
        
        writer.print(evalValue);
    }

}
