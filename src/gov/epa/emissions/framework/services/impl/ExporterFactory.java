/*
 * Creation on Sep 1, 2005
 * Eclipse Project Name: EMF
 * File Name: ExporterFactory.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.exporter.Exporter;
import gov.epa.emissions.commons.io.exporter.orl.ORLExporter;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class ExporterFactory {

	private DbServer dbServer;

	public ExporterFactory(DbServer dbServer) {
		this.dbServer = dbServer;
	}

	public Exporter create(String datasetType) {
		// FIXME: Get the specific type of importer for the filetype. Use a
		// Factory pattern
		return new ORLExporter(dbServer);
	}
}
