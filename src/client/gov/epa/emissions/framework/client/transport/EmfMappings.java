package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.cost.controlStrategy.DoubleValue;

import java.io.File;

import org.apache.axis.client.Call;

public class EmfMappings extends Mappings {

    private DataMappings dataMappings;

    private CaseMappings caseMappings;

    public EmfMappings() {
        dataMappings = new DataMappings();
        caseMappings = new CaseMappings();
    }

    public void register(Call call) {
        caseMappings.register(call);
        dataMappings.register(call);

        bean(call, DoubleValue.class, "DoubleValue");
        array(call, DoubleValue[].class, "DoubleValueArray");

        bean(call, File.class, "File");
        array(call, File[].class, "Files");

        bean(call, EmfFileSystemView.class, "EmfFileSystemView");
        
        array(call, String[].class, strings());
    }

}
