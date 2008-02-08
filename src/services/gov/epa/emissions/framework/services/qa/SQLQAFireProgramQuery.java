package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class SQLQAFireProgramQuery {
    
    private QAStep qaStep;

    private String tableName;

    private HibernateSessionFactory sessionFactory;
    
    private static final String poundQueryTag = "#";
    
    private String emissioDatasourceName;
    
    private static final String invTableTag = "-invtable";

    ArrayList<String> allDatasetNames = new ArrayList<String>();
    
    private boolean hasInvTableDataset;
    
    public SQLQAFireProgramQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName, String tableName, QAStep qaStep) {
        this.qaStep = qaStep;
        this.tableName = tableName;
        this.sessionFactory = sessionFactory;
        this.emissioDatasourceName = emissioDatasourceName;
    }
        
    public String createFireProgramQuery() throws EmfException {
        
        String programArguments = qaStep.getProgramArguments();
        
        //get applicable tables from the program arguments
        String inventoriesToken = "";
        String invtableToken = "";
        String invTableDatasetName = "";
        
        int index1 = programArguments.indexOf(invTableTag);
        inventoriesToken = programArguments.substring(0, index1).trim();
        invtableToken = programArguments.substring(index1 + invTableTag.length());

         //parse inventories names...
        if (inventoriesToken.length() > 0) {
            StringTokenizer tokenizer2 = new StringTokenizer(inventoriesToken);
            tokenizer2.nextToken();
            while (tokenizer2.hasMoreTokens()) {
                String datasetName = tokenizer2.nextToken().trim();
                if (datasetName.length() > 0)
                    allDatasetNames.add(datasetName);
            }
        } else {
            //see if there are tables to build the query with, if not throw an exception
            throw new EmfException("There are no ORL Day-Specific Fire Data Inventory datasets specified.");
        }

         //parse inventory table name...
         StringTokenizer tokenizer3 = new StringTokenizer(invtableToken);
         while (tokenizer3.hasMoreTokens()) {
             invTableDatasetName  = tokenizer3.nextToken().trim();
             if (invTableDatasetName.length() > 0) hasInvTableDataset = true;
         }
         
        //Create the outer query (the # symbol is a placeholder for the inner sql statement)
         String outerQuery = "select te.fipsst, te.scc, " 
             + (hasInvTableDataset ? "coalesce(i.name, te.data)" : "te.data") + " as data, "
             + "sum(datavalue) as datavalue "
             + "\nfrom (#) as te " 
             + (hasInvTableDataset ? "\nleft outer join\n $DATASET_TABLE[\"" + invTableDatasetName + "\", 1] i \non te.data = i.cas " : "") 
             + " \ngroup by te.fipsst, te.scc, " + (hasInvTableDataset ? "coalesce(i.name, te.data)" : "te.data") 
             + " \norder by te.fipsst, te.scc, " + (hasInvTableDataset ? "coalesce(i.name, te.data)" : "te.data") + "";
        
         outerQuery = query(outerQuery, true);
                
        //build inner sql statement with the datasets specified
        String innerSQL = "";
        for (int j = 0; j < allDatasetNames.size(); j++) {
            innerSQL += (j > 0 ? " \nunion all " : "") + createFireDatasetQuery(allDatasetNames.get(j).toString().trim());
        }
        
        return outerQuery.replaceAll(poundQueryTag, innerSQL);
    }

        private String createFireDatasetQuery(String datasetName) throws EmfException {

           String sql = "";
           
           sql = "\nselect substr(fips, 1, 2) as fipsst, scc, data, sum(datavalue) as datavalue \nfrom $DATASET_TABLE[\"" + 
               datasetName + "\", 1] m \ngroup by substr(fips, 1, 2), scc, data ";

           sql = query(sql, false);

           return sql;
        }

        private String query(String partialQuery, boolean createClause) throws EmfException {
            
            SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissioDatasourceName, tableName );
            return parser.parse(partialQuery, createClause);
        }
        
//        private EmfDataset getDataset(String dsName) throws EmfException {
//            //System.out.println("Database name = \n" + dsName + "\n");
//            DatasetDAO dao = new DatasetDAO();
//            try {
//                return dao.getDataset(sessionFactory.getSession(), dsName);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                throw new EmfException("The dataset name " + dsName + " is not valid");
//            }
//        }
}
