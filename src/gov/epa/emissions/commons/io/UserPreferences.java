package gov.epa.emissions.commons.io;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;

import java.util.Properties;
import java.io.*;
/**
 * UserPreferences.java
 *
 * Created on July 15, 2005, 11:24 AM
 *
 * @version $Id: UserPreferences.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 * @author  Parthee Partheepans
 */
public class UserPreferences extends Properties
{
   //keys for the properties
   //reference database
   public static final String DB_REFERENCE_NAME="Database.Reference.Name";
   public static final String DB_REFERENCE_SERVER="Database.Reference.Type";
   public static final String DB_REFERENCE_USER="Database.Reference.UserName";
   public static final String DB_REFERENCE_PWD="Database.Reference.Password";
   public static final String DB_REFERENCE_HOST="Database.Reference.Host";
   public static final String DB_REFERENCE_PORT="Database.Reference.Port";

   //emission database
   public static final String DB_EMISSION_NAME="Database.Emission.Name";
   public static final String DB_EMISSION_SERVER="Database.Emission.Type";
   public static final String DB_EMISSION_USER="Database.Emission.UserName";
   public static final String DB_EMISSION_PWD="Database.Emission.Password";
   public static final String DB_EMISSION_HOST="Database.Emission.Host";
   public static final String DB_EMISSION_PORT="Database.Emission.Port";

   //analysis database
   public static final String DB_ANALYSIS_NAME="Database.Analysis.Name";
   public static final String DB_ANALYSIS_SERVER="Database.Analysis.Type";
   public static final String DB_ANALYSIS_USER="Database.Analysis.UserName";
   public static final String DB_ANALYSIS_PWD="Database.Analysis.Password";
   public static final String DB_ANALYSIS_HOST="Database.Analysis.Host";
   public static final String DB_ANALYSIS_PORT="Database.Analysis.Port";

   //NIF File definition 
   public static final String DATASET_NIF_FIELD_DEFS="DATASET_NIF_FIELD_DEFS";

   //Reffile dir
   public static final String REFERENCE_FILES_BASE_DIR="REFERENCE_FILE_BASE_DIR";

   private static  UserPreferences userPreferences = null;

   static
   {
      userPreferences = UserPreferences.getInstance();
      try
      {
         String fileName = getSystemProperty("USER_PREFERENCES");
         String nifFieldDef = getSystemProperty(UserPreferences.DATASET_NIF_FIELD_DEFS);
         userPreferences.setProperty(UserPreferences.DATASET_NIF_FIELD_DEFS, 
            nifFieldDef);
         checkFile(fileName,"USER_PREFERENCES");
         checkFile(nifFieldDef,UserPreferences.DATASET_NIF_FIELD_DEFS);
         
         File file = new File(fileName);
         FileInputStream inStream = new FileInputStream(file);
         userPreferences.load(inStream);
         if(Constants.DEBUG)
         {
            System.out.println("User Preferences......");
            java.util.Set keys = userPreferences.keySet();
            java.util.Iterator iterator = keys.iterator();
            while(iterator.hasNext())
            {
               Object key = iterator.next();
               String value =userPreferences.getProperty((String)key);
               System.out.println("key=" +key + ", value=" +value);
            }
            System.out.println("..............");
         }
         
      }
      catch(Exception e)
      {
         DefaultUserInteractor.get().notify(null,"Error", e.getMessage(), 
            UserInteractor.ERROR);
         e.printStackTrace();
         System.exit(1);
      }
      
   }

   public static String getSystemProperty(String key) throws Exception
   {
      String value = System.getProperty(key);
      if(value == null || value.trim().length()==0)
      {
         throw new Exception("Please specify the '" +key + "' environment variable");
      }
      return value.trim();
   }//getSystemProperty

   public static void checkDirectory(String fileName,String key) throws Exception
   {
      File file = new File(fileName);
      if(!file.exists())
      {
         throw new Exception("The directory '" +file +
            "' does not exist.\nPlease specify a correct directory"+ 
            "name for '"+key +"' environment variable");
      }
      if(!file.isDirectory())
      {
         throw new Exception("The '" +file +
            "' is not a directory.\nPlease specify a correct directory "+ 
            "name for '" +key+ "' environment variable");
      }
   }

   public static void checkFile(String fileName,String key) throws Exception
   {
      File file = new File(fileName);
      if(!file.exists())
      {
         throw new Exception("The file '" +fileName +
            "' does not exist.\nPlease specify a correct file "+ 
            "name for '"+key +"' environment variable");
      }
      if(!file.isFile())
      {
         throw new Exception("The '" +fileName +
            "' is not a file.\nPlease specify a correct file "+ 
            "name for '" +key+ "' environment variable");
      }
   }

   private UserPreferences()
   {
      
   }

   public static UserPreferences getInstance()
   {
      if(userPreferences  == null)
      {
         userPreferences = new UserPreferences();
      }
      return userPreferences;
   }

   //helper method to get FIELD DEF
   //helper method to get REF_BASE_DIR

}
