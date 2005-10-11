package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.DefaultORLDatasetTypesFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.orl.BaseORLImporter;
import gov.epa.emissions.commons.io.importer.shape.ShapefilesImporter;

public class ImporterFactory {

    private DbServer dbServer;

    public ImporterFactory(DbServer dbServer) {
        this.dbServer = dbServer;
    }

    public Importer create(DatasetType datasetType) {
    	Importer importer = null;
    	
        // FIXME: Get the specific type of importer for the filetype. Use a
        // Factory pattern
    	if (datasetType.getName().indexOf("ORL")>=0){
            return new BaseORLImporter(dbServer, true, new DefaultORLDatasetTypesFactory());    		
    	}else if (datasetType.getName().indexOf("Shape")>=0){
    		return new ShapefilesImporter();
    	}
    	return importer;
    }

}
