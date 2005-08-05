package gov.epa.emissions.commons.io.importer;

import java.util.*;

/**
 * @author		Craig Mattocks
 * @version $Id: FileImportDetails.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 *
 */
public class FileImportDetails extends Vector/*<String>*/{

   private HashMap/*<String, String>*/ nameTypeMap = new HashMap/*<String, String>*/();
   private HashMap/*<String, String>*/ nameWidthMap = new HashMap/*<String, String>*/();

   /** what file types these details are for **/
   private String name = null;

   public FileImportDetails(String name) {
      super();
      this.name = name;
   }//FileImportDetails(String)

   /**
    * set the type of a column
    * @param name the column name
    * @param type the column type
    * @throws Exception if the column name does not already exist in the names list
    */
   public void setType(String name, String type) throws Exception
   {
      if (!contains(name))
         throw new Exception("Could not set type since name does not exist");

      nameTypeMap.put(name, type);
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

      nameWidthMap.put(name, width);
   }//setWidth(String, String)

   /**
    * gets the type of a column
    * @param name the column name
    * @return Class the column type
    */
   public String getType(String name)
   {
      String type = (String) nameTypeMap.get(name);

      if (type.equals("C"))
         return "VARCHAR(" + getWidth(name) + ")";
      else if(type.equals("I"))
          return "INT";
      // if the type is "N" that means number, then check if there is either
      // "date" or "time" contained in the name.. if so.. return appropriate
      // type
      else if (type.equals("N"))
      {
         // if the name contains date
         if (name.indexOf("date") > -1)
         {
            return "DATE";
         }
         if (name.indexOf("time") > -1)
         {
            return "INT";
         }
         return "DOUBLE";
      }
      return null;
   }//getType(String)

   /**
    * gets the width of a column
    * @param name the column name
    * @return int the column width
    */
   public int getWidth(String name)
   {
      int width = Integer.parseInt((String) nameWidthMap.get(name));
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
   public void setName(String newName)
   {
      this.name = newName;
   }//setName(String)

   /**
    * returns the name of this details object
    * @return String the name
    */
   public String getName()
   {
      return name;
   }//getName(String)
}
