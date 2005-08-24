package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PointBody implements ORLBody {

    public void write(ResultSet data, PrintWriter writer) throws SQLException {
        while (data.next()) {
            // FIPS field
            if (data.getString(1) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(FipsFormatter.FORMAT.format(data.getInt(1)) + Formatter.DELIMITER);

            // PLANTID field
            if (data.getString(3) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(3) + Formatter.DELIMITER);

            // POINTID field
            if (data.getString(4) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(4) + Formatter.DELIMITER);

            // STACKID field
            if (data.getString(5) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(5) + Formatter.DELIMITER);

            // SEGMENT field
            if (data.getString(6) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(6) + Formatter.DELIMITER);

            // PLANT field
            if (data.getString(7) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(7) + Formatter.DELIMITER);

            // SCC field
            if (data.getString(8) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(8) + Formatter.DELIMITER);

            // ERPTYPE field
            if (data.getString(9) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.ERPTYPE_FORMAT.format(data.getString(9)) + Formatter.DELIMITER);

            // SRCTYPE field
            if (data.getString(10) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(10) + Formatter.DELIMITER);

            // STKHGT field
            if (data.getString(11) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.STKHGT_FORMAT.format(data.getDouble(11)) + Formatter.DELIMITER);

            // STKDIAM field
            if (data.getString(12) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.STKDIAM_FORMAT.format(data.getDouble(12)) + Formatter.DELIMITER);

            // STKTEMP field
            if (data.getString(13) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.STKTEMP_FORMAT.format(data.getDouble(13)) + Formatter.DELIMITER);

            // STKFLOW field
            if (data.getString(14) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.STKFLOW_FORMAT.format(data.getDouble(14)) + Formatter.DELIMITER);

            // STKVEL field
            if (data.getString(15) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.STKVEL_FORMAT.format(data.getDouble(15)) + Formatter.DELIMITER);

            // SIC field
            if (data.getString(16) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(16) + Formatter.DELIMITER);

            // MACT field
            if (data.getString(17) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(17) + Formatter.DELIMITER);

            // NAICS field
            if (data.getString(18) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(18) + Formatter.DELIMITER);

            // CTYPE field
            if (data.getString(19) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.ERPTYPE_FORMAT.format(data.getString(19)) + Formatter.DELIMITER);

            // XLOC field
            if (data.getString(20) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.XLOC_FORMAT.format(data.getDouble(20)) + Formatter.DELIMITER);

            // YLOC field
            if (data.getString(21) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.YLOC_FORMAT.format(data.getDouble(21)) + Formatter.DELIMITER);

            // UTMZ field
            if (data.getString(22) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.UTMZ_FORMAT.format(data.getInt(22)) + Formatter.DELIMITER);

            // POLL field
            if (data.getString(23) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(data.getString(23) + Formatter.DELIMITER);

            // ANN_EMIS field
            if (data.getString(24) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(AnnEmisFormatter.FORMAT.format(data.getDouble(24)) + Formatter.DELIMITER);

            // AVD_EMIS field
            if (data.getString(25) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.AVD_EMIS_FORMAT.format(data.getDouble(25)) + Formatter.DELIMITER);

            // CEFF field
            if (data.getString(26) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.CEFF_FORMAT.format(data.getDouble(26)) + Formatter.DELIMITER);

            // REFF field
            if (data.getString(27) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.REFF_FORMAT.format(data.getDouble(27)) + Formatter.DELIMITER);

            // CPRI field
            if (data.getString(28) == null)
                writer.print("-9" + Formatter.DELIMITER);
            else
                writer.print(ORLFormats.CPRI_FORMAT.format(data.getInt(28)) + Formatter.DELIMITER);

            // CSEC field
            if (data.getString(29) == null)
                writer.print("-9");
            else
                writer.print(ORLFormats.CSEC_FORMAT.format(data.getInt(29)));

            // Close the line and count the number of rows
            writer.println();
        }

    }

}
