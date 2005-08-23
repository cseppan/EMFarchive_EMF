package gov.epa.emissions.commons.io.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id: DatasetTypes.java,v 1.3 2005/08/23 20:28:27 rhavaldar Exp $
 * 
 */
public final class DatasetTypes {
    /**
     * Enumeration elements are constructed once upon class loading. Order of
     * appearance here determines index value.
     */
    public static final String NIF_AREA = "NIF3 Nonpoint Inventory";

    public static final String NIF_POINT = "NIF3 Point Inventory";

    public static final String NIF_MOBILE_EMISSIONS = "NIF3 Mobile Onroad Emissions Inventory";

    public static final String IDA_AREA = "IDA Area Inventory";

    public static final String IDA_POINT = "IDA Point Inventory";

    public static final String IDA_MOBILE_EMISSIONS = "IDA Mobile Emissions Inventory";

    public static final String IDA_MOBILE_ACTIVITY = "IDA Mobile Activity Inventory";

    public static final String ORL_AREA_NONPOINT_TOXICS = "ORL Nonpoint Inventory";

    public static final String ORL_AREA_NONROAD_TOXICS = "ORL Nonroad Inventory";

    public static final String ORL_MOBILE_TOXICS = "ORL Onroad Inventory";

    public static final String ORL_POINT_TOXICS = "ORL Point Inventory";

    public static final String REFERENCE = "Reference";

    /** List of all NAMES in enumeration. */
    public static final List NAMES;

    /** Map of all table names for a given Dataset type. */
    private static final Map TABLE_NAMES;

    private static final Map SUMMARY_TABLE_TYPE;

    /**
     * descriptive column names for each dataset type
     */
    private static String[] NIF_AREA_COLUMN_NAMES = { "State", "FIPS", "SCC", "MACT", "SIC", "NAICS" };

    private static String[] NIF_POINT_COLUMN_NAMES = { "State", "FIPS", "Facility", "Unit", "Process", "Point", "SCC",
            "MACT", "SIC", "NAICS" };

    private static String[] NIF_MOBILE_EMISSIONS_COLUMN_NAMES = { "State", "FIPS" };

    private static final Map DESCRIPTIVE_COLUMN_NAMES;

    static {
        NAMES = new ArrayList();
        NAMES.add(NIF_AREA);
        NAMES.add(NIF_POINT);
        NAMES.add(NIF_MOBILE_EMISSIONS);
        // TODO keithlee - uncomment to add as option in GUI
        // NAMES.add(NIF_MOBILE_ACTIVITY);
        NAMES.add(IDA_AREA);
        NAMES.add(IDA_POINT);
        NAMES.add(IDA_MOBILE_EMISSIONS);
        // TODO keithlee - uncomment to add as option in GUI
        // NAMES.add(IDA_MOBILE_ACTIVITY);
        // NAMES.add(REFERENCE);
        NAMES.add(ORL_AREA_NONPOINT_TOXICS);
        NAMES.add(ORL_AREA_NONROAD_TOXICS);
        NAMES.add(ORL_MOBILE_TOXICS);
        NAMES.add(ORL_POINT_TOXICS);

        SUMMARY_TABLE_TYPE = new HashMap();
        SUMMARY_TABLE_TYPE.put(NIF_AREA, TableTypes.NIF_AREA_SUMMARY);
        SUMMARY_TABLE_TYPE.put(NIF_MOBILE_EMISSIONS, TableTypes.NIF_MOBILE_EMISSIONS_SUMMARY);
        SUMMARY_TABLE_TYPE.put(NIF_POINT, TableTypes.NIF_POINT_SUMMARY);
        SUMMARY_TABLE_TYPE.put(IDA_AREA, TableTypes.IDA_AREA);
        SUMMARY_TABLE_TYPE.put(IDA_POINT, TableTypes.IDA_POINT);
        SUMMARY_TABLE_TYPE.put(IDA_MOBILE_EMISSIONS, TableTypes.IDA_MOBILE_EMISSIONS);
        SUMMARY_TABLE_TYPE.put(IDA_MOBILE_ACTIVITY, TableTypes.IDA_MOBILE_ACTIVITY);
        SUMMARY_TABLE_TYPE.put(ORL_AREA_NONPOINT_TOXICS, TableTypes.ORL_AREA_NONPOINT_TOXICS_SUMMARY);
        SUMMARY_TABLE_TYPE.put(ORL_AREA_NONROAD_TOXICS, TableTypes.ORL_AREA_NONROAD_TOXICS_SUMMARY);
        SUMMARY_TABLE_TYPE.put(ORL_MOBILE_TOXICS, TableTypes.ORL_MOBILE_TOXICS_SUMMARY);
        SUMMARY_TABLE_TYPE.put(ORL_POINT_TOXICS, TableTypes.ORL_POINT_TOXICS_SUMMARY);

        TABLE_NAMES = new HashMap();
        TABLE_NAMES.put(NIF_AREA, TableTypes.getNamesArray(NIF_AREA));
        TABLE_NAMES.put(NIF_POINT, TableTypes.getNamesArray(NIF_POINT));
        TABLE_NAMES.put(NIF_MOBILE_EMISSIONS, TableTypes.getNamesArray(NIF_MOBILE_EMISSIONS));
        TABLE_NAMES.put(IDA_AREA, TableTypes.getNamesArray(IDA_AREA));
        TABLE_NAMES.put(IDA_POINT, TableTypes.getNamesArray(IDA_POINT));
        TABLE_NAMES.put(IDA_MOBILE_EMISSIONS, TableTypes.getNamesArray(IDA_MOBILE_EMISSIONS));
        TABLE_NAMES.put(IDA_MOBILE_ACTIVITY, TableTypes.getNamesArray(IDA_MOBILE_ACTIVITY));
        TABLE_NAMES.put(REFERENCE, TableTypes.getNamesArray(REFERENCE));
        TABLE_NAMES.put(ORL_AREA_NONPOINT_TOXICS, TableTypes.getNamesArray(ORL_AREA_NONPOINT_TOXICS));
        TABLE_NAMES.put(ORL_AREA_NONROAD_TOXICS, TableTypes.getNamesArray(ORL_AREA_NONROAD_TOXICS));
        TABLE_NAMES.put(ORL_MOBILE_TOXICS, TableTypes.getNamesArray(ORL_MOBILE_TOXICS));
        TABLE_NAMES.put(ORL_POINT_TOXICS, TableTypes.getNamesArray(ORL_POINT_TOXICS));

        DESCRIPTIVE_COLUMN_NAMES = new HashMap();
        DESCRIPTIVE_COLUMN_NAMES.put(NIF_AREA, NIF_AREA_COLUMN_NAMES);
        DESCRIPTIVE_COLUMN_NAMES.put(NIF_POINT, NIF_POINT_COLUMN_NAMES);
        DESCRIPTIVE_COLUMN_NAMES.put(NIF_MOBILE_EMISSIONS, NIF_MOBILE_EMISSIONS_COLUMN_NAMES);
    }

    public static boolean isORLDataset(String datasetType) {
        if (datasetType != null)
            return (datasetType.equals(ORL_AREA_NONPOINT_TOXICS) || datasetType.equals(ORL_AREA_NONROAD_TOXICS)
                    || datasetType.equals(ORL_MOBILE_TOXICS) || datasetType.equals(ORL_POINT_TOXICS));
        return false;
    }

    private DatasetTypes() {
    }

    /**
     * Get the valid table types for a given dataset type
     */
    public static String[] getTableTypes(final String datasetType) {
        return (String[]) TABLE_NAMES.get(datasetType);
    }

    public static String getSummaryTableType(final String datasetType) {
        return (String) SUMMARY_TABLE_TYPE.get(datasetType);
    }

}
