package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import corejava.Format;

public class RpenFormatter implements Formatter {

    public static final Format FORMAT = new Format("%6.2f");

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        String value = data.getString("RPEN");
        String evalValue = (value == null) ? "-9" : FORMAT.format(value);

        writer.print(evalValue);
    }

}
