package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.io.importer.DatasetTypes;

import java.util.HashMap;
import java.util.Map;

public class ORLBodyFactory {

    private Map bodyMap;

    ORLBodyFactory() {
        bodyMap = new HashMap();

        bodyMap.put(DatasetTypes.ORL_AREA_NONPOINT_TOXICS, new ORLBody(new NonPointFormatterSequence()));
        bodyMap.put(DatasetTypes.ORL_AREA_NONROAD_TOXICS, new ORLBody(new NonRoadFormatterSequence()));
        bodyMap.put(DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS, new ORLBody(new OnRoadMobileFormatterSequence()));
        bodyMap.put(DatasetTypes.ORL_POINT_TOXICS, new ORLBody(new PointFormatterSequence()));
    }

    ORLBody getBody(String datasetType) {
        return (ORLBody) bodyMap.get(datasetType);
    }
}
