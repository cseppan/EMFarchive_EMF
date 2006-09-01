package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.services.cost.controlStrategy.DoubleValue;

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
    }

}
