package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.framework.EmfException;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VersionedExporterFactory {
    private static Log log = LogFactory.getLog(VersionedExporterFactory.class);

    private DbServer dbServer;

    private SqlDataTypes sqlDataTypes;

    public VersionedExporterFactory(DbServer dbServer, SqlDataTypes sqlDataTypes) {
        this.dbServer = dbServer;
        this.sqlDataTypes = sqlDataTypes;
    }

    public Exporter create(Dataset dataset, int version) throws EmfException {
        try {
            String exporterName = dataset.getDatasetType().getExporterClassName();

            Class exporterClass = Class.forName(exporterName);

            Class[] classParams = new Class[] { Dataset.class, DbServer.class, SqlDataTypes.class,
                    DataFormatFactory.class };
            Object[] params = new Object[] { dataset, dbServer, sqlDataTypes, new VersionedDataFormatFactory(version) };

            Constructor exporterConstructor = exporterClass.getDeclaredConstructor(classParams);
            return (Exporter) exporterConstructor.newInstance(params);
        } catch (Exception e) {
            log.error("Could not create Exporter", e);
            throw new EmfException("Could not create Exporter for Dataset Type: " + dataset.getDatasetTypeName());
        }
    }

}
