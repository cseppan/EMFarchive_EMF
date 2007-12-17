package gov.epa.emissions.framework.tasks;

import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.Services;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ImportTask;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;

public class ImportCaseOutputTask extends ImportTask {
    
    @Override
    public boolean isEquivalent(Task task) { //NOTE: needs to verify definition of equality
        ImportCaseOutputTask importTask = (ImportCaseOutputTask) task;
        
        if (this.dataset.getName().equalsIgnoreCase(importTask.getDataset().getName())){
            return true;
        }
        
        return false;
    }

    protected static Log log = LogFactory.getLog(ImportCaseOutputTask.class);
    
    private CaseDAO caseDAO;

    private CaseOutput output;

    public ImportCaseOutputTask(CaseOutput output, EmfDataset dataset, String[] files, Importer importer, User user, Services services,
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        super(dataset, files, importer, user, services, dbServerFactory, sessionFactory);
        
        if (DebugLevels.DEBUG_1)
        {
            System.out.println(">>>> " + createId());
            System.out.println("Output="+output.getName()+";dataset="+dataset.getName()+";#files="+files.length+
                    ";file[0]="+files[0]);
        }
        
        this.output = output;
        this.caseDAO = new CaseDAO(sessionFactory);
    }

    public void run() {
        Session session = null;
        try {
            long startTime = System.currentTimeMillis();
            session = sessionFactory.getSession();
            session.setFlushMode(FlushMode.NEVER);
            
            prepare(session);
            importer.run();
            numSeconds = (System.currentTimeMillis() - startTime)/1000;
            complete(session, "Imported");
        } catch (Exception e) {
            // this doesn't give the full path for some reason
            logError("Failed to import file(s) : " + filesList(), e);
            setStatus("failed", "Failed to import dataset " + dataset.getName() + ". Reason: " + e.getMessage());
            removeDataset(dataset);
            updateCaseOutput("Failed", "Registering case output " + dataset.getName() + " of type " + output.getDatasetType() + " failed.");
        } finally {
            if (session != null)
                session.flush();
                session.close();
        }
    }

    protected void prepare(Session session) throws EmfException {
        addStartStatus();
        addCaseOutput(output);
        dataset.setStatus("Started import");
        addDataset(dataset, session);
    }

    private void addCaseOutput(CaseOutput localOutput) {
//        try {
//            if (caseDAO.caseOutputNameUsed(output.getName()))
//                throw new EmfException("The selected case output name is already in use");
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new EmfException(e.getMessage() == null ? "" : e.getMessage());
//        }
        
        localOutput.setMessage("Registering case output for " + dataset.getName() + " of type " + output.getDatasetType());
        this.output = caseDAO.add(localOutput);
    }

    private void updateCaseOutput(String status, String message) {
        output.setStatus(status);
        output.setMessage(message);
        caseDAO.updateCaseOutput(output);
    }

    protected void complete(Session session, String status) {
        dataset.setStatus(status);
        //dataset.setModifiedDateTime(new Date()); //last mod time has been set when creted
        output.setDatasetId(dataset.getId());
        updateDataset(dataset, session);
        updateCaseOutput(status, "Case output registered successfully.");
        addCompletedStatus();
    }

}
