package gov.epa.emissions.commons.io.importer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ORLTableType {

    private String datasetType;

    private String summaryType;

    private String[] baseTypes;

    private ORLTableType(String datasetType, String[] baseTypes, String summaryType) {
        this.datasetType = datasetType;
        this.baseTypes = baseTypes;
        this.summaryType = summaryType;
    }

    public static final ORLTableType ORL_AREA_NONPOINT_TOXICS = new ORLTableType(DatasetTypes.ORL_AREA_NONPOINT_TOXICS,
            new String[] { "ORL Nonpoint Source Toxics" }, "ORL Nonpoint Toxics Summary");

    public static final ORLTableType ORL_AREA_NONROAD_TOXICS = new ORLTableType(DatasetTypes.ORL_AREA_NONROAD_TOXICS,
            new String[] { "ORL Nonroad Source Toxics" }, "ORL Nonroad Toxics Summary");

    // FIXME: get consistent w/ naming 'onroad mobile'
    public static final ORLTableType ORL_ONROAD_MOBILE_TOXICS = new ORLTableType(DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS,
            new String[] { "ORL Mobile Source Toxics" }, "ORL Mobile Toxics Summary");

    public static final ORLTableType ORL_POINT_TOXICS = new ORLTableType(DatasetTypes.ORL_POINT_TOXICS,
            new String[] { "ORL Point Source Toxics" }, "ORL Point Toxics Summary");

    private static List list() {
        List list = new ArrayList();

        list.add(ORL_AREA_NONPOINT_TOXICS);
        list.add(ORL_AREA_NONROAD_TOXICS);
        list.add(ORL_ONROAD_MOBILE_TOXICS);
        list.add(ORL_POINT_TOXICS);
        list.add(new ORLTableType(DatasetTypes.REFERENCE, ReferenceTable.types(), null));

        return list;
    }

    public static final ORLTableType type(String datasetType) {
        for (Iterator iter = list().iterator(); iter.hasNext();) {
            ORLTableType element = (ORLTableType) iter.next();
            if (element.datasetType.equals(datasetType))
                return element;
        }

        return null;
    }

    public String getDatasetType() {
        return datasetType;
    }

    public String summaryType() {
        return summaryType;
    }

    public String[] baseTypes() {
        return baseTypes;
    }

}
