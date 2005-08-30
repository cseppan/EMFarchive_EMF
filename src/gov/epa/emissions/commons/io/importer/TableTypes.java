package gov.epa.emissions.commons.io.importer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class TableTypes {

    public static final String REF_CONTROL_DEVICE_CODES = "Reference Control Device Codes";

    public static final String REF_CONVERSION_FACTORS = "Reference Conversion Factors";

    // public static final String REF_COUNTRIES = "Reference Countries";
    public static final String REF_EMISSION_TYPES = "Reference Emission Types";

    public static final String REF_EMISSION_UNITS_CODES = "Reference Emissions Units Codes";

    public static final String REF_FIPS = "Reference Facility Identification Data Standard (FIPS)";

    public static final String REF_MACT_CODES = "Reference Maximum Achievable Control Technology (MACT) Codes";

    public static final String REF_MATERIAL_CODES = "Reference Material Codes";

    public static final String REF_NAICS_CODES = "Reference North American Industrial Classification System (NAICS) Codes";

    public static final String REF_POLLUTANT_CODES = "Reference Pollutant Codes";

    // public static final String REF_POLLUTANTS = "Reference Pollutants";
    public static final String REF_SCC = "Reference Source Classification Codes (SCC)";

    // public static final String REF_SECTORS = "Reference Sectors";
    public static final String REF_SIC_CODES = "Reference Standard Industrial Classification (SIC) Codes";

    // public static final String REF_STATES = "Reference States";
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

    private static final Map DATASET_TYPE_TO_TABLE_TYPES_MAP;

    private static HashMap FileNamePrefixesToTableType;

    static {
        DATASET_TYPE_TO_TABLE_TYPES_MAP = new HashMap();
        DATASET_TYPE_TO_TABLE_TYPES_MAP.put(DatasetTypes.REFERENCE, new String[] { REF_CONTROL_DEVICE_CODES,
                REF_CONVERSION_FACTORS, REF_EMISSION_TYPES, REF_EMISSION_UNITS_CODES, REF_FIPS, REF_MACT_CODES,
                REF_MATERIAL_CODES, REF_NAICS_CODES, REF_POLLUTANT_CODES, REF_SCC, REF_SIC_CODES, REF_TIME_ZONES,
                REF_TRIBAL_CODES });

        DATASET_TYPE_TO_TABLE_TYPES_MAP.put(DatasetTypes.ORL_AREA_NONPOINT_TOXICS, new String[] {
                ORL_AREA_NONPOINT_TOXICS, ORL_AREA_NONPOINT_TOXICS_SUMMARY });
        DATASET_TYPE_TO_TABLE_TYPES_MAP.put(DatasetTypes.ORL_AREA_NONROAD_TOXICS, new String[] {
                ORL_AREA_NONROAD_TOXICS, ORL_AREA_NONROAD_TOXICS_SUMMARY });
        DATASET_TYPE_TO_TABLE_TYPES_MAP.put(DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS, new String[] { ORL_MOBILE_TOXICS,
                ORL_MOBILE_TOXICS_SUMMARY });
        DATASET_TYPE_TO_TABLE_TYPES_MAP.put(DatasetTypes.ORL_POINT_TOXICS, new String[] { ORL_POINT_TOXICS,
                ORL_POINT_TOXICS_SUMMARY });

        FileNamePrefixesToTableType = new HashMap();
        FileNamePrefixesToTableType.put("control_device_codes", REF_CONTROL_DEVICE_CODES);
        FileNamePrefixesToTableType.put("conversion_factors", REF_CONVERSION_FACTORS);
        FileNamePrefixesToTableType.put("emission_types", REF_EMISSION_TYPES);
        FileNamePrefixesToTableType.put("emission_units_codes", REF_EMISSION_UNITS_CODES);
        FileNamePrefixesToTableType.put("fips", REF_FIPS);
        FileNamePrefixesToTableType.put("mact_codes", REF_MACT_CODES);
        FileNamePrefixesToTableType.put("material_codes", REF_MATERIAL_CODES);
        FileNamePrefixesToTableType.put("naics_codes", REF_NAICS_CODES);
        FileNamePrefixesToTableType.put("pollutant_codes", REF_POLLUTANT_CODES);
        FileNamePrefixesToTableType.put("scc", REF_SCC);
        FileNamePrefixesToTableType.put("sic_codes", REF_SIC_CODES);
        FileNamePrefixesToTableType.put("time_zones", REF_TIME_ZONES);
        FileNamePrefixesToTableType.put("tribal_codes", REF_TRIBAL_CODES);
    }

    public static final String[] getTableTypes(String datasetType) {
        return (String[]) DATASET_TYPE_TO_TABLE_TYPES_MAP.get(datasetType);
    }

    public static final String getTableType(String datasetType, String filename) {
        if (!DatasetTypes.REFERENCE.equals(datasetType))
            return null;

        for (Iterator iter = FileNamePrefixesToTableType.keySet().iterator(); iter.hasNext();) {
            String filenameKey = (String) iter.next();
            if (filename.startsWith(filenameKey))
                return (String) FileNamePrefixesToTableType.get(filenameKey);
        }

        return null;
    }
}
