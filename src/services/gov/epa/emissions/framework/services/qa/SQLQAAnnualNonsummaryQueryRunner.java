package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class SQLQAAnnualNonsummaryQueryRunner extends SQLQAProgramRunner{
    private String emissioDatasourceName;

    public SQLQAAnnualNonsummaryQueryRunner(DbServer dbServer, HibernateSessionFactory sessionFactory, QAStep qaStep) {
        super(dbServer, sessionFactory, qaStep);
        //this.sessionFactory = sessionFactory;
        this.emissioDatasourceName = dbServer.getEmissionsDatasource().getName();
        //this.qaStep = qaStep;
        
    }
    
    protected String query(DbServer dbServer, QAStep qaStep, String tableName) throws EmfException {
    //protected String query() throws EmfException {
        //System.out.println("The string is: " + qaStep.getProgramArguments());
        SQLQAAnnualNonsummaryQuery parser = new SQLQAAnnualNonsummaryQuery(sessionFactory, emissioDatasourceName, tableName, qaStep);
        return parser.createAnnualQuery();
    }
    
    public void run() throws EmfException{
        // Add the stuff from the superclass, and modify it to take in the datasets as program 
        // arguments.  Then pass it into the runner.  the output for the runner should go to the db
        // for processing.
        
        String programArguments = qaStep.getProgramArguments();
        //System.out.println("The string is: " + programArguments);
        //String programArguments = "hello";
        if (programArguments == null || programArguments.trim().length() == 0) {
            throw new EmfException("Please specify the annual emissions query");
        }
        //System.out.println("The input to tableName is: " + qaStep);
        String tableName = tableName(qaStep);
        //System.out.println("The tableName is: " + tableName);
        String query = query(dbServer, qaStep, tableName);
        printQuery(query);
        try {
            dropTable(getExistedTableName(qaStep));
            dbServer.getEmissionsDatasource().query().execute(query);
            success(qaStep, tableName);
        // Changed as per SQLQAProgramRunner
        } catch (Exception e) {
            failure(qaStep);
            //throw new EmfException("Check the query - " + query);
            throw new EmfException("Check the query - " + e.getMessage());
        }
    }
}
