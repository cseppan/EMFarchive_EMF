package gov.epa.emissions.commons.io;

import java.util.HashMap;
import java.util.Map;


/**
 * Table type String constants
 *
 * @author    Craig Mattocks, Keith Lee
 * @version $Id: TableTypes.java,v 1.1 2005/08/05 13:14:28 rhavaldar Exp $
 */
public final class TableTypes
{
    public static final String IDA_AREA = "IDA Area Source Emissions";
    public static final String IDA_MOBILE_ACTIVITY = "IDA Mobile Source Activity";
    public static final String IDA_MOBILE_EMISSIONS = "IDA Mobile Source Emissions";
    public static final String IDA_POINT = "IDA Point Source Emissions";

    public static final String NIF_AREA_CE = "Nonpoint Control Equipment";
    public static final String NIF_AREA_EM = "Nonpoint Emission";
    public static final String NIF_AREA_EP = "Nonpoint Emission Processes";
    public static final String NIF_AREA_PE = "Nonpoint Emission Periods";
    public static final String NIF_AREA_SUMMARY = "Nonpoint Emission Summary";

    public static final String NIF_POINT_CE = "Point Control Equipment";
    public static final String NIF_POINT_EM = "Point Emission Records";
    public static final String NIF_POINT_EP = "Point Emission Processes";
    public static final String NIF_POINT_ER = "Point Emission Release";
    public static final String NIF_POINT_EU = "Point Emission Units";
    public static final String NIF_POINT_PE = "Point Emission Periods";
    public static final String NIF_POINT_SI = "Point Emission Sites";
    public static final String NIF_POINT_SUMMARY = "Point Emission Summary";

    public static final String REF_CONTROL_DEVICE_CODES = "Reference Control Device Codes";
    public static final String REF_CONVERSION_FACTORS = "Reference Conversion Factors";
    //public static final String REF_COUNTRIES = "Reference Countries";
    public static final String REF_EMISSION_TYPES = "Reference Emission Types";
    public static final String REF_EMISSION_UNITS_CODES = "Reference Emissions Units Codes";
    public static final String REF_FIPS = "Reference Facility Identification Data Standard (FIPS)";
    public static final String REF_MACT_CODES = "Reference Maximum Achievable Control Technology (MACT) Codes";
    public static final String REF_MATERIAL_CODES = "Reference Material Codes";
    public static final String REF_NAICS_CODES = "Reference North American Industrial Classification System (NAICS) Codes";
    public static final String REF_POLLUTANT_CODES = "Reference Pollutant Codes";
    //public static final String REF_POLLUTANTS = "Reference Pollutants";
    public static final String REF_SCC = "Reference Source Classification Codes (SCC)";
    //public static final String REF_SECTORS = "Reference Sectors";
    public static final String REF_SIC_CODES = "Reference Standard Industrial Classification (SIC) Codes";
    //public static final String REF_STATES = "Reference States";
    public static final String REF_TIME_ZONES = "Reference Time Zones";
    public static final String REF_TRIBAL_CODES = "Reference Tribal Codes";

    public static final String ORL_AREA_NONPOINT_TOXICS = "ORL Nonpoint Source Toxics";
    public static final String ORL_AREA_NONPOINT_TOXICS_SUMMARY = "ORL Nonpoint Toxics Summary";
    public static final String ORL_AREA_NONROAD_TOXICS = "ORL Nonroad Source Toxics";
    public static final String ORL_AREA_NONROAD_TOXICS_SUMMARY = "ORL Nonroad Toxics Summary";
    public static final String ORL_MOBILE_TOXICS = "ORL Mobile Source Toxics";
    public static final String ORL_MOBILE_TOXICS_SUMMARY = "ORL Mobile Toxics Summary";
    public static final String ORL_POINT_TOXICS = "ORL Point Source Toxics";
    public static final String ORL_POINT_TOXICS_SUMMARY = "ORL Point Toxics Summary";

    /** List of all NAMES in enumeration. */
//    public static final java.util.List NAMES = getAllNames(IDAMobileTableTypes.class);

    /** List of all objects in enumeration. */
//    public static final java.util.List VALUES = getInstances(NIFPointTableTypes.class);

    /** */
    private static final Map DATASET_TYPE_TO_NAMES_ARRAY_MAP;

    private static final Map EMISSION_TABLE_NAMES;

    private static final Map DATASET_TYPE_TO_MAP_OF_FILENAME_TO_TABLE_TYPE_MAP;
    
    static
    {
        DATASET_TYPE_TO_NAMES_ARRAY_MAP = new HashMap();
        DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(DatasetTypes.IDA_AREA, 
            new String[]{IDA_AREA});
        DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(DatasetTypes.IDA_MOBILE_EMISSIONS,
            new String[]{IDA_MOBILE_EMISSIONS});
        //DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(DatasetTypes.IDA_MOBILE_ACTIVITY, new String[]{IDA_MOBILE_ACTIVITY});
        DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(DatasetTypes.IDA_POINT, 
            new String[]{IDA_POINT});
        DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(DatasetTypes.NIF_AREA,
            new String[]{NIF_AREA_CE, NIF_AREA_EM, NIF_AREA_EP, NIF_AREA_PE, NIF_AREA_SUMMARY});
        DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(DatasetTypes.NIF_POINT,
            new String[]{NIF_POINT_CE, NIF_POINT_EM, NIF_POINT_EP, NIF_POINT_ER, NIF_POINT_EU, NIF_POINT_PE, NIF_POINT_SI, NIF_POINT_SUMMARY});
        DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(DatasetTypes.REFERENCE,
                new String[] { REF_CONTROL_DEVICE_CODES,
                        REF_CONVERSION_FACTORS, /*REF_COUNTRIES,*/
                        REF_EMISSION_TYPES, REF_EMISSION_UNITS_CODES, REF_FIPS,
                        REF_MACT_CODES, REF_MATERIAL_CODES, REF_NAICS_CODES,
                        REF_POLLUTANT_CODES, /*REF_POLLUTANTS,*/ REF_SCC,
                        /*REF_SECTORS,*/ REF_SIC_CODES, /*REF_STATES,*/ REF_TIME_ZONES,
                        REF_TRIBAL_CODES });
        DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(
                DatasetTypes.ORL_AREA_NONPOINT_TOXICS,
                new String[] { ORL_AREA_NONPOINT_TOXICS, ORL_AREA_NONPOINT_TOXICS_SUMMARY });
        DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(
                DatasetTypes.ORL_AREA_NONROAD_TOXICS,
                new String[] { ORL_AREA_NONROAD_TOXICS, ORL_AREA_NONROAD_TOXICS_SUMMARY });
        DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(DatasetTypes.ORL_MOBILE_TOXICS,
                new String[] { ORL_MOBILE_TOXICS, ORL_MOBILE_TOXICS_SUMMARY });
        DATASET_TYPE_TO_NAMES_ARRAY_MAP.put(DatasetTypes.ORL_POINT_TOXICS,
                new String[] { ORL_POINT_TOXICS, ORL_POINT_TOXICS_SUMMARY });
        
        EMISSION_TABLE_NAMES = new HashMap();
        EMISSION_TABLE_NAMES.put(DatasetTypes.NIF_AREA, NIF_AREA_EM);
        EMISSION_TABLE_NAMES.put(DatasetTypes.NIF_POINT, NIF_POINT_EM);

        DATASET_TYPE_TO_MAP_OF_FILENAME_TO_TABLE_TYPE_MAP = new HashMap();
        Map filenameToTableTypeMap = new HashMap();
        filenameToTableTypeMap.put("area_ce", NIF_AREA_CE);
        filenameToTableTypeMap.put("area_em", NIF_AREA_EM);
        filenameToTableTypeMap.put("area_ep", NIF_AREA_EP);
        filenameToTableTypeMap.put("area_pe", NIF_AREA_PE);
        DATASET_TYPE_TO_MAP_OF_FILENAME_TO_TABLE_TYPE_MAP.put(DatasetTypes.NIF_AREA, filenameToTableTypeMap);
        filenameToTableTypeMap = new HashMap();
        filenameToTableTypeMap.put("point_ce", NIF_POINT_CE);
        filenameToTableTypeMap.put("point_em", NIF_POINT_EM);
        filenameToTableTypeMap.put("point_ep", NIF_POINT_EP);
        filenameToTableTypeMap.put("point_er", NIF_POINT_ER);
        filenameToTableTypeMap.put("point_eu", NIF_POINT_EU);
        filenameToTableTypeMap.put("point_pe", NIF_POINT_PE);
        filenameToTableTypeMap.put("point_si", NIF_POINT_SI);
        DATASET_TYPE_TO_MAP_OF_FILENAME_TO_TABLE_TYPE_MAP.put(DatasetTypes.NIF_POINT, filenameToTableTypeMap);
        filenameToTableTypeMap = new HashMap();
        filenameToTableTypeMap.put("control_device_codes", REF_CONTROL_DEVICE_CODES);
        filenameToTableTypeMap.put("conversion_factors", REF_CONVERSION_FACTORS);
        //filenameToTableTypeMap.put("countries", REF_COUNTRIES);
        filenameToTableTypeMap.put("emission_types", REF_EMISSION_TYPES);
        filenameToTableTypeMap.put("emission_units_codes", REF_EMISSION_UNITS_CODES);
        filenameToTableTypeMap.put("fips", REF_FIPS);
        filenameToTableTypeMap.put("mact_codes", REF_MACT_CODES);
        filenameToTableTypeMap.put("material_codes", REF_MATERIAL_CODES);
        filenameToTableTypeMap.put("naics_codes", REF_NAICS_CODES);
        filenameToTableTypeMap.put("pollutant_codes", REF_POLLUTANT_CODES);
        //filenameToTableTypeMap.put("pollutants", REF_POLLUTANTS);
        filenameToTableTypeMap.put("scc", REF_SCC);
        //filenameToTableTypeMap.put("sectors", REF_SECTORS);
        filenameToTableTypeMap.put("sic_codes", REF_SIC_CODES);
        //filenameToTableTypeMap.put("states", REF_STATES);
        filenameToTableTypeMap.put("time_zones", REF_TIME_ZONES);
        filenameToTableTypeMap.put("tribal_codes", REF_TRIBAL_CODES);
        DATASET_TYPE_TO_MAP_OF_FILENAME_TO_TABLE_TYPE_MAP.put(DatasetTypes.REFERENCE, filenameToTableTypeMap);
    }

    public static final String [] DEFAULT_NIF_AREA_NAMES = 
      {"nei_area_ce","nei_area_em","nei_area_ep","nei_area_pe","nei_area_summary"};

    public static final String [] DEFAULT_NIF_POINT_NAMES = {
       "nei_point_ce","nei_point_em","nei_point_ep","nei_point_er",
       "nei_point_eu","nei_point_pe","nei_point_si","nei_point_summary"};

    /**
     * Private constructor prevents construction outside of this class.
     */
    private TableTypes()
    {
        /** DO NOTHING **/
    }

    /** 
     * Get a String array of all names in the enumeration.
     *
     * @return    An unmodifiable String[] Array of the names.
     */
    public static final String[] getNamesArray(String datasetType)
    {
        return (String[])DATASET_TYPE_TO_NAMES_ARRAY_MAP.get(datasetType);
    }

    public static final String[] getNIFPointTableTypes()
    {
        return getNamesArray(DatasetTypes.NIF_POINT);
    }

    public static final String[] getNIFAreaTableTypes()
    {
        return getNamesArray(DatasetTypes.NIF_AREA);
    }

    public static final String getEmissionTableType(String datasetType)
    {
       return (String)EMISSION_TABLE_NAMES.get(datasetType);
    }

    //some helper methods
    public static final String getTableType(String datasetType, String fileName)
    {
        String tableType = null;
        Map filenameToTableTypeMap = (Map)DATASET_TYPE_TO_MAP_OF_FILENAME_TO_TABLE_TYPE_MAP.get(datasetType);
        if(filenameToTableTypeMap != null)
        {
            java.util.Iterator it = filenameToTableTypeMap.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry entry = (Map.Entry)it.next();
                String key = (String)entry.getKey();
                if(fileName.indexOf(key) != -1)
                {
                    tableType = (String)entry.getValue();
                    break;//break while
                }
            }
        }

        return tableType;

    }
}
