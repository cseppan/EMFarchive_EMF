package gov.epa.emissions.commons.io.exporter.orl;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.importer.DatasetTypes;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ORLHeaderWriter {

    /* Header record command fields */
    private static final String COMMAND = "#";

    private static final String ORL_COMMAND = COMMAND + "ORL";

    private static final String TYPE_COMMAND = COMMAND + "TYPE    ";

    private static final String COUNTRY_COMMAND = COMMAND + "COUNTRY ";

    private static final String REGION_COMMAND = COMMAND + "REGION  ";

    private static final String YEAR_COMMAND = COMMAND + "YEAR    ";

    private static final String DESCRIPTION_COMMAND = COMMAND + "DESC    ";

    private static final Map typeHackMap;
    static {
        typeHackMap = new HashMap();
        typeHackMap.put(DatasetTypes.ORL_AREA_NONPOINT_TOXICS, "Non-point Source Inventory");
        typeHackMap.put(DatasetTypes.ORL_AREA_NONROAD_TOXICS, "Non-road Vehicle Emission Inventory");
        typeHackMap.put(DatasetTypes.ORL_ON_ROAD_MOBILE_TOXICS, "On-road Vehicle Emission Inventory");
        typeHackMap.put(DatasetTypes.ORL_POINT_TOXICS, "Point Source Inventory");
    }

    void writeHeader(Dataset dataset, PrintWriter writer) {
    	
    	String OUT_COMMAND = ORL_COMMAND;
    	if (dataset.getDatasetType().equals("ORL Nonpoint Inventory")){
    		OUT_COMMAND = ORL_COMMAND + " NONPOINT";
    	}
        writer.println(OUT_COMMAND);
        String regionMessage = (dataset.getRegion() != null) ? dataset.getRegion() : " Region not found in database";
        writer.println(REGION_COMMAND + regionMessage);

        String countryMessage = (dataset.getCountry() != null) ? dataset.getCountry()
                : " Country not found in database";
        writer.println(COUNTRY_COMMAND + countryMessage);

        writer.println(YEAR_COMMAND
                + ((dataset.getYear() != 0) ? "" + dataset.getYear() : " Year not found in database"));

        String type = (dataset.getDatasetType() != null) ? (String) typeHackMap.get(dataset.getDatasetType())
                : " Dataset Type not found in database";
        writer.println(TYPE_COMMAND + type);

        writeDescription(dataset.getDescription(), writer);
    }

    private void writeDescription(String description, PrintWriter writer) {
        if (description == null || description.length() == 0)
            writer.println(DESCRIPTION_COMMAND + " Description not found in database");

        String[] descriptions = description.split("\\n");
        for (int i = 0; i < descriptions.length; i++) {
            writer.println(DESCRIPTION_COMMAND + descriptions[i]);
        }

    }
}
