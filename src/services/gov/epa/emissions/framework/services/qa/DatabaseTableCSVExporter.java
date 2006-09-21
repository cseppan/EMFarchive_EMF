package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.OptimizedQuery;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.ExportStatement;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.SimpleExportStatement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseTableCSVExporter implements Exporter {

    private Datasource datasource;

    private String delimiter;

    private int batchSize;

    private String tableName;

    public DatabaseTableCSVExporter(String tableName, Datasource datasource, int optimizedBatchSize) {
        this.tableName = tableName;
        this.datasource = datasource;
        this.batchSize = optimizedBatchSize;
        setDelimiter(",");
    }

    public void export(File file) throws ExporterException {
        PrintWriter writer = printWriter(file);
        write(file, writer);
    }

    private PrintWriter printWriter(File file) throws ExporterException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        } catch (IOException e) {
            throw new ExporterException("could not open file - " + file + " for writing");
        }
        return writer;
    }

    protected void write(File file, PrintWriter writer) throws ExporterException {
        try {
            String[] cols = getCols(datasource, tableName);
            writeColumnNames(writer, cols);
            writeData(writer, datasource, cols);
        } catch (SQLException e) {
            throw new ExporterException("could not export file - " + file, e);
        } finally {
            writer.close();
        }
    }

    private void writeData(PrintWriter writer, Datasource datasource, String[] cols) throws SQLException {
        String query = getQueryString(datasource);
        OptimizedQuery runner = datasource.optimizedQuery(query, batchSize);
        while (runner.execute()) {
            ResultSet rs = runner.getResultSet();
            writeBatchOfData(writer, rs, cols);
        }
        runner.close();
    }

    private String[] getCols(Datasource datasource, String tableName) throws SQLException {
        try {
            ColumnMetaData[] cols = datasource.tableDefinition().getTableMetaData(tableName).getCols();
            String[] colNames = new String[cols.length];
            for (int i = 0; i < colNames.length; i++) {
                colNames[i] = cols[i].getName();
            }
            return colNames;
        } catch (SQLException e) {
            throw new SQLException("Could not get the column names\n" + e.getMessage());
        }
    }

    private void writeColumnNames(PrintWriter writer, String[] cols) {
        for (int i = 0; i < cols.length; i++) {
            writer.print(cols[i]);
            if (i + 1 < cols.length)
                writer.print(delimiter);// delimiter
        }
        writer.println();
    }

    private void writeBatchOfData(PrintWriter writer, ResultSet data, String[] cols) throws SQLException {
        while (data.next())
            writeRecord(data, writer, cols);
        data.close();
    }

    private String getQueryString(Datasource datasource) {
        String qualifiedTable = datasource.getName() + "." + tableName;
        ExportStatement export = new SimpleExportStatement();

        return export.generate(qualifiedTable);
    }

    private void writeRecord(ResultSet data, PrintWriter writer, String[] cols) throws SQLException {
        for (int i = 0; i < cols.length; i++) {
            String value = data.getString(i + 1);
            if (value != null)
                writer.write(getValue(value));

            if (i + 1 < cols.length)
                writer.print(delimiter);// delimiter
        }
        writer.println();
    }

    protected String getValue(String value) {
        return formatValue(value);

    }

    protected String formatValue(String value) {
        if (containsDelimiter(value))
            return "\"" + value + "\"";

        return value;
    }

    public void setDelimiter(String del) {
        this.delimiter = del;
    }

    private boolean containsDelimiter(String s) {
        return s.indexOf(delimiter) >= 0;
    }

}
