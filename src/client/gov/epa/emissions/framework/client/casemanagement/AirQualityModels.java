package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.casemanagement.AirQualityModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AirQualityModels {

    private List list;

    public AirQualityModels(AirQualityModel[] array) {
        this.list = new ArrayList(Arrays.asList(array));
    }

    public AirQualityModel get(String name) {
        name = name.trim();
        for (int i = 0; i < list.size(); i++) {
            AirQualityModel item = ((AirQualityModel) list.get(i));
            if (item.getName().equalsIgnoreCase(name))
                return item;
        }
        return new AirQualityModel(name);
    }

    public AirQualityModel[] all() {
        return (AirQualityModel[]) list.toArray(new AirQualityModel[0]);
    }

}
