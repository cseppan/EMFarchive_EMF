package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.db.DbServer;
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
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyMeasure;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.FlushMode;
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
            
            DbServer dbServer = dbServerFactory.getDbServer();

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
            
                
                
                //look for dependencies on Control Strategies and Control Programs
                ControlStrategyDAO csDAO = new ControlStrategyDAO();
                List<ControlStrategy> cs = //new ControlStrategyDAO().getControlStrategiesByControlMeasures(ids, session);
                    csDAO.getControlStrategiesByControlMeasures(ids, session);
                
                String msg = "There are " + cs.size() + " Strategies affected.";
                setDetailStatus( msg + "\n");
                setStatus( msg);
                
                String cmMsg = "";
                for ( ControlStrategy s : cs) {
                    
                    //setDetailStatus( ">>> \n" + s.getDescription() + "\n"); // for debug
                    
                    cmMsg = ""; //s.getDescription();
                    cmMsg += "Purge: " + this.truncate + "\n";
                    cmMsg += "Date deleted: " + new Date() + "\n";
                    int numCMToBeDeleted = this.getNumControlMeasuresDeleted(s, ids);
                    cmMsg += "Measures deleted: " + numCMToBeDeleted + "\n";
                    cmMsg += "Control Technolgies Affected: \n";
                    cmMsg += this.getControlTechnologiesAffected(s, ids);
                    //setDetailStatus( ">>> " + s.getName() + ": \n" + cmMsg + "\n"); // for debug
                    //s.setDescription( desc);
                    //s.setIsFinal( true);
                    csDAO.finalizeControlStrategy(s.getId(), cmMsg, session);
                }
                
                ControlProgramDAO cpDAO = new ControlProgramDAO();
                List<ControlProgram> cp = //new ControlStrategyDAO().getControlStrategiesByControlMeasures(ids, session);
                    cpDAO.getControlProgramsByControlMeasures(ids, session);
                
                msg = "There are " + cp.size() + " Programs affected.";
                setDetailStatus( msg + "\n");
                setStatus( msg);
                
                for ( ControlProgram p : cp) {
                    
                    //setDetailStatus( ">>> \n" + s.getDescription() + "\n"); // for debug
                    
                    cmMsg = ""; //s.getDescription();
                    cmMsg += "Purge: " + this.truncate + "\n";
                    cmMsg += "Date deleted: " + new Date() + "\n";
                    int numCMToBeDeleted = this.getNumControlMeasuresDeleted(p, ids);
                    cmMsg += "Measures deleted: " + numCMToBeDeleted + "\n";
                    cmMsg += "Control Technolgies Affected: \n";
                    cmMsg += this.getControlTechnologiesAffected(p, ids);
                    //setDetailStatus( ">>> " + p.getName() + ": \n" + cmMsg + "\n"); // for debug
                    //s.setDescription( desc);
                    //s.setIsFinal( true);
                    cpDAO.updateControlProgram(p.getId(), cmMsg, session);
                }                

//README                
//To get a count of the measures being deleted from a strategy
//create a function that can iterate over the strategy's measures (ALL Measures are returned not just the ones of interest to us)
//and see if they match the an item in the ids array, 
//you should be able to use a similar technique and build a Collection of DISTINCT Strategy ControlTechnolgies 
//so this can iterated over and reported on in the strategy/control program description
//Actually, the controlStrategy.getControlMeasures()[0].getControlMeasure() returns a Light version of the ControlMeasure -- LightControlMeasure.java is the POJO
//this class doesn't have the ControlTechnology field -- you need to add the getter/setter for this to the POJO and then make sure you map the new field 
//in the LightControlMeasure.hbm.xml file, rebuild, and it now should be populated and accesible.
                
//Here is some sample code on how finalize a control strategy with a message being appended to the description
//Create something similar for the ControlProgram, but name it something like updateControlProgramDescription, since
//we're not finalizing a ControlProgram
//                for (ControlStrategy controlStrategy : cs) {
//                    csDAO.finalizeControlStrategy(controlStrategy.getId(), "Some Message for the Description Field", session);
//                    controlStrategy.getControlMeasures()[0].getControlMeasure()
//                    break;
//                }
                
                //System.out.println(cs.size());
                
                //next you need to finalize Control Strategies and add a relevant message to the description
                
                
                
                //next you need to update the Control Programs and add a relevant message to the description
                
                
                
                
                //delete measure by sector....
//                session.setFlushMode(FlushMode.NEVER);

                new ControlMeasureDAO().remove(sectorIds, sessionFactory, dbServer);
                
            } catch (Exception e) {
//                LOG.error("Could not export control measures.", e);
                e.printStackTrace();
                setDetailStatus("Exception occured: " + e.getMessage());
            } finally {
                session.close();
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
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
    
    public int getControlMeasureCountInSummaryFile(boolean purge, int [] sectorIDs, String folderPath, String[] fileNames, User user)  {
        try {
            ControlMeasuresImporter importer = null;
            try {
                importer = new ControlMeasuresImporter(folder, files, user, truncate, sectorIds, sessionFactory, dbServerFactory);
            } catch (Exception e) {
                setDetailStatus(e.getMessage());
                setStatus(e.getMessage());
            }
            if (importer != null)
                return importer.getControlMeasureCountInSummaryFile();
        } catch (Exception e) {
            //
        } finally {
            //
        }
        return 0;
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
    
    private int getNumControlMeasuresDeleted( ControlStrategy cs, int [] cmIDs) {
        ControlStrategyMeasure[] csms = cs.getControlMeasures();
        if ( csms == null || cmIDs == null || cmIDs.length == 0 || csms.length == 0) {
            return 0;
        }
        int count = 0;
        for ( int i = 0; i<csms.length; i++) {
            int id = csms[i].getControlMeasure().getId();
            boolean toBeDeleted = false;
            for ( int j=0; j<cmIDs.length; j++) {
                if ( cmIDs[j] == id) {
                    toBeDeleted = true;
                    break;
                }
            }
            if ( toBeDeleted ) {
                count ++;
            }
        }
        return count;
    }
    
    private String getControlTechnologiesAffected(ControlStrategy cs, int [] cmIDs){
        if ( cs == null || cmIDs == null || cmIDs.length == 0)
            return "";
        String techNames = "";
        List<Integer> ctIDList = new ArrayList<Integer>();
        
        ControlStrategyMeasure[] csms = cs.getControlMeasures();
        for ( int i = 0; i<csms.length; i++) {
            LightControlMeasure lcm = csms[i].getControlMeasure();
            ControlTechnology ct = lcm.getControlTechnology();
            if (ct != null) {
                int id = ct.getId();
                if ( !ctIDList.contains( id)) {
                    ctIDList.add( id);
                    techNames += "  " + ct.getName() + "\n";
                }
            }
        }
        
        System.out.println( techNames);        
        
        return techNames;        
    }
    
    private int getNumControlMeasuresDeleted( ControlProgram cp, int [] cmIDs) {
        if ( cp == null) {
            return 0;
        }
        ControlMeasure[] csms = cp.getControlMeasures();
        if ( csms == null || cmIDs == null || cmIDs.length == 0 || csms.length == 0) {
            return 0;
        }
        int count = 0;
        for ( int i = 0; i<csms.length; i++) {
            int id = csms[i].getId();
            boolean toBeDeleted = false;
            for ( int j=0; i<cmIDs.length; j++) {
                if ( cmIDs[j] == id) {
                    toBeDeleted = true;
                    break;
                }
            }
            if ( toBeDeleted ) {
                count ++;
            }
        }
        return count;
    }
    
    private String getControlTechnologiesAffected(ControlProgram cs, int [] cmIDs){
        if ( cs == null || cmIDs == null || cmIDs.length == 0)
            return "";
        String techNames = "";
        List<Integer> ctIDList = new ArrayList<Integer>();
        
        ControlMeasure[] csms = cs.getControlMeasures();
        for ( int i = 0; i<csms.length; i++) {
            ControlTechnology ct = csms[i].getControlTechnology();
            int id = ct.getId();
            if ( !ctIDList.contains( id)) {
                ctIDList.add( id);
                techNames += "  " + ct.getName() + "\n";
            }
        }
        
        System.out.println( techNames);        
        
        return techNames;        
    }

}
