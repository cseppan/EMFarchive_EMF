package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.io.orl.ORLNonRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.io.orl.ORLPointImporter;
import gov.epa.emissions.commons.io.temporal.TemporalProfileImporter;
import gov.epa.emissions.commons.io.temporal.TemporalReferenceImporter;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EMFConstants;
import gov.epa.emissions.framework.services.EmfDataset;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImporterFactory {
    private static Log log = LogFactory.getLog(ImporterFactory.class);

    private Datasource datasource;

    private SqlDataTypes sqlDataTypes;

    public ImporterFactory(DbServer dbServer) {
        datasource = dbServer.getEmissionsDatasource();
        sqlDataTypes = dbServer.getSqlDataTypes();
    }

    /*
     * TODO: After all importers constructors are changed to have same signatures, remove all these if statements
     * 
     */
    public Importer create(EmfDataset dataset, File folder, String filename) throws ImporterException {
        DatasetType datasetType = dataset.getDatasetType();
        String[] filePatterns = new String[] { filename };

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_ORL) >= 0) {
            Importer orlImporter = orlImporter(dataset, folder, filePatterns);
            return new VersionedImporter(orlImporter, dataset, datasource);
        }

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_TEMPORAL) >= 0) {
            Importer temporalImporter = temporalImporter(dataset, folder, filePatterns);
            return new VersionedImporter(temporalImporter, dataset, datasource);
        }

        try {// External
            return new ReflectionHelper().getImporterInstance(folder, filePatterns, dataset, datasource, sqlDataTypes);
        } catch (EmfException e) {
            log.error("Failed to create importer: " + e.getMessage());
            throw new ImporterException("Failed to create importer: " + e.getMessage());
        }
    }

    private Importer temporalImporter(EmfDataset dataset, File folder, String[] filenames) throws ImporterException {
        DatasetType datasetType = dataset.getDatasetType();

        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_TEMPORALCROSSREFERENCE) >= 0) {
            return new TemporalReferenceImporter(folder, filenames, dataset, datasource, sqlDataTypes);
        }

        // Temporal Profile
        if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_TEMPORALPROFILE) >= 0) {
            return new TemporalProfileImporter(folder, filenames, dataset, datasource, sqlDataTypes);
        }

        // If no datasetType match then throw exception
        throw new RuntimeException("Dataset Type - " + datasetType.getName() + " unsupported");
    }

    // FIXME: use a better scheme than rely on 'type names'
    private Importer orlImporter(EmfDataset dataset, File folder, String[] filenames) throws ImporterException {

        DatasetType datasetType = dataset.getDatasetType();

        if (datasetType.getName().equals("ORL Nonpoint Inventory"))
            return new ORLNonPointImporter(folder, filenames, dataset, datasource, sqlDataTypes);
        if (datasetType.getName().equals("ORL Nonroad Inventory"))
            return new ORLNonRoadImporter(folder, filenames, dataset, datasource, sqlDataTypes);
        if (datasetType.getName().equals("ORL Onroad Inventory"))
            return new ORLOnRoadImporter(folder, filenames, dataset, datasource, sqlDataTypes);
        if (datasetType.getName().equals("ORL Point Inventory"))
            return new ORLPointImporter(folder, filenames, dataset, datasource, sqlDataTypes);

        throw new RuntimeException("Dataset Type - " + datasetType.getName() + " unsupported");
    }

}
