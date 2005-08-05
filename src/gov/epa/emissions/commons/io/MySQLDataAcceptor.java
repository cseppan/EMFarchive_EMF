package gov.epa.emissions.commons.io;

import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 * An acceptor which takes in data and puts it in a database
 * @author Prashant Pai, MCNC
 * @version $Id: MySQLDataAcceptor.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
**/

public class MySQLDataAcceptor extends DataAcceptor
{
    private Database database;

    /**
    * instantiate a DataAcceptor object with a flag whether to use transactions
    * or not
    * @param useTransactions flag whether to use transactions or not
    * @throws Exception
    */
   public MySQLDataAcceptor(boolean useTransactions, boolean usePrepStatement) throws Exception
   {
      super(useTransactions, usePrepStatement);
   } // MySQLDataAcceptor(boolean, boolean)

   /**
    * loads the appropriate driver class into the Java namespace
    * @throws Exception if the class could not be located
    */
   public void loadDriverClass() throws Exception
   {
      final String driver = Database.get_Driver("mysql");
      // Load the DB driver.
      try
      {
         Class.forName(driver);
//       Class.forName("com.mysql.jdbc.Driver");
//       Class.forName("org.gjt.mm.mysql.Driver");
      }
      catch(ClassNotFoundException e)
      {
         throw new Exception("The MySQL JDBC driver does not exist on the path");
      }
   }

   /**
    * creates a new statement object.
    * @param dbStr the name of the database IF NULL then generic connection to
    * database is established
    * @throws Exception error getting the connection or creating statement
    */
   public void createStatement(String dbStr) throws Exception
   {
   		final String server		= database.getServer();
   		final String host		= database.getHost();
   		final String port		= database.getPort();
   		final String user		= database.getUser();
   		final String password	= database.getPassword();

      // Get a connection.
      if (connection == null || statement == null || connection.isClosed())
      {
			connection = DriverManager.getConnection
			(
				"jdbc:" +
				server +
				"://" +
				host +
				((port != null) ? (":" + port) : "") +
				"/" +
				((dbStr != null) ? dbStr : ""),
				user,
				password
			);

//       connection = DriverManager.getConnection("jdbc:mysql://localhost/"
//             + ((dbStr != null)?dbStr:""), "root", "");

         statement  = connection.createStatement();
      }
   }

  public String customizeCreateTableQuery(String origQueryString)
  {
     String queryString = origQueryString;
     if (useTransactions)
     {
        queryString+=" type=InnoDB";
     }
     return queryString;
  }

    public boolean tableExists(String dbName, String tableName) throws Exception
    {
        //if SHOW TABLES query returns one or more rows, the table exists
        statement.execute("SHOW TABLES FROM " + dbName + " LIKE '" + tableName + "'");
        return statement.getResultSet().getRow() > 0;
    }

    public ResultSet showColumnsLike(String columnLike) throws Exception
    {
        statement.execute("SHOW COLUMNS FROM " + dbStr + "." + table + " LIKE '" + columnLike + "%'");
        return statement.getResultSet();
    }
}
