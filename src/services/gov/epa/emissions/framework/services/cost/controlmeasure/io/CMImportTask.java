package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperty;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import java.io.File;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class CMImportTask implements Runnable {

    private File folder;

    private String[] files;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private StatusDAO statusDao;

    private boolean truncate;

    private int[] sectorIds;

    public CMImportTask(File folder, String[] files, User user, boolean truncate, int[] sectorIds,
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.folder = folder;
        this.files = files;
        this.user = user;
        this.truncate = truncate;
        this.sectorIds = sectorIds;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.statusDao = new StatusDAO(sessionFactory);
    }

    public void run() {
        //if truncate measures, lets first backup the existing measures
        //then purge measures by sector(s)
        if (truncate) {
            Session session = sessionFactory.getSession();

            try {
                
                List<ControlMeasure> controlMeasures = new ControlMeasureDAO().getControlMeasureBySectors(sectorIds, session);
                int[] ids = new int[controlMeasures.size()];
                
                for (int i = 0; i < controlMeasures.size(); i++)
                    ids[i] = controlMeasures.get(i).getId();
                
                EmfProperty property = new EmfPropertiesDAO().getProperty("COST_CMDB_BACKUP_FOLDER", session);
                
                String backupFolder = property.getValue();

                CMExportTask exportTask = new CMExportTask(new File(backupFolder), CustomDateFormat.format_YYDDHHMMSS(new Date()), ids, user,
                        sessionFactory, dbServerFactory);
                exportTask.run();
            } catch (Exception e) {
//                LOG.error("Could not export control measures.", e);
                setDetailStatus("Could not export control measures: " + e.getMessage());
            } finally {
                session.close();
            }
        }
        try {
            ControlMeasuresImporter importer = null;
            try {
                importer = new ControlMeasuresImporter(folder, files, user, truncate, sectorIds, sessionFactory, dbServerFactory);
            } catch (Exception e) {
                setDetailStatus(e.getMessage());
                setStatus(e.getMessage());
            }
            if (importer != null)
                importer.run();
        } catch (Exception e) {
            //
        } finally {
            //
        }
    }

    private void setDetailStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImportDetailMsg");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("CMImport");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);
    }

}
