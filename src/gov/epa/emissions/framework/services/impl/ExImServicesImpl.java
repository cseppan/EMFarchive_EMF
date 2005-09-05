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
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.commons.io.exporter.Exporter;
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
	private ExporterFactory exporterFactory;

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
		exporterFactory = new ExporterFactory(dbServer);
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

	/*
	 *  (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.ExImServices#startImport(gov.epa.emissions.framework.services.User, java.lang.String, gov.epa.emissions.commons.io.EmfDataset, gov.epa.emissions.commons.io.DatasetType)
	 */
	public void startImport(User user, String fileName, EmfDataset dataset, DatasetType datasetType)
			throws EmfException {
		log.debug("In ExImServicesImpl:startImport START");

		try {
			File file = validateFile(fileName);
			StatusServices statusSvc = new StatusServicesImpl();
			DataServices dataSvc = new DataServicesImpl();
			
			Importer importer = importerFactory.create(datasetType);
			ImportTask eximTask = new ImportTask(user, file, dataset, datasetType,
					dataSvc,statusSvc, importer);

			// FIXME: use a thread pool
			new Thread(eximTask).start();
		} catch (Exception e) {
			log.error("Exception attempting to start import of file: "
					+ fileName, e);
			throw new EmfException(e.getMessage());
		}

		log.debug("In ExImServicesImpl:startImport END");
	}

	/*
	 *  (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.ExImServices#startExport(gov.epa.emissions.framework.services.User, gov.epa.emissions.commons.io.Dataset, java.lang.String)
	 */
	public void startExport(User user, EmfDataset dataset, String fileName)
			throws EmfException {
		log.debug("In ExImServicesImpl:startExport START");

		try {
			File file = new File(fileName);
			StatusServices statusSvc = new StatusServicesImpl();
			DataServices dataSvc = new DataServicesImpl();
			Exporter exporter = exporterFactory.create(dataset.getDatasetType());
			System.out.println("Is exporter null? " + (exporter == null));
//		System.out.println(exporter.);
			ExportTask eximTask = new ExportTask(user,file,dataset,dataSvc,statusSvc,exporter);

			// FIXME: use a thread pool
			new Thread(eximTask).start();
		} catch (Exception e) {
			log.error("Exception attempting to start export of file: "
					+ fileName, e);
			throw new EmfException(e.getMessage());
		}

		log.debug("In ExImServicesImpl:startExport END");

	}

	/*
	 *  (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.ExImServices#getDatasetTypes()
	 */
	public DatasetType[] getDatasetTypes() throws EmfException {
		log.debug("In ExImServicesImpl:getDatasetTypes START");

//      Session session = HibernateUtils.currentSession();
    	Session session = EMFHibernateUtil.getSession();
		List datasettypes = DatasetTypesDAO.getDatasetTypes(session);
		log.debug("In ExImServicesImpl:getDatasetTypes END");
        session.flush();
        session.close();
		return (DatasetType[]) datasettypes
				.toArray(new DatasetType[datasettypes.size()]);
	}

	public void insertDatasetType(DatasetType aDst) throws EmfException {
		log.debug("In ExImServicesImpl:insertDatasetType START");

//      Session session = HibernateUtils.currentSession();
    	Session session = EMFHibernateUtil.getSession();
		DatasetTypesDAO.insertDatasetType(aDst, session);
        session.flush();
        session.close();
		log.debug("In ExImServicesImpl:insertDatasetType END");

	}


}
