package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NonPointBody implements ORLBody {

    private List formatters;

    public NonPointBody() {
        this.formatters = new ArrayList();

        formatters.add(new FipsFormatter());
        formatters.add(new SccFormatter());
        formatters.add(new SicFormatter());
        formatters.add(new MactFormatter());
        formatters.add(new SrcTypeFormatter());
        formatters.add(new NaicsFormatter());
        formatters.add(new CasFormatter());
        formatters.add(new AnnEmisFormatter());
    }

    public void write(ResultSet data, PrintWriter writer) throws SQLException {
        while (data.next()) {
            writeRecord(data, writer);
        }
    }

    private void writeRecord(ResultSet data, PrintWriter writer) throws SQLException {
        for (Iterator iter = formatters.iterator(); iter.hasNext();) {
            Formatter element = (Formatter) iter.next();
            element.format(data, writer);
        }

        // AVD_EMIS field
        if (data.getString("AVD_EMIS") == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(ORLFormats.AVD_EMIS_FORMAT.format(data.getDouble("AVD_EMIS")) + Formatter.DELIMITER);

        // CEFF field
        if (data.getString("CEFF") == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(ORLFormats.CEFF_FORMAT.format(data.getDouble("CEFF")) + Formatter.DELIMITER);

        // REFF field
        if (data.getString("REFF") == null)
            writer.print("-9" + Formatter.DELIMITER);
        else
            writer.print(ORLFormats.REFF_FORMAT.format(data.getDouble("REFF")) + Formatter.DELIMITER);

        // RPEN field
        String value = data.getString("RPEN");
        if (value == null)
            writer.print("-9");
        else
            writer.print(ORLFormats.RPEN_FORMAT.format(value));

        // Close the line
        writer.println();
    }
}
