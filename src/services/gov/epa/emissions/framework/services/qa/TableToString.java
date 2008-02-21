package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.ExporterException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class TableToString {

    private String qualifiedTableName;

    private Datasource datasource;

    private String delimiter;

    private StringBuffer output;

    protected String lineFeeder = System.getProperty("line.separator");

    public TableToString(DbServer dbServer, String qualifiedTableName, String delimiter) {
        this.qualifiedTableName = qualifiedTableName;
        this.datasource = dbServer.getEmissionsDatasource();
        this.delimiter = delimiter;
        this.output = new StringBuffer();
    }

    public String toString() {
        try {
            writeToString();
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return output.toString();
    }

    private void writeToString() throws Exception {
        try {
            ResultSet rs = datasource.query().executeQuery("select * from " + qualifiedTableName);
            ResultSetMetaData md = rs.getMetaData();
            writeHeaderRow(md);
            int columnCount = md.getColumnCount();
            String row = "";
            String value = "";
            while (rs.next()) {
                row = "";
                for (int i = 1; i <= columnCount; i++) {
                    value = rs.getString(i);
                    row += (i > 1 ? delimiter : lineFeeder) + (!rs.wasNull() ? value : "");
                }
                output.append(row);
            }
        } catch (SQLException e) {
            throw new ExporterException("could not export lines ", e);
        }
    }

    private void writeHeaderRow(ResultSetMetaData md) throws SQLException {
        String header = "";
        for (int i = 1; i <= md.getColumnCount(); i++) {
            header += (i > 1 ? delimiter : "") + md.getColumnName(i).toLowerCase();
        }
        output.append(header);
    }
}
