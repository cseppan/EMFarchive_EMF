package gov.epa.emissions.commons.io;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * An acceptor which takes in data and puts it in a database
 * @author Prashant Pai, CEP UNC
 * @version $Id: DataAcceptor.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 * @see MySQLDataAcceptor.java
 */

public abstract class DataAcceptor
{

   /** The database connection. need to store the connection since we would
    * want to close it at the end */
   protected Connection connection = null;

   /** The statement to execute. */
   protected Statement statement = null;

   /** a prepared statement to use **/
   protected PreparedStatement prepStatement = null;

   /** The name of the database to connect to. */
   protected String dbStr = null;

   /** The name of the table that we will be updating. */
   protected String table = null;

   /** The prefix for the insert statements. */
   protected String insertPrefix = null;

   /** The prefix for the alter statements. */
   protected String alterAddPrefix = null;
   protected String alterDropPrefix = null;
   protected String alterModifyPrefix = null;

   /** The prefix for the update statements. */
   protected String updatePrefix = null;

   /** the column names **/
   protected String[] colNames;

   /** the column types **/
   protected String[] colTypes;

   /** if transactions need to be used **/
   protected boolean useTransactions = false;

   /** if prepared statement are going to be used **/
   protected boolean usePrepStatement = false;

   private int batchCount = 0;
   private static final int BATCH_LIMIT = 1000;
   private int batchLimit = BATCH_LIMIT;

   /**
    * instantiate a DataAcceptor object with a flag whether to use transactions
    * or not
    * @param useTransactions flag whether to use transactions or not
    * @throws Exception
    */
   public DataAcceptor(boolean useTransactions, boolean usePrepStatement) 
   		throws Exception
   {
      this.useTransactions = useTransactions;
      this.usePrepStatement = usePrepStatement;
      loadDriverClass();
      createStatement(null);
   } //DataAcceptor()

   /**
    * loads the appropriate driver class into the Java namespace
    * @throws Exception if the class could not be located
    */
   public abstract void loadDriverClass() throws Exception;

   /**
    * Open the database connection and get a Statement ready.
    * @throws Exception when either the database or table has not been set
    */
   public void startAcceptingData() throws Exception
   {
      if (dbStr == null)
         throw new Exception(
            "You must set the database connection before writing data.");

      if (table == null)
         throw new Exception(
            "You must set the table name before writing data.");

      if (usePrepStatement)
      {
      	connection.setAutoCommit(false);
      }

      if (useTransactions)
      {
      	statement.execute("SET AUTOCOMMIT=1");
      	statement.execute("START TRANSACTION");
      }
   } // startAcceptingData()

   /**
    * creates a new statement object. this is abstract since the connection
    * setup would be different for each database i.e. MySQL PostgreSQL
    * @param dbStr the name of the database IF NULL then generic connection to
    * database is established
    * @throws Exception error getting the connection or creating statement
    */
   public abstract void createStatement(String dbStr) throws Exception;

   /**
    * Set the name of the database to write data to.
    * @param databaseName the name of the database to write to.
    * @throws Exception
    */
   public void setDB(String databaseName) throws Exception
   {
      dbStr = databaseName;

      createStatement(dbStr);

      statement.execute("CREATE DATABASE IF NOT EXISTS " + dbStr);

      // this is probably MySQL specific
      statement.execute("USE " + dbStr);
   } // setDB(String)

   /**
    * Set the name of the database to be deleted
    * @param databaseName the name of the database to delete
    * @return boolean if the operation finished successfully or not
    * @throws Exception on deleting database
    */
   public boolean deleteDB(String databaseName) throws Exception
   {
      return statement.execute("DROP DATABASE " + databaseName);
   } // deleteDB(String)

   /**
    * Set the name of the table to write data to.
    * @param tableName the name of the table
    */
   public void setTable(String tableName)
   {
      table = tableName;
      insertPrefix = "INSERT INTO " + dbStr + "." + table + " VALUES(";
      alterAddPrefix  = "ALTER TABLE " + dbStr + "." + table + " ADD ";
      alterDropPrefix = "ALTER TABLE " + dbStr + "." + table + " DROP ";
      alterModifyPrefix = "ALTER TABLE " + dbStr + "." + table + " MODIFY ";
      updatePrefix = "UPDATE " + dbStr + "." + table + " SET ";
   } // setTable()

   /**
    * gets rid of unwanted characters
    * @param dirtyStr the string to be cleaned
    * @return String the cleaned up string
    */
   public String clean(String dirtyStr)
   {
      // currently only remove " and replace by blank
      String cleanStr = dirtyStr.replace('"', ' ');
     /*
    String cleanStr = dirtyStr.replace('-', ' ');
    */
/*
    String cleanStr = dirtyStr.replace(',', ' ');
    cleanStr = cleanStr.replace('#', ' ');
    cleanStr = cleanStr.replace('\\', '/');
    return cleanStr.replace(' ', '_');
      */
      return cleanStr;
   }

  /**
   * Create the table using the header
   * @param colNames the column names
   * @param colTypes the column types
   * @param primaryCol the primary Col
   * @param auto whether the primary Col should be auto increment
   * @throws Exception when there are different numbers of colNames and colTypes
  **/
  public void createTable(String[] colNames, String[] colTypes, String primaryCol,
    boolean auto) throws Exception
  {
    // check to see if there are the same number of  column names and column types
    if (colNames.length != colTypes.length)
      throw new Exception("There are different numbers of column names and types");

    this.colNames = colNames;
    this.colTypes = colTypes;

    String queryString = "CREATE TABLE " + dbStr + "." + table + " (";

    for (int i = 0; i < colNames.length; i++)
    {
       // one of the columnnames was "dec" for december.. caused a problem there
       if (colNames[i].equals("dec"))
          colNames[i] = colNames[i]+"1";

       queryString = queryString + clean(colNames[i]) + " " + colTypes[i] +
          (colNames[i].equals(primaryCol)?" PRIMARY KEY "+
          (auto?" AUTO_INCREMENT, ":", "):", ");
    }// for i
    queryString = queryString.substring(0, queryString.length() - 2) + ")";

    queryString = customizeCreateTableQuery(queryString);

//System.out.println("create table : " + queryString);
    // execute the query
    try
    {
       statement.execute(queryString);
    }
    catch (Exception e)
    {
       System.err.println("Error occurred while executing this query: "+queryString);
       throw e;
    }
  }//createTable()

  public abstract String customizeCreateTableQuery(String origQueryString);

    public abstract boolean tableExists(String dbName, String tableName) throws Exception;
    public abstract ResultSet showColumnsLike(String columnLike) throws Exception;

  public void deleteTable(String dbName, String tableName) throws Exception
  {
    statement.execute("DROP TABLE IF EXISTS " + dbName + "." + tableName);
  }//deleteTable

  /**
   * This method
   * @param colTypes
   * @throws Exception
   */
  public void prepareForInsert(int numberOfValues) throws Exception
  {
  	String questionMarks = "";
  	for (int i = 0; i < numberOfValues; i++)
  	{
  		questionMarks+=", ?";
  	}// for int i
  	questionMarks = questionMarks.substring(2);
  	String finalPrepQuery = insertPrefix+questionMarks+")";
  	prepStatement = connection.prepareStatement(finalPrepQuery);
    batchCount = 0;
  }//prepareForInsert(int)

  /**
   * @param data to be put into the table
   * @param colTypes the data types
   * @throws Exception if error entering data
   */
  public void insertPreparedRow(String[] data, String[] colTypes) throws Exception
  {

     // append data to the query.. put quotes around VARCHAR entries
     for (int i = 0; i < data.length; i++)
     {
        if (colTypes[i].startsWith("VARCHAR"))
        	try		
			{	
        		prepStatement.setString(i+1, clean(data[i]));
			}
        	catch(Exception e)
			{
                prepStatement.setNull(i+1, java.sql.Types.VARCHAR);
			}
        else if (colTypes[i].startsWith("DOUBLE"))
        {
           try
		   {
           	prepStatement.setDouble(i+1, Double.parseDouble(data[i]));
		   }
           catch(Exception e)
           {
               prepStatement.setNull(i+1, java.sql.Types.DOUBLE);
           }
        }
        else if (colTypes[i].startsWith("INT"))
        {
        	try
			{
        		prepStatement.setInt(i+1, Integer.parseInt(data[i]));
			}
            catch(Exception e)
            {
                prepStatement.setNull(i+1, java.sql.Types.INTEGER);
            }
        }
        else if (colTypes[i].startsWith("DATE"))
        {
        	try
			{
        		prepStatement.setDate(i+1, Date.valueOf(data[i]));
			}
            catch(Exception e)
            {
                prepStatement.setNull(i+1, java.sql.Types.DATE);
            }
        }
     }// for int i

     // execute the query
     try
     {
     	//prepStatement.executeUpdate();
        prepStatement.addBatch();
        batchCount++;
        if(batchCount >= batchLimit)
        {
            prepStatement.executeBatch();
            batchCount = 0;
        }
     }
     catch (Exception e)
     {
        throw e;
     }
  } // insertPreparedRow(String[], String[])

  public void insertRow(String[] data, String[] colTypes) throws Exception
  {
  	if (usePrepStatement)
  	{
  		if (prepStatement == null)
  		{
  			prepareForInsert(colTypes.length);
  		}
  		insertPreparedRow(data, colTypes);
  	}
  	else
  	{
  		insertStandardRow(data, colTypes);
  	}
  }//insertRow(String[], String[])
  
  
   /**
    * @param data to be put into the table
    * @param colTypes the data types
    * @throws Exception if error entering data
    */
   public void insertStandardRow(String[] data, String[] colTypes) throws Exception
   {
/*
for (int k = 0; k < data.length; k++)
  System.out.print(data[k] + " ");
System.out.println();
*/
      // instantiate a new string buffer in which the query would be created
      StringBuffer sb = new StringBuffer(insertPrefix);

      // append data to the query.. put quotes around VARCHAR entries
      for (int i = 0; i < data.length; i++)
      {
         if (colTypes[i].startsWith("VARCHAR"))
            try
         {
            sb.append("\"" + clean(data[i]) + "\"");
         }
         catch(Exception e)
         {
            System.out.println("");
         }


         else
         {
            if (data[i].trim().length() == 0)
               data[i] = "NULL";
            sb.append(data[i]);
         }
         sb.append(',');
      }// for int i

      // there will an extra comma at the end so delete that
      sb.deleteCharAt(sb.length()-1);

      // close parentheses around the query
      sb.append(')');

//System.out.println(sb.toString());
      // execute the query
      try
      {
         statement.execute(sb.toString());
      }
      catch (Exception e)
      {
         System.err.println("Error occurred while executing this query: "+sb.toString());
         throw e;
      }
   } // insertStandardRow(String[], String[])

   /**
    * Given a table name, determine what table name should be used next.
    * @param tableName - the table name to check
    * @return the next table name to use
    */
    //public abstract String getNextTableName(String tableName) throws Exception;

    /**
     * Alter the table by adding a new column of the specified type in the
     * specified location.
     * 
     * ALTER TABLE databaseName.tableName
     * ADD columnName columnType
     * [AFTER afterColumnName]
     * 
     * @param columnName - the name of the new column to add
     * @param columnType - the type of the new column
     * @param afterColumnName - the column name to add the new column after.
     *                          Use null for default function (add to end)
     * @throws Exception if encounter error altering table
     */
    public void addColumn(String columnName, String columnType, String afterColumnName) throws Exception
    {
        //instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(alterAddPrefix);
        final String AFTER = " AFTER ";

        sb.append(columnName + " " + columnType);
        if(afterColumnName != null)
        {
            sb.append(AFTER + afterColumnName);
        }//if

        //execute the statement
        try
        {
            statement.execute(sb.toString());
        }
        catch(Exception e)
        {
            System.err.println("Error occurred while executing this statement: " + sb.toString());
            throw e;
        }
    }//addColumn(String, String, String)

    /**
     * Alter the table by dropping the specified column name.
     * 
     * ALTER TABLE databaseName.tableName
     * DROP columnName
     * 
     * @param columnName - the name of the column to drop
     * @throws Exception
     */
    public void dropColumn(String columnName) throws Exception
    {
        //instantiate a new string in which the query would be created
        String dropStatement = alterDropPrefix + columnName;

        //execute the statement
        try
        {
            statement.execute(dropStatement);
        }
        catch(Exception e)
        {
            System.err.println("Error occurred while executing this statement: " + dropStatement);
            throw e;
        }
    }//dropColumn(String)

    /**
     * ALTER TABLE databaseName.tableName
     * MODIFY columnName columnType
     * AFTER afterColumnName
     * 
     * @param columnName
     * @param columnType
     * @param afterColumnName
     * @throws Exception
     */
    public void modifyColumn(String columnName, String columnType, String afterColumnName) throws Exception
    {
        final String AFTER = " AFTER ";
        String modifyStatement = alterModifyPrefix + columnName + " " + columnType + AFTER + afterColumnName;

        //execute the statement
        try
        {
            statement.execute(modifyStatement);
        }
        catch(Exception e)
        {
            System.err.println("Error occurred while executing this statement: " + modifyStatement);
            throw e;
        }
    }//modifyColumn(String, String, String)

    /**
     * Alter the table by adding an index composed of <i>i</i> column names.
     * 
     * ALTER TABLE
     * ADD INDEX indexName (indexColumnNames[i])
     * 
     * @param indexName
     * @param indexColumnNames
     * @throws Exception
     */
    public void addIndex(String indexName, String[] indexColumnNames) throws Exception
    {
        //instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(alterAddPrefix);
        final String INDEX = "INDEX ";

        sb.append(INDEX + indexName + "(" + indexColumnNames[0]);
        for(int i = 1; i < indexColumnNames.length; i++)
        {
            sb.append(", " + indexColumnNames[i]);
        }
        sb.append(")");
        
        //execute the statement
        try
        {
            statement.execute(sb.toString());
        }
        catch(Exception e)
        {
            System.err.println("Error occurred while executing this statement: " + sb.toString());
            throw e;
        }
    }//addIndex(String, String[])

    /**
     * Update the specified column in the database table using the
     * desired expression.
     * 
     * UPDATE databaseName.tableName
     * SET columnName = setExpr
     * 
     * @param columnName - the column to update
     * @param setExpr    - the expression used to update the column value
     * @throws Exception if encounter error updating table
     */
    /*
    public void update(String columnName, String setExpr) throws Exception
    {
        String updateStatement = updatePrefix + columnName + " = " + setExpr;

        //execute the statement
        try
        {
            statement.execute(updateStatement);
        }
        catch(Exception e)
        {
            System.err.println("Error occurred while executing this statement: " + updateStatement);
            throw e;
        }
    }//update(String, String)
    */

    /**
     * Update the specified column in the database table using a subquery,
     * selecting the update source column with the needed join conditions.
     * 
     * UPDATE databaseName.tableName
     * SET columnName = (SELECT selectColumnName
     *                   FROM joinDbName.joinTableName
     *                   WHERE joinWhereColumns[i] = joinEqualsClauses[i])
     * 
     * @param columnName        - the column to update (update destination column)
     * @param selectColumnName  - the column to select in the subquery (update source column)
     * @param joinDbName        - the database containing the table to join
     * @param joinTableName     - the table to join in the subquery
     * @param joinWhereColumns  - left hand sides of = expressions for subquery WHERE
     * @param joinEqualsClauses - right hand sides of = expressions for subquery WHERE
     * @throws Exception if encounter error updating table
     */
    /*
    public void updateSubQuery(String columnName, String selectColumnName,
            String joinDbName, String joinTableName, String[] joinWhereColumns,
            String[] joinEqualsClauses) throws Exception
    {
        if(joinWhereColumns.length != joinEqualsClauses.length)
        {
            throw new Exception("There are different numbers of WHERE column names and LIKE clauses");
        }

        //instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(updatePrefix + columnName
                + " = (SELECT " + selectColumnName
                + " FROM " + joinDbName + "." + joinTableName
                + " WHERE ");

        //add the first join = expression
        sb.append(joinWhereColumns[0] + " = " + joinEqualsClauses[0]);

        //if there is more than one join = expression, add
        //"AND" before each of the remaining expressions
        for(int i = 1; i < joinWhereColumns.length; i++)
        {
            sb.append(" AND " + joinWhereColumns[i] + " = " + joinEqualsClauses[i]);
        }
        sb.append(")");

        //execute the statement
        try
        {
            //System.out.println(sb.toString());
            statement.execute(sb.toString());
        }
        catch(Exception e)
        {
            System.err.println("Error occurred while executing this statement: " + sb.toString());
            throw e;
        }
    }//updateSubQuery(String, String, String, String, String[], String[])
    */

    /**
     * UPDATE databaseName.tableName
     * SET columnName = setExpr
     * WHERE whereColumns[i] LIKE 'likeClauses[i]'
     * 
     * @param columnName   - the column to update
     * @param setExpr      - the expression used to update the column value
     * @param whereColumns - left hand sides of LIKE expressions for WHERE
     * @param likeClauses  - right hand sides of LIKE expressions for WHERE
     * @throws Exception if encounter error updating table
     */
    public void updateWhereLike(String columnName, String setExpr, String[] whereColumns, String[] likeClauses) throws Exception
    {
        if(whereColumns.length != likeClauses.length)
        {
            throw new Exception("There are different numbers of WHERE column names and LIKE clauses");
        }

        //instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(updatePrefix + columnName + " = " + setExpr + " WHERE ");

        //add the first LIKE expression
        sb.append(whereColumns[0] + " LIKE '" + likeClauses[0] + "'");

        //if there is more than one LIKE expression, add
        //"AND" before each of the remaining expressions
        for(int i = 1; i < whereColumns.length; i++)
        {
            sb.append(" AND " + whereColumns[i] + " LIKE '" + likeClauses[i] + "'");
        }

        //execute the statement
        try
        {
            statement.execute(sb.toString());
        }
        catch(Exception e)
        {
            System.err.println("Error occurred while executing this statement: " + sb.toString());
            throw e;
        }
    }//updateWhereLike(String, String, String[], String[])

    /**
     * UPDATE databaseName.tableName
     * SET columnName = setExpr
     * WHERE whereColumns[i] = equalsClauses[i]
     * 
     * @param columnName    - the column to update
     * @param setExpr       - the expression used to update the column value
     * @param whereColumns  - left hand sides of = expressions for WHERE
     * @param equalsClauses - right hand sides of = expressions for WHERE
     * @throws Exception if encounter error updating table
     */
    public void updateWhereEquals(String columnName, String setExpr, String[] whereColumns, String[] equalsClauses) throws Exception
    {
        if(whereColumns.length != equalsClauses.length)
        {
            throw new Exception("There are different numbers of WHERE column names and = clauses");
        }

        //instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer(updatePrefix + columnName + " = " + setExpr + " WHERE ");

        //add the first LIKE expression
        sb.append(whereColumns[0] + " = " + equalsClauses[0]);

        //if there is more than one LIKE expression, add
        //"AND" before each of the remaining expressions
        for(int i = 1; i < whereColumns.length; i++)
        {
            sb.append(" AND " + whereColumns[i] + " = " + equalsClauses[i]);
        }

        //execute the statement
        try
        {
            statement.execute(sb.toString());
        }
        catch(Exception e)
        {
            System.err.println("Error occurred while executing this statement: " + sb.toString());
            throw e;
        }
    }//updateWhereEquals(String, String, String[], String[])

    /**
     * Generate a concat expression for usage in SQL statements. If the value
     * to be concatenated is a literal (constant), it should be enclosed in ''.
     * @param exprs - Array of data ('literals' and column names)
     * @return the SQL concat expression
     */
    public String generateConcatExpr(String[] exprs)
    {
        final String CONCAT = "concat(";
        //begin the concat expression
        StringBuffer concat = new StringBuffer(CONCAT);
        //add the first string
        concat.append(exprs[0]);
        //if there is more than one string to concat, add a
        //comma separator before each of the remaining strings
        for(int i = 1; i < exprs.length; i++)
        {
            concat.append("," + exprs[i]);
        }
        //close off the concat expression
        concat.append(")");

        return concat.toString();
    }//generateConcatExpr(String[])

    /**
     * SELECT columnNames[i]
     * FROM dbName.tableName
     * 
     * @param columnNames - the column names to select
     * @param dbName      - the database containing the table
     * @param tableName   - the table containing the columns
     * @throws Exception if encounter error selecting table
     */
    public ResultSet select(String[] columnNames, String dbName, String tableName) throws Exception
    {
        final String selectPrefix = "SELECT ";
        StringBuffer sb = new StringBuffer(selectPrefix);
        sb.append(columnNames[0]);
        for(int i = 1; i < columnNames.length; i++)
        {
            sb.append("," + columnNames[i]);
        }
        final String fromSuffix = " FROM " + dbName + "." + tableName;
        sb.append(fromSuffix);

        //execute the statement
        ResultSet results = null;
        try
        {
            statement.execute(sb.toString());
            results = statement.getResultSet();
        }
        catch(Exception e)
        {
            System.err.println("Error occurred while executing this statement: " + sb.toString());
            throw e;
        }

        return results;
    }//select(String[], String, String)

    /**
     * SELECT *
     * FROM dbName.tableName
     * 
     * @param dbName      - the database containing the table
     * @param tableName   - the table containing the data
     * @throws Exception if encounter error selecting table
     */
    public ResultSet selectAll(String dbName, String tableName) throws Exception
    {
        return select(new String[]{"*"}, dbName, tableName);
    }//selectAll(String, String)

   /**
    * Close the database connection.
    * @throws Exception on closing connection to database.
    */
    public void finishAcceptingData() throws Exception
   {
      if (usePrepStatement && prepStatement != null)
      {
      	prepStatement.executeBatch();
      	connection.commit();
      	connection.setAutoCommit(true);
      }
      if (useTransactions)
      {
         statement.execute("COMMIT");
      }
      statement.close();
      connection.close();
      statement = null;
      connection = null;
      prepStatement = null;
      batchCount = 0;
    } // finishAcceptingData()
}
