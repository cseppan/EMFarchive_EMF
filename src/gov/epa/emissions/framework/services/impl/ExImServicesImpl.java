/*
 * Created on Aug 4, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.service;
 * File Name: EMFDataService.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services.impl;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetTypesDAO;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.StatusServices;
import gov.epa.emissions.framework.services.User;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class ExImServicesImpl implements ExImServices {

	private static Log log = LogFactory.getLog(ExImServicesImpl.class);

	private ImporterFactory importerFactory;

	public ExImServicesImpl() throws NamingException, SQLException {
		// TODO: should we move this into an abstract super class ?
		Context ctx = new InitialContext();
		DataSource datasource = (DataSource) ctx
				.lookup("java:/comp/env/jdbc/EMFDB");

		// FIXME: we should not hard-code the db server. Also, read the
		// datasource names from properties
		DbServer dbServer = new PostgresDbServer(datasource.getConnection(),
				"reference", "emissions");

		importerFactory = new ImporterFactory(dbServer);
	}

	private File validateFile(String fileName) throws EmfException {
		log.debug("check if file exists " + fileName);
		File file = new File(fileName);

		if (!file.exists() || !file.isFile()) {
			log.error("file " + fileName + " not found");
			throw new EmfException("file " + fileName + " not found");
		}
		log.debug("check if file exists " + fileName);

		return file;
	}

	public void startImport(User user, String filename, EmfDataset dataset, DatasetType datasetType)
			throws EmfException {
		log.debug("In ExImServicesImpl:startImport START");

		try {
			File file = validateFile(filename);
			StatusServices statusSvc = new StatusServicesImpl();
			DataServices dataSvc = new DataServicesImpl();
			
			Importer importer = importerFactory.create(datasetType);
			ImportTask eximTask = new ImportTask(user, file, dataset, datasetType,
					dataSvc,statusSvc, importer);

			// FIXME: use a thread pool
			new Thread(eximTask).start();
		} catch (Exception e) {
			log.error("Exception attempting to start import of file: "
					+ filename, e);
			throw new EmfException(e.getMessage());
		}

		log.debug("In ExImServicesImpl:startImport END");
	}

	public DatasetType[] getDatasetTypes() throws EmfException {
		log.debug("In ExImServicesImpl:getDatasetTypes START");

		Session session = HibernateUtils.currentSession();
		List datasettypes = DatasetTypesDAO.getDatasetTypes(session);
		log.debug("In ExImServicesImpl:getDatasetTypes END");

		return (DatasetType[]) datasettypes
				.toArray(new DatasetType[datasettypes.size()]);
	}

	public void insertDatasetType(DatasetType aDst) throws EmfException {
		log.debug("In ExImServicesImpl:insertDatasetType START");

		Session session = HibernateUtils.currentSession();
		DatasetTypesDAO.insertDatasetType(aDst, session);

		log.debug("In ExImServicesImpl:insertDatasetType END");

	}

	public void startExport(User user, Dataset dataset, String fileName)
			throws EmfException {
		// TODO Auto-generated method stub

	}

}
