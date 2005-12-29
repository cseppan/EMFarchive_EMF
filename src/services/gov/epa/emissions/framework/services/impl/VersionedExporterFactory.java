package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.framework.EmfException;

import java.io.File;
import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VersionedExporterFactory {
    private static Log log = LogFactory.getLog(VersionedExporterFactory.class);

    public Exporter createExporter(File folder, String[] filePatterns, Dataset dataset, Datasource datasource,
            SqlDataTypes sqlDataType) throws EmfException {
        try {
            String exporterName = dataset.getDatasetType().getExporterClassName();

            Class exporterClass = Class.forName(exporterName);

            Class[] classParams = new Class[] { File.class, String[].class, Dataset.class, Datasource.class,
                    SqlDataTypes.class };
            Object[] params = new Object[] { folder, filePatterns, dataset, datasource, sqlDataType };

            Constructor exporterConstructor = exporterClass.getDeclaredConstructor(classParams);
            return (Exporter) exporterConstructor.newInstance(params);
        } catch (Exception e) {
            log.error("Could not create Exporter. Reason: " + e.getMessage());
            throw new EmfException("Could not create Exporter. Reason: " + e.getMessage());
        }
    }

}
