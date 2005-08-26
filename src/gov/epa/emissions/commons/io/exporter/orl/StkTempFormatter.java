package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import corejava.Format;

/**
 * Stack Gas Exit Temperature (STKTEMP)
 */
public class StkTempFormatter implements Formatter {

    public static final Format FORMAT = new Format("%7.4f");

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        if (data.getString("stktemp") == null)
            writer.print("-9");
        else
            writer.print(FORMAT.format(data.getDouble("stktemp")));

    }

}
