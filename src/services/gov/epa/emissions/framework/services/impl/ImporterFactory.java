package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.DefaultORLDatasetTypesFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.external.ExternalFilesImporter;
import gov.epa.emissions.commons.io.importer.meteorology.MeteorologyFilesImporter;
import gov.epa.emissions.commons.io.importer.modelreadyemissions.ModelReadyEmissionsFilesImporter;
import gov.epa.emissions.commons.io.importer.orl.BaseORLImporter;
import gov.epa.emissions.commons.io.importer.shape.ShapeFilesImporter;
import gov.epa.emissions.framework.services.EMFConstants;

public class ImporterFactory {

    private DbServer dbServer;

    public ImporterFactory(DbServer dbServer) {
        this.dbServer = dbServer;
    }

    public Importer create(DatasetType datasetType) {
    	Importer importer = null;
    	
        // FIXME: Get the specific type of importer for the filetype. Use a
        // Factory pattern
    	if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_ORL)>=0){
            return new BaseORLImporter(dbServer, true, new DefaultORLDatasetTypesFactory());    		
    	}else if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_SHAPEFILES)>=0){
    		return new ShapeFilesImporter();
    	}else if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_EXTERNALFILES)>=0){
    		return new ExternalFilesImporter();
    	}else if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_MODELREADYEMISSIONSFILES)>=0){
    		return new ModelReadyEmissionsFilesImporter();
    	}else if (datasetType.getName().indexOf(EMFConstants.DATASETTYPE_NAME_METEOROLOGYFILES)>=0){
    		return new MeteorologyFilesImporter();
    	}
    	return importer;
    }

}
