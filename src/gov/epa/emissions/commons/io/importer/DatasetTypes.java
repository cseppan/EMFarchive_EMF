package gov.epa.emissions.commons.io.importer;

import java.util.HashMap;
import java.util.Map;

public final class DatasetTypes {

    public static final String ORL_AREA_NONPOINT_TOXICS = "ORL Nonpoint Inventory";

    public static final String ORL_AREA_NONROAD_TOXICS = "ORL Nonroad Inventory";

    public static final String ORL_ON_ROAD_MOBILE_TOXICS = "ORL Onroad Inventory";

    public static final String ORL_POINT_TOXICS = "ORL Point Inventory";

    public static final String REFERENCE = "Reference";

    private static final Map DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP;

    static {
        DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP = new HashMap();
        DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP.put(ORL_AREA_NONPOINT_TOXICS,
                TableTypes.ORL_AREA_NONPOINT_TOXICS_SUMMARY);
        DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP.put(ORL_AREA_NONROAD_TOXICS, TableTypes.ORL_AREA_NONROAD_TOXICS_SUMMARY);
        DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP.put(ORL_ON_ROAD_MOBILE_TOXICS, TableTypes.ORL_MOBILE_TOXICS_SUMMARY);
        DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP.put(ORL_POINT_TOXICS, TableTypes.ORL_POINT_TOXICS_SUMMARY);
    }

    public static boolean isORLDataset(String datasetType) {
        if (datasetType != null)
            return (datasetType.equals(ORL_AREA_NONPOINT_TOXICS) || datasetType.equals(ORL_AREA_NONROAD_TOXICS)
                    || datasetType.equals(ORL_ON_ROAD_MOBILE_TOXICS) || datasetType.equals(ORL_POINT_TOXICS));
        return false;
    }

    public static String[] getTableTypes(final String datasetType) {
        return TableTypes.getTableTypes(datasetType);
    }

    public static String getSummaryTableType(final String datasetType) {
        return (String) DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP.get(datasetType);
    }

}
