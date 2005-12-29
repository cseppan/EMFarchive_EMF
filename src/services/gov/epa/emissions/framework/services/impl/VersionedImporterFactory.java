package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.framework.services.EmfDataset;

import java.io.File;
import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VersionedImporterFactory {
    private static Log log = LogFactory.getLog(VersionedImporterFactory.class);

    private Datasource datasource;

    private SqlDataTypes sqlDataTypes;

    public VersionedImporterFactory(DbServer dbServer) {
        datasource = dbServer.getEmissionsDatasource();
        sqlDataTypes = dbServer.getSqlDataTypes();
    }

    public Importer create(EmfDataset dataset, File folder, String filename) throws ImporterException {
        String[] filePatterns = new String[] { filename };

        try {
            Importer importer = create(dataset, folder, filePatterns);
            return new VersionedImporter(importer, dataset, datasource);
        } catch (Exception e) {
            log.error("Failed to create importer: " + e.getMessage());
            throw new ImporterException("Failed to create importer: " + e.getMessage());
        }
    }

    private Importer create(EmfDataset dataset, File folder, String[] filePatterns) throws Exception {
        String importerClassName = dataset.getDatasetType().getImporterClassName();
        Class importerClass = Class.forName(importerClassName);

        Class[] classParams = new Class[] { File.class, String[].class, Dataset.class, Datasource.class,
                SqlDataTypes.class, DataFormatFactory.class };
        Object[] params = new Object[] { folder, filePatterns, dataset, datasource, sqlDataTypes,
                new VersionedDataFormatFactory(0) };

        Constructor importerConstructor = importerClass.getDeclaredConstructor(classParams);
        return (Importer) importerConstructor.newInstance(params);
    }
}
