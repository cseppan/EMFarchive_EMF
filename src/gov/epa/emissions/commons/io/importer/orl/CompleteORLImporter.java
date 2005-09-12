package gov.epa.emissions.commons.io.importer.orl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.Table;
import gov.epa.emissions.commons.io.importer.SummaryTableCreator;
import gov.epa.emissions.commons.io.importer.TableType;

import java.io.File;

/**
 * The importer for ORL (One Record per Line) format text files.
 */
public class CompleteORLImporter extends BaseORLImporter {
    public CompleteORLImporter(DbServer dbServer, boolean useTransactions, boolean annualNotAverageDaily) {
        super(dbServer, useTransactions, annualNotAverageDaily);
    }

    public void run(File[] files, Dataset dataset, boolean overwrite) throws Exception {
        super.run(files, dataset, overwrite);
        createSummaryTable(dataset, overwrite);
    }

    private void createSummaryTable(Dataset dataset, boolean overwrite) throws Exception {
        Datasource emissionsDatasource = dbServer.getEmissionsDatasource();

        TableType tableType = tableTypes.type(dataset.getDatasetType());
        // only one base type.
        // FIXME: why not have a ORLTableType that only has one base table ?
        Table table = dataset.getTable(tableType.baseTypes()[0]);
        String qualifiedTableName = emissionsDatasource.getName() + "." + table.getTableName();

        String summaryTableSuffix = (String) dataset.getTablesMap().get(tableType.summaryType());
        String summaryTable = emissionsDatasource.getName() + "." + summaryTableSuffix;

        SummaryTableCreator modifier = new SummaryTableCreator(dbServer.getEmissionsDatasource(), dbServer
                .getReferenceDatasource());
        modifier.createORLSummaryTable(dataset.getDatasetType(), qualifiedTableName, summaryTable, overwrite,
                annualNotAverageDaily);
    }

}
