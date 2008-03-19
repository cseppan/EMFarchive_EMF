package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class SQLMultiInvDiffProgramQuery {
    
    private QAStep qaStep;

    private String tableName;

    private HibernateSessionFactory sessionFactory;
    
    //private static final String poundQueryTag = "#";
    
    private String emissioDatasourceName;
    
    private static final String invBaseTag = "-inv_base";
    
    private static final String invCompareTag = "-inv_compare";
    
    private static final String invTableTag = "-invtable";

    private static final String summaryTypeTag = "-summaryType";

    ArrayList<String> baseDatasetNames = new ArrayList<String>();
    
    ArrayList<String> compareDatasetNames = new ArrayList<String>();
    
    private boolean hasInvTableDataset;
    
    public SQLMultiInvDiffProgramQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName, String tableName, QAStep qaStep) {
        this.qaStep = qaStep;
        this.tableName = tableName;
        this.sessionFactory = sessionFactory;
        this.emissioDatasourceName = emissioDatasourceName;
    }
        
    public String createInvDiffProgramQuery() throws EmfException {
        
        String programArguments = qaStep.getProgramArguments();
        
        //get applicable tables from the program arguments
        String invBaseToken = "";
        String invCompareToken ="";
        String invtableToken = "";
        String summaryTypeToken = "State+SCC";
        String invTableDatasetName = "";
        
        int indexBase = programArguments.indexOf(invBaseTag);
        int indexCompare = programArguments.indexOf(invCompareTag);
        int indexInvTable = programArguments.indexOf(invTableTag);
        int indexSumType = programArguments.indexOf(summaryTypeTag);
        
        if (indexBase !=-1  && indexCompare !=-1 && indexInvTable != -1){
            invBaseToken = programArguments.substring(0, indexCompare).trim();
            invCompareToken = programArguments.substring(indexCompare, indexInvTable).trim();
            invtableToken = programArguments.substring(indexInvTable + invTableTag.length(), indexSumType == -1 ? programArguments.length() : indexSumType);
        }
        if (indexSumType != -1) {
            summaryTypeToken = programArguments.substring(indexSumType + summaryTypeTag.length()).trim();
        } 
        //default just in case...
        if (summaryTypeToken.trim().length() == 0)
            summaryTypeToken = "State+SCC";

         //parse inventories names for base and compare...
        if (invBaseToken.length() > 0 ) {
            StringTokenizer tokenizer2 = new StringTokenizer(invBaseToken);
            tokenizer2.nextToken();
            while (tokenizer2.hasMoreTokens()) {
                String datasetName = tokenizer2.nextToken().trim();
                if (datasetName.length() > 0)
                    baseDatasetNames.add(datasetName);
            }
            
        } else {
            //see if there are tables to build the query with, if not throw an exception
            throw new EmfException("There are no ORL Inventory datasets specified (base).");
        }
        
        // get compare datasets
       if ( invCompareToken.length() > 0 ) {
            StringTokenizer tokenizer2 = new StringTokenizer(invCompareToken);
            tokenizer2.nextToken();
            while (tokenizer2.hasMoreTokens()) {
                String datasetName = tokenizer2.nextToken().trim();
                if (datasetName.length() > 0)
                    compareDatasetNames.add(datasetName);
            }
        } else {
            //see if there are tables to build the query with, if not throw an exception
            throw new EmfException("There are no ORL Inventory datasets specified (compare).");
        }

         //parse inventory table name...
         StringTokenizer tokenizer3 = new StringTokenizer(invtableToken);
         while (tokenizer3.hasMoreTokens()) {
             invTableDatasetName  = tokenizer3.nextToken().trim();
             if (invTableDatasetName.length() > 0) hasInvTableDataset = true;
         }
         String diffQuery = "select @@!, t.poll, t.base_ann_emis, t.compare_ann_emis, (t.compare_ann_emis-t.base_ann_emis) as diff_ann_emis, " 
             + " \nabs(t.compare_ann_emis-t.base_ann_emis) as abs_diff_ann, "
             + " \ncase when t.base_ann_emis >0  then (abs(t.compare_ann_emis-t.base_ann_emis)/t.base_ann_emis) "
             + " \nelse null "
             + " \nend as percent_diff_ann, "
             + " \nt.base_avd_emis, t.compare_avd_emis, (t.compare_avd_emis-t.base_avd_emis) as diff_avd_emis, " 
             + " \nabs(t.compare_avd_emis-t.base_avd_emis) as abs_diff_avd, "
             + " \ncase when t.base_avd_emis >0  then (abs(t.compare_avd_emis-t.base_avd_emis)/t.base_avd_emis) "
             + " \nelse null "
             + " \nend as percent_diff_avd "
             
             + "\n from (select @!@, " 
             + " \ncoalesce(b.poll, c.poll) as poll,"
             + " \ncoalesce(" + (hasInvTableDataset ? "i.name" : "p.pollutant_code_desc") + ", 'AN UNSPECIFIED DESCRIPTION') as poll_desc, "
             + " \nsum(coalesce(" + (hasInvTableDataset ? "cast(i.factor as double precision) * b.ann_emis" : "null") + ", b.ann_emis)) as base_ann_emis," 
             + " \nsum(coalesce(" + (hasInvTableDataset ? "cast(i.factor as double precision) * c.ann_emis" : "null") + ", c.ann_emis)) as compare_ann_emis,"

             + " \nsum(coalesce("+ (hasInvTableDataset ? "cast(i.factor as double precision) * b.avd_emis" : "null") + ", b.avd_emis)) as base_avd_emis, " 
             + " \nsum(coalesce("+ (hasInvTableDataset ? "cast(i.factor as double precision) * c.avd_emis" : "null") + ", c.avd_emis)) as compare_avd_emis "

             + " \nfrom (#base) as b "
             + " \nfull outer join (#compare) as c "
             + " \non !!! and b.poll = c.poll"
             + (hasInvTableDataset ? "\nleft outer join\n $DATASET_TABLE[\"" + invTableDatasetName + "\", 1] i \non coalesce(b.poll, c.poll) = i.cas " : "\nleft outer join reference.pollutant_codes p \non coalesce(b.poll, c.poll) = p.pollutant_code ") 
             + " \ngroup by @@@, " + "coalesce(b.poll, c.poll)," + "coalesce(" + (hasInvTableDataset ? "i.name" : "p.pollutant_code_desc") + ", 'AN UNSPECIFIED DESCRIPTION')"
             + " \norder by @@@, " + "coalesce(b.poll, c.poll)," + "coalesce(" + (hasInvTableDataset ? "i.name" : "p.pollutant_code_desc") + ", 'AN UNSPECIFIED DESCRIPTION')) as t";

         diffQuery = query(diffQuery, true);

        //build inner sql statement with the datasets specified, make sure and unionize (append) the tables together
         String innerSQLBase = "";
         for (int j = 0; j < baseDatasetNames.size(); j++) {
             innerSQLBase += (j > 0 ? " \nunion all " : "") + createDatasetQuery(baseDatasetNames.get(j).toString().trim());
         }

         //replace #base symbol with the unionized fire datasets query
         diffQuery = diffQuery.replaceAll("#base", innerSQLBase);

         String innerSQLCompare = "";
       for (int j = 0; j < compareDatasetNames.size(); j++) {
           //System.out.println("compare dataset : " +j +"  " + compareDatasetNames.get(j));
           innerSQLCompare += (j > 0 ? " \nunion all " : "") + createDatasetQuery(compareDatasetNames.get(j).toString().trim());
       }

       //replace #compare symbol with the unionized fire datasets query
       diffQuery = diffQuery.replaceAll("#compare", innerSQLCompare);
       
     //replace !!! symbol with conditions when join table base with compare
       String sql = "";
       if (summaryTypeToken.equals("State+SCC")) 
           sql = "b.fipsst=c.fipsst and b.scc=c.scc";
       else if (summaryTypeToken.equals("State")) 
           sql = "b.fipsst=c.fipsst";
       else if (summaryTypeToken.equals("County")) 
           sql = "b.fips=c.fips";
       else if (summaryTypeToken.equals("County+SCC")) 
           sql = "b.fips=c.fips and b.scc=c.scc";
       diffQuery = diffQuery.replaceAll("!!!", sql);
       
     //replace @@! symbol with main columns in outer select statement
       if (summaryTypeToken.equals("State+SCC")) 
           sql = "t.fipsst, t.scc";
       else if (summaryTypeToken.equals("State")) 
           sql = "t.fipsst";
       else if (summaryTypeToken.equals("County")) 
           sql = "t.fips";
       else if (summaryTypeToken.equals("County+SCC")) 
           sql = "t.fips, t.scc";
       diffQuery = diffQuery.replaceAll("@@!", sql);

       
         //replace @!@ symbol with main columns in outer select statement
         if (summaryTypeToken.equals("State+SCC")) 
             sql = "coalesce(b.fipsst, c.fipsst) as fipsst, coalesce(b.scc, c.scc) as scc";
         else if (summaryTypeToken.equals("State")) 
             sql = "coalesce(b.fipsst, c.fipsst) as fipsst";
         else if (summaryTypeToken.equals("County")) 
             sql = "coalesce(b.fips, c.fips) as fips";
         else if (summaryTypeToken.equals("County+SCC")) 
             sql = "coalesce(b.fips, c.fips) as fips, coalesce(b.scc, c.scc) as scc";
         diffQuery = diffQuery.replaceAll("@!@", sql);

         //replace !@! symbol with main columns in inner select statement
         if (summaryTypeToken.equals("State+SCC")) 
             sql = "substr(fips, 1, 2) as fipsst, scc";
         else if (summaryTypeToken.equals("State")) 
             sql = "substr(fips, 1, 2) as fipsst";
         else if (summaryTypeToken.equals("County")) 
             sql = "fips";
         else if (summaryTypeToken.equals("County+SCC")) 
             sql = "fips, scc";
         diffQuery = diffQuery.replaceAll("!@!", sql);
         
         //replace @@@ symbol with group by columns in outer select statement
         if (summaryTypeToken.equals("State+SCC")) 
             sql = "coalesce(b.fipsst, c.fipsst), coalesce(b.scc, c.scc)";
         else if (summaryTypeToken.equals("State")) 
             sql = "coalesce(b.fipsst, c.fipsst)";
         else if (summaryTypeToken.equals("County")) 
             sql = "coalesce(b.fips, c.fips)";
         else if (summaryTypeToken.equals("County+SCC")) 
             sql = "coalesce(b.fips, c.fips), coalesce(b.scc, c.scc)";
         diffQuery = diffQuery.replaceAll("@@@", sql);
         
         //replace !!@ symbol with group by columns in inner select statement
         if (summaryTypeToken.equals("State+SCC")) 
             sql = "substr(fips, 1, 2), scc";
         else if (summaryTypeToken.equals("State")) 
             sql = "substr(fips, 1, 2)";
         else if (summaryTypeToken.equals("County")) 
             sql = "fips";
         else if (summaryTypeToken.equals("County+SCC")) 
             sql = "fips, scc";
         diffQuery = diffQuery.replaceAll("!!@", sql);

        //return the built query
        return diffQuery;
    }

    
    private String createDatasetQuery(String datasetName) throws EmfException {

        String sql = "";
        sql = "\nselect !@!, poll, sum(ann_emis) as ann_emis, sum(avd_emis) as avd_emis  \nfrom $DATASET_TABLE[\"" + 
        datasetName + "\", 1] m  \ngroup by !!@, poll ";

        sql = query(sql, false);

        return sql;
    }

    private String query(String partialQuery, boolean createClause) throws EmfException {

        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissioDatasourceName, tableName );
        return parser.parse(partialQuery, createClause);
    }

}
