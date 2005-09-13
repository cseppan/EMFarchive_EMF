package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import corejava.Format;

/**
 * Annual Emissions (ANN_EMIS)
 */
public class AnnEmisFormatter implements Formatter {

    public static final Format FORMAT = new Format("%14.7e");

    public void format(ResultSet data, PrintWriter writer) throws SQLException {
        if (data.getString("ANN_EMIS") == null)
            writer.print("-9");
        else
            writer.print(FORMAT.format(data.getDouble("ANN_EMIS")));
    }

}
