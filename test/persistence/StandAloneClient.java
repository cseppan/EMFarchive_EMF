import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.dao.DatasetDAO;
import gov.epa.emissions.framework.dao.DatasetTypesDAO;
import gov.epa.emissions.framework.services.impl.DatasetTypesServicesImpl;
import gov.epa.emissions.framework.services.impl.HibernateUtils;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class StandAloneClient {
    private static Log log = LogFactory.getLog(DatasetTypesServicesImpl.class);

    public StandAloneClient() throws EmfException{
      super();  
      //doDatasetTypes();
      
      doDatasets();
    }
   
    private void doDatasets() throws EmfException {
        Dataset[] allDatasets = getDatasets();
        
        
        
        if (false) throw new EmfException("");
    }

    private Dataset[] getDatasets() throws EmfException {
        List datasets = null;
        try {
            log.debug("In DatasetTypesServicesImpl:getDatasetTypes START");
            Session session = HibernateUtils.currentSession();
            datasets = DatasetDAO.getDatasets(session);
            log.debug("In DatasetServicesImpl:getDatasetTypes END: " + datasets.size());
            session.flush();
            //session.close();

//            hsqlCleanup(session);
        } catch (HibernateException e) {
            log.error("Error in the database" + e);
            throw new EmfException("Database error");
        }

        return (Dataset[]) datasets.toArray(new DatasetType[datasets.size()]);
    }

    private void doDatasetTypes() throws EmfException {
        DatasetType[] allDsts = getDatasetTypes();
        log.debug("OUTPUT $$$$$$$ " + allDsts.length);
        Keyword kw = new Keyword("Halo");
        for (int i=0; i<allDsts.length;i++){
            
            DatasetType dst =allDsts[i]; 
            Keyword[] kws = dst.getKeywords();
            System.out.println("DatasetName: " + dst.getName() + " # of Kws: " + kws.length);
            if (dst.getName().equals("Shapefile")){
                dst.addKeyword(kw);
                updateDatasetType(dst);
            }
        }
    }

    private void updateDatasetType(DatasetType dst) throws EmfException {
        try {
            log.debug("In DatasetTypesServicesImpl:getDatasetTypes START");
            Session session = HibernateUtils.currentSession();
            DatasetTypesDAO.updateDatasetType(dst,session);
            log.debug("In UPDATE ");
            
            session.flush();
            session.close();
        } catch (HibernateException e) {
            log.error("Error in the database" + e);
            throw new EmfException("Database error");
        }
        
    }

    public DatasetType[] getDatasetTypes() throws EmfException{
            
            List datasettypes = null;
            try {
                log.debug("In DatasetTypesServicesImpl:getDatasetTypes START");
                Session session = HibernateUtils.currentSession();
                datasettypes = DatasetTypesDAO.getDatasetTypes(session);
                log.debug("In DatasetTypesServicesImpl:getDatasetTypes END: " + datasettypes.size());
                session.flush();
                //session.close();

//                hsqlCleanup(session);
            } catch (HibernateException e) {
                log.error("Error in the database" + e);
                throw new EmfException("Database error");
            }

            return (DatasetType[]) datasettypes.toArray(new DatasetType[datasettypes.size()]);
        }

    
	public static void main(String[] args) throws Exception {
        new StandAloneClient();
               
		System.exit(0);	
	}	
    
    private void hsqlCleanup(Session s) {
        try {
            s.connection().createStatement().execute("SHUTDOWN");
        } catch (Exception e) {
        }
    }

}