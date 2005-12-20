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
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.io.orl.ORLNonRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLPointImporter;
import gov.epa.emissions.commons.io.temporal.TemporalProfileImporter;
import gov.epa.emissions.commons.io.temporal.TemporalReferenceImporter;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EmfDataset;

import java.io.File;

public class ImporterFactory {

    private DbServer dbServer;

    private Datasource datasource;

    private SqlDataTypes sqlDataTypes;

    public ImporterFactory(DbServer dbServer) {
        this.dbServer = dbServer;
        datasource = dbServer.getEmissionsDatasource();
        sqlDataTypes = dbServer.getSqlDataTypes();
    }

    public Importer create(EmfDataset dataset, File folder, String filename) throws ImporterException {
        DatasetType datasetType = dataset.getDatasetType();
        String[] filePatterns = new String[1];
        filePatterns[0]=filename;
                
        // FIXME: Get the specific type of importer for the filetype. Use a
        // Factory pattern
        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_ORL) >= 0) {
            File file = new File(folder, filename);
            return orlImporter(dataset, file);
        }

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_TEMPORAL) >= 0) {
            File file = new File(folder, filename);
            return temporalImporter(dataset, file);
        }

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_SHAPEFILES) >= 0) {
            return new ShapeFilesImporter(folder,filePatterns,dataset,datasource,sqlDataTypes);
        }

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_EXTERNALFILES) >= 0) {
            return new ExternalFilesImporter(folder,filePatterns,dataset,datasource,sqlDataTypes);
        }

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_MODELREADYEMISSIONSFILES) >= 0) {
            return new ModelReadyEmissionsFilesImporter(folder,filePatterns,dataset,datasource,sqlDataTypes);
        }

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_METEOROLOGYFILES) >= 0) {
            return new MeteorologyFilesImporter(folder,filePatterns,dataset,datasource,sqlDataTypes);
        }

        return null;
    }

    private Importer temporalImporter(EmfDataset dataset, File file) throws ImporterException {
        DatasetType datasetType = dataset.getDatasetType();

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_TEMPORALCROSSREFERENCE) >= 0) {
            return new TemporalReferenceImporter(file, dataset, datasource, sqlDataTypes);
        }

        // Temporal Profile
        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_TEMPORALPROFILE) >= 0) {
            return new TemporalProfileImporter(file, dataset, datasource, sqlDataTypes);
        }

        // If no datasetType match then throw exception
        throw new RuntimeException("Dataset Type - " + datasetType.getName() + " unsupported");
    }

    // FIXME: use a better scheme than rely on 'type names'
    private Importer orlImporter(EmfDataset dataset, File file) throws ImporterException {

        DatasetType datasetType = dataset.getDatasetType();

        if (datasetType.getName().equals("ORL Nonpoint Inventory"))
            return new ORLNonPointImporter(file, dataset, datasource, sqlDataTypes);
        if (datasetType.getName().equals("ORL Nonroad Inventory"))
            return new ORLNonRoadImporter(file, dataset, datasource, sqlDataTypes);
        if (datasetType.getName().equals("ORL Onroad Inventory"))
            return new ORLOnRoadImporter(file, dataset, datasource, sqlDataTypes);
        if (datasetType.getName().equals("ORL Point Inventory"))
            return new ORLPointImporter(file, dataset, datasource, sqlDataTypes);

        throw new RuntimeException("Dataset Type - " + datasetType.getName() + " unsupported");
    }

}
