package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
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

    private Datasource datasource;

    private SqlDataTypes sqlDataTypes;

    public VersionedExporterFactory(Datasource datasource, SqlDataTypes sqlDataTypes) {
        this.datasource = datasource;
        this.sqlDataTypes = sqlDataTypes;
    }

    public Exporter create(Dataset dataset) throws EmfException {
        try {
            String exporterName = dataset.getDatasetType().getExporterClassName();

            Class exporterClass = Class.forName(exporterName);

            Class[] classParams = new Class[] { Dataset.class, Datasource.class, SqlDataTypes.class,
                    DataFormatFactory.class };
            Object[] params = new Object[] { dataset, datasource, sqlDataTypes, new VersionedDataFormatFactory(0) };

            Constructor exporterConstructor = exporterClass.getDeclaredConstructor(classParams);
            return (Exporter) exporterConstructor.newInstance(params);
        } catch (Exception e) {
            log.error("Could not create Exporter. Reason: " + e.getMessage());
            throw new EmfException("Could not create Exporter. Reason: " + e.getMessage());
        }
    }

}
