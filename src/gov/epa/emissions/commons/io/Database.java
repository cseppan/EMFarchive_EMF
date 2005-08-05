package gov.epa.emissions.commons.io;

import java.io.ObjectStreamException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * <p>Database object</p>
 *
 * @author      Craig Mattocks
 * @version $Id: Database.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 */

public class Database
  implements Cloneable, java.io.Serializable
{

  /**
   * Global class instance variables
   */

  /** Set a serialVersionUID for interoperability. */
  static final long serialVersionUID = -3652580628581165662L;

  // Types of databases
  public static final int REFERENCE	= 0;
  public static final int ANALYSIS	= 1;
  public static final int EMISSIONS	= 2;

  // Defaults
  private static final int	DEFAULT_TYPE		= EMISSIONS;
  private static final String	DEFAULT_SERVER		= "mysql";
  private static final String	DEFAULT_HOST		= "localhost";
  private static final String	DEFAULT_PORT		= "3306";
  private static final String	DEFAULT_NAME		= "";
  private static final String	DEFAULT_USER		= "root";
  private static final String	DEFAULT_PASSWORD	= "root";

  private static HashMap drivers;
  static
  {
      drivers = new HashMap();
      drivers.put("mysql","com.mysql.jdbc.Driver");
      drivers.put("postgresql","org.postgresql.Driver");
      drivers.put("odbc","sun.jdbc.odbc.jdbcOdbcDriver");
      drivers.put("hsqldb","org.hsqldb.jdbcDriver");
  }
  
  /**
   * Instance variables
   */
  private Long id;				// unique, system-defined ID, used as primary key
  private int type;
  private String server;
  private String host;
  private String port;
  private String name;
  private String driver;
  private String user;
  private String password;

  private Connection connection;
  private Statement statement;
  private boolean recheck;
  private java.util.List tableNames  = null;
  private java.util.List columnNames = null;

  // Database connection status
  private volatile boolean connectedToDatabase = false;

  /**
   * Full constructor
   */
  public Database(final int type, final String server, final String host, final String port, final String name, final String user, final String password)
  {
    // Use setters to allow easy implementation of concurrency (in future)
    setType(type);
    setServer(server);
    setHost(host);
    setPort(port);
    setName(name);
    setUser(user);
    setPassword(password);
  }

  public Database(final int type, final String server, final String host, final String port, final String name)
  {
    // Use setters to allow easy implementation of concurrency (in future)
    this(type, server, host, port, name, DEFAULT_USER, DEFAULT_PASSWORD);
  }

  public Database(final String name)
  {
    this(DEFAULT_TYPE, DEFAULT_SERVER, DEFAULT_HOST, DEFAULT_PORT, name);
  }

  public Database()
  {
    this(DEFAULT_NAME);
  }

  /**
   * Read and print the database's meta data
   */
  public void getMetaData(final String user, final String password)
  {
    try
    {
      // Load the DB driver and establish a connection
      loadDriver(getDriver());
      connect(getName(), user, password);

      // Get the meta data
      if (Constants.DEBUG) System.out.println("Getting DB meta data...\n");
      DatabaseMetaData metaData = connection.getMetaData();

      // Scan the database's tables
      String[] validTypes = {"TABLE"};
      ResultSet theTables = metaData.getTables(null, null, null, validTypes);
      while (theTables.next())
      {
        String tableName = theTables.getString("TABLE_NAME");
        if (Constants.DEBUG) System.out.println("Table found: " + tableName);

        // Now get the columns in that table
        ResultSet theColumns = metaData.getColumns(null, null, tableName, null);
        while (theColumns.next())
        {
          String columnName = theColumns.getString("COLUMN_NAME");
          if (Constants.DEBUG) System.out.println("\tColumn found: " + columnName);
        }
        System.out.println();
      }
    }
    catch (Exception e)
    {
      System.out.println("Problems communicating with the database!");
    }
  } // end getMetaData method

  /**
   * Read and print the database's meta data
   */
  public void getDBMetaData()
  {
    try
    {
      // Load the DB driver and establish a connection
      autoconnect();

      // Get the meta data
      if (Constants.DEBUG) System.out.println("Getting DB meta data...\n");
      DatabaseMetaData metaData = connection.getMetaData();

      // Provide storage for table and column names
      tableNames  = new ArrayList();
      columnNames = new ArrayList();

      // Scan the database's tables
      String[] validTypes = {"TABLE"};
      ResultSet theTables = metaData.getTables(null, null, null, validTypes);
      while (theTables.next())
      {
        String tableName = theTables.getString("TABLE_NAME");
        if (Constants.DEBUG) System.out.println("Table found: " + tableName);
        tableNames.add(tableName);

        // Now get the columns in that table
        ResultSet theColumns = metaData.getColumns(null, null, tableName, null);
        while (theColumns.next())
        {
          String columnName = theColumns.getString("COLUMN_NAME");
          if (Constants.DEBUG) System.out.println("\tColumn found: " + columnName);
          columnNames.add(columnName);
        }
        //System.out.println();
      }
    }
    catch (Exception e)
    {
      System.out.println("Problems communicating with the database!");
    }
  } // end getDBMetaData method

  /**
   * Connect to a database using its stored settings
   */
  public void autoconnect()
  {
    try
    {
      // Load the DB driver
      loadDriver(getDriver());

      // Establish a connection
      connect(getName(), getUser(), getPassword());

      // create Statement to query database
      statement = connection.createStatement
      (
        ResultSet.TYPE_SCROLL_INSENSITIVE,
        ResultSet.CONCUR_READ_ONLY
      );

      connectedToDatabase = true;
    }
    catch (Exception x)
    {
      connectedToDatabase = false;
      if (Constants.DEBUG)
        System.out.println("Problems communicating with the database!");
    }
  } // end autoconnect

  /**
   * Loads the appropriate driver class into the Java namespace
   * @throws Exception if the class could not be located
   */
  public void loadDriver(final String driver) throws Exception
  {
    // Load the DB driver.
    try
    {
      Class.forName(driver);
    }
    catch(ClassNotFoundException cnfx)
    {
      throw new Exception("Can't load JDBC driver!");
    }
  }

  /**
   * Establishes a connection to a database.
   * @param name the name of the database IF NULL then generic connection to
   * database is established
   * @throws Exception error getting the connection or creating statement
   */
  public void connect(final String name, final String user, final String password) throws Exception
  {
    // Get a connection.
    if (connection == null || connection.isClosed() || !connectedToDatabase)
    {
      try
      {
        if (Constants.DEBUG)
          System.out.println("Attempting to connect to database " + name + "...\n");

        String connectionString =  "jdbc:" +
                                   server +
                                   "://" +
                                   host +
                                   ((port != null)? (":" + port) : "") +
                                   "/" +
                                   ((name != null)?name:"");

        connection = DriverManager.getConnection(connectionString, user, password);

        connectedToDatabase = true;

        if (Constants.DEBUG) System.out.println("... connected.\n");

        // Detect SQL warnings
        if (Constants.DEBUG)
        {
          SQLWarning warning = connection.getWarnings();
          while (warning != null)
          {
            System.out.println("SQLState: " + warning.getSQLState ());
            System.out.println("Message : " + warning.getMessage  ());
            System.out.println("Vendor  : " + warning.getErrorCode());
            System.out.println();
            warning = warning.getNextWarning();
          }
        }
      }
      catch (SQLException sqlx)
      {
        connectedToDatabase = false;
//				sqlx.printStackTrace();
        if (Constants.DEBUG)
        {
           sqlx.printStackTrace();
        }
        throw new Exception("Could not establish connection to database " + name
          + ".\n " + sqlx.getMessage() );
      }
    }
  }

  /**
   * Disconnect a database.
   */
  public void disconnect()
  {
    if (! connectedToDatabase)
      return;

    try
    {
      if (Constants.DEBUG) System.out.println("Attempting to disconnect DB...\n");

      // Close statement and connection
      statement.close();
      connection.close();

      if (Constants.DEBUG) System.out.println("... disconnected.\n");
    }
    catch (SQLException sqlx)
    {
      sqlx.printStackTrace();
    }
    finally
    {
      // Update database connection status
      connectedToDatabase = false;
    }
  } // end method disconnect

  /**
  * Set (mutator) methods
  */
  public void setId(final Long id)
  {
    this.id = id;
  }

  public void setType(final int type)
  {
    this.type = type;
  }

  public void setServer(final String server)
  {
    this.server = server;
    String driver = get_Driver(server);
    setDriver(driver);
  }
  
  public static String get_Driver(final String serverName)
  {
    String name = serverName.toLowerCase();
    return (String)drivers.get(name); 
  }

  public void setHost(final String host)
  {
    this.host = host;
  }

  public void setPort(final String port)
  {
    this.port = port;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public void setDriver(final String driver)
  {
    this.driver = driver;
  }

  public void setUser(final String user)
  {
    this.user = user;
  }

  public void setPassword(final String password)
  {
    this.password = password;
  }

  public void setConnection(final Connection connection)
  {
    this.connection = connection;
  }

  public void setStatement(final Statement statement)
  {
    this.statement = statement;
  }

  public void setRecheck(final boolean recheck)
  {
    this.recheck = recheck;
  }

  public void setTableNames(final java.util.List tableNames)
  {
    this.tableNames = tableNames;
  }

  public void setColumnNames(final java.util.List columnNames)
  {
    this.columnNames = columnNames;
  }

  public void setConnectedToDatabase(final boolean connectedToDatabase)
  {
    this.connectedToDatabase = connectedToDatabase;
  }

  /**
  * Get (accessor) methods
  */
  public Long getId()
  {
    return id;
  }

  public int getType()
  {
    return type;
  }

  public String getServer()
  {
    return server;
  }

  public String getHost()
  {
    return host;
  }

  public String getPort()
  {
    return port;
  }

  public String getName()
  {
    return name;
  }

  public String getDriver()
  {
    return driver;
  }

  public String getUser()
  {
    return user;
  }

  public String getPassword()
  {
    return password;
  }

  public Connection getConnection()
  {
    return connection;
  }

  public Statement getStatement()
  {
    return statement;
  }

  public boolean getRecheck()
  {
    return recheck;
  }

  public java.util.List getTableNames(boolean recheck)
  {
    if (tableNames == null || recheck)
      getDBMetaData();
    return tableNames;
  }

   public java.util.List getTableNames()
  {
    return tableNames;
  }

  public java.util.List getColumnNames()
  {
//		if (columnNames == null)
//			getDBMetaData();
    return columnNames;
  }
  
  public String[] getColumnNames(String tableName) throws Exception
  {
      if(connection == null)
      {
         throw new Exception("Please establish the connection with \"" + getName()+"\"");
      }
      Statement statement = connection.createStatement();
      statement.setMaxRows(1);
      ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
      ResultSetMetaData metaData = rs.getMetaData();
      int colCount = metaData.getColumnCount();
      String [] columns = new String[colCount];
      for(int i=0; i<colCount; i++)
      {
         columns[i] = metaData.getColumnName(i+1); //add 1 for to start the index from 1
      }
      rs.close();
      statement.close();
      return columns;
  }
  
  public Class[] getColumnClasses(String tableName) throws Exception
  {
      if(connection == null)
      {
         throw new Exception("Please establish the connection with \"" + getName()+"\"");
      }
      Statement statement = connection.createStatement();
      statement.setMaxRows(1);
      ResultSet rs = statement.executeQuery("SELECT * FROM " + tableName);
      ResultSetMetaData metaData = rs.getMetaData();
      int count = metaData.getColumnCount();
      Class [] colClasses = new Class[count];
      for(int i=0; i< count; i++)
      {
         colClasses[i] = Class.forName(metaData.getColumnClassName(i+1));
      }
      rs.close();
      statement.close();
      return colClasses;
  }

  public boolean isConnected()
  {
    return connectedToDatabase;
  }

  public boolean getConnectedToDatabase()
  {
    return connectedToDatabase;
  }

  /**
   * Provide a String representation of a database
   */
  public String toString()
  {
    return (getHost() + ":" + getName());
  } // end toString method

  /**
   * Return a synopsis of a database
   */
  public String getSynopsis()
  {
    return
    (
      new StringBuffer("Database ")
        .append(getName())
        .append(" on host ")
        .append(getHost())
        .append(" uses a ")
        .append(getDriver())
        .append(" driver")
        .append(" to connect with a ")
        .append(getServer())
        .append(" server")
        .append(" on port ")
        .append(getPort())
        .append(".\n")
        .append("The user is ")
        .append(getUser())
        .append(".\n")
    ).toString();
  } // end getSynopsis method

  /**
   * Execute an SQL query and return the result set.
   */
  public ResultSet executeQuery(final String query)
    throws SQLException, Exception
  {
    if (connection == null || connection.isClosed() || !connectedToDatabase)
    {
      throw new Exception("Please establish a connection first");
    }
    else
    {
//			Statement statement = connection.createStatement();
//System.out.println("statement.executeQuery="+query);       
      return statement.executeQuery(query);
    }
  }

  /**
   * Execute an SQL query.
   */
  public boolean execute(final String query)
    throws SQLException, Exception
  {
    if (connection == null || connection.isClosed() || !connectedToDatabase)
    {
       //autoconnect();
       throw new Exception("Please establish a connection first");
    }
    if (Constants.DEBUG)
       System.out.println("Query: " + query);

    try
    {
       //			Statement statement = connection.createStatement();
       return statement.execute(query);
    }
    catch(SQLException sqle)
    {
       System.out.println("Query Error: " + query);
       throw sqle;
    }
  }

     /**
   * Create the table using the header with multiple primary colums
   * NOTE: please ensure that primaryCols is a subset of colNames before calling this method
   * @param colNames the column names
   * @param colTypes the column types
   * @param primaryCols String [] the primary Col
   * @throws Exception when there are different numbers of colNames and colTypes
  **/
  public void createTable(String tableName, String[] colNames, String[] colTypes,
                          String []primaryCols, boolean overwrite) throws Exception
  {
    // check to see if there are the same number of  column names and column types
    int length = colNames.length;
    if (length != colTypes.length)
      throw new Exception("There are different numbers of column names and types");

    if (overwrite)
    {
       execute("DROP TABLE IF EXISTS " + tableName);
    }

    String queryString = "CREATE TABLE IF NOT EXISTS "
                       + getName() + "." + tableName + " (";


    for (int i = 0; i < length-1 ; i++)
    {
       queryString +=  clean(colNames[i]) + " " + colTypes[i] + ", " ;
    }// for i
    queryString += clean(colNames[length-1]) +" "+ colTypes[length-1] ;

    String primaryColumns = "";
    if(primaryCols!=null && primaryCols.length !=0)
    {
      primaryColumns =  ", PRIMARY KEY (";
      for(int i=0; i<primaryCols.length-1; i++)
       {
         primaryColumns += clean(primaryCols[i]) + ", ";
       }
      primaryColumns += clean(primaryCols[primaryCols.length-1]) + " )";
    }
    queryString = queryString + primaryColumns + ")";
System.out.println("create table : " + queryString);
    // execute the query
    try
    {
       execute(queryString);
    }
    catch (Exception e)
    {
       System.err.println("Error occurred while executing this query: "+queryString);
       throw e;
    }
  }//createTable()

   /** Create the table using the header
   *RP: WARNING: auto can be specified for integer colTypes only. we have to change this method
   * @param colNames the column names
   * @param colTypes the column types
   * @param primaryCol the primary Col
   * @param auto whether the primary Col should be auto increment
   * @throws Exception when there are different numbers of colNames and colTypes
  **/
  public void createTable(String tableName, String[] colNames, String[] colTypes,
                          String primaryCol, boolean auto, boolean overwrite) throws Exception
  {
    // check to see if there are the same number of	column names and column types
    if (colNames.length != colTypes.length)
      throw new Exception("There are different numbers of column names and types");

    if (overwrite)
    {
       execute("DROP TABLE IF EXISTS " + tableName);
    }

    String queryString = "CREATE TABLE IF NOT EXISTS "
                       + getName() + "." + tableName + " (";

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

//System.out.println("create table : " + queryString);
    // execute the query
    try
    {
       execute(queryString);
    }
    catch (Exception e)
    {
       System.err.println("Error occurred while executing this query: "+queryString);
       throw e;
    }
  }//createTable()

  /**
   * @param data to be put into the table
   * @param colTypes the data types
   * @throws Exception if error entering data
   */
  public void insertRow(String table, String[] data, String[] colTypes) throws Exception
  {
/*
for (int k = 0; k < data.length; k++)
 System.out.print(data[k] + " ");
System.out.println();
*/
     String insertPrefix = "INSERT INTO " + getName() + "." + table + " VALUES(";

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
        execute(sb.toString());
     }
     catch (Exception e)
     {
        System.err.println("Error occurred while executing this query: "+sb.toString());
        throw e;
     }
  } // acceptDataRow()

  /**
   * gets rid of unwanted characters
   * @param dirtyStr the string to be cleaned
   * @return String the cleaned up string
   */
  public static String clean(String dirtyStr)
  {
     // currently only remove " and replace by blank
     String cleanStr = dirtyStr.replace('-', '_');
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
   * Obtain unique values in a column from a table in an SQL database.
   */
   public java.util.List getUniqueColumnValues(final String tableName, final String columnName, boolean ascending)
   {
    java.util.List uniqueValues = null;

    try
    {
        String orderStyle = ascending ? "ASC" : "DESC";
      // @TODO: Use a PreparedStatement for this
      final ResultSet rs = executeQuery
      (
        "SELECT DISTINCT " + columnName +
        " FROM " + tableName +
        " ORDER BY " + columnName +
        " " + orderStyle
      );

      uniqueValues = new ArrayList();
      while (rs.next())
        uniqueValues.add(rs.getObject(1));
    }
    catch (SQLException sqlx)
    {
      if (Constants.DEBUG)
        System.out.println("Database error:\n" + sqlx.getMessage());
    }
    catch (Exception x)
    {
      if (Constants.DEBUG)
        x.printStackTrace();
    }

    return uniqueValues;
  }

  /**
   * Resolve a read in object.
   *
   * @return The resolved object read in.
   *
   * @throws ObjectStreamException if there is a problem reading the object.
   * @throws RuntimeException If the read object doesnt exist.
   */
  public Object readResolve() throws ObjectStreamException
  {
    Database db = new Database(type, server, host, port, name, user, password);
    if (db == null)
    {
      throw new RuntimeException("Database object not found");
    }
    return db;
  }

  /**
   * Implement an equals() method for a Database -
   * required by Hibernate for custom objects.
   */
  public boolean equals(final Object other)
  {
    if (this == other)
      return true;

    if (! (other instanceof Database) )
      return false;

    Database that = (Database) other;
    String thing1 = this.getSynopsis();
    String thing2 = that.getSynopsis();
    if (thing1 != null && thing2 != null)
    {
      if (! thing1.equals(thing2))
        return false;
    }
    return true;
  }

  /**
   * Implement a hashCode() method for a Database -
   * required by Hibernate for custom objects.
   */
  public int hashCode()
  {
    return getSynopsis().hashCode();
  }

   /**
   * Creates a new object of the same class and with the
   * same contents as this object.
   */
  public Object clone()
  {
    try
    {
      return super.clone();
    }
    catch (CloneNotSupportedException cnsx)
    {
      throw new InternalError();
    }
  }
} // end Database class
