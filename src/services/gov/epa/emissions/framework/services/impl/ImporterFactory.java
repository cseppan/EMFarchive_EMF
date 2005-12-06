package gov.epa.emissions.framework.services.impl;

import java.io.File;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.external.ExternalFilesImporter;
import gov.epa.emissions.commons.io.external.MeteorologyFilesImporter;
import gov.epa.emissions.commons.io.external.ModelReadyEmissionsFilesImporter;
import gov.epa.emissions.commons.io.external.ShapeFilesImporter;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
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

    public Importer create(EmfDataset dataset, File folder, String filename) throws ImporterException {
        DatasetType datasetType = dataset.getDatasetType();

        // FIXME: Get the specific type of importer for the filetype. Use a
        // Factory pattern
        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_ORL) >= 0) {
            File file = new File(folder, filename);
            return orlImporter(dbServer, dataset, file);
        }

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_SHAPEFILES) >= 0) {
            return new ShapeFilesImporter(folder, filename, dataset);
        }

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_EXTERNALFILES) >= 0) {
            return new ExternalFilesImporter(folder, filename, dataset);
        }

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_MODELREADYEMISSIONSFILES) >= 0) {
            return new ModelReadyEmissionsFilesImporter(folder, filename, dataset);
        }

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_METEOROLOGYFILES) >= 0) {
            return new MeteorologyFilesImporter(folder, filename, dataset);
        }

        return null;
    }

    // FIXME: use a better scheme than rely on 'type names'
    private Importer orlImporter(DbServer dbServer, EmfDataset dataset, File file) throws ImporterException {
        Datasource datasource = dbServer.getEmissionsDatasource();
        SqlDataTypes dataType = dbServer.getSqlDataTypes();
        DatasetType datasetType = dataset.getDatasetType();

        if (datasetType.getName().equals("ORL Nonpoint Inventory"))
            return new ORLNonPointImporter(file, dataset, datasource, dataType);
        if (datasetType.getName().equals("ORL Nonroad Inventory"))
            return new ORLNonRoadImporter(file, dataset, datasource, dataType);
        if (datasetType.getName().equals("ORL Onroad Inventory"))
            return new ORLOnRoadImporter(file, dataset, datasource, dataType);
        if (datasetType.getName().equals("ORL Point Inventory"))
            return new ORLPointImporter(file, dataset, datasource, dataType);

        throw new RuntimeException("Dataset Type - " + datasetType.getName() + " unsupported");
    }

}
