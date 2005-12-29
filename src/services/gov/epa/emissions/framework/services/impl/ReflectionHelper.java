package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.framework.EmfException;

import java.io.File;
import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReflectionHelper {
    private static Log log = LogFactory.getLog(ReflectionHelper.class);

    Class fileClass = File.class;

    Class fileNamesClass = String[].class;

    Class datasetClass = Dataset.class;

    Class datasourceClass = Datasource.class;

    Class sqlDataTypesClass = SqlDataTypes.class;

    public Importer createImporter(File folder, String[] filePatterns, Dataset dataset, Datasource datasource,
            SqlDataTypes sqlDataType) throws EmfException {
        log.debug("Reflection helper");

        Importer importer = null;

        try {
            String importerName = dataset.getDatasetType().getImporterClassName();

            Class importerClass = Class.forName(importerName);

            Class[] classParams = new Class[5];
            classParams[0] = fileClass;
            classParams[1] = fileNamesClass;
            classParams[2] = datasetClass;
            classParams[3] = datasourceClass;
            classParams[4] = sqlDataTypesClass;

            Constructor importerConstructor = importerClass.getDeclaredConstructor(classParams);

            Object[] params = new Object[5];
            params[0] = folder;
            params[1] = filePatterns;
            params[2] = dataset;
            params[3] = datasource;
            params[4] = sqlDataType;

            importer = (Importer) importerConstructor.newInstance(params);

        } catch (Exception e) {
            log.error("Reflection exception: " + e.getMessage());
            throw new EmfException("Reflection exception: " + e.getMessage());
        }

        log.debug("Reflection helper: " + importer.getClass().getName());

        return importer;
    }

    public Exporter createExporter(File folder, String[] filePatterns, Dataset dataset, Datasource datasource,
            SqlDataTypes sqlDataType) throws EmfException {
        log.debug("Reflection helper");

        Exporter exporter = null;

        try {
            String exporterName = dataset.getDatasetType().getExporterClassName();

            Class exporterClass = Class.forName(exporterName);

            Class[] classParams = new Class[] { fileClass, fileNamesClass, datasetClass, datasourceClass,
                    sqlDataTypesClass };

            Constructor exporterConstructor = exporterClass.getDeclaredConstructor(classParams);

            Object[] params = new Object[5];
            params[0] = folder;
            params[1] = filePatterns;
            params[2] = dataset;
            params[3] = datasource;
            params[4] = sqlDataType;

            exporter = (Exporter) exporterConstructor.newInstance(params);

        } catch (Exception e) {
            log.error("Reflection exception: " + e.getMessage());
            throw new EmfException("Reflection exception: " + e.getMessage());
        }

        log.debug("Reflection helper: " + exporter.getClass().getName());

        return exporter;
    }

}
