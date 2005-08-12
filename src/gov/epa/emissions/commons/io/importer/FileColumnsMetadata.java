package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.db.SqlTypeMapper;

import java.util.HashMap;
import java.util.Vector;

/**
 * Maps File Type to Generic Data Type
 * Maps Name of a Column in a File to its Width 
 * @author		Craig Mattocks
 * @version $Id: FileColumnsMetadata.java,v 1.1 2005/08/12 15:46:42 rhavaldar Exp $
 *
 */
public class FileColumnsMetadata extends Vector/*<String>*/{//TODO: why ???

   private HashMap/*<String, String>*/ columnNameTypeMap = new HashMap/*<String, String>*/();
   private HashMap/*<String, String>*/ columnNameWidthMap = new HashMap/*<String, String>*/();

   /** what file types these details are for **/
   private String tableName; //TODO: name of file ? or table ? or ??
   private SqlTypeMapper sqlTypeMapper;

   public FileColumnsMetadata(String tableName, SqlTypeMapper sqlTypeMapper) {
      this.tableName = tableName;
      this.sqlTypeMapper = sqlTypeMapper;
   }

   /**
    * set the type of a column
    * @param columnName the column name
    * @param columnType the column type
    * @throws Exception if the column name does not already exist in the names list
    */
   public void setType(String columnName, String columnType) throws Exception
   {
      if (!contains(columnName))
         throw new Exception("Could not set type since name does not exist");

      columnNameTypeMap.put(columnName, columnType);
   }//setType(String, String)

   /**
    * set the width of a column
    * @param name the column name
    * @param width the column width
    * @throws Exception if the column name does not already exist in the names list
    */
   public void setWidth(String name, String width) throws Exception
   {
      if (!contains(name))
         throw new Exception("Could not set type since name does not exist");

      columnNameWidthMap.put(name, width);
   }//setWidth(String, String)

   /**
    * gets the type of a column
    * @param name the column name
    * @return Class the column type
    */
   public String getType(String name)
   {
      String type = (String) columnNameTypeMap.get(name);

      return sqlTypeMapper.getSqlType(name, type, getWidth(name));      
   }//getType(String)

   /**
    * gets the width of a column
    * @param name the column name
    * @return int the column width
    */
   public int getWidth(String name)
   {
      int width = Integer.parseInt((String) columnNameWidthMap.get(name));
      return width;
   }//getWidth(String)

   /**
    * get the namess for all columns
    * @return
    */
   public String[] getColumnNames()
   {
    String[] columnNames = new String[size()];
    columnNames = (String[]) toArray(columnNames);
	return columnNames;
   }//getColumnNames()
   
   /**
    * get the widths for all columns
    * @return
    */
   public int[] getColumnWidths()
   {
    int[] columnWidths = new int[size()];
    for (int i = 0; i < size(); i++)
    {
      columnWidths[i] = getWidth(get(i).toString());
    }//for int i
	return columnWidths;
   }//getColumnWidths()

   /**
    * get the types for all columns
    * @return
    */
   public String[] getColumnTypes()
   {
    String[] columnTypes = new String[size()];
    for (int i = 0; i < size(); i++)
    {
      columnTypes[i] = getType(get(i).toString());
    }//for int i
	return columnTypes;
   }//getColumnTypes()
   
   /**
    * sets a new name for this details object
    * @param newName to be set
    */
   public void setTableName(String newName)
   {
      this.tableName = newName;
   }//setName(String)

   /**

    * returns the name of this details object
    * @return String the name
    */
   public String getTableName()
   {
      return tableName;
   }
}
