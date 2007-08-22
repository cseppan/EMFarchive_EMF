package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.StringTokenizer;


public class SQLQAAnnualNonsummaryQuery {
    
    // Input is currently a set of 12 or 24 or 12*n monthly files
    // The lists are filled using command-line and/or GUI input.
    
    private QAStep qaStep;

    private String tableName;

    private HibernateSessionFactory sessionFactory;
    
    private String emissioDatasourceName;

    public SQLQAAnnualNonsummaryQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName, String tableName, QAStep qaStep) {
        
        this.qaStep = qaStep;
        this.tableName = tableName;
        this.sessionFactory = sessionFactory;
        this.emissioDatasourceName = emissioDatasourceName;
        
    }
    
    ArrayList<String> janDatasetNames = new ArrayList<String>();
    
    ArrayList<String> febDatasetNames = new ArrayList<String>();
    ArrayList<String> marDatasetNames = new ArrayList<String>();
    ArrayList<String> aprDatasetNames = new ArrayList<String>();
    ArrayList<String> mayDatasetNames = new ArrayList<String>();
    ArrayList<String> junDatasetNames = new ArrayList<String>();
    ArrayList<String> julDatasetNames = new ArrayList<String>();
    ArrayList<String> augDatasetNames = new ArrayList<String>();
    ArrayList<String> sepDatasetNames = new ArrayList<String>();
    ArrayList<String> octDatasetNames = new ArrayList<String>();
    ArrayList<String> novDatasetNames = new ArrayList<String>();
    ArrayList<String> decDatasetNames = new ArrayList<String>();
    
    String janQuery = "";
    String febQuery = "";
    String marQuery = "";
    String aprQuery = "";
    String mayQuery = "";
    String junQuery = "";
    String julQuery = "";
    String augQuery = "";
    String sepQuery = "";
    String octQuery = "";
    String novQuery = "";
    String decQuery = "";
    
    ArrayList<String> allDatasetNames = new ArrayList<String>();
    
    public String createAnnualQuery() throws EmfException {
        
        String programArguments = qaStep.getProgramArguments();
        
        StringTokenizer tokenizer2 = new StringTokenizer(programArguments);
        tokenizer2.nextToken();
        while (tokenizer2.hasMoreTokens()) {
             allDatasetNames.add(tokenizer2.nextToken());
            }
        
       String annualQueryPrefix = "select te.fipsst, te.poll, te.scc, sum(mo_emis) as ann_emis from \n";
       String annualQuerySuffix = " as te group by te.fipsst, te.poll, te.scc order by te.fipsst, te.poll, te.scc";

        for (int j = 0; j < allDatasetNames.size(); j++) {
            //Check for month name and year name here
            
            //String year = "";
            String month = "";
            EmfDataset dataset;
            try {
            dataset = getDataset(allDatasetNames.get(j).toString().trim());
            } catch(EmfException ex){
                throw new EmfException("There is at least one invalid dataset name");
            }
            
            // New String Tokenizers for the StartDate and StopDate values.
            // They are compared to determine if they fall in the same month.
            
            StringTokenizer tokenizer5 = new StringTokenizer(dataset.getStartDateTime().toString());
            
            String yearMonthDay = tokenizer5.nextToken();
            StringTokenizer tokenizer8 = new StringTokenizer(yearMonthDay, "-");
            
            String startYear = tokenizer8.nextToken();
            String startMonth = tokenizer8.nextToken();
            
            StringTokenizer tokenizer6 = new StringTokenizer(dataset.getStopDateTime().toString());
            
            String yearMonthDay2 = tokenizer6.nextToken();
            StringTokenizer tokenizer9 = new StringTokenizer(yearMonthDay2, "-");
            
            String stopYear = tokenizer9.nextToken();
            String stopMonth = tokenizer9.nextToken();
            
            // New String Tokenizer to parse the dataset names to find month values.
            StringTokenizer tokenizer7 = new StringTokenizer(dataset.toString(), "_");
            String month2 = "";
            while (tokenizer7.hasMoreTokens()) {
                String unsure = tokenizer7.nextToken();
                if(unsure.equalsIgnoreCase("jan")||
                   unsure.equalsIgnoreCase("feb")||
                   unsure.equalsIgnoreCase("mar")||
                   unsure.equalsIgnoreCase("apr")||
                   unsure.equalsIgnoreCase("may")||
                   unsure.equalsIgnoreCase("jun")||
                   unsure.equalsIgnoreCase("jul")||
                   unsure.equalsIgnoreCase("aug")||
                   unsure.equalsIgnoreCase("sep")||
                   unsure.equalsIgnoreCase("oct")||
                   unsure.equalsIgnoreCase("nov")||
                   unsure.equalsIgnoreCase("dec")) {
                month2 = unsure;
                }
            }
            
            if(startMonth.equals(stopMonth) && startYear.equals(stopYear)) {
                month = startMonth;
                //System.out.println("The month of the dataset from startMonth is: " + month);
            } else if (!(month2.equals(""))){
                month = month2;
                //System.out.println("The month of the dataset from month2 is: " + month);
            }else {
                throw new EmfException("The dataset covers more than one month.");
            }
            // Then the file or files must be put into the appropriate method call to create a monthly 
            // query for them.
            
            //System.out.println("The dataset is :" + allDatasetNames.get(j).toString());
            
            //Add exceptions for case where month value not found
            if (month.equalsIgnoreCase("jan") || month.equalsIgnoreCase("january") || month.equals("01"))
                janDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("feb") || month.equalsIgnoreCase("february") || month.equals("02"))
                febDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("mar") || month.equalsIgnoreCase("march") || month.equals("03"))
                marDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("apr") || month.equalsIgnoreCase("april") || month.equals("04"))
                aprDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("may") || month.equals("05"))
                mayDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("jun") || month.equalsIgnoreCase("june") || month.equals("06"))
                junDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("jul") || month.equalsIgnoreCase("july") || month.equals("07"))
                julDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("aug") || month.equalsIgnoreCase("august") || month.equals("08"))
                augDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("sep") || month.equalsIgnoreCase("september") || month.equals("09"))
                sepDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("oct") || month.equalsIgnoreCase("october") || month.equals("10"))
                octDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("nov") || month.equalsIgnoreCase("november") || month.equals("11"))
                novDatasetNames.add(allDatasetNames.get(j).toString());
            else if (month.equalsIgnoreCase("dec") || month.equalsIgnoreCase("december") || month.equals("12"))
                decDatasetNames.add(allDatasetNames.get(j).toString());
        }
        
        // Then the full query must be created by piecing togther all of the monthly
        // queries.  Somehow every clause needs to include a union all except the last one.
        
        // Here there needs to be a series of "if"/"else if" clauses which collectively run
        // each of the appropriate monthly queries by determining if there are any datasets 
        // for that month.  The results can be stored in a String array.  If no datasets exist 
        // for a given month, that string must be empty.
        
        
        if (janDatasetNames.size() > 0) {
            janQuery = createMonthlyQuery(31, janDatasetNames);
        }
        if (febDatasetNames.size() > 0) {
            febQuery = createMonthlyQuery(28, febDatasetNames);
        }
        if (marDatasetNames.size() > 0) {
            marQuery = createMonthlyQuery(31, marDatasetNames);
        }
        if (aprDatasetNames.size() > 0) {
            aprQuery = createMonthlyQuery(30, aprDatasetNames);
        }
        if (mayDatasetNames.size() > 0) {
            mayQuery = createMonthlyQuery(31, mayDatasetNames);
        }
        if (junDatasetNames.size() > 0) {
            junQuery = createMonthlyQuery(30, junDatasetNames);
        }
        if (julDatasetNames.size() > 0) {
            julQuery = createMonthlyQuery(31, julDatasetNames);
        }
        if (augDatasetNames.size() > 0) {
            augQuery = createMonthlyQuery(31, augDatasetNames);
        }
        if (sepDatasetNames.size() > 0) {
            sepQuery = createMonthlyQuery(30, sepDatasetNames);
        }
        if (octDatasetNames.size() > 0) {
            octQuery = createMonthlyQuery(31, octDatasetNames);
        }
        if (novDatasetNames.size() > 0) {
            novQuery = createMonthlyQuery(30, novDatasetNames);
        }
        if (decDatasetNames.size() > 0) {
            decQuery = createMonthlyQuery(31, decDatasetNames);
        }
        
        String fullQuery1 = annualQueryPrefix + "(" + janQuery + febQuery + marQuery + aprQuery + mayQuery + 
        junQuery + julQuery + augQuery + sepQuery + octQuery + novQuery + decQuery + ")" + annualQuerySuffix;
        
        //Use pattern matching to remove the last and unrequired union all from the query
        String fullQuery = fullQuery1.replaceAll("poll, scc  union all...as te", "poll, scc ) as te");
        
        //System.out.println("The final query is : " + fullQuery);

        return query(fullQuery, true);
        
    }

        private String createMonthlyQuery(int daysInMonth, ArrayList datasetNames) throws EmfException
{
           String monthlyQueryPrefix = "select substr(fips, 1, 2) as fipsst, poll, scc, sum( avd_emis * ";

           String monthlyQueryMiddle = ") as mo_emis from\n $DATASET_TABLE[";
           
           String monthlyQuerySuffix = ", 1] m group by substr(fips, 1, 2), poll, scc ";

           String fullMonthlyQuery = "";
           
           String currentMonthlyQuery = "";
           
           //String [] singleMonthlyQuery = new String [5];

           for (int i = 0; i < datasetNames.size(); i++)
           {
               //System.out.println("Dataset names: " + datasetNames );
               currentMonthlyQuery = monthlyQueryPrefix + daysInMonth + monthlyQueryMiddle + 
                  datasetNames.get(i).toString() + monthlyQuerySuffix;

               fullMonthlyQuery += query(currentMonthlyQuery, false);  // expand the intermediate query to 
                 // use the version info for the monthly inventory datasets 
                 // note that the big query below does not have version info for the 
                 // monthly datasets, but the real version will need to
               
               //singleMonthlyQuery [i] = query(currentMonthlyQuery, false);
               
               if (i != datasetNames.size() -1 && i >=0)
                   fullMonthlyQuery+= " union all ";
                   //singleMonthlyQuery[i] += " union all ";
                   //currentMonthlyQuery += " union all\n ";
           }
           
           if (datasetNames.size() > 0)
           {
               fullMonthlyQuery += " union all ";
               
           }
           
           //System.out.println("Fullmonth: " + fullMonthlyQuery + "\n");
           return fullMonthlyQuery;
        }
        // This method uses a newly created constructor of SQLQueryParser.
        
        //private String query(DbServer dbServer, QAStep qaStep, String tableName, String partialQuery, EmfDataset dataset, Version version) throws EmfException {
        private String query(String partialQuery, boolean createClause) throws EmfException {
            //System.out.println(dbServer + "\n" + qaStep + "\n" + tableName + "\n" 
                    //+ partialQuery + "\n" + dataset + "\n" + version);
            SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissioDatasourceName, tableName );
            return parser.parse(partialQuery, createClause);
        }
        
        private EmfDataset getDataset(String dsName) throws EmfException {
            //System.out.println("Database name = \n" + dsName + "\n");
            DatasetDAO dao = new DatasetDAO();
            try {
                return dao.getDataset(sessionFactory.getSession(), dsName);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new EmfException("The dataset name must be valid");
            }
        }
}
