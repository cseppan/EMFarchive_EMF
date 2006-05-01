package gov.epa.emissions.framework.services.exim;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.framework.services.EmfException;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VersionedExporterFactory {
    private static Log log = LogFactory.getLog(VersionedExporterFactory.class);

    private DbServer dbServer;

    private SqlDataTypes sqlDataTypes;

    private int batchSize;

    public VersionedExporterFactory(DbServer dbServer, SqlDataTypes sqlDataTypes, int batchSize) {
        this.dbServer = dbServer;
        this.sqlDataTypes = sqlDataTypes;
        this.batchSize = batchSize;
    }

    public Exporter create(Dataset dataset, Version version) throws EmfException {
        try {
            String exporterName = dataset.getDatasetType().getExporterClassName();

            Class exporterClass = Class.forName(exporterName);

            Class[] classParams = new Class[] { Dataset.class, DbServer.class, SqlDataTypes.class,
                    DataFormatFactory.class, Integer.class };
            Object[] params = new Object[] { dataset, dbServer, sqlDataTypes, new VersionedDataFormatFactory(version),
                    new Integer(batchSize) };

            Constructor exporterConstructor = exporterClass.getDeclaredConstructor(classParams);
            return (Exporter) exporterConstructor.newInstance(params);
        } catch (Exception e) {
            log.error("Could not create Exporter", e);
            throw new EmfException("Could not create Exporter for Dataset Type: " + dataset.getDatasetTypeName());
        }
    }

}
