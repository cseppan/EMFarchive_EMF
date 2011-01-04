package gov.epa.emissions.framework.services.qa.comparedatasets;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableMetaData;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetVersion;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.SQLQAProgramQuery;
import gov.epa.emissions.framework.services.qa.SQLQueryParser;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.hibernate.Session;

public class SQLCompareDatasetsProgramQuery extends SQLQAProgramQuery{
    
    public static final String BASE_TAG = "-base"; 
    //Sample:
/*
dataset name1|1
dataset name2|5
dataset name3|3
*/
    
    public static final String COMPARE_TAG = "-compare";
    //Sample:
/*
dataset name1|1
dataset name2|5
dataset name3|3
*/
    
    public static final String GROUP_BY_EXPRESSIONS_TAG = "-groupby";
    //Sample:
/*
scc
fips
plantid
pointid
stackid
segment
poll
*/
    
    public static final String AGGREGATE_EXPRESSIONS_TAG = "-aggregate";
    //Sample:
/*
ann_emis
avd_emis
*/
    
    public static final String MATCHING_EXPRESSIONS_TAG = "-matching";
/*
scc|scc
fips|fips
plantid|plantid
pointid|pointid
stackid|stackid
segment|segment
poll|poll
*/

//    private static final String FULL_JOIN_EXPRESSIONS_TAG = "-fulljoin";
/*
scc|scc
fips|fips
plantid|plantid
pointid|pointid
stackid|stackid
segment|segment
poll|poll
*/

    ArrayList<String> baseDatasetNames = new ArrayList<String>();
    
    ArrayList<String> compareDatasetNames = new ArrayList<String>();
    
    private boolean hasInvTableDataset;
    
    private Datasource datasource;
    
    public SQLCompareDatasetsProgramQuery(HibernateSessionFactory sessionFactory, Datasource datasource, String emissioDatasourceName, String tableName, QAStep qaStep) {
        super(sessionFactory,emissioDatasourceName,tableName,qaStep);
        this.datasource = datasource;    
    }

//    public class DatasetVersion {
//        private EmfDataset dataset;
//        private Version version;
//        private String datasetName;
//        private int datasetVersion;
//        
//        public DatasetVersion(String datasetName, int datasetVersion) {
//            this.setDatasetName(datasetName);
//            this.setDatasetVersion(datasetVersion);
////            this.setDataset(dataset);
////            this.setVersion(version);
//        }
//
//        public void setDataset(EmfDataset dataset) {
//            this.dataset = dataset;
//        }
//
//        public EmfDataset getDataset() {
//            return dataset;
//        }
//
//        public void setDatasetName(String datasetName) {
//            this.datasetName = datasetName;
//        }
//
//        public String getDatasetName() {
//            return datasetName;
//        }
//
//        public void setVersion(Version version) {
//            this.version = version;
//        }
//
//        public Version getVersion() {
//            return version;
//        }
//
//        public void setDatasetVersion(int datasetVersion) {
//            this.datasetVersion = datasetVersion;
//        }
//
//        public int getDatasetVersion() {
//            return datasetVersion;
//        }
//
//    }

    public class ColumnMatchingMap {
        private String dataset1Expression;
        private String dataset2Expression;
        
        public ColumnMatchingMap(String dataset1Expression, String dataset2Expression) {
            this.setDataset1Expression(dataset1Expression);
            this.setDataset2Expression(dataset2Expression);
        }

        public void setDataset1Expression(String dataset1Expression) {
            this.dataset1Expression = dataset1Expression;
        }

        public String getDataset1Expression() {
            return dataset1Expression;
        }

        public void setDataset2Expression(String dataset2Expression) {
            this.dataset2Expression = dataset2Expression;
        }

        public String getDataset2Expression() {
            return dataset2Expression;
        }
        
    }

    public class Expression {
        
        private String expression;
        private String alias;

        public Expression(String expression, String alias) {
            this.setExpression(expression);
            this.setAlias(alias);
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public String getExpression() {
            return expression;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getAlias() {
            return alias;
        }

    }

    public String createProgramQuery() throws EmfException, SQLException {
        
        String programArguments = qaStep.getProgramArguments();
        
        //get applicable tables from the program arguments
        String[] baseTokens = new String[] {};
        String[] compareTokens = new String[] {};
        String[] groupByExpressions = new String[] {};
        String[] aggregateExpressions = new String[] {};
        String[] matchingExpressionTokens = new String[] {};
//        String[] fullJoinExpressionTokens = new String[] {};
        Map<String, Column> baseColumns;
        Map<String, Column> compareColumns;
        
        List<DatasetVersion> baseDatasetList = new ArrayList<DatasetVersion>();
        List<DatasetVersion> compareDatasetList = new ArrayList<DatasetVersion>();
        Map<String, ColumnMatchingMap> matchingExpressionMap = new HashMap<String, ColumnMatchingMap>();
        Map<String, String> expressionAliasMap = new HashMap<String, String>();
        
        String[] arguments;
        
        
        
        //Load up arguments into local variables...
        
        
        int indexBase = programArguments.indexOf(BASE_TAG);
        int indexCompare = programArguments.indexOf(COMPARE_TAG);
        int indexGroupBy = programArguments.indexOf(GROUP_BY_EXPRESSIONS_TAG);
        int indexAggregate = programArguments.indexOf(AGGREGATE_EXPRESSIONS_TAG);
        int indexMatching = programArguments.indexOf(MATCHING_EXPRESSIONS_TAG);
        
        if (indexBase != -1) {
            arguments = parseSwitchArguments(programArguments, indexBase, programArguments.indexOf("\n-", indexBase) != -1 ? programArguments.indexOf("\n-", indexBase) : programArguments.length());
            if (arguments != null && arguments.length > 0) baseTokens = arguments;
            for (String datasetVersion : baseTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = getDataset(qaStep.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                datasetNames.add(datasetVersionToken[0]);
                baseDatasetList.add(new DatasetVersion(datasetVersionToken[0], Integer.parseInt(datasetVersionToken[1])));
            }
        }
        if (indexCompare != -1) {
            arguments = parseSwitchArguments(programArguments, indexCompare, programArguments.indexOf("\n-", indexCompare) != -1 ? programArguments.indexOf("\n-", indexCompare) : programArguments.length());
            if (arguments != null && arguments.length > 0) compareTokens = arguments;
            for (String datasetVersion : compareTokens) {
                String[] datasetVersionToken = new String[] {};
                if (!datasetVersion.equals("$DATASET")) { 
                    datasetVersionToken = datasetVersion.split("\\|");
                } else {
                    EmfDataset qaStepDataset = getDataset(qaStep.getDatasetId());
                    datasetVersionToken = new String[] { qaStepDataset.getName(), qaStepDataset.getDefaultVersion() + "" };
                }
                datasetNames.add(datasetVersionToken[0]);
                compareDatasetList.add(new DatasetVersion(datasetVersionToken[0], Integer.parseInt(datasetVersionToken[1])));
            }
        }
        
        checkDataset();
        
        if (indexGroupBy != -1) {
            arguments = parseSwitchArguments(programArguments, indexGroupBy, programArguments.indexOf("\n-", indexGroupBy) != -1 ? programArguments.indexOf("\n-", indexGroupBy) : programArguments.length());
            if (arguments != null && arguments.length > 0) groupByExpressions = arguments;
        }
        if (indexAggregate != -1) {
            arguments = parseSwitchArguments(programArguments, indexAggregate, programArguments.indexOf("\n-", indexAggregate) != -1 ? programArguments.indexOf("\n-", indexAggregate) : programArguments.length());
            if (arguments != null && arguments.length > 0) aggregateExpressions = arguments;
        }
        if (indexMatching != -1) {
            arguments = parseSwitchArguments(programArguments, indexMatching, programArguments.indexOf("\n-", indexMatching) != -1 ? programArguments.indexOf("\n-", indexMatching) : programArguments.length());
            if (arguments != null && arguments.length > 0) matchingExpressionTokens = arguments;
            for (String matchingExpressionToken : matchingExpressionTokens) {
                String[] matchingExpression = matchingExpressionToken.split("\\=");
                String dataset1Expression = matchingExpression[0];
                String dataset2Expression = matchingExpression[1];
                matchingExpressionMap.put(dataset1Expression, new ColumnMatchingMap(dataset1Expression, dataset2Expression));
                matchingExpressionMap.put(dataset2Expression, new ColumnMatchingMap(dataset2Expression, dataset1Expression));
            }
        }

     
        //Validate program arguments (i.e., does dataset and version exist, does mapping make sense, etc...)
       
        
        //see if there is issues with the base datasets 
        if (baseDatasetList.size() > 0 ) {
            for (DatasetVersion datasetVersion : baseDatasetList) {
                EmfDataset dataset = getDataset(datasetVersion.getDatasetName());
                //make sure dataset exists
                if (dataset == null)
                    throw new EmfException("Dataset, " + datasetVersion.getDatasetName() + ", doesn't exist.");
                datasetVersion.setDataset(dataset);
                Version version = version(dataset.getId(), datasetVersion.getDatasetVersion());
                //make sure version exists
                if (version == null)
                    throw new EmfException("Version, " + datasetVersion.getDatasetName() + " - " + datasetVersion.getDatasetVersion() + ", doesn't exists.");
                datasetVersion.setVersion(version);
            }
            //do one last pass now that the dataset and version objects have been populated and 
            //make sure all of these datasets are of the same dataset type!
            DatasetType prevDatasetType = null;
            for (DatasetVersion datasetVersion : baseDatasetList) {
                EmfDataset dataset = datasetVersion.getDataset();
                if (prevDatasetType != null && !prevDatasetType.equals(dataset.getDatasetType()))
                    throw new EmfException("The base datasets must be of the same dataset type.");
                prevDatasetType = dataset.getDatasetType();
            }
            
        } else {
            throw new EmfException("There are no base datasets specified.");
        }
        
        //see if there are issues with the compare datasets 
        if (compareDatasetList.size() > 0 ) {
            for (DatasetVersion datasetVersion : compareDatasetList) {
                EmfDataset dataset = getDataset(datasetVersion.getDatasetName());
                //make sure dataset exists
                if (dataset == null)
                    throw new EmfException("Dataset, " + datasetVersion.getDatasetName() + ", doesn't exist.");
                datasetVersion.setDataset(dataset);
                Version version = version(dataset.getId(), datasetVersion.getDatasetVersion());
                //make sure version exists
                if (version == null)
                    throw new EmfException("Version, " + datasetVersion.getDatasetName() + " - " + datasetVersion.getDatasetVersion() + ", doesn't exists.");
                datasetVersion.setVersion(version);
            }
            //do one last pass now that the dataset and version objects have been populated and 
            //make sure all of these datasets are of the same dataset type!
            DatasetType prevDatasetType = null;
            for (DatasetVersion datasetVersion : compareDatasetList) {
                EmfDataset dataset = datasetVersion.getDataset();
                if (prevDatasetType != null && !prevDatasetType.equals(dataset.getDatasetType()))
                    throw new EmfException("The compare datasets must be of the same dataset type.");
                prevDatasetType = dataset.getDatasetType();
            }
            
        } else {
            throw new EmfException("There are no compare datasets specified.");
        }

        //get columns that represent both the compare and base datasets
        baseColumns = getDatasetColumnMap(baseDatasetList.get(0).getDataset());
        compareColumns = getDatasetColumnMap(compareDatasetList.get(0).getDataset());

        //see if there are issues with the matching expressions
        if (matchingExpressionMap.size() > 0 ) {
            //make sure these expressions exists
            for (String matchingExpression : matchingExpressionTokens) {
                String[] matchingExpressionToken = matchingExpression.split("\\=");
                //make sure expression exists
                if (!expressionExists(matchingExpressionToken[0], baseColumns))
                    throw new EmfException("Matching expression 1, " + matchingExpressionToken[0] + ", doesn't exist as a column in the dataset.");
                //make sure expression exists
                if (!expressionExists(matchingExpressionToken[1], compareColumns))
                    throw new EmfException("Matching expression 2, " + matchingExpressionToken[1] + ", doesn't exist as a column in the dataset.");

            }
        } 
//there might not be any matching criteria specified, could be comparing like datasets        
//        else {
//            throw new EmfException("There are no matching expressions specified.");
//        }

        //see if there are issues with the group by expressions
        if (groupByExpressions.length > 0 ) {
            //make sure these expressions exists
            for (String groupByExpression : groupByExpressions) {

                //parse group by token and put in a map for later use...
                String[] groupByExpressionParts = groupByExpression.toLowerCase().split(" as ");
//                StringTokenizer tokenizer = new StringTokenizer(groupByExpression.toLowerCase(), "\\ as ");
                int count = groupByExpressionParts.length;
                String expression = "";
                String alias = "";
                //has no alias
                if (count == 1) {
                    expression = groupByExpressionParts[0];//tokenizer.nextToken();
                    alias = expression;
                //has alias
                } else if (count == 2) {
                    expression = groupByExpressionParts[0];//tokenizer.nextToken();
                    alias = groupByExpressionParts[1];//tokenizer.nextToken();
                //unkown number of tokens, throw an error
                } else if (count > 2) {
                    throw new EmfException("Invalid formatted GROUP BY expression, " + groupByExpression + ". Should be formatted as: expression AS alias (i.e., subtring(fips,1,2) as fipsst).");
                }
                if (expressionAliasMap.containsKey(alias))
                    throw new EmfException("GROUP BY expression, " + groupByExpression + ", has already been specified.  Only specify the expression once.");
                //add to map, will be used to help build sql statement
                expressionAliasMap.put(alias, expression);
            
            
                //ignoring mappings for now, just see if expression is appropriate for dataset(s)
                boolean baseExpressionExists = expressionExists(expression, baseColumns);
                boolean compareExpressionExists = expressionExists(expression, compareColumns);

                //make sure group by expression exists
                if (!baseExpressionExists && !compareExpressionExists)
                    throw new EmfException("GROUP BY expression, " + expression + ", doesn't exist as a column in either the base or compare datasets.");

                //if either one of the dataset types doesn't contain the column, then make sure we have a mapping for it...
                baseExpressionExists = expressionExists(expression, baseColumns, matchingExpressionMap);
                compareExpressionExists = expressionExists(expression, compareColumns, matchingExpressionMap);
                if (!baseExpressionExists && !compareExpressionExists) {
                    if (matchingExpressionMap.get(expression) == null)
                        throw new EmfException("GROUP BY expression, " + expression + ", needs a mapping entry specified, the column doesn't exist in either the base or compare datasets.");
                }
            }
        } else {
            throw new EmfException("There are no GROUP BY expressions specified.");
        }

        //see if there are issues with the aggregate expressions
        if (aggregateExpressions.length > 0 ) {
            //make sure these expressions returns a number
            for (String aggregateExpression : aggregateExpressions) {
                Column baseColumn = baseColumns.get(aggregateExpression);
                Column compareColumn = compareColumns.get(aggregateExpression);
                //make sure aggregate expression exists
                if (baseColumn == null && compareColumn == null)
                    throw new EmfException("Aggregate expression, " + aggregateExpression + ", doesn't exist as a column in either the base or compare datasets.");
                //if either one of the dataset types doesn't contain the column, then make sure we have a mapping for it...
                if (baseColumn == null || compareColumn == null) {
                    if (matchingExpressionMap.get(aggregateExpression) == null)
                        throw new EmfException("Aggregate expression, " + aggregateExpression + ", needs a mapping entry specified, the column doesn't exist in either the base or compare datasets.");
                }
                
                Column column = (baseColumn != null ? baseColumn : (compareColumn != null ? compareColumn : null));
                //make sure aggregate expression represents a number data type
                if (!(column.getSqlType() == "INTEGER"
                    || column.getSqlType() == "float(15)"
                    || column.getSqlType() == "INT"
                    || column.getSqlType() == "BIGINT"
                    || column.getSqlType() == "double precision"
                    || column.getSqlType() == "INT2"
                ))
                    throw new EmfException("Aggregate expression, " + aggregateExpression + ", must be a numeric data type (i.e., tinyint, smallint, int, bigint, double precision, float, numeric, decimal).");
                
            }
        } else {
            throw new EmfException("There are no AGGREGATE expressions specified.");
        }

        
        //Build SQL statement
        
        String selectSQL = "select ";
        String baseSelectSQL = "select ";
        String compareSelectSQL = "select ";
        String baseUnionSelectSQL = "select ";
        String compareUnionSelectSQL = "select ";
        String groupBySQL = "group by ";
        String baseGroupBySQL = "group by ";
        String compareGroupBySQL = "group by ";
        String baseUnionGroupBySQL = "group by ";
        String compareUnionGroupBySQL = "group by ";
        String fullJoinClauseSQL = "on ";

        //build core group by expressions into SELECT statement 
        int counter = 0;
        
        Set<String> expressionAliasKeySet = expressionAliasMap.keySet();
        Iterator<String> expressionAliasKeySetIterator = expressionAliasKeySet.iterator();
        Iterator<String> expressionAliasValuesIterator = expressionAliasMap.values().iterator();
        while (expressionAliasKeySetIterator.hasNext()) {
            String groupByExpression = expressionAliasValuesIterator.next();
            String groupByExpressionAlias = expressionAliasKeySetIterator.next();
            
            
            
//        for (int counter = 0; counter < expressionAliasMap.size(); ++counter) {
        //String groupByExpression : groupByExpressions) {
            String baseExpression = getBaseExpression(groupByExpression, matchingExpressionMap, baseColumns, "b");
            Column baseColumn = getBaseColumn(groupByExpression, matchingExpressionMap, baseColumns);
            String compareExpression = getCompareExpression(groupByExpression, matchingExpressionMap, compareColumns, "c");
            Column compareColumn = getCompareColumn(groupByExpression, matchingExpressionMap, compareColumns);
            
//            selectSQL += (!selectSQL.equals("select ") ? ", " : "") + "coalesce(b." + baseColumn.getName() + ",c." + compareColumn.getName() + ") as " + groupByExpression + "";
//            baseSelectSQL += (!baseSelectSQL.equals("select ") ? ", " : "") + "b." + baseColumn.getName();
//            compareSelectSQL += (!compareSelectSQL.equals("select ") ? ", " : "") + "c." + compareColumn.getName();

            selectSQL += (!selectSQL.equals("select ") ? ", " : "") + "coalesce(b.expr" + counter + ",c.expr" + counter + ") as \"" + groupByExpressionAlias + "\"";
            baseSelectSQL += (!baseSelectSQL.equals("select ") ? ", " : "") + baseExpression + " as expr" + counter;
            compareSelectSQL += (!compareSelectSQL.equals("select ") ? ", " : "") + compareExpression + " as expr" + counter;
            baseUnionSelectSQL += (!baseUnionSelectSQL.equals("select ") ? ", " : "") + " expr" + counter;
            compareUnionSelectSQL += (!compareUnionSelectSQL.equals("select ") ? ", " : "") + " expr" + counter;
            
            groupBySQL += (!groupBySQL.equals("group by ") ? ", " : "") + "coalesce(b.expr" + counter + ",c.expr" + counter + ")";
            baseGroupBySQL += (!baseGroupBySQL.equals("group by ") ? ", " : "") + baseExpression;
            compareGroupBySQL += (!compareGroupBySQL.equals("group by ") ? ", " : "") + compareExpression;
            baseUnionGroupBySQL += (!baseUnionGroupBySQL.equals("group by ") ? ", " : "") + " expr" + counter;
            compareUnionGroupBySQL += (!compareUnionGroupBySQL.equals("group by ") ? ", " : "") + " expr" + counter;

            fullJoinClauseSQL += (!fullJoinClauseSQL.equals("on ") ? " and " : "") + "c.expr" + counter + " = b.expr" + counter + " ";
            ++counter;
        }
        
        //build aggregrate expressions into SELECT statement 
//        selectSQL += ",count(1) as cnt";
//        baseSelectSQL += ",count(1) as cnt";
//        compareSelectSQL += ",count(1) as cnt";
        for (String aggregateExpression : aggregateExpressions) {
            Column baseColumn = getBaseColumn(aggregateExpression, matchingExpressionMap, baseColumns);
            Column compareColumn = getCompareColumn(aggregateExpression, matchingExpressionMap, compareColumns);
            selectSQL += ",sum(b." + baseColumn.getName() + ") as " + aggregateExpression + "_b, sum(c." + compareColumn.getName() + ") as " + aggregateExpression + "_c, sum(c." + compareColumn.getName() + ") - sum(b." + baseColumn.getName() + ") as " + aggregateExpression + "_diff, abs(sum(c." + compareColumn.getName() + ") - sum(b." + baseColumn.getName() + ")) as " + aggregateExpression + "_absdiff, case when coalesce(sum(b." + baseColumn.getName() + "),0.0) <> 0.0 then (sum(c." + compareColumn.getName() + ") - sum(b." + baseColumn.getName() + ")) / sum(b." + baseColumn.getName() + ") * 100.0 else null::double precision end as " + aggregateExpression + "_pctdiff, case when coalesce(sum(b." + baseColumn.getName() + "),0.0) <> 0.0 then abs((sum(c." + compareColumn.getName() + ") - sum(b." + baseColumn.getName() + ")) / sum(b." + baseColumn.getName() + ") * 100.0) else null::double precision end as " + aggregateExpression + "_abspctdiff";
            baseSelectSQL += ",sum(b." + baseColumn.getName() + ") as " + baseColumn.getName() + "";
            compareSelectSQL += ",sum(c." + compareColumn.getName() + ") as " + compareColumn.getName() + "";
            baseUnionSelectSQL += ",sum(b." + baseColumn.getName() + ") as " + baseColumn.getName() + "";
            compareUnionSelectSQL += ",sum(c." + compareColumn.getName() + ") as " + compareColumn.getName() + "";
        }

        //build inner sql statement with the datasets specified, make sure and unionize (append) the tables together
         String innerSQLBase = "";
         if (baseDatasetList.size() > 1) 
             innerSQLBase = baseUnionSelectSQL + ", sum(b.cnt) as cnt from (";
         for (int j = 0; j < baseDatasetList.size(); j++) {
             DatasetVersion datasetVersion = baseDatasetList.get(j);
             EmfDataset dataset = datasetVersion.getDataset();
             VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion.getVersion(), "b");

             innerSQLBase += (j > 0 ? " \nunion all " : "") + baseSelectSQL + ", count(1) as cnt from emissions." + dataset.getInternalSources()[0].getTable() + " as b where " + datasetVersionedQuery.query() + " " + baseGroupBySQL;
         }
         if (baseDatasetList.size() > 1) 
             innerSQLBase += ") b " + baseUnionGroupBySQL;

//         //replace #base symbol with the unionized fire datasets query
//         diffQuery = diffQuery.replaceAll("#base", innerSQLBase);

         String innerSQLCompare = "";
         if (compareDatasetList.size() > 1) 
             innerSQLCompare = compareUnionSelectSQL + ", sum(c.cnt) as cnt from (";
         for (int j = 0; j < compareDatasetList.size(); j++) {
             DatasetVersion datasetVersion = compareDatasetList.get(j);
             EmfDataset dataset = datasetVersion.getDataset();
             VersionedQuery datasetVersionedQuery = new VersionedQuery(datasetVersion.getVersion(), "c");
             innerSQLCompare += (j > 0 ? " \nunion all " : "") + compareSelectSQL + ", count(1) as cnt from emissions." + dataset.getInternalSources()[0].getTable() + " as c where " + datasetVersionedQuery.query() + " " + compareGroupBySQL;
         }
         if (compareDatasetList.size() > 1) 
             innerSQLCompare += ") c " + compareUnionGroupBySQL;

         String sql = selectSQL + ", sum(b.cnt) as count_b, sum(c.cnt) as count_c from (" + innerSQLBase + ") as b full outer join (" + innerSQLCompare + ") as c ";
//         for (int j = 0; j < fullJoinExpressionList.size(); j++) {
//             
//             ColumnMatchingMap columnMatchingMap = fullJoinExpressionList.get(j);
//             sql += (j == 0 ? " on " : " and ") + "c." + columnMatchingMap.getDataset2Expression() + " = b." + columnMatchingMap.getDataset1Expression();
//         }
//         for (int j = 0; j < groupByExpressions.length; j++) {
//             //String groupByExpression : groupByExpressions
//             String groupByExpression = groupByExpressions[j];
//             Column baseColumn = getBaseColumn(groupByExpression, matchingExpressionMap, baseColumns);
//             Column compareColumn = getCompareColumn(groupByExpression, matchingExpressionMap, compareColumns);
//             sql += (j == 0 ? " on " : " and ") + "c." + compareColumn.getName() + " = b." + baseColumn.getName();
//         }

         
         sql += fullJoinClauseSQL + " " + groupBySQL + " " + groupBySQL.replace("group by", "order by");

        System.out.println(sql);

        
        SQLQueryParser parser = new SQLQueryParser(sessionFactory, emissionDatasourceName, tableName );
//return the built query
        return parser.createTableQuery() + " " + sql;
    }

    private Column getBaseColumn(String columnName, Map<String,ColumnMatchingMap> columnMatchingMap, Map<String,Column> baseColumns) {
        Column baseColumn = baseColumns.get(columnName);
        if (baseColumn != null) {
            return baseColumn;
        }
        return baseColumns.get(columnMatchingMap.get(columnName).getDataset1Expression());
    }
    
    private boolean expressionExists(String expression, Map<String,Column> columns) {
        Set<String> columnsKeySet = columns.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            if (expression.toLowerCase().contains(columnName.toLowerCase())) 
                return true;
        }
        return false;
    }
    
    private boolean expressionExists(String expression, Map<String,Column> columns, Map<String,ColumnMatchingMap> columnMatchingMap) {
        if (expressionExists(expression, columns)) 
            return true;

        //didn't find the column in the known list of columns, lets see if there is a mapping for this.
        Set<String> columnMatchingMapKeySet = columnMatchingMap.keySet();
        Iterator<String> iterator = columnMatchingMapKeySet.iterator();
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            if (expression.toLowerCase().contains(columnName.toLowerCase())) 
                return true;
        }
        
        return false;
    }
    
    private String getBaseExpression(String expression, Map<String,ColumnMatchingMap> columnMatchingMap, Map<String,Column> baseColumns, String tableAlias) throws EmfException {
        Set<String> columnsKeySet = baseColumns.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            if (expression.toLowerCase().contains(columnName.toLowerCase())) 
                return expression.replace(columnName, tableAlias + "." + columnName);
        }
        
        //didn't find the column in the known list of columns, lets see if there is a mapping for this.
        Set<String> columnMatchingMapKeySet = columnMatchingMap.keySet();
        iterator = columnMatchingMapKeySet.iterator();
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            if (expression.toLowerCase().contains(columnName.toLowerCase())) 
                return columnMatchingMap.get(columnName).getDataset1Expression().replace(columnName, tableAlias + "." + columnName);
        }
//
//        return columnMatchingMap.get(expression).getDataset1Expression();
        throw new EmfException("Unknown base dataset expression.");
    }
    
    private String getCompareExpression(String expression, Map<String,ColumnMatchingMap> columnMatchingMap, Map<String,Column> compareColumns, String tableAlias) throws EmfException {
        Set<String> columnsKeySet = compareColumns.keySet();
        Iterator<String> iterator = columnsKeySet.iterator();
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            if (expression.toLowerCase().contains(columnName.toLowerCase())) 
                return expression.replace(columnName, tableAlias + "." + columnName);
        }
        
        //didn't find the column in the known list of columns, lets see if there is a mapping for this.
        Set<String> columnMatchingMapKeySet = columnMatchingMap.keySet();
        iterator = columnMatchingMapKeySet.iterator();
        while (iterator.hasNext()) {
            String columnName = iterator.next();
            if (expression.toLowerCase().contains(columnName.toLowerCase())) 
                return columnMatchingMap.get(columnName).getDataset2Expression().replace(columnName, tableAlias + "." + columnName);
        }

        throw new EmfException("Unknown compare dataset expression.");
    }
    

    private Column getCompareColumn(String columnName, Map<String,ColumnMatchingMap> columnMatchingMap, Map<String,Column> compareColumns) {
        Column compareColumn = compareColumns.get(columnName);
        if (compareColumn != null) {
            return compareColumn;
        }
        return compareColumns.get(columnMatchingMap.get(columnName).getDataset2Expression());
    }
    
    private Map<String,Column> getDatasetColumnMap(EmfDataset dataset) throws SQLException {
        Column[] columns = new TableMetaData(datasource).getColumns(dataset.getInternalSources()[0].getTable());
        Map<String,Column> map = new HashMap<String,Column>();
        for (Column column : columns) {
            map.put(column.getName(), column);
        }
        return map;
    }

    private Version version(int datasetId, int version) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, session);
        } finally {
            session.close();
        }
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
