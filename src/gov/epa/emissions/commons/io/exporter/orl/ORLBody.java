package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ORLBody {
    static final String DELIMITER = ",";

    void write(PrintWriter writer, ResultSet data) throws SQLException;
}
