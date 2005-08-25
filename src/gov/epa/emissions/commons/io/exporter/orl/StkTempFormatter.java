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
        // FIXME: the column that STKTEMP field corresponds to ?
        if (data.getString(13) == null)
            writer.print("-9");
        else
            writer.print(FORMAT.format(data.getDouble(13)));

    }

}
