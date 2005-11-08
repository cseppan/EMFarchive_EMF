package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.external.ExternalFilesImporter;
import gov.epa.emissions.commons.io.importer.meteorology.MeteorologyFilesImporter;
import gov.epa.emissions.commons.io.importer.modelreadyemissions.ModelReadyEmissionsFilesImporter;
import gov.epa.emissions.commons.io.importer.shape.ShapeFilesImporter;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.io.orl.ORLNonRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLPointImporter;
import gov.epa.emissions.framework.services.EMFConstants;

public class ImporterFactory {

    private DbServer dbServer;

    public ImporterFactory(DbServer dbServer) {
        this.dbServer = dbServer;
    }

    public Importer create(DatasetType datasetType) {
        Importer importer = null;

        // FIXME: Get the specific type of importer for the filetype. Use a
        // Factory pattern
        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_ORL) >= 0) {
            return orlImporter(dbServer, datasetType);
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
    private Importer orlImporter(DbServer dbServer, DatasetType datasetType) {
        Datasource emissions = dbServer.getEmissionsDatasource();
        SqlDataTypes dataType = dbServer.getDataType();

        if (datasetType.getName().equals("ORL Nonpoint Inventory"))
            return new ORLNonPointImporter(emissions, dataType);
        if (datasetType.getName().equals("ORL Nonroad Inventory"))
            return new ORLNonRoadImporter(emissions, dataType);
        if (datasetType.getName().equals("ORL Onroad Inventory"))
            return new ORLOnRoadImporter(emissions, dataType);
        if (datasetType.getName().equals("ORL Point Inventory"))
            return new ORLPointImporter(emissions, dataType);

        throw new RuntimeException("Dataset Type - " + datasetType.getName() + " unsupported");
    }

}
