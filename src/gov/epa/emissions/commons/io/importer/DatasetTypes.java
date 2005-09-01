package gov.epa.emissions.commons.io.importer;

public final class DatasetTypes {

    // NOTE & FIXME: the values must match those in the DatasetTypes table.
    // In fact, this class should go away and the DatasetType object (read
    // from the db) must be used.
    public static final String ORL_AREA_NONPOINT_TOXICS = "ORL Nonpoint Inventory";

    public static final String ORL_AREA_NONROAD_TOXICS = "ORL Nonroad Inventory";

    public static final String ORL_ON_ROAD_MOBILE_TOXICS = "ORL Onroad Inventory";

    public static final String ORL_POINT_TOXICS = "ORL Point Inventory";

    public static final String REFERENCE = "Reference";

    public static boolean isORL(String datasetType) {
        if (datasetType != null)
            return (datasetType.equals(ORL_AREA_NONPOINT_TOXICS) || datasetType.equals(ORL_AREA_NONROAD_TOXICS)
                    || datasetType.equals(ORL_ON_ROAD_MOBILE_TOXICS) || datasetType.equals(ORL_POINT_TOXICS));
        return false;
    }

}
