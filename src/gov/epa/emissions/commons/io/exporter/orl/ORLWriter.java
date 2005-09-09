package gov.epa.emissions.commons.io.exporter.orl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Query;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.framework.EmfException;

public class ORLWriter {

    private ORLHeaderWriter headerWriter;

    private ORLBodyFactory bodyFactory;

    private DbServer dbServer;

    public ORLWriter(DbServer dbServer) {
        this.dbServer = dbServer;
        this.headerWriter = new ORLHeaderWriter();
        this.bodyFactory = new ORLBodyFactory();
    }

    public void write(EmfDataset dataset, File file) throws EmfException {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file.getCanonicalPath())));
            writeHeader(dataset, writer);
            writeBody(dataset, writer);
        } catch (Exception e) {
            throw new EmfException("could not export file - " + file, e.getMessage(), e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeBody(EmfDataset dataset, PrintWriter writer) throws SQLException {
        Datasource datasource = dbServer.getEmissionsDatasource();

        // TODO: we know ORL only has a single base table, but cleaner
        // interface needed
        Table baseTable = dataset.getTables()[0];
        String qualifiedTableName = datasource.getName() + "." + baseTable.getTableName();

        Query query = datasource.query();
        ResultSet data = query.selectAll(qualifiedTableName);

        String datasetType = dataset.getDatasetType();

        ORLBody body = bodyFactory.getBody(datasetType);
        body.write(data, writer);
    }

    private void writeHeader(EmfDataset dataset, PrintWriter writer) {
        headerWriter.writeHeader(dataset, writer);
    }
}
