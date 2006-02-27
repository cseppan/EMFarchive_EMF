package gov.epa.emissions.framework.services.impl;

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

public class ImporterFactory {
    private static Log log = LogFactory.getLog(ImporterFactory.class);

    private DbServer dbServer;

    private SqlDataTypes sqlDataTypes;

    public ImporterFactory(DbServer dbServer, SqlDataTypes sqlDataTypes) {
        this.dbServer = dbServer;
        this.sqlDataTypes = sqlDataTypes;
    }

    public Importer createVersioned(EmfDataset dataset, File folder, String filename) throws ImporterException {
        String[] filePatterns = new String[] { filename };
        Importer importer = create(dataset, folder, filePatterns);
        return new VersionedImporter(importer, dataset, dbServer);
    }

    public Importer create(EmfDataset dataset, File folder, String[] filePatterns) throws ImporterException {
        try {
            return doCreate(dataset, folder, filePatterns);
        } catch (Exception e) {
            log.error("Failed to create importer. Cause: " + e.getCause().getMessage(), e.getCause());
            throw new ImporterException(e.getCause().getMessage());
        }
    }

    private Importer doCreate(EmfDataset dataset, File folder, String[] filePatterns) throws Exception {
        String importerClassName = dataset.getDatasetType().getImporterClassName();
        Class importerClass = Class.forName(importerClassName);

        Class[] classParams = new Class[] { File.class, String[].class, Dataset.class, DbServer.class,
                SqlDataTypes.class, DataFormatFactory.class };
        Object[] params = new Object[] { folder, filePatterns, dataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(0) };

        Constructor importerConstructor = importerClass.getDeclaredConstructor(classParams);
        return (Importer) importerConstructor.newInstance(params);
    }
}
