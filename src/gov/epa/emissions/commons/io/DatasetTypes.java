package gov.epa.emissions.commons.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Dataset type String constants
 *
 * @version $Id: DatasetTypes.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 *
 */
public final class DatasetTypes
{
	/**
	 * Enumeration elements are constructed once upon class loading.
	 * Order of appearance here determines index value.
	 */
	public static final String NIF_AREA	= "NIF3 Nonpoint Inventory";
	public static final String NIF_POINT = "NIF3 Point Inventory";
	//public static final String NIF_MOBILE_EMISSIONS = "NIF3 Mobile Emissions Inventory";
	//public static final String NIF_MOBILE_ACTIVITY = "NIF3 Mobile Activity Inventory";

	public static final String IDA_AREA	= "IDA Area Inventory";
	public static final String IDA_POINT = "IDA Point Inventory";
	public static final String IDA_MOBILE_EMISSIONS = "IDA Mobile Emissions Inventory";
    //public static final String IDA_MOBILE_ACTIVITY = "IDA Mobile Activity Inventory";

    public static final String ORL_AREA_NONPOINT_TOXICS = "ORL Nonpoint Source Toxics Inventory";
    public static final String ORL_AREA_NONROAD_TOXICS = "ORL Nonroad Source Toxics Inventory";
    public static final String ORL_MOBILE_TOXICS = "ORL Mobile Source Toxics Inventory";
    public static final String ORL_POINT_TOXICS = "ORL Point Source Toxics Inventory";

    public static final String REFERENCE = "Reference";

	/** List of all NAMES in enumeration. */
	public static final java.util.List NAMES /*= getAllNames(DatasetTypes.class)*/;

	/** List of all objects in enumeration. */
//	public static final java.util.List VALUES = getInstances(DatasetTypes.class);

	/** Map of all table names for a given Dataset type. */
    private static final Map TABLE_NAMES;

    private static final Map SUMMARY_TABLE_TYPE;

    /**descriptive column names for each dataset type
     */
    private static String[] NIF_AREA_COLUMN_NAMES= {"State","FIPS","SCC","MACT","SIC","NAICS"};
    private static String[] NIF_POINT_COLUMN_NAMES= {"State","FIPS","Facility","Unit","Process",
      "Point","SCC","MACT","SIC","NAICS"};
    
    private static final Map DESCRIPTIVE_COLUMN_NAMES;

    static 
    {
        NAMES = new ArrayList();
        NAMES.add(NIF_AREA);
        NAMES.add(NIF_POINT);
        //NAMES.add(NIF_MOBILE_EMISSIONS);
        //NAMES.add(NIF_MOBILE_ACTIVITY);
        NAMES.add(IDA_AREA);
        NAMES.add(IDA_POINT);
        NAMES.add(IDA_MOBILE_EMISSIONS);
        //NAMES.add(IDA_MOBILE_ACTIVITY);
        //NAMES.add(REFERENCE);
        NAMES.add(ORL_AREA_NONPOINT_TOXICS);
        NAMES.add(ORL_AREA_NONROAD_TOXICS);
        NAMES.add(ORL_MOBILE_TOXICS);
        NAMES.add(ORL_POINT_TOXICS);

        SUMMARY_TABLE_TYPE = new HashMap();
        SUMMARY_TABLE_TYPE.put(NIF_AREA, TableTypes.NIF_AREA_SUMMARY);
        //SUMMARY_TABLE_TYPE.put(NIF_MOBILE_EMISSIONS, TableTypes.NIF_MOBILE_EMISSIONS_SUMMARY);
        SUMMARY_TABLE_TYPE.put(NIF_POINT, TableTypes.NIF_POINT_SUMMARY);
        SUMMARY_TABLE_TYPE.put(IDA_AREA, TableTypes.IDA_AREA);
        SUMMARY_TABLE_TYPE.put(IDA_POINT, TableTypes.IDA_POINT);
        SUMMARY_TABLE_TYPE.put(IDA_MOBILE_EMISSIONS, TableTypes.IDA_MOBILE_EMISSIONS);
        //SUMMARY_TABLE_TYPE.put(IDA_MOBILE_ACTIVITY, TableTypes.IDA_MOBILE_ACTIVITY);
        SUMMARY_TABLE_TYPE.put(ORL_AREA_NONPOINT_TOXICS, TableTypes.ORL_AREA_NONPOINT_TOXICS_SUMMARY);
        SUMMARY_TABLE_TYPE.put(ORL_AREA_NONROAD_TOXICS, TableTypes.ORL_AREA_NONROAD_TOXICS_SUMMARY);
        SUMMARY_TABLE_TYPE.put(ORL_MOBILE_TOXICS, TableTypes.ORL_MOBILE_TOXICS_SUMMARY);
        SUMMARY_TABLE_TYPE.put(ORL_POINT_TOXICS, TableTypes.ORL_POINT_TOXICS_SUMMARY);

        TABLE_NAMES = new HashMap();
        TABLE_NAMES.put(NIF_AREA, TableTypes.getNamesArray (NIF_AREA));
        TABLE_NAMES.put(NIF_POINT, TableTypes.getNamesArray(NIF_POINT));
        //TABLE_NAMES.put(NIF_MOBILE_EMISSIONS, TableTypes.getNamesArray(NIF_MOBILE_EMISSIONS));
        TABLE_NAMES.put(IDA_AREA, TableTypes.getNamesArray(IDA_AREA));
        TABLE_NAMES.put(IDA_POINT, TableTypes.getNamesArray(IDA_POINT));
        TABLE_NAMES.put(IDA_MOBILE_EMISSIONS, TableTypes.getNamesArray(IDA_MOBILE_EMISSIONS));
        //TABLE_NAMES.put(IDA_MOBILE_ACTIVITY, TableTypes.getNamesArray(IDA_MOBILE_ACTIVITY));
        TABLE_NAMES.put(REFERENCE, TableTypes.getNamesArray(REFERENCE));
        TABLE_NAMES.put(ORL_AREA_NONPOINT_TOXICS, TableTypes.getNamesArray(ORL_AREA_NONPOINT_TOXICS));
        TABLE_NAMES.put(ORL_AREA_NONROAD_TOXICS, TableTypes.getNamesArray(ORL_AREA_NONROAD_TOXICS));
        TABLE_NAMES.put(ORL_MOBILE_TOXICS, TableTypes.getNamesArray(ORL_MOBILE_TOXICS));
        TABLE_NAMES.put(ORL_POINT_TOXICS, TableTypes.getNamesArray(ORL_POINT_TOXICS));
        
        DESCRIPTIVE_COLUMN_NAMES = new HashMap();
        DESCRIPTIVE_COLUMN_NAMES.put(NIF_AREA, NIF_AREA_COLUMN_NAMES);
        DESCRIPTIVE_COLUMN_NAMES.put(NIF_POINT, NIF_POINT_COLUMN_NAMES);
    }

    public static boolean isNIFDataset(String datasetType)
    {
        if(datasetType != null)
            return (datasetType.equals(NIF_AREA)
                 || datasetType.equals(NIF_POINT)
                 /*|| datasetType.equals(NIF_MOBILE_EMISSIONS)*/);
        return false;
    }

    public static boolean isIDADataset(String datasetType)
    {
        if(datasetType != null)
            return (datasetType.equals(IDA_AREA)
                 //|| datasetType.equals(IDA_MOBILE_ACTIVITY)
                 || datasetType.equals(IDA_MOBILE_EMISSIONS)
                 || datasetType.equals(IDA_POINT));
        return false;
    }

    public static boolean isORLDataset(String datasetType)
    {
        if(datasetType != null)
            return (datasetType.equals(ORL_AREA_NONPOINT_TOXICS)
                 || datasetType.equals(ORL_AREA_NONROAD_TOXICS)
                 || datasetType.equals(ORL_MOBILE_TOXICS)
                 || datasetType.equals(ORL_POINT_TOXICS));
        return false;
    }

    public static boolean isDirRequired(String datasetType)
    {
        if(datasetType != null)
            return (datasetType.equals(NIF_AREA)
                 || datasetType.equals(NIF_POINT)
                 /*|| datasetType.equals(NIF_MOBILE_EMISSIONS)*/);
        return false;
    }

    public static boolean isFileRequired(String datasetType)
    {
        if(datasetType != null)
            return (datasetType.equals(IDA_AREA)
                 //|| datasetType.equals(IDA_MOBILE_ACTIVITY)
                 || datasetType.equals(IDA_MOBILE_EMISSIONS)
                 || datasetType.equals(IDA_POINT)
                 || datasetType.equals(ORL_AREA_NONPOINT_TOXICS)
                 || datasetType.equals(ORL_AREA_NONROAD_TOXICS)
                 || datasetType.equals(ORL_MOBILE_TOXICS)
                 || datasetType.equals(ORL_POINT_TOXICS));
        return false;
    }

	/**
	 * Private constructor prevents construction outside of this class.
	 */
	private DatasetTypes()
	{
		/** DO NOTHING **/
	}

	/**
	 * Get all dataset types (all names)
	 */
	public static String[] getDatasetTypes()
	{
		return getNamesArray();
	}

	/**
	 * Get the valid table types for a given dataset type
	 */
	public static String[] getTableTypes(final String datasetType)
	{
		return (String[])TABLE_NAMES.get(datasetType);
	}

    public static String getSummaryTableType(final String datasetType)
    {
        return (String)SUMMARY_TABLE_TYPE.get(datasetType);
    }

    public static final String[] getDescriptiveColumnNames(String datasetType)
    {
        return (String[])DESCRIPTIVE_COLUMN_NAMES.get(datasetType);
    }

	/* ########## DO NOT EDIT BELOW THIS LINE ########## */
	/**
	 * Valid enumerator?
	 *
	 * Not rigorous, just check for its name.
	 */
	public static boolean isValid(final String name)
	{
		return NAMES.contains(name);
	}

	/** 
	 * Get a String array of all names in the enumeration.
	 *
	 * @return	An unmodifiable String[] Array of the names.
	 */
	public static final String[] getNamesArray()
	{
		return (String[])NAMES.toArray(new String[NAMES.size()]);
	}
//
//	   /**
//	    * Get a List of all names in the enumeration.
//	    *
//	    * @return	A List of all names of the enumerated items.
//	    */
//   public static java.util.List getAllNames(final Class clazz)
//   {
//      java.util.List names  = new ArrayList();
//      for (Iterator it = getInstances(clazz).iterator(); it.hasNext();)
//      {
//         names.add(((Enum)it.next()).getName());
//      }
//      return names;
//   }
//
}
