package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;

import org.hibernate.Session;

public class SQLQAProgramQuery {
    
    // Input is currently a set of 12 or 24 or 12*n monthly files
    // The lists are filled using command-line and/or GUI input.
    
    protected QAStep qaStep;

    protected String tableName;

    protected HibernateSessionFactory sessionFactory;
    
    protected String emissionDatasourceName;
       
    protected boolean hasInvTableDataset;
    
    protected ArrayList<String> datasetNames = new ArrayList<String>();
    
    protected DatasetDAO dao = new DatasetDAO();
    

    public SQLQAProgramQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName, String tableName, QAStep qaStep) {
        
        this.qaStep = qaStep;
        this.tableName = tableName;
        this.sessionFactory = sessionFactory;
        this.emissionDatasourceName = emissioDatasourceName;         
    }            

    protected void checkDataset() throws EmfException {
        String errors ="";
        Session session = sessionFactory.getSession();
        try {
            if ( datasetNames.size() > 0){
                for (String dsName : datasetNames){
                    //System.out.println(dsName);
                    if ( !dao.exists(dsName, session))
                        errors += "The dataset name \"" + dsName + "\" does not exist. ";
                }
                if ( errors.length() > 0)
                    throw new EmfException(errors);
            }else
                throw new EmfException("Inventories are needed. ");
        } catch (Exception ex) {           
            throw new EmfException(ex.getMessage());
        } finally {
            session.close();
        }
    }
    
    protected EmfDataset getDataset(String dsName) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getDataset(session, dsName);
        } catch (Exception ex) {
            //ex.printStackTrace();
            throw new EmfException("The dataset named" + dsName + " does not exist");
        } finally {
            session.close();
        }

    }
    
    protected EmfDataset getDataset(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getDataset(session, id);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new EmfException("The dataset id " + id + " is not valid");
        } finally {
            session.close();
        }
    }

}
