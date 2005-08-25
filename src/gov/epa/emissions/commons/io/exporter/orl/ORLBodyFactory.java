package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.io.importer.DatasetTypes;

import java.util.HashMap;
import java.util.Map;

public class ORLBodyFactory {

    private Map bodyMap;

    ORLBodyFactory() {
        bodyMap = new HashMap();

        bodyMap.put(DatasetTypes.ORL_AREA_NONPOINT_TOXICS, new NonPointBody());
        bodyMap.put(DatasetTypes.ORL_AREA_NONROAD_TOXICS, new NonRoadBody());
        bodyMap.put(DatasetTypes.ORL_ON_ROAD_TOXICS, new MobileBody());
        bodyMap.put(DatasetTypes.ORL_POINT_TOXICS, new PointBody());
    }

    ORLBody getBody(String datasetType) {
        return (ORLBody) bodyMap.get(datasetType);
    }
}
