package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.external.ExternalFilesImporter;
import gov.epa.emissions.commons.io.external.MeteorologyFilesImporter;
import gov.epa.emissions.commons.io.external.ModelReadyEmissionsFilesImporter;
import gov.epa.emissions.commons.io.external.ShapeFilesImporter;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.io.orl.ORLNonRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLPointImporter;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EmfDataset;

public class ImporterFactory {

    private DbServer dbServer;

    public ImporterFactory(DbServer dbServer) {
        this.dbServer = dbServer;
    }

    public Importer create(EmfDataset dataset) {
        Importer importer = null;
        DatasetType datasetType = dataset.getDatasetType();

        // FIXME: Get the specific type of importer for the filetype. Use a
        // Factory pattern
        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_ORL) >= 0) {
            return orlImporter(dbServer, dataset);
        } else if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_SHAPEFILES) >= 0) {
            return new ShapeFilesImporter(datasetType);
        } else if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_EXTERNALFILES) >= 0) {
            return new ExternalFilesImporter(datasetType);
        } else if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_MODELREADYEMISSIONSFILES) >= 0) {
            return new ModelReadyEmissionsFilesImporter(datasetType);
        } else if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_METEOROLOGYFILES) >= 0) {
            return new MeteorologyFilesImporter(datasetType);
        }
        return importer;
    }

    // FIXME: use a better scheme than rely on 'type names'
    private Importer orlImporter(DbServer dbServer, EmfDataset dataset) {
        Datasource emissions = dbServer.getEmissionsDatasource();
        SqlDataTypes dataType = dbServer.getSqlDataTypes();
        DatasetType datasetType = dataset.getDatasetType();

        if (datasetType.getName().equals("ORL Nonpoint Inventory"))
            return new ORLNonPointImporter(dataset, emissions, dataType);
        if (datasetType.getName().equals("ORL Nonroad Inventory"))
            return new ORLNonRoadImporter(dataset, emissions, dataType);
        if (datasetType.getName().equals("ORL Onroad Inventory"))
            return new ORLOnRoadImporter(dataset, emissions, dataType);
        if (datasetType.getName().equals("ORL Point Inventory"))
            return new ORLPointImporter(dataset, emissions, dataType);

        throw new RuntimeException("Dataset Type - " + datasetType.getName() + " unsupported");
    }

}
