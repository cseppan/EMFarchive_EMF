package gov.epa.emissions.framework.services.qa.flatFile2010Pnt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.SQLQAProgramQuery;

public class SQLEnhanceFlatFile2010PointQuery extends SQLQAProgramQuery {
    
    public static final String FF10P_TAG = "-ff10p"; 
    
    public static final String SSFF_TAG = "-ssff"; 
    
    public static final String MANYNEIID_TAG = "-manyneiid"; 
    
    public static final String MANYFRS_TAG = "-manyfrs";     

    public SQLEnhanceFlatFile2010PointQuery(HibernateSessionFactory sessionFactory, String emissioDatasourceName,
            String tableName, QAStep qaStep) {
        super(sessionFactory, emissioDatasourceName, tableName, qaStep);
    }
    
    public String createProgramQuery() throws EmfException, SQLException {

        String programArguments = qaStep.getProgramArguments();
        
        String[] ff10pTokens = new String[] {};
        String[] ssffTokens = new String[] {};
        String whereFilter = new String();
        
        String[] arguments; 
        
        List<DatasetVersion> ff10pDatasetList = new ArrayList<DatasetVersion>();
        List<DatasetVersion> ssffDatasetList = new ArrayList<DatasetVersion>();
        
        int indexFF10P = programArguments.indexOf(QAStep.FF10P_TAG);
        int indexSSFF = programArguments.indexOf(QAStep.SSFF_TAG);
        int indexMultiNEI = programArguments.indexOf(QAStep.MANYNEIID_TAG);
        int indexMultiFRS = programArguments.indexOf(QAStep.MANYFRS_TAG);
        int indexWhereFilter = programArguments.indexOf(QAStep.WHERE_FILTER_TAG);
        
        if (indexFF10P != -1) {
            arguments = parseSwitchArguments(programArguments, indexFF10P, programArguments.indexOf("\n-", indexFF10P) != -1 ? programArguments.indexOf("\n-", indexFF10P) : programArguments.length());
            if (arguments != null && arguments.length > 0) ff10pTokens = arguments;
            for (String datasetVersion : ff10pTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = getDataset(qaStep.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                datasetNames.add(datasetVersionToken[0]);
                ff10pDatasetList.add(new DatasetVersion(datasetVersionToken[0], Integer.parseInt(datasetVersionToken[1])));
            }
        }

        if (indexSSFF != -1) {
            arguments = parseSwitchArguments(programArguments, indexSSFF, programArguments.indexOf("\n-", indexSSFF) != -1 ? programArguments.indexOf("\n-", indexSSFF) : programArguments.length());
            if (arguments != null && arguments.length > 0) ssffTokens = arguments;
            for (String datasetVersion : ssffTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = getDataset(qaStep.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                datasetNames.add(datasetVersionToken[0]);
                ssffDatasetList.add(new DatasetVersion(datasetVersionToken[0], Integer.parseInt(datasetVersionToken[1])));
            }
        }
        
        if ( ff10pDatasetList.size() != 1) {
            throw new EmfException("One and only one Flat File 2010 Point dataset should be chosen.");
        }
        
        String sqlStr = "";
        String fstcTable = "reference.facility_source_type_codes";
        String fipsTable = "reference.fips";
        String selectClause = "select ";
        String fromClause = "from ";
        String whereClause = "where ";
        String ff10pAlias = "ff10p";
        String ssffAlias = "ssff";
        String fstcAlias = "fstc";
        String fipsAlias = "fipst";
        String ff10pCols = "";
        String ff10pTable = "";    

        DatasetVersion dvFF10P = ff10pDatasetList.get(0);
        InternalSource[] dvFF10PiSources = dvFF10P.getDataset().getInternalSources();
        if ( dvFF10PiSources.length != 1) {
            throw new EmfException("This Flat File 2010 Point dataset contains more than one tables.");
        }
        DatasetVersion dvSSFF = ssffDatasetList.get(0);
        InternalSource[] dvSSFFiSources = dvSSFF.getDataset().getInternalSources();
        if ( dvSSFFiSources.length != 1) {
            throw new EmfException("This Smoke Supporting Flat File dataset contains more than one tables.");
        }
        
        Column[] cols = dvFF10P.getDataset().getDatasetType().getFileFormat().cols();
        for ( Column col : cols) {
            ff10pCols += ff10pAlias + "." + col.name() + " ";
        }
        selectClause += ff10pCols + " ";
        selectClause += ssffAlias + ".facility_company_name ";
        selectClause += fstcAlias + ".description as facility_type_description ";
        selectClause += ssffAlias + ".facilty_status_cd ";
        selectClause += ssffAlias + ".facilty_address ";
        selectClause += fipsAlias + ".state_name ";
        selectClause += ssffAlias + ".unit_desc ";
        selectClause += ssffAlias + ".unit_status_cd ";
        selectClause += ssffAlias + ".process_desc ";
        // other fields
        
        ff10pTable  = dvFF10PiSources[0].getTable();
        fromClause += ff10pTable + " as " + ff10pAlias + " ";
        fromClause += "left outer join ";
        fromClause += dvSSFFiSources[0].getTable() + " as " + ssffAlias + " ";
        fromClause += ssffAlias + ".region_cd" + " = " + ff10pAlias + ".region_cd"
                   +  " and " + ssffAlias + ".facility_id" + " = " + ff10pAlias + ".facility_id"
                   +  " and " + ssffAlias + ".unit_id" + " = " + ff10pAlias + ".unit_id"
                   +  " and " + ssffAlias + ".rel_point_id" + " = " + ff10pAlias + ".rel_point_id"
                   +  " and " + ssffAlias + ".process_id" + " = " + ff10pAlias + ".process_id"
                   +  " and " + ssffAlias + ".scc" + " = " + ff10pAlias + ".scc"
                   ;
                   //+ /*supporting ff10 version query*/
                   //and sff10.version IN (0) and sff10.dataset_id = 717
        // other join
        
        //where clause
        
        // construct the sql here
        sqlStr += selectClause + fromClause + whereClause;
        
        return sqlStr;        
    }
    
    private String[] parseSwitchArguments(String programSwitches, int beginIndex, int endIndex) {
        List<String> inventoryList = new ArrayList<String>();
        String value = "";
        String valuesString = "";
        
        valuesString = programSwitches.substring(beginIndex, endIndex);
        StringTokenizer tokenizer2 = new StringTokenizer(valuesString, "\n");
        tokenizer2.nextToken(); // skip the flag

        while (tokenizer2.hasMoreTokens()) {
            value = tokenizer2.nextToken().trim();
            if (!value.isEmpty())
                inventoryList.add(value);
        }
        return inventoryList.toArray(new String[0]);
    }

}
