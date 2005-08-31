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

    static {// FIXME: analyze usage and refactor ?
        DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP = new HashMap();
        DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP.put(ORL_AREA_NONPOINT_TOXICS, ORLTableType.ORL_AREA_NONPOINT_TOXICS
                .summaryType());

        DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP.put(ORL_AREA_NONROAD_TOXICS, ORLTableType.ORL_AREA_NONROAD_TOXICS
                .summaryType());
        DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP.put(ORL_ON_ROAD_MOBILE_TOXICS, ORLTableType.ORL_ONROAD_MOBILE_TOXICS
                .summaryType());
        DATASET_TYPE_TO_SUMMARY_TABLE_TYPE_MAP.put(ORL_POINT_TOXICS, ORLTableType.ORL_POINT_TOXICS.summaryType());
    }

    public static boolean isORLDataset(String datasetType) {
        if (datasetType != null)
            return (datasetType.equals(ORL_AREA_NONPOINT_TOXICS) || datasetType.equals(ORL_AREA_NONROAD_TOXICS)
                    || datasetType.equals(ORL_ON_ROAD_MOBILE_TOXICS) || datasetType.equals(ORL_POINT_TOXICS));
        return false;
    }

}
