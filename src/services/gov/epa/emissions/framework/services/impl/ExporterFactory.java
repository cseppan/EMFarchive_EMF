package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.orl.ORLNonPointExporter;
import gov.epa.emissions.commons.io.orl.ORLNonRoadExporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadExporter;
import gov.epa.emissions.commons.io.orl.ORLPointExporter;
import gov.epa.emissions.framework.services.EmfDataset;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class ExporterFactory {

    private DbServer dbServer;

    public ExporterFactory(DbServer dbServer) {
        this.dbServer = dbServer;
    }

    public Exporter create(EmfDataset dataset) {
        // FIXME: Use Factory pattern
        DatasetType datasetType = dataset.getDatasetType();
        String name = datasetType.getName();
        Datasource datasource = dbServer.getEmissionsDatasource();
        SqlDataTypes sqlTypes = dbServer.getDataType();

        // FIXME: matching w/ names is weak. Updating the name property of
        // DatasetType would break this Factory
        if (name.equals("ORL Nonpoint Inventory"))
            return new ORLNonPointExporter(dataset, datasource, sqlTypes);
        if (name.equals("ORL Nonroad Inventory"))
            return new ORLNonRoadExporter(dataset, datasource, sqlTypes);
        if (name.equals("ORL Onroad Inventory"))
            return new ORLOnRoadExporter(dataset, datasource, sqlTypes);
        if (name.equals("ORL Point Inventory"))
            return new ORLPointExporter(dataset, datasource, sqlTypes);

        throw new RuntimeException("Dataset Type - " + name + " unsupported");
    }
}
