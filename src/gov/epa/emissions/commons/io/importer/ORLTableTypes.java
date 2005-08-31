package gov.epa.emissions.commons.io.importer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ORLTableTypes implements TableTypes {

    public static final TableType ORL_AREA_NONPOINT_TOXICS = new TableType(DatasetTypes.ORL_AREA_NONPOINT_TOXICS,
            new String[] { "ORL Nonpoint Source Toxics" }, "ORL Nonpoint Toxics Summary");

    public static final TableType ORL_AREA_NONROAD_TOXICS = new TableType(DatasetTypes.ORL_AREA_NONROAD_TOXICS,
            new String[] { "ORL Nonroad Source Toxics" }, "ORL Nonroad Toxics Summary");

    public static final TableType ORL_ONROAD_MOBILE_TOXICS = new TableType(DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS,
            new String[] { "ORL Mobile Source Toxics" }, "ORL Mobile Toxics Summary");

    public static final TableType ORL_POINT_TOXICS = new TableType(DatasetTypes.ORL_POINT_TOXICS,
            new String[] { "ORL Point Source Toxics" }, "ORL Point Toxics Summary");

    private List list() {
        List list = new ArrayList();

        list.add(ORL_AREA_NONPOINT_TOXICS);
        list.add(ORL_AREA_NONROAD_TOXICS);
        list.add(ORL_ONROAD_MOBILE_TOXICS);
        list.add(ORL_POINT_TOXICS);

        return list;
    }

    public TableType type(String datasetType) {
        for (Iterator iter = list().iterator(); iter.hasNext();) {
            TableType type = (TableType) iter.next();
            if (type.getDatasetType().equals(datasetType))
                return type;
        }

        return null;
    }

}
