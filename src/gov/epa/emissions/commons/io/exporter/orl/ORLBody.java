package gov.epa.emissions.commons.io.exporter.orl;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ORLBody {
    void write(ResultSet data, PrintWriter writer) throws SQLException;
}
