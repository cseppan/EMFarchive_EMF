package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import corejava.Format;

/**
 * Five digit FIPS code for state and county
 */
class FipsFormatter implements Formatter {

    public static final Format FORMAT = new Format("%5d");

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        if (data.getString("FIPS") == null)
            writer.print("-9");
        else
            writer.print(FORMAT.format(data.getInt("FIPS")));
    }

}