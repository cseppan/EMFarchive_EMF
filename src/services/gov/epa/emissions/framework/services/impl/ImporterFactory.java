package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.framework.EmfException;
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

    public Importer create(EmfDataset dataset, File folder, String filename) throws ImporterException {
        String[] filePatterns = new String[] { filename };

        try {
            Importer importer = createImporter(dataset, folder, filePatterns);
            return new VersionedImporter(importer, dataset, datasource);
        } catch (EmfException e) {
            log.error("Failed to create importer: " + e.getMessage());
            throw new ImporterException("Failed to create importer: " + e.getMessage());
        }
    }

    private Importer createImporter(EmfDataset dataset, File folder, String[] filePatterns) throws EmfException {
        ReflectionHelper reflectionHelper = new ReflectionHelper();
        return reflectionHelper.createImporter(folder, filePatterns, dataset, datasource, sqlDataTypes);
    }

}
