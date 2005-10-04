package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.orl.BaseORLImporter;

public class ImporterFactory {

	private DbServer dbServer;

	public ImporterFactory(DbServer dbServer) {
		this.dbServer = dbServer;
	}

	public Importer create(DatasetType datasetType) {
		// FIXME: Get the specific type of importer for the filetype. Use a
		// Factory pattern
		return new BaseORLImporter(dbServer, true);
	}

}
